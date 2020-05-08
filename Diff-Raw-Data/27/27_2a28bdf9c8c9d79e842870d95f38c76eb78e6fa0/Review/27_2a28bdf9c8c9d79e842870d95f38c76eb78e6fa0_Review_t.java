 package main;
 
 public class Review {
 	
	public String b_id;
	public String u_id;
 	public int stars;
 	public int useful;
 	public int funny;
 	public int cool;
 	
	public Review(String b, String u, int s, int us, int f, int c) {
 		b_id = b;
 		u_id = u;
 		stars = s;
 		useful = us;
 		funny = f;
 		cool = c;
 	}
 
 }
