 /**
  * Copyright 2010 Tristan Tarrant
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.dataforte.doorkeeper.account.provider.ldap;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.regex.Pattern;
 
 import javax.annotation.PostConstruct;
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 import javax.naming.ldap.Control;
 import javax.naming.ldap.InitialLdapContext;
 import javax.naming.ldap.LdapContext;
 import javax.naming.ldap.PagedResultsControl;
 import javax.naming.ldap.PagedResultsResponseControl;
 import javax.naming.ldap.StartTlsRequest;
 import javax.naming.ldap.StartTlsResponse;
 
 import net.dataforte.commons.slf4j.LoggerFactory;
 import net.dataforte.doorkeeper.User;
 import net.dataforte.doorkeeper.account.provider.AbstractAccountProvider;
 import net.dataforte.doorkeeper.annotations.Property;
 import net.dataforte.doorkeeper.annotations.Required;
 import net.dataforte.doorkeeper.authenticator.AuthenticatorException;
 import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;
 import net.dataforte.doorkeeper.authenticator.AuthenticatorUser;
 import net.dataforte.doorkeeper.authenticator.PasswordAuthenticatorToken;
 
 import org.slf4j.Logger;
 
 @Property(name = "name", value = "ldap")
 public class LdapAccountProvider extends AbstractAccountProvider {
 	private static final String COM_SUN_JNDI_LDAP_CONNECT_POOL = "com.sun.jndi.ldap.connect.pool";
 	private static final Logger log = LoggerFactory.make();
 	static final Pattern MAPPING_REGEX = Pattern.compile("([\\w_\\-]+)(?:[\\s]*=[\\s]*)((?:[\'\"]).+?(?:[\'\"]))");
 	static final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 
 	private ConcurrentMap<String, Object> cache;
 	private Hashtable<String, String> env;
 	private String searchBase;
 	private String userBase;
 	private String groupBase;
 	private String url;
 	private String principal;
 	private String credentials;
 	private String attributeFilter;
 	private boolean useTls;
 	private boolean paging;
 	private int pageSize;
 	private String uidAttribute = "uid";
 	private String groupAttribute = "cn";
 	private String memberOfAttribute;
 	private String memberAttribute = "member";
 	List<String> staticGroups;
 	private String[] userReturnedAttributes;
 	private Map<Pattern, String> ouMap = new LinkedHashMap<Pattern, String>();
 	private Map<Pattern, String> groupMap = new LinkedHashMap<Pattern, String>();
 	private Map<String, String> attributeMap = new LinkedHashMap<String, String>();
 	private DateFormat serverDateFormat = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
 
 	static final LdapEntry NULL_USER = new LdapEntry(null);
 
 	@Required
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	/**
 	 * The DN of the search base under which users and groups will be searched
 	 * 
 	 * @return
 	 */
 	@Required
 	public String getSearchBase() {
 		return searchBase;
 	}
 
 	public void setSearchBase(String searchBase) {
 		this.searchBase = searchBase;
 	}
 
 	/**
 	 * The RDN, relative to the searchBase, where user entries are
 	 * 
 	 * @return
 	 */
 	public String getUserBase() {
 		return userBase;
 	}
 
 	/**
 	 * The RDN, relative to the searchBase, where group entries are
 	 * 
 	 * @return
 	 */
 	public String getGroupBase() {
 		return groupBase;
 	}
 
 	public void setUserBase(String userBase) {
 		this.userBase = userBase;
 	}
 
 	public void setGroupBase(String groupBase) {
 		this.groupBase = groupBase;
 	}
 
 	public String getMemberAttribute() {
 		return memberAttribute;
 	}
 
 	public void setMemberAttribute(String memberAttribute) {
 		this.memberAttribute = memberAttribute;
 	}
 
 	public String getPrincipal() {
 		return principal;
 	}
 
 	public void setPrincipal(String principal) {
 		this.principal = principal;
 	}
 
 	public String getCredentials() {
 		return credentials;
 	}
 
 	public void setCredentials(String credentials) {
 		this.credentials = credentials;
 	}
 
 	public boolean isUseTls() {
 		return useTls;
 	}
 
 	public void setUseTls(boolean useTls) {
 		this.useTls = useTls;
 	}
 
 	public boolean isPaging() {
 		return paging;
 	}
 
 	public void setPaging(boolean paging) {
 		this.paging = paging;
 	}
 
 	public int getPageSize() {
 		return pageSize;
 	}
 
 	public void setPageSize(int pageSize) {
 		this.pageSize = pageSize;
 	}
 
 	public String getUidAttribute() {
 		return uidAttribute;
 	}
 
 	public void setUidAttribute(String uidAttribute) {
 		this.uidAttribute = uidAttribute;
 	}
 
 	public String getGroupAttribute() {
 		return groupAttribute;
 	}
 
 	public void setGroupAttribute(String groupAttribute) {
 		this.groupAttribute = groupAttribute;
 	}
 
 	public String getMemberOfAttribute() {
 		return memberOfAttribute;
 	}
 
 	public void setMemberOfAttribute(String memberOfAttribute) {
 		this.memberOfAttribute = memberOfAttribute;
 	}
 
 	public Map<String, String> getAttributeMap() {
 		return attributeMap;
 	}
 
 	public void setAttributeMap(Map<String, String> attributeMap) {
 		this.attributeMap = attributeMap;
 	}
 
 	public Map<Pattern, String> getOuMap() {
 		return ouMap;
 	}
 
 	public void setOuMap(Map<String, String> ouMap) {
 		this.ouMap.clear();
 		for (Entry<String, String> entry : ouMap.entrySet()) {
 			this.ouMap.put(Pattern.compile(entry.getKey()), entry.getValue());
 		}
 	}
 
 	public Map<Pattern, String> getGroupMap() {
 		return groupMap;
 	}
 
 	public void setGroupMap(Map<String, String> groupMap) {
 		this.groupMap.clear();
 		for (Entry<String, String> entry : groupMap.entrySet()) {
 			this.groupMap.put(Pattern.compile(entry.getKey()), entry.getValue());
 		}
 	}
 	
 	private User entry2user(String principalName, LdapEntry entry) {
 		AuthenticatorUser user = new AuthenticatorUser(principalName);
 		user.getGroups().addAll(entry.addGroups);
 		user.getGroups().removeAll(entry.delGroups);
 		for(Entry<String, String> attr : entry.attributes.entrySet()) {
 			user.getProperties().put(attr.getKey(), new String[] {attr.getValue()});
 		}
 		return user;
 	}
 	
 	private LdapEntry loadInternal(AuthenticatorToken token) {
 		// Check the cache first
 		LdapEntry entry = (LdapEntry) cache.get(token.getPrincipalName());
 
 		if (entry != null) {
 			if (log.isDebugEnabled()) {
 				log.debug("Cache lookup for " + token.getPrincipalName() + " = " + entry);
 			}
 			if (entry != NULL_USER) {
 				return entry;
 			} else {
 				return null;
 			}
 		}
 
 		try {
 			if (log.isDebugEnabled()) {
 				log.debug("LDAP search for " + token.getPrincipalName());
 			}
 			// Search LDAP for the user
 			List<LdapEntry> entries = ldapSearch(username2filter(token.getPrincipalName()));
 			if (entries.size() == 0) {
 				if (log.isDebugEnabled()) {
 					log.debug(token.getPrincipalName() + " not found in LDAP");
 				}
 				cache.put(token.getPrincipalName(), NULL_USER);
 				return null;
 			} else {
 				entry = entries.get(0);
 				if (log.isDebugEnabled()) {
 					log.debug(token.getPrincipalName() + " found in LDAP, adding to cache");
 				}
 				// Cache the entry data
 				cache.put(token.getPrincipalName(), entry);
 
 				return entry;
 			}
 		} catch (NamingException e) {
 			log.error("LDAP Error", e);
 			return null;
 		}
 	}
 
 	@Override
 	public void flushCaches() {
 		cache.clear();
 	}
 
 	@Override
 	public User authenticate(AuthenticatorToken token) throws AuthenticatorException {
 		LdapContext ctx = null;
 		LdapContext ctx2 = null;
 
 		PasswordAuthenticatorToken passwordToken = (PasswordAuthenticatorToken) token;
 		try {		
 			LdapEntry entry = loadInternal(token);
 			
 			if (entry == null) {				
 				return null;
 			} else {
 				// Perform the actual authentication
 				Hashtable<String, String> authEnv = new Hashtable<String, String>(env);
 
 				authEnv.put(Context.SECURITY_PRINCIPAL, entry.dn);
 				authEnv.put(Context.SECURITY_CREDENTIALS, passwordToken.getPassword());
 
 				// Disable connection pool for authentication
 				authEnv.put(COM_SUN_JNDI_LDAP_CONNECT_POOL, "false");
 
 				ctx = new InitialLdapContext(authEnv, null);
 				if (useTls) {
 					StartTlsResponse tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
 					tls.negotiate();
 				}
 				// Lookup the user itself
 				ctx2 = (LdapContext) ctx.lookup(entry.dn);
 
 				if (log.isDebugEnabled()) {
 					log.debug("Authenticated successfully user " + passwordToken.getPrincipalName());
 				}
 
 				// Cache the data
 				cache.put(passwordToken.getPrincipalName(), entry);
 				
 				return entry2user(passwordToken.getPrincipalName(), entry);
 			}
 		} catch (Exception e) {
 			log.error("Error during LDAP authentication", e);
 			throw new AuthenticatorException(e);
 		} finally {
 			closeContexts(ctx2, ctx);
 		}
 	}
 
 	@Override
 	public User load(AuthenticatorToken token) {
 		LdapEntry entry = loadInternal(token);
 		if(entry==null) {
 			return null;
 		} else {
 			return entry2user(token.getPrincipalName(), entry);
 		}
 	}
 
 	@Override
 	public List<User> getUsersInGroup(String group) {
 		try {
 			if (log.isDebugEnabled()) {
 				log.debug("LDAP search for group " + group);
 			}
 			// First of all find the group
 			List<LdapEntry> entries = ldapSearch(group2filter(group));
 			if (entries.size() == 0) {
 				if (log.isDebugEnabled()) {
 					log.debug(group + " not found in LDAP");
 				}				
 				return null;
 			} else {
 				LdapEntry entry = entries.get(0);
 				if (log.isDebugEnabled()) {
 					log.debug(group + " found in LDAP");
 				}
 				
 
 				return null;
 			}
 		} catch (NamingException e) {
 			log.error("LDAP Error", e);
 			return null;
 		}
 	}
 
 	@Override
 	public Collection<String> getGroups() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@PostConstruct
 	public void init() {
 		if (url == null) {
 			throw new IllegalStateException("Parameter 'url' is required");
 		}
 		if (searchBase == null) {
 			throw new IllegalStateException("Parameter 'searchBase' is required");
 		}
 
 		cache = new ConcurrentHashMap<String, Object>();		
 
 		// Add the uid attribute without a value
 		attributeMap.put(uidAttribute, null);
 		// Add the memberOf attribute without a value
 		if (memberOfAttribute != null) {
 			attributeMap.put(memberOfAttribute, null);
 		}
 		// Build an array of attributes we want returned from LDAP
 		userReturnedAttributes = attributeMap.keySet().toArray(new String[attributeMap.size()]);
 
 		ouMap = new HashMap<Pattern, String>();
 
 		// Inject the default settings into the hashtable
 		env = new Hashtable<String, String>();
 		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 		env.put(Context.PROVIDER_URL, url);
 		env.put(Context.SECURITY_AUTHENTICATION, "simple");
 		if (principal != null) {
 			env.put(Context.SECURITY_PRINCIPAL, principal);
 			env.put(Context.SECURITY_CREDENTIALS, credentials);
 		}
 
 		if (log.isInfoEnabled()) {
 			log.info("Initialized");
 		}
 	}
 
 	/**
 	 * We don't handle listing the users
 	 */
 	public List<String> list() {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * Creates an LDAP filter for finding a user based on his user id
 	 * 
 	 * @param username
 	 * @return
 	 */
 	private String username2filter(String username) {
 		if (attributeFilter == null || attributeFilter.equals("")) {
 			return String.format("%s=%s", uidAttribute, username);
 		} else {
 			return "(&(" + uidAttribute + "=" + username + ")(" + attributeFilter + "))";
 		}
 	}
 	
 	/**
 	 * Creates an LDAP filter for finding a group based on its name
 	 * 
 	 * @param username
 	 * @return
 	 */
 	private String group2filter(String groupname) {
 		return String.format("%s=%s", groupAttribute, groupname);
 	}
 
 	private List<LdapEntry> ldapSearch(String filter) throws NamingException {
 		return ldapSearch(filter, searchBase);
 	}
 	
 	private List<LdapEntry> ldapSearch(String filter, String base) throws NamingException {
 		LdapContext ctx = null;
 		try {
 			ctx = new InitialLdapContext(env, null);
 			List<LdapEntry> results = search(ctx, base, filter);
 			return results;
 		} finally {
 			closeContexts(ctx);
 		}
 	}
 
 	/**
 	 * Searches the LdapContext with a specified filter under base. Returns a
 	 * list of LDAPEntry elements with attributes and addGroups remapped
 	 * 
 	 * @param ctx
 	 * @param base
 	 * @param filter
 	 * @return
 	 * @throws NamingException
 	 */
 	private List<LdapEntry> search(LdapContext ctx, String base, String filter) throws NamingException {
 		NamingEnumeration<SearchResult> en = null;
 
 		List<LdapEntry> results = new ArrayList<LdapEntry>();
 		try {
 			SearchControls searchCtls = new SearchControls();
 			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 
 			if (paging) {
 				ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, true) });
 			}
 
 			searchCtls.setReturningAttributes(userReturnedAttributes);
 
 			byte[] cookie = null;
 			do {
 				// Search for objects using the filter
 				en = ctx.search(base, filter, searchCtls);
 				while (en != null && en.hasMoreElements()) {
 					SearchResult sr = en.nextElement();
 					LdapEntry item = new LdapEntry(sr.getNameInNamespace());
 
 					// Add all static addGroups if necessary
 					if (staticGroups != null)
 						item.addGroups.addAll(staticGroups);
 
 					// Add addGroups determined by ou map
 					for (Map.Entry<Pattern, String> ouEntry : ouMap.entrySet()) {
 						if (ouEntry.getKey().matcher(item.dn).matches()) {
 							item.addGroups.add(ouEntry.getValue());
 						}
 					}
 
 					NamingEnumeration<? extends Attribute> ne = sr.getAttributes().getAll();
 					for (; ne.hasMoreElements();) {
 						Attribute a = ne.nextElement();
 						if (a != null) {
							String id = a.getID();
 							// If it's the memberOf field, handle the addGroups
 							if (id.equalsIgnoreCase(memberOfAttribute)) {
 								Set<String> groups = new HashSet<String>();
 								for (int i = 0; i < a.size(); i++) {
 									String memberOf = (String) a.get(i);
 									groups.add(memberOf);
 								}
 
 								remapGroups(item, groups);
 							} else {
 								// Otherwise retrieve the field mapping
 								String remappedAttribute = attributeMap.get(id);
 								if (remappedAttribute != null) {
 									item.attributes.put(remappedAttribute, convertAttribute(a.get(0)));
 								}
 							}
 						}
 					}
 					closeEnumerations(ne);
 
 					if (groupBase != null) {
 						remapGroups(item, searchMembership(ctx, item.dn, new HashSet<String>()));
 					}
 
 					results.add(item);
 				}
 
 				closeEnumerations(en);
 
 				if (paging) {
 					Control[] responseControls = ctx.getResponseControls();
 					for (int i = 0; i < responseControls.length; i++) {
 						if (responseControls[i] instanceof PagedResultsResponseControl) {
 							cookie = ((PagedResultsResponseControl) responseControls[i]).getCookie();
 						}
 					}
 					if (cookie != null) {
 						ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, cookie, true) });
 					}
 				}
 			} while (cookie != null);
 
 			return results;
 		} catch (Exception e) {
 			log.error("Search error", e);
 			return null;
 		} finally {
 			closeEnumerations(en);
 		}
 	}
 
 	private void recursiveSearch(LdapContext ctx, String dn, String field, Set<String> results) throws NamingException {
 		Attributes attrs = ctx.getAttributes(dn, new String[] { field });
 		NamingEnumeration<? extends Attribute> en = null;
 		for (en = attrs.getAll(); en.hasMoreElements();) {
 			Object aobj = en.nextElement();
 			if (aobj instanceof Attribute) {
 				Attribute attr = (Attribute) aobj;
 				NamingEnumeration<?> e = null;
 				for (e = attr.getAll(); e.hasMoreElements();) {
 					Object vobj = e.nextElement();
 					if (vobj instanceof String) {
 						String value = (String) vobj;
 						if (!results.contains(value)) {
 							results.add(value);
 							recursiveSearch(ctx, value, field, results);
 						}
 					}
 				}
 				closeEnumerations(e);
 			}
 		}
 		closeEnumerations(en);
 	}
 
 	/**
 	 * Searches recursively for all groups which contain the specified dn
 	 * 
 	 * @param ctx
 	 * @param dn
 	 * @param groups
 	 * @return
 	 */
 	private Set<String> searchMembership(LdapContext ctx, String dn, Set<String> groups) {
 		NamingEnumeration<SearchResult> en = null;
 		try {
 			SearchControls searchCtls = new SearchControls();
 			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 			if (paging) {
 				ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, true) });
 			}
 
 			searchCtls.setReturningAttributes(new String[] { groupAttribute });
 			byte[] cookie = null;
 			do {
 				en = ctx.search(groupBase, String.format("(%s=%s)", memberAttribute, dn), searchCtls);
 				while (en != null && en.hasMoreElements()) {
 					SearchResult sr = en.nextElement();
 					if(!groups.contains(sr.getNameInNamespace())) {
 						groups.add(sr.getNameInNamespace());
 						// Now fill out the recursive group structure
 						searchMembership(ctx, sr.getNameInNamespace(), groups);
 					}
 				}
 				closeEnumerations(en);
 
 				if (paging) {
 					Control[] responseControls = ctx.getResponseControls();
 					for (int i = 0; i < responseControls.length; i++) {
 						if (responseControls[i] instanceof PagedResultsResponseControl) {
 							cookie = ((PagedResultsResponseControl) responseControls[i]).getCookie();
 						}
 					}
 					if (cookie != null) {
 						ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, cookie, true) });
 					}
 				}
 			} while (cookie != null);
 			
 			return groups;
 		} catch (Exception e) {
 			log.error("Search error", e);
 			return null;
 		} finally {
 			closeEnumerations(en);
 		}
 	}
 
 	private void remapGroups(LdapEntry item, Set<String> groups) {
 
 		// If a group map has not been specified, simply extract the group's
 		// name from its DN
 		if (groupMap.isEmpty()) {
 			for (String group : groups) {
 				String rdns[] = group.split("\\s*,\\s*");
 				item.addGroups.add(rdns[0].substring(rdns[0].indexOf('=') + 1));
 			}
 		} else {
 			// Remap them using the groupMap
 			for (String group : groups) {
 				for (Map.Entry<Pattern, String> groupEntry : groupMap.entrySet()) {
 					if (groupEntry.getKey().matcher(group).matches()) {
 						item.addGroups.add(groupEntry.getValue());
 					} else {
 						item.delGroups.add(groupEntry.getValue());
 					}
 				}
 			}
 			// Do not delete the user from groups which are
 			// also being added
 			item.delGroups.removeAll(item.addGroups);
 		}
 	}
 
 	private static void closeEnumerations(NamingEnumeration<?>... en) {
 		for (int i = 0; i < en.length; i++) {
 			try {
 				if (en[i] != null) {
 					en[i].close();
 				}
 			} catch (Throwable t) {
 				log.error("Error closing enumeration " + i, t);
 			}
 		}
 	}
 
 	private static void closeContexts(LdapContext... ctx) {
 		for (int i = 0; i < ctx.length; i++) {
 			try {
 				if (ctx[i] != null) {
 					ctx[i].close();
 				}
 			} catch (Throwable t) {
 				log.error("Error closing context " + i, t);
 			}
 		}
 	}
 
 	/**
 	 * Converts an attribute to a String
 	 * 
 	 * @param value
 	 * @return
 	 */
 	private String convertAttribute(Object value) {
 		if (value instanceof String) {
 			String v = (String) value;
 			try {
 				Date d = serverDateFormat.parse(v);
 				return df.format(d);
 			} catch (ParseException pe) {
 				return v;
 			}
 		} else if (value instanceof byte[]) {
 			byte[] bytes = (byte[]) value;
 			if (bytes.length == 16) {
 				return byteToUUID(bytes);
 			} else {
 				return new String(bytes);
 			}
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Converts a byte representation of a UUID in the canonical string
 	 * representation
 	 * 
 	 * @param b
 	 * @return
 	 */
 	private static String byteToUUID(byte b[]) {
 		return String.format("%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x", b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7], b[8], b[9], b[10], b[11], b[12], b[13], b[14],
 				b[15]);
 	}
 }
