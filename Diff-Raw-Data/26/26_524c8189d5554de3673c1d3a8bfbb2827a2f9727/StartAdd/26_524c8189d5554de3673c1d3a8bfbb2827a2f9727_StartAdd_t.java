 package webapp.help.controller;
 
 
 
import javax.servlet.http.HttpServletRequest;
 
 import webapp.help.dao.Model;
 
 public class StartAdd extends Action {
 	public static String actionName="startAdd.do";
 	public StartAdd(Model model) {
 		super(model);
 	}
 
 	@Override
 	public String getName() {
 		return actionName;
 	}
 
 	@Override
 	public String perform(HttpServletRequest request) {
 
 		return "view/add.jsp";
 
 	}
 
 }
