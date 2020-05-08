 package com.redshape.utils.validators.impl.annotations;
 
 import com.redshape.utils.AnnotatedObject;
 import com.redshape.utils.beans.bindings.BeanInfo;
 import com.redshape.utils.beans.bindings.BindingException;
 import com.redshape.utils.beans.bindings.IBeanInfo;
 import com.redshape.utils.beans.bindings.accessors.AccessException;
 import com.redshape.utils.beans.bindings.types.IBindable;
 import com.redshape.utils.validators.IValidator;
 import com.redshape.utils.validators.IValidatorsRegistry;
 import com.redshape.utils.validators.annotations.Bean;
 import com.redshape.utils.validators.impl.annotations.result.ValidationResult;
 import com.redshape.utils.validators.result.IResultsList;
 import com.redshape.utils.validators.result.IValidationResult;
 import com.redshape.utils.validators.result.ResultsList;
 
 import java.lang.annotation.Annotation;
 
 /**
  * @author nikelin
  * @date 18/04/11
  * @package com.redshape.validators
  */
 public class BeansValidator extends AbstractAnnotationValidator<AnnotatedObject, IResultsList> {
 
     public BeansValidator( IValidatorsRegistry registry) {
         super(Bean.class, registry);
     }
 
     @Override
 	public boolean isValid(AnnotatedObject value) {
 		return this.validate( value ).isValid();
 	}
 
 	@Override
 	public IResultsList validate(AnnotatedObject annotated) {
 		try {
             Object value = annotated.getContext();
             IBeanInfo beanInfo = new BeanInfo(value.getClass());
 			ResultsList result = new ResultsList();
 
 			for ( IBindable bindable : beanInfo.getBindables() ) {
 			   result.add( this.proccessMember( value, bindable) );
 			}
 
 			return result;
 		} catch ( BindingException e ) {
 			return new ResultsList(false);
 		}
 	}
 
 	protected IValidationResult proccessMember( Object value, IBindable bindable ) throws AccessException {
 		for (Annotation annotation : bindable.getAnnotations() ) {
 			if ( !this.isValid( value, bindable, annotation ) ) {
 				return new ValidationResult( bindable.getId(), annotation, false );
 			}
 		}
 
 		return new ValidationResult( bindable.getId() );
 	}
 
 	protected boolean isValid( Object value, IBindable bindable, Annotation annotation ) throws AccessException {
 		IValidator<AnnotatedObject,?> validator = this.getRegistry().getAnnotationValidator( annotation.annotationType() );
 		if ( validator == null ) {
 			return true;
 		}
 
		return validator.isValid( new AnnotatedObject( bindable.<Object>read(value), annotation ) );
 	}
 }
