 package icloude;
 
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 
 @Path("/api/info")
 public class CloudService {
 	
 	Gson gson = new Gson();
 	
 	private class SimpleMessage {
 		public String text;
 		
 		public SimpleMessage (String inpWord) {
 			text = inpWord;
 		}
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public String getInfoJSON() {
 		SimpleMessage msg = new SimpleMessage("Hello from GET in JSON.");
 		return gson.toJson(msg);
 	}
 
 	
 	@POST
 	@Produces(MediaType.APPLICATION_JSON)
 	//@Consumes(MediaType.TEXT_PLAIN)
	public String postInfoJSON(@FormParam("JSON") String inpJSON) {
 		SimpleMessage msg;
 		if (inpJSON == null){
			msg = new SimpleMessage("No 'JSON' parameter.");
 		} else {
 			try {
 				SimpleMessage fromJSON = gson.fromJson(inpJSON, SimpleMessage.class);
				msg = new SimpleMessage("Recieved 'JSON' parameter containing:'" + inpJSON + "'. After encoding JSON got: '" + fromJSON.text + "'");
 			} catch (JsonSyntaxException e) {
				msg = new SimpleMessage("Recieved 'JSON' parameter containing:'" + inpJSON + "'. But can't decode JSON.");
 			}
 		}
 		return gson.toJson(msg);
 	}
 	
 }
 
 
 //import java.io.IOException;
 //
 //import javax.servlet.http.HttpServlet;
 //import javax.servlet.http.HttpServletRequest;
 //import javax.servlet.http.HttpServletResponse;
 //
 //import storage.Database;
 //
 //import com.google.gson.Gson;
 //
 //@SuppressWarnings("serial")
 //public class CloudService extends HttpServlet {
 //	
 //	private class SimpleMessage {
 //		private String text;
 //		
 //		public SimpleMessage (String inpWord) {
 //			text = inpWord;
 //		}
 //	}
 //	
 //	@Override
 //	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 //		resp.setContentType("text/JSON");
 //		//resp.getWriter().println("Hello from GET");
 //		
 //		SimpleMessage msg = new SimpleMessage("Hello from GET.");
 //		Gson gson = new Gson();
 //		String json = gson.toJson(msg);
 //		
 //		resp.getWriter().println(json);
 //		/*
 //		if (req.getParameterMap().containsKey("testWord")) {
 //			String testWord = req.getParameter("testWord");
 //			resp.getWriter().println("Parameter 'testWord' recieved.");
 //			if (db.contains(testWord)) {
 //				resp.getWriter().println("Word '" + testWord + "' found in database.");
 //			} else {
 //				resp.getWriter().println("Word '" + testWord + "' not found in database.");
 //			}
 //		} else {
 //			resp.getWriter().println("There is no parameter 'testWord'.");
 //		}
 //		*/
 //	}
 //	
 //	@Override
 //	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 //		resp.setContentType("text/JSON");
 //		//resp.getWriter().println("Hello from POST");
 //		
 //		SimpleMessage msg = new SimpleMessage("Hello from POST.");
 //		Gson gson = new Gson();
 //		String json = gson.toJson(msg);
 //		
 //		resp.getWriter().println(json);
 //		
 //		/*
 //		if (req.getParameterMap().containsKey("testJSON")) {
 //			String testWord = req.getParameter("testJSON");
 //			resp.getWriter().println("Parameter 'testWord' recieved.");
 //			db.add(new TestData(testWord));
 //			resp.getWriter().println("Word '" + testWord + "' added to database.");
 //		} else {
 //			resp.getWriter().println("There is no parameter 'testJSON'.");
 //		}
 //		*/
 //	}
 //}
