 package com.redshape.utils.validators.impl.annotations;
 
 import com.redshape.utils.AnnotatedObject;
import com.redshape.utils.validators.IValidatorsRegistry;
 import com.redshape.utils.validators.annotations.Length;
 import com.redshape.utils.validators.impl.annotations.result.ValidationResult;
 
 /**
  * @author nikelin
  * @date 18/04/11
  * @package com.redshape.validators.impl
  */
 public class LengthAnnotationValidator extends AbstractAnnotationValidator<AnnotatedObject, ValidationResult> {
 
     public LengthAnnotationValidator() {
         super(Length.class);
     }
 
     @Override
 	public boolean isValid(AnnotatedObject value) {
 		Length annotation = value.getAnnotation( Length.class );
 		if ( !( value.getContext() instanceof String ) ) {
 			return true;
 		}
 
 		return annotation.max() >= value.<String>getContext().length()
				&& annotation.min() >= value.<String>getContext().length();
 
 	}
 	
 	@Override
 	public ValidationResult validate(AnnotatedObject value) {
 		return new ValidationResult( this.isValid(value) );
 	}
 }
