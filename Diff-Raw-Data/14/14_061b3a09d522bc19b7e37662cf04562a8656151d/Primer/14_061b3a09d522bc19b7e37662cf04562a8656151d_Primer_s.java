 package uk.co.epsilontechnologies.primer;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A canned HTTP Server instance that can be programmed to behave in a required fashion - to return the appropriate
  * response/s when given specific request/s.
  *
  * @author Shane Gibson
  */
 public class Primer {
 
     private final List<PrimedInvocation> primedInvocations = new ArrayList();
 
     private final Server server;
 
     public Primer(final String contextPath, final int port) {
         this.server = new Server(port);
         this.server.setHandler(new PrimedHandler(contextPath));
     }
 
     public void start() {
         try {
             this.server.start();
         } catch (final Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public void stop() {
         try {
             this.primedInvocations.clear();
             this.server.stop();
         } catch (final Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public void reset() {
         this.primedInvocations.clear();
     }
 
     public Action get(final String uri, final Parameters parameters, final Headers headers) {
         return new Action(this, new Request("GET", uri, "", parameters, headers));
     }
 
     public Action get(final String uri, final Headers headers) {
         return get(uri, new Parameters(), headers);
     }
 
     public Action get(final String uri, final Parameters parameters) {
         return get(uri, parameters, new Headers());
     }
 
     public Action get(final String uri) {
         return get(uri, new Parameters(), new Headers());
     }
 
     public Action head(final String uri, final Parameters parameters, final Headers headers) {
         return new Action(this, new Request("HEAD", uri, "", parameters, headers));
     }
 
     public Action head(final String uri, final Headers headers) {
         return head(uri, new Parameters(), headers);
     }
 
     public Action head(final String uri, final Parameters parameters) {
         return head(uri, parameters, new Headers());
     }
 
     public Action head(final String uri) {
         return head(uri, new Parameters(), new Headers());
     }
 
     public Action trace(final String uri, final Parameters parameters, final Headers headers) {
         return new Action(this, new Request("TRACE", uri, "", parameters, headers));
     }
 
     public Action trace(final String uri, final Headers headers) {
         return trace(uri, new Parameters(), headers);
     }
 
     public Action trace(final String uri, final Parameters parameters) {
         return trace(uri, parameters, new Headers());
     }
 
     public Action trace(final String uri) {
         return trace(uri, new Parameters(), new Headers());
     }
 
     public Action options(final String uri, final Parameters parameters, final Headers headers) {
         return new Action(this, new Request("OPTIONS", uri, "", parameters, headers));
     }
 
     public Action options(final String uri, final Headers headers) {
         return options(uri, new Parameters(), headers);
     }
 
     public Action options(final String uri, final Parameters parameters) {
         return options(uri, parameters, new Headers());
     }
 
     public Action options(final String uri) {
         return options(uri, new Parameters(), new Headers());
     }
 
     public Action delete(final String uri, final Parameters parameters, final Headers headers) {
         return new Action(this, new Request("DELETE", uri, "", parameters, headers));
     }
 
     public Action delete(final String uri, final Headers headers) {
         return delete(uri, new Parameters(), headers);
     }
 
     public Action delete(final String uri, final Parameters parameters) {
         return delete(uri, parameters, new Headers());
     }
 
     public Action delete(final String uri) {
         return delete(uri, new Parameters(), new Headers());
     }
 
     public Action put(final String uri, final String body, final Parameters parameters, final Headers headers) {
         return new Action(this, new Request("PUT", uri, body, parameters, headers));
     }
 
     public Action put(final String uri, final String body, final Parameters parameters) {
         return put(uri, body, parameters, new Headers());
     }
 
     public Action put(final String uri, final String body, final Headers headers) {
         return put(uri, body, new Parameters(), headers);
     }
 
     public Action put(final String uri, final String body) {
         return put(uri, body, new Parameters(), new Headers());
     }
 
     public Action post(final String uri, final String body, final Parameters parameters, final Headers headers) {
         return new Action(this, new Request("POST", uri, body, parameters, headers));
     }
 
     public Action post(final String uri, final String body, final Parameters parameters) {
         return post(uri, body, parameters, new Headers());
     }
 
     public Action post(final String uri, final String body, final Headers headers) {
         return post(uri, body, new Parameters(), headers);
     }
 
     public Action post(final String uri, final String body) {
         return post(uri, body, new Parameters(), new Headers());
     }
 
     void verify() {
         if (!this.primedInvocations.isEmpty()) {
            System.err.println("PRIMER --- Primed Requests Not Invoked. [PrimedInvocations:"+this.primedInvocations+"]");
             throw new IllegalStateException("Primed Requests Not Invoked");
         }
     }
 
     void prime(final PrimedInvocation primedInvocation) {
         this.primedInvocations.add(primedInvocation);
     }
 
     class PrimedHandler extends AbstractHandler {
 
         private final ResponseHandler responseHandler;
         private final RequestMatcher requestMatcher;
 
         public PrimedHandler(final String contextPath) {
             this(new ResponseHandler(), new RequestMatcher(contextPath));
         }
 
         public PrimedHandler(final ResponseHandler responseHandler, final RequestMatcher requestMatcher) {
             this.responseHandler = responseHandler;
             this.requestMatcher = requestMatcher;
         }
 
         @Override
         public void handle(
                 final String target,
                 final org.eclipse.jetty.server.Request baseRequest,
                 final HttpServletRequest httpServletRequest,
                 final HttpServletResponse httpServletResponse) throws IOException, ServletException {
 
             final HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpServletRequest);
 
            if (!checkPrimedInvocations(new ArrayList<PrimedInvocation>(primedInvocations), requestWrapper, httpServletResponse)) {
                System.err.println("PRIMER :-- Request Not Primed. [PrimedInvocations:"+primedInvocations+"]");
                 this.responseHandler.respond(new Response(404, "application/json", "Request Not Primed"), httpServletResponse);
             }
 
         }
 
         private boolean checkPrimedInvocations(
                 final List<PrimedInvocation> primedInvocationsToCheck,
                 final HttpServletRequestWrapper requestWrapper,
                 final HttpServletResponse httpServletResponse) {
 
             if (!primedInvocationsToCheck.isEmpty()) {
 
                 final PrimedInvocation primedInvocationToCheck = primedInvocationsToCheck.remove(0);
 
                 if (this.requestMatcher.match(primedInvocationToCheck.getRequest(), requestWrapper)) {
 
                     final Response response = primedInvocationToCheck.getResponses().remove(0);
 
                     if (primedInvocationToCheck.getResponses().isEmpty()) {
                         primedInvocations.remove(primedInvocationToCheck);
                     }
 
                     this.responseHandler.respond(response, httpServletResponse);
 
                     return true;
                 }
 
                 return checkPrimedInvocations(primedInvocationsToCheck, requestWrapper, httpServletResponse);
 
             }
 
             return false;
         }
 
     }
 
 }
