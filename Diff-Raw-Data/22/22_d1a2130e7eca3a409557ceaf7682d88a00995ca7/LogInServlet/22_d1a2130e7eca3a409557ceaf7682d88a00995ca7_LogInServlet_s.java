 package Servlets;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 /*
 import org.json.simple.*;
 import org.json.simple.parser.*;
 */
 import Entities.Mentee;
 import Entities.OfyService;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 @SuppressWarnings("serial")
 public class LogInServlet extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		Gson gson = new Gson();
 
 		JsonParser jsonParser = new JsonParser();
 		JsonObject jo = (JsonObject)jsonParser.parse(req.getParameter("json"));
 		
 		Mentee temp = new Mentee(jo.get("email").toString().replace("\"", ""), jo.get("password").toString().replace("\"", ""));
 		Mentee fetched = OfyService.ofy().load().type(Mentee.class).id(temp.getEmail()).get();
 		


		if(fetched != null)
		{
			if(fetched.getPassword().equals(temp.getPassword()))
			{
				//resp.getWriter().println(gson.toJson(fetched).toString());
			}
			else
			{
				fetched.setPassword("null");
				//resp.getWriter().println(gson.toJson(fetched).toString());
			}
		}
 		else
		{
 			fetched = new Mentee("null", "null");
			//resp.getWriter().println(gson.toJson(fetched).toString());
		}
 		
 		resp.setContentType("application/json");
 		resp.getWriter().write(gson.toJson(fetched).toString());
 	}
 }
