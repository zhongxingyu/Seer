 /*
  * OrderDAO.java
  * StockPlay - Abastracte Data access object laag voor de orders
  *
  * Copyright (c) 2010 StockPlay development team
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.kapti.data.persistence.oracle;
 
 import com.kapti.exceptions.*;
 import com.kapti.data.*;
 import com.kapti.data.persistence.GenericDAO;
 import com.kapti.filter.Filter;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Collection;
 
 public class OrderDAO implements GenericDAO<Order, Integer> {
     //
     // Member data
     //
 
     private static final String SELECT_ORDER_LASTID = "select orderid_seq.currval from dual";
     private static final String SELECT_ORDER = "SELECT userid, isin, limit, amount, type, status, creationtime, expirationtime, executiontime, secondairylimit FROM orders WHERE id = ?";
     private static final String SELECT_ORDERS = "SELECT id, userid, isin, limit, amount, type, status, creationtime, expirationtime, executiontime, secondairylimit FROM orders WHERE ((status <> 'CANCELLED') or (status='CANCELLED' and  sysdate-creationtime < NUMTODSINTERVAL(7, 'day')))";
     private static final String INSERT_ORDER = "INSERT INTO orders(id, userid, isin, limit, amount, type, status, creationtime, expirationtime, executiontime, secondairylimit) VALUES(orderid_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
     private static final String UPDATE_ORDER = "UPDATE orders SET userid = ?, isin = ?, limit = ?, amount = ?, type = ?, status = ?, creationtime = ?, expirationtime = ?, executiontime = ?, secondairylimit = ? WHERE id = ?";
     private static final String DELETE_ORDER = "DELETE FROM orders WHERE id = ?";
 
 
     //
     // Construction
     //
     
     private static OrderDAO instance = new OrderDAO();
 
     private OrderDAO() {
     }
 
     public static OrderDAO getInstance() {
         return instance;
     }
 
     //
     // Methods
     //
 
     public Order findById(Integer id) throws StockPlayException {
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {
             try {
                 conn = OracleConnection.getConnection();
                 stmt = conn.prepareStatement(SELECT_ORDER);
 
                 stmt.setInt(1, id);
 
                 rs = stmt.executeQuery();
                 if (rs.next()) {
 
                     Order o = new Order(id, rs.getInt(1), rs.getString(2));
                     o.setPrice(rs.getDouble(3));
                     o.setAmount(rs.getInt(4));
                     // type op 5
                     o.setType(InstructionType.valueOf(rs.getString(5).toUpperCase()));
                     //status op 6
                     o.setStatus(OrderStatus.valueOf(rs.getString(6).toUpperCase()));
                     o.setCreationTime(rs.getTimestamp(7));
                     o.setExpirationTime(rs.getTimestamp(8));
                     o.setExecutionTime(rs.getTimestamp(9));
                     o.setSecondairyLimit(rs.getDouble(10));
 
                     return o;
 
                 } else {
                     throw new InvocationException(InvocationException.Type.NON_EXISTING_ENTITY, "There is no index with id '" + id + "'");
                 }
             } finally {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException ex) {
             throw new DBException(ex);
         }
     }
 
     public Collection<Order> findByFilter(Filter iFilter) throws StockPlayException, FilterException {
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {
             try {
                 conn = OracleConnection.getConnection();
 
                 StringBuilder tQuery = new StringBuilder(SELECT_ORDERS);
                 if (!iFilter.empty())
                     tQuery.append(" AND (" + (String)iFilter.compile("sql") + ")");
                 stmt = conn.prepareStatement(tQuery.toString());
 
                 rs = stmt.executeQuery();
                 ArrayList<Order> list = new ArrayList<Order>();
                 while (rs.next()) {
                     Order o = new Order(rs.getInt(1), rs.getInt(2), rs.getString(3));
                     o.setPrice(rs.getDouble(4));
                     o.setAmount(rs.getInt(5));
                     // type op 5
                     o.setType(InstructionType.valueOf(rs.getString(6).toUpperCase()));
                     //status op 6
                     o.setStatus(OrderStatus.valueOf(rs.getString(7).toUpperCase()));
                     o.setCreationTime(rs.getTimestamp(8));
                     o.setExpirationTime(rs.getTimestamp(9));
                     o.setExecutionTime(rs.getTimestamp(10));
                     o.setSecondairyLimit(rs.getDouble(11));
 
                     list.add(o);
                 }
                 return list;
             } finally {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException ex) {
             throw new DBException(ex);
         }
 
     }
 
     public Collection<Order> findAll() throws StockPlayException {
         return findByFilter(new Filter());
     }
 
     /**
      * Maakt de opgegeven index aan in de database. De id van het object wordt genegeerd, en er wordt door de database mbv. een sequence een uniek nummer gecreërd.
      * @param entity Het object dat moet worden aangemaakt in de database
      * @return 
      * @throws StockPlayException
      */
     public int create(Order entity) throws StockPlayException {
         Connection conn = null;
         PreparedStatement stmt = null;
         PreparedStatement stmtID =null;
         ResultSet rs = null;
         try {
             try {
                 conn = OracleConnection.getConnection();
                 conn.setAutoCommit(false);
                 stmt = conn.prepareStatement(INSERT_ORDER);
 
                 stmt.setInt(1, entity.getUser());
                 stmt.setString(2, entity.getIsin());
                 stmt.setDouble(3, entity.getPrice());
                 stmt.setInt(4, entity.getAmount());
                 stmt.setString(5, entity.getType().toString());
 
                 stmt.setString(6, entity.getStatus().toString());
                 stmt.setTimestamp(7, new Timestamp(entity.getCreationTime().getTime()));
                 if (entity.getExpirationTime() != null) {
                     stmt.setTimestamp(8, new Timestamp(entity.getExpirationTime().getTime()));
                 } else {
                     stmt.setNull(8, Types.TIMESTAMP);
                 }
 
                 if (entity.getExecutionTime() != null) {
                     stmt.setTimestamp(9, new Timestamp(entity.getExpirationTime().getTime()));
                 } else {
                     stmt.setNull(9, Types.TIMESTAMP);
                 }
                 stmt.setDouble(10, entity.getSecondairyLimit());
 
                 if(stmt.executeUpdate() == 1){
                     stmtID = conn.prepareStatement(SELECT_ORDER_LASTID);
 
                     rs = stmtID.executeQuery();
                     if(rs.next()){
                         conn.commit();
                         conn.setAutoCommit(true);
                         return rs.getInt(1);
                     }else{
                         return -1;
                     }
                 }else{
                     return -1;
                 }
 
 
             } finally {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
                 if(stmtID != null){
                     stmtID.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException ex) {
             throw new DBException(ex);
         }
     }
 
     /**
      * Past de Order met de opgegeven id aan in de database.
      * @param entity Het object dat moet worden aangemaakt in de database
      * @return
      * @throws StockPlayException
      */
     public boolean update(Order entity) throws StockPlayException {
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {
             try {
                 conn = OracleConnection.getConnection();
                 stmt = conn.prepareStatement(UPDATE_ORDER);
 
                stmt.setInt(11, entity.getId());
                 stmt.setInt(1, entity.getUser());
                 stmt.setString(2, entity.getIsin());
                 stmt.setDouble(3, entity.getPrice());
                 stmt.setInt(4, entity.getAmount());
                 stmt.setString(5, entity.getType().toString());
                 stmt.setString(6, entity.getStatus().toString());
                 stmt.setTimestamp(7, new Timestamp(entity.getCreationTime().getTime()));
                 if (entity.getExpirationTime() != null) {
                     stmt.setTimestamp(8, new Timestamp(entity.getExpirationTime().getTime()));
                 } else {
                     stmt.setNull(8, Types.TIMESTAMP);
                 }
 
                 if (entity.getExecutionTime() != null) {
                     stmt.setTimestamp(9, new Timestamp(entity.getExpirationTime().getTime()));
                 } else {
                     stmt.setNull(9, Types.TIMESTAMP);
                 }
                 stmt.setDouble(10, entity.getSecondairyLimit());
 
                 return stmt.executeUpdate() == 1;
 
 
             } finally {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException ex) {
             throw new DBException(ex);
         }
     }
 
     /**
      * Verwijdert de index met de id van het object uit de database.
      * @param entity Enkel de Id van het object is van belang
      * @return True als het verwijderen van de index gelukt is.
      * @throws StockPlayException
      */
     public boolean delete(Order entity) throws StockPlayException {
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {
             try {
                 conn = OracleConnection.getConnection();
                 stmt = conn.prepareStatement(DELETE_ORDER);
 
                 stmt.setInt(1, entity.getId());
 
                 return stmt.executeUpdate() == 1;
 
 
             } finally {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException ex) {
             throw new DBException(ex);
         }
     }
 }
