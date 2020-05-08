 package GatewayMapper;
 import Resources.*;
 
 public class Controller {
     ProductGateway prodController;
 
     public Controller() {
         prodController = new ProductGateway();
     }
     public Product getProduct(int ID){
         return prodController.getProduct(ID);
     }
     public Product searchProdByName(String name) {
         return prodController.searchProdByName(name);
     }
     public boolean addProduct(int ID, String name, int volume, int quantity, String description, int price){
         return prodController.addProduct(ID, name, volume, quantity, description, price);
     }
     public boolean editProduct(int ID, String name, int volume, int quantity, String description, int price){
         return prodController.editProduct(ID, name, volume, quantity, description, price);
     }
     public boolean deleteProduct(int ID){
         return prodController.deleteProduct(ID);
     }
     public int getProductListsize() {
         return prodController.getProductListsize();
     }
    public int getPackagetListsize() {
        return prodController.getPackagetListsize();
    }
     public Product showProducts(int index) {
         return prodController.showProducts(index);
     }
     public boolean getAllProducts() {
         return prodController.getAllProducts();
     }
     public boolean searchForProduct(String name) {
         return prodController.searchForProduct(name);
     }
     public boolean searchForProduct(int ID) {
         return prodController.searchForProduct(ID);
     }
 }
