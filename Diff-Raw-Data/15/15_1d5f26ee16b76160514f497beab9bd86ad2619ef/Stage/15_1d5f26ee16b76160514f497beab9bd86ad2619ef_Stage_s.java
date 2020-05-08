 package com.dcdl.spear;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.dcdl.spear.collision.Arena;
 import com.dcdl.spear.collision.LinearArena;
 
 public class Stage implements Arena {
  private class Block extends Entity {
    public Block(int x, int y, Entity rect) {
      super(Util.scaleRect(new Rectangle(x * 16, (240 - (y + 1) * 16), 16, 16),
          Constants.SCALE));
     }
   }
 
   private final LinearArena arena = new LinearArena();
   private final List<Block> blocks = new ArrayList<Block>();
 
   public Stage() {
     addBlock(1, 0);
     addBlock(3, 0);
     addBlock(5, 0);
     addBlock(5, 1);
     addBlock(6, 0);
     addBlock(8, 0);
     addBlock(8, 1);
     addBlock(14, 0);
     for (int x = 0; x < 5; x++) {
       for (int y = 0; y < 2; y++) {
         addBlock(15 + x, y);
       }
     }
   }
 
   public void render(Graphics g) {
     g.setColor(Color.RED);
     for (Block block : blocks) {
       Rectangle rect = Util.scaleRect(block.getRect(), 1.0 / Constants.SCALE);
       g.fillRect(rect.x, rect.y, rect.width, rect.height);
     }
   }
 
   private void addBlock(int x, int y) {
    Entity rect = new Entity(Util.scaleRect(new Rectangle(x * 16,
        240 - (y + 1) * 16, 16, 16), Constants.SCALE));
    Block block = new Block(x, y, rect);
     blocks.add(block);
    arena.addEntity(rect);
   }
 
   public void collide(Entity entity, Direction collisionDirection) {
     arena.collide(entity, collisionDirection);
   }
 }
