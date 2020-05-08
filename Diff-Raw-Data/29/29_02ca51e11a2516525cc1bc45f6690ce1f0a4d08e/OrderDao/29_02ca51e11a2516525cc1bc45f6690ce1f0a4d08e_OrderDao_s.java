 package com.df.order.dao;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Expression;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.df.core.persist.eclipselink.Property;
 import com.df.masterdata.dao.ItemDao;
 import com.df.order.entity.Constants;
 import com.df.order.entity.Constants.ORDER;
 import com.df.order.entity.Order;
 
 public class OrderDao extends TransactionDataAccessFoundation {
 
 	@Autowired
 	private ItemDao itemDao;
 
 	public void setItemDao(ItemDao itemDao) {
 		this.itemDao = itemDao;
 	}
 
 	public void updateOrderStatus() {
 
 	}
 
	public List<Order> getOrderList(String storeCode,long userId, List<Long> orderIds) {
 		CriteriaBuilder builder = createQueryBuilder();
 		CriteriaQuery<Order> query = builder.createQuery(Order.class);
 		Root<Order> root = query.from(Order.class);
 		Expression<Long> inExpression = root.get(Constants.ORDER.TRAN_ID);
 		Predicate inPredicate = inExpression.in(orderIds);
 		Predicate storeCodeEqal = builder.equal(root.get(ORDER.STORE_CODE), storeCode);
 		Predicate ownerIdEqual = builder.equal(root.get(ORDER.OWNER_ID), userId);
		query.where(storeCodeEqal,ownerIdEqual, inPredicate);
 		EntityManager em = getEntityManager();
 		TypedQuery<Order> tQuery = em.createQuery(query);
 		return tQuery.getResultList();
 	}
 
	public List<Order> getOrderList(String storeCode,List<Long> orderIds) {
 		CriteriaBuilder builder = createQueryBuilder();
 		CriteriaQuery<Order> query = builder.createQuery(Order.class);
 		Root<Order> root = query.from(Order.class);
 		Expression<Long> inExpression = root.get(ORDER.TRAN_ID);
 		Predicate inPredicate = inExpression.in(orderIds);
 		Predicate storeCodeEqal = builder.equal(root.get(ORDER.STORE_CODE), storeCode);
		query.where(storeCodeEqal,inPredicate);
 		EntityManager em = getEntityManager();
 		TypedQuery<Order> tQuery = em.createQuery(query);
 		return tQuery.getResultList();
 	}
 
 	public Order getOrderById(String storeCode, long orderId) {
 		Property<String> p1 = new Property<String>(ORDER.STORE_CODE, storeCode);
 		Property<Long> p2 = new Property<Long>(ORDER.TRAN_ID, orderId);
 		return this.findSingleEntityByProperties(Order.class, p1, p2);
 	}
 
 	public Order getOrderByServiceCard(String storeCode, long serviceCardId) {
 		Property<String> p1 = new Property<String>(ORDER.STORE_CODE, storeCode);
 		Property<Long> p2 = new Property<Long>(ORDER.SERVICE_CARD_ID, serviceCardId);
 		return this.findSingleEntityByProperties(Order.class, p1, p2);
 	}
 }
