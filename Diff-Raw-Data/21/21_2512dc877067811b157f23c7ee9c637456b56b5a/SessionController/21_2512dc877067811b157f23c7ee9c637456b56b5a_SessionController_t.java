 package se.ryttargardskyrkan.cordate.controller;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.web.filter.AccessControlFilter;
 import org.apache.shiro.web.util.SavedRequest;
 import org.apache.shiro.web.util.WebUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 import org.springframework.web.servlet.support.RequestContextUtils;
 
 import se.ryttargardskyrkan.cordate.model.UserSession;
 
 @Controller
 public class SessionController {
 	
 	@Autowired
 	private UserSession userSession;
 	
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String getIndex() {
 		return "index";
 	}
 	
 	@RequestMapping(value = "/sessions", method = RequestMethod.POST)
 	public String postSession(@RequestParam String username, @RequestParam String password, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) throws IOException {
 		try {
 			SecurityUtils.getSubject().login(new UsernamePasswordToken(username, password));
 			
 			userSession.setUsername(username);
 			userSession.setPassword(password);
 		
 			String successUrl = null;
 	        boolean contextRelative = true;
 	        SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(request);
 	        if (savedRequest != null && savedRequest.getMethod().equalsIgnoreCase(AccessControlFilter.GET_METHOD)) {
 	            successUrl = savedRequest.getRequestUrl();
 	            contextRelative = false;
 	        }
 
 	        if (successUrl != null) {
 	        	WebUtils.issueRedirect(request, response, successUrl, null, contextRelative);
 	        	return null;
 	        } else 
 	        	return "redirect:/";
 		} catch (AuthenticationException e) {
			String errorMessage = e.getMessage();

            redirectAttributes.addFlashAttribute("loginFailed", true);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
 			redirectAttributes.addFlashAttribute("username", username);
 			return "redirect:/login";
 		}
 	}
 	
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public ModelAndView getLogin(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
 		ModelAndView modelAndView = new ModelAndView("login");
 		
 		response.addHeader("X-cordate-login", "true");
 		
 		Map<String, ?> map = RequestContextUtils.getInputFlashMap(request); 
         if (map != null) {
         	modelAndView.addObject("loginFailed", map.get("loginFailed"));
            modelAndView.addObject("errorMessage", map.get("errorMessage"));
         	modelAndView.addObject("username", map.get("username"));
         }
 		
 		return modelAndView;
 	}
 	
 	@RequestMapping(value = "/logout", method = RequestMethod.GET)
 	public String getLogout() {
 		SecurityUtils.getSubject().logout();
 		return "redirect:/";
 	}
 }
