 package grisu.control;
 
 import grisu.backend.model.User;
 import grisu.control.exceptions.NoValidCredentialException;
 import grisu.control.serviceInterfaces.AbstractServiceInterface;
 import grisu.settings.ServerPropertiesManager;
 import grith.jgrith.cred.AbstractCred;
 import grith.jgrith.cred.MyProxyCred;
 import grith.jgrith.credential.MyProxyCredential;
 import grith.jgrith.utils.CertHelpers;
 
 import java.util.Date;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.globus.myproxy.CredentialInfo;
 import org.globus.myproxy.MyProxy;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.UserDetails;
 
 import com.google.common.collect.Sets;
 
 
 public class GrisuUserDetails implements UserDetails {
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(GrisuUserDetails.class.getName());
 
 
 	private final String username;
 	private UsernamePasswordAuthenticationToken authentication;
 	private final boolean success = true;
 	private AbstractCred proxy = null;
 
 	private Date lastProxyRetrieve = null;
 
 	private User user = null;
 
 	private final String myproxyHost = ServerPropertiesManager.getMyProxyHost();
 	private final int myproxyPort = ServerPropertiesManager.getMyProxyPort();
 
 	public GrisuUserDetails(String username) {
 		myLogger.debug("Creating GrisuUserDetails object for " + username);
 		this.username = username;
 	}
 
 	private synchronized AbstractCred createProxyCredential(String username,
 			String password, String myProxyServer, int port, int lifetime) {
 
 		// System.out.println("Username: "+username);
 		// System.out.println("Password: "+password);
 
 		// final MyProxy myproxy = new MyProxy(myProxyServer, port);
 		try {
 			myLogger.debug("Getting delegated proxy from MyProxy...");
			AbstractCred cred = new MyProxyCred(username, password.toCharArray(), myProxyServer, port, lifetime, false);
 			cred.init();
 			// proxy = myproxy.get(username, password, lifetime);
 			final int remaining = cred.getRemainingLifetime();
 			myLogger.debug("Finished getting delegated proxy from MyProxy. DN: "
 					+ CertHelpers.getDnInProperFormat(cred.getGSSCredential())
 					+ " remaining liftime: " + remaining);
 
 			if (remaining <= 0) {
 				throw new RuntimeException("Proxy not valid anymore.");
 			}
 
 			return cred;
 			// return new Credential(proxy);
 		} catch (final Exception e) {
 			myLogger.error(
 					"Could not create myproxy credential: "
 							+ e.getLocalizedMessage(), e);
 			throw new NoValidCredentialException(e.getLocalizedMessage());
 		}
 
 	}
 
 
 	public synchronized AbstractCred fetchCredential()
 			throws AuthenticationException {
 
 		// myLogger.debug("Getting proxy credential...");
 
 		if (authentication == null) {
 			throw new AuthenticationException("No authentication token set.") {
 			};
 		}
 
 		if ((proxy != null) && proxy.isValid()) {
 
 			// myLogger.debug("Old valid proxy found.");
 			long oldLifetime = -1;
 			try {
 				oldLifetime = proxy.getGSSCredential().getRemainingLifetime();
 				if (oldLifetime >= ServerPropertiesManager
 						.getMinProxyLifetimeBeforeGettingNewProxy()) {
 
 					// myLogger.debug("Proxy still valid and long enough lifetime.");
 					// myLogger.debug("Old valid proxy still good enough. Using it.");
 					return proxy;
 				}
 
 				// only get the proxy every xx minutes if valid but not within
 				// remaining liftime threshold anymore
 				if (lastProxyRetrieve != null) {
 					long lastTime = lastProxyRetrieve.getTime();
 					long now = new Date().getTime();
 
 					long diff = ServerPropertiesManager
 							.getWaitTimeBetweenProxyRetrievals() * 1000;
 
 					if ((lastTime + diff) >= now) {
 						return proxy;
 					}
 				}
 
 			} catch (final Exception e) {
 				myLogger.error(e.getLocalizedMessage(), e);
 			}
 			// myLogger.debug("Old proxy not good enough. Creating new one...");
 		}
 
 		AbstractCred proxyTemp = null;
 
 		String username = authentication.getPrincipal().toString();
 		String password = authentication.getCredentials().toString();
 
 		String host = null;
 
 		int index = username.lastIndexOf('@');
 
 		if ((index > 0) && (index < username.length())) {
 			host = username.substring(index + 1);
 			username = username.substring(0, index);
 		}
 
 		int port = ServerPropertiesManager.getMyProxyPort();
 		if (StringUtils.isBlank(host)) {
 			host = ServerPropertiesManager.getMyProxyHost();
 		}
 
 		try {
 			proxyTemp = createProxyCredential(username, password, host, port,
 					ServerPropertiesManager.getMyProxyLifetime());
 			lastProxyRetrieve = new Date();
 		} catch (final NoValidCredentialException e) {
 			throw new AuthenticationException(e.getLocalizedMessage(), e) {
 			};
 		}
 
 		if ((proxyTemp == null) || !proxyTemp.isValid()) {
 
 			throw new AuthenticationException(
 					"Could not get valid myproxy credential.") {
 			};
 		} else {
 			// myLogger.info("Authentication successful.");
 			this.proxy = proxyTemp;
 			return this.proxy;
 		}
 
 	}
 
 	public Set<GrantedAuthority> getAuthorities() {
 
 		if (success) {
 			final Set<GrantedAuthority> result = Sets.newHashSet();
 			result.add(new GrantedAuthorityImpl("User"));
 			return result;
 		} else {
 			return null;
 		}
 
 	}
 
 	public synchronized long getCredentialEndTime() {
 
 		if (authentication == null) {
 			return -1;
 		}
 
 		final MyProxy myproxy = new MyProxy(
 				ServerPropertiesManager.getMyProxyHost(),
 				ServerPropertiesManager.getMyProxyPort());
 
 		CredentialInfo info = null;
 		try {
 			final String user = authentication.getPrincipal().toString();
 			final String password = authentication.getCredentials().toString();
 			info = myproxy.info(fetchCredential().getGSSCredential(), user,
 					password);
 		} catch (final Exception e) {
 			myLogger.error(e.getLocalizedMessage(), e);
 			return -1;
 		}
 
 		return info.getEndTime();
 
 	}
 
 	public String getPassword() {
 
 		return "dummy";
 	}
 
 	public synchronized User getUser(AbstractServiceInterface si) {
 
 		if (user == null) {
 			user = User.createUser(fetchCredential(), si);
 		}
 
 		user.setCredential(fetchCredential());
 		return user;
 
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public boolean isAccountNonExpired() {
 		return success;
 	}
 
 	public boolean isAccountNonLocked() {
 		return success;
 	}
 
 	public boolean isCredentialsNonExpired() {
 		return success;
 	}
 
 	public boolean isEnabled() {
 		return success;
 	}
 
 	public void setAuthentication(
 			UsernamePasswordAuthenticationToken authentication) {
 		if (this.authentication != null) {
 			Object cred = this.authentication.getCredentials();
 
 			if ((cred != null) && !cred.equals(authentication.getCredentials())) {
 				this.proxy = null;
 			}
 		}
 
 		this.authentication = authentication;
 		fetchCredential();
 	}
 
 
 
 }
