 /**
  * 
  */
 package com.snyder.modifiable.validation;
 
 import com.snyder.modifiable.approved.ApprovedLeafModifiable;
 import com.snyder.modifiable.approved.ModificationApprover;
 import com.snyder.review.shared.validator.Validated;
 import com.snyder.review.shared.validator.Validator;
import com.snyder.review.shared.validator.algorithm.ValidationAlgorithm;
 import com.snyder.review.shared.validator.state.StateValidator;
 
 /**
  * 
 * @author SnyderGP
  */
 public class ValidatedApprovedLeafModifiable<T> extends ApprovedLeafModifiable<T> implements 
 	Validated
 {
 	
 	private final Validator validator;
 	
 	/**
 	 * @param initial
 	 * @param approver
 	 */
     public ValidatedApprovedLeafModifiable(T initial, ModificationApprover approver, 
     	ValidationAlgorithm<T> validationAlgorithm)
     {
 	    super(initial, approver);
 	    validator = new StateValidator<T>(modifiedState, validationAlgorithm);
     }
 
 	@Override
     public Validator getValidator()
     {
 	    return validator;
     }
 
 }
