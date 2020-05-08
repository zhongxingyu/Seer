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
 
 package org.picketlink.idm.impl.api.session.managers;
 
 import org.picketlink.idm.api.*;
 import org.picketlink.idm.common.exception.IdentityException;
 import org.picketlink.idm.impl.api.model.GroupKey;
 import org.picketlink.idm.impl.api.model.SimpleGroup;
 import org.picketlink.idm.impl.api.model.SimpleUser;
 import org.picketlink.idm.impl.api.session.IdentitySessionImpl;
 import org.picketlink.idm.impl.cache.GroupSearchImpl;
 import org.picketlink.idm.impl.types.SimpleIdentityObjectType;
 import org.picketlink.idm.spi.model.IdentityObject;
 import org.picketlink.idm.spi.model.IdentityObjectType;
 import org.picketlink.idm.spi.store.IdentityObjectSearchCriteriaType;
 import org.picketlink.idm.spi.store.IdentityStore;
 
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
  * @version : 0.1 $
  */
 public class PersistenceManagerImpl extends AbstractManager implements PersistenceManager, Serializable
 {
 
     private static Logger log = Logger.getLogger(PersistenceManagerImpl.class.getName());
 
     private final PersistenceManagerFeaturesDescription featuresDescription;
 
     private static final long serialVersionUID = -4691446225503953920L;
 
 
     public PersistenceManagerImpl(IdentitySessionImpl session)
     {
         super(session);
 
         featuresDescription = new PersistenceManagerFeaturesDescription()
         {
             public boolean isUsersAddRemoveSupported()
             {
                 IdentityObjectType objectType = getSessionContext().getIdentityObjectTypeMapper().getIdentityObjectType();
 
                 return getSessionContext().getIdentityStoreRepository().getSupportedFeatures().
                         isIdentityObjectAddRemoveSupported(objectType);
             }
 
             public boolean isGroupsAddRemoveSupported(String groupType)
             {
                 IdentityObjectType objectType = getSessionContext().getIdentityObjectTypeMapper().getIdentityObjectType(groupType);
 
                 return getSessionContext().getIdentityStoreRepository().getSupportedFeatures().
                         isIdentityObjectAddRemoveSupported(objectType);
             }
 
             public boolean isUsersSearchCriteriumTypeSupported(IdentitySearchCriteriumType constraintType)
             {
                 IdentityObjectType objectType = getSessionContext().getIdentityObjectTypeMapper().getIdentityObjectType();
 
                 IdentityObjectSearchCriteriaType constraint = IdentityObjectSearchCriteriaType.valueOf(constraintType.name());
 
 
                 if (constraint != null)
                 {
                     return getSessionContext().getIdentityStoreRepository().getSupportedFeatures().
                             isSearchCriteriaTypeSupported(objectType, constraint);
                 }
 
                 return false;
             }
 
 
             public boolean isGroupsSearchCriteriumTypeSupported(String groupType, IdentitySearchCriteriumType constraintType)
             {
                 IdentityObjectType objectType = getSessionContext().getIdentityObjectTypeMapper().getIdentityObjectType(groupType);
 
                 IdentityObjectSearchCriteriaType constraint = IdentityObjectSearchCriteriaType.valueOf(constraintType.name());
 
                 if (constraint != null)
                 {
                     return getSessionContext().getIdentityStoreRepository().getSupportedFeatures().
                             isSearchCriteriaTypeSupported(objectType, constraint);
                 }
 
                 return false;
             }
 
         };
     }
 
     public PersistenceManagerFeaturesDescription getFeaturesDescription()
     {
         return featuresDescription;
     }
 
     public User createUser(String identityName) throws IdentityException
     {
         try
         {
             checkNotNullArgument(identityName, "Identity name");
             checkObjectName(identityName);
 
             IdentityObjectType iot = getUserObjectType();
 
             preCreate(new SimpleUser(identityName));
 
             IdentityObject identityObject = getRepository().createIdentityObject(getInvocationContext(), identityName, iot);
 
             User user = null;
 
             if (identityObject != null)
             {
                 user = new SimpleUser(identityName);
             }
 
             //Cache
             if (cache != null)
             {
                 //TODO: maybe invalidate only part
                 cache.invalidate(cacheNS);
                 cache.putUser(cacheNS, user);
             }
 
             postCreate(user);
 
 
             return user;
         }
         catch (IdentityException e)
         {
             if (log.isLoggable(Level.FINER))
             {
                 log.log(Level.FINER, "Exception occurred: ", e);
             }
             throw e;
         }
     }
 
     public Group createGroup(String groupName, String groupType) throws IdentityException
     {
         try
         {
             checkNotNullArgument(groupName, "Group name");
             checkNotNullArgument(groupType, "Group type");
             checkObjectName(groupName);
             checkObjectName(groupType);
 
             IdentityObjectType iot = getIdentityObjectType(groupType);
 
             preCreate(new SimpleGroup(groupName, groupType));
 
             IdentityObject identityObject = getRepository().createIdentityObject(getInvocationContext(), groupName, iot);
 
 
             Group group = null;
 
             if (identityObject != null)
             {
                 group = new SimpleGroup(groupName, groupType);
             }
 
             if (cache != null)
             {
                 //TODO: maybe invalidate only part
                 cache.invalidate(cacheNS);
                 cache.putGroup(cacheNS, group);
             }
 
             postCreate(new SimpleGroup(groupName, groupType));
 
             return group;
         }
         catch (IdentityException e)
         {
             if (log.isLoggable(Level.FINER))
             {
                 log.log(Level.FINER, "Exception occurred: ", e);
             }
             throw e;
         }
     }
 
     public String createGroupKey(String groupName, String groupType)
     {
         checkNotNullArgument(groupName, "Group name");
         checkNotNullArgument(groupType, "Group type");
         checkObjectName(groupName);
         checkObjectName(groupType);
 
         return new GroupKey(groupName, groupType).getKey();
     }
 
     public String createUserKey(String id)
     {
         return id;
     }
 
     public void removeUser(User user, boolean force) throws IdentityException
     {
         try
         {
             checkNotNullArgument(user, "User");
 
             preRemove(user);
 
             getRepository().removeIdentityObject(getInvocationContext(), createIdentityObject(user));
 
             if (cache != null)
             {
                 //TODO: maybe invalidate only part
                 cache.invalidate(cacheNS);
             }
 
             postRemove(user);
         }
         catch (IdentityException e)
         {
             if (log.isLoggable(Level.FINER))
             {
                 log.log(Level.FINER, "Exception occurred: ", e);
             }
             throw e;
         }
 
 
     }
 
     public void removeUser(String userName, boolean force) throws IdentityException
     {
         try
         {
             checkNotNullArgument(userName, "User name");
 
             preRemove(new SimpleUser(userName));
 
             getRepository().removeIdentityObject(getInvocationContext(), createIdentityObjectForUserName(userName));
 
             if (cache != null)
             {
                 //TODO: maybe invalidate only part
                 cache.invalidate(cacheNS);
             }
 
             postRemove(new SimpleUser(userName));
         }
         catch (IdentityException e)
         {
             if (log.isLoggable(Level.FINER))
             {
                 log.log(Level.FINER, "Exception occurred: ", e);
             }
             throw e;
         }
 
     }
 
     public void removeGroup(Group group, boolean force) throws IdentityException
     {
         try
         {
             checkNotNullArgument(group, "Group");
 
             //TODO: force
 
             preRemove(group);
 
             getRepository().removeIdentityObject(getInvocationContext(), createIdentityObject(group));
 
             if (cache != null)
             {
                 cache.invalidate(cacheNS);
             }
 
             postRemove(group);
         }
         catch (IdentityException e)
         {
             if (log.isLoggable(Level.FINER))
             {
                 log.log(Level.FINER, "Exception occurred: ", e);
             }
             throw e;
         }
     }
 
     public void removeGroup(String groupId, boolean force) throws IdentityException
     {
         try
         {
             checkNotNullArgument(groupId, "Group Id");
 
             //TODO: force
 
             preRemove(new SimpleGroup(new GroupKey(groupId)));
 
             getRepository().removeIdentityObject(getInvocationContext(), createIdentityObjectForGroupId(groupId));
 
             if (cache != null)
             {
                 cache.invalidate(cacheNS);
             }
 
             postRemove(new SimpleGroup(new GroupKey(groupId)));
         }
         catch (IdentityException e)
         {
             if (log.isLoggable(Level.FINER))
             {
                 log.log(Level.FINER, "Exception occurred: ", e);
             }
             throw e;
         }
 
     }
 
     public int getUserCount() throws IdentityException
     {
         try
         {
             if (cache != null)
             {
                 int count = cache.getUserCount(cacheNS);
                 if (count != -1)
                 {
                     return count;
                 }
             }
 
             // count users for all stores
             final Map<String, IdentityStore> identityStoreMappings = getRepository().getIdentityStoreMappings();
             int count = 0;
             for (String storeId : identityStoreMappings.keySet())
             {
                 int partialCount = getRepository().getIdentityObjectsCount(getInvocationContext(), new SimpleIdentityObjectType(storeId));
                 count += partialCount;
             }
 
             if (cache != null)
             {
                 cache.putUserCount(cacheNS, count);
             }
 
             return count;
         }
         catch (IdentityException e)
         {
             if (log.isLoggable(Level.FINER))
             {
                 log.log(Level.FINER, "Exception occurred: ", e);
             }
             throw e;
         }
     }
 
     public int getGroupTypeCount(String groupType) throws IdentityException
     {
         try
         {
             checkNotNullArgument(groupType, "Group type");
 
             if (cache != null)
             {
                 int count = cache.getGroupCount(cacheNS, groupType);
                 if (count != -1)
                 {
                     return count;
                 }
             }
 
             IdentityObjectType iot = getIdentityObjectType(groupType);
 
             int count = getRepository().getIdentityObjectsCount(getInvocationContext(), iot);
 
             if (cache != null)
             {
                 cache.putGroupCount(cacheNS, groupType, count);
             }
 
             return count;
         }
         catch (IdentityException e)
         {
             if (log.isLoggable(Level.FINER))
             {
                 log.log(Level.FINER, "Exception occurred: ", e);
             }
             throw e;
         }
     }
 
     public User findUser(String name) throws IdentityException
     {
         try
         {
             checkNotNullArgument(name, "User name");
 
             if (cache != null)
             {
                 User user = cache.getUser(cacheNS, name);
                 if (user != null)
                 {
                     return user;
                 }
             }
 
             // find user in multiple stores
             final Map<String, IdentityStore> identityStoreMappings = getRepository().getIdentityStoreMappings();
             IdentityObject io = null;
             for (String storeId : identityStoreMappings.keySet())
             {
                 IdentityObject foundIo = getRepository().findIdentityObject(getInvocationContext(), name, new SimpleIdentityObjectType(storeId));
                 if (foundIo != null)
                 {
                     io = foundIo;
                     break;
                 }
             }
 
             if (io != null)
             {
                 User user = createUser(io);
 
                 if (cache != null)
                 {
                     cache.putUser(cacheNS, user);
                 }
 
                 return user;
             }
             return null;
         }
         catch (IdentityException e)
         {
             if (log.isLoggable(Level.FINER))
             {
                 log.log(Level.FINER, "Exception occurred: ", e);
             }
             throw e;
         }
     }
 
     public Collection<User> findUser(IdentitySearchCriteria criteria) throws IdentityException
     {
         try
         {
             if (cache != null)
             {
                 Collection<User> users = cache.getUsers(cacheNS, criteria);
                 if (users != null)
                 {
                     return users;
                 }
             }
 
             // search all stores
             final Map<String, IdentityStore> identityStoreMappings = getRepository().getIdentityStoreMappings();
             List<IdentityObject> identityObjects = new LinkedList<IdentityObject>();
 
             for (String storeId : identityStoreMappings.keySet())
             {
                 final Collection<IdentityObject> ios = getRepository().findIdentityObject(getInvocationContext(), new SimpleIdentityObjectType(storeId), convertSearchControls(criteria));
                 identityObjects.addAll(ios);
             }
 
             final List<User> identities = new LinkedList<User>();
             for (IdentityObject identityObject : identityObjects)
             {
                 identities.add(createUser(identityObject));
             }
 
             if (cache != null)
             {
                 cache.putUsers(cacheNS, criteria, identities);
             }
 
             return identities;
         }
         catch (IdentityException e)
         {
             if (log.isLoggable(Level.FINER))
             {
                 log.log(Level.FINER, "Exception occurred: ", e);
             }
             throw e;
         }
     }
 
     public Group findGroup(String name, String groupType) throws IdentityException
     {
         try
         {
             checkNotNullArgument(name, "Group name");
             checkNotNullArgument(groupType, "Group type");
 
             if (cache != null)
             {
                 Group group = cache.getGroup(cacheNS, groupType, name);
                 if (group != null)
                 {
                     return group;
                 }
             }
 
             IdentityObject io = getRepository().findIdentityObject(getInvocationContext(), name, getIdentityObjectType(groupType));
 
             if (io != null)
             {
                 Group group = createGroup(io);
 
                 if (cache != null)
                 {
                     cache.putGroup(cacheNS, group);
                 }
 
                 return group;
             }
             return null;
         }
         catch (IdentityException e)
         {
             if (log.isLoggable(Level.FINER))
             {
                 log.log(Level.FINER, "Exception occurred: ", e);
             }
             throw e;
         }
     }
 
     public Group findGroupByKey(String id) throws IdentityException
     {
         checkNotNullArgument(id, "Group id");
 
         if (!GroupKey.validateKey(id))
         {
             throw new IdentityException("Provided group id is not valid: " + id + "; " +
                     "Please use PersistenceManager.createGroupKey() to obtain valid group id");
         }
 
         GroupKey groupKey = new GroupKey(id);
 
         return findGroup(groupKey.getName(), groupKey.getType());
 
     }
 
     public Collection<Group> findGroup(String groupType, IdentitySearchCriteria criteria) throws IdentityException
     {
         try
         {
             checkNotNullArgument(groupType, "Group type");
 
             if (cache != null)
             {
                 GroupSearchImpl search = new GroupSearchImpl();
                 search.setGroupType(groupType);
                 search.setSearchCriteria(criteria);
 
                 Collection<Group> results = cache.getGroupSearch(cacheNS, search);
                 if (results != null)
                 {
                     return results;
                 }
             }
 
             Collection<IdentityObject> ios = getRepository().findIdentityObject(getInvocationContext(), getIdentityObjectType(groupType), convertSearchControls(criteria));
             List<Group> groups = new LinkedList<Group>();
 
             for (Iterator<IdentityObject> iterator = ios.iterator(); iterator.hasNext(); )
             {
                 IdentityObject identityObject = iterator.next();
                 groups.add(createGroup(identityObject));
             }
 
             if (cache != null)
             {
                 GroupSearchImpl search = new GroupSearchImpl();
                 search.setGroupType(groupType);
                 search.setSearchCriteria(criteria);
 
                 cache.putGroupSearch(cacheNS, search, groups);
 
             }
 
             return groups;
         }
         catch (IdentityException e)
         {
             if (log.isLoggable(Level.FINER))
             {
                 log.log(Level.FINER, "Exception occurred: ", e);
             }
             throw e;
         }
     }
 
     public Collection<Group> findGroup(String groupType) throws IdentityException
     {
         checkNotNullArgument(groupType, "Group type");
 
         return findGroup(groupType, (IdentitySearchCriteria) null);
     }
 
 
 }
