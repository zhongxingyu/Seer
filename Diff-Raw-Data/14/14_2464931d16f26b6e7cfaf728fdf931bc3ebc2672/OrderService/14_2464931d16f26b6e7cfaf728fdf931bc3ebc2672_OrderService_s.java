 package org.jpetstore.domain.logic;
 
 import org.jpetstore.domain.Order;
 

/**
 * Separate OrderService interface, implemented by PetStoreImpl
 * in addition to PetStoreFacade.
 *
 * <p>Mainly targeted at usage as remote service interface,
 * just exposing the <code>getOrder</code> method.
 *
 * @author Juergen Hoeller
 * @since 26.12.2003
 * @see PetStoreFacade
 * @see PetStoreImpl
 * @see org.springframework.samples.jpetstore.service.JaxRpcOrderService
 */
 public interface OrderService {
 
 	Order getOrder(int orderId);
 
 }
