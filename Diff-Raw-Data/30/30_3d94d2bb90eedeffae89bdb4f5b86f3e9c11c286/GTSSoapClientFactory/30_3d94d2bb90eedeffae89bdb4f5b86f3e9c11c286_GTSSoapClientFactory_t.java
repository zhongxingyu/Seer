 package org.cagrid.gts.soapclient;
 
 import java.io.IOException;
 import java.security.GeneralSecurityException;
 
import javax.net.ssl.KeyManager;
 import javax.xml.ws.BindingProvider;
 
 import org.apache.cxf.Bus;
 import org.apache.cxf.configuration.Configurer;
 import org.apache.cxf.configuration.security.KeyManagersType;
 import org.apache.cxf.configuration.security.KeyStoreType;
 import org.apache.cxf.endpoint.Client;
 import org.apache.cxf.frontend.ClientProxy;
 import org.cagrid.core.common.security.SSLConfigurer;
 import org.cagrid.core.common.security.X509Credential;
 import org.cagrid.gts.wsrf.service.GTSService;
 import org.cagrid.gts.wsrf.stubs.GTSPortType;
 
 public class GTSSoapClientFactory {
 
 	public static GTSPortType createSoapClient(String url) {
 		GTSService gtsService = new GTSService();
 		GTSPortType gtsPort = gtsService.getGTSPortTypePort();
 
 		BindingProvider bp = (BindingProvider) gtsPort;
 		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
 				url);
 		return gtsPort;
 	}
 
 	public static GTSPortType createSoapClient(String url,
 			KeyStoreType truststore, KeyManagersType keyManager)
 			throws GeneralSecurityException, IOException {
 		GTSPortType gtsPort = createSoapClient(url);
 
 		Client gtsClient = ClientProxy.getClient(gtsPort);
 		Bus bus = gtsClient.getBus();
 		Configurer baseConf = bus.getExtension(Configurer.class);
 		
 		SSLConfigurer sslConf = new SSLConfigurer(baseConf);
 		sslConf.setTruststore(truststore);
 		sslConf.setKeystore(keyManager);
 		bus.setExtension(sslConf, Configurer.class);
 
 		return gtsPort;
 	}
 	
 	public static GTSPortType createSoapClient(String url,
 			KeyStoreType truststore, X509Credential cred)
 			throws GeneralSecurityException, IOException {
 		GTSPortType gtsPort = createSoapClient(url);
 
 		Client gtsClient = ClientProxy.getClient(gtsPort);
 		Bus bus = gtsClient.getBus();
 		Configurer baseConf = bus.getExtension(Configurer.class);
 		
 		SSLConfigurer sslConf = new SSLConfigurer(baseConf);
 		sslConf.setTruststore(truststore);
 		sslConf.setCredential(cred);
 		bus.setExtension(sslConf, Configurer.class);
 		return gtsPort;
 	}
	
	public static GTSPortType createSoapClient(String url,
			KeyStoreType truststore, KeyManager keyManager)
			throws GeneralSecurityException, IOException {
		GTSPortType dorianPort = createSoapClient(url);

		Client dorianClient = ClientProxy.getClient(dorianPort);
		Bus bus = dorianClient.getBus();
		Configurer baseConf = bus.getExtension(Configurer.class);
		
		SSLConfigurer sslConf = new SSLConfigurer(baseConf);
		sslConf.setTruststore(truststore);
		sslConf.setKm(new KeyManager[] { keyManager });
		bus.setExtension(sslConf, Configurer.class);

		return dorianPort;
	}
 
 }
