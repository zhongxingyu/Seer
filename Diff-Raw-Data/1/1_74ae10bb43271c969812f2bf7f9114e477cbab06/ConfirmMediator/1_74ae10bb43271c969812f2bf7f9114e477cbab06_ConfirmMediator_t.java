 
 package simplemail;
 
 import javax.swing.JDialog;
 
 /**
  *
  * @author Richard Kelly, Sean Kelly, David Kerr
  * 
  * Confirm Mediator implements the mediator pattern for the ConfirmDlg.
  * It implements the buttons and fields, as well as an observer pattern
  * for the table in the MainFrame.
  */
 public class ConfirmMediator {
     
     JDialog gui;
     Observer dependent;
     
     /* register necessary components for logic */
     public void registerGUI(JDialog g)
     {
         gui = g;
     }
 
     public void registerDependent(Observer a)
     {
         dependent = a;
     }
   
     // Properly dispose of gui
     public void closeGUI()
     {
         dependent.poke();
         gui.dispose();
     }
     
     // Delete selected contact
     public void yes(Contact contact){
         DataStore.getInstance().getContacts().remove(contact);
        dependent.poke();
         gui.dispose();
     }
 }
