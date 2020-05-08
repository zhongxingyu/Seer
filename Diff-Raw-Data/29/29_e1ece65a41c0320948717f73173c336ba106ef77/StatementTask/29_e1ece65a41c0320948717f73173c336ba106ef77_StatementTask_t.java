 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following 
  * conditions are provided as a summary of the NSL but the NSL remains the 
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL. 
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only 
  *    (as Java .class files or a .jar file containing the .class files) and only 
  *    as part of an application that uses The Software as part of its primary 
  *    functionality. No distribution of the package is allowed as part of a software 
  *    development kit, other library, or development tool without written consent of 
  *    Netspective Corporation. Any modified form of The Software is bound by 
  *    these same restrictions.
  * 
  * 3. Redistributions of The Software in any form must include an unmodified copy of 
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective 
  *    Corporation and may not be used to endorse products derived from The 
  *    Software without without written consent of Netspective Corporation. "Sparx" 
  *    and "Netspective" may not appear in the names of products derived from The 
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the 
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind. 
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING 
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.      
  *
  * @author Shahid N. Shah
  */
  
 /**
 * $Id: StatementTask.java,v 1.5 2002-09-17 18:30:35 aye.thu Exp $
  */
 
 package com.netspective.sparx.xaf.task.sql;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.sql.SQLException;
 
 import javax.naming.NamingException;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.w3c.dom.Element;
 
 import com.netspective.sparx.xif.db.DatabaseContext;
 import com.netspective.sparx.xif.db.DatabaseContextFactory;
 import com.netspective.sparx.xaf.report.ReportDestination;
 import com.netspective.sparx.xaf.report.ReportSkin;
 import com.netspective.sparx.xaf.skin.SkinFactory;
 import com.netspective.sparx.xaf.sql.StatementInfo;
 import com.netspective.sparx.xaf.sql.StatementManager;
 import com.netspective.sparx.xaf.sql.StatementManagerFactory;
 import com.netspective.sparx.xaf.sql.StatementNotFoundException;
 import com.netspective.sparx.xaf.sql.ResultInfo;
 import com.netspective.sparx.xaf.sql.StatementDialog;
 import com.netspective.sparx.xaf.task.BasicTask;
 import com.netspective.sparx.xaf.task.TaskContext;
 import com.netspective.sparx.xaf.task.TaskExecuteException;
 import com.netspective.sparx.xaf.task.TaskInitializeException;
 import com.netspective.sparx.xaf.form.DialogContext;
 import com.netspective.sparx.xaf.form.DialogSkin;
 import com.netspective.sparx.util.value.DialogFieldValue;
 import com.netspective.sparx.util.value.SingleValueSource;
 import com.netspective.sparx.util.value.StaticValue;
 import com.netspective.sparx.util.value.ValueSourceFactory;
 
 public class StatementTask extends BasicTask
 {
     static public final String DEFAULT_REPORTSKINID = "report";
     public static final int DEFAULT_ROWS_PER_PAGE = 10;
 
     private StatementInfo statementInfo;
     private String stmtName;
     private String stmtSourceId;
     private SingleValueSource dataSourceValueSource;
     private String reportId;
    private String storeValueName = null;
     private SingleValueSource skinValueSource = new StaticValue(DEFAULT_REPORTSKINID);
    private SingleValueSource storeValueSource = null;
     private SingleValueSource reportDestValueSource;
     private boolean produceReport = true;
     private boolean pageableReport = false;
     private int storeValueType = SingleValueSource.RESULTSET_STORETYPE_SINGLEROWMAP;
     private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
 
     public StatementTask()
     {
         super();
        System.out.println("StatementTask!");
     }
 
     public void reset()
     {
        System.out.println("Task reset");
         super.reset();
         statementInfo = null;
         stmtName = null;
         stmtSourceId = null;
         dataSourceValueSource = null;
         reportId = null;
         skinValueSource = new StaticValue(DEFAULT_REPORTSKINID);
         storeValueName = null;
         storeValueSource = null;
         reportDestValueSource = null;
         produceReport = true;
         pageableReport = false;
         storeValueType = SingleValueSource.RESULTSET_STORETYPE_SINGLEROWMAP;
     }
 
     public SingleValueSource getReportDestSource()
     {
         return reportDestValueSource;
     }
 
     public void setReportDestId(String value)
     {
         if(value == null || value.length() == 0)
             reportDestValueSource = null;
         else
             reportDestValueSource = ValueSourceFactory.getSingleOrStaticValueSource(value);
     }
 
     public String getStmtName()
     {
         return stmtName;
     }
 
     public void setStmtName(String value)
     {
         stmtName = value;
     }
 
     public String getStmtSource()
     {
         return stmtSourceId;
     }
 
     public void setStmtSource(String value)
     {
         stmtSourceId = value;
     }
 
     public SingleValueSource getDataSource()
     {
         return dataSourceValueSource;
     }
 
     public void setDataSource(String value)
     {
         dataSourceValueSource = (value != null && value.length() > 0) ? ValueSourceFactory.getSingleOrStaticValueSource(value) : null;
     }
 
     public String getReport()
     {
         return reportId;
     }
 
     public void setReport(String value)
     {
         reportId = value;
         if("none".equals(value)) produceReport = false;
     }
 
     public SingleValueSource getSkin()
     {
         return skinValueSource;
     }
 
     public void setSkin(String value)
     {
         skinValueSource = ValueSourceFactory.getSingleOrStaticValueSource(value);
     }
 
     public String getStore()
     {
         return storeValueName;
     }
 
     public void setStore(String value)
     {
         storeValueName = value;
     }
 
     public String getStoreType()
     {
         return Integer.toString(storeValueType);
     }
 
     public void setStoreType(String value)
     {
         if(value.length() == 0)
         {
             storeValueType = -1;
             return;
         }
 
         String[] typeNames = SingleValueSource.RESULTSET_STORETYPES;
         int typeCount = typeNames.length;
         for(int i = 0; i < typeCount; i++)
         {
             if(typeNames[i].equals(value))
             {
                 storeValueType = i;
                 return;
             }
         }
         storeValueType = -1;
     }
     /**
      * Whether or not the report should be pageable
      *
      * @return boolean True if the report is pageable
      */
     public boolean isPageable()
     {
         return this.pageableReport;
     }
 
     public void setPageableReport(boolean pageableReport)
     {
         this.pageableReport = pageableReport;
     }
 
     public void setPageableReport(String value)
     {
        if ("yes".equalsIgnoreCase(value))
             setPageableReport(true);
         else
             setPageableReport(false);
     }
 
     public void setRowsPerPage(int rowsPerPage)
     {
         this.rowsPerPage = rowsPerPage;
     }
 
     /**
      * Returns the number of rows to display per page if the report is pageable
      *
      * @return int number of rows per page
      */
     public int getRowsPerPage()
     {
         return this.rowsPerPage;
     }
 
     public void initialize(Element elem) throws TaskInitializeException
     {
         super.initialize(elem);
 
         stmtSourceId = elem.getAttribute("stmt-src");
         if(stmtSourceId.length() == 0) stmtSourceId = null;
 
         setDataSource(elem.getAttribute("data-src"));
 
         if(elem.getChildNodes().getLength() > 0)
         {
             if(elem.getAttribute("name").length() == 0)
                 elem.setAttribute("name", "SqlExecuteAction-" + getTaskNum());
             statementInfo = new StatementInfo();
             statementInfo.importFromXml(null, elem, "Task", null);
         }
         else
         {
             stmtName = elem.getAttribute("name");
         }
 
         reportId = elem.getAttribute("report");
         if(reportId.length() == 0) reportId = null;
 
         String reportSkinId = elem.getAttribute("skin");
         if(reportSkinId.length() == 0) reportSkinId = DEFAULT_REPORTSKINID;
         setSkin(reportSkinId);
 
         storeValueName = elem.getAttribute("store");
         if(storeValueName.length() == 0) storeValueName = null;
 
         if(reportId != null && reportId.equals("none"))
             produceReport = false;
 
         if(storeValueName != null)
         {
             setStoreType(elem.getAttribute("store-type"));
         }
 
         setReportDestId(elem.getAttribute("destination"));
 
        String pageable = elem.getAttribute("pageable");
        if (pageable != null && pageable.equals("yes"))
         {
             this.pageableReport = true;
             String rowStr = elem.getAttribute("rows");
             if (rowStr != null && rowStr.length() > 0)
             {
                 this.rowsPerPage = Integer.parseInt(rowStr);
             }
         }
 
     }
 
     public void execute(TaskContext tc) throws TaskExecuteException
     {
         tc.registerTaskExecutionBegin(this);
 
        if(storeValueName != null)
         {
             storeValueSource = ValueSourceFactory.getStoreValueSource(storeValueName);
             if(storeValueSource == null)
                 throw new TaskExecuteException("SingleValueSource '" + storeValueName + "' not found");
             if(!storeValueSource.supportsSetValue())
                 throw new TaskExecuteException("SingleValueSource '" + storeValueName + "' does not support value storage.");
 
             if(storeValueSource instanceof DialogFieldValue)
                 storeValueType = SingleValueSource.RESULTSET_STORETYPE_SINGLEROWFORMFLD;
 
             if(storeValueType == -1)
                 throw new TaskExecuteException("store-type must be one of " + SingleValueSource.RESULTSET_STORETYPES.toString());
         }
 
         ServletContext context = tc.getServletContext();
         StatementManager stmtManager = stmtSourceId == null ? StatementManagerFactory.getManager(context) : StatementManagerFactory.getManager(stmtSourceId);
         DatabaseContext dbContext = DatabaseContextFactory.getContext(tc.getRequest(), context);
 
         if(stmtManager == null)
         {
             tc.addErrorMessage("StatementManager file '" + stmtSourceId + "' not found (specified in ServletContext config init parameter 'sql-statements-file'", false);
             return;
         }
 
         if(flagIsSet(TASKFLAG_DEBUG))
         {
             StringBuffer debugMessage = new StringBuffer();
             StatementInfo si;
             if(statementInfo != null)
                 si = statementInfo;
             else
                 si = stmtManager.getStatement(stmtName);
 
             if(si == null)
                 debugMessage.append("SQL: statement '" + stmtName + "' doesn't exist");
             else
             {
                 debugMessage.append("<p>SQL: statement '");
                 debugMessage.append(si.getId());
                 debugMessage.append("</p></pre>");
                 debugMessage.append(si.getDebugHtml(tc));
                 debugMessage.append("</pre>");
             }
             tc.addErrorMessage(debugMessage.toString(), false);
             return;
         }
 
         int reportDestId = ReportDestination.DEST_BROWSER_SINGLE_PAGE;
         if(reportDestValueSource != null)
         {
             String reportDestName = reportDestValueSource.getValue(tc);
             reportDestId = ReportDestination.getDestIdFromName(reportDestName);
             if(reportDestId == -1 || reportDestId == ReportDestination.DEST_BROWSER_MULTI_PAGE)
                 throw new TaskExecuteException("ReportDestination '" + reportDestName + "' not supported. Use 'browser', 'file' or 'email'");
         }
 
         Writer out = null;
         ReportDestination reportDest = null;
         ReportSkin reportSkin = SkinFactory.getReportSkin(skinValueSource.getValue(tc));
         if(reportSkin == null)
         {
             tc.addErrorMessage("ReportSkin '" + skinValueSource.getId() + "' not found.", false);
             return;
         }
 
         try
         {
             if(reportDestId == ReportDestination.DEST_FILE_DOWNLOAD || reportDestId == ReportDestination.DEST_FILE_EMAIL)
             {
                 reportDest = new ReportDestination(reportDestId, tc, reportSkin);
                 out = reportDest.getWriter();
             }
             else
                 out = new StringWriter();
             String dataSourceId = this.getDataSource() != null ? this.getDataSource().getValue(tc) : null;
 
             if (produceReport && this.pageableReport)
             {
                 // Special Case: This static query must produce a report that is pageable
                 StatementDialog stmtDialog = new StatementDialog(stmtManager.getStatement(stmtName), getReport(), getSkin() != null ? getSkin().getValue(tc) : null);
                 stmtDialog.setRowsPerPage(getRowsPerPage());
                 DialogSkin skin = com.netspective.sparx.xaf.skin.SkinFactory.getDialogSkin();
                DialogContext dc = stmtDialog.createContext(context, tc.getServlet(),
                         (javax.servlet.http.HttpServletRequest) tc.getRequest(),
                         (javax.servlet.http.HttpServletResponse) tc.getResponse(), skin);
                stmtDialog.prepareContext(dc);
                 stmtDialog.renderHtml(out, dc, false);
             }
             else if(produceReport && storeValueSource == null)
             {
                 if(statementInfo != null)
                     stmtManager.produceReport(out, dbContext, tc, dataSourceId, reportSkin, statementInfo, null, reportId);
                 else
                     stmtManager.produceReport(out, dbContext, tc, dataSourceId, reportSkin, stmtName, null, reportId);
             }
             else if(!produceReport && storeValueSource != null)
             {
                 if(statementInfo != null)
                     stmtManager.executeAndStore(dbContext, tc, dataSourceId, statementInfo, storeValueSource, storeValueType);
                 else
                     stmtManager.executeAndStore(dbContext, tc, dataSourceId, stmtName, storeValueSource, storeValueType);
               System.out.println(storeValueSource.getId() + " " + storeValueType);
             }
             else if(produceReport && storeValueSource != null)
             {
                 if(statementInfo != null)
                     stmtManager.produceReportAndStoreResultSet(out, dbContext, tc, dataSourceId, reportSkin, statementInfo, null, reportId, storeValueSource, storeValueType);
                 else
                     stmtManager.produceReportAndStoreResultSet(out, dbContext, tc, dataSourceId, reportSkin, stmtName, null, reportId, storeValueSource, storeValueType);
             }
             else // we're not producing a report nor are we storing values so just execute and leave (could be DML)
             {
                 ResultInfo ri = null;
                 if(statementInfo != null)
                     ri = stmtManager.execute(dbContext, tc, dataSourceId, statementInfo, null);
                 else
                     ri = stmtManager.execute(dbContext, tc, dataSourceId, stmtName, null);
                 if(ri != null)
                     ri.close();
             }
         }
         catch(IOException e)
         {
             throw new TaskExecuteException(e);
         }
         catch(SQLException e)
         {
             StringBuffer errorMsg = new StringBuffer();
             StatementInfo si = statementInfo != null ? statementInfo : stmtManager.getStatement(stmtName);
 
             StringWriter stack = new StringWriter();
             e.printStackTrace(new PrintWriter(stack));
 
             errorMsg.append("<pre>");
             errorMsg.append(si.getDebugHtml(tc));
             errorMsg.append("\n\n");
             errorMsg.append(stack.toString());
             errorMsg.append("</pre>");
 
             tc.addErrorMessage(errorMsg.toString(), false);
             return;
         }
         catch(StatementNotFoundException e)
         {
             throw new TaskExecuteException(e);
         }
         catch(NamingException e)
         {
             throw new TaskExecuteException(e);
         }
         finally
         {
             try
             {
                 if(out != null) out.close();
             }
             catch(IOException ioe)
             {
                 throw new TaskExecuteException(ioe);
             }
         }
 
         if(reportDest != null)
             tc.addResultMessage(reportDest.getUserMessage());
         else
             tc.addResultMessage(out.toString());
 
         tc.registerTaskExecutionEnd(this);
     }
 }
