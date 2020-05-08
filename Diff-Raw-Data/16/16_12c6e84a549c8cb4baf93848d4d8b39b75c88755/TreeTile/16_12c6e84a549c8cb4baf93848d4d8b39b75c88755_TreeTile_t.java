 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package caregame.field;
 
 import caregame.ImageCache;
 import caregame.entity.Creature;
 import caregame.entity.Entity;
 import caregame.entity.ItemEntity;
 import caregame.entity.Player;
 import caregame.entity.particle.SmashParticle;
 import caregame.entity.particle.TextParticle;
 import caregame.item.Item;
 import caregame.item.ResourceItem;
 import caregame.item.ToolItem;
 import caregame.item.ToolType;
 import caregame.item.resource.Resource;
 import java.awt.Graphics;
 
 /**
  *
  * @author Markus
  */
 public class TreeTile extends Tile {
 
     private int type;
     
     public TreeTile(int id, int type) {
         super(id);
         this.type = type;
     }
 
     @Override
     public void tick(GameField field, int xt, int yt) {
         int damage = field.getData(xt, yt);
         if (damage > 0) field.setData(xt, yt, damage - 1);
     }
 
     @Override
     public void render(Graphics g, GameField field, int xt, int yt) {
         Tile.grass.render(g, field, xt, yt);
         if (type == 0) {
             ImageCache.get().get("baum.png").render(g, xt*32, yt*32, 32, 32);
         } else if (type == 1){
             boolean u = field.getTile(xt, yt - 1) == this;
             boolean l = field.getTile(xt - 1, yt) == this;
             boolean r = field.getTile(xt + 1, yt) == this;
             boolean d = field.getTile(xt, yt + 1) == this;
             boolean ul = field.getTile(xt - 1, yt - 1) == this;
             boolean ur = field.getTile(xt + 1, yt - 1) == this;
             boolean dl = field.getTile(xt - 1, yt + 1) == this;
             boolean dr = field.getTile(xt + 1, yt + 1) == this;
             
             if (u) {
                 if (d) {
                     ImageCache.get().get("tanne.png").render(g, xt*32, yt*32, xt*32+32, yt*32+32, 0, 32, 64, 64);
                     ImageCache.get().get("tanne.png").render(g, xt*32, yt*32, xt*32+32, yt*32+32, 0, 0, 64, 32);
                 } else {
                     ImageCache.get().get("tanne.png").render(g, xt*32, yt*32, xt*32+32, yt*32+32, 0, 32, 64, 64);
                 }
             } else {
                 if (d) {
                     ImageCache.get().get("tanne.png").render(g, xt*32, yt*32, xt*32+32, yt*32+32, 0, 0, 64, 32);
                 } else {
                     ImageCache.get().get("tanne_faellen2.png").render(g, xt*32, yt*32, 32, 32);
                 }
             }
                     
             
         }
     }
 
     @Override
     public boolean mayPass(GameField field, int x, int y, Entity e) {
         return false;
     }
 
     @Override
     public void hurt(GameField field, int xt, int yt, Creature attacker, int dmg, int dir) {
         hurt(field, xt, yt, dmg);
     }
     
     @Override
     public boolean interact(GameField field, int xt, int yt, Player player, Item item, int attackDir) {
         if (item instanceof ToolItem) {
             ToolItem tool = (ToolItem) item;
             if(tool.type == ToolType.axe) {
                 if (player.payStamina(4 - tool.level)) {
                     hurt(field, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
                 }
             } else {
                 if (player.payStamina(4 - tool.level)) {
                     hurt(field, xt, yt, 1 + (tool.level));
                 }
             }
             tool.durability--;
            return true;
         }
         return false;
     }
     
     
     protected void hurt(GameField field, int xt, int yt, int dmg) {
         int damage = field.getData(xt, yt) + dmg;
         field.add(new SmashParticle(xt*32 + 16, yt*32 + 16));
         field.add(new TextParticle(Integer.toString(dmg), xt*32 + 16, yt*32 + 16));
         if (damage >= 25) {
             int count = random.nextInt(2) + 1;
             for (int i = 0; i < count; i++) {
                 field.add(new ItemEntity(new ResourceItem(Resource.wood), xt * 32 + random.nextInt(10) + 3, yt * 32 + random.nextInt(10) + 3));
             }
             count = random.nextInt(2);
             for (int i = 0; i < count; i++) {
                 if (type == 0)
                     field.add(new ItemEntity(new ResourceItem(Resource.treeSapling), xt * 32 + random.nextInt(10) + 3, yt * 32 + random.nextInt(10) + 3));
                 else if (type == 1)
                     field.add(new ItemEntity(new ResourceItem(Resource.pineSapling), xt * 32 + random.nextInt(10) + 3, yt * 32 + random.nextInt(10) + 3));
             }
             field.setTile(xt, yt, Tile.grass, 0);
         } else {
             field.setData(xt, yt, damage);
         }
     }
     
 }
