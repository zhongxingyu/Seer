 package util;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.Arrays;
 import java.util.Collection;
 
 import javax.persistence.Embeddable;
 
 import org.joda.money.Money;
 
 @Embeddable
 public class Percent implements Comparable<Percent> {
 	public static final Percent ONE = new Percent(BigDecimal.ONE);
 	public static final Percent ZERO = new Percent(BigDecimal.ZERO);
 	public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN; 
 
 	public static final Percent valueOf(double arg) {
 		return new Percent(BigDecimal.valueOf(arg));
 	}
 
 	public static final Percent normalizedValueOf(double arg) {
 		return new Percent(BigDecimal.valueOf(arg / 100.0));
 	}
 
 	public static final Percent normalizedValueOf(long arg) {
 		return new Percent(BigDecimal.valueOf(arg / 100L));
 	}
 
 	public static final Percent normalizedValueOf(BigDecimal arg) {
 		return new Percent(arg.divide(BigDecimal.valueOf(100L)));
 	}
 
 	public static final Percent sum(Percent... args) {
 		return sum(Arrays.asList(args));
 	}
 
 	public static final Percent sum(Collection<Percent> args) {
 		BigDecimal result = BigDecimal.ZERO;
 
 		for (Percent p : args) {
 			result = result.add(p.value);
 		}
 
 		return new Percent(result);
 	}
 	
 	public static final Percent subtract(Percent arg1, Percent arg2){
 		return new Percent(arg1.value.subtract(arg2.value));
 	}
 
 	private BigDecimal value;
 
 	public Percent() {
 		super();
 		this.value = BigDecimal.ZERO;
 	}
 
 	public Percent(BigDecimal value) {
 		super();
 //		if (value.abs().compareTo(BigDecimal.ONE) > 0) {
 //			throw new IllegalArgumentException("Illegal percent value (its absolute value is greater than 1)");
 //		}
 		this.value = value.setScale(4, RoundingMode.HALF_EVEN);
 	}
 
 	public BigDecimal getValue() {
 		return value;
 	}
 
 	public BigDecimal computeOn(BigDecimal amount) {
 		return amount.multiply(value);
 	}
 
 	public Money computeOn(Money amount) {
 		return amount.multipliedBy(value, RoundingMode.HALF_EVEN);
 	}
 
 	/*
 	 * public Percent add(Percent arg){ return new
 	 * Percent(this.value.add(arg.value)); }
 	 * 
 	 * public Percent addAll(Percent... args){ return
 	 * addAll(Arrays.asList(args)); }
 	 * 
 	 * public Percent addAll(Collection<Percent> args){ Percent result = new
 	 * Percent(value);
 	 * 
 	 * for(Percent p : args){ result.add(p); } System.out.println();
 	 * 
 	 * return result; }
 	 * 
 	 * 
 	 * public Percent subtract(Percent arg){ return new
 	 * Percent(this.value.subtract(arg.value)); }
 	 */
 
 	@Override
 	public int compareTo(Percent o) {
 		return this.value.compareTo(o.value);
 	}
 
 	public boolean greaterThan(Percent arg) {
 		return compareTo(arg) > 0;
 	}
 
 	public boolean lessThan(Percent arg) {
 		return compareTo(arg) < 0;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((value == null) ? 0 : value.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Percent other = (Percent) obj;
 		if (value == null) {
 			if (other.value != null)
 				return false;
 		} else if (value.compareTo(other.value) != 0){
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
		return value.multiply(BigDecimal.valueOf(100L)) + "%";
 	}
 
 }
