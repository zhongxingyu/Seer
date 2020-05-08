 package controllers;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.security.SecureRandom;
 import java.math.BigInteger;
 import play.data.DynamicForm;
 import play.data.Form;
 import play.db.DB;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.about;
 import views.html.createProject;
 import views.html.index;
 import views.html.project;
 import views.html.register;
 import views.html.signIn;
 import views.html.team;
 import views.html.testspage;
 import views.html.user;
 import utils.*;
 import utils.crypto;
 
 public class Application extends Controller {
 
 	private static Connection ds = DB.getConnection();
 
 	public static Result index() {
 		ResultSet rs = null;
 		String Nbuser = "APOCALYPSE";
 		String Nbproject = "APOCALYPSE";
 		try {
 			Statement stmt = ds.createStatement();
 			String query = "SELECT COUNT(*) FROM users;";
 			rs = stmt.executeQuery(query);
 			rs.next();
 			Nbuser = rs.getString(1);
 			query = "SELECT COUNT(*) FROM projects;";
 			rs = stmt.executeQuery(query);
 			rs.next();
 			Nbproject = rs.getString(1);
 			return ok(index.render(isItConnected(),Nbuser, Nbproject));
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return ok(index.render(isItConnected(),"9999", "9999"));
 	}
 
 	public static Result sayHello() {
 		DynamicForm requestData = Form.form().bindFromRequest();
 		//
 		String name = requestData.get("form");
 		ResultSet rs = null;
 		Statement stmt;
 		if (name.contains("forma")) {
 
 			name = requestData.get("username");
 
 			if ((name.compareTo("") == 0) || (name.compareTo("username") == 0)) {
 				return ok(register.render(isItConnected(),"username missing"));
 			} else if (name.length() > 31) {
 				return ok(register.render(isItConnected(),"username too long"));
 			}
 			try {
 
 				stmt = ds.createStatement();
 
 				// TODO Auto-generated catch block
 
 				String query = "SELECT COUNT(*) FROM users_profile WHERE username='"
 						+ name + "';";
 				rs = stmt.executeQuery(query);
 				rs.next();
 
 				if (Integer.parseInt(rs.getString(1)) > 0) {
 					return ok(register.render(isItConnected(),"username already used"));
 				}
 
 				name = requestData.get("email");
 
 				query = "SELECT COUNT(*) FROM users WHERE email='" + name
 						+ "';";
 				rs = stmt.executeQuery(query);
 				rs.next();
 				if (Integer.parseInt(rs.getString(1)) > 0) {
 					return ok(register.render(isItConnected(),"already used"));
 				}
 				if ((name.compareTo("") == 0) || (name.compareTo("email") == 0)) {
 					return ok(register.render(isItConnected(),"email missing"));
 				}
 				if (!((name.contains("@")) && (name.contains(".")))) {
 					return ok(register.render(isItConnected(),"email incorrect"));
 				}
 				name = requestData.get("password");
 				String nameb = requestData.get("verifpassword");
 
 				if (name.length() < 7) {
 					return ok(register
 							.render(isItConnected(),"email incorrect must be 8 characters or more"));
 				}
 
 				if (name.compareTo(nameb) != 0) {
 					return ok(register.render(isItConnected(),"the two passwords don't match"));
 				}
 
 				SessionIdentifierGenerator MyUIDGene = new SessionIdentifierGenerator();
 				String myUID = MyUIDGene.nextSessionId();
 
 				query = "INSERT INTO users (uid,email,pass,register_date,language,active) VALUES ('"
 						+ myUID
 						+ "','"
 						+ requestData.get("email")
 						+ "','"
 						+ new utils.crypto()
 								.getMD5(requestData.get("password"))
 						+ "', NOW(),'EN','1');";
 				System.out.println(query);
 				stmt.executeUpdate(query);
 				System.out.println("yay");
 				query = "INSERT INTO users_profile (uid,username,usertag) VALUES ('"
 						+ myUID
 						+ "','"
 						+ requestData.get("username")
 						+ "','"
 						+ myUID.substring(0, 7) + "');";
 				stmt.executeUpdate(query);
 				query = "INSERT INTO  users_profile_data (uid) VALUES ('"
 						+ myUID + "');";
 				stmt.executeUpdate(query);
 				return ok(register.render(isItConnected(),"Succces ! You can now log in !!"));
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			return ok(register.render(isItConnected(),"SQL CHAOS"));
 		} else if (name.contains("formb")) {
 			try {
 				name = requestData.get("username");
 				String password = new crypto().getMD5(requestData
 						.get("password"));
 
 				stmt = ds.createStatement();
 
 				String query = "SELECT  * FROM users, users_profile WHERE users_profile.username='"
 						+ name
 						+ "' and users.pass='"
 						+ password
 						+ "' and users.uid=users_profile.uid;";
 				rs = stmt.executeQuery(query);
 				if (rs.next()) {
 
 					session("uid", rs.getString("uid"));
 					response().setCookie("uid", rs.getString("uid"));
 					String Nbuser = "APOCALYPSE";
 					String Nbproject = "APOCALYPSE";
 					try {
 						stmt = ds.createStatement();
 						query = "SELECT COUNT(*) FROM users;";
 						rs = stmt.executeQuery(query);
 						rs.next();
 						Nbuser = rs.getString(1);
 						query = "SELECT COUNT(*) FROM projects;";
 						rs = stmt.executeQuery(query);
 						rs.next();
 						Nbproject = rs.getString(1);
 						return ok(index.render(isItConnected(),Nbuser, Nbproject));
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 				} else {
 					return ok(register.render(isItConnected(),"Connection failed"));
 
 				}
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return ok(register.render(isItConnected(),"Critical Error"));
 		} else {
 			return ok(register.render(isItConnected(),"An Error Occured"));
 		}
 
 		// do what you want with the name variable
 	}
 
 	public static Result register() {
 		return ok(register.render(isItConnected(),""));
 	}
 
 	public static Result user() {
 		return ok(user.render(isItConnected(), 5, "qms@kth.se"));
 	}
 
 	public static Result signIn() {
 		return ok(signIn.render(isItConnected()));
 	}
 
 	public static Result about() {
 		return ok(about.render(isItConnected()));
 	}
 
 	public static Result createProject() {
 		return ok(createProject.render(isItConnected()));
 	}
 
 	public static Result submitProject() {
 		
 		DynamicForm requestData = Form.form().bindFromRequest();
 		String name = requestData.get("projectName");
 		String sDescription = requestData.get("projectShortDescription");
 		String description = requestData.get("projectLongDescription");
 		String projectURL = requestData.get("projectURL");
 		System.out.println(requestData.data());
 		
 		ResultSet rs = null;
 		Statement stmt;
 		
 		
 		try {
 			stmt = ds.createStatement();
 	
 			String query = "INSERT INTO  projects (uid, project_name, project_tag, project_quick_description) VALUES ('"
 					+name+"','"+ projectURL + "','"+projectURL+"','"+sDescription+"');";
 			stmt.executeUpdate(query);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		return redirect("/project/"+projectURL);
 		return ok(createProject.render(isItConnected()));
 	}
 
 	public static Result team() {
 		return ok(team.render(isItConnected()));
 	}
 	
 	public static Result project(String id) {
 		if(id == ""){
 			return redirect("/create-project/");
 		} else {
 			
 			ResultSet rs = null;
 			Statement stmt;
 			
 			try {
 				stmt = ds.createStatement();
 		
 				String query = "SELECT  * FROM projects WHERE projects.project_tag='"
 						+ id+"';";
 				rs = stmt.executeQuery(query);
 				
 				if(rs.next()){
 					name = rs.getString("project_name");
 					return ok(project.render(name, id, id)); 
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			return redirect("/create-project/"); 
 		}
 	}
 	
 	public static Result project(String title, String description, String sDescription) {
 		return ok(project.render(title, description, sDescription));
 		
 	public static Result project() {
 		return ok(project.render(isItConnected()));
 	}
 
 	public static Result testspage() {
 		return ok(testspage.render());
 	}
 
 	public static String isItConnected() {
 		String uid = session("uid");
 
 		if (uid != null) {
 			try {
 				ResultSet rs = null;
 				Statement stmt =ds.createStatement();
 				String query = "SELECT username FROM users_profile WHERE uid ='"
 						+ uid + "';";
 
 				rs = stmt.executeQuery(query);
 
 				rs.next();
 				return rs.getString("username");
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 
 				e.printStackTrace();
 				return "";
 			}
 		} else {
 			return "";
 		}
 	}
 
 }
