 package com.mss.domain.services;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.util.Log;
 
 import com.mss.domain.models.Order;
 import com.mss.domain.models.OrderItem;
 import com.mss.domain.models.OrderPickedUpItem;
 import com.mss.domain.models.OrderPickupItem;
 import com.mss.domain.models.Preferences;
 import com.mss.domain.models.Route;
 import com.mss.domain.models.RoutePoint;
 import com.mss.domain.models.Status;
 import com.mss.infrastructure.ormlite.DatabaseHelper;
 import com.mss.infrastructure.ormlite.OrmliteOrderItemRepository;
 import com.mss.infrastructure.ormlite.OrmliteOrderPickupItemRepository;
 import com.mss.infrastructure.ormlite.OrmliteOrderRepository;
 import com.mss.infrastructure.ormlite.OrmlitePreferencesRepository;
 import com.mss.infrastructure.ormlite.OrmliteStatusRepository;
 import com.mss.utils.IterableHelpers;
import com.mss.utils.MathHelpers;
 
 public class OrderService {
 	private static final String TAG = OrderService.class.getSimpleName();
 	
 	private DatabaseHelper databaseHelper;
 	private OrmliteOrderRepository orderRepo;
 	private OrmliteOrderItemRepository orderItemRepo;
 	private OrmliteOrderPickupItemRepository orderPickUpItemRepo;
 	private OrmlitePreferencesRepository preferencesRepo;
 	private OrmliteStatusRepository statusRepo;
 	
 	public OrderService(DatabaseHelper databaseHelper) throws Throwable{
 		this.databaseHelper = databaseHelper;
 		orderRepo = new OrmliteOrderRepository(this.databaseHelper);
 		orderItemRepo = new OrmliteOrderItemRepository(this.databaseHelper);
 		orderPickUpItemRepo = new OrmliteOrderPickupItemRepository(this.databaseHelper);
 		preferencesRepo = new OrmlitePreferencesRepository(this.databaseHelper);
 		statusRepo = new OrmliteStatusRepository(this.databaseHelper);		
 	}
 	
 	public Order getById(long id) {
 		try {
 			return orderRepo.getById(id);
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 			return null;
 		}
 	}
 	
 	public Iterable<Order> getByRoutePoint(RoutePoint routePoint){		
 		try {
 			return orderRepo.findByRoutePointId(routePoint.getId());
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 			return new ArrayList<Order>();
 		}
 	}
 	
 	public Iterable<Order> findByOrderDate(Date date){		
 		try {
 			Date dateOnly = new Date(date.getYear(), date.getMonth(), date.getDate());
 			return orderRepo.findByDate(dateOnly);
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 			return new ArrayList<Order>();
 		}
 	}
 	
 	public OrderPickupItem getOrderPickupItemById(long id) {
 		try {
 			return orderPickUpItemRepo.getById(id);
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 			return null;
 		}
 	}
 	
 	public Iterable<OrderPickupItem> getOrderPickupItems(long priceListId, Iterable<Long> categoryFilter) {
 		try {
 			return orderPickUpItemRepo.findByPriceListId(priceListId, IterableHelpers.toArray(Long.class, categoryFilter));
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 			return new ArrayList<OrderPickupItem>();
 		}
 	}
 	
 	public Iterable<OrderPickupItem> getOrderPickupItems(long priceListId, Iterable<Long> categoryFilter, String searchCriteria) {
 		try {
 			return orderPickUpItemRepo.findByPriceListId(priceListId, IterableHelpers.toArray(Long.class, categoryFilter), searchCriteria);
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 			return new ArrayList<OrderPickupItem>();
 		}
 	}
 	
 	public Iterable<OrderPickedUpItem> getOrderPickedUpItems(long orderId) {
 		try {			
 			Iterable<OrderItem> orderItems = orderItemRepo.findByOrderId(orderId);
 			ArrayList<OrderPickedUpItem> orderPickedUpItems = new ArrayList<OrderPickedUpItem>();
 			for (OrderItem orderItem : orderItems) {
 				orderPickedUpItems.add(
 						new OrderPickedUpItem(
 								orderItem.getProductId(),
 								orderItem.getProductName(), 
 								orderItem.getPrice(),
 								orderItem.getCount(), 
 								orderItem.getProductUnitOfMeasureId(),
 								orderItem.getUnitOfMeasureId(),
 								orderItem.getUnitOfMeasureName(),
 								orderItem.getCountInUnitOfMeasure()));
 			}	
 			
 			return orderPickedUpItems;
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 			return new ArrayList<OrderPickedUpItem>();
 		}
 	}
 	
 	public OrderItem getOrderItemById(long id) {
 		try {
 			return orderItemRepo.getById(id);
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 			return null;
 		}
 	}
 	
 	public Iterable<OrderItem> getOrderItems(long orderId) {
 		try {			
 			return orderItemRepo.findByOrderId(orderId);
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 			return new ArrayList<OrderItem>();
 		}
 	}
 		
 	public Order createOrder(Route route, RoutePoint routePoint) {
 		Order order = new Order(route, routePoint);		
 		return order;
 	}
 	
 	public void saveOrder(Order order, Iterable<OrderPickedUpItem> pickedUpItems){
 		try {
 			Preferences preferences = preferencesRepo.getById(Preferences.ID);
 			orderRepo.save(order);
 			double amount = 0;
 			Iterable<OrderItem> items = orderItemRepo.findByOrderId(order.getId());
 			for (OrderItem orderItem : items) {
 				boolean found = false;				
 				for (OrderPickedUpItem pickedUpItem : pickedUpItems) {
 					if (orderItem.getProductId() == pickedUpItem.getId()) {
 						orderItem.setProductUnitOfMeasureId(pickedUpItem.getProductUoMId());
 						orderItem.setUnitOfMeasureId(pickedUpItem.getUoMId());
 						orderItem.setUnitOfMeasureName(pickedUpItem.getUoMName());
 						orderItem.setCountInUnitOfMeasure(pickedUpItem.getCountInBase());
 						orderItem.setCount(pickedUpItem.getCount());
 						orderItem.setPrice(pickedUpItem.getItemPrice());
 						orderItemRepo.save(orderItem);						
 						found = true;
 						
						amount = amount + orderItem.getAmount();						
 						break;
 					}
 				}
 				
 				if (!found) 
 					orderItemRepo.delete(orderItem);
 			}	
 			
 			for (OrderPickedUpItem pickedUpItem : pickedUpItems) {				
 				boolean found = false;
 				for (OrderItem orderItem : items) {
 					if (orderItem.getProductId() == pickedUpItem.getId()) {
 						found = true;						
 						break;
 					}
 				}
 				
 				if (!found) {
 					OrderItem newOrderItem = new OrderItem(order.getId());
 					newOrderItem.setProductId(pickedUpItem.getId());
 					newOrderItem.setProductName(pickedUpItem.getName());
 					newOrderItem.setProductUnitOfMeasureId(pickedUpItem.getProductUoMId());
 					newOrderItem.setPrice(pickedUpItem.getItemPrice());
 					newOrderItem.setCount(pickedUpItem.getCount());					
 					newOrderItem.setUnitOfMeasureId(pickedUpItem.getUoMId());
 					newOrderItem.setUnitOfMeasureName(pickedUpItem.getUoMName());
 					newOrderItem.setCountInUnitOfMeasure(pickedUpItem.getCountInBase());
 					orderItemRepo.save(newOrderItem);		
 					
 					amount = amount + newOrderItem.getAmount();
 				}
 			}
 			
			order.setAmount(MathHelpers.Round(amount, 2));
 			orderRepo.save(order);
 			
 			RoutePointService routePointService = new RoutePointService(databaseHelper);
 			RoutePoint routePoint = routePointService.getById(order.getRoutePointId());
 			if (routePoint.getStatusId() == preferences.getDefaultRoutePointStatusId()) {
 				Status defaultAttendedStatus = statusRepo.getById(preferences.getDefaultRoutePointAttendedStatusId());
 				
 				routePoint.setStatus(defaultAttendedStatus);
 				routePointService.savePoint(routePoint);
 			}
 			
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 		}
 	}
 	
 	public void deleteOrder(Order order) {
 		try {
 			orderRepo.delete(order);
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 		}
 	}
 	
 	public boolean canBeEditedOrDeleted(long orderId) {
 		try {
 			return !orderRepo.getById(orderId).getIsSynchronized();
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 		}
 		
 		return false;
 	}
 }
