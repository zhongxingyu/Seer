 import java.util.*;
 
 public class Bird {
 	private String name;
 	private ArrayList<String> families;
 	private ArrayList<String> locations;
 	private ArrayList<String> colors;
 	private String size;
 	private String description;
 	
 	public Bird(String name, ArrayList<String> families, ArrayList<String> locations, 
 				ArrayList<String> colors, String size, String description){
 		this.name = name;
 		this.families = families;
 		this.locations = locations;
 		this.colors = colors;
 		this.size = size;
 		this.description = description;
 	}
 
 //--get methods---------------------------
 	public String getName(){
 		return name;
 	}
 	
 	public ArrayList<String> getFamilies(){
 		return families;
 	}
 	
 	public ArrayList<String> getLocations(){
 		return locations;
 	}
 	
 	public ArrayList<String> getColors(){
 		return colors;
 	}
 	
 	public String getSize(){
 		return size;
 	}
 	
 	public String getDescription(){
 		return description;
 	}
 	
 //--set methods---------------------------
 	public void setName(String name){
 		this.name = name;
 	}
 	
 	public void setFamilies(ArrayList<String> families){
 		this.families = families;
 	}
 	
 	public void setLocations(ArrayList<String> locations){
 		this.locations = locations;
 	}
 	
 	public void setColors(ArrayList<String> colors){
 		this.colors = colors;
 	}
 	
 	public void setSize(String size){
 		this.size = size;
 	}
 	
	public void getDescription(String description){
 		this.description = description;
 	}
 }
