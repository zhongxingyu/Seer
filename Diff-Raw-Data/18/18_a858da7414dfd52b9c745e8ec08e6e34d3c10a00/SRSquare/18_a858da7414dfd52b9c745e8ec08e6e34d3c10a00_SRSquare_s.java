 /**
  *This class creates the squares for the game board.
  *Each square keeps track of its slide length, the maximum
  *amount of pawns that it can hold, and if it's the home/start square.
  *By default, slideLength = 0, maxPawns = 1, and isHome/isStart = false.
  */
 
 public class SRSquare {
 	
 	public int slideLength;
 	public int maxPawns;
 	public boolean isHome;
 	public boolean isStart;
 
 	//default constructor
 	public SRSquare() {
		slideLength = 0;
		maxPawns = 1;
		isHome = false;
		isStart = false;
 	}
 	
 	public SRSquare(int slideLength, int maxPawns, boolean isHome, boolean isStart) {
 		this.slideLength = slideLength;
 		this.maxPawns = maxPawns;
 		this.isHome = isHome;
 		this.isStart = isStart;
 	}
 	
 	//getters
 	public int getSlideLength() {
 		return slideLength;
 	}
 	
 	public int getMaxPawns() {
 		return maxPawns;
 	}
 	
 	public boolean isHome() {
 		return isHome;
 	}
 	
 	public boolean isStart() {
 		return isStart;
 	}
 	
 	//setters
 	public void setSlideLength(int newLength){
 		this.slideLength = newLength;
 	}
 	
 	public void setMaxPawns(int maxPawns){
 		this.maxPawns = maxPawns;
 	}
 	
 	//**********TEST FUNCTION*************
 	public static void main(String[] args) {
 		
 		//Square using default constructor
 		int newSlideLength = 14;
 		int newMaxPawns = 9;
 		
 		SRSquare defaultSquare = new SRSquare();
 		
 		System.out.print("-Default Square-\nslideLength: " + defaultSquare.getSlideLength() + "\nmaxPawns: " + defaultSquare.getMaxPawns() +
 							  "\nisHome: " + defaultSquare.isHome() + "\nisStart: " + defaultSquare.isStart());
 							  
 		defaultSquare.setSlideLength(newSlideLength);
 		System.out.print("\nslideLength (after setSlideLength): " + defaultSquare.getSlideLength());
 		
 		defaultSquare.setMaxPawns(newMaxPawns);
 		System.out.print("\nmaxPawns (after setMaxPawns): " + defaultSquare.getMaxPawns());
 							  
 		//Square using constructor with 4 parameters
 		int slideLength = 3;
 		int maxPawns = 7;
 		boolean isHome = false;
 		boolean isStart = true;
 		
 		SRSquare square2 = new SRSquare(slideLength, maxPawns, isHome, isStart);
 		
 		System.out.print("\n\n-Square 2-\nslideLength: " + square2.getSlideLength() + "\nmaxPawns: " + square2.getMaxPawns() +
 							  "\nisHome: " + square2.isHome() + "\nisStart: " + square2.isStart());
 		
 		System.exit(0);
 	}
 }
