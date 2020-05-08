 package org.iplantc.core.tito.client.panels;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.tito.client.I18N;
 import org.iplantc.core.tito.client.events.TemplateLoadEvent;
 import org.iplantc.core.tito.client.images.Resources;
 import org.iplantc.core.tito.client.models.JsTemplate;
 import org.iplantc.core.tito.client.models.TemplateSummary;
 import org.iplantc.core.tito.client.services.EnumerationServices;
 import org.iplantc.core.tito.client.widgets.PublishButton;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.util.DateParser;
 
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.tips.QuickTip;
 import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 /**
  * 
  * A grid panel for listing all the template integration
  * 
  * @author sriram
  *
  */
 public class TemplatesListingGridPanel extends ContentPanel {
     private static final String ID_BTN_CREATE = "idBtnCreate";
 	private static final String ID_DELETE = "idBtnDelete"; //$NON-NLS-1$
     private static final String ID_COPY = "idBtnCopy"; //$NON-NLS-1$
     private static final String ID_EDIT = "idBtnEdit"; //$NON-NLS-1$
     private static final String ID_PUBLISH = "idBtnPublish"; //$NON-NLS-1$
     private Grid<TemplateSummary> grid;
     private HashMap<String,Button> buttons;
 
     /**
      * creates new instance of TemplateListingGridPanel
      * 
      * @param addButtonCommand
      */
     public TemplatesListingGridPanel(Command addButtonCommand) {
         init(addButtonCommand);
     }
 
     private void init(Command addButtonCommand) {
         setHeading(I18N.DISPLAY.templateList());
         setBorders(false);
         setLayout(new FitLayout());
        setSize(800, 575);
         buttons = new HashMap<String,Button>();
 
         GridSelectionModel<TemplateSummary> checkBoxModel = buildSelectionModel();
         grid = new Grid<TemplateSummary>(buildStore(), buildColumnModel());
         grid.setSelectionModel(checkBoxModel);
         grid.setAutoExpandColumn(TemplateSummary.NAME);
         grid.getView().setForceFit(true);
         new QuickTip(grid);
 
         Button addBtn = buildAddButton(addButtonCommand);
         compose(addBtn);
     }
 
     private GridSelectionModel<TemplateSummary> buildSelectionModel() {
         GridSelectionModel<TemplateSummary> selectionModel = new GridSelectionModel<TemplateSummary>();
         selectionModel.setSelectionMode(SelectionMode.SINGLE);
         selectionModel.addSelectionChangedListener(new SelectionChangedListener<TemplateSummary>() {
             @Override
             public void selectionChanged(SelectionChangedEvent<TemplateSummary> se) {
                 updateButtonEnablement(se);
             }
         });
 
         return selectionModel;
     }
 
     /**
      * Enables the edit button if a row is selected, or disables it if no selection.
      * 
      * @param se
      * @param editButton
      */
     private void updateButtonEnablement(SelectionChangedEvent<TemplateSummary> se) {
         TemplateSummary selection = se.getSelectedItem();
         if (selection != null) {
             PublishButton btnPublish = (PublishButton)buttons.get(ID_PUBLISH);
             btnPublish.setTemplateId(selection.getId());
             if(!selection.isPublic()) {
                 setStatusOfAllButtons(true);
                 buttons.get(ID_PUBLISH).setEnabled(selection.isPublishable());
             } else {
                 //public apps can only be copied.
                 buttons.get(ID_COPY).setEnabled(true);
                 buttons.get(ID_DELETE).setEnabled(false);
                 buttons.get(ID_EDIT).setEnabled(false);
                 buttons.get(ID_PUBLISH).setEnabled(true);
             }
         } else {
             setStatusOfAllButtons(false);
         }
     }
 
     private void setStatusOfAllButtons(boolean enabled) {
         for (Button b : buttons.values()) {
            b.setEnabled(enabled);
         }
     }
 
     private void compose(Button addButton) {
         ToolBar toolBar = new ToolBar();
 
         toolBar.add(addButton);
         toolBar.add(buildEditButton());
         toolBar.add(buildCopyButton());
         toolBar.add(buildPublishButton());
         toolBar.add(new FillToolItem());
         toolBar.add(buildDeleteButton());
         
         setTopComponent(toolBar);
         add(grid);
     }
 
     private Button buildAddButton(final Command addButtonCommand) {
         Button btn = new Button(I18N.DISPLAY.newTemplate());
         btn.setId(ID_BTN_CREATE);
         btn.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 addButtonCommand.execute();
             }
         });
         btn.setIcon(AbstractImagePrototype.create(Resources.ICONS.add()));
         return btn;
     }
 
     private Button buildDeleteButton() {
         Button btn = new Button(I18N.DISPLAY.delete());
         btn.setId(ID_DELETE);
         btn.setIcon(AbstractImagePrototype.create(Resources.ICONS.cancel()));
         buttons.put(ID_DELETE, btn);
 
         final Listener<MessageBoxEvent> callback = new Listener<MessageBoxEvent>() {
             @Override
             public void handleEvent(MessageBoxEvent ce) {
                 Button btn = ce.getButtonClicked();
                 // did the user click yes?
                 if (btn.getItemId().equals(Dialog.YES)) {
                     doDelete(grid.getSelectionModel().getSelectedItem().getId());
                 }
             }
         };
 
         btn.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 MessageBox.confirm(I18N.DISPLAY.warning(), I18N.DISPLAY.msgTemplateDelete(), callback);
             }
         });
         btn.setEnabled(false);
         return btn;
     }
     
     private Button buildCopyButton() {
         Button btn = new Button(I18N.DISPLAY.copy());
         btn.setId(ID_COPY);
         btn.setIcon(AbstractImagePrototype.create(Resources.ICONS.copy()));
         btn.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 fireTemplateLoadEvent(grid.getSelectionModel().getSelectedItem(), TemplateLoadEvent.MODE.COPY);
             }
         });
         buttons.put(ID_COPY,btn);
     
         btn.setEnabled(false);
         return btn;
     }
 
     private PublishButton buildPublishButton() {
         final PublishButton button = new PublishButton() {
             @Override
             protected void unorderedNoPublish() {
                 MessageBox.info(I18N.DISPLAY.information(), I18N.DISPLAY.unorderedArgumentsInfoMsg(),
                                 null);
             }
 
             @Override
             protected boolean isOrdered() {
                 if (grid.getSelectionModel().getSelectedItem() != null) {
                     return grid.getSelectionModel().getSelectedItem().isOrdered();
                 } else {
                     return false;
                 }
             }
 
             @Override
             protected void afterPublishSucess(String result) {
                 updatePublishDetails(getTemplateId());
             }
         };
         button.setId(ID_PUBLISH);
         buttons.put(ID_PUBLISH, button);
         button.setEnabled(false);
         return button;
     }
 
     private void updatePublishDetails(final String templateId) {
         EnumerationServices services = new EnumerationServices();
         services.getIntegrationAsSummary(templateId, new AsyncCallback<String>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
             @Override
             public void onSuccess(String result) {
                TemplateSummary summary = grid.getStore().findModel(TemplateSummary.ID, templateId);
                 JSONArray arr = JsonUtil.getArray(JsonUtil.getObject(result), "objects"); //$NON-NLS-1$
                 if (arr != null && arr.size() > 0 && summary != null) {
                     com.google.gwt.core.client.JsArray<JsTemplate> jsarrayTemplates = JsonUtil
                             .asArrayOf(arr.toString());
                     JsTemplate jstemplate = jsarrayTemplates.get(0);
 
                     summary.set(TemplateSummary.STATUS, jstemplate.getStatus());
                     summary.set(TemplateSummary.PUBLISHED_DATE,
                             DateParser.parseDate(jstemplate.getPublishedDate()));
 
                     grid.getStore().update(summary);
 
                 }
             }
         });
     }
 
     private void doDelete(final String id) {
         EnumerationServices services = new EnumerationServices();
         UserInfo user = UserInfo.getInstance();
         if (user != null && user.getUsername() != null && !user.getUsername().isEmpty()) {
             JSONObject body = new JSONObject();
             body.put("tito", new JSONString(id)); //$NON-NLS-1$
             body.put("user", new JSONString(user.getUsername())); //$NON-NLS-1$
             body.put("action", new JSONString("delete")); //$NON-NLS-1$ //$NON-NLS-2$
 
             services.deleteIntegration(body, new AsyncCallback<String>() {
 
                 @Override
                 public void onFailure(Throwable arg0) {
                     ErrorHandler.post(I18N.DISPLAY.deleteFailed(), arg0);
                 }
 
                 @Override
                 public void onSuccess(String result) {
                     TemplateSummary ts = grid.getStore().findModel(TemplateSummary.ID, id);
                     if (ts != null) {
                         grid.getStore().remove(ts);
                     }
                 }
             });
         } else {
             ErrorHandler.post(I18N.DISPLAY.deleteFailed());
         }
     }
 
     private Button buildEditButton() {
         Button btn = new Button(I18N.DISPLAY.editTemplate());
         btn.setId(ID_EDIT);
         buttons.put(ID_EDIT,btn);
         btn.setIcon(AbstractImagePrototype.create(Resources.ICONS.listItems()));
         btn.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 // getSelectedItem() should never return null because the edit button can
                 // only be clicked
                 // when a tool is selected
                 fireTemplateLoadEvent(grid.getSelectionModel().getSelectedItem(), TemplateLoadEvent.MODE.EDIT);
             }
         });
         btn.setEnabled(false);
         return btn;
     }
 
     private void fireTemplateLoadEvent(TemplateSummary t, TemplateLoadEvent.MODE mode) {
         EventBus.getInstance().fireEvent(new TemplateLoadEvent(t.getId(),mode));
     }
 
     private ListStore<TemplateSummary> buildStore() {
         final ListStore<TemplateSummary> store = new ListStore<TemplateSummary>();
         EnumerationServices services = new EnumerationServices();
         UserInfo user = UserInfo.getInstance();
         if (user != null && user.getUsername() != null && !user.getUsername().equals("")) { //$NON-NLS-1$
             services.getIntegrationsAsSummary(user.getUsername(), new AsyncCallback<String>() {
                 @Override
                 public void onSuccess(String result) {
                     store.add(parseTools(result));
                     store.sort(TemplateSummary.LAST_EDITED_DATE, SortDir.DESC);
                 }
 
                 @Override
                 public void onFailure(Throwable caught) {
                     ErrorHandler.post(I18N.DISPLAY.cantRetrieveIntegrations(), caught);
                 }
             });
             return store;
         } else {
             ErrorHandler.post(I18N.DISPLAY.cantRetrieveTemplates());
             return null;
         }
     }
 
     private List<TemplateSummary> parseTools(String json) {
         List<TemplateSummary> templates = new ArrayList<TemplateSummary>();
         JSONArray arr = JsonUtil.getArray(JsonUtil.getObject(json), "objects"); //$NON-NLS-1$
         if (arr != null && arr.size() > 0) {
             com.google.gwt.core.client.JsArray<JsTemplate> jsarrayTemplates = JsonUtil.asArrayOf(arr.toString());
             for (int i = 0; i < jsarrayTemplates.length(); i++) {
                 JsTemplate template = jsarrayTemplates.get(i);
                 TemplateSummary t = new TemplateSummary(template.getId(), template.getName(),
                         template.getStatus(), template.getLastEditedDate(), template.getPublishedDate(),
                         template.isPUblic(), template.isPublishable(), template.isOrdered());
                 templates.add(t);
             }
         }
         return templates;
     }
 
     private ColumnModel buildColumnModel() {
         ColumnConfig name = new ColumnConfig(TemplateSummary.NAME, I18N.DISPLAY.name(), 200);
         ColumnConfig status = new ColumnConfig(TemplateSummary.STATUS, I18N.DISPLAY.status(), 100);
         ColumnConfig last_edited = new ColumnConfig(TemplateSummary.LAST_EDITED_DATE,
                 I18N.DISPLAY.lastEdited(), 150);
         ColumnConfig published_date = new ColumnConfig(TemplateSummary.PUBLISHED_DATE,
                 I18N.DISPLAY.publishToWorkspaceOn(), 150);
 
         name.setRenderer(new TemplateNameCellRenderer());
 
         DateTimeFormat dateTimeFormatter = DateTimeFormat
                 .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
         last_edited.setDateTimeFormat(dateTimeFormatter);
         published_date.setDateTimeFormat(dateTimeFormatter);
 
         return new ColumnModel(Arrays.asList(name, status, last_edited, published_date));
     }
 
     private class TemplateNameCellRenderer implements GridCellRenderer<TemplateSummary> {
 
         @Override
         public Object render(TemplateSummary model, String property, ColumnData config, int rowIndex,
                 int colIndex, ListStore<TemplateSummary> store, Grid<TemplateSummary> grid) {
             if(model.isPublic()) {
                 String tip = model.getName() + "- submitted for public use";
                 return "<span qtip='" + tip + "'>" + model.getName() + "</span>" + "&nbsp;<img src='./images/world.png' height='12px' width='12px'></img>" ;
             } else {
                 return "<span qtip='" + model.getName() + "'>"  + model.getName() + "</span>" ;
             }
         }
         
     }
     
 }
