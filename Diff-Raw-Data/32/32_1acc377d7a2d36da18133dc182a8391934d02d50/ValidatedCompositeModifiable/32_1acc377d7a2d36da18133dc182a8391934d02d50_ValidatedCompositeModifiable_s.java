 package com.snyder.modifiable.validation;
 
import com.snyder.modifiable.approved.ApprovedCompositeModifiable;
import com.snyder.modifiable.approved.ModificationApprover;
 import com.snyder.review.shared.validator.BaseValidator;
 import com.snyder.review.shared.validator.Validated;
 import com.snyder.review.shared.validator.Validator;
 import com.snyder.review.shared.validator.state.ValidationAlgorithm;
 
 
public abstract class ValidatedCompositeModifiable<T> extends ApprovedCompositeModifiable<T> 
 	implements Validated
 {
     
     protected BaseValidator validator = new BaseValidator();
 
    public ValidatedCompositeModifiable(T initial, ModificationApprover approver)
     {
        super(initial, approver);
     }
 
     @Override
     public Validator getValidator()
     {
         return validator;
     }
     
    protected final <U> ValidatedApprovedLeafModifiable<U> buildLeaf(U initial, 
         ValidationAlgorithm<U> validationAlgorithm)
     {
        ValidatedApprovedLeafModifiable<U> leaf = 
            new ValidatedApprovedLeafModifiable<U>(initial, this, validationAlgorithm);
         addChild(leaf);
         validator.addChildValidator(leaf.getValidator());
         return leaf;
     }
     
 }
