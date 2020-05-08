 package ui.SwingMain;
 
 import Email.MessageController;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.regex.Pattern;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import ui.SwingMessage.MessageEditor;
 
 /**
  * MessageMenu class is to create a right click popup menu on the selected message 
  * 
  */
 public class MessageMenu extends JPopupMenu {
 
     final JFrame frame = new JFrame("Error Message");
     String selected;
     String folder;
     String[] neededPath, neededString;
     MessageController controller;
    final MessageList list;
 
     /**
      * Pops up a menu when right clicking on a message
      * @param selectedMessage
      * @param containingFolder
      */
    public MessageMenu(String selectedMessage, String containingFolder, MessageList messageList) {
         selected = selectedMessage;
         folder = containingFolder;
        list = messageList;
        
         // FIXME: we should not manipulate ids
         String pattern = Pattern.quote(System.getProperty("file.separator"));
         neededString = selected.split(pattern);
         controller = MessageController.getInstance();
         JMenuItem openItem = new JMenuItem("Open");
         JMenuItem deleteItem = new JMenuItem("Delete");
         JMenuItem moveItem = new JMenuItem("Move to ...");
         JMenuItem composeFromTemplate = new JMenuItem("Compose From Template");
 
         openItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 open();
             }
         });
         deleteItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 delete();
                list.displayFolder(folder);
             }
         });
         moveItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 move();
                list.displayFolder(folder);
             }
         });
 
         composeFromTemplate.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 composeTemplate();
             }
         });
         this.add(openItem);
         this.add(deleteItem);
         if (neededString[1].matches("Inbox") || neededString[1].matches("Trash")) {
             this.add(moveItem);
         }
 
         if (folder.equals(controller.getTemplateFolderId())) {
             this.add(composeFromTemplate);
         }
     }
 
     void open() {
         MessageEditor editor = new MessageEditor(selected, MessageEditor.Type.COMPOSE);
         editor.init();
         editor.setVisible(true);
     }
 
     void delete() {
         controller.moveMessageToFolder(selected, controller.getTrashFolderId());
     }
 
     void move() {
 
         String path = System.getProperty("user.home") + File.separator + "_mailbox" + File.separator;
         String inboxPath = path + Persist.PersistentStorage.getInstance().getMailboxID()
                 + File.separator + "Inbox";
         
         String destinationPath;
         JFileChooser fc = new JFileChooser(inboxPath);
         fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         fc.setAcceptAllFileFilterUsed(false);
         fc.setDialogTitle("Move selected mail to folder");
         int res = fc.showOpenDialog(null);
 
         if (res == JFileChooser.APPROVE_OPTION) {
             destinationPath = fc.getSelectedFile().getPath();
             destinationPath = destinationPath.substring(path.length());
 
             controller.moveMessageToFolder(selected, destinationPath);
         }
     }
 
     void composeTemplate() {
         MessageEditor editor = new MessageEditor(controller.composeFrom(selected), MessageEditor.Type.COMPOSE);
         editor.init();
         editor.setVisible(true);
     }
 }
