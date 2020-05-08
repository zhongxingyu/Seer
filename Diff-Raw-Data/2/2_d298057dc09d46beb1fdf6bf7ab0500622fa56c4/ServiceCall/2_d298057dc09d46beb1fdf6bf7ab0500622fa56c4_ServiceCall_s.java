 package sirius.web.services;
 
 import sirius.kernel.async.CallContext;
 import sirius.kernel.commons.Strings;
 import sirius.kernel.commons.Value;
 import sirius.kernel.health.Exceptions;
 import sirius.kernel.health.HandledException;
 import sirius.kernel.health.Log;
 import sirius.kernel.xml.StructuredOutput;
 import sirius.web.http.WebContext;
 
 import java.util.Arrays;
 
 /**
  * Created with IntelliJ IDEA.
  * User: aha
  * Date: 27.07.13
  * Time: 12:24
  * To change this template use File | Settings | File Templates.
  */
 public abstract class ServiceCall {
 
     protected static Log LOG = Log.get("services");
 
     protected WebContext ctx;
 
     public ServiceCall(WebContext ctx) {
         this.ctx = ctx;
     }
 
     public void handle(String errorCode, Throwable error) {
         HandledException he = Exceptions.handle(LOG, error);
         StructuredOutput out = createOutput();
         out.beginResult("error");
         out.property("success", false);
         out.property("message", he.getMessage());
         Throwable cause = error.getCause();
        while (cause != null && !cause.getCause().equals(cause)) {
             cause = cause.getCause();
         }
         if (cause == null) {
             cause = error;
         }
         out.property("type", cause.getClass().getName());
         if (Strings.isFilled(errorCode)) {
             out.property("code", errorCode);
         }
         out.property("flow", CallContext.getCurrent().getMDCValue(CallContext.MDC_FLOW));
         out.endResult();
     }
 
     public WebContext getContext() {
         return ctx;
     }
 
     public Value get(String... keys) {
         for (String key : keys) {
             Value result = ctx.get(key);
             if (result.isFilled()) {
                 return result;
             }
         }
         return Value.of(null);
     }
 
     public Value require(String... keys) {
         for (String key : keys) {
             Value result = ctx.get(key);
             if (result.isFilled()) {
                 return result;
             }
         }
         throw Exceptions.createHandled()
                         .withSystemErrorMessage(
                                 "A required parameter was not filled. Provide at least one value for: %s",
                                 Arrays.asList(keys))
                         .handle();
     }
 
     public void invoke(StructuredService serv) {
         try {
             serv.call(this, createOutput());
         } catch (Throwable t) {
             handle(null, t);
         }
     }
 
     protected abstract StructuredOutput createOutput();
 }
