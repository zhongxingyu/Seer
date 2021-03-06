 package org.infoscoop.account.ldap;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 import javax.security.auth.Subject;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.infoscoop.account.AuthenticationException;
 import org.infoscoop.account.IAccount;
 import org.infoscoop.account.IAccountManager;
 import org.infoscoop.account.IGroup;
 import org.infoscoop.account.PrincipalDef;
 import org.infoscoop.acl.ISPrincipal;
 
 /**
  * @author hr-endoh
  *
  * TODO:getNameInNamespace isn't possible if version of Java isn't 1.5 or more.
  * As for present, the way of making dn is slight.
  */
 public class LDAPAccountManager implements IAccountManager{
 	private static Log log = LogFactory.getLog(LDAPAccountManager.class);
 	private static final String LDAP_GROUP_PRINCIPAL = "LDAPGroupPrincipal";
 
 	private static String USER_SEARCH_BASE_KEY = "userSearchBase";
 	private static String GROUP_SEARCH_BASE_KEY = "groupSearchBase";
 	private static Collection principalDefs = new ArrayList();
 	static{
 		principalDefs.add(new PrincipalDef(LDAP_GROUP_PRINCIPAL, "LDAP Group"));
 	}
 
 	private InitialDirContext context;
 
 	private String connectionURL;
 	private String connectionName;
 	private String connectionPassword;
 	private String userBase = "ou=Users,dc=example,dc=com";
 	private String groupBase = "cn=groups,dc=example,dc=com";
 
 	private Map<String,String> propAttrMap = new HashMap<String,String>();
 	private Map<String,String> propBaseMap = new HashMap<String,String>();
 
 	/**
 	 * Account atrribute in User entry;
 	 */
 	private String accountAttr = "uid";
 
 	/**
 	 * Display name atrribute in User entry.
 	 */
 	private String userNameAttr = "displayName";
 
 	/**
 	 * Group Name in User entry.
 	 */
 	private String userGroupNameAttr = "departmentnumber";
 
 	/**
 	 * Reference of group in User entry
 	 */
 	private String userGroupAttr = "memberOf";
 
 	/**
 	 * Display name attribute in Group entry.
 	 */
 	private String groupNameAttr;//"departmentnumber";
 
 	public LDAPAccountManager(String connectionURL, String userSearchBase, String groupSearchBase) throws NamingException{
 		this( connectionURL, null, null, userSearchBase, groupSearchBase);
 	}
 
 	public LDAPAccountManager(String connectionURL, String connectionName, String connectionPassword, String userSearchBase, String groupSearchBase) throws NamingException{
 		this.connectionURL =  connectionURL;
 	    this.connectionName = connectionName;
 	    this.connectionPassword = connectionPassword;
 	    this.userBase = userSearchBase;
 	    this.groupBase = groupSearchBase;
 	}
 
 
 	private void initContext() throws NamingException{
 		Hashtable env = new Hashtable();
 	    env.put(Context.INITIAL_CONTEXT_FACTORY,
 	            "com.sun.jndi.ldap.LdapCtxFactory");
 
 	    env.put(Context.PROVIDER_URL , this.connectionURL);
 	    env.put("java.naming.ldap.version" , "3" );
 	    if(this.connectionName != null){
 	    	env.put(Context.SECURITY_PRINCIPAL , this.connectionName );
 	    	env.put(Context.SECURITY_CREDENTIALS , this.connectionPassword );
 	    }
 	    this.context = new InitialDirContext( env );
 	}
 
 
 	public void setUserSearchAttr(Map propAttrMap){
 		this.propAttrMap.putAll(propAttrMap);
 
 		for(Iterator it = propAttrMap.keySet().iterator(); it.hasNext();){
 			String propName = (String)it.next();
 			this.propBaseMap.put(propName, USER_SEARCH_BASE_KEY);
 		}
 
 	}
 	public void setGroupSearchAttr(Map propAttrMap){
 		this.propAttrMap.putAll(propAttrMap);
 
 		for(Iterator it = propAttrMap.keySet().iterator(); it.hasNext();){
 			String propName = (String)it.next();
 			this.propBaseMap.put(propName, GROUP_SEARCH_BASE_KEY);
 		}
 	}
 
 	public void setUserNameAttr(String userNameAttr) {
 		this.userNameAttr = userNameAttr;
 	}
 
 	public void setUserGroupNameAttr(String userGroupNameAttr) {
 		this.userGroupNameAttr = userGroupNameAttr;
 	}
 
 	public void setGroupNameAttr(String groupNameAttr) {
 		this.groupNameAttr = groupNameAttr;
 	}
 	/**
 	 * Return the filter of each SearchBase.
 	 * @param searchConditionMap
 	 * @return
 	 */
 	private Map getConditionForSearchBase(Map searchConditionMap){
 		Map conditionForBase = new HashMap();
 		for(Iterator it = searchConditionMap.entrySet().iterator();it.hasNext();){
 			Map.Entry entry = (Map.Entry)it.next();
 			String propName = (String)entry.getKey();
 			String value = (String)entry.getValue();
 			if(value == null || "".equals(value)) continue;
 			String attrName = (String)this.propAttrMap.get(propName);
 			String searchBase = (String)this.propBaseMap.get(propName);
 
 			Map filterMap = (Map)conditionForBase.get(searchBase);
 			if(filterMap == null)filterMap = new HashMap();
 			filterMap.put(attrName, value);
 			conditionForBase.put(searchBase, filterMap);
 		}
 		return conditionForBase;
 	}
 
 	public IAccount getUser(String uid) throws NamingException {
 
 		if(this.context == null){
 			this.initContext();
 		}
 
 		SearchControls searchControls = new SearchControls();
 		searchControls.setSearchScope( SearchControls.SUBTREE_SCOPE );
 		NamingEnumeration searchResultEnum;
 		Map filters = new HashMap();
 
 		String uidAttrName = "uid";
 		if( this.propAttrMap.containsKey("user_id") ) {
 			try {
 				uidAttrName = ( String )this.propAttrMap.get("user_id");
 			} catch( Exception ex ) {
 				//ignore
 			}
 		}
 		if(uid != null && !"".equals(uid)) filters.put( uidAttrName, uid);
 
 		searchResultEnum = this.context.search(userBase, buildFilterByUid(filters), searchControls);
 		//roop of retrieval result
 
 		while ( searchResultEnum.hasMore() ){
 			SearchResult searchResult =
 				(SearchResult)searchResultEnum.next();
 
 			String dn = searchResult.getName() + "," + userBase;
 			LDAPAccount user = createLDAPUser(dn, searchResult.getAttributes());
 			setGroup(user);
 
 			return user;
 		}
 		return null;
 
 	}
 
 	public List searchUser(Map searchConditionMap) throws Exception {
 		Map confitionForBase = getConditionForSearchBase(searchConditionMap);
 
 		Collection users = new TreeSet(new Comparator(){
 
 			public int compare(Object o1, Object o2) {
 				try{
 				LDAPAccount user1 = (LDAPAccount)o1;
 				LDAPAccount user2 = (LDAPAccount)o2;
 
 				return user1.getUid().compareTo(user2.getUid());
 				}catch(Exception e){
 					log.error("", e);
 					return  0;
 				}
 			}
 
 		});
 
 		Map groupFilterMap = (Map) confitionForBase.get(GROUP_SEARCH_BASE_KEY);
 		Collection groupMembers = null;
 		if( groupFilterMap != null ){
 			groupMembers = searchGroupMember(groupFilterMap);
 		}
 		Map userFilterMap = (Map)confitionForBase.get(USER_SEARCH_BASE_KEY);
 
 		if( userFilterMap != null ){
 			users = searchFromUsers(userFilterMap);
 
 			if(groupMembers != null){
 				users.retainAll(groupMembers);
 			}
 		}else if(groupMembers != null){
 			users.addAll(groupMembers);
 		}
 
 		List result = new ArrayList();
 		for(Iterator it = users.iterator(); it.hasNext();){
 			LDAPAccount user = (LDAPAccount)it.next();
 			if(user.getGroupName() == null)
 				setGroup(user);
 			result.add(user);
 		}
 
 		return result;
 	}
 
 	public void login(String userid, String password) throws AuthenticationException{
 		try {
 			LDAPAccount user = (LDAPAccount) getUser(userid);
 			if(user == null){
 				throw new AuthenticationException(userid + " is not found.");
 			}
 			Hashtable env = new Hashtable();
 			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 		    env.put(Context.PROVIDER_URL , this.connectionURL);
 			env.put("java.naming.ldap.version" , "3" );
 			env.put(Context.SECURITY_PRINCIPAL , user.getDn() );
 			env.put(Context.SECURITY_CREDENTIALS , password );
 
 			new InitialDirContext( env );
 			
 		} catch (NamingException e) {
 			throw new AuthenticationException(e);
 		}
 	}
 
 	public Subject getSubject(String userid) throws Exception {
 		LDAPAccount user = (LDAPAccount) getUser(userid);
 		if(user == null){
 			throw new AuthenticationException(userid + " is not found.");
 		}
 		Subject loginUser = new Subject();
 		ISPrincipal p = new ISPrincipal(ISPrincipal.UID_PRINCIPAL, user.getUid());
 		p.setDisplayName(user.getName());
 		loginUser.getPrincipals().add(p);
 		for (IGroup group : user.getGroups()) {
 			p = new ISPrincipal(LDAP_GROUP_PRINCIPAL, group.getName());
 			loginUser.getPrincipals().add(p);
 		}
 		return loginUser;
 	}
 
 
 	public boolean enableChangePassword(){
 		return false;
 	}
 
 	public void changePassword(String userid, String password,
 			String oldPassword) {
 		throw new UnsupportedOperationException();
 	}
 
 	private void setGroup(LDAPAccount user) throws NamingException{
 
 		SearchControls searchControls = new SearchControls();
 		searchControls.setSearchScope( SearchControls.SUBTREE_SCOPE );
 		//create the filter of group
 		Map filters = new HashMap();
 		String uniqueMemberAttrName = "uniquemember";
 		if(this.propAttrMap.containsKey("org_member"))
 			uniqueMemberAttrName = (String) this.propAttrMap.get("org_member");
 
 		filters.put(uniqueMemberAttrName, user.getDn());
 		String grpFilter = buildGroupFilterByDN(filters);
 
 		NamingEnumeration grpRes = this.context.search( groupBase,
 				grpFilter, searchControls);
 
 		List grpList = new ArrayList();
 
 		while(grpRes.hasMoreElements()) {
 			SearchResult findGrpEntry = (SearchResult) grpRes.next();
 			if(log.isDebugEnabled())log.debug("Found Groups: " + findGrpEntry.getAttributes().get("DN").get(0));
 			String grpdn = findGrpEntry.getName() + "," + userBase;
 
 			grpList.add(createLDAPGroup(grpdn, findGrpEntry.getAttributes()));
 		}
 
 		IGroup[] igroup = new IGroup[grpList.size()];
 
 		for(int i=0; i < igroup.length; i++){
 			igroup[i] = (IGroup)grpList.get(i);
 		}
 		user.setGroups(igroup);
 
 	}
 
 	private List searchFromUsers(Map filters) throws NamingException{
 
 		if(this.context == null){
 			this.initContext();
 		}
 
 		SearchControls searchControls = new SearchControls();
 		searchControls.setSearchScope( SearchControls.SUBTREE_SCOPE );
 		NamingEnumeration searchResultEnum;
 
 		String filter = buildFilter(filters);
 		if(log.isInfoEnabled())
 			log.info("Search User from " + userBase + " by " +  filter );
 		searchResultEnum = this.context.search(userBase, filter, searchControls);
 		//roop of retrieval result
 
 		List users = new ArrayList();
 		while ( searchResultEnum.hasMore() ){
 			SearchResult searchResult =
 				(SearchResult)searchResultEnum.next();
 			String dn = searchResult.getName() + "," + userBase;
 			LDAPAccount user = createLDAPUser(dn, searchResult.getAttributes());
 			users.add(user);
 		}
 		return users;
 	}
 
 	private List searchGroupMember(Map filters) throws NamingException{
 
 		if(this.context == null){
 			this.initContext();
 		}
 
 		SearchControls searchControls = new SearchControls();
 		searchControls.setSearchScope( SearchControls.SUBTREE_SCOPE );
 
 		Set userList = new HashSet();
 		String filter = buildFilter(filters);
 		if(log.isInfoEnabled())
 			log.info("Search User from " + userBase + " by " +  filter );
 		NamingEnumeration searchResultEnum = this.context.search(this.groupBase, filter, searchControls);
 
 
 		while ( searchResultEnum.hasMore() ){
 			SearchResult searchResult =
 				(SearchResult)searchResultEnum.next();
 			Attributes attrs = searchResult.getAttributes();
 			String dn = searchResult.getName() + "," + groupBase;
 			String uniquememberAttrName = "uniqueMember";
 			if( this.propAttrMap.containsKey("org_member") ) {
 				try {
 					uniquememberAttrName = ( String )this.propAttrMap.get("org_member");
 				} catch( Exception ex ) {
 					//ignore
 				}
 			}
 			Attribute uniquememberAttr = attrs.get( uniquememberAttrName );
 			if(uniquememberAttr == null)continue;
 			NamingEnumeration memberDNs = uniquememberAttr.getAll();
 			while(memberDNs.hasMoreElements()){
 				//System.out.println(memberDNs[j]);
 				userList.add(memberDNs.next());//DN of user
 			}
 		}
 
 		List members = new ArrayList();
 
 		for(Iterator userDns = userList.iterator(); userDns.hasNext();) {
 
 			/* Next directory entry */
 			String userDn = (String)userDns.next();
 			Attributes userEntry = null;
 			try{
 				userEntry = this.context.getAttributes(userDn);//DN of user
 			}catch(Exception e){
 				log.error(userDn + ": " + e.getMessage());
 			}
 			if(userEntry == null)continue;
 
 			LDAPAccount user = createLDAPUser(userDn, userEntry);
 			if(user.getUid() == null)continue;
 
 			members.add(user);
 
 		}
 
 		return members;
 
 	}
 
 	private String buildFilter(Map filters){
 		StringBuffer filter = new StringBuffer();
 
 		if(filters.size() > 1){
 			filter.append("(&");
 		}
 		for(Iterator it = filters.entrySet().iterator(); it.hasNext();){
 			Map.Entry entry = (Map.Entry)it.next();
 			filter.append("(").append(entry.getKey()).append("=").append(entry.getValue()).append("*)");
 		}
 
 		if(filters.size() > 1){
 			filter.append(")");
 		}
 		return filter.toString();
 	}
 
 	private String buildGroupFilterByDN(Map filters) {
 		StringBuffer filter = new StringBuffer();
 
 		if(filters.size() > 1){
 			filter.append("(&");
 		}
 		for(Iterator it = filters.entrySet().iterator(); it.hasNext();){
 			Map.Entry entry = (Map.Entry)it.next();
 			filter.append("(").append(entry.getKey()).append("=").append(entry.getValue()).append(")");
 		}
 
 		if(filters.size() > 1){
 			filter.append(")");
 		}
 		return filter.toString();
 	}
 
 	private String buildFilterByUid(Map filters){
 		StringBuffer filter = new StringBuffer();
 
 		if(filters.size() > 1){
 			filter.append("(&");
 		}
 		for(Iterator it = filters.entrySet().iterator(); it.hasNext();){
 			Map.Entry entry = (Map.Entry)it.next();
 			filter.append("(").append(entry.getKey()).append("=").append(entry.getValue()).append(")");
 		}
 
 		if(filters.size() > 1){
 			filter.append(")");
 		}
 		return filter.toString();
 	}
 
 	private LDAPAccount createLDAPUser(String dn, Attributes attrs) throws NamingException{
 		String uid = null;
 		if( propAttrMap.containsKey("user_id") ) {
 			uid = getAttribute( attrs,propAttrMap.get("user_id") );
 		} else {
 			uid = getAttribute( attrs,accountAttr );
 		}
 
 		String mail = null;
		if( propAttrMap.containsKey("user_email"))
 			mail = getAttribute( attrs,propAttrMap.get("user_email"));
 
 		Attribute displayNameAttr = attrs.get(this.userNameAttr);
 		String displayName = null;
 		if(displayNameAttr != null){
 			try{
 				displayName = (String)displayNameAttr.get(0);
 			}catch(ArrayIndexOutOfBoundsException e){
 			}
 		}
 
 		String name = null;
 		if( userNameAttr != null ) {
 			name = getAttribute( attrs,userNameAttr );
 		} else if( propAttrMap.containsKey("user_name")) {
 			name = getAttribute( attrs,propAttrMap.get("user_name"));
 		}
 
 		String groupName = null;
 		if( userGroupNameAttr != null )
 			groupName = getAttribute( attrs,propAttrMap.get( userGroupNameAttr ));
 
 
 		LDAPAccount user = new LDAPAccount( dn,uid,mail,displayName,groupName );
 		return user;
 	}
 
 	private String getAttribute( Attributes attrs,String name ) throws NamingException {
 		if( name == null )
 			return null;
 
 		Attribute attr = attrs.get( name );
 		if( attr != null ){
 			try{
 				return ( String )attr.get( 0 );
 			}catch( ArrayIndexOutOfBoundsException ex ){}
 		}
 
 		return null;
 	}
 
 	private LDAPGroup createLDAPGroup(String dn, Attributes attrs) throws NamingException{
 		String name;
 		if( groupNameAttr != null ) {
 			name = getAttribute( attrs,groupNameAttr );
 		} else {
 			name = getAttribute( attrs,propAttrMap.get("org_name"));
 		}
 
 		return new LDAPGroup( dn,name,null );
 	}
 
 	public Collection<PrincipalDef> getPrincipalDefs() {
 		return principalDefs;
 	}
 }
