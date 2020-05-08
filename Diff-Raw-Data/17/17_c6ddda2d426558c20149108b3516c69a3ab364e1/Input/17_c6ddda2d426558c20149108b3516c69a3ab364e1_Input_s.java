 package fls.engine.main.input;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import fls.engine.main.Init;
 
 public class Input implements KeyListener, MouseListener {
 
     public Input(Init game) {
         game.addKeyListener(this);
     }
 
     public class Key {
         private int numTimesPressed = 0;
         private boolean pressed = false;
 
         public int getNumTimesPressed() {
             return numTimesPressed;
         }
 
         public boolean isPressed() {
             return pressed;
         }
 
         public void toggle(boolean isPressed) {
             pressed = isPressed;
             if (isPressed)
                 numTimesPressed++;
         }
     }
     
     public class Mouse{
         private int numTimesClicked =0;
         private boolean clicked = false;
         
         public int getNumTimesPressed(){
             return numTimesClicked;
         }
         
        public boolean isClick(){
             return clicked;
         }
         
         public void toggle(boolean isClicked){
             clicked = isClicked;
             if(isClicked)
                 numTimesClicked++;
         }
     }
 
    public void releaseAll() {
         for (int i = 0; i < keys.size(); i++) {
             keys.get(i).pressed = false;
         }
     }
 
     public List<Key> keys = new ArrayList<Key>();
 
     public Key up = new Key();
     public Key down = new Key();
     public Key left = new Key();
     public Key right = new Key();
     public Key shift = new Key();
     public Key enter = new Key();
     public Key esc = new Key();
     public Key space = new Key();
     
     public List<Mouse> mb = new ArrayList<Mouse>();
     
     public Mouse lb = new Mouse();
     public Mouse rb = new Mouse();
 
     public void keyPressed(KeyEvent e) {
         toggleKey(e.getKeyCode(), true);
     }
 
     public void keyReleased(KeyEvent e) {
         toggleKey(e.getKeyCode(), false);
     }
 
     public void keyTyped(KeyEvent e) {
     }
 
     public void toggleKey(int keyCode, boolean isPressed) {
         if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
             up.toggle(isPressed);
         }
         if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
             down.toggle(isPressed);
         }
         if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
             left.toggle(isPressed);
         }
         if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
             right.toggle(isPressed);
         }
         if (keyCode == KeyEvent.VK_SHIFT) {
             shift.toggle(isPressed);
         }
         if (keyCode == KeyEvent.VK_ENTER) {
             enter.toggle(isPressed);
         }
         if (keyCode == KeyEvent.VK_ESCAPE) {
             esc.toggle(isPressed);
         }
         if (keyCode == KeyEvent.VK_SPACE) {
             space.toggle(isPressed);
         }
     }
 
     public void mouseClicked(MouseEvent arg0) {
 
     }
 
     public void mouseEntered(MouseEvent arg0) {
 
     }
 
     public void mouseExited(MouseEvent arg0) {
 
     }
 
     public void mousePressed(MouseEvent e) {
         toggleMouse(e.getButton(),true);
     }
 
     public void mouseReleased(MouseEvent e) {
         toggleMouse(e.getButton(),false);
     }
     
     public void toggleMouse(int mouseButton , boolean isClicked){
         if(mouseButton == MouseEvent.BUTTON1)
             lb.toggle(isClicked);
        if(mouseButton == MouseEvent.BUTTON2)
             rb.toggle(isClicked);
     }
 }
