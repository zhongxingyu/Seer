 package br.com.objective.training.hugeinteger;
 
 import java.util.Arrays;
 
 import br.com.objective.training.foundation.ReadOnly;
 
 class HugeIntegerImpl implements HugeInteger, ReadOnly {
 
 	private static final HugeIntegerImpl ZERO = new HugeIntegerImpl(0, new int[] { 0 });
 
 	/** -1 (negative), 0 (neutral), 1 (positive) */
 	private int sign;
 	private int[] magnitude;
 
 	HugeIntegerImpl(final String value) {
 		if (value.length() == 0)
 			throw new NumberFormatException("Zero length HugeInteger");
 
 		this.setMagnitude(parse(stripSign(value)));
 		setNeutralIfNecessary();
 	}
 
 	private HugeIntegerImpl(int sign, int[] magnitude) {
 		this.sign = sign;
 		this.setMagnitude(magnitude);
 	}
 
 	private void setMagnitude(int[] magnitude) {
 		this.magnitude = stripLeadingZeros(magnitude);
 	}
 
 	private void setNeutralIfNecessary() {
 		if (isZero()) this.sign = 0;
 	}
 
 	private boolean isZero() {
 		return this.magnitude.length == 1 && this.magnitude[0] == 0;
 	}
 
 	private String stripSign(String value) {
 		int index = value.lastIndexOf("-");
 		if (index != -1)
 			if (index == 0) {
 				if (value.length() == 1)
 					throw new NumberFormatException("Zero length HugeInteger");
 				this.sign = -1; 
 				return value.substring(1);
 			} else {
 				throw new NumberFormatException("Illegal embedded minus sign");
 			}
 		
 		this.sign = 1;
 		return value;
 	}
 
 	private int[] parse(String value) {
 		int size = value.length();
 		int[] result = new int[size];
 
 		for (int i = 0; i < size; ++i)
 			result[i] = value.charAt(size - i - 1) - '0';
 
 		return result; 
 	}
 
 	private static int[] stripLeadingZeros(int[] value) {
         int maxIndex = value.length - 1;
         int index = maxIndex;
         while(index > 0 && value[index] == 0) --index;
 
         return (index == maxIndex) ? value : Arrays.copyOfRange(value, 0, index + 1);
     }
 
 	private boolean isNegative() {
 		return this.sign == -1;
 	}
 
 	@Override
 	public int compareTo(HugeInteger value) {
 		HugeIntegerImpl other = (HugeIntegerImpl) value;
 
 		if (this.sign != other.sign)
 			return this.sign > other.sign ? 1 : -1;
 
 		switch (this.sign) {
         	case 1:
         		return compareMagnitude(other.magnitude);
         	case -1:
         		return other.compareMagnitude(this.magnitude);
         	default:
         		return 0;
         }
 	}
 
 	private int compareMagnitude(int[] b) {
 		int[] a = magnitude;
 
 		if (a.length < b.length) return -1;
 		if (a.length > b.length) return 1;
 
 		for(int i = a.length - 1; i >= 0; --i)
 			if (a[i] != b[i])
 				return (a[i] - b[i] > 0) ? 1 : -1;
 
 		return 0;
 	}
 
 	@Override
 	public HugeInteger add(HugeInteger value) {
 		HugeIntegerImpl other = (HugeIntegerImpl) value;
 
 		if (other.sign == 0) return this;
 		if (this.sign == 0) return other;
 
 		if (this.sign == other.sign)
 			return new HugeIntegerImpl(this.sign, add(this.magnitude, other.magnitude));
 
 		int compare = compareMagnitude(other.magnitude);
 		if (compare == 0) return ZERO;
 
		if (compare > 0)
			return new HugeIntegerImpl(this.sign, subtract(this.magnitude, other.magnitude));
		else
			return new HugeIntegerImpl(other.sign, subtract(other.magnitude, this.magnitude));
 	}
 
 	private static int[] add(int[] a, int[] b) {
 		// Swaps 'a' and 'b', so 'a' always contains the bigger value
 		if (a.length < b.length) {
 			final int[] tmp = a;
 			a = b;
 			b = tmp;
 		}
 
 		int size = a.length + 1; // add 1 for the carry (example: 99 + 1 = 100)
 		int[] result = new int[size];
 
 		int carry = 0;
 		for (int i = 0; i < a.length; ++i) {
 			result[i] = a[i] + (i < b.length ? b[i] : 0) + carry;
 			carry = result[i] < 10 ? 0 : 1; // Comparing is faster than using division: (result[i] / 10)
 			result[i] = result[i] % 10;
 		}
 
 		if (carry != 0) {
 			result[size - 1] = carry;			
 		}
 
 		return result;
 	}
 
 	@Override
 	public HugeInteger subtract(HugeInteger value) {
 		HugeIntegerImpl other = (HugeIntegerImpl) value;
 
 		if (other.sign == 0) return this;
 		if (this.sign == 0) return other.negate();
 
 		if (this.sign != other.sign)
             return new HugeIntegerImpl(this.sign, add(this.magnitude, other.magnitude));
 
         int compare = compareMagnitude(other.magnitude);
         if (compare == 0) return ZERO;
 
        if (compare > 0)
        	return new HugeIntegerImpl(this.sign, subtract(this.magnitude, other.magnitude));
        else
        	return new HugeIntegerImpl(other.sign, subtract(other.magnitude, this.magnitude));
 	}
 
 	@Override
 	public HugeInteger negate() {		
 		return new HugeIntegerImpl(-this.sign, this.magnitude);
 	}
 
 	private int[] subtract(int[] a, int[] b) {
 		int biggerSize = a.length;
 		int smallerSize = b.length;
 
 		int[] result = new int[biggerSize];
 		for (int i = 0; i < smallerSize; ++i) {
 			if (a[i] < b[i]) borrow(a, i);
 			result[i] = a[i] - b[i];
 		}
 
 		int sizeDifference = biggerSize - smallerSize;
 		if (sizeDifference > 0)
 			System.arraycopy(a, smallerSize, result, smallerSize, sizeDifference);
 
 		return result;
 	}
 
 	private void borrow(int[] a, int initialPosition) {
 		int borrowPosition = initialPosition + 1;
 		if (a[borrowPosition] == 0)
 			borrow(a, borrowPosition);
 
 		a[initialPosition] += 10;
 		--a[borrowPosition];
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder result = new StringBuilder(magnitude.length);
 
 		for (int i = magnitude.length - 1; i >= 0; --i)
 			result.append(magnitude[i]);
 
 		if(isNegative()) result.insert(0, '-');
 
 		return result.toString();
 	}
 
 }
