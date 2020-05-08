 package org.jboss.ballroom.client.widgets.forms;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.client.ui.DeckPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.RowCountChangeEvent;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import org.jboss.ballroom.client.rbac.SecurityContext;
 import org.jboss.ballroom.client.rbac.SecurityService;
 import org.jboss.ballroom.client.spi.Framework;
 import org.jboss.ballroom.client.widgets.common.DefaultButton;
 import org.jboss.ballroom.client.widgets.window.DialogueOptions;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Heiko Braun
  * @date 11/12/12
  */
 public abstract class AbstractForm<T> implements FormAdapter<T> {
 
     public static final String EXPR_TAG = "EXPRESSIONS";
 
     protected final Map<String, Map<String, FormItem>> formItems = new LinkedHashMap<String, Map<String, FormItem>>();
     protected final Map<String,GroupRenderer> renderer = new HashMap<String, GroupRenderer>();
     protected int numColumns = 1;
     protected int nextId = 1;
     protected int maxTitleLength = 0;
 
     private DeckPanel deck;
     private List<PlainFormView> plainViews = new ArrayList<PlainFormView>();
     private boolean isEnabled =true;
 
     final static String DEFAULT_GROUP = "default";
     protected FormCallback toolsCallback = null; // if set, the tools will be provided externally
 
     public abstract void edit(T bean);
 
     public abstract void cancel();
 
     public abstract Map<String, Object> getChangedValues();
 
     public abstract T getUpdatedEntity();
 
     static Framework FRAMEWORK  = GWT.create(Framework.class);
     static SecurityService SECURITY_SERVICE = FRAMEWORK.getSecurityService();
 
     public FormValidation validate()
     {
 
         FormValidation outcome = new FormValidation();
 
         for(Map<String, FormItem> groupItems : formItems.values())
         {
             for(FormItem item : groupItems.values())
             {
                 // two cases: empty form (create entity) and updating an existing entity
                 // we basically force validation on newly created entities
                 boolean requiresValidation = getEditedEntity()!=null ? item.isModified() : true;
 
                 if(requiresValidation)
                 {
                     Object value = item.getValue();
 
                     // ascii or empty string are ok. the later will be checked in each form item implementation.
                     String stringValue = String.valueOf(value);
                     boolean ascii = stringValue.isEmpty() ||
                             stringValue.matches("^[\\u0020-\\u007e]+$");
 
                     if(!ascii)
                     {
                         outcome.addError(item.getName());
                         item.setErroneous(true);
                     }
                     else
                     {
                         boolean validValue = item.validate(value);
                         if(validValue)
                         {
                             item.setErroneous(false);
                         }
                         else
                         {
                             outcome.addError(item.getName());
                             item.setErroneous(true);
                         }
                     }
                 }
             }
         }
 
         return outcome;
     }
 
 
     public Widget asWidget() {
         return build();
     }
 
     public abstract Set<String> getReadOnlyNames();
 
     private Widget build() {
 
         deck = new DeckPanel();
         deck.setStyleName("fill-layout-width");
 
         // RBAC
         SecurityContext securityContext = SECURITY_SERVICE.getSecurityContext();
         boolean writePriviledges = securityContext.getWritePriviledge().isGranted();
 
         // ----------------------
         // view panel
 
         VerticalPanel viewPanel = new VerticalPanel();
         viewPanel.setStyleName("fill-layout-width");
         viewPanel.addStyleName("form-view-panel");
 
         if(toolsCallback!=null)
         {
 
             final HTML edit = new HTML("<i class='icon-edit'></i>&nbsp; Edit");
             edit.setStyleName("form-edit-button");
             ClickHandler editHandler = new ClickHandler() {
                 @Override
                 public void onClick(ClickEvent event) {
                     setEnabled(true);
                     toggleViews();
                 }
             };
             edit.addClickHandler(editHandler);
             viewPanel.add(edit);
 
             if(!writePriviledges)
                 edit.getElement().addClassName("rbac-suppressed");
         }
 
         deck.add(viewPanel.asWidget());
 
         // ----------------------
         // edit panel
 
         VerticalPanel editPanel = new VerticalPanel();
         editPanel.setStyleName("fill-layout-width");
         editPanel.addStyleName("form-edit-panel");
 
         if(toolsCallback!=null)
         {
             final HTML editDisabled = new HTML("<i class='icon-edit'></i>&nbsp; Edit");
             editDisabled.setStyleName("form-edit-button-disabled");
             editPanel.add(editDisabled);
         }
 
         RenderMetaData metaData = new RenderMetaData();
         metaData.setNumColumns(numColumns);
         metaData.setTitleWidth(maxTitleLength);
 
         // RBAC
         Set<String> readOnly = getReadOnlyNames();
 
         for(String group : formItems.keySet())
         {
             Map<String, FormItem> groupItems = formItems.get(group);
 
             // RBAC: read-only attributes
             for(String attr : groupItems.keySet())
             {
                 FormItem formItem = groupItems.get(attr);
                //formItem.setFiltered(false); // reset to default state
 
                 if(readOnly.contains(attr))
                 {
                     formItem.setFiltered(true);
                 }
             }
 
 
             GroupRenderer groupRenderer = null;
 
             if(DEFAULT_GROUP.equals(group))
                 groupRenderer = new DefaultGroupRenderer();
             else
                 groupRenderer = renderer.get(group)!=null ? renderer.get(group) : new FieldsetRenderer();
 
             // edit view
             Widget widget = groupRenderer.render(metaData, group, groupItems);
             editPanel.add(widget);
 
             // plain view
             PlainFormView plainView = new PlainFormView(new ArrayList<FormItem>(groupItems.values()));
             plainView.setNumColumns(numColumns);
             plainViews.add(plainView);
             viewPanel.add(groupRenderer.renderPlain(metaData, group, plainView));
         }
 
         // inline tools (if callback is provided)
         if(toolsCallback!=null)
         {
             editPanel.add(
                     new DialogueOptions(
                             "Save",
                             new ClickHandler() {
                                 @Override
                                 public void onClick(ClickEvent event) {
 
                                     if(!validate().hasErrors())
                                     {
                                         setEnabled(false);
                                         toggleViews();
                                         toolsCallback.onSave(getChangedValues());
                                     }
                                 }
                             },
                             "Cancel",
                             new ClickHandler() {
                                 @Override
                                 public void onClick(ClickEvent event) {
                                     setEnabled(false);
                                     toggleViews();
                                     cancel();
 
                                     // TODO: used anymore?
                                 }
                             }
                     )
             );
         }
 
         deck.add(editPanel);
 
         // toggle default view
         toggleViews();
         refreshPlainView(); // make sure it's build, even empty...
 
         return deck;
     }
 
     /**
      * Number of layout columns.<br>
      * Form fields will fill columns in the order they have been specified
      * in {@link #setFields(org.jboss.ballroom.client.widgets.forms.FormItem[])}.
      *
      * @param columns
      */
     public void setNumColumns(int columns)
     {
         //this.numColumns = columns;
     }
 
     /**
      * Specify the form fields.
      * Needs to be called before {@link #asWidget()}.
      *
      * @param items
      */
     public void setFields(FormItem... items) {
         setFieldsInGroup(Form.DEFAULT_GROUP, items);
     }
 
     public void setFields(FormItem[]... items) {
         setFieldsInGroup(Form.DEFAULT_GROUP, flatten(items));
     }
 
     public void setFieldsInGroup(String group, FormItem... items) {
 
         // create new group
         LinkedHashMap<String, FormItem> groupItems = new LinkedHashMap<String, FormItem>();
         formItems.put(group, groupItems);
 
         for(FormItem item : items)
         {
             String title = item.getTitle();
             if(title.length()>maxTitleLength)
             {
                 maxTitleLength = title.length();
             }
 
             // key maybe be used multiple times
             String itemKey = item.getName();
 
             if(groupItems.containsKey(itemKey)) {
                 groupItems.put(itemKey+"#"+nextId, item);
                 nextId++;
             }
             else
             {
                 groupItems.put(itemKey, item);
             }
         }
     }
 
     public void setFieldsInGroup(String group, FormItem[]... items) {
         setFieldsInGroup(group, flatten(items));
     }
 
     public void setFieldsInGroup(String group, GroupRenderer renderer, FormItem... items) {
         this.renderer.put(group, renderer);
         setFieldsInGroup(group, items);
     }
 
     public void setFieldsInGroup(String group, GroupRenderer renderer, FormItem[]... items) {
         setFieldsInGroup(group, renderer, flatten(items));
     }
 
     private FormItem<?>[] flatten(FormItem<?>[]... items) {
         List<FormItem<?>> l = new ArrayList<FormItem<?>>();
         for (FormItem<?> [] fiArray : items) {
             for (FormItem<?> fi : fiArray) {
                 l.add(fi);
             }
         }
         FormItem<?>[] array = l.toArray(new FormItem<?>[l.size()]);
         return array;
     }
 
     protected void refreshPlainView() {
         for(PlainFormView view : plainViews)
             view.refresh(getEditedEntity()!=null);
     }
 
     public List<String> getFormItemNames() {
         List<String> result = new ArrayList<String>();
 
         for(Map<String, FormItem> groupItems : formItems.values())
         {
             for(FormItem item : groupItems.values())
             {
                 result.add(item.getName());
             }
         }
 
         return result;
     }
 
     public String getFormItemTitle(String ref) {
         String result = null;
         for(Map<String, FormItem> groupItems : formItems.values())
         {
             for(FormItem item : groupItems.values())
             {
                 if(item.getName().equals(ref))
                 {
                     result = item.getTitle();
                     break;
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Enable/disable this form.
      *
      * @param isEnabled
      */
     public void setEnabled(boolean isEnabled) {
 
         this.isEnabled = isEnabled;
 
         if(deck!=null)  // might no be created yet (backwards compatibility)
             toggleViews();
     }
 
     private void toggleViews() {
         int index = isEnabled ? 1 :0;
         deck.showWidget(index);
     }
 
     /**
      * Binds a default single selection model to the table
      * that displays selected rows in a form.
      *
      * @param table
      */
     public void bind(CellTable<T> table) {
         SingleSelectionModel<T> selectionModel = (SingleSelectionModel<T>)table.getSelectionModel();
         if (selectionModel == null) {
             selectionModel = new SingleSelectionModel<T>();
         }
 
         final SingleSelectionModel<T> finalSelectionModel = selectionModel;
 
         selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
             public void onSelectionChange(SelectionChangeEvent event) {
 
                 Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
 
                     public void execute() {
                         T selectedObject = finalSelectionModel.getSelectedObject();
                         if(selectedObject!=null)
                             edit(selectedObject);
                         else
                         {
                             clearValues();
 
                         }
                     }
                 });
             }
         });
 
         table.setSelectionModel(finalSelectionModel);
 
         table.addRowCountChangeHandler(new RowCountChangeEvent.Handler() {
 
             public void onRowCountChange(RowCountChangeEvent event) {
                 if(event.getNewRowCount()==0 && event.isNewRowCountExact())
                     clearValues();
 
             }
         });
 
     }
 
     @Override
     public void setToolsCallback(FormCallback callback) {
         this.toolsCallback = callback;
     }
 }
