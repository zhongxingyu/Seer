 import org.wings.*;
 import org.wings.comet.Comet;
 import org.wings.event.*;
 import org.wings.header.StyleSheetHeader;
 import org.wings.session.Session;
 import org.wings.session.SessionManager;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class Chat implements ActionListener, SExitListener {
 
     private static final ChatRoom chatRoom = new ChatRoom();
 
     private Client client;
     private String nickName;
 
     private boolean login = true;
 
     private STextArea textArea;
     private STextField textField;
     private SLabel label;
     private SButton button_left;
     private SButton button_right;
 
     private final Comet comet;
 
     public Chat() {
         Session session = SessionManager.getSession();
         comet = session.getComet();
         session.addExitListener(this);
         buildGUI();
     }
 
     private void buildGUI() {
         final SFrame frame = new SFrame();
         frame.addHeader(new StyleSheetHeader("../css/myapp.css"));
 
         final SBorderLayout borderLayout = new SBorderLayout();
         final SPanel panel_top = new SPanel(borderLayout);
 
         final SFlowLayout flowLayout = new SFlowLayout(SConstants.LEFT, 15, 15);
         final SPanel panel_bottom = new SPanel(flowLayout);
         panel_bottom.setPreferredSize(new SDimension("800px", "60px"));
 
         final SFont font = new SFont(null, SFont.BOLD, 12);
 
         final SLabel titel = new SLabel("wingS Chat (COMET - Long Polling)");
         titel.setFont(font);
 
         label = new SLabel("Name:");
         label.setFont(font);
 
         button_left = new SButton("Join");
         button_left.setFont(font);
         button_left.addActionListener(this);
 
         button_right = new SButton("Leave");
         button_right.setFont(font);
         button_right.addActionListener(this);
         button_right.setVisible(false);
 
         textArea = new STextArea();
        textArea.setEditable(false);
         textArea.setFont(font);
         textArea.setPreferredSize(new SDimension("800px", "300px"));
 
         textField = new STextField();
         textField.setFont(font);
         textField.setPreferredSize(new SDimension("500px", null));
 
         panel_top.add(titel, SBorderLayout.NORTH);
         panel_top.add(textArea, SBorderLayout.SOUTH);
 
         panel_bottom.add(label);
         panel_bottom.add(textField);
         panel_bottom.add(button_left);
         panel_bottom.add(button_right);
 
         final SContainer container = frame.getContentPane();
         container.add(panel_top, SBorderLayout.NORTH);
         container.add(panel_bottom, SBorderLayout.SOUTH);
 
         ((SForm)container).setDefaultButton(button_left);
 
         frame.setVisible(true);
     }
 
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == button_left) {
             if (login) {
                 if (!textField.getText().equals("")) {
                     comet.connect();
                     client = new Client(SessionManager.getSession(), this);
                     nickName = textField.getText();
                     chatRoom.register(client);
                     chatRoom.addMessage(" ... " + nickName + " has joined.");
                     label.setText(nickName + ":");
                     button_left.setText("Send");
                     button_right.setVisible(true);
                     textField.setText("");
                     login = false;
                 }
             } else {
                 if (!textField.getText().equals("")) {
                     chatRoom.addMessage(" " + nickName + ": " + textField.getText());
                     textField.setText("");
                 }
             }
         } 
         if (e.getSource() == button_right) {
             comet.disconnect();
             chatRoom.addMessage(" ... " + nickName + " has left.");
             chatRoom.unregister(client);
             label.setText("Name:");
             button_left.setText("Join");
             button_right.setVisible(false);
             textField.setText("");
             login = true;
         }
     }
 
     public void appendText(String text) {
         final String newText = text + "\n" + textArea.getText();
         textArea.setText(newText);
     }
 
     public void prepareExit(SExitEvent e) throws ExitVetoException {
         chatRoom.unregister(client);
     }
 }
