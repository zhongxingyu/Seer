 package dit126.group4.group4shop_app.view;
 
 import dit126.group4.group4shop.core.IProductCatalogue;
 import dit126.group4.group4shop.core.OrderItem;
 import dit126.group4.group4shop.core.Product;
 import dit126.group4.group4shop_app.model.Group4Shop;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 /**
  *
  * @author David
  */
 @Named("infoProduct")
 @RequestScoped
 public class InfoProductBB {
     
     
     private Long id;
     
     private String name;
     private String price;
     private String category;
     private String description;
     @Inject
     private Group4Shop shop;
     
     public void setSelected(String id) {
         Logger.getAnonymousLogger().log(Level.INFO, "setSelected id={0}", id);
         Product p = shop.getProductCatalogue().find(Long.valueOf(id));
         Logger.getAnonymousLogger().log(Level.INFO, "setSelected p={0}", p);
         this.id = p.getId();
         this.name = p.getName();
         this.price = String.valueOf(p.getPrice());
         this.category = p.getCategory();
         this.description = p.getDescription();
     }
     
     public String actOnSelected() {
        //OrderItem orderI = new OrderItem(id, (Product) shop.getProductCatalogue().getByName(name), 1);
         //lägg till OrderItem till PurchaseOrder
         return "banan";
     }
     
     protected IProductCatalogue getProductCatalogue() {
         return shop.getProductCatalogue();
     }
     
     public Long getId() {
         return id;
     }
     
     public String getName() {
         return name;
     }
     
     public String getPrice() {
         return price;
     }
     
     public String getCategory() {
         return category;
     }
     
     public String getDescription() {
         return description;
     }
     
 }
