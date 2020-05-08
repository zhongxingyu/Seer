 package org.yogocodes.bikewars.web.personal;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.yogocodes.bikewars.model.UserInfoModel;
 import org.yogocodes.bikewars.services.UserInfoService;
 
 @Controller
public class UserInfoController {
 
 	private final Logger log = LoggerFactory.getLogger(UserInfoController.class);
 
 	@Autowired
 	private UserInfoService userInfoService;
 
 	@RequestMapping(value = "/personal/userinfos/{userId}")
 	@ResponseBody
 	public UserInfoModel getUserInfo(@PathVariable final Long userId) {

 		final UserInfoModel userInfo = getUserInfoService().getUserInfo(userId);
 		return userInfo;
 	}
 
 	public UserInfoService getUserInfoService() {
 		return userInfoService;
 	}
 
 	public void setUserInfoService(final UserInfoService userInfoService) {
 		this.userInfoService = userInfoService;
 	}
 }
