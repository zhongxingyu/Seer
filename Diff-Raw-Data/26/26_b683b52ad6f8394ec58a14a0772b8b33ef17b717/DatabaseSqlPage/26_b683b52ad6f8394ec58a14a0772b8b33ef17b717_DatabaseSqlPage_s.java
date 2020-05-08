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
 * $Id: DatabaseSqlPage.java,v 1.5 2002-09-08 02:08:11 shahid.shah Exp $
  */
 
 package com.netspective.sparx.ace.page;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.netspective.sparx.ace.AceServletPage;
 import com.netspective.sparx.xif.db.DatabaseContext;
 import com.netspective.sparx.xif.db.DatabaseContextFactory;
 import com.netspective.sparx.xaf.page.PageContext;
 import com.netspective.sparx.xaf.skin.SkinFactory;
 import com.netspective.sparx.xaf.sql.StatementInfo;
 import com.netspective.sparx.xaf.sql.StatementManager;
 import com.netspective.sparx.xaf.sql.StatementManagerFactory;
 import com.netspective.sparx.xaf.sql.StatementDialog;
 import com.netspective.sparx.xaf.task.sql.StatementTask;
 import com.netspective.sparx.xaf.task.TaskContext;
 import com.netspective.sparx.xaf.task.TaskExecuteException;
 import com.netspective.sparx.xaf.form.DialogSkin;
 import com.netspective.sparx.xaf.form.DialogContext;
 import com.netspective.sparx.xaf.form.Dialog;
 import com.netspective.sparx.util.value.ValueContext;
 
 public class DatabaseSqlPage extends AceServletPage
 {
     public final String getName()
     {
         return "sql";
     }
 
     public final String getPageIcon()
     {
         return "sql.gif";
     }
 
     public final String getCaption(PageContext pc)
     {
         return "SQL Statements";
     }
 
     public final String getHeading(PageContext pc)
     {
         return "SQL Statements";
     }
 
     public static boolean useDialogParams(ValueContext vc)
     {
        return ! "no".equals(vc.getRequest().getParameter("ui"));
     }
 
     public void handlePageBody(PageContext pc) throws ServletException, IOException
     {
         String testItem = getTestCommandItem(pc);
         if(testItem != null)
         {
             handleUnitTestPageBegin(pc, "Static SQL Unit Test");
             if (useDialogParams(pc))
                 handleTestStatementWithUI(pc, testItem);
             else
                 handleTestStatementNoUI(pc, testItem);
             handleUnitTestPageEnd(pc);
         }
         else
         {
             ServletContext context = pc.getServletContext();
             StatementManager manager = StatementManagerFactory.getManager(context);
             manager.updateExecutionStatistics();
             manager.addMetaInfoOptions();
             transform(pc, manager.getDocument(), com.netspective.sparx.Globals.ACE_CONFIG_ITEMS_PREFIX + "sql-browser-xsl");
         }
     }
 
     public void handleTestStatementWithUI(PageContext pc, String stmtId) throws IOException
     {
         ServletContext context = pc.getServletContext();
         StatementManager manager = StatementManagerFactory.getManager(context);
 
         PrintWriter out = pc.getResponse().getWriter();
         DatabaseContext dbc = DatabaseContextFactory.getContext(pc.getRequest(), context);
 
         StatementInfo si = manager.getStatement(stmtId);
         if(si != null)
         {
             out.write("<h1>SQL Unit Test: " + stmtId + "</h1>");
             StatementDialog dialog = si.getDialog();
             if(dialog != null)
             {
                 DialogContext dc = dialog.createContext(context, pc.getServlet(), (HttpServletRequest) pc.getRequest(), (HttpServletResponse) pc.getResponse(), SkinFactory.getDialogSkin());
                 dialog.prepareContext(dc);
                 dialog.renderHtml(out, dc, true);
                 out.write("<p>");
                 out.write(si.getDebugHtml(pc));
 
             }
             else
                 out.write("Statement '"+ stmtId +"' produced a NULL dialog.");
 
         }
         else
             out.write("Statement '"+ stmtId +"' not found in default context.");
     }
 
     public void handleTestStatementNoUI(PageContext pc, String stmtId) throws ServletException, IOException
     {
         ServletContext context = pc.getServletContext();
         StatementManager manager = StatementManagerFactory.getManager(context);
 
         PrintWriter out = pc.getResponse().getWriter();
         DatabaseContext dbc = DatabaseContextFactory.getContext(pc.getRequest(), context);
 
         out.write("<h1>SQL: " + stmtId + "</h1>");
         StatementInfo si = manager.getStatement(stmtId);
         try
         {
             if ("yes".equals(pc.getRequest().getParameter("pageable")))
             {
                 try
                 {
                     StatementTask task = new StatementTask();
                     task.setPageableReport(true);
                     task.setStmtName(stmtId);
                     String rows = pc.getRequest().getParameter("rows");
                     if (rows != null && rows.length() > 0)
                         task.setRowsPerPage(Integer.parseInt(rows));
 
                     TaskContext tc = new TaskContext(pc);
                     task.execute(tc);
                     if(tc.hasError())
                         out.write(tc.getErrorMessage());
                     else if(tc.hasResultMessage())
                         out.write(tc.getResultMessage());
                 }
                 catch (TaskExecuteException e)
                 {
                     StringWriter msg = new StringWriter();
                     msg.write(e.toString());
 
                     PrintWriter pw = new PrintWriter(msg);
                     e.printStackTrace(pw);
                     out.write("<pre>");
                     out.write(msg.toString());
                     out.write("</pre>");
                 }
             }
             else
                 manager.produceReport(out, dbc, pc, null, SkinFactory.getReportSkin("report"), stmtId, null, null);
         }
         catch(Exception e)
         {
             StringWriter msg = new StringWriter();
             msg.write(e.toString());
 
             PrintWriter pw = new PrintWriter(msg);
             e.printStackTrace(pw);
             out.write("<pre>");
             out.write(msg.toString());
             out.write("</pre>");
         }
         out.write(si.getDebugHtml(pc));
     }
 }
