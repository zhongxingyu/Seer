 package edu.gatech.oad.rocket.findmythings.server.security;
 
 import java.util.Collection;
 import java.util.logging.Logger;
 
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.AuthenticationInfo;
 import org.apache.shiro.authc.AuthenticationToken;
 import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
 import org.apache.shiro.realm.AuthenticatingRealm;
 import org.apache.shiro.subject.PrincipalCollection;
 import org.apache.shiro.subject.SimplePrincipalCollection;
 
 import com.google.common.base.Preconditions;
 
 import edu.gatech.oad.rocket.findmythings.server.db.DatabaseService;
 import edu.gatech.oad.rocket.findmythings.server.db.MemcacheManager;
 import edu.gatech.oad.rocket.findmythings.server.db.model.AppAuthenticationToken;
 
 public class BearerTokenAuthenticatingRealm extends AuthenticatingRealm {
 	private static final Logger LOGGER = Logger.getLogger(BearerTokenAuthenticatingRealm.class.getName());
 
 	private class BearerAuthenticationInfo implements AuthenticationInfo {
 		private static final long serialVersionUID = 3470761774414912759L;
 		private AppAuthenticationToken token;
 
 		BearerAuthenticationInfo(AppAuthenticationToken token) {
 			this.token = token;
 		}
 
 		@Override
 		public Object getCredentials() {
 			return token.getIdentifierString();
 		}
 
 		@Override
 		public PrincipalCollection getPrincipals() {
 			SimplePrincipalCollection ret = new SimplePrincipalCollection();
			ret.add(token.getEmail(), null);
 			ret.add(token.getIdentifierString(), getName());
 			return ret;
 		}
 
 	}
 
 	public BearerTokenAuthenticatingRealm() {
 		super(new MemcacheManager(), new AllowAllCredentialsMatcher());
 		setAuthenticationTokenClass(BearerToken.class);
 	}
 
 	private static final boolean tokenIsValid(BearerToken token, AppAuthenticationToken dbToken) {
 		return token != null && dbToken != null && dbToken.getEmail().equals((String)token.getPrincipal());
 	}
 
 	@Override
 	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken arg0) throws AuthenticationException {
 		BearerToken token = (BearerToken)arg0;
 		String email = (String)token.getPrincipal();
 		String creds = (String)token.getCredentials();
 
 		Preconditions.checkNotNull(email, "Email can't be null");
 		Preconditions.checkNotNull(token, "Token can't be null");
 
 		AppAuthenticationToken dbToken = DatabaseService.ofy().load().type(AppAuthenticationToken.class).id(creds).get();
 		if (!tokenIsValid(token, dbToken)) {
 			LOGGER.info("Rejecting token " + creds + " for user " + email);
 			return null;
 		}
 
 		return new BearerAuthenticationInfo(dbToken);
 	}
 
 	@Override
 	public void onLogout(PrincipalCollection principals) {
 		super.onLogout(principals);
 		deleteTokens(principals);
     }
 
 	public static final String createNewToken(String email) {
 		return DatabaseService.ofy().save().authenticationToken(email);
 	}
 
 	@SuppressWarnings("unchecked")
 	public final void deleteTokens(PrincipalCollection principals) {
 		Collection<String> tokens = principals.fromRealm(getName());
 		if (tokens != null) { //  && tokens.size() > 1
 			DatabaseService.ofy().delete().type(AppAuthenticationToken.class).ids(tokens);
 		}
 	}
 
 }
