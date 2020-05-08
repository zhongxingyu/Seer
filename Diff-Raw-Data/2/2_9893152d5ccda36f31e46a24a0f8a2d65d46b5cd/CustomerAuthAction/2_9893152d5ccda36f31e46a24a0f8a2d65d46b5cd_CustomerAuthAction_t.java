 package com.omartech.tdg.action.customer;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.omartech.tdg.mapper.NoticeMapper;
 import com.omartech.tdg.model.Customer;
 import com.omartech.tdg.model.Notice;
 import com.omartech.tdg.service.customer.CustomerAuthService;
 import com.omartech.tdg.utils.InputChecker;
 import com.omartech.tdg.utils.TaobaoSession;
 import com.omartech.tdg.utils.TaobaoSettings;
 import com.omartech.tdg.utils.UserType;
 import com.taobao.api.internal.util.WebUtils;
 
 @Controller
 public class CustomerAuthAction {
 	
 	@Autowired
 	private CustomerAuthService customerAuthService;
 	@Autowired
 	private NoticeMapper noticeMapper;
 	Logger logger = Logger.getLogger(CustomerAuthAction.class);
 	
 	@RequestMapping("/isCustomerEmailExist")
 	@ResponseBody
 	public boolean isEmailExist(@RequestParam String email){
 		boolean flag = customerAuthService.isEmailExist(email);
 		return flag;
 	}
 	
 	@RequestMapping(value="/loginascustomer")
 	public String loginAsCustomer(){
 		return "customer/auth/login";
 	}
 	@RequestMapping(value="/logoutascustomer")
 	public String logout(HttpSession session){
 		session.invalidate();
 		return "redirect:/customerindex";
 	}
 	@RequestMapping(value="/customerlogin", method=RequestMethod.POST)
 	public String customerLogin(
 			@RequestParam(value = "email", required = true) String email,
 			@RequestParam(value = "password", required = true) String password,
 			HttpSession session,
 			HttpServletResponse response){
 		if(InputChecker.emailChecker(email) && InputChecker.passwordChecker(password)){
 			Customer customer = customerAuthService.isLegalUser(email, password);
 			if(customer !=null ){
 				session.setAttribute("customer", customer);
 				response.addCookie(new Cookie("customer", email));
 				return "redirect:/customerindex";
 			}else{
 				logger.info("customer input a wrong email || password");
 				return "customer/auth/login";
 			}
 		}else{
 			return "redirect:/loginascustomer";
 		}
 	}
 	
 	@RequestMapping(value="/customerforgetpwd")
 	public String customerForgetPwd(){
 		return "customer/auth/forget";
 	}
 	
 	@RequestMapping(value="/registascustomer")
 	public String registAsCustomer(){
 		return "customer/auth/register";
 	}
 	
 	
 	@RequestMapping(value="/customerregister", method=RequestMethod.POST)
 	public ModelAndView customerRegister(
 			@RequestParam(value = "email", required = true) String email,
 			@RequestParam(value = "password", required = true) String password,
 			@RequestParam(value = "phoneNum", required = true) String phoneNum,
 			HttpSession session
 			){
 		if(phoneNum == null || phoneNum.trim().length()==0){
 			return new ModelAndView("customer/auth/register").addObject("message", "手机号必须填!");
 		}
		if(!customerAuthService.isEmailExist(email) && InputChecker.emailChecker(email) && InputChecker.passwordChecker(password)){
 			boolean flag = customerAuthService.isEmailExist(email);
 			Customer customer = null;
 			if(!flag){
 				customer = new Customer(email,password);
 				customer.setPhoneNum(phoneNum);
 				customerAuthService.add(customer);
 				session.setAttribute("customer", customer);
 			}
 			return new ModelAndView("customer/auth/confirm").addObject("customer", customer);
 		}else{
 			return new ModelAndView("customer/auth/register");
 		}
 	}
 	@RequestMapping("/customer/auth/welcome")
 	public ModelAndView welcome(){
 		Notice notice = noticeMapper.getNoticeByUserType(UserType.CUSTOMER);
 		return new ModelAndView("customer/auth/welcome").addObject("notice", notice);
 	}
 
 	
 	@RequestMapping("/customer/auth/show")
 	public ModelAndView showSelf(HttpSession session){
 		Customer customer = (Customer) session.getAttribute("customer");
 		return new ModelAndView("/customer/auth/show").addObject("customer", customer);
 	}
 	@RequestMapping("/customer/auth/edit")
 	public ModelAndView editSelf(HttpSession session){
 		Customer customer = (Customer) session.getAttribute("customer");
 		return new ModelAndView("/customer/auth/edit").addObject("customer", customer);
 	}
 	
 	@RequestMapping("/customer/auth/update")
 	public String updateSelf(
 			@RequestParam String oldPassword,
 			@RequestParam String password,
 			@RequestParam String phoneNum,
 			HttpSession session
 			){
 		Customer customer = (Customer) session.getAttribute("customer");
 		if(password == null || oldPassword == null){
 			customer.setPhoneNum(phoneNum);
 		}else{
 			if(oldPassword.equals(customer.getPassword())){
 				customer.setPassword(password);
 				customer.setPhoneNum(phoneNum);
 			}
 		}
 		customerAuthService.updatePassword(customer);
 		return "redirect:/customer/auth/show";
 	}
 	
 	
 	@RequestMapping("/customer/auth/taobao")
 	public ModelAndView showTaobao(HttpSession session){
 		Customer customer = (Customer) session.getAttribute("customer");
 		return new ModelAndView("/customer/auth/taobao").addObject("customer", customer);
 	}
 	
 	@RequestMapping("/customer/auth/taobao/new")
 	public ModelAndView connectTaobao(HttpSession session){
 		Customer customer = (Customer) session.getAttribute("customer");
 		
 		return new ModelAndView("/customer/auth/connect").addObject("customer", customer);
 	}
 	
 	@RequestMapping("/customer/auth/taobao/callback")
 	public ModelAndView taoBaoCallBack(
 			@RequestParam String code,
 			@RequestParam int state,
 			HttpSession session
 			){
 //		System.out.println("code: "+code);
 //		System.out.println("status: "+state);
 		
 		Map<String, String> param = new HashMap<String, String>();
 		param.put("grant_type", "authorization_code");
 		param.put("code", code);
 		param.put("client_id", TaobaoSettings.appKey);
 		param.put("client_secret", TaobaoSettings.appSecret);
 		param.put("redirect_uri", TaobaoSettings.callbackUrl);
 		param.put("scope", "item");
 		param.put("view", "web");
 		param.put("state", "1212");
 		String responseJson=null;
 		try {
 			responseJson = WebUtils.doPost(TaobaoSettings.oauthURL, param, 30000000, 30000000);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		Gson gson = new Gson();
 		TaobaoSession taobaoSession = gson.fromJson(responseJson, new TypeToken<TaobaoSession>(){}.getType()); 
 		String refreshToken = taobaoSession.getRefresh_token();
 		String accessToken = taobaoSession.getAccess_token();
 		
 		Customer customer = (Customer) session.getAttribute("customer");
 		customer.setAccessToken(accessToken);
 		customer.setRefreshToken(refreshToken);
 		customerAuthService.updateCustomer(customer);
 		
 		System.out.println(responseJson);
 		String message = responseJson;
 		
 		return new ModelAndView("/customer/auth/callback").addObject("message", message).addObject("customer", customer);
 	}
 	
 	public CustomerAuthService getCustomerAuthService() {
 		return customerAuthService;
 	}
 
 	public void setCustomerAuthService(CustomerAuthService customerAuthService) {
 		this.customerAuthService = customerAuthService;
 	}
 
 	public NoticeMapper getNoticeMapper() {
 		return noticeMapper;
 	}
 
 	public void setNoticeMapper(NoticeMapper noticeMapper) {
 		this.noticeMapper = noticeMapper;
 	}
 
 }
