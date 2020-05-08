 package org.conan.fans.weibo.web;
 
import java.util.HashMap;
import java.util.Map;

 import org.conan.fans.exception.ErrorMessage;
 import org.conan.fans.exception.ValidateException;
import org.conan.fans.system.model.ConfigDTO;
import org.conan.fans.system.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.validation.BindException;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 
 abstract public class WebController {

    @Autowired
    ConfigService configService;

     @ExceptionHandler(BindException.class)
     public ResponseEntity<ErrorMessage> handleBindException(BindException error) {
         BindingResult result = error.getBindingResult();
         ErrorMessage em = new ErrorMessage();
         for (FieldError e : result.getFieldErrors()) {
             em.getErrors().add(em.new Error(e.getCode(), e.getField()));
         }
         return new ResponseEntity<ErrorMessage>(new ValidateException(em).getErrorMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
     }

    protected ConfigDTO config(String type) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("type", type);
        return configService.getConfigOne(paramMap);
    }
 }
