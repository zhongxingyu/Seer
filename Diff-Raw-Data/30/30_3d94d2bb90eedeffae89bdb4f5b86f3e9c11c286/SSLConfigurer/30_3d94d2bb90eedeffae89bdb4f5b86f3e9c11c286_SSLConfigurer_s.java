 package org.cagrid.core.common.security;
 
 import javax.net.ssl.KeyManager;
 import javax.net.ssl.TrustManager;
 
 import org.apache.cxf.configuration.Configurer;
 import org.apache.cxf.configuration.jsse.TLSClientParameters;
 import org.apache.cxf.configuration.jsse.TLSParameterJaxBUtils;
 import org.apache.cxf.configuration.security.CipherSuites;
 import org.apache.cxf.configuration.security.FiltersType;
 import org.apache.cxf.configuration.security.KeyManagersType;
 import org.apache.cxf.configuration.security.KeyStoreType;
 import org.apache.cxf.configuration.security.TrustManagersType;
 import org.apache.cxf.transport.http.HTTPConduit;
 import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
 
 public class SSLConfigurer implements Configurer {
 
 	private final Configurer baseConf;
 	private X509Credential credential;
 	private KeyStoreType truststore;
 	private KeyManagersType keystore;
 	private TrustManager[] tm;
 	private KeyManager[] km;
 
 	public SSLConfigurer(Configurer baseConf) {
 		this.baseConf = baseConf;
 	}
 
 	public X509Credential getCredential() {
 		return credential;
 	}
 
 	public void setCredential(X509Credential credential) {
 		this.credential = credential;
 		km = null;
 	}
 
 	public KeyStoreType getTruststore() {
 		return truststore;
 	}
 
 	public void setTruststore(KeyStoreType truststore) {
 		this.truststore = truststore;
 		tm = null;
 	}
 
 	public KeyManagersType getKeystore() {
 		return keystore;
 	}
 
 	public void setKeystore(KeyManagersType keystore) {
 		this.keystore = keystore;
 		km = null;
 	}
 
 	@Override
 	public void configureBean(Object beanInstance) {
 		configureBean(null, beanInstance);
 	}
 
 	@Override
 	public void configureBean(String name, Object beanInstance) {
 		if (beanInstance instanceof HTTPConduit) {
 			HTTPConduit http = (HTTPConduit) beanInstance;
 			TLSClientParameters tls = new TLSClientParameters();
 			if (getTruststore() != null) {
 				try {
 					tls.setTrustManagers(getTrustManager());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			if ((getCredential() != null) || (getKeystore() != null)) {
 				try {
 					tls.setKeyManagers(getKeyManager());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			tls.setDisableCNCheck(true);
 			tls.setCipherSuitesFilter(getCipherSuites());
 			http.setTlsClientParameters(tls);
 			HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
 			httpClientPolicy.setConnectionTimeout(36000);
 			httpClientPolicy.setAllowChunking(false);
 			httpClientPolicy.setReceiveTimeout(120000);
 			http.setClient(httpClientPolicy);
 
 		} else {
 			baseConf.configureBean(name, beanInstance);
 		}
 	}
 
 	private KeyManager[] getKeyManager() throws Exception {
 		if (km == null) {
 			if (getCredential() != null) {
 				km = new KeyManager[1];
 				km[0] = credential.getKeyManager();
 			} else if (getKeystore() != null) {
 				km = TLSParameterJaxBUtils.getKeyManagers(this.keystore);
 			}
 		}
 		return km;
 	}
 
 	private TrustManager[] getTrustManager() throws Exception {
 		if (tm == null) {
 			if (getTruststore() != null) {
 				TrustManagersType type = new TrustManagersType();
 				type.setKeyStore(getTruststore());
 				tm = TLSParameterJaxBUtils.getTrustManagers(type);
 			}
 		}
 		return tm;
 	}
 
 	private FiltersType getCipherSuites() {
 		FiltersType filters = new FiltersType();
 		filters.getInclude().add(".*_WITH_3DES_.*");
 		filters.getInclude().add(".*_WITH_DES_.*");
 		filters.getExclude().add(".*_WITH_NULL_.*");
 		filters.getExclude().add(".*_DH_anon_.*");
 		return filters;
 	}
 
 	public static void main(String[] args) {
 		CipherSuites cipherSuites = new CipherSuites();
 		System.out.println(cipherSuites.getCipherSuite());
 	}
 }
