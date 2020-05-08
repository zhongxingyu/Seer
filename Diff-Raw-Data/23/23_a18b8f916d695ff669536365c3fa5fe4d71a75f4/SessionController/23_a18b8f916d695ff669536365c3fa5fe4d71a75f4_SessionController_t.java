 package edu.hust.k54.controller;
 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

 import edu.hust.k54.persistence.Taikhoandangnhap;
 import edu.hust.k54.persistence.TaikhoandangnhapHome;
 
 
public class SessionController implements Controller {
 	private static final int GUEST_PERMISSION = 0;
 	private static final int STAFF_PERMISSION = 1;
 	private static final int MANAGER_PERMISSION = 2;
 	private static final int ADMIN_PERMISSION = 3;
 	
 	public Representative login(String userName, String passWord){
 		TaikhoandangnhapHome checkLogin = new TaikhoandangnhapHome();
 		Taikhoandangnhap persion = new Taikhoandangnhap();
 		persion.setUsername(userName);
 		persion.setPass(passWord);
 		if(!checkLogin.findByExample(persion).isEmpty()){
 			//return (Pep)checkLogin.findByExample(persion).get(0);
 		}
 		return null;
 	}
 	
 	public boolean logout(){
 		return true;
 	}
 
	@Override
	public ModelAndView handleRequest(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
		ModelAndView modelAndView = new ModelAndView("homepage");
		modelAndView.addObject("messege", "test");
		String name = arg0.getParameter("user_name");
		String pass = arg0.getParameter("user_password");
		System.out.println(name + "  " + pass);
		return modelAndView;
	}

 }
