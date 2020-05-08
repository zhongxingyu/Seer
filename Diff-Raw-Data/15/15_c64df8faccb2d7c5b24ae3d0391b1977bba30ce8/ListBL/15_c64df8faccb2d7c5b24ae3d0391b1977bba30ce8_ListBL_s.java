 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2008 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * Created on Nov 26, 2004
  *
  */
 package com.sapienter.jbilling.server.list;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import javax.ejb.CreateException;
 import javax.ejb.FinderException;
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 
 import sun.jdbc.rowset.CachedRowSet;
 
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.common.Util;
 import com.sapienter.jbilling.server.customer.CustomerSQL;
 import com.sapienter.jbilling.server.invoice.InvoiceSQL;
 import com.sapienter.jbilling.server.list.db.ListDAS;
 import com.sapienter.jbilling.server.list.db.ListDTO;
 import com.sapienter.jbilling.server.list.db.ListFieldDAS;
 import com.sapienter.jbilling.server.list.db.ListFieldDTO;
 import com.sapienter.jbilling.server.order.OrderSQL;
 import com.sapienter.jbilling.server.payment.PaymentSQL;
 import com.sapienter.jbilling.server.user.db.CompanyDAS;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.util.Constants;
 
 /**
  * @author Emil
  * 
  */
 public class ListBL {
     private static final String CUSTOM_PREFIX = "CUSTOM_";
     private JNDILookup EJBFactory = null;
     private ListDAS listDas = null;
     private ListDTO list = null;
     private static final Logger LOG = Logger.getLogger(ListBL.class);;
     private Hashtable parameters = null;
 
     public ListBL(Integer listId) throws NamingException {
         init();
         set(listId);
     }
 
     public ListBL() throws NamingException {
         init();
     }
 
     public ListBL(ListDTO list) throws NamingException {
         init();
         set(list);
     }
 
     private void init() throws NamingException {
         EJBFactory = JNDILookup.getFactory(false);
         listDas = new ListDAS();
     }
 
     public ListDTO getEntity() {
         return list;
     }
 
     public ListDAS getHome() {
         return listDas;
     }
 
     public void set(Integer id) {
         list = listDas.find(id);
     }
 
     public void set(ListDTO list) {
         this.list = list;
     }
 
     public void set(String legacyName) {
         list = listDas.findByName(legacyName);
     }
 
     public void updateStatistics() throws SessionInternalError {
         // for the statistics, the only relevant parameters is the entityId
         // still they have to be there for thw sql to run
         parameters = new Hashtable();
         parameters.put("userType", new Integer(2)); // run them all as root
         parameters.put("userId", new Integer(3)); // just a root user
         parameters.put("languageId", new Integer(1)); // english will do
 
         // go through all the lists
         try {
             // make the counting
             Connection conn = EJBFactory.lookUpDataSource().getConnection();
 
             for (Iterator listsIt = listDas.findAll().iterator(); listsIt
                     .hasNext();) {
                 list = (ListDTO) listsIt.next();
                 // now for each entity
                 for (Iterator entityIt = new CompanyDAS().findEntities()
                         .iterator(); entityIt.hasNext();) {
                     CompanyDTO anEntity = (CompanyDTO) entityIt.next();
 
                     parameters.put("entityId", anEntity.getId());
                     PreparedStatement stmt = conn
                             .prepareStatement(getCountSQL());
                     setSQLParameters(stmt, null);
                     ResultSet res = stmt.executeQuery();
                     res.next();
                     Integer count = new Integer(res.getInt(1));
 
                     // update/create the entity record for this list
                     ListEntityBL listEntityBl = new ListEntityBL();
                     listEntityBl.set(list.getId(), anEntity.getId());
                     
                     if (getEntity() == null) {
                         // this entity has no records for this list
                         // create one
                         listEntityBl.create(list, anEntity.getId(), count);
                     } else {
                         listEntityBl.update(count);    
                     }
 
                     // now update each of the fields
                     int column = 2; // skip the count
                     for (Iterator fieldIt = list.getListFields().iterator(); fieldIt
                             .hasNext();) {
                         ListFieldDTO field = (ListFieldDTO) fieldIt.next();
                         if (field.getOrdenable().intValue() == 1) {
                             Integer min = null, max = null;
                             String minStr = null, maxStr = null;
                             Date minDate = null, maxDate = null;
 
                             if (field.getDataType().equals("integer")) {
                                 min = new Integer(res.getInt(column++));
                                 max = new Integer(res.getInt(column++));
                             } else if (field.getDataType().equals("string")) {
                                 minStr = res.getString(column++);
                                 maxStr = res.getString(column++);
                             } else if (field.getDataType().equals("date")) {
                                 minDate = res.getDate(column++);
                                 maxDate = res.getDate(column++);
                             }
                             ListFieldEntityBL fieldEntityBl = new ListFieldEntityBL();
                             fieldEntityBl.createUpdate(field, listEntityBl
                                     .getEntity(), min, max, minStr, maxStr,
                                     minDate, maxDate);
                         }
                     }
 
                     res.close();
                     stmt.close();
                 }
             }
 
             conn.close();
         } catch (NamingException e2) {
             throw new SessionInternalError(e2);
         } catch (CreateException e2) {
             throw new SessionInternalError(e2);
         } catch (SQLException e3) {
             throw new SessionInternalError(e3);
         }
     }
 
     /**
      * I need this kind of hack to reuse the exiting SQL code written for 21
      * lists. Otherwise I'd have to move it all to the DB.
      * 
      * @param listId
      * @return
      */
     private String getSQL() {
         String retValue = null;
 
         // these are very common parameters
         Integer entityId = (Integer) parameters.get("entityId");
         Integer userType = (Integer) parameters.get("userType");
         Integer userId = (Integer) parameters.get("userId");
         Integer languageId = (Integer) parameters.get("languageId");
 
         switch (list.getId()) {
         case 1: // CUSTOMERS - standards
             if (userType.equals(Constants.TYPE_INTERNAL)
                     || userType.equals(Constants.TYPE_ROOT)
                     || userType.equals(Constants.TYPE_CLERK)) {
                 retValue = CustomerSQL.listCustomers;
             } else if (userType.equals(Constants.TYPE_PARTNER)) {
                 // partners only see their own
                 retValue = CustomerSQL.listPartner;
             }
             break;
         case 2: // INVOICES - standard
             if (userType.equals(Constants.TYPE_ROOT)
                     || userType.equals(Constants.TYPE_CLERK)) {
                 retValue = InvoiceSQL.rootClerkList;
             } else if (userType.equals(Constants.TYPE_PARTNER)) {
                 retValue = InvoiceSQL.partnerList;
             } else if (userType.equals(Constants.TYPE_CUSTOMER)) {
                 retValue = InvoiceSQL.customerList;
             }
             break;
         case 3: // ORDERS - standard
             if (userType.equals(Constants.TYPE_ROOT)
                     || userType.equals(Constants.TYPE_CLERK)) {
                 retValue = OrderSQL.listInternal;
             } else if (userType.equals(Constants.TYPE_PARTNER)) {
                 retValue = OrderSQL.listPartner;
             } else if (userType.equals(Constants.TYPE_CUSTOMER)) {
                 retValue = OrderSQL.listCustomer;
             }
             break;
         case 4: // PAYMENTS - standard
             if (userType.equals(Constants.TYPE_ROOT)
                     || userType.equals(Constants.TYPE_CLERK)) {
                 retValue = PaymentSQL.rootClerkList;
             } else if (userType.equals(Constants.TYPE_PARTNER)) {
                 retValue = PaymentSQL.partnerList;
             } else if (userType.equals(Constants.TYPE_CUSTOMER)) {
                 retValue = PaymentSQL.customerList;
             }
             break;
         }
 
         return retValue;
     }
 
     private int setSQLParameters(PreparedStatement stmt, Integer from)
             throws SQLException {
         int index = 1;
 
         // these are very common parameters
         Integer entityId = (Integer) parameters.get("entityId");
         Integer userType = (Integer) parameters.get("userType");
         Integer userId = (Integer) parameters.get("userId");
         Integer languageId = (Integer) parameters.get("languageId");
 
         switch (list.getId()) {
         case 1: // CUSTOMERS - standards
             stmt.setInt(index++, entityId.intValue());
             if (userType.equals(Constants.TYPE_PARTNER)) {
                 stmt.setInt(index++, userId.intValue());
             }
             break;
         case 2: // INVOICES - standard
             if (userType.equals(Constants.TYPE_ROOT)
                     || userType.equals(Constants.TYPE_CLERK)) {
                 stmt.setInt(index++, entityId.intValue());
             } else if (userType.equals(Constants.TYPE_PARTNER)) {
                 stmt.setInt(index++, entityId.intValue());
                 stmt.setInt(index++, userId.intValue());
             } else if (userType.equals(Constants.TYPE_CUSTOMER)) {
                 stmt.setInt(index++, userId.intValue());
             }
 
             break;
         case 3:// ORDER - standard
             if (userType.equals(Constants.TYPE_ROOT)
                     || userType.equals(Constants.TYPE_CLERK)) {
                 stmt.setInt(index++, entityId.intValue());
             } else if (userType.equals(Constants.TYPE_PARTNER)) {
                 stmt.setInt(index++, entityId.intValue());
                 stmt.setInt(index++, userId.intValue());
             } else if (userType.equals(Constants.TYPE_CUSTOMER)) {
                 stmt.setInt(index++, userId.intValue());
             }
             break;
         case 4: // PAYMENTS - standard
             if (userType.equals(Constants.TYPE_ROOT)
                     || userType.equals(Constants.TYPE_CLERK)) {
                 stmt.setInt(index++, 0); // it is for payments, not refunds
                 stmt.setInt(index++, entityId.intValue());
                 stmt.setInt(index++, languageId.intValue());
             } else if (userType.equals(Constants.TYPE_PARTNER)) {
                 stmt.setInt(index++, 0); // it is for payments, not refunds
                 stmt.setInt(index++, entityId.intValue());
                 stmt.setInt(index++, userId.intValue());
                 stmt.setInt(index++, languageId.intValue());
             } else if (userType.equals(Constants.TYPE_CUSTOMER)) {
                 stmt.setInt(index++, 0); // it is for payments, not refunds
                 stmt.setInt(index++, userId.intValue());
                 stmt.setInt(index++, languageId.intValue());
             }
             break;
         }
 
         if (from != null) {
             stmt.setInt(index++, from.intValue());
         }
 
         return index;
     }
 
     private String createHeader() {
         StringBuffer header = new StringBuffer();
         header.append("select count(*) ");
 
         // now go through each field
         for (Iterator it = list.getListFields().iterator(); it.hasNext();) {
             ListFieldDTO field = (ListFieldDTO) it.next();
             if (field.getOrdenable().intValue() == 1) {
                 header.append(", min(" + field.getColumnName() + ")");
                 header.append(", max(" + field.getColumnName() + ")");
             }
         }
         header.append(' ');
 
         return header.toString();
     }
 
     private String getCountSQL() {
         // first, get the normal SQL
         String sql = getSQL();
         // remove the select
         sql = sql.substring(sql.indexOf("from"));
         // add the count select
         sql = createHeader() + sql;
 
         return sql;
     }
 
     public CachedRowSet getPage(Integer start, Integer end, int size,
             Integer listId, Integer entityId, boolean direction,
             Integer fieldId, Hashtable parameters) throws SQLException,
             NamingException, SessionInternalError {
         this.parameters = parameters;
         // start by getting a connection to the db
         Connection conn = EJBFactory.lookUpDataSource().getConnection();
         PreparedStatement stmt = null;
         int parameterIndex = -1;
 
         set(listId);
         /*
          * Find the right range to do the query, by counting with estimates
          * based on the stats
          */
         StringBuffer sql = new StringBuffer();
         sql.append("select count(*) ");
 
         // append the original sql
         String orgSql = getSQL();
         sql.append(orgSql.substring(orgSql.indexOf("from")));
 
         // and the from limit is it is there
         ListFieldBL field = new ListFieldBL(fieldId);
         if (start != null) {
             sql.append(" and " + field.getEntity().getColumnName());
             if (direction) {
                 sql.append(" > ");
             } else {
                 sql.append(" < ");
             }
             sql.append("? ");
         }
 
         // calculate the cof based on the statistics
         boolean hasStats = true;
         long cof = 0;
         long limit = 0;
         long min = 0;
         long max = 0;
         if (end == null) { // we'll try to guess it from the stats
             ListEntityBL listEntityBl = new ListEntityBL();
             listEntityBl.set(listId, entityId);
             if (listEntityBl.getEntity() == null || listEntityBl.getEntity().getTotalRecords() == 0) {
                 // if counted 0, it is like not having any statistics
                 hasStats = false;
 
             } else {
 
                 ListFieldEntityBL fieldEntity = new ListFieldEntityBL();
                 fieldEntity.set(fieldId, listEntityBl.getEntity().getId());
 
                 if (field.getEntity().getDataType().equals("integer")) {
                     min = fieldEntity.getEntity().getMinValue().longValue();
                     max = fieldEntity.getEntity().getMaxValue().longValue();
                     cof = (max + 1 - min) / listEntityBl.getEntity().getTotalRecords();
                     cof *= size;
                     if (cof <= 0) {
                         LOG.error("cof is " + cof);
                         hasStats = false;
                     } else {
                         if (start != null) {
                             limit = start.longValue();
                         } else {
                             if (direction) {
                                 limit = min;
                             } else {
                                 limit = max;
                             }
                         }
                     }
                 } else if (field.getEntity().getDataType().equals("string")) {
 
                 } else if (field.getEntity().getDataType().equals("date")) {
 
                 }
             }
 
         }
 
         // if there is an end value
         if (hasStats || end != null) {
             sql.append(" and " + field.getEntity().getColumnName());
             if (direction) {
                 sql.append(" <= ");
             } else {
                 sql.append(" >= ");
             }
             sql.append("? ");
         }
 
         // start the attempts to find the minimum span
         // only if no concrete end has been provided and stats are available
         boolean lastPage = false;
         if (end == null && hasStats) {
             // the basic query is ready
             stmt = conn.prepareStatement(sql.toString());
             // get the basic values replaced
             parameterIndex = setSQLParameters(stmt, start);
             int attempts = 0;
             long count = 0;
             while (count < size && !lastPage) {
                 if (field.getEntity().getDataType().equals("integer")) {
                     if ((direction && limit > max)
                             || (!direction && limit < min)) {
                         // this is probably the last page. Remove the limit to
                         // avoid skipping any new rows
                         sql.delete(sql.lastIndexOf("and"), sql.length());
                         stmt = conn.prepareStatement(sql.toString());
                         parameterIndex = setSQLParameters(stmt, start);
                         lastPage = true;
                     } else {
                         if (direction) {
                             limit += cof;
                         } else {
                             limit -= cof;
                         }
                         stmt.setLong(parameterIndex, limit);
                     }
                 } else {
                     throw new SessionInternalError(
                             "Unsupported field data type "
                                     + field.getEntity().getDataType());
                 }
                 ResultSet res = stmt.executeQuery();
                 res.next();
                 count = res.getLong(1);
                 attempts++;
                 cof *= 2;
             }
 
             LOG.debug("Done pageing. list " + listId + " entity " + entityId
                     + " attempts " + attempts + " cof " + cof + " needed "
                     + size + " got " + count);
         }
         // done, cook the final sql
         // replace the count by the original select
         sql.replace(0, sql.indexOf("from"), orgSql.substring(0, orgSql
                 .indexOf("from")));
         // add the order by
         sql.append(" order by " + field.getEntity().getColumnName());
         if (!direction) {
             sql.append(" desc");
         }
 
         // run it
         CachedRowSet results = new CachedRowSet();
         stmt = conn.prepareStatement(sql.toString());
         stmt.setMaxRows(size);
         parameterIndex = setSQLParameters(stmt, start);
 
         if (end != null) {
             stmt.setLong(parameterIndex, end.intValue());
         } else {
             if (hasStats && !lastPage) {
                 if (field.getEntity().getDataType().equals("integer")) {
                     stmt.setLong(parameterIndex, limit);
                 }
             }
         }
 
         // log.debug("query = " + sql);
         ResultSet res = stmt.executeQuery();
         results.populate(res);
 
         // close the connection
         res.close();
         stmt.close();
         conn.close();
 
         return results;
     }
 
     public CachedRowSet search(String start, String end, Integer fieldId,
             Integer listId, Integer entityId, Hashtable parameters)
             throws FinderException, SQLException, NamingException {
         this.parameters = parameters;
         StringBuffer sql = new StringBuffer();
         set(listId);
         // start by getting a connection to the db
         Connection conn = EJBFactory.lookUpDataSource().getConnection();
         // get the original SQL
         sql.append(getSQL());
         // add the where from the parameters
         ListFieldBL field = new ListFieldBL(fieldId);
         String columnName = field.getEntity().getColumnName();
         if (columnName.startsWith(CUSTOM_PREFIX)) {
             sql.append(getCustomFilter(columnName.substring(CUSTOM_PREFIX
                     .length())));
         } else {
             sql.append(" and " + field.getEntity().getColumnName());
             if (field.getEntity().getDataType().equals("integer")) {
                 // it is just an equal
                 sql.append(" = ?");
             } else if (field.getEntity().getDataType().equals("string")) {
                 // it is a like
                 sql.append(" like ?");
             } else if (field.getEntity().getDataType().equals("date")) {
                 // it is a date range
                 if (start != null) {
                     sql.append(" >= ?");
                     if (end != null) {
                         sql.append(" and " + field.getEntity().getColumnName());
                     }
                 }
                 if (end != null) {
                     sql.append(" < ?");
                 }
             }
 
             // and a courtesy order
             sql.append(" order by ");
             sql.append(field.getEntity().getColumnName());
         }
 
         PreparedStatement stmt = conn.prepareStatement(sql.toString());
         int varIndex = setSQLParameters(stmt, null);
 
         // set the values of the where
         if (field.getEntity().getDataType().equals("integer")) {
             // it is just an equal
             stmt.setInt(varIndex++, Integer.valueOf(start).intValue());
         } else if (field.getEntity().getDataType().equals("string")) {
             stmt.setString(varIndex++, '%' + start + '%');
         } else if (field.getEntity().getDataType().equals("date")) {
             // it is a date range
             if (start != null) {
                stmt.setString(varIndex++, start);
             }
             if (end != null) {
                 // we need to move it one day ahead to make it inclusive
                 Calendar cal = Calendar.getInstance();
                 cal.setTime(Util.parseDate(end));
                 cal.add(Calendar.DATE, 1);
 
                 stmt.setDate(varIndex++, new java.sql.Date(cal.getTime()
                         .getTime()));
             }
         }
 
         // execute
         stmt.setMaxRows(500); // at no time we want more than 500 rows returned
         ResultSet res = stmt.executeQuery();
         CachedRowSet result = new CachedRowSet();
         result.populate(res);
 
         // close the connection
         res.close();
         stmt.close();
         conn.close();
 
         return result;
     }
 
     /**
      * Map a key to a sql string for a WHERE clause that isn't a simple column
      * in the query
      * 
      * @param key
      * @return
      */
     private String getCustomFilter(String key) {
         if (key.equals("CC")) {
             return CustomerSQL.listCustomersCCFiler;
         } else {
             throw new SessionInternalError("Custom filer " + key
                     + " not supported");
         }
     }
 
     /*
      * private Object convertData(String data, String type) throws
      * SessionInternalError { if (type.equals("integer")) { return
      * Integer.valueOf(data); } else if (type.equals("string")) { return data; }
      * else if (type.equals("date")) { // the client will have to pass the
      * milliseconds long mils = Integer.valueOf(data).longValue(); return new
      * Date(mils); } else { throw new SessionInternalError("Invalid data type "
      * + type); } }
      */
 
 }
