 package net.sf.jguard.core.authentication.filters;
 
 import net.sf.jguard.core.filters.Filter;
 import net.sf.jguard.core.filters.FilterChain;
 import net.sf.jguard.core.lifecycle.Request;
 import net.sf.jguard.core.lifecycle.Response;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.security.auth.Subject;
 import java.security.PrivilegedAction;
 
 /**
  * Base class for Authentication {@link Filter}.It provides
  * a security propagation via a Subject.
  *
  * @param <Req>
  * @param <Res>
  */
 public abstract class AuthenticationFilter<Req extends Request, Res extends Response> implements Filter<Req, Res> {
     private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class.getName());
 
     /**
      * propagate the call with security information into the Thread, i.e
      * under the hood in the JVM (and not as a ThreadLocal).
      *
      * @param subject  authenticated user
      * @param request
      * @param response
      * @param chain    filterChain to propagate to the next filter the call with a securized call
      */
     protected void propagateWithSecurity(final Subject subject, final Req request, final Res response, final FilterChain<Req, Res> chain) {
 
         //propagate security information into the Thread
         Subject.doAsPrivileged(subject, new PrivilegedAction() {
             public Object run() {
                 //we wrap the ServletRequest to 'correct' the j2ee's JAAS handling
                 // according to the j2se way
                 logger.info(" after successful authentication , before propagation");
                 chain.doFilter(request, response);
                // the 'null' tells to the SecurityManager to consider this resource access
                 //in an isolated context, ignoring the permissions of code currently
                 //on the execution stack.
                 //noinspection ReturnOfNull
                 return null;
             }
         }, null);
     }
 }
