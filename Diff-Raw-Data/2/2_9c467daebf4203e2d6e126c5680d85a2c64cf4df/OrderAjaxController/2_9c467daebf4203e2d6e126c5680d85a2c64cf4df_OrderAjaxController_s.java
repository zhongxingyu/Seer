 package com.res.controller;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.res.constant.ResConstant;
 import com.res.exception.ServiceException;
 import com.res.model.Address;
 import com.res.model.CustomerOrder;
 import com.res.model.Menu;
 import com.res.model.OrderDetail;
 import com.res.model.Person;
 import com.res.model.Restaurant;
 import com.res.service.AddressService;
 import com.res.service.CustomerService;
 import com.res.service.MenuService;
 import com.res.service.OrderService;
 import com.res.service.RestaurantService;
 import com.res.util.MessageLoader;
 import com.res.util.Price;
 
 @Controller
 @SessionAttributes
 @Scope(value="session")
 public class OrderAjaxController {
 
 	private static Logger logger = Logger.getLogger(OrderAjaxController.class);
 	
 	@Autowired private OrderService orderService;
 	@Autowired private MenuService menuService;
 	@Autowired private RestaurantService restaurantService;
 	@Autowired private AddressService addressService;
 	@Autowired private CustomerService customerService;
 	@Autowired private MessageLoader messageLoader;
 	
 	private List<OrderDetail> orderList = new ArrayList<OrderDetail>();
 	private BigDecimal subTotal;
 	private BigDecimal tax;
 	private BigDecimal grandTotal;
 	
 	@RequestMapping(value="/showOrder", method=RequestMethod.GET)
 	public ModelAndView showOrderList(HttpServletRequest request, HttpServletResponse response) 
 			throws ServiceException{
 		HttpSession session = request.getSession();
 		Long restaurantId = Long.parseLong((String)session.getAttribute("restaurantId"));
 		if(restaurantId == null){
 			throw new ServiceException(messageLoader.getMessage("restaurantid.not.set"));
 		}
 		ModelAndView mav = new ModelAndView("showOrder");
 		
 		mav.addObject("orderList", orderList);
 		mav.addObject("orderListSize", orderList.size());
 		
 		BigDecimal subTotal = new BigDecimal(0.00);
 		
 		Restaurant res = restaurantService.getResturantInfo(restaurantId);
 		BigDecimal tax = res.getTax();
 		logger.info("tax = " + tax);
 		BigDecimal grandTotal = new BigDecimal(0.00);
 		
 		mav.addObject("salesTax", tax.multiply(new BigDecimal(100)));
 		
 		
 		if(!orderList.isEmpty()){
 			for(OrderDetail orderDetail : orderList){
 				subTotal = subTotal.add(orderDetail.getPrice());
 			}
 			BigDecimal calcSubTotal = subTotal.setScale(ResConstant.SCALE, RoundingMode.HALF_UP);
 			this.setSubTotal(calcSubTotal);
 			mav.addObject("subTotal", calcSubTotal);
 			
 			BigDecimal calcTax = subTotal.multiply(tax).setScale(ResConstant.SCALE, RoundingMode.HALF_UP); 
 			this.setTax(calcTax);		
 			mav.addObject("tax", calcTax);
 			
 			grandTotal = subTotal.add(calcTax);
 
 			if(res.getRounding()){
 				logger.info(res.getRestaurantName() + " rounds to nearest nickel.");
 				grandTotal = Price.roundToNearestNickel(grandTotal);
 			}else{
 				logger.info(res.getRestaurantName() + " does not round to nearest nickel.");
 				grandTotal = grandTotal.setScale(ResConstant.SCALE, RoundingMode.HALF_UP);
 			}
 			this.setGrandTotal(grandTotal);
 			mav.addObject("grandTotal", grandTotal);
 		}
 
 		return mav;
 	}
 	
 	@RequestMapping(value="/addToOrder.json", method=RequestMethod.GET)
 	public String addToOrder(HttpServletRequest request, HttpServletResponse response){
 		
 		String menuId = request.getParameter("menuId");
 		logger.info("hitting addOrder controller with menuId " + menuId);
 		Menu menu = menuService.getMenuByMenuId(Long.parseLong(menuId));
 		
 		OrderDetail orderDetail = new OrderDetail();
 		orderDetail.setQuantity(1);
 		orderDetail.setSize("Large");
 		orderDetail.setMenu(menu);
 		
 		BigDecimal price = orderDetail.getMenu().getLarge().multiply(new BigDecimal(orderDetail.getQuantity()));
 		orderDetail.setPrice(price.setScale(ResConstant.SCALE));
 		
 		orderList.add(orderDetail);
 		
 		return "redirect:/showOrder.html";
 	}
 	
 	@RequestMapping(value="/deleteItem.json", method=RequestMethod.GET)
 	public String deleteItem(HttpServletRequest req, HttpServletResponse res)
 			throws NumberFormatException{
 		String idx = req.getParameter("idx");
 		try{
 			int index = Integer.parseInt(idx);
 			logger.info("removing orderList with " + idx);
 			orderList.remove(index);
 		}catch(Exception e){
 			throw new NumberFormatException(messageLoader.getMessage("is.not.a.number"));
 		}
 		
 		return "redirect:/showOrder.html";
 	}
 	
 	@RequestMapping(value="/increaseQty.json", method=RequestMethod.GET)
 	public String increaseQty(HttpServletRequest request, HttpServletResponse response)
 			throws NumberFormatException{
 		String idx = request.getParameter("idx");
 		try{
 			int index = Integer.parseInt(idx);
 			logger.info("increasing qty for item");
 			OrderDetail orderDetail = orderList.get(index);
 			orderDetail.setQuantity(orderDetail.getQuantity() + 1);
 			
 			BigDecimal price = orderDetail.getMenu().getLarge().multiply(new BigDecimal(orderDetail.getQuantity()));
 			orderDetail.setPrice(price.setScale(ResConstant.SCALE));
 		}catch(Exception e){
 			throw new NumberFormatException(messageLoader.getMessage("is.not.a.number"));
 		}
 		
 		return "redirect:/showOrder.html";
 	}
 	
 	@RequestMapping(value="/decreaseQty.json", method=RequestMethod.GET)
 	public String decreaseQty(HttpServletRequest request, HttpServletResponse response)
 			throws NumberFormatException{
 		String idx = request.getParameter("idx");
 		try{
 			int index  = Integer.parseInt(idx);
 			logger.info("decreasing qty for item");
 			OrderDetail orderDetail = orderList.get(index);
 			int quantity = orderDetail.getQuantity();
 			
 			if(quantity > 1){
 				orderDetail.setQuantity(quantity - 1);
 				BigDecimal price = orderDetail.getMenu().getLarge().multiply(new BigDecimal(orderDetail.getQuantity()));
 				orderDetail.setPrice(price.setScale(ResConstant.SCALE));
 			}else{
 				logger.info("subtracting quantity of 1, therefore deleting item.");
 				orderList.remove(index);
 			}
 		}catch(Exception e){
 			throw new NumberFormatException(messageLoader.getMessage("is.not.a.number"));
 		}
 			
 		return "redirect:/showOrder.html";
 	}
 	
 	@RequestMapping(value="/newOrder.json", method=RequestMethod.GET)
 	public String newOrder(HttpServletRequest request, HttpServletResponse response){
 		logger.info("clearing the order and customer information...");
 		orderList.clear();
 		return "redirect:/showOrder.html";
 	}
 	
 	@RequestMapping(value="/voidOrder.json", method=RequestMethod.GET)
 	public String voidOrder(HttpServletRequest request, HttpServletResponse response){
 		logger.info("deleting order...");
 		orderList.clear();
 		return "redirect:/showOrder.html";
 	}
 	
 	@RequestMapping(value="/saveOrder.json", method=RequestMethod.GET)
 	public String saveOrder(HttpServletRequest request, HttpServletResponse response){
 		HttpSession session = request.getSession();
 		String agentName = (String)session.getAttribute("agentName");
 		Long restaurantId = Long.parseLong((String)session.getAttribute("restaurantId"));
 
 		//TODO: check if it is a delivery order, if yes, save address
 		//TODO: if address or person info already exists in database, use that id and dont save into database.
 		Address address = new Address();
 		address.setStreet1(StringUtils.trimToNull(request.getParameter("address[street1]")));
 		address.setStreet2(StringUtils.trimToNull(request.getParameter("address[street2]")));
 		address.setCity(StringUtils.trimToNull(request.getParameter("address[city]")));
 		address.setState(StringUtils.trimToNull(request.getParameter("address[state]")));
 		address.setZipCode(StringUtils.trimToNull(request.getParameter("address[zipCode]")));
 		addressService.save(address);
 		
 		Person customer = new Person();
 		customer.setFirstName(StringUtils.trimToNull(request.getParameter("customer[firstName]")));
 		customer.setLastName(StringUtils.trimToNull(request.getParameter("customer[lastName]")));
 		customer.setPhone1(StringUtils.trimToNull(request.getParameter("customer[phone1]")));
 		customer.setPhone2(StringUtils.trimToNull(request.getParameter("customer[phone2]")));
 		customer.setEmail(StringUtils.trimToNull(request.getParameter("customer[email]")));
 		customer.setNote(StringUtils.trimToNull(request.getParameter("customer[note]")));
 		customer.setLastUpdatedBy(agentName);
 		customer.setAddress(address);
 		customerService.save(customer);
 		
 		CustomerOrder customerOrder = new CustomerOrder();
 		customerOrder.setRestaurantId(restaurantId);
 		customerOrder.setCustomer(customer);
 		customerOrder.setUsername(agentName);
 		customerOrder.setOrderOption("Delivery");
 		customerOrder.setOrderTime(new Date());
 		customerOrder.setSubTotal(this.getSubTotal());
 		customerOrder.setTax(this.getTax());
 		customerOrder.setGrandTotal(this.getGrandTotal());
 		
 		for(OrderDetail orderDetail : orderList){
 			orderDetail.setCustomerOrder(customerOrder);
 		}
 		
 		orderService.saveOrder(customerOrder, orderList);
 		orderList.clear();
 		
		return "redirect:/welcome.html";
 	}
 
 	public BigDecimal getSubTotal() {
 		return subTotal;
 	}
 
 	public void setSubTotal(BigDecimal subTotal) {
 		this.subTotal = subTotal;
 	}
 
 	public BigDecimal getTax() {
 		return tax;
 	}
 
 	public void setTax(BigDecimal tax) {
 		this.tax = tax;
 	}
 
 	public BigDecimal getGrandTotal() {
 		return grandTotal;
 	}
 
 	public void setGrandTotal(BigDecimal grandTotal) {
 		this.grandTotal = grandTotal;
 	}
 
 }
