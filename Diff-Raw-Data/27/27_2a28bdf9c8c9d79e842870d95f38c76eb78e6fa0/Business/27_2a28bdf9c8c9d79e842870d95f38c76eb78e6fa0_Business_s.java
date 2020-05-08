 package main;
 
 import java.util.ArrayList;
 
 public class Business {
 	
	public int id;
 	public String name;
 	public String address;
 	public String city;
 	public String state;
 	public float lat;
 	public float lon;
 	public int stars;
 	public String photo;
 	public ArrayList<Category> categories = new ArrayList<Category>();
 	public ArrayList<Review> reviews = new ArrayList<Review>();
 	
	public Business (int i, String n, String a, String c, String s, float la, float lo, int st, String p) {
 		id = i;
 		name = n;
 		address = a;
 		city = c;
 		state = s;
 		lat = la;
 		lon = lo;
 		stars = st;
 		photo = p;
 	}
 	
 	public void addCategory(Category c) {
 		if (!categories.contains(c)) {
 			categories.add(c);
 		}
 	}
 	
 	public void addReview(Review r) {
 		if (!reviews.contains(r)) {
 			reviews.add((r));
 		}
 	}
 
 }
