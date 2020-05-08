 package com.teoware.refapp.service.interceptor;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.ejb.EJB;
 import javax.interceptor.AroundInvoke;
 import javax.interceptor.InvocationContext;
 import javax.validation.ConstraintViolation;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.teoware.refapp.service.ServiceException;
 import com.teoware.refapp.service.validation.Validate;
 import com.teoware.refapp.service.validation.ValidationException;
 import com.teoware.refapp.service.validation.group.ValidationGroup;
 import com.teoware.refapp.service.validation.util.ServiceFacade;
 import com.teoware.refapp.service.validation.util.ServiceFacadeHolder;
 import com.teoware.refapp.service.validation.util.ValidationUtils;
 
 /**
  * EJB 3 Interceptor to validate method parameters which is marked with {@link Validate}. Calls {@link ValidationUtils}
  * for validation processing.
  * 
  * @author thomas.johansen
  * 
  */
 public class ValidationInterceptor {
 
 	private final Logger LOG = LoggerFactory.getLogger(ValidationInterceptor.class);
 
 	@EJB
 	private ServiceFacade serviceFacade;
 
 	@AroundInvoke
 	public Object validate(InvocationContext context) throws Exception {
 		Method method = context.getMethod();
 
 		Validate annotation = method.getAnnotation(Validate.class);
 
 		if (annotation == null) {
 			return context.proceed();
 		}
 
 		if (annotation.value() == ValidationGroup.class) {
 			throw new ServiceException("Validate annotation on method should have group parameter");
 		}
 
 		final Class<? extends ValidationGroup> group = annotation.value();
 
 		Object[] params = context.getParameters();
 		Annotation[][] annotations = method.getParameterAnnotations();
 
 		List<? super Object> annotatedParams = findValidateAnnotatedParams(annotations, params);
 
 		if (!annotatedParams.isEmpty()) {
 			try {
 				validateParams(annotatedParams, group);
 			} catch (ValidationException e) {
				String message = printDebug(method.getName(), e.getConstraintViolations());
 				LOG.info(message);
 				throw e;
 			}
 		}
 
 		return context.proceed();
 	}
 
 	protected List<? super Object> findValidateAnnotatedParams(Annotation[][] annotations, Object[] params) {
 		List<? super Object> annotatedParams = new ArrayList<Object>();
 
 		for (int i = 0; i < annotations.length; i++) {
 			for (Annotation a : annotations[i]) {
 				if (a.annotationType() == Validate.class) {
 					annotatedParams.add(params[i]);
 					break;
 				}
 			}
 		}
 		return annotatedParams;
 	}
 
 	protected void validateParams(List<? super Object> annotatedParams, Class<? extends ValidationGroup> group)
 			throws ValidationException, ServiceException {
 		final Set<ConstraintViolation<?>> errors = new HashSet<ConstraintViolation<?>>();
 
 		ServiceFacadeHolder.setServiceFacade(serviceFacade);
 
 		for (Object param : annotatedParams) {
 			try {
 				ValidationUtils.validate(param, group);
 			} catch (ValidationException e) {
 				errors.addAll(e.getConstraintViolations());
 			} catch (RuntimeException e) {
 				e.printStackTrace();
 				throw e;
 			}
 		}
 
 		if (errors.isEmpty()) {
			throw new ServiceException();
 		}
 
 		throw new ValidationException(errors);
 	}
 
	private String printDebug(String methodName, final Set<? extends ConstraintViolation<?>> errors) {
		// for debug...
 		final StringBuilder buf = new StringBuilder("Validation failed for method: ");
 		buf.append(methodName);
 		buf.append('\n');
 
 		for (ConstraintViolation<?> item : errors) {
 			buf.append("path: ");
 			buf.append(item.getPropertyPath().toString());
 			buf.append('\n');
 
 			buf.append("msgId: ");
 			buf.append(item.getMessageTemplate());
 			buf.append('\n');
 
 			buf.append("message: ");
 			buf.append(item.getMessage());
 			buf.append('\n');
 		}
 
 		buf.append("Validation.");
 
 		System.out.println(buf);
		// for debug...
 		return buf.toString();
 	}
 }
