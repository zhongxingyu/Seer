 package ch9k.chat;
 
 import ch9k.chat.gui.AddContactPanel;
 import ch9k.core.ChatApplication;
 import ch9k.core.I18n;
 import java.awt.Dialog.ModalityType;
 import java.awt.Window;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 
 /**
  * Shows a dialog to enter
  * @author Pieter De Baets
  */
 public class AddContactController {
     private AddContactPanel view;
 
     public AddContactController(Window window) {
         JDialog dialog = new JDialog(window,
                 I18n.get("ch9k.chat", "add_contact"),
                 ModalityType.APPLICATION_MODAL);
         dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         dialog.setLocationRelativeTo(window);
         dialog.setResizable(false);
 
         view = new AddContactPanel(this, dialog);
         dialog.setVisible(true);
     }
 
    public boolean addContact(String username, String inetAddress) {
         InetAddress ip = null;
         boolean hasErrors = true;
 
         // do some validation
         if(username.isEmpty() || inetAddress.isEmpty()) {
             view.setError(I18n.get("ch9k.core", "error_fill_all_fields"));
         } else {
             try {
                 ip = InetAddress.getByName(inetAddress);
                 if(InetAddress.getLocalHost().equals(ip)) {
                     view.setError(I18n.get("ch9k.chat", "error_own_ip"));
                 } else {
                     hasErrors = false;
                 }
             } catch(UnknownHostException ex) {
                 view.setError(I18n.get("ch9k.chat", "error_invalid_ip"));
             }
         }
 
         ContactList list = ChatApplication.getInstance().
                 getAccount().getContactList();
         if(!hasErrors && list.getContact(ip, username) != null) {
             list.addContact(new Contact(username, ip), true);
         } else {
             view.setError(I18n.get("ch9k.chat", "error_contact_already_added"));
         }
 
         return !hasErrors;
    }
 }
