 import java.util.Arrays;
 
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
  *  See the README file accompanying this project for additional
  *  details.
 
 
  * BLAH BLAH BLAH experimetning with git
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
 
     private int width;
     private int height;
     private int starveTime;
     private Animal[][] gridData;
 
   /**
    *  Define any variables associated with an Ocean object here.  These
    *  variables MUST be private.
    */
 
 
 
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
     // Your solution here.
       width = i;
       height = j;
       this.starveTime = starveTime;
       gridData = new Animal[i][j];
   }
 
   /**
    *  width() returns the width of an Ocean object.
    *  @return the width of the ocean.
    */
 
   public int width() {
       return this.width;
   }
 
   /**
    *  height() returns the height of an Ocean object.
    *  @return the height of the ocean.
    */
 
   public int height() {
       return this.height;
   }
 
   /**
    *  starveTime() returns the number of timesteps sharks survive without food.
    *  @return the number of timesteps sharks survive without food.
    */
 
   public int starveTime() {
     // Replace the following line with your solution.
       return this.starveTime;
   }
 
   /**
    *  addFish() places a fish in cell (x, y) if the cell is empty.  If the
    *  cell is already occupied, leave the cell as it is.
    *  @param x is the x-coordinate of the cell to place a fish in.
    *  @param y is the y-coordinate of the cell to place a fish in.
    */
 
   public void addFish(int x, int y) {
       int[] spacePos = checkOffGrid(x,y);
       if(gridData[spacePos[0]][spacePos[1]] == null) {
     	  gridData[spacePos[0]][spacePos[1]] = new Animal(2);
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
       int[] spacePos = checkOffGrid(x,y);
       if(gridData[spacePos[0]][spacePos[1]] == null) {
     	  gridData[spacePos[0]][spacePos[1]] = new Shark(starveTime);
       }
   }
 
   /**
    *  cellContents() returns EMPTY if cell (x, y) is empty, FISH if it contains
    *  a fish, and SHARK if it contains a shark.
    *  @param x is the x-coordinate of the cell whose contents are queried.
    *  @param y is the y-coordinate of the cell whose contents are queried.
    */
 
   public int cellContents(int x, int y) {
       int[] spacePos = checkOffGrid(x,y);
       if(gridData[spacePos[0]][spacePos[1]] == null) {
     	  return 0;
       } else {
     	  return gridData[spacePos[0]][spacePos[1]].getType();
       }
   }
 
   /**
    *  timeStep() performs a simulation timestep as described in README.
    *  @return an ocean representing the elapse of one timestep.
    */
 
   public Ocean timeStep() {
       // Replace the following line with your solution.
       Ocean nextOcean = new Ocean(width, height, starveTime);
       int content;
       for(int i=0; i<width; i++) {
     	  for(int j=0; j<height; j++) {
     		  content = cellContents(i,j);
     		  switch (content) {
     		  case 0: //empty
     			  emptyUpdate(i, j, nextOcean);
     			  break;
     		  case 1: //shark
     			  sharkUpdate(i, j, nextOcean);
     			  break;
     		  case 2: //fish
     			  fishUpdate(i, j, nextOcean);
     			  break;
     		  }
     	  }	
       }
       return nextOcean;
   }
 
   	private void emptyUpdate(int x, int y, Ocean next) {
   		int[] counter = animalAround(x,y);
   		if(counter[1] < 2) {
   			//less than two fish, do nothing
   		} else if(counter[1] > 1 && counter[0] < 2) {
   			//2+ fish and only 1 shark, new fish
   			next.addFish(x, y);
   		} else if(counter[1] > 1 && counter[0] > 1) {
   			//2+ fish and 2+ shark, new shark
   			next.addShark(x, y);
   		}
   	}
   
     private void sharkUpdate(int x, int y, Ocean next) {
     	int[] counter = animalAround(x,y);
     	if(counter[1] > 0) {
     		//if there's a fish, the shark becomes well fed
     		next.addShark(x, y);
     	} else if(counter[1] == 0) {
     		if((((Shark) gridData[x][y]).dyingTime())-1 < 0) {
     			//do nothing, the shark dies
     		} else {
     			//add a shark that has one less time
     			next.addShark(x,y, ((Shark) gridData[x][y]).dyingTime() -1);
     		}
     	}
     }
 
     private void fishUpdate(int x, int y, Ocean next) {
     	int[] counter = animalAround(x,y);
     	if(counter[0]==0) {
     		//if there are no sharks, the fish stays alive
     		next.addFish(x,y);
     	} else if(counter[0] > 1) {
     		//if there are more than one shark, new shark spawns
     		next.addShark(x,y);
     	} else {
     		//do nothing
     	}
     }
     
     private int[] animalAround(int x, int y) {
     	int[] count = {0,0}; //0 index is shark and 1 index is fish
     	for(int i=-1; i<2; i++) {
     		for(int j=-1; j<2; j++) {
     			if(i==0&&j==0) {
     				
     			} else {
     				int[] searchPos = {i+x, j+y};
     				if(cellContents(searchPos[0], searchPos[1]) == 1) {
     					count[0]++;
     				} else if(cellContents(searchPos[0], searchPos[1]) == 2) {
     					count[1]++;
     				} else {
     					//if there isnt a shark or a fish, do nothing
     				}
     			}	
     		}
     	}
     	return count;
     }
     
     private int[] checkOffGrid(int x, int y) {
     	int[] actualPos = {0,0};
     	if(x<0) {
     		actualPos[0] = width + x%width;
     	} else {
     		actualPos[0] = Math.abs(x)%width;
     	}
     	if(y<0) {
     		actualPos[1] = height + y%height;
     	} else {
     		actualPos[1] = Math.abs(y)%height;
     	}
     	return actualPos;
     }
     
     public static void main(String[] args) {
     	Ocean sea = new Ocean(4, 4, 2);
     	sea.addShark(4,1);
     	System.out.println(sea.cellContents(0,0));
     	System.out.println("Should be a 1: "+sea.cellContents(0,1));
     	System.out.println("Should be a 1: "+sea.cellContents(4,1));
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
 	  if(gridData[x][y] == null) {
 		  gridData[x][y] = new Shark(feeding);
 	  }
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
 	  return ((Shark)gridData[x][y]).dyingTime();
   }
 
 }
