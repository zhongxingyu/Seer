 package ch9k.chat.gui;
 
 import ch9k.chat.ChatMessage;
 import ch9k.chat.Conversation;
import java.awt.BorderLayout;
 import java.awt.Component;
 import javax.swing.JLabel;
 import javax.swing.JList;
import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListCellRenderer;
 
 /**
  * Presentation of the Conversation
  * @author Pieter De Baets
  */
 class ConversationListView extends JScrollPane {
     private Conversation conversation;
     private JList messages;
 
     /**
      * Construct a new ConversationListView
      * @param conversation
      */
     public ConversationListView(Conversation conversation) {
         this.conversation = conversation;
 
         messages = new JList(conversation);
         messages.setCellRenderer(new ChatMessageListCellRenderer());
 
         setViewportView(messages);
     }
 
     /**
      * Display a message to notify the user that this
      * conversation has been closed
      */
     public void notifyClosed() {
         // TODO: do something useful
     }
 
     /**
      * Renders a chat message
      */
    public class ChatMessageListCellRenderer extends JPanel implements ListCellRenderer {

        private JLabel author;

        private JLabel text;

        public ChatMessageListCellRenderer() {
            setLayout(new BorderLayout());
            author = new JLabel();
            text = new JLabel();
            add(author, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }

         @Override
         public Component getListCellRendererComponent(JList list, Object value,
                 int index, boolean isSelected, boolean cellHasFocus) {
             ChatMessage message = (ChatMessage) value;
            author.setText(message.getAuthor() + ": ");
            text.setText(message.getText());
             return this;
         }
     }
 }
