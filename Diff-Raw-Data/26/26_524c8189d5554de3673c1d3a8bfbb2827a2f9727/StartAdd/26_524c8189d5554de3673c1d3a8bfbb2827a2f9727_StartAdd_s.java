 package webapp.help.controller;
 
import java.util.ArrayList;
import java.util.List;
 
import javax.servlet.http.HttpServletRequest;
 
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
 
import webapp.help.beans.ContactBean;
 import webapp.help.dao.Model;
import webapp.help.utility.Category;
 
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
