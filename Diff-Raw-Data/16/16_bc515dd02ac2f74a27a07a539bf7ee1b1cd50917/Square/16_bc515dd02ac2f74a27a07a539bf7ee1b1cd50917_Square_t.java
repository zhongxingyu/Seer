 package ChiniMess;
 
 public class Square {
 
 	public int col, row;
 	
 	
 	
 
     public Square(String koordinaten){
 		
 		char [] chararray = koordinaten.toCharArray();
 		
 		switch(chararray[0]){
 		
 			case 'a' : col = 0; break; 
 			case 'b' : col = 1; break; 
 			case 'c' : col = 2; break; 
 			case 'd' : col = 3; break; 
 			case 'e' : col = 4; break; 
 			
 			default : col = -1; break; 
 		}
 		switch(chararray[1]){
 		
 			case '1' : row = 0; break; 
 			case '2' : row = 1; break; 
 			case '3' : row = 2; break; 
 			case '4' : row = 3; break; 
 			case '5' : row = 4; break; 
 			case '6' : row = 5; break; 
 			
 			default : row = -1; break; 
 		}
 	}
 	
 	public Square(int col, int row){
 		
 		this.col = col;
 		this.row = row;
 		
 	}
 
 	public String toString(){
 		
		char col;
		
		switch(this.col){
		
			
			case 0 : col = 'a'; break; 
			case 1 : col = 'b'; break; 
			case 2 : col = 'c'; break; 
			case 3 : col = 'd'; break; 
			case 4 : col = 'e'; break; 
			
			default : col = 'z'; break; 
		}
		
		return col+""+(row+1);
 	}
 	
 	@Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + col;
         result = prime * result + row;
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Square other = (Square) obj;
         if (col != other.col)
             return false;
         if (row != other.row)
             return false;
         return true;
     }
 }
