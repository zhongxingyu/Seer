 package Fabflix;
 
 import java.io.IOException;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import javax.naming.NamingException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public class AddMovie extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     public AddMovie() {
         super();
     }
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		if (Login.kickNonUsers(request, response)) {return;}
 		if (Login.kickNonAdmin(request, response)) {return;}
 		HttpSession session = request.getSession();
 		session.setAttribute("title", "Add Movie");
 		session.removeAttribute("addMovie_err");
 		response.sendRedirect("addmovie.jsp");
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		if (Login.kickNonUsers(request, response)) {return;}
 		if (Login.kickNonAdmin(request, response)) {return;}
 		CallableStatement cst = null;
 		Connection dbcon = null;
 		try {
 			HttpSession session = request.getSession();
 			
 			String title = request.getParameter("title");
 			Integer year = 0;
 			String director = request.getParameter("director");
 			String first_name = request.getParameter("first_name");
 			String last_name = request.getParameter("last_name");
 			String genre = request.getParameter("genre");
 			
 			ArrayList<String> errors = new ArrayList<String>();
 			
 			if (title == null || title.isEmpty()){
 				errors.add("Must provide a title.");
 				session.setAttribute("addMovie_err", errors);
 			}
 			
 			try{
 				year = Integer.valueOf(request.getParameter("year"));
 			}catch(Exception e){
 				errors.add("Provided year is invalid.");
 				session.setAttribute("addMovie_err", errors);
 			}
 			if ( year == 0 ){
 				errors.remove("Provided year is invalid.");
 				errors.add("Must provide a year.");
 				session.setAttribute("addMovie_err", errors);
 			} 	
 			
 			if (director == null || director.isEmpty() ){
 				errors.add("Must provide a director.");
 				session.setAttribute("addMovie_err", errors);
 			} 
 			
 			if (genre == null || genre.isEmpty() ){
 				errors.add("Must provide a genre.");
 				session.setAttribute("addMovie_err", errors);
 			}
 			
 			if(first_name == null || first_name.isEmpty() ){
 				errors.add("Must provide star's first name.");
 				session.setAttribute("addMovie_err", errors);
 			}
 			
 			if(last_name == null || last_name.isEmpty()) {
 				errors.add("Must provide Star's last name");
 				session.setAttribute("addMovie_err", errors);
 			} 
 			
 			if (!errors.isEmpty()) {
 				response.sendRedirect("addmovie.jsp");
 				return;
 			}
				
 			
 			dbcon = Database.openConnection();
 			cst = dbcon.prepareCall("{call add_movie(?, ?, ?, ?, ?, ?)}");
 			cst.setString(1, title);
 			cst.setInt(2, year);
 			cst.setString(3, director);
 			cst.setString(4, first_name);
 			cst.setString(5, last_name);
 			cst.setString(6, genre);
 			cst.execute();
 			
 			session.removeAttribute("addMovie_err");
 			
 			Statement st = dbcon.createStatement();
 			ResultSet rs = st.executeQuery("SELECT * FROM movies WHERE title='" + title + "' AND year='"+year+"' AND director='"+director+"';");
 			if (rs.next()) {
 				Integer id = rs.getInt("id");
 				rs.close();
 				st.close();
 				cst.close();
 				dbcon.close();
 				response.sendRedirect("MovieDetails?id=" + id);
 			}
 			
 		} catch (NamingException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
