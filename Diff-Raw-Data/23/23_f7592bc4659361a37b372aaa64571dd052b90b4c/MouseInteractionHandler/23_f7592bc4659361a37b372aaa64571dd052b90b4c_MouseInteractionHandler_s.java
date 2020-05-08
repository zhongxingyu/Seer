 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package student.gui;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import student.world.World;
 
 /**
  *
  * @author Panda^H^H^H^H^Hhwh48
  */
 public class MouseInteractionHandler extends MouseAdapter {
     private World model;
     private WorldFrame view;
     
     public MouseInteractionHandler(final World _model, final WorldFrame _view) {
         model = _model;
         view = _view;
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         switch(e.getButton()) {
             case 1:
                 leftClick(e);
                 break;
             case 2:
                 rightClick(e);
                 break;
         }
     }
 
     private void leftClick(MouseEvent e) {
         
     }
 
     private void rightClick(MouseEvent e) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
     
     
 }
