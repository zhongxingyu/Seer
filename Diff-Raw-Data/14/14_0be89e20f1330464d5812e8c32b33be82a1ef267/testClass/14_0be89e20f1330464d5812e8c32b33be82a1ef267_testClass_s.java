 package generalChess;
 
 public class testClass {
 	public static void main (String[] args)
 	{
 		Board b= new Board();
		Bobby BoardLords=new Bobby(true, b);
 	
 		
 		System.out.print(BoardLords.getMoves());
 	}
 }
