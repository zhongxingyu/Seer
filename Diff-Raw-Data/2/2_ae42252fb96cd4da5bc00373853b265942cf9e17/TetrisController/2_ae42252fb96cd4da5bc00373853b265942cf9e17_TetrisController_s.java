 package github.iruuunechka.controller;
 
 import github.iruuunechka.model.TetrisModel;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 public class TetrisController implements KeyListener {
     private final TetrisModel model;
 
     public TetrisController(TetrisModel model) {
         this.model = model;
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
 
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         switch (e.getKeyCode()) {
             case KeyEvent.VK_LEFT:
                 model.moveLeft();
                 break;
             case KeyEvent.VK_RIGHT:
                 model.moveRight();
                 break;
             case KeyEvent.VK_DOWN:
                 model.moveDown();
                 break;
             case KeyEvent.VK_UP:
                 model.rotate();
                 break;
             case KeyEvent.VK_SPACE:
                 model.pause();
                 break;
             case KeyEvent.VK_P:
                model.start();
                 break;
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
 
     }
 }
