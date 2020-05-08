 package com.sivasrinivas.ShopManager.action.admin;
 
 import org.apache.log4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.sivasrinivas.ShopManager.model.AdminModel;
 import com.sivasrinivas.ShopManager.model.UserModel;
 import com.sivasrinivas.ShopManager.service.LoginService;
 
 public class LoginAction extends ActionSupport{
 	/**
 	 * generated version id of the class
 	 */
 	private static final long serialVersionUID = 6025733989482587432L;
 	static Logger logger = Logger.getLogger(LoginAction.class);
 	private ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
 	private LoginService loginService = (LoginService) context.getBean("loginService");
 	
 	private AdminModel admin;
 	
 	public String execute(){
 		return "login";
 	}
 	
 	public String login(){
 			boolean loginRes = loginService.verifyPassword(admin);
 			if(loginRes){
 				return SUCCESS;
 			}
 			else{
 				logger.error("login failed");
				addActionError("Email Id or Password didn't match. Please try again.");
 				return INPUT;
 			}
 				
 	}
 
 	/**
 	 * @return the admin
 	 */
 	public AdminModel getAdmin() {
 		return admin;
 	}
 
 	/**
 	 * @param admin the admin to set
 	 */
 	public void setAdmin(AdminModel admin) {
 		this.admin = admin;
 	}
 }
