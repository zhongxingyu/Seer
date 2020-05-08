 package generalChess;
 
 public class testClass {
 	public static void main (String[] args)
 	{
 		Board b= new Board();
		Bobby BoardLords=new Bobby(false, b);
 	
 		
 		System.out.print(BoardLords.getMoves());
 	}
 }
