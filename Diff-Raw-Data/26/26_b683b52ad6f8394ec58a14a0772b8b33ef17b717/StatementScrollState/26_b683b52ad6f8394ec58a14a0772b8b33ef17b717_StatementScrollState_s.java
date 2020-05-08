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
 package com.netspective.sparx.xaf.sql;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.naming.NamingException;
 
 import com.netspective.sparx.xaf.form.DialogContext;
 import com.netspective.sparx.xaf.report.Report;
 import com.netspective.sparx.xaf.report.ReportContext;
 import com.netspective.sparx.xaf.report.ReportSkin;
 import com.netspective.sparx.xaf.report.StandardReport;
 import com.netspective.sparx.xaf.skin.SkinFactory;
 import com.netspective.sparx.xif.db.DatabaseContext;
 
 public class StatementScrollState extends ResultSetScrollState
 {
     private DatabaseContext dbContext;
     private StatementInfo stmtInfo;
     private Report reportDefn;
     private ReportSkin skin;
     private String dialogData;
     private boolean resultSetValid;
     private StatementInfo stmt;
     private String datasourceId;
     private String reportName;
 
     /**
      *
      */
     public StatementScrollState(StatementInfo si, DatabaseContext dbContext, DialogContext dc, String datasourceId, String reportName, String skinName, int rowsPerPage, int scrollType)
             throws NamingException, SQLException
     {
         super(StatementManager.execute(dbContext, dc, datasourceId, si, null, scrollType == ResultSetScrollState.SCROLLTYPE_USERESULTSET ? true : false), rowsPerPage, scrollType);
         this.skin = SkinFactory.getReportSkin(skinName == null ? "report" : skinName);
         this.stmtInfo = si;
         this.datasourceId = datasourceId;
         this.reportName = reportName;
 
         this.dbContext = dbContext;
         ResultSet rs = getResultSet();
         if (rs != null)
         {
             this.reportDefn = new StandardReport();
             this.reportDefn.initialize(rs, si.getReportElement(reportName));
             this.resultSetValid = true;
         }
         else
         {
             throw new SQLException("Unable to execute SQL: " + stmtInfo.getSql(dc));
         }
     }
     /**
     *
      */
     public boolean isValid()
     {
         return resultSetValid;
     }
 
     /**
      * Get the report skin
      *
      * @return ReportSkin report skin
      */
     public ReportSkin getSkin()
     {
         return skin;
     }
 
     /**
      * Produce a pageable HTML report of the result set
      *
      * @param writer
      * @param dc dialog context
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      */
     public void produceReport(Writer writer, DialogContext dc) throws SQLException, NamingException, IOException
     {
         if (!isScrollable())
             scrollToActivePage(StatementManager.execute(dbContext, dc, datasourceId, stmtInfo, null, false));
         else
             scrollToActivePage();
 
         ReportContext rc = new ReportContext(dc.getServletContext(), dc.getServlet(), dc.getRequest(), dc.getResponse(),
                 reportDefn, this.skin);
         rc.setResultsScrolling(this);
         /*
         if(primaryOrderByColIndex != -1)
             rc.getState(primaryOrderByColIndex).setFlag(sortFieldInfo.isDescending() ? ReportColumn.COLFLAG_SORTED_DESCENDING : ReportColumn.COLFLAG_SORTED_ASCENDING);
         */
         this.skin.produceReport(writer, rc, getResultSet());
 
     }
 }
