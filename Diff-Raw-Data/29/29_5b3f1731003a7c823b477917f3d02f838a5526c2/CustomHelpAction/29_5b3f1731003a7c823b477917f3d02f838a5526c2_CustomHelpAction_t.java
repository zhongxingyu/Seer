 package com.omartech.tdg.action.customer;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.omartech.tdg.model.HelpService;
 import com.omartech.tdg.service.HelpServiceService;
 
 @Controller
 @RequestMapping(value="/help")
 public class CustomHelpAction {
 	@Autowired
 	private HelpServiceService helpServiceService; 
 	
 	@RequestMapping(value="/license")
 	public String registerLicense(){
 		return "/customer/help/license";
 	}
 	
 	@RequestMapping(value="/shoppingProcess")
 	public ModelAndView shoppingProcesshelp(){
 		HelpService helpService = helpServiceService.getHelpService();
 		String shoppingProcess =" ";
 		if(helpService != null){
 			shoppingProcess = helpService.getShoppingProcess();
 		}
 		//String shoppingProcess = "1. 注册帐号。<br>2. 查找商品。<br>3. 放入购物车。<br>4. 提交订单。<br>5. 查看订单状态";
 		return new ModelAndView("/customer/help/shopping-process").addObject("shoppingProcess", shoppingProcess);
 	}
 	
 	@RequestMapping(value="/afterSalePolicy")
 	public ModelAndView afterSaleServiceHelp(){
 		HelpService helpService = helpServiceService.getHelpService();
 		String afterSalePolicy =" ";
 		if(helpService != null){
 			afterSalePolicy = helpService.getAfterSalePolicy();
 		}
 		return new ModelAndView("/customer/help/after-sale-policy").addObject("afterSalePolicy", afterSalePolicy);
 	}
 	
 	@RequestMapping(value="/cashAfterDelivery")
 	public ModelAndView cashOnDeliveryHelp(){
 		HelpService helpService = helpServiceService.getHelpService();
 		String cashAfterDelivery =" ";
 		if(helpService != null){
 			cashAfterDelivery = helpService.getCashAfterDelivery();
 		}
 		return new ModelAndView("/customer/help/cash-after-delivery").addObject("cashAfterDelivery", cashAfterDelivery);
 	}
 	
 	@RequestMapping(value="/contactCustomService")
 	public ModelAndView contactServiceHelp(){
 		HelpService helpService = helpServiceService.getHelpService();
 		String contactCustomService =" ";
 		if(helpService != null){
 			contactCustomService = helpService.getContactCustomService();
 		}
 		return new ModelAndView("/customer/help/contact-custom-service").addObject("contactCustomService", contactCustomService);
 	}
 	
 	@RequestMapping(value="/FAQ")
 	public ModelAndView FAQHelp(){
 		HelpService helpService = helpServiceService.getHelpService();
 		String FAQ =" ";
 		if(helpService != null){
 			FAQ = helpService.getFAQ();
 		}
 		return new ModelAndView("/customer/help/FAQ").addObject("FAQ", FAQ);
 	}
 	
 	@RequestMapping(value="/orderStatus")
 	public ModelAndView orderStatusHelp(){
 		HelpService helpService = helpServiceService.getHelpService();
 		String orderStatus =" ";
 		if(helpService != null){
 			orderStatus = helpService.getOrderStatus();
 		}
 		return new ModelAndView("/customer/help/order-status").addObject("orderStatus", orderStatus);
 	}
 	
 	@RequestMapping(value="/payOnline")
 	public ModelAndView payOnlineHelp(){
 		HelpService helpService = helpServiceService.getHelpService();
 		String payOnline =" ";
 		if(helpService != null){
 			payOnline = helpService.getPayOnline();
 		}
 		return new ModelAndView("/customer/help/pay-online").addObject("payOnline", payOnline);
 	}
 	
 	@RequestMapping(value="/priceProtect")
 	public ModelAndView priceProtectHelp(){
 		HelpService helpService = helpServiceService.getHelpService();
 		String priceProtect =" ";
 		if(helpService != null){
 			priceProtect = helpService.getPriceProtect();
 		}
 		return new ModelAndView("/customer/help/price-protect").addObject("priceProtect", priceProtect);
 	}
 	
 	@RequestMapping(value="/refundInstruction")
 	public ModelAndView refundInstructionHelp(){
 		HelpService helpService = helpServiceService.getHelpService();
 		String refundInstruction =" ";
 		if(helpService != null){
 			refundInstruction = helpService.getRefundInstruction();
 		}
 		return new ModelAndView("/customer/help/refund-instruction").addObject("refundInstruction", refundInstruction);
 	}
 	
 	@RequestMapping(value="/thirdPartyExpress")
 	public ModelAndView thirdPartyExpressHelp(){
 		HelpService helpService = helpServiceService.getHelpService();
 		String thirdPartyExpress =" ";
 		if(helpService != null){
			thirdPartyExpress = helpService.getThirdPartyExpress();
 		}
 		return new ModelAndView("/customer/help/third-party-express").addObject("thirdPartyExpress", thirdPartyExpress);
 	}
 	
 	@RequestMapping(value="/tradeRules")
 	public ModelAndView tradeRulesHelp(){
 		HelpService helpService = helpServiceService.getHelpService();
 		String tradeRules =" ";
 		if(helpService != null){
 			tradeRules = helpService.getTradeRules();
 		}
 		return new ModelAndView("/customer/help/trade-rules").addObject("tradeRules", tradeRules);
 	}
 	
 	@RequestMapping(value="/vipIntroduction")
 	public ModelAndView vipIntroductionHelp(){
 		HelpService helpService = helpServiceService.getHelpService();
 		String vipIntroduction =" ";
 		if(helpService != null){
 			vipIntroduction = helpService.getVipIntroduction();
 		}
 		return new ModelAndView("/customer/help/vip-introduction").addObject("vipIntroduction", vipIntroduction);
 	}
 	
 
 }
