 package com.action;
 
 import java.util.Map;
 
 import com.dao.SysuserDao;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import com.pojo.Sysuser;
 
 public class EmployeeWelcomeAction extends ActionSupport {
 
 	private Sysuser user;
 	private String oldPassword;
 	private String newPassword;
 	private String confirmPassword;
 	private int isSuccess;
 
 	public int getIsSuccess() {
 		return isSuccess;
 	}
 
 	public void setIsSuccess(int isSuccess) {
 		this.isSuccess = isSuccess;
 	}
 
 	public Sysuser getUser() {
 		return user;
 	}
 
 	public void setUser(Sysuser user) {
 		this.user = user;
 	}
 
 	public String getOldPassword() {
 		return oldPassword;
 	}
 
 	public void setOldPassword(String oldPassword) {
 		this.oldPassword = oldPassword;
 	}
 
 	public String getNewPassword() {
 		return newPassword;
 	}
 
 	public void setNewPassword(String newPassword) {
 		this.newPassword = newPassword;
 	}
 
 	public String getConfirmPassword() {
 		return confirmPassword;
 	}
 
 	public void setConfirmPassword(String confirmPassword) {
 		this.confirmPassword = confirmPassword;
 	}
 	
 	public String welcome() {
 		Map session = ActionContext.getContext().getSession();
 		user = (Sysuser) session.get(LoginAction.SYSUSER);
 		return SUCCESS;
 	}
 
 	public String changePassword() {
 		this.isSuccess = 0;
 		return SUCCESS;
 	}
 	
 	public static synchronized boolean checkAndChange(int userId, String oldPassword, String newPassword) {
 		Sysuser changePswUser = null;
 		
 		try {
 			changePswUser =SysuserDao.getInstance().getByUserId(userId);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
		if (changePswUser == null) {
			return false;
		}
		if(oldPassword != null && !AuthorizationFilter.MD5(oldPassword).equals(changePswUser.getPassword())){
 			return false;
 		}else{
 			changePswUser.setPassword(AuthorizationFilter.MD5(newPassword));
 			try {
 				SysuserDao.getInstance().update(changePswUser);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return false;
 			}
 			return true;
 		}		
 	}
 	
 	public String submit(){
 		Map session = ActionContext.getContext().getSession();
 		user = (Sysuser) session.get(LoginAction.SYSUSER);
 		if (oldPassword != null && newPassword != null && confirmPassword != null) {
 			oldPassword.trim();
 			newPassword.trim();
 			confirmPassword.trim();
 			if (newPassword.equals("")) {
 				this.addActionError("Password can not be empty, or only space!");
 				this.isSuccess = -1;
 				return ERROR;
 			} else if(!newPassword.equals(confirmPassword)){
 				this.addActionError("Confirm password is not the same as new password!");
 				this.isSuccess = -1;
 				return ERROR;
 			}
 			
 			if(!checkAndChange(user.getId(), oldPassword, newPassword)){
 				this.addActionError("Current password is Incorrect!");
 				this.isSuccess = -1;
 				return ERROR;
 			}else{
 				this.isSuccess = 1;
 				return SUCCESS;
 			}			
 		}
 		this.addActionError("Password shouldn't be empty.");
 		this.isSuccess = -1;
 		return ERROR;
 	}
 }
