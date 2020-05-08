 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
  *
  * The software in this package is published under the terms of the MuleSource MPL
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.transport.jersey;
 
 import com.sun.jersey.api.core.DefaultResourceConfig;
 import com.sun.jersey.spi.container.ContainerRequest;
 import com.sun.jersey.spi.container.ContainerResponse;
 import com.sun.jersey.spi.container.InBoundHeaders;
 import com.sun.jersey.spi.container.WebApplication;
 import com.sun.jersey.spi.container.WebApplicationFactory;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashSet;
 import java.util.Set;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.mule.api.MuleEventContext;
 import org.mule.api.MuleMessage;
 import org.mule.api.component.JavaComponent;
 import org.mule.api.endpoint.EndpointURI;
 import org.mule.api.endpoint.InboundEndpoint;
 import org.mule.api.lifecycle.Callable;
 import org.mule.api.lifecycle.CreateException;
 import org.mule.api.service.Service;
 import org.mule.api.transformer.TransformerException;
 import org.mule.api.transport.Connector;
 import org.mule.transport.AbstractMessageReceiver;
 import org.mule.transport.ConnectException;
 import org.mule.transport.http.HttpConnector;
 
 /**
  * <code>JerseyMessageReceiver</code> TODO document
  */
 public class JerseyMessageReceiver extends AbstractMessageReceiver implements Callable {
 
    protected transient Log logger = LogFactory.getLog(getClass());
    
     private WebApplication application;
     
     public JerseyMessageReceiver(Connector connector, 
                                  Service service, 
                                  InboundEndpoint endpoint)
         throws CreateException {
         super(connector, service, endpoint);
     }
 
     public Object onCall(MuleEventContext event) throws Exception {
         MuleMessage message = event.getMessage();
         
         String path = (String) message.getProperty(HttpConnector.HTTP_REQUEST_PROPERTY);
         String query = null;
         int queryIdx = path.indexOf('?');
         if (queryIdx != -1) {
             query = path.substring(queryIdx+1);
             path = path.substring(0, queryIdx);
         }
         
         EndpointURI endpointUri = endpoint.getEndpointURI();
         String host = (String) message.getProperty("Host", endpointUri.getHost());
         String method = (String)message.getProperty(HttpConnector.HTTP_METHOD_PROPERTY);
         InBoundHeaders headers = new InBoundHeaders();
         
         
         URI baseUri = getBaseUri(endpointUri, host);
         URI completeUri = getCompleteUri(endpointUri, host, path, query);
         ContainerRequest req = new ContainerRequest(application,
                                                     method,
                                                     baseUri,
                                                     completeUri,
                                                     headers,
                                                     getInputStream(message));
        if (logger.isDebugEnabled())
        {
            logger.debug("Base URI: " + baseUri);
            logger.debug("Complete URI: " + completeUri);
        }
         
         MuleResponseWriter writer = new MuleResponseWriter();
         ContainerResponse res = new ContainerResponse(application, req, writer);
         
         application.handleRequest(req, res);
         
         return writer.getMessage();
     }
 
     protected static URI getCompleteUri(EndpointURI endpointUri, String host, String path, String query) throws URISyntaxException {
         String uri = endpointUri.getScheme() + "://" + host + path;
         if (query != null) {
             uri += "?" + query;
         }
             
         return new URI(uri);
     }
 
     protected static URI getBaseUri(EndpointURI endpointUri, String host) throws URISyntaxException {
         String path;
         if ("servlet".equals(endpointUri.getScheme())) {
             path = "/" + endpointUri.getHost() + "/";
         } else {
             path = endpointUri.getPath();
         }
         return new URI(endpointUri.getScheme() + "://" + host + path);
     }
     
     protected static InputStream getInputStream(MuleMessage message) throws TransformerException {
         return (InputStream) message.getPayload(InputStream.class);
     }
     
     public void doConnect() throws Exception {
         final Set<Class<?>> resources = new HashSet<Class<?>>();
         
         try {
             Class c = ((JavaComponent) service.getComponent()).getObjectType();
             resources.add(c);
         } catch (Exception e) {
             throw new ConnectException(e, this);
         }
         
         DefaultResourceConfig resourceConfig = new DefaultResourceConfig(resources);
         
         application = WebApplicationFactory.createWebApplication();
         application.initiate(resourceConfig);
         
         ((JerseyConnector) connector).registerReceiverWithMuleService(this, getEndpointURI());
     }
 
     public void doDisconnect() throws ConnectException {
         
     }
 
     public void doStart() {
        
     }
 
     public void doStop() {
         
     }
 
     public void doDispose() {
 
     }
 
 }
