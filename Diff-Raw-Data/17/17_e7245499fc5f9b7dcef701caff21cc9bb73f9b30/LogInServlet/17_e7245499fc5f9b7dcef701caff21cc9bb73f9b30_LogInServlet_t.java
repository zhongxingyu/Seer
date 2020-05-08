 package Servlets;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import Entities.Mentee;
 import Entities.OfyService;
 
 import com.google.gson.Gson;
 
 /*
 import com.google.appengine.labs.repackaged.org.json.JSONObject;
 import com.google.gwt.json.client.JSONValue;*/
 
 @SuppressWarnings("serial")
 public class LogInServlet extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		resp.setContentType("text/json");
 		String json = req.getParameter("json");
 		Gson gson = new Gson();
 		Mentee temp = gson.fromJson(json, Mentee.class);
		Mentee fetched = OfyService.ofy().load().type(Mentee.class).id(temp.getPassword()).get();

 		if(fetched != null)
 		{
 			if(fetched.getPassword().equals(temp.getPassword()))
 			{
 				resp.getWriter().println(gson.toJson(fetched).toString());
 				//req.setAttribute("json", gson.toJson(fetched).toString());
 			}
 			else
 			{
				fetched.setPassword("null");
 				resp.getWriter().println(gson.toJson(fetched).toString());
 			}
 		}
 		else
 		{
			fetched = new Mentee("null", "null");
			System.out.println(gson.toJson(fetched).toString());
 			resp.getWriter().println(gson.toJson(fetched).toString());
 		}
 		resp.sendRedirect(req.getHeader("referer"));
 		resp.getWriter().flush();
 		resp.getWriter().close();
 	}
 }
