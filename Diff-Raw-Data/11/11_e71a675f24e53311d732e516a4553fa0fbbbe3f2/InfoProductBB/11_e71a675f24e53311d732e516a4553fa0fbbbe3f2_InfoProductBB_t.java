 package dit126.group4.group4shop_app.view;
 
 import dit126.group4.group4shop.core.IProductCatalogue;
 import dit126.group4.group4shop.core.Product;
 import dit126.group4.group4shop.core.ProductImage;
 import dit126.group4.group4shop_app.model.Group4Shop;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Provider;
 import org.primefaces.model.DefaultStreamedContent;
 import org.primefaces.model.StreamedContent;
 /**
  *
  * @author David
  */
 @Named("infoProduct")
 @SessionScoped
 public class InfoProductBB implements Serializable{
     
     
     private Long id;
     
     private String name;
     private String price;
     private String category;
     private String description;
     private byte[] imageData;
     
     @Inject
     private Provider<Group4Shop> shop;
     
     public void setSelected(String id) {
         Product p = shop.get().getProductCatalogue().find(Long.valueOf(id));
         this.id = p.getId();
         this.name = p.getName();
         this.price = String.valueOf(p.getPrice());
         this.category = p.getCategory();
         this.description = p.getDescription();
         
         if (p.getImage() != null){
             this.imageData = p.getImage().getImageBytes();
         }else{
            this.imageData = null;
         }
     }
     
     protected IProductCatalogue getProductCatalogue() {
         return shop.get().getProductCatalogue();
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
     
     public StreamedContent getImage() throws IOException{
         if(this.imageData == null){
             return null;
         }
         StreamedContent blobImage = new DefaultStreamedContent(new ByteArrayInputStream(this.imageData), "image/jpg");
         return blobImage;
     }
 }
