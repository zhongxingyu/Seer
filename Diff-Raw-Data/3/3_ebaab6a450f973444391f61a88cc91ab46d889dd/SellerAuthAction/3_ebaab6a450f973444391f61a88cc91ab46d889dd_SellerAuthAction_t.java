 package com.omartech.tdg.action.seller;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.omartech.tdg.mapper.NoticeMapper;
 import com.omartech.tdg.model.Notice;
 import com.omartech.tdg.model.Seller;
 import com.omartech.tdg.service.seller.SellerAuthService;
 import com.omartech.tdg.utils.UserType;
 
 @Controller
 public class SellerAuthAction {
 	Logger logger = Logger.getLogger(SellerAuthAction.class);
 	@Autowired
 	private SellerAuthService sellerAuthService;
 	@Autowired
 	private NoticeMapper noticeMapper;
 
 	@RequestMapping(value="/loginasseller")
 	public String loginAsSeller(HttpSession session){
 		Seller seller = (Seller) session.getAttribute("seller");
 		if(seller!=null){
 			return "redirect:/seller/welcome";
 		}
 		return "seller/auth/login";
 	}
 	
 	@RequestMapping(value="/seller/logout")
 	public String logout(HttpSession session){
 		session.invalidate();
 		return "redirect:/sellerindex";
 	}
 	
 	@RequestMapping(value="/sellerlogin", method=RequestMethod.POST)
 	public String sellerLogin(@RequestParam(value="email") String email,
 			@RequestParam(value="password") String password, HttpSession session){
 		logger.info("seller login:"+email+" - "+password);
 		Seller seller = sellerAuthService.getSellerByEmailAndPassword(email, password);
 		if(seller!=null){
 			session.setAttribute("seller", seller);
 			return "redirect:/seller/welcome";
 		}else{
 			return "redirect:/loginasseller";
 		}
 	}
 	
 	@RequestMapping(value="/seller/welcome")
 	public ModelAndView welcome(){
 		Notice notice = noticeMapper.getNoticeByUserType(UserType.SELLER);
 		return new ModelAndView("seller/auth/welcome").addObject("notice", notice);
 	}
 	
 	
 	@RequestMapping(value="/sellerforgetpwd")
 	public String sellerForgetPwd(){
 		return "seller/auth/forget";
 	}
 	
 	@RequestMapping(value="/registerasseller")
 	public String registerAsSeller(){
 		return "seller/auth/register";
 	}
 	
 	@RequestMapping(value="/sellerregist", method=RequestMethod.POST)
 	public String sellerRegister(
 		@RequestParam(value="email", required=true) String email,
 		@RequestParam(value="password", required=true) String password,
 		@RequestParam String businessName,
 		@RequestParam String firstName,
 		@RequestParam String lastName,
 		@RequestParam String businessAddress,
 		@RequestParam String city,
		@RequestParam String state,
 		@RequestParam String country,
 		@RequestParam String primaryPhoneNumber,
 		@RequestParam String productLines,
 		@RequestParam String secondPhoneNumber,
 		@RequestParam String companyWebsiteAddress,
 		HttpSession session){
 		Seller checkSeller = sellerAuthService.getSellerByEmail(email);
 		if(checkSeller == null){
 			Seller seller = new Seller();
 			seller.setEmail(email);
 			seller.setPassword(password);
 			seller.setBusinessName(businessName);
 			seller.setFirstName(firstName);
 			seller.setLastName(lastName);
 			seller.setBusinessAddress(businessAddress);
 			seller.setCity(city);
			seller.setState(state);
 			seller.setCountry(country);
 			seller.setPrimaryPhoneNumber(primaryPhoneNumber);
 			seller.setProductLines(productLines);
 			seller.setSecondPhoneNumber(secondPhoneNumber);
 			seller.setCompanyWebsiteAddress(companyWebsiteAddress);
 			sellerAuthService.insertSeller(seller);
 			session.setAttribute("seller", seller);
 			return "redirect:/seller/welcome";
 		}else{
 			return "redirect:/sellerindex";
 		}
 	}
 	
 	@RequestMapping("/seller/auth/show")
 	public ModelAndView showProfile(HttpSession session){
 		Seller se = (Seller)session.getAttribute("seller");
 		String email = se.getEmail();
 		Seller seller = sellerAuthService.getSellerByEmail(email);
 		return new ModelAndView("/seller/auth/show").addObject("selller", seller);
 	}
 	
 	@RequestMapping("/seller/auth/edit")
 	public ModelAndView editProfile(HttpSession session){
 		Seller se = (Seller)session.getAttribute("seller");
 		String email = se.getEmail();
 		Seller seller = sellerAuthService.getSellerByEmail(email);
 		return new ModelAndView("/seller/auth/modify").addObject("selller", seller);
 	}
 	@RequestMapping("/seller/auth/update")
 	public String editProfile(@RequestParam String email, @RequestParam String password,HttpSession session){
 		Seller se = (Seller)session.getAttribute("seller");
 		String em = se.getEmail();
 		if(em.equals(email)){
 			se.setPassword(password);
 		}
 		sellerAuthService.updateSeller(se);
 		return "redirect:/seller/auth/show";
 	}
 
 	public SellerAuthService getSellerAuthService() {
 		return sellerAuthService;
 	}
 
 	public void setSellerAuthService(SellerAuthService sellerAuthService) {
 		this.sellerAuthService = sellerAuthService;
 	}
 
 	public NoticeMapper getNoticeMapper() {
 		return noticeMapper;
 	}
 
 	public void setNoticeMapper(NoticeMapper noticeMapper) {
 		this.noticeMapper = noticeMapper;
 	}
 	
 }
