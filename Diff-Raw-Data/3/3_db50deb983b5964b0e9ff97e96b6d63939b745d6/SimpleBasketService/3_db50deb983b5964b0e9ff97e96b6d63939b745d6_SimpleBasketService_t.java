 package com.acme.fitness.orders.simple;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.acme.fitness.dao.orders.BasketDao;
 import com.acme.fitness.domain.exceptions.FitnessDaoException;
 import com.acme.fitness.domain.exceptions.StoreQuantityException;
 import com.acme.fitness.domain.orders.Basket;
 import com.acme.fitness.domain.orders.OrderItem;
 import com.acme.fitness.domain.products.Membership;
 import com.acme.fitness.domain.products.Product;
 import com.acme.fitness.domain.products.Training;
 import com.acme.fitness.domain.users.User;
 import com.acme.fitness.orders.BasketService;
 import com.acme.fitness.orders.OrderItemService;
 import com.acme.fitness.orders.StoreService;
 import com.acme.fitness.products.MembershipService;
 import com.acme.fitness.products.TrainingService;
 
 @Service
 public class SimpleBasketService implements BasketService {
 
 	private BasketDao basketDao;
 	private MembershipService membershipService;
 	private TrainingService trainingService;
 	private OrderItemService orderItemService;
 	private StoreService storeService;
 	
 	private Logger logger = LoggerFactory.getLogger(SimpleBasketService.class);
 	
 	@Autowired
 	public SimpleBasketService(BasketDao basketDao, MembershipService membershipService, TrainingService trainingService, OrderItemService orderItemService, StoreService storeService){
 		this.basketDao=basketDao;
 		this.membershipService=membershipService;
 		this.trainingService=trainingService;
 		this.orderItemService=orderItemService;
 		this.storeService=storeService;
 	}
 
 	@Override
 	public Basket newBasket(User client) {
 		Basket basket = new Basket(false, client);
 		return basket;
 	}
 
 	@Override
 	public void deleteBasket(Basket basket) {
 		basketDao.delete(basket);
 	}
 
 	@Override
 	public void updateBasket(Basket basket) {
 		basketDao.update(basket);
 	}
 
 	@Override
 	public void addMembershipToBasket(Basket basket, Membership membership) {
 		basket.addMembership(membership);
 	}
 
 	@Override
 	public void addTrainingToBasket(Basket basket, Training training) {
 		basket.addTraining(training);
 	}
 
 	@Override
 	public void addOrderItemToBasket(Basket basket, OrderItem orderItem) {
 		boolean isProductExists = false;
 		for (OrderItem oi : basket.getOrderItems()) {
 			if (oi.getProduct().equals(orderItem.getProduct())) {
 				isProductExists = true;
 				oi.setQuantity(oi.getQuantity() + orderItem.getQuantity());
 			}
 		}
 		if (!isProductExists) {
 			basket.addOrderItem(orderItem);
 			orderItem.setBasket(basket);
 		}
 	}
 
 	@Override
 	public List<Membership> getMemberships(Basket basket) {
 		return basket.getMemberships();
 	}
 
 	@Override
 	public List<Training> getTrainings(Basket basket) {
 		return basket.getTrainings();
 	}
 
 	@Override
 	public List<OrderItem> getOrderItems(Basket basket) {
 		return basket.getOrderItems();
 	}
 
 	@Override
 	public void checkOutBasket(Basket basket) throws StoreQuantityException {
 		basketDao.save(basket);
 		saveMemberships(basket);
 		saveTrainings(basket);
 		saveProducts(basket);
 	}
 
 	@Override
 	public void deliver(Basket basket) {
 		basket.setDelivered(true);
 		basketDao.update(basket);
 	}
 
 	@Override
 	public List<Basket> getBasketsByUser(User user) {
 		return basketDao.getBasketsByUser(user);
 	}
 
 	@Override
 	public Basket getBasketById(long id) throws FitnessDaoException {
 		return basketDao.getBasketById(id);
 	}
 
 	private void saveProducts(Basket basket) throws StoreQuantityException {
 		List<Product> missingProducts = new ArrayList<Product>();
 		for (OrderItem o : basket.getOrderItems()) {
 			try {
 				takeOutProduct(basket, missingProducts, o);
 			} catch (FitnessDaoException e) {
 				e.fillInStackTrace().printStackTrace();
 				missingProducts.add(o.getProduct());
 			}
 		}
 		if (missingProducts.size() > 0) {
 			throw new StoreQuantityException(missingProducts);
 		}
 	}
 
 	private void takeOutProduct(Basket basket, List<Product> missingProducts,
 			OrderItem o) throws FitnessDaoException {
 		if (storeService.takeOutProduct(o.getProduct(), o.getQuantity())) {
 			orderItemService.updateOrderItem(o);
 		} else {
 			missingProducts.add(o.getProduct());
 		}
 	}
 
 	private void saveTrainings(Basket basket) {
 		for (Training t : basket.getTrainings()) {
 			trainingService.saveTraining(basket, t);
 		}
 	}
 
 	private void saveMemberships(Basket basket) {
 		for (Membership m : basket.getMemberships()) {
 			membershipService.saveMemberShip(basket, m);
 		}
 	}
 }
