 package assignment2;
 
 public class N implements NInterface{
 	
 	private StringBuffer s;
 	
 	N(int i){
 		s.insert(0, i);
 	}
 	
 	public N init(){
 		s.delete(0, getLength());
 		s.insert(0, 0);
 		return this;
 	}
 	
 	public void addChar(char c){
 		if(s.charAt(0) == 0){
 			s.insert(0, c);
 		}else s.append(c);
 	}
 	
 	public char getChar(int index){
 		return s.charAt(index);
 	}
 	
 	public int getLength(){
 		return s.length();
 	}
 	
 	public N clone(){
 		N copy = new N(0);
 		
 		for(int i = 0; i < getLength(); i++){
 			copy.addChar(getChar(i));
 		}
 		
 		return copy;
 	}
 	
 	public int compareTo(N n){
 		if(getLength() < n.getLength()){
 			return -1;
 		}else if(getLength() > n.getLength()){
 			return 1;
 		}else{
 			
 			for(int i = 0; i < getLength(); i++){
 				if(getChar(i) < n.getChar(i)){
 					return -1;
 				}else if(getChar(i) > n.getChar(i)){
 					return 1;
 				}
 			}
 			return 0;
 		}		
 	}	
 }
