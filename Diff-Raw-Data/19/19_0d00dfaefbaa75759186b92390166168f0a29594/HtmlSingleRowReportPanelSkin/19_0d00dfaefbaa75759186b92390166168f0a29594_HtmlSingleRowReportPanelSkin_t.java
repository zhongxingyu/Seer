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
 * $Id: HtmlSingleRowReportPanelSkin.java,v 1.2 2003-08-30 14:06:36 shahid.shah Exp $
  */
 
 package com.netspective.sparx.theme.basic;
 
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.report.tabular.HtmlTabularReportValueContext;
 import com.netspective.sparx.report.tabular.HtmlTabularReportDataSource;
 import com.netspective.sparx.report.tabular.HtmlTabularReport;
 import com.netspective.sparx.command.RedirectCommand;
 import com.netspective.commons.xdm.XdmBitmaskedFlagsAttribute;
 import com.netspective.commons.report.tabular.TabularReportColumns;
 import com.netspective.commons.report.tabular.TabularReportColumnState;
 import com.netspective.commons.report.tabular.TabularReportColumn;
 import com.netspective.commons.command.Command;
 
 import java.io.Writer;
 import java.io.IOException;
 
 /**
  */
 public class HtmlSingleRowReportPanelSkin extends BasicHtmlTabularReportPanelSkin
 {
     public static class Flags extends BasicHtmlTabularReportPanelSkin.Flags
     {
         public static final int SKIP_NULL_COLUMNS = BasicHtmlTabularReportPanelSkin.Flags.STARTCUSTOM;
         public static final int STARTCUSTOM = SKIP_NULL_COLUMNS * 2;
 
         public static final XdmBitmaskedFlagsAttribute.FlagDefn[] FLAGDEFNS = new XdmBitmaskedFlagsAttribute.FlagDefn[BasicHtmlTabularReportPanelSkin.Flags.FLAGDEFNS.length + 2];
 
         static
         {
             for(int i = 0; i < BasicHtmlTabularReportPanelSkin.Flags.FLAGDEFNS.length; i++)
                 FLAGDEFNS[i] = BasicHtmlTabularReportPanelSkin.Flags.FLAGDEFNS[i];
             FLAGDEFNS[BasicHtmlTabularReportPanelSkin.Flags.FLAGDEFNS.length + 0] = new FlagDefn(ACCESS_XDM, "SKIP_NULL_COLUMNS", SKIP_NULL_COLUMNS);
         }
 
         public XdmBitmaskedFlagsAttribute.FlagDefn[] getFlagsDefns()
         {
             return FLAGDEFNS;
         }
     }
 
     protected int tableCols;
     protected boolean horizontalLayout;
 
     public HtmlSingleRowReportPanelSkin()
     {
         super();
         this.tableCols = 2;
         this.horizontalLayout = true;
         flags.setFlag(Flags.SHOW_BANNER | Flags.SKIP_NULL_COLUMNS);
         flags.clearFlag(Flags.SHOW_HEAD_ROW | Flags.SHOW_FOOT_ROW);
     }
 
     public HtmlSingleRowReportPanelSkin(Theme theme, String panelClassNamePrefix, String panelResourcesPrefix, boolean fullWidth, int tableCols, boolean horizontalLayout)
     {
         super(theme, panelClassNamePrefix, panelResourcesPrefix, fullWidth);
         this.tableCols = tableCols;
         this.horizontalLayout = horizontalLayout;
         flags.setFlag(Flags.SHOW_BANNER | Flags.SKIP_NULL_COLUMNS);
         flags.clearFlag(Flags.SHOW_HEAD_ROW | Flags.SHOW_FOOT_ROW);
     }
 
     public BasicHtmlPanelSkin.Flags createFlags()
     {
         return new Flags();
     }
 
     public int produceDataRows(Writer writer, HtmlTabularReportValueContext rc, HtmlTabularReportDataSource ds) throws IOException
     {
         if(!ds.next())
         {
             writer.write("<tr><td class=\"report-detail\">No data found.</td></tr>");
             return 0;
         }
 
         HtmlTabularReport defn = (HtmlTabularReport) rc.getReport();
         TabularReportColumns columns = rc.getColumns();
         int dataColsCount = columns.size();
         TabularReportColumnState[] states = rc.getStates();
 
         StringBuffer dataTable = new StringBuffer();
         if(horizontalLayout)
         {
             int colCount = 0;
             dataTable.append("<tr>");
             for(int i = 0; i < dataColsCount; i++)
             {
                // spacer before the column
                dataTable.append("<td class=\"report-detail\">&nbsp;&nbsp;</td>");

                 TabularReportColumn column = columns.getColumn(i);
                 if(column.getBreak() != null)
                 {
                     // TODO: Need to define the style for the column break font style
                     dataTable.append("<td height='10'></td></tr><tr><td align='left' bgcolor='#FFFBA5' colspan='2'><table border=0 cellspacing=0>");
                     dataTable.append("<tr><td align='left' class=\"\">" + column.getBreak() + "</td></tr>");
                     dataTable.append("</table></td></tr><tr height='2' bgcolor='#ABA61B'><td colspan='2'></td><tr>");
                 }
 
                 TabularReportColumnState state = states[i];
                 if(!state.isVisible())
                     continue;
 
                 if(flags.flagIsSet(Flags.SKIP_NULL_COLUMNS) && ds.getActiveRowColumnData(column.getColIndex(), 0) == null)
                     continue;
 
                 String data =
                         state.getFlags().flagIsSet(TabularReportColumn.Flags.HAS_OUTPUT_PATTERN) ?
                         state.getOutputFormat() :
                         column.getFormattedData(rc, ds, TabularReportColumn.GETDATAFLAG_FOR_URL);
                 Command command = column.getCommand();
                 if (command != null && command instanceof RedirectCommand)
                 {
                     data = rc.getSkin().constructRedirect(rc, column.getCommand(), data, null, null);
                 }
 
                String heading = column.getHeading() != null ? column.getHeading().getValue(rc).getTextValue() : "&nbsp;";
                dataTable.append("<td class=\"report-field-detail\"><nobr>" + heading + "</nobr></td>");
                 dataTable.append("<td class=\"report-detail\" style=\""+ state.getCssStyleAttrValue() + "\">" +
                         data + "</td>");
 
                // spacer after the column
                dataTable.append("<td class=\"report-detail\">&nbsp;&nbsp;</td>");

                 colCount++;
                 if(colCount >= tableCols)
                 {
                     dataTable.append("</tr><tr>");
                     colCount = 0;
                 }
             }
             dataTable.append("</tr>");
             writer.write(defn.replaceOutputPatterns(rc, ds, dataTable.toString()));
         }
         else
         {
            writer.write("Vertical layout is not supported yet.");
         }
         return 1;
     }
 }
