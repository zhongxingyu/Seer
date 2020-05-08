 
 package vooga.scroller.level_editor;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.ImageIcon;
 import util.Location;
 import vooga.scroller.util.Editable;
 import vooga.scroller.util.Sprite;
 import vooga.scroller.viewUtil.Renderable;
 import vooga.scroller.collision_manager.CollisionManager;
 import vooga.scroller.model.IInput;
 import vooga.scroller.model.PlayerInputs;
 import vooga.scroller.scrollingmanager.ScrollingManager;
 import vooga.scroller.sprites.superclasses.NonStaticEntity;
 import vooga.scroller.sprites.superclasses.Player;
 import vooga.scroller.util.PlatformerConstants;
 import vooga.scroller.view.View;
 
 public class Level implements Editable, Renderable {
 
     private Dimension mySize;
     private Dimension frameOfReferenceSize;
     private Dimension frameOfActionSize;
     private Player myPlayer;
     private List<Sprite> mySprites;
     private List<Sprite> myFrameOfActionSprites;
     private List<Sprite> myFrameOfReferenceSprites;
     private View myView;
     private ScrollingManager myScrollManager;
     private Image myBackground;
     
     private IInput myInput;
     
     //not longer temp. This is useful.
     // TODO: force dev to specify background.
     // This background is all clear
     private Image DEFAULT_BACKGROUND = new ImageIcon(getClass().getResource("/vooga/scroller/images/default_background.png")).getImage();
 
     private int myID;
     
     public int getID () {
         return myID;
     }
     
     private Level() {
         mySize = PlatformerConstants.DEFAULT_LEVEL_SIZE;
         myBackground = DEFAULT_BACKGROUND;
         frameOfReferenceSize = PlatformerConstants.REFERENCE_FRAME_SIZE;
         myBackground = DEFAULT_BACKGROUND;
         mySprites = new ArrayList<Sprite>();
         initFrames();
     }
 
     public Level(int id, ScrollingManager sm){
         this(); //TODO Incomplete. figure out SM constraints...
     }
     
     public Level(int id){
         this();
         myID = id;
     }
 
     public Level(int id, ScrollingManager sm, View view){
         //MIGHT WANT TO INITIALIZE THIS WITH A PLAYER AS WELL
         this();
         myView = view;
         frameOfActionSize = calcActionFrameSize(myView.getSize());
         myScrollManager = sm;
         myID = id;
     }
 
     private void initFrames() {
         myFrameOfActionSprites = new ArrayList<Sprite>();
         myFrameOfReferenceSprites = new ArrayList<Sprite>();      
     }
 
     
 
     
     public void setSize(Dimension size) {
         mySize = size;
     }
     /**
      * Adds a sprite to the level.
      * @param s the Sprite to be added
      */
 
     public void addSprite(Sprite s){
             mySprites.add(s);
     }
     
     public void removeSprite(Sprite s) {
         mySprites.remove(s);
     }
     
     public void addPlayer(Player s) {
         myPlayer = s;
         for (Sprite sprite : mySprites) {
             if (sprite instanceof NonStaticEntity) {
                 addPlayerToSprite((NonStaticEntity) sprite);
             }            
         }
     }
     
     
     public void addPlayerToSprite(NonStaticEntity sprite) {
         sprite.addPlayer(myPlayer);
     }
     
     public void setBackground(Image i) {
         myBackground = i;
     }
     
     public Image getBackground() {
         return myBackground;
     }
 
     //Methods from Renderable Interface. To be called by View components.  
 
     @Override
     public Object getState () {
         //TODO auto-generated.
         return null;
     }
 
     public void update(double elapsedTime, Dimension bounds, View view) {
         if(myPlayer != null) {
 //            System.out.println(myPlayer.getPaintLocation());
             updateFrames(view);
             myPlayer.update(elapsedTime, bounds);
 
             
             for(Sprite s: myFrameOfActionSprites) {
                 s.update(elapsedTime, bounds);
                 if (s.getHealth() <= 0) {
                     this.removeSprite(s);
                 }
             }
             intersectingSprites();
         }
     }
 
     @Override
     public void paint (Graphics2D pen) {
         if(myPlayer != null) {
             for(Sprite s: this.mySprites) {
                 s.paint(pen,myPlayer.getCenter(), myPlayer.getPaintLocation());
             }
             myPlayer.paint(pen);
         }
     }
 
     private void updateFrames(View view) {
         myFrameOfActionSprites.clear();
         myFrameOfReferenceSprites.clear();
         frameOfReferenceSize = view.getSize();
         frameOfActionSize = calcActionFrameSize(view.getSize());
         if(mySprites.size() > 0) {
             for(Sprite s: mySprites) {
                 if(checkRange(s, frameOfReferenceSize)) {
                     myFrameOfReferenceSprites.add(s);
                     myFrameOfActionSprites.add(s);
                 }
                 if(!myFrameOfActionSprites.contains(s) & checkRange(s, frameOfActionSize)) {
                     myFrameOfActionSprites.add(s);
                 }
             }
         }
 
     }
 
     private boolean checkRange(Sprite sprite, Dimension frame) {
         //This is pretty hacky, I am trying to think of a more elegant way
         if(myPlayer == null ||
                 getLeftBoundary(frame) > sprite.getX()
                 || getRightBoundary(frame) < sprite.getX()
                 || getLowerBoundary(frame) < sprite.getY()
                 || getUpperBoundary(frame) > sprite.getY()) {
             return false;
         }
         return true;
     }
 
     private Dimension calcActionFrameSize(Dimension size) {
         Dimension temp = new Dimension((int) size.getWidth() + 200, (int) size.getHeight() + 200);
         return temp;
     }
     
     public double getRightBoundary(Dimension frame) {
         return myScrollManager.getRightBoundary(frame, myPlayer.getCenter());
     }
     
     public double getLeftBoundary(Dimension frame) {
         return myScrollManager.getLeftBoundary(frame, myPlayer.getCenter());
     }
     
     public double getUpperBoundary(Dimension frame) {
         return myScrollManager.getUpperBoundary(frame, myPlayer.getCenter());
     }
     
     public double getLowerBoundary(Dimension frame) { 
         return myScrollManager.getLowerBoundary(frame, myPlayer.getCenter());
     }
     public double getRightBoundary() {
         return myScrollManager.getRightBoundary(frameOfReferenceSize, myPlayer.getCenter());
     }
     
     public double getLeftBoundary() {
         return myScrollManager.getLeftBoundary(frameOfReferenceSize, myPlayer.getCenter());
     }
     
     public double getUpperBoundary() {
         return myScrollManager.getUpperBoundary(frameOfReferenceSize, myPlayer.getCenter());
     }
                    
     public double getLowerBoundary() { 
         return myScrollManager.getLowerBoundary(frameOfReferenceSize, myPlayer.getCenter());
     }
 
 
     public Dimension getLevelBounds() {
         return mySize;
     }
     
     
 
     //Methods from Editable Interface. Methods called by LevelEditor.
 
     @Override
     public void changeBackground () { //params need to be added
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void deleteSprite (int x, int y) {
         // TODO Auto-generated method stub
         
     }
     
     private void intersectingSprites () {
         Sprite obj1;
         Sprite obj2;
         CollisionManager CM = new CollisionManager(this);
 
        mySprites.add(myPlayer);
         for (int i = 0; i < mySprites.size(); i++) {
            for (int j = 0; j < mySprites.size(); j++) {     
                 obj1 = mySprites.get(i);
                 obj2 = mySprites.get(j);
                 if (obj1.intersects(obj2)) {
                     //System.out.println("Obj1: " + obj1.getClass() + " Obj2: " + obj2.getClass());
                     CM.handleCollision(obj1, obj2);
                 }
             }
         }
         
        mySprites.remove(mySprites.size()-1);
     }
     
     public View getView() {
         return myView;
     }
 
     @Override
     public void addSprite (Sprite s, int x, int y) {
         // TODO Not sure if needed
         // not needed in current use
         
     }
     
     
     /**
      * These two methods are quite something. Need to initialize input and delete it.
      */
     public void activate(){
         myInput = new PlayerInputs(myPlayer, myView);
     }
     
     public void deactivate() {
         myInput = null;
         // TODO: input maybe do soemthing about this. I don't want to have to rely
         // on java garbage collection.
     }
     
     // We never need getter because of how input is designed (only needs to be instantiated).
     public void setInput(IInput input) {
         myInput = input;
     }
     
     public IInput getInput(){
         return myInput;
     }
     
     public Player getPlayer() {
         return myPlayer;
     }
 
 }
