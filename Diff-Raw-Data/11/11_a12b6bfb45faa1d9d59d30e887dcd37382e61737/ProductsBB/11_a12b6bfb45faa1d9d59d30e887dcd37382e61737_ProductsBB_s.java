 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop_app.view;
 
 import dit126.group4.group4shop.core.Product;
 import dit126.group4.group4shop_app.model.Group4Shop;
 import dit126.group4.group4shop_app.controller.ContainerNavigator;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
 import javax.faces.component.UIColumn;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import org.primefaces.model.SortMeta;
 import org.primefaces.model.SortOrder;
 import javax.inject.Provider;
 
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
     private String productfilter = "all";
     private List<SortMeta> preSortOrder = new ArrayList();
     
     @PostConstruct
     public void post() {
         // We know all injection are done so shop not null (hopefully)
         cn = new ContainerNavigator(0, 3, shop.getProductCatalogue());
         buildSortOrder();
     }
     
            /*
      * method to build initial sort order for multisort
      */
     private void buildSortOrder() {
         UIViewRoot viewRoot =  FacesContext.getCurrentInstance().getViewRoot();
         UIComponent column = viewRoot.findComponent("productsDT:nameColumn"); 
 
         SortMeta sm1 = new SortMeta();
         sm1.setSortBy((org.primefaces.component.api.UIColumn)(UIColumn)column);
         sm1.setSortField("nameColumn");
         sm1.setSortOrder(SortOrder.DESCENDING);
         preSortOrder.add(sm1);          
     }
 
     public List<SortMeta> getPreSortOrder(){
         return preSortOrder;
     }
     
      public void setProductfilter(String filter){
         this.productfilter = filter;
     }
     
     
     public String getProductfilter(){
         return this.productfilter;
     }
     
     public List<Product> getProducts(){
         int count = shop.getProductCatalogue().getCount();
         List<Product> productList = shop.getProductCatalogue().getRange(0, count);     
         return productList;
     }
     /*
      * public List<Product> getByCategory(String cat){
      * List<Product> productList = shop.getProductCatalogue().getByCategory(cat);
      * return productList;
      * }
      */
     
     public List<Product> next() {
        // inte säker på att ny cn behöver skapas
        cn = new ContainerNavigator(0, 3, shop.getProductCatalogue());
        return cn.next();
     }
     
     public List<Product> prev() {
        cn = new ContainerNavigator(0, 3, shop.getProductCatalogue());
        return cn.previous();
     }
     
     public String navigate(String target) {
         return target;
     }
 }
