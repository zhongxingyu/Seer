 import java.util.*;
 
 /*
  *      Cube.java
  *      making a test comment
  */
 
 
 public class Cube {
 
     // Constants
     public static final int X = 0;
     public static final int Y = 1;
     public static final int Z = 2;
     public static final int BLACK = 0;
     public static final int WHITE = 1;
     public static final int EMPTY = 2;
     public static final int DOME = 3;
     public static final int XUP = 0;
     public static final int XDOWN = 1;
     public static final int YUP = 2;
     public static final int YDOWN = 3;
     public static final int ZUP = 4;
     public static final int ZDOWN = 5;
     public static final int NONE = -1;
 	public static final String[] fnames = {"x+", "x-", "y+", "y-", "z+", "z-"};
 
     // Data members
     private int[] location;
     private int[] faces;
     private int color;
     private HashMap<String, Cube> board;
     
     // Constructor
     public Cube (int x, int y, int z, int d1, int d2, int color) {
         location = new int[3];
         location[X] = x;
         location[Y] = y;
         location[Z] = z;
         faces = new int[6];
         for (int i = 0; i < faces.length; i++) {
             if (i == d1 || i == d2) {
                 faces[i] = DOME;
             } else {
                 faces[i] = EMPTY;
             }
         }
         this.color = color;
     }
     
     public Cube (String loc, int d1, int d2, int color) {
     	int first = loc.indexOf(",");
     	int second = loc.indexOf(",", first + 1);
         location = new int[3];
 		location[X] = Integer.parseInt(loc.substring(0, first));
 		location[Y] = Integer.parseInt(loc.substring(first + 1, second));
 		location[Z] = Integer.parseInt(loc.substring(second + 1));
         faces = new int[6];
         for (int i = 0; i < faces.length; i++) {
             if (i == d1 || i == d2) {
                 faces[i] = DOME;
             } else {
                 faces[i] = EMPTY;
             }
         }
         this.color = color;
 	}
 	
     public void setBoard(HashMap<String, Cube> b) {
         this.board = b;
     }
 
     public int getFace(int f) {
         if (f == NONE) {
             return NONE;
         }
         return faces[f];
     }
     
     public String getName() {
         return "" + getX() + "," + getY() + "," + getZ();
     }
     
     public int getColor() {
     	return color;
     }
     
     public int getX() {
         return location[X];
     }
 
     public int getY() {
         return location[Y];
     }
 
     public int getZ() {
         return location[Z];
     }
 
     public Cube clone() {
         Cube c = new Cube(location[X], location[Y], location[Z], firstDome(), secondDome(), color);
         if (c.addSceptre(firstSceptre(), getFace(firstSceptre()))) {
             c.addSceptre(secondSceptre(), getFace(secondSceptre()));
         }
         return c;
     }
 
     public int firstDome() {
         for (int i = 0; i < faces.length; i++) {
             if (faces[i] == DOME) {
                 return i;
             }
         }
         // should NEVER happen
         System.err.println("UHOH!!!");
         System.err.println(getName());
         System.exit(-1);
         return NONE;
     }
 
     public int secondDome() {
         for (int i = firstDome() + 1; i < faces.length; i++) {
             if (faces[i] == DOME) {
                 return i;
             }
         }
         return NONE;
     }
 
     public int firstSceptre() {
         for (int i = 0; i < faces.length; i++) {
             if (faces[i] == BLACK || faces[i] == WHITE) {
                 return i;
             }
         }
         return NONE;
     }
     
     public int secondSceptre() {
         int f = firstSceptre();
         if (f == NONE) {
             return NONE;
         }
         for (int i = f + 1; i < faces.length; i++) {
             if (faces[i] == BLACK || faces[i] == WHITE) {
                 return i;
             }
         }
         return NONE;
     }
 
     public boolean addSceptre(int f, int color) {
         if (f != NONE && isEmpty(f)) {
             faces[f] = color;
             return true;
         }
         return false;
     }
     
     public int removeSceptre(int f) {
         if (faces[f] == WHITE || faces[f] == BLACK) {
             int temp = faces[f];
             faces[f] = EMPTY;
             // 
             return temp;
         }
         return -1;
     }
     
     public boolean isEncroached() {
         for (int i = 0; i < faces.length; i++) {
             if (faces[i] == WHITE && color == BLACK) {
                 return true;           
             }
             if (faces[i] == BLACK && color == WHITE) {
                 return true;
             }
         }
         return false;
     }
     
     public boolean isOccupied() {
         for (int i = 0; i < faces.length; i++) {
             if (faces[i] == WHITE || faces[i] == BLACK) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isFree() {
         if (isOccupied()) {
             return false;
         }
         String above = "" + location[X] + "," + 
                             location[Y] + "," + 
                             (location[Z] + 1);
         if (board.get(above) == null) {
             return true;
         }
         return false;
     }
     
     public Cube getNeighbor(int f) {
     	if (f == ZUP) {
     		return board.get("" + getX() + "," + getY() + "," + (getZ() + 1));
     	} else if (f == ZDOWN) {
     		return board.get("" + getX() + "," + getY() + "," + (getZ() - 1));    	
     	} else if (f == YUP) {
     		return board.get("" + getX() + "," + (getY() + 1) + "," + getZ());    	
     	} else if (f == YDOWN) {
     		return board.get("" + getX() + "," + (getY() - 1) + "," + getZ());    	
     	} else if (f == XUP) {
     		return board.get("" + (getX() + 1) + "," + getY() + "," + getZ());    	
     	} else if (f == XDOWN) {
     		return board.get("" + (getX() - 1)+ "," + getY() + "," + getZ());    	
     	}
     	return null;
     }
 
 	public Cube getNeighbor(int dx, int dy, int dz) {
 		return board.get("" + (getX() + dx) + "," + (getY() + dy) + "," + (getZ() + dz));
 	}
 
     public ArrayList<String> freeFaces() {
     	ArrayList<String> free = new ArrayList<String>();
     	for (int i = 0; i < faces.length; i++) {
     		if (faces[i] == EMPTY || faces[i] == DOME) {
 				String neighbor = null;
 				String support = null;
     			if (i == XUP) {
     			    support = "" + (location[X] + 1) + "," + 
     				 	    	    location[Y] + "," +
     						       (location[Z] - 1);
     			    if (location[Z] == 1 || board.get(support) != null) {
     			        neighbor = "" + (location[X] + 1) + "," + 
     					    			 location[Y] + "," +
     						     	     location[Z];
     				}
     			} else if (i == YUP) {
     			    support = "" + location[X] + "," + 
     					    		(location[Y] + 1) + "," +
     						       (location[Z] - 1);
     			    if (location[Z] == 1 || board.get(support) != null) {
     			 	    neighbor = "" + location[X] + "," + 
     						    	   (location[Y] + 1) + "," +
     						 	        location[Z];  
     				}
     			} else if (i == ZUP) {
     			 	    neighbor = "" + location[X] + "," + 
     					    			location[Y] + "," +
     						     	   (location[Z] + 1);    		
     						  
     			} else if (i == XDOWN) {
     			    support = "" + (location[X] - 1) + "," + 
     					    		location[Y] + "," +
     						       (location[Z] - 1);
     			    if (location[Z] == 1 || board.get(support) != null) {
     			 	    neighbor = "" + (location[X] - 1) + "," + 
     					    			 location[Y] + "," +
     						     	     location[Z];
     		        }
     			} else if (i == YDOWN) {
     			    support = "" + location[X] + "," + 
     					          (location[Y] - 1) + "," +
     						      (location[Z] - 1);
     			    if (location[Z] == 1 || board.get(support) != null) {
     			 	    neighbor = "" + location[X] + "," + 
     					    		   (location[Y] - 1) + "," +
     						     	    location[Z];    			    			
     			    }
     			}
     			if (neighbor != null && board.get(neighbor) == null) {
     				free.add(neighbor);
     			}
     		}
     	}
     	return free;
     }
 
     public boolean legal() {
         // do you have a locked dome and no conflicts DOME - DOME? *Denotes legal positioning*
         
         if (location[Z] == 1 && (firstDome() == ZDOWN || secondDome() == ZDOWN)) {
         	return false;
         }
         boolean locked = false;
         for (int i = 0; i < faces.length; i++) {
             String neighbor = null;
             if (i == XUP) {
                 neighbor = "" + (location[X] + 1) + "," + 
     					         location[Y] + "," +
     					 	     location[Z];  
     			if (board.containsKey(neighbor)) {
     			    if ((faces[i] == DOME && board.get(neighbor).getFace(XDOWN) == EMPTY) || 
     			        (faces[i] == EMPTY && board.get(neighbor).getFace(XDOWN) == DOME)){
     			        locked = true; 
     			    }
     			    if (faces[i] == DOME && board.get(neighbor).getFace(XDOWN) == DOME) {
     					return false;
     			    }
     			}
     	    }    
             if (i == XDOWN) {
                 neighbor = "" + (location[X] - 1) + "," + 
     					         location[Y] + "," +
     					 	     location[Z];  
                 if (board.containsKey(neighbor)) {
     			    if ((faces[i] == DOME && board.get(neighbor).getFace(XUP) == EMPTY) || 
     			        (faces[i] == EMPTY && board.get(neighbor).getFace(XUP) == DOME)){
     			        locked = true; 
     			    }
     			    if (faces[i] == DOME && board.get(neighbor).getFace(XUP) == DOME) {
     					return false;
     			    }
     			}
             }
             if (i == YUP) {
                 neighbor = "" + location[X] + "," + 
     					       (location[Y] + 1) + "," +
     					 	    location[Z];  
                 if (board.containsKey(neighbor)) {
     			    if ((faces[i] == DOME && board.get(neighbor).getFace(YDOWN) == EMPTY) || 
     			        (faces[i] == EMPTY && board.get(neighbor).getFace(YDOWN) == DOME)){
     			        locked = true; 
     			    }
     			    if (faces[i] == DOME && board.get(neighbor).getFace(YDOWN) == DOME) {
     					return false;
     			    }
     			}
             }
             if (i == YDOWN) {
                 neighbor = "" + location[X] + "," + 
     					       (location[Y] - 1) + "," +
     					 	    location[Z];  
                 if (board.containsKey(neighbor)) {
     			    if ((faces[i] == DOME && board.get(neighbor).getFace(YUP) == EMPTY) || 
     			        (faces[i] == EMPTY && board.get(neighbor).getFace(YUP) == DOME)){
     			        locked = true; 
     			    }
     			    if (faces[i] == DOME && board.get(neighbor).getFace(YUP) == DOME) {
     					return false;
     			    }
     			}
             }
             if (i == ZUP) {
                 neighbor = "" + location[X] + "," + 
     					        location[Y] + "," +
     					 	   (location[Z] + 1);  
                 if (board.containsKey(neighbor)) {
     			    if ((faces[i] == DOME && board.get(neighbor).getFace(ZDOWN) == EMPTY) || 
     			        (faces[i] == EMPTY && board.get(neighbor).getFace(ZDOWN) == DOME)){
     			        locked = true; 
     			    }
     			    if (faces[i] == DOME && board.get(neighbor).getFace(ZDOWN) == DOME) {
     					return false;
     			    }
     			}
             }
             if (i == ZDOWN) {
                 neighbor = "" + location[X] + "," + 
     					        location[Y] + "," +
     					 	   (location[Z] - 1);  
                 if (board.containsKey(neighbor)) {
     			    if ((faces[i] == DOME && board.get(neighbor).getFace(ZUP) == EMPTY) || 
     			        (faces[i] == EMPTY && board.get(neighbor).getFace(ZUP) == DOME)){
     			        locked = true; 
     			    }
     			    if (faces[i] == DOME && board.get(neighbor).getFace(ZUP) == DOME) {
     					return false;
     			    }
     			}
             }   
         }
         return locked;
     }
 
     public boolean twoSceptreSameColor() {
         int first = getFace(firstSceptre());
         int second = getFace(secondSceptre());
         if (first != NONE) {
             return first == second;
         }
         return false;
     }
 
     public boolean twoSceptreDifferentColor() {
         int first = getFace(firstSceptre());
         int second = getFace(secondSceptre());
         if (first != NONE && second != NONE) {
             return first != second;
         }
         return false;
     }
     
     public boolean isEmpty(int f) {
         return faces[f] == EMPTY;
     }
     
     // Rotate the cube in the direction d, being one of X, Y or Z
    // MHG 11/5/2011 Ended up not being used..
     public void rotate(int d) {
         if (d == Y) {
             int temp = faces[XUP];
             faces[XUP] = faces[ZUP];
             faces[ZUP] = faces[XDOWN];
             faces[XDOWN] = faces[ZDOWN];
             faces[ZDOWN] = temp;
         }
         else if (d == X) {
             int temp = faces[YUP];
             faces[YUP] = faces[ZUP];
             faces[ZUP] = faces[YDOWN];
             faces[YDOWN] = faces[ZDOWN];
             faces[ZDOWN] = temp;       
         }
         else if (d == Z) {
             int temp = faces[XUP];
             faces[XUP] = faces[YDOWN];
             faces[YDOWN] = faces[XDOWN];
             faces[XDOWN] = faces[YUP];
             faces[YUP] = temp;
             }
     }
 
     // Use the notation from the play by email paper
     public String toString() {
         String c = "C (" + getX() + "," + getY() + "," + getZ() + ")";
         int fd = firstDome();
         if (fd == XUP) {
             c += "x+";
         }
         else if (fd == XDOWN) {
             c += "x-";
         }
         else if (fd == YUP) {
             c += "y+";
         }
         else if (fd == YDOWN) {
             c += "y-";
         }
         else if (fd == ZUP) {
             c += "z+";
         }
         else if (fd == ZDOWN) {
             c += "z-";
         }
         if (secondDome() != NONE) {
             int sd = secondDome();
             if (sd == XUP) {
                 c += " x+";
             }
             else if (sd == XDOWN) {
                 c += " x-";
             }
             else if (sd == YUP) {
                 c += " y+";
             }
             else if (sd == YDOWN) {
                 c += " y-";
             }
             else if (sd == ZUP) {
                c += " z+";
             }
             else if (sd == ZDOWN) {
                 c += " z-";
             }
         }
         if (color == WHITE) {
             c += " (white) ";
         } else {
             c += " (black) ";
         }
 
         return c;
     }
     
     // Use the notation from the play by email paper
     public String toSceptreString() {
         String t = "";
         String s = "S (" + getX() + "," + getY() + "," + getZ() + ")";
         if (firstSceptre() != NONE) {
             t += s;
             int fs = firstSceptre();
             if (fs == XUP) {
                 t += "x+";
             }
             else if (fs == XDOWN) {
                 t += "x-";
             }
             else if (fs == YUP) {
                 t += "y+";
             }
             else if (fs == YDOWN) {
                 t += "y-";
             }
             else if (fs == ZUP) {
                 t += "z+";
             }
             else if (fs == ZDOWN) {
                 t += "z-";
             }
             if (getFace(firstSceptre()) == WHITE) {
                 t += " (white) ";
             } else {
                 t += " (black) ";
             }
             if (secondSceptre() != NONE) {
                 t += "\n" + s;
                 int ss = secondSceptre();
                 if (ss == XUP) {
                     t += " x+";
                 }
                 else if (ss == XDOWN) {
                     t += " x-";
                 }
                 else if (ss == YUP) {
                     t += " y+";
                 }
                 else if (ss == YDOWN) {
                     t += " y-";
                 }
                 else if (ss == ZUP) {
                     t += " z+";
                 }
                 else if (ss == ZDOWN) {
                     t += " z-";
                 }
                 if (getFace(secondSceptre()) == WHITE) {
                     t += " (white) ";
                 } else {
                     t += " (black) ";
                 }
             }
          }
         return t;
     }
     
     public static void main (String args[]) {
         HashMap<String, Cube> b = new HashMap<String, Cube>();
        Cube stuff = new Cube(-1, 0, 2, YDOWN, XDOWN, BLACK);
         stuff.addSceptre(ZUP, BLACK);
         stuff.setBoard(b);
         b.put(stuff.getName(), stuff);
         System.out.println(stuff);
         System.out.println(stuff.toSceptreString());
         System.out.println("rotating");
         stuff.rotate(X);
         System.out.println(stuff);
         System.out.println(stuff.toSceptreString());
      }
 }
