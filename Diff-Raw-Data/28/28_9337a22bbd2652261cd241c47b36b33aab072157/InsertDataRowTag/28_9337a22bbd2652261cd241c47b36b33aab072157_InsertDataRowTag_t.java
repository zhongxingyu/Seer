 /*
 The contents of this file are subject to the Jbilling Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.jbilling.com/JPL/
 
 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.
 
 The Original Code is jbilling.
 
 The Initial Developer of the Original Code is Emiliano Conde.
 Portions created by Sapienter Billing Software Corp. are Copyright 
 (C) Sapienter Billing Software Corp. All Rights Reserved.
 
 Contributor(s): ______________________________________.
 */
 
 package com.sapienter.jbilling.client.list;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.sql.Date;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.BodyTagSupport;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.Globals;
 import org.apache.struts.validator.Resources;
 
 import sun.jdbc.rowset.CachedRowSet;
 
 import com.sapienter.jbilling.client.util.Constants;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.list.ListDTO;
 
 /**
  * This tag will display all the colums of a query previously done with 
  * a tag that extends ListTagBase. It skips the very first column, 
  * because it should be an ID.
  * It makes available the first column (ID) as the property 'rowID' to the
  * caller JSP page.
  * 
  * @author emilc
  *
  * @jsp:tag name="insertDataRow"
  *          body-content="empty"
  */
 
 public class InsertDataRowTag extends BodyTagSupport {
     public int doStartTag() throws JspException {
         Logger log = Logger.getLogger(InsertDataRowTag.class);
         //log.debug("bp1");
         CachedRowSet results = ((ListTagBase) getParent()).getQueryResults();
         ListDTO dtoResults = ((ListTagBase) getParent()).getQueryDtoResults();
         int dtoIndex = ((ListTagBase) getParent()).getDtoIndex();
         int method = ((ListTagBase) getParent()).getQueryMethod();
         HttpSession session = pageContext.getSession();
         boolean isPaged = session.getAttribute(Constants.SESSION_PAGED_LIST) 
                 != null;
 
         JspWriter out = pageContext.getOut();
         // this is the description
         //log.debug("bp2");
         try {
             // the ID of this row is mada available to the rest of the page
             String rowId;
             ResultSetMetaData metaData = null;
             int columns;
             Object dtoLine[] = null;
             if (method == ListTagBase.METHOD_JDBC) {
                 rowId = results.getString(1);
                 metaData = results.getMetaData();
                 columns = metaData.getColumnCount();
             } else {
                 dtoLine = (Object[]) dtoResults.getLines().
                         get(dtoIndex);
                 rowId = (String) dtoLine[0];
                 columns = dtoLine.length;
             }
             pageContext.setAttribute("rowID", rowId);
             if (isPaged) {
                 session.setAttribute(Constants.SESSION_LIST_LAST_ID, 
                         new Integer(rowId));
             }
             // this skips the very first column, it is assumed is the
             // internal ID
             
             //log.debug("bp3");
             for (int f = 2; f <= columns; f++) {
                 boolean alignRight = false;
                 int dataType;
                 if (method == ListTagBase.METHOD_JDBC) {
                     dataType = metaData.getColumnType(f);
                 } else {
                     dataType = dtoResults.getTypes()[f-1].intValue();
                 }
                 String field = null;
                 String dateFormat = null;
                 String timeStampFormat = null;
 //                log.debug("column " + f + " datatype:" + dataType); 
 
                 switch (dataType) {
                 case Types.DATE:
                     Date date; 
                     if (method == ListTagBase.METHOD_JDBC) {
                         date = results.getDate(f);
                     } else {
                         date = (Date) dtoLine[f-1];
                     }
                     if (date != null) {
                         if (dateFormat == null) {
                             dateFormat = Resources.getMessage(
                                     (HttpServletRequest) pageContext.getRequest(), 
                                     "format.date"); 
                         }
                         SimpleDateFormat df = new SimpleDateFormat(dateFormat);
                         field = df.format(date);
                     }
                     break;
                 case Types.TIMESTAMP:
                    Date ts = null;
                     if (method == ListTagBase.METHOD_JDBC) {
                         Object timeObj = results.getObject(f);
                        //log.debug(timeObj + " class " + ((timeObj == null) ? "null" : timeObj.getClass()) );
                        if (timeObj != null) {
	                        if (timeObj instanceof java.util.Date) {
	                            ts = results.getDate(f);
	                        } else if (timeObj.getClass().getName().equals("oracle.sql.TIMESTAMP")){
	                            // Oracle does its own thing. Not good :(
	                            // we do not want to have any dependencies with Oracle. Thus, use reflexion
	                            Method toCall = timeObj.getClass().getMethod("timestampValue", null);
	                            ts = new Date(((Timestamp) toCall.invoke(timeObj, null)).getTime());
	                        } else {
	                            log.error("Time stamp of type " + 
	                                    timeObj.getClass().getName() + " not supported");
	                            ts = new Date(Calendar.getInstance().getTime().getTime());
	                        }
                         }
                     } else { 
                         ts = (Date) dtoLine[f-1];
                     }
                     if (ts != null) {
                         if (timeStampFormat == null) {
                             timeStampFormat = Resources.getMessage(
                                     (HttpServletRequest) pageContext.getRequest(), 
                                     "format.timestamp"); 
                         }
                         SimpleDateFormat df = new SimpleDateFormat(
                                 timeStampFormat);
                         field = df.format(ts);
                     }
                     break;
                 case Types.FLOAT:
                 case Types.DECIMAL:
                 case Types.DOUBLE:
                     float dec;
                     alignRight = true;
                     //log.debug("bp4-1");
                     if (method == ListTagBase.METHOD_JDBC) {
                         dec = results.getFloat(f);
                     } else {
                         dec = ((Float) dtoLine[f-1]).floatValue();
                     }
                     //log.debug("bp4-2");
                     Locale loc = (Locale) session.getAttribute(
                             Globals.LOCALE_KEY);
                     //log.debug("bp4-3" + loc);
                     NumberFormat nf = NumberFormat.getInstance(loc);
                     //log.debug("bp4-4" + nf);
                     if (nf instanceof DecimalFormat) {
                         // we always want two decimals
                         ((DecimalFormat) nf).applyPattern(Resources.getMessage(
                             (HttpServletRequest) pageContext.getRequest(), 
                             "format.float"));
                     }
                     field = nf.format(dec);
                     //log.debug("bp4-5");
                     break;
                 default:
                     if (method == ListTagBase.METHOD_JDBC) {
                         field = results.getString(f);
                     } else {
                         field = (String) dtoLine[f-1];
                     } 
                     break;
                 }
                 
                 //log.debug("bp4");
                 if (field == null) {
                     field = "";
                 } 
                 out.print("<td class=\"list\"");
                 if (alignRight) {
                     out.print(" align=\"right\"");
                 }
                 out.print(">");
                 out.print(field);
                 out.println("</td>");
             }
         } catch (SQLException e) {
             log.error("SQLException at InsertDataRowTag tag", e);
             SessionInternalError err = new SessionInternalError(e);
             throw new JspException("Web error:" + e.getMessage());
         } catch (IOException e) {
             log.error("IOException at InsertDataRowTag tag", e);
             SessionInternalError err = new SessionInternalError(e);
             throw new JspException("Web error:" + e.getMessage());
         } catch (Exception e) {
             log.error("Null pointer!", e);
             SessionInternalError err = new SessionInternalError(e);
             throw new JspException("Web error:" + e.getMessage());
         }
 
         //log.debug("bp5");
         return SKIP_BODY;
     }
 }
