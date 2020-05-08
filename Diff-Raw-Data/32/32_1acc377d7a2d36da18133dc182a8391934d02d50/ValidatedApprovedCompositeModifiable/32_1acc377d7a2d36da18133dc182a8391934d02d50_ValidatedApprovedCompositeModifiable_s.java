 /**
  * 
  */
 package com.snyder.modifiable.validation;
 
 import com.snyder.modifiable.approved.ApprovedCompositeModifiable;
 import com.snyder.modifiable.approved.ModificationApprover;
 import com.snyder.review.shared.validator.BaseValidator;
 import com.snyder.review.shared.validator.Validated;
 import com.snyder.review.shared.validator.Validator;
 import com.snyder.review.shared.validator.state.ValidationAlgorithm;
 
 /**
  * 
  * @author greg
  */
 public abstract class ValidatedApprovedCompositeModifiable<T> extends ApprovedCompositeModifiable<T> 
 	implements Validated
 {
 	
 	protected final BaseValidator validator = new BaseValidator();
 
 	/**
 	 * @param initial
 	 * @param approver
 	 */
     public ValidatedApprovedCompositeModifiable(T initial, ModificationApprover approver)
     {
 	    super(initial, approver);
     }
 
 	@Override
     public Validator getValidator()
     {
 	    return validator;
     }
     
    protected final <U> ValidatedLeafModifiable<U> buildLeaf(U initial, 
         ValidationAlgorithm<U> validationAlgorithm)
     {
        ValidatedLeafModifiable<U> leaf = 
            new ValidatedLeafModifiable<U>(initial, validationAlgorithm);
         addChild(leaf);
         validator.addChildValidator(leaf.getValidator());
         return leaf;
     }
 
 }
