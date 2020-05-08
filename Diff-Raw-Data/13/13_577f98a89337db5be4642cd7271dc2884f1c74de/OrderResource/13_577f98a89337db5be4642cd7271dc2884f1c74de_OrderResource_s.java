 package com.walmart.hackday.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.log4j.Logger;
 
 import com.walmart.hackday.dao.DAO;
 import com.walmart.hackday.entity.LineItem;
 import com.walmart.hackday.entity.Order;
 import com.walmart.hackday.entity.Profile;
 
 @Path("/order")
 public class OrderResource extends BaseResource {
 	private static Logger logger = Logger.getLogger(OrderResource.class);
 
 	// The Java method will process HTTP GET requests
 	@GET
 	@Produces("application/json")
 	public List<Order> handleGet(@QueryParam("store") Integer store, @QueryParam("profileId") Integer profileId, 
 			@QueryParam("orderId") Integer orderId, @QueryParam("orderStatus") String orderStatus) throws IOException {
 
 		Order order = null;
 		EntityManagerFactory emf = null;
 		EntityManager em = null;
 		List<Order> orders = new ArrayList<Order>();
 		Query query = null;
 		try {
 			emf = Persistence.createEntityManagerFactory("hackday");
 			em = emf.createEntityManager();	
 			if(store != null) {
 				String queryString = null;
 				if(orderStatus != null && !orderStatus.trim().equalsIgnoreCase("")){
					queryString = "SELECT order FROM " + Order.class.getName() + " order WHERE storeId = :storeId and status = :orderStatus order by duration";
 					query = em.createQuery(queryString);
 					query.setParameter("orderStatus", orderStatus);
 				} else {
					queryString = "SELECT order FROM " + Order.class.getName() + " order WHERE storeId = :storeId order by duration";
 					query = em.createQuery(queryString);
 				}
				
				query.setParameter("storeId", store);				
 			} else if (profileId != null) {
 				query = em.createQuery("SELECT order FROM " + Order.class.getName() + " order WHERE createdBy = :createdBy order by duration");
 				Profile createdBy = new Profile();
 				createdBy.setId(profileId);
 				query.setParameter("createdBy", createdBy);				
 			} else if (orderId != null) {
 				query = em.createQuery("SELECT order FROM " + Order.class.getName() + " order WHERE orderId = :orderId");
 				query.setParameter("orderId", orderId);
 			}
 			if(query != null) {
 				List resultList = query.getResultList();
 				for(Object obj : resultList) {
 					order = (Order)obj;
 					updateOrderPrice(order);
 					orders.add(order);
 				}
 			}
 		} catch (Exception exp) {
 			logger.error("handleGet(): error while fetching the orders for store", exp);
 		}
 		finally {
 			em.close();
 			emf.close();
 		}
 		return orders;
 	}
 	
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Order handlePost(Order order){
 		DAO dao = null;
 		Order orderResp = null;
 		try{
 			dao = new DAO();
 			orderResp = dao.get(Order.class, order.getOrderId());
 			if (orderResp != null){
 				if(order.getStatus() != null) {
 					orderResp.setStatus(order.getStatus());
 				}
 				if(order.getCheckInLatitude() != null) {
 					orderResp.setCheckInLatitude(order.getCheckInLatitude());
 				}				
 				if(order.getCheckInLongiude() != null) {
 					orderResp.setCheckInLongiude(order.getCheckInLongiude());
 				}
 				if(order.getPickedBy() != null) {
 					orderResp.setPickedBy(order.getPickedBy());
 				}	
 				if(order.getDuration() != null){
 					orderResp.setDuration(order.getDuration());
 				}
 				if(order.getDurationText() != null){
 					orderResp.setDurationText(order.getDurationText());
 				}
 				dao.update(orderResp);
 			} else {
 				orderResp = new Order();
 			}
 				
 		} catch (Exception exp) {
 			logger.error("handlePost(): error while updating the order - " + order.getOrderId(), exp);
 		} finally {
 			dao.close();
 		}
 
 		return orderResp;
 	}
 	
 	private void updateOrderPrice(Order order){
 		if(order != null) {
 			Set<LineItem> lineItems = order.getLineItems();
 			double price = 0.0;
 			for (LineItem lineItem : lineItems) {
 				price = price + lineItem.getSku().getPrice();
 			}
 			order.setPrice(price);
 		}
 	}
 }
