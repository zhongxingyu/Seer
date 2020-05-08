 /**
  * 
  */
 package com.snyder.review.shared.validator.shared;
 
 import com.snyder.review.shared.validator.BaseValidator;
 import com.snyder.review.shared.validator.ValidationError;
 import com.snyder.review.shared.validator.Validator;
 import com.snyder.state.State;
 import com.snyder.state.StateObserver;
 
 /**
  * 
 * @author SnyderGP
  */
 public class NonNullValidator extends BaseValidator
 {
 
     private final String name;
 	private NullError error;
 	
 	/**
 	 * @param name
 	 */
     public <T> NonNullValidator(String name, State<T> state)
     {
 	    this.name = name;
 	    state.addObserver(new StateObs<T>(), true);
     }
     
     /**
      * Maintains the validation state depending on whether the target state is null
      * 
      * @param <U>
      */
     private class StateObs<U> implements StateObserver<U>
     {
 
 		@Override
         public void stateChanged(State<? extends U> state, U oldValue, U newValue)
         {
 	        if(newValue == null && error == null)
 	        {
 	        	error = new NullError();
 	        	getErrors().add(error);
 	        }
 	        else if(newValue != null && error != null)
 	        {
 	        	getErrors().remove(error);
 	        	error = null;
 	        }
 	        	
         }
     	
     }
     
     private class NullError implements ValidationError
     {
 
 		@Override
         public Validator getValidator()
         {
 	        return NonNullValidator.this;
         }
 
 		@Override
         public String getMessage()
         {
 	        return name + " is required";
         }
     	
     }
 
 }
