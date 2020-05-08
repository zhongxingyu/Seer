 package amp.rabbit;
 
 
 import java.io.FileInputStream;
 import java.security.KeyStore;
 
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManagerFactory;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import amp.rabbit.topology.Exchange;
 
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.DefaultSaslConfig;
 
 
 public class CertificateChannelFactory extends BaseChannelFactory {
 
     protected Logger log;
 	protected String keystorePassword;
     protected String keystore;
    protected String truststore;
 
    public CertificateChannelFactory(String keystore, String keystorePassword, String truststore) {
 
         log = LoggerFactory.getLogger(this.getClass());
         this.keystore = keystore;
         this.keystorePassword = keystorePassword;
        this.truststore = truststore;
     }
 	
 	@Override
 	public void configureConnectionFactory(ConnectionFactory factory, Exchange exchange) throws Exception {
 
         log.debug("Getting connection for exchange: {}", exchange.toString());
 
 		char[] keyPassphrase = keystorePassword.toCharArray();
 
         KeyStore clientCertStore = KeyStore.getInstance("JKS");
         clientCertStore.load(new FileInputStream(keystore), keyPassphrase);
 
         KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
         kmf.init(clientCertStore, keyPassphrase);
 
         KeyStore remoteCertStore = KeyStore.getInstance("JKS");
        remoteCertStore.load(new FileInputStream(truststore), null);
 
         TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
         tmf.init(remoteCertStore);
 
         SSLContext c = SSLContext.getInstance("SSLv3");
         c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
 
     	super.configureConnectionFactory(factory, exchange);
         factory.setSaslConfig(DefaultSaslConfig.EXTERNAL);
         factory.useSslProtocol(c);
 	}
 
 }
