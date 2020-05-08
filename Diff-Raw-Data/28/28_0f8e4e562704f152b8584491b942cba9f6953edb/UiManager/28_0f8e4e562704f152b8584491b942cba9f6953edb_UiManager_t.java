 package dungeon.ui;
 
 import dungeon.client.Client;
 import dungeon.client.ServerDisconnected;
 import dungeon.game.messages.DefeatEvent;
 import dungeon.game.messages.StartGame;
 import dungeon.game.messages.TradeWithMerchant;
 import dungeon.game.messages.WinEvent;
 import dungeon.messages.LifecycleEvent;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.ui.messages.*;
 import dungeon.ui.screens.*;
 
 import javax.swing.*;
 import java.awt.CardLayout;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 /**
  * Follows the flow of the game and activates the appropriate screen.
  *
  * For example a StartGame message is received. Then the UiManager will activate the Canvas. If later in the game a
  * WinEvent is received, the UiManager will switch to the WinScreen.
  */
 public class UiManager extends JPanel implements MessageHandler {
   private static final String START_MENU = "START_MENU";
 
   private static final String CANVAS = "CANVAS";
 
   private static final String WIN_SCREEN = "WIN_SCREEN";
 
   private static final String DEFEAT_SCREEN = "DEFEAT_SCREEN";
 
   private static final String INVENTORY_SCREEN = "INVENTORY_SCREEN";
 
   private static final String SHOP_SCREEN = "SHOP_SCREEN";
 
   private static final String LOBBY_SCREEN = "LOBBY_SCREEN";
 
   private static final String QUEST_LOG_SCREEN = "QUEST_LOG_SCREEN";
 
   private final Client client;
 
   private final Map<String, JPanel> screens = new LinkedHashMap<>();
 
   private final CardLayout layout;
 
   public UiManager (Client client, Canvas canvas, StartMenu startMenu, WinScreen winScreen, DefeatScreen defeatScreen, InventoryScreen inventoryScreen, ShopScreen shopScreen, LobbyScreen lobbyScreen, QuestLogScreen questLogScreen) {
     super(new CardLayout());
 
     this.client = client;
 
     this.layout = (CardLayout)this.getLayout();
 
     this.screens.put(CANVAS, canvas);
     this.screens.put(START_MENU, startMenu);
     this.screens.put(WIN_SCREEN, winScreen);
     this.screens.put(DEFEAT_SCREEN, defeatScreen);
     this.screens.put(INVENTORY_SCREEN, inventoryScreen);
     this.screens.put(SHOP_SCREEN, shopScreen);
     this.screens.put(LOBBY_SCREEN, lobbyScreen);
     this.screens.put(QUEST_LOG_SCREEN, questLogScreen);
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message == LifecycleEvent.INITIALIZE) {
       this.addScreens();
 
       this.showScreen(START_MENU);
     } else if (message == MenuCommand.SHOW_MENU) {
       this.showScreen(START_MENU);
     } else if (message instanceof StartGame || (message instanceof ShowGame && ((ShowGame)message).getPlayerId() == this.client.getPlayerId())) {
       this.showScreen(CANVAS);
     } else if (message instanceof DefeatEvent) {
       this.showScreen(DEFEAT_SCREEN);
     } else if (message instanceof ShowInventory) {
       this.showScreen(INVENTORY_SCREEN);
     } else if (message instanceof TradeWithMerchant && ((TradeWithMerchant)message).getPlayerId() == this.client.getPlayerId()) {
       this.showScreen(SHOP_SCREEN);
     } else if (message instanceof ServerDisconnected) {
       this.showScreen(START_MENU);
     } else if (message instanceof ShowLobby && ((ShowLobby)message).getPlayerId() == this.client.getPlayerId()) {
       this.showScreen(LOBBY_SCREEN);
     } else if (message instanceof ShowQuestLog) {
       this.showScreen(QUEST_LOG_SCREEN);
     }
   }
 
   /**
    * Adds all registered screens to the layout.
    */
   private void addScreens () {
     for (Map.Entry<String, JPanel> entry : this.screens.entrySet()) {
       this.add(entry.getKey(), entry.getValue());
     }
   }
 
   /**
    * Brings up the requested screen and gives them focus.
    */
   private void showScreen (String id) {
     this.layout.show(this, id);
 
     if (this.screens.containsKey(id)) {
       this.screens.get(id).requestFocus();
     }
   }
 }
