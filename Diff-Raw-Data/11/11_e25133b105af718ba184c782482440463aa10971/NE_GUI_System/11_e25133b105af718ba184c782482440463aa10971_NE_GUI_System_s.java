 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ne.ui;
 
 import events.EGUIDrop;
 import events.Event;
 import game.build.BuildManager;
 import game.ent.buildings.EntBuilding;
 import items.BaseItem;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.lwjgl.util.Point;
 import player.Player;
 import render.WindowRender;
 import world.WorldView;
 
 /**
  *
  * @author Administrator
  */
 public class NE_GUI_System {
     public final NE_GUI_Element root = new NE_GUI_Element(){
 
         {
             w = WindowRender.get_window_w();
             h = WindowRender.get_window_h();
 
             dragable = false;
             solid = false;
         }
 
         @Override
         public void e_on_grab(EGUIDrop event){
             if (event.element instanceof NE_GUI_InventoryItem){
                 try {
                     BaseItem item = ((NE_GUI_InventoryItem) event.element).item;
                     System.out.println("WorldArea: grabed item " + item.get_type());
                     Class building = BuildManager.get_building(item.get_type());
 
                     if (building!= null){
                         EntBuilding ent_building = (EntBuilding) building.newInstance();
 
                         int ex = event.coord.getX();
                         int ey = WindowRender.get_window_h()-event.coord.getY();
 
                         System.out.println("spawning building");
 
                         Point tile_coord = WorldView.getTileCoord(ex,ey);
                         ent_building.spawn(54321, tile_coord);
                         ent_building.set_blocking(true);
 
                        Player.get_ent().container.remove_item(item);
                     }else{
                         System.err.println("item-related entity is null");
                     }
 
                 } catch (InstantiationException ex) {
                     Logger.getLogger(NE_GUI_System.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (IllegalAccessException ex) {
                     Logger.getLogger(NE_GUI_System.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
 
 
     }; //big invisible container
 
     public void render(){
         //glEnable(GL_TEXTURE_2D);
         //glDisable(GL_BLEND);
 
         root.render();
 
         //glEnable(GL_BLEND);
     }
 
     public void clear(){
         root.clear();
     }
 
 
     public NE_GUI_System(){
 
         //init root coord system
         root.x = 0;
         root.y = 0;
 
         //EventManager.subscribe(this);
 
         
     }
 
     public void e_on_event(Event event) {
         //throw new UnsupportedOperationException("Not supported yet.");
         root.notify_event(event);
     }
 
     public void e_on_event_rollback(Event event) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 }
