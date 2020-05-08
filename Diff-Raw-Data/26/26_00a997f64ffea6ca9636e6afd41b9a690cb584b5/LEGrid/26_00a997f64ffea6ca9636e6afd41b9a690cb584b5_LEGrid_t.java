 package vooga.scroller.level_editor.controllerSuite;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.util.HashSet;
 import java.util.Set;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.Scrollable;
 import util.Location;
 import vooga.scroller.level_editor.Level;
 import vooga.scroller.level_editor.StartPoint;
 import vooga.scroller.level_editor.model.SpriteBox;
 import vooga.scroller.level_editor.view.LEGridView;
 import vooga.scroller.level_management.LevelPortal;
 import vooga.scroller.scrollingmanager.OmniScrollingManager;
 import vooga.scroller.scrollingmanager.ScrollingManager;
 import vooga.scroller.util.Editable;
 import vooga.scroller.util.IBackgroundView;
 import vooga.scroller.util.Renderable;
 import vooga.scroller.util.Sprite;
 import vooga.scroller.util.mvc.IView;
 import vooga.scroller.util.mvc.SimpleView;
 import vooga.scroller.view.GameView;
 
 
 public class LEGrid implements Editable, Renderable<LEGridView>, Scrollable {
 
     public static final int DEFAULT_SPRITE_SIZE = 32;
     private static final Location DEFAULT_START_LOC = new Location(0, 0);
     private int mySpriteSize;
     private SpriteBox[][] myGrid;
     private Dimension mySize;
     private Set<SpriteBox> myPaintableBoxes;
     private StartPoint myStartPoint;
     private LevelPortal myDoor; 
     private IBackgroundView myBackground;
 
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
         if (myBackground != null) {
             pen.drawImage(myBackground.getImage(), 0, 0, null);
         }
         for (int i = 0; i < mySize.width; i++) {
             for (int j = 0; j < mySize.height; j++) {
                 myGrid[i][j].paint(pen);
             }
         }
     }
 
     private BufferedImage paintThumbnail (BufferedImage img) {
         Graphics2D drawer = img.createGraphics();
         paint(drawer);
         return img;
     }
 
     @Override
     public void addSprite (Sprite spr, int x, int y) {
         SpriteBox currentBox = getBox(x, y);
         addToBox(spr, currentBox);
     }
 
     public void addSpriteWithCoor (int xcoor, int ycoor, Sprite sprite) {
         addToBox(sprite, getBoxFromCoor(xcoor, ycoor));
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
     }
 
     @Override
     public void deleteSprite (int x, int y) {
         SpriteBox currentBox = getBox(x, y);
         currentBox.deleteSprite();
         myPaintableBoxes.remove(currentBox);
     }
 
     public Set<SpriteBox> getBoxes () {
         return myPaintableBoxes;
     }
 
     /**
      * TODO - Moving this responsibility to Level
      * 
      * @param id
      * @param sm
      * @param v
      * @return
      */
     public Level createLevel (int id,
                               ScrollingManager sm) {
         // TODO need to refactor. Editable Level.
         Level lev = new Level(id, sm);
         for (SpriteBox box : getBoxes()) {
             lev.addSprite(box.getSprite());
         }
         return lev;
     }
 
     public SpriteBox getBoxFromCoor (int xcoor, int ycoor) {
         return myGrid[xcoor][ycoor];
     }
 
     public Sprite getSprite (int xcoor, int ycoor) {
         return getBoxFromCoor(xcoor, ycoor).getSprite();
     }
 
     public Dimension getSize () {
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
         return mySpriteSize;
     }
 
     @Override
     public int getScrollableBlockIncrement (Rectangle visibleRect, int orientation, int direction) {
         return mySpriteSize;
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
      * 
      * @return
      */
     public Dimension getPixelSize () {
         Dimension res = new Dimension(
                                       mySize.width * mySpriteSize,
                                       mySize.height * mySpriteSize);
 
         return res;
     }
 
     public boolean isValidForSimulation () {
         // TODO Check for valid starting and exit points.
         return (myStartPoint != null && myDoor != null);
     }
 
     @Override
     public void addStartPoint (int x, int y) {
         if (myStartPoint == null) {
             myStartPoint = new StartPoint();
         }
         else {
             deleteSprite((int) myStartPoint.getX(), (int) myStartPoint.getY());
         }
         addSprite(myStartPoint, x, y);
     }
 
     public Location removeStartPoint () {
         if (myStartPoint == null) { return DEFAULT_START_LOC; }
         Location center = myStartPoint.getCenter();
         deleteSprite((int) center.getX(), (int) center.getY());
         return center;
     }
 
     @Override
     public void addDoor (Sprite s, int x, int y) {
         if (myDoor == null) {
             myDoor = (LevelPortal) s;
         }
         else {
             deleteSprite((int) myDoor.getX(), (int) myDoor.getY());
         }
         addSprite(myDoor, x, y);
     }
 
     public void addDoorWithCoor (int xcoor, int ycoor, Sprite sprite) {
         myDoor = (LevelPortal) sprite;
         addSpriteWithCoor(xcoor,ycoor,sprite);
     }
 
     @Override
     public LEGridView initializeRenderer (IView parent) {
         return new LEGridView(parent, this);
     }
 
     @Override
     public void changeBackground (IBackgroundView bg) {
         myBackground = bg;
     }
 
     public IBackgroundView getBackground () {
         return myBackground;
     }
 
     private BufferedImage getThumbnail () {
         BufferedImage res = new BufferedImage(getPixelSize().width,
                                               getPixelSize().height,
                                               BufferedImage.TYPE_INT_ARGB);
 
         return paintThumbnail(res);
     }
 
     /**
      * TODO - needs to be completed to provide for fileName etc...
      */
     public void saveThumbnail (String levelFilePath) {
         try {
             ImageIO.write(getThumbnail(), "PNG", new File(levelFilePath + ".png"));
         }
         catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public void simulate () {
         // // TODO Auto-generated method stub
         // ScrollingManager sm = new OmniScrollingManager();
         // GameView display = new GameView(PlatformerConstants.DEFAULT_WINDOW_SIZE, sm);
         IView simContainer = new SimpleView("Level Simulation");
         ScrollingManager sm = new OmniScrollingManager();
         Level sim = new Level(1, sm, this);
         GameView display = sim.initializeRenderer(simContainer);
         // container that will work with user's OS
         JFrame frame = new JFrame("Level Simulation");
         // add our user interface components
         frame.getContentPane().add(display, BorderLayout.CENTER);
         // display them
         frame.pack();
         frame.setVisible(true);
         // start animation
         display.start();
     }
 
 }
