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
     
<<<<<<< HEAD
     public IRatingCatalogue getRatingCatalogue();
=======
     public IShoppingCart getShoppingCart();
>>>>>>> b0e2ff5a65f95bb25e2f7a2121b785d78244ec19
     
 }
