 package com.kodelog.validation.innovativevalidators;
 
 import com.kodelog.validation.validators.Validator;
 
 /**
  * Some innovative validators can make your day
  * 
  * @author 73ddy
 *
 * @param <K>
 * @param <V>
 * @param <R>
  */
 public class GreaterThanValidator<K extends Comparable<K>, V extends Comparable<K>, R> implements
 		Validator<K, K, Boolean> {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.kodelog.validation.validators.Validator#validate(java.lang.Object,
 	 * java.lang.Object)
 	 */
 	@Override
 	public Boolean validate(K param, K value) {
 		if (param.compareTo(value) <= 0) {
 			throw new IllegalStateException((new StringBuilder(param.toString()).append(" is not greater than ")
 					.append(value.toString()).toString()));
 		}
		return null;
 	}
 }
