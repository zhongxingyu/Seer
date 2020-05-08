 package com.rixon.virtualmarket.exchange.order.controller;
 
 import com.alibaba.fastjson.JSON;
 import com.alibaba.fastjson.serializer.PropertyFilter;
 import com.alibaba.fastjson.serializer.SerializerFeature;
 import com.rixon.virtualmarket.exchange.order.domain.Order;
 import com.rixon.virtualmarket.exchange.order.domain.OrderResponse;
 import com.rixon.virtualmarket.exchange.order.handler.OrderHandler;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * This class represents the controllers for handling requests related to Orders
  * User: rixon
  * Date: 19/8/13
  * Time: 1:44 PM
  */
 @Controller(value="/order")
 public class OrderController {
 
     @Autowired
     private OrderHandler orderHandler;
 
     @RequestMapping(method = RequestMethod.POST,
                    headers = {"Accept=application/xml,application/json"})
    public @ResponseBody OrderResponse handleOrder(@PathVariable Order order) {
         return orderHandler.placeOrder(order);
     }
 
     //TODO This is for testing connectivity remove after testing
     @RequestMapping(method = RequestMethod.GET,
                     headers = {"Accept=application/xml,application/json"})
     public @ResponseBody OrderResponse get() {
         OrderResponse response = new OrderResponse();
         response.setStatus("OK");
         response.setOrderID("10101");
         return response;
 
     }
 
     private String responseAsJSON(OrderResponse orderResponse){
         PropertyFilter filter = new PropertyFilter() {
             public boolean apply(Object source, String name, Object value) {
                 if (name.equals("orderOK")){
                     return false;
                 }
                 return true;
             }
         };
         SerializerFeature[] features = {SerializerFeature.UseISO8601DateFormat,
                 SerializerFeature.SortField,
                 SerializerFeature.WriteNullListAsEmpty
         };
         return JSON.toJSONString(orderResponse, filter, features);
     }
 
 
 }
