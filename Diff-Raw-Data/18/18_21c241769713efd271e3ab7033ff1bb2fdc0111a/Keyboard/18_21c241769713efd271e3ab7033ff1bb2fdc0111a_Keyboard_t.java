 package shapes;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyAdapter;
 import java.util.Iterator;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.ArrayList;
 
 public class Keyboard extends KeyAdapter {
   public static Set<KeyEvent> keysPressed;
   public static KeyEvent mostRecentKeyPressed;
 
   public Keyboard() {
     if (keysPressed == null) {
       keysPressed = new HashSet<KeyEvent>();
     }
   }
 
   public static String getKey() {
     if (mostRecentKeyPressed == null) {
       return null;
     }
 
     return KeyEvent.getKeyText(mostRecentKeyPressed.getKeyCode());
   }
 
  public static Direction getDirection() {
    return getDirection(KeySet.ARROWS);
  }

  // returns null if no arrows are pressed, or if 3+ arrows are pressed, or if
  // opposing keys are pressed
   public static Direction getDirection(KeySet set) {
     ArrayList<Vector> vectorsPressed = new ArrayList<Vector>();
     for (KeyEvent keyPressed : keysPressed) {
       Vector vectorPressed = set.getVector(keyPressed);
       if (vectorPressed != null) {
         vectorsPressed.add(vectorPressed);
       }
     }
 
    if (vectorsPressed.size() == 0) {
       return null;
     }
     Vector ret = vectorsPressed.get(0);
    for (int i = 1; i < vectorsPressed.size(); i++) {
      ret = ret.add(vectorsPressed.get(i));
    }
    if (Math.abs(ret.getMagnitude()) < Geometry.EPSILON) {
      return null;
     }
     return ret.getDirection();
   }
 
   // TODO: ConcurrentModificationException bug
   // This bug occurs when I mash a bunch of keys at once. 
   // Perhaps keyPressed is adding to keysPressed while keyReleased
   // is iterating over keysPressed?
   // This bug is hard to reproduce
 
   @Override
   public void keyPressed(KeyEvent keyPressed) {
     keysPressed.add(keyPressed);
     mostRecentKeyPressed = keyPressed;
   }
 
   @Override
   public void keyReleased(KeyEvent keyReleased) {
     // remove from keysPressed
     Iterator<KeyEvent> iter = keysPressed.iterator();
     while (iter.hasNext()) {
       if (iter.next().getKeyCode() == keyReleased.getKeyCode()) {
         iter.remove();
       }
     }
 
     if (mostRecentKeyPressed != null &&
         mostRecentKeyPressed.getKeyCode() == keyReleased.getKeyCode()) {
       mostRecentKeyPressed = null;
     }
   }
 }
