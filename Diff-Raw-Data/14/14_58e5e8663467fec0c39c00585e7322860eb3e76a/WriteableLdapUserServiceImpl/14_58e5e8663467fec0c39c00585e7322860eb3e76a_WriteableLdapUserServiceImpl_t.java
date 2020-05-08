 /**
  * ESUP-Portail Commons - Copyright (c) 2006-2009 ESUP-Portail consortium.
  */
 package org.esupportail.commons.services.ldap;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.naming.Name;
 
 import net.sf.ehcache.CacheManager;
 
 import org.apache.commons.lang.StringUtils;
 import org.esupportail.commons.exceptions.UserNotFoundException;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.utils.Assert;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.ldap.UncategorizedLdapException;
 import org.springframework.ldap.core.DirContextAdapter;
 import org.springframework.ldap.core.DistinguishedName;
 import org.springframework.ldap.core.LdapTemplate;
 import org.springframework.ldap.core.support.LdapContextSource;
 
 /**
  * An implementation of WriteableLdapService based on LdapTemplate.
  * See /properties/ldap/ldap-write-example.xml.
  */
 public class WriteableLdapUserServiceImpl implements WriteableLdapUserService, InitializingBean {
 	
 	/**
 	 * The serialization id.
 	 */
 	private static final long serialVersionUID = -2833750508738328830L;
 
 	/**
 	 * A logger.
 	 */
 	private final Logger logger = new LoggerImpl(getClass());
 
 	/**
 	 * A LdapTemplate instance, to perform LDAP operations.
 	 */
 	private LdapTemplate ldapTemplate;
 	
 	/**
 	 * A LdapContextSource instance, to modify LDAP connections.
 	 */
 	private LdapContextSource contextSource;
 
 	/**
 	 * The name of the attribute that contains the unique id.
 	 */
 	private String idAttribute;
 
 	/**
 	 * The DN of LDAP users. for example : ou=people,dc=domain,dc=edu
 	 */
 	private String dnAuth;
 	
 	/**
 	 * The name of the attribute that contains the unique id of LDAP users.
 	 */
 	private String idAuth;
 	
 	/**
 	 * The DN sub path.
 	 */
 	private String dnSubPath;
 	
 	/**
 	 * The names of the attributes to update.
 	 */
 	private List<String> attributes;
     
 	/**
 	 * The cacheManager to invalidate when writing to LDAP to ensure coherence
 	 */
 	private CacheManager cacheManager;
 
 	/**
 	 * The name of cache managed by cacheManager
 	 */
 	private String cacheName;
 	
 	/**
 	 * Bean constructor.
 	 */
 	public WriteableLdapUserServiceImpl() {
 		super();
 	}
 
 	/**
 	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
 	 */
 	public void afterPropertiesSet() {
 		Assert.hasText(idAttribute, 
 				"property idAttribute of class " + getClass().getName() + " can not be null");
 		Assert.notEmpty(attributes,  
 				"property attributes of class " + getClass().getName() + " can not be empty");
 		Assert.hasText(dnSubPath,  
 				"property dnSubPath of class " + getClass().getName() + " can not be empty");
 		if (ldapTemplate == null) {
 			dnSubPath = null;
 			logger.info(getClass() + ": property ldapTemplate is not set"); 
 		}
 		if (logger.isDebugEnabled() && dnAuth != null) {
 			logger.debug("dnAuth" + dnAuth); 
 		}
 		if (!attributes.contains(idAttribute)) {
 			attributes.add(idAttribute);
 		}
 		if (cacheManager == null) {
 			logger.info("Property cacheManager is not set. This is not a problem if you do not use a LDAP cache. Otherwise this cache will be incoherent after using updateLdapUser()");
 		}
 		else {
 			if (cacheName == null) {
 				cacheName = org.esupportail.commons.services.ldap.CachingLdapEntityServiceImpl.class.getName();
 				logger.info("Property cacheName is not set. So default (" + cacheName +
 					") is used. This is not a problem if you do not specify a specific cacheName in ldapService Bean.");				
 			}
 		}
 
 	}
 
 	/** Modify an LDAP user using Spring LdapContextSource.
 	 * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#updateLdapUser(
 	 * org.esupportail.commons.services.ldap.LdapUser)
 	 */
 	public void updateLdapUser(final LdapUser ldapUser) throws LdapAttributesModificationException {
 		Name dn = buildLdapUserDn(ldapUser.getId());
 		try {
 			if (logger.isTraceEnabled()) {
 				logger.trace("Looking for :" + dn);
 			}
 			DirContextAdapter context = (DirContextAdapter) ldapTemplate.lookup(dn);
 			if (logger.isTraceEnabled()) {
 				logger.trace("mapToContext()" + ldapUser);
 			}
 			mapToContext(ldapUser, context);
 			logger.info("Update of LDAP user :" + dn + " : " + ldapUser);
 			ldapTemplate.modifyAttributes(dn, context.getModificationItems());
 
 			// ensure LDAP cache 
 			invalidateLdapCache();
 			
 		} catch (UncategorizedLdapException e) {
 			if (e.getCause() instanceof javax.naming.AuthenticationException) {
 				throw new LdapBindFailedException("Couldn't bind to LDAP with user" + ldapUser.getId());
 			}
 		} catch (Exception e) {
 			if (logger.isTraceEnabled()) {
 				logger.trace("Error in updateLdapUser(): ", e);
 			}
 			throw new LdapAttributesModificationException(
 					"Couldn't get modification items for '" + dn + "'", e);
 		}
 		
 	}
 	
 	/**
 	 * Set or clear a user specified attribute.
 	 * It handles the attribute etiquette: 
 	 * - it keeps unmodified attribute values without this etiquette
 	 * - it prefixes the values with this etiquette
 	 * @param ldapUser
 	 * @param attrName
 	 * @param etiquette
 	 * @param value
 	 * @throws LdapAttributesModificationException 
 	 */
 	public void setOrClearUserAttribute(final LdapUser ldapUser, final String attrName, 
 					    final String etiquette, final List<String> value) 
 					throws LdapAttributesModificationException {
 		Map<String, List<String>> attrs = ldapUser.getAttributes();
 		List<String> allValues = computeAttributeValues(attrs.get(attrName), etiquette, value);
 		attrs.put(attrName, allValues);
 
 		// call updateLdapUser with only the attribute we want to write in LDAP
 		ldapUser.setAttributes(singletonMap(attrName, allValues));
 		updateLdapUser(ldapUser);
 		ldapUser.setAttributes(attrs); // restore other attributes
 	}
 
 	public void setOrClearUserAttribute(final LdapUserService ldapService, final String id, 
 					    final String attrName, final String etiquette, final List<String> value) 
 					throws UserNotFoundException, LdapAttributesModificationException {
 		// ensure we read straight from LDAP
 		// it is especially important since the attribute values with a different etiquette may have changed since last read
 		invalidateLdapCache();
 
 		LdapUser ldapUser = ldapService.getLdapUser(id);
 		setOrClearUserAttribute(ldapUser, attrName, etiquette, value);
 		checkAttributeWriteSucceeded(ldapService, id, attrName, ldapUser);
 	}
 
 	public void setOrClearUserAttribute(final LdapUserAndGroupService ldapService, final String id, 
 					    final String attrName, final String etiquette, final List<String> value) 
 					throws UserNotFoundException, LdapAttributesModificationException {
 		// ensure we read straight from LDAP
 		// it is especially important since the attribute values with a different etiquette may have changed since last read
 		invalidateLdapCache();
 
 		LdapUser ldapUser = ldapService.getLdapUser(id);
 		setOrClearUserAttribute(ldapUser, attrName, etiquette, value);
 		checkAttributeWriteSucceeded(ldapService, id, attrName, ldapUser);
 	}
 
 	private <A, B> Map<A, B> singletonMap(A key, B value) {
 		Map<A, B> r = new HashMap<A, B>();
 		r.put(key, value);
 		return r;
 	}
 
 	public static String join(Iterable<?> elements, CharSequence separator) {
 		if (elements == null) return "";
 
 		StringBuilder sb = null;
 
 		for (Object s : elements) {
 			if (sb == null)
 				sb = new StringBuilder();
 			else
 				sb.append(separator);
 			sb.append(s);			
 		}
 		return sb == null ? "" : sb.toString();
 	}
 
 	private List<String> computeAttributeValues(List<String> currentValues,	final String etiquette, final List<String> wantedValues) {
 		if (StringUtils.isEmpty(etiquette))
 			return wantedValues;
 
 		Set<String> set = new TreeSet<String>();
 		if (currentValues != null) {
 		    for (String s : currentValues)
 			if (!s.startsWith(etiquette)) set.add(s);
 		}
 		for (String v : wantedValues) 
 			set.add(mayAddPrefix(etiquette, v));
 		return new ArrayList<String>(set);
 	}
 
 	private String mayAddPrefix(String prefix, String s) {
 		return prefix == null || s.startsWith(prefix) ? s : prefix + s;
 	}
 
 	/**
 	 * Check wether setting or clearing attribute worked correctly
 	 * @throws UserNotFoundException 
 	 * @throws LdapAttributesModificationException
 	 */
 	private void checkAttributeWriteSucceeded(final LdapUserService ldapService, final String id, final String attrName, final LdapUser wantedLdapUser) throws UserNotFoundException, LdapAttributesModificationException {
 		checkAttributeWriteSucceeded(ldapService.getLdapUser(id), attrName, wantedLdapUser);
 	}
 	private void checkAttributeWriteSucceeded(final LdapUserAndGroupService ldapService, final String id, final String attrName, final LdapUser wantedLdapUser) throws UserNotFoundException, LdapAttributesModificationException {
 		checkAttributeWriteSucceeded(ldapService.getLdapUser(id), attrName, wantedLdapUser);
 	}
 	private void checkAttributeWriteSucceeded(final LdapUser storedLdapUser, final String attrName, final LdapUser wantedLdapUser) throws LdapAttributesModificationException {
 		List<String> value = wantedLdapUser.getAttributes(attrName);
 		List<String> storedValue = storedLdapUser.getAttributes(attrName);
 
 		String error = null;
 		if (value == null && (storedValue == null || storedValue.isEmpty()))
 			;
 		// nb: we can't check wether clearing attribute really removed the attribute or simply emptied it
 		else if (value != null && storedValue == null)
 			// this never happens, storedValue is never null afaik
 			error = "could not create attribute '" + attrName + "' with value " + join(value, ", ");
 		else if (!value.containsAll(storedValue) || !storedValue.containsAll(value))
 			error = "could not modify attribute '" + attrName + "' with value " + join(value, ", ") + ", it's value is still " + join(storedValue, ", "); 
 
 		if (error != null) {
 			logger.error(error);
 			throw new LdapAttributesModificationException(error);
 		}
 	}
 
 	public void invalidateLdapCache() {
 		if (cacheManager != null) {
 			net.sf.ehcache.Cache cache = cacheManager.getCache(cacheName);
 			if (cache != null)
 				cache.removeAll();
 			else
 				logger.error("could not find cacheManager");
 		} else {
 			logger.debug("no LDAP cacheManager to warn");
 		}
 	}
 	
 	/** Create an LDAP user using Spring LdapContextSource.
 	 * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#createLdapUser(
 	 * org.esupportail.commons.services.ldap.LdapUser)
 	 */
 	public void createLdapUser(final LdapUser ldapUser) {
 		Name dn = buildLdapUserDn(ldapUser.getId());
 		DirContextAdapter context = new DirContextAdapter(dn);
 		mapToContext(ldapUser, context);
 		ldapTemplate.bind(dn, context, null);
 		logger.info("created [" + dn + "] from [" + ldapUser + "]");
 	}
 	
 	/** Build user full DN.
 	 * @param userId
 	 * @return user full DN
 	 */
 	protected DistinguishedName buildLdapUserDn(final String userId) {
 		DistinguishedName dn;
 		dn = new DistinguishedName(dnSubPath);
 		dn.add(this.idAttribute, userId);
 		return dn;
 	}
 	
 	/** Delete an LDAP user using Spring LdapContextSource.
 	 * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#deleteLdapUser(
 	 * org.esupportail.commons.services.ldap.LdapUser)
 	 */
 	public void deleteLdapUser(final LdapUser ldapUser) {
 		DistinguishedName ldapUserDn = buildLdapUserDn(ldapUser.getId());
 		ldapTemplate.unbind(ldapUserDn);
 	}
 	
 	/**
 	 * @param ldapUser
 	 * @param context
 	 */
 	protected void mapToContext(final LdapUser ldapUser, final DirContextAdapter context) {
 		List<String> attributesNames = ldapUser.getAttributeNames();
 		for (String ldapAttributeName : attributesNames) {
 			List<String> listAttr = new ArrayList<String>();
 			listAttr = ldapUser.getAttributes(ldapAttributeName);
 			// The attribute exists
 			if (listAttr != null && listAttr.size() != 0) {
 				if (listAttr.contains(null)) {
 					//send empty list to remove attribute in ldap
 					listAttr = new ArrayList<String>();
 				} 
 				context.setAttributeValues(ldapAttributeName, listAttr.toArray());
 			}
 		}
 	}
 	
 	/**
 	 * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#setAuthenticatedContext(
 	 * java.lang.String, java.lang.String)
 	 */
 	public void setAuthenticatedContext(
 			final String userId, 
 			final String password) throws LdapException {
 		DistinguishedName ldapBindUserDn = new DistinguishedName(this.dnAuth);
 		ldapBindUserDn.add(this.idAuth, userId);
 		if (logger.isDebugEnabled()) {
 			logger.debug("Binding to LDAP with DN [" + ldapBindUserDn + "] (password ******)");
 		}
 		contextSource.setUserDn(ldapBindUserDn.encode());
 		contextSource.setPassword(password);
 	}
 	
 	
 	/**
 	 * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#defineAnonymousContext()
 	 */
 	public void defineAnonymousContext() throws LdapException {
 		contextSource.setUserDn("");
 		contextSource.setPassword("");
 	}
 
 	/**
 	 * @return ldapTemplate the LdapTemplate to get
 	 */
 	public LdapTemplate getLdapTemplate() {
 		return ldapTemplate;
 	}
 
 	
 	/**
 	 * @param ldapTemplate the LdapTemplate to set
 	 */
 	public void setLdapTemplate(final LdapTemplate ldapTemplate) {
 		this.ldapTemplate = ldapTemplate;
 	}
 
 	/**
 	 * @return the contextSource to get
 	 */
 	public LdapContextSource getContextSource() {
 		return contextSource;
 	}
 
 	/**
 	 * @param contextSource the ContextSource to set
 	 */
 	public void setContextSource(final LdapContextSource contextSource) {
 		this.contextSource = contextSource;
 	}
 
 	/**
 	 * @return the idAttribute to get
 	 */
 	public String getIdAttribute() {
 		return idAttribute;
 	}
 
 	/**
 	 * @param idAttribute the idAttribute to set
 	 */
 	public void setIdAttribute(final String idAttribute) {
 		this.idAttribute = idAttribute;
 	}
 
 	/**
 	 * @return the dnSubPath to get
 	 */
 	public String getDnSubPath() {
 		return dnSubPath;
 	}
 
 	/**
 	 * @param dnSubPath the dnSubPath to set
 	 */
 	public void setDnSubPath(final String dnSubPath) {
 		this.dnSubPath = dnSubPath;
 	}
 
 	/**
 	 * @return the attributes to get
 	 */
 	public List<String> getAttributes() {
 		return attributes;
 	}
 
 	/**
 	 * @param attributes the attributes to set
 	 */
 	public void setAttributes(final List<String> attributes) {
 		this.attributes = attributes;
 	}
 
 	/**
 	 * @param cacheManager the cacheManager to set
 	 */
 	public void setCacheManager(final CacheManager cacheManager) {
 		this.cacheManager = cacheManager;
 	}
 
 	/**
 	 * @return the dnAuth to get
 	 */
 	public String getDnAuth() {
 		return dnAuth;
 	}
 
 	/**
 	 * @param dnAuth the dnAuth to set
 	 */
 	public void setDnAuth(final String dnAuth) {
 		this.dnAuth = dnAuth;
 	}
 
 	/**
 	 * @return the idAuth to get
 	 */
 	public String getIdAuth() {
 		return idAuth;
 	}
 
 	/**
 	 * @param idAuth the idAuth to set
 	 */
 	public void setIdAuth(final String idAuth) {
 		this.idAuth = idAuth;
 	}

	/**
	 * @return the cacheName
	 */
	public String getCacheName() {
		return cacheName;
	}

	/**
	 * @param cacheName the cacheName to set
	 */
	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}
 	
 }
