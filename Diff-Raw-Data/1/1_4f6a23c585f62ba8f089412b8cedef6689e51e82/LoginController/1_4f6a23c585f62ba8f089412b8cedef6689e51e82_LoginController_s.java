 package com.chihuo.web.controller;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.chihuo.bussiness.Owner;
 import com.chihuo.bussiness.Restaurant;
 import com.chihuo.service.CityService;
 import com.chihuo.service.OwnerService;
 import com.chihuo.service.RestaurantService;
 import com.chihuo.service.UserContext;
 import com.chihuo.util.PinyinUtil;
 import com.chihuo.web.form.Signup;
 
 @Controller
 public class LoginController {
 	@Autowired
 	private UserContext userContext;
 
 	@Autowired
 	private OwnerService ownerService;
 
 	@Autowired
 	private RestaurantService restaurantService;
 
 	@Autowired
 	private CityService cityService;
 
 	@RequestMapping("/")
 	public String welcome(HttpServletRequest request) {
 
 		return "index";
 	}
 	
 	@RequestMapping("/test")
 	public String test(HttpServletRequest request) {
 
 		return "test";
 	}
 
 	@RequestMapping("/login/form")
 	public String login() {
 		return "login";
 	}
 
 	@RequestMapping("/signup/form")
 	public String signup(@ModelAttribute("signupForm") Signup signup,
 			Model model) {
 		return "signup";
 	}
 
 	@RequestMapping(value = "/signup/new", method = RequestMethod.POST)
 	public String signup(@ModelAttribute("signupForm") Signup signupForm,
 			BindingResult result, RedirectAttributes redirectAttributes) {
 		if (result.hasErrors()) {
 			return "signup";
 		}
 
 		if (StringUtils.isBlank(signupForm.getUserName())) {
 			result.rejectValue("userName", "userName", "用户名不能为空");
 		}
 		if (StringUtils.isBlank(signupForm.getRestaurantName())) {
 			result.rejectValue("restaurantName", "restaurantName", "餐厅名称不能为空");
 		}
 		if (StringUtils.isBlank(signupForm.getPassword())) {
 			result.rejectValue("password", "password", "密码不能为空");
 		}
 		if (StringUtils.isBlank(signupForm.getPassword2())) {
 			result.rejectValue("password2", "password2", "确定密码不能为空");
 		}
 		if (!StringUtils.isBlank(signupForm.getPassword())
 				&& !StringUtils.isBlank(signupForm.getPassword2())
 				&& !signupForm.getPassword().equals(signupForm.getPassword2())) {
 			result.rejectValue("password2", "password2", "两次密码不匹配");
 		}
 		if (ownerService.findByName(signupForm.getUserName()) != null) {
 			result.rejectValue("userName", "userName", "该用户名已被注册，更换用户名");
 		}
 
 		if (result.hasErrors()) {
 			return "signup";
 		}
 
 		Owner owner = ownerService.create(signupForm.getUserName(),
 				signupForm.getPassword());
 
 		Restaurant restaurant = new Restaurant();
 		restaurant.setName(signupForm.getRestaurantName());
 		restaurant.setOwner(owner);
 		restaurant.setPinyin(PinyinUtil.converterToFirstSpell(signupForm
 				.getRestaurantName()));
 		restaurant.setStatus(1);
 		// TODO 获取城市列表
 		restaurant.setCity(cityService.findById(1));
 		restaurantService.saveOrUpdate(restaurant);
 
 		userContext.setCurrentUser(owner);
 
 		redirectAttributes.addFlashAttribute("message", "Success");
 		return "redirect:/";
 	}
 
 	@RequestMapping("/errors/403")
 	public String error() {
 		return "/errors/403";
 	}
 }
