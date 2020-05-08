 package com.snyder.modifiable.validation;
 
 import com.snyder.modifiable.CompositeModifiable;
 import com.snyder.review.shared.validator.BaseValidator;
 import com.snyder.review.shared.validator.Validated;
 import com.snyder.review.shared.validator.Validator;
import com.snyder.review.shared.validator.algorithm.ValidationAlgorithm;
 
 
 public abstract class ValidatedCompositeModifiable<T> extends CompositeModifiable<T> 
 	implements Validated
 {
     
     protected BaseValidator validator = new BaseValidator();
 
     public ValidatedCompositeModifiable(T initial)
     {
         super(initial);
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
