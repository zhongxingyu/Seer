 package qa.qcri.aidr.predictui.util;
 
 import com.sun.jersey.api.json.JSONConfiguration;
 import com.sun.jersey.api.json.JSONJAXBContext;
import qa.qcri.aidr.predictui.entities.Crisis;
 
import javax.ejb.Singleton;
import javax.ejb.Startup;
 import javax.ws.rs.ext.ContextResolver;
 import javax.xml.bind.JAXBContext;
import org.hibernate.annotations.Proxy;
 
 
 import javax.ws.rs.ext.Provider;
 
 @Provider
 public class JAXBContextResolver implements ContextResolver<JAXBContext> {
 
     private JAXBContext context;
 
     private final Class[] types = {
            Crisis.class,
            ResponseWrapper.class,
     };
 
     public JAXBContextResolver() throws Exception {
        this.context = new JSONJAXBContext(JSONConfiguration.mapped().arrays().build(), types);
     }
 
     @Override
     public JAXBContext getContext(final Class objectType) {
         for (Class type : types) {
             if (type == objectType) {
                 return context;
             }
         }
         return null;
     }
 
 }
