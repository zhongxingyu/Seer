 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.views;
 
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.commons.collections.ComparatorUtils;
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jubula.client.core.ClientTestFactory;
 import org.eclipse.jubula.client.core.businessprocess.ITestresultSummaryEventListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ITestresultChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.TestresultState;
 import org.eclipse.jubula.client.core.model.ITestResultSummary;
 import org.eclipse.jubula.client.core.model.ITestResultSummaryPO;
 import org.eclipse.jubula.client.core.model.TestResultNode;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.Hibernator;
 import org.eclipse.jubula.client.core.persistence.TestResultPM;
 import org.eclipse.jubula.client.core.persistence.TestResultSummaryPM;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.constants.CommandIDs;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.filter.JBPatternFilter;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.provider.contentprovider.TestresultSummaryContentProvider;
 import org.eclipse.jubula.client.ui.provider.labelprovider.TestresultSummaryViewColumnLabelProvider;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.jubula.client.ui.utils.JobUtils;
 import org.eclipse.jubula.client.ui.utils.Utils;
 import org.eclipse.jubula.client.ui.wizards.ExportTestResultDetailsWizard;
 import org.eclipse.jubula.tools.constants.MonitoringConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.menus.CommandContributionItem;
 import org.eclipse.ui.part.ViewPart;
 
 /**
  * View for presenting summaries of Test Results.
  * 
  * @author BREDEX GmbH
  */
 @SuppressWarnings("unchecked")
 public class TestresultSummaryView extends ViewPart
     implements ITestresultSummaryEventListener, ITestresultChangedListener {
     /**
      * <code>defaultDateTimeFormat</code> the Date Time Format used in to
      * display dates
      */
     public static final DateFormat DTF_DEFAULT = DateFormat.getDateTimeInstance(
             DateFormat.DEFAULT, DateFormat.DEFAULT);
 
     /**
      * <code>fullDateTimeFormat</code> the detailed Date Time Format used in to
      * display dates
      */
     public static final DateFormat DTF_LONG = DateFormat.getDateTimeInstance(
             DateFormat.DEFAULT, DateFormat.LONG);
     
     /**
      * <code>FILTER_CONTROL_INPUT_DELAY</code>
      */
     private static final int FILTER_CONTROL_INPUT_DELAY = 300;
 
     /**
      * <code>NO_DATA_AVAILABLE</code>
      */
     private static final String NO_DATA_AVAILABLE = 
         Messages.TestresultSummaryNoData;
 
     /**
      * <code>TESTRESULT_SUMMARY_NUMBER_OF_FAILED_CAPS</code>
      */
     private static final String TESTRESULT_SUMMARY_NUMBER_OF_FAILED_CAPS = 
         Messages.TestresultSummaryNumberOfFailedCaps;
 
     /**
      * <code>TESTRESULT_SUMMARY_DETAILS_AVAILABLE</code>
      */
     private static final String TESTRESULT_SUMMARY_DETAILS_AVAILABLE = 
         Messages.TestresultSummaryDetailsAvailable;
 
     /**
      * <code>TESTRESULT_SUMMARY_TESTRUN_RELEVANT</code>
      */
     private static final String TESTRESULT_SUMMARY_TESTRUN_RELEVANT = 
         Messages.TestresultSummaryTestrunRelevant;
 
     /**
      * <code>TESTRESULT_SUMMARY_CMD_PARAM</code>
      */
     private static final String TESTRESULT_SUMMARY_CMD_PARAM = 
         Messages.TestresultSummaryCmdParam;
 
     /**
      * <code>TESTRESULT_SUMMARY_HANDLER_CAPS</code>
      */
     private static final String TESTRESULT_SUMMARY_HANDLER_CAPS = 
         Messages.TestresultSummaryHandlerCaps;
 
     /**
      * <code>TESTRESULT_SUMMARY_EXECUTED_CAPS</code>
      */
     private static final String TESTRESULT_SUMMARY_EXECUTED_CAPS = 
         Messages.TestresultSummaryExecCaps;
 
     /**
      * <code>TESTRESULT_SUMMARY_EXPECTED_CAPS</code>
      */
     private static final String TESTRESULT_SUMMARY_EXPECTED_CAPS = 
         Messages.TestresultSummaryExpecCaps;
 
     /**
      * <code>TESTRESULT_SUMMARY_DURATION</code>
      */
     private static final String TESTRESULT_SUMMARY_DURATION = 
         Messages.TestresultSummaryDuration;
 
     /**
      * <code>TESTRESULT_SUMMARY_END_TIME</code>
      */
     private static final String TESTRESULT_SUMMARY_END_TIME = 
         Messages.TestresultSummaryEndTime;
 
     /**
      * <code>TESTRESULT_SUMMARY_START_TIME</code>
      */
     private static final String TESTRESULT_SUMMARY_START_TIME = 
         Messages.TestresultSummaryStartTime;
 
     /**
      * <code>TESTRESULT_SUMMARY_LANGUAGE</code>
      */
     private static final String TESTRESULT_SUMMARY_LANGUAGE = 
         Messages.TestresultSummaryLanguage;
 
     /**
      * <code>TESTRESULT_SUMMARY_TOOLKIT</code>
      */
     private static final String TESTRESULT_SUMMARY_TOOLKIT = 
         Messages.TestresultSummaryToolkit;
 
     /**
      * <code>TESTRESULT_SUMMARY_AUT_OS</code>
      */
     private static final String TESTRESULT_SUMMARY_AUT_OS = 
         Messages.TestresultSummaryAutOS;
 
     /**
      * <code>TESTRESULT_SUMMARY_AUT_AGENT_HOSTNAME</code>
      */
     private static final String TESTRESULT_SUMMARY_AUT_AGENT_HOSTNAME = 
         Messages.TestresultSummaryAutAgentHostname;
 
     /**
      * <code>TESTRESULT_SUMMARY_AUT_HOSTNAME</code>
      */
     private static final String TESTRESULT_SUMMARY_AUT_HOSTNAME = 
         Messages.TestresultSummaryAutHostname;
 
     /**
      * <code>TESTRESULT_SUMMARY_AUT_CONFIG</code>
      */
     private static final String TESTRESULT_SUMMARY_AUT_CONFIG = 
         Messages.TestresultSummaryAutConf;
 
     /**
      * <code>TESTRESULT_SUMMARY_AUT_ID</code>
      */
     private static final String TESTRESULT_SUMMARY_AUT_ID = 
         Messages.TestresultSummaryAutId;
 
     /**
      * <code>TESTRESULT_SUMMARY_AUT_NAME</code>
      */
     private static final String TESTRESULT_SUMMARY_AUT_NAME = 
         Messages.TestresultSummaryAutName;
 
     /**
      * <code>TESTRESULT_SUMMARY_TESTSUITE_STATUS</code>
      */
     private static final String TESTRESULT_SUMMARY_TESTSUITE_STATUS = 
         Messages.TestresultSummaryTestsuiteStatus;
 
     /**
      * <code>TESTRESULT_SUMMARY_TESTSUITE</code>
      */
     private static final String TESTRESULT_SUMMARY_TESTSUITE = 
         Messages.TestresultSummaryTestsuite;
 
     /**
      * <code>TESTRESULT_SUMMARY_PROJECT_NAME</code>
      */
     private static final String TESTRESULT_SUMMARY_PROJECT_NAME = 
         Messages.TestresultSummaryProjectName;
 
     /**
      * <code>TESTRESULT_SUMMARY_TESTRUN_STATE</code>
      */
     private static final String TESTRESULT_SUMMARY_TESTRUN_STATE = 
         Messages.TestresultSummaryTestrunState;
 
     /**
      * <code>TESTRESULT_SUMMARY_DATE</code>
      */
     private static final String TESTRESULT_SUMMARY_DATE = 
         Messages.TestresultSummaryDate;
 
     /**
      * <code>TESTRESULT_SUMMARY_COMMENT_TITLE</code>
      */
     private static final String TESTRESULT_SUMMARY_COMMENT_TITLE =
         Messages.TestresultSummaryCommentTitle;
     
     /**
      * <code>TESTRESULT_SUMMARY_TEST_JOB_START_TIME</code>
      */
     private static final String TESTRESULT_SUMMARY_TEST_JOB_START_TIME = 
         Messages.TestresultSummaryTestJobStartTime;
 
     /**
      * <code>TESTRESULT_SUMMARY_TEST_JOB</code>
      */
     private static final String TESTRESULT_SUMMARY_TEST_JOB = 
         Messages.TestresultSummaryTestJob;
 
     /**
      * <code>TESTRESULT_SUMMARY_TESTRUN_ID</code>
      */
     private static final String TESTRESULT_SUMMARY_TESTRUN_ID = 
         Messages.TestresultSummaryTestrunID;
     
     /**
      * <code>MONITORING_ID</code>
      */
     private static final String MONITORING_ID = 
         Messages.TestresultSummaryMonitoringId;
     
     /**
      * <code>MONITORING_VALUE</code>
      */
     private static final String MONITORING_VALUE = 
         Messages.TestresultSummaryMonitoringValue;
     
     /**
      * <code>MONITORING_DETAILS</code>
      */
     private static final String MONITORING_DETAILS = 
         Messages.TestresultSummaryMonitoringDetails;
     /** standard logging */
     private static Log log = LogFactory.getLog(TestresultSummaryView.class);
     
     /** column tag for memento*/
     private static final String TAG_COLUMN = "column"; //$NON-NLS-1$
 
     /** number tag for memento*/
     private static final String TAG_NUMBER = "number"; //$NON-NLS-1$
 
     /** width tag for memento*/
     private static final String TAG_WIDTH = "width"; //$NON-NLS-1$
     
     /** column index tag for memento*/
     private static final String TAG_COL_IDX = "columnIndex"; //$NON-NLS-1$
     
     /** filter tag for memento*/
     private static final String TAG_FILTER = "filter"; //$NON-NLS-1$
     
     /** filter type tag for memento*/
     private static final String TAG_FILTER_TYPE = "filterType"; //$NON-NLS-1$
 
     /** search type tag for memento*/
     private static final String TAG_SEARCH_TYPE = "searchType"; //$NON-NLS-1$
     
     /** sort tag for memento*/
     private static final String TAG_SORT = "sort"; //$NON-NLS-1$
     
     /** sort column tag for memento*/
     private static final String TAG_SORT_COL = "sortColumn"; //$NON-NLS-1$
 
     /** sort direction tag for memento*/
     private static final String TAG_SORT_DIRECTION = "sortDirection"; //$NON-NLS-1$
     
     /** table viewer of metadata table */
     private TableViewer m_tableViewer;
 
     /** filter for metadata */
     private TestresultSummaryFilter m_filter;
 
     /** menu to show/hide columns */
     private Menu m_headerMenu;
 
     /** IMemento to persist view settings like filter, sort etc */
     private IMemento m_memento;
     
     /** combobox for filter type */
     private Combo m_filterCombo;
 
     /** search string textfield for filter*/
     private Text m_searchText;
     
     /** the list of summary ids that have details */
     private List<Number> m_detailedSummaryIds = new ArrayList<Number>();
 
     /**
      * <code>m_filterJob</code>
      */
     private TestresultFilterJob m_filterJob = new TestresultFilterJob(
             Messages.JobFilterSummaryView,
             StringConstants.EMPTY);
 
     /**
      * The constructor.
      */
     public TestresultSummaryView() {
         //default constructor
     }
 
     /**
      * {@inheritDoc}
      */
     public void init(IViewSite site, IMemento memento)
         throws PartInitException {
         super.init(site, memento);
         this.m_memento = memento;
     }
 
     /**
      * {@inheritDoc}
      */
     public void createPartControl(Composite parent) {
         m_headerMenu = new Menu(parent);
         GridLayout layout = new GridLayout(4, false);
         parent.setLayout(layout);
         m_filter = new TestresultSummaryFilter();
         createSearchFilter(parent);
         m_tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
                 | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
         addDetailsColumn(m_tableViewer);
         addDateColumn(m_tableViewer);
         addCommentTitleColumn(m_tableViewer);
         addTestrunIdColumn(m_tableViewer);
         addTestRelevantColumn(m_tableViewer);
         addTestJobStartTimeColumn(m_tableViewer);
         addTestJobColumn(m_tableViewer);
         addStatusDecoratorColumn(m_tableViewer);
         addTsStatusColumn(m_tableViewer);
         addTestsuiteColumn(m_tableViewer);
         addProjectNameColumn(m_tableViewer);
         addAutIdColumn(m_tableViewer);
         addAutNameColumn(m_tableViewer);
         addAutConfColumn(m_tableViewer);
         addLanguageColumn(m_tableViewer);
         addCmdParamColumn(m_tableViewer);
         addAutOSColumn(m_tableViewer);
         addAutHostnameColumn(m_tableViewer);
         addAutAgentHostnameColumn(m_tableViewer);
         addToolkitColumn(m_tableViewer);
         addStartTimeColumn(m_tableViewer);
         addEndTimeColumn(m_tableViewer);
         addDurationColumn(m_tableViewer);
         addExpecCapsColumn(m_tableViewer);
         addExecCapsColumn(m_tableViewer);
         addEventhandlerCapsColumn(m_tableViewer);
         addFailedCapsColumn(m_tableViewer);
         addMonitoringIdColumn(m_tableViewer);
         addMonitoringValueColumn(m_tableViewer);
         addMonitoringReportColumn(m_tableViewer);
         
         getSite().setSelectionProvider(m_tableViewer);
         m_tableViewer.setContentProvider(
                 new TestresultSummaryContentProvider());
         m_tableViewer.getTable().setLinesVisible(true);
         m_tableViewer.getTable().setHeaderVisible(true);
         TableColumn sortColumn = m_tableViewer.getTable().getColumn(0);
         m_tableViewer.getTable().setSortColumn(sortColumn);
         m_tableViewer.getTable().setSortDirection(SWT.DOWN);
         ColumnSortListener sortListener = new ColumnSortListener(
                 m_tableViewer, sortColumn);
 
         for (TableColumn col : m_tableViewer.getTable().getColumns()) {
             col.addSelectionListener(sortListener);
         }
         ClientTestFactory.getClientTest()
             .addTestresultSummaryEventListener(this);
         m_tableViewer.setUseHashlookup(true);
 
         addContextMenu(m_tableViewer, m_headerMenu);
         
         // set table viewer / refresh
         refreshView();
         m_tableViewer.addFilter(m_filter);
         Plugin.getHelpSystem().setHelp(m_tableViewer.getControl(),
                 ContextHelpIds.TESTRESULT_SUMMARY_VIEW);
         setTableViewerLayout();
         restoreViewStatus();
         m_tableViewer.refresh();
         DataEventDispatcher.getInstance().addTestresultListener(
                 this, true);
         addDoubleClickListener(m_tableViewer);
             
     }
 
     /**
      * Adds a context menu to the table's header.
      * 
      * @param tableViewer The table viewer.
      * @param headerMenu The context menu to add to the table's header.
      */
     private void addContextMenu(final TableViewer tableViewer, 
             final Menu headerMenu) {
         /*
          * Add context menu to header. Similar to the snippets described in:
          * https://bugs.eclipse.org/bugs/show_bug.cgi?id=23103
          */
         final Table table = tableViewer.getTable();
         table.addListener(SWT.MenuDetect, new Listener() {
             public void handleEvent(Event event) {
                 Point pt = event.display.map(
                         null, table, new Point(event.x, event.y));
                 Rectangle clientArea = table.getClientArea();
                 boolean isHeaderEvent = clientArea.y <= pt.y 
                     && pt.y < (clientArea.y + table.getHeaderHeight());
                 if (isHeaderEvent) {
                     table.setMenu(headerMenu);
                 } else {
                     // Create menu manager.
                     MenuManager menuMgr = new MenuManager();
                     menuMgr.setRemoveAllWhenShown(true);
                     menuMgr.addMenuListener(new IMenuListener() {
                         public void menuAboutToShow(IMenuManager mgr) {
                             fillContextMenu(mgr);
                         }
                     });
                     // Create menu.
                     Menu menu = menuMgr.createContextMenu(table);
                     // Register menu for extension.
                     getSite().registerContextMenu(menuMgr, tableViewer);
                     table.setMenu(menu);
                 }
             }
         });
         // Comment from snippet in https://bugs.eclipse.org/bugs/show_bug.cgi?id=23103
         // IMPORTANT: Dispose the menus (only the current menu, set with 
         // setMenu(), will be automatically disposed) 
         table.addListener(SWT.Dispose, new Listener() {
             public void handleEvent(Event event) {
                 headerMenu.dispose();
             }
         });
     }
 
     /**
      * @param mgr the menu manager
      */
     private void fillContextMenu(IMenuManager mgr) {
         mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
         
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.OPEN_TEST_RESULT_DETAIL_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.ADD_COMMENT_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.REFRESH_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.DELETE_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.TOGGLE_RELEVANCE_COMMAND_ID);
         Map<String, String> params = new HashMap<String, String>();
         params.put(CommandIDs.EXPORT_WIZARD_PARAM_ID, 
                 ExportTestResultDetailsWizard.ID);
         mgr.add(CommandHelper.createContributionItem(
                 CommandIDs.ECLIPSE_RCP_FILE_EXPORT_COMMAND_ID, 
                 params, null,
                 CommandContributionItem.STYLE_PUSH));
     }
     
     /**
      * @param tableViewer
      *            the table viewer
      * @return the added column.
      */
     private TableViewerColumn addFailedCapsColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.EH_CAP_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_NUMBER_OF_FAILED_CAPS);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 int noFailed = ((ITestResultSummaryPO)element)
                         .getTestsuiteFailedTeststeps();
                 if (noFailed == ITestResultSummaryPO
                         .DEFAULT_NUMBER_OF_FAILED_TEST_STEPS) {
                     return NO_DATA_AVAILABLE;
                 }
                 return String.valueOf(noFailed);
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1)
                             .getTestsuiteFailedTeststeps(), 
                         ((ITestResultSummaryPO)e2)
                             .getTestsuiteFailedTeststeps());
             }
         };
         return column;
     }
 
     /**
      * @param tableViewer
      *            the table viewer
      * @return the added column.
      */
     private TableViewerColumn addDetailsColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(75);
         column.getColumn().setText(TESTRESULT_SUMMARY_DETAILS_AVAILABLE);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public Image getImage(Object element) {
                 return null;
             }
             public String getText(Object element) {
                 return String.valueOf(m_detailedSummaryIds
                         .contains(((ITestResultSummaryPO)element).getId()));
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                     m_detailedSummaryIds
                         .contains(((ITestResultSummaryPO)e1).getId()), 
                     m_detailedSummaryIds
                         .contains(((ITestResultSummaryPO)e2).getId()));
             }
         };
         return column;
     }
 
     /**
      * Adds a double-click listener to the given viewer. This listener 
      * opens/activates a Test Result Viewer based on the current selection.
      * 
      * @param viewer The viewer to which to add the listener.
      */
     private void addDoubleClickListener(StructuredViewer viewer) {
         viewer.addDoubleClickListener(new IDoubleClickListener() {
             public void doubleClick(DoubleClickEvent event) {
                 CommandHelper.executeCommand(
                         CommandIDs.OPEN_TEST_RESULT_VIEWER_COMMAND_ID,
                         getSite());
             }
         });
     }
     
     /**
      * save filters, sorting etc
      * @param memento IMemento
      */
     public void saveState(IMemento memento) {
         Table table = m_tableViewer.getTable();
         // save columns width
         TableColumn columns[] = table.getColumns();
         int[] colOrder = table.getColumnOrder();
         for (int i = 0; i < columns.length; i++) {
             IMemento colWidthChild = memento.createChild(TAG_COLUMN);
             colWidthChild.putInteger(TAG_NUMBER, i);
             colWidthChild.putInteger(TAG_WIDTH, columns[i].getWidth());
             colWidthChild.putInteger(TAG_COL_IDX, colOrder[i]);
         }
         // save filter
         IMemento filterChild = memento.createChild(TAG_FILTER);
         filterChild.putString(TAG_FILTER_TYPE, m_filterCombo.getItem(
                 m_filterCombo.getSelectionIndex()));
         filterChild.putString(TAG_SEARCH_TYPE, m_searchText.getText());
         //save sorting
         IMemento sortChild = memento.createChild(TAG_SORT);
         TableColumn sortCol = table.getSortColumn();
         sortChild.putString(TAG_SORT_COL, sortCol.getText());
         sortChild.putInteger(TAG_SORT_DIRECTION, table.getSortDirection());
     }
 
     /**
      * restore view settings like column order, width etc
      */
     private void restoreViewStatus() {
         if (m_memento != null) {
             Table table = m_tableViewer.getTable();
             // restore columns
             IMemento children[] = m_memento.getChildren(TAG_COLUMN);
             if (children.length == table.getColumnCount()) {
                 if (children != null) {
                     int[] colOrder = new int[table.getColumnOrder().length];
                     for (int i = 0; i < children.length; i++) {
                         Integer val = children[i].getInteger(TAG_NUMBER);
                         if (val != null) {
                             int index = val.intValue();
                             Integer width = children[i].getInteger(TAG_WIDTH);
                             if (width != null) {
                                 table.getColumn(index).setWidth(
                                         width.intValue());
                             }
                             Integer colIdx = children[i].getInteger(
                                     TAG_COL_IDX);
                             if (colIdx != null) {
                                 colOrder[i] = colIdx.intValue();
                             }
                         }
                     }
                     if (children.length == colOrder.length) {
                         table.setColumnOrder(colOrder);
                     }
                 }
                 //restore filter
                 IMemento filterChild = m_memento.getChild(TAG_FILTER);
                 if (filterChild != null) {
                     String filterTypeString = 
                         filterChild.getString(TAG_FILTER_TYPE);
                     String searchString = 
                         filterChild.getString(TAG_SEARCH_TYPE);
                     m_filterCombo.select(m_filterCombo.indexOf(
                             filterTypeString));
                     m_filter.setFilterType(filterTypeString);
                     m_searchText.setText(searchString);
                     m_filter.setPattern(searchString);
                 }
                 //restore sorting
                 IMemento sortChild = m_memento.getChild(TAG_SORT);
                 if (sortChild != null) {
                     //set sort column
                     String sortHeader = sortChild.getString(TAG_SORT_COL);
                     for (int i = 0; i < table.getColumnCount(); i++) {
                         TableColumn tblCol = table.getColumn(i);
                         if (sortHeader.equals(tblCol.getText())) {
                             table.setSortColumn(tblCol);
                         }
                     }
                     //set sort direction
                     table.setSortDirection(
                             sortChild.getInteger(TAG_SORT_DIRECTION));
                 }
             }
         }
     }
 
     /**
      * create textfield dfor search filter
      * @param parent composite
      */
     private void createSearchFilter(Composite parent) {
         // "filter by" label
         Label searchLabel = new Label(parent, SWT.NONE);
         searchLabel.setText(Messages.TestresultSummaryFilterLabel);
 
         // combo box to change column for filter
         m_filterCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
         String[] items = new String[] { TESTRESULT_SUMMARY_TESTRUN_ID,
             TESTRESULT_SUMMARY_TEST_JOB,
             TESTRESULT_SUMMARY_TEST_JOB_START_TIME,
             TESTRESULT_SUMMARY_DATE,
             TESTRESULT_SUMMARY_COMMENT_TITLE,
             TESTRESULT_SUMMARY_TESTRUN_STATE,
             TESTRESULT_SUMMARY_PROJECT_NAME,
             TESTRESULT_SUMMARY_TESTSUITE,
             TESTRESULT_SUMMARY_TESTSUITE_STATUS,
             TESTRESULT_SUMMARY_AUT_NAME,
             TESTRESULT_SUMMARY_AUT_ID,
             TESTRESULT_SUMMARY_AUT_CONFIG,
             TESTRESULT_SUMMARY_AUT_HOSTNAME,
             TESTRESULT_SUMMARY_AUT_AGENT_HOSTNAME,
             TESTRESULT_SUMMARY_AUT_OS,
             TESTRESULT_SUMMARY_TOOLKIT,
             TESTRESULT_SUMMARY_LANGUAGE,
             TESTRESULT_SUMMARY_START_TIME,
             TESTRESULT_SUMMARY_END_TIME,
             TESTRESULT_SUMMARY_DURATION,
             TESTRESULT_SUMMARY_EXPECTED_CAPS,
             TESTRESULT_SUMMARY_EXECUTED_CAPS,
             TESTRESULT_SUMMARY_HANDLER_CAPS,
             TESTRESULT_SUMMARY_CMD_PARAM,
             TESTRESULT_SUMMARY_TESTRUN_RELEVANT,
             TESTRESULT_SUMMARY_DETAILS_AVAILABLE,
             TESTRESULT_SUMMARY_NUMBER_OF_FAILED_CAPS };
         Arrays.sort(items);
         m_filterCombo.setItems(items);
         m_filterCombo.addListener(SWT.Selection, new Listener() {
             public void handleEvent(Event e) {
                 if (e.widget instanceof Combo) {
                     Combo cbx = (Combo)e.widget;
                     m_filter.setFilterType(cbx.getItem(
                             cbx.getSelectionIndex()));
                 }
             }
         });
         int index = m_filterCombo.indexOf(TESTRESULT_SUMMARY_TESTRUN_STATE);
         if (index != -1) {
             m_filterCombo.select(index);
             m_filter.setFilterType(TESTRESULT_SUMMARY_TESTRUN_STATE);
         } else {
             m_filterCombo.select(0);
         }
 
         createLabel(parent);
 
         // search filter textfield
         m_searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
         m_searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                 | GridData.HORIZONTAL_ALIGN_FILL));
         m_searchText.addKeyListener(new KeyAdapter() {
             public void keyReleased(KeyEvent ke) {
                 if (m_filterJob.cancel()) {
                     m_filterJob = new TestresultFilterJob(
                             Messages.JobFilterSummaryView,
                             m_searchText.getText());
                     JobUtils.executeJob(m_filterJob, null, 
                             FILTER_CONTROL_INPUT_DELAY);
                 }
             }
         });
     }
     
     /**
      * @param parent
      *            the parent to use for label creation
      */
     private void createLabel(Composite parent) {
         IPreferenceStore ps = Plugin.getDefault().getPreferenceStore();
         final Label forLabel = new Label(parent, SWT.NONE);
         forLabel.setText(NLS.bind(Messages.TestresultSummaryForLabel,
                 ps.getInt(Constants.MAX_NUMBER_OF_DAYS_KEY)));
         ps.addPropertyChangeListener(new IPropertyChangeListener() {
             public void propertyChange(PropertyChangeEvent event) {
                 if (event.getProperty()
                         .equals(Constants.MAX_NUMBER_OF_DAYS_KEY)) {
                     forLabel.setText(NLS.bind(
                             Messages.TestresultSummaryForLabel,
                             event.getNewValue()));
                 }
             }
         });
     }
 
     /**
      * get the testrun ids of selected testruns
      * @return the testrun ids of selected testruns
      */
     public Long[] getSelectedTestrunIds() {
         ISelection selection = m_tableViewer.getSelection();
         if (selection instanceof IStructuredSelection) {
             List<Long> selectedIds = new ArrayList<Long>();
             for (Object selectedItem 
                     : ((IStructuredSelection)selection).toArray()) {
                 ITestResultSummaryPO summary = 
                     (ITestResultSummaryPO)selectedItem;
                 selectedIds.add(summary.getId());
             }
             return selectedIds.toArray(new Long [selectedIds.size()]);
         }
 
         return new Long[0];
     }
     
     
     /**
      * delete selected testresults
      * @param testrunIds testruns which will be deleted
      */
     public void deleteTestresults(final Long[] testrunIds) {
         final String jobName = Messages.UIJobDeletingTestResultFromDB;
         Job job = new Job(jobName) {
             public IStatus run(IProgressMonitor monitor) {
                 monitor.beginTask(jobName, IProgressMonitor.UNKNOWN);
                 TestResultSummaryPM.deleteTestruns(testrunIds);
                 refreshView();
                 monitor.done();
                 return Status.OK_STATUS;
             }
         };
         job.schedule();
     }
     
     /**
      * refresh view
      */
     public void refreshView() {
         m_tableViewer.getControl().getDisplay().syncExec(new Runnable() {
             public void run() {
                 List<ITestResultSummaryPO> metaList;
                 try {
                     int maxNoOfDays = Plugin.getDefault().getPreferenceStore()
                             .getInt(Constants.MAX_NUMBER_OF_DAYS_KEY);
                     Date startTime = DateUtils.addDays(new Date(), 
                             maxNoOfDays * -1);
                     metaList = TestResultSummaryPM
                             .findAllTestResultSummaries(startTime);
                     if (Hibernator.instance() != null) {
                         m_detailedSummaryIds = TestResultPM
                             .computeTestresultIdsWithDetails(
                                 GeneralStorage.getInstance()
                                     .getMasterSession());
                     }
                     
                     if (metaList != null) {
                         m_tableViewer.setInput(metaList.toArray());
                     }
                 } catch (JBException e) {
                     String msg = Messages.CantLoadMetadataFromDatabase;
                     log.error(msg, e);
                     showErrorDialog(msg);
                 }
                 // re-set the selection as this could otherwise lead to cached selected
                 // POs which are not up-to-date and lead to db-problems on
                 // EntityManager.merge();
                 ISelection s = m_tableViewer.getSelection();
                 m_tableViewer.setSelection(null);
                 m_tableViewer.setSelection(s);
             }
         });
     }
 
     /**
      * set layout for table viewer
      */
     private void setTableViewerLayout() {
         GridData gridData = new GridData();
         gridData.verticalAlignment = GridData.FILL;
         gridData.horizontalSpan = 4;
         gridData.grabExcessHorizontalSpace = true;
         gridData.grabExcessVerticalSpace = true;
         gridData.horizontalAlignment = GridData.FILL;
         m_tableViewer.getControl().setLayoutData(gridData);
     }
     
     /**
      * Adds a "Testjob name" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addTestJobColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.TJ_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_TEST_JOB);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getTestJobName());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getTestJobName(), 
                         ((ITestResultSummaryPO)e2).getTestJobName());
             }
         };
         return column;
     }
     
     /**
      * Adds a "Test job Start Time" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addTestJobStartTimeColumn(
             TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.CLOCK_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_TEST_JOB_START_TIME);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 Date date = ((ITestResultSummaryPO)element)
                         .getTestJobStartTime();
                 if (date != null) {
                     return DTF_LONG.format(date);
                 }
                 return ObjectUtils.toString(date, StringConstants.EMPTY);
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getTestJobStartTime(), 
                         ((ITestResultSummaryPO)e2).getTestJobStartTime());
             }
         };
         return column;
     }
 
     /**
      * Adds a "Status decorator" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addStatusDecoratorColumn(
             TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(100);
         column.getColumn().setText(TESTRESULT_SUMMARY_TESTRUN_STATE);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 ITestResultSummaryPO row = (ITestResultSummaryPO)element;
                 return row.getTestRunState();
             }
             public Image getImage(Object element) {
                 ITestResultSummaryPO row = (ITestResultSummaryPO)element;
                 switch (row.getTestsuiteStatus()) {
                     case TestResultNode.NOT_YET_TESTED:
                         break;
                     case TestResultNode.NO_VERIFY:
                         return IconConstants.STEP_OK_IMAGE;
                     case TestResultNode.TESTING:
                         return IconConstants.STEP_TESTING_IMAGE;
                     case TestResultNode.SUCCESS:
                         return IconConstants.STEP_OK_IMAGE;
                     case TestResultNode.ERROR:
                         return IconConstants.STEP_NOT_OK_IMAGE;
                     case TestResultNode.ERROR_IN_CHILD:
                         return IconConstants.STEP_NOT_OK_IMAGE;
                     case TestResultNode.NOT_TESTED:
                         return IconConstants.STEP_FAILED_IMAGE;
                     case TestResultNode.RETRYING:
                         return IconConstants.STEP_RETRY_IMAGE;
                     case TestResultNode.SUCCESS_RETRY:
                         return IconConstants.STEP_RETRY_OK_IMAGE;
                     case TestResultNode.ABORT:
                         return IconConstants.STEP_NOT_OK_IMAGE;
                     default:
                         return null;
                 }
                 
                 return null;
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getTestRunState(), 
                         ((ITestResultSummaryPO)e2).getTestRunState());
             }
         };
         return column;
     }
 
     /**
      * Adds a "Project Name" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addProjectNameColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setImage(IconConstants.PROJECT_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_PROJECT_NAME);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getProjectName());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getProjectName(), 
                         ((ITestResultSummaryPO)e2).getProjectName());
             }
         };
         return column;
     }
 
     /**
      * Adds a "Testsuite name" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addTestsuiteColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setImage(IconConstants.TS_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_TESTSUITE);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getTestsuiteName());
             }
         });
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getTestsuiteName(), 
                         ((ITestResultSummaryPO)e2).getTestsuiteName());
             }
         };
         createMenuItem(m_headerMenu, column.getColumn());
         return column;
     }
 
     /**
      * Adds a "Testsuite status" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addTsStatusColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.TS_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_TESTSUITE_STATUS);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getStatusString());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getStatusString(), 
                         ((ITestResultSummaryPO)e2).getStatusString());
             }
         };
         return column;
     }
 
     /**
      * Adds a "Aut Name" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addAutNameColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.AUT_RUNNING_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_AUT_NAME);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getAutName());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getAutName(), 
                         ((ITestResultSummaryPO)e2).getAutName());
             }
         };
         return column;
     }
     
     /**
      * Adds a "Aut Id" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addAutIdColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setImage(IconConstants.AUT_RUNNING_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_AUT_ID);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getAutId());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getAutId(), 
                         ((ITestResultSummaryPO)e2).getAutId());
             }
         };
         return column;
     }
 
     /**
      * Adds a "Aut Config" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addAutConfColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.AUT_RUNNING_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_AUT_CONFIG);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getAutConfigName());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getAutConfigName(), 
                         ((ITestResultSummaryPO)e2).getAutConfigName());
             }
         };
         return column;
     }
 
     /**
      * Adds a "AutServer" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addAutAgentHostnameColumn(
             TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.AUT_RUNNING_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_AUT_AGENT_HOSTNAME);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getAutAgentName());
             }
         });
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getAutAgentName(), 
                         ((ITestResultSummaryPO)e2).getAutAgentName());
             }
         };
         createMenuItem(m_headerMenu, column.getColumn());
         return column;
     }
     
     /**
      * Adds a "AutHostname" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addAutHostnameColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.AUT_RUNNING_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_AUT_HOSTNAME);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getAutHostname());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getAutHostname(), 
                         ((ITestResultSummaryPO)e2).getAutHostname());
             }
         };
         return column;
     }
     
     /**
      * Adds a "AutOS" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addAutOSColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.AUT_RUNNING_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_AUT_OS);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getAutOS());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getAutOS(), 
                         ((ITestResultSummaryPO)e2).getAutOS());
             }
         };
         return column;
     }
 
     /**
      * Adds a "Language" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addLanguageColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.LANGUAGE_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_LANGUAGE);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return ObjectUtils.toString(
                     ((ITestResultSummaryPO)element).getTestsuiteLanguage());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getTestsuiteLanguage(), 
                         ((ITestResultSummaryPO)e2).getTestsuiteLanguage());
             }
         };
         return column;
     }
 
     /**
      * Adds a "Toolkit" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addToolkitColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setText(TESTRESULT_SUMMARY_TOOLKIT);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                         ((ITestResultSummaryPO)element).getAutToolkit());
             }
         });
 
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getAutToolkit(), 
                         ((ITestResultSummaryPO)e2).getAutToolkit());
             }
         };
 
         return column;
     }
 
     /**
      * Adds a "Date" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addDateColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setText(TESTRESULT_SUMMARY_DATE);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return DTF_DEFAULT.format(
                         ((ITestResultSummary)element).getTestsuiteDate());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummary)e1).getTestsuiteDate(), 
                         ((ITestResultSummary)e2).getTestsuiteDate());
             }
         };
         return column;
     }
     
     /**
      * Adds a "Comment Title" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addCommentTitleColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setText(TESTRESULT_SUMMARY_COMMENT_TITLE);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return ObjectUtils.toString(
                     ((ITestResultSummaryPO)element).getCommentTitle());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getCommentTitle(), 
                         ((ITestResultSummaryPO)e2).getCommentTitle());
             }
         };
         return column;
     }
     
     /**
      * Adds a "Start Time" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addStartTimeColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.CLOCK_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_START_TIME);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return DTF_LONG.format(
                     ((ITestResultSummaryPO)element).getTestsuiteStartTime());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getTestsuiteStartTime(), 
                         ((ITestResultSummaryPO)e2).getTestsuiteStartTime());
             }
         };
         return column;
     }
 
     /**
      * Adds a "End Time" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addEndTimeColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.CLOCK_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_END_TIME);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return DTF_LONG.format(
                     ((ITestResultSummaryPO)element).getTestsuiteEndTime());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getTestsuiteEndTime(), 
                         ((ITestResultSummaryPO)e2).getTestsuiteEndTime());
             }
         };
         return column;
     }
 
     /**
      * Adds a "Duration" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addDurationColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setImage(IconConstants.CLOCK_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_DURATION);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getTestsuiteDuration());
             }
         });
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getTestsuiteDuration(), 
                         ((ITestResultSummaryPO)e2).getTestsuiteDuration());
             }
         };
         createMenuItem(m_headerMenu, column.getColumn());
         return column;
     }
 
     /**
      * Adds a "Expected Caps" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addExpecCapsColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.CAP_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_EXPECTED_CAPS);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return String.valueOf(((ITestResultSummaryPO)element)
                         .getTestsuiteExpectedTeststeps());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1)
                             .getTestsuiteExpectedTeststeps(), 
                         ((ITestResultSummaryPO)e2)
                             .getTestsuiteExpectedTeststeps());
             }
         };
         return column;
     }
 
     /**
      * Adds a "Executed Caps" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addExecCapsColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.CAP_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_EXECUTED_CAPS);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return String.valueOf(((ITestResultSummaryPO)element)
                         .getTestsuiteExecutedTeststeps());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1)
                             .getTestsuiteExecutedTeststeps(), 
                         ((ITestResultSummaryPO)e2)
                             .getTestsuiteExecutedTeststeps());
             }
         };
         return column;
     }
 
     /**
      * Adds a "errorhandler caps" column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addEventhandlerCapsColumn(
             TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setImage(IconConstants.EH_CAP_IMAGE);
         column.getColumn().setText(TESTRESULT_SUMMARY_HANDLER_CAPS);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return String.valueOf(((ITestResultSummaryPO)element)
                         .getTestsuiteEventHandlerTeststeps());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1)
                             .getTestsuiteEventHandlerTeststeps(), 
                         ((ITestResultSummaryPO)e2)
                             .getTestsuiteEventHandlerTeststeps());
             }
         };
         return column;
     }
 
     /**
      * Adds a "cmd param " column to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addCmdParamColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(0);
         column.getColumn().setText(TESTRESULT_SUMMARY_CMD_PARAM);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getAutCmdParameter());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getAutCmdParameter(), 
                         ((ITestResultSummaryPO)e2).getAutCmdParameter());
             }
         };
         return column;
     }
     
     /**
      * Adds a "testrun id" column for birt reporting (test details) to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addTestrunIdColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setText(TESTRESULT_SUMMARY_TESTRUN_ID);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultString(
                     ((ITestResultSummaryPO)element).getId().toString());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getId(), 
                         ((ITestResultSummaryPO)e2).getId());
             }
         };
         return column;
     }
     
     /**
      * Adds a "testrun relevant" column for birt reporting (test details) to the given viewer.
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addTestRelevantColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setText(TESTRESULT_SUMMARY_TESTRUN_RELEVANT);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return String.valueOf(
                     ((ITestResultSummaryPO)element).isTestsuiteRelevant());
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).isTestsuiteRelevant(), 
                         ((ITestResultSummaryPO)e2).isTestsuiteRelevant());
             }
         };
         return column;
     }
     /**
      * @param tableViewer the table viewer
      * @return A column with the monitoring report
      */
     private TableViewerColumn addMonitoringReportColumn(
             TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setImage(IconConstants.INFO_IMAGE);
         column.getColumn().setText(MONITORING_DETAILS);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {  
                 if (((ITestResultSummaryPO)element).isReportWritten()) {
                     return Messages.TestresultSummaryMonitoringDetailsAvailable;
                 }                
                 return Messages.TestresultSummaryMonitoringDetailsNotAvailable;
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).isReportWritten(), 
                         ((ITestResultSummaryPO)e2).isReportWritten());
             }
         };
         return column;
     }
     /**
      * 
      * @param tableViewer the tableViewer
      *            
      * @return A column with the monitoring ID
      */
     private TableViewerColumn addMonitoringIdColumn(TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setImage(IconConstants.INFO_IMAGE);
         column.getColumn().setText(MONITORING_ID);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 return StringUtils.defaultIfEmpty(
                     ((ITestResultSummaryPO)element).getInternalMonitoringId(), 
                     Messages.TestresultSummaryMonitoringIdNonSelected);
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getInternalMonitoringId(), 
                         ((ITestResultSummaryPO)e2).getInternalMonitoringId());
             }
         };
         return column;
     }
  /**
      * 
      * @param tableViewer the tableViewer
      *            
      * @return A column with the monitoring value
      */
     private TableViewerColumn addMonitoringValueColumn(
             TableViewer tableViewer) {
         TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(80);
         column.getColumn().setImage(IconConstants.INFO_IMAGE);
         column.getColumn().setText(MONITORING_VALUE);
         column.getColumn().setMoveable(true);        
         column.setLabelProvider(new TestresultSummaryViewColumnLabelProvider() {
             public String getText(Object element) {
                 String monitoringValue = 
                     ((ITestResultSummaryPO)element).getMonitoringValue();
                 String monitoringId = 
                     ((ITestResultSummaryPO)element).getInternalMonitoringId();
                 String monitoringValueTyp = 
                     ((ITestResultSummaryPO)element).getMonitoringValueType(); 
                 if (monitoringId != null && monitoringValue != null) { 
                     if (monitoringValueTyp.equals(
                             MonitoringConstants.PERCENT_VALUE)) {  
                         DecimalFormat n = new DecimalFormat("0.0#%"); //$NON-NLS-1$
                         Double doubleValue = Double.valueOf(monitoringValue);
                         return StringUtils.defaultString(
                                 n.format(doubleValue.doubleValue())); 
                     }
                     if (monitoringValueTyp.equals(
                             MonitoringConstants.DOUBLE_VALUE)) {
                         return String.format(Locale.getDefault(), 
                                 "%f", monitoringValue);
                     }
                     return StringUtils.defaultString(monitoringValue);
                 } 
                 return Messages.TestresultSummaryMonitoringValueNotAvailable;  
             }
         });
         createMenuItem(m_headerMenu, column.getColumn());
         new ColumnViewerSorter(tableViewer, column) {
             @Override
             protected int doCompare(Viewer viewer, Object e1, Object e2) {
                 return getCommonsComparator().compare(
                         ((ITestResultSummaryPO)e1).getMonitoringValue(), 
                         ((ITestResultSummaryPO)e2).getMonitoringValue());
             }
         };
         return column;
     }
     /**
      * Opens an error dialog.
      * @param message the messag eto show in the dialog.
      */
     private void showErrorDialog(String message) {
         Utils.createMessageDialog(new JBException(message,
                 MessageIDs.E_HIBERNATE_LOAD_FAILED), null,
                 new String[] { message });
     }
 
     /**
      * create menus for columns
      * @param menu Menu
      * @param column TableColumn
      */
     private void createMenuItem(Menu menu, final TableColumn column) {
         final MenuItem itemName = new MenuItem(menu, SWT.CHECK);
         itemName.setText(column.getText());
         itemName.setSelection(false);
         if (column.getWidth() > 0) {
             itemName.setSelection(true);
         }
         itemName.addListener(SWT.Selection, new Listener() {
             public void handleEvent(Event event) {
                 if (itemName.getSelection()) {
                     column.setWidth(150);
                     column.setResizable(true);
                 } else {
                     column.setWidth(0);
                     column.setResizable(false);
                 }
             }
         });
 
     }
 
     /**
      * {@inheritDoc}
      */
     public void setFocus() {
         m_tableViewer.getControl().setFocus();
     }
     
     /**
      * {@inheritDoc}
      */
     public void dispose() {
         ClientTestFactory.getClientTest()
             .removeTestresultSummaryEventListener(this);
         DataEventDispatcher.getInstance().removeTestresultListener(this);
         super.dispose();
     }
     
     /**
      * Clears the view (table).
      */
     public void clear() {
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 // avoid resetting selection on database change               
                 m_tableViewer.setSelection(StructuredSelection.EMPTY);
                 m_tableViewer.setInput(null);
                 m_tableViewer.refresh();
             }
         });
     }
 
     /**
      * {@inheritDoc}
      */
     public void handleTestresultChanged(TestresultState state) {
         if (state == TestresultState.Clear) {
             clear();
         } else if (state == TestresultState.Refresh) {
             refreshView();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void testresultSummaryChanged() {
         refreshView();
     }
     
     /**
      * @author BREDEX GmbH
      * @created Nov 23, 2010
      */
     private class TestresultFilterJob extends Job {
         /**
          * <code>m_filterText</code>
          */
         private String m_filterText = StringConstants.EMPTY;
         
         /**
          * @param name the name of the job
          * @param filterText the filter Pattern
          */
         public TestresultFilterJob(String name, String filterText) {
             super(name);
             m_filterText = filterText;
         }
 
         /**
          * {@inheritDoc}
          */
         protected IStatus run(IProgressMonitor monitor) {
             Plugin.getDisplay().syncExec(new Runnable() {
                 public void run() {
                     m_filter.setPattern(m_filterText);
                     m_tableViewer.refresh();
                 }
             });
             return Status.OK_STATUS;
         }
     }
     
     /**
      * @author BREDEX GmbH
      * @created Jan 28, 2010
      */
     private class TestresultSummaryFilter extends JBPatternFilter {
         /**
          * defines, which column should be the filter value
          */
         private String m_filterType = StringUtils.EMPTY;
 
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public boolean isElementVisible(Viewer viewer, Object element) {
             ITestResultSummaryPO m = (ITestResultSummaryPO) element;
             String metaValue = StringConstants.EMPTY;
             if (m_filterType.equals(TESTRESULT_SUMMARY_DATE)) {
                 metaValue = DTF_DEFAULT.format(m.getTestsuiteDate());
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_TESTRUN_ID)) {
                 metaValue = String.valueOf(m.getId());
             } else if (m_filterType
                     .equals(TESTRESULT_SUMMARY_TEST_JOB_START_TIME)) {
                 Date date = m.getTestJobStartTime();
                 metaValue = 
                     date != null ? DTF_LONG.format(date) : StringUtils.EMPTY;
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_TEST_JOB)) {
                 metaValue = m.getTestJobName();
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_TESTRUN_STATE)) {
                 metaValue = m.getTestRunState();
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_PROJECT_NAME)) {
                 metaValue = m.getProjectName();
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_TESTSUITE)) {
                 metaValue = m.getTestsuiteName();
             } else if (m_filterType.equals(
                     TESTRESULT_SUMMARY_TESTSUITE_STATUS)) {
                 metaValue = m.getStatusString();
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_AUT_NAME)) {
                 metaValue = m.getAutName();
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_AUT_ID)) {
                 metaValue = m.getAutId();
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_AUT_CONFIG)) {
                 metaValue = m.getAutConfigName();
             } else if (m_filterType
                     .equals(TESTRESULT_SUMMARY_AUT_AGENT_HOSTNAME)) {
                 metaValue = m.getAutAgentName();
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_AUT_HOSTNAME)) {
                 metaValue = m.getAutHostname();
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_AUT_OS)) {
                 metaValue = m.getAutOS();
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_TOOLKIT)) {
                 metaValue = m.getAutToolkit();
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_LANGUAGE)) {
                 metaValue = m.getTestsuiteLanguage();
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_START_TIME)) {
                 metaValue = DTF_LONG.format(m.getTestsuiteStartTime());
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_END_TIME)) {
                 metaValue = DTF_LONG.format(m.getTestsuiteEndTime());
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_DURATION)) {
                 metaValue = m.getTestsuiteDuration();
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_EXPECTED_CAPS)) {
                 metaValue = String.valueOf(m.getTestsuiteExpectedTeststeps());
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_EXECUTED_CAPS)) {
                 metaValue = String.valueOf(m.getTestsuiteExecutedTeststeps());
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_HANDLER_CAPS)) {
                 metaValue = String.valueOf(m
                         .getTestsuiteEventHandlerTeststeps());
             } else if (m_filterType.equals(TESTRESULT_SUMMARY_CMD_PARAM)) {
                 metaValue = m.getAutCmdParameter();
             } else if (m_filterType.equals(
                     TESTRESULT_SUMMARY_TESTRUN_RELEVANT)) {
                 metaValue = String.valueOf(m.isTestsuiteRelevant());
             } else if (m_filterType
                     .equals(TESTRESULT_SUMMARY_DETAILS_AVAILABLE)) {
                 metaValue = String.valueOf(m_detailedSummaryIds.contains(m
                         .getId()));
             } else if (m_filterType
                     .equals(TESTRESULT_SUMMARY_NUMBER_OF_FAILED_CAPS)) {
                 metaValue = String.valueOf(m.getTestsuiteFailedTeststeps());
             } else if (m_filterType
                     .equals(TESTRESULT_SUMMARY_COMMENT_TITLE)) {
                 metaValue = StringUtils.defaultString(m.getCommentTitle());
             }
             
             if (wordMatches(metaValue)) {
                 return true;
             }
             return false;
         }
 
         /**
          * @param filterType the filterType to set
          */
         public void setFilterType(String filterType) {
             m_filterType = filterType;
         }
 
         /**
          * @return the filterType
          */
         public String getFilterType() {
             return m_filterType;
         }
     }
     
     /**
      * Creates and returns a comparator for natural comparison that can also 
      * handle <code>null</code> values. 
      * 
      * @return the created comparator.
      */
     @SuppressWarnings("rawtypes")
     private static Comparator getCommonsComparator() {
         return ComparatorUtils.nullHighComparator(
                 ComparatorUtils.naturalComparator());
     }
     
 }
