 package org.lastbamboo.common.sip.proxy.stateless;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Transformer;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.lastbamboo.common.sip.proxy.LocationService;
 import org.lastbamboo.common.sip.proxy.SipRegistrar;
 import org.lastbamboo.common.sip.proxy.SipRequestAndResponseForwarder;
 import org.lastbamboo.common.sip.proxy.SipRequestForwarder;
 import org.lastbamboo.common.sip.stack.message.Invite;
 import org.lastbamboo.common.sip.stack.message.SipMessage;
 import org.lastbamboo.common.sip.stack.message.SipMessageFactory;
 import org.lastbamboo.common.sip.stack.message.SipMessageUtils;
 import org.lastbamboo.common.sip.stack.message.SipResponse;
 import org.lastbamboo.common.sip.stack.transport.SipTcpTransportLayer;
 import org.lastbamboo.common.sip.stack.util.UriUtils;
 
 /**
  * This class is responsible for forwarding messages to URIs we do not have
  * registration data for.
  */
 public class UnregisteredUriForwarder implements SipRequestForwarder
     {
 
     private static final Log LOG = 
         LogFactory.getLog(UnregisteredUriForwarder.class);
     
     private final LocationService m_locationService;
     
     private final ExecutorService m_executor = Executors.newCachedThreadPool();
 
     private final SipTcpTransportLayer m_transportLayer;
 
     private final UriUtils m_uriUtils;
 
     private final SipMessageFactory m_messageFactory;
 
     private SipRequestAndResponseForwarder m_proxy;
 
     private final SipRegistrar m_registrar;
 
     //private ApplicationContext m_applicationContext;
 
     /**
      * Creates a new class for forwarding URIs we don't have registration data
      * for.
      * 
      * @param locationService The SIP location service for handling requests
      * this proxy has no registration data for.
      * @param transportLayer The transport layer for sending messages.
      * @param uriUtils Utilities for manipulating URIs.
      * @param messageFactory Factory for creating responses.
      * @param registrar Reference to the registrar for when we have to create
      * new connections, since the new connections need a reference to the
      * registrar.
      */
     public UnregisteredUriForwarder(final LocationService locationService,
         final SipTcpTransportLayer transportLayer,
         final UriUtils uriUtils,
         final SipMessageFactory messageFactory,
         final SipRegistrar registrar) 
         {
         this.m_locationService = locationService;
         this.m_transportLayer = transportLayer;
         this.m_uriUtils = uriUtils;
         this.m_messageFactory = messageFactory;
         this.m_registrar = registrar;
         }
     
     public void forwardSipRequest(final Invite request)
         {
         /*
         if (this.m_proxy == null)
             {
             this.m_proxy = 
                 (StatelessSipProxy) this.m_applicationContext.getBean(
                     "statelessSipProxy");
             }
         if (LOG.isDebugEnabled())
             {
             LOG.debug("Forwarding request to another proxy: "+request);
             }
         final URI uri;
         try
             {
             uri = SipMessageUtils.extractUriFromRequestLine(request);
             }
         catch (final IOException e)
             {
             // TODO Return a response indicating an invalid message from the
             // client.  For now, we just drop it.
             LOG.warn("Could not extract URI", e);
             return;
             }
         final Runnable runner = new Runnable()
             {
             public void run()
                 {
                 forwardSipRequest(uri, request);
                 }     
             };
         this.m_executor.execute(runner);
         */
         }
 
     private void forwardSipRequest(final URI uri, final Invite request) 
         {
         if (LOG.isDebugEnabled())
             {
             LOG.debug("Forwarding SIP request for URI: "+uri);
             }
         final Collection targetUris = this.m_locationService.getTargetSet(uri);
         if (targetUris.isEmpty())
             {
             // TODO: Return a response indicating we don't know how to forward
             // the request.
             LOG.debug("No targets for URI...");
             
             // Return a 408 Request Timeout response, as specified in RFC 3261
             // page 105.
             sendRequestTimeout(request);
             return;
             }
         
         if (LOG.isDebugEnabled())
             {
             LOG.debug("Forwarding to targetUris: "+targetUris);
             }
         
         final Transformer socketAddressTransformer = new Transformer()
             {
             public Object transform(final Object obj)
                 {
                 LOG.debug("Transforming: "+obj);
                 final URI targetUri = (URI) obj;
                 LOG.debug("Cast URI...");
                 final String host = m_uriUtils.getHostInSipUri(targetUri);
                 final int port = m_uriUtils.getPortInSipUri(targetUri);
                 final InetSocketAddress socketAddress = 
                     new InetSocketAddress(host, port);
                 return socketAddress;
                 }
             };
             
         final Collection targetSocketAddresses = 
             CollectionUtils.collect(targetUris, socketAddressTransformer);
         
         LOG.debug("Using socket addresses: " + targetSocketAddresses);
         if (this.m_transportLayer.hasConnectionForAny(targetSocketAddresses))
             {
             LOG.debug("Writing request using existing connection...");
             this.m_transportLayer.writeRequest(targetSocketAddresses, request);
             }
         else 
             {
             LOG.debug("Creating new connection to forward request...");
             connectToAnyTargetAndSendRequest(targetSocketAddresses, request);
             }
         }
 
     private void sendRequestTimeout(final Invite request)
         {
         final InetSocketAddress socketAddress;
         try
             {
             socketAddress = SipMessageUtils.extractNextHopFromVia(request);
             }
         catch (final IOException e)
             {
             LOG.warn("Could not extract Via", e);
             
             // Nothing we can do other than try to extract the Via.
             return;
             }
         final SipResponse requestTimeout = 
             this.m_messageFactory.createRequestTimeoutResponse(request);
         this.m_transportLayer.writeResponse(socketAddress, requestTimeout);
         }
 
     private boolean connectToAnyTargetAndSendRequest(
         final Collection targetUris, final Invite request)
         {
         LOG.debug("Attempting to connect to " + targetUris.size() + " URIs...");
         // Loop through and use the first URI we're able to connect to.
         for (final Iterator iter = targetUris.iterator(); iter.hasNext();)
             {
             final InetSocketAddress target = (InetSocketAddress) iter.next();
             try
                 {
                 if (connectToTargetAndSendRequest(target, request))
                     {
                     // We've successfully sent the message, so return.
                     return true;
                     }
                 }
             catch (final IOException e)
                 {
                 // Go on to the next one...
                 LOG.debug("Could not connect to URI: "+target, e);
                 }
             }
         
         // If we get here, none of the targets worked, so send a timeout.
         // This is specified in RFC 3261 page 105, section 16.6 section 7.
         sendRequestTimeout(request);
         return false;
         }
 
     private boolean connectToTargetAndSendRequest(
         final InetSocketAddress target, final SipMessage request) 
         throws IOException
         {
         if (LOG.isDebugEnabled())
             {
             LOG.debug("Connecting to external URI: "+target);
             }
         
         // TODO: We need to implement this with MINA.
         return true;
         
         /*
         final SipProxyConnectListener listener = 
             new SipProxyConnectListener();
         final ReadWriteConnector connector = 
             new ReadWriteConnectorImpl(this.m_selector, target, listener);
         connector.connect();
         synchronized (listener)
             {
             if (!listener.hasDeterminedStatus())
                 {
                 try
                     {
                     // Wait to connect to the external proxy.
                     listener.wait(10 * 1000);
                     }
                 catch (final InterruptedException e)
                     {
                     LOG.error("Should never happen", e);
                     }
                 }
             if (!listener.hasDeterminedStatus())
                 {
                 LOG.warn("Timeout connecting to external host: "+target);
                 return false;
                 }
             else
                 {
                 final ReaderWriter readerWriter = listener.getReaderWriter();
                 if (readerWriter == null)
                     {
                     LOG.warn("Could not get reader/writer...");
                     return false;
                     }
                 
                 final SipMessageVisitor visitor = 
                     new SipProxyMessageVisitor(readerWriter, this.m_registrar, 
                         this.m_proxy, this.m_messageFactory);
                 final ProtocolHandler protocolHandler = 
                     new SipProtocolHandler(this.m_messageFactory, visitor);
                 readerWriter.setProtocolHandler(protocolHandler);
                 if (LOG.isDebugEnabled())
                     {
                     LOG.debug("Adding connection to remote host...");
                     }
                 this.m_transportLayer.addConnection(readerWriter);
                 LOG.debug("Forwarding request to another proxy...");
                 this.m_transportLayer.writeRequestStatelessly(request, 
                     readerWriter);
                 return true;
                 }
             }
             */
         }
 
     /*
     public void setApplicationContext(final ApplicationContext ac) 
         throws BeansException
         {
         this.m_applicationContext = ac;
         }
         */
     }
