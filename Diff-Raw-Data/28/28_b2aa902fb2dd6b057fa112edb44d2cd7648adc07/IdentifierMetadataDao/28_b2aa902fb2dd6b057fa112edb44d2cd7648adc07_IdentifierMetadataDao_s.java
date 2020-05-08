 package org.cagrid.identifiers.namingauthority.dao;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.UUID;
 
 import javax.persistence.NonUniqueResultException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.identifiers.namingauthority.InvalidIdentifierException;
 import org.cagrid.identifiers.namingauthority.InvalidIdentifierValuesException;
 import org.cagrid.identifiers.namingauthority.NamingAuthorityConfigurationException;
 import org.cagrid.identifiers.namingauthority.NamingAuthoritySecurityException;
 import org.cagrid.identifiers.namingauthority.SecurityInfo;
 import org.cagrid.identifiers.namingauthority.domain.IdentifierData;
 import org.cagrid.identifiers.namingauthority.domain.IdentifierValues;
 import org.cagrid.identifiers.namingauthority.domain.KeyData;
 import org.cagrid.identifiers.namingauthority.domain.KeyValues;
 import org.cagrid.identifiers.namingauthority.hibernate.IdentifierMetadata;
 import org.cagrid.identifiers.namingauthority.hibernate.IdentifierValueKey;
 import org.cagrid.identifiers.namingauthority.impl.IdentifierGeneratorImpl;
 import org.cagrid.identifiers.namingauthority.impl.SecurityInfoImpl;
 import org.cagrid.identifiers.namingauthority.util.Constant;
 import org.cagrid.identifiers.namingauthority.util.IdentifierUtil;
 import org.cagrid.identifiers.namingauthority.util.Keys;
 import org.cagrid.identifiers.namingauthority.util.SecurityUtil;
 import org.cagrid.identifiers.namingauthority.util.SecurityUtil.Access;
 import org.cagrid.identifiers.namingauthority.util.Tree;
 
 public class IdentifierMetadataDao extends AbstractDao<IdentifierMetadata>
 {
 
 	protected static Log LOG = LogFactory.getLog(IdentifierMetadataDao.class.getName());
 	private IdentifierMetadata systemValues = null;
 	private URI prefix = null;
 
 	@Override
 	public Class<IdentifierMetadata> domainClass()
 	{
 		return IdentifierMetadata.class;
 	}
 
 	public synchronized void initialize(URI prefix) throws NamingAuthorityConfigurationException
 	{
 		this.prefix = prefix;
 		try
 		{
 			systemValues = loadLocalIdentifier(SecurityUtil.LOCAL_SYSTEM_IDENTIFIER);
 		}
 		catch (InvalidIdentifierException e)
 		{
 			LOG.debug("No system identifier defined");
 			createSystemIdentifier();
 		}
 	}
 
 	public IdentifierMetadata loadLocalIdentifier(final URI localIdentifier) throws InvalidIdentifierException,
 			NamingAuthorityConfigurationException
 	{
 		LOG.debug("The local identifier is " + localIdentifier);
 		List<IdentifierMetadata> results = getHibernateTemplate().find(
 				"SELECT md FROM " + domainClass().getName() + " md WHERE md.localIdentifier = ?",
 				new Object[] { localIdentifier });
 
 		IdentifierMetadata result = null;
 
 		if (results.size() > 1)
 		{
 			throw new NonUniqueResultException("Found " + results.size() + " " + domainClass().getName() + " objects.");
 		}
 		else if (results.size() == 1)
 		{
 			result = results.get(0);
 		}
 
 		if (result == null)
 		{
 			throw new InvalidIdentifierException("Local identifier (" + localIdentifier + ") does not exist");
 		}
 
 		return result;
 	}
 
 	public IdentifierMetadata loadIdentifier(URI identifier) throws InvalidIdentifierException,
 			NamingAuthorityConfigurationException
 	{
 		return loadLocalIdentifier(IdentifierUtil.getLocalName(prefix, identifier));
 	}
 
 	public List<IdentifierMetadata> getSiteData(URI localIdentifier) throws InvalidIdentifierException,
 			NamingAuthorityConfigurationException
 	{
		LOG.debug("getSiteData The local identifier is " + localIdentifier);
 		List<IdentifierMetadata> results = getHibernateTemplate()
 				.find("SELECT md FROM "
 						+ domainClass().getName()
 						+ " as md INNER JOIN md.values as value INNER JOIN value.values as val WHERE value.key='GSID' and val=?",
 						new Object[] { localIdentifier.toString() });
 
 		return results;
 	}
 
 	/*
 	 * Returns keys associated with the given identifier
 	 */
 	public String[] getKeyNames(SecurityInfo secInfo, java.net.URI identifier) throws InvalidIdentifierException,
 			NamingAuthoritySecurityException, NamingAuthorityConfigurationException
 	{
 
 		IdentifierData values = null;
 
 		try
 		{
 			values = getIdentifierData(secInfo, identifier, null);
 		}
 		catch (InvalidIdentifierValuesException e)
 		{
 			throw new NamingAuthorityConfigurationException(e);
 		}
 
 		if (values != null)
 		{
 			return values.getKeys();
 		}
 
 		return null;
 	}
 
 	/*
 	 * Returns values associated with a key in the given identifier
 	 */
 	public KeyData getKeyData(SecurityInfo secInfo, URI identifier, String key) throws InvalidIdentifierException,
 			NamingAuthoritySecurityException, NamingAuthorityConfigurationException, InvalidIdentifierValuesException
 	{
 
 		IdentifierData values = getIdentifierData(secInfo, identifier, key);
 		if (values != null && values.getValues(key) != null)
 		{
 			return values.getValues(key);
 		}
 		return null;
 	}
 
 	/*
 	 * Resolves an identifier to its associated meta-data
 	 */
 	public IdentifierData resolveIdentifier(SecurityInfo secInfo, java.net.URI identifier)
 			throws InvalidIdentifierException, NamingAuthoritySecurityException, NamingAuthorityConfigurationException
 	{		
 		if (identifier == null)
 		{
 			throw new InvalidIdentifierException("Identifier cannot be null.");
 		}
 		try
 		{			
 			if (!identifier.toString().startsWith(prefix.toString()))
 			{
 				try{
 				identifier = IdentifierUtil.build(prefix, identifier);
 				}catch(Exception e)
				{
					
 				}
 			}
 			LOG.warn("the identifier is "+identifier.toString());
 			IdentifierData completeData = getIdentifierData(secInfo, identifier, null);
 			List<IdentifierMetadata> siteData = getSiteData(identifier);
 			LOG.warn("site data size is "+siteData.size());
 			if (siteData != null && siteData.size() > 0)
 			{

 				int counter = 0;
 				for (IdentifierMetadata metaData : siteData)
 				{
 					IdentifierData currentData = IdentifierUtil.convert(metaData.getValues());
 					for (String key : currentData.getKeys())
 					{
 						if (key.equalsIgnoreCase("PUBLISHER"))
 						{
 							KeyData data = currentData.getValues(key);
 							String publisherIdentifier = data.getValues().get(0);
 
 							try
 							{
 								URI publisherURI = new URI(prefix + publisherIdentifier);
 								IdentifierData siteIdentifier = getIdentifierData(secInfo, publisherURI, null);
 								if (siteIdentifier != null)
 								{
 									for (String key1 : siteIdentifier.getKeys())
 									{
 										if (!key1.equalsIgnoreCase("TYPE"))
 										{
 											completeData.put(counter + ":" + key1, siteIdentifier.getValues(key1));
 										}
 									}
 								}
 							}
 							catch (Exception e)
 							{
 								LOG.warn("");
 								throw new InvalidIdentifierException();
 							}
 						}
						else if (!key.equalsIgnoreCase("GSID") || !key.equalsIgnoreCase("TYPE"))
 						{
 							completeData.put(counter + ":" + key, currentData.getValues(key));
 						}
 					}
 					counter++;
 				}
 
 			}
 
 			return completeData;
 		}
 		catch (InvalidIdentifierValuesException e)
 		{
 			throw new NamingAuthorityConfigurationException(e);
 		}
 	}
 
 	/*
 	 * A user can read a key from an identifier if any one of the below
 	 * conditions are met:
 	 * 
 	 * (a) User is identifier's administrator - User is listed by ADMIN_USERS
 	 * key, or - User is listed by ADMIN_IDENTIFIERS's ADMIN_USERS key, or -
 	 * User is listed by root identifier's ADMIN_USERS key
 	 * 
 	 * (b) User is listed by the key's READWRITE_IDENTIFIER's READ_USERS list
 	 * (c) Key has no READWRITE_IDENTIFIER.READ_USERS and user is listed by
 	 * identifier's READ_USERS (d) Key has no READWRITE_IDENTIFIER.READ_USERS
 	 * and user is listed by identifier's READWRITE_IDENTIFIERS.READ_USERS (e)
 	 * No READ_USERS keys at any level (key & identifier)
 	 * 
 	 * A security exception is thrown if the identifier has keys and none are
 	 * returned due to permission checks.
 	 */
 	public IdentifierData getIdentifierData(SecurityInfo secInfo, java.net.URI identifier, String keyName)
 			throws InvalidIdentifierException, NamingAuthoritySecurityException, NamingAuthorityConfigurationException,
 			InvalidIdentifierValuesException
 	{
 
 		secInfo = validateSecurityInfo(secInfo);
 
 		IdentifierMetadata tmpValues = loadIdentifier(identifier);
 
 		if (tmpValues == null)
 		{
 			return null;
 		}
 
 		if (hasIdentifierAdminUserAccess(secInfo, tmpValues))
 		{
 			//
 			// User is ADMIN_USER
 			//
 			return IdentifierUtil.convert(tmpValues.getValues());
 		}
 
 		Collection<IdentifierValueKey> valueCol = tmpValues.getValues();
 		if (valueCol == null || valueCol.size() == 0)
 		{
 			return null;
 		}
 
 		if (keyName != null)
 		{
 			IdentifierValueKey searchKey = new IdentifierValueKey();
 			searchKey.setKey(keyName);
 			if (!valueCol.contains(searchKey))
 			{
 				throw new InvalidIdentifierValuesException("Key [" + keyName + "] does not exist");
 			}
 		}
 
 		Access identifierReadAccess = null;
 		IdentifierData newValues = new IdentifierData();
 
 		for (IdentifierValueKey ivk : valueCol)
 		{
 
 			if (keyName != null && !keyName.equals(ivk.getKey()))
 			{
 				continue;
 			}
 
 			Access keyAccess = getKeyReadAccess(secInfo, ivk.getPolicyIdentifier());
 			if (keyAccess == Access.GRANTED)
 			{
 				LOG.debug("SECURITY: User [" + secInfo.getUser() + "] can access key [" + ivk.getKey() + "]");
 				newValues.put(ivk.getKey(), IdentifierUtil.convert(ivk));
 
 			}
 			else if (keyAccess == Access.DENIED)
 			{
 				LOG.debug("SECURITY: User [" + secInfo.getUser() + "] can't access key [" + ivk.getKey() + "]");
 
 			}
 			else if (keyAccess == Access.NOSECURITY)
 			{
 				LOG.debug("SECURITY: No key security for [" + ivk.getKey() + "]. Checking identifier security...");
 
 				// Apply identifier level security
 
 				if (identifierReadAccess == null)
 				{
 					List<String> identifierReadUsers = getAllReadUsers(tmpValues);
 					identifierReadAccess = userAccess(secInfo.getUser(), identifierReadUsers);
 				}
 
 				if (identifierReadAccess == Access.DENIED)
 				{
 					LOG.debug("SECURITY: User [" + secInfo.getUser() + "] is NOT authorized to read key ["
 							+ ivk.getKey() + "] by identifier");
 
 				}
 				else
 				{
 					LOG.debug("SECURITY: User [" + secInfo.getUser() + "] is authorized to read [" + ivk.getKey()
 							+ "] by identifier");
 					newValues.put(ivk.getKey(), IdentifierUtil.convert(ivk));
 				}
 
 			}
 			;
 		}
 
 		// Is this the only case when we bark?
 		if (newValues.getKeys() == null || newValues.getKeys().length == 0)
 		{
 			String msg;
 			if (keyName != null)
 			{
 				msg = "resolve identifier key [" + keyName + "]";
 
 			}
 			else
 			{
 				msg = "resolve identifier";
 			}
 
 			throw new NamingAuthoritySecurityException(SecurityUtil.securityError(secInfo, msg));
 		}
 
 		return newValues;
 	}
 
 	/*
 	 * Persists the provided identifier with the given values
 	 */
 	public void createIdentifier(SecurityInfo secInfo, URI localIdentifier, IdentifierData ivalues)
 			throws InvalidIdentifierException, NamingAuthorityConfigurationException, NamingAuthoritySecurityException
 	{
 		LOG.debug("The value of localIdentifier is " + localIdentifier);
 		secInfo = validateSecurityInfo(secInfo);
 
 		createIdentifierSecurityChecks(secInfo);
 
 		save(IdentifierUtil.convert(localIdentifier, ivalues));
 	}
 
 	/*
 	 * Adds the provided keys to the given identifier.
 	 * 
 	 * An exception is thrown if any of the provided keys already exists.
 	 * 
 	 * Permissions:
 	 * 
 	 * Case 1) Creating security-type keys as defined by Keys.isAdminKey() A
 	 * user can create security-type keys if s/he is explicitly listed as an
 	 * ADMIN_USER by either the identifier, or the system (root) identifier.
 	 * 
 	 * Case 2) Creating other keys A user can create other keys if s/he is
 	 * listed as a WRITE_USER by the identifier, or if a WRITE_USERS key is not
 	 * configured by the identifier.
 	 * 
 	 * Identifier's ADMIN_USERS can create keys of any type. It is unnecessary
 	 * to list them as WRITE_USERS.
 	 */
 	public void createKeys(SecurityInfo secInfo, URI identifier, IdentifierData values)
 			throws InvalidIdentifierException, NamingAuthoritySecurityException, InvalidIdentifierValuesException,
 			NamingAuthorityConfigurationException
 	{
 
 		Boolean writerAccess = null;
 		Boolean adminAccess = null;
 
 		if (values == null || values.getKeys() == null || values.getKeys().length == 0)
 		{
 			throw new InvalidIdentifierValuesException("No keys were provided");
 		}
 
 		secInfo = validateSecurityInfo(secInfo);
 
 		URI localIdentifier = IdentifierUtil.getLocalName(prefix, identifier);
 		IdentifierMetadata resolvedValues = loadLocalIdentifier(localIdentifier);
 
 		Collection<IdentifierValueKey> valueKeys = resolvedValues.getValues();
 		for (String key : values.getKeys())
 		{
 
 			// Start of security checks
 			if (Keys.isAdminKey(key))
 			{
 
 				if (adminAccess == null)
 				{
 					adminAccess = hasIdentifierAdminUserAccess(secInfo, resolvedValues);
 				}
 
 				if (!adminAccess)
 				{
 					throw new NamingAuthoritySecurityException(SecurityUtil.securityError(secInfo, "create key [" + key
 							+ "]. Not an ADMIN_USER"));
 				}
 
 			}
 			else
 			{
 				if (writerAccess == null)
 				{
 					writerAccess = hasWriteUserAccess(secInfo, resolvedValues);
 				}
 
 				if (!writerAccess)
 				{
 					//
 					// Check if user is administrator
 					//
 					if (adminAccess == null)
 					{
 						adminAccess = hasIdentifierAdminUserAccess(secInfo, resolvedValues);
 					}
 
 					if (!adminAccess)
 					{
 						throw new NamingAuthoritySecurityException(SecurityUtil.securityError(secInfo,
 								"create keys. Neither WRITE_USER nor ADMIN_USER"));
 					}
 				}
 			}
 			// End of security checks
 
 			IdentifierValueKey ivk = IdentifierUtil.convert(key, values.getValues(key));
 			if (valueKeys.contains(ivk))
 			{
 				throw new InvalidIdentifierValuesException("Key [" + key + "] already exists for identifier ["
 						+ identifier.normalize().toString() + "]");
 			}
 			valueKeys.add(ivk);
 		}
 
 		save(resolvedValues);
 
 		if (SecurityUtil.isSystemIdentifier(localIdentifier))
 		{
 			replaceSystemValues(secInfo, resolvedValues);
 		}
 	}
 
 	/*
 	 * Deletes the provided keys from the given identifier.
 	 * 
 	 * An exception is thrown if any of the provided keys does not exist.
 	 * 
 	 * Permissions:
 	 * 
 	 * Case 1) Deleting security-type keys as defined by Keys.isAdminKey() A
 	 * user can delete security-type keys if s/he is explicitly listed as an
 	 * ADMIN_USER by either the identifier, or the system (root) identifier.
 	 * 
 	 * Case 2) Deleting other keys A user can delete other keys if s/he is
 	 * listed as a WRITE_USER by the identifier, or if a WRITE_USERS key is not
 	 * configured by the identifier.
 	 * 
 	 * Identifier's ADMIN_USERS can delete keys of any type. It is unnecessary
 	 * to list them as WRITE_USERS.
 	 */
 	public void deleteKeys(SecurityInfo secInfo, URI identifier, String[] keyList) throws InvalidIdentifierException,
 			NamingAuthoritySecurityException, InvalidIdentifierValuesException, NamingAuthorityConfigurationException
 	{
 
 		if (keyList == null || keyList.length == 0)
 		{
 			throw new InvalidIdentifierValuesException("No keys were provided");
 		}
 
 		secInfo = validateSecurityInfo(secInfo);
 
 		Boolean writerAccess = null;
 		Boolean adminAccess = null;
 		URI localIdentifier = IdentifierUtil.getLocalName(prefix, identifier);
 		IdentifierMetadata resolvedValues = loadLocalIdentifier(localIdentifier);
 
 		if (resolvedValues.getValues() == null || resolvedValues.getValues().size() == 0)
 		{
 			throw new InvalidIdentifierValuesException("Identifier [" + identifier + "] has no keys");
 		}
 
 		LOG.debug("User [" + secInfo.getUser() + "] deleting some keys for identifier [" + identifier.toString() + "]");
 
 		List<IdentifierValueKey> keysToDelete = new ArrayList<IdentifierValueKey>();
 		ArrayList<String> keyNames = new ArrayList<String>(Arrays.asList(keyList));
 
 		for (IdentifierValueKey ivk : resolvedValues.getValues())
 		{
 			if (!keyNames.contains(ivk.getKey()))
 			{
 				continue;
 			}
 
 			// Start of security checks
 			if (Keys.isAdminKey(ivk.getKey()))
 			{
 
 				if (adminAccess == null)
 				{
 					adminAccess = hasIdentifierAdminUserAccess(secInfo, resolvedValues);
 				}
 
 				if (!adminAccess)
 				{
 					throw new NamingAuthoritySecurityException(SecurityUtil.securityError(secInfo,
 							"delete key [" + ivk.getKey() + "]. Not an ADMIN_USER"));
 				}
 
 			}
 			else
 			{
 				if (writerAccess == null)
 				{
 					writerAccess = hasWriteUserAccess(secInfo, resolvedValues);
 				}
 
 				if (!writerAccess)
 				{
 					//
 					// Check if user is administrator
 					//
 					if (adminAccess == null)
 					{
 						adminAccess = hasIdentifierAdminUserAccess(secInfo, resolvedValues);
 					}
 
 					if (!adminAccess)
 					{
 						throw new NamingAuthoritySecurityException(SecurityUtil.securityError(secInfo,
 								"delete keys. Neither WRITE_USER nor ADMIN_USER"));
 					}
 				}
 			}
 			// End of security checks
 
 			LOG.debug("Removing key [" + ivk.getKey() + "]");
 			keysToDelete.add(ivk);
 		}
 
 		if (keysToDelete.size() != keyNames.size())
 		{
 			// Unrecognized key. Should we actually fail this?
 			String missingKeys = "";
 			for (String key : keyNames)
 			{
 				if (!keysToDelete.contains(key))
 				{
 					if (missingKeys.equals(""))
 						missingKeys += key;
 					else
 						missingKeys += ", " + key;
 				}
 			}
 			throw new InvalidIdentifierValuesException("Unexpected keys found in the request [" + missingKeys + "]");
 		}
 
 		if (keysToDelete.size() > 0)
 		{
 			getHibernateTemplate().deleteAll(keysToDelete);
 			resolvedValues.getValues().removeAll(keysToDelete);
 		}
 
 		save(resolvedValues);
 
 		if (SecurityUtil.isSystemIdentifier(localIdentifier))
 		{
 			replaceSystemValues(secInfo, resolvedValues);
 		}
 	}
 
 	/*
 	 * Replaces the values associated with existing keys with the new provided
 	 * values.
 	 * 
 	 * An exception is thrown if any of the provided keys does not exist
 	 * 
 	 * Permissions:
 	 * 
 	 * Identifier's ADMIN_USERS can replace any key values
 	 * 
 	 * Security-type key values can only be replaced by ADMIN_USERS
 	 * 
 	 * Regular users can replace the values for a key if s/he is listed as a
 	 * WRITE_USER for that key.
 	 * 
 	 * When no WRITE_USERS key is defined at the key level, the user must be
 	 * listed as a WRITE_USER for the identifier.
 	 * 
 	 * When no WRITE_USERS key is defined for either the key or the identifier,
 	 * any user can replace the values.
 	 */
 	public void replaceKeyValues(SecurityInfo secInfo, URI identifier, IdentifierValues newValues)
 			throws InvalidIdentifierException, NamingAuthoritySecurityException, InvalidIdentifierValuesException,
 			NamingAuthorityConfigurationException
 	{
 
 		if (newValues == null || newValues.getKeys() == null || newValues.getKeys().length == 0)
 		{
 			throw new InvalidIdentifierValuesException(Constant.NO_KEY_VALUES);
 		}
 
 		secInfo = validateSecurityInfo(secInfo);
 
 		URI localIdentifier = IdentifierUtil.getLocalName(prefix, identifier);
 		IdentifierMetadata resolvedValues = loadLocalIdentifier(localIdentifier);
 
 		if (resolvedValues.getValues() == null || resolvedValues.getValues().size() == 0)
 		{
 			throw new InvalidIdentifierValuesException("Identifier [" + identifier + "] has no keys");
 		}
 
 		Boolean identifierAdminAccess = hasIdentifierAdminUserAccess(secInfo, resolvedValues);
 		Boolean identifierWriteAccess = null;
 
 		ArrayList<String> keysToReplace = new ArrayList<String>(Arrays.asList(newValues.getKeys()));
 		for (IdentifierValueKey ivk : resolvedValues.getValues())
 		{
 
 			if (!keysToReplace.contains(ivk.getKey()))
 			{
 				continue;
 			}
 
 			if (!identifierAdminAccess)
 			{
 
 				if (Keys.isAdminKey(ivk.getKey()))
 				{
 					String error = SecurityUtil.securityError(secInfo, "replace key [" + ivk.getKey()
 							+ "]. Not an ADMIN_USER");
 					LOG.error(error);
 					throw new NamingAuthoritySecurityException(error);
 				}
 
 				//
 				// Check key level security using key's read-write identifier
 				//
 				Access keyAccess = getKeyWriteAccess(secInfo, ivk.getPolicyIdentifier());
 				if (keyAccess == Access.DENIED)
 				{
 					//
 					// WRITE_USERS defined for key and this user is not listed
 					//
 					String error = SecurityUtil.securityError(secInfo, "replace key [" + ivk.getKey()
 							+ "]. Not a WRITE_USER");
 					LOG.error(error);
 					throw new NamingAuthoritySecurityException(error);
 				}
 
 				if (keyAccess == Access.NOSECURITY)
 				{
 					//
 					// Fall back to identifier level security
 					//
 					if (identifierWriteAccess == null)
 					{
 						identifierWriteAccess = hasWriteUserAccess(secInfo, resolvedValues);
 					}
 
 					if (!identifierWriteAccess)
 					{
 						//
 						// WRITE_USERS defined identifier and this user is not
 						// listed
 						//
 						String error = SecurityUtil.securityError(secInfo, "replace key [" + ivk.getKey()
 								+ "]. Not a WRITE_USER");
 						LOG.error(error);
 						throw new NamingAuthoritySecurityException(error);
 					}
 				}
 			}
 			keysToReplace.remove(ivk.getKey());
 			KeyValues kvs = newValues.getValues(ivk.getKey());
 			ivk.setValues(kvs.getValues());
 		}
 
 		if (keysToReplace.size() > 0)
 		{
 			throw new InvalidIdentifierValuesException("Key [" + keysToReplace.get(0)
 					+ "] does not exist for identifier [" + identifier.normalize().toString() + "]");
 		}
 
 		save(resolvedValues);
 
 		// Save system identifier if necessary
 		if (SecurityUtil.isSystemIdentifier(localIdentifier))
 		{
 			replaceSystemValues(secInfo, resolvedValues);
 		}
 	}
 
 	/*
 	 * Adds administrator identity to system identifier
 	 */
 	public synchronized void createInitialAdministrator(String identity) throws NamingAuthorityConfigurationException
 	{
 
 		if (systemValues == null || systemValues.getValues() == null)
 		{
 			throw new NamingAuthorityConfigurationException("No system values. Please initialize DAO first");
 		}
 
 		IdentifierValueKey adminKey = null;
 		Collection<IdentifierValueKey> valCol = systemValues.getValues();
 		for (IdentifierValueKey ivk : valCol)
 		{
 			if (ivk.getKey().equals(Keys.ADMIN_USERS))
 			{
 				adminKey = ivk;
 				break;
 			}
 		}
 
 		if (adminKey == null)
 		{
 			adminKey = new IdentifierValueKey();
 			valCol.add(adminKey);
 		}
 
 		if (adminKey.getValues() != null && adminKey.getValues().size() > 0)
 		{
 			throw new NamingAuthorityConfigurationException("An administrator already exists");
 		}
 
 		ArrayList<String> values = new ArrayList<String>();
 		values.add(identity);
 		adminKey.setValues(values);
 		save(systemValues);
 	}
 
 	public String registerGSID(SecurityInfo secInfo, String suggestedIdentifier, String[] parentIdentifiers)
 			throws NamingAuthorityConfigurationException, InvalidIdentifierValuesException, InvalidIdentifierException,
 			NamingAuthoritySecurityException
 	{
 		checkSecurity(secInfo, false, true);
 		String userName = secInfo.getUser();
 		String publisherIdentifier = getIdentifierFromUser(userName);
 		String temp = null;
 		List<String> keys = new ArrayList<String>();
 		List<String> values = new ArrayList<String>();
 		if (suggestedIdentifier != null && suggestedIdentifier.length() > 0)
 		{
 			validateIdentifierPattern(suggestedIdentifier);
 		}
 		if (!checkIfParentsEmpty(parentIdentifiers))
 		{
 			for (String parent : parentIdentifiers)
 				LOG.warn("the parent is " + parent);
 			keys.add("parent");
 			String value = "";
 			String total_value = "";
 			for (int i = 0; i < parentIdentifiers.length; i++)
 			{
 				value = parentIdentifiers[i];
 				// try
 				// {
 				// validateIdentifierPattern(value);
 				// }
 				// catch(InvalidIdentifierException e)
 				// {
 				// LOG.warn("invalid value for parent identifier \n"+value+"\n"+parentIdentifiers.length);
 				// throw new
 				// InvalidIdentifierException("Parent identifier cannot be null.");
 				// }
 				// out.println(key+" "+value);
 				total_value += value;
 				if (i < parentIdentifiers.length - 1)
 				{
 					total_value += ",";
 				}
 			}
 			values.add(total_value);
 		}
 		keys.add("TYPE");
 		values.add("GSID");
 		temp = suggestedIdentifier = createIdentifier(secInfo, suggestedIdentifier, keys, values);
 
 		if (suggestedIdentifier != null)
 		{
 			// second transaction.
 			keys = new ArrayList<String>();
 			values = new ArrayList<String>();
 
 			keys.add("PUBLISHER");
 			values.add(publisherIdentifier);
 			keys.add("GSID");
 			values.add(suggestedIdentifier);
 			keys.add("TYPE");
 			values.add("SITEDATA");
 			createIdentifier(secInfo, null, keys, values);
 		}
 		return temp;
 
 	}
 
 	public void registerSite(SecurityInfo secInfo, String application, String applicationURL,
 			String applicationVersion, String contactName, String contactEmail, String contactPhone, String organization)
 			throws NamingAuthorityConfigurationException, InvalidIdentifierValuesException, InvalidIdentifierException,
 			NamingAuthoritySecurityException
 	{
 		checkSecurity(secInfo, false, false);
 		List<String> keys = new ArrayList<String>();
 		List<String> values = new ArrayList<String>();
 		String[] tempValues = { application, applicationURL, applicationVersion, contactEmail, contactName,
 				contactPhone, organization, secInfo.getUser() };
 		String[] fixedKeys = { "application", "applicationUrl", "applicationVersion", "contactEmail", "contactName",
 				"contactPhone", "organization", "userName" };
 		for (int i = 0; i < fixedKeys.length; i++)
 		{
 			if (tempValues[i] == null || tempValues[i].trim().length() == 0)
 			{
 				throw new InvalidIdentifierException();
 			}
 			keys.add(fixedKeys[i]);
 			values.add(tempValues[i]);
 		}
 		keys.add("TYPE");
 		values.add("SITE");
 		createIdentifier(secInfo, null, keys, values);
 	}
 
 	public void addSite(SecurityInfo secInfo, String identifier) throws NamingAuthorityConfigurationException,
 			InvalidIdentifierValuesException, InvalidIdentifierException, NamingAuthoritySecurityException
 	{
 		checkSecurity(secInfo, true, true);
 		if (identifier == null || identifier.length() == 0)
 		{
 			throw new InvalidIdentifierException("Identifier cannot be null.");
 		}
 		validateIdentifierPattern(identifier);
 		URI identifierURI = null;
 		try
 		{
 			identifierURI = new URI(identifier);
 		}
 		catch (URISyntaxException e)
 		{
 			throw new InvalidIdentifierException("Unable to parse identifier.");
 		}
 		IdentifierMetadata imd = loadLocalIdentifier(identifierURI);
 		if (imd == null)
 		{
 			throw new InvalidIdentifierException("Identifier does not exists.");
 		}
 		String userName = secInfo.getUser();
 		String publisherIdentifier = getIdentifierFromUser(userName);
 
 		List<String> keys = new ArrayList<String>();
 		List<String> values = new ArrayList<String>();
 		keys.add("TYPE");
 		values.add("SITEDATA");
 		keys.add("PUBLISHER");
 		values.add(publisherIdentifier);
 
 		keys.add("GSID");
 		values.add(identifier);
 
 		String temp = createIdentifier(secInfo, null, keys, values);
 	}
 
 	public boolean validateIdentifier(SecurityInfo secInfo, String identifier) throws InvalidIdentifierException,
 			NamingAuthoritySecurityException, NamingAuthorityConfigurationException
 	{
 		if (identifier == null)
 		{
 			throw new InvalidIdentifierException("Identifier cannot be null.");
 		}
 		validateIdentifierPattern(identifier);
 		try
 		{
 			URI temp = new URI(identifier);
 			try
 			{
 				IdentifierMetadata metaData = loadLocalIdentifier(temp);
 				if (metaData != null)
 					return false;
 			}
 			catch (Exception e)
 			{
 				return true;
 			}
 		}
 		catch (URISyntaxException e)
 		{
 			throw new InvalidIdentifierException("Identifier cannot parsed.");
 		}
 
 		return true;
 	}
 
 	public Tree getParentHierarchy(SecurityInfo secInfo, String identifier) throws InvalidIdentifierException,
 			NamingAuthoritySecurityException, NamingAuthorityConfigurationException
 	{
 		if (identifier == null || identifier.length() == 0)
 		{
 			throw new InvalidIdentifierException("Identifier cannot be null");
 		}
 		validateIdentifierPattern(identifier);
 		List<Tree> parents = new ArrayList<Tree>();
 
 		Tree currentNode = new Tree(identifier);
 		Tree rootNode = currentNode;
 		parents.add(currentNode);
 		while (parents.size() != 0)
 		{
 			currentNode = parents.get(0);
 			String tempIdentifier = (currentNode).getIdentifier();
 			parents.remove(0);
 			IdentifierData currentKeyData = null;
 			try
 			{
 				URI uri = null;
 				try
 				{
 					uri = new java.net.URI(prefix + tempIdentifier);
 				}
 				catch (Exception e)
 				{
 				}
 				if (uri != null)
 				{
 					currentKeyData = getIdentifierData(secInfo, uri, "parent");
 				}
 			}
 			catch (InvalidIdentifierValuesException e1)
 			{
 				tempIdentifier = null;
 			}
 			if (currentKeyData != null)
 			{
 				if (currentKeyData.getKeys() != null)
 				{
 					for (String key : currentKeyData.getKeys())
 					{
 						KeyData values = currentKeyData.getValues(key);
 						// parent should have only one value so get the first
 						// one.
 						if (values != null)
 						{
 							for (String value : values.getValues())
 							{
 								String tempURI = null;
 								try
 								{
 									if (value != null)
 									{
 										tempURI = value;
 									}
 									else
 									{
 										LOG.debug("values is null");
 									}
 								}
 								catch (Exception e)
 								{
 									e.printStackTrace();
 								}
 								if (tempURI != null)
 								{
 									Tree tempNode = new Tree(tempURI);
 									currentNode.addChild(tempNode);
 									parents.add(tempNode);
 								}
 							}
 						}
 						else
 						{
 							LOG.debug("values are null");
 						}
 					}
 				}
 				else
 				{
 					LOG.debug("getKeys is null");
 				}
 			}
 			else
 			{
 				LOG.debug("currentkeydata is null");
 			}
 		}
 		LOG.debug("the tree is " + rootNode.toString(true));
 		return rootNode;
 	}
 
 	public Tree getChildHierarchy(SecurityInfo secInfo, String identifier) throws InvalidIdentifierException,
 			NamingAuthoritySecurityException, InvalidIdentifierValuesException, NamingAuthorityConfigurationException
 	{
 		if (identifier == null || identifier.length() == 0)
 		{
 			throw new InvalidIdentifierException("Identifier cannot be null");
 		}
 		validateIdentifierPattern(identifier);
 		List<Tree> parents = new ArrayList<Tree>();
 		Tree currentNode = new Tree(identifier);
 		Tree rootNode = currentNode;
 		parents.add(currentNode);
 		while (parents.size() != 0)
 		{
 			currentNode = parents.get(0);
 			String tempIdentifier = currentNode.getIdentifier();
 			parents.remove(0);
 			List<IdentifierMetadata> currentChildren = getChildItems(secInfo, tempIdentifier);
 			if (currentChildren != null)
 			{
 				if (currentChildren.size() > 0)
 				{
 					for (IdentifierMetadata child : currentChildren)
 					{
 						String key = child.getLocalIdentifier().toString();
 						// parent should have only one value so get the first
 						// one.
 						if (key != null)
 						{
 							LOG.debug("the key is " + key);
 							Tree tempNode = new Tree(key);
 							currentNode.addChild(tempNode);
 							parents.add(tempNode);
 						}
 						else
 						{
 							LOG.debug("values are null");
 						}
 					}
 				}
 				else
 				{
 					LOG.debug("current children size is zero");
 				}
 			}
 			else
 			{
 				LOG.debug("current child is null");
 			}
 		}
 		LOG.debug("the tree is " + rootNode.toString(true));
 		return rootNode;
 	}
 
 	public List<IdentifierMetadata> getChildItems(SecurityInfo secInfo, String identifier)
 			throws InvalidIdentifierException, NamingAuthoritySecurityException, InvalidIdentifierValuesException,
 			NamingAuthorityConfigurationException
 	{
 		secInfo = validateSecurityInfo(secInfo);
 
 		List<IdentifierMetadata> children = null;
 		List<IdentifierMetadata> results = getHibernateTemplate()
 				.find("SELECT md FROM "
 						+ domainClass().getName()
 						+ " as md INNER JOIN md.values as value INNER JOIN value.values as val WHERE value.key='parent' and val=?",
 						new Object[] { identifier });
 		LOG.debug("The results Size is " + results.size());
 		return results;
 	}
 
 	public List<IdentifierData> getSitesData(SecurityInfo secInfo, URI naPrefix) throws InvalidIdentifierException,
 			NamingAuthoritySecurityException, InvalidIdentifierValuesException, NamingAuthorityConfigurationException
 	{
 		secInfo = validateSecurityInfo(secInfo);
 
 		List<IdentifierMetadata> results = getHibernateTemplate()
 				.find("SELECT md FROM "
 						+ domainClass().getName()
 						+ " as md INNER JOIN md.values as value INNER JOIN value.values as val WHERE value.key='TYPE' and val='SITE'");
 		if (results != null)
 		{
 			LOG.debug("The results Size is " + results.size());
 		}
 		List<IdentifierData> resultData = new ArrayList<IdentifierData>();
 		if (results != null)
 		{
 			for (IdentifierMetadata result : results)
 			{
 				if (result != null)
 				{
 					IdentifierData tempData = IdentifierUtil.convert(result.getValues());
 					if (tempData != null)
 					{
 						String identifierValue = result.getLocalIdentifier().toString();
 						KeyData tempKeyData = new KeyData(tempData.getValues(tempData.getKeys()[0])
 								.getPolicyIdentifier(), new String[] { identifierValue });
 						tempData.put("IDENTIFIER", tempKeyData);
 						resultData.add(tempData);
 					}
 				}
 			}
 		}
 		return resultData;
 	}
 
 	private boolean checkIfParentsEmpty(String[] parents)
 	{
 		boolean flag = false;
 		if (parents != null && parents.length > 0)
 		{
 			for (String parent : parents)
 			{
 				if (parent == null || parent.trim().length() == 0)
 				{
 					flag = true;
 					break;
 				}
 			}
 		}
 		else
 		{
 			flag = true;
 		}
 		return flag;
 	}
 
 	public String getIdentifierFromUser(String userName)
 	{
 		if (userName == null)
 			return null;
 		String temp = null;
 		List<IdentifierMetadata> results = getHibernateTemplate()
 				.find("SELECT md FROM "
 						+ domainClass().getName()
 						+ " as md INNER JOIN md.values as value INNER JOIN value.values as val WHERE value.key='userName' and val=?",
 						new Object[] { userName.trim() });
 		if (results != null && results.size() == 1)
 		{
 			URI tempGURI = results.get(0).getLocalIdentifier();
 			if (tempGURI != null)
 			{
 				temp = tempGURI.toString();
 			}
 		}
 		return temp;
 	}
 
 	public boolean checkIfIdentifierExists(URI identifier)
 	{
 		List<IdentifierMetadata> results = getHibernateTemplate().find(
 				"SELECT md FROM " + domainClass().getName() + " md WHERE md.localIdentifier = ?",
 				new Object[] { identifier });
 		return results == null || results.size() == 0 ? false : true;
 	}
 
 	public void checkSecurity(SecurityInfo secInfo, boolean checkOnlyLogin, boolean checkHasSite)
 			throws NamingAuthoritySecurityException
 	{
 		String caller = secInfo.getUser();
 		if (caller == null || caller.length() == 0)
 		{
 			throw new NamingAuthoritySecurityException("Please login into the grid to identify yourselves");
 		}
 		if (!checkOnlyLogin)
 		{
 			// check for a group.
 		}
 		String Identifier = getIdentifierFromUser(caller);
 		if (checkHasSite)
 		{
 			if (Identifier == null)
 			{
 				throw new NamingAuthoritySecurityException("Please register a site before you proceed");
 			}
 		}
 		else
 		{
 			if (Identifier != null)
 			{
 				throw new NamingAuthoritySecurityException("You have already registered a site");
 			}
 		}
 	}
 
 	/*
 	 * Private Stuff
 	 */
 
 	private String createIdentifier(SecurityInfo secInfo, String identifier, List<String> keys, List<String> values)
 			throws InvalidIdentifierException, NamingAuthorityConfigurationException, NamingAuthoritySecurityException
 	{
 
 		String temp = null;
 
 		String[] keyArray = new String[keys.size()];
 		String[][] valueArray = new String[keys.size()][];
 		for (int i = 0; i < keys.size(); i++)
 		{
 			keyArray[i] = keys.get(i);
 			String value = values.get(i);
 			String[] valueArr;
 			if (value != null && value.contains(","))
 			{
 				valueArr = value.split(",");
 			}
 			else
 			{
 				valueArr = new String[1];
 				valueArr[0] = value;
 			}
 			if (keys.get(i).contains("parent"))
 			{
 				LOG.debug("the values are " + valueArr);
 				for (String valueAr1 : valueArr)
 				{
 
 					// org.apache.axis.types.URI axisUri = getAxisURI(valueAr1,
 					// builder);
 					LOG.debug("value Ar1 " + valueAr1);
 					try
 					{
 						URI valueAr1URI = new URI(valueAr1);
 						valueAr1URI = IdentifierUtil.build(prefix, valueAr1URI);
 						IdentifierData data = resolveIdentifier(secInfo, valueAr1URI);
 
 					}
 					catch (URISyntaxException e)
 					{
 						LOG.debug("URI casting from String execption " + valueAr1);
 						throw new InvalidIdentifierException();
 					}
 					catch (NamingAuthorityConfigurationException e)
 					{
 						LOG.debug("NamingAuthorityConfigurationException occured during resolving Identifier");
 						throw new InvalidIdentifierException();
 					}
 					catch (InvalidIdentifierException e)
 					{
 						LOG.debug("InvalidIdentifierException occured during resolving Identifier");
 						throw new InvalidIdentifierException();
 					}
 					catch (NamingAuthoritySecurityException e)
 					{
 						LOG.debug("NamingAuthoritySecurityException occured during resolving Identifier");
 						throw new NamingAuthoritySecurityException();
 					}
 
 				}
 			}
 			valueArray[i] = valueArr;
 		}
 
 		org.cagrid.identifiers.namingauthority.domain.IdentifierData ivs = new org.cagrid.identifiers.namingauthority.domain.IdentifierData();
 		for (int i = 0; i < keyArray.length; i++)
 		{
 			URI policyReference = null;
 			KeyData kd = new KeyData(policyReference, valueArray[i]);
 			ivs.put(keyArray[i], kd);
 		}
 		URI identifierURI = null;
 		if (identifier == null)
 		{
 			identifier = (new IdentifierGeneratorImpl()).generate(null).toString();
 		}
 		else
 		{
 			try
 			{
 				while (checkIfIdentifierExists(new URI(identifier)))
 				{
 					identifier = (new IdentifierGeneratorImpl()).generate(null).toString();
 				}
 			}
 			catch (URISyntaxException e)
 			{
 				// TODO Auto-generated catch block
 				LOG.warn("URI Syntax exception occured");
 				throw new InvalidIdentifierException("URI is Invalid");
 			}
 		}
 
 		try
 		{
 			identifierURI = new URI(identifier);
 		}
 		catch (URISyntaxException e)
 		{
 			LOG.debug("URI sytanx exception occured by identifier " + identifier);
 		}
 		try
 		{
 			createIdentifier(secInfo, identifierURI, ivs);
 			temp = identifierURI.toString();
 		}
 		catch (NamingAuthorityConfigurationException e)
 		{
 			LOG.debug("Naming authority configuration exception occured");
 			throw new NamingAuthorityConfigurationException();
 		}
 		catch (InvalidIdentifierException e)
 		{
 			LOG.debug("Invalid Identifier Exception occured");
 			throw new InvalidIdentifierException();
 		}
 		catch (NamingAuthoritySecurityException e)
 		{
 			LOG.debug("Naming Authority Security Exception occured");
 			throw new NamingAuthoritySecurityException();
 		}
 		return temp;
 	}
 
 	/*
 	 * A user can create identifiers if any one of the below conditions are met:
 	 * 
 	 * (a) PUBLIC_CREATTION key set to "Y" in root identifier (b) User is listed
 	 * by IDENTIFIER_CREATION_USERS key in root identifier (c) No security
 	 * settings are specified - No root identifier - No PUBLIC_CREATION key in
 	 * root identifier
 	 */
 	private synchronized void createIdentifierSecurityChecks(SecurityInfo secInfo) throws InvalidIdentifierException,
 			NamingAuthorityConfigurationException, NamingAuthoritySecurityException
 	{
 
 		List<String> values = SecurityUtil.getPublicCreation(systemValues);
 		if (values == null || values.size() == 0)
 		{
 			// no security
 			LOG.debug("SECURITY: No PUBLIC_CREATION");
 			return;
 		}
 
 		if (values.size() != 1)
 		{
 			throw new NamingAuthorityConfigurationException("Bad PUBLIC_CREATION setting detected");
 		}
 
 		if (values.get(0).equalsIgnoreCase(SecurityUtil.PUBLIC_CREATION_YES))
 		{
 			// everyone can create identifiers
 			return;
 		}
 
 		List<String> authorizedUsers = SecurityUtil.getIdentifierCreationUsers(systemValues);
 		if (authorizedUsers == null || !authorizedUsers.contains(secInfo.getUser()))
 		{
 			throw new NamingAuthoritySecurityException(SecurityUtil.securityError(secInfo, "create identifiers"));
 		}
 	}
 
 	/*
 	 * Returns any directly specified READ_USERS plus any included by
 	 * READWRITE_IDENTIFIERS
 	 */
 	private List<String> getAllReadUsers(IdentifierMetadata values) throws InvalidIdentifierException,
 			NamingAuthorityConfigurationException
 	{
 
 		List<String> readUsers = SecurityUtil.getReadUsers(values);
 		List<String> otherReadUsers = getReadUsersFromReadWriteIdentifiers(values);
 
 		if (readUsers == null)
 		{
 			return otherReadUsers;
 		}
 
 		if (otherReadUsers != null)
 		{
 			readUsers.addAll(otherReadUsers);
 		}
 
 		return readUsers;
 	}
 
 	private boolean hasIdentifierAdminUserAccess(SecurityInfo secInfo, IdentifierMetadata values)
 			throws InvalidIdentifierException, NamingAuthorityConfigurationException
 	{
 
 		if (hasAdminUserAccess(secInfo, values) || hasSystemAdminUserAccess(secInfo))
 		{
 			return true;
 		}
 
 		return false;
 	}
 
 	// Checks if user is in ADMIN_USERS in system identifier
 	private synchronized boolean hasSystemAdminUserAccess(SecurityInfo secInfo)
 	{
 		if (userAccess(secInfo.getUser(), SecurityUtil.getAdminUsers(systemValues)) == Access.GRANTED)
 		{
 			return true;
 		}
 		return false;
 	}
 
 	//
 	// Checks whether user is an ADMIN_USER
 	//
 	private boolean hasAdminUserAccess(SecurityInfo secInfo, IdentifierMetadata values)
 			throws InvalidIdentifierException, NamingAuthorityConfigurationException
 	{
 
 		// Check locally defined ADMIN_USERS list
 		Access access = userAccess(secInfo.getUser(), SecurityUtil.getAdminUsers(values));
 		if (access == Access.GRANTED)
 		{
 			// No further checks needed
 			return true;
 		}
 
 		// Check ADMIN_USERS defined by ADMIN_IDENTIFIERS
 		Access rwAccess = userAccess(secInfo.getUser(), getAdminUsersFromAdminIdentifiers(values));
 		if (rwAccess == Access.GRANTED)
 		{
 			return true;
 		}
 
 		return false;
 	}
 
 	/*
 	 * Checks whether user has WRITE_USER permission. Default is "true" if there
 	 * are no security settings (no WRITE_USERS key defined)
 	 */
 	private boolean hasWriteUserAccess(SecurityInfo secInfo, IdentifierMetadata values)
 			throws InvalidIdentifierException, NamingAuthorityConfigurationException
 	{
 
 		// Check locally defined WRITE_USERS list
 		Access access = userAccess(secInfo.getUser(), SecurityUtil.getWriteUsers(values));
 		if (access == Access.GRANTED)
 		{
 			return true;
 		}
 
 		// Check WRITE_USERS defined by READWRITE_IDENTIFIERS
 		Access rwAccess = userAccess(secInfo.getUser(), getWriteUsersFromReadWriteIdentifiers(values));
 		if (rwAccess != Access.NOSECURITY)
 		{
 			access = rwAccess;
 		}
 
 		if (access == Access.DENIED)
 		{
 			return false;
 		}
 
 		// Access is granted in the absence of a WRITE_USERS key
 		return true;
 	}
 
 	private Access userAccess(String requestingUser, List<String> authorizedUsers)
 	{
 		if (authorizedUsers == null)
 		{
 			return Access.NOSECURITY;
 		}
 
 		if (authorizedUsers.contains(requestingUser))
 		{
 			return Access.GRANTED;
 		}
 
 		return Access.DENIED;
 	}
 
 	//
 	// Returns WRITE_USERS listed by any READWRITE_IDENTIFIERS
 	//
 	private List<String> getWriteUsersFromReadWriteIdentifiers(IdentifierMetadata values)
 			throws InvalidIdentifierException, NamingAuthorityConfigurationException
 	{
 
 		List<String> writers = null;
 
 		List<String> rwIdentifiers = SecurityUtil.getReadWriteIdentifiers(values);
 		if (rwIdentifiers != null)
 		{
 			for (String identifier : rwIdentifiers)
 			{
 				List<String> writeUsers = SecurityUtil.getWriteUsers(loadSecurityIdentifier(identifier));
 				if (writeUsers != null)
 				{
 					if (writers == null)
 					{
 						writers = new ArrayList<String>();
 					}
 					writers.addAll(writeUsers);
 				}
 			}
 		}
 
 		return writers;
 	}
 
 	//
 	// Returns READ_USERS listed by any READWRITE_IDENTIFIERS
 	//
 	private List<String> getReadUsersFromReadWriteIdentifiers(IdentifierMetadata values)
 			throws InvalidIdentifierException, NamingAuthorityConfigurationException
 	{
 
 		List<String> readers = null;
 
 		List<String> rwIdentifiers = SecurityUtil.getReadWriteIdentifiers(values);
 		if (rwIdentifiers != null)
 		{
 			for (String identifier : rwIdentifiers)
 			{
 				List<String> readUsers = SecurityUtil.getReadUsers(loadSecurityIdentifier(identifier));
 				if (readUsers != null)
 				{
 					if (readers == null)
 					{
 						readers = new ArrayList<String>();
 					}
 					readers.addAll(readUsers);
 				}
 			}
 		}
 
 		return readers;
 	}
 
 	//
 	// Returns ADMIN_USERS listed by any ADMIN_IDENTIFIERS
 	//
 	private List<String> getAdminUsersFromAdminIdentifiers(IdentifierMetadata values)
 			throws InvalidIdentifierException, NamingAuthorityConfigurationException
 	{
 
 		List<String> allAdmins = null;
 
 		List<String> adminIdentifiers = SecurityUtil.getAdminIdentifiers(values);
 		if (adminIdentifiers != null)
 		{
 			for (String identifier : adminIdentifiers)
 			{
 				List<String> admins = SecurityUtil.getAdminUsers(loadSecurityIdentifier(identifier));
 
 				if (admins != null)
 				{
 					if (allAdmins == null)
 					{
 						allAdmins = new ArrayList<String>();
 					}
 					allAdmins.addAll(admins);
 				}
 			}
 		}
 
 		return allAdmins;
 	}
 
 	//
 	// Looks at READ security settings at the key level.
 	// Key security is determined by looking at the READWRITE_IDENTIFIER
 	// attached to the key.
 	//
 	private Access getKeyReadAccess(SecurityInfo secInfo, URI rwIdentifier) throws InvalidIdentifierException,
 			NamingAuthorityConfigurationException
 	{
 
 		if (rwIdentifier == null || rwIdentifier.normalize().toString().length() == 0)
 		{
 			// no security at key level
 			return Access.NOSECURITY;
 		}
 
 		List<String> readers = SecurityUtil.getReadUsers(loadIdentifier(rwIdentifier));
 
 		return userAccess(secInfo.getUser(), readers);
 	}
 
 	//
 	// Looks at WRITE security settings at the key level.
 	// Key security is determined by looking at the READWRITE_IDENTIFIER
 	// attached to the key.
 	//
 	private Access getKeyWriteAccess(SecurityInfo secInfo, URI rwIdentifier) throws InvalidIdentifierException,
 			NamingAuthorityConfigurationException
 	{
 
 		if (rwIdentifier == null || rwIdentifier.normalize().toString().length() == 0)
 		{
 			// no security at key level
 			return Access.NOSECURITY;
 		}
 
 		List<String> writers = SecurityUtil.getWriteUsers(loadIdentifier(rwIdentifier));
 
 		return userAccess(secInfo.getUser(), writers);
 	}
 
 	private synchronized void replaceSystemValues(SecurityInfo secInfo, IdentifierMetadata resolvedValues)
 	{
 		systemValues = resolvedValues;
 		LOG.debug("System identifier updated by [" + secInfo.getUser() + "]");
 	}
 
 	private synchronized void createSystemIdentifier()
 	{
 		systemValues = new IdentifierMetadata();
 		Collection<IdentifierValueKey> valCol = new ArrayList<IdentifierValueKey>();
 		systemValues.setLocalIdentifier(SecurityUtil.LOCAL_SYSTEM_IDENTIFIER);
 		systemValues.setValues(valCol);
 
 		//
 		// PUBLIC_CREATION is true by default
 		//
 		valCol.add(new IdentifierValueKey(Keys.PUBLIC_CREATION, new String[] { SecurityUtil.PUBLIC_CREATION_YES }, null));
 
 		//
 		// ADMIN_USERS is an empty list by default
 		//
 		valCol.add(new IdentifierValueKey(Keys.ADMIN_USERS, new String[] {}, null));
 		save(systemValues);
 	}
 
 	private SecurityInfo validateSecurityInfo(SecurityInfo secInfo)
 	{
 		if (secInfo == null || secInfo.getUser() == null)
 		{
 			return new SecurityInfoImpl("");
 		}
 
 		return secInfo;
 	}
 
 	private IdentifierMetadata loadSecurityIdentifier(String identifier) throws InvalidIdentifierException,
 			NamingAuthorityConfigurationException
 	{
 
 		try
 		{
 			return loadIdentifier(new URI(identifier));
 		}
 		catch (URISyntaxException e)
 		{
 			LOG.error(IdentifierUtil.getStackTrace(e));
 			throw new NamingAuthorityConfigurationException("Referred security identifier is bad [" + identifier + "]");
 		}
 	}
 
 	private void validateIdentifierPattern(String identifier) throws InvalidIdentifierException
 	{
 
 		if (identifier == null || identifier.length() == 0)
 		{
 			return;
 		}
 		try
 		{
 			UUID temp = UUID.fromString(identifier);
 		}
 		catch (IllegalArgumentException e)
 		{
 			LOG.warn("Invalid Identifier " + identifier);
 			throw new InvalidIdentifierException("identifier is not valid");
 		}
 	}
 }
