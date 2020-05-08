 package com.tp.action;
 
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

 import com.opensymphony.xwork2.ActionSupport;
import com.tp.service.account.ShiroDbRealm.ShiroUser;
import com.tp.utils.Struts2Utils;
 
 public class LoginAction extends ActionSupport {
 
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public String execute() throws Exception {
 		return login();
 	}
 
 	public String login() throws Exception {
		Subject currentUser = SecurityUtils.getSubject();
		ShiroUser user = (ShiroUser) currentUser.getPrincipal();
		if (user == null) {
			return "login";
		} else {
			Struts2Utils.getResponse().sendRedirect("file/file.action");
			return null;
		}

 	}
 
 }
