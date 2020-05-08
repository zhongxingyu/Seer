 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.eboy.action.admin;
 
 import com.eboy.po.Delivery;
 import com.eboy.po.Order;
 import com.eboy.service.OrderService;
 import com.eboy.service.DeliveryService;
 import com.opensymphony.xwork2.ActionSupport;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts2.ServletActionContext;
 
 /**
  *
  * @author Tongda
  */
 public class AddOrderDeliveryAction extends ActionSupport {
 
         private OrderService orderService;
        private DeliveryService deliveryService;
         
         @Override
         public String execute() {
                 HttpServletRequest request = ServletActionContext.getRequest();
                 
                 int orderId = Integer.parseInt(request.getParameter("orderId"));
                 String deliveryLocation = request.getParameter("deliveryLocation");
                 String deliveryRemark = request.getParameter("deliveryRemark");
                 Order order = orderService.getOrder(orderId);
                 Delivery delivery = new Delivery();
                 delivery.setOrder(order);
                 delivery.setDeliveryLocation(deliveryLocation);
                 delivery.setDeliveryTime(new Date());
                 delivery.setDeliveryRemark(deliveryRemark);
                 deliveryService.addDelivery(delivery);
                 String responseText = delivery.getDeliveryId().toString();
                 HttpServletResponse response = ServletActionContext.getResponse();
                 response.setCharacterEncoding("utf-8");
                 response.setContentType("text/plain");
                 try{
                         PrintWriter out = response.getWriter();
                         out.print(responseText);
                         out.flush();
                         out.close();
                 }
                 catch(IOException ex)
                 {
                         
                 }
                 return null;
         }
 
         public OrderService getOrderService() {
                 return orderService;
         }
 
         public void setOrderService(OrderService orderService) {
                 this.orderService = orderService;
         }
         
         public DeliveryService getDeliveryService() {
                 return deliveryService;
         }
 
         public void setDeliveryService(DeliveryService deliveryService) {
                 this.deliveryService = deliveryService;
         }
 
 }
