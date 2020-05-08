 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Domain;
 
 import GatewayMapper.Facade;
 import Resources.Product;
 import java.util.ArrayList;
 
 /**
  *
  * @author Aaron
  */
 public class ProductList {
     
     private ArrayList<Product> productList = new ArrayList<>();
     private Product currentProduct;
     Facade facade = new Facade();
     
      public void addToProductList(int prodID, String prodName, int prodVol, int prodQTY, String prodDisc, int prodPrice) {
         productList.add(new Product(prodID, prodName, prodVol, prodQTY, prodDisc, prodPrice));
      }
 
     public Product getCurrentProduct(int i){
         currentProduct = productList.get(i);
         return currentProduct;
     }
     
     public int getProductListsize() {
         return productList.size();   
     }
     
     public void clearProductList(){
         productList.clear();
     }
     
     //these are the getters
     public int getProdID() {
         return currentProduct.getProductID();
     }
     
     public String getProdName() {
         return currentProduct.getName();
     }
     
     public int getProdVol() {
         return currentProduct.getVolume();
     }
     
     public int getProdQTY() {
         return currentProduct.getQuantity();
     }
     
     public String getProdDisc() {
         return currentProduct.getDescription();
     }
     
     public int getProdPrice() {
         return currentProduct.getPrice();
     }
     
     //these are the setters
     public void editProdName(String newName){
         currentProduct.setName(newName);
     }
     
     public void editProdVol(int newVol){
         currentProduct.setVolume(newVol);
     }
     
     public void editProdQTY(int newQTY){
         currentProduct.setQuantity(newQTY);
     }
     
     public void editProdDisc(String newDisc){
         currentProduct.getDescription();
     }
     
     public void editProdPrice(int newPrice){
         currentProduct.setPrice(newPrice);
     }
 
     //communication to productGateway
     public boolean buildProductList(ProductList productList){
         return facade.buildProductList(productList);
     }
     public boolean addProduct(String prodName, int prodVol, int prodQTY, String prodDisc, int prodPrice){
         return facade.addProduct(prodName, prodVol, prodQTY, prodDisc, prodPrice);
     }
     public boolean saveEditedProduct(int prodID, String prodName, int prodVol, int prodQTY, String prodDisc, int prodPrice) {
         return facade.saveEditedProduct(prodID, prodName, prodVol, prodQTY, prodDisc, prodPrice);
     }
     public boolean deleteProduct(int prodID) {
         return facade.deleteProduct(prodID);
     }
     
     
     //product search
    public Product searchProdByNameinArray(String name) {
            Product temp = null;
         for (int i = 0; i < productList.size(); i++) {
             if (productList.get(i).getName().equals(name)) {
                temp = productList.get(i);
             }
         }
        return temp;
     }

     //need to be changed to java not SQL
     
     
 }
