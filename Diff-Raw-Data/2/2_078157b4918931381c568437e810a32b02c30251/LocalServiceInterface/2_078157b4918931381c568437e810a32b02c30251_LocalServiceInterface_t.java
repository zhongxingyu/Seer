 package org.vpac.grisu.control.serviceInterfaces;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Arrays;
 
 import org.globus.myproxy.CredentialInfo;
 import org.globus.myproxy.MyProxy;
 import org.globus.myproxy.MyProxyException;
 import org.ietf.jgss.GSSException;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.ServiceTemplateManagement;
 import org.vpac.grisu.control.exceptions.NoSuchTemplateException;
 import org.vpac.grisu.control.exceptions.NoValidCredentialException;
 import org.vpac.grisu.control.utils.LocalTemplatesHelper;
 import org.vpac.grisu.control.utils.MyProxyServerParams;
 import org.vpac.grisu.control.utils.ServerPropertiesManager;
 import org.vpac.grisu.credential.model.ProxyCredential;
 import org.vpac.security.light.control.CertificateFiles;
 import org.vpac.security.light.control.VomsesFiles;
 import org.vpac.security.light.myProxy.MyProxy_light;
 import org.vpac.security.light.plainProxy.LocalProxy;
 import org.w3c.dom.Document;
 
 public class LocalServiceInterface extends AbstractServiceInterface implements
 		ServiceInterface {
 
 	private ProxyCredential credential = null;
 	private String myproxy_username = null;
 	private char[] passphrase = null;
 
 	@Override
 	protected ProxyCredential getCredential() throws NoValidCredentialException {
 		
 		long oldLifetime = -1;
 		try {
 			if ( credential != null ) {
 				oldLifetime = credential.getGssCredential().getRemainingLifetime();
 			}
 		} catch (GSSException e2) {
 			myLogger.debug("Problem getting lifetime of old certificate: "+e2);
 			credential = null;
 		}
 		if ( oldLifetime < ServerPropertiesManager.getMinProxyLifetimeBeforeGettingNewProxy() ) {
 			myLogger.debug("Credential reached minimum lifetime. Getting new one from myproxy. Old lifetime: "+oldLifetime);
 			this.credential = null;
 //			user.cleanCache();
 		}
 
 		if (credential == null || ! credential.isValid() ) {
 
 			if (myproxy_username == null || myproxy_username.length() == 0) {
 				if (passphrase == null || passphrase.length == 0) {
 					// try local proxy
 					try {
 						credential = new ProxyCredential(LocalProxy
 								.loadGSSCredential());
 					} catch (Exception e) {
 						throw new NoValidCredentialException(
 								"Could not load credential/no valid login data.");
 					}
 					if (!credential.isValid()) {
 						throw new NoValidCredentialException(
 								"Local proxy is not valid anymore.");
 					}
 				} 
 			} else {
 				// get credential from myproxy
 				String myProxyServer = MyProxyServerParams.getMyProxyServer();
 				int myProxyPort = MyProxyServerParams.getMyProxyPort();
 				
 				try {
 					// this is needed because of a possible round-robin myproxy server
 					myProxyServer = InetAddress.getByName(myProxyServer).getHostAddress();
 				} catch (UnknownHostException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 					throw new NoValidCredentialException("Could not download myproxy credential: "+e1.getLocalizedMessage());
 				}
 
 				try {
 					credential = new ProxyCredential(MyProxy_light.getDelegation(myProxyServer, myProxyPort, myproxy_username, passphrase, 3600));
 					if ( getUser() != null ) {
 						getUser().cleanCache();
 					}
 				} catch (Throwable e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					throw new NoValidCredentialException("Could not get myproxy credential: "+e.getLocalizedMessage());
 				}
 				if (!credential.isValid()) {
 					throw new NoValidCredentialException(
 							"MyProxy credential is not valid.");
 				}
 			}
 		}
 		
 		
 		return credential;
 
 	}
 
 	public Document getTemplate(String application)
 			throws NoSuchTemplateException {
 		Document doc = ServiceTemplateManagement.getAvailableTemplate(application);
 		
 		if ( doc == null ) {
 			throw new NoSuchTemplateException("Could not find template for application: "+application+".");
 		}
 		
 		return doc;
 	}
 
 	public Document getTemplate(String application, String version)
 			throws NoSuchTemplateException {
 		Document doc = ServiceTemplateManagement.getAvailableTemplate(application);
 		
 		if ( doc == null ) {
 			throw new NoSuchTemplateException("Could not find template for application: "+application+", version "+version);
 		}
 		
 		return doc;
 		
 	}
 
 	public String[] listHostedApplicationTemplates() {
 		return ServiceTemplateManagement.getAllAvailableApplications();
 	}
 
 	public void login(String username, char[] password)
 			throws NoValidCredentialException {
 
 		try {
 			LocalTemplatesHelper.copyTemplatesAndMaybeGlobusFolder();
 			VomsesFiles.copyVomses();
 			CertificateFiles.copyCACerts();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
//			throw new RuntimeException("Could not initiate local backend: "+e.getLocalizedMessage());
 		}
 		
 		this.myproxy_username = username;
 		this.passphrase = password;
 		
 		try {
 			getCredential();
 		} catch (Exception e) {
 			throw new NoValidCredentialException("No valid credential: "+e.getLocalizedMessage());
 		}
 	}
 
 	public String logout() {
 		Arrays.fill(passphrase, 'x');
 		return null;
 	}
 
 	public long getCredentialEndTime() {
 		
 		String myProxyServer = MyProxyServerParams.getMyProxyServer();
 		int myProxyPort = MyProxyServerParams.getMyProxyPort();
 
 		try {
 			// this is needed because of a possible round-robin myproxy server
 			myProxyServer = InetAddress.getByName(myProxyServer).getHostAddress();
 		} catch (UnknownHostException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			throw new NoValidCredentialException("Could not download myproxy credential: "+e1.getLocalizedMessage());
 		}
 		
 		MyProxy myproxy = new MyProxy(myProxyServer, myProxyPort);
 		CredentialInfo info = null;
 		try {
 			info = myproxy.info(getCredential().getGssCredential(), myproxy_username, new String(passphrase));
 		} catch (MyProxyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return info.getEndTime();
 
 		
 	}
 
 }
