 package tr2.server.http.util;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import tr2.server.common.entity.Interval;
 import tr2.server.common.entity.User;
 import tr2.server.common.entity.UserType;
 import tr2.server.common.util.NetworkConstants;
 import tr2.server.http.UserDB;
 import tr2.server.http.exception.BadRequestException;
 import tr2.server.interval.data.Data;
 import tr2.server.sync.data.*;
 
 public class HttpRequestParser {
 	public static final String EOF = "EOF";
 
 	public static String editUsers(String name1) throws BadRequestException {
 		String msg = null;
 
 		msg = HttpServerUtil.getHttpOK();
		msg += HttpServerUtil.getHeader("Lista de usu�rios");
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.admin_menu,"name",name1);
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.edit_users,"name",name1);
 		
 		HashMap<String,User> users = UserDB.getUsers();
 		
 		for (String name : users.keySet()) {
 			User user = users.get(name);
 			HashMap<String,String> user_map = new HashMap<String,String>();
 			user_map.put("name", user.getUsername());
 			user_map.put("type", user.getUserType().toString());
 			user_map.put("myname", name1);
 			msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.edit_users_row,user_map);
 		}
 		
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.edit_users_footer);
 		msg += HttpServerUtil.getFooter();
 		return msg;
 	}
 
 	public static String index() throws BadRequestException {
 		String msg = null;
 		msg = HttpServerUtil.getHttpOK();
 		msg += HttpServerUtil.getHeader("Login");
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.login);
 		msg += HttpServerUtil.getFooter();
 		return msg;
 	}
 	
 	public static String login(String name) throws BadRequestException {
 		UserDB userDB = UserDB.instance();
 		if (userDB.isUser(name)) {
 			User user = userDB.getUser(name);
 			userDB.updateUser(user);
 
 			HashMap<String,String> data = new HashMap<String,String>();
 			data.put("type", user.getUserType().toString().toLowerCase() + "/?name=" + name);
 			data.put("port",String.valueOf(NetworkConstants.LOCAL_CLIENT_PORT));
 			return HttpServerUtil.getTemplate(HttpHeaderTemplates.redirect,data);
 		} else {
 			HashMap<String,String> data = new HashMap<String,String>();
 			data.put("type", "");
 			data.put("port",String.valueOf(NetworkConstants.LOCAL_CLIENT_PORT));
 			return HttpServerUtil.getTemplate(HttpHeaderTemplates.redirect,data);
 		}
 	}
 
 	public static String admin(String name) throws BadRequestException {
 		String msg = null;
 		msg = HttpServerUtil.getHttpOK();
		msg += HttpServerUtil.getHeader("Administra��o");
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.admin_menu,"name",name);
 		msg += getIntervalBody("admin",name);
 		msg += HttpServerUtil.getFooter();
 		return msg;
 	}
 
 	private static String getIntervalBody(String type,String name)
 			throws BadRequestException {
 		String msg = null;
 
 		msg = HttpServerUtil.getTemplateDecider(
 				HttpServerUtil.getTemplate(HttpHtmlTemplates.intervals), type);
 
 		ArrayList<Interval> intervals = Data.getCalculatedIntervals();
 		for (Interval in : intervals) {
 			msg += getIntervalRow(in, "CALCULATED", type,name);
 		}
 		ArrayList<Interval> runningIntervals = Data.getRunningIntervals();
 		for (Interval in : runningIntervals) {
 			msg += getIntervalRow(in, "RUNNING", type,name);
 		}
 		
 		ArrayList<Interval> pendingIntervals = Data.getPendingIntervals();
 		for (Interval in : pendingIntervals) {
 			msg += getIntervalRow(in, "PENDING", type,name);
 		}
 
 		
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.intervals_footer);
 
 		return msg;
 	}
 	
 	private static String getIntervalRow(Interval in, String state, String type, String name) throws BadRequestException {
 		HashMap<String,String> data = new HashMap<String,String>();
 		data.put("interval", in.getFirstDenominator() + " - " + in.getLastDenominator()); 
 		data.put("state",state); 
 		data.put("client",in.getClientIP()); 
 		data.put("name",name);
 		if (state.equals("CALCULATED")) { 
 			data.put("result",String.valueOf(in.getResult())); 
 		} else { 
 			data.put("result", "-"); 
 		}
 		
 		
 		Integer index; 
 		if (in.getFirstDenominator() > 0) { 
 			index =(int)in.getFirstDenominator()/10000; 
 		} else { 
 			index = 0; 
 		}
 		
 		String msg;
 		if (type == "admin") 
 			msg = HttpServerUtil.getTemplateDecider(HttpServerUtil.getTemplate(HttpHtmlTemplates.intervals_row,data),type,"index",index.toString());
 		else 
 			msg = HttpServerUtil.getTemplateDecider(HttpServerUtil.getTemplate(HttpHtmlTemplates.intervals_row,data),type); 
 		
 		return msg;
 	}
 
 	public static String logout() throws BadRequestException {
 		HashMap<String, String> data = new HashMap<String, String>();
 		data.put("type", "");
 		data.put("port", String.valueOf(NetworkConstants.LOCAL_CLIENT_PORT));
 		return HttpServerUtil.getTemplate(HttpHeaderTemplates.redirect, data);
 	}
 
 	public static String addUser(String myname) throws BadRequestException {
 		String msg = HttpServerUtil.getHttpOK();
		msg += HttpServerUtil.getHeader("Adicionar usu�rio");
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.admin_menu);
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.add_user,"myname",myname);
 		msg += HttpServerUtil.getFooter();
 		return msg;
 	}
 	
 	public static String addUser(String name, String type, String myname) throws BadRequestException  {
 		UserDB userDB = UserDB.instance();
 		User user = new User();
 		
 		user.setUsername(name);
 		user.setUserType(UserType.valueOf(type));
 		userDB.addUser(user);
 		
 		HashMap<String,String> data = new HashMap<String,String>();
 		data.put("type", "admin/edit_users?name=" + myname);
 		data.put("port",String.valueOf(NetworkConstants.LOCAL_CLIENT_PORT));
 		return HttpServerUtil.getTemplate(HttpHeaderTemplates.redirect,data);
 	}
 
 	public static String editUserAdmin(String name, String type, String myname)
 			throws BadRequestException {
 		String msg = null;
 		msg = HttpServerUtil.getHttpOK();
		msg += HttpServerUtil.getHeader("Editar usu�rio");
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.admin_menu,"name",myname);
 		HashMap<String, String> data = new HashMap<String, String>();
 		data.put("name", name);
 		data.put("type", type);
 		data.put("myname", myname);
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.edit_user_admin,
 				data);
 
 		msg += HttpServerUtil.getFooter();
 		return msg;
 	}
 	
 	public static String updateUser(String name, String type, String oldname, String myname) throws BadRequestException {
 		UserDB userDB = UserDB.instance();
 		
 		if(userDB.isUser(oldname)) {
 			User user = userDB.getUser(oldname);
 			userDB.removeUser(oldname);
 			user.setUsername(name);
 			user.setUserType(UserType.valueOf(type));
 			userDB.addUser(user);
 		}
 		
 		HashMap<String,String> data = new HashMap<String,String>();
 		data.put("type", "admin/edit_users?name=" + myname);
 		data.put("port",String.valueOf(NetworkConstants.LOCAL_CLIENT_PORT));
 		return HttpServerUtil.getTemplate(HttpHeaderTemplates.redirect,data);
 	}
 	
 	public static String removeUser(String name, String myname) throws BadRequestException {
 		UserDB userDB = UserDB.instance();
 
 		if(userDB.isUser(name)) {
 			userDB.removeUser(name);
 		} else {
 			System.out.print("\n ----------- \n");
 		}
 		
 		HashMap<String,String> data = new HashMap<String,String>();
 		data.put("type", "admin/edit_users?name=" + myname);
 		data.put("port",String.valueOf(NetworkConstants.LOCAL_CLIENT_PORT));
 		return HttpServerUtil.getTemplate(HttpHeaderTemplates.redirect,data);
 	}
 	
 	public static String editUser(String name) throws BadRequestException {
 		String msg = null;
 		msg = HttpServerUtil.getHttpOK();
 		msg += HttpServerUtil.getHeader("Editar conta");
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.user_menu);
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.edit_user,"oldname",name);
 		msg += HttpServerUtil.getFooter();
 		return msg;
 	}
 	
 	public static String editUser(String newname, String oldname) throws BadRequestException  {
 		UserDB userDB = UserDB.instance();
 		User user = userDB.getUser(oldname);
 		userDB.removeUser(oldname);
 		user.setUsername(newname);
 		userDB.addUser(user);
 
 		HashMap<String,String> data = new HashMap<String,String>();
 		data.put("type", "user?name=" + newname);
 		data.put("port",String.valueOf(NetworkConstants.LOCAL_CLIENT_PORT));
 		return HttpServerUtil.getTemplate(HttpHeaderTemplates.redirect,data);
 	}
 
 	public static String intervals(String type, String name) throws BadRequestException {
 		String msg = null;
 		msg = HttpServerUtil.getHttpOK();
 		msg += HttpServerUtil.getHeader("Intervalos calculados");
 
 		if (type == "admin")
 			msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.admin_menu,"name",name);
 		else
 			msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.user_menu,"name",name);
 		msg += getIntervalBody(type,name);
 		msg += HttpServerUtil.getFooter();
 		return msg;
 	}
 
 	public static String servers(String type, String name) throws BadRequestException {
 		String msg = null;
 		msg = HttpServerUtil.getHttpOK();
 
 		if (type == "user")
 			msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.user_menu,"name",name);
 		else
 			msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.admin_menu,"name",name);
 
 		msg += HttpServerUtil.getHeader("Servidores");
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.servers);
 		
 		ArrayList<ServerInfo> servers = ServerData.getServers();
 		
 		for (int i = 0; i < servers.size(); i++) {
 			HashMap<String,String> data = new HashMap<String,String>();
 			data.put("server", servers.get(i).getAddress());
 			
 			String state;
 			if (i == servers.size()-1) {
 				SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
 				state = "MANAGER (" + dateFormat.format(ServerData.getStartTime()) + ")";
 			} else
 				state = "BACKUP";
 			
 			data.put("state",state);
 			msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.servers_row,data); 
 		}
 
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.servers_footer);
 		msg += HttpServerUtil.getFooter();
 		return msg;
 	}
 
 	public static String result(String type, String name) throws BadRequestException {
 		String msg = null;
 		msg = HttpServerUtil.getHttpOK();
 
 		if (type == "user")
 			msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.user_menu,"name",name);
 		else
 			msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.admin_menu,"name",name);
 
 		msg += HttpServerUtil.getHeader("Resultado");
 		
 		Double res = Data.intervalsSum();
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.result,"result",res.toString());
 		msg += HttpServerUtil.getFooter();
 		return msg;
 	}
 
 	public static String user(String name) throws BadRequestException {
 		String msg = null;
 		msg = HttpServerUtil.getHttpOK();
		msg += HttpServerUtil.getHeader("Usu�rio");
 		msg += HttpServerUtil.getTemplate(HttpHtmlTemplates.user_menu,"name",name);
 		msg += getIntervalBody("user",name);
 		msg += HttpServerUtil.getFooter();
 		return msg;
 	}
 
 	public static String remove_seq(String interval, String name) throws BadRequestException {
 		Interval inte = new Interval();
 		inte.setIndex(Integer.parseInt(interval));
 		
 		ArrayList<Interval> ar = new ArrayList<Interval>();
 		
 		ar.add(inte);
 		
 		Data.removeIntervals(ar);
 		
 		HashMap<String,String> data = new HashMap<String,String>();
 		data.put("type", "admin/?name=" + name);
 		data.put("port",String.valueOf(NetworkConstants.LOCAL_CLIENT_PORT));
 		return HttpServerUtil.getTemplate(HttpHeaderTemplates.redirect,data);
 	}
 }
