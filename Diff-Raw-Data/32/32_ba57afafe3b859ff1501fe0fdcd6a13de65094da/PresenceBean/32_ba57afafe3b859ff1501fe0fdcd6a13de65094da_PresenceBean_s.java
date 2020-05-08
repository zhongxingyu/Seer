 package org.sakaiproject.sdata.services.presence;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
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
 import org.sakaiproject.sdata.services.connections.ConnectionSqlreader;
 import org.sakaiproject.sdata.services.connections.ConnectionSqlresult;
 import org.sakaiproject.sdata.services.profile.ProfileSqlreader2;
 import org.sakaiproject.sdata.services.profile.ProfileSqlresult2;
 import org.sakaiproject.sdata.tool.api.ServiceDefinition;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 
 /**
  * A service definition bean for recent changes that the current user can see.
  * 
  * @author
  */
 public class PresenceBean implements ServiceDefinition
 {
 
 	private static final Log log = LogFactory.getLog(PresenceBean.class);
 
 	private SqlService sqlService = Kernel.sqlService();
 
 	Map<String, Object> resultMap = new HashMap<String, Object>();
 
 	private SessionManager sessionManager = Kernel.sessionManager();
 	
 	private UserDirectoryService userDirectoryService = Kernel.userDirectoryService();
 
 	/**
 	 * Create a recent changes bean with the number of pages.
 	 * 
 	 * @param paging
 	 */
 	public PresenceBean(HttpServletRequest request, HttpServletResponse response)
 	{
 		try {
 			if (request.getMethod().equalsIgnoreCase("get")){
 				showOnlineFriends(request, response);
 			} else if (request.getMethod().equalsIgnoreCase("post")){
 				notifyPresence(request, response);
 			} 
 		} catch (Exception ex){
 			try {
 				ex.printStackTrace();
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}  
 	
 	private void notifyPresence(HttpServletRequest request, HttpServletResponse response) {
 		
 		String currentUser = sessionManager.getCurrentSessionUserId();
 		Object[] params = new Object[1];
 		params[0] = currentUser;
 		
 		List lst = sqlService.dbRead("SELECT * FROM sdata_presence WHERE userid = ?", params, null);
 		
 		String s = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
 		long now = Long.parseLong(s);
 		
 		if (lst.size() == 0){
 			Object[] params2 = new Object[2];
 			params2[0] = currentUser;
 			params2[1] = now;
 			sqlService.dbWrite("INSERT INTO sdata_presence (userid, lastseen) VALUES (?,?)", params2);
 		} else {
 			Object[] params2 = new Object[2];
 			params2[0] = now;
 			params2[1] = currentUser;
 			sqlService.dbWrite("UPDATE sdata_presence SET lastseen = ? WHERE userid = ?", params2);
 		}
 		
 	}
 
 	private void showOnlineFriends(HttpServletRequest request, HttpServletResponse response) {
 		
 		// Get a list of my friends first
 		
 		Object[] params = new Object[2];
 		params[0] = sessionManager.getCurrentSessionUserId();
 		params[1] = sessionManager.getCurrentSessionUserId();
 		
 		List<ConnectionSqlresult> lst = (List<ConnectionSqlresult>) sqlService.dbRead("SELECT * FROM sdata_connections WHERE receiver = ? OR inviter = ?", params, new ConnectionSqlreader());
		
		log.error("(((((((( Size of lst = " + lst.size() + ")))))))))))))))");
		
 		if (lst.size() > 0){
 			
 			String sql = "SELECT userid FROM sdata_presence WHERE";
 			Object[] params2 = new Object[lst.size()];
 			
 			for (int i = 0; i < lst.size(); i++){
 				if (lst.get(i).getInviter().equalsIgnoreCase(sessionManager.getCurrentSessionUserId())){
 					params2[i] = lst.get(i).getReceiver();
 				} else {
 					params2[i] = lst.get(i).getInviter();
 				}
 				if (i != 0){
 					sql += " OR";
 				}
 				sql += " userid = ?";
 			}
 			
 			List lst2 = sqlService.dbRead(sql, params2, null);
 			
 			// Get profiles of these people
 			
			log.error("(((((((( Size of lst2 = " + lst2.size() + ")))))))))))))))");
			
			ArrayList<ProfileSqlresult2> arl = new ArrayList<ProfileSqlresult2>();
			String sql2 = "SELECT * FROM (SELECT *  FROM SAKAI_USER  LEFT OUTER JOIN sdata_profile ON SAKAI_USER.USER_ID = sdata_profile.userid) as new WHERE";
			Object[] params3 = new Object[lst2.size()];
			for (int ii = 0; ii < lst2.size(); ii++){
				if (ii != 0){
					sql2 += " OR";
 				}
				sql2 += " new.USER_ID = ?";
				params3[ii] = lst2.get(ii);
 			}
			List <ProfileSqlresult2> lst3 = sqlService.dbRead(sql2, params3, new ProfileSqlreader2());
 			
 			resultMap.put("items", lst3);
 			resultMap.put("total", lst3.size());
 			
 		} else {
 			
 			resultMap.put("items", new ArrayList());
 			
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
