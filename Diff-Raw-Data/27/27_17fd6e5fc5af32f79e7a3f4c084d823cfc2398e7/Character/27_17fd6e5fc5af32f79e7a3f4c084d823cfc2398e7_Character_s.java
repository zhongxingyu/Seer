 /**
  * Stores info on the character
  * 
  * @author byrneciar
  * 
  */
 
 public class Character implements Card {
     
     
     private String name; 
     
 	public Character(String s) {
 	    this.name = s;
 	}
 	
 	public String getName() {
         return name;
     }
 
 
 	public char getBoardToken() {
 	    
	    if (getName().equals("Kasandra Scarlett")) {
 	        return 'S';
 	    }
	    else  if (getName().equals("Jack Mustard")) {
             return 'M';
         }
	    else  if (getName().equals("Diane White")) {
             return 'W';
         }
	    else if (getName().equals("Jacob Green")) {
             return 'G';
         }
	    else if (getName().equals("Eleanor Peacock")) {
             return 'P';
         }
	    else  if (getName().equals("Victor Plum")) {
             return 'V';
         }
 	    
 	    // TODO need to change, can't switch over a string on java 1.6 like at uni
 	    
 //		switch (getName()) {
 //		case "Kasandra Scarlett":
 //			return 'S';
 //		case "Jack Mustard":
 //			return 'M';
 //		case "Diane White":
 //			return 'W';
 //		case "Jacob Green":
 //			return 'G';
 //		case "Eleanor Peacock":
 //			return 'P';
 //		case "Victor Plum":
 //			return 'V';
 //		}
 		return '?';
 	}
 }
