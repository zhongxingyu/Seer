 package model;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import database.Database;
 
 import view.Error;
 
 import model.Product;
 /**
  * Class for deliveryFee.
  * It uses the singleton patters to make sure that only one deliveryFee object exist.
  * @author Sigurd Lund
  *
  */
 public class DeliveryFee extends Product {
 	
 	private static DeliveryFee deliveryFee;
 	private static double originalFee;
 	/**
 	 * Constructor that creates a product with the given name, price and comment.
 	 * @param name
 	 * @param price
 	 * @param comment
 	 */
 	private DeliveryFee(int id, String name, double price, String comment) {
 		super(id, name, price, 1, comment, 1);
 		originalFee = price;
 	}
 	
 	/**
 	 * Returns the deliveryFee product. It looks up in the database if the deliveryFee isn't already initialized.
 	 * @return DeliveryFee
 	 */
 	public static DeliveryFee getDeliveryFee(){
 		if (deliveryFee!=null)
 			return deliveryFee;
 		Database db = Database.getDatabase();
 		try {
 			ResultSet rs = db.select("SELECT * FROM product WHERE name='Frakt'");
 			if (rs.next()){
 				int idProduct = rs.getInt("idproduct");
 				double price = rs.getDouble("price");
 				String name = rs.getString("name");
 				String comment = rs.getString("comments");
 				deliveryFee = new DeliveryFee(idProduct, name, price, comment);
 				return deliveryFee;
 			}
 		} catch (SQLException e) {
 			Error.databaseError();
 		}
 		return null;
 	}
 	
 	/**
 	 * Sets the delivery fee to the original price.
 	 */
 	public void setPriceToOriginal(){
 		this.setPrice(originalFee);
 	}
 	
 	/**
 	 * Changes the original price and the price to the new price given.
 	 */
	@Override
	public void setPrice(double price){
 		originalFee = price;
 		super.setPrice(price);
 	}
 	
 	/**
 	 * Returns the original fee.
 	 * @return delivery fee as double
 	 */
 	public static double getOriginalFee(){
 		if (deliveryFee==null){
 			DeliveryFee.getDeliveryFee();
 		}
 		return originalFee;
 	}
 	
 }
