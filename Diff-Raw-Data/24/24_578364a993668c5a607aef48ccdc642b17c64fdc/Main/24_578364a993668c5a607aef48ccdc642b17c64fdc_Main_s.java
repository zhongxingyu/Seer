 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package jgaunt;
 
 import java.util.Arrays;
 import javax.swing.JFrame;
 import jgaunt.assets.Depot;
 import jgaunt.assets.resource.Resources;
 import jgaunt.engine.Engine;
 import jgaunt.engine.Module;
 import jgaunt.engine.physics.Physics;
 import jgaunt.engine.renderer.Renderer;
 import jgaunt.engine.renderer.Sprite;
import jgaunt.engine.renderer.View;
 import jgaunt.engine.renderer.swing.Canvas;
 import jgaunt.engine.timing.Ticker;
 import jgaunt.world.Boundary;
 import jgaunt.world.Entity;
import jgaunt.world.Position;
 import jgaunt.world.World;
 import jgaunt.world.behavior.Common.Render;
 
 /**
  *
  * @author woeltjen
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         JFrame frame  = new JFrame("Gaunt's Dungeon");
         Canvas canvas = new Canvas();
 
         frame.setSize(640, 480);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.getContentPane().add(canvas);
         frame.setVisible(true);
 
         World w = new World();
         Module renderer = new Renderer(canvas);
         Module physics  = new Physics();
         Module ticker   = new Ticker(30.0);
         Depot depot = new Resources();
 
         Sprite s = depot.retrieve(Sprite.class, "items", null);
 
 
         
        Render r = new Render() {
            public void invoke(Entity e, View v) {
                for (Sprite s : e.get(Sprite.class))
                    for (Boundary b : e.first(Boundary.class))
                        v.draw(s, b);
            }            
        };
 
         Entity e = new Entity();
         e.add(s);
         e.add(new Boundary( 1, 1));
         e.add(r);
 
         w.addEntity(e);
 
         Engine engine = new Engine(w, Arrays.asList(renderer, physics, ticker));
         engine.run();
     }
 
 }
