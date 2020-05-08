 package com.luntsys.luntbuild.security;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.acegisecurity.providers.dao.AbstractUserDetailsAuthenticationProvider;
 import org.acegisecurity.userdetails.UserDetails;
 import org.acegisecurity.userdetails.UserDetailsService;
 import org.acegisecurity.userdetails.UsernameNotFoundException;
 import org.acegisecurity.providers.dao.SaltSource;
 
 import org.acegisecurity.providers.encoding.PasswordEncoder;
 import org.acegisecurity.providers.encoding.PlaintextPasswordEncoder;
 
 import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
 
 import org.acegisecurity.AuthenticationCredentialsNotFoundException;
 import org.acegisecurity.AuthenticationException;
 import org.acegisecurity.BadCredentialsException;
 import org.acegisecurity.GrantedAuthority;
 import org.acegisecurity.GrantedAuthorityImpl;
 import org.acegisecurity.AuthenticationServiceException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.dao.DataAccessException;
 import org.acegisecurity.DisabledException;
 import org.acegisecurity.AccountExpiredException;
 import org.acegisecurity.CredentialsExpiredException;
 
 import org.acegisecurity.Authentication;
 import org.springframework.util.Assert;
 
 import com.luntsys.luntbuild.db.Project;
 import com.luntsys.luntbuild.db.Role;
 import com.luntsys.luntbuild.db.User;
 import com.luntsys.luntbuild.notifiers.EmailNotifier;
 import com.luntsys.luntbuild.notifiers.TemplatedNotifier;
 import com.luntsys.luntbuild.utility.Luntbuild;
 
 import org.acegisecurity.providers.dao.UserCache;
 import org.acegisecurity.providers.dao.cache.NullUserCache;
 
 /**
  * LDAP authentication provider.
  *
  * @author Lubos Pochman based on contribution from Kira (kec161)
  */
 public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider  {
 
     private static transient final Log logger = LogFactory.getLog(AuthenticationProvider.class);
 
     private UserDetailsService authenticationDao;
     private PasswordEncoder passwordEncoder = new PlaintextPasswordEncoder();
     private SaltSource saltSource;
     private boolean hideUserNotFoundExceptions = true;
     private UserCache userCache = new NullUserCache();
     private boolean forcePrincipalAsString = false;
     private UserDetails userAuth = null;
 
     /**
      * Sets the authentication DAO.
      * 
      * @param authenticationDao the authentication DAO
      */
     public void setAuthenticationDao(UserDetailsService authenticationDao) {
         this.authenticationDao = authenticationDao;
     }
 
     /**
      * Gets the authentication DAO.
      * 
      * @return the authentication DAO
      */
     public UserDetailsService getAuthenticationDao() {
         return this.authenticationDao;
     }
 
     /**
      * Sets suppression of <code>BadCredentialsException</code> for unfound
      * usernames or incorrect passwords.
      * 
      * <p>By default the <code>DaoAuthenticationProvider</code> throws a
      * <code>BadCredentialsException</code> if a username is not found or the
      * password is incorrect. Setting this property to <code>false</code> will
      * cause <code>UsernameNotFoundException</code>s to be thrown instead for
      * the former. Note this is considered less secure than throwing
      * <code>BadCredentialsException</code> for both exceptions.</p>
      *
      * @param hideUserNotFoundExceptions set to <code>false</code> if you wish
      *        <code>UsernameNotFoundException</code>s to be thrown instead of
      *        the non-specific <code>BadCredentialsException</code> (defaults
      *        to <code>true</code>)
      */
     public void setHideUserNotFoundExceptions(
         boolean hideUserNotFoundExceptions) {
         this.hideUserNotFoundExceptions = hideUserNotFoundExceptions;
     }
 
     /**
      * Checks if suppression of <code>BadCredentialsException</code> for unfound
      * usernames or incorrect passwords is on.
      * 
      * @return <code>true</code> if <code>BadCredentialsException</code> should be thrown,
      * <code>false</code> if <code>UsernameNotFoundException</code> should be thrown
      */
     public boolean isHideUserNotFoundExceptions() {
         return this.hideUserNotFoundExceptions;
     }
 
     /**
      * Sets the <code>PasswordEncoder</code> instance to be used to encode and validate
      * passwords. If not set, {@link PlaintextPasswordEncoder} will be used by
      * default.
      *
      * @param passwordEncoder the <code>PasswordEncoder</code> to use
      */
     public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
         this.passwordEncoder = passwordEncoder;
     }
 
     /**
      * Gets the <code>PasswordEncoder</code> instance to be used.
      * 
      * @return the <code>PasswordEncoder</code> to used,
      * or <code>null</code> if {@link PlaintextPasswordEncoder} will be used
      */
     public PasswordEncoder getPasswordEncoder() {
         return this.passwordEncoder;
     }
 
     /**
      * Sets the source of salts to use when decoding passwords.  <code>null</code>
      * is a valid value, meaning the <code>DaoAuthenticationProvider</code>
      * will present <code>null</code> to the relevant
      * <code>PasswordEncoder</code>.
      *
      * @param saltSource the salt source to use when attempting to decode passwords via the
      *        <code>PasswordEncoder</code>
      */
     public void setSaltSource(SaltSource saltSource) {
         this.saltSource = saltSource;
     }
 
     /**
      * Gets the source of salts to use when decoding passwords.
      * 
      * @return the salt source to use when attempting to decode passwords via the
      *        <code>PasswordEncoder</code>, or <code>null</code> for no source
      */
     public SaltSource getSaltSource() {
         return this.saltSource;
     }
 
     protected void additionalAuthenticationChecks(UserDetails userDetails,
             UsernamePasswordAuthenticationToken authentication)
     		throws AuthenticationException {
         Object salt = null;
         this.userAuth = userDetails;
 
         String name = authentication.getPrincipal().toString().trim();
         String password = authentication.getCredentials().toString().trim();
 
         // Handle sys admin
         if (name.equals("luntbuild") || name.equals("anonymous")) {
             if (userDetails == null)
                 throw new AuthenticationServiceException(
                 "AuthenticationDao returned null, which is an interface contract violation");
 
             if (this.saltSource != null) {
                 salt = this.saltSource.getSalt(userDetails);
             }
 
             if (!this.passwordEncoder.isPasswordValid(userDetails.getPassword(),
                     authentication.getCredentials().toString(), salt)) {
                 throw new BadCredentialsException("Bad credentials: "
                         + userDetails.toString());
             }
             return;
         }
 
         // Get Luntbuild authenticator
         if (!(this.authenticationDao instanceof ApplicationInternalDAO)) return;
 
         ApplicationInternalDAO luntbuildAuthenticator = (ApplicationInternalDAO)this.authenticationDao;
 
         int port = 389;
         try {
             port = Integer.parseInt(luntbuildAuthenticator.getLdapPort());
         } catch (NumberFormatException e) {
             port = 389;
         }
 
         // Get LDAP host and userDN
         String host = luntbuildAuthenticator.getLdapHost();
         String userDN = luntbuildAuthenticator.getLdapUserDn();
         boolean useLuntbuildOnFail =
             new Boolean(luntbuildAuthenticator.getLdapUseLuntbuildOnFail()).booleanValue();
         boolean canCreateProject =
             new Boolean(luntbuildAuthenticator.getLdapCanCreateProject()).booleanValue();
         boolean canViewProject =
             new Boolean(luntbuildAuthenticator.getLdapCanViewProject()).booleanValue();
         boolean canBuildProject =
             new Boolean(luntbuildAuthenticator.getLdapCanBuildProject()).booleanValue();
         boolean doCreateLuntbuildUser =
             new Boolean(luntbuildAuthenticator.getLdapCreateLuntbuildUser()).booleanValue();
         String emailAttr = luntbuildAuthenticator.getLdapEmailAttrName();
 
         // LDAP not specified use Luntbuild authentication
         if (host.length() == 0 || userDN.length() == 0 || host.startsWith("${") || userDN.startsWith("${")) {
             if (useLuntbuildOnFail &&
                     userDetails != null && userDetails.getPassword().equals(password)) {
                 logger.info("Luntbuild User Authentication (user: " + name + ") SUCCESS\n");
                 return;
             }
             authentication.setAuthenticated(false);
             logger.warn("User Authentication (user: " + name + ") FAILURE\n");
             throw new AuthenticationCredentialsNotFoundException(
                     "Cannot authenticate user " + name);
         }
         String ldapAuthentication = luntbuildAuthenticator.getLdapAuthentication();
         String ldapUserId = luntbuildAuthenticator.getLdapUserId();
         String ldapUrl = luntbuildAuthenticator.getLdapUrl();
         String ldapPrefix = luntbuildAuthenticator.getLdapPrefix();
         String ldapSuffix = luntbuildAuthenticator.getLdapSuffix();
 
         // LDAP specified, use it
         LDAPAuthenticator authenticator =
             new LDAPAuthenticator(host, port, userDN, ldapAuthentication, ldapUserId);
         if (ldapUrl != null) {
             authenticator.setLdapUrl(ldapUrl);
         }
         if (ldapPrefix != null) {
             authenticator.setLdapPrefix(ldapPrefix);
         }
         if (ldapSuffix != null) {
             authenticator.setLdapSuffix(ldapSuffix);
         }
         if (authenticator.authenticate(name, password)){
             // Load Luntbuild user if exist, or create new user
             if (doCreateLuntbuildUser) {
                 if (!Luntbuild.getDao().isUserExist(name.toLowerCase())) {
                     // create user
                     User user = new User();
                     user.setName(name.toLowerCase());
                     user.setCanCreateProject(canCreateProject);
                     user.setDecryptedPassword(password);
                     // set email
                     if (emailAttr != null && emailAttr.trim().length() > 0) {
                         String email = authenticator.lookupEmail(name, password, emailAttr.trim());
                         Map contacts = user.getContacts();
                         List notifiers = Luntbuild.getNotifierInstances(Luntbuild.notifiers);
                         EmailNotifier emailNotifier = null;
                         for (Iterator iter = notifiers.iterator(); iter.hasNext();) {
                             TemplatedNotifier cz = (TemplatedNotifier) iter.next();
                             if (cz instanceof EmailNotifier) {
                                 emailNotifier = (EmailNotifier)cz;
                                 break;
                             }
                         }
                         if (emailNotifier != null) {
                             contacts.put(emailNotifier.getKey(), email);
                             user.setContacts(contacts);
                         }
                     }
 
                     Luntbuild.getDao().saveUserInternal(user);
 
                     if (canCreateProject) setProjectsPrivileges(user, Role.LUNTBUILD_PRJ_ADMIN);
                     if (canBuildProject) setProjectsPrivileges(user, Role.LUNTBUILD_PRJ_BUILDER);
                     if (canViewProject) setProjectsPrivileges(user, Role.LUNTBUILD_PRJ_VIEWER);
 
                     userDetails = this.authenticationDao.loadUserByUsername(name.toLowerCase());
 
                     this.userAuth = userDetails;
 
                 } else {
                     // Update password
                     User user = Luntbuild.getDao().loadUser(name.toLowerCase());
                     user.setDecryptedPassword(password);
                 }
 
             } else {
                 this.userAuth =
                     authorizeUser(name, password, canCreateProject, canBuildProject, canViewProject);
             }
             logger.info("LDAP User Authentication (user: " + name + ") SUCCESS\n");
             return;
         } else {
             if (useLuntbuildOnFail &&
                     userDetails != null && userDetails.getPassword().equals(password)) {
                 logger.info("Luntbuild User Authentication (user: " + name + ") SUCCESS\n");
                 return;
             }
             authentication.setAuthenticated(false);
             logger.warn("User Authentication (user: " + name + ") FAILURE\n");
             throw new AuthenticationCredentialsNotFoundException(
                     "Cannot authenticate user " + name + " against LDAP");
         }
     }
 
     private void setProjectsPrivileges(User user, String role) {
         Iterator it = Luntbuild.getDao().loadProjectsInternal().iterator();
         while (it.hasNext()) {
             Project project = (Project) it.next();
             List origUsers = project.getMappedRolesUserList(role);
             ArrayList users = new ArrayList();
             users.addAll(origUsers);
             users.add(user);
             project.putMappedRolesUserList(users, role);
             Luntbuild.getDao().saveProjectInternal(project);
         }
     }
 
     private UserDetails authorizeUser(String name, String password,
             boolean canCreateProject, boolean canBuildProject, boolean canViewProject) {
         UserDetails userdetails = null;
         GrantedAuthority authorities[] = null;
 
         int authSize = 1;
         if (canCreateProject) authSize++;
         if (canViewProject) authSize += 2;
         if (canBuildProject) authSize++;
 
         authorities = new GrantedAuthorityImpl[authSize];
 
         int ix = 0;
         authorities[ix++] = new GrantedAuthorityImpl(Role.ROLE_AUTHENTICATED);
         if (canCreateProject)
             authorities[ix++] = new GrantedAuthorityImpl("LUNTBUILD_PRJ_ADMIN_0");
         if (canViewProject) {
             authorities[ix++] = new GrantedAuthorityImpl(Role.ROLE_ANONYMOUS);
             authorities[ix++] = new GrantedAuthorityImpl("LUNTBUILD_PRJ_VIEWER");
         }
         if (canBuildProject)
             authorities[ix++] = new GrantedAuthorityImpl("LUNTBUILD_PRJ_BUILDER");
 
         userdetails =
            new org.acegisecurity.userdetails.User(name, password, true, true, true, authorities);
 
         return userdetails;
     }
 
     protected final UserDetails retrieveUser(String username,
             UsernamePasswordAuthenticationToken authentication)
     		throws AuthenticationException {
         UserDetails loadedUser;
 
         try {
             loadedUser = this.authenticationDao.loadUserByUsername(username);
         } catch (UsernameNotFoundException notFound) {
             return null;
         } catch (DataAccessException repositoryProblem) {
             throw new AuthenticationServiceException(repositoryProblem
                     .getMessage(), repositoryProblem);
         }
 
         if (loadedUser == null) {
             throw new AuthenticationServiceException(
             "AuthenticationDao returned null, which is an interface contract violation");
         }
 
         return loadedUser;
     }
 
     /**
      * @inheritDoc
      */
     public Authentication authenticate(Authentication authentication)
     		throws AuthenticationException {
         Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class,
                 authentication,
         "Only UsernamePasswordAuthenticationToken is supported");
 
         // Determine username
         String username = (authentication.getPrincipal() == null)
         ? "NONE_PROVIDED" : authentication.getName();
 
         boolean cacheWasUsed = true;
         UserDetails user = this.userCache.getUserFromCache(username);
 
         if (user == null) {
             cacheWasUsed = false;
             user = retrieveUser(username, (UsernamePasswordAuthenticationToken) authentication);
         }
 
         // This check must come here, as we don't want to tell users
         // about account status unless they presented the correct credentials
         try {
             additionalAuthenticationChecks(user,
                     (UsernamePasswordAuthenticationToken) authentication);
         } catch (AuthenticationException exception) {
             // There was a problem, so try again after checking we're using latest data
             cacheWasUsed = false;
             this.userAuth = null;
             user = retrieveUser(username,
                     (UsernamePasswordAuthenticationToken) authentication);
             additionalAuthenticationChecks(user,
                     (UsernamePasswordAuthenticationToken) authentication);
         }
         if (user == null) user = this.userAuth;
         if (user == null) {
             throw new UsernameNotFoundException("User " + username + " not found");
         }
 
         if (!user.isEnabled()) {
             throw new DisabledException("User is disabled");
         }
 
         if (!user.isAccountNonExpired()) {
             throw new AccountExpiredException("User account has expired");
         }
 
         if (!user.isCredentialsNonExpired()) {
             throw new CredentialsExpiredException("User credentials have expired");
         }
 
         if (!cacheWasUsed) {
             this.userCache.putUserInCache(user);
         }
 
         Object principalToReturn = user;
 
         if (this.forcePrincipalAsString) {
             principalToReturn = user.getUsername();
         }
 
         return createSuccessAuthentication(principalToReturn, authentication, user);
     }
 }
