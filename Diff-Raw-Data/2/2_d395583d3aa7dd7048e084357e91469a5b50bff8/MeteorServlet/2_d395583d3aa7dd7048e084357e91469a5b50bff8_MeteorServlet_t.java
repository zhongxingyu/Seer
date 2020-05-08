 //CHECKSTYLE:OFF
 package org.atmosphere.cpr;
 
 import static org.atmosphere.cpr.ApplicationConfig.FILTER_CLASS;
 import static org.atmosphere.cpr.ApplicationConfig.FILTER_NAME;
 import static org.atmosphere.cpr.ApplicationConfig.MAPPING;
 import static org.atmosphere.cpr.ApplicationConfig.SERVLET_CLASS;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.atmosphere.handler.ReflectorServletProcessor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Simple Servlet to use when Atmosphere {@link Meteor} are used. This Servlet
  * will look for a Servlet init-param named org.atmosphere.servlet or
  * org.atmosphere.filter and will delegate request processing to them. When
  * used, this Servlet will ignore any value defined in META-INF/atmosphere.xml
  * as internally it will create a {@link org.atmosphere.handler.ReflectorServletProcessor}
  * @author Jean-Francois Arcand
  */
 public final class MeteorServlet extends AtmosphereServlet {
 
     protected static final Logger logger = LoggerFactory.getLogger(MeteorServlet.class);
 
     private Servlet servlet;
 
     private String filterClassName;
     
     public MeteorServlet() {
         this(false);
     }
 
     public MeteorServlet(boolean isFilter) {
         super(isFilter, false);
     }
 
     /**
      * Class constructor.
      * @param aServlet servlet to add meteor.
      */
     public MeteorServlet(final Servlet aServlet) {
         super(false, false);
         this.servlet = aServlet;
     }
 
     public void setFilterClassName(final String fcn) {
         this.filterClassName = fcn;
     }
 
     @Override
     public void init(final ServletConfig sc) throws ServletException {
         super.init(sc);
 
         ReflectorServletProcessor r = this.servlet==null?new ReflectorServletProcessor():new ReflectorServletProcessor(this.servlet);
         r.setFilterClassName(filterClassName);
         r.setFilterName(filterClassName);
        String mapping = "/[a-zA-Z0-9-_&.*=@~;\\?]+"; // "/*";
         BroadcasterFactory.getDefault().remove("/*");
         framework.addAtmosphereHandler(mapping, r).initAtmosphereHandler(sc);
     }
 
     @Override
     public void destroy() {
         super.destroy();
         Meteor.cache.clear();
     }
 }
