 package com.team33.entities.dao;
 
 import com.team33.entities.Orders;
 import com.team33.entities.LoginToken;
 import com.team33.entities.Purchase;
 import com.team33.entities.Rental;
 import java.util.List;
 
 import org.hibernate.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import com.team33.services.exception.*;
 import org.springframework.stereotype.Repository;
 
 /**
  * Provides data access during order processing
  *
  * @author Samual
  */
 @Repository
 public class OrdersDaoImpl implements OrdersDao {
     //tells Spring to inject the dependency
 
     @Autowired
     private SessionFactory sessionFactory;
 
     /**
      * Find all orders of a particular account that is activated
      *
      * @param loginToken
      * @return List<Orders>
      * @throws DataAccessException
      */
     @Override
     public List<Orders> getOrders(LoginToken loginToken) throws DataAccessException {
         Session curSession = sessionFactory.getCurrentSession();
         Query orderQuery;
         orderQuery = curSession.getNamedQuery("Orders.findByActiveAccount");
         orderQuery.setParameter("accountid", loginToken.getAccount().getId());
         return orderQuery.list();
     }
 
     /**
      * Retrieves an order provided its id
      *
      * @param orderId
      * @return Orders
      * @throws DataAccessException
      */
     @Override
     public Orders getOrder(Integer orderId) throws DataAccessException {
         return (Orders) sessionFactory.getCurrentSession().get(Orders.class, orderId);
     }
 
     /**
      * Persists an order in the database
      *
      * @param order
      * @throws DataAccessException
      */
     @Override
     public void saveOrder(Orders order) throws DataAccessException {
         sessionFactory.getCurrentSession().save(order);
     }
 
     /**
      * Removes an order from the database
      *
      * @param orderId
      */
     @Override
     public void removeOrder(Integer orderId) {
         Orders order = (Orders) sessionFactory.getCurrentSession().load(
                 Orders.class, orderId);
         if (null != order) {
             sessionFactory.getCurrentSession().delete(order);
         }
 
     }
 
     /**
      * Removes a purchase from an order
      *
      * @param order
      * @param purchase
      * @throws DataAccessException
      */
     @Override
     public void removePurchase(Orders order, Purchase purchase) throws DataAccessException {
         if (order != null) {
             if (purchase != null) {
                 sessionFactory.getCurrentSession().delete(purchase);
             }
         }
     }
 
     /**
      * Removes a rental from an order
      *
      * @param order
      * @param rental
      * @throws DataAccessException
      */
     @Override
     public void removeRental(Orders order, Rental rental) throws DataAccessException {
         if (order != null) {
             if (rental != null) {
                 sessionFactory.getCurrentSession().delete(rental);
             }
         }
     }
 
     /**
      * Persists a purchase for a particular order
      *
      * @param order
      * @param purchase
      * @throws DataAccessException
      */
     @Override
     public void savePurchase(Orders order, Purchase purchase) throws DataAccessException {
         if (order != null) {
             if (purchase != null) {
                 sessionFactory.getCurrentSession().save(purchase);
             }
         }
     }
 
     /**
      * Persists a rental for a particular order
      *
      * @param order
      * @param rental
      * @throws DataAccessException
      */
     @Override
     public void saveRental(Orders order, Rental rental) throws DataAccessException {
         if (order != null) {
             if (rental != null) {
                 sessionFactory.getCurrentSession().save(rental);
             }
         }
     }
 
     /**
      * Retrieves a login token given its id
      *
      * @param uuid
      * @return
      * @throws DataAccessException
      */
     @Override
     public LoginToken getLoginToken(int uuid) throws DataAccessException {
        Query query = sessionFactory.getCurrentSession().getNamedQuery("LoginToken.findById");
        query.setParameter("id", uuid);
        if (query.list().isEmpty()){
            throw new DataAccessException("The activation key is invalid");
         }
        return (LoginToken)query.list().get(0);
     }
 }
