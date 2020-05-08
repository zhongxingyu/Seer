 /*
 * JBoss, a division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 
 package org.picketlink.idm.impl.store.ldap;
 
 import org.picketlink.idm.api.cfg.IdentityConfigurationRegistry;
 import org.picketlink.idm.common.exception.IdentityException;
 import org.picketlink.idm.impl.NotYetImplementedException;
 import org.picketlink.idm.impl.api.SimpleAttribute;
 import org.picketlink.idm.impl.helper.Tools;
 import org.picketlink.idm.impl.model.ldap.LDAPIdentityObjectImpl;
 import org.picketlink.idm.impl.model.ldap.LDAPIdentityObjectRelationshipImpl;
 import org.picketlink.idm.impl.store.FeaturesMetaDataImpl;
 import org.picketlink.idm.impl.types.SimpleIdentityObject;
 import org.picketlink.idm.spi.cache.IdentityStoreCacheProvider;
 import org.picketlink.idm.spi.configuration.IdentityStoreConfigurationContext;
 import org.picketlink.idm.spi.configuration.metadata.IdentityObjectAttributeMetaData;
 import org.picketlink.idm.spi.configuration.metadata.IdentityObjectTypeMetaData;
 import org.picketlink.idm.spi.configuration.metadata.IdentityStoreConfigurationMetaData;
 import org.picketlink.idm.spi.exception.OperationNotSupportedException;
 import org.picketlink.idm.spi.model.IdentityObject;
 import org.picketlink.idm.spi.model.IdentityObjectAttribute;
 import org.picketlink.idm.spi.model.IdentityObjectCredential;
 import org.picketlink.idm.spi.model.IdentityObjectRelationship;
 import org.picketlink.idm.spi.model.IdentityObjectRelationshipType;
 import org.picketlink.idm.spi.model.IdentityObjectType;
 import org.picketlink.idm.spi.search.IdentityObjectSearchCriteria;
 import org.picketlink.idm.spi.store.FeaturesMetaData;
 import org.picketlink.idm.spi.store.IdentityObjectSearchCriteriaType;
 import org.picketlink.idm.spi.store.IdentityStore;
 import org.picketlink.idm.spi.store.IdentityStoreInvocationContext;
 import org.picketlink.idm.spi.store.IdentityStoreSession;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import javax.naming.CompositeName;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.Name;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.BasicAttribute;
 import javax.naming.directory.BasicAttributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 import javax.naming.ldap.*;
 
 import static org.picketlink.idm.impl.store.ldap.SimpleLDAPIdentityStoreConfiguration.MAX_SEARCH_RESULTS;
 import static org.picketlink.idm.impl.store.ldap.SimpleLDAPIdentityStoreConfiguration.MAX_SEARCH_RESULTS_DEFAULT;
 
 /**
  * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
  * @version : 0.1 $
  */
 public class LDAPIdentityStoreImpl implements IdentityStore
 {
 
    //TODO: JNDI connection credentials encoding (pluggable?)
 
    private static Logger log = Logger.getLogger(LDAPIdentityStoreImpl.class.getName());
 
    private final String id;
 
    private IdentityStoreCacheProvider cache;
 
    public static final String MEMBERSHIP_TYPE = "JBOSS_IDENTITY_MEMBERSHIP";
 
    private FeaturesMetaData supportedFeatures;
 
   private int maxResults = 0;

    LDAPIdentityStoreConfiguration configuration;
 
    IdentityStoreConfigurationMetaData configurationMD;
 
    private final Set<IdentityObjectSearchCriteriaType> supportedSearchCriteriaTypes =
       new HashSet<IdentityObjectSearchCriteriaType>();
 
    // <IdentityObjectType name, <Attribute name, MD>
    private Map<String, Map<String, IdentityObjectAttributeMetaData>> attributesMetaData = new HashMap<String, Map<String, IdentityObjectAttributeMetaData>>();
 
     public LDAPIdentityStoreImpl(String id)
    {
       this.id = id;
    }
 
    public void bootstrap(IdentityStoreConfigurationContext configurationContext) throws IdentityException
    {
       if (configurationContext == null)
       {
          throw new IllegalArgumentException("Configuration context is null");
       }
 
       this.configurationMD = configurationContext.getStoreConfigurationMetaData();
 
       configuration = new SimpleLDAPIdentityStoreConfiguration(configurationMD);
 
       Set<String> readOnlyObjectTypes = new HashSet<String>();
 
       for (IdentityObjectType identityObjectType : configuration.getConfiguredTypes())
       {
          if (!configuration.getTypeConfiguration(identityObjectType.getName()).isAllowCreateEntry())
          {
             readOnlyObjectTypes.add(identityObjectType.getName());
          }
       }
 
       supportedSearchCriteriaTypes.clear();
       if (configuration.isSortExtensionSupported())
       {
          supportedSearchCriteriaTypes.add(IdentityObjectSearchCriteriaType.SORT);
       }
       supportedSearchCriteriaTypes.add(IdentityObjectSearchCriteriaType.PAGE);
       supportedSearchCriteriaTypes.add(IdentityObjectSearchCriteriaType.NAME_FILTER);
 
 
       supportedFeatures = new FeaturesMetaDataImpl(configurationMD, supportedSearchCriteriaTypes, false, false, readOnlyObjectTypes);
 
       // Attribute mappings - helper structures
 
       for (IdentityObjectTypeMetaData identityObjectTypeMetaData : configurationMD.getSupportedIdentityTypes())
       {
          Map<String, IdentityObjectAttributeMetaData> metadataMap = new HashMap<String, IdentityObjectAttributeMetaData>();
          for (IdentityObjectAttributeMetaData attributeMetaData : identityObjectTypeMetaData.getAttributes())
          {
             metadataMap.put(attributeMetaData.getName(), attributeMetaData);
          }
 
          attributesMetaData.put(identityObjectTypeMetaData.getName(), metadataMap);
 
       }
 
       if (configuration.isCreateMissingContexts())
       {
          // Get all configured DNs
          Set<String> dns = new HashSet<String>();
 
          if (configuration.getRelationshipNamesCtxDNs() != null)
          {
             for (String dn : configuration.getRelationshipNamesCtxDNs())
             {
                dns.add(dn);
             }
          }
 
          for (LDAPIdentityObjectTypeConfiguration typeCfg : configuration.getTypesConfiguration().values())
          {
             for (String dn : typeCfg.getCtxDNs())
             {
                dns.add(dn);
             }
          }
 
          DirContext ctx = (DirContext)createIdentityStoreSession().getSessionContext();
 
          try
          {
 
             for (String dn : dns)
             {
                checkCtx(ctx, dn);
             }
          }
          catch (Exception e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Cannot create entries in LDAP during store initialization: " + e);
          }
          finally
          {
             try
             {
                ctx.close();
             }
             catch (NamingException e)
             {
                if (log.isLoggable(Level.FINER))
                {
                   log.log(Level.FINER, "Exception occurred: ", e);
                }
 
                throw new IdentityException("Cannot close LDAP connection: ", e);
             }
          }
       }
 
       // Cache
 
       // Cache
 
       Map<String, String> cacheProps = new HashMap<String, String>();
       String cacheClassName = null;
       String cacheRegistryName = null;
 
       // Parse all 'cache.' prefixed options
       for (String key : configurationMD.getOptions().keySet())
       {
          if (key.startsWith("cache."))
          {
             if (configurationMD.getOptions().get(key).size() > 0)
             {
                cacheProps.put(key, configurationMD.getOptions().get(key).get(0));
             }
             if (key.equals("cache.providerClass") && configurationMD.getOptions().get(key).size() > 0)
             {
                cacheClassName = configurationMD.getOptions().get(key).get(0);
             }
 
             if (key.equals("cache.providerRegistryName") && configurationMD.getOptions().get(key).size() > 0)
             {
                cacheRegistryName = configurationMD.getOptions().get(key).get(0);
             }
          }
       }
 
       IdentityStoreCacheProvider provider = null;
 
       if (cacheRegistryName != null)
       {
          try
          {
             provider = (IdentityStoreCacheProvider)configurationContext.
                getConfigurationRegistry().getObject(cacheRegistryName);
          }
          catch (Exception e)
          {
             throw new IdentityException("Cannot find IdentityStoreCacheProvider in ConfigurationRegistry using " +
                "provided name:" + cacheRegistryName, e);
          }
 
       }
 
       // Instantiate provider
       if (provider == null && cacheClassName != null)
       {
          Class repoClass = null;
          try
          {
             repoClass = Class.forName(cacheClassName);
 
             Constructor ct = repoClass.getConstructor();
 
             provider = (IdentityStoreCacheProvider)ct.newInstance();
 
             provider.initialize(cacheProps, configurationContext);
          }
          catch (Exception e)
          {
             throw new IdentityException("Cannot instantiate IdentityStoreCacheProvider:" + cacheClassName, e);
          }
       }
 
       cache = provider;
 
    }
 
    public IdentityStoreSession createIdentityStoreSession()
    {
       return new LDAPIdentityStoreSessionImpl(configuration);
    }
 
    public String getId()
    {
       return id;
    }
 
    public FeaturesMetaData getSupportedFeatures()
    {
       return supportedFeatures;
    }
 
    public IdentityObject createIdentityObject(IdentityStoreInvocationContext invocationCtx,
                                               String name,
                                               IdentityObjectType identityObjectType) throws IdentityException
    {
       return createIdentityObject(invocationCtx, name, identityObjectType, null);
    }
 
    public IdentityObject createIdentityObject(IdentityStoreInvocationContext invocationCtx,
                                               String name,
                                               IdentityObjectType type,
                                               Map<String, String[]> attributes) throws IdentityException
    {
       if (name == null)
       {
          throw new IdentityException("Name cannot be null");
       }
 
       checkIOType(type);
 
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".createIdentityObject with name: " + name + " and type: " + type.getName());
       }
 
       LdapContext ldapContext = getLDAPContext(invocationCtx);
 
       String dn = null;
 
       try
       {
          //  If there are many contexts specified in the configuration the first one is used
          // Escape JNDI special characters
          Name jndiName = new CompositeName().add(getTypeConfiguration(invocationCtx, type).getCtxDNs()[0]);
 
          LdapContext ctx = (LdapContext)ldapContext.lookup(jndiName);
 
          //We store new entry using set of attributes. This should give more flexibility then
          //extending identity object from ContextDir - configure what objectClass place there
          Attributes attrs = new BasicAttributes(true);
 
          //create attribute using provided configuration
          Map<String, String[]> attributesToAdd = getTypeConfiguration(invocationCtx, type).getCreateEntryAttributeValues();
 
          //merge
          if (attributes != null)
          {
             for (Map.Entry<String, String[]> entry : attributes.entrySet())
             {
 
                if (!attributesToAdd.containsKey(entry.getKey()))
                {
                   attributesToAdd.put(entry.getKey(), entry.getValue());
                }
                else
                {
                   List<String> list1 = Arrays.asList(attributesToAdd.get(entry.getKey()));
                   List<String> list2 = Arrays.asList(entry.getValue());
 
                   list1.addAll(list2);
 
                   String[] vals = list1.toArray(new String[list1.size()]);
 
                   attributesToAdd.put(entry.getKey(), vals);
 
                }
             }
          }
 
          //attributes
          for (Iterator it1 = attributesToAdd.keySet().iterator(); it1.hasNext();)
          {
             String attributeName = (String)it1.next();
 
 
             Attribute attr = new BasicAttribute(attributeName);
             String[] attributeValues = attributesToAdd.get(attributeName);
 
             //values
 
             for (String attrValue : attributeValues)
             {
                attr.add(attrValue);
             }
 
             attrs.put(attr);
          }
 
          // Make it RFC 2253 compliant
          LdapName validLDAPName = new LdapName(getTypeConfiguration(invocationCtx, type).getIdAttributeName().concat("=").concat(name));
 
          if (log.isLoggable(Level.FINER))
          {
             log.finer("creating ldap entry for: " + validLDAPName + "; " + attrs);
          }
          DirContext entry = ctx.createSubcontext(validLDAPName, attrs);
 
          invalidateCache();
 
          if (entry != null)
          {
             dn = entry.getNameInNamespace();
          }
       }
       catch (Exception e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("Failed to create identity object", e);
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
       return new SimpleIdentityObject(name, dn, type);
 
    }
 
    public void removeIdentityObject(IdentityStoreInvocationContext invocationCtx, IdentityObject identity) throws IdentityException
    {
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".removeIdentityObject: " + identity);
       }
 
       LDAPIdentityObjectImpl ldapIdentity = getSafeLDAPIO(invocationCtx, identity);
 
       String dn = ldapIdentity.getDn();
 
       if (dn == null)
       {
          throw new IdentityException("Cannot obtain DN of identity");
       }
 
       LdapContext ldapContext = getLDAPContext(invocationCtx);
 
       try
       {
          log.finer("removing entry: " + dn);
 
          // Escape JNDI special characters
          Name jndiName = new CompositeName().add(dn);
          ldapContext.unbind(jndiName);
 
          invalidateCache();
       }
       catch (Exception e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("Failed to remove identity: ", e);
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
    }
 
    public int getIdentityObjectsCount(IdentityStoreInvocationContext ctx, IdentityObjectType identityType) throws IdentityException
    {
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".getIdentityObjectsCount for type: " + identityType);
       }
 
       checkIOType(identityType);
 
       try
       {
          String filter = getTypeConfiguration(ctx, identityType).getEntrySearchFilter();
 
          if (filter != null && filter.length() > 0)
          {
             // chars are escaped in filterArgs so we must replace it manually
             filter = filter.replaceAll("\\{0\\}", "*");
          }
          else
          {
             //search all entries
             filter = "(".concat(getTypeConfiguration(ctx, identityType).getIdAttributeName()).concat("=").concat("*").concat(")");
          }
 
 
          String[] entryCtxs = getTypeConfiguration(ctx, identityType).getCtxDNs();
          String scope = getTypeConfiguration(ctx, identityType).getEntrySearchScope();
 
          //log.debug("Search filter: " + filter);
          List sr = searchIdentityObjects(ctx,
             entryCtxs,
             filter,
             null,
             new String[]{getTypeConfiguration(ctx, identityType).getIdAttributeName()},
             scope,
             null);
 
          return sr.size();
 
       }
       catch (NoSuchElementException e)
       {
          //log.debug("No identity object found", e);
       }
       catch (Exception e)
       {
          if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
          throw new IdentityException("User search failed.", e);
       }
       return 0;
    }
 
    public IdentityObject findIdentityObject(IdentityStoreInvocationContext invocationCtx, String name, IdentityObjectType type) throws IdentityException
    {
 
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".findIdentityObject with name: " + name + "; and type: " + type);
       }
 
       Context ctx = null;
       checkIOType(type);
       try
       {
          //log.debug("name = " + name);
 
          if (name == null)
          {
             throw new IdentityException("Identity object name canot be null");
          }
 
          String filter = getTypeConfiguration(invocationCtx, type).getEntrySearchFilter();
          List sr = null;
 
 
          String[] entryCtxs = getTypeConfiguration(invocationCtx, type).getCtxDNs();
          String scope = getTypeConfiguration(invocationCtx, type).getEntrySearchScope();
 
 
          if (filter != null && filter.length() > 0)
          {
             Object[] filterArgs = {name};
             sr = searchIdentityObjects(invocationCtx,
                entryCtxs,
                filter,
                filterArgs,
                new String[]{getTypeConfiguration(invocationCtx, type).getIdAttributeName()},
                scope,
                null);
          }
          else
          {
             //search all entries
             filter = "(".concat(getTypeConfiguration(invocationCtx, type).getIdAttributeName()).concat("=").concat(name).concat(")");
             sr = searchIdentityObjects(invocationCtx,
                entryCtxs,
                filter,
                null,
                new String[]{getTypeConfiguration(invocationCtx, type).getIdAttributeName()},
                scope,
                null);
          }
 
          //log.debug("Search filter: " + filter);
 
          if (sr.size() > 1)
          {
             throw new IdentityException("Found more than one identity object with name: " + name +
                "; Posible data inconsistency");
          }
          SearchResult res = (SearchResult)sr.iterator().next();
          ctx = (Context)res.getObject();
          String dn = ctx.getNameInNamespace();
          IdentityObject io = createIdentityObjectInstance(invocationCtx, type, res.getAttributes(), dn);
          ctx.close();
          return io;
 
       }
       catch (NoSuchElementException e)
       {
          //log.debug("No identity object found with name: " + name, e);
       }
       catch (NamingException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("IdentityObject search failed.", e);
       }
       finally
       {
          try
          {
             if (ctx != null)
             {
                ctx.close();
             }
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
       return null;
    }
 
    public IdentityObject findIdentityObject(IdentityStoreInvocationContext ctx, String id) throws IdentityException
    {
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".findIdentityObject with id: " + id);
       }
 
       LdapContext ldapContext = getLDAPContext(ctx);
 
       try
       {
          if (id == null)
          {
             throw new IdentityException("identity id cannot be null");
          }
 
          String dn = id;
 
          IdentityObjectType type = null;
 
          //Recognize the type by ctx DN
 
          IdentityObjectType[] possibleTypes = getConfiguration(ctx).getConfiguredTypes();
          Set<IdentityObjectType> matches = new HashSet<IdentityObjectType>();
 
          for (IdentityObjectType possibleType : possibleTypes)
          {
             String[] typeCtxs = getTypeConfiguration(ctx, possibleType).getCtxDNs();
 
             for (String typeCtx : typeCtxs)
             {
                if (dn.toLowerCase().endsWith(typeCtx.toLowerCase()))
                {
                   matches.add(possibleType);
                   break;
                }
             }
          }
 
          if (matches.size() == 1)
          {
             type = matches.iterator().next();
          }
          else if (matches.size() > 1)
          {
             // Several identity types are mapped with the same LDAP Context DN. Will use the first one that have same DN from
             // name/type search
 
             String name = Tools.stripDnToName(dn);
 
             for (IdentityObjectType match : matches)
             {
                LDAPIdentityObjectImpl entry = (LDAPIdentityObjectImpl)this.findIdentityObject(ctx, name, match);
                if (entry != null && entry.getDn().equalsIgnoreCase(dn))
                {
                   type = match;
                   break;
                }
             }
          }
 
 
 
          if (type == null)
          {
             throw new IdentityException("Cannot recognize identity object type by its DN: " + dn);
          }
 
          // Grab entry
 
          Name jndiName = new CompositeName().add(dn);
          Attributes attrs = ldapContext.getAttributes(jndiName);
 
          if (attrs == null)
          {
             throw new IdentityException("Can't find identity entry with DN: " + dn);
          }
 
          return createIdentityObjectInstance(ctx, type, attrs, dn);
 
       }
       catch (NoSuchElementException e)
       {
          //log.debug("No identity object found with dn: " + dn, e);
       }
       catch (NamingException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("Identity object search failed.", e);
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
       return null;
    }
 
    public Collection<IdentityObject> findIdentityObject(IdentityStoreInvocationContext invocationCtx,
                                                         IdentityObjectType type,
                                                         IdentityObjectSearchCriteria criteria) throws IdentityException
    {
 
       //TODO: page control with LDAP request control
 
 
 
       String nameFilter = "*";
 
       //Filter by name
       if (criteria != null  && criteria.getFilter() != null)
       {
          nameFilter = criteria.getFilter();
       }
 
 
       LdapContext ctx = getLDAPContext(invocationCtx);
 
 
       checkIOType(type);
 
       LinkedList<IdentityObject> objects = new LinkedList<IdentityObject>();
 
       LDAPIdentityObjectTypeConfiguration typeConfiguration = getTypeConfiguration(invocationCtx, type);
 
       try
       {
          Control[] requestControls = null;
 
          // Sort control
          if (criteria != null && criteria.isSorted() && configuration.isSortExtensionSupported())
          {
             //TODO sort by attribute name
             requestControls = new Control[]{
                new SortControl(typeConfiguration.getIdAttributeName(), Control.CRITICAL)
             };
          }
 
          StringBuilder af = new StringBuilder();
 
          // Filter by attribute values
          if (criteria != null && criteria.isFiltered())
          {
             af.append("(&");
 
             for (Map.Entry<String, String[]> stringEntry : criteria.getValues().entrySet())
             {
                for (String value : stringEntry.getValue())
                {
                   String attributeName = getTypeConfiguration(invocationCtx, type).getAttributeMapping(stringEntry.getKey());
 
                   if (attributeName == null)
                   {
                      attributeName = stringEntry.getKey();
                   }
 
                   af.append("(")
                      .append(attributeName)
                      .append("=")
                      .append(value)
                      .append(")");
                }
             }
 
             af.append(")");
          }
 
          String filter = getTypeConfiguration(invocationCtx, type).getEntrySearchFilter();
          List<SearchResult> sr = null;
 
          String[] entryCtxs = getTypeConfiguration(invocationCtx, type).getCtxDNs();
          String scope = getTypeConfiguration(invocationCtx, type).getEntrySearchScope();
 
          if (filter != null && filter.length() > 0)
          {
 
             // Wildcards will be escabed by filterArgs
             filter = filter.replaceAll("\\{0\\}", nameFilter);
 
             sr = searchIdentityObjects(invocationCtx,
                entryCtxs,
                "(&(" + filter + ")" + af.toString() + ")",
                null,
                new String[]{typeConfiguration.getIdAttributeName()},
                scope,
                requestControls);
          }
          else
          {
             filter = "(".concat(typeConfiguration.getIdAttributeName()).concat("=").concat(nameFilter).concat(")");
             sr = searchIdentityObjects(invocationCtx,
                entryCtxs,
                "(&(" + filter + ")" + af.toString() + ")",
                null,
                new String[]{typeConfiguration.getIdAttributeName()},
                scope,
                requestControls);
          }
 
 
          for (SearchResult res : sr)
          {
             ctx = (LdapContext)res.getObject();
             String dn = ctx.getNameInNamespace();
             if (criteria != null && criteria.isSorted() && configuration.isSortExtensionSupported())
             {
                // It seams that the sort order is not configurable and
                // sort control returns entries in descending order by default...
                if (!criteria.isAscending())
                {
                   objects.addFirst(createIdentityObjectInstance(invocationCtx, type, res.getAttributes(), dn));
                }
                else
                {
                   objects.addLast(createIdentityObjectInstance(invocationCtx, type, res.getAttributes(), dn));
                }
             }
             else
             {
                objects.add(createIdentityObjectInstance(invocationCtx, type, res.getAttributes(), dn));
             }
          }
 
          ctx.close();
 
 
       }
       catch (NoSuchElementException e)
       {
          //log.debug("No identity object found with name: " + name, e);
       }
       catch (Exception e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("IdentityObject search failed.", e);
       }
       finally
       {
          try
          {
             if (ctx != null)
             {
                ctx.close();
             }
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
       // In case sort extension is not supported
       if (criteria != null && criteria.isSorted() && !configuration.isSortExtensionSupported())
       {
          sortByName(objects, criteria.isAscending());
       }
 
       if (criteria != null && criteria.isPaged())
       {
          objects = (LinkedList)cutPageFromResults(objects, criteria);
       }
 
       return objects;
    }
 
    public Collection<IdentityObject> findIdentityObject(IdentityStoreInvocationContext invocationCtx,
                                                         IdentityObjectType type) throws IdentityException
    {
       return findIdentityObject(invocationCtx, type, null);
    }
 
 
    public Collection<IdentityObject> findIdentityObject(IdentityStoreInvocationContext ctx,
                                                         IdentityObject identity,
                                                         IdentityObjectRelationshipType relationshipType,
                                                         boolean parent,
                                                         IdentityObjectSearchCriteria criteria) throws IdentityException
    {
 
       if (relationshipType != null && !relationshipType.getName().equals(MEMBERSHIP_TYPE))
       {
          throw new IdentityException("This store implementation supports only '" + MEMBERSHIP_TYPE +"' relationship type");
       }
 
       LDAPIdentityObjectImpl ldapIO = getSafeLDAPIO(ctx, identity);
 
       LDAPIdentityObjectTypeConfiguration typeConfig = getTypeConfiguration(ctx, identity.getIdentityType());
 
       LdapContext ldapContext = getLDAPContext(ctx);
 
       List<IdentityObject> objects = new LinkedList<IdentityObject>();
 
       try
       {
 
          // If parent simply look for all its members
          if (parent)
          {
             if (typeConfig.getParentMembershipAttributeName() != null)
             {
 
 
                Name jndiName = new CompositeName().add(ldapIO.getDn());
                Attributes attrs = ldapContext.getAttributes(jndiName);
                Attribute member = attrs.get(typeConfig.getParentMembershipAttributeName());
 
                if (member != null)
                {
                   NamingEnumeration memberValues = member.getAll();
                   while (memberValues.hasMoreElements())
                   {
                      String memberRef = memberValues.nextElement().toString();
 
                      // Ignore placeholder value in memberships
                      String placeholder = typeConfig.getParentMembershipAttributePlaceholder();
                      if (placeholder != null && memberRef.equalsIgnoreCase(placeholder))
                      {
                         continue;
                      }
 
                      if (typeConfig.isParentMembershipAttributeDN())
                      {
                         //TODO: use direct LDAP query instead of other find method and add attributesFilter
 
                         if (criteria != null && criteria.getFilter() != null)
                         {
                            String name = Tools.stripDnToName(memberRef);
                            String regex = Tools.wildcardToRegex(criteria.getFilter());
 
                            if (Pattern.matches(regex, name))
                            {
                               objects.add(findIdentityObject(ctx, memberRef));
                            }
                         }
                         else
                         {
                            objects.add(findIdentityObject(ctx, memberRef));
                         }
                      }
                      else
                      {
                         //TODO: if relationships are not refered with DNs and only names its not possible to map
                         //TODO: them to proper IdentityType and keep name uniqnes per type. Workaround needed
                         throw new NotYetImplementedException("LDAP limitation. If relationship targets are not refered with FQDNs " +
                            "and only names, it's not possible to map them to proper IdentityType and keep name uniqnes per type. " +
                            "Workaround needed");
                      }
                      //break;
                   }
                }
             }
             else
             {
 
                objects.addAll(findRelatedIdentityObjects(ctx, identity, ldapIO, criteria, false));
 
             }
 
 
          }
          // if not parent then all parent entries need to be found
          else
          {
             if (typeConfig.getChildMembershipAttributeName() == null)
             {
                objects.addAll(findRelatedIdentityObjects(ctx, identity, ldapIO, criteria, true));
             }
             else
             {
                // Escape JNDI special characters
                Name jndiName = new CompositeName().add(ldapIO.getDn());
                Attributes attrs = ldapContext.getAttributes(jndiName);
                Attribute member = attrs.get(typeConfig.getChildMembershipAttributeName());
 
                if (member != null)
                {
                   NamingEnumeration memberValues = member.getAll();
                   while (memberValues.hasMoreElements())
                   {
                      String memberRef = memberValues.nextElement().toString();
 
                      if (typeConfig.isChildMembershipAttributeDN())
                      {
                         //TODO: use direct LDAP query instead of other find method and add attributesFilter
 
                         if (criteria != null && criteria.getFilter() != null)
                         {
                            String name = Tools.stripDnToName(memberRef);
                            String regex = Tools.wildcardToRegex(criteria.getFilter());
 
                            if (Pattern.matches(regex, name))
                            {
                               objects.add(findIdentityObject(ctx, memberRef));
                            }
                         }
                         else
                         {
                            objects.add(findIdentityObject(ctx, memberRef));
                         }
                      }
                      else
                      {
                         //TODO: if relationships are not refered with DNs and only names its not possible to map
                         //TODO: them to proper IdentityType and keep name uniqnes per type. Workaround needed
                         throw new NotYetImplementedException("LDAP limitation. If relationship targets are not refered with FQDNs " +
                            "and only names, it's not possible to map them to proper IdentityType and keep name uniqnes per type. " +
                            "Workaround needed");
                      }
                      //break;
                   }
                }
             }
 
 
 
 
          }
 
       }
       catch (NamingException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("Failed to resolve relationship", e);
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
       if (criteria != null && criteria.isPaged())
       {
          objects = cutPageFromResults(objects, criteria);
       }
 
       if (criteria != null && criteria.isSorted())
       {
          sortByName(objects, criteria.isAscending());
       }
 
       return objects;
    }
 
    public List<IdentityObject> findRelatedIdentityObjects(IdentityStoreInvocationContext ctx,
                                                           IdentityObject identity,
                                                           LDAPIdentityObjectImpl ldapIO,
                                                           IdentityObjectSearchCriteria criteria,
                                                           boolean parents)
       throws IdentityException, NamingException
    {
 
       List<IdentityObject> objects = new LinkedList<IdentityObject>();
 
       LDAPIdentityObjectTypeConfiguration typeConfiguration = getTypeConfiguration(ctx, identity.getIdentityType());
 
       List<String> allowedTypes = Arrays.asList(typeConfiguration.getAllowedMembershipTypes());
 
 
       // Search in all other type contexts
       for (IdentityObjectType checkedIOType : configuration.getConfiguredTypes())
       {
          checkIOType(checkedIOType);
 
          LDAPIdentityObjectTypeConfiguration checkedTypeConfiguration = getTypeConfiguration(ctx, checkedIOType);
 
          List<String> checkedAllowedTypes = Arrays.asList(checkedTypeConfiguration.getAllowedMembershipTypes());
 
          if (parents)
          {
             // Check if given identity type can be parent
             if (!checkedAllowedTypes.contains(identity.getIdentityType().getName()))
             {
                continue;
             }
 
          }
          else
          {
             //Check if given identity type can be child
             if (!allowedTypes.contains(checkedIOType.getName()))
             {
                continue;
             }
          }
 
          // Check if this type is capable to keep needed reference
          if (parents && checkedTypeConfiguration.getParentMembershipAttributeName() == null)
          {
             continue;
          }
 
          if (!parents && checkedTypeConfiguration.getChildMembershipAttributeName() == null)
          {
             continue;
          }
 
          String nameFilter = "*";
 
          //Filter by name
          if (criteria != null && criteria.getFilter() != null)
          {
             nameFilter = criteria.getFilter();
          }
 
          Control[] requestControls = null;
 
          StringBuilder af = new StringBuilder();
 
          // Filter by attribute values
          if (criteria != null && criteria.isFiltered())
          {
             af.append("(&");
 
             for (Map.Entry<String, String[]> stringEntry : criteria.getValues().entrySet())
             {
                for (String value : stringEntry.getValue())
                {
                   af.append("(")
                      .append(stringEntry.getKey())
                      .append("=")
                      .append(value)
                      .append(")");
                }
             }
 
             af.append(")");
          }
 
 
          if(parents)
          {
             // Add filter to search only parents of the given entry
             af.append("(")
                .append(checkedTypeConfiguration.getParentMembershipAttributeName())
                .append("=");
             if (checkedTypeConfiguration.isParentMembershipAttributeDN())
             {
                af.append(ldapIO.getDn());
             }
             else
             {
                //TODO: this doesn't make much sense unless parent/child are same identity types and resides in the same LDAP context
                af.append(ldapIO.getName());
             }
             af.append(")");
          }
          else
          {
             // Add filter to search only childs of the given entry
             af.append("(")
                .append(checkedTypeConfiguration.getChildMembershipAttributeName())
                .append("=");
             if (checkedTypeConfiguration.isChildMembershipAttributeDN())
             {
                af.append(ldapIO.getDn());
             }
             else
             {
                //TODO: this doesn't make much sense unless parent/child are same identity types and resides in the same LDAP context
                af.append(ldapIO.getName());
             }
             af.append(")");
          }
 
 
          String filter = checkedTypeConfiguration.getEntrySearchFilter();
          List<SearchResult> sr = null;
 
          String[] entryCtxs = checkedTypeConfiguration.getCtxDNs();
          String scope = checkedTypeConfiguration.getEntrySearchScope();
 
           if (filter != null && filter.length() > 0)
           {
 
              filter = filter.replaceAll("\\{0\\}", nameFilter);
              sr = searchIdentityObjects(ctx,
                 entryCtxs,
                 "(&(" + filter + ")" + af.toString() + ")",
                 null,
                 new String[]{checkedTypeConfiguration.getIdAttributeName()},
                 scope,
                 requestControls);
           }
           else
           {
              filter = "(".concat(checkedTypeConfiguration.getIdAttributeName()).concat("=").concat(nameFilter).concat(")");
              sr = searchIdentityObjects(ctx,
                 entryCtxs,
                 "(&(" + filter + ")" + af.toString() + ")",
                 null,
                 new String[]{checkedTypeConfiguration.getIdAttributeName()},
                 scope,
                 requestControls);
           }
           for (SearchResult res : sr)
          {
             LdapContext ldapCtx = (LdapContext)res.getObject();
             String dn = ldapCtx.getNameInNamespace();
 
             if (parents)
             {
                // Ignore placeholder value in memberships
                String placeholder = checkedTypeConfiguration.getParentMembershipAttributePlaceholder();
                if (placeholder != null && dn.equalsIgnoreCase(placeholder))
                {
                   continue;
                }
             }
 
             objects.add(createIdentityObjectInstance(ctx, checkedIOType, res.getAttributes(), dn));
          }
       }
 
       return objects;
    }
 
    public Set<IdentityObjectRelationship> resolveRelationships(IdentityStoreInvocationContext ctx,
                                                                IdentityObject identity,
                                                                IdentityObjectRelationshipType type,
                                                                boolean parent,
                                                                boolean named,
                                                                String name) throws IdentityException
    {
 
       if (type == null || !type.getName().equals(MEMBERSHIP_TYPE))
       {
          throw new IdentityException("This store implementation supports only '" + MEMBERSHIP_TYPE +"' relationship type");
       }
 
       LDAPIdentityObjectImpl ldapIO = getSafeLDAPIO(ctx, identity);
 
       LDAPIdentityObjectTypeConfiguration typeConfig = getTypeConfiguration(ctx, identity.getIdentityType());
 
       LdapContext ldapContext = getLDAPContext(ctx);
 
       Set<IdentityObjectRelationship> relationships = new HashSet<IdentityObjectRelationship>();
 
       try
       {
 
          // If parent simply look for all its members
          if (parent)
          {
             // Escape JNDI special characters
             Name jndiName = new CompositeName().add(ldapIO.getDn());
             Attributes attrs = ldapContext.getAttributes(jndiName);
 
             if (typeConfig.getParentMembershipAttributeName() != null )
             {
                Attribute member = attrs.get(typeConfig.getParentMembershipAttributeName());
 
 
                if (member != null)
                {
                   NamingEnumeration memberValues = member.getAll();
                   while (memberValues.hasMoreElements())
                   {
                      String memberRef = memberValues.nextElement().toString();
 
                      // Ignore placeholder value in memberships
                      String placeholder = typeConfig.getParentMembershipAttributePlaceholder();
                      if (placeholder != null && memberRef.equalsIgnoreCase(placeholder))
                      {
                         continue;
                      }
 
                      if (typeConfig.isParentMembershipAttributeDN())
                      {
                         //TODO: use direct LDAP query instaed of other find method and add attributesFilter
 
                         relationships.add(new LDAPIdentityObjectRelationshipImpl(MEMBERSHIP_TYPE, ldapIO, findIdentityObject(ctx, memberRef)));
 
                      }
                      else
                      {
                         //TODO: if relationships are not refered with DNs and only names its not possible to map
                         //TODO: them to proper IdentityType and keep name uniqnes per type. Workaround needed
                         throw new NotYetImplementedException("LDAP limitation. If relationship targets are not refered with FQDNs " +
                            "and only names, it's not possible to map them to proper IdentityType and keep name uniqnes per type. " +
                            "Workaround needed");
                      }
                      //break;
                   }
                }
             }
             else
             {
 
                relationships.addAll(findRelationships(ctx, identity, ldapIO, false));
             }
 
          }
 
          // if not parent then all parent entries need to be found
          else
          {
             // Escape JNDI special characters
             Name jndiName = new CompositeName().add(ldapIO.getDn());
             Attributes attrs = ldapContext.getAttributes(jndiName);
 
             if (typeConfig.getChildMembershipAttributeName() != null)
             {
                Attribute member = attrs.get(typeConfig.getChildMembershipAttributeName());
 
                if (member != null)
                {
                   NamingEnumeration memberValues = member.getAll();
                   while (memberValues.hasMoreElements())
                   {
                      String memberRef = memberValues.nextElement().toString();
 
                      // Ignore placeholder value in memberships
 
                      if (typeConfig.isChildMembershipAttributeDN())
                      {
                         //TODO: use direct LDAP query instaed of other find method and add attributesFilter
 
                         relationships.add(new LDAPIdentityObjectRelationshipImpl(MEMBERSHIP_TYPE, findIdentityObject(ctx, memberRef), ldapIO));
 
                      }
                      else
                      {
                         //TODO: if relationships are not refered with DNs and only names its not possible to map
                         //TODO: them to proper IdentityType and keep name uniqnes per type. Workaround needed
                         throw new NotYetImplementedException("LDAP limitation. If relationship targets are not refered with FQDNs " +
                            "and only names, it's not possible to map them to proper IdentityType and keep name uniqnes per type. " +
                            "Workaround needed");
                      }
                      //break;
                   }
                }
 
             }
             else
             {
                relationships.addAll(findRelationships(ctx, identity, ldapIO, true));
             }
          }
 
       }
       catch (NamingException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("Failed to resolve relationship", e);
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
 
       return relationships;
    }
 
    /**
     * Search all configured LDAP contexts for possible Identity Object Types and lookup for parent/child references
     * to form relationships
     *
     * @param ctx
     * @param identity
     * @param ldapIO
     * @param parents if true will lookup parents to reference child, if false will look children to reference parent
     * @return
     * @throws IdentityException
     * @throws NamingException
     */
    private Collection<LDAPIdentityObjectRelationshipImpl> findRelationships(IdentityStoreInvocationContext ctx,
                                                                             IdentityObject identity,
                                                                             LDAPIdentityObjectImpl ldapIO,
                                                                             boolean parents)
       throws IdentityException, NamingException
    {
 
       Set<LDAPIdentityObjectRelationshipImpl> relationships = new HashSet<LDAPIdentityObjectRelationshipImpl>();
 
       LDAPIdentityObjectTypeConfiguration typeConfiguration = getTypeConfiguration(ctx, identity.getIdentityType());
 
       List<String> allowedTypes = Arrays.asList(typeConfiguration.getAllowedMembershipTypes());
 //
       // Search in all other type contexts
       for (IdentityObjectType checkedIOType : configuration.getConfiguredTypes())
       {
          checkIOType(checkedIOType);
 
          LDAPIdentityObjectTypeConfiguration checkedTypeConfiguration = getTypeConfiguration(ctx, checkedIOType);
 
          List<String> checkedAllowedTypes = Arrays.asList(checkedTypeConfiguration.getAllowedMembershipTypes());
 
          if (parents)
          {
             // Check if given identity type can be parent
             if (!checkedAllowedTypes.contains(identity.getIdentityType().getName()))
             {
                continue;
             }
 
          }
          else
          {
             //Check if given identity type can be child
             if (!allowedTypes.contains(checkedIOType.getName()))
             {
                continue;
             }
          }
 
          String nameFilter = "*";
 
          //Filter by name
          Control[] requestControls = null;
 
          StringBuilder af = new StringBuilder();
 
 
          if(parents)
          {
             // Add filter to search only parents of the given entry
             af.append("(")
                .append(checkedTypeConfiguration.getParentMembershipAttributeName())
                .append("=");
             if (checkedTypeConfiguration.isParentMembershipAttributeDN())
             {
                af.append(ldapIO.getDn());
             }
             else
             {
                //TODO: this doesn't make much sense unless parent/child are same identity types and resides in the same LDAP context
                af.append(ldapIO.getName());
             }
             af.append(")");
          }
          else
          {
             // Add filter to search only childs of the given entry
             af.append("(")
                .append(checkedTypeConfiguration.getChildMembershipAttributeName())
                .append("=");
             if (checkedTypeConfiguration.isChildMembershipAttributeDN())
             {
                af.append(ldapIO.getDn());
             }
             else
             {
                //TODO: this doesn't make much sense unless parent/child are same identity types and resides in the same LDAP context
                af.append(ldapIO.getName());
             }
             af.append(")");
          }
 
          String filter = checkedTypeConfiguration.getEntrySearchFilter();
          List<SearchResult> sr = null;
 
          String[] entryCtxs = checkedTypeConfiguration.getCtxDNs();
          String scope = checkedTypeConfiguration.getEntrySearchScope();
 
          if (filter != null && filter.length() > 0)
          {
 
             filter = filter.replaceAll("\\{0\\}", nameFilter);
             sr = searchIdentityObjects(ctx,
                entryCtxs,
                "(&(" + filter + ")" + af.toString() + ")",
                null,
                new String[]{checkedTypeConfiguration.getIdAttributeName()},
                scope,
                requestControls);
          }
          else
          {
             filter = "(".concat(checkedTypeConfiguration.getIdAttributeName()).concat("=").concat(nameFilter).concat(")");
             sr = searchIdentityObjects(ctx,
                entryCtxs,
                "(&(" + filter + ")" + af.toString() + ")",
                null,
                new String[]{checkedTypeConfiguration.getIdAttributeName()},
                scope,
                requestControls);
          }
 
          for (SearchResult res : sr)
          {
             LdapContext ldapCtx = (LdapContext)res.getObject();
             String dn = ldapCtx.getNameInNamespace();
 
 
 
             if (parents)
             {
                // Ignore placeholder values
                String placeholder = checkedTypeConfiguration.getParentMembershipAttributePlaceholder();
                if (placeholder != null && dn.equalsIgnoreCase(placeholder))
                {
                   continue;
                }
 
 
                relationships.add(new LDAPIdentityObjectRelationshipImpl(MEMBERSHIP_TYPE, createIdentityObjectInstance(ctx, checkedIOType, res.getAttributes(), dn), ldapIO));
             }
             else
             {
                relationships.add(new LDAPIdentityObjectRelationshipImpl(MEMBERSHIP_TYPE, ldapIO, createIdentityObjectInstance(ctx, checkedIOType, res.getAttributes(), dn)));
             }
          }
       }
 
       return relationships;
    }
 
    public Collection<IdentityObject> findIdentityObject(IdentityStoreInvocationContext ctx,
                                                         IdentityObject identity,
                                                         IdentityObjectRelationshipType relationshipType,
                                                         boolean parent) throws IdentityException
    {
       return findIdentityObject(ctx, identity, relationshipType, parent, null);
    }
 
    public IdentityObjectRelationship createRelationship(IdentityStoreInvocationContext ctx, IdentityObject fromIdentity, IdentityObject toIdentity,
                                   IdentityObjectRelationshipType relationshipType,
                                   String name, boolean createNames) throws IdentityException
    {
 
       //TODO: relationshipType is ignored for now
 
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".createRelationship with "
             + "fromIdentity: " + fromIdentity
             + "; toIdentity: " + toIdentity
             + "; relationshipType: " + relationshipType
          );
       }
 
       if (relationshipType == null || !relationshipType.getName().equals(MEMBERSHIP_TYPE))
       {
          throw new IdentityException("This store implementation supports only '" + MEMBERSHIP_TYPE +"' relationship type");
       }
 
       LDAPIdentityObjectRelationshipImpl relationship = null;
 
       LDAPIdentityObjectImpl ldapFromIO =  getSafeLDAPIO(ctx, fromIdentity);
 
       LDAPIdentityObjectImpl ldapToIO = getSafeLDAPIO(ctx, toIdentity);
 
       LDAPIdentityObjectTypeConfiguration fromTypeConfig = getTypeConfiguration(ctx, fromIdentity.getIdentityType());
       LDAPIdentityObjectTypeConfiguration toTypeConfig = getTypeConfiguration(ctx, toIdentity.getIdentityType());
 
       LdapContext ldapContext = getLDAPContext(ctx);
 
       // Check posibilities
       if (!getSupportedFeatures().isRelationshipTypeSupported(fromIdentity.getIdentityType(), toIdentity.getIdentityType(), relationshipType))
       {
          throw new IdentityException("Relationship not supported. RelationshipType[ " + relationshipType + " ] " +
             "beetween: [ " + fromIdentity.getIdentityType().getName() + " ] and [ " + toIdentity.getIdentityType().getName() + " ]");
       }
 
       try
       {
          // Construct new member attribute values
          Attributes attrs = new BasicAttributes(true);
 
          if (fromTypeConfig.getParentMembershipAttributeName() != null)
          {
 
             Attribute member = new BasicAttribute(fromTypeConfig.getParentMembershipAttributeName());
 
             if (fromTypeConfig.isParentMembershipAttributeDN())
             {
                member.add(ldapToIO.getDn());
             }
             else
             {
                member.add(toIdentity.getName());
             }
 
             attrs.put(member);
 
             // Escape JNDI special characters
             Name jndiName = new CompositeName().add(ldapFromIO.getDn());
             ldapContext.modifyAttributes(jndiName, DirContext.ADD_ATTRIBUTE, attrs);
 
             invalidateCache();
          }
 
          if (toTypeConfig.getChildMembershipAttributeName() != null && !toTypeConfig.isChildMembershipAttributeVirtual())
          {
 
             Attribute member = new BasicAttribute(toTypeConfig.getChildMembershipAttributeName());
 
             if (toTypeConfig.isChildMembershipAttributeDN())
             {
                member.add(ldapFromIO.getDn());
             }
             else
             {
                member.add(fromIdentity.getName());
             }
 
             attrs.put(member);
 
             // Escape JNDI special characters
             Name jndiName = new CompositeName().add(ldapToIO.getDn());
             ldapContext.modifyAttributes(jndiName, DirContext.ADD_ATTRIBUTE, attrs);
 
             invalidateCache();
          }
 
          relationship = new LDAPIdentityObjectRelationshipImpl(name, ldapFromIO, ldapToIO);
 
       }
       catch (NamingException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("Failed to create relationship", e);
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
 
       return relationship;
    }
 
    public void removeRelationship(IdentityStoreInvocationContext ctx, IdentityObject fromIdentity, IdentityObject toIdentity, IdentityObjectRelationshipType relationshipType, String name) throws IdentityException
    {
       // relationshipType is ignored for now
 
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".removeRelationship with "
             + "fromIdentity: " + fromIdentity
             + "; toIdentity: " + toIdentity
             + "; relationshipType: " + relationshipType
          );
       }
 
       LDAPIdentityObjectImpl ldapFromIO = getSafeLDAPIO(ctx, fromIdentity);
       LDAPIdentityObjectImpl ldapToIO = getSafeLDAPIO(ctx, toIdentity);
 
       LDAPIdentityObjectTypeConfiguration fromTypeConfig = getTypeConfiguration(ctx, fromIdentity.getIdentityType());
       LDAPIdentityObjectTypeConfiguration toTypeConfig = getTypeConfiguration(ctx, toIdentity.getIdentityType());
 
       // If relationship is not allowed simply return
       //TODO: use features description instead
       if (!Arrays.asList(fromTypeConfig.getAllowedMembershipTypes()).contains(ldapToIO.getIdentityType().getName()))
       {
          return;
       }
 
       LdapContext ldapContext = getLDAPContext(ctx);
 
       // Check posibilities
 
       //TODO: null RelationshipType passed from removeRelationships
       if (relationshipType != null &&
          !getSupportedFeatures().isRelationshipTypeSupported(fromIdentity.getIdentityType(), toIdentity.getIdentityType(), relationshipType))
       {
          throw new IdentityException("Relationship not supported");
       }
 
       try
       {
          //construct new member attribute values
          Attributes attrs = new BasicAttributes(true);
 
          if (fromTypeConfig.getParentMembershipAttributeName() != null)
          {
 
             Attribute member = new BasicAttribute(fromTypeConfig.getParentMembershipAttributeName());
 
             if (fromTypeConfig.isParentMembershipAttributeDN())
             {
                member.add(ldapToIO.getDn());
             }
             else
             {
                member.add(toIdentity.getName());
             }
 
             attrs.put(member);
 
             // Escape JNDI special characters
             Name jndiName = new CompositeName().add(ldapFromIO.getDn());
             ldapContext.modifyAttributes(jndiName, DirContext.REMOVE_ATTRIBUTE, attrs);
 
             invalidateCache();
          }
 
          if (toTypeConfig.getChildMembershipAttributeName() != null && !toTypeConfig.isChildMembershipAttributeVirtual())
          {
             Attribute member = new BasicAttribute(toTypeConfig.getChildMembershipAttributeName());
 
             if (toTypeConfig.isChildMembershipAttributeDN())
             {
                member.add(ldapFromIO.getDn());
             }
             else
             {
                member.add(fromIdentity.getName());
             }
 
             attrs.put(member);
 
             // Escape JNDI special characters
             Name jndiName = new CompositeName().add(ldapToIO.getDn());
             ldapContext.modifyAttributes(jndiName, DirContext.REMOVE_ATTRIBUTE, attrs);
 
             invalidateCache();
          }
 
       }
       catch (NamingException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("Failed to remove relationship", e);
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
    }
 
    public void removeRelationships(IdentityStoreInvocationContext ctx, IdentityObject identity1, IdentityObject identity2, boolean named) throws IdentityException
    {
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".removeRelationships with "
             + "identity1: " + identity1
             + "; identity2: " + identity2
          );
       }
 
 
       // as relationship type is ignored in this impl for now...
       removeRelationship(ctx, identity1, identity2, null, null);
       removeRelationship(ctx, identity2, identity1, null, null);
 
    }
 
    public Set<IdentityObjectRelationship> resolveRelationships(IdentityStoreInvocationContext ctx,
                                                                IdentityObject fromIdentity,
                                                                IdentityObject toIdentity,
                                                                IdentityObjectRelationshipType relationshipType) throws IdentityException
    {
       // relationshipType is ignored for now
 
 
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".resolveRelationships with "
             + "fromIdentity: " + fromIdentity
             + "; toIdentity: " + toIdentity
          );
       }
 
       if (relationshipType != null && !relationshipType.getName().equals(MEMBERSHIP_TYPE))
       {
          throw new IdentityException("This store implementation supports only '" + MEMBERSHIP_TYPE +"' relationship type");
       }
 
       Set<IdentityObjectRelationship> relationships = new HashSet<IdentityObjectRelationship>();
 
 
 
       LDAPIdentityObjectImpl ldapFromIO = getSafeLDAPIO(ctx, fromIdentity);
       LDAPIdentityObjectImpl ldapToIO = getSafeLDAPIO(ctx, toIdentity);
 
       LDAPIdentityObjectTypeConfiguration fromTypeConfig = getTypeConfiguration(ctx, fromIdentity.getIdentityType());
       LDAPIdentityObjectTypeConfiguration toTypeConfig = getTypeConfiguration(ctx, toIdentity.getIdentityType());
 
       // If relationship is not allowed return empty set
       //TODO: use features description instead
 
       if (!Arrays.asList(fromTypeConfig.getAllowedMembershipTypes()).contains(ldapToIO.getIdentityType().getName()))
       {
          return relationships;
       }
 
       LdapContext ldapContext = getLDAPContext(ctx);
 
       try
       {
          // Escape JNDI special characters
          Name jndiName = new CompositeName().add(ldapFromIO.getDn());
          Attributes attrs = ldapContext.getAttributes(jndiName);
 
          if (fromTypeConfig.getParentMembershipAttributeName() != null)
          {
             Attribute member = attrs.get(fromTypeConfig.getParentMembershipAttributeName());
 
             if (member != null)
             {
                NamingEnumeration memberValues = member.getAll();
                while (memberValues.hasMoreElements())
                {
                   String memberRef = memberValues.nextElement().toString();
 
                   if ((fromTypeConfig.isParentMembershipAttributeDN() && memberRef.equals(ldapToIO.getDn())) ||
                      (!fromTypeConfig.isParentMembershipAttributeDN() && memberRef.equals(ldapToIO.getName())))
                   {
                      //TODO: impl lacks support for rel type
                      relationships.add(new LDAPIdentityObjectRelationshipImpl(MEMBERSHIP_TYPE, ldapFromIO, ldapToIO));
                   }
                }
             }
          }
          else if (toTypeConfig.getChildMembershipAttributeName() != null)
          {
             Attribute member = attrs.get(toTypeConfig.getChildMembershipAttributeName());
 
             if (member != null)
             {
                NamingEnumeration memberValues = member.getAll();
                while (memberValues.hasMoreElements())
                {
                   String memberRef = memberValues.nextElement().toString();
 
                   if ((fromTypeConfig.isChildMembershipAttributeDN() && memberRef.equals(ldapFromIO.getDn())) ||
                      (!fromTypeConfig.isChildMembershipAttributeDN() && memberRef.equals(ldapFromIO.getName())))
                   {
                      //TODO: impl lacks support for rel type
                      relationships.add(new LDAPIdentityObjectRelationshipImpl(MEMBERSHIP_TYPE, ldapFromIO, ldapToIO));
                   }
                }
             }
          }
 
 
       }
       catch (NamingException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("Failed to resolve relationship", e);
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
       return relationships;
    }
 
    public String createRelationshipName(IdentityStoreInvocationContext invocationCtx, String name) throws IdentityException, OperationNotSupportedException
    {
       if (name == null)
       {
          throw new IdentityException("Name cannot be null");
       }
 
 
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".createRelationshipName with name: " + name);
       }
 
       LdapContext ldapContext = getLDAPContext(invocationCtx);
 
       try
       {
 
          //  If there are many contexts specified in the configuration the first one is used
          //  Escape JNDI special characters
          Name jndiName = new CompositeName().add(getConfiguration(invocationCtx).getRelationshipNamesCtxDNs()[0]);
          LdapContext ctx = (LdapContext)ldapContext.lookup(jndiName);
 
          Attributes attrs = new BasicAttributes(true);
 
          //create attribute using provided configuration
          Map<String, String[]> attributesToAdd = getConfiguration(invocationCtx).
             getRelationshipNameCreateEntryAttributeValues();
 
          //attributes
          for (Iterator it1 = attributesToAdd.keySet().iterator(); it1.hasNext();)
          {
             String attributeName = (String)it1.next();
 
 
             Attribute attr = new BasicAttribute(attributeName);
             String[] attributeValues = attributesToAdd.get(attributeName);
 
             //values
 
             for (String attrValue : attributeValues)
             {
                attr.add(attrValue);
             }
 
             attrs.put(attr);
          }
 
          // Make it RFC 2253 compliant
          LdapName validLDAPName = new LdapName(getConfiguration(invocationCtx).getRelationshipNameAttributeName().concat("=").concat(name));
 
          if (log.isLoggable(Level.FINER))
          {
             log.finer("creating ldap entry for: " + validLDAPName + "; " + attrs);
          }
          ctx.createSubcontext(validLDAPName, attrs);
 
          invalidateCache();
       }
       catch (Exception e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("Failed to create relationship name object", e);
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
       return name;
    }
 
    public String removeRelationshipName(IdentityStoreInvocationContext invocationCtx, String name)  throws IdentityException, OperationNotSupportedException
    {
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".removeRelationshipName with name: " + name);
       }
 
       Context ctx = null;
       try
       {
 
          if (name == null)
          {
             throw new IdentityException("relationship name canot be null");
          }
 
          String filter = getConfiguration(invocationCtx).getRelationshipNameSearchFilter();
          List sr = null;
 
 
          String[] entryCtxs = getConfiguration(invocationCtx).getRelationshipNamesCtxDNs();
          String scope = getConfiguration(invocationCtx).getRelationshipNameSearchScope();
 
 
          if (filter != null && filter.length() > 0)
          {
             Object[] filterArgs = {name};
             sr = searchIdentityObjects(invocationCtx,
                entryCtxs,
                filter,
                filterArgs,
                new String[]{getConfiguration(invocationCtx).getRelationshipNameAttributeName()},
                scope,
                null);
          }
          else
          {
             //search all entries
             filter = "(".concat(getConfiguration(invocationCtx).getRelationshipNameAttributeName()).concat("=").concat(name).concat(")");
             sr = searchIdentityObjects(invocationCtx,
                entryCtxs,
                filter,
                null,
                new String[]{getConfiguration(invocationCtx).getRelationshipNameAttributeName()},
                scope,
                null);
          }
 
 
          if (sr.size() > 1)
          {
             throw new IdentityException("Found more than one relationship name entry: " + name +
                "; Posible data inconsistency");
          }
          SearchResult res = (SearchResult)sr.iterator().next();
          ctx = (Context)res.getObject();
          String dn = ctx.getNameInNamespace();
 
          // Escape JNDI special characters
          Name jndiName = new CompositeName().add(dn);
          ctx.unbind(jndiName);
 
          invalidateCache();
 
          ctx.close();
          return null;
 
       }
       catch (NoSuchElementException e)
       {
          //log.debug("No identity object found with name: " + name, e);
       }
       catch (NamingException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("relationship name remove failed.", e);
       }
       finally
       {
          try
          {
             if (ctx != null)
             {
                ctx.close();
             }
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
       return null;
    }
 
    public Set<String> getRelationshipNames(IdentityStoreInvocationContext invocationCtx, final IdentityObjectSearchCriteria criteria) throws IdentityException, OperationNotSupportedException
    {
 
       //TODO: page control with LDAP request control
 
       String nameFilter = "*";
 
       //Filter by name
       if (criteria != null  && criteria.getFilter() != null)
       {
          nameFilter = criteria.getFilter();
       }
 
 
       LdapContext ctx = getLDAPContext(invocationCtx);
 
 
       Set<String> names = new HashSet<String>();
 
       LDAPIdentityStoreConfiguration config = getConfiguration(invocationCtx);
 
       try
       {
          Control[] requestControls = null;
 
          StringBuilder af = new StringBuilder();
 
          String filter = config.getRelationshipNameSearchFilter();
          List<SearchResult> sr = null;
 
          String[] entryCtxs = config.getRelationshipNamesCtxDNs();
          String scope = config.getRelationshipNameSearchScope();
 
          if (filter != null && filter.length() > 0)
          {
 
             filter = filter.replaceAll("\\{0\\}", nameFilter);
             sr = searchIdentityObjects(invocationCtx,
                entryCtxs,
                "(&(" + filter + ")" + af.toString() + ")",
                null,
                new String[]{config.getRelationshipNameAttributeName()},
                scope,
                requestControls);
          }
          else
          {
             filter = "(".concat(config.getRelationshipNameAttributeName()).concat("=").concat(nameFilter).concat(")");
             sr = searchIdentityObjects(invocationCtx,
                entryCtxs,
                "(&(" + filter + ")" + af.toString() + ")",
                null,
                new String[]{config.getRelationshipNameAttributeName()},
                scope,
                requestControls);
          }
 
 
          for (SearchResult res : sr)
          {
             ctx = (LdapContext)res.getObject();
             String dn = ctx.getNameInNamespace();
             String[] parts = dn.split("=");
 
             names.add(parts[1]);
 
          }
 
          ctx.close();
 
 
       }
       catch (NoSuchElementException e)
       {
       }
       catch (Exception e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("relationship names search failed.", e);
       }
       finally
       {
          try
          {
             if (ctx != null)
             {
                ctx.close();
             }
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
 //      if (criteria != null && criteria.isPaged())
 //      {
 //         names = (LinkedList)(cutPageFromResults(names, criteria);
 //      }
 
       return names;
    }
 
    public Set<String> getRelationshipNames(IdentityStoreInvocationContext ctx) throws IdentityException, OperationNotSupportedException
    {
       return getRelationshipNames(ctx, (IdentityObjectSearchCriteria)null);
    }
 
    public Set<String> getRelationshipNames(IdentityStoreInvocationContext ctx, IdentityObject identity, IdentityObjectSearchCriteria criteria) throws IdentityException, OperationNotSupportedException
    {
       throw new OperationNotSupportedException("Named relationships are not supported by this implementation of LDAP IdentityStore");
 
 
    }
 
    public Set<String> getRelationshipNames(IdentityStoreInvocationContext ctx, IdentityObject identity) throws IdentityException, OperationNotSupportedException
    {
       throw new OperationNotSupportedException("Named relationships are not supported by this implementation of LDAP IdentityStore");
    }
 
 
    public Map<String, String> getRelationshipNameProperties(IdentityStoreInvocationContext ctx, String name) throws IdentityException, OperationNotSupportedException
    {
       throw new OperationNotSupportedException("Named relationships are not supported by this implementation of LDAP IdentityStore");
 
    }
 
    public void setRelationshipNameProperties(IdentityStoreInvocationContext ctx, String name, Map<String, String> properties) throws IdentityException, OperationNotSupportedException
    {
       throw new OperationNotSupportedException("Named relationships are not supported by this implementation of LDAP IdentityStore");
 
    }
 
    public void removeRelationshipNameProperties(IdentityStoreInvocationContext ctx, String name, Set<String> properties) throws IdentityException, OperationNotSupportedException
    {
       throw new OperationNotSupportedException("Named relationships are not supported by this implementation of LDAP IdentityStore");
 
    }
 
    public Map<String, String> getRelationshipProperties(IdentityStoreInvocationContext ctx, IdentityObjectRelationship relationship) throws IdentityException, OperationNotSupportedException
    {
       throw new OperationNotSupportedException("Relationship properties are not supported by this implementation of LDAP IdentityStore");
 
    }
 
    public void setRelationshipProperties(IdentityStoreInvocationContext ctx, IdentityObjectRelationship relationship, Map<String, String> properties) throws IdentityException, OperationNotSupportedException
    {
       throw new OperationNotSupportedException("Relationship properties are not supported by this implementation of LDAP IdentityStore");
 
    }
 
    public void removeRelationshipProperties(IdentityStoreInvocationContext ctx, IdentityObjectRelationship relationship, Set<String> properties) throws IdentityException, OperationNotSupportedException
    {
       throw new OperationNotSupportedException("Relationship properties are not supported by this implementation of LDAP IdentityStore");
 
    }
 
    public boolean validateCredential(IdentityStoreInvocationContext ctx, IdentityObject identityObject, IdentityObjectCredential credential) throws IdentityException
    {
       if (credential == null)
       {
          throw new IllegalArgumentException();
       }
 
       LDAPIdentityObjectImpl ldapIO = getSafeLDAPIO(ctx, identityObject);
 
       if (supportedFeatures.isCredentialSupported(ldapIO.getIdentityType(),credential.getType()))
       {
 
          String passwordString = null;
 
          // Handle generic impl
 
          if (credential.getValue() != null)
          {
             //TODO: support for empty password should be configurable
             passwordString = credential.getValue().toString();
             if (passwordString.length() == 0 && !getTypeConfiguration(ctx, identityObject.getIdentityType()).isAllowEmptyPassword())
             {
                return false;
             }
          }
          else
          {
             if (!getTypeConfiguration(ctx, identityObject.getIdentityType()).isAllowEmptyPassword())
             {
                new IdentityException("Null password value");
             }
             passwordString = "";
          }
 
          LdapContext ldapContext = getLDAPContext(ctx);
 
          try
          {
 
             Hashtable env = ldapContext.getEnvironment();
 
             env.put(Context.SECURITY_PRINCIPAL, ldapIO.getDn());
             env.put(Context.SECURITY_CREDENTIALS, passwordString);
 
             InitialContext initialCtx = new InitialLdapContext(env, null);
 
             if (initialCtx != null)
             {
                initialCtx.close();
                return true;
             }
 
          }
          catch (NamingException e)
          {
             //
          }
          finally
          {
             try
             {
                ldapContext.close();
             }
             catch (NamingException e)
             {
                if (log.isLoggable(Level.FINER))
                {
                   log.log(Level.FINER, "Exception occurred: ", e);
                }
 
                throw new IdentityException("Failed to close LDAP connection", e);
             }
          }
          return false;
 
 
       }
       else
       {
          throw new IdentityException("CredentialType not supported for a given IdentityObjectType");
       }
    }
 
    public void updateCredential(IdentityStoreInvocationContext ctx, IdentityObject identityObject, IdentityObjectCredential credential) throws IdentityException
    {
       if (credential == null)
       {
          throw new IllegalArgumentException();
       }
 
       LDAPIdentityObjectImpl ldapIO = getSafeLDAPIO(ctx, identityObject);
 
       if (supportedFeatures.isCredentialSupported(ldapIO.getIdentityType(),credential.getType()))
       {
 
          String passwordString = null;
 
          // Handle generic impl
 
          LDAPIdentityObjectTypeConfiguration typeConfig = getTypeConfiguration(ctx, identityObject.getIdentityType());
 
          if (credential.getValue() != null)
          {
             //TODO: support for empty password should be configurable
             passwordString = credential.getValue().toString();
             if (passwordString.length() == 0 && !typeConfig.isAllowEmptyPassword())
             {
                new IdentityException("Empty password is not allowed by configuration");;
             }
          }
          else
          {
             if (!typeConfig.isAllowEmptyPassword())
             {
                new IdentityException("Null password value");
             }
             passwordString = "";
          }
 
          if (typeConfig.getEnclosePasswordWith() != null)
          {
             String enc = typeConfig.getEnclosePasswordWith();
             passwordString = enc + passwordString + enc;
          }
 
          byte[] encodedPassword = null;
 
          if (typeConfig.getPasswordEncoding() != null)
          {
             try
             {
                encodedPassword = passwordString.getBytes(typeConfig.getPasswordEncoding());
             }
             catch (UnsupportedEncodingException e)
             {
                if (log.isLoggable(Level.FINER))
                {
                   log.log(Level.FINER, "Exception occurred: ", e);
                }
 
                throw new IdentityException("Error while encoding password with configured setting: " + typeConfig.getPasswordEncoding(),
                   e);
             }
          }
 
          String attributeName = getTypeConfiguration(ctx, ldapIO.getIdentityType()).getPasswordAttributeName();
 
          if (attributeName == null)
          {
             throw new IdentityException("IdentityType doesn't have passwordAttributeName option set: "
                + ldapIO.getIdentityType().getName());
          }
 
          LdapContext ldapContext = getLDAPContext(ctx);
 
          try
          {
             //TODO: maybe perform a schema check if this attribute is allowed for such entry
 
             Attributes attrs = new BasicAttributes(true);
             Attribute attr = new BasicAttribute(attributeName);
 
             if (encodedPassword != null)
             {
                attr.add(encodedPassword);
             }
             else
             {
                attr.add(passwordString);
             }
 
             attrs.put(attr);
 
             if(typeConfig.getUpdatePasswordAttributeValues().size() > 0)
             {
                Map<String, String[]>  attributesToAdd = typeConfig.getUpdatePasswordAttributeValues();
                for (Map.Entry<String, String[]> entry : attributesToAdd.entrySet())
                {
                   Attribute additionalAttr = new BasicAttribute(entry.getKey());
                   for (String val : entry.getValue())
                   {
                      additionalAttr.add(val);
                   }
                   attrs.put(additionalAttr);
                }
 
             }
 
             // Escape JNDI special characters
             Name jndiName = new CompositeName().add(ldapIO.getDn());
             ldapContext.modifyAttributes(jndiName, DirContext.REPLACE_ATTRIBUTE, attrs);
 
             invalidateCache();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Cannot set identity password value.", e);
          }
          finally
          {
             try
             {
                ldapContext.close();
             }
             catch (NamingException e)
             {
                if (log.isLoggable(Level.FINER))
                {
                   log.log(Level.FINER, "Exception occurred: ", e);
                }
 
                throw new IdentityException("Failed to close LDAP connection", e);
             }
          }
 
       }
       else
       {
          throw new IdentityException("CredentialType not supported for a given IdentityObjectType");
       }
    }
 
 
    // Attributes
 
    public Set<String> getSupportedAttributeNames(IdentityStoreInvocationContext invocationContext, IdentityObjectType identityType) throws IdentityException
    {
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".getSupportedAttributeNames with "
             + "identityType: " + identityType
          );
       }
 
       checkIOType(identityType);
 
       return getTypeConfiguration(invocationContext, identityType).getMappedAttributesNames();
    }
 
    public Map<String, IdentityObjectAttributeMetaData> getAttributesMetaData(IdentityStoreInvocationContext invocationContext, IdentityObjectType identityObjectType)
    {
       return attributesMetaData.get(identityObjectType.getName());
    }
 
 
    public IdentityObjectAttribute getAttribute(IdentityStoreInvocationContext invocationContext, IdentityObject identity, String name) throws IdentityException
    {
       //TODO: dummy temporary implementation
       return getAttributes(invocationContext, identity).get(name);
    }
 
    public Map<String, IdentityObjectAttribute> getAttributes(IdentityStoreInvocationContext ctx, IdentityObject identity) throws IdentityException
    {
 
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".getAttributes with "
             + "identity: " + identity
          );
       }
 
       // Cache
 
       if (getCache() != null)
       {
          Map<String, IdentityObjectAttribute> cachedAttributes = getCache().
             getIdentityObjectAttributes(getNamespace(), identity);
 
          if (cachedAttributes != null)
          {
             return cachedAttributes;
          }
       }
 
       Map<String, IdentityObjectAttribute> attrsMap = new HashMap<String, IdentityObjectAttribute>();
 
       LDAPIdentityObjectImpl ldapIdentity = getSafeLDAPIO(ctx, identity);
 
 
       LdapContext ldapContext = getLDAPContext(ctx);
 
       try
       {
          Set<String> mappedNames = getTypeConfiguration(ctx, identity.getIdentityType()).getMappedAttributesNames();
 
          // as this is valid LDAPIdentityObjectImpl DN is obtained from the Id
 
          String dn = ldapIdentity.getDn();
 
          // Escape JNDI special characters
          Name jndiName = new CompositeName().add(dn);
          Attributes attrs = ldapContext.getAttributes(jndiName);
 
          for (Iterator iterator = mappedNames.iterator(); iterator.hasNext();)
          {
             String name = (String)iterator.next();
             String attrName = getTypeConfiguration(ctx, identity.getIdentityType()).getAttributeMapping(name);
             Attribute attr = attrs.get(attrName);
 
             if (attr != null)
             {
 
                IdentityObjectAttribute identityObjectAttribute = new SimpleAttribute(name);
 
                NamingEnumeration values = attr.getAll();
 
                while (values.hasMoreElements())
                {
                   identityObjectAttribute.addValue(values.nextElement().toString());
                }
 
                attrsMap.put(name, identityObjectAttribute);
             }
             else
             {
                log.fine("No such attribute ('" + attrName + "') in entry: " + dn);
             }
          }
       }
       catch (NamingException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("Cannot get attributes value.", e);
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
       // Cache
 
       if (getCache() != null)
       {
          getCache().putIdentityObjectAttributes(getNamespace(), identity, attrsMap);
       }
 
       return attrsMap;
 
    }
 
    public void updateAttributes(IdentityStoreInvocationContext ctx, IdentityObject identity, IdentityObjectAttribute[] attributes) throws IdentityException
    {
 
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".updateAttributes with "
             + "identity: " + identity
             + "attributes: " + attributes
          );
       }
 
       if (getCache() != null)
       {
          getCache().invalidate(getNamespace());
       }
 
       if (attributes == null)
       {
          throw new IllegalArgumentException("attributes is null");
       }
 
       LDAPIdentityObjectImpl ldapIdentity = getSafeLDAPIO(ctx, identity);
 
 
       // as this is valid LDAPIdentityObjectImpl DN is obtained from the Id
 
       String dn = ldapIdentity.getDn();
 
       LdapContext ldapContext = getLDAPContext(ctx);
 
       try
       {
 
          for (IdentityObjectAttribute attribute : attributes)
          {
             String name = attribute.getName();
 
             String attributeName = getTypeConfiguration(ctx, identity.getIdentityType()).getAttributeMapping(name);
 
             if (attributeName == null)
             {
                log.fine("Proper LDAP attribute mapping not found for such property name: " + name);
                continue;
             }
 
             //TODO: maybe perform a schema check if this attribute is not required
 
             Attributes attrs = new BasicAttributes(true);
             Attribute attr = new BasicAttribute(attributeName);
 
             Collection values = attribute.getValues();
 
             Map<String, IdentityObjectAttributeMetaData> mdMap = attributesMetaData.get(identity.getIdentityType().getName());
 
             if (mdMap != null)
             {
                IdentityObjectAttributeMetaData amd = mdMap.get(attributeName);
                if (amd != null && !amd.isMultivalued() && values.size() > 1)
                {
                   throw new IdentityException("Cannot assigned multiply values to single valued attribute: " + attributeName);
                }
                if (amd != null && amd.isReadonly())
                {
                   throw new IdentityException("Cannot update readonly attribute: " + attributeName);
                }
 
                if (amd != null && amd.isUnique())
                {
                   IdentityObject checkIdentity = findIdentityObjectByUniqueAttribute(ctx,
                      identity.getIdentityType(),
                      attribute);
 
                   if (checkIdentity != null && !checkIdentity.getName().equals(identity.getName()))
                   {
                      throw new IdentityException("Unique attribute '" + attribute.getName() + " value already set for identityObject: " +
                         checkIdentity);
                   }
                }
             }
 
             if (values != null)
             {
                for (Object value : values)
                {
                   attr.add(value);
                }
 
                attrs.put(attr);
 
                try
                {
                   // Escape JNDI special characters
                   Name jndiName = new CompositeName().add(dn);
                   ldapContext.modifyAttributes(jndiName, DirContext.REPLACE_ATTRIBUTE, attrs);
 
                   invalidateCache();
                }
                catch (NamingException e)
                {
                   if (log.isLoggable(Level.FINER))
                   {
                      log.log(Level.FINER, "Exception occurred: ", e);
                   }
 
                   throw new IdentityException("Cannot add attribute", e);
                }
             }
 
          }
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
       // Cache new attributes
       if (getCache() != null)
       {
          getAttributes(ctx, identity);
       }
 
    }
 
    public void addAttributes(IdentityStoreInvocationContext ctx, IdentityObject identity, IdentityObjectAttribute[] attributes) throws IdentityException
    {
 
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".addAttributes with "
             + "identity: " + identity
             + "attributes: " + attributes
          );
       }
 
       if (getCache() != null)
       {
          getCache().invalidate(getNamespace());
       }
 
       if (attributes == null)
       {
          throw new IllegalArgumentException("attributes is null");
       }
 
       LDAPIdentityObjectImpl ldapIdentity = getSafeLDAPIO(ctx, identity);
 
 
       // as this is valid LDAPIdentityObjectImpl DN is obtained from the Id
 
       String dn = ldapIdentity.getDn();
 
       LdapContext ldapContext = getLDAPContext(ctx);
 
       try
       {
          for (IdentityObjectAttribute attribute : attributes)
          {
             String name = attribute.getName();
 
             String attributeName = getTypeConfiguration(ctx, identity.getIdentityType()).getAttributeMapping(name);
 
             if (attributeName == null)
             {
                log.fine("Proper LDAP attribute mapping not found for such property name: " + name);
                continue;
             }
 
             //TODO: maybe perform a schema check if this attribute is not required
 
             Attributes attrs = new BasicAttributes(true);
             Attribute attr = new BasicAttribute(attributeName);
 
             Collection values = attribute.getValues();
 
             Map<String, IdentityObjectAttributeMetaData> mdMap = attributesMetaData.get(identity.getIdentityType().getName());
 
             if (mdMap != null)
             {
                IdentityObjectAttributeMetaData amd = mdMap.get(attribute.getName());
                if (amd != null && !amd.isMultivalued() && values.size() > 1)
                {
                   throw new IdentityException("Cannot assigned multiply values to single valued attribute: " + attributeName);
                }
                if (amd != null && amd.isReadonly())
                {
                   throw new IdentityException("Cannot update readonly attribute: " + attributeName);
                }
 
                if (amd != null && amd.isUnique())
                {
                   IdentityObject checkIdentity = findIdentityObjectByUniqueAttribute(ctx,
                      identity.getIdentityType(),
                      attribute);
 
                   if (checkIdentity != null && !checkIdentity.getName().equals(identity.getName()))
                   {
                      throw new IdentityException("Unique attribute '" + attribute.getName() + " value already set for identityObject: " +
                         checkIdentity);
                   }
                }
 
             }
 
 
             if (values != null)
             {
                for (Object value : values)
                {
                   attr.add(value);
                }
 
                attrs.put(attr);
 
                try
                {
                   // Escape JNDI special characters
                   Name jndiName = new CompositeName().add(dn);
                   ldapContext.modifyAttributes(jndiName, DirContext.ADD_ATTRIBUTE, attrs);
 
                   invalidateCache();
                }
                catch (NamingException e)
                {
                   if (log.isLoggable(Level.FINER))
                   {
                      log.log(Level.FINER, "Exception occurred: ", e);
                   }
 
                   throw new IdentityException("Cannot add attribute", e);
                }
             }
 
          }
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
       // Cache new attributes
       if (getCache() != null)
       {
          getAttributes(ctx, identity);
       }
    }
 
    public void removeAttributes(IdentityStoreInvocationContext ctx, IdentityObject identity, String[] attributeNames) throws IdentityException
    {
 
       if (log.isLoggable(Level.FINER))
       {
          log.finer(toString() + ".removeAttributes with "
             + "identity: " + identity
             + "attributeNames: " + attributeNames
          );
       }
 
       if (getCache() != null)
       {
          getCache().invalidate(getNamespace());
       }
 
       if (attributeNames == null)
       {
          throw new IllegalArgumentException("attributes is null");
       }
 
       LDAPIdentityObjectImpl ldapIdentity = getSafeLDAPIO(ctx, identity);
 
       // as this is valid LDAPIdentityObjectImpl DN is obtained from the Id
 
       String dn = ldapIdentity.getDn();
 
       LdapContext ldapContext = getLDAPContext(ctx);
 
       try
       {
          for (String name : attributeNames)
          {
             String attributeName = getTypeConfiguration(ctx, identity.getIdentityType()).getAttributeMapping(name);
 
             if (attributeName == null)
             {
                log.fine("Proper LDAP attribute mapping not found for such property name: " + name);
                continue;
             }
 
             Map<String, IdentityObjectAttributeMetaData> mdMap = attributesMetaData.get(identity.getIdentityType().getName());
 
             if (mdMap != null)
             {
                //TODO: maybe perform a schema check if this attribute is not required on the LDAP level
                IdentityObjectAttributeMetaData amd = mdMap.get(name);
                if (amd != null && amd.isRequired())
                {
                   throw new IdentityException("Cannot remove required attribute: " + name);
                }
             }
 
 
 
             Attributes attrs = new BasicAttributes(true);
             Attribute attr = new BasicAttribute(attributeName);
             attrs.put(attr);
 
             try
             {
                // Escape JNDI special characters
                Name jndiName = new CompositeName().add(dn);
                ldapContext.modifyAttributes(jndiName, DirContext.REMOVE_ATTRIBUTE, attrs);
 
                invalidateCache();
             }
             catch (NamingException e)
             {
                if (log.isLoggable(Level.FINER))
                {
                   log.log(Level.FINER, "Exception occurred: ", e);
                }
 
                throw new IdentityException("Cannot remove attribute", e);
             }
 
          }
       }
       finally
       {
          try
          {
             ldapContext.close();
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
       // Cache new attributes
       if (getCache() != null)
       {
          getAttributes(ctx, identity);
       }
    }
 
    public IdentityObject findIdentityObjectByUniqueAttribute(IdentityStoreInvocationContext invocationCtx, IdentityObjectType type, IdentityObjectAttribute attribute) throws IdentityException
    {
       String nameFilter = "*";
 
 
       LdapContext ctx = getLDAPContext(invocationCtx);
 
 
       checkIOType(type);
 
       LinkedList<IdentityObject> objects = new LinkedList<IdentityObject>();
 
       LDAPIdentityObjectTypeConfiguration typeConfiguration = getTypeConfiguration(invocationCtx, type);
 
       String attributeName = getTypeConfiguration(invocationCtx, type).getAttributeMapping(attribute.getName());
 
 
       try
       {
          Control[] requestControls = null;
 
          StringBuilder af = new StringBuilder();
 
          // Filter by attribute values
          af.append("(")
             .append(attributeName)
             .append("=")
             .append(attribute.getValue())
             .append(")");
 
          String filter = getTypeConfiguration(invocationCtx, type).getEntrySearchFilter();
          List<SearchResult> sr = null;
 
          String[] entryCtxs = getTypeConfiguration(invocationCtx, type).getCtxDNs();
          String scope = getTypeConfiguration(invocationCtx, type).getEntrySearchScope();
 
          if (filter != null && filter.length() > 0)
          {
 
             filter = filter.replaceAll("\\{0\\}", nameFilter);
             sr = searchIdentityObjects(invocationCtx,
                entryCtxs,
                "(&(" + filter + ")" + af.toString() + ")",
                null,
                new String[]{typeConfiguration.getIdAttributeName()},
                scope,
                requestControls);
          }
          else
          {
             filter = "(".concat(typeConfiguration.getIdAttributeName()).concat("=").concat(nameFilter).concat(")");
             sr = searchIdentityObjects(invocationCtx,
                entryCtxs,
                "(&(" + filter + ")" + af.toString() + ")",
                null,
                new String[]{typeConfiguration.getIdAttributeName()},
                scope,
                requestControls);
          }
 
 
          for (SearchResult res : sr)
          {
             ctx = (LdapContext)res.getObject();
             String dn = ctx.getNameInNamespace();
             objects.add(createIdentityObjectInstance(invocationCtx, type, res.getAttributes(), dn));
          }
 
          ctx.close();
 
 
       }
       catch (NoSuchElementException e)
       {
          //log.debug("No identity object found with name: " + name, e);
       }
       catch (Exception e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("IdentityObject search failed.", e);
       }
       finally
       {
          try
          {
             if (ctx != null)
             {
                ctx.close();
             }
          }
          catch (NamingException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.log(Level.FINER, "Exception occurred: ", e);
             }
 
             throw new IdentityException("Failed to close LDAP connection", e);
          }
       }
 
       if (objects.size() == 0)
       {
          return null;
       }
       if (objects.size() > 1)
       {
          throw new IdentityException("Illegal state - more than one IdentityObject of same type share same unique attribute value");
       }
 
 
       return objects.get(0);
    }
 
    //Internal
 
    public LDAPIdentityObjectImpl createIdentityObjectInstance(IdentityStoreInvocationContext ctx, IdentityObjectType type, Attributes attrs, String dn) throws IdentityException
    {
       LDAPIdentityObjectImpl ldapio = null;
       try
       {
          String idAttrName = getTypeConfiguration(ctx, type).getIdAttributeName();
 
          Attribute ida = attrs.get(idAttrName);
          if (ida == null)
          {
             throw new IdentityException("LDAP entry doesn't contain proper attribute: " + idAttrName + "; dn=" + dn);
          }
 
          //make DN as user ID
          ldapio = new LDAPIdentityObjectImpl(dn, ida.get().toString(), type);
 
       }
       catch (Exception e)
       {
 
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("Couldn't create LDAPIdentityObjectImpl object from ldap entry (SearchResult)", e);
       }
 
       return ldapio;
    }
 
    public List<SearchResult> searchIdentityObjects(IdentityStoreInvocationContext ctx,
                                                    String[] entryCtxs,
                                                    String filter,
                                                    Object[] filterArgs,
                                                    String[] returningAttributes,
                                                    String searchScope,
                                                    Control[] requestControls) throws NamingException, IdentityException
    {
 
        // Set max results for query. Try to get from configuration file, first. Use {@link MAX_SEARCH_RESULTS_DEFAULT}, alternatively.
        try
        {
            List<Control> controlList = new ArrayList<Control>();
            if (requestControls != null)
            {
                controlList.addAll(Arrays.asList(requestControls));
            }
 
           if (maxResults <= 0) // lazily load maxResults from configuration
           {
               maxResults = MAX_SEARCH_RESULTS_DEFAULT; // default value
               try
               {
                   maxResults = Integer.valueOf(getConfiguration(ctx).getConfigurationMetaData().getOptions().get(MAX_SEARCH_RESULTS).get(0));
               }
               catch (Exception e)
               {
                   log.finer("Could not load configuration value for maxResults from options of identity store context: " + ctx);
               }
            }
 
            controlList.add(new PagedResultsControl(maxResults, Control.CRITICAL));
            requestControls = new Control[controlList.size()];
            requestControls = controlList.toArray(requestControls);
        }
        catch (IOException e)
        {
            if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
 
          throw new IdentityException("IdentityObject search failed.", e);
        }
 
 
        //Debug
       if (log.isLoggable(Level.FINER))
       {
          StringBuffer sb = new StringBuffer();
          sb.append("Prepared LDAP Search ");
          if (entryCtxs != null)
          {
             sb.append("; contexts: ").append(Arrays.toString(entryCtxs));
          }
          if (filter != null)
          {
             sb.append("; filter: ").append(filter);
          }
          if (filterArgs != null)
          {
             sb.append("; filter args: ").append(Arrays.toString(filterArgs));
          }
          if (returningAttributes != null)
          {
             sb.append("; returning attributes: ").append(Arrays.toString(returningAttributes));
          }
          if (searchScope != null)
          {
             sb.append("; searchScope: ").append(searchScope);
          }
 
          log.finer(sb.toString());
       }
 
       if (getCache() != null)
       {
          LDAPSearch search =
             new LDAPSearch(entryCtxs, filter, filterArgs, returningAttributes, searchScope, requestControls);
 
          Object results = getCache().getObject(getNamespace(), search.hashCode());
 
          if (results != null && results instanceof List)
          {
 
             //Debug
             if (log.isLoggable(Level.FINER))
             {
                log.finer("LDAP search results found in cache. size=" + ((List)results).size());
             }
 
             return (List<SearchResult>)results;
 
          }
       }
 
 
       LdapContext ldapContext = getLDAPContext(ctx);
 
       if (ldapContext != null)
       {
          ldapContext.setRequestControls(requestControls);
       }
 
       NamingEnumeration results = null;
 
       List<SearchResult> finalResults;
 
       try
       {
 
          SearchControls searchControls = new SearchControls();
          if (searchScope != null)
          {
             if (searchScope.equalsIgnoreCase("subtree"))
             {
                searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
             }
             else if (searchScope.equalsIgnoreCase("object"))
             {
                searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
             }
          }
          searchControls.setReturningObjFlag(true);
          searchControls.setTimeLimit(getConfiguration(ctx).getSearchTimeLimit());
 
 
          if (returningAttributes != null)
          {
             searchControls.setReturningAttributes(returningAttributes);
          }
 
 
          if (entryCtxs.length == 1)
          {
             // Escape JNDI special characters
             Name jndiName = new CompositeName().add(entryCtxs[0]);
 
             if (filterArgs == null)
             {
                results = ldapContext.search(jndiName, filter, searchControls);
             }
             else
             {
                results = ldapContext.search(jndiName, filter, filterArgs, searchControls);
             }
 
             List toReturn = Tools.toList(results);
 
             if (log.isLoggable(Level.FINER) && toReturn != null)
             {
                log.finer("Search in " + entryCtxs[0] + " returned " + toReturn.size() + " entries");
             }
 
             finalResults = toReturn;
 
 
          }
          else
          {
             List<SearchResult> merged = new LinkedList();
 
             for (String entryCtx : entryCtxs)
             {
 
                // Escape JNDI special characters
                Name jndiName = new CompositeName().add(entryCtx);
 
                if (filterArgs == null)
                {
                   results = ldapContext.search(jndiName, filter, searchControls);
                }
                else
                {
                   results = ldapContext.search(jndiName, filter, filterArgs, searchControls);
                }
                List singleResult = Tools.toList(results);
 
                if (log.isLoggable(Level.FINER) && merged != null)
                {
                   log.finer("Search in " + entryCtx + " returned " + merged.size() + " entries");
                }
 
                merged.addAll(singleResult);
                results.close();
             }
 
 
 
             finalResults = merged;
          }
       }
       finally
       {
          if (results != null)
          {
             results.close();
          }
          ldapContext.close();
       }
 
       if (getCache() != null && finalResults != null)
       {
          LDAPSearch search =
             new LDAPSearch(entryCtxs, filter, filterArgs, returningAttributes, searchScope, requestControls);
 
          getCache().putObject(getNamespace(), search.hashCode(), finalResults);
 
          if (log.isLoggable(Level.FINER))
          {
             log.finer("LDAP search results stored in cache. size=" + finalResults.size());
          }
       }
 
       return finalResults;
    }
 
    // HELPER
 
    private LDAPIdentityObjectImpl getSafeLDAPIO(IdentityStoreInvocationContext ctx, IdentityObject io) throws IdentityException
    {
       if (io == null)
       {
          throw new IllegalArgumentException("IdentityObject is null");
       }
 
       if (io instanceof LDAPIdentityObjectImpl)
       {
          return (LDAPIdentityObjectImpl)io;
       }
       else
       {
          try
          {
             return (LDAPIdentityObjectImpl)findIdentityObject(ctx, io.getName(), io.getIdentityType());
          }
          catch (IdentityException e)
          {
             if (log.isLoggable(Level.FINER))
             {
                log.finer("Failed to find IdentityObject in LDAP: " + io);
             }
 
             throw new IdentityException("Provided IdentityObject is not present in the store. Cannot operate on not stored objects.", e);
          }
       }
 
    }
 
    private void checkIOType(IdentityObjectType iot) throws IdentityException
    {
       if (iot == null)
       {
          throw new IllegalArgumentException("IdentityObjectType is null");
       }
 
       if (!getSupportedFeatures().isIdentityObjectTypeSupported(iot))
       {
          throw new IdentityException("IdentityType not supported by this IdentityStore implementation: " + iot);
       }
    }
 
 
    private LdapContext getLDAPContext(IdentityStoreInvocationContext ctx) throws IdentityException
    {
 
       LdapContext ldapContext = null;
 
       try
       {
          ldapContext = (LdapContext)ctx.getIdentityStoreSession().getSessionContext();
       }
       catch (Exception e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.finer("Failed to obtain LDAP connection!");
          }
 
          throw new IdentityException("Could not obtain LDAP connection: ", e);
       }
 
       if (ldapContext == null)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.finer("Failed to obtain LDAP connection!");
          }
 
          throw new IdentityException("IllegalState: - Could not obtain LDAP connection");
       }
 
       return ldapContext;
    }
 
    private LDAPIdentityStoreConfiguration getConfiguration(IdentityStoreInvocationContext ctx) throws IdentityException
    {
       return configuration;
    }
 
    private LDAPIdentityObjectTypeConfiguration getTypeConfiguration(IdentityStoreInvocationContext ctx, IdentityObjectType type) throws IdentityException
    {
       return getConfiguration(ctx).getTypeConfiguration(type.getName());
    }
 
    public String toString()
    {
       return this.getClass().getName() + "[" + getId() +"]";
    }
 
    private void sortByName(List<IdentityObject> objects, final boolean ascending)
    {
       Collections.sort(objects, new Comparator<IdentityObject>(){
          public int compare(IdentityObject o1, IdentityObject o2)
          {
             if (ascending)
             {
                return o1.getName().compareTo(o2.getName());
             }
             else
             {
                return o2.getName().compareTo(o1.getName());
             }
          }
       });
    }
 
    //TODO: dummy and inefficient temporary workaround. Need to be implemented with ldap request control
    private <T> List<T> cutPageFromResults(List<T> objects, IdentityObjectSearchCriteria criteria)
    {
 
       List<T> results = new LinkedList<T>();
 
       if (criteria.getMaxResults() == 0)
       {
          for (int i = criteria.getFirstResult(); i < objects.size(); i++)
          {
             if (i < objects.size())
             {
                results.add(objects.get(i));
             }
          }
       }
       else
       {
          for (int i = criteria.getFirstResult(); i < criteria.getFirstResult() + criteria.getMaxResults(); i++)
          {
             if (i < objects.size())
             {
                results.add(objects.get(i));
             }
          }
       }
       return results;
    }
 
    protected void checkCtx(DirContext ctx, String dn) throws Exception
    {
       String[] parts = dn.split(",");
 
       // Reverse array so we start with the root DN
 
       int l  = 0;
       int r = parts.length-1;
 
       while (l < r)
       {
          String temp = parts[l];
          parts[l]  = parts[r];
          parts[r] = temp;
 
          l++;
          r--;
       }
 
       // Discover root DN
       String rootDN = "";
       DirContext root = null;
 
       for (String part : parts)
       {
 
          if (rootDN.length() > 0)
          {
             rootDN = part + "," + rootDN;
          }
          else
          {
             rootDN = part;
          }
 
 
          try
          {
             // Escape JNDI special characters
             Name jndiName = new CompositeName().add(rootDN);
             root = (DirContext)ctx.lookup(jndiName);
          }
          catch (NamingException e)
          {
             // Ignore
          }
 
          if (root != null)
          {
             break;
          }
       }
 
 
       DirContext dx = root;
 
       String parsedDN = "";
 
       try
       {
          for (String part : parts)
          {
             if (parsedDN.length() > 0)
             {
                parsedDN = part + "," + parsedDN;
             }
             else
             {
                parsedDN = part;
             }
 
 
             if (parsedDN.length() > rootDN.length())
             {
                dx = obtainOrCreateContext(dx, part);
             }
 
          }
       }
       finally
       {
          if (dx != null)
          {
             dx.close();
          }
       }
 
 
    }
 
 
    protected DirContext obtainOrCreateContext(DirContext ctx, String dn) throws Exception
    {
 
       if (ctx == null)
       {
          throw new IllegalArgumentException("DirContext is null");
       }
       if (dn == null || dn.length() == 0 || !dn.contains("="))
       {
          throw new IllegalArgumentException("DN doens't have proper format: " + dn);
       }
 
       DirContext subContext = null;
       try
       {
          // Escape JNDI special characters
          Name jndiName = new CompositeName().add(dn);
          subContext = (LdapContext)ctx.lookup(jndiName);
       }
       catch (NamingException e)
       {
          // Ignore
       }
 
       if (subContext != null)
       {
          ctx.close();
          return subContext;
       }
 
       // Create new context
       Attributes attrs = new BasicAttributes(true);
 
       String[] parts = dn.split("=");
 
       // Set the dn attr
       Attribute attr = new BasicAttribute(parts[0]);
       attr.add(parts[1]);
       attrs.put(attr);
 
       // objectClass=top
       attr = new BasicAttribute("objectClass");
       attr.add("top");
 
       if (parts[0].equalsIgnoreCase("dc"))
       {
          attr.add("dcObject");
       }
       else if (parts[0].equalsIgnoreCase("o"))
       {
          attr.add("organization");
       }
       else if (parts[0].equalsIgnoreCase("ou"))
       {
          attr.add("organizationalUnit");
       }
       else if (parts[0].equalsIgnoreCase("cn"))
       {
          attr.add("organizationalRole");
       }
       else if (parts[0].equalsIgnoreCase("c"))
       {
          attr.add("country");
       }
 
       attrs.put(attr);
 
       try
       {
          // Escape JNDI special characters
          Name jndiName = new CompositeName().add(dn);
          subContext = ctx.createSubcontext(jndiName, attrs);
 
       }
       finally
       {
          ctx.close();
       }
       return subContext;
 
 
    }
 
    IdentityStoreCacheProvider getCache()
    {
       return cache;
    }
 
    String getNamespace()
    {
       return getId();
    }
 
    void invalidateCache()
    {
       if (getCache() != null)
       {
          getCache().invalidate(getNamespace());
       }
    }
 
 }
