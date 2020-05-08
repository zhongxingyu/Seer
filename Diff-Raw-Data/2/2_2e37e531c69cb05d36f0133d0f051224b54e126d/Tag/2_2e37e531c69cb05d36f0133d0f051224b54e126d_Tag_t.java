 package Shopaholix.database;
 
 import java.io.Serializable;
 
 public class Tag implements Serializable {
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
 	public boolean satisfies(Tag partial) {
		return 0==name.indexOf(partial.name);
 	}
 	
 }
