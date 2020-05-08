 package org.moon.rbac.action;
 
 import org.moon.base.action.BaseAction;
 import org.moon.core.orm.mybatis.Criteria;
 import org.moon.core.orm.mybatis.DataConverter;
 import org.moon.core.orm.mybatis.criterion.SimpleCriterion;
 import org.moon.log.domain.Log;
 import org.moon.message.WebResponse;
 import org.moon.pagination.Pager;
 import org.moon.rbac.domain.Role;
 import org.moon.rbac.domain.User;
 import org.moon.rbac.domain.annotation.LoginRequired;
 import org.moon.rbac.domain.annotation.MenuMapping;
 import org.moon.rbac.domain.annotation.PermissionMapping;
 import org.moon.rbac.domain.annotation.WebUser;
 import org.moon.rbac.service.UserService;
 import org.moon.rest.annotation.Get;
 import org.moon.rest.annotation.Post;
 import org.moon.support.spring.annotation.FormParam;
 import org.moon.utils.Dtos;
 import org.moon.utils.Objects;
 import org.moon.utils.ParamUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * @author Gavin
  * @version 1.0
  * @date 2012-12-2
  */
 @Controller
 @RequestMapping("/user")
 public class UserAction extends BaseAction{
 
 	@Resource 
 	private UserService userService;
 	
 	/**
 	 * 显示登录页面
 	 * @return
 	 */
 	@Get("/login")
 	public ModelAndView showLoginPage(@RequestParam("from") String from){
 		 return new ModelAndView("pages/login","from",from);
 	}
 	 
 	 @Get("")
 	 @MenuMapping(url="/user",name="用户列表",code="platform_1",parentCode="platform")
 	 @PermissionMapping(code="000008",name="用户列表")
 	 public ModelAndView userList(HttpServletRequest request){
 		 return new ModelAndView("pages/rbac/userList");
 	 }
 	 
 	/**
 	 * 登录验证
 	 * @param user
 	 * @return
 	 */
 	 @Post("/login/validate")
 	 public @ResponseBody WebResponse userValidate(@FormParam("user")User user,HttpServletRequest request){
 		 if(Objects.isNull(user)){
 			 return WebResponse.build().setSuccess(false);
 		 }
 		 String loginName = user.getUserName();
 		 String password = user.getPassword();
 		 user.setId(null);//设置id为null，此后用id是否为空判断是否成功登录
 		 user = userService.login(user);
 		 if(Objects.isNull(user)||Objects.isNull(user.getId())){
 			new Log(loginName,-1L,"登录失败","{userName:"+loginName+",password:"+password+"}").save();
 			return WebResponse.build().setSuccess(false);
 		 }else{
 			 request.getSession().setAttribute(User.CURRENT_USER_ID, user.getId());
 			 new Log(user.getUserName(),user.getId(),"成功登录系统").save();
 			 return WebResponse.build().setSuccess(true);
 		 }
 	 }
 	 
 
 	 @PermissionMapping(code="000001",name="用户列表")
 	 @Get("/list")
 	 public @ResponseBody WebResponse getUsersList(HttpServletRequest request){
		 Criteria criteria = ParamUtils.getParamsAsCerteria(request);
		 criteria.add(new SimpleCriterion("delete_flag", "=", false));
		 Pager results =  userService.listForPage(criteria, Dtos.newUnderlineToCamelBakConverter(false,"password"));
		 return WebResponse.build().setResult(results);
 	 }
 	 
 	 @Post("/add")
 	 public @ResponseBody  WebResponse add(@WebUser User creator,@FormParam(value="user")User user){
 		 user.setCreateBy(creator.getId());
 		 user.sync(user.encryptPassword().save());
 		 return WebResponse.build().setSuccess(true);
 	 }
 	 
 	 @Get("/get/{id}")
 	 public @ResponseBody WebResponse getUser(@PathVariable("id")Long id){
 		 return WebResponse.build().setResult(userService.get(id).toAllMap());
 	 }
 	 
 	 @Post("/update")
 	 public @ResponseBody WebResponse updateUser(@FormParam("user") User user){
 		 User oldUser = userService.get(user.getId());
 		 oldUser.setUserName(user.getUserName());
 		 oldUser.setRealName(user.getRealName());
 		 oldUser.setPassword(user.getPassword());
 		 oldUser.sync(oldUser.encryptPassword().update());
 		 return WebResponse.build();
 	 }
 	 
 	 @Post("/logicDelete")
 	 public @ResponseBody WebResponse logicDeleteUser(@RequestParam("ids")Long[] ids){
 		 userService.delete(ids, true);
 		 return WebResponse.build();
 	 }
 	 
 	 /**
 	  * 获取用户所在的角色路径
 	  * @return
 	  */
 	 @Get("/getRolePath")
 	 public @ResponseBody WebResponse getRolePath(@RequestParam("uid")Long uid){
 		String rolePath = "";
 		Role role = userService.get(uid).getRole();
 		if(role!=null){
 			rolePath = role.getRolePath();
 		}
 		return WebResponse.build().setResult(rolePath);
 	 }
 	 
 	 /**
 	  * 登出操作
 	  * @return
 	  */
 	 @Get("/loginOut")
 	 public ModelAndView loginOut(HttpServletRequest request){
 		 request.getSession().setAttribute(User.CURRENT_USER_ID, null);
 		 return new ModelAndView("pages/login");
 	 }
 	 
 	 @LoginRequired
 	 @Get("/changePassword")
 	 @MenuMapping(code="platform_5",name="修改密码",url="/user/changePassword",parentCode="platform")
 	 public ModelAndView showChangePasswordPage(@WebUser User user){
 		 String info = null ;
 		 if(user.isSysUser()){
 			 info = "对不起，系统管理员不能修改密码.";
 		 }
 		 return new ModelAndView("pages/rbac/changePassword","info",info);
 	 }
 	 
 	 /**
 	  * 密码匹配,用于密码修改
 	  * @param newPassword
 	  * @return
 	  */
 	 @LoginRequired
 	 @Get("/matchOldPassword")
 	 public @ResponseBody WebResponse matchOldPassword(@WebUser User user,@RequestParam("password")String newPassword){
 		if (user.getPassword().equals(newPassword)) {
 			return WebResponse.build().setSuccess(true);
 		} else {
 			return WebResponse.build().setSuccess(false);
 		}
 	 }
 	 /**
 	  * 修改密码
 	  * @param newPassword
	  * @param request
 	  * @return
 	  */
 	 @LoginRequired
 	 @Post("/doChangePassword")
 	 public @ResponseBody WebResponse changePassword(@WebUser User user,@RequestParam("password")String newPassword){
 		 if(user.isSysUser()){
 			 return WebResponse.build().setSuccess(false);
 		 }
 		 user.setPassword(newPassword);
 		 user.sync(user.encryptPassword().update());
 		 return WebResponse.build().setSuccess(true);
 	 }
 	 
 	 /**
 	  * 检查用户名是否存在
 	  * @param userName
 	  * @return
 	  */
 	 @LoginRequired
 	 @Post("/checkUserName")
 	 public @ResponseBody WebResponse checkUserName(@RequestParam("userName")String userName){
 		 return WebResponse.build().setResult(userService.isUserNameExists(userName));
 	 }
 }
