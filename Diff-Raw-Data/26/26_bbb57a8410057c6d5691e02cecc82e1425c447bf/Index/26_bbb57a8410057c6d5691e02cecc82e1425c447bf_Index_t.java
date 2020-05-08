 package controllers;
 
import play.mvc.Result;
import play.mvc.Results;
 import views.html.index;
 
 public class Index {
 
 	public static Result index() {
		return Results.ok(index.render("Your new application is ready."));
 	}

	public static Result signIn() {
 		return Results.TODO;
 	}

	public static Result signUp() {
 		return Results.TODO;
 	}

 }
