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
 * $Id: QueryBuilderDialog.java,v 1.2 2003-05-31 17:17:42 shahid.shah Exp $
  */
 
 package com.netspective.sparx.form.sql;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.lang.exception.NestableRuntimeException;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 
 import com.netspective.sparx.form.Dialog;
 import com.netspective.sparx.form.DialogFlags;
 import com.netspective.sparx.form.DialogContext;
import com.netspective.sparx.form.DialogDirector;
 import com.netspective.sparx.form.DialogExecuteException;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFields;
 import com.netspective.sparx.form.field.conditional.DialogFieldConditionalDisplay;
 import com.netspective.sparx.form.field.type.SelectField;
 import com.netspective.sparx.form.field.type.TextField;
 import com.netspective.sparx.form.field.type.SeparatorField;
 import com.netspective.sparx.form.field.type.BooleanField;
 import com.netspective.sparx.form.field.type.DataSourceNavigatorButtonsField;
 import com.netspective.sparx.report.tabular.HtmlTabularReportDataSourceScrollStates;
 import com.netspective.sparx.panel.QueryReportPanel;
 import com.netspective.axiom.sql.dynamic.QueryDefinition;
 import com.netspective.axiom.sql.dynamic.QueryDefnSelects;
 import com.netspective.axiom.sql.dynamic.QueryDefnSelect;
 import com.netspective.axiom.sql.dynamic.QueryDefnCondition;
 import com.netspective.axiom.sql.dynamic.SqlComparisonEnumeratedAttribute;
 import com.netspective.axiom.sql.dynamic.QueryDefnConditionConnectorEnumeratedAttribute;
 import com.netspective.axiom.sql.dynamic.QueryDefnSortFieldReference;
 import com.netspective.axiom.sql.dynamic.QueryDefnFieldReference;
 import com.netspective.axiom.sql.dynamic.exception.QueryDefnFieldNotFoundException;
 import com.netspective.axiom.sql.dynamic.exception.QueryDefnSqlComparisonNotFoundException;
