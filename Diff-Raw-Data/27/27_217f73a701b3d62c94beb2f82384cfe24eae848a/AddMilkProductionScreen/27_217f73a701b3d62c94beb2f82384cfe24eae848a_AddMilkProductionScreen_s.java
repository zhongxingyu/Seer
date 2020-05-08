 package org.cgiar.ilri.mistro.farmer.ui;
 
 import com.sun.lwuit.ComboBox;
 import com.sun.lwuit.Command;
 import com.sun.lwuit.Component;
 import com.sun.lwuit.Dialog;
 import com.sun.lwuit.Form;
 import com.sun.lwuit.Label;
 import com.sun.lwuit.TextField;
 import com.sun.lwuit.events.ActionEvent;
 import com.sun.lwuit.events.ActionListener;
 import com.sun.lwuit.layouts.BoxLayout;
 import com.sun.lwuit.spinner.Spinner;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Vector;
 import org.cgiar.ilri.mistro.farmer.Midlet;
 import org.cgiar.ilri.mistro.farmer.carrier.Cow;
 import org.cgiar.ilri.mistro.farmer.carrier.Farmer;
 import org.cgiar.ilri.mistro.farmer.ui.localization.ArrayResources;
 import org.cgiar.ilri.mistro.farmer.ui.localization.Locale;
 import org.cgiar.ilri.mistro.farmer.ui.localization.StringResources;
 import org.cgiar.ilri.mistro.farmer.utils.DataHandler;
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 
 /**
  *
  * @author jason
  */
 public class AddMilkProductionScreen extends Form implements Screen{
     
     private final Midlet midlet;
     private final int locale;
     private final Farmer farmer;
     private Vector validCows;
     
     private BoxLayout parentBoxLayout;
     private Command backCommand;
     private Command addCommand;
     
     private Label cowL;
     private ComboBox cowCB;
     private Label dateL;
     private Spinner dateS;
     private Label timeL;
     private ComboBox timeCB;
     private Label quantityL;
     private TextField quantityTF;
     private Label quantityTypeL;
     private ComboBox quantityTypeCB;
     
     
     public AddMilkProductionScreen(Midlet midlet, int locale, Farmer farmer) {
         super(Locale.getStringInLocale(locale, StringResources.add_production));
         
         this.locale = locale;
         this.midlet = midlet;
         this.farmer = farmer;
         
         this.parentBoxLayout = new BoxLayout(BoxLayout.Y_AXIS);
         this.setLayout(parentBoxLayout);
         
         backCommand = new Command(Locale.getStringInLocale(locale, StringResources.back));
         this.addCommand(backCommand);
         
         addCommand = new Command(Locale.getStringInLocale(locale, StringResources.add));
         this.addCommand(addCommand);
         
         this.addCommandListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent evt) {
                 if(evt.getCommand().equals(backCommand)) {
                     MilkProductionScreen milkProductionScreen = new MilkProductionScreen(AddMilkProductionScreen.this.midlet, AddMilkProductionScreen.this.locale, AddMilkProductionScreen.this.farmer);
                     milkProductionScreen.start();
                 }
                 else if(evt.getCommand().equals(addCommand)){
                     if(validateInput()){
                         JSONObject jsonObject  = new JSONObject();
                         try {
                             jsonObject.put("mobile_no",AddMilkProductionScreen.this.farmer.getMobileNumber());
                             Cow selectedCow = (Cow) validCows.elementAt(cowCB.getSelectedIndex());
                             jsonObject.put("cowName",selectedCow.getName());
                             jsonObject.put("cowEarTagNumber",selectedCow.getEarTagNumber());
 
                             String[] timesInEN = Locale.getStringArrayInLocale(Locale.LOCALE_EN, ArrayResources.milking_times);
                             jsonObject.put("time",timesInEN[timeCB.getSelectedIndex()]);
                             jsonObject.put("quantity",quantityTF.getText());
 
                             String[] quantityTypesInEN = Locale.getStringArrayInLocale(Locale.LOCALE_EN, ArrayResources.quantity_types);
                             jsonObject.put("quantityType",quantityTypesInEN[quantityTypeCB.getSelectedIndex()]);
 
                             Date date = (Date) dateS.getValue();
                             Calendar calendar = Calendar.getInstance();
                             calendar.setTime(date);
                            String dateString = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))+"/"+String.valueOf(calendar.get(Calendar.MONTH))+"/"+String.valueOf(calendar.get(Calendar.YEAR));
                             jsonObject.put("date", dateString);
                             
                             Thread thread = new Thread(new MilkProductionHandler(jsonObject));
                             thread.run();
                         } 
                         catch (JSONException ex) {
                             ex.printStackTrace();
                         }
                     }
                 }
             }
         });
         
         cowL = new Label(Locale.getStringInLocale(locale, StringResources.cow));
         setLabelStyle(cowL);
         this.addComponent(cowL);
         
         String[] cowNames = getValidCows();
         cowCB = new ComboBox(cowNames);
         setComponentStyle(cowCB, true);
         cowCB.setRenderer(new MistroListCellRenderer(cowNames));
         this.addComponent(cowCB);
         
         dateL = new Label(Locale.getStringInLocale(locale, StringResources.date));
         setLabelStyle(dateL);
         this.addComponent(dateL);
         
         dateS = Spinner.createDate(System.currentTimeMillis() - (31536000730l*50), System.currentTimeMillis(), System.currentTimeMillis(), '/', Spinner.DATE_FORMAT_DD_MM_YYYY);
         setComponentStyle(dateS, true);
         dateS.getSelectedStyle().setFgColor(0x2ecc71);
         this.addComponent(dateS);
         
         timeL = new Label(Locale.getStringInLocale(locale, StringResources.time));
         setLabelStyle(timeL);
         this.addComponent(timeL);
         
         timeCB = new ComboBox(Locale.getStringArrayInLocale(locale, ArrayResources.milking_times));
         setComponentStyle(timeCB, true);
         timeCB.setRenderer(new MistroListCellRenderer(Locale.getStringArrayInLocale(locale, ArrayResources.milking_times)));
         this.addComponent(timeCB);
         
         quantityL = new Label(Locale.getStringInLocale(locale, StringResources.quantity));
         setLabelStyle(quantityL);
         this.addComponent(quantityL);
         
         quantityTF = new TextField();
         setComponentStyle(quantityTF, false);
         quantityTF.setConstraint(TextField.NUMERIC);
         quantityTF.setInputModeOrder(new String[] {"123"});
         this.addComponent(quantityTF);
         
         quantityTypeL = new Label(Locale.getStringInLocale(locale, StringResources.quantity_type));
         setLabelStyle(quantityTypeL);
         this.addComponent(quantityTypeL);
         
         quantityTypeCB = new ComboBox(Locale.getStringArrayInLocale(locale, ArrayResources.quantity_types));
         setComponentStyle(quantityTypeCB, true);
         quantityTypeCB.setRenderer(new MistroListCellRenderer(Locale.getStringArrayInLocale(locale, ArrayResources.quantity_types)));
         this.addComponent(quantityTypeCB);
     }
     
     private void setLabelStyle(Label label){
         label.getStyle().setMargin(10, 0, 10, 0);
         label.getSelectedStyle().setMargin(10, 0, 10,0);
     }
     
     private void setComponentStyle(Component component, boolean isFocusable){
         component.getStyle().setMargin(5, 0, 0, 0);
         component.getSelectedStyle().setMargin(5, 0, 0, 0);
         if(isFocusable){
             component.getSelectedStyle().setBgColor(0x2ecc71);
         }
     }
     
     private String[] getValidCows(){
         Cow[] allCows = farmer.getCows();
         validCows = new Vector(allCows.length);
         for(int i = 0; i < allCows.length; i++){
             if(allCows[i].getSex().equals(Cow.SEX_FEMALE)){
                 validCows.addElement(allCows[i]);
             }
         }
         
         String[] cowNames = new String[validCows.size()];
         for(int i =0; i < validCows.size(); i++){
             Cow currentCow = (Cow) validCows.elementAt(i);
             if(currentCow.getName()!= null && currentCow.getName().trim().length() > 0)
                 cowNames[i]=currentCow.getEarTagNumber()+" ("+currentCow.getName()+")";
             else
                 cowNames[i]=currentCow.getEarTagNumber();
         }
         
         return cowNames;
     }
     
     private boolean validateInput(){
         final Dialog infoDialog = new Dialog();
         infoDialog.setDialogType(Dialog.TYPE_INFO);
         final Command backCommand = new Command(Locale.getStringInLocale(locale, StringResources.back));
         infoDialog.addCommand(backCommand);
         infoDialog.addCommandListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent evt) {
                 if(evt.getCommand().equals(backCommand)){
                     infoDialog.dispose();
                 }
             }
         });
         
         Label text = new Label();
         text.getStyle().setAlignment(CENTER);
         infoDialog.addComponent(text);
         
         if(quantityTF.getText()==null || quantityTF.getText().trim().length() == 0){
             text.setText(Locale.getStringInLocale(locale, StringResources.enter_quantity_of_milk_produced));
             quantityTF.requestFocus();
             infoDialog.show(100, 100, 11, 11, true);
             return false;
         }
         
         if(validateDate()!=null){
             text.setText(validateDate());
             dateS.requestFocus();
             infoDialog.show(100, 100, 11, 11, true);
             return false;
         }
         
         String[] quantityTypesInEN = Locale.getStringArrayInLocale(Locale.LOCALE_EN, ArrayResources.quantity_types);
         String quantityType = quantityTypesInEN[quantityTypeCB.getSelectedIndex()];
         if(quantityType.equals("Litres") || quantityType.equals("KGs")){
             if(Integer.parseInt(quantityTF.getText()) > 50){
                 text.setText(Locale.getStringInLocale(locale, StringResources.milk_too_much));
                 quantityTF.requestFocus();
                 infoDialog.show(100, 100, 11, 11, true);
                 return false;
             }
         }
         return true;
     }
     
     private String validateDate(){
         Date dateSelected  = (Date) dateS.getValue();
         long currentTime = System.currentTimeMillis();
         
         long timeDiffDays = (currentTime - dateSelected.getTime())/86400000;
         
         if(timeDiffDays > 30){
             return Locale.getStringInLocale(locale, StringResources.milk_data_too_old);
         }
         else if(timeDiffDays < 0){
             return Locale.getStringInLocale(locale, StringResources.date_in_future);
         }
         
         return null;
     }
 
     public void start() {
         this.show();
     }
 
     public void destroy() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     public void pause() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     private void actOnServerResponse(String response){
         if(response == null){
             final Dialog infoDialog = new Dialog(Locale.getStringInLocale(locale, StringResources.error));
             infoDialog.setDialogType(Dialog.TYPE_ERROR);
             final Command backCommand = new Command(Locale.getStringInLocale(locale, StringResources.back));
             infoDialog.addCommand(backCommand);
             infoDialog.addCommandListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent evt) {
                     if(evt.getCommand().equals(backCommand)){
                         infoDialog.dispose();
                     }
                 }
             });
 
             Label text = new Label();
             text.getStyle().setAlignment(CENTER);
             infoDialog.addComponent(text);
             text.setText(Locale.getStringInLocale(locale, StringResources.problem_connecting_to_server));
             infoDialog.show(100, 100, 11, 11, true);
         }
         else if(response.equals(DataHandler.ACKNOWLEDGE_OK)){
             final Dialog infoDialog = new Dialog(Locale.getStringInLocale(locale, StringResources.success));
             infoDialog.setDialogType(Dialog.TYPE_INFO);
             final Command backCommand = new Command(Locale.getStringInLocale(locale, StringResources.okay));
             infoDialog.addCommand(new Command(""));//placibo command
             infoDialog.addCommand(backCommand);
             infoDialog.addCommandListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent evt) {
                     if(evt.getCommand().equals(backCommand)){
                         infoDialog.dispose();
                         MilkProductionScreen milkProductionScreen = new MilkProductionScreen(midlet, locale, farmer);
                         milkProductionScreen.start();
                     }
                 }
             });
 
             Label text = new Label();
             text.getStyle().setAlignment(CENTER);
             infoDialog.addComponent(text);
             text.setText(Locale.getStringInLocale(locale, StringResources.information_successfully_sent_to_server));
             infoDialog.show(100, 100, 11, 11, true);
         }
         else if(response.equals(DataHandler.DATA_ERROR)){
             final Dialog infoDialog = new Dialog(Locale.getStringInLocale(locale, StringResources.error));
             infoDialog.setDialogType(Dialog.TYPE_ERROR);
             final Command backCommand = new Command(Locale.getStringInLocale(locale, StringResources.back));
             infoDialog.addCommand(backCommand);
             infoDialog.addCommandListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent evt) {
                     if(evt.getCommand().equals(backCommand)){
                         infoDialog.dispose();
                     }
                 }
             });
 
             Label text = new Label();
             text.getStyle().setAlignment(CENTER);
             infoDialog.addComponent(text);
             text.setText(Locale.getStringInLocale(locale, StringResources.problem_in_data_sent_en));
             infoDialog.show(100, 100, 11, 11, true);
         }
     }
     
     private class MilkProductionHandler implements Runnable{
 
         private JSONObject jSONObject;
         
         public MilkProductionHandler(JSONObject jSONObject) {
             this.jSONObject = jSONObject;
         }
         
         
         public void run() {
             String response = DataHandler.sendDataToServer(jSONObject, DataHandler.FARMER_ADD_MILK_PRODUCTION_URL);
             actOnServerResponse(response);
         }
         
     }
 }
