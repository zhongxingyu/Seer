 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop.core;
 
 /**
  *
  * @author Christian
  */
 public class Group4Shop implements IGroup4Shop{
  
     private IProductCatalogue productCatalogue;
     private IProductImageContainer productImageContainer;
     private IAddressCatalogue addressCatalogue;
    private IAdminRegister adminregister;
     private IUserRegister userregister;
     
     public Group4Shop(String puName){
         this.productCatalogue = new ProductCatalogue(puName);
         this.productImageContainer = new ProductImageContainer(puName);
         this.addressCatalogue = new AddressCatalogue(puName);
        this.adminregister = new AdminRegister(puName);
         this.userregister = new UserRegister(puName);
     }
 
     @Override
     public IProductCatalogue getProductCatalogue() {
         return this.productCatalogue;
     }
 
     @Override
     public IProductImageContainer getProductImageConatiner() {
         return this.productImageContainer;
     }
 
     @Override
     public IAddressCatalogue getAddressCatalogue() {
         return this.addressCatalogue;
     }
 
     @Override
    public IAdminRegister getAdminregister() {
        return this.adminregister;
     }
 
     @Override
     public IUserRegister getUserregister() {
         return this.userregister;
     }
     
 }
