 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package render;
 
 import org.lwjgl.util.Point;
 import org.newdawn.slick.Color;
 import render.overlay.OverlaySystem;
 import world.WorldView;
 
 /**
  *
  * @author Administrator
  */
 public class DebugRenderer extends EntityRenderer  {
 
     private Tileset tileset = null;
 
     public DebugRenderer(){
         tileset = new Tileset();
         tileset.texture_name = "tileset1.png";
     }
 
     @Override
     public void render(){
 
         if(ent == null){
             return;
         }
 
         this.tileset.render_sprite(
             ent.origin.getX(),
             ent.origin.getY(),
             8   //hardcoded, lol
         );
        Point ent_screen = WorldView.world2local(ent.origin);
 
        OverlaySystem.ttf.drawString(ent_screen.getX(), ent_screen.getY(), ent.toString(), Color.white);
     }
 }
