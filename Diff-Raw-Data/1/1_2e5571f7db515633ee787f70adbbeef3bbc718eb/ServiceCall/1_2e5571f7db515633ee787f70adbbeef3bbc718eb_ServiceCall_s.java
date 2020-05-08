 /*
  * Made with all the love in the world
  * by scireum in Remshalden, Germany
  *
  * Copyright by scireum GmbH
  * http://www.scireum.de - info@scireum.de
  */
 
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
  * Provides access to the underlying request of a call to a {@link StructuredService}
  *
  * @author Andreas Haufler (aha@scireum.de)
  * @since 2013/11
  */
 public abstract class ServiceCall {
 
     protected static Log LOG = Log.get("services");
 
     protected WebContext ctx;
 
     protected ServiceCall(WebContext ctx) {
         this.ctx = ctx;
     }
 
     public void handle(String errorCode, Throwable error) {
         HandledException he = Exceptions.handle(LOG, error);
         StructuredOutput out = createOutput();
         out.beginResult();
         out.property("success", false);
         out.property("message", he.getMessage());
         Throwable cause = error.getCause();
         while (cause != null && cause.getCause() != null && !cause.getCause().equals(cause)) {
             cause = cause.getCause();
         }
         if (cause == null) {
             cause = error;
         }
         out.property("type", cause.getClass().getName());
         if (Strings.isFilled(errorCode)) {
             out.property("code", errorCode);
         } else {
             out.property("code", "ERROR");
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
