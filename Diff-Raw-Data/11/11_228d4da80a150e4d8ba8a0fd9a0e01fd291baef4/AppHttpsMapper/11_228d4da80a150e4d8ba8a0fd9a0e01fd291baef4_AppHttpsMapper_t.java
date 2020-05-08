 package com.madalla.webapp;
 
 import org.apache.wicket.RuntimeConfigurationType;
 import org.apache.wicket.protocol.https.HttpsConfig;
 import org.apache.wicket.protocol.https.HttpsMapper;
 import org.apache.wicket.request.IRequestHandler;
 import org.apache.wicket.request.IRequestMapper;
 import org.apache.wicket.request.Request;
 import org.apache.wicket.request.Url;
 
 public abstract class AppHttpsMapper extends HttpsMapper {
 
     private final IRequestMapper delegate;
     private final RuntimeConfigurationType type;
 
     public AppHttpsMapper(final IRequestMapper delegate, final RuntimeConfigurationType type, final HttpsConfig config) {
         super(delegate, config);
         this.delegate = delegate;
         this.type = type;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public IRequestHandler mapRequest(final Request request) {
        if (type.equals(RuntimeConfigurationType.DEPLOYMENT) && isSiteSecure()) {
             return super.mapRequest(request);
         } else {
             return delegate.mapRequest(request);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Url mapHandler(IRequestHandler requestHandler) {
        if (type.equals(RuntimeConfigurationType.DEPLOYMENT) && isSiteSecure()) {
             return super.mapHandler(requestHandler);
         } else {
             return delegate.mapHandler(requestHandler);
         }
     }
     
     abstract boolean isSiteSecure();
 }
