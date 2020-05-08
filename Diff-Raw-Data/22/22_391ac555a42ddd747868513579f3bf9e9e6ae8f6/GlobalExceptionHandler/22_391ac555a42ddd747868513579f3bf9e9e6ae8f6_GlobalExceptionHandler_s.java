 package com.gwtao.ui.util.client;
 
 public class GlobalExceptionHandler {
 
   private static final GlobalExceptionHandler INSTANCE = new GlobalExceptionHandler();
 
   public static GlobalExceptionHandler get() {
     return INSTANCE;
   }
 
   public void onUncaughtException(Throwable caught) {
    // TODO Auto-generated method stub
    
   }
 }
