 class BallManager {
   
   public static final int MAX_SLOTS = 10;
   public static final int MAX_BALLS = 15;
   private FallingBall[][] slots;
 
   public BallManager() {
     System.out.println("BallManager constructor");
     slots = new FallingBall[MAX_BALLS][MAX_SLOTS];
     for(int i=0; i<MAX_BALLS; i++) {
       for(int j=0; j<MAX_SLOTS; j++) {
         slots[i][j] = null;
       }
     }
   }
 
   public synchronized void dropBall(int slot) {
     System.out.println("BallManager dropBall(" + slot + ")");
     slots[0][slot] = new FallingBall(slot,this);
   }
 
   public synchronized boolean killBall(int x, int y) {
     System.out.println("BallManager killBall()");
     if(slots[y][x] != null) {
       slots[y][x] = null;
       return true;
     }
     else return false;
   }
 
   public synchronized boolean available(int slot, int row) {
     //System.out.println("BallManager available() slot = " + slot + ", row = " + row);
    if(slots[row][slot] != null) return true;
    else return false;
     
   }
 
   public synchronized void moveBall(int oldSlot, int oldPos, int newSlot, int newPos) {
     //System.out.println("BallManager moveBall - oldPos = " + oldPos + ", newPos = " + newPos);
     slots[newPos][newSlot] = slots[oldPos][oldSlot];
     slots[oldPos][oldSlot] = null;
   }
 
   public synchronized FallingBall[][] getSlots() {
     return slots.clone();
   }
 
 }
