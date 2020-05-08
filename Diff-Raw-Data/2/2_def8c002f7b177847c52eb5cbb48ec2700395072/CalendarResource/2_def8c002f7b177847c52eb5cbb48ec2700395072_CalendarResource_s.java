 package com.ettrema.http;
 
 import com.bradmcevoy.http.PropFindableResource;
import java.util.Date;
import java.util.List;
 
 /**
  *
  * @author alex
  */
 public interface CalendarResource extends CalendarCollection, PropFindableResource {
 
     String getCalendarDescription();
 
 
 }
