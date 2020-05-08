 /*
  * Catcher
  *
  * License: GPL v3
  * Authors: richard_jonsson@hotmail.com, tommyc@lavabit.com
  */
 package MIDP;
 
 import GUI.CatcherMain;
 import GUI.IMapView;
 import System.ICacheProvider;
 import System.IMapProvider;
 import System.IPositionProvider;
 import System.Position;
 import System.IImageLoader;
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.Image;
 
 public class MapView extends CatcherCanvas implements IMapView {
 
     private Position center;
 
     private int zoom;
 
     private String msg = "Greetings Catcher";
 
     private IMapProvider mapProvider = null;
 
     private TextBox textBox;
     /**
      * constructor
      */
     public MapView(CatcherMain main, ViewResources viewResources, ICacheProvider cacheProvider, IPositionProvider positionProvider,
             IMapProvider mapProvider, IImageLoader imageLoader) {
         super(main);
         
         setFullScreenMode(true);
         this.viewResources = viewResources;
         
         this.mapProvider = mapProvider;
         center = positionProvider.getLastPosition();
         textBox = new TextBox(0, 200, getWidth(), getHeight()-220);
         String s = "Text";
         textBox.setText(s);
     }
 
     public Position getCenter() {
         return center;
     }
 
     public void setCenter(Position center) {
         this.center = center;
     }
 
     public int getZoom() {
         return zoom;
     }
 
     public void setZoom(int zoom) {
         this.zoom = zoom;
     }
 
     /**
      * paint
      */
     public void paint(Graphics g) {
         g.setColor(COLOR_BACKGROUND);
         g.fillRect(0, 0, getWidth(), getHeight()-HEIGHT_STATUSBAR);
 
 
         // Lab code ahead!!
 
         g.setColor(COLOR_TEXT);
 
         textBox.paint(g);
 
         setCenter(new Position(57.77947, 14.22107));
         Position pos1 = new Position(57.6, 14.2);
 
         int distance = pos1.distanceTo(getCenter());
         g.drawString("Distance: "+String.valueOf(distance),0,100,Graphics.TOP|Graphics.LEFT);
 
         zoom=14;
         int[] xyCenter = mapProvider.positionToXY(center, center, getWidth(), 200, zoom);
         int[] xyPos1 = mapProvider.positionToXY(pos1, center, getWidth(), 200, zoom);
 
         g.drawString("Zoom:"+String.valueOf(zoom)+" Center: "+
                 String.valueOf(xyCenter[0])+":"+String.valueOf(xyCenter[1])
                 +" Pos1:"+
                 String.valueOf(xyPos1[0])+":"+String.valueOf(xyPos1[1])
                 ,0,120,Graphics.TOP|Graphics.LEFT);
 
         g.drawArc(xyCenter[0], xyCenter[1], 5, 5, 0, 360);
 
         g.setColor(255, 0, 0);
         g.drawArc(xyPos1[0], xyPos1[1], 5, 5, 0, 360);
 
         Position convPos = mapProvider.XYtoPosition(xyCenter[0], xyCenter[1], center, getWidth(), 200, zoom);
 
         g.drawString("Center "+String.valueOf(center.getLat())+", "+
                 String.valueOf(center.getLon()), 0, 140, Graphics.TOP|Graphics.LEFT);
         g.drawString("Conv "+String.valueOf(convPos.getLat())+", "+
                 String.valueOf(convPos.getLon()), 0, 160, Graphics.TOP|Graphics.LEFT);
 
         int[] xyPos2 = mapProvider.positionToXY(convPos, center, getWidth(), 200, zoom);
         g.setColor(127, 255, 0);
         g.drawLine(xyCenter[0], xyCenter[1], xyPos2[0], xyPos2[1]);
         g.drawArc(xyPos2[0], xyPos2[1], 5, 5, 0, 360);
        g.setClip(0, 0, getWidth(), getHeight());
         Image image = (Image)mapProvider.getMap(center, getWidth(), 200, zoom);
         if (image != null) {
             g.drawImage(image, 0, 30, Graphics.TOP|Graphics.LEFT);
         }
         g.setColor(0, 0, 0);
         g.drawString("MapView",0,40,Graphics.TOP|Graphics.LEFT);
         g.drawString(this.msg,0,60,Graphics.TOP|Graphics.LEFT);
 
         paintSelectedCache(g);
         paintStatusBar(g); // Keep this call last, it draws modal items (menu)
     }
     
     /**
      * Called when a key is pressed.
      */
     protected  void keyPressed(int keyCode) {
         if (!globalKeyPressed(keyCode)) {
             switch(getGameAction(keyCode)) {
                 case GAME_A: msg="game_a";break;
                 case GAME_B: msg="game_b";break;
                 case GAME_C: msg="game_c";break;
                 case GAME_D: msg="game_d";break;
                 case UP: msg="game_up";break;
                 case DOWN: msg="game_down";break;
                 case LEFT:
                     msg="game_left";
                     viewNavigator.ShowPrevious();
                     break;
                 case RIGHT:
                     msg="game_right";
                     viewNavigator.ShowNext();
                     break;
                 case FIRE: msg="game_fire";break;
                 default:
                     switch(keyCode) {
                         case KEY_NUM0: msg="0";break;
                         case KEY_NUM1: msg="1";break;
                         case KEY_NUM2: msg="2";break;
                         case KEY_NUM3: msg="3";break;
                         case KEY_NUM4: msg="4";break;
                         case KEY_NUM5: msg="5";break;
                         case KEY_NUM6: msg="6";break;
                         case KEY_NUM7: msg="7";break;
                         case KEY_NUM8: msg="8";break;
                         case KEY_NUM9: msg="9";break;
                         default: msg = "Unknown ("+String.valueOf(keyCode)
                                 +") KeyName: "+getKeyName(keyCode);
                     }
             }
 
             switch (getGameAction(keyCode)) {
                 case UP:textBox.scrollDown();break;
                 case DOWN:textBox.scrollUp();break;
             }
         }
         this.repaint();
     }
     
     /**
      * Called when a key is released.
      */
     protected  void keyReleased(int keyCode) {
     }
 
     /**
      * Called when a key is repeated (held down).
      */
     protected  void keyRepeated(int keyCode) {
     }
     
     /**
      * Called when the pointer is dragged.
      */
     protected  void pointerDragged(int x, int y) {
     }
 
     /**
      * Called when the pointer is pressed.
      */
     protected  void pointerPressed(int x, int y) {
     }
 
     /**
      * Called when the pointer is released.
      */
     protected  void pointerReleased(int x, int y) {
     }
 
     public void activate() {
         String[] viewItems = {"Zoom in", "Zoom out", "Auto follow",
         "Add location", "Select map"};
         menu.viewItems(viewItems);
     }
 
     public void deactivate() {
         menu.clearViewItems();
     }
 
     public void maptileDownloaded() {
         this.repaint();
     }
 
 }
