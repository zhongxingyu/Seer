 package vooga.rts.leveleditor.components;
 
 import java.awt.Graphics;
 import java.io.IOException;
 import vooga.rts.leveleditor.gui.MapPanel;
 
 
 /**
  * the class of a single editable map node. This node has a linked list
  * to store the information of tiles, terrains and resources.
  * 
  * @author Richard Yang
  *
  */
 
 public class EditableNode {
     
     private int myX;
     private int myY;
     private int myWidth;
     private int myHeight;
     private int myPlayerIndex;
     
     private double myZoomRate;
     private boolean myOccupied;
     
     private Tile myTile;
     
     
     public EditableNode() {
         this(0, 0, MapPanel.DEFAULT_TILE_WIDTH, MapPanel.DEFAULT_TILE_HEIGHT, false);
     }
     public EditableNode(int x, int y, int width, int height, boolean isOccupied) {
         myX = x;
         myY = y;
         myWidth = width;
         myHeight = height;
         myOccupied = isOccupied;
         myPlayerIndex = 0;
         myTile = new Tile();
     }
 
     public int getMyX () {
         return myX;
     }
 
     public int getMyY () {
         return myY;
     }
     
     public void setOccupied(boolean b) {
         myOccupied = b;
     }
     
     public void refreshNodeImage() {
         
     }
     
     public boolean getOccupied() {
         return myOccupied;
     }
       
     public double getMyZoomRate() {
         return myZoomRate;
     }
     
     public void setMyZoomRate(double rate) {
         myZoomRate = rate;
     }
     
     public int getMyWidth () {
         return myWidth;
     }
     
     public void setTile(int id) {
         myTile.setType(id);
     }
     
     public int getMyHeight () {
         return myHeight;
     }
     
     public Tile getMyTile() {
         return myTile;
     }
     
     public void paint(Graphics pen) throws IOException {
        pen.drawImage(myTile.getMyImage(), myX*myWidth, myY*myHeight, myWidth, myHeight,null);
     }
 
     public void ZoomIn() {
         myWidth = (int)(myWidth * MapPanel.ZOOM_RATE);
         myHeight = (int)(myHeight * MapPanel.ZOOM_RATE);
     }
 
     public void ZoomOut() {
         myWidth = (int)(myWidth / MapPanel.ZOOM_RATE);
         myHeight = (int)(myHeight / MapPanel.ZOOM_RATE);
     }
     public void reset() {
         myOccupied = false;
         
         myTile.setType(0);
         
     }
     public void setPlayerIndex(int i) {
         myPlayerIndex = i;        
     }
     public int getPlayerIndex() {
         return myPlayerIndex;
     }
 
 }
