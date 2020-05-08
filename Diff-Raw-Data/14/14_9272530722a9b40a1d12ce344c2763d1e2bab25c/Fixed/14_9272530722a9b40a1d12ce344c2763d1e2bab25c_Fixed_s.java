 package com.sc2mod.andromeda.vm.data;
 
 import java.util.Locale;
 
 /**
  * Emulates Galaxy's "fixed" data type.
  * @author XPilot
  */
 public class Fixed {
 	private static final int FRACTION_BITS = 12;
 	private static final int FRACTION_FACTOR = 1 << FRACTION_BITS;
 	
 	private int value;
 	
 	private Fixed(int value) {
 		this.value = value;
 	}
 
 	public int decimalDigits() {
 		int i = 0;
 		while(i < FRACTION_BITS) {
 			if((value & (1 << i)) != 0) break;
 			i++;
 		}
 		return FRACTION_BITS - i;
 	}
 	
 	@Override
 	public String toString() {
 		return String.format((Locale)null, "%." + Math.max(decimalDigits(), 1) + "f", (double)value / FRACTION_FACTOR);
 	}
 	
 	public int toInt() {
 		return (int)(value >> FRACTION_BITS);
 	}
 	
 	public static Fixed fromInt(int i) {
 		return new Fixed(i << FRACTION_BITS);
 	}
 	
 	public static Fixed fromDecimal(double d) {
 		int value = (int)(d * FRACTION_FACTOR);
 		return new Fixed(value);
 	}
 	
 	public double toDouble() {
 		return (double)value / FRACTION_FACTOR;
 	}
 
 	public static Fixed fromString(String s) {
 		return fromDecimal(Double.parseDouble(s));
 	}
 	
 	public Fixed negate() {
 		return new Fixed(-value);
 	}
 	
 	public static Fixed sum(Fixed f1, Fixed f2) {
 		return new Fixed(f1.value + f2.value);
 	}
 	
 	public static Fixed difference(Fixed f1, Fixed f2) {
 		return new Fixed(f1.value - f2.value);
 	}
 	
 	public static Fixed product(Fixed f1, Fixed f2) {
		long value = f1.value * f2.value;
		value = value >> FRACTION_BITS;
 		return new Fixed((int)value);
 	}
 	
 	public static Fixed quotient(Fixed f1, Fixed f2) {
 		long value = f1.value;
 		value = (value << FRACTION_BITS) / f2.value;
 		return new Fixed((int)value);
 	}
 	
 	public static Fixed modulus(Fixed f1, Fixed f2) {
 		int value = f1.value % f2.value;
 		return new Fixed(value);
 	}
 	
 	public static boolean equal(Fixed f1, Fixed f2) {
 		return f1.value == f2.value;
 	}
 	
 	public static boolean notEqual(Fixed f1, Fixed f2) {
 		return f1.value != f2.value;
 	}
 	
 	public static boolean lessThan(Fixed f1, Fixed f2) {
 		return f1.value < f2.value;
 	}
 	
 	public static boolean greaterThan(Fixed f1, Fixed f2) {
 		return f1.value > f2.value;
 	}
 	
 	public static boolean lessThanOrEqualTo(Fixed f1, Fixed f2) {
 		return f1.value <= f2.value;
 	}
 	
 	public static boolean greaterThanOrEqualTo(Fixed f1, Fixed f2) {
 		return f1.value >= f2.value;
 	}
 }
