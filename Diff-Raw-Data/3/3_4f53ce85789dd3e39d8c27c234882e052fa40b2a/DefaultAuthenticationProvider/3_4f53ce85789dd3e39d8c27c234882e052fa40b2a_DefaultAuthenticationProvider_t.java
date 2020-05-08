 package com.raccoonfink.cruisemonkey.security;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.security.authentication.InsufficientAuthenticationException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 
 import com.raccoonfink.cruisemonkey.dao.UserDao;
 import com.raccoonfink.cruisemonkey.model.User;
 import com.raccoonfink.cruisemonkey.server.StatusNetService;
 import com.raccoonfink.cruisemonkey.server.StatusNetServiceFactory;
 
 public class DefaultAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider implements InitializingBean {
 	final Logger m_logger = LoggerFactory.getLogger(DefaultAuthenticationProvider.class);
 
 	@Autowired
 	private UserDao m_userDao;
 
 	static {
 		StatusNetServiceFactory.setHost(System.getProperty("statusNetHost", "c4.amberwood.net"));
 		StatusNetServiceFactory.setPort(Integer.valueOf(System.getProperty("statusNetPort", "80")));
 		StatusNetServiceFactory.setRoot(System.getProperty("statusNetRoot", ""));
 	}
 
 	private StatusNetServiceFactory m_statusNetServiceFactory = StatusNetServiceFactory.getInstance();
 
 	@Override
 	protected void doAfterPropertiesSet() throws Exception {
 		Assert.notNull(m_userDao);
 	}
 
 	@Override
 	protected void additionalAuthenticationChecks(final UserDetails userDetails, final UsernamePasswordAuthenticationToken token) throws AuthenticationException {
         if (userDetails.getPassword() == null || !userDetails.getPassword().equals(token.getCredentials().toString())) {
         	m_logger.debug("additionalAuthenticationChecks failed");
             throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
         }
 	}
 
 	@Override
	@Transactional
 	protected UserDetails retrieveUser(final String username, final UsernamePasswordAuthenticationToken token) throws AuthenticationException {
 		m_logger.debug("username = {}", username);
         if (!StringUtils.hasLength(username)) {
         	m_logger.debug("authentication attempted with empty username");
             throw new UsernameNotFoundException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.emptyUsername", "Username cannot be empty"));
         }
         final String password = token.getCredentials().toString();
         if (!StringUtils.hasLength(password)) {
         	m_logger.debug("authentication attempted with empty password");
             throw new InsufficientAuthenticationException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
         }
 
         UserDetails user = m_userDao.get(username);
         m_logger.debug("user = {}", user);
         if (user == null || !password.equals(user.getPassword())) {
         	final StatusNetService sn = m_statusNetServiceFactory.getService(username, password);
         	try {
             	sn.authorize();
             	user = sn.getUser();
             	m_userDao.save((User)user);
         	} catch (final Exception e) {
         		m_logger.debug("exception while retrieving " + username, e);
                 throw new InsufficientAuthenticationException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
         	}
         }
 
         m_logger.debug("returning user = {}", user);
         return user;
 	}
 
 }
