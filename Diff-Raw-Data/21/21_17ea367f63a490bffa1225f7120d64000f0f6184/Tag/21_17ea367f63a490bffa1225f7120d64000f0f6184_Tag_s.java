 package Shopaholix.database;
 
 public class Tag {
 	public String name;
 	public TagRatings ratings;
 	public Tag(String name){
 		this.name=name.toLowerCase();
 	}
 	public String toString(){
 		return name+", "+ratings;
 	}
 	public boolean equals(Object o){
 		return ((Tag)o).name.equals(name);
 		
 	}
 	public int hashCode(){
 		return name.hashCode();
 	}
 }
