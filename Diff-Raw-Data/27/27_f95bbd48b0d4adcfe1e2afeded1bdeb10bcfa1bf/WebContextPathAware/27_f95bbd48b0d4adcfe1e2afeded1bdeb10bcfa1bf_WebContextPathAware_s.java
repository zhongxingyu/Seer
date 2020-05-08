 package edu.northwestern.bioinformatics.studycalendar.tools.spring;
 
 /**
  * Indicates that the bean would like to know the application context path when it is
  * deployed inside a web container that has one.
  * <p>
  * The context path is the part of the URL path which is the same for all requests to
 * the application.
  *
  * @author Rhett Sutphin
  */
 public interface WebContextPathAware {
     void setWebContextPath(String contextPath);
 }
