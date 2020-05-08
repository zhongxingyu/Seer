 package com.us.fountainhead.gifnoc;
 
import com.us.fountainhead.gifnoc.server.entity.ApplicationDAO;
import com.us.fountainhead.gifnoc.server.entity.EnvironmentDAO;
import com.us.fountainhead.gifnoc.server.entity.EnvironmentPropertyDAO;
import com.us.fountainhead.gifnoc.server.entity.PropertyDAO;
 import com.us.fountainhead.gifnoc.server.validate.ValidationException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 /**
  * Tears down data after tests.
  */
 @Component
 public class TearDown {
 
     @Autowired
     private ApplicationDAO applicationDAO;
 
     @Autowired
     private EnvironmentPropertyDAO environmentPropertyDAO;
 
     @Autowired
     private EnvironmentDAO environmentDAO;
 
     @Autowired
     private PropertyDAO propertyDAO;
 
     /**
      * Deletes all the test data in the right order
      *
      * @throws ValidationException
      */
     public void execute() throws ValidationException {
        environmentPropertyDAO.deleteAll();
         environmentDAO.deleteAll();
         propertyDAO.deleteAll();
         applicationDAO.deleteAll();
     }
 
 }
