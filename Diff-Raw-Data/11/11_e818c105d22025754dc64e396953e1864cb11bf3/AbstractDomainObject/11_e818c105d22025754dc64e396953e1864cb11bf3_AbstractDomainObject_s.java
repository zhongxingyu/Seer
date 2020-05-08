 package org.mendix.runtime.domain;
 
 import java.net.BindException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.mendix.runtime.exception.BusinessRuleException;
 import org.mendix.runtime.validation.BusinessRuleValidationTemplate;
 import org.springframework.validation.Validator;
 
 /**
  * The first reason to introduce this class is to provide a generic {@link #validate} method for all domain objects.
  * 
  * @author Eric Jan Malotaux
  * 
  */
 public abstract class AbstractDomainObject {
     protected List<Validator> validators = new LinkedList<Validator>();
 
     protected final BusinessRuleValidationTemplate validationTemplate = new BusinessRuleValidationTemplate(this);
 
     /**
      * Add the given <code>validator</code> to the collection of validators that will be executed on this object when
      * the {@link #validate()} method is called. The validators will be invoked in the order they were added.
      * 
      * @param validator -
      *            the validator being added.
      */
     public void addValidator(Validator validator) {
        if (!validators.contains(validator)) {
             validators.add(validator);
        }
     }
 
     /**
      * Validate this object.
      * 
      * Validation is performed by calling every {@link Validator} added by the {@link #addValidator(Validator)} method,
      * in the order they were added.
      * 
      * @throws BusinessRuleException -
      *             thrown when one or more errors are found. The exception thrown has as its cause a
      *             {@link BindException} containing information on all errors found.
      */
     protected void validate() throws BusinessRuleException {
         validationTemplate.invokeValidators(validators);
     }
 
 }
