 package webapp.help.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import webapp.help.dao.Model;
 import webapp.help.utility.BrowserType;
import webapp.help.utility.Category;
 import webapp.help.utility.Mailer;
 
 public class SendMessageAction extends Action {
 	public static String actionName="sendMessage.do";
 	public SendMessageAction(Model model) {
 		super(model);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public String getName() {
 		return actionName;
 	}
 
 	@Override
 	public String perform(HttpServletRequest request) {
 		String category = request.getParameter("category");
 		double longitude = Double.parseDouble(request.getParameter("longitude"));
 		double latitude = Double.parseDouble(request.getParameter("latitude"));
 		
 		if(category == null){
 			return "error.jsp";
 		}
 		else{
 			String color;
 			request.setAttribute("category", new String(category));
 			if(category.equals(Category.GENERAL)) color = "#FFC000";
 			else if(category.equals(Category.MEDICAL)) color = "#00B0F0";
 			else if(category.equals(Category.CAR)) color = "#92D050";
 			else if(category.equals(Category.FIRE)) color = "#FFAEC9";
 			else color = "#FFC000";
 			request.setAttribute("color", color);
 		}
 		
 	
 			
 		Mailer.sendMesssages(model.getContactsDAO().getContacts(category), longitude, latitude);
 		
 		
 		if(Controller.detectBrowser(request) == BrowserType.Desktop)
 			return "viewCategory.do";
 		else
 			return "cellView/medical.jsp";
 	}
 
 }
