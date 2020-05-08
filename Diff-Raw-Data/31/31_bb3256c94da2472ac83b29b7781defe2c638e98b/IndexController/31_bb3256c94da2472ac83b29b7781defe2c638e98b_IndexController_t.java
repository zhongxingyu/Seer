 package com.cqlybest.www.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.cqlybest.common.bean.LoginUser;
 import com.cqlybest.common.bean.PhoneValidationCode;
 import com.cqlybest.common.service.PhoneValidationService;
 import com.cqlybest.common.service.SmsService;
 import com.cqlybest.common.service.TemplateService;
 import com.cqlybest.common.service.UserService;
 
 @Controller
 public class IndexController {
 
   @Autowired
   private TemplateService templateService;
   @Autowired
   private PhoneValidationService phoneValidationService;
   @Autowired
   private SmsService smsService;
   @Autowired
   private UserService userService;
 
   @RequestMapping( {"/index.html", // 首页
       "/register.html",// 注册
       "/login.html",// 登录
       "/group/{id}.html",// 产品聚合
       "/group/{id}/{f0}-{f1}-{f2}-{f3}-{f4}-{f5}-{f6}-{f7}-{page}.html",// 产品聚合
       "/page/{id}.html",// 自定义页面
       "/product/{id}.html"// 自定义页面
   })
   public String forward(HttpServletRequest request) {
     return templateService.forward(request.getRequestURI());
   }
 
   @RequestMapping(method = RequestMethod.POST, value = "/register.do")
   @ResponseBody
   public String register(@RequestParam String cellPhone, @RequestParam String password,
       @RequestParam String validationCode, HttpSession session) {
     PhoneValidationCode code = (PhoneValidationCode) session.getAttribute("PHONE_VALIDATION_CODE");
     if (code == null || !validationCode.equalsIgnoreCase(code.getCode())) {
       return "验证码不正确";
     }
     if (userService.getUserByCellPhone(cellPhone) != null) {
       return "手机号已注册，您可以用此手机号登录。如果忘记，可以使用找回密码功能。";
     }
     userService.addUser(new LoginUser(cellPhone, password));
     session.removeAttribute("PHONE_VALIDATION_CODE");
     return null;
   }
 
   @RequestMapping(method = RequestMethod.POST, value = "/register_phone_validation.do")
   @ResponseBody
   public String registerPhoneValidation(@RequestParam String cellPhone, HttpSession session) {
     if (!cellPhone.matches("^1[3458]\\d{9}$")) {
       return "手机号码不正确";
     }
     if (phoneValidationService.checkSendAvailable(cellPhone)) {
       String code = RandomStringUtils.randomNumeric(4);
       PhoneValidationCode validationCode = new PhoneValidationCode(cellPhone, code);
       phoneValidationService.save(validationCode);
      smsService.send(null, "系统", cellPhone, null, "您正在重庆易游天下网站注册帐户，您的手机验证码是" + code + "，使用一次后失效。");
       session.setAttribute("PHONE_VALIDATION_CODE", validationCode);
       return null;
     }
     return "一分钟只能发送一次验证码，请稍后再试";
   }
 
 }
