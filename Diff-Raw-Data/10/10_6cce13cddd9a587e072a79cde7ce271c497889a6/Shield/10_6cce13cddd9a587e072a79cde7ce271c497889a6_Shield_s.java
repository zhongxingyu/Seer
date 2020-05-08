 package items;
 
 import items.projectiles.Projectile;
 import map.Cell;
 import map.MapLoader;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 
 import sounds.SoundGroup;
 import utils.Position;
 import entities.MovingEntity;
 import entities.players.Player;
 import game.MouseCapture;
 import game.config.Config;
 
 public class Shield {
 
     protected Image sprite;
     protected final Rectangle hitbox;
     private String name = "Shield";
     private boolean raised = false;
     
     public Shield(Rectangle hitbox) {
         this.hitbox = hitbox;
         
         try {
             this.sprite = new Image("data/images/shield_item.png");
         } catch (SlickException e) {
             e.printStackTrace();
         }
         
         //TODO Sound effect for hitting shield
     }
     
     public Shield() {
         this(new Rectangle(1,1,1,1));
     }
 
     
 
     public void raise() {
         raised = true;
     }
     
     public boolean raised() {
         return raised;
     }
 
     public void update(GameContainer gc) {
         Input i = gc.getInput();
         Cell cell = MapLoader.getCurrentCell();
         Player p = cell.getPlayer();
         
         if (!i.isKeyDown(Input.KEY_LSHIFT)) {
             raised = false;
         }
         
         if (raised) {
             Position mouse = MouseCapture.getMousePositionRelative();
             Position player = p.getPosition();
             
             double angle = player.distanceTo(mouse).getAngle();
             
            hitbox.setLocation(p.getCentreX() + (float)Math.cos(angle), p.getCentreY() + (float)Math.sin(angle));
         
             for (MovingEntity e : cell.getMovingEntities()) {
                 if (e != p && e.intersects(hitbox)) {
                     if (e instanceof Projectile) {
                         cell.removeMovingEntity(e);
                     }
                 }
             }
         }
     }
     
     public void render(GameContainer gc, Graphics g) {
        sprite.draw((int)((hitbox.getX() - 1.5f)*Config.getTileSize()), (int)((hitbox.getY() - 1.5f)*Config.getTileSize()), new Color(255,255,255));
     }
     
     @Override
     public String toString() {
         return name;
     }
     
 }
