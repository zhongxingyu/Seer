 package edu.sjsu.videolibrary.jspHelper;
 
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.sjsu.videolibrary.model.User;
 import edu.sjsu.videolibrary.service.ServiceProxy;
 import edu.sjsu.videolibrary.util.ClientConfig;
 import edu.sjsu.videolibrary.util.Parameters;
 import edu.sjsu.videolibrary.util.UtilsClient;
 
 public class ViewUsersHelper {
 
 	public static User[] getUsers(HttpServletRequest req, HttpServletResponse resp) throws RemoteException {
		String memType = (String) req.getParameter(Parameters.pMembershipType);
 		int page = 0;
 		String pageNum = (String) req.getParameter(Parameters.pPage);
 		if(pageNum != null) {
 			page = Integer.parseInt(pageNum);
 		}
 		
 		ServiceProxy proxy = UtilsClient.getServiceProxy();
 		User[] userList = null;
 		if (memType == null) {
 			userList = proxy.viewMembers(memType);
 		}
 		return userList;
 		}
 	
 	public static String[] getPageLinks(HttpServletRequest req, HttpServletResponse resp) {
		String memType = (String) req.getParameter(Parameters.pMembershipType);
 		if(memType == null) {
 			memType = "";
 		}
 		int page = 0;
 		String pageNum = (String) req.getParameter(Parameters.pPage);
 		if(pageNum != null) {
 			page = Integer.parseInt(pageNum);
 		}
 		
 		String[] pageLinks = new String[2];
 		if(page!=0) {
 			Map<String,String> map = new HashMap<String,String>();
			map.put(Parameters.pMembershipType, memType);
 			map.put(Parameters.pPage, Integer.valueOf(page-1).toString());
 			pageLinks[0] = UtilsClient.generateQueryString(ClientConfig.VIEW_USERS, map );
 		} else {
 			pageLinks[0] = null;
 		}
 		Map<String,String> map = new HashMap<String,String>();
		map.put(Parameters.pMembershipType, memType);
 		map.put(Parameters.pPage, Integer.valueOf(page+1).toString());
 		pageLinks[1] = UtilsClient.generateQueryString(ClientConfig.VIEW_MOVIES, map );
 		return pageLinks;
 	}
 }
