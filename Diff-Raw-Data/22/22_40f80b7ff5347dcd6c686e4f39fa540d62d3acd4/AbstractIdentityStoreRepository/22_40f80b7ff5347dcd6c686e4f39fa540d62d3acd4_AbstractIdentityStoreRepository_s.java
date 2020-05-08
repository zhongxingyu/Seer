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
 
 package org.picketlink.idm.impl.repository;
 
 import org.picketlink.idm.impl.cache.CacheIdentityStoreWrapper;
 import org.picketlink.idm.spi.repository.IdentityStoreRepository;
 import org.picketlink.idm.spi.store.IdentityStore;
 import org.picketlink.idm.spi.store.AttributeStore;
 import org.picketlink.idm.spi.store.IdentityStoreInvocationContext;
 import org.picketlink.idm.spi.model.IdentityObjectType;
 import org.picketlink.idm.spi.model.IdentityObject;
 import org.picketlink.idm.spi.configuration.metadata.IdentityRepositoryConfigurationMetaData;
 import org.picketlink.idm.spi.configuration.metadata.IdentityStoreMappingMetaData;
 import org.picketlink.idm.spi.configuration.IdentityRepositoryConfigurationContext;
 import org.picketlink.idm.spi.cache.IdentityStoreCacheProvider;
 import org.picketlink.idm.common.exception.IdentityException;
 
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.Map;
 import java.util.HashSet;
 import java.util.List;
 import java.util.HashMap;
 import java.io.Serializable;
 import java.lang.reflect.Constructor;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
  * @version : 0.1 $
  */
 public abstract class AbstractIdentityStoreRepository implements IdentityStoreRepository, Serializable
 {
 
    private static Logger log = Logger.getLogger(AbstractIdentityStoreRepository.class.getName());
 
    protected Map<String, List<IdentityStore>> identityStoreMappings = new HashMap<String, List<IdentityStore>>();
 
    protected Map<String, List<AttributeStore>> attributeStoreMappings = new HashMap<String, List<AttributeStore>>();
 
    protected Set<IdentityStore> configuredIdentityStores = new HashSet<IdentityStore>();
 
    protected Set<AttributeStore> configuredAttributeStores = new HashSet<AttributeStore>();
 
    protected IdentityStore defaultIdentityStore;
 
    protected AttributeStore defaultAttributeStore;
 
    protected IdentityRepositoryConfigurationContext configurationContext;
 
    public static final String CACHE_OPTION = "cache";
 
    public static final String CACHE_CONFIG_FILE_OPTION = "cache.configFile";
 
    public static final String CACHE_PROVIDER_CLASS_OPTION = "cache.providerClass";
 
    public static final String CACHE_PROVIDER_REGISTRY_NAME = "cache.providerRegistryName";
 
    public static final String CACHE_SCOPE = "cache.scope";
 
    public static final String ALLOW_NOT_DEFINED_IDENTITY_OBJECT_TYPES_OPTION = "allowNotDefinedIdentityObjectTypes";
 
    private boolean allowNotDefinedIdentityObjectTypes = false;
 
 
    public void bootstrap(IdentityRepositoryConfigurationContext configurationContext,
                          Map<String, IdentityStore> bootstrappedIdentityStores,
                          Map<String, AttributeStore> bootstrappedAttributeStores) throws IdentityException
    {
       this.configurationContext = configurationContext;
 
       IdentityRepositoryConfigurationMetaData configurationMD = configurationContext.getRepositoryConfigurationMetaData();
 
       String asId = configurationMD.getDefaultAttributeStoreId();
       String isId = configurationMD.getDefaultIdentityStoreId();
 
       if (asId != null && bootstrappedAttributeStores.keySet().contains(asId))
       {
          defaultAttributeStore = bootstrappedAttributeStores.get(asId);
          configuredAttributeStores.add(defaultAttributeStore);
 
          //TODO: cache wrap support
       }
 
       String allowNotDefinedIOT = configurationMD.getOptionSingleValue(ALLOW_NOT_DEFINED_IDENTITY_OBJECT_TYPES_OPTION);
 
       if (allowNotDefinedIOT != null && allowNotDefinedIOT.equalsIgnoreCase("true"))
       {
          this.allowNotDefinedIdentityObjectTypes = true;
       }
 
       if (isId != null && bootstrappedIdentityStores.keySet().contains(isId))
       {
          defaultIdentityStore = bootstrappedIdentityStores.get(isId);
          configuredIdentityStores.add(defaultIdentityStore);
 
          String cacheOption = configurationMD.getOptionSingleValue(CACHE_OPTION);
 
          if (cacheOption != null && cacheOption.equalsIgnoreCase("true"))
          {
             String cacheSupportClass = configurationMD.getOptionSingleValue(CACHE_PROVIDER_CLASS_OPTION);
             String cacheRegistryName = configurationMD.getOptionSingleValue(CACHE_PROVIDER_REGISTRY_NAME);
             String cacheScope = configurationMD.getOptionSingleValue(CACHE_PROVIDER_REGISTRY_NAME);
 
             if (cacheSupportClass == null && cacheRegistryName == null)
             {
                throw new IdentityException(CACHE_PROVIDER_CLASS_OPTION + " is missing in the repository configuration");
             }
 
             IdentityStoreCacheProvider cacheSupport = null;
 
             Map<String, String> cacheProps = new HashMap<String, String>();
 
             // Parse all 'cache.' prefixed options
             for (String key : configurationMD.getOptions().keySet())
             {
                if (key.startsWith("cache."))
                {
                   if (configurationMD.getOptions().get(key).size() > 0)
                   {
                      cacheProps.put(key, configurationMD.getOptions().get(key).get(0));
                   }
                }
             }
 
             if (cacheRegistryName != null)
             {
                try
                {
                   cacheSupport = (IdentityStoreCacheProvider)configurationContext.getConfigurationRegistry().getObject(cacheRegistryName);
                }
                catch (IdentityException e)
                {
 
                   if (log.isLoggable(Level.FINER))
                   {
                      log.log(Level.FINER, "Exception occurred: ", e);
                   }
 
                   throw new IdentityException("Cannot find IdentityStoreCacheProvider cache provider instance" +
                      "with provided name:" + cacheRegistryName, e);
                }
             }
             else
             {
                try
                {
                   Class cacheClass = null;
                   cacheClass = Class.forName(cacheSupportClass);
 
                   Constructor ct = cacheClass.getConstructor();
 
                   cacheSupport = (IdentityStoreCacheProvider)ct.newInstance();
 
                   cacheSupport.initialize(cacheProps, configurationContext);
 
                }
                catch (Exception e)
                {
                   if (log.isLoggable(Level.FINER))
                   {
                      log.log(Level.FINER, "Exception occurred: ", e);
                   }
 
                   throw new IdentityException("Cannot instantiate cache provider:" + cacheSupportClass, e);
                }
             }
 
 
             defaultIdentityStore = new CacheIdentityStoreWrapper(defaultIdentityStore, cacheSupport, cacheScope);
 
 
          }
       }
 
       for (IdentityStoreMappingMetaData identityStoreMappingMetaData : configurationMD.getIdentityStoreToIdentityObjectTypeMappings())
       {
          String storeId = identityStoreMappingMetaData.getIdentityStoreId();
          List<String> identityObjectTypeMappings = identityStoreMappingMetaData.getIdentityObjectTypeMappings();
 
          IdentityStore store = bootstrappedIdentityStores.get(storeId);
 
          String cacheOption = identityStoreMappingMetaData.getOptionSingleValue(CACHE_OPTION);
 
 
          if (cacheOption != null && cacheOption.equalsIgnoreCase("true"))
          {
 
             String cacheSupportClass = identityStoreMappingMetaData.getOptionSingleValue(CACHE_PROVIDER_CLASS_OPTION);
             String cacheRegistryName = identityStoreMappingMetaData.getOptionSingleValue(CACHE_PROVIDER_REGISTRY_NAME);
             String cacheScope = identityStoreMappingMetaData.getOptionSingleValue(CACHE_PROVIDER_REGISTRY_NAME);
 
             if (cacheSupportClass == null && cacheRegistryName == null)
             {
                throw new IdentityException(CACHE_PROVIDER_CLASS_OPTION + " is missing in the <identity-store><options> configuration");
             }
 
             IdentityStoreCacheProvider cacheSupport = null;
 
             Map<String, String> cacheProps = new HashMap<String, String>();
 
             // Parse all 'cache.' prefixed options
             for (String key : identityStoreMappingMetaData.getOptions().keySet())
             {
                if (key.startsWith("cache."))
                {
                   if (identityStoreMappingMetaData.getOptions().get(key).size() > 0)
                   {
                      cacheProps.put(key, identityStoreMappingMetaData.getOptions().get(key).get(0));
                   }
                }
             }
 
             if (cacheRegistryName != null)
             {
                try
                {
                   cacheSupport = (IdentityStoreCacheProvider)configurationContext.getConfigurationRegistry().getObject(cacheRegistryName);
                }
                catch (IdentityException e)
                {
 
                   if (log.isLoggable(Level.FINER))
                   {
                      log.log(Level.FINER, "Exception occurred: ", e);
                   }
 
                   throw new IdentityException("Cannot find IdentityStoreCacheProvider cache provider instance" +
                      "with provided name:" + cacheRegistryName, e);
                }
             }
             else
             {
                try
                {
                   Class cacheClass = null;
                   cacheClass = Class.forName(cacheSupportClass);
 
                   Constructor ct = cacheClass.getConstructor();
 
                   cacheSupport = (IdentityStoreCacheProvider)ct.newInstance();
 
                   cacheSupport.initialize(cacheProps, configurationContext);
 
                }
                catch (Exception e)
                {
                   if (log.isLoggable(Level.FINER))
                   {
                      log.log(Level.FINER, "Exception occurred: ", e);
                   }
 
                   throw new IdentityException("Cannot instantiate cache provider:" + cacheSupportClass, e);
                }
             }
 
 
             store = new CacheIdentityStoreWrapper(store, cacheSupport, cacheScope);
 
          }
 
          if (store == null)
          {
             throw new IdentityException("Mapped IdentityStore not available: " + storeId);
          }
 
          for (String mapping : identityObjectTypeMappings)
          {
             List<IdentityStore> mappedIS = identityStoreMappings.get(mapping);
             if (mappedIS == null)
             {
                mappedIS = new LinkedList<IdentityStore>();
                identityStoreMappings.put(mapping, mappedIS);
             }
             mappedIS.add(store);
             configuredIdentityStores.add(store);
 
             List<AttributeStore> mappedAS = attributeStoreMappings.get(mapping);
             if (mappedAS == null)
             {
                mappedAS = new LinkedList<AttributeStore>();
                attributeStoreMappings.put(mapping, mappedAS);
             }
             mappedAS.add(store);
             configuredAttributeStores.add(store);
 
          }
 
       }
 
    }
 
    public Set<IdentityStore> getConfiguredIdentityStores()
    {
       return new HashSet<IdentityStore>(configuredIdentityStores);
    }
 
    public Set<AttributeStore> getConfiguredAttributeStores()
    {
       return new HashSet<AttributeStore>(configuredIdentityStores);
    }
 
    public Map<String, IdentityStore> getIdentityStoreMappings()
    {
       Map<String, IdentityStore> firstInTheList = new HashMap<String, IdentityStore>();
 
       for (Map.Entry<String, List<IdentityStore>> entry : identityStoreMappings.entrySet())
       {
          if(entry.getValue().size() > 0)
          {
             firstInTheList.put(entry.getKey(), entry.getValue().get(0));
          }
       }
 
       return firstInTheList;
    }
 
    public Map<String, AttributeStore> getAttributeStoreMappings()
    {
       Map<String, AttributeStore> firstInTheList = new HashMap<String, AttributeStore>();
 
       for (Map.Entry<String, List<AttributeStore>> entry : attributeStoreMappings.entrySet())
       {
          if(entry.getValue().size() > 0)
          {
             firstInTheList.put(entry.getKey(), entry.getValue().get(0));
          }
       }
 
       return firstInTheList;
    }
 
    public List<IdentityStore> getIdentityStores(IdentityObjectType identityObjectType) throws IdentityException
    {
       if (identityStoreMappings.keySet().contains(identityObjectType.getName()))
       {
          return identityStoreMappings.get(identityObjectType.getName());
       }
       else
       {
          return new LinkedList<IdentityStore>();
       }
 
    }
 
    public List<AttributeStore> getAttributeStores(IdentityObjectType identityObjectType) throws IdentityException
    {
       if (attributeStoreMappings.keySet().contains(identityObjectType.getName()))
       {
          return attributeStoreMappings.get(identityObjectType.getName());
       }
       else
       {
          return new LinkedList<AttributeStore>();
       }
 
    }
 
    public IdentityStore getIdentityStore(IdentityObjectType identityObjectType) throws IdentityException
    {
       IdentityStore store = getIdentityStoreMappings().get(identityObjectType.getName());
 
       if (store == null)
       {
          String option = configurationContext.getRepositoryConfigurationMetaData().getOptionSingleValue(ALLOW_NOT_DEFINED_IDENTITY_OBJECT_TYPES_OPTION);
 
          if (option != null && option.equalsIgnoreCase("true"))
          {
             return defaultIdentityStore;
          }
          else
          {
             throw new IdentityException("IdentityObjectType not mapped in the configuration: " + identityObjectType);
          }
       }
 
       return store;
    }
 
    public AttributeStore getAttributeStore(IdentityObjectType identityObjectType) throws IdentityException
    {
       AttributeStore store = getAttributeStoreMappings().get(identityObjectType.getName());
 
       if (store == null)
       {
          String option = configurationContext.getRepositoryConfigurationMetaData().getOptionSingleValue(ALLOW_NOT_DEFINED_IDENTITY_OBJECT_TYPES_OPTION);
 
          if (option != null && option.equalsIgnoreCase("true"))
          {
             return defaultIdentityStore;
          }
          else
          {
             throw new IdentityException("IdentityObjectType not mapped in the configuration: " + identityObjectType);
          }
       }
 
       return store;
    }
 
    protected boolean hasIdentityObject(IdentityStoreInvocationContext ctx, IdentityStore is, IdentityObject io) throws IdentityException
    {
       try
       {
          if (is.findIdentityObject(ctx, io.getName(), io.getIdentityType()) != null)
          {
             return true;
          }
       }
       catch (IdentityException e)
       {
          //store may throw exception if there is no identity so do nothing
          //TODO: should have isIdentityPresent method
          
       }
       return false;
    }
 
 
    public boolean isAllowNotDefinedIdentityObjectTypes()
    {
       return allowNotDefinedIdentityObjectTypes;
    }
 }
