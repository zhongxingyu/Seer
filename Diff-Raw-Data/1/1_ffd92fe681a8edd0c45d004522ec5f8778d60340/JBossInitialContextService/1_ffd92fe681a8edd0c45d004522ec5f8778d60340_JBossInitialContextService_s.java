 package com.github.marschall.osgi.remoting.ejb.jboss;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.naming.Context;
 
 import org.jboss.ejb.client.ContextSelector;
 import org.jboss.ejb.client.EJBClientConfiguration;
 import org.jboss.ejb.client.EJBClientContext;
 import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
 import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 
 import com.github.marschall.osgi.remoting.ejb.api.InitialContextService;
 
 public class JBossInitialContextService implements InitialContextService {
   
   // mvn dependency:copy-dependencies -DoutputDirectory=lib
   // unzip -c jboss-transaction-api_1.1_spec-1.0.1.Final.jar META-INF/MANIFEST.MF
   private static final String[] PARENT_BUNDLE_IDS = {
     "org.jboss.spec.javax.transaction.jboss-transaction-api_1.1_spec",
     "org.jboss.spec.javax.ejb.jboss-ejb-api_3.1_spec",
     "org.jboss.logging.jboss-logging"
   };
   private static final Set<String> BUNDLE_IDS;
   
   static {
     Set<String> bundleIds = new HashSet<String>(PARENT_BUNDLE_IDS.length);
     for (String bundleId : PARENT_BUNDLE_IDS) {
       bundleIds.add(bundleId);
     }
     BUNDLE_IDS = Collections.unmodifiableSet(bundleIds);
   }
 
   public void activate(BundleContext context) throws IOException {
     Bundle bundle = context.getBundle();
     URL entry = bundle.getEntry("jboss-ejb-client.properties");
     // http://stackoverflow.com/questions/6244993/no-access-to-bundle-resource-file-osgi
     
     Properties invokerProperties = new Properties();
     InputStream stream = entry.openStream();
     try {
       // TODO check for null
       invokerProperties.load(stream);
     } finally {
       stream.close();
     }
     
     EJBClientConfiguration ejbcc = new PropertiesBasedEJBClientConfiguration(invokerProperties);
     ContextSelector<EJBClientContext> ejbCtxSel = new ConfigBasedEJBClientContextSelector(ejbcc);
     EJBClientContext.setSelector(ejbCtxSel);
   }
 
   @Override
   public Hashtable<?, ?> getEnvironment() {
     Properties jndiProps = new Properties();
     jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
     return jndiProps;
   }
 
   @Override
   public Set<String> getClientBundleSymbolicNames() {
     return BUNDLE_IDS;
   }
 
 
 }
