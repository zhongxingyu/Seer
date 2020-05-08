 package fr.ultimate.breakfast.web.pages;
 
 import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.ExceptionReporter;
 
import fr.ultimate.breakfast.web.services.UltimateBreakfastApplicationScope;

 public class ApplicationError implements ExceptionReporter
 {
     @Property
     private String message;
 
    @Inject
    @Property
    private UltimateBreakfastApplicationScope aScope;

     @Override
     public void reportException(Throwable exception)
     {
         this.message = exception.getMessage();
     }
 
 }
