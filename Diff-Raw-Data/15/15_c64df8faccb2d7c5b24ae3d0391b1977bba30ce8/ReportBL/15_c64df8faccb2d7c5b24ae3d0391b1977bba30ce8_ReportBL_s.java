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
 
 package com.sapienter.jbilling.server.report;
 
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.apache.log4j.Logger;
 
 import sun.jdbc.rowset.CachedRowSet;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.list.ResultList;
 import com.sapienter.jbilling.server.report.db.ReportDAS;
 import com.sapienter.jbilling.server.report.db.ReportDTO;
 import com.sapienter.jbilling.server.report.db.ReportFieldDAS;
 import com.sapienter.jbilling.server.report.db.ReportFieldDTO;
 import com.sapienter.jbilling.server.report.db.ReportTypeDAS;
 import com.sapienter.jbilling.server.report.db.ReportUserDAS;
 import com.sapienter.jbilling.server.report.db.ReportUserDTO;
 import com.sapienter.jbilling.server.user.UserBL;
 import com.sapienter.jbilling.server.user.db.CompanyDAS;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.util.DTOFactory;
 import com.sapienter.jbilling.server.util.Util;
 
 
 public class ReportBL extends ResultList {
     
     private static final Logger LOG = Logger.getLogger(ReportBL.class);
     ReportUserDAS reportUserDas = null;
     ReportUserDTO report = null;
     ReportDAS reportDas = null;
     ReportFieldDAS reportFieldDas = null;
     ReportTypeDAS reportTypeDas = null;
 
     
     public ReportBL() {
 
         reportUserDas = new ReportUserDAS();
 
         reportDas = new ReportDAS();
 
         reportFieldDas = new ReportFieldDAS();
 
         reportTypeDas = new ReportTypeDAS();
 
     }
     
     private String parseSQL(ReportDTOEx report) 
             throws SessionInternalError {
         StringBuffer select = new StringBuffer("select ");
         StringBuffer from = new StringBuffer("from " + report.getTablesList());
         StringBuffer where = new StringBuffer("where " + report.getWhereStr());
         StringBuffer orderBy = new StringBuffer("order by ");
         StringBuffer groupBy = new StringBuffer("group by ");
         String selectSeparator = "";
         String whereSeparator;
         String orderBySeparator = "";
         String groupBySeparator = "";
         String columnHelper[] = new String[report.getFields().size()];
         int orderedColumns = 0;
         boolean isGrouped = false;
         int firstField = 0; // normaly 0, but if is agregated and 
                             // with id column it has to skip the first
         
         // the first column has to be always the id column, due to the
         // insertRowTag. For agregated reports or those that don't have one,
         // a static 1 is added.
         if (report.getIdColumn() != 1 || 
                 report.getAgregated().booleanValue()) {
             select.append("1,"); 
         }
         
         // reports with id column that are run agregated have to loose their
         // id column
         firstField = report.getFirstFieldIndex();
         
         for (int f = firstField; f < report.getFields().size(); f++) {
             Field field = (Field) report.getFields().get(f);
             
             // add the column to the select 
             if (field.getIsShown().intValue() == 1) {
                 
                 if (field.getFunctionName() != null) {
                     columnHelper[f] = field.getFunctionName() + "(" +
                             field.getTableName() + "." + field.getColumnName() + ")";
                 } else {
                     columnHelper[f] = field.getTableName() + "." + 
                             field.getColumnName();
                 }
                 select.append(selectSeparator + columnHelper[f]);
                 selectSeparator = ",";
             }
             
             // add the condition to the where
             if (report.getWhereStr() != null &&
                     report.getWhereStr().length() > 0) {
                 whereSeparator = " and ";
             } else {
                 whereSeparator = "";
             }
             if (field.getWhereValue() != null &&
                     field.getWhereValue().length() > 0) {
                 // now we can only state the where, we can't put the value
                 // or we would clogg the database cache
                 if (field.getDataType().equals(Field.TYPE_INTEGER) &&
                         field.getWhereValue().indexOf(',') >= 0) {
                     String oper;
                     if (field.getOperatorValue().equals(Field.OPERATOR_EQUAL)) {
                         oper = " in";
                     } else if (field.getOperatorValue().equals(
                             Field.OPERATOR_DIFFERENT)) {
                         oper = " not in";
                     } else {
                         throw new SessionInternalError("operator mismatch for " +
                                 "null value");
                     }
                     where.append(whereSeparator + field.getTableName() + "." + 
                             field.getColumnName() + oper + "(");
                     StringTokenizer tok = new StringTokenizer(
                             field.getWhereValue().toString(), ",");
                     for (int ff = 0; ff < tok.countTokens(); ff++) {
                         where.append("?");
                         if (ff + 1 < tok.countTokens()) {
                             where.append(",");
                         }
                     }
                     where.append(")");
                 } else {
                     String oper = field.getOperatorValue();
                     if (field.getWhereValue().equalsIgnoreCase("null")) {
                         
                         if (field.getOperatorValue().equals(Field.OPERATOR_EQUAL)) {
                             oper = "is";
                         } else if (field.getOperatorValue().equals(
                                 Field.OPERATOR_DIFFERENT)) {
                             oper = "is not";
                         } else {
                             throw new SessionInternalError("operator mismatch for " +
                                     "null value");
                         }
                     } 
                     where.append(whereSeparator + field.getTableName() + "." + 
                             field.getColumnName() + " " +
                             oper + " ?");
                 }
                 whereSeparator = " and ";
             }
             
             // the order can only be marked, as it has to follow a particular order
             if (field.getOrderPosition() != null) {
                 orderedColumns++;
             }
             
             // the group by
             if (field.getIsGrouped().intValue() == 1) {
                 isGrouped = true;
                 groupBy.append(groupBySeparator + field.getTableName() + "." + 
                         field.getColumnName());
                 groupBySeparator = ",";
             }
         }
         
         // now take care of the order by
         orderBySeparator = "";
         if (orderedColumns > 0) {
             for (int done = 1; done <= orderedColumns; done++) {
                 int f;
                 for (f = 0; f < report.getFields().size(); f++) {
                     Field field = (Field) report.getFields().get(f);
                     if (field.getOrderPosition() != null && 
                             field.getOrderPosition().intValue() == done) {
                         String orderByStr = null;
                         if (field.getIsShown().intValue() == 1) {
                             orderByStr = columnHelper[f];
                         } else {
                             orderByStr = field.getTableName() + "." +
                                     field.getColumnName();
                         }
                         LOG.debug("Adding to orderby " + orderBySeparator + orderByStr);
                         orderBy.append(orderBySeparator + orderByStr);    
                         orderBySeparator = ",";
                         break;
                     }
                 }
                 if (f >= report.getFields().size()) {
                     throw new SessionInternalError("The ordered fields are" +
                             "inconsistent. Can't find field " + done);
                 }            
             }       
         }
 
         // construct the query string
         // first the select - from - where        
         StringBuffer completeQuery = new StringBuffer(select + " " + from + 
                 " " + where);  
         // then the group by 
         if (isGrouped) {
             completeQuery.append(" " + groupBy);
         }
         // last the order by 
         if (orderedColumns > 0) {
             completeQuery.append(" " + orderBy);
         }
         LOG.debug("Generated:[" + completeQuery.toString() + "]");
         return completeQuery.toString();
     }
     
     protected CachedRowSet execute(ReportDTOEx reportDto) 
             throws SQLException, SessionInternalError,
             Exception {
         int index = 1;
         int dynamicVariableIndex = 0;
         // make the preparation 
         prepareStatement(parseSQL(reportDto));
        
         // set all the variables of the query
         for (int f=0; f < reportDto.getFields().size(); f++) {
             Field field = (Field) reportDto.getFields().get(f);
             if (field.getWhereValue() != null &&
                     field.getWhereValue().length() > 0) {
                 if (field.getWhereValue().equals("?")) {
                     // this is a dynamic variable, the value it's not known in the field
                     // record nor is entered by the user
                     field.setWhereValue(reportDto.getDynamicParameter(
                             dynamicVariableIndex));
                     dynamicVariableIndex++;
                 } 
 
                 LOG.debug("Setting " + field.getColumnName() + " index " + index +
                         " value " + field.getWhereValue());
                 // see if this is just a null 
                 if (field.getWhereValue().equalsIgnoreCase("null")) {
                     cachedResults.setNull(index, toSQLType(
                             field.getDataType()));
                 } else {
                    if (field.getDataType().equals(Field.TYPE_DATE) ||
                            field.getDataType().equals(Field.TYPE_STRING)) {
                         cachedResults.setString(index, field.getWhereValue());
                     } else if (field.getDataType().equals(Field.TYPE_INTEGER)) {
                         if (field.getWhereValue().indexOf(',') >= 0) {
                             // it is an 'in' with many values
                             StringTokenizer tok = new StringTokenizer(
                                     field.getWhereValue(), ",");
                             int ff = 0;
                             for (;tok.hasMoreElements(); ff++) {
                                 LOG.debug("    in(...) value. index " + 
                                         (index + ff));
                                 cachedResults.setInt(index + ff, Integer.valueOf(
                                         tok.nextToken()).intValue());
                             }
                             index += ff - 1;
                         } else {
                             cachedResults.setInt(index, Integer.valueOf(
                                     field.getWhereValue()).intValue());
                         }
                     } else if (field.getDataType().equals(Field.TYPE_FLOAT)) {
                         cachedResults.setFloat(index, Util.string2float(
                                 field.getWhereValue(), 
                                 reportDto.getLocale()).floatValue());
                     }
                 }
                 
                 index++;
             }
         }
         
         execute();
         conn.close();
         return cachedResults;        
     }
     
    
     public ReportDTOEx getReport(Integer reportId, Integer entityId) 
             throws SessionInternalError {
         LOG.debug("Getting report " + reportId + " for entity " + entityId);
         return DTOFactory.getReportDTOEx(reportId, entityId);
     }
 
     public ReportDTOEx getReport(Integer userReportId) throws SessionInternalError {
         LOG.debug("Getting user report " + userReportId);
         ReportDTOEx reportDto = null;
         
         ReportUserDTO reportUser = reportUserDas.find(userReportId);
         
         // create the initial report dto from the relationship
         UserBL user = new UserBL(reportUser.getBaseUser());
         reportDto = DTOFactory.getReportDTOEx(new ReportDAS().find(reportUser.getReport().getId()),
                 user.getLocale());
         reportDto.setUserReportId(userReportId);
         
         // find this user's saved fields
         Collection fields = reportUser.getFields();
         
         // convert these entities to dtos
         for (Iterator it = fields.iterator(); it.hasNext(); ){
             ReportFieldDTO field = (ReportFieldDTO) it.next();
             reportDto.addField(DTOFactory.getFieldDTO(field));
         }
 
         return reportDto;
     }
     
     public Collection getList(Integer entityId) 
             throws SQLException, Exception{
 
         CompanyDTO entity = new CompanyDAS().find(entityId);
         
         Set<ReportDTO> reports = entity.getReports();
         return DTOFactory.reportEJB2DTOEx(reports, true);
 
     }
     
     public Collection getListByType(Integer typeId) {
         return DTOFactory.reportEJB2DTOEx(new ReportTypeDAS().find(typeId).getReports(), false);
     }
     
     public void save(ReportDTOEx report, Integer userId, String title) {
         
         ReportDTO reportRow = reportDas.find(report.getId());
         // create the report user row
         ReportUserDTO reportUser = reportUserDas.create(title,
                 reportRow, userId);
         
         // now all the fields rows
         for (int f = 0; f < report.getFields().size(); f++) {
             Field field = (Field) report.getFields().get(f);
             
             ReportFieldDTO fieldRow = reportFieldDas.create(
                     field.getPositionNumber(), field.getTableName(), field.getColumnName(),
                     field.getIsGrouped(), field.getIsShown(), 
                     field.getDataType(), field.getFunctionable(),
                     field.getSelectable(), field.getOrdenable(),
                     field.getOperatorable(), field.getWhereable());
             
             fieldRow.setOrderPosition(field.getOrderPosition());
             fieldRow.setWhereValue(field.getWhereValue());
             fieldRow.setTitleKey(field.getTitleKey());
             fieldRow.setFunctionName(field.getFunctionName());
             fieldRow.setOperatorValue(field.getOperatorValue());
             fieldRow.setReportUser(reportUser);
             
             reportUser.getFields().add(fieldRow);
         }
     }
     
     public void delete(Integer userReportId) {
         report = reportUserDas.find(userReportId);
         reportUserDas.delete(report);
     }
     
     public Collection getUserList(Integer report, Integer userId) {
         Collection reports = reportUserDas.findByTypeUser(report, userId);
         return DTOFactory.reportUserEJB2DTO(reports);
     }
     
     private static int toSQLType(String type) 
             throws SessionInternalError {
         if (type.equals(Field.TYPE_DATE)) {
             return Types.DATE;
         } else if (type.equals(Field.TYPE_FLOAT)) {
             return Types.FLOAT;
         } else if (type.equals(Field.TYPE_INTEGER)) {
             return Types.INTEGER;
         } else if (type.equals(Field.TYPE_STRING)) {
             return Types.VARCHAR;
         } else {
             throw new SessionInternalError(type + " is not a supported type");
         }
     }
 }
