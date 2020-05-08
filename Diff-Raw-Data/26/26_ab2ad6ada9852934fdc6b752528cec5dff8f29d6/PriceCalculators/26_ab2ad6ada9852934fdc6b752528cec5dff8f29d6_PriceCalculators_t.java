 package pizzaProgram.utils;
 
 import java.text.DecimalFormat;
 
 import javax.swing.JOptionPane;
 
 import pizzaProgram.core.DatabaseConstants;
 import pizzaProgram.dataObjects.Dish;
 import pizzaProgram.dataObjects.Extra;
 import pizzaProgram.dataObjects.Order;
 import pizzaProgram.dataObjects.OrderDish;
 import pizzaProgram.database.databaseUtils.DatabaseReader;
 
 /**
  * Class for generating properly formatted strings of the various prices in the
  * system.
  */
 public class PriceCalculators {
 	private static DecimalFormat formatter = new DecimalFormat("00");
 
 	private static int deliverMoms = -1;
 	private static int pickupMoms = -1;
 
 	private static int freeDeliveryThreshold = -1;
 	private static int deliveryCost = -1;
 
 	private static String restaurantName = "";
 
 	/**
 	 * Generates a priceString on the form "0,00" based on the price of the
 	 * dish, without VAT added (IE a price in norwegian re of 12345 would
 	 * return a string which contains "123,45".
 	 * 
 	 * @param d
 	 *            - the {@link pizzaProgram.dataObjects.Dish dish} to get the
 	 *            priceString for.
 	 * @return The priceString as specified above.
 	 */
 	public static String getPriceForDish(Dish d) {
 		return d.price / 100 + "," + formatter.format(d.price % 100);
 	}
 
 	/**
 	 * Generates a priceString on the form "0,00" based on the sum of the items
 	 * in the order, with VAT and delivery cost added (IE a price in norwegian
 	 * re of 12345 would return a string which contains "123,45".
 	 * 
 	 * @param o
 	 *            - the {@link pizzaProgram.dataObjects.Order order} to get the
 	 *            priceString for.
 	 * @return The priceString as specified above.
 	 */
 	public static String getPriceForOrderWithVATAndDelivery(Order o) {
 		int totalPrice = getPriceForOrder(o);
 		if (o.deliveryMethod.equals(Order.DELIVER_AT_HOME)) {
 			totalPrice += totalPrice * (deliverMoms / 100.0);
 			if (totalPrice < freeDeliveryThreshold) {
 				totalPrice += deliveryCost;
 			}
 		} else {
 			totalPrice += totalPrice * (pickupMoms / 100.0);
 		}
 		return totalPrice / 100 + "," + formatter.format(totalPrice % 100);
 	}
 
 	/**
 	 * Generates a priceString on the form "0,00" based on the sum of the items
 	 * in the order, without VAT and delivery cost added (IE a price in
 	 * norwegian re of 12345 would return a string which contains "123,45".
 	 * 
 	 * @param o
 	 *            - the {@link pizzaProgram.dataObjects.Order order} to get the
 	 *            priceString for.
 	 * @return The priceString as specified above.
 	 */
 	public static String getPriceForOrderWithoutVAT(Order o) {
 		int price = getPriceForOrder(o);
 		return price / 100 + "," + formatter.format(price % 100);
 	}
 
 	/**
 	 * Generates a priceString on the form "0,00" based on the sum of the items
 	 * in the order, with VAT, but without delivery cost added (IE a price in
 	 * norwegian re of 12345 would return a string which contains "123,45".
 	 * 
 	 * @param o
 	 *            - the {@link pizzaProgram.dataObjects.Order order} to get the
 	 *            priceString for.
 	 * @return The priceString as specified above.
 	 */
 	public static String getPriceForOrderWithVAT(Order o) {
 		int totalPrice = getPriceForOrder(o);
 		if (o.deliveryMethod.equals(Order.DELIVER_AT_HOME)) {
 			totalPrice += totalPrice * (deliverMoms / 100.0);
 		} else {
 			totalPrice += totalPrice * (pickupMoms / 100.0);
 		}
 		return totalPrice / 100 + "," + formatter.format(totalPrice % 100);
 	}
 
 	/**
 	 * Generates a priceString on the form "0,00" containing the delivery cost
 	 * of the provided order (IE a price in norwegian re of 12345 would return
 	 * a string which contains "123,45".
 	 * 
 	 * @param o
 	 *            - the {@link pizzaProgram.dataObjects.Order order} to get the
 	 *            delivery cost for.
 	 * @return The priceString as specified above.
 	 */
 	public static String getDeliveryCostForOrder(Order o) {
 		int totalPrice = getPriceForOrder(o);
 		totalPrice += totalPrice * (deliverMoms / 100.0);
 		if (o.deliveryMethod.equals(Order.DELIVER_AT_HOME)
 				&& totalPrice < freeDeliveryThreshold) {
 			return deliveryCost / 100 + ","
 					+ formatter.format(deliveryCost % 100);
 		}
 		return "0,00";
 	}
 
 	public static String getDeliveryCost() {
		return deliveryCost / 100 + "," + formatter.format(deliveryCost % 100);
 	}

	public static String getFreeDeliveryTreshold() {
		return freeDeliveryThreshold / 100 + ","
				+ formatter.format(freeDeliveryThreshold % 100);
 	}
 
 	/**
 	 * Generates a priceString on the form "0,00" containing the VAT for the
 	 * provided order (IE a price in norwegian re of 12345 would return a
 	 * string which contains "123,45".
 	 * 
 	 * @param o
 	 *            - the {@link pizzaProgram.dataObjects.Order order} to get the
 	 *            VAT for.
 	 * @return The priceString as specified above.
 	 */
 	public static String getVATForOrder(Order o) {
 		int totalPrice = getPriceForOrder(o);
 		if (o.deliveryMethod.equals(Order.DELIVER_AT_HOME)) {
 			totalPrice *= deliverMoms / 100.0;
 		} else {
 			totalPrice *= pickupMoms / 100.0;
 		}
 		return totalPrice / 100 + "," + formatter.format(totalPrice % 100);
 
 	}
 
 	/**
 	 * Generates the value of the order, in norwegian re (1/100 NOK), based on
 	 * the sum of the ordered dishes and extras in the order. Does not include
 	 * VAT or delivery cost.
 	 * 
 	 * @param o
 	 *            - the {@link pizzaProgram.dataObjects.Order order} to get the
 	 *            value for.
 	 * @return The value of the order in norwegian re as an int.
 	 */
 	private static int getPriceForOrder(Order o) {
 		int totalPrice = 0;
 		for (OrderDish od : o.orderedDishes) {
 			totalPrice += getValueForOrderDish(od);
 		}
 		return totalPrice;
 	}
 
 	/**
 	 * Generates the value of the ordered dish, in norwegian re (1/100 NOK),
 	 * based on the sum of the ordered dish and its extras. Does not include
 	 * VAT.
 	 * 
 	 * @param od
 	 *            - the {@link pizzaProgram.dataObjects.OrderDish ordered dish}
 	 *            to get the value for.
 	 * @return The value of the ordered dish in norwegian re as an int.
 	 */
 	private static int getValueForOrderDish(OrderDish od) {
 		int totalPrice = od.dish.price;
 		for (Extra e : od.getExtras()) {
 			if (e.priceFuncPart == '*') {
				totalPrice += od.dish.price * ((e.priceValPart - 100) / 100.0);
 			} else if (e.priceFuncPart == '-') {
 				totalPrice -= e.priceValPart;
 			} else {
 				totalPrice += e.priceValPart;
 			}
 		}
 		return totalPrice;
 	}
 
 	/**
 	 * Generates a priceString on the form "0,00" containing the VAT for the
 	 * provided ordered dish (IE a price in norwegian re of 12345 would return
 	 * a string which contains "123,45".
 	 * 
 	 * @param od
 	 *            - the {@link pizzaProgram.dataObjects.OrderDish ordered dish}
 	 *            to get the priceString for.
 	 * @return The priceString as specified above.
 	 */
 	public static String getPriceForOrderDish(OrderDish od) {
 		int totalPrice = getValueForOrderDish(od);
 		return totalPrice / 100 + "," + formatter.format(totalPrice % 100);
 	}
 
 	/**
 	 * Generates a string displaying the change in price if you add the given
 	 * Extra to the given Dish.
 	 * 
 	 * @param d
 	 *            - the {@link pizzaProgram.dataObjects.Dish dish} the info in
 	 *            the string is to be in relation to.
 	 * @param e
 	 *            - the {@link pizzaProgram.dataObjects.Extra extra} the info in
 	 *            the string is to be in relation to.
 	 * @return a string in the form of "+0,00" or "-0,00" depending on wether or
 	 *         not it is a positive or negative price change.
 	 */
 	public static String getPriceForExtraOnDish(Dish d, Extra e) {
 		if (e.priceFuncPart != '*') {
 			return e.priceFuncPart + "" + e.priceValPart / 100 + ","
 					+ formatter.format(e.priceValPart % 100);
 		}
 		int returnVal = (int) (d.price * ((e.priceValPart - 100) / 100.0));
 		char func = returnVal >= 0 ? '+' : '-';
 		returnVal = Math.abs(returnVal);
 		return func + "" + returnVal / 100 + ","
				+ formatter.format(returnVal % 100);
 	}
 
 	/**
 	 * Generates a string on the form "+0,00", "-0,00" or "*0,00" containing the
 	 * price modification adding the given extra will have on a dish.
 	 * 
 	 * @param e
 	 *            - the {@link pizzaProgram.dataObjects.Extra extra} to get the
 	 *            price modification from.
 	 * @return the priceString as specified above.
 	 */
 	public static String getPriceForExtra(Extra e) {
 		return e.priceFuncPart + "" + e.priceValPart / 100 + ","
 				+ formatter.format(e.priceValPart % 100);
 	}
 
 	/**
 	 * Method that sets the static fields of the class based on a fetch from the
 	 * Config table of the database.
 	 */
 	public static void getConstantsFromDataBase() {
 		deliverMoms = Integer
 				.parseInt(DatabaseReader
 						.getSettingByKey(DatabaseConstants.SETTING_KEY_DELIVERY_AT_HOME_TAX).value);
 		pickupMoms = Integer
 				.parseInt(DatabaseReader
 						.getSettingByKey(DatabaseConstants.SETTING_KEY_PICKUP_AT_RESTAURANT_TAX).value);
 		freeDeliveryThreshold = Integer
 				.parseInt(DatabaseReader
 						.getSettingByKey(DatabaseConstants.SETTING_KEY_FREE_DELIVERY_LIMIT).value);
 		deliveryCost = Integer
 				.parseInt(DatabaseReader
 						.getSettingByKey(DatabaseConstants.SETTING_KEY_DELIVERY_PRICE).value);
 		restaurantName = DatabaseReader
 				.getSettingByKey(DatabaseConstants.SETTING_KEY_RESTAURANT_NAME).value;
 	}
 
 	public static String getRestaurantName() {
 		if (restaurantName.equals("")) {
 			JOptionPane
					.showMessageDialog(
							null,
							"Restaurantnavn ikke hentet ut korrekt fra databasen - sjekk tilkoblingen til databasen og prv igjen!",
							"En alvorlig feil har oppsttt!",
							JOptionPane.ERROR_MESSAGE, null);
 		}
 		return restaurantName;
 	}
 
 }
