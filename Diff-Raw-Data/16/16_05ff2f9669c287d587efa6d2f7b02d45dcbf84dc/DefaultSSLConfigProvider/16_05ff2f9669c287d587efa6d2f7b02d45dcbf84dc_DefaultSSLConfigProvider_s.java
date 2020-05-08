 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.connection;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.lookup.ServiceProvider;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 
 import de.cismet.cids.server.ws.SSLConfig;
 import de.cismet.cids.server.ws.SSLConfigFactory;
 import de.cismet.cids.server.ws.SSLConfigFactoryException;
 import de.cismet.cids.server.ws.SSLConfigProvider;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 @ServiceProvider(service = SSLConfigProvider.class)
 public final class DefaultSSLConfigProvider implements SSLConfigProvider {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(DefaultSSLConfigProvider.class);
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public SSLConfig getSSLConfig() {
         if (LOG.isDebugEnabled()) {
             LOG.debug("retrieving default SSL config"); // NOI18N
         }
 
         final InputStream is = getClass().getResourceAsStream("cids-server-jetty.cert"); // NOI18N
 
         SSLConfig sslConfig;
         if (is == null) {
             LOG.warn("cannot load default server certificate");                       // NOI18N
             sslConfig = null;
         } else {
             try {
                sslConfig = SSLConfigFactory.getDefault().createClientConfig(new BufferedInputStream(is));
             } catch (final SSLConfigFactoryException ex) {
                 LOG.warn("cannot create config from default server certificate", ex); // NOI18N
                 sslConfig = null;
             }
         }
 
         return sslConfig;
     }
 }
