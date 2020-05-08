 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controlbeans;
 
 import backingbeans.CustomerPurchaseOrdersBackingBean;
 import core.Customer;
 import core.OrderItem;
 import core.PurchaseOrder;
 import java.io.Serializable;
 import java.util.List;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 import modelbeans.CartModelBean;
 import modelbeans.OrderBookModelBean;
 import monkeybusiness.SimpleLogin;
 
 /**
  *
  * @author Gustaf Werlinder
  */
 @Named
 @SessionScoped
 public class CompletePurchaseControlBean implements Serializable {
 
     @Inject
     private CustomerPurchaseOrdersBackingBean completePurchaseBackingBean;
     @Inject
     private SimpleLogin simpleLogin;
     @Inject
     private OrderBookModelBean orderBookModelBean;
     @Inject
     private CustomerPurchaseOrdersControlBean customerPurchaseOrdersControlBean;
     @Inject
     private CustomerShowPurchaseOrderControlBean customerShowPurchaseOrderControlBean; 
     @Inject
     private CartModelBean cartModelBean;
    PurchaseOrder purchaseOrder;
    Customer customer;
 
     public void completePurchaseOrder() {
     }
 
     public void actionListener(ActionEvent ae) {
         String userName = (String) ae.getComponent().getAttributes().get("userName");
         System.out.println(userName);
         customer = simpleLogin.getCustomer(userName);
         System.out.println("Customer:" + customer);
         customer.setCart(cartModelBean.getCart());
        
     }
     public String action()
     {
         purchaseOrder = customer.finishShopping();
         List<OrderItem> listOfItems = purchaseOrder.getItems();
         if (listOfItems.isEmpty()) {
             FacesContext.getCurrentInstance().addMessage("noItems", new FacesMessage("Your cart is empty"));
             return null;
         } else {
             orderBookModelBean.addOrder(purchaseOrder);
             cartModelBean.clearCart();
             customer.setCart(cartModelBean.getCart());
             customerPurchaseOrdersControlBean.action(customer.getUserName());
             
             customerShowPurchaseOrderControlBean.setPurchaseOrder(purchaseOrder);
             customerShowPurchaseOrderControlBean.makeOrderitemList();
             customerShowPurchaseOrderControlBean.calculateTotalOrderCost();
             
             return "customerShowPurchaseOrder?faces-redirect=true";
         }
         
     }
 }