import com.netspective.axiom.sql.dynamic.exception.QueryDefnJoinNotFoundException;
 import com.netspective.axiom.sql.dynamic.exception.QueryDefinitionException;
 import com.netspective.axiom.value.source.QueryDefnSelectsValueSource;
 import com.netspective.axiom.value.source.QueryDefnFieldsValueSource;
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.ValueSources;
 import com.netspective.commons.value.source.StaticListValueSource;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.sparx.report.tabular.HtmlTabularReportDestination;
 import com.netspective.sparx.report.tabular.HtmlTabularReportDestinations;
 import com.netspective.sparx.report.tabular.HtmlTabularReportSkin;
 import com.netspective.sparx.report.tabular.HtmlTabularReportDataSourceScrollState;
 import com.netspective.sparx.report.tabular.destination.HtmlTabularReportBrowserDestination;
 import com.netspective.sparx.report.tabular.destination.HtmlTabularReportFileDestination;
 import com.netspective.sparx.report.tabular.destination.HtmlTabularReportEmailDestination;
 
 public class QueryBuilderDialog extends Dialog
 {
     private static final Log log = LogFactory.getLog(QueryBuilderDialog.class);
 
     public static final String QBDIALOG_QUERYDEFN_NAME_PASSTHRU_FIELDNAME = "query-defn-name";
     public static final String QBDIALOG_RESORT_PARAMNAME = "_qbd_resort";
 
    public static final ValueSource VS_CONDITION_CONNECTORS = new StaticListValueSource(new String[] { "", "and", "or" });
 
     public static final int OUTPUTSTYLE_HTML = 0;
     public static final int OUTPUTSTYLE_TEXT_CSV = 1;
     public static final int OUTPUTSTYLE_TEXT_TAB = 2;
 
     public static final int DESTINATION_BROWSER_PAGED = 0;
     public static final int DESTINATION_BROWSER = 1;
     public static final int DESTINATION_DOWNLOAD_FILE = 2;
     public static final int DESTINATION_EMAIL = 3;
 
     private int maxConditions;
     private QueryDefinition queryDefn;
     private String reportId;
 
     public class ConditionField extends DialogField
     {
         SelectField queryFieldsSelect;
         SelectField compareSelect;
         TextField valueText;
 
         public ConditionField(int index, int lastConditionNum, ValueSource fieldsList, ValueSource compList)
         {
             super();
             setName("condition_" + index);
 
             // add the field first because the dialog expects parents to be added before children
             QueryBuilderDialog.this.addField(this);
 
             queryFieldsSelect = new SelectField();
             queryFieldsSelect.setName("field");
             queryFieldsSelect.setChoices(fieldsList);
 
             compareSelect = new SelectField();
             compareSelect.setName("compare");
             compareSelect.setChoices(compList);
 
             valueText = new TextField();
             valueText.setName("value");
 
             addField(queryFieldsSelect);
             addField(compareSelect);
             addField(valueText);
 
             if(index != lastConditionNum)
             {
                 SelectField joinSelect = new SelectField();
                 joinSelect.setName("join");
                 joinSelect.setChoices(VS_CONDITION_CONNECTORS);
                 addField(joinSelect);
             }
 
             if(index > 0)
             {
 
                 addConditional(new DialogFieldConditionalDisplay(this, "condition_" + (index - 1) + ".join", "control.value != ' '"));
             }
         }
 
         public boolean isAvailable(DialogContext dc)
         {
             if(! super.isAvailable(dc))
                 return false;
 
             State condState = dc.getFieldStates().getState(this);
             if(condState.getStateFlags().flagIsSet(Flags.UNAVAILABLE))
                 return false;
 
             State valueState = dc.getFieldStates().getState(valueText);
             return dc.inExecuteMode() ? valueState.hasRequiredValue() : true;
         }
     }
 
     public QueryBuilderDialog()
     {
     }
 
     public DialogFlags createDialogFlags()
     {
         return new QueryBuilderDialogFlags();
     }
 
     public void addInputFields()
     {
         int lastConditionNum = maxConditions - 1;
         QueryDefnFieldsValueSource fieldsList = getQueryDefn().getFieldsValueSource();
         ValueSource compList = ValueSources.getInstance().getValueSource("sql-comparisons:all", ValueSources.VSNOTFOUNDHANDLER_THROW_EXCEPTION);
 
         DialogField hiddenName = new DialogField();
         hiddenName.setName(QBDIALOG_QUERYDEFN_NAME_PASSTHRU_FIELDNAME);
         hiddenName.getFlags().setFlag(DialogField.Flags.INPUT_HIDDEN);
         addField(hiddenName);
 
         SeparatorField separator = new SeparatorField();
         separator.setName("conditions_separator");
         separator.setHeading(new StaticValueSource("Conditions"));
         addField(separator);
 
         for(int i = 0; i < maxConditions; i++)
         {
             // condition field will add itself
             new ConditionField(i, lastConditionNum, fieldsList, compList);
         }
     }
 
     public void addResultsSepatorField()
     {
         SeparatorField separator = new SeparatorField();
         separator.setName("results_separator");
         separator.setHeading(new StaticValueSource("Results"));
         addField(separator);
     }
 
     public void addOutputDestinationFields()
     {
         DialogField output = new DialogField();
         output.setName("output");
         output.getFlags().setFlag(DialogField.Flags.SHOW_CAPTION_AS_CHILD);
         addField(output);
 
         SelectField outputStyle = new SelectField();
         outputStyle.setName("style");
         outputStyle.setCaption(new StaticValueSource("Style"));
         outputStyle.setChoices(ValueSources.getInstance().getValueSource("text-list:[;]HTML=0;CSV Text File=1;Tab-delimited Text File=2", ValueSources.VSNOTFOUNDHANDLER_THROW_EXCEPTION));
         outputStyle.setDefault(new StaticValueSource("0"));
         output.addField(outputStyle);
 
         /* the numbers should match com.netspective.sparx.xaf.report.ReportDestination.DEST_* */
 
         SelectField outputDest = new SelectField();
         outputDest.setName("destination");
         outputDest.setCaption(new StaticValueSource("Destination"));
         outputDest.setChoices(ValueSources.getInstance().getValueSource("text-list:[;]Browser (HTML) multiple pages=0;Browser (HTML) single page=1;Download File=2;E-mail as Attachment=3", ValueSources.VSNOTFOUNDHANDLER_THROW_EXCEPTION));
         outputDest.setDefault(new StaticValueSource("0"));
         output.addField(outputDest);
 
         SelectField rowsPerPage = new SelectField();
         rowsPerPage.setName("rows_per_page");
         rowsPerPage.setChoices(ValueSources.getInstance().getValueSource("text-list:[;]10 rows per page=10;20 rows per page=20;30 rows per page=30", ValueSources.VSNOTFOUNDHANDLER_THROW_EXCEPTION));
         rowsPerPage.setDefault(new StaticValueSource("10"));
         rowsPerPage.addConditional(new DialogFieldConditionalDisplay(rowsPerPage, "output.destination", "control.selectedIndex == 0"));
         output.addField(rowsPerPage);
     }
 
     public void addDisplayOptionsFields()
     {
         ValueSource fieldsList = getQueryDefn().getFieldsValueSource();
 
         SelectField predefinedSels = null;
         QueryDefnSelects predefinedSelects = queryDefn.getSelects();
         if(predefinedSelects.size() > 0)
         {
             predefinedSels = new SelectField();
             predefinedSels.setName("predefined_select");
             predefinedSels.setCaption(new StaticValueSource("Display"));
             predefinedSels.setChoices(getQueryDefn().getSelectsValueSource());
         }
 
         SelectField displayFields = new SelectField();
         displayFields.setName("display_fields");
         displayFields.setStyle(new SelectField.Style(SelectField.Style.MULTIDUAL));
         displayFields.setMultiDualWidth(150);
         displayFields.setSize(7);
         displayFields.setChoices(fieldsList);
 
         SelectField sortFields = new SelectField();
         sortFields.setName("sort_fields");
         sortFields.setStyle(new SelectField.Style(SelectField.Style.MULTIDUAL));
         sortFields.setMultiDualCaptionLeft(new StaticValueSource("Available Sort Fields"));
         sortFields.setMultiDualCaptionRight(new StaticValueSource("Sort Fields"));
         sortFields.setMultiDualWidth(150);
         sortFields.setSize(5);
         sortFields.setChoices(fieldsList);
 
         if(predefinedSels != null)
         {
             displayFields.addConditional(new DialogFieldConditionalDisplay(displayFields, "options.predefined_select", "control.options[control.selectedIndex].value == '" + QueryDefnSelectsValueSource.CUSTOMIZE_TEXT + "'"));
             sortFields.addConditional(new DialogFieldConditionalDisplay(sortFields, "options.predefined_select", "control.options[control.selectedIndex].value == '" + QueryDefnSelectsValueSource.CUSTOMIZE_TEXT + "'"));
         }
 
         DialogField options = new DialogField();
         options.setName("options");
         options.getFlags().setFlag(DialogField.Flags.SHOW_CAPTION_AS_CHILD);
         addField(options);
 
         if(predefinedSels != null)
             options.addField(predefinedSels);
 
         if(getDialogFlags().flagIsSet(QueryBuilderDialogFlags.ALLOW_DEBUG))
         {
             BooleanField debugField = new BooleanField();
             debugField.setName("debug");
            debugField.setCaption(new StaticValueSource("View Generated SQL"));
             debugField.setStyle(new BooleanField.Style(BooleanField.Style.CHECK));
             options.addField(debugField);
         }
 
         addField(displayFields);
         addField(sortFields);
     }
 
 
     public void createContents()
     {
         addInputFields();
         addResultsSepatorField();
         addOutputDestinationFields();
         addDisplayOptionsFields();
         addField(new DataSourceNavigatorButtonsField());
     }
 
     public String getReportId()
     {
         return reportId;
     }
 
     public void setReportId(String reportId)
     {
         this.reportId = reportId;
     }
 
     public int getMaxConditions()
     {
         return maxConditions;
     }
 
     public void setMaxConditions(int value)
     {
         maxConditions = value;
     }
 
     public QueryDefinition getQueryDefn()
     {
         return queryDefn;
     }
 
     public void setQueryDefn(QueryDefinition queryDefn)
     {
         this.queryDefn = queryDefn;
     }
 
     public void finalizeContents(ServletContext context)
     {
        super.finalizeContents(context);
         createContents();
     }
 
     public void makeStateChanges(DialogContext dc, int stage)
     {
         DialogFields fields = getFields();
         for(int i = 0; i < fields.size(); i++)
         {
             DialogField field = fields.get(i);
             field.makeStateChanges(dc, stage);
         }
 
         QueryBuilderDialogFlags dFlags = (QueryBuilderDialogFlags) getDialogFlags();
         DialogContext.DialogFieldStates states = dc.getFieldStates();
 
         if(dc.inExecuteMode() && stage == DialogContext.STATECALCSTAGE_FINAL)
         {
             states.getState("conditions_separator").getStateFlags().setFlag(DialogField.Flags.UNAVAILABLE);
             states.getState("results_separator").getStateFlags().setFlag(DialogField.Flags.UNAVAILABLE);
             states.getState("output").getStateFlags().setFlag(DialogField.Flags.UNAVAILABLE);
             states.getState("options").getStateFlags().setFlag(DialogField.Flags.UNAVAILABLE);
             states.getState("display_fields").getStateFlags().setFlag(DialogField.Flags.UNAVAILABLE);
             states.getState("sort_fields").getStateFlags().setFlag(DialogField.Flags.UNAVAILABLE);
             states.getState(getDirector()).getStateFlags().setFlag(DialogField.Flags.UNAVAILABLE);
             states.getState("ds_nav_buttons").getStateFlags().clearFlag(DialogField.Flags.UNAVAILABLE);
 
             int flag = dFlags.flagIsSet(QueryBuilderDialogFlags.HIDE_CRITERIA) ? DialogField.Flags.UNAVAILABLE : DialogField.Flags.READ_ONLY;
             for(int i = 0; i < maxConditions; i++)
                 states.getState("condition_" + i).getStateFlags().setFlag(flag);
         }
         else
         {
             if(dFlags.flagIsSet(QueryBuilderDialogFlags.HIDE_OUTPUT_DESTS))
                 states.getState("output").getStateFlags().setFlag(DialogField.Flags.UNAVAILABLE);
             states.getState("ds_nav_buttons").getStateFlags().setFlag(DialogField.Flags.UNAVAILABLE);
         }
     }
 
     public QueryDefnSelect createSelect(DialogContext dc) throws QueryDefnFieldNotFoundException, QueryDefnSqlComparisonNotFoundException, QueryDefinitionException
     {
         DialogContext.DialogFieldStates states = dc.getFieldStates();
         QueryDefnSelect result = new QueryDefnSelect(queryDefn);
 
         boolean customizing = true;
         DialogField.State predefinedSelState = states.getState("options.predefined_select");
         if(predefinedSelState.hasRequiredValue())
         {
             String predefinedSel = predefinedSelState.getValue().getTextValue();
             if(! predefinedSel.equals(QueryDefnSelectsValueSource.CUSTOMIZE_TEXT))
             {
                 customizing = false;
                 QueryDefnSelect sel = queryDefn.getSelects().get(predefinedSel);
                 if(sel != null)
                     result.copy(sel);
                 else
                     throw new RuntimeException("QueryDefnSelect '" + predefinedSel + "' not found.");
             }
         }
 
         if(customizing)
         {
             String[] display = states.getState("display_fields").getValue().getTextValues();
             if(display != null && display.length > 0)
             {
                 for(int i = 0; i < display.length; i++)
                 {
                     QueryDefnFieldReference ref = new QueryDefnFieldReference(getQueryDefn());
                     ref.setField(display[i]);
                     result.addDisplay(ref);
                 }
             }
         }
 
         int lastCondIndex = maxConditions-1;
         for(int i = 0; i < maxConditions; i++)
         {
             String conditionId = "condition_" + i;
             String value = states.getState(conditionId + ".value").getValue().getTextValue();
             String join = i < lastCondIndex ? states.getState(conditionId + ".join").getValue().getTextValue() : null;
 
             if(value != null && value.length() > 0)
             {
                 QueryDefnCondition cond = result.createCondition();
                 cond.setField(states.getState(conditionId + ".field").getValue().getTextValue());
                 SqlComparisonEnumeratedAttribute comparison = new SqlComparisonEnumeratedAttribute();
                 comparison.setValue(states.getState(conditionId + ".compare").getValue().getTextValue());
                 cond.setComparison(comparison);
                 cond.setValue(new StaticValueSource(value));
                 QueryDefnConditionConnectorEnumeratedAttribute conn = new QueryDefnConditionConnectorEnumeratedAttribute();
                 conn.setValue(join);
                 cond.setConnector(conn);
                 result.addCondition(cond);
             }
 
             if(join == null || join.equals(" "))
                 break;
         }
 
         if(customizing)
         {
             String[] sort = states.getState("sort_fields").getValue().getTextValues();
             if(sort != null && sort.length > 0)
             {
                 for(int i = 0; i < sort.length; i++)
                 {
                     QueryDefnSortFieldReference sortFieldRef = new QueryDefnSortFieldReference(queryDefn);
                     sortFieldRef.setField(new StaticValueSource(sort[i]));
                     result.addOrderBy(sortFieldRef);
                 }
             }
         }
 
         return result;
     }
 
     protected String getFirstAvailableFieldValue(DialogContext dc, String[] fieldNames, String defaultValue)
     {
         DialogFields fields = getFields();
         DialogContext.DialogFieldStates dfStates = dc.getFieldStates();
 
         for(int i = 0; i < fieldNames.length; i++)
         {
             String fieldName = fieldNames[i];
             DialogField field = fields.getByQualifiedName(fieldName);
             if(field != null)
             {
                 String value = dfStates.getState(field).getValue().getTextValue();
                 if(value != null && value.length() > 0)
                     return value;
             }
         }
 
         return defaultValue;
     }
 
     /*
     TODO: implement this
     public void handleSortOrderChange(DialogContext dc, QuerySelectScrollState activeState, String resortBy)
     {
         try
         {
             activeState.populateData(dc);
         }
         catch(Exception e)
         {
             throw new RuntimeException(e.toString());
         }
 
         QueryDefinition.QueryFieldSortInfo activeSortFieldInfo = activeState.getSortFieldInfo();
         String activeSortFieldName = activeSortFieldInfo != null ? activeSortFieldInfo.getField().getName() : null;
         if(activeSortFieldInfo != null && resortBy.equals(activeSortFieldName))
             resortBy = activeSortFieldInfo.isDescending() ? resortBy : ("-" + resortBy);
 
         dc.setValue("sort_order", resortBy);
     }
     */
 
     public void renderReport(DialogContext dc, HtmlTabularReportDestination destination, HtmlTabularReportSkin reportSkin) throws IOException, QueryDefinitionException
     {
         QueryReportPanel reportPanel = null;
 
         if(destination instanceof HtmlTabularReportBrowserDestination)
         {
             HtmlTabularReportBrowserDestination browserDest = (HtmlTabularReportBrowserDestination) destination;
             HtmlTabularReportDataSourceScrollStates scrollStatesManager = HtmlTabularReportDataSourceScrollStates.getInstance();
             HtmlTabularReportDataSourceScrollState scrollStateById = scrollStatesManager.getScrollStateByDialogTransactionId(dc);
 
             /*
                 If the state is not found, then we have not executed at all yet;
                 if the state is found and it's the initial execution then it means
                 that the user has pressed the "back" button -- which means we
                 should reset the state management.
              */
             if(scrollStateById == null || (scrollStateById != null && dc.isInitialExecute()))
             {
                 // if our transaction does not have a scroll state, but there is an active scroll state available, then it
                 // means that we need to close the previous one and remove the attribute so that the connection can be
                 // closed and returned to the pool
                 HtmlTabularReportDataSourceScrollState activeScrollState = scrollStatesManager.getActiveScrollState(dc);
 
                 String resortBy = dc.getRequest().getParameter(QBDIALOG_RESORT_PARAMNAME);
                 if(activeScrollState != null && resortBy != null)
                 {
                     // TODO
                     //handleSortOrderChange(dc, activeState, resortBy);
                 }
 
                 if(activeScrollState != null && ! getDialogFlags().flagIsSet(QueryBuilderDialogFlags.ALLOW_MULTIPLE_SCROLL_STATES))
                     scrollStatesManager.removeActiveState(dc, activeScrollState);
 
                 QueryDefnSelect select = createSelect(dc);
                 reportPanel = new QueryReportPanel();
                 reportPanel.setQuery(select);
                 if(browserDest.isScrollable())
                 {
                     reportPanel.setScrollable(true);
                     reportPanel.setScrollRowsPerPage(browserDest.getPageSize());
                 }
             }
             else
             {
                 reportPanel = (QueryReportPanel) scrollStateById.getPanel();
             }
         }
         else
         {
             QueryDefnSelect select = createSelect(dc);
             reportPanel = new QueryReportPanel();
             reportPanel.setQuery(select);
         }
 
         destination.render(reportPanel, reportSkin);
     }
 
     public void execute(Writer writer, DialogContext dc) throws IOException, DialogExecuteException
     {
         DialogContext.DialogFieldStates states = dc.getFieldStates();
         String debugStr = states.getState("options.debug").getValue().getTextValue();
         if(debugStr != null && debugStr.equals("1"))
         {
             try
             {
                 QueryDefnSelect select = createSelect(dc);
                 ValueSource dataSource = queryDefn.getDataSrc();
                 ConnectionContext cc = dc.getConnection(dataSource != null ? dataSource.getTextValue(dc) : null, false);
                 String message = select.createExceptionMessage(cc, null);
                 cc.close();
                 writer.write("<p><pre><code>" + message + "</code></pre>");
                 return;
             }
             catch (Exception e)
             {
                 log.error("Error trying to get debug SQL", e);
                 throw new NestableRuntimeException();
             }
         }
 
         String outputStyleStr = getFirstAvailableFieldValue(dc, new String[] { "output_style", "output-style", "output.style" }, "0");
         String outputDestStr = getFirstAvailableFieldValue(dc, new String[] { "output_destination", "output-destination", "output.destination" }, "0");
 
         int outputStyle = Integer.parseInt(outputStyleStr);
         int outputDest = Integer.parseInt(outputDestStr);
 
         HtmlTabularReportDestinations desintations = HtmlTabularReportDestinations.getInstance();
         HtmlTabularReportDestination destination = null;
         HtmlTabularReportSkin reportSkin = null;
 
         switch(outputDest)
         {
             case DESTINATION_BROWSER:
                 HtmlTabularReportBrowserDestination browserDest = desintations.createBrowserDestination(writer, dc);
                 destination = browserDest;
                 break;
 
             case DESTINATION_BROWSER_PAGED:
                 String rowsPerPageStr = getFirstAvailableFieldValue(dc, new String[] { "rows_per_page", "rows-per-page", "output.rows_per_page" }, "20");
                 browserDest = desintations.createBrowserDestination(writer, dc, Integer.parseInt(rowsPerPageStr));
                 destination = browserDest;
                 break;
 
             case DESTINATION_DOWNLOAD_FILE:
                 HtmlTabularReportFileDestination fileDest = desintations.createDownloadableFileDestination();
                 destination = fileDest;
                 break;
 
             case DESTINATION_EMAIL:
                 HtmlTabularReportEmailDestination emailDest = desintations.createEmailDestination();
                 destination = emailDest;
                 break;
 
             default:
                 writer.write("Destination" + outputDest + " is unknown.");
                 return;
         }
 
         switch(outputStyle)
         {
             case OUTPUTSTYLE_HTML:
                 String reportSkinName = getFirstAvailableFieldValue(dc, new String[] { "report_skin", "report-skin", "output.report_skin" }, "report");
                 reportSkin = dc.getActiveTheme().getReportSkin(reportSkinName);
                 break;
 
             case OUTPUTSTYLE_TEXT_CSV:
                 reportSkin = dc.getActiveTheme().getReportSkin("text-csv");
                 break;
 
             case OUTPUTSTYLE_TEXT_TAB:
                 reportSkin = dc.getActiveTheme().getReportSkin("text-tab");
                 break;
 
             default:
                 writer.write("Output Style " + outputStyle + " is unknown.");
                 return;
         }
 
         try
         {
             renderReport(dc, destination, reportSkin);
         }
         catch (Exception e)
         {
             log.error("Exception while trying to render report", e);
             throw new DialogExecuteException(e);
         }
     }
 
     /**
      * return the output from the execute method or the execute method and the dialog (which contains
      * the ResultSetNavigagors next/prev buttons). If there is only one page or scrolling is not being
      * performed (state == null) then only show the output of the query. However, if there is more than
      * one page or the number of pages is unknown, then show the entire dialog.
      */
 
     public void render(Writer writer, DialogContext dc, boolean contextPreparedAlready) throws IOException, DialogExecuteException
     {
         if(!contextPreparedAlready)
             prepareContext(dc);
 
         if(dc.inExecuteMode())
         {
             execute(writer, dc);
 
             HtmlTabularReportDataSourceScrollStates scrollStatesManager = HtmlTabularReportDataSourceScrollStates.getInstance();
             HtmlTabularReportDataSourceScrollState scrollState = scrollStatesManager.getScrollStateByDialogTransactionId(dc);
 
             if(scrollState != null)
             {
                 int totalPages = scrollState.getTotalPages();
                 if(getDialogFlags().flagIsSet(QueryBuilderDialogFlags.ALWAYS_SHOW_DSNAV) || (totalPages == -1 || totalPages > 1))
                 {
                     writer.write(getLoopSeparator());
                     dc.getSkin().renderHtml(writer, dc);
                 }
                 else
                     scrollStatesManager.removeActiveState(dc, scrollState);
             }
         }
         else
         {
             dc.getSkin().renderHtml(writer, dc);
         }
     }
 }
