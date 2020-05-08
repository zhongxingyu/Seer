 package Servlets;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.servlet.http.*;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 import Entities.Mentee;
 import Entities.OfyService;
 import Entities.Search;
 import Entities.SearchRequest;
 import Entities.SearchResult;
 
 	@SuppressWarnings("serial")
 	public class SearchServlet extends HttpServlet {
 		Gson gson = new Gson();
 		
 		boolean TEST = false;
 		public void doPost(HttpServletRequest req, HttpServletResponse resp)
 				throws IOException {		
 						
 			resp.setContentType("text/plain");
 			System.out.println(Arrays.asList(gson.fromJson( req.getParameter("params"), String[].class)).toString());
 			System.out.println(req.getParameter("email"));
 			SearchRequest sr = new SearchRequest( req.getParameter("email") );
 			sr.addParameters( Arrays.asList(gson.fromJson( req.getParameter("params"), String[].class)) );
 			sr.empty = true;
 			
//			if ( req.getParameter("majors") == "true" ){
//				sr.majors = true;
//				sr.empty = false;
//			} if ( req.getParameter("zipcode") == "true" ) {
//				sr.zipcode = true;
//				sr.empty = false;
//			} if ( req.getParameter("interests") == "true" ) {
//				sr.interests = true;
//				sr.empty = false;
//			} if ( req.getParameter("currentClasses") == "true" ) {
//				sr.classes = true;
//				sr.empty = false;				
//			} if ( req.getParameter("pastClasses") == "true" ){
//				sr.majors = true;
//				sr.empty = false;
//			} if ( req.getParameter("classification") == "true" ){
//				sr.majors = true;
//				sr.empty = false;
//			}
			
 			SearchResult result = new SearchResult();
 			String password = "password";
 			if (TEST) {
 				Mentee bob = new Mentee("testEasy@test.com", password.hashCode());
 				bob.setFirstName("Bob").setLastName("Smith")
 								.setMajors(Arrays.asList("ECE", "CS"))
 								.setInterests(Arrays.asList("Computers, Baseball"))
 								.setZipCode(78705);
 				
 				Mentee joe = new Mentee("testEasyMentor@test.com", password.hashCode());
 				joe.setFirstName("Joe").setLastName("Doe")
 								.setMajor("ECE")
 								.setZipCode(78705)
 								.setClassification("Senior");
 				
 				Mentee steve = new Mentee("testEasyMentor2@test.com", password.hashCode());
 				steve.setFirstName("Steve").setLastName("Smith").setMajor("ECE").setZipCode(70000);
 				
 				result.setMatches(Arrays.asList(bob, joe, steve));
 				
 			} else {			
 				result = Search.performSearch( sr );
 			}
 			
 			System.out.println("Resp: " + result.toJson() );
 			resp.getWriter().write( result.toJson() );
 		}
 		
 		public void doGet(HttpServletRequest req, HttpServletResponse resp)
 				throws IOException {			
 			doPost( req, resp );
 		}
 	}
 
