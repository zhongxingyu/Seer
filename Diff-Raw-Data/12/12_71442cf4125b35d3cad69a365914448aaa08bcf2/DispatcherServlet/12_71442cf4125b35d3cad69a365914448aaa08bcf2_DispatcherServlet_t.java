 package com.algo.webshop.client.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.algo.webshop.client.authorization.OrderForm;
 import com.algo.webshop.common.domain.Basket;
 import com.algo.webshop.common.domain.Category;
 import com.algo.webshop.common.domain.Good;
 import com.algo.webshop.common.domain.Price;
 import com.algo.webshop.common.domainimpl.ICategory;
 import com.algo.webshop.common.domainimpl.IGood;
 import com.algo.webshop.common.domainimpl.IPrice;
 
 @Controller
 public class DispatcherServlet {
 
 	private ICategory serviceCategory;
 	private IGood serviceGood;
 	private IPrice servicePrice;
 
 	@Autowired
 	public void setUserService(@Qualifier("goodService") IGood service) {
 		this.serviceGood = service;
 	}
 
 	@Autowired
 	public void setUserService(@Qualifier("categoryService") ICategory service) {
 		this.serviceCategory = service;
 	}
 
 	@Autowired
 	public void setUserService(@Qualifier("priceService") IPrice service) {
 		this.servicePrice = service;
 	}
 
 	@RequestMapping({ "/", "/index" })
 	public ModelAndView index(Model model) {
 		List<Category> categorysList = serviceCategory.getCategorys();
 		List<Good> goodListByOneCategory = new ArrayList<Good>();
 		List<Price> priceList = servicePrice.getMaxDateAllPrice();
 		Map<Integer, Float> priceMap = new HashMap<Integer, Float>();
 
 		Random randomValue = new Random();
 
 		for (Category cat : categorysList) {
 			List<Good> goodList = serviceGood.getGoods(cat.getId());
 			try {
 				goodListByOneCategory.add(goodList.get(randomValue
 						.nextInt(goodList.size())));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		for (Price price : priceList) {
 			priceMap.put(price.getGoodId(), price.getValue());
 		}
 		model.addAttribute("priceMap", priceMap);
 		model.addAttribute("goodList", goodListByOneCategory);
 		model.addAttribute("categorysList", categorysList);
 		return new ModelAndView("index");
 	}
 
 	@RequestMapping(value = "/category", method = RequestMethod.GET)
 	public ModelAndView categorys(Model model,
 			@RequestParam("category") int categoryId) {
 		List<Good> goods = serviceGood.getGoods(categoryId);
 		List<Category> categorysList = serviceCategory.getCategorys();
 		List<Price> priceList = servicePrice
 				.getMaxDatePriceByOneCategory(categoryId);
 		Map<Integer, Float> priceMap = new HashMap<Integer, Float>();
 		for (Price price : priceList) {
 			priceMap.put(price.getGoodId(), price.getValue());
 		}
 		model.addAttribute("categorysList", categorysList);
 		model.addAttribute("goodList", goods);
 		model.addAttribute("id", categoryId);
 		model.addAttribute("priceMap", priceMap);
 		return new ModelAndView("category");
 	}
 
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/putinbasket", method = RequestMethod.POST)
 	public ResponseEntity<String> putInBasket(
 			@RequestParam("goodId") int goodId, HttpSession session) {
 		try {
 			for (Basket basketInSession : (LinkedList<Basket>) session
 					.getAttribute("basketList")) {
 				if (basketInSession.getGoodId() == goodId)
 					return new ResponseEntity<String>("good",
 							HttpStatus.CREATED);
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		List<Basket> basketList = new LinkedList<Basket>();
 		Basket basket = new Basket();
 		basket.setGoodId(goodId);
 		basket.setNameGood(serviceGood.getGood(goodId).getName());
 		basket.setValue(1);
 		basket.setPrice(servicePrice.getMaxDatePriceByOneGood(goodId)
 				.getValue());
 		try {
 			basketList.addAll((LinkedList<Basket>) session
 					.getAttribute("basketList"));
 		} catch (Exception e) {
 		}
 		basketList.add(basket);
 		session.setAttribute("basketList", basketList);
 		return new ResponseEntity<String>("good", HttpStatus.CREATED);
 	}
 
 	@SuppressWarnings("unchecked")
 	@RequestMapping("/basket")
 	public ModelAndView basket(Model model, HttpSession session) {
 		float sum = 0;
 		List<Basket> basketList = new LinkedList<Basket>();
 		try {
 			basketList.addAll((LinkedList<Basket>) session
 					.getAttribute("basketList"));
 		} catch (Exception e) {
 			e.getStackTrace();
 		}
 
 		for (Basket basket : basketList) {
 			sum += basket.getValue() * basket.getPrice();
 		}
 
 		model.addAttribute("basketList", basketList);
 		model.addAttribute("sum", sum);
 		return new ModelAndView("basket");
 	}
 
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/deletegood", method = RequestMethod.POST)
 	public ModelAndView deleteGood(Model model,
 			@RequestParam("goodId") int goodId, HttpSession session) {
 		List<Basket> basketList = new LinkedList<Basket>();
 		basketList.addAll((LinkedList<Basket>) session
 				.getAttribute("basketList"));
 		for (Basket basketInSession : (LinkedList<Basket>) session
 				.getAttribute("basketList")) {
 			if (basketInSession.getGoodId() == goodId) {
 				basketList.remove(basketInSession);
 			}
 		}
 		session.setAttribute("basketList", basketList);
 		return new ModelAndView("redirect:basket");
 	}
 
 	@RequestMapping(value = "/order", method = RequestMethod.GET)
 	public ModelAndView order(Model model, HttpSession session) {
 		@SuppressWarnings("unchecked")
 		List<Basket> basketList = (List<Basket>) session
 				.getAttribute("basketList");
 		List<Basket> ItemsInStock = new LinkedList<Basket>();
 		List<Basket> noProductsInStock = new LinkedList<Basket>();
 		List<Double> amountProductsInStock = new LinkedList<Double>();
 		Iterator<Basket> iterator = basketList.iterator();
 		float sum = 0;
 		double amount;
 		while (iterator.hasNext()) {
 			Basket element = iterator.next();
 			amount = serviceGood.getGood(element.getGoodId()).getAmount();
 			if (amount >= element
 					.getValue()) {
 				ItemsInStock.add(element);
 				sum += element.getPrice();
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
 		return "redirect:index";
 	}
 
 	
 
 	@RequestMapping(value = "/applayorder", method = RequestMethod.POST)
 	public String applayorder(Model model, HttpSession sesion) {
 		return "redirect:index";
 	}
 
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/upvalue", method = RequestMethod.POST)
 	public ModelAndView upValue(Model model,
 			@RequestParam("goodId") int goodId, HttpSession session) {
 		List<Basket> basketList = new LinkedList<Basket>();
 		basketList.addAll((LinkedList<Basket>) session
 				.getAttribute("basketList"));
 		for (Basket basketInSession : (LinkedList<Basket>) session
 				.getAttribute("basketList")) {
 			if (basketInSession.getGoodId() == goodId) {
 				basketInSession.setValue(basketInSession.getValue() + 1);
 			}
 		}
 		session.setAttribute("basketList", basketList);
 		return new ModelAndView("redirect:basket");
 	}
 
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/downvalue", method = RequestMethod.POST)
 	public ModelAndView downValue(Model model,
 			@RequestParam("goodId") int goodId, HttpSession session) {
 		List<Basket> basketList = new LinkedList<Basket>();
 		basketList.addAll((LinkedList<Basket>) session
 				.getAttribute("basketList"));
 		for (Basket basketInSession : (LinkedList<Basket>) session
 				.getAttribute("basketList")) {
 			if (basketInSession.getGoodId() == goodId) {
 				if (basketInSession.getValue() > 1) {
 					basketInSession.setValue(basketInSession.getValue() - 1);
 				}
 
 			}
 		}
 		session.setAttribute("basketList", basketList);
 		return new ModelAndView("redirect:basket");
 	}
 }
