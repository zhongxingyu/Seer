 package nl.sense_os.commonsense.client.states.defaults;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.common.components.CenteredWindow;
 import nl.sense_os.commonsense.client.common.constants.Constants;
 import nl.sense_os.commonsense.client.common.models.DeviceModel;
 import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;
 
 import com.extjs.gxt.ui.client.Registry;
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
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
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.form.AdapterField;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
 import com.extjs.gxt.ui.client.widget.form.LabelField;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 
 public class StateDefaultsDialog extends View {
 
     private static final Logger LOG = Logger.getLogger(StateDefaultsDialog.class.getName());
     private CenteredWindow window;
     private ListStore<DeviceModel> store;
     private Grid<DeviceModel> grid;
     private FormPanel form;
     private Button submitButton;
     private Button cancelButton;
     private ButtonBar buttons;
     private CheckBox overwrite;
 
     public StateDefaultsDialog(Controller c) {
         super(c);
     }
 
     @Override
     protected void handleEvent(AppEvent event) {
         final EventType type = event.getType();
 
         if (type.equals(StateDefaultsEvents.CheckDefaults)) {
             // LOG.fine( "CheckDefaults");
             showDialog();
 
         } else if (type.equals(StateDefaultsEvents.CheckDefaultsSuccess)) {
             // LOG.fine( "CheckDefaultsSuccess");
             onCheckDefaultsSucess();
 
         } else if (type.equals(StateDefaultsEvents.CheckDefaultsFailure)) {
             LOG.warning("CheckDefaultsFailure");
             onCheckDefaultsFailure();
 
         } else {
             LOG.warning("Unexpected event type: " + type);
         }
     }
 
     private void hideDialog() {
         window.hide();
     }
 
     private void initButtons() {
 
         grid.getSelectionModel().addSelectionChangedListener(
                 new SelectionChangedListener<DeviceModel>() {
 
                     @Override
                     public void selectionChanged(SelectionChangedEvent<DeviceModel> se) {
                         // enable the submit button as soon as a device was selected
                         submitButton.setEnabled(se.getSelection().size() > 0);
                     }
                 });
 
         submitButton = new Button("Submit", SenseIconProvider.ICON_BUTTON_GO,
                 new SelectionListener<ButtonEvent>() {
 
                     @Override
                     public void componentSelected(ButtonEvent ce) {
                         submit();
                     }
                 });
         submitButton.setEnabled(false);
         submitButton.setMinWidth(75);
 
         cancelButton = new Button("Cancel", new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 hideDialog();
             }
         });
         cancelButton.setMinWidth(75);
 
         buttons = new ButtonBar();
         buttons.setAlignment(HorizontalAlignment.CENTER);
         buttons.add(submitButton);
         buttons.add(cancelButton);
     }
 
     private void initForm() {
         form = new FormPanel();
         form.setHeaderVisible(false);
         form.setBodyBorder(false);
         form.setScrollMode(Scroll.AUTOY);
         form.setLabelAlign(LabelAlign.TOP);
 
         LabelField label = new LabelField("CommonSense will generate default state sensors, "
                 + "using the sensors in your library.<br><br>");
         label.setHideLabel(true);
 
         AdapterField gridField = new AdapterField(grid);
         gridField.setHeight(100);
         gridField.setResizeWidget(true);
         gridField.setFieldLabel("Select device(s) to use for the states");
 
         overwrite = new CheckBox();
        overwrite
                .setBoxLabel("Update existing state sensors (this will overwrite of their settings)");
         overwrite.setHideLabel(true);
         overwrite.setValue(false);
 
         form.add(label, new FormData("-10"));
         form.add(gridField, new FormData("-10"));
         form.add(overwrite, new FormData("-10"));
     }
 
     private void initGrid() {
 
         store = new ListStore<DeviceModel>();
 
         ColumnConfig id = new ColumnConfig(DeviceModel.ID, "ID", 50);
         ColumnConfig type = new ColumnConfig(DeviceModel.TYPE, "Type", 150);
         ColumnConfig uuid = new ColumnConfig(DeviceModel.UUID, "UUID", 50);
         ColumnModel cm = new ColumnModel(Arrays.asList(id, type, uuid));
 
         grid = new Grid<DeviceModel>(store, cm);
         grid.setAutoExpandColumn(DeviceModel.UUID);
         grid.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
     }
 
     @Override
     protected void initialize() {
         super.initialize();
 
         window = new CenteredWindow();
         window.setHeading("Create default states");
         window.setLayout(new FitLayout());
         window.setSize(400, 300);
 
         initGrid();
         initForm();
         initButtons();
 
         window.setBottomComponent(buttons);
         window.add(form);
     }
 
     private void onCheckDefaultsFailure() {
         setBusy(false);
 
         MessageBox.confirm(null, "Failed to create the default states! Retry?",
                 new Listener<MessageBoxEvent>() {
 
                     @Override
                     public void handleEvent(MessageBoxEvent be) {
                         if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                             submit();
                         } else {
                             hideDialog();
                         }
                     }
                 });
 
     }
 
     private void onCheckDefaultsSucess() {
         setBusy(false);
         hideDialog();
         MessageBox.info(null, "The default states were created successfully.", null);
     }
 
     private void reset() {
 
         List<DeviceModel> devices = Registry.get(Constants.REG_DEVICE_LIST);
         store.removeAll();
         store.add(devices);
 
         setBusy(false);
     }
 
     private void setBusy(boolean busy) {
         if (busy) {
             submitButton.setIcon(SenseIconProvider.ICON_LOADING);
         } else {
             submitButton.setIcon(SenseIconProvider.ICON_BUTTON_GO);
         }
     }
 
     private void showDialog() {
         reset();
         window.show();
         window.center();
     }
 
     private void submit() {
         setBusy(true);
 
         AppEvent checkDefaults = new AppEvent(StateDefaultsEvents.CheckDefaultsRequest);
         checkDefaults.setData("devices", grid.getSelectionModel().getSelection());
         checkDefaults.setData("overwrite", overwrite.getValue());
         fireEvent(checkDefaults);
     }
 }
