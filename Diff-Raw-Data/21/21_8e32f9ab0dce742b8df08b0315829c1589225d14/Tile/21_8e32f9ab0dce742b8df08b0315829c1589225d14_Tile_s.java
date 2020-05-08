 /**
  * ???????????????? what characters are on the each tile
  * 
  * @author byrneciar
  * 
  */
 public class Tile implements Occupiable {
     
     public enum Type{
         WALL, ROOM, DOOR, MOVEABLE;
     }
     
     private int x;
     private int y;
 	private Character character; // the character occupying this tile
 	private Type type;
 	private String room = null;
 
 	public Tile(int x, int y, Type t) {
 	    this.x = x;
 	    this.y = y;
 	    this.type = t;
 	}
 	
 	public char getBoardToken() {
 	    switch(type) {
 	    case WALL:
 	        return '0';
 	    
 	    case DOOR:
 	        return 'E';
 	        
 	    case ROOM:
 	        return 'R';
 	    
 	    case MOVEABLE:
 	        if (character == null)
 	            return '.';
 	        return character.getBoardToken();
 	    }
 	    
 		return 0;
 	}
 	
 	public char getType(){
 	    switch(type) {
         case WALL:
             return '0';
         
         case DOOR:
             return 'E';
             
         case ROOM:
             return 'R';
         
         case MOVEABLE:
             return '.';
         }
         return 0;
 	}
 
 	public Character getCharacter() {
 		return character;
 	}
 
 	public void setCharacter(Character character) {
 		this.character = character;
 	}
 	
 	public void setRoom(String name){
 	    this.room = name;
 	}
 	
 	public String getRoom(){
 	    if (room == null)
	        return "0";
 	    else 
 	        return room; 
     }
 	
 	public int getX() {
 	    return x;
 	}
 	
 	public int getY() {
 	    return this.y;
 	}
 }
