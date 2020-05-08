 package com.acme.fitness.webmvc.basket;
 
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.acme.fitness.domain.exceptions.BasketCheckOutException;
 import com.acme.fitness.domain.exceptions.FitnessDaoException;
 import com.acme.fitness.domain.exceptions.StoreQuantityException;
 import com.acme.fitness.domain.orders.Basket;
 import com.acme.fitness.domain.users.User;
 import com.acme.fitness.orders.GeneralOrdersService;
 import com.acme.fitness.users.GeneralUsersService;
 import com.acme.fitness.webmvc.controllers.WebShopController;
 import com.acme.fitness.webmvc.cookie.CookieManager;
 import com.acme.fitness.webmvc.user.UserManager;
 
 @Service
 public class BasketManager {
 
 	private static final Logger logger = LoggerFactory.getLogger(WebShopController.class);
 
 	@Autowired
 	private GeneralOrdersService gos;
 
 	@Autowired
 	private GeneralUsersService gus;
 
 	@Autowired
 	private ProductsManager pm;
 
 	@Autowired
 	private MembershipManager mm;
 	
 	@Autowired
 	private TrainingManager tm;
 	
 	@Autowired
 	private UserManager um;
 
 	private CookieManager cookieManager;
 
 	public BasketManager() {
 		cookieManager = new CookieManager();
 	}
 
 	public boolean isAnonymousBasketContainsMemberships(HttpServletRequest request, HttpServletResponse response, ObjectMapper mapper) {
 		return cookieManager.readFromCookies(request, "membershipsInBasket") == null ? false : true;
 	}
 
 	public boolean isAnonymousBasketContainsProducts(HttpServletRequest request, HttpServletResponse response, ObjectMapper mapper) {
 		return cookieManager.readFromCookies(request, "productsInBasket") == null ? false : true;
 	}
 	
 	public boolean isAnonymousBasketContainsTrainings(HttpServletRequest request, HttpServletResponse response, ObjectMapper mapper) {
 		return cookieManager.readFromCookies(request, "trainingsInBasket") == null ? false : true;
 	}
 
 	public void addBasketToSessionIfExists(HttpServletRequest request, HttpServletResponse response, ObjectMapper mapper) {
 		Basket basket = gos.newBasket(new User());
 		boolean isMembershipExists = mm.isItemExists(request, mapper, basket, "membershipsInBasket");
 		boolean isProductExists = pm.isItemExists(request, mapper, basket, "productsInBasket");
 		boolean isTrainingExists = tm.isItemExists(request, mapper, basket, "trainingsInBasket");
 		if (isMembershipExists || isProductExists || isTrainingExists) {
 			addBasketToSession(request, basket);
 		} else {
 			deleteBasket(request, response);
 		}
 	}
 
 	public void addNewMembership(long membershipId, HttpServletResponse response, HttpServletRequest request, ObjectMapper mapper) {
 		mm.addNewMembership(membershipId, response, request, mapper);
 	}
 
 	public void addNewOrderItem(long id, int quantity, HttpServletResponse response, HttpServletRequest request, ObjectMapper mapper) {
 		pm.addNewOrderItem(id, quantity, response, request, mapper);
 	}
 	
 	public void addNewTraining(String trainerName, String date, HttpServletResponse response, HttpServletRequest request, ObjectMapper mapper) {
 		tm.addNewTraining(trainerName, date, response, request, mapper);
 	}
 	
 	public void addAnonymousProductsBasketLoggedInUser(HttpServletResponse response, HttpServletRequest request, ObjectMapper mapper) {
 		pm.addOrderItemListToLoggedInUser(response, request, mapper);
 		cookieManager.removeTheCookieByName(request, response, "productsInBasket");
 	}
 
 	public void addAnonymousMembershipsBasketLoggedInUser(HttpServletResponse response, HttpServletRequest request, ObjectMapper mapper) {
 		mm.addNewMembershipFromAnonymousBasket(response, request, mapper);
 		cookieManager.removeTheCookieByName(request, response, "membershipsInBasket");
 	}
 
 	public void addAnonymousTrainingsBasketLoggedInUser(HttpServletResponse response, HttpServletRequest request, ObjectMapper mapper) {
 		tm.addNewTrainingFromAnonymousBasket(response, request, mapper);
 		cookieManager.removeTheCookieByName(request, response, "trainingsInBasket");
 	}
 	
 	public void checkOutBasket(HttpServletResponse response, HttpServletRequest request) throws StoreQuantityException, BasketCheckOutException {
 		Basket basket = (Basket) request.getSession().getAttribute("basket");
 		if (!noUserLoggedIn()) {
 			try {
 				basket.setUser(gus.getUserByUsername(loggedInUserName()));
 				gos.checkOutBasket(basket);
 				deleteUserBasket(request, response, loggedInUserName());
 				logger.info("Basket with id: " + basket.getId() + " has confirmed!");
 			} catch (FitnessDaoException e) {
 				e.printStackTrace();
 			}
 		} else {
 			throw new BasketCheckOutException("Termék rendeléséhez be kell jelentkezni");
 		}
 	}
 
 	public void removeProductFromBasket(long id, HttpServletRequest request, HttpServletResponse response) {
 		pm.removeProduct(id, request, response);
 	}
 
 	public void removeMembershipFromBasket(HttpServletRequest request, HttpServletResponse response) {
 		mm.removeMembership(response, request);
 	}
 	
 	public void removeTrainingFromBasket(HttpServletRequest request, HttpServletResponse response) {
 		tm.removeTraining(response, request);
 	}
 
 	public void deleteBasket(HttpServletRequest request, HttpServletResponse response) {
 		if (noUserLoggedIn()) {
 			deleteAnonymousBasket(request, response);
 		} else {
 			deleteUserBasket(request, response, loggedInUserName());
 		}
 		request.getSession().removeAttribute("basket");
 	}
 
 	public void deleteAnonymousBasket(HttpServletRequest request, HttpServletResponse response) {
 		removeFromCookiesAndSession(request, response, "membershipsInBasket");
 		removeFromCookiesAndSession(request, response, "productsInBasket");
 		removeFromCookiesAndSession(request, response, "trainingsInBasket");
 	}
 
 	public void isAnonymousBasketIfUserLoggedIn(HttpServletRequest request, HttpServletResponse response, ObjectMapper mapper) {
 		if (isUserLoggedInAndAnonymousBasketContainsElement(request)) {
 			Basket anonymousBasket = gos.newBasket(new User());
 			mm.loadBasketToAnonymousUser(request, mapper, anonymousBasket, "membershipsInBasket");
 			pm.loadBasketToAnonymousUser(request, mapper, anonymousBasket, "productsInBasket");
 			tm.loadBasketToAnonymousUser(request, mapper, anonymousBasket, "trainingsInBasket");
 			request.getSession().setAttribute("anonymousBasket", anonymousBasket);
 		} else {
 			request.getSession().removeAttribute("anonymousBasket");
 		}
 	}
 
 	public void removeAnonymousProduct(long productId, HttpServletRequest request, HttpServletResponse response) {
 		Map<String, String> products = cookieManager.loadValueToMap(request, new ObjectMapper(), "productsInBasket");
 		products.remove(Long.toString(productId));
 		if (products.size() > 0) {
 			cookieManager.writeMapToCookie(response, new ObjectMapper(), "productsInBasket", products);
 		} else {
 			cookieManager.removeTheCookieByName(request, response, "productsInBasket");
 		}
 	}
 
 	public void removeAnonymousMembership(HttpServletRequest request, HttpServletResponse response) {
 		cookieManager.removeTheCookieByName(request, response, "membershipsInBasket");
 	}
 	
 	public void removeAnonymousTraining(HttpServletRequest request, HttpServletResponse response) {
 		cookieManager.removeTheCookieByName(request, response, "trainingsInBasket");
 	}
 
 	private void addBasketToSession(HttpServletRequest request, Basket basket) {
 		request.getSession().setAttribute("basket", basket);
 	}
 
 	private boolean noUserLoggedIn() {
 		return loggedInUserName().equals("anonymousUser");
 	}
 
 	private void deleteUserBasket(HttpServletRequest request, HttpServletResponse response, String userName) {
 		Map<String, Map<String, Map<String, String>>> users = um.loadUserNamesCookieValue(request, new ObjectMapper(), cookieManager);
 		if (users.containsKey(userName)) {
 			users.remove(userName);
 		}
 		cookieManager.writeMapToCookie(response, new ObjectMapper(), "userNames", users);
 		request.getSession().removeAttribute("basket");
 	}
 
 	private String loggedInUserName() {
 		return um.getLoggedInUserName();
 	}
 
 	private void removeFromCookiesAndSession(HttpServletRequest request, HttpServletResponse response, String cookieName) {
 		request.getSession().removeAttribute(cookieName);
 		Cookie[] cookies = request.getCookies();
 		if ((cookies != null) && cookies.length > 0) {
 			cookieManager.removeTheCookieByName(request, response, cookieName);
 		}
 	}
 
 	private boolean isUserLoggedInAndAnonymousBasketContainsElement(HttpServletRequest request) {
 		return !noUserLoggedIn() && (cookieManager.readFromCookies(request, "productsInBasket") != null || cookieManager.readFromCookies(request, "membershipsInBasket") != null || cookieManager.readFromCookies(request, "trainingsInBasket") != null);
 	}
 
 }
