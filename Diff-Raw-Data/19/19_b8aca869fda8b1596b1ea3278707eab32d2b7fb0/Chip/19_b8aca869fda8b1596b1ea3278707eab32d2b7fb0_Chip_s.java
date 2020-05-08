 /* Chip.java */
 
 package machine; 
 
 import player.*;
 
class Chip{
	static final int BLACK = 0;
	static final int WHITE = 1;
	static final int GREY = 2;
	static final int EMPTY = 3;
 
 	int color;
 	boolean visited = false;
 	int x;
 	int y;
 	                                                      
 
 	Chip(int color){		
 		this.color = color;
 	}
 	
 	Chip(int x, int y, int color){		
 		this.color = color;
 		this.x = x;
 		this.y = y;
 	}
 	void flag(){
 		visited = true;
		System.out.println("Chip at (" + x + " " + y + ") is visited");
 	}
 
 	void unflag(){
		if(visited == false)
			 System.out.println("Chip at (" + x + " " + y + ") isn't visited");
 		visited = false;	
 	}
 	
 	boolean isFlagged(){
 		if(visited == true)
 			return true;
 		else return false;			
 	}
 	
	public int returnColor(){
 		return color;	
 	}
 	int getX(){
 		return x;
 	}
 	int getY(){
 		return y;
 	}
 	void changeColor( int color){
 		this.color = color;			
 	}
 	public String toString()
 	{
 		if(color== BLACK)
 			return "BK";
 		else if(color == WHITE)
 			return "WH";
 		else if(color == GREY)
 			return "--";
 		else
 			return "__";
 		
 	}
 }
