 package uk.ac.cam.dashboard.util;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 
 import org.apache.commons.codec.binary.Base64;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableMap;
 
 public class LDAPProvider {
 
 	//Logger
 	private static Logger log = LoggerFactory.getLogger(LDAPProvider.class);
 	
 	/**
 	 * Sets up basic LDAP Query
 	 * subtree is the subtree under o (Cam uni) to search
 	 * possible subtrees: people, groups, institutions 
 	 * @return Attributes
 	 */
 	public static Hashtable setupQuery() {
 		
 		Hashtable env = new Hashtable();
 		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 		env.put(Context.PROVIDER_URL, "ldap://ldap.lookup.cam.ac.uk:389");
 		
 		return env;
 	}
 	
 	/**
 	 * Unique result query
 	 * Constructs and calls final query, returning a single string result 
 	 * Takes 4 arguments: attribute to search (eg. uid), parameter to search with, 
 	 * attribute to return as result and subtree to search
 	 * Possible subtrees to search: people, groups, institutions
 	 * @return String result
 	 */
 	public static String uniqueQuery(String type, String parameter, String result, String subtree) {
 		
 		Hashtable env = setupQuery();
 
 		Attributes a = null;
 		try {
 			DirContext ctx = new InitialDirContext(env);
 			SearchControls controls = new SearchControls();
 			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 			SearchResult searchResult = ctx.search(
 					"ou="+ subtree+",o=University of Cambridge,dc=cam,dc=ac,dc=uk",
 					"("+type+"=" + parameter + ")", controls).next();
 			a = searchResult.getAttributes();
 		} catch (NullPointerException e) {
 			log.debug(e.getMessage() + " : no result for " +subtree+" "+ result +" query of " +type+" with parameter "+ parameter);
 			return null;
 		} catch (Exception e){
 			log.debug(e.getMessage() + " : error querying " +subtree+" for "+ result +" using " +type+":"+ parameter);			
 			
 		}
 		
 		//If no match in search
 		if(a==null){ return null;}
 		
 		try {
         	return a.get(result).get().toString();
         } catch (NamingException e) {
 			log.error(e.getMessage());
 			return null;
 		} catch (NullPointerException e) {
 			log.error(e.getMessage());
 			return null;
 		}
 		
 	}
 	
 	/**
 	 * List of results query
 	 * Constructs and calls final query, returning a list of strings 
 	 * Takes 4 arguments: attribute to search (eg. uid), parameter to search with, 
 	 * attribute to return as result and subtree to search
 	 * Possible subtrees to search: people, groups, institutions
 	 * @return List<String> result
 	 */
 	public static List<String> multipleQuery(String type, String parameter, String result, String subtree) {
 		
 		Hashtable env = setupQuery();
 
 		Attributes a = null;
 		try {
 			DirContext ctx = new InitialDirContext(env);
 			SearchControls controls = new SearchControls();
 			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 			SearchResult searchResult = ctx.search(
 					"ou="+ subtree+",o=University of Cambridge,dc=cam,dc=ac,dc=uk",
 					"("+type+"=" + parameter + ")", controls).next();
 			a = searchResult.getAttributes();
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			return null;
 		}
 		
 		//If no match in search
 		if(a==null){ return null;}
 		
 		ArrayList<String> listResults = new ArrayList<String>();
 		
 		try {
 			//Initialise enumResults
 			NamingEnumeration enumResults;
 			
 			//If no results null
 			try {
 			 enumResults = a.get(result).getAll();
 			} catch (NullPointerException e){
 				log.debug("No " + result + " found");
 				return null;
 			}
 			
 			// For a photo need to do something weird
 			if(result.equals("jpegPhoto")){
 				try {
 					while(enumResults.hasMore()){
 						byte[] p = (byte[])enumResults.next();
 						listResults.add(new String(Base64.encodeBase64(p)));	
 					}
 					return listResults;
 				} catch (NamingException e){
 					log.error(e.getMessage());
 					return null;
 				} 
 			}
 			
 			// Convert enumeration type results to string otherwise
 				while(enumResults.hasMore()){
 					listResults.add(enumResults.next().toString());
 				}
 					
         } catch (NamingException e) {
 			log.error(e.getMessage());
 			return null;
 		} 
 		
 		return listResults;
 		
 	}
 
 	/**
 	 * Partial User Query
 	 * Constructs and calls final query, returning immutable map of crsid, displayname, surname
 	 * Includes partial matches in search
 	 * Takes 2 arguments: attribute to search (eg. uid) and, string x to match results with
 	 * Possible subtrees to search: people, groups, institutions
 	 * @return List<ImmutableMap<String,?>>
 	 */
	public static List<ImmutableMap<String,?>> partialUserQuery(String x, String type){
 		
 		Hashtable env = setupQuery();
 		NamingEnumeration<SearchResult> enumResults;
 		
 		Attributes a = null;
 		try {
 			DirContext ctx = new InitialDirContext(env);
 			SearchControls controls = new SearchControls();
 			controls.setReturningAttributes(new String[]{"uid", "displayName", "sn"});
 			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 			enumResults = ctx.search(
 					"ou=people,o=University of Cambridge,dc=cam,dc=ac,dc=uk",
 					"("+type+"=" + x + "*)", controls);
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			return null;
 		}
 		
 		try {
 			ArrayList<ImmutableMap<String,?>> userMatches = new ArrayList<ImmutableMap<String,?>>();			
 			
 			// Convert enumeration type results to string
 				while(enumResults.hasMore()){
 					Attributes result = enumResults.next().getAttributes();
 					userMatches.add(ImmutableMap.of("crsid", result.get("uid").get().toString(), "name", result.get("displayName").get().toString(), "surname", result.get("sn").get().toString()));
 				}
 				
 			return userMatches;
 					
         } catch (NamingException e) {
 			log.error(e.getMessage());
 			return null;
 		} 
 		
 	}
 	
 	/**
 	 * Partial Group Query
 	 * Constructs and calls final query, returning immutable map of crsid, displayname, surname
 	 * Includes partial matches in search
 	 * Takes 2 arguments: attribute to search (eg. uid) and, string x to match results with
 	 * Possible subtrees to search: people, groups, institutions
 	 * @return List<ImmutableMap<String,?>>
 	 */
 	public static List partialGroupQuery(String x, String type){
 		
 		System.out.println("Searching " + type + " for " + x);
 		
 		Hashtable env = setupQuery();
 		NamingEnumeration<SearchResult> enumResults;
 		
 		Attributes a = null;
 		try {
 			DirContext ctx = new InitialDirContext(env);
 			SearchControls controls = new SearchControls();
 			controls.setReturningAttributes(new String[]{"groupID", "groupTitle", "description", "visibility"});
 			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 			enumResults = ctx.search(
 					"ou=groups,o=University of Cambridge,dc=cam,dc=ac,dc=uk",
 					"("+type+"=*" + x + "*)", controls);
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			return null;
 		}
 		
 		try {
 			ArrayList<ImmutableMap<String,?>> groupMatches = new ArrayList<ImmutableMap<String,?>>();			
 			
 			// Convert enumeration type results to string
 				while(enumResults.hasMore()){
 					Attributes result = enumResults.next().getAttributes();
 					// only add if members are public
 					if(result.get("visibility").get().toString().equals("cam")){
 						//dont add if the group is larger than 50 people
 						groupMatches.add(ImmutableMap.of("id", result.get("groupID").get().toString(), "name", result.get("groupTitle").get().toString(), "description", result.get("description").get().toString()));
 					} 
 				}
 				
 			return groupMatches;
 					
         } catch (NamingException e) {
 			log.error(e.getMessage());
 			return null;
 		} 
 		
 	}
 
 }
