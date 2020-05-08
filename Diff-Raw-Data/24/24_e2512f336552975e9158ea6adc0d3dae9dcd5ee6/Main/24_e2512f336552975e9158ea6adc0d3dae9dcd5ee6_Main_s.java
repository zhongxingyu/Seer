 package dungeon;
 
 import dungeon.game.LogicHandler;
 import dungeon.load.LevelLoadHandler;
 import dungeon.messages.Mailman;
import dungeon.models.Player;
 import dungeon.pulse.PulseGenerator;
 import dungeon.ui.*;
 import dungeon.ui.screens.*;
 
 import java.util.concurrent.atomic.AtomicReference;
 
 public class Main {
   public static void main (String[] args) {
     AtomicReference<Integer> localPlayerId = new AtomicReference<>();
 
     Mailman mailman = new Mailman();
 
     InputToMessageConverter converter = new InputToMessageConverter(mailman, localPlayerId);
 
     Canvas canvas = new Canvas(localPlayerId);
     canvas.addKeyListener(converter);
 
     StartMenu startMenu = new StartMenu(mailman, localPlayerId);
 
     WinScreen winScreen = new WinScreen(mailman);
 
     DefeatScreen defeatScreen = new DefeatScreen(mailman);
 
     InventoryScreen inventoryScreen = new InventoryScreen(mailman, localPlayerId);
 
     ShopScreen shopScreen = new ShopScreen(mailman, localPlayerId);
 
     UiManager uiManager = new UiManager(canvas, startMenu, winScreen, defeatScreen, inventoryScreen, shopScreen);
 
     MainFrame mainFrame = new MainFrame(mailman, uiManager);
 
     mailman.addMailbox(new SwingMailbox(mainFrame));
     mailman.addMailbox(new SwingMailbox(canvas));
     mailman.addMailbox(new SwingMailbox(uiManager));
     mailman.addMailbox(new SwingMailbox(inventoryScreen));
     mailman.addMailbox(new SwingMailbox(shopScreen));
 
     mailman.addMailbox(new PulseGenerator(mailman));
 
     mailman.addHandler(new LevelLoadHandler(mailman));
     mailman.addHandler(new LogicHandler(mailman));
 
     mailman.run();
   }
 }
