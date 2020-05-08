 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.io.IOException;
 import java.util.Date;
 import javax.microedition.lcdui.Choice;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.DateField;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.List;
 import javax.microedition.lcdui.TextField;
 import javax.microedition.midlet.*;
 import javax.microedition.rms.InvalidRecordIDException;
 import javax.microedition.rms.RecordEnumeration;
 import javax.microedition.rms.RecordStore;
 import javax.microedition.rms.RecordStoreException;
 import javax.microedition.rms.RecordStoreNotOpenException;
 
 /**
  * @author viswanathgs
  */
 public class DeltaDrain extends MIDlet implements CommandListener {
     String RECORDNAME = "MachiDatabase";
 
     MachiRecord machiRecord = new MachiRecord(RECORDNAME);
 
     Display display = Display.getDisplay(this);
     List machiList = null;
     Form addForm = null;
     Form updateForm = null;
     Form viewForm = null;
     Form aboutForm = null;
 
     Command machiListAddCommand = new Command("Add Bugger", Command.OK, 1);
     Command machiListViewCommand = new Command("View Bugger Info", Command.OK, 1);
     Command machiListDeleteCommand = new Command("Delete Bugger", Command.OK, 2);
     Command machiListResetCommand = new Command("Reset Database", Command.OK, 2);
     Command machiListAboutCommand = new Command("About", Command.OK, 3);
     Command machiListExitCommand = new Command("Exit", Command.OK, 4);
     Command addFormAddCommand = new Command("Add this bugger", Command.OK, 1);
     Command addFormBackCommand = new Command("Back to My Buggers", Command.OK, 2);
     Command updateFormUpdateCommand = new Command("Update Delta", Command.OK, 1);
 
     TextField addFormNameField = null;
     TextField updateFormDeltaField = null;
     TextField updateFormCommentField = null;
 
     String currentMachiName;
 
     void setAndDisplayMachiList() throws RecordStoreNotOpenException, InvalidRecordIDException, IOException, RecordStoreException {
         machiList = new List("My Buggers", Choice.IMPLICIT);
 
         RecordEnumeration recordEnum = machiRecord.recordStore.enumerateRecords(null, machiRecord.comparator, true);
 
         while (recordEnum.hasNextElement()) {
             Machi machi = machiRecord.BytesToMachi(recordEnum.nextRecord());
 
             //Add image part instead of null
             machiList.append(machi.name, null);
         }
         
         // Commands go here
         machiList.addCommand(machiListAddCommand);
         machiList.addCommand(machiListViewCommand);
         machiList.addCommand(machiListDeleteCommand);
 //        machiList.addCommand(machiListResetCommand);
         machiList.addCommand(machiListAboutCommand);
         machiList.addCommand(machiListExitCommand);
 
         machiList.setCommandListener(this);
         display.setCurrent(machiList);
     }
 
     public DeltaDrain() {
         connect();
         try {
             setAndDisplayMachiList();
 
         } catch (RecordStoreNotOpenException ex) {
             ex.printStackTrace();
         } catch (InvalidRecordIDException ex) {
             ex.printStackTrace();
         } catch (IOException ex) {
             ex.printStackTrace();
         } catch (RecordStoreException ex) {
             ex.printStackTrace();
         }
     }
 
     void connect() {
         try {
             machiRecord.recordStore = RecordStore.openRecordStore(RECORDNAME, true);
         }
         catch (Exception e) {
 
       	}
     }
 
     void disconnect()  {
         if (machiRecord.recordStore != null) {
             try {
                 machiRecord.recordStore.closeRecordStore();
             } catch (RecordStoreNotOpenException ex) {
                 ex.printStackTrace();
             } catch (RecordStoreException ex) {
                 ex.printStackTrace();
             }
         }
     }
 
     void switchToAddForm() {
         addForm = new Form("Add New Bugger");
         addFormNameField = new TextField("Bugger's Name:", "", 25, TextField.ANY);
         addForm.append(addFormNameField);
 
         addForm.addCommand(addFormAddCommand);
         addForm.addCommand(addFormBackCommand);
         addForm.addCommand(machiListExitCommand);
 
         addForm.setCommandListener(this);
         display.setCurrent(addForm);
     }
 
     void switchToUpdateForm() throws RecordStoreNotOpenException, InvalidRecordIDException, IOException, RecordStoreException {
         updateForm = new Form("Update " + currentMachiName + "'s Delta");
         updateFormDeltaField = new TextField("Delta value: (-ve if you're lending)", "0", 10, TextField.NUMERIC);
         updateFormCommentField = new TextField("Comment:", "", 30, TextField.ANY);
 
        String outstr = "Current Delta (-ve if you need to get money) = " + machiRecord.getCurrentDelta(currentMachiName) + " bucks\n\n";
         updateForm.append(outstr);
         updateForm.append(updateFormDeltaField);
         updateForm.append(updateFormCommentField);
 
         updateForm.addCommand(updateFormUpdateCommand);
         updateForm.addCommand(addFormBackCommand);
         updateForm.addCommand(machiListExitCommand);
 
         updateForm.setCommandListener(this);
         display.setCurrent(updateForm);
     }
 
     void switchToViewForm() throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException, IOException {
         viewForm = new Form(currentMachiName + "'s Info");
 
         RecordEnumeration recordEnum = machiRecord.recordStore.enumerateRecords(null, null, true);
         while (recordEnum.hasNextElement()) {
             int recordId = recordEnum.nextRecordId();
             byte[] rec = machiRecord.recordStore.getRecord(recordId);
 
             Machi machi = machiRecord.BytesToMachi(rec);
             if (currentMachiName.equals(machi.name)) {
                 machi.calculateType();
 
                 String outstr = "";
                outstr = "Current Delta (-ve if you need to get money) = " + machi.balance + " bucks\n\n";
                 viewForm.append(outstr);
                 outstr = "Category: " + machi.type + "\n\n";
                 viewForm.append(outstr);
                 viewForm.append("History:\n");
 
                 for (int i = machi.history.historySize-1; i >= 0; i--) {
                     Date date = new Date(machi.history.transHistory[i].timestamp);
                     DateField dateField = new DateField("", DateField.DATE_TIME);
                     dateField.setDate(date);
 
                     outstr = (machi.history.historySize - i) + ". ";
                     outstr += machi.history.transHistory[i].amount + " bucks on ";
                     viewForm.append(outstr);
                     
                     viewForm.append(dateField);
 
                     outstr = " (" + machi.history.transHistory[i].comment + ")\n\n";
                     viewForm.append(outstr);
                 }
                 
                 break;
             }
         }
 
         viewForm.addCommand(addFormBackCommand);
         viewForm.addCommand(machiListExitCommand);
 
         viewForm.setCommandListener(this);
         display.setCurrent(viewForm);
     }
 
     void switchToAboutForm() {
         aboutForm = new Form("About DeltaDrain");
         
         String outstr = "A simple Java App to keep track of accounts among friends.\n";
         outstr += "License: GNU GPL v3\n\n";
         outstr += "Developer: Viswanath S (viswanathgs)\n";
         outstr += "Source Code: http://github.com/viswanathgs/DeltaDrain\n";
         outstr += "Feel free to send bug reports to viswanathgs@gmail.com\n\n";
 
         outstr += "Credits:\n";
         outstr += "Anirudh, Vamsi, Shankar - Ideas and Specifications\n";
         outstr += "Jug - Logo\n";
         
         aboutForm.append(outstr);
 
         aboutForm.addCommand(addFormBackCommand);
         aboutForm.addCommand(machiListExitCommand);
 
         aboutForm.setCommandListener(this);
         display.setCurrent(aboutForm);
     }
 
     public void startApp() {
 
     }
 
     public void pauseApp() {
     }
 
     public void destroyApp(boolean unconditional) {
         disconnect();
     }
 
     public void commandAction(Command c, Displayable d) {
         try {
 
             if (c == machiListExitCommand) {
                 destroyApp(false);
                 notifyDestroyed();
                 return;
             }
             else if (c == machiListAddCommand) {
                 switchToAddForm();
             }
             else if (c == machiListDeleteCommand) {
                 String deleteName = machiList.getString(machiList.getSelectedIndex());
 
                 machiRecord.deleteRecord(deleteName);
 
                 setAndDisplayMachiList();
             }
             else if (d == machiList && c == List.SELECT_COMMAND) {
                 currentMachiName = machiList.getString(machiList.getSelectedIndex());
                 switchToUpdateForm();
             }
             else if (c == machiListViewCommand) {
                 currentMachiName = machiList.getString(machiList.getSelectedIndex());
                 switchToViewForm();
             }
             else if (c == machiListResetCommand) {
                 // Not completely functional
                 RecordStore.deleteRecordStore(RECORDNAME);
                 machiRecord.recordStore = RecordStore.openRecordStore(RECORDNAME, true);
                 setAndDisplayMachiList();
             }
             else if (c == machiListAboutCommand) {
                 switchToAboutForm();
             }
             else if (c == addFormAddCommand) {
                 String addName = addFormNameField.getString();
 
                 if (addName.equals("") == false)
                     machiRecord.addRecord(addName, 0);
                 setAndDisplayMachiList();
             }
             else if (c == addFormBackCommand) {
                 setAndDisplayMachiList();
             }
             else if (c == updateFormUpdateCommand) {
                 int delta = Integer.parseInt(updateFormDeltaField.getString());
                 String comment = updateFormCommentField.getString();
 
                 if (delta != 0) {
                     machiRecord.updateRecord(currentMachiName, delta, comment);
                 }
                 setAndDisplayMachiList();
             }
 
         } catch (Exception ex) {
         }
     }
 
     public static void main(String args[]) {
         new DeltaDrain().startApp();
     }
 }
