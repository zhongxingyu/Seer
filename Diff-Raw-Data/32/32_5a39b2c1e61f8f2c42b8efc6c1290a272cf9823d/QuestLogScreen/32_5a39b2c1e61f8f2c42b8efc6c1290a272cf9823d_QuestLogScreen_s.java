 package dungeon.ui.screens;
 
 import dungeon.client.Client;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.Quest;
 import dungeon.ui.messages.ShowGame;
 import dungeon.ui.messages.ShowQuestLog;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.MouseInputAdapter;
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
 
 public class QuestLogScreen extends JPanel implements MessageHandler {
   private final Client client;
 
   private final QuestList questList = new QuestList();
 
   private final JTextArea questText = new JTextArea();
 
   private final JButton backButton = new JButton("Zurück");
 
   public QuestLogScreen (Client client) {
    super(new GridLayout(1, 3));
 
     this.client = client;
 
     this.questText.setEnabled(false);
 
     this.add(this.questList);
     this.add(this.questText);
     this.add(this.backButton);
 
     this.backButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         QuestLogScreen.this.client.send(new ShowGame(QuestLogScreen.this.client.getPlayerId()));
       }
     });
 
     this.questList.addListSelectionListener(new ListSelectionListener() {
       @Override
       public void valueChanged (ListSelectionEvent listSelectionEvent) {
         Quest quest = QuestLogScreen.this.questList.getSelectedValue();
 
         QuestLogScreen.this.questText.setText(quest.getText());
       }
     });
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message instanceof ShowQuestLog) {
      this.questList.setItems(this.client.getPlayer().getOpenQuests());
     }
   }
 }
