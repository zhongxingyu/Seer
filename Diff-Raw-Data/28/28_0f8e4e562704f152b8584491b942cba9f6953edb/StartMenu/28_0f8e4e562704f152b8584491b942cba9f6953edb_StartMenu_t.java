 package dungeon.ui.screens;
 
import dungeon.game.messages.WinEvent;
 import dungeon.load.WorldLoader;
 import dungeon.messages.LifecycleEvent;
 import dungeon.client.Client;
import dungeon.messages.Message;
import dungeon.messages.MessageHandler;
 import dungeon.models.World;
 import dungeon.ui.messages.ShowLobby;
 
 import javax.swing.*;
 import javax.swing.event.MouseInputAdapter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * The start menu that shows things like a start and quit button.
  */
public class StartMenu extends JPanel implements MessageHandler {
   private static final Logger LOGGER = Logger.getLogger(StartMenu.class.getName());
 
   private final JTextField nameField = new JTextField("Link");
 
   private final JButton startButton = new JButton("Lokales Spiel starten");
 
   private final JButton loadButton = new JButton("Gespeichertes Spiel laden");
 
   private final JButton startNetworkButton = new JButton("Lobby erstellen");
 
   private final JButton joinNetworkButton = new JButton("Lobby beitreten");
 
   private final JButton quitButton = new JButton("Beenden");
 
   private final Client client;
 
   public StartMenu (Client client) {
     super(new GridLayout(6, 1));
 
     this.client = client;
 
     this.add(this.nameField);
     this.add(this.startButton);
     this.add(this.loadButton);
     this.add(this.startNetworkButton);
     this.add(this.joinNetworkButton);
     this.add(this.quitButton);
 
     this.startButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         StartMenu.this.client.setPlayerName(StartMenu.this.nameField.getText());
 
         try {
           StartMenu.this.client.startGame(6077);
 
           StartMenu.this.client.sendReady();
         } catch (Client.ServerStartException ex) {
           JOptionPane.showMessageDialog(StartMenu.this, "Spiel konnte nicht gestartet werden");
         } catch (Client.ConnectException ex) {
           JOptionPane.showMessageDialog(StartMenu.this, "Spiel konnte nicht gestartet werden");
         }
       }
     });
 
     this.loadButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Dungeon Game Speicherstand", "dungeon"));
 
         int button = fileChooser.showOpenDialog(StartMenu.this);
 
         if (button == JFileChooser.APPROVE_OPTION) {
           File selectedFile = fileChooser.getSelectedFile();
 
           try {
             World world = new WorldLoader().loadFromFile(selectedFile);
 
             StartMenu.this.client.loadWorld(world, 6077);
 
             JOptionPane.showMessageDialog(StartMenu.this, "Los gehts");
           } catch (Exception ex) {
             JOptionPane.showMessageDialog(StartMenu.this, "Die Welt konnte nicht geladen werden. Vielleicht ist die Datei beschädigt.");
             LOGGER.log(Level.INFO, "Error while loading world", ex);
           }
         }
       }
     });
 
     this.startNetworkButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         StartMenu.this.client.setPlayerName(StartMenu.this.nameField.getText());
 
         try {
           StartMenu.this.client.startGame(6077);
 
           StartMenu.this.client.send(new ShowLobby(StartMenu.this.client.getPlayerId()));
         } catch (Client.ServerStartException ex) {
           JOptionPane.showMessageDialog(StartMenu.this, "Lobby konnte nicht erstellt werden");
         } catch (Client.ConnectException ex) {
           JOptionPane.showMessageDialog(StartMenu.this, "Lobby konnte nicht erstellt werden");
         }
       }
     });
 
     this.joinNetworkButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         StartMenu.this.client.setPlayerName(StartMenu.this.nameField.getText());
 
         String server = "localhost";
 
         server = JOptionPane.showInputDialog(StartMenu.this, "Adresse des Servers", server);
 
         if (server == null) {
           return;
         }
 
         try {
           StartMenu.this.client.joinServer(server, 6077);
 
           StartMenu.this.client.send(new ShowLobby(StartMenu.this.client.getPlayerId()));
         } catch (Client.ConnectException ex) {
           JOptionPane.showMessageDialog(StartMenu.this, "Es konnte keine Verbindung hergestellt werden");
         }
       }
     });
 
     this.quitButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         StartMenu.this.client.send(LifecycleEvent.SHUTDOWN);
       }
     });
   }

  @Override
  public void handleMessage (Message message) {
    if (message instanceof WinEvent) {
      JOptionPane.showMessageDialog(this, "Du hast gewonnen");
    }
  }
 }
