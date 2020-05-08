 package nfjs.theme;
 
 import com.opensymphony.module.sitemesh.mapper.AbstractDecoratorMapper;
 import com.opensymphony.module.sitemesh.Decorator;
 import com.opensymphony.module.sitemesh.Page;
 import com.opensymphony.module.sitemesh.Config;
 import com.opensymphony.module.sitemesh.DecoratorMapper;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.ServletException;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * User: ben
  * Date: Aug 21, 2008
  * Time: 4:28:01 PM
  */
 public class ThemeDecoratorMapper extends AbstractDecoratorMapper {
 
     private Log log = LogFactory.getLog(getClass());
 
     private ThemeConfigLoader configLoader = null;
 
     /** Create new ConfigLoader using '/WEB-INF/decorators.xml' file. */
     public void init(Config config, Properties properties, DecoratorMapper parent) throws InstantiationException {
         super.init(config, properties, parent);
         try {
             String fileName = properties.getProperty("config", "/WEB-INF/decorators.xml");
             configLoader = new ThemeConfigLoader(fileName, config);
         }
         catch (Exception e) {
             e.printStackTrace();
             throw new InstantiationException(e.toString());
         }
     }
 
     /** Retrieve {@link com.opensymphony.module.sitemesh.Decorator} based on 'pattern' tag. */
     public Decorator getDecorator(HttpServletRequest request, Page page) {
 
         String thisPath = parsePath(request);
         String name = getMappedName(thisPath);
         Decorator result = getNamedDecorator(request, name);
 
		if(log.isDebugEnabled()) {
			log.debug("GET DECORATOR: " + thisPath + " : NAME: " + name + " R: " + result );
		}

         return result == null ? super.getDecorator(request, page) : result;
     }
 
     public String parsePath(HttpServletRequest request) {
 
		// seeing a bug because servlet path does not match the request URI
		String thisPath = request.getRequestURI();

/*
         String thisPath = request.getServletPath();
 
         // getServletPath() returns null unless the mapping corresponds to a servlet
         if (thisPath == null) {
             String requestURI = request.getRequestURI();
 
             if (request.getPathInfo() != null) {
                 // strip the pathInfo from the requestURI
                 thisPath = requestURI.substring(0, requestURI.indexOf(request.getPathInfo()));
             } else {
                 thisPath = requestURI;
             }
         }
         else if ("".equals(thisPath)) {
             // in servlet 2.4, if a request is mapped to '/*', getServletPath returns null (SIM-130)
             thisPath = request.getPathInfo();
         }
*/
         return thisPath;
     }
     
     public String getMappedName(String thisPath) {
         String name = null;
         try {
             name = configLoader.getMappedName(thisPath);
         }
         catch (ServletException e) {
             log.warn("SITEMESH MAPPING ERROR",e);
         }
         return name;
     }
 
 
     /** Retrieve Decorator named in 'name' attribute. Checks the role if specified. */
     public Decorator getNamedDecorator(HttpServletRequest request, String name) {
         Decorator result = null;
         try {
             result = configLoader.getDecoratorByName(name);
         }
         catch (ServletException e) {
             e.printStackTrace();
         }
 
         if (result == null ||
                 (result.getRole() != null && !request.isUserInRole(result.getRole()))) {
             // if the result is null or the user is not in the role
 
             log.warn("REQ: " + request + " SU: " + super.getClass() + " NAME: " + name );
 
             return super.getNamedDecorator(request, name);
         }
         else {
             return result;
         }
     }
 
     
 }
