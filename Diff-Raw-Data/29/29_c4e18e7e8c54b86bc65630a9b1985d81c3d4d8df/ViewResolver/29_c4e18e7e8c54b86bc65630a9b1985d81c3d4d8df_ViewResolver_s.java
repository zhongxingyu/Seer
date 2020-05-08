 package edu.unsw.triangle.view;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import edu.unsw.triangle.web.LoginRequestController;
 
 public class ViewResolver 
 {
 	private final Logger logger = Logger.getLogger(LoginRequestController.class.getName());
 	private final static Map<String, String> mapping;
 	
 	static
 	{
 		mapping = new HashMap<String, String>();
 		mapping.put("login.view", "/WEB-INF/login.jsp");
 		mapping.put("login", "login");
 		mapping.put("error.view", "/WEB-INF/error.jsp");
 		mapping.put("register", "register");
 		mapping.put("register.view", "/WEB-INF/register.jsp");
 		mapping.put("main", "main");
 		mapping.put("main.view", "/WEB-INF/main.jsp");
 		mapping.put("profile.view", "/WEB-INF/profile.jsp");
 		mapping.put("sell", "sell");
 		mapping.put("sell.view", "/WEB-INF/sell.jsp");
		mapping.put("sell.confirm.view", "/WEB-INF/sell.jsp");
 		mapping.put("item.view", "/WEB-INF/item.jsp");
 		mapping.put("item", "item");
 		mapping.put("confirm", "/WEB-INF/confirm.jsp");
 		mapping.put("logout.view", "/WEB-INF/logout.jsp");
 		mapping.put("admin", "admin");
 		mapping.put("admin.view", "/WEB-INF/admin.jsp");
 		mapping.put("confirm.view", "/WEB-INF/confirm.jsp");
 	}
 	
 	public String resolve(String name)
 	{
 		if (!mapping.containsKey(name))
 		{
 			logger.warning("no mapping exist for view '" + name + "'");
 		}
 		return mapping.get(name);
 	}
 
 }
