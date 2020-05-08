 package jp.kurusugawa.jircd;
 
 import java.net.URI;
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.naming.NamingEnumeration;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 import javax.security.auth.Subject;
 import javax.security.auth.login.AppConfigurationEntry;
 import javax.security.auth.login.Configuration;
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 
 import jp.kurusugawa.jircd.project.ProjectUser;
 
 import org.apache.log4j.Logger;
 
 public class LdapSecurityManager implements SecurityManager {
 	private static Logger LOG = Logger.getLogger(LdapSecurityManager.class);
 
 	private final TimerCacheHashMap<String, Collection<String>> mGroupCache;
 
	private LdapSecurityManager() {
 		mGroupCache = new TimerCacheHashMap<String, Collection<String>>();
 	}
 
 	@Override
 	public boolean authenticate(ProjectUser aUser) {
 		try {
 			if (LOG.isInfoEnabled()) {
 				LOG.info("begin authenticate. aUser(=" + aUser.getIdent() + ")");
 			}
 			LoginContext tLoginContext = new LoginContext("IRCLogin", new Subject(), aUser.getAuthenticationCallbackHandler());
 			tLoginContext.login();
 			tLoginContext.logout();
 			if (LOG.isInfoEnabled()) {
 				LOG.info("end authenticate. aUser(=" + aUser.getIdent() + ")");
 			}
 			return true;
 		} catch (LoginException e) {
 			LOG.warn("authenticate failed", e);
 			return false;
 		}
 	}
 
 	@Override
 	public boolean isProjectMember(ProjectUser aUser, String aProjectName) {
 		return getBelongGroups(aUser).contains(aProjectName);
 	}
 
 	@Override
 	public void startup(Properties aSettings) {
 	}
 
 	@Override
 	public String getName() {
 		return getClass().getCanonicalName();
 	}
 
 	@Override
 	public void shutdown() {
 	}
 
 	private Collection<String> getBelongGroups(ProjectUser aUser) {
 		String tUserIdentity = aUser.getIdent();
 		ReentrantLock tLock = mGroupCache.getLock();
 		tLock.lock();
 		Collection<String> tGroups;
 		try {
 			tGroups = mGroupCache.get(tUserIdentity);
 			if (tGroups == null) {
 				try {
 					boolean tIsInfoEnabled = LOG.isInfoEnabled();
 
 					if (tIsInfoEnabled) {
 						LOG.info("get user(=" + tUserIdentity + ") belong groups: ");
 					}
 
 					Configuration tConfiguration = Configuration.getConfiguration();
 					AppConfigurationEntry[] tAppConfigurationEntries = tConfiguration.getAppConfigurationEntry("IRCProject");
 
 					AppConfigurationEntry tAppConfigurationEntry = tAppConfigurationEntries[0];
 					Hashtable<String, String> tHashtable = new Hashtable<String, String>();
 					tHashtable.put(InitialDirContext.INITIAL_CONTEXT_FACTORY, tAppConfigurationEntry.getLoginModuleName());
 					URI tGroupProvider = new URI((String) tAppConfigurationEntry.getOptions().get("groupProvider"));
 					tHashtable.put(InitialDirContext.PROVIDER_URL, tGroupProvider.toString());
 					InitialDirContext tInitialDirContext = new InitialDirContext(tHashtable);
 
 					// Create the search controls
 					SearchControls tSearchControls = new SearchControls();
 					tSearchControls.setReturningAttributes(new String[] { "cn", });
 					tSearchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
 
 					// replace "{USERNAME}"
 					String tGroupFilter = ((String) tAppConfigurationEntry.getOptions().get("groupFilter")).replace("{USERNAME}", "{0}");
 
 					if (tIsInfoEnabled) {
 						LOG.info("ldap query: " + new MessageFormat(tGroupFilter).format(new Object[] { tUserIdentity }));
 					}
 
 					NamingEnumeration<SearchResult> tAnswers = tInitialDirContext.search("", tGroupFilter, new Object[] { tUserIdentity }, tSearchControls);
 
 					int totalResults = 0;
 					tGroups = new HashSet<String>();
 					while (tAnswers.hasMoreElements()) {
 						SearchResult tSearchResult = tAnswers.next();
 						totalResults++;
 						String tGroupName = (String) tSearchResult.getAttributes().get("cn").get();
 						tGroups.add(tGroupName);
 						if (tIsInfoEnabled) {
 							LOG.info(">" + tSearchResult.getName() + ", " + tSearchResult.getAttributes());
 						}
 					}
 					tInitialDirContext.close();
 
 					if (tIsInfoEnabled) {
 						LOG.info("Total results: " + totalResults);
 					}
 					mGroupCache.put(tUserIdentity, tGroups);
 				} catch (Exception e) {
 					throw new RuntimeException("group get failed", e);
 				}
 			}
 		} finally {
 			tLock.unlock();
 		}
 		return tGroups;
 	}
 }
