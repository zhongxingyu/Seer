 package uk.org.ponder.springutil;
 
 import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.InputStreamResource;
 
 import java.io.InputStream;
 
 /*
  * @author Raymond Chan (raymond@caret.cam.ac.uk)
  * A straightforward holder for an InputStream, wrapped as a Spring ResourceLoader.
  * It will return the supplied stream for any request for a resource. This would
  * typically be used with some form of request-scope proxy.
  */ 
 public class DynamicStreamResourceLoader extends DefaultResourceLoader {
 
   private InputStream inputstr = null;
 
   /*
    * Set the InputStream from which the resource can be loaded.
    */
   public void setInputStream(InputStream inputstr) {
     this.inputstr = inputstr;
   }
 
   /*
    * For whatever resource is requested, return a resource dispensing the
    * supplied stream. 
    */
   public Resource getResource(String wedontcarewhatthisis) {
     return new InputStreamResource (inputstr);
   }
   
 }
 
