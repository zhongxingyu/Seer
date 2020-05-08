 package com.worksnet.controller;
 
 import com.worksnet.model.User;
 import com.worksnet.model.oauth.GitHubAuth;
 import com.worksnet.service.OAuthService;
 import com.worksnet.service.UserService;
 import com.worksnet.system.Conf;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * User: maxim.levicky
  * Date: 4/10/13
  * Time: 5:56 PM
  */
 @Controller
 public class OAuthController extends BaseController {
 
     @Autowired
     private OAuthService service;
 
     @Autowired
     @Qualifier("userService")
     private UserService userService;
 
     @RequestMapping(value = "/oauth/github", method = RequestMethod.GET)
     public String githubAuthPage(Model model, @RequestParam(value = "code", required = false) String code, HttpServletRequest request) {
         Authentication qwe = SecurityContextHolder.getContext().getAuthentication();
         if (code != null) {
             try {
                 GitHubAuth gitHubAuth = service.getGitHubLoginByCode(code);
                 User user = service.userByGitHubAuth(gitHubAuth);
                 if (null != user) {
                     userService.loginByUser(user, request);
                 } else {
                     if (UserService.isAuthorized()) {
                         gitHubAuth.setUser(UserService.getCurrentUser());
                         service.addGitHubAuth(gitHubAuth);
                     } else {
                         service.addGitHubAuthWithNewUser(gitHubAuth);
                         userService.loginByUser(user, request);
                     }
                 }
                return "redirect:/";
             } catch (OAuthService.OAuthError oAuthError) {
                 oAuthError.printStackTrace();
 //                result.rejectValue("OAuthError", "OAuth.cantAuth");
             }
         }
         qwe = SecurityContextHolder.getContext().getAuthentication();
         model.addAttribute("clientId", Conf.get("github.clientId"));
         return "auth/oauth/github/auth";
     }
 
 
 }
