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
 import com.acme.fitness.domain.products.Product;
 import com.acme.fitness.domain.products.Training;
 import com.acme.fitness.domain.users.User;
 import com.acme.fitness.orders.GeneralOrdersService;
 import com.acme.fitness.orders.simple.TrainingDateReservedException;
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
 
 	public void AddAnonymousBasketToLoggedInUserBasket(HttpServletResponse response, HttpServletRequest request, ObjectMapper mapper) {
 		Map<String, String> anonymousProducts = pm.readFromCookies(request, mapper, "productsInBasket");
 		Map<String, String> anonymousMemberships = mm.readFromCookies(request, mapper, "membershipsInBasket");
 		Map<String, String> anonymousTrainings = tm.readFromCookies(request, mapper, "trainingsInBasket");
 		
 		Map<String, Map<String, Map<String, String>>> users = pm.loadUserNamesCookieValue(request, mapper);
 		Map<String, Map<String, String>> basket = pm.loadBasketByUserName(users, loggedInUserName());
 		
 		if (isAnonymousBasketContainsProducts(request, response, mapper)) {
 			pm.addOrderItemToList(basket, anonymousProducts);
 			cookieManager.removeTheCookieByName(request, response, "productsInBasket");
 		}
 		if (isAnonymousBasketContainsMemberships(request, response, mapper)) {
 			mm.addMembershipToList(basket, anonymousMemberships);
 			cookieManager.removeTheCookieByName(request, response, "membershipsInBasket");
 		}
 		if (isAnonymousBasketContainsTrainings(request, response, mapper)) {
 			tm.addTrainingToList(basket, anonymousTrainings);
 			cookieManager.removeTheCookieByName(request, response, "trainingsInBasket");
 		}
 		request.getSession().removeAttribute("anonymousBasket");
 		users.put(loggedInUserName(), basket);
 		cookieManager.writeMapToCookie(response, mapper, "userNames", users);
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
 			} catch (StoreQuantityException e) {
 				String info = "There is no enough quantity from the product above! ";
 				for (Product p : e.getProduct()) {
 					info += " " + p.getName();
 				}
 				logger.info(info);
 				throw e;
 			} catch (TrainingDateReservedException e) {
 				String info = "Trainings was reserved ago.";
 				for(Training t : e.getReservedTrainings()) {
 					info += " trainer: " + t.getTrainer() + " date: " + t.getDate(); 
 				}
 				logger.info(info);
 				throw e;
 			}
 		} else {
			throw new BasketCheckOutException("Termék rendeléséhez be kell jelentkezni!");
 		}
 	}
 
 	public void removeProductFromBasket(long id, HttpServletRequest request, HttpServletResponse response) {
 		pm.removeProduct(id, request, response);
 	}
 
 	public void removeMembershipFromBasket(HttpServletRequest request, HttpServletResponse response) {
 		mm.removeMembership(response, request);
 	}
 
 	public void removeTrainingFromBasket(HttpServletRequest request, HttpServletResponse response, String trainerName) {
 		tm.removeTraining(response, request, trainerName);
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
 			deleteTrainingIfTrainerAndClientEqual(request, response, anonymousBasket);
 			if (isAnonymousBasketContainsItem(anonymousBasket)) {
 				request.getSession().setAttribute("anonymousBasket", anonymousBasket);
 			}
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
 
 	public void removeAnonymousTraining(HttpServletRequest request, HttpServletResponse response, String username) {
 		tm.removeAnonymousTrainingFromCookiesByTrainerName(response, request, username);
 	}
 
 	private boolean isAnonymousBasketContainsItem(Basket anonymousBasket) {
 		return anonymousBasket.getTrainings().size() > 0 || anonymousBasket.getMemberships().size() > 0 || anonymousBasket.getOrderItems().size() > 0;
 	}
 
 	private void deleteTrainingIfTrainerAndClientEqual(HttpServletRequest request, HttpServletResponse response, Basket anonymousBasket) {
 		Training trainingToDelete = null;
 		for (Training t : anonymousBasket.getTrainings()) {
 			if (t.getTrainer().getUsername().equals(t.getClient().getUsername())) {
 				trainingToDelete = t;
 				cookieManager.removeTheCookieByName(request, response, "trainingsInBasket");
 				tm.removeAnonymousTrainingFromCookiesByTrainerName(response, request, t.getTrainer().getUsername());
 			}
 		}
 		anonymousBasket.getTrainings().remove(trainingToDelete);
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
 		return !noUserLoggedIn()
 				&& (cookieManager.readFromCookies(request, "productsInBasket") != null || cookieManager.readFromCookies(request, "membershipsInBasket") != null || cookieManager
 						.readFromCookies(request, "trainingsInBasket") != null);
 	}
 
 	private boolean isAnonymousBasketContainsMemberships(HttpServletRequest request, HttpServletResponse response, ObjectMapper mapper) {
 		return cookieManager.readFromCookies(request, "membershipsInBasket") == null ? false : true;
 	}
 
 	private boolean isAnonymousBasketContainsProducts(HttpServletRequest request, HttpServletResponse response, ObjectMapper mapper) {
 		return cookieManager.readFromCookies(request, "productsInBasket") == null ? false : true;
 	}
 
 	private boolean isAnonymousBasketContainsTrainings(HttpServletRequest request, HttpServletResponse response, ObjectMapper mapper) {
 		return cookieManager.readFromCookies(request, "trainingsInBasket") == null ? false : true;
 	}
 
 	public void updateMissingProductToMaxValue(HttpServletRequest request, HttpServletResponse response, ObjectMapper objectMapper) {
 		try {
 			checkOutBasket(response, request);
 		} catch (StoreQuantityException e) {
 			pm.updateProductsToMaxQuantity(loggedInUserName(), e.getProduct(), request, response, objectMapper);
 		} catch (BasketCheckOutException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void removeProductsFromBasket(HttpServletRequest request, HttpServletResponse response, ObjectMapper objectMapper) {
 		try {
 			checkOutBasket(response, request);
 		} catch (StoreQuantityException e) {
 			pm.removeProductsFromBasket(loggedInUserName(), e.getProduct(), request, response, objectMapper);
 		} catch (BasketCheckOutException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void removeReservedTrainings(HttpServletRequest request, HttpServletResponse response, ObjectMapper objectMapper) {
 		try {
 			checkOutBasket(response, request);
 		} catch (TrainingDateReservedException e) {
 			tm.removeTrainingsFromBasket(loggedInUserName(), e.getReservedTrainings(), request, response, objectMapper);
 		} catch (BasketCheckOutException e) {
 			e.printStackTrace();
 		} catch (StoreQuantityException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
