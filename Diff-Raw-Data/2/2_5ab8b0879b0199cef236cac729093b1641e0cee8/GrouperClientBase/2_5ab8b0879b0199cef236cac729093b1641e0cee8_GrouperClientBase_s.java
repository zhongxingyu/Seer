 package org.cagrid.gridgrouper.soapclient;
 
 import org.apache.cxf.configuration.security.KeyStoreType;
 import org.cagrid.core.common.security.CredentialFactory;
 import org.cagrid.core.common.security.X509Credential;
 import org.cagrid.core.soapclient.SingleEntityKeyManager;
 import org.cagrid.gridgrouper.wsrf.stubs.GridGrouperPortType;
 
 import javax.net.ssl.KeyManager;
 
 public abstract class GrouperClientBase {
 
	public final static String LOCAL_URL = "https://localhost:7737/gridgrouper";
 
 	protected GridGrouperPortType gridGrouper;
 
 	GrouperClientBase(String url) throws Exception {
 		KeyStoreType truststore = new KeyStoreType();
 //		truststore.setUrl(getClass().getClassLoader().getResource("truststore.jks").toString());
         truststore.setFile("/Users/cmelean/Documents/Developer/source/cagrid/apache-servicemix-4.5.1/etc/gridgrouper/truststore.jks");
         truststore.setType("JKS");
         truststore.setPassword("inventrio");
 
         X509Credential credential = CredentialFactory.getCredential(
                 "/Users/cmelean/Documents/Developer/source/cagrid/apache-servicemix-4.5.1/etc/gridgrouper/host.jks",
                 "inventrio",
                 "tomcat",
                 "inventrio");
 
         KeyManager keyManager = new SingleEntityKeyManager("tomcat", credential);
 
         gridGrouper = GridGrouperSoapClientFactory.createSoapClient(url, truststore, keyManager);
 	}
 }
