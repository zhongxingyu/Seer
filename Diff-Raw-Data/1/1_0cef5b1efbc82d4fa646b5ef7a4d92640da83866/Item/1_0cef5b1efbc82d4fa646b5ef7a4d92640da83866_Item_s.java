 package Shopaholix.database;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 public class Item {
 	public String upc;
 	public String name;
 	public HashSet<String> tags;
 	public ItemRatings ratings;
 	public Item(String upc, String name, ItemRatings ratings){
 		this.upc=upc;
 		this.name=name;
 		this.ratings=ratings;
 		for(String tag:name.split(" ")){
 			tags.add(tag);
 		}
 		
 	}
 	public boolean satisfies(ArrayList<Tag> requiredTags) {
 		for(Tag tag:requiredTags){
 			if(!tags.contains(tag))
 				return false;
 		}
 		return true;
 	}
 	public boolean satisfies(ArrayList<Tag> requiredTags, Tag requiredTag) {
 		 return satisfies(requiredTags)&&tags.contains(requiredTag);
 	}
 
 	public String toString(){
 		return upc+", "+name+", "+ratings.toString();
 	}
 }
