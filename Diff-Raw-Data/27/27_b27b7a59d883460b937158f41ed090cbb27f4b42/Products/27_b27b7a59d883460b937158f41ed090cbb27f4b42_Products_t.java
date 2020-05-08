 package gameCommons.balanceSettings;
 
 import gameCommons.balanceSettings.DataProduct;
 import gameCommons.balanceSettings.DataItemProduct;
 
 
 /*
 Products product = new Products();
 product(5).xpNeeded
 product.5.xpNeeded 
 product.with(5).xpNeeded
 */
 public class Products {
     protected DataProduct settings = null;
 
 	public Products() {
         this.settings = create();
     }
 
    public DataItemProduct with(String key) {
         return settings.read(key);
     }
 
     protected DataProduct create() {
         return new DataProduct();
     }
 }
