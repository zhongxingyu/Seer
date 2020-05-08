 package framework.command;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import framework.controller.Command;
 
 public class Logout implements Command{
 
 	@Override
 	public String perform(HttpServletRequest request,HttpServletResponse response) {
 		HttpSession session = request.getSession(false);
		
 		session.invalidate();
	
 		
 		return "Login";
 	}
 
 }
