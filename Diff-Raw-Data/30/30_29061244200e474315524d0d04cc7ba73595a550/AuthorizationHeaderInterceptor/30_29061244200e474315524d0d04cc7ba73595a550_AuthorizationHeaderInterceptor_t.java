 package fr.kissy.module.rest.interceptor;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.cxf.helpers.CastUtils;
 import org.apache.cxf.interceptor.Fault;
 import org.apache.cxf.interceptor.security.AccessDeniedException;
 import org.apache.cxf.message.Message;
 import org.apache.cxf.phase.AbstractPhaseInterceptor;
 import org.apache.cxf.phase.Phase;
 import org.springframework.beans.factory.annotation.Value;
 
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
  * @version $Id$
  */
 public class AuthorizationHeaderInterceptor extends AbstractPhaseInterceptor<Message> {
     @Value("${AUTHORIZATION_TOKEN}")
     private String token;
 
     /**
      * Default Constructor.
      */
     public AuthorizationHeaderInterceptor() {
         super(Phase.READ);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void handleMessage(Message message) throws Fault {
         if (((String) message.get(Message.PATH_INFO)).startsWith("/rest/api")) {
             return;
         }
 
         Map<String, List<String>> headers = CastUtils.cast((Map) message.get(Message.PROTOCOL_HEADERS));
        if (headers != null) {
            List<String> authorizations = headers.get("Authorization");
            if (authorizations != null && !authorizations.isEmpty()) {
                String authorization = authorizations.get(0);
                String token = authorization.replace("Token ", StringUtils.EMPTY);
                if (StringUtils.equals(this.token, token)) {
                    return;
                }
            }
         }

        Response response = Response.status(Response.Status.UNAUTHORIZED).build();
        message.getExchange().put(Response.class, response);
     }
 }
