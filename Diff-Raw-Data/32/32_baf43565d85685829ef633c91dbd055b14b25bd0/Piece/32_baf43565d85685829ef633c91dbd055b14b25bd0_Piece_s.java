 package ai2010v2;
 
 import java.util.Vector;
 
import sun.io.Converters;
 
 /*
  *    Piece is rectangular piece of furniture.
  *    
  *    x,y_________w
  *    |           |
  *    |           |
  *    h------------
  * 
  * 
  * 
  * 
  * */
 public class Piece {
 	public int x,y;
 	public int w, h;
 	public int id;
 	public char c;
 	public Vector<Operator> Operators;
 	Piece(int ID)
 	{
 		id=ID;
 		Operators = new Vector<Operator>();
 	}
 	public void Draw()
 	{
 		
 		System.out.println("I am piece " + id +", placed on ("+ x +","+y+"), with size of["+w +","+h+"]");
 		System.out.println("My operators are: ");
 		for (Operator tmpOp:Operators)
 		{
 			System.out.println(tmpOp.type);
 			if (tmpOp.reasons!=null)
 				System.out.println(tmpOp.reasons.toString());
 		}
 	}
 	
 	
 	
 }
