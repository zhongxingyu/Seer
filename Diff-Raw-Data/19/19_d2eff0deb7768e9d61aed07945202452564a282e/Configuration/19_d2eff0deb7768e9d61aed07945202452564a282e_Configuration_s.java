 package bazooka.client;
 
 import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.*;
 
import java.util.*;
 
 public class Configuration extends Composite {
 
   interface Binder extends UiBinder<Widget, Configuration> {}
 
   private static final Binder binder = GWT.create(Binder.class);
 
   @UiField ListBox configList;
   @UiField Button saveButton;
   @UiField Button deleteButton;
   @UiField Button cloneButton;
   @UiField Button addParamButton;
   @UiField Button removeParamButton;
   @UiField VerticalPanel parametersPanel;
 
   Map<String, Map<String, String>> configurations = new HashMap<String, Map<String, String>>();
 
   Configuration() {
     initWidget(binder.createAndBindUi(this));
   }
 
   @Override protected void onLoad() {
     populateConfigList();
     populateDefaultParameters();
   }
 
   @UiHandler("configList")
   void onConfigListChanged(ChangeEvent event) {
     reloadParameters();
     disableSaveButton();
 
     if (isFirstConfigSelected())
       disableDeleteButton();
     else
       enableDeleteButton();
   }
 
   @UiHandler("saveButton")
   void onSaveButtonClicked(ClickEvent event) {
     refreshParameters();
     disableSaveButton();
   }
 
   @UiHandler("deleteButton")
   void onDeleteButtonClicked(ClickEvent event) {
     if (isFirstConfigSelected()) {
       Window.alert("Cannot delete the default configuration.");
     }
     else if (Window.confirm("Are you sure you want to delete '" + getSelectedConfig() + "'?")) {
       removeSelectedConfig();
       selectFirstConfig();
       reloadParameters();
       disableDeleteButton();
     }
   }
 
   @UiHandler("cloneButton")
   void onCloneButtonClicked(ClickEvent event) {
     String newConfigName = askNewConfigName();
     if (newConfigName != null) {
       addConfig(newConfigName);
       selectLastConfig();
       cloneParameters();
       enableDeleteButton();
     }
   }
 
   @UiHandler("addParamButton")
   void onAddParamClicked(ClickEvent event) {
     addParameter("", "");
     addParamButton.setFocus(true);
     enableSaveButton();
   }
 
   @UiHandler("removeParamButton")
   void onRemoveParamClicked(ClickEvent event) {
     removeLastParameter();
     removeParamButton.setFocus(true);
     enableSaveButton();
   }
 
   void populateConfigList() {
     addConfig("Default");
     addConfig("Foo");
     addConfig("Bar");
     addConfig("Baz");
   }
 
   void populateDefaultParameters() {
     for (int i = 1; i <= 10; i++) {
       String key = "FOO" + i, value = "BAR" + i;
       getSelectedParameters().put(key, value);
       addParameter(key, value);
     }
   }
 
   void reloadParameters() {
     clearParameters();
     for (Map.Entry<String, String> param : getSelectedParameters().entrySet())
       addParameter(param.getKey(), param.getValue());
   }
 
   void cloneParameters() {
     for (int i = 0; i < parametersPanel.getWidgetCount(); i++) {
       HorizontalPanel entry = (HorizontalPanel) parametersPanel.getWidget(i);
       String key = getParameterKey(entry);
       String value = getParameterValue(entry);
       getSelectedParameters().put(key, value);
     }
   }
 
   void refreshParameters() {
     getSelectedParameters().clear();
 
     for (int i = parametersPanel.getWidgetCount() - 1; i >= 0 ; i--) {
       HorizontalPanel entry = (HorizontalPanel) parametersPanel.getWidget(i);
       String key = getParameterKey(entry);
 
       if (mustDiscardParameter(key))
         removeParameter(entry);
       else
         getSelectedParameters().put(key, getParameterValue(entry));
     }
   }
 
   void clearParameters() {
     parametersPanel.clear();
   }
 
   void addParameter(String key, String value) {
     parametersPanel.add(buildParameterEntry(key, value));
   }
 
   void removeParameter(HorizontalPanel entry) {
     parametersPanel.remove(entry);
   }
 
   Map<String, String> getSelectedParameters() {
     return configurations.get(getSelectedConfig());
   }
 
   String getParameterKey(HorizontalPanel entry) {
     TextBox key = (TextBox) entry.getWidget(0);
     return key.getText();
   }
 
   String getParameterValue(HorizontalPanel entry) {
     TextBox key = (TextBox) entry.getWidget(2);
     return key.getText();
   }
 
   void removeLastParameter() {
     removeParameter(getLastParameterEntry());
   }
 
   HorizontalPanel getLastParameterEntry() {
     int lastIndex = parametersPanel.getWidgetCount() - 1;
     return (HorizontalPanel) parametersPanel.getWidget(lastIndex);
   }
 
   boolean mustDiscardParameter(String key) {
     return "".equals(key.trim());
   }
 
   void enableSaveButton() {
     saveButton.setEnabled(true);
   }
 
   void disableSaveButton() {
     saveButton.setEnabled(false);
   }
 
   void enableDeleteButton() {
     deleteButton.setEnabled(true);
   }
 
   void disableDeleteButton() {
     deleteButton.setEnabled(false);
   }
 
   void addConfig(String name) {
     configList.addItem(name);
     configurations.put(name, new HashMap<String, String>());
   }
 
   void removeSelectedConfig() {
     configurations.remove(getSelectedConfig());
     configList.removeItem(getSelectedConfigIndex());
   }
 
   boolean containsConfig(String name) {
     return configurations.containsKey(name);
   }
 
   String getSelectedConfig() {
     return configList.getItemText(getSelectedConfigIndex());
   }
 
   int getSelectedConfigIndex() {
     return configList.getSelectedIndex();
   }
 
   void selectFirstConfig() {
     configList.setSelectedIndex(0);
   }
 
   boolean isFirstConfigSelected() {
     return configList.getSelectedIndex() == 0;
   }
 
   void selectLastConfig() {
     int lastIndex = configList.getItemCount() - 1;
     configList.setSelectedIndex(lastIndex);
   }
 
   HorizontalPanel buildParameterEntry(String key, String value) {
     HorizontalPanel entry = new HorizontalPanel();
     entry.setSpacing(5);
     entry.add(buildParameterTextBox(key));
     entry.add(buildEqualsLabel());
     entry.add(buildParameterTextBox(value));
     return entry;
   }
 
   TextBox buildParameterTextBox(String text) {
     TextBox paramBox = new TextBox();
     paramBox.setText(text);
     paramBox.setWidth("125");
     paramBox.addChangeHandler(new ChangeHandler() {
       public void onChange(ChangeEvent event) {
         enableSaveButton();
       }
     });
     return paramBox;
   }
 
   Label buildEqualsLabel() {
     Label label = new Label("=");
     label.setWidth("10");
     label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
     return label;
   }
 
   String askNewConfigName() {
     String newName = Window.prompt("Please specify the configuration name.", "My Config");
     while (containsConfig(newName)) {
       Window.alert("'" + newName + "' already exists, please choose a different name.");
       newName = askNewConfigName();
     }
     return newName;
   }
 }
