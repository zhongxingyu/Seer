 import java.util.*;
 
 public class State
 {
 	static final int width = 7;
 	static final int height = 6;
	
 	static final long fullBoard = Long.parseLong("3FFFFFFFFFF", 16);
 	
 	public State(long white,long red, boolean whiteTurn) {
 		this.white = white;
 		this.red = red;
 		this.whiteTurn = whiteTurn;
 	}
 	
 	public boolean whiteTurn;
 	long white;
 	long red;
 	
 	long getOccupied() {
 		return white | red;
 	}
 	
 	int getRow(int i) {
 		Long ret = (getOccupied() >> width * i ) & (long)0x7F;
 		return ret.intValue();
 		// mask out other then the intended row.
 		// with the first 6 bits set.
 	}
 	boolean occupied(int i, int j) {
 		return ((getRow(i) >> j) & 1) == 1;
 	}
 	int getTop(int i) {
 		long occupied = getOccupied();
 		
 		for (int x = 0; x < height; x++) {
 			if (((occupied >> i) & 1)  == 0)
 				return x;
 			occupied = occupied >> width;
 		}
 		return -1;
 	}
 	
 	public void addMove(int dropedInColumn, boolean white) {
 		int top = getTop(dropedInColumn-1);
		//New top 
		top += width;
 		if (white)
			this.white += (1 << top);
 		else
			this.red += (1 << top);
 	}
 	
 	public int terminalState() {
 		if (getOccupied() == fullBoard) // All Posistions are Occupied
 			return 3;
 		if (colorWin(red))
 			return 2;
 		if (colorWin(white))
 			return 1;
 		return 0;
 	}
 	
 	boolean colorWin(long color) {
 		// Check if there is a 4 consecutive piese in diagonal pattern.
 		// example: 001010100
 		//          001		
 		//           010
 		//            100
 		// For both left and right leaned pieses.
 		// color & ((color >> 5) & ~1) & ((color >> 9) & ~3) & ((color >> 12) & ~7)
 		// example: 100010001
 		//             100		
 		//            010
 		//           001
 		// color & ((color >> 7) & ~7) & ((color >> 15) & ~3) & ((color >> 24) & ~3)
 		long tColor = color;
 		long mask = 127;
 		for (int i = 0; i < 3; i++){
 			//Diagonal left
 			if (((tColor & (tColor >> 8) & (tColor >> 16)  & (tColor >> 24)) & 7) > 0)
 				return true;
 			//System.out.println(tColor & (tColor >> 6) & (tColor >> 12) & (tColor >> 18));
 			//Diagonal right
 			if (((tColor & (tColor >> 6) & (tColor >> 12) & (tColor >> 18)) & 112) > 0)
 				return true;
 			//Column 
 			if (((tColor & (tColor >> 7) & (tColor >> 14) & (tColor >> 21)) & mask) > 0)
 				return true;
 				
 			tColor = tColor >> width;
 		}
 		
 		tColor = color;
 		for (int i = 0; i < width; i++){
 			//Row
 			if ((tColor & 0xF) == 0xF || 
 			    (tColor & 0x1E) == 0x1E || 
 			    (tColor & 0x3C) == 0x3C ||
 			    (tColor & 0x78) == 0x78)
 				return true;
 			tColor = tColor >> width;
 		}
 		return false;
 	}
 	
 	public boolean RedWin() {
 		return colorWin(red);
 	}
 	
 	public boolean WhiteWin() {
 		return colorWin(white);
 	}
 	
 	///
 	/// White has the most significant four bits and red has the least significant four bits.
 	///
 	ArrayList<Integer> getCombos()
 	{
 		ArrayList<Integer> combos = new ArrayList<Integer>();
 		
 		//long mask = 127;
 		//for (int i = 0; i < 3; i++){
 		//	//Diagonal left
 		//	if (((tColor & (tColor >> 8) & (tColor >> 16)  & (tColor >> 24)) & 7) > 0)
 		//		return true;
 		//	//System.out.println(tColor & (tColor >> 6) & (tColor >> 12) & (tColor >> 18));
 		//	//Diagonal right
 		//	if (((tColor & (tColor >> 6) & (tColor >> 12) & (tColor >> 18)) & 112) > 0)
 		//		return true;
 		//	//Column 
 		//	if (((tColor & (tColor >> 7) & (tColor >> 14) & (tColor >> 21)) & mask) > 0)
 		//		return true;
 		//		
 		//	tColor = tColor >> width;
 		//}
 
 		long r = red;
 		long w = white;
 		for (int i = 0; i < height; i++){
 			if (((w & 0xF) | (r & 0xF)) > 0 && (w & 0xF) == 0 || (r & 0xF) == 0)
 				combos.add((int)(((w & 0xF) << 4)  | (r & 0xF)));
 
 			if (((w & 0x1E) | (r & 0x1E)) > 0 && (w & 0x1E) == 0 || (r & 0x1E) == 0)
 				combos.add((int)(((w & 0x1E) << 3) | ((r & 0x1E) >> 1)));
 
 			if (((w & 0x3C) | (r & 0x3C)) > 0 && (w & 0x3C) == 0 || (r & 0x3C) == 0)
 				combos.add((int)(((w & 0x3C) << 2) |  ((r & 0x3C) >> 2)));
 
 			if (((w & 0x3C) | (r & 0x3C)) > 0 && (w & 0x3C) == 0 || (r & 0x3C) == 0)
 				combos.add((int)(((w & 0x78) << 1) | ((r & 0x78) >> 3)));
 
 			r = r >> width;
 			w = w >> width;
 		}
 		r = red;
 		w = white;
 		long line = 0;
 		for(int i = 0; i< 3;i++){           
             for(int k = 0; k<4;k++){        //this is for left leaning sets of four
                 line = (r & 1) + ((r >> 7) & 2) + ((r >> 14) & 4) + ((r >> 21) & 8);
                 line += ((w & 1) + ((w >> 7) & 2) + ((r >> 14) & 4) + ((r >> 21) & 8))<<4;              
                 r = r >> 1;
                 w = w >> 1;
                 if(line != 0 && ((line & 0xF) == 0 || line <= 0xF)) combos.add((int)line);
             }
             r = r >> 3;
             w = w >> 3;               
 		}
 		r = red;
 		w = white;
 		for(int i = 0; i< 3;i++){           
             for(int k = 0; k<4;k++){        //this is for right leaning sets of four
                 line = (r & 8) + ((r >> 7) & 4) + ((r >> 14) & 2) + ((r >> 21) & 1);
                 line += (  (w & 8) + ((w >> 7) & 4) + ((r >> 14) & 2) + ((r >> 21) & 1))<<4;
                 r = r >> 1;
                 w = w >> 1;
                 if(line != 0 && ((line & 0xF) == 0 || line <= 0xF)) combos.add((int)line);
             }
 		}
 		r = red;
 		w = white;
 		for(int i = 0; i< 21;i++) {
             line = (r & 1) + ((r >> 6) & 2) + ((r >> 12) & 4) + ((r >> 18) & 8);
             line += (w & 1) + ((w >> 6) & 2) + ((w >> 12) & 4) + ((w >> 18) & 8);
             r = r >> 1;
             w = w >> 1;
             if(line != 0 && ((line & 0xF) == 0 || line <= 0xF)) combos.add((int)line);
 		}
 		return combos;
 	}
 
 	ArrayList<State> legalMoves() {
 		ArrayList<State> l = new ArrayList<State>();
 		for (int i = 0; i < width; i++)
 		{
 			int top = getTop(i);
 			if (top != -1){
 				long insert = (1 << ((top*width)+i));
 				if (whiteTurn) 
 					l.add(new State(white + insert,red,!whiteTurn));
 				else 
 					l.add(new State(white,red + insert,!whiteTurn));	
 			}
 			else{
 				l.add(null);// just so there are 7 columns.
 			}
 		}
 		return l;
 	}	
 	
 	@Override
     public int hashCode(){
 		Long ret = (long)19 * white + (long)31 * red;
         return ret.intValue();
     }	
 }
