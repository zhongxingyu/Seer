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
 * $Id: BasicHtmlTabularReportPanelSkin.java,v 1.33 2004-08-10 20:20:30 aye.thu Exp $
  */
 
 package com.netspective.sparx.theme.basic;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.netspective.commons.lang.ClassPath;
 import com.netspective.commons.report.tabular.TabularReportColumn;
 import com.netspective.commons.report.tabular.TabularReportColumnState;
 import com.netspective.commons.report.tabular.TabularReportColumns;
 import com.netspective.commons.report.tabular.TabularReportDataSource;
 import com.netspective.commons.report.tabular.TabularReportValueContext;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.source.RedirectValueSource;
 import com.netspective.commons.xdm.XdmBitmaskedFlagsAttribute;
 import com.netspective.sparx.form.sql.QueryDialog;
 import com.netspective.sparx.panel.HtmlPanel;
 import com.netspective.sparx.panel.HtmlTabularReportPanel;
 import com.netspective.sparx.report.tabular.BasicHtmlTabularReport;
 import com.netspective.sparx.report.tabular.HtmlReportAction;
 import com.netspective.sparx.report.tabular.HtmlReportActions;
 import com.netspective.sparx.report.tabular.HtmlTabularReport;
 import com.netspective.sparx.report.tabular.HtmlTabularReportDataSource;
 import com.netspective.sparx.report.tabular.HtmlTabularReportDataSourceScrollState;
 import com.netspective.sparx.report.tabular.HtmlTabularReportSkin;
 import com.netspective.sparx.report.tabular.HtmlTabularReportValueContext;
 import com.netspective.sparx.theme.Theme;
 
 public class BasicHtmlTabularReportPanelSkin extends BasicHtmlPanelSkin implements HtmlTabularReportSkin
 {
     public static class Flags extends BasicHtmlPanelSkin.Flags
     {
         public static final int SHOW_HEAD_ROW = BasicHtmlPanelSkin.Flags.STARTCUSTOM;
         public static final int SHOW_FOOT_ROW = SHOW_HEAD_ROW * 2;
         public static final int ALLOW_TREE_EXPAND_COLLAPSE = SHOW_FOOT_ROW * 2;
         public static final int SHOW_SELECTION_COMMAND_HEAD_ROW = ALLOW_TREE_EXPAND_COLLAPSE * 2;       // show the selectable report's command at the top of the frame
         public static final int SHOW_SELECTION_COMMAND_FOOT_ROW = SHOW_SELECTION_COMMAND_HEAD_ROW * 2;  // show the selectable report's command at the bottom of the frame
         public static final int STARTCUSTOM = ALLOW_TREE_EXPAND_COLLAPSE * 2;
 
        public static final XdmBitmaskedFlagsAttribute.FlagDefn[] FLAGDEFNS = new XdmBitmaskedFlagsAttribute.FlagDefn[BasicHtmlPanelSkin.Flags.FLAGDEFNS.length + 5];
 
         static
         {
             for(int i = 0; i < BasicHtmlPanelSkin.Flags.FLAGDEFNS.length; i++)
                 FLAGDEFNS[i] = BasicHtmlPanelSkin.Flags.FLAGDEFNS[i];
             FLAGDEFNS[BasicHtmlPanelSkin.Flags.FLAGDEFNS.length + 0] = new FlagDefn(ACCESS_XDM, "SHOW_HEAD_ROW", SHOW_HEAD_ROW);
             FLAGDEFNS[BasicHtmlPanelSkin.Flags.FLAGDEFNS.length + 1] = new FlagDefn(ACCESS_XDM, "SHOW_FOOT_ROW", SHOW_FOOT_ROW);
            FLAGDEFNS[BasicHtmlPanelSkin.Flags.FLAGDEFNS.length + 2] = new FlagDefn(ACCESS_XDM, "ALLOW_TREE_EXPAND_COLLAPSE", ALLOW_TREE_EXPAND_COLLAPSE);
            FLAGDEFNS[BasicHtmlPanelSkin.Flags.FLAGDEFNS.length + 3] = new FlagDefn(ACCESS_XDM, "SHOW_SELECTION_COMMAND_HEAD_ROW", SHOW_SELECTION_COMMAND_HEAD_ROW);
            FLAGDEFNS[BasicHtmlPanelSkin.Flags.FLAGDEFNS.length + 4] = new FlagDefn(ACCESS_XDM, "SHOW_SELECTION_COMMAND_FOOT_ROW", SHOW_SELECTION_COMMAND_FOOT_ROW);
         }
 
         public XdmBitmaskedFlagsAttribute.FlagDefn[] getFlagsDefns()
         {
             return FLAGDEFNS;
         }
     }
 
     public BasicHtmlTabularReportPanelSkin()
     {
         super();
         flags.setFlag(Flags.SHOW_HEAD_ROW | Flags.SHOW_FOOT_ROW);
     }
 
     public BasicHtmlTabularReportPanelSkin(Theme theme, String name, String panelClassNamePrefix, String panelResourcesPrefix, boolean fullWidth)
     {
         super(theme, name, panelClassNamePrefix, panelResourcesPrefix, fullWidth);
         flags.setFlag(Flags.SHOW_HEAD_ROW | Flags.SHOW_FOOT_ROW);
     }
 
     public BasicHtmlPanelSkin.Flags createFlags()
     {
         return new Flags();
     }
 
     public String constructClassRef(Class cls)
     {
         String className = cls.getName();
         if(className.startsWith("com.netspective"))
             className = "~" + className.substring("com.netspective".length());
         return "<span title=\""+ ClassPath.getClassFileName(cls) +"\">" + className + "</span>";
     }
 
     /**
      * Constructs the redirect URL from the passed in command
      * @param rc
      * @param redirect
      * @param label
      * @param hint
      * @param target
      * @return
      */
     public String constructRedirect(TabularReportValueContext rc, ValueSource redirect, String label, String hint, String target)
     {
         if (redirect instanceof RedirectValueSource)
         {
             StringBuffer sb = new StringBuffer();
             String url = ((RedirectValueSource)redirect).getUrl(rc);
             if (url.startsWith("javascript"))
             {
                 sb.append("<a href=\"#\" onclick=\"" + url + "\"");
             }
             else
             {
                 sb.append("<a href='" + url + "'");
 
             }
             if (hint != null)
                 sb.append(" title=\"" + hint + "\"");
             if (target != null)
                 sb.append(" target=\"" + target + "\"");
             sb.append(">" + label + "</a>");
             return sb.toString();
         }
         return null;
     }
 
     public String getBlankValue()
     {
         return "&nbsp;";
     }
 
     public String getFileExtension()
     {
         return ".html";
     }
 
     public void render(Writer writer, TabularReportValueContext rc, TabularReportDataSource ds) throws IOException
     {
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
 
             if(flags.flagIsSet(Flags.SHOW_FOOT_ROW) && rc.getCalcsCount() > 0)
                 produceFootRow(writer, rvc);
 
             // TODO: Need to check the flag to find out where the command item for the selectable report should be
             produceSelectableCommandRow(writer, rvc);
             writer.write("    </table>\n");
 
             if((panelRenderFlags & HtmlPanel.RENDERFLAG_NOFRAME) == 0)
                 renderFrameEnd(writer, rvc);
         }
     }
 
    protected int getTableColumnsCount(HtmlTabularReportValueContext rc)
     {
         return (rc.getVisibleColsCount() * 2) +
                (getRowDecoratorPrependColsCount(rc) * 2) +
                (getRowDecoratorAppendColsCount(rc) * 2) +
                + 1; // each column has "spacer" in between, first column as spacer before too
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
                  writer.write("        <th class=\"report-column-heading\" nowrap scope=\"col\">&nbsp;&nbsp;</th>");
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
 
     public int produceDataRows(Writer writer, HtmlTabularReportValueContext rc, HtmlTabularReportDataSource ds) throws IOException
     {
         HtmlTabularReport defn = ((HtmlTabularReport) rc.getReport());
         TabularReportColumns columns = rc.getColumns();
         TabularReportColumnState[] states = rc.getStates();
 
         int rowsWritten = 0;
         int dataColsCount = columns.size();
         int tableColsCount = getTableColumnsCount(rc);
 
         boolean hiearchical = ds.isHierarchical();
         boolean allowTreeExpandCollapse = getFlags().flagIsSet(Flags.ALLOW_TREE_EXPAND_COLLAPSE);
         String panelId = rc.getPanel().getPanelIdentifier();
 
         StringBuffer treeScript = null;
         if(hiearchical && allowTreeExpandCollapse)
         {
             treeScript = new StringBuffer();
             treeScript.append("var activeTree = new Tree(\""+ panelId +"\");\n");
             treeScript.append("var activeNode;\n");
         }
 
         HtmlTabularReportDataSourceScrollState scrollState = (HtmlTabularReportDataSourceScrollState) rc.getScrollState();
         boolean paging = scrollState != null;
         boolean isOddRow = false;
 
         int currentPage = 1;
 
         if (paging)
         {
             currentPage = scrollState.getActivePage();
         }
 
         while(ds.next())
         {
             isOddRow = ! isOddRow;
             int activeRow = ds.getActiveRowNumber();
             int hiearchyCol = 0;
             int activeLevel = 0;
             int activeParent = -1;
 
             if(hiearchical)
             {
                 TabularReportDataSource.Hierarchy activeHierarchy = ds.getActiveHierarchy();
                 hiearchyCol = activeHierarchy.getColumn();
                 activeLevel = activeHierarchy.getLevel();
                 activeParent = activeHierarchy.getParentRow();
 
                 if(allowTreeExpandCollapse)
                 {
                     if(activeParent == -1)
                         treeScript.append("activeNode = activeTree.newNode(null, \"node_"+ rowsWritten +"\");\n");
                     else
                         treeScript.append("activeNode = activeTree.newNode(\"node_"+ activeHierarchy.getParentRow() +"\", \"node_"+ rowsWritten +"\");\n");
                     writer.write("<tr id=\""+ panelId + "_node_" + rowsWritten + "\">");
                 }
                 else
                     writer.write("<tr>");
             }
             else
                 writer.write("<tr>");
 
             // construct the HTML for the data columns
             String[] rowData = new String[dataColsCount];
             StringBuffer dataBuffer = new StringBuffer();
             for(int i = 0; i < dataColsCount; i++)
             {
                 TabularReportColumn column = columns.getColumn(i);
                 TabularReportColumnState state = states[i];
 
                 if(! state.isVisible())
                     continue;
 
                 String data =
                         state.getFlags().flagIsSet(TabularReportColumn.Flags.HAS_OUTPUT_PATTERN) ?
                         state.getOutputFormat() :
                         column.getFormattedData(rc, ds, TabularReportColumn.GETDATAFLAG_DO_CALC);
                 RedirectValueSource redirect = column.getRedirect();
                 if (data != null && redirect != null)
                 {
                     String newdata = rc.getSkin().constructRedirect(rc, redirect, data, null, null);
                     data = defn.replaceOutputPatterns(rc, ds, newdata);
                 }
 
                 String style = state.getCssStyleAttrValue();
                 if(hiearchical && (hiearchyCol == i) && activeLevel > 0)
                 {
                     style += "padding-left:" + (activeLevel * 15) + ";";
                     if(allowTreeExpandCollapse)
                         data = "<span id=\""+ panelId + "_node_" + rowsWritten + "_controller\" onclick=\"TREES.toggleNodeExpansion('"+ panelId +"', 'node_"+ rowsWritten +"')\" class=\"panel-output-tree-collapse\">&nbsp;</span>" + data;
                 }
 
                 String singleColumn =
                         "<td class=\"" +
                             (ds.isActiveRowSelected() ? "report-column-selected" : (isOddRow ? "report-column-even" : "report-column-odd")) + "\" style=\"" + style + "\">" +
                         data +
                         "&nbsp;</td>";
                 dataBuffer.append(defn.replaceOutputPatterns(rc, ds, singleColumn));
                 rowData[i] = data;
             }
             produceDataRowDecoratorPrepend(writer, rc, ds, rowData, isOddRow);
             writer.write(dataBuffer.toString());
             produceDataRowDecoratorAppend(writer, rc, ds, rowData, isOddRow);
 
             writer.write("</tr>");
             rowsWritten++;
             // check to see if this row should be the last
             if (paging && rowsWritten == scrollState.getRowsPerPage())
                 break;
 
         }
 
         if (rowsWritten == 0)
         {
             // no rows were written out that means that there was no data in the result set
             ValueSource noDataFoundMsgSrc = ds.getNoDataFoundMessage();
             String noDataFoundMsg = noDataFoundMsgSrc != null ? noDataFoundMsgSrc.getTextValue(rc) : null;
             // add the 'no data found' message
             if(noDataFoundMsg != null)
                 writer.write("<tr><td class=\"report-column-summary\" colspan='" + tableColsCount + "'>"+ noDataFoundMsg +"</td></tr>");
             //TODO: Sparx 2.x conversion required
             if(paging)
                 scrollState.setNoMoreRows();
         }
         //TODO: Sparx 2.x conversion required
         else if (paging)
         {
             // record the number of rows written to the total number ofrows already displayed
             scrollState.accumulateRowsProcessed(rowsWritten);
             // if the total number of rows written is less than the scroll state's number of rows per page setting,
             // this must be the last page
             if(rowsWritten < scrollState.getRowsPerPage())
                 scrollState.setNoMoreRows();
         }
 
         if(hiearchical && allowTreeExpandCollapse)
         {
             treeScript.append("TREES.registerTree(activeTree)\n");
             treeScript.append("activeTree.initialize()\n");
 
             writer.write("<script>\n");
             writer.write(treeScript.toString());
             writer.write("</script>");
         }
 
 
         return rowsWritten;
     }
 
     public void produceFootRow(Writer writer, HtmlTabularReportValueContext rc) throws IOException
     {
         int calcsCount = rc.getCalcsCount();
         if(calcsCount == 0)
             return;
 
         TabularReportColumnState[] states = rc.getStates();
         TabularReportColumns columns = rc.getColumns();
         int dataColsCount = columns.size();
 
         writer.write("<tr>");
         int prependColCount = getRowDecoratorPrependColsCount(rc);
         if (prependColCount > 0)
         {
             for (int k=0; k < prependColCount; k++)
             {
                  writer.write("        <td class=\"report-column-summary\" nowrap>&nbsp;&nbsp;</td>");
             }
         }
         for(int i = 0; i < dataColsCount; i++)
         {
             TabularReportColumn column = columns.getColumn(i);
             if(! states[i].isVisible())
                 continue;
 
             String summary = column.getFormattedData(rc, states[i].getCalc());
             if(summary == null)
                 summary = "&nbsp;";
 
             writer.write("<td class=\"report-column-summary\" style=\""+ states[i].getCssStyleAttrValue() +"\">" + summary + "</td>");
         }
 
         writer.write("</tr>");
     }
 
     /**
      * Produce the HTML for the button/link for the selectable report action processing
      * @param rc
      * @param writer
      * @throws IOException
      */
     public void produceSelectableCommandRow( Writer writer, HtmlTabularReportValueContext rc) throws IOException
     {
         BasicHtmlTabularReport report = (BasicHtmlTabularReport)rc.getReport();
         HtmlReportActions actions = report.getActions();
         StringBuffer sb = new StringBuffer();
         sb.append("    <tr>");
         if (actions != null)
         {
             HtmlReportAction[] reportActions = actions.getByType(HtmlReportAction.Type.RECORD_SELECT);
             if (reportActions != null && reportActions.length > 0)
             {
                 TabularReportColumns columns = rc.getColumns();
                 int colsCount = columns.size() + getRowDecoratorPrependColsCount(rc) + getRowDecoratorAppendColsCount(rc);
                 RedirectValueSource redirect = (RedirectValueSource)reportActions[0].getRedirect();
                 if (redirect != null)
                 {
                     String title = reportActions[0].getTitle() != null ? reportActions[0].getTitle().getTextValue(rc) : "";
                     sb.append("            <td colspan=\"" + colsCount + "\" class=\"report-column-heading\">" +
                             "<a class=\""+ panelClassNamePrefix +"-frame-action\" title=\""+ title + "\" href=\"" +
                              redirect.getUrl(rc)  + "\" onClick=\"return ReportAction_submit(" + QueryDialog.EXECUTE_SELECT_ACTION +
                             ", '"+ redirect.getUrl(rc)  +
                             "')\">&nbsp;" + reportActions[0].getCaption().getTextValue(rc) + "&nbsp;</a></td>");
                 }
             }
         }
         sb.append("    </tr>");
         writer.write(sb.toString());
     }
 
     /**
      * Produces html to prepend to the data row
      * @param writer
      * @param rc
      * @param isOddRow
      * @throws IOException
      */
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
             StringBuffer valueStr = new StringBuffer(activeRowNumber + "\t");
             for (int i=0; i < rowData.length; i++)
             {
                 String colHeading = ds.getHeadingRowColumnData(i);
                 valueStr.append(i != rowData.length-1 ? (colHeading + "=" + rowData[i] + "\t") : (colHeading + "=" + rowData[i]));
             }
 
             writer.write("<td " + (isOddRow ? "class=\"report-column-even\"" : "class=\"report-column-odd\"") + " width=\"10\">");
             writer.write("<input type=\"checkbox\" value=\"" + valueStr.toString() + "\" name=\"checkbox_" + activeRowNumber +
                         "\" title=\"Click here to select the row.\" ");
             HttpServletRequest request = (HttpServletRequest) rc.getRequest();
 
             // get the list of selected rows so that the correct checkboxes can be highlighted
             String[] selectedValues = request.getParameterValues("_dc.selectedItemList");
 
             if (selectedValues != null)
             {
                 for (int i = 0; i < selectedValues.length; i++)
                 {
                     StringTokenizer st = new StringTokenizer(selectedValues[i], "\t");
                     if (st.countTokens() > 0)
                     {
                         int selectedRow = Integer.parseInt(st.nextToken());
 
                         if (selectedRow == activeRowNumber)
                         {
                             writer.write("checked");
                             break;
                         }
                     }
 
                 }
             }
             writer.write(" onClick=\"ReportAction_selectRow(this, 'selected_item_list', '" + rowData + "')\">\n");
             writer.write("</td>");
         }
     }
 
     /**
      * Produces html to append to the data row
      * @param writer
      * @param rc
      * @param isOddRow
      * @throws IOException
      */
     // TODO: Change isOddRow to the ACTUAL row number
     public void produceDataRowDecoratorAppend(Writer writer, HtmlTabularReportValueContext rc, HtmlTabularReportDataSource ds, String[] rowData, boolean isOddRow) throws IOException
     {
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
 
     protected int getRowDecoratorAppendColsCount(HtmlTabularReportValueContext rc)
     {
         return 0;
     }
 }
