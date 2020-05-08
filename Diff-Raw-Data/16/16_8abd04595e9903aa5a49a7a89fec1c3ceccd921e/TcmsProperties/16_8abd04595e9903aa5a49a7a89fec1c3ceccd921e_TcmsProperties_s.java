 /*
  * Copyright (C) 2012 Red Hat, Inc.     
  * 
  * This copyrighted material is made available to anyone wishing to use, 
  * modify, copy, or redistribute it subject to the terms and conditions of the 
  * GNU General Public License v.2.
  * 
  * Authors: Adam Saleh (asaleh at redhat dot com)
  *          Jan Rusnacko (jrusnack at redhat dot com)
  */
 package NitrateIntegration;
 
 import com.redhat.nitrate.TcmsConnection;
 import com.redhat.nitrate.TcmsException;
 import com.redhat.nitrate.command.Product;
 import com.redhat.nitrate.command.TestCase;
 import com.redhat.nitrate.command.TestPlan;
 import com.redhat.nitrate.command.User;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import redstone.xmlrpc.XmlRpcArray;
 import redstone.xmlrpc.XmlRpcStruct;
 
 /**
  *
  * @author asaleh
  */
 public class TcmsProperties {
 
     public final String plan;
     public final String product;
     public final String product_v;
     public final String category;
     public final String priority;
     public final String manager;
     private Integer plan_id = null;
     private Integer product_id = null;
     private Integer product_v_id = null;
     private Integer category_id = null;
     private Integer priority_id = null;
     private Integer manager_id = null;
     private TcmsConnection connection;
 
     public TcmsProperties(String plan, String product, String product_v, String category, String priority, String manager) {
         this.plan = plan;
         this.product = product;
         this.product_v = product_v;
         this.category = category;
         this.priority = priority;
         this.manager = manager;
     }
     
     
     public Integer getCategoryID() {
         return category_id;
     }
         
     public Integer getPriorityID() {
         return priority_id;
     }
     
     public Integer getPlanID() {
         return plan_id;
     }
     
     public Integer getProductID() {
         return product_id;
     }
     
     public Integer getProduct_vID() {
         return product_v_id;
     }
     
     public Integer getManagerId() {
         return manager_id;
     }
 
     public String getCategory() {
         return category;
     }
 
     public String getManager() {
         return manager;
     }
 
     public String getPlan() {
         return plan;
     }
 
     public String getPriority() {
         return priority;
     }
 
     public String getProduct() {
         return product;
     }
 
     public String getProduct_v() {
         return product_v;
     }
     
     public void setConnection(TcmsConnection connection) {
         this.connection = connection;
     }
 
     public void reload() {
         try {
             reloadPlanId();
         } catch (TcmsException ex) {
             Logger.getLogger(TcmsProperties.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             reloadProductId();
         } catch (TcmsException ex) {
             Logger.getLogger(TcmsProperties.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             reloadProduct_vId();
         } catch (TcmsException ex) {
             Logger.getLogger(TcmsProperties.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             reloadPriorityId();
         } catch (TcmsException ex) {
             Logger.getLogger(TcmsProperties.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             reloadCategoryId();
         } catch (TcmsException ex) {
             Logger.getLogger(TcmsProperties.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             reloadManagerId();
         } catch (TcmsException ex) {
             Logger.getLogger(TcmsProperties.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     
     /**
      * Checks all fields of <code>properties</code>. If a field is not set, 
      * string describing problem is added to the list, that is returned
      *
      * @param checkEnvironmentGroup Check also environment group, if true
      *      (generally environment group is optional, so may not be checked)
      * @return List of error messages
      */
     public static List<String> checkUsersetProperties(TcmsProperties properties) {
         List problems = new LinkedList();
 
         if (properties.getPlanID() == null) {
             problems.add("\"" + properties.plan + "\" is possibly wrong plan id");
         }
 
         if (properties.getProductID() == null) {
             problems.add("\"" +properties.product + "\" is possibly wrong product name (couldn't check product version and category)");
         } else {
             if (properties.getProduct_vID() == null) {
                 problems.add("\"" +properties.product_v + "\" is possibly wrong product version");
             }
             if (properties.getCategoryID() == null) {
                 problems.add("\"" +properties.category + "\" is possibly wrong category name");
             }
         }
 
         if (properties.getPriorityID() == null) {
             problems.add("\"" + properties.priority + "\" is possibly wrong priority name");
         }
 
         if (properties.getManagerId() == null) {
             problems.add("\"" + properties.manager + "\" is possibly wrong manager's username");
         }
 
         return problems;
     }
 
 
     public void reloadPlanId() throws TcmsException {
         plan_id = null;
         TestPlan.get get = new TestPlan.get();
         try {
             get.id = Integer.parseInt(plan);
 
         } catch (Exception e) {//plan is not numeric
             return;
         }
         Object o = connection.invoke(get);
 
         if (o instanceof XmlRpcStruct) {
             TestPlan result = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, TestPlan.class);
             plan_id = result.plan_id;
         }
 
     }  
 
 
     public void reloadProductId() throws TcmsException {
         product_id = null;
         Product.check_product get = new Product.check_product();
         get.name = product;
         Object o = connection.invoke(get);
         if (o instanceof XmlRpcStruct) {
             Product result = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, Product.class);
             product_id = result.id;
         }
 
     }
 
     public void reloadProduct_vId() throws TcmsException {
         product_v_id = null;
         Product.get_versions get = new Product.get_versions();
         get.id_str = product;
         Object a = connection.invoke(get);
         if (a instanceof XmlRpcArray) {
             XmlRpcArray array = (XmlRpcArray) a;
             for (Object o : array) {
                 if (o instanceof XmlRpcStruct) {
                     Product.Version result = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, Product.Version.class);
                     if (result.value.contentEquals(product_v)) {
                         product_v_id = result.id;
                         return;
                     }
                 }
             }
         }
 
     }
 
 
     public void reloadCategoryId() throws TcmsException  {
         category_id = null;
         Product.check_category get = new Product.check_category();
         get.name = category;
         get.product = product_id;
         Object o = connection.invoke(get);
         if (o instanceof XmlRpcStruct) {
             Product.Category result = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, Product.Category.class);
             category_id = result.id;
         }
     }
 
 
     public void reloadPriorityId() throws TcmsException  {
         priority_id = null;
         TestCase.check_priority get = new TestCase.check_priority();
         get.value = priority;
         Object o = connection.invoke(get);
         if (o instanceof XmlRpcStruct) {
             TestCase.Priority result = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, TestCase.Priority.class);
             priority_id = result.id;
         }
     }
 
 
     public void reloadManagerId() throws TcmsException  {
         manager_id = null;
         User.filter get = new User.filter();
         get.username__startswith = manager;
         Object o = connection.invoke(get);
         if (o instanceof XmlRpcArray) {
             if (((XmlRpcArray) o).size() == 0) {
                 manager_id = null;
                 return;
             }
             o = ((XmlRpcArray) o).get(0);
         }
         if (o instanceof XmlRpcStruct) {
             User result = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, User.class);
             manager_id = result.id;
         }
 
     }
 
 }
