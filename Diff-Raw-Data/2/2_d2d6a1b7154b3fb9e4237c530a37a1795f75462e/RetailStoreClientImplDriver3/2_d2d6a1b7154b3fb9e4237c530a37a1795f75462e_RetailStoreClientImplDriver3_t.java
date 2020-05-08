 package app.client;
 
 import java.io.IOException;
 
 import app.orb.RetailStorePackage.InsufficientQuantity;
 import app.orb.RetailStorePackage.NoSuchItem;
 
 public class RetailStoreClientImplDriver3 extends RetailStoreClientImpl {
 
 	public RetailStoreClientImplDriver3(String customerID)
 			throws IOException {
 		super(customerID, null);
 	}
 	
 	public void run() {
 		try {
 			for (int i = 0; i != 60; i++) {
				System.out.println("Attempting to purchase 1 of 1000");
 				purchaseItem(1000, 1); // should succeed
 				Thread.sleep(1000);
 			}		
 		} catch (NoSuchItem e) {
 			System.err.println(getCustomerID() + ": The requested item does not exist!");
 		} catch (InsufficientQuantity e) {
 			System.err.println(getCustomerID() + ": There is not enough stock to fulfill your order!");
 		} catch (Exception e) {
 		    System.err.println("Purchase exception: " + e.toString());
 		    e.printStackTrace();
 		}
 		shutdown();
 	}
 }
