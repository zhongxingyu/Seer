 package org.controller;
 
 import java.util.Collection;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 
 import org.entity.Article;
 import org.entity.User;
 import org.eweb4j.mvc.MVC;
 import org.eweb4j.mvc.validator.annotation.Validate;
 import org.eweb4j.util.CommonUtil;
 import org.ioc.ServiceIOCs;
 import org.service.ArticleService;
 import org.service.UserService;
 import org.util.CookieHelper;
 
 @Path("/user")
 public class UserController {
 	private UserService userService=ServiceIOCs.create().createUserService();
 	private ArticleService articleService=ServiceIOCs.create().createArticleService();
 	
 	private static String VERIFIED="1";
 	private static String ERROR_USER_NAME="-1";
 	private static String ERROR_EMAIL="-2";
 	private static String EXIST_USER_NAME="-3";
 	private static String EXIST_EMAIL="-4";
 	private static String ERROR_PASSWORD="-5";
 	
 	public String index(Map<String,Object> model){
 	 	Collection<User> users=userService.userList();
 		model.put("users", users);
 	 	
 		return "vm:user/index.html";
 	}
 	
 	@Path("/profile/{name}")
 	public String profile(@PathParam("name")String name, Map<String,Object> model){
 		User user=userService.getOneUserByName(name);
 		
 		if(user!=null){
 			model.put("user", user);
 			
 			long articleCount=userService.getUserArticleCount(user);
 			model.put("articleCount", articleCount);
 			
 			long replyCount=userService.getUserReplyCount(user);
 			model.put("replyCount", replyCount);
 			
 			return "vm:user/profile.html";
 		}
 		
 		return "action:";
 	}
 	
 	@Path("/signup")
 	public String signup(){
 		
 		return "vm:user/signup.html";
 	}
 	
 	@Path("/doSignup")
 	@POST
 	public String doSignup(@QueryParam("user")User user, Map<String,Object> model){
 		boolean isVerified=true;
 		String msg=null;
 		
 		if(!checkUserName(user.getName()).equals(VERIFIED)){
			msg="用户名无效";
 			isVerified=false;
 		}
 		else if(!checkEmail(user.getEmail()).equals(VERIFIED)){
 			msg="邮箱无效";
 			isVerified=false;
 		}
 		else if(!checkPassword(user.getPassword()).equals(VERIFIED)){
 			msg="密码无效";
 			isVerified=false;
 		}
 		else{
 			
 			User regUser=userService.signup(user);
 			
 			if(regUser!=null){
 				MVC.ctx().getSession().setAttribute("user", user);
 			}
 			else{
 				msg="注册失败";
 				isVerified=false;
 			}
 		}
 		
 		if(!isVerified){
 			model.put("ERROR", msg);
 			return "vm:user/signup.html";
 		}
 		
 		return "action:";
 	}
 	
 	@Path("/check_user_name")
 	@GET
 	public String checkUserName(@QueryParam("user_name")String userName){
 		if(!CommonUtil.verifyWord(userName, "^\\w{3,10}$")){
 			return ERROR_USER_NAME;
 		}
 		
 		if(userService.getOneUserByName(userName)!=null){
 			return EXIST_USER_NAME;
 		}
 		
 		return VERIFIED;
 	}
 	
 	@Path("/check_email")
 	@GET
 	public String checkEmail(@QueryParam("email")String email){
 		if(!CommonUtil.isValidEmail(email)){
 			return ERROR_EMAIL;
 		}
 		
 		if(userService.getOneUserByEmail(email)!=null){
 			return EXIST_EMAIL;
 		}
 		
 		return VERIFIED;
 	}
 	
 	public String checkPassword(String pwd){
 		if(!CommonUtil.verifyWord(pwd, "^[^/s]{6,}$"))
 			return ERROR_PASSWORD;
 		
 		return VERIFIED;
 	}
 	
 	@Path("/login")
 	public String login(){
 			
 		return "vm:user/login.html";
 	}
 	
 	@Path("/doLogin")
 	@POST
 	public String doLogin(@QueryParam("user")User user,@QueryParam("keep")int keep,Map<String,Object> model){
 				
 		User loginUser = userService.login(user);
 		if(loginUser != null){
 			MVC.ctx().getSession().setAttribute("user", loginUser);
 			
 			//判断是否30天内免登陆
 			if(keep==1){
 				Cookie cookieUser=new Cookie("user", loginUser.getName());
 				cookieUser.setMaxAge(30*24*60*60);
 				cookieUser.setPath("/");
 				MVC.ctx().getResponse().addCookie(cookieUser);
 				
 				String token=userService.genernateToken(loginUser);
 				Cookie cookieToken=new Cookie("token",token);
 				cookieToken.setMaxAge(30*24*60*60);
 				cookieToken.setPath("/");
 				MVC.ctx().getResponse().addCookie(cookieToken);
 			}
 			
 			return "action:";
 		}
 		
 		model.put("ERROR", true);
 		return "vm:user/login.html";
 	}
 	
 	@Path("/logout")
 	public String logout(){
 		MVC.ctx().getSession().removeAttribute("user");
 		CookieHelper.delAllCookie();
 		return "action:user/login";
 	}
 	
 	@Path("/setting/password")
 	public String changePassword(){
 		
 		return "vm:user/password.html";
 	}
 	
 	@Path("/setting/doPassword")
 	@POST
 	public String doChangePassword(@QueryParam("old_pwd")String oldPwd, 
 			@QueryParam("new_pwd")String newPwd, 
 			@QueryParam("re_pwd")String rePwd,
 			Map<String,Object> model){
 		
 		oldPwd=CommonUtil.md5(oldPwd);
 		
 		User user = (User)MVC.ctx().getSession().getAttribute("user");
 		
 		if(!oldPwd.equals(user.getPassword())){
 			
 			model.put("STATUS_CODE", -1); //旧密码错误
 		}
 		else if(this.checkPassword(newPwd)!=this.VERIFIED){
 			model.put("STATUS_CODE", -3); //密码长度不符
 		}
 		else if(!newPwd.equals(rePwd)){
 			model.put("STATUS_CODE", -2); //输入密码不一致
 		}
 		else{
 			user.setPassword(CommonUtil.md5(newPwd));
 			Boolean status=userService.changePassword(user);
 			
 			if(status){
 				MVC.ctx().getSession().setAttribute("user",user);
 				model.put("STATUS_CODE", 1); //成功
 			}
 		}
 		
 		return "vm:user/password.html";
 	}
 	
 	@Path("favorite")
 	public String favorite(Map<String,Object> model){
 		User user = (User)MVC.ctx().getSession().getAttribute("user");
 		Collection<Article> articles=articleService.getMarkArticlesByUserId(user.getId());
 		model.put("articles", articles);
 		
 		//return CommonUtil.toJson(articles);
 		return "vm:user/favorite.html";
 	}
 }
