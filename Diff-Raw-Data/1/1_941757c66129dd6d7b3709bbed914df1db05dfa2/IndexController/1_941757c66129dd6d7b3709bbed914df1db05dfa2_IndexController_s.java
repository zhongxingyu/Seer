 package com.belerweb.maohuahua.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.belerweb.maohuahua.model.Site;
 import com.belerweb.maohuahua.model.User;
 import com.belerweb.maohuahua.service.EmailService;
 import com.belerweb.maohuahua.service.ImageService;
 import com.belerweb.maohuahua.service.SmsService;
 import com.belerweb.maohuahua.service.TemplateService;
 import com.belerweb.maohuahua.service.UserService;
 
 @Controller
 public class IndexController extends ControllerHelper {
 
   @Autowired
   private UserService userService;
   @Autowired
   private TemplateService templateService;
   @Autowired
   private EmailService emailService;
   @Autowired
   private SmsService smsService;
   @Autowired
   private ImageService imageService;
 
   @RequestMapping({"/", "/index.html"})
   public Object index(HttpServletRequest request, Model model) {
     String host = request.getServerName();
     String subdomain = "";
     if (host.length() > 14) {
       subdomain = host.substring(0, host.length() - 14);
     }
 
     User user = userService.getUser("subDomain", subdomain);
     if (user != null) {
       Site site = userService.getUserSite(user.getId());
       model.addAttribute("site", site);
       model.addAttribute("imgs", imageService.getUserImages(user.getId()));
       return "/" + site.getTemplate() + "/index";
     }
 
     return "/login";
   }
 
   @RequestMapping("/login.html")
   public Object login(Model model) {
     return "/login";
   }
 
   /**
    * 注册
    */
   @RequestMapping(method = RequestMethod.POST, value = "/signup")
   public Object signup(@RequestParam String id) {
     if (!StringUtils.hasText(id)) {
       return error("请输入邮件地址或手机号。");
     }
     String account = id.trim();
     if (account.contains("@")) {
       if (!emailService.isValidEmail(account)) {
         return error("请输入正确的邮件地址。");
       }
 
       if (userService.getUser("email", account) != null) {
         return error("该邮件已被注册。");
       }
 
       User user = userService.signup("email", account);
       String emailContent = null;
       try {
         Map<String, String> dataModel = new HashMap<>();
         dataModel.put("account", account);
         dataModel.put("password", user.getPassword());
         emailContent = templateService.render("/signup_notify.ftl", dataModel);
       } catch (Exception e) {
         e.printStackTrace();
       }
       if (emailContent == null || !emailService.send(account, "猫画画网站注册通知", emailContent)) {
         return error("邮件发送失败，如需帮助，请电话联系 18996350680。");
       }
       return ok("登录密码已发送到您的邮箱，请查收。");
     }
 
     if (!smsService.isValidMobile(account)) {
       return error("请输入正确的手机号码。");
     }
     if (userService.getUser("mobile", account) != null) {
       return error("该手机号已被注册。");
     }
 
     User user = userService.signup("mobile", account);
     String smsContent = null;
     try {
       Map<String, String> dataModel = new HashMap<>();
       dataModel.put("account", account);
       dataModel.put("password", user.getPassword());
       smsContent = templateService.render("/signup_notify.ftl", dataModel);
     } catch (Exception e) {
       e.printStackTrace();
     }
     if (smsContent == null || !smsService.send(account, smsContent)) {
       return error("短信发送失败，如需帮助，请电话联系 18996350680。");
     }
 
     return ok("登录密码已发送到您的手机，请查收。");
   }
 
   /**
    * 忘记密码/找回密码
    */
   @RequestMapping(method = RequestMethod.POST, value = "/forgot_password")
   public Object forgotPassword(@RequestParam String id) {
     return ok("找回密码，请电话联系 18996350680。");
   }
 
   /**
    * 登录后跳转
    */
   @RequestMapping("/redirect")
   public Object redirect() {
     return "redirect:/home";
   }
 
   /**
    * 登录后跳转
    */
   @RequestMapping("/home")
   public Object home() {
     return "/v1/home";
   }
 
 }
