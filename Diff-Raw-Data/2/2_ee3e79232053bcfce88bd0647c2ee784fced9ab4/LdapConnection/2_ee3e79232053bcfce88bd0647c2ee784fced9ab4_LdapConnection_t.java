 package ca.usask.ocd.ldap;
 
 //import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 import java.io.IOException;
 
 import javax.naming.CommunicationException;
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.NoInitialContextException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 
 import org.apache.log4j.Logger;
 
 public class LdapConnection 
 {
 //	private static Logger logger = Logger.getLogger( LdapConnection.class );
 	private static LdapConnection singleton;
 	private static boolean connectionError;
 	/**	the reference to the connection info stored in the context */ 
 	private static DirContext ctx;
 	private static Properties env; 
 	final static String ldapServerName = "ldap.usask.ca";
 	static String nsid = "abv641";
 	private int retries = 0;
 	static long start=0;
 	private static Logger logger = Logger.getLogger( LdapConnection.class );
 	
 	
 	public static LdapConnection instance() throws Exception
 	{
 		if(singleton == null)
 		{
 			singleton = new LdapConnection();
 			singleton.initConnection();
 		}
 		return singleton;
 	}
 	public LdapConnection()
 	{
 		if(singleton==null)
 		{
 			logger.info("In LdapConnection contructor, should only happen when tomcat or context is restarted.");
 			retries=0;
 			connectionError=true;
 			java.util.ResourceBundle bundle =java.util.ResourceBundle.getBundle("ldapuser");
 			String ldapuser=bundle.getString("user");
 			String ldappass=bundle.getString("password");
 			String ldapServerName=bundle.getString("serverName");
 			
 			//for testing, use the ldapuserid for the memberOf query as well (comment next line to disable)
 			//nsid=ldapuser;
 			
 			// set up environment to access the server
 			env = new Properties();
 			String connDN = "uid="+ldapuser+",ou=nsids,ou=people,dc=usask,dc=ca";
 			
 			env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
 			env.put( Context.PROVIDER_URL, "ldaps://"+ldapServerName);//+":636/");
 			env.put(Context.SECURITY_PROTOCOL, "ssl");
 			env.put( Context.SECURITY_PRINCIPAL, connDN );
 			env.put( Context.SECURITY_CREDENTIALS, ldappass );
 			env.put(Context.SECURITY_AUTHENTICATION, "simple");
 			env.put("com.sun.jndi.ldap.read.timeout", "10000");
 			env.put("com.sun.jndi.ldap.connect.timeout","5000");
 			logger.debug("Singleton is null, initConnection!");
 			try
 			{
 				initConnection();
 				logger.debug("initConnection complete!");
 			}
 			catch(Exception e)
 			{
 				logger.fatal("LDAP connection failure (tried 5 times)");
 			}
 		}
 		else
 			logger.debug("Singleton not null, no need to init Connection");
 	}
 	
 	public boolean hasErrors()
 	{
 		return connectionError;
 	}
 	
 	/** sets up the connection to the ldap server
 	 * Any Exceptions that occur are logged using log4j */
 	public void initConnection() throws Exception
 	{
 		try 
 		{
 				// obtain initial directory context using the environment
 				ctx = new InitialDirContext( env );
 				connectionError=false;
 		}
 		catch(javax.naming.CommunicationException e)
 		{					
 			logger.error("Oops, creating ldap connection problem",e);
 		}
 		catch(Exception e)
 		{
 			boolean retry=false;
 			if(e instanceof NoInitialContextException )
 			{
 				singleton=new LdapConnection();
 				retry=true;
 			}
 			logger.error("Oops, creating ldap connection problem",e);
 			String message=e.getMessage();
 			//connection has gone away!
 			if(message.startsWith("LDAP response read timed out"))
 			{
 				retry=true;
 			
 			}
 			if(retry)
 			{
 				//try to reconnect
 				connectionError=true;
 				try{Thread.sleep(500);}catch(Exception te){}
 				
 				retries++;
 				logger.error("LDAP Connection error, trying again");
 				if(retries<5)
 				{
 					try
 					{
 						initConnection();
 					}
 					catch(Exception ee)
 					{
 						logger.fatal("LDAP connection failure (tried 5 times)");
 					}
 				}
 				else
 				{
 					retries=0;
 					logger.error("unresolveable error!",e);
 					throw new Exception("UNABLE TO CONNECT TO LDAP SERVER");
 				}
 			}
 		}
 	}
 	
 	
 	/** closes open connection, should really only be used from a stand alone app, or from finalize
 	 * any Exceptions are logged using log4j*/
 	public void closeConnection()
 	{
 
 		try 
 		{
 				ctx.close();
 				logger.debug("Closing ldap connection!");
 		}
 		catch(javax.naming.CommunicationException e)
 		{					
 			logger.error("Oops, closing ldap connection problem",e);
 		}
 		catch (NamingException e)
 		{
 			logger.error("Oops, closing ldap connection problem",e);
 			
 		}
 		catch(NullPointerException e)
 		{
 			logger.error("Oops, closing ldap connection problem",e);
 		}
 
 	}
 	public ArrayList<String> getTeachingGroups(String user) throws Exception
 	{		
 		ArrayList<String> groups=new ArrayList<String>();
 		SearchControls constraints = new SearchControls();
 		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
 		
 		//this limits the list of attributes that the LDAP query will return
 		//NOTE: operational attributes like MemberOf only appear if requested specifically
 		String[] attrIDs = {"memberOf"};
 		constraints.setReturningAttributes(attrIDs);
 		//you can also limit search results to a specified number
 		//constraints.setCountLimit(10);
 		
 		
 		String searchStringGroups="ou=people,dc=usask,dc=ca";
 		String searchStringData="(&(objectClass=eduPerson)(uid="+user+"))";
 		groups=this.runQuery(attrIDs, searchStringGroups,searchStringData,null);
 		ArrayList<String> toReturn = new ArrayList<String>();
 		
 		if(groups!=null)
 		{
 			
 			
 			for(int i=0;i<groups.size();i++)
 			{
 		
 				String value=groups.get(i);
 				if(value.contains("ou=classLeaders"))
 				{
 					String[] valueSets = value.split(",");
 					for(int j = 0; j< valueSets.length; j++)
 					{
 						if(valueSets[j].startsWith("cn="))
 						{
 							String courseOfferingInfo = valueSets[j].split("=")[1];
 							int  leaderIndex = courseOfferingInfo.indexOf("_leaders");
 							if(leaderIndex > 0)
 							{
 								String offeringString = courseOfferingInfo.substring(0, leaderIndex);
 								if(startsWithTerm(offeringString))
 								{
 									toReturn.add(offeringString);
 								}
 							}
 						}
 					}
 				}
 					
 			}
 		
 		}
 		return toReturn;
 	}
 	private boolean startsWithTerm(String s)
 	{
 		try
 		{
 			String term = s.substring(0,6);
 			Integer.parseInt(term);
 			return true;
 		}
 		catch(Exception e)
 		{
 			//either the start wasn't a number or the string was too short. Either way, it didn't start with the term
 		}
 		return false;
 	}
 	
 	public static void main( String[] args ) throws Exception
 	{
 		start=System.currentTimeMillis();
 		LdapConnection ldap=new LdapConnection();
 		
 		TreeMap<String,String> data = ldap.getUserData("dfv574");
 		for(String key : data.keySet())
 		{
 			System.out.println(key +" = " + data.get(key));
 		}
 		
 		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
 		List<String> grouplist = ldap.getDirectDeptEmployees("University Learning Centre");
 		for(String group:grouplist)
 		{
 			System.out.println(group);
 		}
 		
 		/* List<TreeMap<String,String>> list = ldap.getUserData(grouplist);
 		for(TreeMap<String,String> data:list)
 		{
 			for(String key : data.keySet())
 			{
 				System.out.println(key+" : "+data.get(key));
 			}
 			System.out.println("+++++++++");
 		}*/
 		for(String group:grouplist)
 		{
 			List<String> depts = ldap.getUserDepartments(group);
 		
 			for(String dept:depts)
 			{
 				System.out.println(dept);
 			}
 			System.out.println("+++++++++");
 		}
 		
 		
 	/*	List<TreeMap<String,String>> results = ldap.searchForUserWithSurname("reer");
 		for(TreeMap<String,String> res : results)
 		{
 			for(String key : res.keySet())
 			{
 				System.out.println(key+" : "+res.get(key));
 			}
 			System.out.println("+++++++++");
 		}*/
 		/*List<String> grouplist = ldap.getUserGroups("abv641");
 		for(String group:grouplist)
 		{
 			System.out.println(group);
 		}*/
 /*
 		List<String> leaders = ldap.getGroupMembers("201201_ETAD_470_02_leaders");
 		for(String group:leaders)
 		{
 			System.out.println(group);
 		}
 		
 		*/
 
 	/*	Collection<String> groups=ldap.getGroupsContaining("biol");
 		System.out.println("\n\n\nResults:");
 		for(String group:groups)
 		{
 			System.out.println(group);
 		}
 		
 	//	BIOL
 //		Biology
 	//	student_major_biol
 		Map<String,String> data=ldap.getUsersInGroup("Biology");
 		System.out.println("\n\n\nResults:");
 		for(String group:data.keySet())
 		{
 			System.out.println(group);
 		}
 		*/
 		//logger.debug("time taken (including creating a connection) ="+(System.currentTimeMillis()-start));	
 		ldap.closeConnection();
 	}
 	
 	public List<TreeMap<String,String>> searchForUserWithSurname(String name) throws Exception
 	{
 		List<TreeMap<String,String>> valuesList=new ArrayList<TreeMap<String,String>>();
 		
		String[] attributesToReturn = {"cn","givenName","sn","uid"};	// this changed for guest accounts.  Everyone has at least one mail attribute, though it may be different from abc123@mail.usask.ca, which is the uofsofficialemailaddress
 		SearchControls constraints = new SearchControls();
 		constraints.setReturningAttributes(attributesToReturn);
 		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
 		
 		//this limits the list of attributes that the LDAP query will return
 		//NOTE: operational attributes like MemberOf only appear if requested specifically
 			
 		NamingEnumeration<SearchResult> results = this.executeSearch("ou=people,dc=usask,dc=ca","(&(objectClass=eduPerson)(sn=*"+name+"*))", constraints);
 		if(results != null)
 		{
 			while (results.hasMore()) 
 			{
 				TreeMap<String,String> values=new TreeMap<String,String>();
 				SearchResult sr = (SearchResult)results.next();
 				Attributes attrs = sr.getAttributes();
 				if (attrs != null) 
 				{
 					/* print each attribute */
 					for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMoreElements();) 
 					{
 						 
 						 Attribute attr = (Attribute)ae.next();
 						 String attrId = attr.getID();
 						 /* print each value */
 						 for (Enumeration<?> vals = attr.getAll(); vals.hasMoreElements(); )
 						 {
 							 Object temp = (Object)vals.nextElement();
 							 
 							 if(temp instanceof String)
 							 {
 								 values.put(attrId, (String)temp);
 							 }
 						 }
 					}
 				}
 				valuesList.add(values);
 			}
 			results.close();
 		}
 		else
 		{
 			System.out.println("Did not find an person with account "+ nsid);
 		}
 		
 		return valuesList;
 	}
 	
 	public TreeMap<String,String> getUserData(String nsid) throws Exception
 	{
 		TreeMap<String,String> values=new TreeMap<String,String>();
 		
 		//the assumption is made that sn and givenName exist.  If these values have a different name, 
 		// please use a translation where the values are placed in the treemap
 		String[] attributesToReturn = {"sn", "givenName","initials","uofsStudentNumber","cn"};
 		SearchControls constraints = new SearchControls();
 		constraints.setReturningAttributes(attributesToReturn);
 		
 		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
 		
 		//this limits the list of attributes that the LDAP query will return
 		//NOTE: operational attributes like MemberOf only appear if requested specifically
 			
 		String ouGroupString = "ou=guests";
 		
 		if (isNsidType(nsid))
 		{
 			ouGroupString = "ou=nsids";
 		}			 
 				 
 		NamingEnumeration<SearchResult> results = this.executeSearch(ouGroupString + ",ou=people,dc=usask,dc=ca","(uid="+nsid+")", constraints);
 									 
 		if(results != null)
 		{
 			while (results.hasMore()) 
 			{
 				SearchResult sr = (SearchResult)results.next();
 				Attributes attrs = sr.getAttributes();
 				if (attrs != null) 
 				{
 					/* print each attribute */
 					for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMoreElements();) 
 					{
 						 
 						 Attribute attr = (Attribute)ae.next();
 						 String attrId = attr.getID();
 						 /* print each value */
 						 for (Enumeration<?> vals = attr.getAll(); vals.hasMoreElements(); )
 						 {
 							 Object temp = (Object)vals.nextElement();
 							 
 							 if(temp instanceof String)
 							 {
 								//put attrId key and temp value in treemap.
 								/*
 								if (attrId.equals("your first name key"))
 									values.put("givenName",(String)temp);
 								else if (attrId.equals("your last name key"))
 									values.put("sn",(String)temp);
 								*/
 						
 								values.put(attrId, (String)temp);
 							 }
 						 }
 					}
 				}
 			}
 			results.close();
 		}
 		else
 		{
 			System.out.println("Did not find an person with account "+ nsid);
 		}
 		return values;
 	}
 	public TreeMap<String,TreeMap<String,String>> getUserData(List<String> nsids) throws Exception
 	{
 		StringBuilder nsidString = new StringBuilder("(|");
 		for(String nsid: nsids)
 		{
 			nsidString.append("(uid=");
 			nsidString.append(nsid);
 			nsidString.append(")");
 		}
 		nsidString.append(")");
 		
 		TreeMap<String,TreeMap<String,String>> valuesMap=new TreeMap<String,TreeMap<String,String>>();
 		String[] attributesToReturn = {"uid","sn", "givenName","initials","uofsStudentNumber","cn"};
 		SearchControls constraints = new SearchControls();
 		constraints.setReturningAttributes(attributesToReturn);
 		
 		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
 		
 		//this limits the list of attributes that the LDAP query will return
 		//NOTE: operational attributes like MemberOf only appear if requested specifically
 			
 		NamingEnumeration<SearchResult> results = this.executeSearch("ou=people,dc=usask,dc=ca","(&(objectClass=eduPerson)"+nsidString.toString()+")", constraints);
 		if(results != null)
 		{
 			while (results.hasMore()) 
 			{
 				TreeMap<String,String> values=new TreeMap<String,String>();
 				SearchResult sr = (SearchResult)results.next();
 				Attributes attrs = sr.getAttributes();
 				if (attrs != null) 
 				{
 					/* print each attribute */
 					for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMoreElements();) 
 					{
 						 
 						 Attribute attr = (Attribute)ae.next();
 						 String attrId = attr.getID();
 						 /* print each value */
 						 for (Enumeration<?> vals = attr.getAll(); vals.hasMoreElements(); )
 						 {
 							 Object temp = (Object)vals.nextElement();
 							 
 							 if(temp instanceof String)
 							 {
 								 values.put(attrId, (String)temp);
 							 }
 						 }
 					}
 					valuesMap.put(values.get("uid"),values);
 				}
 			}
 			results.close();
 		}
 		else
 		{
 			System.out.println("Did not find an person with account "+ nsid);
 		}
 		return valuesMap;
 	}
 	
 	
 	public Map<String,String> getUsersInGroup(String groupName) throws Exception 
 	{
 		Map<String,String> users = new TreeMap<String,String>();
 		SearchControls constraints = new SearchControls();
 		String[] emailAttr = {"displayName"};	// this changed for guest accounts.  Everyone has at least one mail attribute, though it may be different from abc123@mail.usask.ca, which is the uofsofficialemailaddress
 		constraints.setReturningAttributes(emailAttr);
 		constraints.setCountLimit(500L);
 		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
 		StringBuilder builder = new StringBuilder();
 		builder.append("(&(objectClass=eduPerson)");
 		builder.append("(uofsGroupName=");
 			builder.append(groupName);
 			builder.append("))");
 		
 		NamingEnumeration<SearchResult> answer = this.executeSearch("ou=people,dc=usask,dc=ca", builder.toString(), constraints);
 		while (answer.hasMoreElements()) {
 			SearchResult sr = (SearchResult) answer.next();
 			Attributes attrs = sr.getAttributes();
 			if (attrs != null) {
 				for (NamingEnumeration<? extends Attribute> ae = attrs.getAll(); ae.hasMoreElements();) {
 					Attribute attr = (Attribute) ae.next();
 					for (Enumeration<?> vals = attr.getAll(); vals.hasMoreElements();) {
 						Object temp = (Object) vals.nextElement();	// just grab the first email address
 						if (temp instanceof String) 
 						{
 							String value = (String) temp;
 							String name = sr.getName();
 							String nsid = name.split("=")[1].split(",")[0];
 							users.put(value, nsid);
 						}
 					}
 				}
 			}
 		}
 		return users;
 	}
 	
 	public List<String> getDirectDeptEmployees(String dept) throws Exception
 	{
 		String[] attrIDs = {"member"};
 	
 		List<String> groups=this.runQuery(attrIDs, "ou=direct,ou=staff,ou=employeeDepartments,ou=groups,dc=usask,dc=ca","(&(objectClass=groupOfNames)(uofsEmployeeDepartmentLong="+dept+"))","uid=");
 		List<String> toReturn  =  new ArrayList<String>();
 		
 		for(String x : groups)
 		{
 			System.out.println(x);
 			
 			int uidLocation = x.indexOf("uid=");
 			 
 			 String value = x.substring(uidLocation, x.indexOf(",",uidLocation)).split("=")[1];
 			 toReturn.add(value);
 		}
 		return toReturn;
 	}
 	public List<String> getGroupMembers(String group) throws Exception
 	{
 		String[] attrIDs = {"member"};
 	
 		List<String> groups=this.runQuery(attrIDs, "ou=groups,dc=usask,dc=ca","(&(objectClass=groupOfNames)(cn="+group+"))","uid=");
 		List<String> toReturn  =  new ArrayList<String>();
 		
 		for(String x : groups)
 		{
 			 int uidLocation = x.indexOf("uid=");
 			 String value = x.substring(uidLocation, x.indexOf(",",uidLocation)).split("=")[1];
 			 toReturn.add(value);
 		}
 		return toReturn;
 	}
 	public ArrayList<String> getUserDepartments(String user) throws Exception
 	{		
 		logger.error("Retrieving groups for "+user);
 		ArrayList<String> groups=new ArrayList<String>();
 		SearchControls constraints = new SearchControls();
 		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
 		
 		//this limits the list of attributes that the LDAP query will return
 		//NOTE: operational attributes like MemberOf only appear if requested specifically
 		String[] attrIDs = {"uofsEmployeeDepartmentLong"};
 		constraints.setReturningAttributes(attrIDs);
 		//you can also limit search results to a specified number
 		//constraints.setCountLimit(10);
 		String searchStringGroups="ou=people,dc=usask,dc=ca";
 		String searchStringData="(&(objectClass=eduPerson)(uid="+user+"))";
 		groups=this.runQuery(attrIDs, searchStringGroups,searchStringData,null);
 		logger.error("done retrieving groups for "+user);
 
 		return groups;
 	}
 	
 	public ArrayList<String> getUserGroups(String user) throws Exception
 	{		
 		logger.error("Retrieving groups for "+user);
 		ArrayList<String> groups=new ArrayList<String>();
 		SearchControls constraints = new SearchControls();
 		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
 		
 		//this limits the list of attributes that the LDAP query will return
 		//NOTE: operational attributes like MemberOf only appear if requested specifically
 		//String[] attrIDs = {"uofsEmployeeDepartmentLong"};
 		//constraints.setReturningAttributes(attrIDs);
 		//you can also limit search results to a specified number
 		//constraints.setCountLimit(10);
 		String searchStringGroups="dc=usask,dc=ca";
 		String searchStringData="(&(objectClass=eduPerson)(uid="+user+"))";
 		NamingEnumeration<SearchResult> results = this.executeSearch(searchStringGroups,searchStringData, constraints);
 		if(results != null)
 		{
 			while (results.hasMore()) 
 			{
 				SearchResult sr = (SearchResult)results.next();
 				Attributes attrs = sr.getAttributes();
 				if (attrs != null) 
 				{
 					/* print each attribute */
 					for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMoreElements();) 
 					{
 						 
 						 Attribute attr = (Attribute)ae.next();
 						 String attrId = attr.getID();
 						 /* print each value */
 						 for (Enumeration<?> vals = attr.getAll(); vals.hasMoreElements(); )
 						 {
 							 Object temp = (Object)vals.nextElement();
 							 
 							 if(temp instanceof String)
 							 {
 								// values.put(attrId, (String)temp);
 								 System.out.println(attrId +" = "+ temp );
 							 }
 						 }
 					}
 				}
 			}
 			results.close();
 		}
 		else
 		{
 			System.out.println("Did not find an person with account "+ nsid);
 		}
 		System.out.println("done retrieving groups for "+user);
 
 		return groups;
 	}
 
 	/** retrieves all the roles matching search criteria
 	 * 
 	 * @param term	if included, the role must match term
 	 * @param name if included, role-names returned must contain the name 
 	 * @param exact if true, only exact name matches are included. Since the term is also part of the name, it makes no sense to search for a term, a name and set exact to true.
 	 * @return All groups matching given search-criteria
 	 * @throws NamingException
 	 * @throws CommunicationException
 	 * @throws NullPointerException
 	 */
 	public ArrayList<String> getGroupsMatchingSearch(String term,String name,boolean exact) throws Exception
 	{
 		ArrayList<String> groups=new ArrayList<String>();
 		String[] attrIDs = {"cn"};
 		String searchStringGroups="ou=groups,dc=usask,dc=ca";
 		String searchStringData=null;
 		if(term!=null)
 			searchStringData="(&(objectClass=groupOfNames)(cn="+term+"*))";
 		if(name !=null)
 		{
 			if(!exact)
 				name="*"+name+"*";
 			if(searchStringData==null)
 				searchStringData="(&(objectClass=groupOfNames)(cn="+name+"))";
 			else
 				searchStringData="(&"+searchStringData+"(cn="+name+"))";
 		}
 		logger.info("Running LDAP query:["+searchStringGroups+"] ssd=["+searchStringData+"]");
 		groups=this.runQuery(attrIDs, searchStringGroups, searchStringData, null);
 		return groups;
 	}
 	
 	/**Method does very little other than handle the retry if the connection has gone away.
 	 *
 	 * @param g  groups to be returned
 	 * @param c conditions
 	 * @param s search conditions
 	 * @return speaks for itself.
 	 * @throws Exception
 	 */
 	
 	public NamingEnumeration<SearchResult> executeSearch(String g,String c, SearchControls s) throws Exception
 	{
 		NamingEnumeration<SearchResult> results =null;
 		try
 		{
 			results=ctx.search(g,c, s);
 			return results;
 		}
 		catch (Exception ne)
 		{
 			boolean retry=false;
 						if(ne instanceof NoInitialContextException )
 			{
 				logger.error("LDAP Connection: NoInitialContextException ");
 				retry=true;
 			}
 			else if(ne instanceof IOException )
 			{
 				logger.error("LDAP Connection: IOException ");
 				
 				retry=true;
 			}
 			else if(ne instanceof CommunicationException )
 			{
 				logger.error("LDAP Connection: CommunicationException ");
 				
 				retry=true;
 			}
 			else
 			{
 				String message=ne.getMessage();
 				//connection has gone away!
 				if(message.startsWith("LDAP response read timed out")|| message.startsWith("Connection reset"))
 				{
 					logger.error("LDAP Connection: LDAP response read timed out or Connection Reset");
 					retry=true;
 				}
 				else
 				{
 					logger.error("LDAP Connection, other kind of error!", ne);
 				}
 			}
 			if(retry)
 			{
 				//try to reconnect
 				connectionError=true;
 				initConnection();
 				
 				if(!hasErrors())
 				{//there are no errors, but if it was already attempted and I still got
 					//an exception, don't bother trying again. 
 					retries++;
 					if(retries>3)
 					{
 						throw new Exception("Unable to retrieve groups from ULDAP, tried it 3 times.");
 					}
 					else
 					{
 						return executeSearch(g,c,s);
 					}
 				}
 			}
 			throw ne;
 		}
 	}
 	
 	
 	/**
 	 * method queries uldap for either membership groups or groups matching conditions 
 	 * @param attributesToReturn which field in the result-set do we want back
 	 * @param groups which groups should be used to determine search-domain
 	 * @param conditions search-criteria
 	 * @param filter any values that the returned 
 	 * @return
 	 */
 	public ArrayList<String> runQuery(String[] attributesToReturn, String groups,String conditions, String filter ) throws Exception
 	{
 		ArrayList<String> toReturn=new ArrayList<String>();
 		SearchControls constraints = new SearchControls();
 		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
 		constraints.setReturningAttributes(attributesToReturn);
 		constraints.setCountLimit(1000);
 		NamingEnumeration<SearchResult> results=executeSearch(groups,conditions,constraints);
 		try
 		{
 			if(results != null)
 			{
 				while (results.hasMore()) 
 				{
 					SearchResult sr = results.next();
 					Attributes attrs = sr.getAttributes();
 					if (attrs != null) 
 					{
 						/* print each attribute */
 						for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMoreElements();) 
 						{
 							 
 							 Attribute attr = (Attribute)ae.next();
 							 /* print each value */
 							 for (Enumeration<?> vals = (Enumeration<?>)attr.getAll(); vals.hasMoreElements(); )
 							 {
 								 Object temp = vals.nextElement();
 								 
 								 if(temp instanceof String)
 								 {
 									 String x = (String)temp;
 									 if(filter!=null)
 									 {
 										 if(x!=null && x.contains(filter))
 											 toReturn.add(x);
 									 }
 									 else
 										 toReturn.add(x);
 								 }
 							 }
 						}
 					}
 				}
 				results.close();
 				retries=0;	
 			}
 		}
 		catch(NamingException e)
 		{
 			String message=e.getMessage();
 			if(message.contains("Sizelimit Exceeded"))
 				throw new Exception("Your search-criteria are too broad, more than 1000 entries were returned");
 
 			throw new Exception("Something went horribly wrong while attempting to read the ULDAP results");
 		}
 		//if(toReturn!=null)
 		//	logger.info("Number of groups returned : "+toReturn.size());
 		return toReturn;
 	}
 
 
 	
 	
 	public void finalize()
 	{
 		this.closeConnection();
 	}
 
 
 		
 		
 		public Collection<String> getDepartmentsContaining(String text) throws Exception
 		{
 			TreeMap<String,String> groups=new TreeMap<String,String>();
 			SearchControls constraints = new SearchControls();
 			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
 			
 			String[] attrIDs = {"uofsEmployeeDepartmentLong"};
 			constraints.setReturningAttributes(attrIDs);
 			constraints.setCountLimit(1000);
 			
 			NamingEnumeration<SearchResult> results =this.executeSearch("dc=usask,dc=ca","(uofsEmployeeDepartmentLong=*"+text+"*)", constraints);
 			text = text.toLowerCase();
 			if(results != null)
 			{
 				while (results.hasMore()) 
 				{
 					SearchResult sr = results.next();
 					Attributes attrs = sr.getAttributes();
 					if (attrs != null) 
 					{
 						for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMoreElements();) 
 						{
 							 
 							 Attribute attr = (Attribute)ae.next();
 							 for (Enumeration<?> vals = (Enumeration<?>)attr.getAll(); vals.hasMoreElements(); )
 							 {
 								 Object temp = vals.nextElement();
 								 if(temp!= null && temp instanceof String)
 								 {
 									 String x = (String)temp;
 									 String key = x.toLowerCase();
 									 if(!groups.containsKey(key))
 									 {
 										 if(key.contains(text))
 										 {
 											 groups.put(key,x);
 										 }
 									 }
 									 
 								 }
 							 }
 						}
 					}
 				}
 				results.close();
 			}
 	
 			return groups.values();
 		}
 		public Collection<String> getGroupsContaining(String text) throws Exception
 		{
 			TreeMap<String,String> groups=new TreeMap<String,String>();
 			SearchControls constraints = new SearchControls();
 			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
 			
 			//String[] attrIDs = {"uofsGroupName"};
 			//constraints.setReturningAttributes(attrIDs);
 			constraints.setCountLimit(1000);
 			
 			NamingEnumeration<SearchResult> results =this.executeSearch("ou=groups,dc=usask,dc=ca","(&(objectCLass=groupOfNames)(uofsGroupName=*"+text+"*))", constraints);
 			text = text.toLowerCase();
 			if(results != null)
 			{
 				while (results.hasMore()) 
 				{
 					SearchResult sr = results.next();
 					Attributes attrs = sr.getAttributes();
 					if (attrs != null) 
 					{
 						for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMoreElements();) 
 						{
 							 
 							 Attribute attr = (Attribute)ae.next();
 							 for (Enumeration<?> vals = (Enumeration<?>)attr.getAll(); vals.hasMoreElements(); )
 							 {
 								 Object temp = vals.nextElement();
 								 if(temp!= null && temp instanceof String)
 								 {
 									 String x = (String)temp;
 									 String key = x.toLowerCase();
 									// System.out.println("uofsGroupName = "+x);
 									 if(!groups.containsKey(key))
 									 {
 										 if(key.contains(text))
 										 {
 											 groups.put(key,x);
 										 }
 									 }
 									 
 								 }
 							 }
 						}
 					}
 				}
 				results.close();
 			}
 	
 			return groups.values();
 		}
 		
 		
 	// if you want to get email addresses for users, you can pass in their "group":  uid=cfh928,ou=blah,etc
 
 		public HashMap<String, List<String>> getUsersAndEmailsInGroups(List<String> userids) throws Exception 
 		{
 			if (userids.isEmpty()) {
 				return new HashMap<String, List<String>>();
 			}
 			HashMap<String, List<String>> usersAndEmails = new HashMap<String, List<String>>();
 			SearchControls constraints = new SearchControls();
 			String[] emailAttr = {"mail"};	// this changed for guest accounts.  Everyone has at least one mail attribute, though it may be different from abc123@mail.usask.ca, which is the uofsofficialemailaddress
 			constraints.setReturningAttributes(emailAttr);
 			constraints.setCountLimit(500L);
 			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
 			StringBuilder builder = new StringBuilder();
 			builder.append("(&(objectClass=eduPerson)");
 			builder.append("(|");
 			for (String userid : userids) 
 			{
 				builder.append("(uid=");
 				builder.append(userid);
 				builder.append(")");
 			}
 			builder.append(")");	// close the |
 			builder.append(")");	// close the &
 			
 			NamingEnumeration<SearchResult> answer = this.executeSearch("ou=people,dc=usask,dc=ca", builder.toString(), constraints);
 			while (answer.hasMoreElements()) {
 				SearchResult sr = (SearchResult) answer.next();
 				Attributes attrs = sr.getAttributes();
 				if (attrs != null) {
 					for (NamingEnumeration<? extends Attribute> ae = attrs.getAll(); ae.hasMoreElements();) {
 						Attribute attr = (Attribute) ae.next();
 						for (Enumeration<?> vals = attr.getAll(); vals.hasMoreElements();) {
 							Object temp = (Object) vals.nextElement();	// just grab the first email address
 							if (temp instanceof String) {
 								String value = (String) temp;
 								String name = sr.getName();
 								String userid = name.substring(name.indexOf("=")+1, name.indexOf(","));
 								List<String> existingList = usersAndEmails.get(userid);
 								if(existingList == null)
 								{
 									existingList = new ArrayList<String>();
 								}
 								if(!existingList.contains(value))
 								{
 									existingList.add(value);
 									usersAndEmails.put(userid,existingList);
 								}
 							}
 						}
 					}
 				}
 			}
 			return usersAndEmails;
 		}
 		private boolean isNsidType(String s)
 		{
 			if(s == null)
 				return false;
 			s = s.trim().toLowerCase();
 			if (s.length() != 6)
 				return false;
 			for(int i= 0 ; i < 3; i++)
 			{
 				if(s.charAt(i) < 'a' || s.charAt(i) > 'z')
 					return false;
 			}
 			try
 			{
 				Integer.parseInt(s.substring(3,6));
 				return true;
 			}
 			catch(NumberFormatException e)
 			{
 				return false;
 			}
 			
 		}
 }
 
