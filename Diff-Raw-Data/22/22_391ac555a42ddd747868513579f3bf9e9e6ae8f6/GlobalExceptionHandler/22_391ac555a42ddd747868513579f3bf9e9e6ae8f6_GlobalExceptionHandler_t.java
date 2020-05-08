 package com.gwtao.ui.util.client;
 
import org.apache.commons.lang.StringUtils;

import com.gwtao.ui.dialog.client.AsyncOkAnswere;
import com.gwtao.ui.dialog.client.MessageDialog;

 public class GlobalExceptionHandler {
 
   private static final GlobalExceptionHandler INSTANCE = new GlobalExceptionHandler();
 
   public static GlobalExceptionHandler get() {
     return INSTANCE;
   }
 
   public void onUncaughtException(Throwable caught) {
    String message = caught.getMessage();
    if (StringUtils.isBlank(message))
      message = caught.getClass().getName();
    MessageDialog.alert("Unhandled Exception", message, AsyncOkAnswere.OK);
   }
 }
