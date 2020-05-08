 /*
  * SafeOnline project.
  *
  * Copyright 2006-2009 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 package net.link.safeonline.sdk.ws;
 
 import static com.lyndir.lhunath.opal.system.util.ObjectUtils.*;
 import static net.link.safeonline.sdk.configuration.SDKConfigHolder.*;
 
 import com.google.common.base.Supplier;
 import java.security.cert.X509Certificate;
 import javax.security.auth.x500.X500Principal;
 import net.link.safeonline.sdk.ws.attrib.AttributeClient;
 import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
 import net.link.safeonline.sdk.ws.data.DataClient;
 import net.link.safeonline.sdk.ws.data.DataClientImpl;
 import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
 import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
 import net.link.safeonline.sdk.ws.notification.consumer.NotificationConsumerClient;
 import net.link.safeonline.sdk.ws.notification.consumer.NotificationConsumerClientImpl;
 import net.link.safeonline.sdk.ws.notification.producer.NotificationProducerClient;
 import net.link.safeonline.sdk.ws.notification.producer.NotificationProducerClientImpl;
 import net.link.safeonline.sdk.ws.notification.subscription.NotificationSubscriptionManagerClient;
 import net.link.safeonline.sdk.ws.notification.subscription.NotificationSubscriptionManagerClientImpl;
 import net.link.safeonline.sdk.ws.session.SessionTrackingClient;
 import net.link.safeonline.sdk.ws.session.SessionTrackingClientImpl;
 import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;
 import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;
 import net.link.safeonline.sdk.ws.xkms2.Xkms2Client;
 import net.link.safeonline.sdk.ws.xkms2.Xkms2ClientImpl;
 import net.link.util.config.KeyProvider;
 import net.link.util.ws.security.WSSecurityConfiguration;
 import org.jetbrains.annotations.NotNull;
 
 
 /**
  * <h2>{@link LinkIDServiceFactory}</h2>
  * <p/>
  * <p> [description / usage]. </p>
  * <p/>
  * <p> <i>Jan 15, 2009</i> </p>
  *
  * @author lhunath
  */
 public class LinkIDServiceFactory extends ServiceFactory {
 
     public static final String SSL_ALIAS = "ws-ssl";
 
     private static ServiceFactory instance = new LinkIDServiceFactory();
 
     protected LinkIDServiceFactory() {
 
     }
 
     private static ServiceFactory getInstance() {
 
         return instance;
     }
 
     /**
      * Retrieve a proxy to the linkID attribute web service.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static AttributeClient getAttributeService() {
 
         return getInstance()._getAttributeService( new SDKWSSecurityConfiguration(), null );
     }
 
     /**
      * Retrieve a proxy to the linkID attribute web service.
      *
      * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
      * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
      *                       validation.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static AttributeClient getAttributeService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                       final X509Certificate sslCertificate) {
 
         return getInstance()._getAttributeService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
     }
 
     /**
      * Retrieve a proxy to the linkID attribute web service.
      *
      * @param configuration  Configuration of the WS-Security layer that secures the transport.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static AttributeClient getAttributeService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {
 
         return getInstance()._getAttributeService( configuration, sslCertificate );
     }
 
     @Override
     protected AttributeClient _getAttributeService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {
 
         return new AttributeClientImpl( config().web().wsBase(), getSSLCertificate( sslCertificate ), configuration );
     }
 
     /**
      * Retrieve a proxy to the linkID attribute web service.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static DataClient getDataService() {
 
         return getDataService( new SDKWSSecurityConfiguration(), null );
     }
 
     /**
      * Retrieve a proxy to the linkID data web service.
      *
      * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
      * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
      *                       validation.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID data web service.
      */
    public static DataClient getDataService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                            final X509Certificate sslCertificate) {
 
         return getInstance()._getDataService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
     }
 
     /**
      * Retrieve a proxy to the linkID data web service.
      *
      * @param configuration  Configuration of the WS-Security layer that secures the transport.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID data web service.
      */
     public static DataClient getDataService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {
 
         return getInstance()._getDataService( configuration, sslCertificate );
     }
 
     @Override
     protected DataClient _getDataService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {
 
         return new DataClientImpl( config().web().wsBase(), getSSLCertificate( sslCertificate ), configuration );
     }
 
     /**
      * Retrieve a proxy to the linkID attribute web service.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static NameIdentifierMappingClient getIdMappingService() {
 
         return getIdMappingService( new SDKWSSecurityConfiguration(), null );
     }
 
     /**
      * Retrieve a proxy to the linkID ID mapping web service.
      *
      * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
      * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
      *                       validation.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static NameIdentifierMappingClient getIdMappingService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                                   final X509Certificate sslCertificate) {
 
         return getInstance()._getIdMappingService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
     }
 
     /**
      * Retrieve a proxy to the linkID ID mapping web service.
      *
      * @param configuration  Configuration of the WS-Security layer that secures the transport.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static NameIdentifierMappingClient getIdMappingService(final WSSecurityConfiguration configuration,
                                                                   X509Certificate sslCertificate) {
 
         return getInstance()._getIdMappingService( configuration, sslCertificate );
     }
 
     @Override
     protected NameIdentifierMappingClient _getIdMappingService(final WSSecurityConfiguration configuration,
                                                                X509Certificate sslCertificate) {
 
         return new NameIdentifierMappingClientImpl( config().web().wsBase(), getSSLCertificate( sslCertificate ), configuration );
     }
 
     /**
      * Retrieve a proxy to the linkID attribute web service.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static SecurityTokenServiceClient getStsService() {
 
         return getStsService( new SDKWSSecurityConfiguration(), null );
     }
 
     /**
      * Retrieve a proxy to the linkID Security Token web service.
      *
      * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
      * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
      *                       validation.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static SecurityTokenServiceClient getStsService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                            final X509Certificate sslCertificate) {
 
         return getInstance()._getStsService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
     }
 
     /**
      * Retrieve a proxy to the linkID Security Token web service.
      *
      * @param configuration  Configuration of the WS-Security layer that secures the transport.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static SecurityTokenServiceClient getStsService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {
 
         return getInstance()._getStsService( configuration, sslCertificate );
     }
 
     @Override
     protected SecurityTokenServiceClient _getStsService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {
 
         return new SecurityTokenServiceClientImpl( config().web().wsBase(), getSSLCertificate( sslCertificate ), configuration );
     }
 
     /**
      * Retrieve a proxy to the linkID attribute web service.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static NotificationConsumerClient getNotificationConsumerService() {
 
         return getNotificationConsumerService( new SDKWSSecurityConfiguration(), null );
     }
 
     /**
      * Retrieve a proxy to the linkID notification consumer web service.
      *
      * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
      * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
      *                       validation.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static NotificationConsumerClient getNotificationConsumerService(final X500Principal trustedDN,
                                                                             @NotNull final KeyProvider keyProvider,
                                                                             final X509Certificate sslCertificate) {
 
         return getInstance()._getNotificationConsumerService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
     }
 
     /**
      * Retrieve a proxy to the linkID notification consumer web service.
      *
      * @param configuration  Configuration of the WS-Security layer that secures the transport.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID attribute web service.
      */
     public static NotificationConsumerClient getNotificationConsumerService(final WSSecurityConfiguration configuration,
                                                                             X509Certificate sslCertificate) {
 
         return getInstance()._getNotificationConsumerService( configuration, sslCertificate );
     }
 
     @Override
     protected NotificationConsumerClient _getNotificationConsumerService(final WSSecurityConfiguration configuration,
                                                                          X509Certificate sslCertificate) {
 
         return new NotificationConsumerClientImpl( config().web().wsBase(), getSSLCertificate( sslCertificate ), configuration );
     }
 
     /**
      * Retrieve a proxy to the linkID notification producer web service.
      *
      * @return proxy to the linkID notification producer web service.
      */
     public static NotificationProducerClient getNotificationProducerService() {
 
         return getNotificationProducerService( new SDKWSSecurityConfiguration(), null );
     }
 
     /**
      * Retrieve a proxy to the linkID notification producer web service.
      *
      * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
      * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
      *                       validation.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID notification producer web service.
      */
     public static NotificationProducerClient getNotificationProducerService(final X500Principal trustedDN,
                                                                             @NotNull final KeyProvider keyProvider,
                                                                             final X509Certificate sslCertificate) {
 
         return getInstance()._getNotificationProducerService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
     }
 
     /**
      * Retrieve a proxy to the linkID notification producer web service.
      *
      * @param configuration  Configuration of the WS-Security layer that secures the transport.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID notification producer web service.
      */
     public static NotificationProducerClient getNotificationProducerService(final WSSecurityConfiguration configuration,
                                                                             X509Certificate sslCertificate) {
 
         return getInstance()._getNotificationProducerService( configuration, sslCertificate );
     }
 
     @Override
     protected NotificationProducerClient _getNotificationProducerService(final WSSecurityConfiguration configuration,
                                                                          X509Certificate sslCertificate) {
 
         return new NotificationProducerClientImpl( config().web().wsBase(), getSSLCertificate( sslCertificate ), configuration );
     }
 
     /**
      * Retrieve a proxy to the linkID notification subscription web service.
      *
      * @return proxy to the linkID notification subscription web service.
      */
     public static NotificationSubscriptionManagerClient getNotificationSubscriptionService() {
 
         return getNotificationSubscriptionService( new SDKWSSecurityConfiguration(), null );
     }
 
     /**
      * Retrieve a proxy to the linkID notification subscription manager web service.
      *
      * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
      * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
      *                       validation.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID notification subscription web service.
      */
     public static NotificationSubscriptionManagerClient getNotificationSubscriptionService(final X500Principal trustedDN,
                                                                                            @NotNull final KeyProvider keyProvider,
                                                                                            final X509Certificate sslCertificate) {
 
         return getInstance()._getNotificationSubscriptionService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ),
                 sslCertificate );
     }
 
     /**
      * Retrieve a proxy to the linkID notification subscription manager web service.
      *
      * @param configuration  Configuration of the WS-Security layer that secures the transport.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID notification subscription web service.
      */
     public static NotificationSubscriptionManagerClient getNotificationSubscriptionService(final WSSecurityConfiguration configuration,
                                                                                            X509Certificate sslCertificate) {
 
         return getInstance()._getNotificationSubscriptionService( configuration, sslCertificate );
     }
 
     @Override
     protected NotificationSubscriptionManagerClient _getNotificationSubscriptionService(final WSSecurityConfiguration configuration,
                                                                                         X509Certificate sslCertificate) {
 
         return new NotificationSubscriptionManagerClientImpl( config().web().wsBase(), getSSLCertificate( sslCertificate ), configuration );
     }
 
     /**
      * Retrieve a proxy to the linkID session tracking web service.
      *
      * @return proxy to the linkID session tracking web service.
      */
     public static SessionTrackingClient getSessionTrackingService() {
 
         return getSessionTrackingService( new SDKWSSecurityConfiguration(), null );
     }
 
     /**
      * Retrieve a proxy to the linkID session tracking web service.
      *
      * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
      * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
      *                       validation.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID session tracking web service.
      */
     public static SessionTrackingClient getSessionTrackingService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                                   final X509Certificate sslCertificate) {
 
         return getInstance()._getSessionTrackingService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
     }
 
     /**
      * Retrieve a proxy to the linkID session tracking web service.
      *
      * @param configuration  Configuration of the WS-Security layer that secures the transport.
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID session tracking web service.
      */
     public static SessionTrackingClient getSessionTrackingService(final WSSecurityConfiguration configuration,
                                                                   X509Certificate sslCertificate) {
 
         return getInstance()._getSessionTrackingService( configuration, sslCertificate );
     }
 
     @Override
     protected SessionTrackingClient _getSessionTrackingService(final WSSecurityConfiguration configuration,
                                                                X509Certificate sslCertificate) {
 
         return new SessionTrackingClientImpl( config().web().wsBase(), getSSLCertificate( sslCertificate ), configuration );
     }
 
     /**
      * Retrieve a proxy to the linkID XKMS2 web service.
      *
      * @return proxy to the linkID XKMS2 web service.
      */
     public static Xkms2Client getXkms2Client() {
 
         return getXkms2Client( config().linkID().app().keyProvider() );
     }
 
     /**
      * Retrieve a proxy to the linkID XKMS 2 web service.
      *
      * @param keyProvider The key provider that provides the keys and certificates used by WS-Security for authentication and validation.
      *
      * @return proxy to the linkID XKMS2 web service.
      */
     public static Xkms2Client getXkms2Client(@NotNull final KeyProvider keyProvider) {
 
         return getInstance()._getXkms2Client( keyProvider.getTrustedCertificate( SSL_ALIAS ) );
     }
 
     /**
      * Retrieve a proxy to the linkID XKMS 2 web service.
      *
      * @param sslCertificate The server's SSL certificate.  If not <code>null</code>, validates whether SSL is encrypted using the given
      *                       certificate.
      *
      * @return proxy to the linkID XKMS2 web service.
      */
     public static Xkms2Client getXkms2Client(X509Certificate sslCertificate) {
 
         return getXkms2Client( sslCertificate );
     }
 
     @Override
     protected Xkms2Client _getXkms2Client(X509Certificate sslCertificate) {
 
         return new Xkms2ClientImpl( config().web().wsBase(), getSSLCertificate( sslCertificate ) );
     }
 
     private static X509Certificate getSSLCertificate(final X509Certificate sslCertificate) {
 
         return ifNotNullElseNullable( sslCertificate, new Supplier<X509Certificate>() {
             public X509Certificate get() {
 
                 return config().linkID().app().keyProvider().getTrustedCertificate( SSL_ALIAS );
             }
         } );
     }
 }
