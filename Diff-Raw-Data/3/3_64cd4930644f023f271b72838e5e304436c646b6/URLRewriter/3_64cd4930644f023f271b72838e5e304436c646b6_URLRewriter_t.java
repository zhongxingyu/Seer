 /*
  * Created on Aug 5, 2005
  */
 package uk.org.ponder.rsf.viewstate;
 
 import org.springframework.util.StringUtils;
 
 import uk.org.ponder.stringutil.URLUtil;
 import uk.org.ponder.util.Logger;
 
 /**
  * @author Antranig Basman (antranig@caret.cam.ac.uk) A URL rewriter for
  *         "resource" URLs (i.e. those which lead outside the RSF system).
  */
 public class URLRewriter {
   /**
    * URLs with this prefix will be rewritten to be relative to the "context
    * resource root" for this application. In a Webapp environment e.g., this
    * will be the directory one level above WEB-INF/
    */
   public static final String CONTEXT_PREFIX = "$context/";
 
   private ViewStateHandler viewstatehandler;
 
   public void setViewStateHandler(ViewStateHandler viewstatehandler) {
     this.viewstatehandler = viewstatehandler;
   }
 
   public static boolean isContextURL(String path) {
     return path.startsWith(CONTEXT_PREFIX);
   }
   
   public String rewriteContextURL(String path) {
     return viewstatehandler.encodeResourceURL(path
           .substring(CONTEXT_PREFIX.length()));
   }
   
   /**
    * relpath has leading slash removed.
    */
   public String rewriteResourceURL(String path, String resourcebase) {
     String resourceURL = null;
     if (!URLUtil.isAbsolute(path) && path.charAt(0) != '/') {
       if (isContextURL(path)) {
         resourceURL = rewriteContextURL(path);
       }
       else {
         resourceURL = viewstatehandler.encodeResourceURL(resourcebase + path);
       }
      resourceURL = StringUtils.cleanPath(resourceURL);
     }
     if (Logger.log.isDebugEnabled()) {
       Logger.log.debug("getResourceURL returning " + resourceURL + " for path "
           + path);
     }
     return resourceURL;
   }
 }
