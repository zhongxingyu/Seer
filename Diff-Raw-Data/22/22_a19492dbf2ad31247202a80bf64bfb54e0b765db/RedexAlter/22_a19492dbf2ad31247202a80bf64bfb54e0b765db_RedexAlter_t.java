 /*
  * Copyright (c) 2012. Redex Scripting - Unauthorized use prohibited by author.
  */
 
 package RedexAlter;
 
import RedexAlter.walk.Walker;
 import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.bot.event.listener.PaintListener;
 
 import java.awt.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: 0xe9
  * Date: 6/23/12
  * Time: 4:45 PM
  */
 public class RedexAlter extends ActiveScript implements PaintListener {
 
     @Override
     protected void setup() {
        provide(new Walker(Constants.YANILLE_BANK, (Inventory.getCount(Constants.BONES) < 1) && Constants.YANILLE_PORTAL.contains(Players.getLocal())));
        provide(new Walker(Constants.YANILLE_PORTAL, (Inventory.getCount(Constants.BONES) > 0) && Constants.YANILLE_BANK.contains(Players.getLocal())));
     }
 
     @Override
     public void onRepaint(Graphics g) {
 
     }
 }
