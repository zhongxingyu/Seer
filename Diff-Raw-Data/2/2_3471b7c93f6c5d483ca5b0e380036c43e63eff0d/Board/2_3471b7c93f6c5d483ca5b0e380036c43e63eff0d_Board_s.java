 import java.util.*;
 
 /**This object contains the field of mines for the game using an array of object Location
   * 
   */
 public class Board {
   
  private ArrayList mines; //used to create a list of 40 unique random ints
   private Location[][] locations; //used to store all the locations
   private int xdimension;
   private int ydimension; 
   private int size;
   private int minequantity;
   private Random number;
   
   /**Only one constructor, which doesn't take any parameters
     *The way the board is constructed is by creating an arraylist of 40 random ints (number of mines)
     *then making an array of size 18 by 18 and going through the 16 by 16 sub array in and adding in the mines
     *in random locations. The sub array is then looked at again, examining how many mines surround each location
     *and attaching that value to the Location object
     * 
     */
   public Board() {
     xdimension = 16;
     ydimension = 16;
     minequantity = 40;
     size = xdimension * ydimension;
     mines = new ArrayList<Integer>();
     locations = new Location[ydimension + 2][xdimension + 2];
     number = new Random();
     int add;
     for(int ii = 0; ii < minequantity; ii++) {
       add = number.nextInt(size) + 1;
       while(mines.contains(add))
         add = number.nextInt(size) + 1;
       mines.add(add);
     }
     for(int ii = 1; ii < ydimension + 1; ii++) {
       for(int jj = 1; jj < xdimension + 1; jj++) {
         if(mines.contains(((jj-1) + (ii-1) * xdimension)))
           locations[ii][jj] = new Location(true);
         else
           locations[ii][jj] = new Location(false);
       }
       
     }
     int count;
     for(int ii = 1; ii < ydimension + 1; ii++) {
       for(int jj = 1; jj < xdimension + 1; jj++) {
         count = 0;
         if(locations[ii][jj-1] != null && locations[ii][jj-1].isMine())
           count++;
         if(locations[ii][jj+1] != null && locations[ii][jj+1].isMine())
           count++;
         if(locations[ii-1][jj] != null && locations[ii-1][jj].isMine())
           count++;
         if(locations[ii-1][jj-1] != null && locations[ii-1][jj-1].isMine())
           count++;
         if(locations[ii-1][jj+1] != null && locations[ii-1][jj+1].isMine())
           count++;
         if(locations[ii+1][jj] != null && locations[ii+1][jj].isMine())
           count++;
         if(locations[ii+1][jj-1] != null && locations[ii+1][jj-1].isMine())
           count++;
         if(locations[ii+1][jj+1] != null && locations[ii+1][jj+1].isMine())
           count++;
         locations[ii][jj].defineBorder(count);
       }
     }
   }
   
   /**Returns whether or not the given coordinate has a mine
     * 
     *@param x This is the x-coordinate
     *@param y This is the y-coordinate
     *@return A boolean saying whether or not it is a mine
     */
   public boolean isMine(int x, int y) {
     return locations[y+1][x+1].isMine();
   }
   
   /**Returns the number of mines bordering the given coordinate
     *
     *@param x This is the x-coordinate
     *@param y This is the y-coordinate
     *@return The number of mines
     */  
   public int getBorder(int x, int y) {
     return locations[y+1][x+1].getBorder();
   }
   
   /**Returns whether or not the given coordinate has a flag on it
     * 
     *@param x This is the x-coordinate
     *@param y This is the y-coordinate
     *@return A boolean saying whether or not it has a flag on it
     */  
   public boolean hasFlag(int x, int y) {
     return locations[y+1][x+1].hasFlag();
   }
   
   /**Returns whether or not the given coordinate has been clicked
     * 
     *@param x This is the x-coordinate
     *@param y This is the y-coordinate
     *@return A boolean saying whether or not it has been clicked
     */  
   public boolean isClicked(int x, int y) {
     return locations[y+1][x+1].isClicked();
   }
   
   
   /**Determines the state of the flag on the specific coordinate
     * 
     *@param x This is the x-coordinate
     *@param y This is the y-coordinate
     *@boolean flag This is whether the flag is being set or taken away
     */  
   public void defineFlag(int x, int y, boolean flag) {
     locations[y+1][x+1].defineFlag(flag);
   }
   
   /**Determines the state of being clicked or not on a given coordinate
     * 
     *@param x This is the x-coordinate
     *@param y This is the y-coordinate
     *@boolean clicked Whether or not the location has been clicked
     */  
   public void defineClick(int x, int y, boolean clicked) {
     locations[y+1][x+1].defineClick(clicked);
   }
   
 }
