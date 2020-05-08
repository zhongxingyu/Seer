 package org.iplantc.de.client.views.panels;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.images.Resources;
 import org.iplantc.de.client.models.AnalysisExecution;
 import org.iplantc.de.client.models.DataWindowConfig;
 import org.iplantc.de.client.services.AnalysisServiceFacade;
 import org.iplantc.de.client.utils.MyDataViewContextExecutor;
 import org.iplantc.de.client.utils.NotificationHelper;
 import org.iplantc.de.client.utils.NotifyInfo;
 import org.iplantc.de.client.views.MyAnalysesGrid;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.Store;
 import com.extjs.gxt.ui.client.store.StoreEvent;
 import com.extjs.gxt.ui.client.store.StoreFilter;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 /**
  * A container panel of MyAanalysesGrid
  * 
  * @author sriram
  * 
  */
 public class MyAnalysesPanel extends ContentPanel {
 
     private final String DELETE_ITEM_ID = "idDeleteBtn"; //$NON-NLS-1$
     private final String CANCEL_JOB_ITEM_ID = "idCancelJobBtn"; //$NON-NLS-1$
     private final String VIEW_OUTPUT_ITEM_ID = "idViewBtn"; //$NON-NLS-1$
     private static final String VIEW_PARAMETER_ITEM_ID = "idViewParameter";
 
     private MyAnalysesGrid analysisGrid;
 
     private final HashMap<String, Button> analyses_buttons;
     private final HashMap<String, Menu> menus;
 
     private ToolBar topComponentMenu;
 
     private ArrayList<HandlerRegistration> handlers;
 
     private String idWorkspace;
 
     private String idCurrentSelection;
 
     private final AnalysisServiceFacade facadeAnalysisService;
 
     protected static CheckBoxSelectionModel<AnalysisExecution> sm;
     private TextField<String> filter;
 
     /**
      * Indicates the status of a job's.
      */
     public static enum EXECUTION_STATUS {
         /** job status unknown */
         UNKNOWN(I18N.CONSTANT.unknown()),
         /** job is ready */
         SUBMITTED(I18N.CONSTANT.submitted()),
         /** job is running */
         RUNNING(I18N.CONSTANT.running()),
         /** job is complete */
         COMPLETED(I18N.CONSTANT.completed()),
         /** job timed out */
         HELD(I18N.CONSTANT.held()),
         /** job failed */
         FAILED(I18N.CONSTANT.failed()),
         /** job was stopped */
         SUBMISSION_ERR(I18N.CONSTANT.subErr()),
         /** job is idle */
         IDLE(I18N.CONSTANT.idle()),
         /** job is removed */
         REMOVED(I18N.CONSTANT.removed());
 
         private String displayText;
 
         private EXECUTION_STATUS(String displaytext) {
             this.displayText = displaytext;
         }
 
         /**
          * Returns a string that identifies the EXECUTION_STATUS.
          * 
          * @return
          */
         public String getTypeString() {
             return toString().toLowerCase();
         }
 
         /**
          * Null-safe and case insensitive variant of valueOf(String)
          * 
          * @param typeString name of an EXECUTION_STATUS constant
          * @return
          */
         public static EXECUTION_STATUS fromTypeString(String typeString) {
             if (typeString == null || typeString.isEmpty()) {
                 return null;
             }
 
             return valueOf(typeString.toUpperCase());
         }
 
         @Override
         public String toString() {
             return displayText;
         }
     }
 
     /**
      * Create a new MyAnalysisPanel
      * 
      * @param caption text to be displayed as caption
      * @param idCurrentSelection
      * 
      */
     public MyAnalysesPanel(final String caption, final String idCurrentSelection) {
         this.idCurrentSelection = idCurrentSelection;
         analyses_buttons = new LinkedHashMap<String, Button>();
         menus = new HashMap<String, Menu>();
         init(caption);
         initWorkspaceId();
 
         facadeAnalysisService = new AnalysisServiceFacade();
     }
 
     private void initWorkspaceId() {
         idWorkspace = UserInfo.getInstance().getWorkspaceId();
     }
 
     private void init(String caption) {
         buildTopComponent();
         setTopComponent(topComponentMenu);
         setHeading(caption);
         setLayout(new FitLayout());
         registerHandlers();
     }
 
     private void registerHandlers() {
         handlers = new ArrayList<HandlerRegistration>();
     }
 
     private void buildTopComponent() {
         topComponentMenu = new ToolBar();
         topComponentMenu.add(buildViewOpButton());
         topComponentMenu.add(buildViewParamsButton());
         topComponentMenu.add(buildDeleteButton());
         topComponentMenu.add(buildCancelJobButton());
         buildFilterField();
         topComponentMenu.add(filter);
     }
 
     private void setButtonState() {
         int selectionSize = 0;
 
         if (analysisGrid != null) {
             selectionSize = analysisGrid.getSelectionModel().getSelectedItems().size();
         }
 
         switch (selectionSize) {
             case 0:
                 for (Button btn : analyses_buttons.values()) {
                     btn.disable();
                 }
 
                 break;
 
             case 1:
                 enableDeleteButtonByStatus();
                 enableViewButtonByStatus();
                 enableCancelJobButtonByStatus();
                 break;
 
             default:
                 analyses_buttons.get(DELETE_ITEM_ID).enable();
                 analyses_buttons.get(VIEW_PARAMETER_ITEM_ID).disable();
                 analyses_buttons.get(VIEW_OUTPUT_ITEM_ID).disable();
                 enableCancelJobButtonByStatus();
         }
     }
 
     private Button buildDeleteButton() {
         Button b = new Button(I18N.DISPLAY.delete());
         b.setId(DELETE_ITEM_ID);
         b.setIcon(AbstractImagePrototype.create(Resources.ICONS.cancel()));
         b.setEnabled(false);
         b.addSelectionListener(new DeleteSelectionListener());
         analyses_buttons.put(DELETE_ITEM_ID, b);
         return b;
     }
 
     private Button buildCancelJobButton() {
        Button b = new Button(I18N.DISPLAY.cancelJob());
         b.setId(CANCEL_JOB_ITEM_ID);
         b.setIcon(AbstractImagePrototype.create(Resources.ICONS.stop()));
         b.setEnabled(false);
         b.addSelectionListener(new CancelJobSelectListener());
         analyses_buttons.put(CANCEL_JOB_ITEM_ID, b);
 
         return b;
     }
 
     private Button buildViewOpButton() {
         Button b = new Button(I18N.DISPLAY.viewOutput());
         b.setId(VIEW_OUTPUT_ITEM_ID);
         b.setIcon(AbstractImagePrototype.create(Resources.ICONS.fileView()));
         b.setEnabled(false);
         b.addSelectionListener(new ViewOutputSelectionListener());
         analyses_buttons.put(VIEW_OUTPUT_ITEM_ID, b);
         return b;
     }
 
     private Button buildViewParamsButton() {
         Button b = new Button(I18N.DISPLAY.viewParamLbl());
         b.setId(VIEW_PARAMETER_ITEM_ID);
         b.setIcon(AbstractImagePrototype.create(Resources.ICONS.fileView()));
         b.setEnabled(false);
         b.addSelectionListener(new ViewParamSelectionListener());
         analyses_buttons.put(VIEW_PARAMETER_ITEM_ID, b);
         return b;
     }
 
     /**
      * Builds a text field for filtering items displayed in the data container.
      */
     private void buildFilterField() {
         filter = new TextField<String>() {
             @Override
             public void onKeyUp(FieldEvent fe) {
                 analysisGrid.getStore().applyFilters(null);
             }
         };
 
         filter.setEmptyText(I18N.DISPLAY.filterAnalysesList());
 
     }
 
     private class StoreFilterImpl implements StoreFilter<AnalysisExecution> {
         @Override
         public boolean select(Store<AnalysisExecution> store, AnalysisExecution parent,
                 AnalysisExecution item, String property) {
             if (filter != null && filter.getValue() != null && !filter.getValue().isEmpty()) {
                 return item.getName().toLowerCase().startsWith(filter.getValue().toLowerCase())
                         || item.getAnalysisName().toLowerCase()
                                 .startsWith(filter.getValue().toLowerCase());
             }
 
             return true;
 
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onRender(Element parent, int index) {
         super.onRender(parent, index);
 
         buildCheckBoxSelectionModel(menus);
         analysisGrid = MyAnalysesGrid.createInstance(sm);
         if (getIdCurrentSelection() != null && analysisGrid.getStore() != null) {
             analysisGrid.setCurrentSelection(getIdCurrentSelection());
         }
 
         analysisGrid.getStore().addFilter(new StoreFilterImpl());
         analysisGrid.getView().setEmptyText(I18N.DISPLAY.noAnalyses());
         add(analysisGrid);
         addGridEventListeners();
     }
 
     /**
      * Builds the CheckBoxSelectionModel used in the ColumnDisplay.ALL ColumnModel.
      * 
      * @param menus2
      */
     protected static void buildCheckBoxSelectionModel(final HashMap<String, Menu> floating_menus) {
         if (sm != null) {
             return;
         }
         sm = new CheckBoxSelectionModel<AnalysisExecution>();
         sm.getColumn().setAlignment(HorizontalAlignment.CENTER);
     }
 
     @SuppressWarnings({"rawtypes"})
     private void addGridEventListeners() {
         analysisGrid.getSelectionModel().addListener(Events.SelectionChange, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 setButtonState();
                 AnalysisExecution ae = analysisGrid.getSelectionModel().getSelectedItem();
                 if (ae != null) {
                     idCurrentSelection = ae.getId();
                 }
             }
         });
         analysisGrid.getStore().addListener(Store.Update, new Listener<StoreEvent>() {
 
             @Override
             public void handleEvent(StoreEvent be) {
                 setButtonState();
             }
         });
     }
 
     private void doDelete() {
         if (analysisGrid.getSelectionModel().getSelectedItems().size() > 0) {
             final List<AnalysisExecution> execs = analysisGrid.getSelectionModel().getSelectedItems();
             MessageBox.confirm(I18N.DISPLAY.warning(), I18N.DISPLAY.analysesExecDeleteWarning(),
                     new DeleteMessageBoxListener(execs));
         }
     }
 
     private void doCancelJob() {
         if (analysisGrid.getSelectionModel().getSelectedItems().size() > 0) {
             final List<AnalysisExecution> execs = analysisGrid.getSelectionModel().getSelectedItems();
             for (AnalysisExecution ae : execs) {
                 if (ae.getStatus().equalsIgnoreCase(EXECUTION_STATUS.RUNNING.toString())) {
                     facadeAnalysisService.stopJob(ae.getId(), new CancelJobServiceCallback(ae));
                 }
             }
         }
 
     }
 
     private void enableViewButtonByStatus() {
         AnalysisExecution ae = analysisGrid.getSelectionModel().getSelectedItem();
         if (ae != null) {
             if (ae.getStatus().equalsIgnoreCase((EXECUTION_STATUS.COMPLETED.toString()))
                     || ae.getStatus().equalsIgnoreCase((EXECUTION_STATUS.FAILED.toString()))) {
                 analyses_buttons.get(VIEW_OUTPUT_ITEM_ID).enable();
                 analyses_buttons.get(VIEW_PARAMETER_ITEM_ID).enable();
             } else {
                 analyses_buttons.get(VIEW_OUTPUT_ITEM_ID).disable();
                 analyses_buttons.get(VIEW_PARAMETER_ITEM_ID).enable();
             }
         }
     }
 
     private void enableDeleteButtonByStatus() {
         List<AnalysisExecution> aes = analysisGrid.getSelectionModel().getSelectedItems();
         boolean enable = true;
         for (AnalysisExecution ae : aes) {
             if (ae != null) {
                 if (!ae.getStatus().equalsIgnoreCase((EXECUTION_STATUS.COMPLETED.toString()))
                         && !ae.getStatus().equalsIgnoreCase((EXECUTION_STATUS.FAILED.toString()))) {
                     enable = false;
                     break;
                 }
             }
         }
         analyses_buttons.get(DELETE_ITEM_ID).setEnabled(enable);
     }
 
     private void enableCancelJobButtonByStatus() {
         List<AnalysisExecution> aes = analysisGrid.getSelectionModel().getSelectedItems();
         boolean enable = false;
         for (AnalysisExecution ae : aes) {
             if (ae != null) {
                 if (ae.getStatus().equalsIgnoreCase((EXECUTION_STATUS.RUNNING.toString()))) {
                     enable = true;
                     break;
                 }
             }
         }
         analyses_buttons.get(CANCEL_JOB_ITEM_ID).setEnabled(enable);
 
     }
 
     private class DeleteSelectionListener extends SelectionListener<ButtonEvent> {
         @Override
         public void componentSelected(ButtonEvent ce) {
             doDelete();
         }
     }
 
     private class CancelJobSelectListener extends SelectionListener<ButtonEvent> {
         @Override
         public void componentSelected(ButtonEvent ce) {
             doCancelJob();
         }
     }
 
     private class ViewOutputSelectionListener extends SelectionListener<ButtonEvent> {
         @Override
         public void componentSelected(ButtonEvent ce) {
             retrieveOutputFolder(analysisGrid.getSelectionModel().getSelectedItem().getId());
         }
     }
 
     private class ViewParamSelectionListener extends SelectionListener<ButtonEvent> {
         @Override
         public void componentSelected(ButtonEvent ce) {
             AnalysisExecution ae = analysisGrid.getSelectionModel().getSelectedItem();
             Dialog d = new Dialog();
             d.setResizable(false);
             d.setHeading(I18N.DISPLAY.viewParameters(ae.getName()));
             d.add(new AnalysisParameterViewerPanel(ae.getId()));
             d.setSize(520, 375);
             d.setButtons(Dialog.OK);
             d.setHideOnButtonClick(true);
             d.show();
         }
     }
 
     private void retrieveOutputFolder(String id) {
         AnalysisExecution ae = analysisGrid.getStore().findModel("id", id); //$NON-NLS-1$
 
         if (ae != null && ae.getResultFolderId() != null && !ae.getResultFolderId().isEmpty()) {
             JSONObject context = new JSONObject();
             context.put(DataWindowConfig.FOLDER_ID, new JSONString(ae.getResultFolderId()));
 
             MyDataViewContextExecutor contextExec = new MyDataViewContextExecutor();
             contextExec.execute(context.toString());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void cleanup() {
         EventBus eventbus = EventBus.getInstance();
 
         // unregister
         for (HandlerRegistration reg : handlers) {
             eventbus.removeHandler(reg);
         }
 
         // clear our list
         handlers.clear();
         analysisGrid.cleanup();
     }
 
     /**
      * update id of the execution that needs to selected
      * 
      * @param id id of the analysis execution that needs to selected
      */
     public void updateSelection(String idCurrentSelection) {
         if (analysisGrid != null) {
             analysisGrid.selectModel(idCurrentSelection);
         }
     }
 
     /**
      * @return the idCurrentSelection
      */
     public String getIdCurrentSelection() {
         return idCurrentSelection;
     }
 
     private final class DeleteSeviceCallback implements AsyncCallback<String> {
         private final List<AnalysisExecution> execs;
         private final List<AnalysisExecution> items_to_delete;
 
         private DeleteSeviceCallback(List<AnalysisExecution> items_to_delete,
                 List<AnalysisExecution> execs) {
             this.execs = execs;
             this.items_to_delete = items_to_delete;
         }
 
         @Override
         public void onSuccess(String arg0) {
             updateGrid(execs);
 
         }
 
         @Override
         public void onFailure(Throwable caught) {
             ErrorHandler.post(I18N.ERROR.deleteJobError(), caught);
         }
 
         private void updateGrid(List<AnalysisExecution> execs) {
             for (AnalysisExecution ae : items_to_delete) {
                 analysisGrid.getStore().remove(ae);
             }
 
             if (items_to_delete == null || execs.size() != items_to_delete.size()) {
                 MessageBox.alert(I18N.DISPLAY.warning(), I18N.DISPLAY.analysesNotDeleted(), null);
             }
         }
     }
 
     private final class CancelJobServiceCallback implements AsyncCallback<String> {
 
         private final AnalysisExecution ae;
 
         public CancelJobServiceCallback(final AnalysisExecution ae) {
             this.ae = ae;
         }
 
         @Override
         public void onSuccess(String result) {
             NotifyInfo.notify(NotificationHelper.Category.ANALYSIS, "Success!", "Job \"" + ae.getName()
                     + "\" stopped successfully.", null);
         }
 
         @Override
         public void onFailure(Throwable caught) {
             /*
              * JDS Send generic error message. In the future, the "error_code" string should be parsed
              * from the JSON to provide more detailed user feedback.
              */
            ErrorHandler.post(I18N.ERROR.stopJobError(ae.getName()), caught);
         }
 
     }
 
     private final class DeleteMessageBoxListener implements Listener<MessageBoxEvent> {
         private final List<AnalysisExecution> execs;
         private final List<AnalysisExecution> items_to_delete;
 
         private DeleteMessageBoxListener(List<AnalysisExecution> execs) {
             this.execs = execs;
             items_to_delete = new ArrayList<AnalysisExecution>();
         }
 
         @Override
         public void handleEvent(MessageBoxEvent ce) {
             Button btn = ce.getButtonClicked();
 
             // did the user click yes?
             if (btn.getItemId().equals(Dialog.YES)) {
                 String body = buildDeleteRequestBody(execs);
                 facadeAnalysisService.deleteJob(idWorkspace, body, new DeleteSeviceCallback(
                         items_to_delete, execs));
             }
         }
 
         private String buildDeleteRequestBody(List<AnalysisExecution> execs) {
             JSONObject obj = new JSONObject();
             JSONArray items = new JSONArray();
             int count = 0;
             for (AnalysisExecution ae : execs) {
                 if (ae.getStatus().equalsIgnoreCase((EXECUTION_STATUS.COMPLETED.toString()))
                         || ae.getStatus().equalsIgnoreCase((EXECUTION_STATUS.FAILED.toString()))) {
                     items.set(count++, new JSONString(ae.getId()));
                     items_to_delete.add(ae);
                 }
 
             }
             obj.put("executions", items); //$NON-NLS-1$
             return obj.toString();
         }
     }
 
 }
