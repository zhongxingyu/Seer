 package net.link.safeonline.sdk.auth.protocol;
 
 import com.lyndir.lhunath.opal.system.logging.Logger;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.http.HttpSession;
 
 
 /**
  * <h2>{@link ProtocolContext}<br> <sub>[in short] (TODO).</sub></h2>
  * <p/>
  * <p> <i>08 17, 2010</i> </p>
  *
  * @author lhunath
  */
 public class ProtocolContext implements Serializable {
 
     private static final Logger logger = Logger.get( ProtocolContext.class );
 
     public static final String SESSION_CONTEXTS = ProtocolContext.class.getName() + ".contexts";
 
     private final String id;
 
     /**
      * @param id A unique ID that will match the response to this request.
      */
     public ProtocolContext(String id) {
 
         this.id = id;
     }
 
     /**
      * @return A unique ID that will match the response to this request.
      */
     public String getId() {
 
        return id;
        //        return checkNotNull( id, "Id not set for %s", this );
     }
 
     public static void addContext(HttpSession session, ProtocolContext protocolContext) {
 
         getContexts( session ).put( protocolContext.getId(), protocolContext );
         logger.dbg( "Added protocol context to session: %s", protocolContext );
     }
 
     @SuppressWarnings("unchecked")
     public static Map<String, ProtocolContext> getContexts(HttpSession session) {
 
         Map<String, ProtocolContext> contexts = (Map<String, ProtocolContext>) session.getAttribute( SESSION_CONTEXTS );
         if (contexts == null)
             session.setAttribute( SESSION_CONTEXTS, contexts = new HashMap<String, ProtocolContext>() );
 
         return contexts;
     }
 
     @SuppressWarnings("unchecked")
     public static <P extends ProtocolContext> P findContext(HttpSession session, String id) {
 
         return (P) getContexts( session ).get( id );
     }
 
     @Override
     public String toString() {
 
         return String.format( "{%s: %s}", getClass().getSimpleName(), id );
     }
 }
