 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package caregame.field;
 
 import caregame.Game;
 import caregame.ImageCache;
 import caregame.OPCODES;
 import caregame.entity.Entity;
 import caregame.entity.ItemEntity;
 import caregame.entity.Player;
 import caregame.item.Item;
 import caregame.item.ResourceItem;
 import caregame.item.ToolItem;
 import caregame.item.ToolType;
 import caregame.item.resource.Resource;
 import java.awt.Graphics;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 /**
  *
  * @author Markus
  */
 public class GrassTile extends Tile {
 
     public GrassTile(int id) {
         super(id);
     }
 
     
     
     @Override
     public boolean interact(GameField field, int xt, int yt, Player player, Item item, int attackDir) {
         if (item instanceof ToolItem) {
             ToolItem tool = (ToolItem) item;
             if (tool.type == ToolType.hoe) {
                 field.setTile(xt, yt, Tile.farmland, 0);
                 if (random.nextInt(25) == 0) {    
                     field.add(new ItemEntity(new ResourceItem(Resource.wheatSeeds, 1),xt * 32 + random.nextInt(10) + 3, yt * 32 + random.nextInt(10) + 3));
                 }
                tool.durability--;
                 return true;
             }
             if (tool.type == ToolType.shovel) {
                 field.setTile(xt, yt, Tile.hole, 0);
                tool.durability--;
                return true;
             }
         }
         if (item instanceof ResourceItem) {
             ResourceItem ri = (ResourceItem) item;
             ri.interactOn(this, field, xt, yt, player, attackDir);
         }
         return false;
     }
 
     @Override
     public void render(Graphics g, GameField field, int xt, int yt) {
         ImageCache.get().get("texturen/gras.png").render(g, xt*32, yt*32, 32, 32);
 //        g.drawRect(xt*32,yt*32,32,32);
     }
     
     
 
     @Override
     public void write(DataOutputStream out) throws IOException {
         out.writeInt(OPCODES.OP_TILE_GRASS);
     }
 
     @Override
     public void steppedOn(GameField field, int xt, int yt, Entity e) {
     }
     
     
     
     
     
     
 }
