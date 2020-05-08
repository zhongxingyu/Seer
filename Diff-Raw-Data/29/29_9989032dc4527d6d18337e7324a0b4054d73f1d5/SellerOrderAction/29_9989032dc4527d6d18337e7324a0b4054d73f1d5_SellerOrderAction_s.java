 package com.omartech.tdg.action.seller;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.omartech.tdg.exception.UnauthorizedException;
 import com.omartech.tdg.model.Order;
 import com.omartech.tdg.model.Seller;
 import com.omartech.tdg.service.ClaimService;
 import com.omartech.tdg.service.EmailService;
 import com.omartech.tdg.service.FinanceService;
 import com.omartech.tdg.service.OrderService;
 import com.omartech.tdg.utils.OrderStatus;
 import com.omartech.tdg.utils.SystemDefaultSettings;
 import com.omartech.tdg.utils.TimeFormat;
 import com.omartech.tdg.utils.UserType;
 
 @Controller
 public class SellerOrderAction {
 
 	@Autowired
 	private OrderService orderService;
 	@Autowired
 	private EmailService emailService;
 	@Autowired
 	private FinanceService financeService;
	
 	@Autowired
 	private ClaimService claimService;
 	/**
 	 * 指向取消订单的页面
 	 * @param id
 	 * @param session
 	 * @return
 	 */
 	@RequestMapping("/seller/order/cancel/{id}")
 	public ModelAndView cancelOrder(@PathVariable int id, HttpSession session){
 		Order order = orderService.getOrderById(id);
 		return new ModelAndView("/seller/order/order-cancel").addObject("order", order);
 	}
 	/**
 	 * 取消订单的具体信息
 	 * @param id
 	 * @param cancelReason
 	 * @param comment
 	 * @param session
 	 * @return
 	 */
 	@RequestMapping(value="/seller/order/cancel/{id}", method=RequestMethod.POST)
 	public String cancelOrderDetails(@PathVariable int id, 
 			@RequestParam int reason, @RequestParam String comment,
 			HttpSession session){
 		Order order = orderService.getOrderById(id);
 		
 		int originStatus = order.getOrderStatus();
 		if(originStatus == OrderStatus.PAID){//如果订单已经付款
 			//退钱
 			financeService.payAllMoneyBack(id);
 		}
 		orderService.updateOrderStatus(OrderStatus.CANCELBYSELLER, id, comment, reason);//将订单状态置为被商家取消,记录取消原因
 		return "redirect:/seller/order/show/"+id;
 	}
 	
 	@RequestMapping("/seller/order/print/{id}")
 	public ModelAndView forPrint(
 			@PathVariable int id,
 			HttpSession session
 			){
 		Order order = orderService.getOrderById(id);
 		Seller seller = (Seller) session.getAttribute(UserType.SELLER);
 		int userId = seller.getId();
 		if(order.getSellerId() != userId){
 			throw new UnauthorizedException();
 		}
 		return new ModelAndView("/seller/order/order-print")
 			.addObject("order", order);
 	}
 	@RequestMapping("/seller/order/printall")
 	public ModelAndView forPrintAll(@RequestParam String ids){
 		String[] idArray = ids.split(",");
 		List<Order> orders = orderService.getOrderByIdArray(idArray);
 		return new ModelAndView("/seller/order/order-print-all").addObject("orders", orders);
 	}
 	
 	/**
 	 * 填写快递信息
 	 * @param orderId
 	 * @return
 	 */
 	@RequestMapping("/seller/order/send/{orderId}")
 	public ModelAndView send(@PathVariable int orderId){
 		Order order = orderService.getOrderById(orderId);
 		return new ModelAndView("/seller/order/order-send").addObject("order",order);
 	}
 	/**
 	 * 更新快递信息
 	 * @param orderId
 	 * @return
 	 */
 	@RequestMapping("/seller/order/updateShipping/{orderId}")
 	public ModelAndView updateShipping(@PathVariable int orderId){
 		Order order = orderService.getOrderById(orderId);
 		return new ModelAndView("/seller/order/order-updatesend").addObject("order",order);
 	}
 	@RequestMapping(value="/seller/order/send",method=RequestMethod.POST)
 	public String sendPackage(
 			@RequestParam int orderId,
 			@RequestParam String sendAt, 
 			@RequestParam String carrier, 
 			@RequestParam String trackingWeb, 
 			@RequestParam String trackingId, 
 			HttpSession session){
 		
 		if(carrier == null || carrier.length() == 0 || trackingWeb == null || trackingWeb.length() == 0 || trackingId == null || trackingId.length() == 0){
 			return "redirect:/seller/error/carrierisnull";
 		}
 		
 		Order order = orderService.getOrderById(orderId);
 		order.setSendAt(TimeFormat.StringToDate(sendAt));
 		order.setCarrier(carrier);
 		order.setTrackingWeb(trackingWeb);
 		order.setTrackingId(trackingId);
 		order.setSendLogAt(new Date());
 		order.setReturnFlag(SystemDefaultSettings.TOTALMONEYRETURN);
 		orderService.updateOrderBySeller(order);
 		orderService.updateOrderStatus(OrderStatus.SEND, orderId);
 		emailService.sendEmailWhenSellerSendPackage(order);
 		return "redirect:/seller/order/show/"+orderId;
 	}
 	/**
 	 * 卖家更改了发货信息
 	 * @param orderId
 	 * @param sendAt
 	 * @param carrier
 	 * @param trackingWeb
 	 * @param trackingId
 	 * @param session
 	 * @return
 	 */
 	@RequestMapping(value="/seller/order/updateShipping",method=RequestMethod.POST)
 	public String updateShipping(
 			@RequestParam int orderId,
 			@RequestParam String sendAt, 
 			@RequestParam String carrier, 
 			@RequestParam String trackingWeb, 
 			@RequestParam String trackingId, 
 			HttpSession session
 			){
 		if(carrier == null || carrier.length() == 0 || trackingWeb == null || trackingWeb.length() == 0 || trackingId == null || trackingId.length() == 0){
 			return "redirect:/seller/error/carrierisnull";
 		}
 		Order order = orderService.getOrderById(orderId);
 		order.setCarrier(carrier);
 		order.setTrackingWeb(trackingWeb);
 		order.setTrackingId(trackingId);
 		orderService.updateOrderBySeller(order);
 		emailService.sendEmailWhenSellerUpdateCarrierInformation(order);
 		return "redirect:/seller/order/show/"+orderId;
 	}
 	
 	/**
 	 * 某订单退全款
 	 * @return
 	 */
 	@RequestMapping(value="/seller/order/return-money", method=RequestMethod.GET)
 	public String returnMoney(@RequestParam int claimId, @RequestParam int orderId){
 		orderService.returnWholeMoneyBySeller(orderId, claimId);
 		return "redirect:/seller/order/show/"+orderId;
 	}
 
 	@RequestMapping("/seller/order/push-claim-to-admin")
 	public String askHelpFromAdminAboutClaim(@RequestParam int claimId, @RequestParam int orderId){
 		claimService.updateFlag(claimId);
 		return "redirect:/seller/order/show/"+orderId; 
 	}
 	
 	public OrderService getOrderService() {
 		return orderService;
 	}
 
 	public void setOrderService(OrderService orderService) {
 		this.orderService = orderService;
 	}
 	
 }
