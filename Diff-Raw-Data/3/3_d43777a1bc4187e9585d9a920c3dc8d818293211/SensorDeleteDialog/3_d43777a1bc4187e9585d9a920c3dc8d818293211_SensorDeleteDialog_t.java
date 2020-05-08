 package nl.sense_os.commonsense.client.sensors.delete;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.common.components.CenteredWindow;
 import nl.sense_os.commonsense.client.common.models.SensorModel;
 import nl.sense_os.commonsense.client.utility.SenseIconProvider;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.Text;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 
 public class SensorDeleteDialog extends View {
 
     private static final Logger logger = Logger.getLogger("SensorDeleteDialog");
     private Window window;
     private Text text;
     private Button removeButton;
     private Button cancelButton;
     private List<SensorModel> sensors;
 
     public SensorDeleteDialog(Controller c) {
         super(c);
     }
 
     private void closeWindow() {
         setBusy(false);
         this.window.hide();
     }
 
     @Override
     protected void handleEvent(AppEvent event) {
         final EventType type = event.getType();
 
         if (type.equals(SensorDeleteEvents.ShowDeleteDialog)) {
             logger.fine("Show");
             final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
             onShow(sensors);
 
         } else if (type.equals(SensorDeleteEvents.DeleteSuccess)) {
             logger.fine("DeleteSuccess");
             onRemoveSuccess();
 
         } else if (type.equals(SensorDeleteEvents.DeleteFailure)) {
             logger.warning("DeleteFailure");
             onRemoveFailure();
 
         } else {
             logger.warning("Unexpected event type");
         }
 
     }
 
     private void initButtons() {
         SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 final Button button = ce.getButton();
                 if (button.equals(removeButton)) {
                     remove();
                 } else if (button.equals(cancelButton)) {
                     closeWindow();
                 }
 
             }
         };
 
         this.removeButton = new Button("Yes", SenseIconProvider.ICON_BUTTON_GO, l);
         this.cancelButton = new Button("No", l);
         this.window.setButtonAlign(HorizontalAlignment.CENTER);
         this.window.addButton(this.removeButton);
         this.window.addButton(this.cancelButton);
     }
 
     @Override
     protected void initialize() {
         super.initialize();
 
         this.window = new CenteredWindow();
         this.window.setHeading("Remove sensors");
         this.window.setLayout(new FitLayout());
         this.window.setSize(323, 200);
 
         this.text = new Text();
         this.text.setStyleAttribute("font-size", "13px");
         this.text.setStyleAttribute("margin", "10px");
         this.window.add(text);
 
         initButtons();
 
         setBusy(false);
     }
 
     private void onRemoveFailure() {
         this.text.setText("Removal failed, retry?");
         setBusy(false);
         this.window.show();
     }
 
     private void onRemoveSuccess() {
         setBusy(false);
         closeWindow();
         MessageBox.info(null, "Removal complete.", null);
     }
 
     private void onShow(final List<SensorModel> sensors) {
 
         this.sensors = sensors;
 
         String message = "Are you sure you want to remove the selected sensor from your list?";
         if (sensors.size() > 1) {
             message = "Are you sure you want to remove all " + sensors.size()
                     + " selected sensors from your list?";
         }
         message += "<br><br>";
         message += "Warning: the removal can not be undone! Any data you stored for this sensor will be lost. Forever.";
 
         this.text.setText(message);
         this.window.show();
         this.window.center();
     }
 
     private void remove() {
         setBusy(true);
         AppEvent delete = new AppEvent(SensorDeleteEvents.DeleteRequest);
         delete.setData("sensors", sensors);
         fireEvent(delete);
     }
 
     private void setBusy(boolean busy) {
         if (busy) {
             this.removeButton.setIcon(SenseIconProvider.ICON_LOADING);
            this.cancelButton.setEnabled(false);
         } else {
             this.removeButton.setIcon(SenseIconProvider.ICON_BUTTON_GO);
            this.cancelButton.setEnabled(true);
         }
     }
 }
