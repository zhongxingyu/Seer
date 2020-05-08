 package nl.sense_os.commonsense.client.states;
 
 import java.util.Arrays;
 import java.util.List;
 
 import nl.sense_os.commonsense.client.common.CenteredWindow;
 import nl.sense_os.commonsense.client.utility.Log;
 import nl.sense_os.commonsense.client.utility.SensorComparator;
 import nl.sense_os.commonsense.client.utility.SensorIconProvider;
 import nl.sense_os.commonsense.client.utility.SensorKeyProvider;
 import nl.sense_os.commonsense.shared.Constants;
 import nl.sense_os.commonsense.shared.SensorModel;
 import nl.sense_os.commonsense.shared.ServiceModel;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.TreeModel;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.StoreSorter;
 import com.extjs.gxt.ui.client.store.TreeStore;
 import com.extjs.gxt.ui.client.util.IconHelper;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.AdapterField;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
 
 public class StateCreator extends View {
 
     private static final String TAG = "StateCreator";
     private Window window;
     private FormPanel form;
     private TextField<String> nameField;
     private ComboBox<ServiceModel> servicesField;
     private ListStore<ServiceModel> servicesStore;
     private AdapterField sensorsField;
     private TreeStore<TreeModel> sensorsStore;
     private TreePanel<TreeModel> sensorsTree;
     private ListStore<ModelData> dataFieldsStore;
     private Grid<ModelData> dataFieldsGrid;
     private AdapterField dataFieldsField;
 
     private Button createButton;
     private Button cancelButton;
 
     public StateCreator(Controller c) {
         super(c);
     }
 
     @Override
     protected void handleEvent(AppEvent event) {
         EventType type = event.getType();
         if (type.equals(StateEvents.ShowCreator)) {
             // Log.d(TAG, "ShowCreator");
             showWindow();
 
         } else if (type.equals(StateEvents.CreateServiceCancelled)) {
             // Log.d(TAG, "CreateCancelled");
             onCancelled(event);
 
         } else if (type.equals(StateEvents.CreateServiceComplete)) {
             // Log.d(TAG, "CreateComplete");
             onComplete(event);
 
         } else if (type.equals(StateEvents.CreateServiceFailed)) {
             Log.w(TAG, "CreateFailed");
             onFailed(event);
 
         } else if (type.equals(StateEvents.LoadSensorsSuccess)) {
             // Log.d(TAG, "LoadSensorsSuccess");
             final List<TreeModel> sensors = event.<List<TreeModel>> getData("sensors");
             onLoadSensorsComplete(sensors);
 
         } else if (type.equals(StateEvents.LoadSensorsFailure)) {
             Log.w(TAG, "LoadSensorsFailure");
             onLoadSensorsComplete(null);
 
         } else if (type.equals(StateEvents.AvailableServicesUpdated)) {
             // Log.d(TAG, "AvailableServicesUpdated");
             final List<ServiceModel> services = event.<List<ServiceModel>> getData("services");
             onAvailableServicesComplete(services);
 
         } else if (type.equals(StateEvents.AvailableServicesNotUpdated)) {
             Log.w(TAG, "AvailableServicesNotUpdated");
             onAvailableServicesComplete(null);
 
         } else {
             Log.w(TAG, "Unexpected event type: " + type);
         }
     }
 
     private void initButtons() {
         SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 final Button pressed = ce.getButton();
                 if (pressed.equals(createButton)) {
                     if (form.isValid()) {
                         submitForm();
                     }
                 } else if (pressed.equals(cancelButton)) {
                     StateCreator.this.fireEvent(StateEvents.CreateServiceCancelled);
                 } else {
                     Log.w(TAG, "Unexpected button pressed");
                 }
             }
         };
         this.createButton = new Button("Create", IconHelper.create(Constants.ICON_BUTTON_GO), l);
 
         this.cancelButton = new Button("Cancel", l);
 
         final FormButtonBinding binding = new FormButtonBinding(this.form);
         binding.addButton(this.createButton);
 
         this.form.setButtonAlign(HorizontalAlignment.CENTER);
         this.form.addButton(this.createButton);
         this.form.addButton(this.cancelButton);
     }
 
     private void initFields() {
 
         this.nameField = new TextField<String>();
         this.nameField.setFieldLabel("State sensor name");
         this.nameField.setAllowBlank(false);
 
         initSensorsTree();
         ContentPanel sensorsPanel = new ContentPanel(new FitLayout());
         sensorsPanel.setHeaderVisible(false);
         sensorsPanel.setStyleAttribute("backgroundColor", "white");
         sensorsPanel.add(this.sensorsTree);
 
         this.sensorsField = new AdapterField(sensorsPanel);
         this.sensorsField.setHeight(300);
         this.sensorsField.setResizeWidget(true);
         this.sensorsField.setFieldLabel("Input sensor");
 
         this.sensorsTree.getSelectionModel().addSelectionChangedListener(
                 new SelectionChangedListener<TreeModel>() {
 
                     @Override
                     public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                         TreeModel selected = se.getSelectedItem();
                         if (selected instanceof SensorModel) {
                             AppEvent getServices = new AppEvent(
                                     StateEvents.AvailableServicesRequested);
                             getServices.setData("sensor", selected);
                             StateCreator.this.fireEvent(getServices);
                         } else {
                             servicesStore.removeAll();
                         }
                     }
                 });
 
         this.servicesStore = new ListStore<ServiceModel>();
 
         this.servicesField = new ComboBox<ServiceModel>();
         this.servicesField.setFieldLabel("Algorithm type");
         this.servicesField.setEmptyText("Select service algorithm type...");
         this.servicesField.setStore(this.servicesStore);
         this.servicesField.setDisplayField(ServiceModel.NAME);
         this.servicesField.setAllowBlank(false);
         this.servicesField.setTriggerAction(TriggerAction.ALL);
         this.servicesField.setTypeAhead(true);
         this.servicesField.setForceSelection(true);
 
         // update sensors and data fields when a service is selected
         this.servicesField
                 .addSelectionChangedListener(new SelectionChangedListener<ServiceModel>() {
 
                     @Override
                     public void selectionChanged(SelectionChangedEvent<ServiceModel> se) {
                         ServiceModel selected = se.getSelectedItem();
                         TreeModel sensor = sensorsTree.getSelectionModel().getSelectedItem();
                         dataFieldsStore.removeAll();
 
                         if (null != selected && sensor instanceof SensorModel) {
                             String sensorName = sensor.<String> get(SensorModel.NAME);
                             sensorName = sensorName.replace(' ', '_');
 
                             Log.d(TAG, "Selected \'" + selected.get(ServiceModel.NAME) + "\', \'"
                                     + sensorName + "\'");
 
                             List<String> dataFields = selected
                                     .<List<String>> get(ServiceModel.DATA_FIELDS);
                             for (String fieldName : dataFields) {
                                 if (fieldName.contains(sensorName)
                                         && fieldName.length() > sensorName.length()) {
                                     int beginIndex = sensorName.length() + 1;
                                     fieldName = fieldName.substring(beginIndex, fieldName.length());
                                 }
                                 ModelData fieldModel = new BaseModelData();
                                 fieldModel.set("text", fieldName);
                                 dataFieldsStore.add(fieldModel);
                             }
                         }
                     }
                 });
 
         this.dataFieldsStore = new ListStore<ModelData>();
 
         ColumnModel cm = new ColumnModel(Arrays.asList(new ColumnConfig("text", "", this.form
                 .getFieldWidth())));
         dataFieldsGrid = new Grid<ModelData>(this.dataFieldsStore, cm);
         dataFieldsGrid.setHideHeaders(true);
         dataFieldsGrid.setAutoExpandColumn("text");
 
         this.dataFieldsField = new AdapterField(dataFieldsGrid);
         this.dataFieldsField.setFieldLabel("Data fields");
         this.dataFieldsField.setResizeWidget(true);
         this.dataFieldsField.setBorders(true);
 
         final FormData formData = new FormData("-10");
         this.form.add(this.nameField, formData);
         this.form.add(this.sensorsField, formData);
         this.form.add(this.servicesField, formData);
         this.form.add(this.dataFieldsField, formData);
     }
     private void initForm() {
 
         this.form = new FormPanel();
         this.form.setHeaderVisible(false);
         this.form.setBodyBorder(false);
         this.form.setScrollMode(Scroll.AUTOY);
 
         initFields();
         initButtons();
 
         this.window.add(form);
     }
 
     @Override
     protected void initialize() {
         super.initialize();
 
         this.window = new CenteredWindow();
         this.window.setHeading("Create state sensor");
         this.window.setSize(400, 550);
         this.window.setLayout(new FitLayout());
 
         initForm();
     }
 
     private void initSensorsTree() {
 
         // trees store
         this.sensorsStore = new TreeStore<TreeModel>();
         this.sensorsStore.setKeyProvider(new SensorKeyProvider());
 
         // sort tree
         this.sensorsStore.setStoreSorter(new StoreSorter<TreeModel>(new SensorComparator()));
 
         this.sensorsTree = new TreePanel<TreeModel>(sensorsStore);
         this.sensorsTree.setBorders(false);
         this.sensorsTree.setDisplayProperty("text");
         this.sensorsTree.setIconProvider(new SensorIconProvider());
         this.sensorsTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
     }
 
     private void onAvailableServicesComplete(List<ServiceModel> services) {
         this.servicesStore.removeAll();
         this.dataFieldsStore.removeAll();
 
         if (services != null) {
             this.servicesStore.add(services);
         } else {
             this.window.hide();
             MessageBox.alert(null, "Error getting list of available services!", null);
         }
 
     }
 
     private void onCancelled(AppEvent event) {
         this.window.hide();
         setBusy(false);
     }
 
     private void onComplete(AppEvent event) {
         this.window.hide();
         setBusy(false);
     }
 
     private void onFailed(AppEvent event) {
         setBusy(false);
         MessageBox.confirm(null, "Failed to create state sensor, retry?",
                 new Listener<MessageBoxEvent>() {
 
                     @Override
                     public void handleEvent(MessageBoxEvent be) {
                         if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                             submitForm();
                         } else {
                             window.hide();
                         }
                     }
                 });
     }
 
     private void onLoadSensorsComplete(List<TreeModel> sensors) {
         this.sensorsStore.removeAll();
         this.servicesStore.removeAll();
         this.dataFieldsStore.removeAll();
 
         if (sensors != null) {
             this.sensorsStore.add(sensors, true);
         } else {
             this.window.hide();
             MessageBox.alert(null, "Error getting list of source sensors!", null);
         }
     }
 
     private void setBusy(boolean busy) {
         if (busy) {
             this.createButton.setIcon(IconHelper.create(Constants.ICON_LOADING));
             this.cancelButton.disable();
         } else {
             this.createButton.setIcon(IconHelper.create(Constants.ICON_BUTTON_GO));
             this.cancelButton.enable();
         }
     }
 
     private void showWindow() {
         this.form.reset();
         this.window.show();
         this.window.center();
 
         fireEvent(StateEvents.LoadSensors);
     }
 
     private void submitForm() {
         setBusy(true);
 
         AppEvent event = new AppEvent(StateEvents.CreateServiceRequested);
         event.setData("name", this.nameField.getValue());
         event.setData("service", this.servicesField.getValue());
         event.setData("sensor", this.sensorsTree.getSelectionModel().getSelectedItem());
         event.setData("dataFields", this.dataFieldsGrid.getSelectionModel().getSelectedItems());
         fireEvent(event);
     }
 }
