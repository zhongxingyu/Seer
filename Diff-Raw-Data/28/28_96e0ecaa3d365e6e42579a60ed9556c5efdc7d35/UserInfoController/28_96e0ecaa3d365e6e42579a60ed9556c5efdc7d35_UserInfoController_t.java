 package org.yogocodes.bikewars.web.personal;
 
import javax.servlet.http.HttpSession;

 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
import org.yogocodes.bikewars.errors.ResourceNotFoundException;
 import org.yogocodes.bikewars.model.UserInfoModel;
 import org.yogocodes.bikewars.services.UserInfoService;
import org.yogocodes.bikewars.util.UserSessionUtil;
import org.yogocodes.bikewars.web.AbstractBaseController;
 
 @Controller
public class UserInfoController extends AbstractBaseController {
 
 	private final Logger log = LoggerFactory.getLogger(UserInfoController.class);
 
 	@Autowired
 	private UserInfoService userInfoService;
 
 	@RequestMapping(value = "/personal/userinfos/{userId}")
 	@ResponseBody
 	public UserInfoModel getUserInfo(@PathVariable final Long userId) {
		log.trace("getting user info for user: {}", userId);
 		final UserInfoModel userInfo = getUserInfoService().getUserInfo(userId);
 		return userInfo;
 	}
 
	@RequestMapping(value = "/personal/userinfos/current")
	@ResponseBody
	public UserInfoModel getUserInfo(final HttpSession session) throws ResourceNotFoundException {

		final Long userId = UserSessionUtil.getUserId(session);

		if (userId == null) {
			throw new ResourceNotFoundException("Can't find user id for anonymous user");
		}

		log.trace("getting user info for current session: {}", userId);
		return getUserInfo(userId);
	}

 	public UserInfoService getUserInfoService() {
 		return userInfoService;
 	}
 
 	public void setUserInfoService(final UserInfoService userInfoService) {
 		this.userInfoService = userInfoService;
 	}
 }
