 package com.omartech.tdg.action.customer;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.omartech.tdg.mapper.ShopSettingMapper;
 import com.omartech.tdg.model.ClaimItem;
 import com.omartech.tdg.model.Order;
 import com.omartech.tdg.model.ShopSetting;
 import com.omartech.tdg.service.OrderService;
 import com.omartech.tdg.utils.ClaimRelation;
 import com.omartech.tdg.utils.JsonMessage;
 import com.omartech.tdg.utils.OrderStatus;
 
 @Controller
 public class CustomerOrderAction {
 	@Autowired
 	private OrderService orderService;
 	@Autowired
 	private ShopSettingMapper shopSettingMapper;
 	
 	/**
 	 * 买家收到货
 	 * @param orderId
 	 * @return
 	 */
 	@RequestMapping("/customer/order/receive/{orderId}")
 	public String receive(@PathVariable int orderId){
 		orderService.updateOrderStatus(OrderStatus.RECEIVE, orderId);
 		return "redirect:/customer/orders/all";
 	}
 	
 	/**
 	 * 显示退货页面
 	 */
 	@RequestMapping("/customer/order/returnShow/{orderId}")
 	public ModelAndView returnShow(@PathVariable int orderId){
 		
 		Order order = orderService.getOrderById(orderId);
 		ModelAndView mav = new ModelAndView("/customer/order/order-return").addObject("orderId", orderId);
 		if(order != null){
 			int sellerId = order.getSellerId();
 			ShopSetting shopSetting = shopSettingMapper.getShopSettingBySellerId(sellerId);
 			mav.addObject("shopSetting", shopSetting);
 		}
 		return mav;
 	}
 	/**
 	 * 买家确认退货
 	 */
 	@RequestMapping(value="/customer/order/returnConfirm", method=RequestMethod.POST)
 	public String confirmReturn(@RequestParam String comment, @RequestParam int orderId){
 		orderService.claimOrder(orderId, comment, ClaimRelation.Return);
 		return "redirect:/customer/orders/return";
 	}
 	
 	/**
 	 * 显示投诉页面
 	 * @param orderId
 	 * @return
 	 */
 	@RequestMapping("/customer/order/complainShow/{orderId}")
 	public ModelAndView complainShow(@PathVariable int orderId){
 		return new ModelAndView("/customer/order/order-complain").addObject("orderId", orderId);
 	}
 	public static int daysOfTwo(Date fDate, Date oDate) {
 	       Calendar aCalendar = Calendar.getInstance();
 	       aCalendar.setTime(fDate);
 	       int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);
 	       aCalendar.setTime(oDate);
 	       int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);
 	       return day2 - day1;
 	}
 	/**
 	 * 买家投诉订单
 	 * @param orderId
 	 * @return
 	 */
 	@RequestMapping(value="/customer/order/complain", method = RequestMethod.POST)
 	@ResponseBody
 	public JsonMessage complainOrder(@RequestParam int orderId, @RequestParam int reasonId, @RequestParam String comment, Locale locale){
 		Order order = orderService.getOrderById(orderId);
 		JsonMessage message = new JsonMessage();
 		Date orderDate = order.getCreateAt();
 		Date now = new Date(System.currentTimeMillis());
 		message.setFlag(true);
 		int distance = 0;
 		switch(reasonId){
 		case 1:
 			distance = daysOfTwo(orderDate, now);
 			if(distance < 3){
 				message.setFlag(false);
 				if(locale.equals("zh_CN")){
 					message.setObject("我们允许卖家在收到您的订单后，有三个工作日的处理发货时间，您只有在下单3个工作日之后，卖家还没有提供发货信息时，您的投诉才能被受理。");
 				}else{
 					message.setObject("我们允许卖家在收到您的订单后，有三个工作日的处理发货时间，您只有在下单3个工作日之后，卖家还没有提供发货信息时，您的投诉才能被受理。in english");
 				}
 			}else{
 				orderService.claimOrder(orderId, reasonId, comment, ClaimRelation.Claim);
 			}
 			break;
 		case 2:
 			int orderStatus = order.getOrderStatus();
 			if(orderStatus< OrderStatus.SEND){
 				message.setFlag(false);
 				if(locale.equals("zh_CN")){
 					message.setObject("您的订单,卖家还未提供发货信息,请选择正确的投诉类型。");
 				}else{
 					message.setObject("您的订单,卖家还未提供发货信息,请选择正确的投诉类型。in english");
 				}
 			}
 			break;
 		case 3:
 			distance = daysOfTwo(orderDate, now);
 			if(distance < 7){
 				message.setFlag(false);
 				if(locale.equals("zh_CN")){
 					message.setObject("我们允许卖家在收到您的订单后,有七个工作日的处理运货到您指定的转运仓库,您 只有在下订单7个工作日后,还没收到的货的,您的投诉才被接受处理。请选择别的正确投诉类型。");
 				}else{
 					message.setObject("我们允许卖家在收到您的订单后,有七个工作日的处理运货到您指定的转运仓库,您 只有在下订单7个工作日后,还没收到的货的,您的投诉才被接受处理。请选择别的正确投诉类型。in english");
 				}
 			}else{
 				orderService.claimOrder(orderId, reasonId, comment, ClaimRelation.Claim);
 			}
 			break;
 		default:
 			orderService.claimOrder(orderId, reasonId, comment, ClaimRelation.Claim);
 			break;
 		}
 		if(message.isFlag()){
 			if(locale.equals("zh_CN"))
 				message.setObject("投诉成功，请等待系统处理~");
 			else
 				message.setObject("投诉成功，请等待系统处理~in english");
 		}
 		return message;
 	}
 
 	public OrderService getOrderService() {
 		return orderService;
 	}
 	public void setOrderService(OrderService orderService) {
 		this.orderService = orderService;
 	}
 	
 }
