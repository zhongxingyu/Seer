 package dungeon.ui;
 
 import dungeon.client.Client;
 import dungeon.messages.Mailman;
 import dungeon.ui.screens.*;
 import dungeon.ui.sound.SoundListener;
 
 import javax.sound.sampled.UnsupportedAudioFileException;
 import java.io.IOException;
 
 /**
  * A swing GUI to play the game.
  */
 public class GUI {
   private final Mailman mailman = new Mailman();
 
   private final Client client = new Client(this.mailman);
 
   private final Canvas canvas = new Canvas(this.client);
 
   private final StartMenu startMenu = new StartMenu(this.client);
 
   private final WinScreen winScreen = new WinScreen(this.client);
 
   private final DefeatScreen defeatScreen = new DefeatScreen(this.client);
 
   private final InventoryScreen inventoryScreen = new InventoryScreen(this.client);
 
   private final ShopScreen shopScreen = new ShopScreen(this.client);
 
   private final LobbyScreen lobbyScreen = new LobbyScreen(this.client);
 
   private final QuestLogScreen questLogScreen = new QuestLogScreen(this.client);
 
   private final InputToMessageConverter converter = new InputToMessageConverter(this.client, this.canvas);
 
   private final UiManager uiManager = new UiManager(this.client, this.canvas, this.startMenu, this.winScreen, this.defeatScreen, this.inventoryScreen, this.shopScreen, this.lobbyScreen, this.questLogScreen);
 
   private final MainFrame mainFrame = new MainFrame(this.client, this.uiManager);
 
   private final SoundListener soundListener;
 
   public GUI () throws UnsupportedAudioFileException, IOException {
     this.soundListener = new SoundListener();
 
     this.canvas.addKeyListener(this.converter);
 
     this.mailman.addMailbox(new SwingMailbox(this.client));
     this.mailman.addMailbox(new SwingMailbox(this.mainFrame));
     this.mailman.addMailbox(new SwingMailbox(this.canvas));
     this.mailman.addMailbox(new SwingMailbox(this.uiManager));
     this.mailman.addMailbox(new SwingMailbox(this.inventoryScreen));
     this.mailman.addMailbox(new SwingMailbox(this.shopScreen));
     this.mailman.addMailbox(new SwingMailbox(this.lobbyScreen));
     this.mailman.addHandler(this.soundListener);
   }
 
   public void run () {
     this.mailman.run();
   }
 }
