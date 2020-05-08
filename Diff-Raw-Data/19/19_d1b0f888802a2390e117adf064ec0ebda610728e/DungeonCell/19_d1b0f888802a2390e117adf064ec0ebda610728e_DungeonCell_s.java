 package com.macbury.procedular;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.util.Log;
 
 public class DungeonCell extends Rectangle {
   public final static int MAX_SPLIT_DEPTH = 7; 
   final static float MIN_SPLIT_FACTOR = 0.48f;
   final static float MAX_SPLIT_FACTOR = 0.52f;
   DungeonCell aCell;
   DungeonCell bCell;
   private Random random;
   private int depth = 0;
   private Room room;
   
   public DungeonCell(float x, float y, float width, float height, int depth, Random random) {
     super(x, y, width, height);
     this.random = random;
     this.depth = depth + 1;
     Log.debug("DungeonCell: X: "+x + " Y: "+ y + " width: "+ width + " height: " +height + " depth: "+depth);
     if (!this.split()) {
       this.createRoom();
     }
   }
   
   private void createRoom() {
    int baseWidth   = (int) ((this.getWidth() - 2) / 2);
    int baseHeight  = (int) ((this.getHeight() - 2) / 2);
     int roomWidth   = baseWidth + random.nextInt(baseWidth);
     int roomHeight  = baseHeight + random.nextInt(baseHeight);
    
    int ex          = random.nextInt((int) (this.getWidth()  - roomWidth));
    int ey          = random.nextInt((int) (this.getHeight() - roomHeight));
    
    this.room = new Room(this.getX() + ex, this.getY() + ey, roomWidth, roomHeight);
   }
 
   private int getSizeA(float size) {
     float factor = ((MAX_SPLIT_FACTOR - MIN_SPLIT_FACTOR) * random.nextFloat()) + MIN_SPLIT_FACTOR;
     return (int) (size * factor);
   }
   
   private void splitHorizontaly() {
     Log.info("Spliting horizontal on depth: "+this.depth);
     int sizeA = getSizeA(this.getWidth());
     int sizeB = (int)(this.getWidth() - sizeA);
     
     aCell = new DungeonCell(this.getX(), this.getY(), sizeA, this.getHeight(), this.depth, this.random);
     bCell = new DungeonCell(this.getX()+sizeA, this.getY(), sizeB, this.getHeight(), this.depth, this.random);
   }
 
   private void splitVertical() {
     Log.info("Spliting vertical on depth: "+this.depth);
     int sizeA = getSizeA(this.getHeight());
     int sizeB = (int)(this.getHeight() - sizeA);
     
     aCell = new DungeonCell(this.getX(), this.getY(), this.getWidth(), sizeA, this.depth, this.random);
     bCell = new DungeonCell(this.getX(), this.getY()+sizeA, this.getWidth(), sizeB, this.depth, this.random);
   }
   
   public boolean split() {
     if (this.depth >= MAX_SPLIT_DEPTH) {
       return false;
     } else {
       randomSplit();
       return true;
     }
   }
   
   private void randomSplit() {
     if (random.nextBoolean()) {
       splitVertical();
     } else {
       splitHorizontaly();
     }
   }
   
   public Random getRandom() {
     return random;
   }
   
   public Room getRoom() {
     return this.room;
   }
   
   public boolean haveRoom() {
     return this.room != null;
   }
   
   public int getDepth() {
     return depth;
   }
 
   public void setDepth(int depth) {
     this.depth = depth;
   }
 
   public void setRandom(Random r) {
     this.random = r;
   }
 
   public ArrayList<Room> getAllRooms() {
     ArrayList<Room> rooms = new ArrayList<Room>();
     return getRoomsFromLowerNode(rooms);
   }
   
   public boolean isBottomNode() {
     return (aCell != null) && (bCell != null);
   }
   
   private ArrayList<Room> getRoomsFromLowerNode(ArrayList<Room> rooms) {
     if (this.haveRoom()) {
       rooms.add(this.getRoom());
       return rooms;
     } else {
       rooms = this.aCell.getRoomsFromLowerNode(rooms);
       rooms = this.bCell.getRoomsFromLowerNode(rooms);
       return rooms;
     }
   }
 }
