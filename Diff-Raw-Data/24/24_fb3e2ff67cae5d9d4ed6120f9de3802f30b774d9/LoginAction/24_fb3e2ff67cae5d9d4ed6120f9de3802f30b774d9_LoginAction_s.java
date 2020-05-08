 /**
  * 
  */
 package com.chinarewards.qqgbvpn.mgmtui.struts.login;
 
 import java.util.Map;
 
 import com.chinarewards.qqgbvpn.mgmtui.logic.login.LoginLogic;
 import com.chinarewards.qqgbvpn.mgmtui.struts.BaseAction;
 import com.chinarewards.qqgbvpn.mgmtui.struts.SessionConstant;
 import com.chinarewards.utils.StringUtil;
 import com.opensymphony.xwork2.ActionContext;
 
 /**
  * Login operation.
  * 
  * @author cream
  * @since 1.0.0 2011-09-02
  */
 public class LoginAction extends BaseAction {
 
 	private static final long serialVersionUID = 6257809328181428130L;
 
 	private String username;
 	private String password;
 	private String backUrl;
 
 	@Override
 	public void validate() {
 		if (StringUtil.isEmptyString(username)
 				|| StringUtil.isEmptyString(password)) {
 			addFieldError("loginError", "用户名或密码为空");
 			log.debug("username or password is empty");
 		}
 	}
 
 	@Override
 	public String execute() throws Exception {
 		return SUCCESS;
 	}
 
 	public String login() {
 
 		LoginLogic loginLogic = getInjector().getInstance(LoginLogic.class);
 
 		boolean validePass = loginLogic.checkLogin(username, password);
 
 		if (validePass) {
 			Map<String, Object> session = ActionContext.getContext()
 					.getSession();
			session.put(SessionConstant.USER_SESSION, true);
 			log.debug("login success");
 			return SUCCESS;
 		} else {
 			addFieldError("loginError", "用户名或密码错误");
 			log.debug("login failed");
 			return INPUT;
 		}
 	}
 
 	public String logout() {
 		return SUCCESS;
 	}
 
 	// ---- getter & setter ----
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getBackUrl() {
 		return backUrl;
 	}
 
 	public void setBackUrl(String backUrl) {
 		this.backUrl = backUrl;
 	}
 
 }
