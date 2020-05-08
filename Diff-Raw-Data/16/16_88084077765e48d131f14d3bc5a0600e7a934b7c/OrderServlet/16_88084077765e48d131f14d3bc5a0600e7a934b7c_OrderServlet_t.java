 package com.algo.webshop.client.controller;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.algo.webshop.client.authorization.OrderForm;
 import com.algo.webshop.common.domain.Basket;
 import com.algo.webshop.common.domain.Category;
 import com.algo.webshop.common.domain.GoodsList;
 import com.algo.webshop.common.domain.Order;
 import com.algo.webshop.common.domain.Position;
 import com.algo.webshop.common.domain.User;
 import com.algo.webshop.common.domainimpl.ICategory;
 import com.algo.webshop.common.domainimpl.IGood;
 import com.algo.webshop.common.domainimpl.IOrder;
import com.algo.webshop.common.domainimpl.IOrderGood;
 import com.algo.webshop.common.domainimpl.IUser;
 
 @Controller
 public class OrderServlet {
 
 	private IGood serviceGood;
 	private IOrder serviceOrder;
 	private IUser serviceUser;
 	private ICategory serviceCategory;
	private IOrderGood serviceOrderGood;
 
 	@Autowired
 	public void setUserService(@Qualifier("goodService") IGood service) {
 		this.serviceGood = service;
 	}
 
 	@Autowired
 	public void setOrderService(@Qualifier("orderService") IOrder service) {
 		this.serviceOrder = service;
 	}
 
 	@Autowired
 	public void setUserService(@Qualifier("userService") IUser service) {
 		this.serviceUser = service;
 	}
 
 	@Autowired
 	public void setUserService(@Qualifier("categoryService") ICategory service) {
 		this.serviceCategory = service;
 	}
 
	@Autowired
	public void setOrderGoodService(
			@Qualifier("orderGoodService") IOrderGood service) {
		this.serviceOrderGood = service;
	}

 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/order", method = RequestMethod.GET)
 	public ModelAndView order(Model model, HttpSession session) {
 		List<Category> categorysList = serviceCategory.getCategorys();
 		model.addAttribute("categorysList", categorysList);
 		removeSessionAttribute(session);
 		List<Basket> basketList = (List<Basket>) session
 				.getAttribute("basketList");
 
 		if (basketList == null) {
 			return new ModelAndView("redirect:basket");
 		}
 		if (basketList.size() == 0) {
 			return new ModelAndView("redirect:basket");
 		}
 		List<Basket> ItemsInStock = new LinkedList<Basket>();
 		List<Basket> noProductsInStock = new LinkedList<Basket>();
 		List<Double> amountProductsInStock = new LinkedList<Double>();
 		Iterator<Basket> iterator = basketList.iterator();
 		float sum = 0;
 		double amount;
 		while (iterator.hasNext()) {
 			Basket element = iterator.next();
 			amount = serviceGood.getGood(element.getGoodId()).getAmount();
 			if (amount >= element.getValue()) {
 				ItemsInStock.add(element);
 				sum += element.getPrice() * element.getValue();
 				continue;
 			}
 			noProductsInStock.add(element);
 			amountProductsInStock.add(amount);
 		}
 		if (ItemsInStock.size() > 0) {
 			session.setAttribute("ItemsInStock", ItemsInStock);
 			session.setAttribute("sum", sum);
 		}
 		if (noProductsInStock.size() > 0) {
 			session.setAttribute("noProductsInStock", noProductsInStock);
 			session.setAttribute("amountProductsInStock", amountProductsInStock);
 		}
 		String login = (String) session.getAttribute("login");
 		if (login == null) {
 			session.setAttribute("userData", "userData");
 			OrderForm orderForm = new OrderForm();
 			model.addAttribute("orderForm", orderForm);
 		} else {
 			session.setAttribute("userData", null);
 		}
 
 		return new ModelAndView("order");
 	}
 
 	@RequestMapping(value = "/order", method = RequestMethod.POST)
 	public String processSignup(@Valid final OrderForm orderForm,
 			final BindingResult result, Model model, HttpSession sesion) {
 		if (result.hasErrors()) {
 			return "order";
 		}
 		sesion.setAttribute("orderForm", orderForm);
 		return "redirect:applayorder";
 	}
 
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/applayorder", method = RequestMethod.GET)
 	public String applayorder(Model model, HttpSession sesion) {
 		if (sesion.getAttribute("ItemsInStock") == null) {
 			return "redirect:index";
 		}
 		List<Category> categorysList = serviceCategory.getCategorys();
 		model.addAttribute("categorysList", categorysList);
 		List<Basket> goods = (List<Basket>) sesion.getAttribute("ItemsInStock");
 		Set<Position> setPosition = new HashSet<Position>();
 		if (goods == null) {
 			return "errorOrder";
 		}
 		for (Basket good : goods) {
 			setPosition.add(new Position(good.getGoodId(), good.getValue(),
 					good.getPrice()));
 		}
 		String login = (String) sesion.getAttribute("login");
 		User user = serviceUser.getUserByLogin(login);
 		GoodsList goodList = new GoodsList();
 		goodList.addPositions(setPosition);
 		Order order;
 		OrderForm orderForm = (OrderForm) sesion.getAttribute("orderForm");
 		if (sesion.getAttribute("login") == null) {
 			order = new Order(getOrderNumber(), orderForm.getPhone(),
 					orderForm.getEmail(), Calendar.getInstance(), goodList);
 		} else {
 			order = new Order(getOrderNumber(), user.getId(),
 					Calendar.getInstance(), goodList);
 		}
 		synchronized (serviceOrder) {
 			if (IsStockGoods(setPosition)) {
 				serviceOrder.addOrder(order);
				int numberOfOrder = serviceOrder.getOrderIdByNumber(order
						.getNumber());
				serviceOrderGood
						.addGoodList(order.getGoodList(), numberOfOrder);
				serviceGood.updateAmount(order.getGoodList());
 				sesion.removeAttribute("basketList");
 				List<Basket> basket = (List<Basket>) sesion
 						.getAttribute("noProductsInStock");
 				sesion.setAttribute("basketList", basket);
 				removeSessionAttribute(sesion);
 				return "orderSuccessfuly";
 			}
 		}
 
 		return "errorOrder";
 	}
 
 	private String getOrderNumber() {
 		String numberOfOrder;
 		String number = serviceOrder.getLastOrderNumber();
 		String numberDate = number.substring(0, 8);
 		Calendar currentDate = Calendar.getInstance();
 		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
 		String time = formatter.format(currentDate.getTime());
 		String tmp[] = time.split("/");
 		String curentDate = tmp[0] + tmp[1] + tmp[2];
 		if (numberDate.equals(curentDate)) {
 			numberOfOrder = curentDate
 					+ Integer
 							.toString(Integer.parseInt(number.substring(8)) + 1);
 		} else {
 			numberOfOrder = curentDate + 1;
 		}
 		return numberOfOrder;
 	}
 
 	private boolean IsStockGoods(Set<Position> listGoods) {
 		double amount;
 		for (Position good : listGoods) {
 			amount = serviceGood.getGood(good.getGoods_id()).getAmount();
 			if (good.getAmount() > amount) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private void removeSessionAttribute(HttpSession session) {
 		if (session.getAttribute("ItemsInStock") != null) {
 			session.removeAttribute("ItemsInStock");
 		}
 		if (session.getAttribute("noProductsInStock") != null) {
 			session.removeAttribute("noProductsInStock");
 		}
 		if (session.getAttribute("amountProductsInStock") != null) {
 			session.removeAttribute("amountProductsInStock");
 		}
 		if (session.getAttribute("userData") != null) {
 			session.removeAttribute("userData");
 		}
 
 		if (session.getAttribute("sum") != null) {
 			session.removeAttribute("sum");
 		}
 	}
 
 }
