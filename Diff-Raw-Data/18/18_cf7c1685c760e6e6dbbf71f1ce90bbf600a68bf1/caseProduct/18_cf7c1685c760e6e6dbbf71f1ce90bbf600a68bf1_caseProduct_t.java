 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beans;
 
 import data.Product;
 import exceptions.StorageException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
 
 /**
  * (name = "caseProduct1")
  *
  * @author christianlinde
  */
 @ManagedBean
 @RequestScoped
 public class caseProduct {
 
   private String name;
   private float price;
   private String description;
   private String manufacturer;
   private int addProductID;
 
   public caseProduct() {
     //this.name ="NewTestName";
   }
 
   public int addProduct(String name, float price, String description, String manufactorer) throws StorageException {
     return data.Storage.getInstance().addProduct(new data.Product(name, price, description, manufactorer));
   }
 
   public void loadProductByID(int id) {
     Product selectedProduct = data.Storage.getInstance().getProductById(id);
 
     if (selectedProduct != null) {
       this.addProductID = id;
       this.name = selectedProduct.getName();
       this.price = selectedProduct.getPrice();
       this.description = selectedProduct.getDescription();
       this.manufacturer = selectedProduct.getManufacturer();
     }
   }
 
   public String getName() {
     return this.name;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public float getPrice() {
     return this.price;
   }
 
   public String getDescription() {
     return this.description;
   }
 
   public String getManufacturer() {
     return this.manufacturer;
   }
 
   public void setPrice(float price) {
     this.price = price;
   }
 
   public void setDescription(String description) {
     this.description = description;
   }
 
   public void setManufacturer(String manufacturer) {
     this.manufacturer = manufacturer;
   }
 
   public void insertNewProduct() {
     try {
       this.addProductID = data.Storage.getInstance().addProduct(new Product(this.name, this.price, this.description, this.manufacturer));
     } catch (StorageException ex) {
       Logger.getLogger(caseProduct.class.getName()).log(Level.SEVERE, null, ex);
     }
   }
 
   public void updateProduct() {
     Product tmpProduct = data.Storage.getInstance().getProductById(addProductID);
    //if (tmpProduct != null) {
       tmpProduct.setName(name);
       tmpProduct.setPrice(price);
       tmpProduct.setDescription(description);
       tmpProduct.setManufacturer(manufacturer);
 
      data.Storage.getInstance().setProduct(tmpProduct);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler: " + tmpProduct.getName(), "Leser ist gespert");
      FacesContext.getCurrentInstance().addMessage("form", msg);
      
    //}
   }
 
   public List<Product> getAllProducts() {
     return data.Storage.getInstance().getAllProducts();
   }
 }
