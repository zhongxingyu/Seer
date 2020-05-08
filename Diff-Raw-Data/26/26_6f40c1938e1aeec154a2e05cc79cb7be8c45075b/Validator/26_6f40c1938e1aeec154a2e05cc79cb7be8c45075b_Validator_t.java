 /**
  * 
  */
 package com.snyder.review.shared.validator;
 
 import com.snyder.state.list.HasListEvents;
 import com.snyder.state.nonnull.NonNullState;
 
 /**
  * 
 * @author SnyderGP
  */
 public interface Validator extends HasListEvents<ValidationError>
 {
 	
 	/**
 	 * @return a state that will be <code>true</code> when the target is valid
 	 */
 	NonNullState<Boolean> isValidState();
 	
 }
