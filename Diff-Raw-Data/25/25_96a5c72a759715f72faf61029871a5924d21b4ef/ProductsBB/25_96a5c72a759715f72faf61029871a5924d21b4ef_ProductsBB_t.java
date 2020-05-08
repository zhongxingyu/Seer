 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop_app.view;
 
 import dit126.group4.group4shop.core.Product;
import dit126.group4.group4shop_app.model.Group4Shop;
 import dit126.group4.group4shop_app.controller.ContainerNavigator;
 import java.io.Serializable;
 import java.util.List;
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  * Must have in session scope, has state (in ContainerNavigator)!
  * @author hajo
  */
 @Named("products")
 @SessionScoped  // NOTE enterprise package, else disaster!!!
 public class ProductsBB implements Serializable{
 
     private ContainerNavigator cn;
    @Inject
    private Group4Shop shop;
 
     @PostConstruct
     public void post() {
         // We know all injection are done so shop not null (hopefully)
         cn = new ContainerNavigator(0, 3, shop.getProductCatalogue());
     }
 
     public List<Product> getProducts(){
         int count = shop.getProductCatalogue().getCount();
         List<Product> productList = shop.getProductCatalogue().getRange(0, count);     
         return productList;
     }
 
     public void next() { 
         cn.next();
     }
 
     public void prev() {
        cn.previous();
     }
 
     public String navigate(String target) {
         return target;
     }
 }
