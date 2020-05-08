 package com.redshape.utils.validators.impl.annotations;
 
 import com.redshape.utils.AnnotatedObject;
 import com.redshape.utils.validators.annotations.NotNull;
 import com.redshape.utils.validators.impl.annotations.result.ValidationResult;
 
 /**
  * @author nikelin
  * @date 18/04/11
  * @package com.redshape.validators.impl
  */
 public class NotNullAnnotationValidator extends AbstractAnnotationValidator<AnnotatedObject, ValidationResult> {
 
     public NotNullAnnotationValidator() {
         super(NotNull.class);
     }
 
     @Override
 	public boolean isValid(AnnotatedObject value) {
		return value.getContext() != null;
 	}
 
 	@Override
 	public ValidationResult validate(AnnotatedObject value) {
 		return new ValidationResult( this.isValid(value) );
 	}
 }
