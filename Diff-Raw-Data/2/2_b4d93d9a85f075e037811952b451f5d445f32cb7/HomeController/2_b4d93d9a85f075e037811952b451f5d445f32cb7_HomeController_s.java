 package org.knetwork.webapp;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.knetwork.webapp.model.ExerciseContainer;
 import org.knetwork.webapp.oauth.KhanOAuthService;
 import org.knetwork.webapp.util.ApiHelper;
 import org.knetwork.webapp.util.KhanAcademyApi;
 import org.scribe.model.OAuthConstants;
 import org.scribe.model.Token;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @RequestMapping("/")
 public class HomeController {
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private final KhanOAuthService oauthService;
     private final KhanAcademyApi api;
 
     @Inject
     public HomeController(final KhanOAuthService oauthService, final KhanAcademyApi api) {
         this.oauthService = oauthService;
         this.api = api;
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String display(final HttpSession session, final HttpServletRequest request, final Model model) {
         final Token accessToken = (Token) session.getAttribute("accessToken");
         model.addAttribute("loggedIn", accessToken != null);
         if (accessToken == null) {
            final String callbackUrl = String.format("http://%s%s", request.getServerName(), OAuthConstants.CALLBACK);
             logger.debug("Callback url for OAuth is: " + callbackUrl);
             final String requestTokenUrl = oauthService.getRequestTokenUrl(callbackUrl);
             model.addAttribute("requestTokenUrl", requestTokenUrl);
             return "login";
         }
         final ApiHelper apiHelper = new ApiHelper(accessToken, oauthService, api);
         model.addAttribute("exercises", new ExerciseContainer(apiHelper.getExercises(), apiHelper.getBadges()));
         model.addAttribute("user", apiHelper.getUser());
         return "home";
     }
     
     @RequestMapping("logout")
     public String logout(HttpSession session) {
         session.removeAttribute("accessToken");
         return "redirect:/";
     }
 
 }
