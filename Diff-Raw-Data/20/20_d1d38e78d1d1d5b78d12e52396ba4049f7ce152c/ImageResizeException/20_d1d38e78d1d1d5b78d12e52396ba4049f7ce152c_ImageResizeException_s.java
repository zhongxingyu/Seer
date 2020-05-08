 package com.higomo.media.imageresizer;
 
 public class ImageResizeException extends Exception {
     public ImageResizeException() {
         super();
     }
 
     public ImageResizeException(String message) {
         super(message);
     }
 
     public ImageResizeException(String message, Throwable cause) {
         super(message, cause);
     }
 
     public ImageResizeException(Throwable cause) {
         super(cause);
     }

    protected ImageResizeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
 }
