 package lab08;
 
 public class Square {
 	private int row;
 	private int column;
 	
     public Square() {
     }
 	
     public Square(int x, int y) {
         this.row = x;
         this.column = y;
     }
     
     public Square(String s) {
    	// TODO Should probably have preconditions to check the string is valid though that may be the job of isValid Move
     	if (s.length() > 1) {
         	this.row = s.charAt(1)-'1';
         	this.column = s.charAt(0)-'a';    	
         }
     }
 
     public int getRow() {
         return row;
     }
 
     public int getColumn() {
         return column;
     }
     
     @Override
 	public String toString() {
     	return "Row: ["+row+"], Column: ["+column+"]";
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if(obj instanceof Square) {
 			Square c = (Square)obj;
 			return (c.row==row && c.column==column);
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		return 9*row+column;
 	}
     
 }
