 package uk.ac.cam.cl.dtg.ldap;
 
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 
import org.apache.commons.codec.binary.Base64;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Holly Priest <hp343@cam.ac.uk>
  * @version 1 All the main functionality for the LDAP queries
  * 
  */
 public class LDAPProvider {
 
 	/** Connection constants **/
 	private static final String CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
 	private static final String PROVIDER_URL = "ldap://ldap.lookup.cam.ac.uk:389";
 	private static final String CONTEXT_FILTER = "o=University of Cambridge,dc=cam,dc=ac,dc=uk";
 
 	private static final Logger log = LoggerFactory
 			.getLogger(LDAPProvider.class);
 
 	private static final Hashtable<String, String> env;
 	static {
 		env = new Hashtable<String, String>();
 		env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
 		env.put(Context.PROVIDER_URL, PROVIDER_URL);
 	}
 
 	/**
 	 * Initialises context and sets up basic query
 	 * 
 	 * @return NamingEnumeration<SearchResult>
 	 */
 	private static NamingEnumeration<SearchResult> initialiseContext(
 			String type, String parameter, String subtree, boolean partial) {
 		try {
 			DirContext ctx = new InitialDirContext(env);
 			SearchControls controls = new SearchControls();
 			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 
 			String searchContext = "ou=" + subtree + "," + CONTEXT_FILTER;
 			String searchParameters = "(" + type + "=" + parameter
 					+ (partial ? "*" : "") + ")";
 			NamingEnumeration<SearchResult> searchResults = ctx.search(
 					searchContext, searchParameters, controls);
 
 			return searchResults;
 
 		} catch (NamingException e) {
 			log.warn("Failed to initialise LDAP context", e);
 		}
 
 		return new EmptyNamingEnumeration();
 	}
 
 	/**
 	 * Sets up unique result query
 	 * 
 	 * @return NamingEnumeration<? extends Attribute>
 	 * @throws LDAPObjectNotFoundException
 	 */
 	private static Attributes setupUniqueQuery(String type, String parameter,
 			String subtree) throws LDAPObjectNotFoundException {
 		try {
 			NamingEnumeration<SearchResult> namingEnumeration = initialiseContext(
 					type, parameter, subtree, false);
 			if (namingEnumeration.hasMore()) {
 				SearchResult searchResult = namingEnumeration.next();
 				return searchResult.getAttributes();
 			} else {
 				log.warn(
 						"No result found for query type={}, parameter={},subtree={}",
 						type, parameter, subtree);
 				return null;
 			}
 		} catch (NamingException e) {
 			log.warn("Naming exception when processing NamingEnumeration", e);
 			return null;
 		}
 	}
 
 	/**
 	 * Unique user query Returns an LDAPUser object Takes 2 arguments: attribute
 	 * to search (eg. uid) and r to search with
 	 * 
 	 * @return LDAPUser
 	 */
 	static LDAPUser uniqueUserQuery(String lookupKey, String lookupValue)
 			throws LDAPObjectNotFoundException {
 
 		Attributes userResult = setupUniqueQuery(lookupKey, lookupValue,
 				"people");
 
 		if (userResult == null) {
 			throw new LDAPObjectNotFoundException("User not found");
 		}
 
 		return initLDAPUser(userResult);
 	}
 
 	/**
 	 * List of users Query Returns a list of LDAPUser objects
 	 */
 	static List<LDAPUser> multipleUserQuery(String type, String parameter,
 			boolean partial) throws LDAPObjectNotFoundException {
 
 		NamingEnumeration<SearchResult> searchResults = initialiseContext(type,
 				parameter, "people", partial);
 
 		List<LDAPUser> users = new LinkedList<LDAPUser>();
 		try {
 			while (searchResults.hasMore()) {
 				users.add(initLDAPUser(searchResults.next().getAttributes()));
 			}
 		} catch (NamingException e) {
 			log.warn(
 					"Naming exception when processing NamingEnumeration in multipleUserQuery",
 					e);
 			return new LinkedList<LDAPUser>();
 		}
 
 		return users;
 
 	}
 
 	/**
 	 * Unique group query Returns an LDAPGroup object Takes 2 arguments:
 	 * attribute to search (eg. uid) and parameter to search with
 	 * 
 	 * @return LDAPUser
 	 */
 	static LDAPGroup uniqueGroupQuery(String type, String parameter)
 			throws LDAPObjectNotFoundException {
 
 		Attributes groupResult = setupUniqueQuery(type, parameter, "groups");
 
 		if (groupResult == null) {
 			throw new LDAPObjectNotFoundException("Group not found");
 		}
 
 		return initLDAPGroup(groupResult);
 	}
 
 	/**
 	 * Multiple group query Returns a list of LDAPGroup objects Takes 3
 	 * arguments: attribute to search (eg. uid), parameter to search with and
 	 * whether partial
 	 * 
 	 * @return LDAPUser
 	 */
 	static List<LDAPGroup> multipleGroupQuery(String type, String parameter,
 			boolean partial) throws LDAPObjectNotFoundException {
 
 		NamingEnumeration<SearchResult> searchResults = initialiseContext(type,
 				parameter, "groups", partial);
 
 		try {
 			List<LDAPGroup> groups = new LinkedList<LDAPGroup>();
 			while (searchResults.hasMore()) {
 				try {
 					LDAPGroup g = initLDAPGroup(searchResults.next()
 							.getAttributes());
 					groups.add(g);
 				} catch (LDAPObjectNotFoundException e) {
 					// I think we do this because some groups might not be
 					// visible
 					// TODO: just update the search to only include visible
 					// groups
 					// don't add the group to the list
 					// log.debug(e.getMessage);
 				}
 			}
 			return groups;
 		} catch (NamingException e) {
 			log.warn("Naming exception in multipleGroupQuery", e);
 			return new LinkedList<LDAPGroup>();
 		}
 	}
 
 	private static String getString(Attributes userResult, String name) {
 		Attribute a = userResult.get(name);
 		if (a != null) {
 			try {
 				return a.get().toString();
 			} catch (NamingException e) {
 				log.warn("Naming exception looking for LDAP result", e);
 			}
 		}
 		return null;
 	}
 
 	private static List<String> getStringList(Attributes userResult, String name) {
 		Attribute a = userResult.get(name);
 		if (a != null) {
 			try {
 				NamingEnumeration<?> instIDEnum = a.getAll();
 				LinkedList<String> instID = new LinkedList<String>();
 				while (instIDEnum.hasMore()) {
					Object v = instIDEnum.next();
					if (v instanceof byte[]) {
						instID.add(new String(Base64.encodeBase64((byte[])v)));
					}
					else {
						instID.add(v.toString());
					}
 				}
 				return instID;
 			} catch (NamingException e) {
 				log.warn("Naming exception looking for LDAP result", e);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Creates an LDAPUser object from attributes
 	 * 
 	 * @return LDAPUser
 	 */
 	private static LDAPUser initLDAPUser(Attributes userResult)
 			throws LDAPObjectNotFoundException {
 
 		String crsid = getString(userResult, "uid");
 
 		if (crsid == null) { // If uid is null the user does not exist
 			log.warn("Asked to initialise a user but no uid found");
 			throw new LDAPObjectNotFoundException("User does not exist");
 		}
 
 		String cn = getString(userResult, "cn");
 		String dn = getString(userResult, "displayName");
 		String sn = getString(userResult, "sn");
 		String mail = getString(userResult, "mail");
 		List<String> instID = getStringList(userResult, "instID");
 		List<String> misAff = getStringList(userResult, "misAffiliation");
 		List<String> institutions = getStringList(userResult, "ou");
 		List<String> photos = getStringList(userResult, "jpegPhoto");
 
 		return new LDAPUser(crsid, cn, dn, sn, mail, instID, misAff,
 				institutions, photos);
 
 	}
 
 	/**
 	 * Creates an LDAPGroup object from attributes
 	 * 
 	 * @return LDAPGroup
 	 */
 	private static LDAPGroup initLDAPGroup(Attributes groupResult)
 			throws LDAPObjectNotFoundException {
 
 		String visibility = getString(groupResult, "visibility");
 		if (!visibility.equals("cam")) {
 			throw new LDAPObjectNotFoundException("Group not publicly visible");
 		}
 
 		String groupID = getString(groupResult, "groupID");
 		if (groupID == null) {
 			throw new LDAPObjectNotFoundException("Group does not exist");
 		}
 
 		String groupTitle = getString(groupResult, "groupTitle");
 		String description = getString(groupResult, "description");
 		List<String> users = getStringList(groupResult, "uid");
 
 		return new LDAPGroup(groupID, groupTitle, description, users);
 
 	}
 
 }
