 /**
  * Matt Drees
  * 10/31/05
  */
 package org.alt60m.gcx;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.*;
 import java.util.*;
 
 import org.alt60m.cas.CASURLConnection;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Basic class useful for communicating with the authz-admin GCX interface.
  * 
  * 
  * 
  * @author matthew.drees
  * 
  */
 public class CommunityAdminInterface {
 
 	//TODO: get from web.xml parameter
	private static String CAS_SIGNIN_URL = "https://signin.ccci.org/cas/login";
 	
 	private static String GCX_AUTHORIZATION_ADMIN_USERNAME = "campus.admin@mygcx.org";
 
 	private static String GCX_AUTHORIZATION_ADMIN_PASSWORD = "BeThouMyVision12";
 
 	private static String GCX_AUTHORIZATION_ADMIN_URL = "https://www.mygcx.org/CampusStaff/authz-admin/";
 
 	private static String GCX_AUTHORIZATION_ADMIN_SERVICENAME = "https://www.mygcx.org/CampusStaff/authz-admin";
 	//maybe make "community" a constant, too?
 	
 	//TODO: There's got to be a better way to tell if we've timed out...
 // want to catch this (for now; in future, may need to change):
 //
 //	<html>
 //	<head>
 //	<title>GCX Authentication Gateway</title>
 //	 <script>
 //	  window.location.href="http://www.mygcx.org/authz-admin/campus?admin_commands=%3C%3Fxml+version+%3D+%221.0%22%3F%3E+%3C%21DOCTYPE+admin_commands+SYSTEM+%22http%3A%2F%2Fgcxapp.mygcx.org%2Fdtd%2Fadmin_commands.dtd%22%3E+%3Cadmin_commands%3E+%3Ccommand%3E+%3ClistGroupMembers%3E%3Cgroup%3Ecampus%3A_OWNER%3C%2Fgroup%3E%3C%2FlistGroupMembers%3E+%3C%2Fcommand%3E%3C%2Fadmin_commands%3E";
 //	 </script>
 //	</head>
 //
 //	<body bgcolor="#0044AA">
 //	 <noscript>
 //	  <p>
 //	   Click <a href="http://www.mygcx.org/authz-admin/campus?admin_commands=%3C%3Fxml+version+%3D+%221.0%22%3F%3E+%3C%21DOCTYPE+admin_commands+SYSTEM+%22http%3A%2F%2Fgcxapp.mygcx.org%2Fdtd%2Fadmin_commands.dtd%22%3E+%3Cadmin_commands%3E+%3Ccommand%3E+%3ClistGroupMembers%3E%3Cgroup%3Ecampus%3A_OWNER%3C%2Fgroup%3E%3C%2FlistGroupMembers%3E+%3C%2Fcommand%3E%3C%2Fadmin_commands%3E">here</a>
 //	   to access the service you requested.
 //	  </p>
 //	 </noscript>
 //	</body>
 //
 //	</html>
 	
 
 //  or this....
 //	<html>
 //	<head>
 //	<title>GCX Account :: Sign In</title>
 //	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
 //	<style>
 //  ...
 
 	
 	private static final Pattern NOT_LOGGED_IN_PATTERN = Pattern.compile(
 				"<\\s*html\\s*>.*<\\s*title\\s*>.*" 
 				+ "GCX" 
 				+ ".*<\\s*/title\\s*>.*<\\s*/html\\s*>", Pattern.DOTALL);
 	
 
 	private static Log log = LogFactory.getLog(CommunityAdminInterface.class);
 	
 	private String community;
 	
 	private String myLastError;
 	
 	private String myLastMessage;
 	
 	private String myLastCode;
 	
 	private static CASURLConnection conn;
 	
 	public static final String dtdLocation = "http://gcxapp.mygcx.org/dtd/admin_commands.dtd";
 	
 	private static void log(String message)
 	{
 		log.info(message);
 	}
 	
 	public CommunityAdminInterface(String community) throws IOException,
 			CommunityAdminInterfaceException {
 		if (conn == null) {
 			log("Creating CASURLConnection");
 			conn = new CASURLConnection(CAS_SIGNIN_URL,
 					GCX_AUTHORIZATION_ADMIN_USERNAME,
 					GCX_AUTHORIZATION_ADMIN_PASSWORD);
 
 			log("Initial Login...");
 			String result = conn.logIn(GCX_AUTHORIZATION_ADMIN_URL + community, GCX_AUTHORIZATION_ADMIN_SERVICENAME + "/" + community, null);
 			log("Initial Login "
 					+ (result != null && !notLoggedIn(result) ? "succeded."
 							: "failed."));
 			if (result == null) {
 				throw new CommunityAdminInterfaceException("Can't login:"
 						+ conn.getError());
 			}
 		}
 		this.community = community;
 	}
 	
 	
 	/**
 	 * Testing function.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			CommunityAdminInterface cai = new CommunityAdminInterface("campus");
 			test1(cai);
 			//test2(cai);
 			
 		} catch (IOException e) {
 			
 			log.debug("IOException", e);
 		} catch (CommunityAdminInterfaceException e) {
 			
 			log.error("CommunityAdminInterfaceException", e);
 		}
 	}
 	
 	private static boolean test1(CommunityAdminInterface cai)
 			throws IOException, CommunityAdminInterfaceException {
 
 		String guid = "BB20A5DB-D31E-65B5-3629-E24504A00942";
 		if (!cai.addToGroup(guid, "OWNER")) {
 			log.debug(cai.getError());
 			log.debug("User not added");
 		} else {
 			log.debug("Success!");
 		}
 
 		Collection<String> userGuids = cai.listUsers();
 		if (userGuids != null) {
 			log.debug("Success!");
 			for (String user : userGuids) {
 				log.debug(user);
 			}
 		} else {
 			log.debug("Failure!");
 		}
 		
 		
 		return true;
 	}
 	
 	private static boolean test2(CommunityAdminInterface cai)
 	throws IOException, CommunityAdminInterfaceException 
 	{
 		String guid = "BB20A5DB-D31E-65B5-3629-E24504A00942";
 	
 
 		Collection<String> groups;
 		if ((groups = cai.listGroups()) == null)
 		{
 			log.debug("Failure; " + cai.getError());
 			return false;
 		}
 		else
 		{
 			log.debug("Groups:");
 			for (String group : groups)
 			{
 				log.debug(group);
 			}
 		}
 		
 		Collection<String> roles;
 		if ((roles = cai.listRoles()) == null)
 		{
 			log.debug("Failure; " + cai.getError());
 			return false;
 		}
 		else
 		{
 			log.debug("Roles:");
 			for (String role : roles)
 			{
 				log.debug(role);
 			}
 		}
 		
 		Collection<String> resources;
 		if ((resources = cai.listResources()) == null)
 		{
 			log.debug("Failure; " + cai.getError());
 			return false;
 		}
 		else
 		{
 			log.debug("Resources:");
 			for (String resource : resources)
 			{
 				log.debug(resource);
 			}
 		}
 		
 		Collection<String> myGroups;
 		if ((myGroups = cai.listContainingGroups(guid)) == null)
 		{
 			log.debug("Failure; " + cai.getError());
 			return false;
 		}
 		else
 		{
 			log.debug("My Groups:");
 			for (String group : myGroups)
 			{
 				log.debug(group);
 			}
 		}
 		
 		
 		Collection<String> perms;
 //		if ((perms = cai.listPermissions(guid)) == null)
 //		{
 //			log.debug("Failure; " + cai.getError());
 //			return false;
 //		}
 //		else
 //		{
 //			log.debug("Permissions:");
 //			for (String perm : perms)
 //			{
 //				log.debug(perm);
 //			}
 //		}
 		
 		if (!groups.contains("campus:_TEST") && !cai.addGroup("campus:_TEST"))
 		{
 			log.debug("Failure; " + cai.getError());
 			return false;
 		}
 		
 		if (!cai.addToGroup(guid, "TEST"))
 		{
 			log.debug("Failure; " + cai.getError());
 			return false;
 		}
 		if (!resources.contains("GCX:www.mygcx.org:campus:testResource") && !cai.addResource("GCX:www.mygcx.org:campus:testResource"))
 		{
 			log.debug("Failure; " + cai.getError());
 			return false;
 		}
 		if (!roles.contains("campus:_TEST_ROLE") && cai.addRole("campus:_TEST_ROLE"))
 		{
 			log.debug("Failure; " + cai.getError());
 			return false;
 		}
 		
 //		if ((groups = cai.listGroups()) == null)
 //		{
 //			log.debug("Failure; " + cai.getError());
 //			return false;
 //		}
 //		else
 //		{
 //			log.debug("Groups:");
 //			for (String group : groups)
 //			{
 //				log.debug(group);
 //			}
 //		}
 		
 //		if ((roles = cai.listRoles()) == null)
 //		{
 //			log.debug("Failure; " + cai.getError());
 //			return false;
 //		}
 //		else
 //		{
 //			log.debug("Roles:");
 //			for (String role : roles)
 //			{
 //				log.debug(role);
 //			}
 //		}
 		
 //		if ((resources = cai.listResources()) == null)
 //		{
 //			log.debug("Failure; " + cai.getError());
 //			return false;
 //		}
 //		else
 //		{
 //			log.debug("Resources:");
 //			for (String resource : resources)
 //			{
 //				log.debug(resource);
 //			}
 //		}
 		
 		if (!cai.addToRole("GCX:www.mygcx.org:campus:testResource", "TEST_ROLE"))
 		{
 			log.debug("Failure; " + cai.getError());
 			return false;
 		}
 		if (!cai.addPermission("campus:_TEST", "campus:_TEST_ROLE"))
 		{
 			log.debug("Failure; " + cai.getError());
 			return false;
 		}
 		
 		if ((perms = cai.listPermittedEntities("GCX:www.mygcx.org:campus:screen:admin")) == null)
 		{
 			log.debug("Failure; " + cai.getError());
 			return false;
 		}
 		else
 		{
 			log.debug("Permissions:");
 			for (String perm : perms)
 			{
 				log.debug(perm);
 			}
 		}
 		return true;
 	}
 	
 	/* ----- Admin-Authz functions ----- */
 
 	// TODO: synchronize?
 	// probably not
 	
 	//TODO:  do roles need a "community:_" prefix? 
 	//Yes.
 	
 	/**
 	 * Add a group.
 	 * 
 	 * @param group
 	 * @return true if successful, false otherwise.
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public boolean addGroup(String group) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Adding group " + group + " to community " + community);
 	
 		BasicAdminResponse resp = sendCommand("<addGroup><group>" + group
 				+ "</group></addGroup> ");
 		
 		return successful(resp);
 	}
 	
 
 	/**
 	 * @param resource
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public boolean addResource(String resource) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Adding resource " + resource + " to community " + community);
 	
 		BasicAdminResponse resp = sendCommand("<addResource><resource>" + resource
 				+ "</resource></addResource> ");
 		
 		return successful(resp);
 	}
 	
 
 	/**
 	 * @param role
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public boolean addRole(String role) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Adding role " + role + " to community " + community);
 	
 		BasicAdminResponse resp = sendCommand("<addRole><role>" + role
 				+ "</role></addRole> ");
 		
 		return successful(resp);
 	}
 	
 	/**
 	 * Add a user to the specified group.  
 	 * 
 	 * @param entity
 	 * @param group group probably should be "MEMBERS", "ADMINISTRATORS", or "OWNER"
 	 * @return true if successful, false otherwise.  
 	 */
 	public boolean addToGroup(String entity, String group) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Adding entity " + entity + " to group " + group);
 		
 		BasicAdminResponse resp = sendCommand("<addToGroup><entity>" + entity
 				+ "</entity> " + "<group>" + community + ":_" + group
 				+ "</group></addToGroup>");
 		
 		return successful(resp);
 	}
 	
 	/**
 	 * Add a target to the specified role.
 	 * @param target
 	 * @param role
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public boolean addToRole(String target, String role) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Adding target " + target + " to role " + role);
 		
 		BasicAdminResponse resp = sendCommand("<addToRole><target>" + target
 				+ "</target> " + "<role>" + community + ":_" + role
 				+ "</role></addToRole>");
 		
 		return successful(resp);
 	}
 	
 	/**
 	 * Permit an entity to the specified target
 	 * @param entity
 	 * @param target
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public boolean addPermission(String entity, String target) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Permitting entity " + entity + " to target " + target);
 		
 		BasicAdminResponse resp = sendCommand("<addPermission><entity>" + entity
 				+ "</entity> " + "<target>" + target
 				+ "</target></addPermission>");
 		
 		return successful(resp);
 	}
 	
 	/**
 	 * Check for the existence of the given object
 	 * 
 	 * @param objectType
 	 *            Should be "user", "group", "role", "resource", "target", or
 	 *            "entity"
 	 * @param objectName 
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public boolean exists(String objectType, String objectName)  throws IOException, CommunityAdminInterfaceException
 	{
 		log("Checking existence of " + objectName + " (of type " + objectType + ")");
 
 		BasicAdminResponse resp = sendCommand("<exists><" + objectType + ">" + objectName
 				+ "</" + objectType + ">" + "</exists>");
 		
 		return successful(resp);
 	}
 	
 	/**
 	 * Get a list of members
 	 * 
 	 * @return A Collection<String> holding guids of the community's members
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listUsers() throws IOException, CommunityAdminInterfaceException
 	{
 		return listUsers(null);
 	}
 	
 	/**
 	 * Get a list of members
 	 * 
 	 * @param filter The (optional) filter consists of a string of search text and
      a % wildcard that may prefix, suffix, or occur on both sides of the search text.
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listUsers(String filter) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Listing users in " + community);
 		
 		BasicAdminResponse resp = sendCommand("<listUsers" + (filter != null ? " filter = \"" + filter + "\"" : "") + "/>");
 		
 		return successful(resp) ? resp.getObjectList("user") : null;
 	}
 	
 
 	/**
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listGroups() throws IOException, CommunityAdminInterfaceException
 	{
 		return listGroups(null);
 	}
 	
 	/**
 	 * @param filter
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listGroups(String filter) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Listing groups in " + community);
 		
 		BasicAdminResponse resp = sendCommand("<listGroups" + (filter != null ? " filter = \"" + filter + "\"" : "") + "/>");
 		
 		return successful(resp) ? resp.getObjectList("group") : null;
 	}
 	
 	/**
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listResources() throws IOException, CommunityAdminInterfaceException
 	{
 		return listResources(null);
 	}
 	
 	/**
 	 * @param filter
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listResources(String filter) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Listing resources in " + community);
 		
 		BasicAdminResponse resp = sendCommand("<listResources" + (filter != null ? " filter = \"" + filter + "\"" : "") + "/>");
 		
 		return successful(resp) ? resp.getObjectList("resource") : null;
 	}
 	
 	/**
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listRoles() throws IOException, CommunityAdminInterfaceException
 	{
 		return listRoles(null);
 	}
 	
 	/**
 	 * @param filter
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listRoles(String filter) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Listing roles in " + community);
 		
 		BasicAdminResponse resp = sendCommand("<listRoles" + (filter != null ? " filter = \"" + filter + "\"" : "") + "/>");
 		
 		return successful(resp) ? resp.getObjectList("role") : null;
 	}
 	
 	/**
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listEntities() throws IOException, CommunityAdminInterfaceException
 	{
 		return listEntities(null);
 	}
 	
 	/**
 	 * @param filter
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listEntities(String filter) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Listing entities in " + community);
 		
 		BasicAdminResponse resp = sendCommand("<listEntities" + (filter != null ? " filter = \"" + filter + "\"" : "") + "/>");
 		
 		return successful(resp) ? resp.getObjectList("entity") : null;
 	}
 	
 	
 	/**
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listTargets() throws IOException, CommunityAdminInterfaceException
 	{
 		return listTargets(null);
 	}
 	
 	/**
 	 * @param filter
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listTargets(String filter) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Listing targets in " + community);
 		
 		BasicAdminResponse resp = sendCommand("<listTargets" + (filter != null ? " filter = \"" + filter + "\"" : "") + "/>");
 		
 		return successful(resp) ? resp.getObjectList("targets") : null;
 	}
 	
 	
 	
 	/**
 	 * @param group
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listGroupMembers(String group) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Listing members of " + group + " (in " + community + ")");
 		
 		BasicAdminResponse resp = sendCommand("<listGroupMembers><group>"
 				+ group + "</group></listGroupMembers>");
 		
 		return successful(resp) ? resp.getObjectList("entity") : null;
 	}
 	
 	/**
 	 * @param role
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listRoleTargets(String role) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Listing targets of " + role + " (in " + community + ")");
 
 		BasicAdminResponse resp = sendCommand("<listRoleTargets><role>"
 				+ role + "</role></listRoleTargets>");
 		
 		return successful(resp) ? resp.getObjectList("target") : null;
 	}
 	
 	
 	/**
 	 * @param entity
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listContainingGroups(String entity) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Listing containing groups of " + entity+ " (in " + community + ")");
 
 		BasicAdminResponse resp = sendCommand("<listContainingGroups><entity>"
 				+ entity + "</entity></listContainingGroups>");
 		
 		return successful(resp) ? resp.getObjectList("group") : null;
 	}
 	
 	/**
 	 * @param target
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listContainingRoles(String target) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Listing containing roles of " + target+ " (in " + community + ")");
 
 		BasicAdminResponse resp = sendCommand("<listContainingRoles><target>"
 				+ target + "</target></listContainingRoles>");
 		
 		return successful(resp) ? resp.getObjectList("role") : null;
 	}
 
 	
 	/**
 	 * @param target
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listPermittedEntities(String target) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Listing entities with permission to " + target+ " (in " + community + ")");
 
 		BasicAdminResponse resp = sendCommand("<listPermittedEntities><target>"
 				+ target + "</target></listPermittedEntities>");
 		
 		return successful(resp) ? resp.getObjectList("entity") : null;
 	}
 	
 	/**
 	 * Get a list of permissions for an entity
 	 * 
 	 * @return A Collection<String> holding the names of the targets the entity
 	 *  has permissions for.
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public Collection<String> listPermissions(String entity) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Listing permissions for " + entity + " (in community " + community + ")");
 		
 		BasicAdminResponse resp = sendCommand("<listPermissions><entity>" + entity
 				+ "</entity></listPermissions>");
 		
 		return successful(resp) ? resp.getObjectList("target") : null;
 	}
 	
 	
 	/**
 	 * Remove a group.
 	 * 
 	 * @param group
 	 * @return true if successful, false otherwise.
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public boolean removeGroup(String group) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Removing group " + group + " from community " + community);
 	
 		BasicAdminResponse resp = sendCommand("<removeGroup><group>" + group
 				+ "</group></removeGroup> ");
 		
 		return successful(resp);
 	}
 	
 
 	/**
 	 * @param resource
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public boolean removeResource(String resource) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Removing resource " + resource + " from community " + community);
 	
 		BasicAdminResponse resp = sendCommand("<removeResource><resource>" + resource
 				+ "</resource></removeResource> ");
 		
 		return successful(resp);
 	}
 	
 
 	/**
 	 * @param role
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public boolean removeRole(String role) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Removing role " + role + " from community " + community);
 	
 		BasicAdminResponse resp = sendCommand("<removeRole><role>" + role
 				+ "</role></removeRole> ");
 		
 		return successful(resp);
 	}
 	
 	/**
 	 * Remove a user from the specified group.  
 	 * 
 	 * @param entity
 	 * @param group group probably should be "MEMBERS", "ADMINISTRATORS", or "OWNER"
 	 * @return true if successful, false otherwise.  
 	 */
 	public boolean removeFromGroup(String entity, String group) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Removing entity " + entity + " from group " + group);
 		
 		BasicAdminResponse resp = sendCommand("<removeFromGroup><entity>" + entity
 				+ "</entity> " + "<group>" + community + ":_" + group
 				+ "</group></removeFromGroup>");
 		
 		return successful(resp);
 	}
 	
 	/**
 	 * Remove a target from the specified role.
 	 * @param target
 	 * @param role
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public boolean removeFromRole(String target, String role) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Removing target " + target + " from role " + role);
 		
 		BasicAdminResponse resp = sendCommand("<removeFromRole><target>" + target
 				+ "</target> " + "<role>" + community + ":_" + role
 				+ "</role></removeFromRole>");
 		
 		return successful(resp);
 	}
 	
 	/**
 	 * Deny permission for the entity to the specified target
 	 * @param entity
 	 * @param target
 	 * @return
 	 * @throws IOException
 	 * @throws CommunityAdminInterfaceException
 	 */
 	public boolean removePermission(String entity, String target) throws IOException, CommunityAdminInterfaceException
 	{
 		log("Removing permission for entity " + entity + " to target " + target);
 		
 		BasicAdminResponse resp = sendCommand("<removePermission><entity>" + entity
 				+ "</entity> " + "<target>" + target
 				+ "</target></removePermission>");
 		
 		return successful(resp);
 	}
 	
 	/* ----- helper methods ----- */
 	
 	
 	private boolean successful(BasicAdminResponse resp)
 	{
 		myLastCode = resp.getCode();
 		
 		if (!resp.isValid())
 		{
 			myLastError = "Invalid response received! Response = " + resp.getContent();
 			return false;
 		}
 		if (resp.isError())
 		{
 			myLastError = "Error(" + resp.getCode() + "): " + resp.getErrorMessage();
 			return false;
 		}
 		if (resp.isWarning())
 		{
 			myLastError = "Warning(" + resp.getCode() + "): " + resp.getResponseMessage();
 			return false;
 		}
 
 		myLastMessage = resp.getResponseMessage();
 
 		return true;
 	}
 	
 	private BasicAdminResponse sendCommand(String command) throws IOException, CommunityAdminInterfaceException
 	{
 		String request = "<?xml version = \"1.0\"?> " +
 		"<!DOCTYPE admin_commands SYSTEM \"" + dtdLocation + "\"> "
 				+ "<admin_commands> <command> "
 				+ command
 				+ "</command></admin_commands>";
 		
 		Map<String, String> paramList = new HashMap<String, String>();
 		paramList.put("admin_commands", request);
 		
 		String content = conn.sendRequest(paramList);
 		
 		if (content == null)
 			throw new CommunityAdminInterfaceException("Can't login: " + conn.getError());
 		
 		if (notLoggedIn(content)) 
 		{
 			log("Not logged in; Logging in ...");
 			String result = conn.logIn(GCX_AUTHORIZATION_ADMIN_URL + community, GCX_AUTHORIZATION_ADMIN_SERVICENAME + "/" + community, null);
 			log("Login " + (result != null && !notLoggedIn(result)  ? "succeded." : "failed."));
 			if (result == null)
 			{
 				throw new CommunityAdminInterfaceException("Can't login: " + conn.getError());
 			}
 			content = conn.sendRequest(paramList);
 			
 			if (content == null)
 				throw new CommunityAdminInterfaceException("Can't login: " + conn.getError());
 			
 			if (notLoggedIn(content)) 
 			{
 				myLastError = "Re-login failed!";
 				return null;
 			}
 		}
 		return new BasicAdminResponse(content);
 	}
 	
 	private static boolean notLoggedIn(String content)
 	{
 		Matcher loggedOutMatcher = NOT_LOGGED_IN_PATTERN.matcher(content);
 		return loggedOutMatcher.find();
 	}
 	
 	/**
 	 * @return the most recent error message
 	 */
 	public String getError()
 	{
 		return myLastError;
 	}
 	
 	/**
 	 * @return the most recent response code
 	 */
 	public String getCode()
 	{
 		return myLastCode;
 	}
 	
 	/**
 	 * @return the most recent message
 	 */
 	public String getMessage()
 	{
 		return myLastMessage;
 	}
 }
 
 //for now, use a simple approach.  Maybe in the future we'll need a SAX parser
 //or something.
 class BasicAdminResponse
  {
 	private static final Pattern errorPattern = Pattern.compile(
 			"<\\s*error\\s+code\\s*=\\s*\"(.*)\">(.*)</error>", Pattern.DOTALL);
 	
 	private static final Pattern responsePattern = Pattern.compile(
 			"<\\s*response\\s+code\\s*=\\s*\"(.*)\"\\s*>(.*)</response>", Pattern.DOTALL);
 	
 	private String content;
 	
 	private boolean isError;
 	
 	private boolean isValid;
 	
 	private String code;
 	
 	private String errorMessage;
 
 	private String responseMessage;
 
 	
 	public BasicAdminResponse(String content)
 	{
 		this.content = content;
 		Matcher errorMatcher;
 		Matcher responseMatcher = responsePattern.matcher(content);
 		if (responseMatcher.find()) {
 			isValid = true;
 			isError = false;
 			code = responseMatcher.group(1).trim();
 			responseMessage = responseMatcher.group(2).trim();
 		}
 		else
 		{
 			errorMatcher = errorPattern.matcher(content);
 			if (errorMatcher.find())
 			{
 				isValid = true;
 				isError = true;
 				code = errorMatcher.group(1).trim();
 				errorMessage = errorMatcher.group(2).trim();
 			}
 		}
 	}
 	
 	public String getContent()
 	{
 		return content;
 	}
 
 	public Collection<String> getObjectList(String objectName)
 	{
 		//assumes that object info printed on a single line
 		Pattern userPattern = Pattern.compile("<\\s*" + objectName
 				+ "\\s*>(.*)<\\s*/\\s*" + objectName + "\\s*>");
 		
 		Collection<String> objects = new Vector<String>();
 		Matcher userMatcher = userPattern.matcher(content);
 		
 		String object = null;
 		while (userMatcher.find())
 		{
 			object = userMatcher.group(1).trim();
 			objects.add(object);
 			debug("adding object " + object + " to collection");
 		}
 		
 		return objects;
 	}
 	
 	public String getErrorMessage()
 	{
 		return errorMessage;
 	}
 	
 	public String getResponseMessage()
 	{
 		return responseMessage;
 	}
 	
 	public String getCode()
 	{
 		return code;
 	}
 	
 	public boolean isError()
 	{
 		return isError;
 	}
 	
 	public boolean isValid()
 	{
 		return isValid;
 	}
 
 	public boolean isWarning()
 	{
 		return code.startsWith("3");
 	}
 	
 	private static void log(String msg)
 	{
 		//log.debug("BasicAdminResponse: " + msg);
 	}
 	
 	private static void debug(String msg)
 	{
 		log(msg);
 	}
 }
