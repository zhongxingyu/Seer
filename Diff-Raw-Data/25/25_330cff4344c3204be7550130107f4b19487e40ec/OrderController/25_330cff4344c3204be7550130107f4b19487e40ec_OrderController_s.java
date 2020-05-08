 /*
  *This controls the access and process of video orders 
  * made by an active account logged in to a web session
  */
 package com.team33.controllers;
 
 import com.team33.entities.Account;
 import com.team33.entities.Orders;
 import com.team33.form.OrderRequest;
 import com.team33.services.AccountService;
 import com.team33.services.BrowseService;
 import com.team33.services.OrderService;
 import com.team33.services.exception.AccountNotActivatedException;
 import com.team33.services.exception.InsufficientFundsException;
 import com.team33.services.exception.PaymentException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestHeader;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * This is the controller for the order feature
  *
  * @author Mark
  */
 @Controller
 public class OrderController {
 
     @Autowired
     private OrderService orderService;
     @Autowired
     private BrowseService browseService;
     @Autowired
     private AccountService accountService;
 
     @RequestMapping(value = "/order/create", method = RequestMethod.POST)
    public String createOrder(@RequestHeader(value="referer", required=false)final String referer, @ModelAttribute("orderRequest") OrderRequest orderRequest, BindingResult result, HttpSession session) {
         String refererOrHome = StringUtils.hasText(referer) ? referer : "/";
         ShoppingCart cart = getCart(session);
         Integer loginToken = orderRequest.getLoginToken();
 
         if (cart == null) {
             return "redirect:" + refererOrHome;
         }
         Orders newOrder = new Orders();
         // Why do I need to do this manually?
         newOrder.setAccount(accountService.getAccountByLoginToken(loginToken));
 
         for (Integer videoID : cart.getPurchaseList()) {
             try {
                 orderService.addPurchase(videoID, newOrder.getOrdersPK().getId(), loginToken);
             } catch (AccountNotActivatedException ane) {
                 return "redirect:" + refererOrHome;
             }
         }
 
         for (Integer videoID : cart.getRentedList()) {
             try {
                 Calendar c = Calendar.getInstance();
                 // The rental period arbitrary
                 c.add(Calendar.DAY_OF_MONTH, 5);
                 orderService.addRental(videoID, newOrder.getOrdersPK().getId(), loginToken, c.getTime());
             } catch (AccountNotActivatedException ane) {
                 return "redirect:" + refererOrHome;
             }
         }
 
         try {
             orderService.confirmPayment(newOrder, loginToken, new Integer(orderRequest.getCreditCardVerification()), orderRequest.getTotalPrice());
         } catch (AccountNotActivatedException ane) {
             return "redirect:" + refererOrHome;
         } catch (PaymentException pe) {
             return "redirect:" + refererOrHome;
         } catch (InsufficientFundsException ife) {
             return "redirect:" + refererOrHome;
         }
         
         return "redirect:show.htm"; //?order=" + orderRequest.getVideotitle();
     }
 
     @RequestMapping("/order/new")
    public String newOrder(@RequestHeader(value="referer", required=false)final String referer, Map<String,Object>model, HttpSession session) {
         String refererOrHome = StringUtils.hasText(referer) ? referer : "/";
         Integer totalPrice = 0;
 
         Integer tokenID = (Integer) session.getAttribute(LoginController.ACCOUNT_ATTRIBUTE);
 
         ShoppingCart cart = getCart(session);
         
         if (cart == null) {
             return "redirect:" + refererOrHome;
         }
 
         Account a = null;
         if (tokenID != null) {
             a = accountService.getAccountByLoginToken(tokenID);
         }
 
         if (a == null) {
             a = new Account();
             a.setName("No User Logged In");
         }
         model.put("account", a);
         model.put("totalPrice", totalPrice);
 
         return "newOrder";
     }
 
     @RequestMapping("/order/show")
     public ModelAndView showOrder(@RequestParam("order") String order) {
         return new ModelAndView("showOrder", "order", order);
     }
 
     private ShoppingCart getCart(HttpSession session) {
         return (ShoppingCart)session.getAttribute(ShoppingCartController.SHOPPING_CART_COOKIE_NAME);
     }
 }
