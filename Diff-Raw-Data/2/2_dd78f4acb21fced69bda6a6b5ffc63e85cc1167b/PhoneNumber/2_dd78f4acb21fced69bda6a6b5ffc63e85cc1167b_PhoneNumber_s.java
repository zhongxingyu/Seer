 package org.swordess.toy.effectivejava.chapter3;
 
 
 public final class PhoneNumber implements Comparable<PhoneNumber> {
 
 	private final short areaCode;
 	private final short prefix;
 	private final short lineNumber;
 
 	public PhoneNumber(int areaCode, int prefix, int lineNumber) {
 		rangeCheck(areaCode, 999, "area code");
 		rangeCheck(prefix, 999, "prefix");
 		rangeCheck(lineNumber, 999, "line number");
 		
 		this.areaCode = (short) areaCode;
 		this.prefix = (short) prefix;
 		this.lineNumber = (short) lineNumber;
 	}
 
 	private void rangeCheck(int arg, int max, String name) {
 		if (arg < 0 || arg > max) {
 			throw new IllegalArgumentException(name + ": " + arg);
 		}
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if (o == this) {
 			return true;
 		}
 		if (!(o instanceof PhoneNumber)) {
 			return false;
 		}
 		PhoneNumber pn = (PhoneNumber) o;
 		return pn.lineNumber == lineNumber
 			&& pn.prefix == prefix
 			&& pn.areaCode == areaCode;
 	}
 	
 	@Override
 	public int hashCode() {
 		int result = 17;
 		result = 31 * result + areaCode;
 		result = 31 * result + prefix;
 		result = 31 * result + lineNumber;
 		return result;
 	}
 	
 	/**
 	 * Returns the string representation of this phone number.
 	 * The string consists of fourteen characters whose format
 	 * is "(XXX) YYY-ZZZZ", where the XXX is the area code, YYY
 	 * is the prefix, and ZZZZ is the line number. (Each of the
 	 * capital letters represents a single decimal digit.)
 	 * 
 	 * If any of the three parts of this phone number is too small
 	 * to fill up its field, the field is padded with leading zeros.
 	 * For example, if the value of the line number is 123, the last
 	 * four characters of the string representation will be "0123".
 	 * 
 	 * Note that there is a single space separating the closing
 	 * parenthesis after the area code from the first digit of the
 	 * prefix.
 	 */
 	@Override
 	public String toString() {
		return String.format("(%3d) %3d-%04d", areaCode);
 	}
 	
 	/*
 	public int compareTo(PhoneNumber pn) {
 		// Compare area codes
 		if (areaCode < pn.areaCode) {
 			return -1;
 		}
 		if (areaCode > pn.areaCode) {
 			return 1;
 		}
 		
 		// Area codes are equal, compare prefixes
 		if (prefix < pn.prefix) {
 			return -1;
 		}
 		if (prefix > pn.prefix) {
 			return 1;
 		}
 		
 		// Area codes and prefixes are equal, compare line numbers
 		if (lineNumber < pn.lineNumber) {
 			return -1;
 		}
 		if (lineNumber > pn.lineNumber) {
 			return 1;
 		}
 		
 		return 0;
 	}
 	*/
 	
 	public int compareTo(PhoneNumber pn) {
 		// Compare area codes
 		int areaCodeDiff = areaCode - pn.areaCode;
 		if (areaCodeDiff != 0) {
 			return areaCodeDiff;
 		}
 		
 		// Area codes are equal, compare prefixes
 		int prefixDiff = prefix - pn.prefix;
 		if (prefixDiff != 0) {
 			return prefixDiff;
 		}
 		
 		// Area codes and prefixes are equal, compare line numbers
 		return lineNumber - pn.lineNumber;
 	}
 	
 }
