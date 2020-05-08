 package dit126.group4.group4shop.core;
 
 /**
  *
  * @author Christian
  */
 public interface IGroup4Shop {
     
     public IProductCatalogue getProductCatalogue();
     
     public IProductImageContainer getProductImageConatiner();
     
     public IAddressCatalogue getAddressCatalogue();
     
     public IUserRegister getUserRegister();
     
     public IRolesRegister getRolesRegister();
     
     public IUserRolesRegister getUserRolesRegister();
     
     public IOrderBook getOrderBook();
     
     public IRatingCatalogue getRatingCatalogue();
     public IShoppingCart getShoppingCart();
     
 }
