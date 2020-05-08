 package com.snyder.modifiable.validation;
 
 import com.snyder.modifiable.LeafModifiable;
 import com.snyder.review.shared.validator.Validated;
 import com.snyder.review.shared.validator.Validator;
 import com.snyder.review.shared.validator.state.StateValidator;
import com.snyder.review.shared.validator.state.ValidationAlgorithm;
 
 
 public class ValidatedLeafModifiable<T> extends LeafModifiable<T> implements Validated
 {
 
     private final Validator validator;
     
     public ValidatedLeafModifiable(T initial, ValidationAlgorithm<T> validationAlgorithm)
     {
         super(initial);
         validator = new StateValidator<T>(modifiedState, validationAlgorithm);
     }
 
     @Override
     public Validator getValidator()
     {
         return validator;
     }
     
 }
