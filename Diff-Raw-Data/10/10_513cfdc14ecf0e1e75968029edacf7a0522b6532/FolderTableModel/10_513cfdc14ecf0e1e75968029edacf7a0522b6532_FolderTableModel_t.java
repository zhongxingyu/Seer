 package ui.SwingMain;
 
 import Email.MessageController;
 import Email.Summary;
 import javax.swing.table.AbstractTableModel;
 
 /**
  *
  * @author chanman
  */
 class FolderTableModel extends AbstractTableModel {
 
     MessageController controller;
     String[] messages;
     boolean isOutbound = false;
 
     public FolderTableModel(MessageController controller, String folderId) {
         this.controller = controller;
         messages = controller.getEmailList(folderId);
 
         if ((folderId.equals(controller.getOutboxFolderId()))
                || (folderId.equals(controller.getSentMessagesFolderId()))
                || (folderId.equals(controller.getDraftsFolderId()))) {
             isOutbound = true;
 
         }
     }
 
     @Override
     public int getRowCount() {
         return messages.length;
     }
 
     @Override
     public int getColumnCount() {
         return 3;
     }
 
     @Override
     public String getColumnName(int col) {
         switch (col) {
             case 0:
                 return "Date";
             case 1:
                 if (isOutbound) {
                     return "To";
                 } else {
                     return "From";
                 }
             case 2:
                 return "Subject";
             default:
                 return new String();
         }
     }
 
     @Override
     public Object getValueAt(int row, int col) {
         Summary summary = controller.getEmailSummary(messages[row]);
 
         switch (col) {
             case -1:
                 return messages[row];
 
             case 0:
                 return summary.getDate();
             case 1:
                 if (isOutbound) {
                     return summary.getTo();
                 } else {
                     return summary.getFrom();
                 }
 
             case 2:
                 return summary.getSubject();
             default:
                 return new String();
         }
 
     }
 
     public String getMessageId(int selected) {
         try {
             return messages[selected];
         } catch (ArrayIndexOutOfBoundsException e) {
             return null;
         }
     }
 }
