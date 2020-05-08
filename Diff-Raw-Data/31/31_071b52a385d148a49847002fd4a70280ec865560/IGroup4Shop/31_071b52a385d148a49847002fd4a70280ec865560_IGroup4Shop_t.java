 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop.core;
 
 /**
  *
  * @author Christian
  */
 public interface IGroup4Shop {
     
     public IProductCatalogue getProductCatalogue();
     
     public IProductImageContainer getProductImageConatiner();
     
     public IAddressCatalogue getAddressCatalogue();
     
    public IAdminRegister getAdminRegister();
     
     public IUserRegister getUserregister();
     
 }
