 package controllers;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.sql.DataSource;
 
 import play.*;
 import play.mvc.*;
 import play.db.*;
 
 import views.html.*;
 
 
 
 public class Application extends Controller {
   
 	private static Connection ds = DB.getConnection();
 	
     public static Result index() {
     	ResultSet rs=null;
     	try {
     		Statement stmt = ds.createStatement() ;
     		String query = "SELECT COUNT(*) FROM users;" ;
     		rs = stmt.executeQuery(query) ;
     		rs.next();
     		return ok(index.render(rs.getString(1),"0"));
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	
     	return ok(index.render("9999","9999"));
     }
     
     public static Result user() {
         return ok(user.render("quentinms", 5, "qms@kth.se"));
     }
     
     public static Result signIn() {
         return ok(signIn.render());
     }
 
     public static Result about(){
     	return ok(about.render());
     }
     
     public static Result createProject(){
     	return ok(createProject.render());
     }
     
     public static Result submitProject(){
     	return ok(createProject.render());
     }
 	
 	  public static Result team(){
     	return ok(team.render());
     }
 	
 	  public static Result project(){
     	return ok(project.render());
     }
 
       public static Result testspage(){
        return ok(project.render());
     }
   
 }
