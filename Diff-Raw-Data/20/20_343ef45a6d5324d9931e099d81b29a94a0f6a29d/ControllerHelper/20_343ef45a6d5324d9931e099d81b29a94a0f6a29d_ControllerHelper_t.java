 package com.cqlybest.common.controller;
 
 import java.lang.reflect.Method;
 
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import com.cqlybest.common.mongo.bean.User;
 
 public abstract class ControllerHelper {
 
   protected ResponseEntity<Object> ok() {
     return new ResponseEntity<Object>(HttpStatus.OK);
   }
 
  protected ResponseEntity<Object> notFound() {
    return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
  }

   protected ResponseEntity<Object> error(String message) {
     return new ResponseEntity<Object>(message, HttpStatus.BAD_REQUEST);
   }
 
   protected ResponseEntity<Object> illegal() {
     return error("非法请求");
   }
 
   protected User getUser() {
     try {
       Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       Method getDetail = principal.getClass().getMethod("getDetail");
       return (User) getDetail.invoke(principal);
     } catch (Exception e) {
       e.printStackTrace();
       throw new RuntimeException(e);
     }
   }
 
 }
