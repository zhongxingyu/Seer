 package controllers;
 
import play.mvc.*;
 import views.html.index;
 
 public class Index {
 
	private static String [] colors = {"FF7F00","A52A2A", "3366FF", "00CC66"};
	
 	public static Result index() {
	  	return Results.ok(index.render("Your new application is ready.", getRandomColor()));
 	}
	
	public static Result signIn(){
 		return Results.TODO;
 	}
	
	public static Result signUp(){
 		return Results.TODO;
 	}
	
	private static String getRandomColor() {
		return colors[((int)(Math.random()*colors.length))];
	}
	
 }
