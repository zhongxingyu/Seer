 package ui.SwingMain;
 
 import Email.MessageController;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.JPopupMenu;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableModel;
 import ui.SwingMeeting.MeetingEditor;
 import ui.SwingMessage.MessageEditor;
 
 /**
  * Email list Display
  */
 public class MessageList extends JTable {
 
     MessageController controller;
     Content content;
     String folderid;
     ToolRibbon toolribbon;
     TableModelInterface model;
    
     /**
      * Constructor of MessageList
      *
      * @param content
      * @param toolribbon
      */
     private static MessageList instance;
     
     public static MessageList getInstance() {
         return instance;
     }
     public MessageList(Content content, ToolRibbon toolribbon) {
         this.controller = MessageController.getInstance();
         this.content = content;
         this.toolribbon = toolribbon;
         this.instance = this;
         this.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 
         ListSelectionModel lsm = this.getSelectionModel();
         lsm.addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent lse) {
                 changemessage();
             }
         });
         this.addMouseListener(new MouseListener() {
             @Override
             public void mouseClicked(MouseEvent me) {
                 if (SwingUtilities.isRightMouseButton(me)) {
                     makeMenu(me.getPoint());
                 }
 
                 if (me.getClickCount() >= 2) {
                     if (folderid.equals(controller.getMeetingFolderId())) {
                         openMeeting(MeetingEditor.Type.VIEW_MEETING);
                     } else {
                         openmessage();
                     }
                 }
             }
 
             @Override
             public void mousePressed(MouseEvent me) {
             }
 
             @Override
             public void mouseReleased(MouseEvent me) {
             }
 
             @Override
             public void mouseEntered(MouseEvent me) {
             }
 
             @Override
             public void mouseExited(MouseEvent me) {
             }
         });
 
         controller.addObserver(new Observer() {
             @Override
             public void update(Observable o, Object o1) {
                 refresh();
                 displayFolder(folderid);
             }
         });
     }
 
     void makeMenu(Point mouseposition) {
 
         // get row
         int row = this.rowAtPoint(mouseposition);
 
         if (row < 0) {
             return;
         }
 
         // get id of row
         String id = (String) this.model.getValueAt(row, -1);
         JPopupMenu menu = new MessageMenu(id, folderid);
         menu.show(this, mouseposition.x, mouseposition.y);
     }
 
     /**
      * Function to display folder given the folder Id
      *
      * @param folderId
      */
     public void displayFolder(String folderId) {
         this.folderid = folderId;
 
         if (folderId.equals(controller.getMeetingFolderId())) {
             model = new MeetingFolderTableModel(folderId);
         } else {
             model = new FolderTableModel(folderId);
         }
         this.setModel(model);
 
         toolribbon.setSelectedFolder(folderId);
     }
 
     /**
      * Set the model
      * @param m
      */
     @Override
     public void setModel(TableModel m) {
         try {
             model = (TableModelInterface) m;
         } catch (ClassCastException e) {
         }
         super.setModel(m);
     }
 
     private void refresh() {
         int row = this.getSelectedRow();
         this.setModel(this.getModel());
         if (row < 0) {
             return;
         }
         this.setRowSelectionInterval(row, row);
     }
 
     private void changemessage() {
         int selected = this.getSelectedRow();
         String messageid = model.getMessageId(selected);
 
         if (messageid == null) {
             content.emptyMessage();
             return;
         }
 
         content.showMessage(messageid);
 
         toolribbon.setSelectedMessage(messageid);
 
         controller.markRead(messageid);
 
     }
 
    public void openmessage() {
         int selected = this.getSelectedRow();
         String messageid = model.getMessageId(selected);
 
         if (folderid.equals(controller.getTemplateFolderId())) {
             messageid = controller.composeFrom(messageid);
         } else if (controller.isThisAMeeting(messageid)) {
             openMeeting(MeetingEditor.Type.RESPOND_MEETING);
             return;
         }
 
         MessageEditor editor = new MessageEditor(messageid, MessageEditor.Type.COMPOSE);
         editor.init();
         editor.setVisible(true);
     }
 
     private void openMeeting(MeetingEditor.Type type) {
         int selected = this.getSelectedRow();
         String messageid = model.getMessageId(selected);
        
         String subject = controller.getEmailHeader(messageid, "Subject");
         
         if (subject.contains("ACCEPTED:") || subject.contains("DECLINED:")) {
             type = MeetingEditor.Type.VIEW_MEETING;
         }
         
         MeetingEditor editor = new MeetingEditor(messageid, type);
         editor.init();
         editor.refresh();
         editor.setVisible(true);
     }
 }
