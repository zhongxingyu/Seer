 package com.sirra.server.rest.annotations;
 
 import java.lang.annotation.*;
 
 /**
  * This enhances the @GET, @PUT and @DELETE annotations.
  * 
  * This annotation @BY_ID means the method expects a single pathParameter, which is the ID of the record.
  * 
  * More specifically:
  * 
  *  - The caller is passing in an ID as the first path parameter, i.e. /events/2343
  *  - That path parameter corresponds to the first parameter of the method.
  *  
  * @author aris
  */
 @Retention(RetentionPolicy.RUNTIME)
 public @interface BY_ID {
	
}
