 package servlets;
 
 import helpers.ProjConst;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import model.Conference;
 import model.ConferencesUsers;
 import model.Location;
 import model.User;
 import model.UserAttendanceStatus;
 import model.UserRole;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 
 import daos.ConferenceDao;
 import daos.ConferencesUsersDao;
 import daos.LocationDao;
 import daos.UserDao;
 
 /**
  * Servlet implementation class UsersServices
  */
 public class ConferenceServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
 	private static final String ADD_CONF = "add";
 	private static final String DELETE_CONF = "delete";
 	private static final String EDIT_CONF = "edit";
 	private static final String SEND_INVITATION_CONF = "sendInvitation";
 	private static final String REMOVE_USER = "removeUser";
 	private static final String ASSING_USER = "assignUser";
 	private static final String CONF_NAME_VALIDATION = "validation";
 	private static final String UPDATE_USER_ATTENDANCE = "updateUserAttendance";
 	
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ConferenceServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
     
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException, ParseException {
     	
     	String action = request.getParameter("action");
 
 		if (action == null) {
 			//throw new Exception("Critical error!!! Servlet path is NULL.");
 		}
 		else if (action.equals(ADD_CONF)) {
 			addConference(request, response);
 		}
 		else if (action.equals(DELETE_CONF)) {
 			deleteConference(request, response);
 		}
 		else if (action.equals(EDIT_CONF)) {
 			editConference(request, response);
 		}
 		else if (action.equals(CONF_NAME_VALIDATION)) {
 			conferenceNameValidation(request, response);
 		}
 		else if (action.equals(SEND_INVITATION_CONF)) {
 			sendInvitationToUsers(request, response);
 		}
 		else if (action.equals(ASSING_USER)) {
 			assignUsers(request, response);
 		}
 		else if (action.equals(REMOVE_USER)) {
 			removeUser(request, response);
 		}
 		else if (action.equals(UPDATE_USER_ATTENDANCE)) {
 			updateAttendance(request, response);
 		}
 		else {
 			//throw new Exception("Unknown request");
 		}	
     }
     
     private void addConference(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
     {
     	String confName = request.getParameter(ProjConst.CONF_NAME);
     	String desc = request.getParameter(ProjConst.CONF_DESC);
     	String location = request.getParameter(ProjConst.CONF_LOCATION);
     	
 	    DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
     			  
     	Date startDate = (Date)formatter.parse(request.getParameter(ProjConst.CONF_START_DATE));
     	Date endDate = (Date)formatter.parse(request.getParameter(ProjConst.CONF_END_DATE));
     	
    	Location locationInstance = LocationDao.getInstance().getLocationByName(location);
     	    	
     	JsonObject jsonObject = new JsonObject();
     	
     	String resultSuccess;
     	String message;
     	try 
     	{
     		ConferenceDao.getInstance().addNewConference(new Conference(confName, locationInstance, desc, startDate, endDate));
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
            	if (ConferenceDao.getInstance().isConferenceNameExists(confName))	
            	{
            		jsonObject.addProperty("resultSuccess", resultSuccess);
            		jsonObject.addProperty("message", message);
            		json = gson.toJson(jsonObject);
            	}
            	else
            	{
            		jsonObject.addProperty("resultSuccess", "false");
            		jsonObject.addProperty("message", "Failed to add conference");
            		json = gson.toJson(jsonObject);
            	}
            	out.write(json);
             out.flush();
         }
          finally {
             out.close();
         }
     }
     
     private void editConference(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
     {
     	String confNameBeforeEdit = request.getParameter(ProjConst.CONF_NAME_BEFORE_EDIT);
     	Conference origConf = ConferenceDao.getInstance().getConferenceByName(confNameBeforeEdit);
     	
     	String confName = request.getParameter(ProjConst.CONF_NAME);
     	String desc = request.getParameter(ProjConst.CONF_DESC);
     	String location = request.getParameter(ProjConst.CONF_LOCATION);
     	
 	    DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
     			  
     	Date startDate = (Date)formatter.parse(request.getParameter(ProjConst.CONF_START_DATE));
     	Date endDate = (Date)formatter.parse(request.getParameter(ProjConst.CONF_END_DATE));
     	
     	Location locationInstance = LocationDao.getInstance().getLocationById(location);  	
     	
     	origConf.setName(confName).setDescription(desc).setLocation(locationInstance).setStartDate(startDate).setEndDate(endDate);
     	
     	JsonObject jsonObject = new JsonObject();
     	
     	String resultSuccess;
     	String message;
     	try 
     	{
     		
     		ConferenceDao.getInstance().updateConference(origConf);
     		message = "Conference successfully edited";
     		resultSuccess = "true";
     		
     	}
     	catch (Exception e)
     	{
     		message = "Found problem while editing conference";
     		resultSuccess = "false";
     	}
     	
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Gson gson = new Gson();
            	String json;
            	if (ConferenceDao.getInstance().isConferenceNameExists(confName))	
            	{
            		jsonObject.addProperty("resultSuccess", resultSuccess);
            		jsonObject.addProperty("message", message);
            		json = gson.toJson(jsonObject);
            	}
            	else
            	{
            		jsonObject.addProperty("resultSuccess", "false");
            		jsonObject.addProperty("message", "Failed to edit conference");
            		json = gson.toJson(jsonObject);
            	}
            	out.write(json);
             out.flush();
         }
          finally {
             out.close();
         }
     }
     
     
     private void removeUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
     {
     	String confName = request.getParameter(ProjConst.CONF_NAME);
     	Conference conference = ConferenceDao.getInstance().getConferenceByName(confName);
     	
     	String userName = request.getParameter("userNames");
     	String[] userNames = userName.split(",");
     	
     	
     	List<User> users = new LinkedList<User>();
     	
     	for (String user : userNames)
     	{
     		if (user != null)
     			users.add(UserDao.getInstance().getUserByUserName(user));
     	}
     	
     	JsonObject jsonObject = new JsonObject();
     	
     	String resultSuccess = null;
     	String message = null;
     	
     	for (User user : users)
     	{
     		try
         	{
         		ConferencesUsersDao.getInstance().removeUserFromConference(conference, user);
         	}
         	catch (Exception e)
         	{
         		message = "Some users were not deleted";
         		resultSuccess = "false";
         	}
     	}
     	
     	if (resultSuccess == null)
     	{
     		message = "Users successfully deleted";
     		resultSuccess = "true";
     	}
     	
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Gson gson = new Gson();
            	String json;
        		jsonObject.addProperty("resultSuccess", resultSuccess);
        		jsonObject.addProperty("message", message);
        		json = gson.toJson(jsonObject);
            	out.write(json);
             out.flush();
         }
          finally {
             out.close();
         }
     }
     
     private void assignUsers(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
     {
     	String confName = request.getParameter(ProjConst.CONF_NAME);
     	Conference conference = ConferenceDao.getInstance().getConferenceByName(confName);
     	
     	String userNamesNotFormatted = request.getParameter("userNames");
     	String[] userNames = userNamesNotFormatted.split(",");
     	
     	List<User> users = new LinkedList<User>();
     	
     	for (String user : userNames)
     	{
     		if (user != null)
     			users.add(UserDao.getInstance().getUserByUserName(user));
     	}
     	
     	String userRole = request.getParameter("userRole");
     	
     	JsonObject jsonObject = new JsonObject();
     	
     	String resultSuccess;
     	String message;
     	try 
     	{
     		ConferencesUsersDao.getInstance().assignUsersToConference(conference, users, Integer.parseInt(userRole));
     		resultSuccess = "true";
     		message = "User/s " + userNamesNotFormatted + " assigned to conference " + confName + " with role " + UserRole.resolveUserRoleToFriendlyName(Integer.parseInt(userRole)).toString();
     		
     	}
     	catch (Exception e)
     	{
     		resultSuccess = "false";
     		message = e.getMessage();
     	}
     	
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Gson gson = new Gson();
            	String json;
            	if (ConferenceDao.getInstance().isConferenceNameExists(confName))	
            	{
            		jsonObject.addProperty("resultSuccess", resultSuccess);
            		jsonObject.addProperty("message", message);
            		json = gson.toJson(jsonObject);
            	}
            	else
            	{
            		jsonObject.addProperty("resultSuccess", "false");
            		jsonObject.addProperty("message", "Failed to assign user");
            		json = gson.toJson(jsonObject);
            	}
            	out.write(json);
             out.flush();
         }
          finally {
             out.close();
         }
     }
     
     private void sendInvitationToUsers(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
     {
     	String conf = request.getParameter(ProjConst.CONF_NAME);
     	Conference conference = ConferenceDao.getInstance().getConferenceByName(conf);
 
     	String userName = request.getParameter("userNames");
     	String[] userNames = userName.split(",");
     	
     	
     	List<User> users = new LinkedList<User>();
     	
     	for (String user : userNames)
     	{
     		if (user != null)
     			users.add(UserDao.getInstance().getUserByUserName(user));
     	}
       	
     	JsonObject jsonObject = new JsonObject();
     	
     	String resultSuccess = null;
     	String message = null;
     	
     	for (User user : users)
     	{
     		try
         	{
         		ConferencesUsersDao.getInstance().sendConferenceAssignmentNotificationEmailToUsers(conference, user);
         	}
         	catch (Exception e)
         	{
         		message = "Some invitation were not sent";
         		resultSuccess = "false";
         	}
     	}
     	
     	if (resultSuccess == null)
     	{
     		message = "Initation/s sent successfully";
     		resultSuccess = "true";
     	}
     	
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Gson gson = new Gson();
            	String json;
 
        		jsonObject.addProperty("resultSuccess", resultSuccess);
        		jsonObject.addProperty("message", message);
        		json = gson.toJson(jsonObject);
 
            	out.write(json);
             out.flush();
         }
          finally {
             out.close();
         }
     }
     
     private void deleteConference(HttpServletRequest request, HttpServletResponse response) throws IOException
     {
     	JsonObject jsonObject = new JsonObject();
     	
     	String confName = request.getParameter(ProjConst.CONF_NAME);
     	
       	String resultSuccess;
     	String message;
     	try 
     	{
     		ConferenceDao.getInstance().deleteConference(confName);
     		message = "Conference successfully deleted";
     		resultSuccess = "true";
     		
     	}
     	catch (Exception e)
     	{
     		message = "Found problem while deleting conference, message: " + e.getMessage();
     		resultSuccess = "false";
     	}
     	
     	response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Gson gson = new Gson();
            	String json;
 
        		jsonObject.addProperty("resultSuccess", resultSuccess);
        		jsonObject.addProperty("message", message);
        		json = gson.toJson(jsonObject);
        		
            	out.write(json);
             out.flush();
         }
          finally {
             out.close();
         }
     }
     
     private void updateAttendance(HttpServletRequest request, HttpServletResponse response) throws IOException
     {
     	JsonObject jsonObject = new JsonObject();
     	
     	String confName = request.getParameter(ProjConst.CONF_NAME);
     	Conference conference = ConferenceDao.getInstance().getConferenceByName(confName);
     	
     	String userName = request.getParameter(ProjConst.USER_NAME);
     	User user = UserDao.getInstance().getUserByUserName(userName);
     	
     	String value = request.getParameter("value");
     	
     	
       	String resultSuccess;
     	String message;
     	try 
     	{
     		ConferencesUsersDao.getInstance().updateUserAttendanceApproval(conference, user, UserAttendanceStatus.valueOf(value));
     		message = "Attendance successfully updated for user " + user.getName();
     		resultSuccess = "true";
     		
     	}
     	catch (Exception e)
     	{
     		message = "Found problem while update user attendance";
     		resultSuccess = "false";
     	}
     	
     	response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Gson gson = new Gson();
            	String json;
            	if (ConferenceDao.getInstance().isConferenceNameExists(confName))	
            	{
            		jsonObject.addProperty("resultSuccess", resultSuccess);
            		jsonObject.addProperty("message", message);
            		json = gson.toJson(jsonObject);
            	}
            	else
            	{
            		jsonObject.addProperty("resultSuccess", "false");
            		jsonObject.addProperty("message", "Failed to delete conference");
            		json = gson.toJson(jsonObject);
            	}
            	out.write(json);
             out.flush();
         }
          finally {
             out.close();
         }
     }
     
     private void conferenceNameValidation(HttpServletRequest request, HttpServletResponse response) throws IOException
     {
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Gson gson = new Gson();
             
             String confName = request.getParameter("data");
             if (confName != null)
             {
  	           	String json;
  	           	if (ConferenceDao.getInstance().isConferenceNameExists(confName))
  	           		json = gson.toJson("true");
  	           	else 
  	           		json = gson.toJson("false");
  	           	out.write(json);
             	}
             out.flush();
         }
          finally {
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
