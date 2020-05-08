 package org.jboss.pressgang.ccms.rest.v1.client;
 
 import javax.ws.rs.ext.Provider;
 
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.resteasy.annotations.interception.ClientInterceptor;
 import org.jboss.resteasy.client.ClientResponse;
 import org.jboss.resteasy.spi.interception.ClientExecutionContext;
 import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
 
 @Provider
 @ClientInterceptor
 public class APIVersionDecorator implements ClientExecutionInterceptor {
     private final String version;
 
     public APIVersionDecorator(String version) {
         this.version = version;
     }
 
    public APIVersionDecorator() {
        this.version = "unknown";
    }

     @Override
     public ClientResponse execute(ClientExecutionContext clientExecutionContext) throws Exception {
         if (version != null) {
             clientExecutionContext.getRequest().getHeadersAsObjects().add(RESTv1Constants.X_VERSION_HEADER, version);
         }
         return clientExecutionContext.proceed();
     }
 }
