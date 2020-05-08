 package gameCommons.balanceSettings;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import gameCommons.system.Trace;
 import gameCommons.balanceSettings.DataStore;
 import gameCommons.balanceSettings.DataItemProduct;
 
 
 public class DataProduct {
 	protected DataStore ds;
 	protected Trace t;
 	public HashMap<String,DataItemProduct> cached = new HashMap<String,DataItemProduct>();
 	
 	public DataProduct() {};
 	public DataProduct(DataItemProduct product, Trace t) {
         cache(product);
         this.t = t;
     }
 	public DataProduct(DataStore ds, Trace t) {
 		this.ds = ds;
         this.t = t;
 	    refresh();	
     }
 
    public DataItemProduct read(String key) {
        return cached.get(key);
    }

     public void refresh() {
 	    ResultSet db_res = retrieve();
 	    DataItemProduct product = null;
 	    try {
 		    while (db_res.next()) {
 		        product = new DataItemProduct();
                 product.name = db_res.getString("Name");
 				product.coinsValue = db_res.getInt("CoinsValue");
 				product.premiumValue = db_res.getInt("PremiumValue");			
 	    	
 				cache(product);
 		    }
 	    } catch (SQLException e) {}
 	}
 
    protected void cache(DataItemProduct product) {
        cached.put(product.name, product);
     }
 
     protected ResultSet retrieve() {
 	    String db_sql = 
 	    	" SELECT Name, CoinsValue, PremiumValue "
 	        + " FROM Products ";
 	    return ds.query(db_sql, "read", null);
     }
 }
