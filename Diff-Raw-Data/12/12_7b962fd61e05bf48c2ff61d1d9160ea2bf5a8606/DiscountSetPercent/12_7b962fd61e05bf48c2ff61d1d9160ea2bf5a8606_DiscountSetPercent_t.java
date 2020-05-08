 package uk.ac.aber.dcs.cs12420.aberpizza.data;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 /**
  * Discount based on percent
  * @author Tim Stableford
  *
  */
 public class DiscountSetPercent extends DiscountSuper{
 	/**
 	 * @param i ArrayList of OrderItems to discount
 	 * @param d Percent discount 0 - 100
 	 * @param n Description of order
 	 */
 	public DiscountSetPercent(ArrayList<OrderItem> i, BigDecimal d, String n){
 		super(i,d,n);
 	}
 	public DiscountSetPercent(){}
 	/**
 	 * Calculates the discount for the order passed to it, if it applies
 	 */
 	@Override
 	public BigDecimal getDiscount(Order o) {
		BigDecimal sub = o.getSubtotal();;
 		BigDecimal disc = (sub.divide(new BigDecimal("100"))).multiply(discount);
 		disc = disc.setScale(2, BigDecimal.ROUND_HALF_EVEN);
 		if(super.discountApplies(o)){
 			return disc;
 		}else{
 			return new BigDecimal("0.00");
 		}
 	}
 	/**
 	 * @return the description followed by that it is a percent discount
 	 */
 	public String toString(){
 		return super.getDescription()+" - (Percent)";
 	}
 
 }
