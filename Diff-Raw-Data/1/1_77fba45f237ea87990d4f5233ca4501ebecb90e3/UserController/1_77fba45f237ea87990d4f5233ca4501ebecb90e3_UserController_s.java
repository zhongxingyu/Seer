 package com.bsg.pcms.user;
 
 import java.util.HashMap;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.bsg.pcms.dashboard.DashboardController;
 import com.bsg.pcms.dto.UserDTO;
 import com.bsg.pcms.utility.BigstarConstant;
 import com.bsg.pcms.utility.BigstarProperties;
 
 @Controller
 public class UserController {
 
 	private Logger logger = LoggerFactory.getLogger(UserController.class);
 
 	@Autowired
 	BigstarConstant bigstarConstant;
 
 	@Autowired
 	private DashboardController dashboardController;
 
 	@Autowired
 	private UserService userSevice;
 
 	@Autowired
 	private BigstarProperties bigstarProperties;
 
 	/**
 	 * PMC 초기화면
 	 * 
 	 * @return
 	 */
 	@RequestMapping(value = "index.do", method = RequestMethod.GET)
 	public String index() {
 
 		return "index";
 
 	}
 
 	@RequestMapping(value = "login", method = RequestMethod.POST)
 	public String login(UserDTO member, HttpServletRequest request) {
 
 		if (userSevice.hasNoUser(member)) {
 			return "redirect:/index.do";
 		}
 
 		UserDTO resultDTO = userSevice.getUser(member);
 
 		HashMap<String, Object> map = new HashMap<String, Object>();
 		map.put("id", resultDTO.getId());
 		map.put("level_cd", resultDTO.getLevel_cd());
 
 		request.getSession().setAttribute("user", map);
 
 		return "redirect:/dashboard.do"; 
 
 	}
 
 	@RequestMapping(value = "logout", method = RequestMethod.GET)
 	public String logout() {
 
 		return "redirect:/index.do";
 
 	}
 
 }
