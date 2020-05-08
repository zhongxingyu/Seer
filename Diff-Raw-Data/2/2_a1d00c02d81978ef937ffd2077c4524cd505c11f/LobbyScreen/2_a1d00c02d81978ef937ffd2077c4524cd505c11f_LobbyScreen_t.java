 package dungeon.ui.screens;
 
 import dungeon.client.Client;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.Player;
 import dungeon.models.World;
 import dungeon.ui.messages.ChatMessage;
 import dungeon.ui.messages.ShowLobby;
 
 import javax.swing.*;
 import javax.swing.event.MouseInputAdapter;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 public class LobbyScreen extends JPanel implements MessageHandler {
   private final Font chatFont = new Font("Arial", Font.PLAIN, 20);
 
   private final JButton backButton = new JButton("Zurück");
 
   private final JButton readyButton = new JButton("Bereit");
 
   private final JTextArea playerList = new JTextArea();
 
   private final JScrollPane playersScrollPane = new JScrollPane(this.playerList);
 
   private final JTextArea chatMessageList = new JTextArea();
 
   private final JScrollPane chatScrollPane = new JScrollPane(this.chatMessageList);
 
   private final JButton messageButton = new JButton("Nachricht schreiben");
 
   private final List<ChatMessage> chatMessages = new ArrayList<>();
 
   private final Client client;
 
   public LobbyScreen (Client client) {
     super(new GridLayout(5, 1));
 
     this.client = client;
 
     this.add(this.backButton);
     this.add(this.readyButton);
     this.add(this.playersScrollPane);
     this.add(this.chatScrollPane);
     this.add(this.messageButton);
 
     this.backButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         LobbyScreen.this.client.disconnect();
       }
     });
 
     this.readyButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         LobbyScreen.this.client.sendReady();
       }
     });
 
     this.playerList.setEnabled(false);
     this.playerList.setFont(this.chatFont);
 
     this.chatMessageList.setEnabled(false);
     this.chatMessageList.setFont(this.chatFont);
 
     this.messageButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
        String message = JOptionPane.showInputDialog(LobbyScreen.this, "Nachricht");
 
         if (message != null) {
           LobbyScreen.this.client.sendChatMessage(message);
         }
       }
     });
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message instanceof ShowLobby) {
       this.reloadPlayerList();
     } else if (message instanceof World.AddPlayerTransform || message instanceof World.RemovePlayerTransform) {
       this.reloadPlayerList();
     } else if (message instanceof ChatMessage) {
       this.chatMessages.add((ChatMessage)message);
 
       this.reloadChatMessages();
     }
   }
 
   private void reloadChatMessages () {
     StringBuilder text = new StringBuilder();
 
     for (ChatMessage message : this.chatMessages) {
       text.append(String.format("%s: %s\n", message.getAuthor(), message.getText()));
     }
 
     this.chatMessageList.setText(text.toString());
 
     JScrollBar scrollBar = this.chatScrollPane.getVerticalScrollBar();
     scrollBar.setValue(scrollBar.getMaximum());
   }
 
   private void reloadPlayerList () {
     StringBuilder text = new StringBuilder();
 
     for (Player player : this.client.getPlayers()) {
       text.append(String.format("%s\n", player.getName()));
     }
 
     this.playerList.setText(text.toString());
   }
 }
