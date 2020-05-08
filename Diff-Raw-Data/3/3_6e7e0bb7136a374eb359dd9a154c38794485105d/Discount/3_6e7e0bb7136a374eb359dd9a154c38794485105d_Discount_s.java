 package uk.ac.aber.dcs.aberpizza.data;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
import apple.laf.JRSUIConstants.Size;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class Discount
  * It handles all the mathematics and logical functions to give the customer
  * the best discount possible (but that's not a good way to do business...)
  * @author Samuel B Sherar (sbs1)
  */
 public class Discount {
 	
 	/** The size discount stored as a hashmap of the Size as the key, and the amount needed before discount applies */
 	private Hashtable<Sizes, Integer> sizeDiscount;
 	
 	/** The amount discount stored as a hashmap of BigDecimal as the condition, and the percent stored as an Integer*/
 	private Hashtable<BigDecimal, Integer> amountDiscount;
 	
 	/**
 	 * Instantiates a new discount,
 	 */
 	public Discount() {
 		sizeDiscount = new Hashtable<Sizes, Integer>();
 		amountDiscount = new Hashtable<BigDecimal, Integer>();
 	}
 	
 	/**
 	 * Creates the size discount.
 	 *
 	 * @param size the size
 	 * @param quantity the quantity
 	 */
 	public void createDiscount(Sizes size, int quantity) {
 		sizeDiscount.put(size, quantity);
 		System.out.println(sizeDiscount.get(size));
 	}
 	
 	/**
 	 * Creates the percentage off discount.
 	 *
 	 * @param total the total
 	 * @param percent the percent
 	 */
 	public void createDiscount(BigDecimal total, int percent) {
 		amountDiscount.put(total, percent);
 	}
 	
 	/**
 	 * Gets the discount which saves the customer the most money
 	 *
 	 * @param o the Order item
 	 * @return the discount
 	 */
 	public BigDecimal getDiscount(Order o) {
 		BigDecimal sizeDiscountPrice = calcSize(o);
 		BigDecimal percentDiscountPrice = calcPercent(o);
 		System.out.println(sizeDiscountPrice);
 		System.out.println(percentDiscountPrice);
 		return (sizeDiscountPrice.compareTo(percentDiscountPrice) > 0) ? sizeDiscountPrice : percentDiscountPrice;
 	}
 	
 	/**
 	 * Calculates the discount if it meets the conditions of the size discount.
 	 *
 	 * @param o the oorder
 	 * @return the discount
 	 */
 	private BigDecimal calcSize(Order o) {
 		BigDecimal total = new BigDecimal(0.00);
 		BigDecimal lowestPrice = new BigDecimal(0.00);
 		int smallQuantity = 0;
 		int medQuantity = 0;
 		int largeQuantity = 0;
 		ArrayList<OrderItem> orderItem = o.getItems();
 		for(OrderItem oi : orderItem) {
 			Item item = oi.getItem();
 			for(OrderItemOption oio : oi.getOptions()) {
 				BigDecimal itemPrice = item.getPrice();
 				Option option = oio.getOption();
 				if(option.getSize() == Sizes.SMALL) {
 					smallQuantity = oio.getQuantity();
 				} else if(option.getSize() == Sizes.MEDIUM) {
 					medQuantity = oio.getQuantity();
 				} else if(option.getSize() == Sizes.LARGE) {
 					largeQuantity = oio.getQuantity();
 				}
 
 				itemPrice = item.getPrice().add(option.getPrice());
 
 				if(sizeDiscount.get(Sizes.LARGE) != null && sizeDiscount.get(Sizes.LARGE) <= largeQuantity) {
 					if(itemPrice.compareTo(lowestPrice) > 0) {
 						lowestPrice = itemPrice;
 					}
 				} else if(sizeDiscount.get(Sizes.MEDIUM) != null && sizeDiscount.get(Sizes.MEDIUM) <= medQuantity) {
 					if(itemPrice.compareTo(lowestPrice) > 0) {
 						lowestPrice = itemPrice;
 					}
 				} else if(sizeDiscount.get(Sizes.SMALL) != null && sizeDiscount.get(Sizes.SMALL) <= smallQuantity) {
 					if(itemPrice.compareTo(lowestPrice) > 0) {
 						lowestPrice = itemPrice;
 					}
 				}
 			}
 			return lowestPrice;
 		}
 		return null;
 	}
 	
 	/**
 	 * Calculates the discount with the percentage discount,
 	 * that is if it meets the requirements.
 	 *
 	 * @param o the Order
 	 * @return the discount
 	 */
 	private BigDecimal calcPercent(Order o) {
 		BigDecimal discountAmount = new BigDecimal(0);
 		BigDecimal orderTotal = o.getTotal();
 		double percent = 0;
 		Set set = amountDiscount.entrySet();
 	    Iterator it = set.iterator();
 	    
 	    while(it.hasNext()) {
 	    	Map.Entry entry = (Map.Entry) it.next();
 	    	if(orderTotal.compareTo((BigDecimal) entry.getKey()) > 0) {
 	    		percent = (Integer) entry.getValue();
 	    	}
 	    }
 
 	    if(percent > 0) {
 	    	discountAmount = orderTotal.multiply(new BigDecimal(percent/100));
 	    }
 		
 		return discountAmount;
 	}
 }
