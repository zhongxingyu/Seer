 /* Ocean.java */
 
 /**
  *  The Ocean class defines an object that models an ocean full of sharks and
  *  fish.  Descriptions of the methods you must implement appear below.  They
  *  include a constructor of the form
  *
  *      public Ocean(int i, int j, int starveTime);
  *
  *  that creates an empty ocean having width i and height j, in which sharks
  *  starve after starveTime timesteps.
  *
  *  See the README file accompanying this project for additional details.
  */
 
 public class Ocean {
 
   /**
    *  Do not rename these constants.  WARNING:  if you change the numbers, you
    *  will need to recompile Test4.java.  Failure to do so will give you a very
    *  hard-to-find bug.
    */
 
   public final static int EMPTY = 0;
   public final static int SHARK = 1;
   public final static int FISH = 2;
 
   /**
    *  Define any variables associated with an Ocean object here.  These
    *  variables MUST be private.
    */
   private int width;
   private int height;
   private int starveTime;
   private int[][] thisocean, sharkdietime;
 
   /**
    *  The following methods are required for Part I.
    */
 
   /**
    *  Ocean() is a constructor that creates an empty ocean having width i and
    *  height j, in which sharks starve after starveTime timesteps.
    *  @param i is the width of the ocean.
    *  @param j is the height of the ocean.
    *  @param starveTime is the number of timesteps sharks survive without food.
    */
 
   public Ocean(int i, int j, int starveTime) {
     this.width = i;
     this.height = j;
     this.starveTime = starveTime;
     thisocean = new int[width][height];
     sharkdietime = new int[width][height];
   }
 
   /**
    *  width() returns the width of an Ocean object.
    *  @return the width of the ocean.
    */
 
   public int width() {
     // Replace the following line with your solution.
     return width;
   }
 
   /**
    *  height() returns the height of an Ocean object.
    *  @return the height of the ocean.
    */
 
   public int height() {
     // Replace the following line with your solution.
     return height;
   }
 
   /**
    *  starveTime() returns the number of timesteps sharks survive without food.
    *  @return the number of timesteps sharks survive without food.
    */
 
   public int starveTime() {
     // Replace the following line with your solution.
     return starveTime;
   }
 
   /**
    *  addFish() places a fish in cell (x, y) if the cell is empty.  If the
    *  cell is already occupied, leave the cell as it is.
    *  @param x is the x-coordinate of the cell to place a fish in.
    *  @param y is the y-coordinate of the cell to place a fish in.
    */
 
   public int modulo(int number, int mod){
     if(number < 0){
       return modulo(number + mod, mod);
     }
     return number % mod;
   }
 
   public void addFish(int x, int y) {
     // Your solution here.
     if(cellContents(x, y) == EMPTY){
       int a = modulo(x, width);
       int b = modulo(y, height);
       thisocean[a][b]= FISH;
     }
   }
 
   /**
    *  addShark() (with two parameters) places a newborn shark in cell (x, y) if
    *  the cell is empty.  A "newborn" shark is equivalent to a shark that has
    *  just eaten.  If the cell is already occupied, leave the cell as it is.
    *  @param x is the x-coordinate of the cell to place a shark in.
    *  @param y is the y-coordinate of the cell to place a shark in.
    */
 
   public void addShark(int x, int y) {
     // Your solution here.
     if(cellContents(x, y) == EMPTY){
       int a = modulo(x, width);
       int b = modulo(y, height);
       thisocean[a][b]= SHARK;
       sharkdietime[a][b] = starveTime;
     }
   }
 
   /**
    *  cellContents() returns EMPTY if cell (x, y) is empty, FISH if it contains
    *  a fish, and SHARK if it contains a shark.
    *  @param x is the x-coordinate of the cell whose contents are queried.
    *  @param y is the y-coordinate of the cell whose contents are queried.
    */
 
   public int cellContents(int x, int y) {
     // Replace the following line with your solution.
     int a = modulo(x, width);
     int b = modulo(y, height);
     return thisocean[a][b];
   }
 
   /**
    *  timeStep() performs a simulation timestep as described in README.
    *  @return an ocean representing the elapse of one timestep.
    */
 
   public Ocean timeStep() {
     // Replace the following line with your solution.
     Ocean newocean = new Ocean(width, height, starveTime);
     for(int i = 0; i < width; i++){
       for(int j = 0; j < height; j++){
        if(cellContents(i, j) == SHARK){
           if(neighborfish(i, j) >= 1){
             newocean.sharkdietime[i][j] = starveTime;
             newocean.thisocean[i][j] = SHARK;
           }else{
            newocean.sharkdietime[i][j]--;
             newocean.thisocean[i][j] = SHARK;
             if(newocean.sharkdietime[i][j] == 0){
               newocean.thisocean[i][j] = EMPTY;
             }
           }
         }
 
        if(cellContents(i, j) == FISH){
           if(neighborshark(i ,j) == 0){
             newocean.thisocean[i][j] = FISH;
           }else if(neighborshark(i, j) == 1){
             newocean.thisocean[i][j] = EMPTY;
          }else if(neighborshark(i, j) > 1){
             newocean.thisocean[i][j] = SHARK;
             newocean.sharkdietime[i][j] = starveTime;
           }
         }
 
        if(cellContents(i ,j) == EMPTY){
           if(neighborfish(i, j) <= 1){
             newocean.thisocean[i][j] = EMPTY;
           }else if(neighborshark(i, j) <= 1 && neighborfish(i, j) >= 2){
             newocean.thisocean[i][j] = FISH; 
           }else if(neighborfish(i, j) >= 2 && neighborshark(i, j) >= 2){
             newocean.thisocean[i][j] = SHARK;
             newocean.sharkdietime[i][j] = starveTime;
           }
         }
       }
     }
     return newocean;
   }
 
   public int neighborfish(int i, int j){
     int fishnum = 0;
     for(int a = i - 1; a <= i + 1; a++){
       for(int b = j - 1; b <= j + 1; b++){
        if(thisocean[modulo(a, width)][modulo(b, height)] == FISH)
           fishnum++;
       }
     }
    if(thisocean[modulo(i, width)][modulo(j, height)] == FISH)
       fishnum--;
     return fishnum;
   }
 
   public int neighborshark(int i, int j){
     int sharknum = 0;
     for(int a = i - 1; a <= i + 1; a++){
       for(int b = j - 1; b <= j + 1; b++){
        if(thisocean[modulo(a, width)][modulo(b, height)] == SHARK)
           sharknum++;
       }
     }
    if(thisocean[modulo(i, width)][modulo(j, height)] == SHARK)
       sharknum--;
     return sharknum;
   }
 
 
   /**
    *  The following method is required for Part II.
    */
 
   /**
    *  addShark() (with three parameters) places a shark in cell (x, y) if the
    *  cell is empty.  The shark's hunger is represented by the third parameter.
    *  If the cell is already occupied, leave the cell as it is.  You will need
    *  this method to help convert run-length encodings to Oceans.
    *  @param x is the x-coordinate of the cell to place a shark in.
    *  @param y is the y-coordinate of the cell to place a shark in.
    *  @param feeding is an integer that indicates the shark's hunger.  You may
    *         encode it any way you want; for instance, "feeding" may be the
    *         last timestep the shark was fed, or the amount of time that has
    *         passed since the shark was last fed, or the amount of time left
    *         before the shark will starve.  It's up to you, but be consistent.
    */
 
   public void addShark(int x, int y, int feeding) {
     // Your solution here.
   }
 
   /**
    *  The following method is required for Part III.
    */
 
   /**
    *  sharkFeeding() returns an integer that indicates the hunger of the shark
    *  in cell (x, y), using the same "feeding" representation as the parameter
    *  to addShark() described above.  If cell (x, y) does not contain a shark,
    *  then its return value is undefined--that is, anything you want.
    *  Normally, this method should not be called if cell (x, y) does not
    *  contain a shark.  You will need this method to help convert Oceans to
    *  run-length encodings.
    *  @param x is the x-coordinate of the cell whose contents are queried.
    *  @param y is the y-coordinate of the cell whose contents are queried.
    */
 
   public int sharkFeeding(int x, int y) {
     // Replace the following line with your solution.
     return 0;
   }
 
 }
