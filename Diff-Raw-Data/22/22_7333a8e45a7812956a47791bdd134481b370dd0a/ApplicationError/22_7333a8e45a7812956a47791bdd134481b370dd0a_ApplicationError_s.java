 package fr.ultimate.breakfast.web.pages;
 
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.services.ExceptionReporter;
 
 public class ApplicationError implements ExceptionReporter
 {
     @Property
     private String message;
 
     @Override
     public void reportException(Throwable exception)
     {
         this.message = exception.getMessage();
     }
 
 }
