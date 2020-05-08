 package com.versionone.taskview.views;
 
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.TreeViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.part.ViewPart;
 
 import com.versionone.common.preferences.PreferenceConstants;
 import com.versionone.common.preferences.PreferencePage;
 import com.versionone.common.sdk.ApiDataLayer;
 import com.versionone.common.sdk.Workitem;
 import com.versionone.taskview.Activator;
 
 import com.versionone.taskview.views.actions.ActionsManager;
 import com.versionone.taskview.views.editors.SingleValueSupport;
 import com.versionone.taskview.views.editors.MultiValueSupport;
 import com.versionone.taskview.views.editors.TextSupport;
 import com.versionone.taskview.views.properties.WorkitemPropertySource;
 import com.versionone.taskview.views.providers.SimpleProvider;
 
 /**
  * VersionOne Task View
  * 
  * @author Jerry D. Odenwelder Jr.
  * 
  */
 
 public class TaskView extends ViewPart implements IPropertyChangeListener {
 
     /*
      * These constants are the VersionOne names for the column titles. We
      * localize these values and use that name as column titles
      */
     private static final String V1_COLUMN_TITLE_ID = "ColumnTitle'ID";
     private static final String V1_COLUMN_TITLE_TITLE = "ColumnTitle'Title";
     private static final String V1_COLUMN_TITLE_DETAIL_ESTIMATE = "ColumnTitle'DetailEstimate";
     private static final String V1_COLUMN_TITLE_TO_DO = "ColumnTitle'ToDo";
     private static final String V1_COLUMN_TITLE_STATUS = "ColumnTitle'Status";
     private static final String V1_COLUMN_TITLE_DONE = "ColumnTitle'Done";
     private static final String V1_COLUMN_TITLE_EFFORT = "ColumnTitle'Effort";
     private static final String V1_COLUMN_TITLE_OWNER = "ColumnTitle'Owner";
 
     private ProxySelectionProvider selectionProvider;
     private boolean isEffortColumsShown;
     private TreeViewer viewer;
 
     private ActionsManager actionsManager;
 
     public TaskView() {
         PreferencePage.getPreferences().addPropertyChangeListener(this);
     }
 
     /**
      * Create tree viewer and initialize it.
      */
     public void createPartControl(Composite parent) {
         actionsManager = new ActionsManager(this, getSite());
         viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
         viewer.setContentProvider(new EntityContentProvider());
         viewer.addSelectionChangedListener(actionsManager);
         selectionProvider = new ProxySelectionProvider(viewer);
 
         if (isEnabled()) {
             createColumns();
         }
 
         createActions();
         createContextMenu();
         setProviders();
         getSite().setSelectionProvider(selectionProvider);
     }
 
     /**
      * Called when preferences changed
      */
     public void propertyChange(PropertyChangeEvent event) {
         String property = event.getProperty();
         if (property.equals(PreferenceConstants.P_ENABLED)) {
             if (0 == viewer.getTree().getColumnCount()) {
                 createColumns();
             }
             setProviders();
        } else if (property.equals(PreferenceConstants.P_MEMBER_TOKEN) || isProxyProperty(property)) {
             ApiDataLayer.getInstance().updateCurrentProjectId();
             setupEffortColumns();
         } else if (property.equals(PreferenceConstants.P_ONLY_USER_WORKITEMS)) {
             loadDataToTable();
             viewer.refresh();
         }
 
     }
 
    private boolean isProxyProperty(String property) {
        return property.equals(PreferenceConstants.P_PROXY_ENABLED) || property.equals(PreferenceConstants.P_PROXY_URI) ||
                property.equals(PreferenceConstants.P_PROXY_PASSWORD) || property.equals(PreferenceConstants.P_PROXY_USER);
    }

     public boolean validRowSelected() {
         return !viewer.getSelection().isEmpty();
     }
 
     public Workitem getCurrentWorkitem() {
         ISelection selection = viewer.getSelection();
         if (selection != null && selection instanceof IStructuredSelection) {
             IStructuredSelection structuredSelection = (IStructuredSelection) selection;
             Object element = structuredSelection.getFirstElement();
             return element == null ? null : (Workitem) element;
         }
         return null;
     }
 
     /**
      * Create context menu, assign actions, store items in a collection to
      * manage visibility.
      */
     private void createContextMenu() {
         final Control control = viewer.getControl();
         final MenuManager menuManager = new MenuManager();
         final Menu menu = menuManager.createContextMenu(control);
         menuManager.setRemoveAllWhenShown(true);
         menuManager.addMenuListener(actionsManager);
         control.setMenu(menu);
     }
 
     protected void updateDescription(Workitem currentWorkitem, String value) {
         currentWorkitem.setProperty(Workitem.DESCRIPTION_PROPERTY, value);
 
     }
 
     /**
      * Refresh viewer, causing it to re-read data from model and remove possibly
      * non-relevant items.
      * 
      * @param selection
      *            select element after refresh. null - no selection
      */
 
     public void refreshViewer(IStructuredSelection selection) {
         viewer.getTree().getShell().traverse(SWT.TRAVERSE_TAB_NEXT);
         loadDataToTable();
         viewer.refresh();
         if (selection != null) {
             viewer.setSelection(selection, true);
         }
     }
 
     /**
      * Set the label providers. And load data.
      */
     private void setProviders() {
         enableTreeAndActions(isEnabled());
 
         viewer.getTree().clearAll(true);
 
         if (isEnabled()) {
             loadDataToTable();
         } else {
             viewer.setSorter(null);
             viewer.setInput(getViewSite());
         }
     }
 
     public void enableTreeAndActions(boolean enabled) {
         actionsManager.enableActions(enabled, enabled);
         enableTree(enabled);
     }
 
     private void enableTree(boolean enabled) {
         final Tree tree = viewer.getTree();
         tree.setLinesVisible(enabled);
         tree.setHeaderVisible(enabled);
         tree.setEnabled(enabled);
     }
 
     public void disableAllButRefresh() {
         viewer.getTree().setEnabled(false);
         actionsManager.enableActions(false, true);
     }
 
     /**
      * Configure the table
      */
     private void createColumns() {
         TreeViewerColumn column = createTableViewerColumn(V1_COLUMN_TITLE_ID, 120, SWT.LEFT, -1);
         column.setLabelProvider(new SimpleProvider(Workitem.ID_PROPERTY, true));
         column.setEditingSupport(new TextSupport(Workitem.ID_PROPERTY, viewer, selectionProvider));
 
         column = createTableViewerColumn(V1_COLUMN_TITLE_TITLE, 150, SWT.LEFT, -1);
         column.setLabelProvider(new SimpleProvider(Workitem.NAME_PROPERTY, false));
         column.setEditingSupport(new TextSupport(Workitem.NAME_PROPERTY, viewer, selectionProvider));
 
         column = createTableViewerColumn(V1_COLUMN_TITLE_OWNER, 150, SWT.LEFT, -1);
         column.setLabelProvider(new SimpleProvider(Workitem.OWNERS_PROPERTY, false));
         column.setEditingSupport(new MultiValueSupport(Workitem.OWNERS_PROPERTY, viewer, selectionProvider));
 
         column = createTableViewerColumn(V1_COLUMN_TITLE_STATUS, 100, SWT.LEFT, -1);
         column.setLabelProvider(new SimpleProvider(Workitem.STATUS_PROPERTY, false));
         column.setEditingSupport(new SingleValueSupport(Workitem.STATUS_PROPERTY, viewer, selectionProvider));
 
         column = createTableViewerColumn(V1_COLUMN_TITLE_DETAIL_ESTIMATE, 100, SWT.CENTER, -1);
         column.setLabelProvider(new SimpleProvider(Workitem.DETAIL_ESTIMATE_PROPERTY, false));
         column.setEditingSupport(new TextSupport(Workitem.DETAIL_ESTIMATE_PROPERTY, viewer, selectionProvider));
 
         column = createTableViewerColumn(V1_COLUMN_TITLE_TO_DO, 50, SWT.CENTER, -1);
         column.setLabelProvider(new SimpleProvider(Workitem.TODO_PROPERTY, false));
         column.setEditingSupport(new TextSupport(Workitem.TODO_PROPERTY, viewer, selectionProvider));
 
         if (ApiDataLayer.getInstance().isTrackEffortEnabled()) {
             addEffortColumns();
         }
     }
 
     /**
      * Adds the columns needed to track effort
      */
     private void addEffortColumns() {
         TreeViewerColumn column = createTableViewerColumn(V1_COLUMN_TITLE_DONE, 50, SWT.CENTER, 5);
         column.setLabelProvider(new SimpleProvider(Workitem.DONE_PROPERTY, false));
         column.setEditingSupport(new TextSupport(Workitem.DONE_PROPERTY, viewer, selectionProvider));
 
         column = createTableViewerColumn(V1_COLUMN_TITLE_EFFORT, 50, SWT.CENTER, 6);
         column.setLabelProvider(new SimpleProvider(Workitem.EFFORT_PROPERTY, false));
         column.setEditingSupport(new TextSupport(Workitem.EFFORT_PROPERTY, viewer, selectionProvider));
 
         isEffortColumsShown = true;
     }
 
     /**
      * removes the columns needed when tracking effort
      */
     private void removeEffortColumns() {
         viewer.getTree().getColumn(6).dispose();
         viewer.getTree().getColumn(5).dispose();
         isEffortColumsShown = false;
     }
 
     /**
      * Create the action menus and add them to Action bars and pull down menu.
      */
     private void createActions() {
         IActionBars bars = getViewSite().getActionBars();
         actionsManager.createToolbar(bars.getMenuManager());
         actionsManager.createToolbar(bars.getToolBarManager());
     }
 
     /**
      * Set focus to the Tree.
      */
     public void setFocus() {
         viewer.getControl().setFocus();
     }
 
     /**
      * This method added for testing. It provides access to the underlying
      * TableViewer
      * 
      * @return TableViewer used in this control
      */
     public TreeViewer getViewer() {
         return this.viewer;
     }
 
     /**
      * Determine if VersionOne plugin is enabled in Eclipse preferences.
      */
     private static boolean isEnabled() {
         return PreferencePage.getPreferences().getBoolean(PreferenceConstants.P_ENABLED);
     }
 
     /**
      * Load the Viewer with Task data
      */
     public boolean loadDataToTable() {
         try {
             viewer.setInput(ApiDataLayer.getInstance().getWorkitemTree());
             updateProperty();
             return true;
         } catch (Exception e) {
             disableAllButRefresh();
             Activator.logError(e);
             MessageDialog.openError(viewer.getControl().getShell(), "Task View Error",
                     "Error Occurred Retrieving Task. Check ErrorLog for more Details");
             return false;
         }
     }
 
     /**
      * Returns actions manager for working with actions
      * 
      * @return actions manager
      */
     public ActionsManager getActionsManager() {
         return actionsManager;
     }
 
     /**
      * Create a TableViewerColumn at a specific column location
      * 
      * @param label
      *            - Column label
      * @param width
      *            - Column Width
      * @param alignment
      *            - Column alignment
      * @param index
      *            - location for column. -1 indicates the column goes at the end
      * @return new TableViewerColumn
      */
     TreeViewerColumn createTableViewerColumn(String label, int width, int alignment, int index) {
         TreeViewerColumn rc = null;
         if (-1 == index) {
             rc = new TreeViewerColumn(viewer, SWT.NONE);
         } else {
             rc = new TreeViewerColumn(viewer, SWT.NONE, index);
         }
         rc.getColumn().setWidth(width);
         rc.getColumn().setAlignment(alignment);
         String localName = ApiDataLayer.getInstance().localizerResolve(label);
         if (localName.startsWith(WorkitemPropertySource.COLUMN_TITLE_PREFIX))
             localName = localName.substring(WorkitemPropertySource.COLUMN_TITLE_PREFIX.length());
         rc.getColumn().setText(localName);
         return rc;
     }
 
     public void showMessage(String message) {
         MessageDialog.openInformation(viewer.getControl().getShell(), "Task View", message);
     }
 
     @Override
     public void dispose() {
         PreferencePage.getPreferences().removePropertyChangeListener(this);
         super.dispose();
     }
 
     /**
      * Create or delete effort related columns according to DataLayer status.
      */
     public void setupEffortColumns() {
         if (isEffortColumsShown && !ApiDataLayer.getInstance().isTrackEffortEnabled()) {
             removeEffortColumns();
         } else if (!isEffortColumsShown && ApiDataLayer.getInstance().isTrackEffortEnabled()) {
             addEffortColumns();
         }
         setProviders();
     }
 
     private void updateProperty() {
         Workitem workitem = getCurrentWorkitem();
         if (workitem != null) {
             selectionProvider.setSelection(new StructuredSelection(new WorkitemPropertySource(workitem, getViewer())));
         }
     }
 }
