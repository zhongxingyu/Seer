 package vooga.scroller.level_editor;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.util.HashSet;
 import java.util.Set;
 import util.Location;
 import vooga.scroller.scrollingmanager.ScrollingManager;
 import vooga.scroller.util.Editable;
 import vooga.scroller.util.Sprite;
 import vooga.scroller.viewUtil.Renderable;
 
 
 public class LEGrid implements Editable, Renderable {
 
     private static final int DEFAULT_SPRITE_SIZE = 32;
     private int mySpriteSize;
     private SpriteBox[][] myGrid;
     private Dimension mySize;
     private Set<SpriteBox> myPaintableBoxes;
     private ScrollingManager myScrollingManager;
 
     public LEGrid (int x, int y) {
         mySpriteSize = DEFAULT_SPRITE_SIZE;
         mySize = new Dimension(x, y);
         myGrid = new SpriteBox[x][y];
         initializeGrid();
         myPaintableBoxes = new HashSet<SpriteBox>();
     }
 
     public int getSpriteSize () {
         return mySpriteSize;
     }
 
     @Override
     public Object getState () {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void paint (Graphics2D pen) {
         for(int i = 0; i < mySize.width; i++){
             for( int j = 0; j < mySize.height; j++){
                 myGrid[i][j].paint(pen);
             }
         }
     }
 
     @Override
     public void addSprite (Sprite spr, int x, int y) {
        SpriteBox currentBox = nearestBox(x, y);
         if (checkAvailable(currentBox, spr.getWidth(), spr.getHeight())) {
             currentBox.addSprite(spr);
             myPaintableBoxes.add(currentBox);
             combineBoxes(currentBox, currentBox, spr.getWidth(), spr.getHeight());
         }
         else {
             // TODO send Unavailable feedback
         }
     }
 
     @Override
     public void deleteSprite (int x, int y) {
        SpriteBox currentBox = nearestBox(x, y);
         currentBox.deleteSprite();
         myPaintableBoxes.remove(currentBox);
     }
 
     @Override
     public void changeBackground () {
         //TODO
     }
 
     public Level createLevel (int id) {
         //TODO need to refactor. Editable Level.
         Level lev = new Level(id);
         for (SpriteBox box : myPaintableBoxes) {
             lev.addSprite(box.getSprite());
         }
         return lev;
     }
 
     private boolean checkAvailable (SpriteBox current, double width, double height) {
         if (!current.isAvailable()) { return false; }
         boolean bool1 = true;
         boolean bool2 = true;
         if (width > mySpriteSize) {
            SpriteBox next = nearestBox(current.getX() + mySpriteSize, current.getY());
             bool1 = checkAvailable(next, width - mySpriteSize, height);
         }
         if (height > mySpriteSize && bool1) {
            SpriteBox nextBox = nearestBox(current.getX(), current.getY() + mySpriteSize);
             bool2 = checkAvailable(nextBox, width, height - mySpriteSize);
         }
         return bool1 && bool2;
     }
 
     private void combineBoxes (SpriteBox initial, SpriteBox current, double width, double height) {
         if (width > mySpriteSize) {
            SpriteBox next = nearestBox(current.getX() + mySpriteSize, current.getY());
             initial.combineWith(next);
             combineBoxes(initial, next, width - mySpriteSize, height);
         }
         if (height > mySpriteSize) {
            SpriteBox next = nearestBox(current.getX(), current.getY() + mySpriteSize);
             initial.combineWith(next);
             combineBoxes(initial, next, width, height - mySpriteSize);
         }
     }
 
    private SpriteBox nearestBox (double x, double y) {
        int xCoord = (int) Math.round(x / mySpriteSize);
        int yCoord = (int) Math.round(y / mySpriteSize);
         return myGrid[xCoord][yCoord];
     }
 
     private void initializeGrid () {
         for (int x = 0; x < mySize.getWidth(); x++) {
             for (int y = 0; y < mySize.getHeight(); y++) {
                 myGrid[x][y] = new SpriteBox(this, x, y);
             }
         }
     }
 
 }
