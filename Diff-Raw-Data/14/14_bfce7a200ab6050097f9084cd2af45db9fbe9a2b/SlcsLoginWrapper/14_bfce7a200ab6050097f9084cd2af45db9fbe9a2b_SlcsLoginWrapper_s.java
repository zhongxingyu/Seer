 package grisu.frontend.control.login;
 
 import grisu.jcommons.configuration.CommonGridProperties;
 import grisu.settings.ClientPropertiesManager;
 import grith.gsindl.SLCS;
 import grith.jgrith.plainProxy.PlainProxy;
 import grith.sibboleth.CredentialManager;
 import grith.sibboleth.IdpObject;
 import grith.sibboleth.Shibboleth;
 import grith.sibboleth.StaticCredentialManager;
 import grith.sibboleth.StaticIdpObject;
 
 import org.apache.commons.lang.StringUtils;
 import org.ietf.jgss.GSSCredential;
 
 
 public class SlcsLoginWrapper {
 
 	public static GSSCredential slcsMyProxyInit(String username,
 			char[] password, String idp, LoginParams params) throws Exception {
 
 		try {
 
 			if (params != null) {
 				final String httproxy = params.getHttpProxy();
 				final int httpProxyPort = params.getHttpProxyPort();
 
 				if (StringUtils.isNotBlank(httproxy)) {
 					Shibboleth.setHttpProxy(httproxy, httpProxyPort,
 							params.getHttpProxyUsername(),
 							params.getMyProxyPassphrase());
 				}
 			}
 
 			final IdpObject idpO = new StaticIdpObject(idp);
 			final CredentialManager cm = new StaticCredentialManager(username,
 					password);
 
 			String url = ClientPropertiesManager.getShibbolethUrl();
 
 			final SLCS slcs = new SLCS(url, idpO, cm);
 
 			if ((slcs.getCertificate() == null)
 					|| (slcs.getPrivateKey() == null)) {
 				throw new Exception(
						"Could not get SLCS certificate and/or SLCS key: reason unknown");
 			}
 
 			final GSSCredential cred = PlainProxy.init(slcs.getCertificate(),
 					slcs.getPrivateKey(), 24 * 10);
 
 			CommonGridProperties.getDefault().setLastShibUsername(username);
 			CommonGridProperties.getDefault().setLastShibIdp(idp);
 			return cred;
 
 		} catch (final Exception e) {
 			throw e;
 		}
 
 	}
 
 }
