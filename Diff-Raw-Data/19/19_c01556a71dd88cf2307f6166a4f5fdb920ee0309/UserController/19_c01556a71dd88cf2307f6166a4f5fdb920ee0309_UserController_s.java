 package com.x.web.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import weibo4j.Weibo;
 import weibo4j.WeiboException;
 import weibo4j.http.AccessToken;
 import weibo4j.http.RequestToken;
 
 import com.x.web.constant.OauthSource;
 import com.x.web.constant.SessionKey;
 import com.x.web.domain.SinaOauthUser;
 import com.x.web.domain.User;
 import com.x.web.exception.AuthException;
 import com.x.web.formbean.UserRegForm;
 import com.x.web.service.UserManagementService;
 import com.x.web.util.FlashMap;
 
 /**
  * 
  * @author sqlxx
  *
  */
 @Controller
 @RequestMapping("/user/*")
 public class UserController extends BaseController{
     @Autowired
     private UserManagementService ums;
     
    
     @RequestMapping(value="login", method=RequestMethod.POST)
     public String login(@RequestParam("username") String userName, @RequestParam String password, HttpSession session) {
         try {
             
             if (userName == null || userName.trim().length() == 0) {
                 setErrorMessage("用户名或email不能为空");
                 return "xweb.login";
             } else if (password == null || password.length() == 0) {
                 setErrorMessage("密码不能为空");
                 return "xweb.login";
             }
             
             User user = ums.login(userName, password);
             if (user != null) {
                 session.setAttribute(SessionKey.USER, user);
                 FlashMap.setSuccessMessage("欢迎 " + userName + ", 你目前的状态是：" + user.getStatus().toString());
                 return "redirect:/main";
             } else {
                 setErrorMessage("登录失败");
                 return "xweb.login";
             }
         } catch (AuthException ex) {
             setErrorMessage(ex.getMessage());
             return "xweb.login";
         }
     }
     
     @RequestMapping("logout") 
     public String logout(HttpSession session){
         session.removeAttribute(SessionKey.USER);
         setInfoMessage("请重新登录");
         return "xweb.login";
         
     }
     
     @RequestMapping(value="login", method=RequestMethod.GET)
     public String login() {
         return "xweb.login";
     }
     
     @RequestMapping(value="register", method=RequestMethod.POST)
     public String register(@Valid UserRegForm userRegForm, BindingResult result) {
         
         if (result.hasErrors()) {
             setErrorMessage("用户注册失败");
             return "xweb.register";
         } else if (userRegForm.getPassword() != null && !userRegForm.getPassword().equals(userRegForm.getVerifyPassword())) {
             setErrorMessage("两次密码输入不同");
             return "xweb.register";
         }
         
         User user = ums.registerUser(userRegForm.getUsername(), userRegForm.getEmail(), userRegForm.getPassword());
         if (user != null) {
             FlashMap.setSuccessMessage("注册成功" + user.getUsername() + "请登录");
             return "redirect:login";
         } else {
             FlashMap.setErrorMessage("注册失败");
         }
         return "redirect:register";
     }
 
     
     @RequestMapping(value="register", method=RequestMethod.GET)
     public String register(HttpSession session, Model model) {
         UserRegForm userForm = (UserRegForm) session.getAttribute("userRegForm");
         model.addAttribute(userForm != null ? userForm : new UserRegForm());
         return "xweb.register";
     }
     
     @RequestMapping(value="login/sina", method=RequestMethod.GET)
     public String sinaLogin(HttpServletRequest request, HttpSession session) {
         try {
             Weibo weibo = new Weibo();
            
             String baseUrl = "http://" + request.getServerName() + (request.getServerPort() == 80? "" : ":" + request.getServerPort()) + request.getContextPath(); 
             RequestToken rt = weibo.getOAuthRequestToken(baseUrl + "/user/login/sinaSuccess");
             String tokenSecret = rt.getTokenSecret();
             session.setAttribute(SessionKey.SINA_TOKEN_SECRET, tokenSecret);
             return "redirect:" + rt.getAuthorizationURL();
         } catch (WeiboException ex){
             throw new AuthException("Authentication for Sina Weibo failed.", ex);
         }
     }
         
     @RequestMapping(value="login/sinaSuccess", method=RequestMethod.GET)
     public String sinaLoginSuccess(HttpSession session, @RequestParam("oauth_token") String oauthToken, @RequestParam("oauth_verifier") String oauthVerifier) {
         String tokenSecret = (String)session.getAttribute(SessionKey.SINA_TOKEN_SECRET);
         if (oauthToken == null || oauthVerifier == null || tokenSecret == null) {
             throw new AuthException("Invalid request.");
         } else {
             try {
                 Weibo weibo = new Weibo();
                 AccessToken accessToken = weibo.getOAuthAccessToken(oauthToken, tokenSecret, oauthVerifier);
                 SinaOauthUser sinaOauthUser = ums.saveOrUpdateSinaOauthUser(accessToken.getUserId(), accessToken.getToken(), accessToken.getTokenSecret());
                 if (sinaOauthUser.getUserId() == null) {
 //                    weibo4j.User user = weibo.showUser(sinaOauthUser.getSinaUserId().toString());
                    User localUser = ums.createUser(OauthSource.SINA.toString() + sinaOauthUser.getSinaUserId(), OauthSource.SINA);
                     session.setAttribute(SessionKey.USER, localUser);
                     FlashMap.setSuccessMessage("欢迎来自新浪的用户: " + localUser.getUsername());
                 } else {
                     User user = ums.login(sinaOauthUser.getUserId());
                     user.setLoginFromOauth(true);
                     user.setOauthSource(OauthSource.SINA);
                     session.setAttribute(SessionKey.USER, user);
                     FlashMap.setSuccessMessage("欢迎来自新浪的用户： " + user.getUsername());
                 }
                 
                 return "redirect:/main";
                 
             } catch(WeiboException ex) {
                 throw new AuthException("Exception when login by Sina id.", ex);
             }
         }
     }
 }
