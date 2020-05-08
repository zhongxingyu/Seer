 /**
  * @file Field.java
  * 
  * A container for fixed points.
  * 
  * @author Grant Hays
  * @date 10/13/11
  * @version 1
  */
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 /**@class Field
  * 
  * This creates an ArrayList that holds all the coordinates for the fixed 
  * points on the field. As the orientation of the axes depends on the side 
  * of the field the starts on, there are two sets of coordinates, each with 
  * opposite signs. 
  * 
  * @author Grant Hays
  *
  */
 public class Field {
 	
 
 	/**
 	 * Field constructor
 	 * @param side	The side of the field the player's team starts on
 	 * @pre The side needs to be parsed from the server's (init) message and 
 	 * passed as the argument
 	 * @post A new Field will be created with access to an array list of all 
 	 * the field's fixed points
 	 * 
 	 */
 	public Field(String side) {
 		
 		// The coordinates for the team starting on the left side
 		if(side.compareTo("l") == 0) {
 			posList.add(new Pos("ftl50", -50, -39));
 			posList.add(new Pos("ftl40", -40, -39));
 			posList.add(new Pos("ftl30",-30, -39));
 			posList.add(new Pos("ftl20", -20, -39));
 			posList.add(new Pos("ftl10", -10, -39));
 			posList.add(new Pos("ft0", 0, -39));
 			posList.add(new Pos("ftr10", 10, -39));
 			posList.add(new Pos("ftr20", 20, -39));
 			posList.add(new Pos("ftr30", 30, -39));
 			posList.add(new Pos("ftr40", 40, -39));
 			posList.add(new Pos("ftr50", 50, -39));
 			
 			posList.add(new Pos("frt30", 57.5, -30));
 			posList.add(new Pos("frt20", 57.5, -20));
 			posList.add(new Pos("frt10", 57.5, -10));
 			posList.add(new Pos("fr0", 57.5, 0));
 			posList.add(new Pos("frb10", 57.5, 10));
 			posList.add(new Pos("frt20", 57.5, 20));
 			posList.add(new Pos("frt30", 57.5, 30));
 			
 			posList.add(new Pos("fbl50", -50, 39));
 			posList.add(new Pos("fbl40", -40, 39));
 			posList.add(new Pos("fbl30", -30, 39));
 			posList.add(new Pos("fbl20", -20, 39));
 			posList.add(new Pos("fbl10", -10, 39));
 			posList.add(new Pos("fb0", 0, 39));
 			posList.add(new Pos("fbr10", 10, 39));
 			posList.add(new Pos("fbr20", 20, 39));
 			posList.add(new Pos("fbr30", 30, 39));
 			posList.add(new Pos("fbr40", 40, 39));
 			posList.add(new Pos("fbr50", 50, 39));
 			
 			posList.add(new Pos("flt30", -57.5, 30));
 			posList.add(new Pos("flt20", -57.5, 20));
 			posList.add(new Pos("flt10", -57.5, 10));
 			posList.add(new Pos("fl0", -57.5, 0));
 			posList.add(new Pos("flb10", -57.5, -10));
 			posList.add(new Pos("flb20", -57.5, -20));
 			posList.add(new Pos("flb30", -57.5, -30));
 			
 			
 			posList.add(new Pos("flt", -52.5, -34));
 			posList.add(new Pos("fct", 0, -34));
 			posList.add(new Pos("frt", 52.5, -34));
 			posList.add(new Pos("flb", -52.5, 34));
 			posList.add(new Pos("fcb", 0, 34));
 			posList.add(new Pos("frb", 52.5, 34));
 			
 			posList.add(new Pos("fplt", -36, -20.16));
 			posList.add(new Pos("fplc", -36, 0));
 			posList.add(new Pos("fplb", -36, 20.16));
 			posList.add(new Pos("fglt", -52.5, -7.01));
 			posList.add(new Pos("fglb", -52.5, 7.01));
 			
 			posList.add(new Pos("fprt", 36, -20.16));
 			posList.add(new Pos("fprc", 36, 0));
 			posList.add(new Pos("fprb", 36, 20.16));
 			posList.add(new Pos("fgrt", 52.5, -7.01));
 			posList.add(new Pos("fgrb", 52.5, 7.01));
 			
 			posList.add(new Pos("fc", 0, 0));
 			
 			posList.add(new Pos("gl", -52.5, 0));
 			posList.add(new Pos("gr", 52.5, 0));
 			
 		}
 		
 		else {
 			posList.add(new Pos("ftl50", 50, 39));
 			posList.add(new Pos("ftl40", 40, 39));
 			posList.add(new Pos("ftl30", 30, 39));
 			posList.add(new Pos("ftl20", 20, 39));
 			posList.add(new Pos("ftl10", 10, 39));
 			posList.add(new Pos("ft0", 0, 39));
 			posList.add(new Pos("ftr10", -10, 39));
 			posList.add(new Pos("ftr20", -20, 39));
 			posList.add(new Pos("ftr30", -30, 39));
 			posList.add(new Pos("ftr40", -40, 39));
 			posList.add(new Pos("ftr50", -50, 39));
 			
 			posList.add(new Pos("frt30", -57.5, 30));
 			posList.add(new Pos("frt20", -57.5, 20));
 			posList.add(new Pos("frt10", -57.5, 10));
 			posList.add(new Pos("fr0", -57.5, 0));
 			posList.add(new Pos("frb10", -57.5, -10));
 			posList.add(new Pos("frt20", -57.5, -20));
 			posList.add(new Pos("frt30", -57.5, -30));
 			
 			posList.add(new Pos("fbl50", 50, -39));
 			posList.add(new Pos("fbl40", 40, -39));
 			posList.add(new Pos("fbl30", 30, -39));
 			posList.add(new Pos("fbl20", 20, -39));
 			posList.add(new Pos("fbl10", 10, -39));
 			posList.add(new Pos("fb0", 0, -39));
 			posList.add(new Pos("fbr10", -10, -39));
 			posList.add(new Pos("fbr20", -20, -39));
 			posList.add(new Pos("fbr30", -30, -39));
 			posList.add(new Pos("fbr40", -40, -39));
 			posList.add(new Pos("fbr50", -50, -39));
 			
 			posList.add(new Pos("flt30", 57.5, -30));
 			posList.add(new Pos("flt20", 57.5, -20));
 			posList.add(new Pos("flt10", 57.5, -10));
 			posList.add(new Pos("fl0", 57.5, 0));
 			posList.add(new Pos("flb10", 57.5, 10));
 			posList.add(new Pos("flb20", 57.5, 20));
 			posList.add(new Pos("flb30", 57.5, 30));
 			
 			
 			posList.add(new Pos("flt", 52.5, 34));
 			posList.add(new Pos("fct", 0, 34));
 			posList.add(new Pos("frt", -52.5, 34));
 			posList.add(new Pos("flb", 52.5, -34));
 			posList.add(new Pos("fcb", 0, -34));
 			posList.add(new Pos("frb", -52.5, -34));
 			
 			posList.add(new Pos("fplt", 36, 20.16));
 			posList.add(new Pos("fplc", 36, 0));
 			posList.add(new Pos("fplb", 36, -20.16));
 			posList.add(new Pos("fglt", 52.5, 7.01));
 			posList.add(new Pos("fglb", 52.5, -7.01));
 			
 			posList.add(new Pos("fprt", -36, 20.16));
 			posList.add(new Pos("fprc", -36, 0));
 			posList.add(new Pos("fprb", -36, -20.16));
 			posList.add(new Pos("fgrt", -52.5, 7.01));
 			posList.add(new Pos("fgrb", -52.5, -7.01));
 			
 			posList.add(new Pos("fc", 0, 0));
 			
 			posList.add(new Pos("gl", 52.5, 0));
 			posList.add(new Pos("gr", -52.5, 0));
 		}
 		
 		
 		
 	}
 	
 	// The Array list that contains all the positions
	public ArrayList<Pos> posList = new ArrayList();
 	
 	
 }
