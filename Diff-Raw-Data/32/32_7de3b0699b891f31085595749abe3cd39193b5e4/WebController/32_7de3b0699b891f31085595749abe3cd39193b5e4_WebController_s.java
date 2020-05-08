 package org.conan.fans.weibo.web;
 
 import org.conan.fans.exception.ErrorMessage;
 import org.conan.fans.exception.ValidateException;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.validation.BindException;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 
 abstract public class WebController {
    
     @ExceptionHandler(BindException.class)
     public ResponseEntity<ErrorMessage> handleBindException(BindException error) {
         BindingResult result = error.getBindingResult();
         ErrorMessage em = new ErrorMessage();
         for (FieldError e : result.getFieldErrors()) {
             em.getErrors().add(em.new Error(e.getCode(), e.getField()));
         }
         return new ResponseEntity<ErrorMessage>(new ValidateException(em).getErrorMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
     }
    
 }
