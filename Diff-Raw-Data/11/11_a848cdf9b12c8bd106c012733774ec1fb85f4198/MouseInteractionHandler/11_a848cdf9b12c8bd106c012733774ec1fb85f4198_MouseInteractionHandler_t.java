 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package student.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.security.Key;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JPopupMenu;
 import student.grid.Critter;
 import student.grid.HexGrid.Reference;
 import student.grid.Tile;
 import student.world.World;
 
 /**
  *
  * @author Panda^H^H^H^H^Hhwh48
  */
 public class MouseInteractionHandler extends MouseAdapter implements java.awt.event.KeyListener {
 
     private World model;
     private WorldFrame view;
     private Reference<Tile> rclxtar = null;
     private JPopupMenu men;
     private Action rock, unrock,
             plant, unplant;
     private Action crit, critMenIts[] = new Action[8];
     private boolean EXIT = false;
 
     public MouseInteractionHandler(final World _model, final WorldFrame _view) {
         model = _model;
         view = _view;
         ControlPanelInteractionHandler cpih = new ControlPanelInteractionHandler(model, view);
         view.worldDisplay.gridpane.addMouseListener(this);
         view.worldDisplay.gridpane.addKeyListener(this);
         view.setVisible(true);
         view.setDefaultCloseOperation(WorldFrame.EXIT_ON_CLOSE);
         view.addWindowListener(new ExitHandler());
         
         rock = new LocAxn("put rock") {
             @Override
             public void act() {
                 rclxtar.setContents(new Tile.Rock());
             }
         };
         unrock = new LocAxn("derock") {
             @Override
             public void act() {
                 rclxtar.setContents(new Tile(false, 0));
             }
         };
         plant = new LocAxn("plant") {
             @Override
             protected void act() {
                 rclxtar.contents().putPlant();
             }
         };
         unplant = new LocAxn("weed-x") {
             @Override
             protected void act() {
                 rclxtar.contents().removePlant();
             }
         };
         crit = new LocAxn("add critter") {
             @Override
             protected void act() {
                 rclxtar.contents().putCritter(new Critter(model, rclxtar, 9));
             }
         };
         critMenIts[0] = new LocAxn("forward") {
             @Override
             public void act() {
                 if (rclxtar.contents().critter()) {
                     Critter rclxtarcri = rclxtar.contents().getCritter();
                     rclxtarcri.forward();
                     view.display().setCurrentLocation(rclxtarcri.loc());
                 }
             }
         };
         critMenIts[1] = new LocAxn("backward") {
             @Override
             public void act() {
                 if (rclxtar.contents().critter()) {
                     Critter rclxtarcri = rclxtar.contents().getCritter();
                     rclxtarcri.backward();
                     view.display().setCurrentLocation(rclxtarcri.loc());
                 }
             }
         };
         critMenIts[2] = new LocAxn("left") {
             @Override
             public void act() {
                 if (rclxtar.contents().critter()) {
                     rclxtar.contents().getCritter().left();
                 }
             }
         };
         critMenIts[3] = new LocAxn("right") {
             @Override
             public void act() {
                 if (rclxtar.contents().critter()) {
                     rclxtar.contents().getCritter().right();
                 }
             }
         };
         critMenIts[4] = new LocAxn("eat") {
             @Override
             public void act() {
                 if (rclxtar.contents().critter()) {
                     rclxtar.contents().getCritter().eat();
                 }
             }
         };
         critMenIts[5] = new LocAxn("attack") {
             @Override
             public void act() {
                 if (rclxtar.contents().critter()) {
                     rclxtar.contents().getCritter().attack();
                 }
             }
         };
         critMenIts[6] = new LocAxn("grow") {
             @Override
             public void act() {
                 if (rclxtar.contents().critter()) {
                     rclxtar.contents().getCritter().grow();
                 }
             }
         };
         critMenIts[7] = new LocAxn("remove") {
             @Override
             public void act() {
                 if (rclxtar.contents().critter()) {
                     rclxtar.contents().removeCritter();
                 }
             }
         };
         /*criMenIts[0] = new LocAxn("bud") {
          @Override
          public void act() {
          if(rclxtar.contents().critter())
          rclxtar.contents().getCritter().bud();
          }
          };*/
         /*criMenIts[0] = new LocAxn("mate") {
          @Override
          public void act() {
          if(rclxtar.contents().critter())
          rclxtar.contents().getCritter().mate();
          }
          };*/
         /*criMenIts[0] = new LocAxn("tag") {
          @Override
          public void act() {
          if(rclxtar.contents().critter())
          rclxtar.contents().getCritter().tag();
          }
          };*/
         //this.gameLoop();
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         switch (e.getButton()) {
             case 1:
                 leftClick(e);
                 break;
             case 3:
                 rightClick(e);
                 break;
         }
     }
 
     private void leftClick(MouseEvent e) {
         Reference<Tile> at = lookup(e);
         view.display().setCurrentLocation(at);
         view.repaint();
     }
 
     private void rightClick(MouseEvent e) {
         if (men != null) {
             men.setVisible(false);
         }
         rclxtar = lookup(e);
         view.display().setCurrentLocation(rclxtar);
         if (rclxtar.contents() == null) {
             rclxtar.setContents(new Tile(false, 0));
         }
         men = new JPopupMenu();
         men.addKeyListener(this);
         if (rclxtar.contents().rock()) {
             men.add(unrock);
         } else if (rclxtar.contents().critter()) {
             for (Action a : critMenIts) {
                 men.add(a);
             }
         } else {
             if (rclxtar.contents().plant()) {
                 men.add(unplant);
             } else {
                 men.add(plant);
             }
             men.add(rock);
             men.add(crit);
         }
         men.setLocation(e.getLocationOnScreen());
         men.setVisible(true);
         view.repaint();//display().update();//repaint();
     }
 
     private Reference<Tile> lookup(MouseEvent e) {
         int x = e.getX();
         int y = e.getY();
        x -= view.worldDisplay.scrollpane.getHorizontalScrollBar().getValue();
        y -= view.worldDisplay.scrollpane.getVerticalScrollBar().getValue();
         int ret[] = view.display().grid().hexAt(x, y);
         if(ret == null)
             return null;
         int r = ret[0];
         int c = ret[1];
         return model.at(r, c);
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         System.out.println("Kev");
         if (e.getKeyCode() == KeyEvent.VK_ESCAPE && men != null) {
             System.out.println("ESC");
             men.setVisible(false);
         }
     }
     private long systime;
 //    public void gameLoop() {
 //        while (!EXIT) {
 //            long deltaTime = System.currentTimeMillis()-systime;
 //            if (model.isRunning() || model.shouldStep()) {
 //                model.step();
 //            }
 //        }
 //
 //    }
 
     @Override
     public void keyTyped(KeyEvent e) { keyPressed(e); }
 
     @Override
     public void keyReleased(KeyEvent e) { keyPressed(e); }
     private abstract class LocAxn extends AbstractAction {
 
         @Override
         public final void actionPerformed(ActionEvent e) {
             act();
             men.setVisible(false);
             view.repaint();
         }
 
         protected abstract void act();
 
         public LocAxn(String s) {
             super(s);
         }
     }
 
     private class ExitHandler extends WindowAdapter {
         @Override
         public void windowClosing(WindowEvent e) {
             EXIT = true;
         }
     }
 }
