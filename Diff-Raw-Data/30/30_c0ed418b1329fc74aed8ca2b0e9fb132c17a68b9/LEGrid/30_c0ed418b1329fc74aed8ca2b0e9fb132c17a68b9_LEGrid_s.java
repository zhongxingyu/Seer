 package vooga.scroller.level_editor;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
import java.awt.Shape;
 import java.util.HashSet;
 import java.util.Set;
 import javax.swing.Scrollable;
 import util.Location;
 import vooga.scroller.scrollingmanager.ScrollingManager;
 import vooga.scroller.util.Editable;
 import vooga.scroller.util.Sprite;
 import vooga.scroller.view.View;
 import vooga.scroller.viewUtil.Renderable;
 
 
 public class LEGrid implements Editable, Renderable, Scrollable {
 
     public static final int DEFAULT_SPRITE_SIZE = 32;
     private int mySpriteSize;
     private SpriteBox[][] myGrid;
     private Dimension mySize;
     private Set<SpriteBox> myPaintableBoxes;
    private ScrollingManager myScrollingManager;
     private StartPoint myStartPoint;
 
     public LEGrid (int numWidthBlocks, int numHeightBlocks) {
         mySpriteSize = DEFAULT_SPRITE_SIZE;
         mySize = new Dimension(numWidthBlocks, numHeightBlocks);
         myGrid = new SpriteBox[numWidthBlocks][numHeightBlocks];
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
         SpriteBox currentBox = getBox(x, y);
         addToBox(spr, currentBox);
     }
 
     public void addSpriteToBox(int xcoor, int ycoor, Sprite sprite){
         addToBox(sprite,getBoxFromCoor(xcoor,ycoor));
     }
 
     /**
      * @param spr
      * @param currentBox
      */
     private void addToBox (Sprite spr, SpriteBox currentBox) {
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
         SpriteBox currentBox = getBox(x, y);
         currentBox.deleteSprite();
         myPaintableBoxes.remove(currentBox);
     }
 
     @Override
     public void changeBackground () {
         //TODO
     }
     
     public Level createLevel (int id, 
                               ScrollingManager sm,
                               View v) {
         //TODO need to refactor. Editable Level.
         Level lev = new Level(id, sm, v);
         for (SpriteBox box : myPaintableBoxes) {
             lev.addSprite(box.getSprite());
         }
         return lev;
     }
     
     public SpriteBox getBoxFromCoor(int xcoor, int ycoor){
         return myGrid[xcoor][ycoor];
     }
     
     public Sprite getSprite(int xcoor, int ycoor){
         return getBoxFromCoor(xcoor,ycoor).getSprite();
     }
     
     public Dimension getSize(){
         return mySize;
     }
 
     private boolean checkAvailable (SpriteBox current, double width, double height) {
         if (!current.isAvailable()) { return false; }
         boolean bool1 = true;
         boolean bool2 = true;
         if (width > mySpriteSize) {
             SpriteBox next = getBox(current.getX() + mySpriteSize, current.getY());
             bool1 = checkAvailable(next, width - mySpriteSize, height);
         }
         if (height > mySpriteSize && bool1) {
             SpriteBox nextBox = getBox(current.getX(), current.getY() + mySpriteSize);
             bool2 = checkAvailable(nextBox, width, height - mySpriteSize);
         }
         return bool1 && bool2;
     }
 
     private void combineBoxes (SpriteBox initial, SpriteBox current, double width, double height) {
         if (width > mySpriteSize) {
             SpriteBox next = getBox(current.getX() + mySpriteSize, current.getY());
             initial.combineWith(next);
             combineBoxes(initial, next, width - mySpriteSize, height);
         }
         if (height > mySpriteSize) {
             SpriteBox next = getBox(current.getX(), current.getY() + mySpriteSize);
             initial.combineWith(next);
             combineBoxes(initial, next, width, height - mySpriteSize);
         }
     }
 
     private SpriteBox getBox (double x, double y) {
         int xCoord = (int) Math.floor(x / mySpriteSize);
         int yCoord = (int) Math.floor(y / mySpriteSize);
         return myGrid[xCoord][yCoord];
     }
 
     private void initializeGrid () {
         for (int x = 0; x < mySize.getWidth(); x++) {
             for (int y = 0; y < mySize.getHeight(); y++) {
                 myGrid[x][y] = new SpriteBox(this, x, y);
             }
         }
     }
 
     @Override
     public Dimension getPreferredScrollableViewportSize () {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public int getScrollableUnitIncrement (Rectangle visibleRect, int orientation, int direction) {
         return DEFAULT_SPRITE_SIZE;
     }
 
     @Override
     public int getScrollableBlockIncrement (Rectangle visibleRect, int orientation, int direction) {
         return DEFAULT_SPRITE_SIZE;
     }
 
     @Override
     public boolean getScrollableTracksViewportWidth () {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean getScrollableTracksViewportHeight () {
         // TODO Auto-generated method stub
         return false;
     }
 
     /**
      * Get the overall pixels size of this LEGrid.
      * @return
      */
     public Dimension getPixelSize () {
         Dimension res= new Dimension(
                                      mySize.width*DEFAULT_SPRITE_SIZE,
                                      mySize.height*DEFAULT_SPRITE_SIZE);
         
         return res;
     }
 
     public boolean isValidForSimulation () {
         // TODO Check for valid starting and exit points.
         return false;
     }
 
     @Override
     public void addStartPoint (int x, int y) {
         if(myStartPoint == null){
             myStartPoint = new StartPoint();
         }
         else{
             deleteSprite((int) myStartPoint.getX(),(int) myStartPoint.getY());
         }
         addSprite(myStartPoint, x, y);
     }
     
     public Location removeStartPoint(){
         Location center = myStartPoint.getCenter();
         deleteSprite((int) center.getX(),(int) center.getY());
         return myStartPoint.getCenter();
     }
 
     
 
 }
