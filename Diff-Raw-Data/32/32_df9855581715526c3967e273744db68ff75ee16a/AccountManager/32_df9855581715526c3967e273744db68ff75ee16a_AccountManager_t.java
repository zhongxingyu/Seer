 //liuchong@baixing.com
 package com.baixing.data;
 
 import android.content.Context;
 
 import com.baixing.entity.UserBean;
 import com.baixing.util.Util;
 
 /**
  * 
  * @author liuchong
  *
  */
 public class AccountManager {
 	private static UserBean currentUser;
	private static UserBean anonymousUser;
 	AccountManager() {
 		
 	}
 	
 	public void logout() {
 		currentUser = null;
 		
 		UserBean anonymousUser = (UserBean) Util.loadDataFromLocate(GlobalDataManager.getInstance().getApplicationContext(), "anonymousUser", UserBean.class);
 		if(anonymousUser != null){
 			Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", anonymousUser);
 			currentUser = anonymousUser;
 		}
 	}
 	
	public UserBean getAnonymousUser() {
		if (anonymousUser != null) {
			return anonymousUser;
		}
		
		anonymousUser = (UserBean) Util.loadDataFromLocate(GlobalDataManager.getInstance().getApplicationContext(), "anonymousUser", UserBean.class);
		return anonymousUser;
	}
	
 	/**
     *
     * @return 返回当前 UserBean user，未登录情况下返回 null
     */
    public UserBean getCurrentUser() {
 		if (currentUser != null) {
 			return currentUser;
 		}
 		currentUser = reloadUser();
 		return currentUser;
    }
    
 	public boolean isUserLogin() {
 		UserBean user = getCurrentUser();
 		return user != null && user.getPhone() != null
 				&& user.getPhone().length() > 0;
 	}
 
 	public String getMyId(Context context) {
 		if (currentUser != null) {
 			return currentUser.getId();
 		}
 
 		currentUser = getCurrentUser();
 
 		return currentUser == null ? null : currentUser.getId();
 	}
 
 	public String refreshAndGetMyId(Context context) {
 		currentUser = null;
 		currentUser = getCurrentUser();
 
 		return currentUser == null ? null : currentUser.getId();
 	}
 
 	public UserBean reloadUser() {
 		UserBean user = (UserBean) Util.loadDataFromLocate(GlobalDataManager
 				.getInstance().getApplicationContext(), "user", UserBean.class);
 		return user;
 	}
 }
