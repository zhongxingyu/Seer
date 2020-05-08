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
 * $Id: BasicTabularReportActionSkin.java,v 1.2 2004-06-23 15:13:50 aye.thu Exp $
  */
 package com.netspective.sparx.theme.basic;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.netspective.commons.report.tabular.TabularReportColumn;
 import com.netspective.commons.report.tabular.TabularReportColumnState;
 import com.netspective.commons.report.tabular.TabularReportColumns;
 import com.netspective.commons.report.tabular.TabularReportDataSource;
 import com.netspective.commons.report.tabular.TabularReportValueContext;
 import com.netspective.sparx.panel.HtmlPanel;
 import com.netspective.sparx.panel.HtmlTabularReportPanel;
 import com.netspective.sparx.report.tabular.BasicHtmlTabularReport;
import com.netspective.sparx.report.tabular.BasicSelectActionHtmlTabularReport;
 import com.netspective.sparx.report.tabular.HtmlReportAction;
 import com.netspective.sparx.report.tabular.HtmlReportActions;
 import com.netspective.sparx.report.tabular.HtmlTabularReport;
 import com.netspective.sparx.report.tabular.HtmlTabularReportDataSource;
 import com.netspective.sparx.report.tabular.HtmlTabularReportValueContext;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.navigate.NavigationContext;
 
 /**
  * Custom skin class for producing a single page report with checkboxes associated with each row and a
  * row of buttons in the footer of the report. Each button has a submit URL associated with it and
  * when at least one of the checkboxes are checked, the button action will be submitted. The value
  * associated with the checkbox is only the value of the first column of the report. Once the report is
  * "submitted", the selected checkboxes can be retrieved using the
 * {@link HtmlReportActions#getSelectedItems(com.netspective.sparx.navigate.NavigationContext) HtmlReportActions.getSelectedItems}
  * method.
  *
  *
  */
 public class BasicTabularReportActionSkin extends BasicHtmlTabularReportPanelSkin
 {
     public BasicTabularReportActionSkin()
     {
         super();
     }
 
     public BasicTabularReportActionSkin(Theme theme, String name, String panelClassNamePrefix, String panelResourcesPrefix, boolean fullWidth)
     {
         super(theme, name, panelClassNamePrefix, panelResourcesPrefix, fullWidth);
     }
 
     public void render(Writer writer, TabularReportValueContext rc, TabularReportDataSource ds) throws IOException
     {
        BasicSelectActionHtmlTabularReport report = (BasicSelectActionHtmlTabularReport) rc.getReport();
         HtmlReportActions actions = report.getActions();
         StringBuffer reportActionHtml = new StringBuffer();
         if (actions != null)
         {
             HtmlReportAction[] submitReportActions = actions.getByType(HtmlReportAction.Type.REPORT_SUBMIT);
             reportActionHtml.append("<script>\n");
             String reportName = rc.getReport().getName();
             reportActionHtml.append(reportName +" = new ReportAction('"+ rc.getReport().getName() + "'); \n");
             for (int i=0; i < submitReportActions.length; i++)
             {
                 reportActionHtml.append(reportName + ".registerSubmitAction('"+ submitReportActions[i].getCaption().getTextValue(rc) + "', " +
                         "'"+ submitReportActions[i].getRedirect().getUrl(rc) + "');\n");
             }
             reportActionHtml.append("</script>\n");
             reportActionHtml.append("<form name=\""  + rc.getReport().getName() + "ReportActionForm\" " +
                 "action=\"\" style=\"margin: 0\">");
         }
         if (reportActionHtml.length() > 0)
             writer.write(reportActionHtml.toString());
         final HtmlTabularReportValueContext rvc = (HtmlTabularReportValueContext) rc;
         renderPanelRegistration(writer, rvc);
 
         final HtmlTabularReportPanel htmlTabularReportPanel = (HtmlTabularReportPanel) rvc.getPanel();
         final HtmlTabularReportPanel.CustomRenderer customRenderer = htmlTabularReportPanel.getRenderer();
         int panelRenderFlags = rvc.getPanelRenderFlags();
 
         if(customRenderer != null)
         {
             boolean handleContainerTableTag = customRenderer.isRenderContainerTableTag();
             if(! customRenderer.isRenderFrame())
                 panelRenderFlags |= HtmlPanel.RENDERFLAG_NOFRAME;
 
             if((panelRenderFlags & HtmlPanel.RENDERFLAG_NOFRAME) == 0)
             {
                 renderFrameBegin(writer, rvc);
                 if(handleContainerTableTag)
                     writer.write("    <table class=\"report\" width=\"100%\" border=\"0\" cellspacing=\"2\" cellpadding=\"0\">\n");
             }
             else if(handleContainerTableTag)
                 writer.write("    <table id=\""+ rvc.getPanel().getPanelIdentifier() +"_content\" class=\"report_no_frame\" width=\"100%\" border=\"0\" cellspacing=\"2\" cellpadding=\"0\">\n");
 
             customRenderer.getTemplateProcessor().process(writer, rc, customRenderer.getTemplateVars(rvc, ds));
 
             if(handleContainerTableTag)
                 writer.write("    </table>\n");
 
             if((panelRenderFlags & HtmlPanel.RENDERFLAG_NOFRAME) == 0)
                 renderFrameEnd(writer, rvc);
         }
         else
         {
             if((panelRenderFlags & HtmlPanel.RENDERFLAG_NOFRAME) == 0)
             {
                 renderFrameBegin(writer, rvc);
                 writer.write("    <table class=\"report\" width=\"100%\" border=\"0\" cellspacing=\"2\" cellpadding=\"0\">\n");
             }
             else
                 writer.write("    <table id=\""+ rvc.getPanel().getPanelIdentifier() +"_content\" class=\"report_no_frame\" width=\"100%\" border=\"0\" cellspacing=\"2\" cellpadding=\"0\">\n");
 
             if(flags.flagIsSet(Flags.SHOW_HEAD_ROW) && !rc.getReport().getFlags().flagIsSet(HtmlTabularReport.Flags.HIDE_HEADING))
                 produceHeadingRow(writer, rvc, (HtmlTabularReportDataSource) ds);
             produceDataRows(writer, rvc, (HtmlTabularReportDataSource) ds);
 
             //if(flags.flagIsSet(Flags.SHOW_FOOT_ROW) && rc.getCalcsCount() > 0)
             produceFootRow(writer, rvc);
             writer.write("    </table>\n");
 
             if((panelRenderFlags & HtmlPanel.RENDERFLAG_NOFRAME) == 0)
                 renderFrameEnd(writer, rvc);
         }
         if (reportActionHtml.length() > 0)
             writer.write("</form>");            
     }
 
     public void produceHeadingRow(Writer writer, HtmlTabularReportValueContext rc, HtmlTabularReportDataSource ds) throws IOException
     {
         TabularReportColumns columns = rc.getColumns();
         TabularReportColumnState[] states = rc.getStates();
         int dataColsCount = columns.size();
 
         Theme theme = getTheme();
         String sortAscImgTag = " <img src=\""+ theme.getResourceUrl("/images/" + panelResourcesPrefix + "/column-sort-ascending.gif") + "\" border=0>";
         String sortDescImgTag = " <img src=\""+ theme.getResourceUrl("/images/" + panelResourcesPrefix + "/column-sort-descending.gif") + "\" border=0>";
 
         writer.write("<tr>");
         int prependColCount = getRowDecoratorPrependColsCount(rc);
         if (prependColCount > 0)
         {
             for (int k=0; k < prependColCount; k++)
             {
                  writer.write("        <th class=\"report-column-heading\" nowrap scope=\"col\">"+
                          "<input type=\"checkbox\" name=\"checkAll\" onclick=\"reportEventOnClick("+ rc.getReport().getName() + ", this, event)\"" +
                          "</th>");
             }
         }
         for(int i = 0; i < dataColsCount; i++)
         {
             TabularReportColumnState rcs = rc.getState(i);
             if(! states[i].isVisible())
                 continue;
 
             String colHeading = ds.getHeadingRowColumnData(i);
             if(colHeading != null)
             {
                 if(rcs.getFlags().flagIsSet(TabularReportColumn.Flags.SORTED_ASCENDING))
                     colHeading += sortAscImgTag;
                 if(rcs.getFlags().flagIsSet(TabularReportColumn.Flags.SORTED_DESCENDING))
                     colHeading += sortDescImgTag;
 
                 writer.write("        <th class=\"report-column-heading\" nowrap scope=\"col\">" + colHeading  + "</th>");
             }
             else
                 writer.write("        <th class=\"report-column-heading\" nowrap scope=\"col\">&nbsp;&nbsp;</th>");
         }
         int appendColCount = getRowDecoratorAppendColsCount(rc);
         if (appendColCount > 0)
         {
             for (int k=0; k < appendColCount; k++)
             {
                  writer.write("        <th class=\"report-column-heading\" nowrap scope=\"col\">&nbsp;&nbsp;</th>");
             }
         }
         writer.write("</tr>");
     }
 
     protected int getRowDecoratorPrependColsCount(HtmlTabularReportValueContext rc)
     {
         BasicHtmlTabularReport report = (BasicHtmlTabularReport)rc.getReport();
         HtmlReportActions actions = report.getActions();
         if (actions == null)
         {
             // no actions are defined in the report so return 0
             return 0;
         }
         HtmlReportAction[] selectReportActions = actions.getByType(HtmlReportAction.Type.RECORD_SELECT);
         if (selectReportActions != null && selectReportActions.length > 0)
             return 1;
         else
             return 0;
     }
 
     public void produceDataRowDecoratorPrepend(Writer writer, HtmlTabularReportValueContext rc, HtmlTabularReportDataSource ds, String[] rowData, boolean isOddRow) throws IOException
     {
         BasicHtmlTabularReport report = (BasicHtmlTabularReport)rc.getReport();
         HtmlReportActions actions = report.getActions();
         if (actions == null)
         {
             // no actions are defined in the report
             return;
         }
         HtmlReportAction[] selectReportActions = actions.getByType(HtmlReportAction.Type.RECORD_SELECT);
         if (selectReportActions != null && selectReportActions.length > 0)
         {
             // use the active row rumber within the result set as the checkbox name
             int activeRowNumber = ds.getActiveRowNumber();
 
             // attach all the row data as name/value pairs to the value of the checkbox. also prepend the active row number
             // to the string
             /*
             StringBuffer valueStr = new StringBuffer(activeRowNumber + "\t");
             for (int i=0; i < rowData.length; i++)
             {
                 String colHeading = ds.getHeadingRowColumnData(i);
                 valueStr.append(i != rowData.length-1 ? (colHeading + "=" + rowData[i] + "\t") : (colHeading + "=" + rowData[i]));
             }
             */
 
             writer.write("<td " + (isOddRow ? "class=\"report-column-even\"" : "class=\"report-column-odd\"") + " width=\"10\">");
             writer.write("<input type=\"checkbox\" value=\"" + rowData[0] + "\" name=\"selectedItemList\" " +
                     "title=\"Click here to select the row.\" ");
 
             writer.write(" onClick=\"reportEventOnClick("+ report.getName() + ", this, event)\">\n");
             writer.write("</td>");
         }
     }
 
     public void produceFootRow(Writer writer, HtmlTabularReportValueContext rc) throws IOException
     {
         super.produceFootRow(writer, rc);
         TabularReportColumns columns = rc.getColumns();
         int colsCount = columns.size() + getRowDecoratorPrependColsCount(rc);
 
         BasicHtmlTabularReport report = (BasicHtmlTabularReport)rc.getReport();
         HtmlReportActions actions = report.getActions();
         if (actions.size() > 0)
         {
             HtmlReportAction[] submitReportActions = actions.getByType(HtmlReportAction.Type.REPORT_SUBMIT);
             writer.write("<tr>\n");
             for (int i=0; i < submitReportActions.length; i++)
             {
                 writer.write("<td colspan=\"" + colsCount + "\"><input type=\"button\" name=\"submitAction\" value=\"" +
                         submitReportActions[i].getCaption().getTextValue(rc) +"\" " +
                         "onclick=\"reportEventOnClick(" + report.getName() + ", this, event)\"/></td>");
             }
             writer.write("</tr>\n");
         }
     }
 
     /**
      * Gets the "selected items" that were submitted in the request as a parameter.
      *
      * @param nc    current navigation context
      * @return      array of selected values
      */
     public static String[] getSelectedItems(NavigationContext nc)
     {
         HttpServletRequest request = nc.getHttpRequest();
         return request.getParameterValues("selectedItemList");
     }
 }
