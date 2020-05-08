 package servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import model.Conference;
 import model.ConferencesUsers;
 import model.Location;
 import model.User;
 import model.UserRole;
 
 import utils.ProjConst;
 import utils.TextPrinter;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 
 import daos.ConferenceDao;
 import daos.ConferencesParticipantsDao;
 import daos.ConferencesUsersDao;
 import daos.LocationDao;
 import daos.UserDao;
 
 /**
  * Servlet implementation class UsersServices
  */
 public class ReceptionServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
 	private static final String FILTER_CHANGE = "filterChange";
 	private static final String UPDATE_USER_ARRIVAL = "updateUserArrival";
 	private static final String PRINT = "print";
 	private static final String REMOVE_USER_ARRIVAL = "removeUserArrival";
 	
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ReceptionServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
     
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException, ParseException {
     	
     	String action = request.getParameter("action");
 
 		if (action == null) {
 			//throw new Exception("Critical error!!! Servlet path is NULL.");
 		}
 		else if (action.equals(FILTER_CHANGE)) {
 			getConferences(request, response);
 		}
 		else if(action.equals(UPDATE_USER_ARRIVAL))
 		{
 			userArrivalUpdate(request, response);
 		}
 		else if (action.equals(REMOVE_USER_ARRIVAL))
 		{
 			removeUser(request, response);
 		}
 		else if(action.equals(PRINT))
 		{
 			print(request, response);
 		}
 		else {
 			//throw new Exception("Unknown request");
 		}	
     }
     
     private void getConferences(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
     {
     	String userId = request.getParameter(ProjConst.USER_ID);
     	String filter = request.getParameter("filter");
    
     	User user = UserDao.getInstance().getUserById(Long.parseLong(userId));
     	    	
     	JsonObject jsonObject = new JsonObject();
     	
     	String resultSuccess;
     	String message;
     	
     	List<String> confList = new LinkedList<String>();
     	
     	try 
     	{
     		List<Conference> conferences = ConferencesUsersDao.getInstance().getScopedConferenceByDate(user, filter);
     		for (Conference conf : conferences)
     		{
     			confList.add(conf.getName());	
     		}
     		message = "Conference successfully added";
     		resultSuccess = "true";
     		
     	}
     	catch (Exception e)
     	{
     		message = "Found problem while adding conference";
     		resultSuccess = "false";
     	}
     	
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Gson gson = new Gson();
            	String json;
        		json = gson.toJson(confList);
            		
            	out.write(json);
             out.flush();
         }
          finally {
             out.close();
         }
     }
     
     private void userArrivalUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
     {
     	String confName = request.getParameter(ProjConst.CONF_NAME);
     	String userId = request.getParameter(ProjConst.USER_ID);
     	
     	User user = UserDao.getInstance().getUserById(Long.parseLong(userId));
     	Conference conference = ConferenceDao.getInstance().getConferenceByName(confName);
     	
     	
     	JsonObject jsonObject = new JsonObject();
     	
     	String resultSuccess;
     	String message;
     	
     	try 
     	{
     		//ConferencesUsersDao.getInstance().assignUserToConference(conference, user, Integer.parseInt(userRole));
     		ConferencesParticipantsDao.getInstance().updateUserArrival(conference, user);
     		resultSuccess = "true";
     		message = "User " + user.getName() + " arrived to conference " + conference.getName();
     		
     	}
     	catch (Exception e)
     	{
     		resultSuccess = "false";
     		message = "Failed to update user arrival";
     	}
     	
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try 
         {
             Gson gson = new Gson();
            	String json;
        		jsonObject.addProperty("resultSuccess", resultSuccess);
        		jsonObject.addProperty("message", message);
        		json = gson.toJson(jsonObject);
            	out.write(json);
             out.flush();
        	}
         finally
         {
             out.close();
         }
     }
 
     private void removeUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
     {
     	String confName = request.getParameter(ProjConst.CONF_NAME);
     	String userId = request.getParameter(ProjConst.USER_ID);
     	
     	User user = UserDao.getInstance().getUserById(Long.parseLong(userId));
     	Conference conference = ConferenceDao.getInstance().getConferenceByName(confName);
     	
     	JsonObject jsonObject = new JsonObject();
     	
     	String resultSuccess;
     	String message;
     	try 
     	{
     		//ConferencesUsersDao.getInstance().removeUserFromConference(conference, user);
     		ConferencesParticipantsDao.getInstance().removeUserArrivalFromConference(conference, user);
     		resultSuccess = "true";
     		message = "User " + user.getName() + " removed from arrival list to conference " + conference.getName();
     		
     	}
     	catch (Exception e)
     	{
     		resultSuccess = "false";
     		message = "Failed to update user arrival";
     	}
     	
     	response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try 
         {
             Gson gson = new Gson();
            	String json;
        		jsonObject.addProperty("resultSuccess", resultSuccess);
        		jsonObject.addProperty("message", message);
        		json = gson.toJson(jsonObject);
            	out.write(json);
             out.flush();
        	}
         finally
         {
             out.close();
         }
     }
     
     private void print(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
     {
     	String confName = request.getParameter(ProjConst.CONF_NAME);
     	String userId = request.getParameter(ProjConst.USER_ID);
     	
     	User user = UserDao.getInstance().getUserById(Long.parseLong(userId));
     	Conference conference = ConferenceDao.getInstance().getConferenceByName(confName);
     	
     	
     	JsonObject jsonObject = new JsonObject();
     	
     	String resultSuccess;
     	String message;
     	
     	try 
     	{
     		String[] header = new String[] { "Conference: " + conference.getName() };
     		String[] stringToPrint = new String[] { "Date: " + new Date() +  "\n\tName: " + user.getName() + "\n\tCompany: " + user.getCompany().getName()};
     		
     		TextPrinter tp = new TextPrinter();
     		tp.doPrint(null, stringToPrint, true);
     		
     		resultSuccess = "true";
     		message = "Participant tag is printing";
     		
     	}
     	catch (Exception e)
     	{
     		resultSuccess = "false";	
     		message = e.getMessage();
     	}
     	
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try 
         {
             Gson gson = new Gson();
            	String json;
        		jsonObject.addProperty("resultSuccess", resultSuccess);
        		jsonObject.addProperty("message", message);
        		json = gson.toJson(jsonObject);
            	out.write(json);
             out.flush();
        	}
         finally
         {
             out.close();
         }
     }
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		try {
 			processRequest(request, response);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		try {
 			processRequest(request, response);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
