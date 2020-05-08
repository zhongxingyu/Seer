 package com.bulbview.recipeplanner.ui;
 
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 import javax.validation.groups.Default;
 
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.bulbview.recipeplanner.ui.presenter.EntityValidationException;
 
 @Configurable
 @Aspect
 public class EntityValidator {
 
     private final Logger logger;
 
     private Validator    validator;
 
     public EntityValidator() {
         this.logger = LoggerFactory.getLogger(getClass());
     }
 
     @Autowired
     public void setValidator(final Validator validator) {
         logger.info("++++++ validator: {}", validator);
         this.validator = validator;
     }
 
    @Before("execution(* com.bulbview.recipeplanner.ui.presenter.*.save*(..))")
     public void validate(final JoinPoint joinpoint) throws Throwable {
         final Object entity = joinpoint.getArgs()[0];
         logger.debug("validating {}......", entity);
         final Set<ConstraintViolation<Object>> violations = validator.validate(entity, Default.class);
         for ( final ConstraintViolation<Object> constraintViolation : violations ) {
             throw new EntityValidationException(constraintViolation.getMessage());
         }
     }
 }
