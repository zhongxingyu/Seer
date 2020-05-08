 package com.jsmadja.wall.projectwall.exception;
 
 public class SonarMetricNotFoundException extends Exception {
 
     private static final long serialVersionUID = -6812679837374431856L;
 
     public SonarMetricNotFoundException(String message, Throwable cause) {
         super(message, cause);
     }
 
    public SonarMetricNotFoundException(String message) {
        super(message);
    }

 }
