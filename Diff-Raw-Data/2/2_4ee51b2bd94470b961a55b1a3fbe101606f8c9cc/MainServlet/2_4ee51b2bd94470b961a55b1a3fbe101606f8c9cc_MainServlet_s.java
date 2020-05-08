 package AndroidWebService;
 
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /*
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 */
 
 public class MainServlet extends HttpServlet {
     private static final long serialVersionUID = 1L;
     
     public static final int kindVolQuery = 1, //id = name
     		kindEventVolQuery = 2, //id = volunteer id
     		kindEventQuery = 3, //id = event id
     		
     		kindFindQuery = 4, //id = null (returns all events)
     		kindInterestQuery = 5, //id = null (returns all interests)
     		kindEventInterestQuery = 6; //id = event id
  
     public MainServlet() {
         super();
     }
  
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         
     	doPost(request,response);
     }
 
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  
         PrintWriter out = response.getWriter();
         response.setContentType("text/html");
         response.setHeader("Cache-control", "no-cache, no-store");
         response.setHeader("Pragma", "no-cache");
         response.setHeader("Expires", "-1");
         response.setHeader("Access-Control-Allow-Origin", "*");
         response.setHeader("Access-Control-Allow-Methods", "GET,POST");
         response.setHeader("Access-Control-Allow-Headers", "Content-Type");
         response.setHeader("Access-Control-Max-Age", "86400");
                 
         String id = null;        
         int kind = 0;
         
        boolean test = true;
         if(test){
             kind = kindFindQuery;
         	id = "4";
         
             //out.println( "testing" );
             //return;
         }
         else {
         
         	//get query kind
         	kind = Integer.parseInt( request.getParameter("kind").trim() );
         	
         	if( kind == kindVolQuery || kind == kindEventVolQuery || kind == kindEventQuery ||
         			kind == kindFindQuery || kind == kindInterestQuery || kind == kindEventInterestQuery) {}
         	else {
         		out.println( "error bad kind" );
         		return;
         	}
         	
         	id = request.getParameter("id").trim();
         }
         
         MySQLQuery query = new MySQLQuery();
         out.println( query.getResultString(kind, id) );
         out.close();
         
         //DO NOT USE GSON AND JSON
 //        //get information
 //        MySQLQuery getter = new MySQLQuery();
 //        EventData event = getter.getEvent(data);
 // 
 //        //if invalid
 //        if(event.getEventID() == null){
 //            JsonObject myObj = new JsonObject();
 //            myObj.addProperty("success", false);
 //            out.println(myObj.toString());
 //        }
 // 
 //        else {
 //            Gson gson = new Gson(); 
 //            //create json from EventData object
 //            JsonElement eventObj = gson.toJsonTree(event);
 // 
 //            //create a new JSON object
 //            JsonObject myObj = new JsonObject();
 //            //add property as success
 //            myObj.addProperty("success", true);
 //            //add the event object
 //            myObj.add("eventInfo", eventObj);
 //            //convert the JSON to string and send back
 //            out.println(myObj.toString());
 //        }
 //        out.close();
     }
 }
