 package framework.command;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import framework.controller.Command;
 
 public class Logout implements Command{
 
 	@Override
 	public String perform(HttpServletRequest request,HttpServletResponse response) {
 		HttpSession session = request.getSession(false);
		String currentUser = (String) session.getAttribute("currentUsername");
		String currentRole = (String) session.getAttribute("currentRole");
		String currentUserID = (String) session.getAttribute("currentUserID");
		if(!session.equals(null)){
		currentUser = null;
		currentRole = null;
		currentUserID = null;
		session.setAttribute("currentUsername", currentUser);
		session.setAttribute("currentRole", currentRole);
		session.setAttribute("currentUserID", currentUserID);
 		session.invalidate();
	}
 		
 		return "Login";
 	}
 
 }
