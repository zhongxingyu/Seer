 package edu.northwestern.bioinformatics.studycalendar.tools.spring;
 
 /**
  * Indicates that the bean would like to know the application context path when it is
  * deployed inside a web container that has one.
  * <p>
  * The context path is the part of the URL path which is the same for all requests to
 * the application.  The provided value will be "" if the application is mounted at
 * the root of the server's URI space.
  *
 * @see javax.servlet.http.HttpServletRequest#getContextPath
  * @author Rhett Sutphin
  */
 public interface WebContextPathAware {
     void setWebContextPath(String contextPath);
 }
