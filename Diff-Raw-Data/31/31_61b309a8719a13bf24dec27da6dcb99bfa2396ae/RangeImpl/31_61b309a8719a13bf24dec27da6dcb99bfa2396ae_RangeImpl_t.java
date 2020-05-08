 package com.iq4j.utils.model;
 
 
 
 public abstract class RangeImpl<T extends Comparable<T>> implements Range<T> {
 
 	private static final long serialVersionUID = -5034074155264916277L;
 
 
 	/**
 	 * returns true in case of min and max values are setted
 	 */
 	@Override
 	public boolean isSetted() {
 		return getMin() != null && getMax() != null;
 	}
 
 	/***
 	 * returns true in case of min and max values are empty
 	 */
 	@Override
 	public boolean isEmpty() {
 		return getMin() == null && getMax() == null;
 	}
 
 	/**
 	 * Returns true in case of min and max values are equals. 
 	 * empty range is treated having equals values
 	 */ 
 	@Override
 	public boolean isMinEqualsMax() {
 		return  compareValues() == 0;
 	}
 
 	/***
 	 * Compares min and max values of range,
 	 * 
 	 * <p>this function return -1,0,1 respectively min value is greater, equal or lower versus max value</p>
 	 */
 	@Override
 	public int compareValues() {
 		return compare(getMin(), getMax());
 	}
 	
 	/***
 	 *  is mimic for value1.compareTo(value2) with checking parameters nullability;
 	 */
 	public int compare(T value1, T value2) {
 		if(value1 == null) {
 			return value2 == null ? 0 : -1;
 		}
 		else
 		{			
 			return value2 == null ? 1 : value1.compareTo(value2);
 		}
 	};
 	
 	/**
 	 * Validates range according to min and max values are setted
 	 * and min value not greater than max value; 
 	 */
 	@Override
 	public boolean isValid() {
 		return isSetted() && getMin().compareTo(getMax()) < 1;
 	}
 	
 	
 	/**
 	 * if range is valid 
 	 * Checks value is in between min and max value. 
 	 * value equality min or max value is treated in range and  returns true
 	 * in other case returns false;  
 	 */
 	@Override
 	public boolean contains(T value) {
 		return isValid() && compare(value, getMin()) > -1 && compare(value, getMax()) < 1;
 	};
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((getMin() == null) ? 0 : getMin().hashCode());
 		result = prime * result + ((getMax() == null) ? 0 : getMax().hashCode());
 		return result;
 		
 	}
 	
 	public boolean equals(Object o) {
 		
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof Period)) return false;
 		
 		Period other = (Period)o;
 		
 		if(getMin() == null  ) {
			if(other.getMin() != null) return false;
		} else if(!getMin().equals(other.getMin())) {			
 			return false;
 		}
 		
		if(getMax() == null  ) {
			if(other.getMax() != null) return false;
		} else if(!getMax().equals(other.getMax())) {
 			return false;
		}
 		
 		return true;
 		
 	}
 	
 }
