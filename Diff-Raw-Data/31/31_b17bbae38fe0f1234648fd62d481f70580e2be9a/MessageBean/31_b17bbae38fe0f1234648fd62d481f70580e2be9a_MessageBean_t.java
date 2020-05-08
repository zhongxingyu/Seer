 package org.sakaiproject.sdata.services.messages;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.Kernel;
 import org.sakaiproject.db.api.SqlService;
 import org.sakaiproject.email.cover.EmailService;
 import org.sakaiproject.sdata.tool.api.ServiceDefinition;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 
 /**
  * A service definition bean for recent changes that the current user can see.
  * 
  * @author
  */
 public class MessageBean implements ServiceDefinition
 {
 
 	private static final Log log = LogFactory.getLog(MessageBean.class);
 
 	private SqlService sqlService = Kernel.sqlService();
 
 	Map<String, Object> resultMap = new HashMap<String, Object>();
 
 	private SessionManager sessionManager = Kernel.sessionManager();
 	
 	private UserDirectoryService userDirectoryService = Kernel.userDirectoryService();
 
 	/**
 	 * Create a recent changes bean with the number of pages.
 	 * 
 	 * @param paging
 	 */
 	public MessageBean(HttpServletRequest request, HttpServletResponse response)
 	{
 		try {
 			if (request.getMethod().equalsIgnoreCase("get")){
 				if (request.getParameter("list") != null){
 					doList(request, response);
 				} else if (request.getParameter("id") != null){
 					doMessage(request, response);
 				}
 			} else if (request.getMethod().equalsIgnoreCase("post")){
 				doPost(request, response);
 			} else if (request.getMethod().equalsIgnoreCase("delete")){
 				if (request.getParameter("id") != null){
 					doDelete(request, response);
 				}
 			} 
 		} catch (Exception ex){
 			try {
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}  
 
 	private void doDelete(HttpServletRequest request, HttpServletResponse response) {
 		
 		try {
 			
 			int id = Integer.parseInt(request.getParameter("id"));
 			
 			Object[] params = new Object[1];
 			params[0] = id;
 			sqlService.dbWrite("DELETE FROM sdata_profile WHERE id = ?", params);
 			
 			resultMap.put("status", "success");
 		
 		} catch (Exception ex){
 			try {
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 	private void doMessage(HttpServletRequest request, HttpServletResponse response) {
 		
 		int id = Integer.parseInt(request.getParameter("id"));
 		
 		Object[] params = new Object[1];
 		params[0] = id;
 		ArrayList <MessageSqlresult> lst = (ArrayList<MessageSqlresult>) sqlService.dbRead("SELECT * FROM sdata_messages WHERE id = ?", params, new MessageSqlreader());
 		
 		try {
 			
 			MessageSqlresult res = lst.get(0);
 			User usr = userDirectoryService.getUser(res.getSender());
 			
 			if (usr.getFirstName() != null || usr.getLastName() != null){
 				res.setSenderToString((usr.getFirstName() + " " + usr.getLastName()).trim());
 			} else {
 				res.setSenderToString(usr.getDisplayName());
 			}
 			
 			resultMap.put("item", res);
 			
 			// Update read
 			
 			Object[] params2 = new Object[2];
 			params2[0] = true;
 			params2[1] = id;
 			sqlService.dbWrite("UPDATE sdata_messages SET read = ? WHERE id = ?", params2);
 			
 		} catch (Exception ex){
 			try {
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 	private void doList(HttpServletRequest request, HttpServletResponse response)
 	{
 		
 		int page = 1;
 		if (request.getParameter("page") != null){
 			page = Integer.parseInt(request.getParameter("page"));
 		}
 		
 		int items = 10;
 		if (request.getParameter("items") != null){
 			items = Integer.parseInt(request.getParameter("items"));
 		}
 		
 		Object[] params = new Object[1];
 		params[0] = sessionManager.getCurrentSessionUserId();
 		List <MessageSqlresult> lst = sqlService.dbRead("SELECT * FROM sdata_messages WHERE receiver = ? ORDER BY datetime DESC", params, new MessageSqlreader());
 		ArrayList <MessageSqlresult> lst2 = new ArrayList<MessageSqlresult>();
 		
 		for (int i = (page - 1) * items ; i < page * items; i++){
 			try {
 				lst2.add(lst.get(i));
 			} catch (Exception ex){
 				// Ignore, continue
 			}
 		}
 		
 		ArrayList <String> usersToLookup = new ArrayList<String>();
 		ArrayList <User> lookedUpUsers = new ArrayList<User>();
 		
 		for (int i = 0; i < lst2.size(); i++){
 			if (! usersToLookup.contains(lst2.get(i).getSender())){
 				usersToLookup.add(lst.get(i).getSender());
 				try {
 					lookedUpUsers.add(userDirectoryService.getUser(lst.get(i).getSender()));
 				} catch (Exception ex){
 					// This user doesn't exists
 				}
 			}
 		}
 		
 		for (int i = 0; i < lst2.size(); i++){
 			MessageSqlresult res = lst2.get(i);
 			for (int ii = 0; ii < lookedUpUsers.size(); ii++){
 				if (lookedUpUsers.get(ii).getId().equalsIgnoreCase(res.getSender())){
 					User usr = lookedUpUsers.get(ii);
 					if (usr.getFirstName() != null && usr.getLastName() != null){
 						res.setSenderToString((usr.getFirstName() + " " + usr.getLastName()).trim());
 					} else {
 						res.setSenderToString(usr.getDisplayName());
 					}
 				}
 			}
 		}
 		
 		resultMap.put("items", lst2);
 		resultMap.put("total", lst.size());
 		
 	}
 	
 	private void doPost (HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		
 		String sender = sessionManager.getCurrentSessionUserId();
 		String title = request.getParameter("title");
 		String message = request.getParameter("message");
 		String receiver = request.getParameter("receiver");
 		boolean isinvite = Boolean.getBoolean(request.getParameter("isinvite"));
 		Date d = new Date();
 		
 		User fromuser = userDirectoryService.getUser(sender);
 		User touser = userDirectoryService.getUser(receiver);
 		
		Object[] params = new Object[6];
 		params[0] = sender;
 		params[1] = receiver;
 		params[2] = title;
 		params[3] = message;
		//params[4] = d; //new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(d);
		params[4] = isinvite;
		params[5] = false;
 		
		sqlService.dbWrite("INSERT INTO sdata_messages (sender, receiver, title, message, isinvite, isread) VALUES (?, ?, ?, ?, ?, ?)", params);
 		
 		// Send emails
 		
 		if (! isinvite){
 			
 			EmailService.send(fromuser.getEmail(), touser.getEmail(), "You have received a connection request on Sakai 3", "Some text and a link to your invitation", null, null, null);
 			
 		} else {
 			
 			EmailService.send(fromuser.getEmail(), touser.getEmail(), "You have received a message on Sakai 3", "Some text, the message and a link to it", null, null, null);
 			
 		}
 		
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.ServiceDefinition#getResponseMap()
 	 */
 	public Map<String, Object> getResponseMap()
 	{
 		return resultMap;
 	}
 
 }
