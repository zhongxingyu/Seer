 package org.tpspencer.tal.objexj.sample.beans.order;
 
 import java.util.List;
 
 import org.tpspencer.tal.objexj.annotations.ObjexRefProp;
 import org.tpspencer.tal.objexj.annotations.ObjexStateBean;
 import org.tpspencer.tal.objexj.sample.api.order.OrderItem;
 
 @ObjexStateBean(name="Order")
 public class OrderBean {
     private static final long serialVersionUID = 1L;
 
     private long account;
     
     @ObjexRefProp(owned=true, type=OrderItem.class, newType="OrderItemBean")
     private List<String> items;
     
     @ObjexRefProp(owned=true, type=OrderItem.class)
     private String test;
     
     private void init() {
     }
 }
