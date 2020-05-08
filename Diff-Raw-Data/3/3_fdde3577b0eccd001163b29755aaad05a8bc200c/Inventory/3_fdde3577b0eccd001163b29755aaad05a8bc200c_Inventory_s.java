 package kniemkiewicz.jqblocks.ingame.item;
 
 import kniemkiewicz.jqblocks.ingame.PointOfView;
 import kniemkiewicz.jqblocks.ingame.RenderQueue;
 import kniemkiewicz.jqblocks.ingame.Renderable;
 import kniemkiewicz.jqblocks.ingame.Sizes;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: krzysiek
  * Date: 10.07.12
  */
 @Component
 public class Inventory implements Renderable {
 
   @Autowired
   RenderQueue renderQueue;
 
   @Autowired
   ShinyPickaxeItem shinyPickaxe;
 
   @Autowired
   PickaxeItem pickaxe;
 
   @Autowired
   PointOfView pointOfView;
 
   List<Item> items = new ArrayList<Item>();
   public static int SQUARE_SIZE = 25;
   public static int SQUARE_DIST = 10;
   public static int SQUARE_ROUNDING = 3;
   public static int Y_MARGIN = 5;
   public static int X_MARGIN = 10;
   public static final int SIZE = 10;
   int selectedIndex = 0;
 
   @PostConstruct
   void init() {
     renderQueue.add(this);
     items.add(new DirtBlockItem());
     items.add(shinyPickaxe);
     items.add(new BowItem());
     items.add(pickaxe);
    for (int i = 0; i < SIZE - items.size(); i++) {
       items.add(new EmptyItem());
     }
   }
 
   public void setSelectedIndex(int x) {
     selectedIndex = x;
   }
 
   public Item getSelectedItem() {
     return items.get(selectedIndex);
   }
 
   final static private String[] ids = {"1", "2","3","4","5","6","7","8","9","0"};
   public void render(Graphics g) {
 
     int x = pointOfView.getWindowWidth() - items.size() * SQUARE_SIZE - (items.size() - 1) * SQUARE_DIST - X_MARGIN;
     int i = 0;
     for (Item item : items) {
       if (i == selectedIndex) {
         g.setColor(Color.lightGray);
       } else {
         g.setColor(Color.gray);
       }
       g.fillRoundRect(x, Y_MARGIN, SQUARE_SIZE, SQUARE_SIZE, SQUARE_ROUNDING);
       if (i == selectedIndex) {
         g.setColor(Color.black);
       } else {
         g.setColor(Color.lightGray);
       }
       g.drawRoundRect(x, Y_MARGIN, SQUARE_SIZE, SQUARE_SIZE, SQUARE_ROUNDING);
       item.renderItem(g, x + SQUARE_ROUNDING, Y_MARGIN + SQUARE_ROUNDING, SQUARE_SIZE - 2 * SQUARE_ROUNDING);
       g.setColor(Color.black);
       g.drawString(ids[i], x - 5, Y_MARGIN - 4);
       x += SQUARE_DIST + SQUARE_SIZE;
       i += 1;
     }
   }
 }
 
