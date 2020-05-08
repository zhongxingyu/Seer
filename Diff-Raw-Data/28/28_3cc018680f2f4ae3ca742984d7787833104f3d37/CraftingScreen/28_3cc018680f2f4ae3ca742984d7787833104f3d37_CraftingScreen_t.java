 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package caregame.screen;
 
 import caregame.crafting.Recipe;
 import caregame.entity.Player;
 import caregame.item.Item;
 import caregame.item.ResourceItem;
 import caregame.sound.Sound;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  *
  * @author Markus
  */
 public class CraftingScreen extends Screen {
 
     private Player player;
     private int selection = 0;
     private List<Recipe> recipes;
 
     public CraftingScreen(Player player, List<Recipe> recipes) {
         this.player = player;
         this.recipes = recipes;
         for (int i = 0; i < recipes.size(); i++) {
             this.recipes.get(i).checkCanCraft(player.inventory);
         }
 
         Collections.sort(this.recipes, new Comparator<Recipe>() {
             public int compare(Recipe r1, Recipe r2) {
                 if (r1.canCraft && !r2.canCraft) {
                     return -1;
                 }
                 if (!r1.canCraft && r2.canCraft) {
                     return 1;
                 }
                 return 0;
             }
         });
     }
 
     @Override
     public void render(Graphics g) {
                 
         int ox = game.getWidth() / 2 - 300;
         Recipe r = recipes.get(selection);
         if (r != null) {
             String []costs = new String[r.costs.size()];
             for (int i = 0; i < r.costs.size(); i++) {
                 Item item = r.costs.get(i);
                 int req = 1;
                 if (item instanceof ResourceItem) req = ((ResourceItem) item).count;
                 int has = player.inventory.count(item);
                 int can = has / req;
                 costs[i] = String.format("%03d / %03d", has, can);
             }
                 
             renderFrameText(g, ox+300, 20, 300, ALIGN_CENTER, "REQUIREMENTS");
             renderItems(g, ox+300, 20+getFrameHeight(1), 200, ALIGN_LEFT, r.costs);
             renderFrameText(g, ox+500, 20+getFrameHeight(1), 100, ALIGN_CENTER, costs);
         }
         renderFrameText(g, ox, 20, 300, ALIGN_CENTER, "CRAFTING");
         renderItems(g, ox, 20+getFrameHeight(1), 300, ALIGN_LEFT, selection, recipes);
     }
 
     @Override
     public void tick() {
         if (input.menu.clicked || input.crafting.clicked) {
             game.setScreen(null);
         }
         if (input.up.clicked)selection--;
         if (input.down.clicked)selection++;
         int len = recipes.size();
         if (len == 0) selection = 0;
         if (selection < 0) selection += len;
         if (selection >= len) selection -= len;
         
         if (input.attack.clicked && len > 0) {
             Recipe r = recipes.get(selection);
             r.checkCanCraft(player.inventory);
             if (r.canCraft) {
                 r.deductCosts(player.inventory);
                 r.craft(player.inventory);
                 Sound.craft.play();
             }
             for (int i = 0; i < recipes.size(); i++) {
                 recipes.get(i).checkCanCraft(player.inventory);
             }
            
            Collections.sort(this.recipes, new Comparator<Recipe>() {
                public int compare(Recipe r1, Recipe r2) {
                    if (r1.canCraft && !r2.canCraft) {
                        return -1;
                    }
                    if (!r1.canCraft && r2.canCraft) {
                        return 1;
                    }
                    return 0;
                }
            });
         }
     }
 }
