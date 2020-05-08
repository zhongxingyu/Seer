 package com.asu.edu;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 
 import com.asu.edu.base.dao.intrf.SecurityDAOImplInterface;
 
 import com.asu.edu.base.vo.ChangePasswordVO;
 import com.asu.edu.base.vo.UserRegistrationServiceVO;
 import com.asu.edu.base.vo.UserVO;
 import com.asu.edu.cache.MasterCache;
 import com.asu.edu.constants.CommonConstants;
 
 
 
 @Controller
 public class LoginController {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(LoginController.class);
 
 	@Autowired
 	private SecurityDAOImplInterface securityDAO = null;
 	@Autowired
 	private UserRegistrationServiceVO userRegistrationService = null;
 
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String dispatcher(Model model, HttpSession session) {
 		if (securityDAO.isLoggedIn()) {
 			securityDAO.unlockUser();
 			//this will be executed when logged in user clicks on Home tab in the header
 			if(session.getAttribute(CommonConstants.USER)!=null){
 				return "redirect:/Dashboard?deptId=-1&folderId=-1";
 			}
 			UserVO userVO = securityDAO.getUserDetails(SecurityContextHolder
 					.getContext().getAuthentication());
 			if (userVO != null) {
 				/*
 				 * this is the only place where user details will be generated
 				 * and stored in the session
 				 */
 				if (userVO.getIsApproved() == 1)
 				{
 					session.setAttribute(CommonConstants.USER, userVO);
 					
 					if(MasterCache.getRoleMap().get(userVO.getRoleId()).getDesc().equals(CommonConstants.ROLE_ADMIN)) {
 						return "redirect:/admin";
 					}
 					if(MasterCache.getRoleMap().get(userVO.getRoleId()).getDesc().equals(CommonConstants.ROLE_GUEST_USR)) {
						return "redirect:/SharedToYouDocument?folderId=-1";
 					} 
 					
 					return "redirect:/Dashboard?deptId=-1&folderId=-1";
 				}
 				else{
 					return "redirect:/temp";
 				}
 			}
 
 		}
 		return "redirect:/loginfailed";
 	}
 
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public String login(Locale locale, Model model) {
 		logger.info("Welcome login! screen");
 		boolean isEnabled  = securityDAO.isCaptchaEnabled();
 		model.addAttribute("isEnabled", isEnabled);
 		System.out.println("At Login page:" + securityDAO.isLoggedIn());
 		return "login";
 	}
 	@RequestMapping(value = "/temp", method = RequestMethod.GET)
 	public String temp(Locale locale, Model model) {
 		logger.info("You are temporary User");
 		System.out.println("At Login page:" + securityDAO.isLoggedIn());
 		return "temp";
 	}
 
 	@RequestMapping(value = "/changePassword", method = RequestMethod.GET)
 	public String changePassword(Locale locale, Map<String, Object> model,
 			Model attr) {
 		logger.info("In Get");
 		model.put("changePasswordVO", new ChangePasswordVO());
 		String errorMessage = null;
 		attr.addAttribute("errorMessage", errorMessage);
 		return "changePassword";
 	}
 
 	@RequestMapping(value = "/changePassword", method = RequestMethod.POST)
 	public String changePassword(@Valid ChangePasswordVO changePasswordVO,
 			BindingResult bindingResult, HttpSession session, Model attr) {
 		logger.info("In post of changePassword");
 		String oldPassword = changePasswordVO.getOldPassword();
 		String newPassword = changePasswordVO.getNewPassword();
 		String rePassword = changePasswordVO.getRePassword();
 		String errorMessage;
 
 		if (bindingResult.hasErrors()) {
 			logger.info("In bindinResult has error");
 			FieldError fieldError = new FieldError("changePasswordVO",
 					"newPassword", "");
 			bindingResult.addError(fieldError);
 			return "changePassword";
 		}
 
 		if (rePassword.equals(newPassword)) {
 			UserVO currentUser = ((UserVO) session
 					.getAttribute(CommonConstants.USER));
 			boolean isValid = securityDAO.isValidPassword(
 					currentUser.getUserName(), oldPassword);
 			if (isValid) {
 				// set new Password for the user
 				securityDAO.setPasswordForUser(currentUser.getUserName(),
 						newPassword);
 				return "redirect:/login";
 
 			} else {
 				// Give error message that oldPassword is wrong
 				logger.info("oldPassword is wrong");
 				errorMessage = "Your old password is wrong";
 
 			}
 		} else {
 			// Retype password and password doesn't match
 			logger.info("Retype password and password doesn't match");
 			errorMessage = "Retype password and new password doesn't match";
 
 		}
 		attr.addAttribute("errorMessage", errorMessage);
 		return "changePassword";
 	}
 
 	@RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
 	public String forgotPassword(Locale locale, Map<String, Object> model) {
 		logger.info("In Get");
 		model.put("changePasswordVO", new ChangePasswordVO());
 		return "forgotPassword";
 	}
 
 	@RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
 	public String sendPassword(ChangePasswordVO changePasswordVO, HttpSession session, Model attr) {
 		logger.info("In post ");
 		logger.info(changePasswordVO.getUserName());
 
 		ServletContext context1 = session.getServletContext();
 		String realContextPath1 = context1.getRealPath("");
 		logger.info(realContextPath1);
 		String userName = changePasswordVO.getUserName();
 		String email_id = securityDAO.getEmailForUser(userName);
 		String errorMessage = null;
 
 		if (email_id == null) {
 			// User is not registered
 			logger.info("IN emailId Empty.");
 
 			errorMessage = "Invalid username";
 			attr.addAttribute("errorMessage", errorMessage);
 			return "forgotPassword";
 		} else {
 			logger.info(email_id);
 			/*
 			 * ApplicationContext context = new ClassPathXmlApplicationContext(
 			 * "/WEB-INF/spring/appServlet/mail-simple.xml");
 			 * UserRegistrationServiceVO service =
 			 * (UserRegistrationServiceVO)context
 			 * .getBean("userRegistrationService");
 			 */
 			Set<String> emailSet = new HashSet<String>(1);
 			emailSet.add(email_id);
 			userRegistrationService.setUserEmailIds(emailSet);
 			userRegistrationService.uponSuccessfulRegistration();
 			String strPasswd = userRegistrationService.getPassword();
 			securityDAO.setPasswordForUser(userName, strPasswd);
 			return "redirect:/login";
 		}
 
 	}
 
 	@RequestMapping(value = "/loginfailed", method = RequestMethod.GET)
 	public String loginFailed(Model model) {
 		logger.info("Welcome loginfailed! screen");
 		boolean isEnabled  = securityDAO.isCaptchaEnabled();
 		model.addAttribute("isEnabled", isEnabled);
 		return "login";
 	}
 
 	@RequestMapping(value = "/logout", method = RequestMethod.GET)
 	public String loginOut(Locale locale, Model model) {
 		logger.info("Welcome logout! screen");
 		return "logout";
 	}
 
 }
