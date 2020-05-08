 package org.iplantc.core.uicommons.client.errorHandling.models;
 
 import com.google.web.bindery.autobean.shared.AutoBean.PropertyName;
 import com.google.web.bindery.autobean.shared.AutoBeanFactory.Category;
 
 /**
  * An object representing the basic structure of error messages returned from service endpoints.
  * 
  * Ideally, this interface will be extended to create type-safe objects representing individual error
  * messages.
  * 
  * @author jstroot
  * 
  */
 public interface ServiceError {
 
     @PropertyName("action")
     String getServiceName();
 
     @PropertyName("error_code")
     String getErrorCode();
 
     @PropertyName("status")
     String getStatus();
 
     /**
      * A Non-property method which must be implemented with a {@link Category} method.
      * 
      * @return
      */
    String getErrorMsg();
 
     /**
      * XXX JDS This key is only used in one or two error codes and should not be relied upon.
      * TODO JDS Request consistent error message JSON response format from backend services.
      * @return
      */
     String getReason();
 }
