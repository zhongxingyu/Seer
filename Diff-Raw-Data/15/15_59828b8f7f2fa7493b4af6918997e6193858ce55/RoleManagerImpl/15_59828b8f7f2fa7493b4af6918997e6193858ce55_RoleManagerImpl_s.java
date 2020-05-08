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
 
 import org.picketlink.idm.api.RoleManager;
 import org.picketlink.idm.api.RoleType;
 import org.picketlink.idm.api.Role;
 import org.picketlink.idm.api.User;
 import org.picketlink.idm.api.Group;
 import org.picketlink.idm.api.IdentityType;
 import org.picketlink.idm.api.RoleManagerFeaturesDescription;
 import org.picketlink.idm.api.IdentitySearchCriteria;
 import org.picketlink.idm.api.IdentitySearchCriteriumType;
 import org.picketlink.idm.common.exception.IdentityException;
 import org.picketlink.idm.spi.model.IdentityObjectRelationshipType;
 import org.picketlink.idm.spi.model.IdentityObjectRelationship;
 import org.picketlink.idm.spi.model.IdentityObjectType;
 import org.picketlink.idm.spi.model.IdentityObject;
 import org.picketlink.idm.spi.exception.OperationNotSupportedException;
 import org.picketlink.idm.spi.store.IdentityObjectSearchCriteriaType;
 import org.picketlink.idm.impl.api.model.SimpleRoleType;
 import org.picketlink.idm.impl.api.model.SimpleRole;
 import org.picketlink.idm.impl.api.session.IdentitySessionImpl;
 import org.picketlink.idm.impl.types.SimpleIdentityObjectRelationship;
 import org.picketlink.idm.impl.cache.RoleTypeSearchImpl;
 import org.picketlink.idm.impl.cache.UserSearchImpl;
 import org.picketlink.idm.impl.cache.GroupSearchImpl;
 import org.picketlink.idm.impl.cache.RoleSearchImpl;
 import org.picketlink.idm.cache.RoleTypeSearch;
 
 import java.util.Collection;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.HashMap;
 import java.io.Serializable;
 import java.util.logging.Logger;
 
 /**
  * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
  * @version : 0.1 $
  */
 public class RoleManagerImpl extends AbstractManager implements RoleManager, Serializable
 {
 
    private static Logger log = Logger.getLogger(RoleManagerImpl.class.getName());
 
    public static final IdentityObjectRelationshipType ROLE = new IdentityObjectRelationshipType()
    {
       public String getName()
       {
          return "JBOSS_IDENTITY_ROLE";
       }
    };
 
    private final RoleManagerFeaturesDescription featuresDescription;
    
    private static final long serialVersionUID = 7246982831145808636L;
 
    public RoleManagerImpl(IdentitySessionImpl session)
    {
       super(session);
 
       featuresDescription = new RoleManagerFeaturesDescription()
       {
          public boolean isRoleTypeAddRemoveSupported()
          {
             return getSessionContext().getIdentityStoreRepository().getSupportedFeatures().isRelationshipNameAddRemoveSupported();
          }
 
          public boolean isRoleTypeSearchCriteriumTypeSupported(IdentitySearchCriteriumType constraintType)
          {
             IdentityObjectSearchCriteriaType constraint = IdentityObjectSearchCriteriaType.valueOf(constraintType.name());
 
             if (constraint != null)
             {
                return getSessionContext().getIdentityStoreRepository().getSupportedFeatures().
                   isRoleNameSearchCriteriaTypeSupported(constraint);
             }
             else
             {
                return false;
             }
          }
       };
    }
 
    protected IdentityObjectRelationship createIdentityObjectRelationship(Role role)
    {
       return new SimpleIdentityObjectRelationship(
          createIdentityObject(role.getGroup()),
          createIdentityObject(role.getUser()),
          role.getRoleType().getName(),
          ROLE
       );
    }
 
    public RoleManagerFeaturesDescription getSupportedFeatures()
    {
       return featuresDescription;
    }
 
    public RoleType createRoleType(String name) throws IdentityException
    {
       checkNotNullArgument(name, "RoleType name");
       checkObjectName(name);
 
       String roleType = null;
 
       try
       {
          preCreate(new SimpleRoleType(name));
 
          roleType = getRepository().createRelationshipName(getInvocationContext(), name);
 
          postCreate(new SimpleRoleType(name));
       }
       catch (OperationNotSupportedException e)
       {
          throw new IdentityException("Role management not supported");
       }
 
       RoleType result = new SimpleRoleType(roleType);
 
       if (cache != null)
       {
          cache.invalidateAllQueries(cacheNS);
          cache.invalidateAllSearches(cacheNS);
          cache.putRoleType(cacheNS, result);
       }
 
       return result;
 
    }
 
    public void removeRoleType(String name) throws IdentityException
    {
       checkNotNullArgument(name, "RoleType name");
 
       try
       {
          preRemove(new SimpleRoleType(name));
 
          getRepository().removeRelationshipName(getInvocationContext(), name);
 
          if (cache != null)
          {
             cache.invalidateAllQueries(cacheNS);
             cache.invalidateAllSearches(cacheNS);
             cache.removeRoleType(cacheNS, new SimpleRoleType(name));
          }
 
          postRemove(new SimpleRoleType(name));
 
       }
       catch (OperationNotSupportedException e)
       {
          throw new IdentityException("Role management not supported");
       }
    }
 
    public void removeRoleType(RoleType roleType) throws IdentityException
    {
       checkNotNullArgument(roleType, "RoleType");
 
       removeRoleType(roleType.getName());
    }
 
    public RoleType getRoleType(String name) throws IdentityException
    {
 
       checkNotNullArgument(name, "RoleType name");
 
       if (cache != null)
       {
          RoleType roleType = cache.getRoleType(cacheNS, new SimpleRoleType(name));
          if (roleType != null)
          {
             return roleType;
          }
       }
 
       try
       {
          Set<String> names = getRepository().getRelationshipNames(getInvocationContext(), null);
          if (names.contains(name))
          {
             if (cache != null)
             {
                cache.putRoleType(cacheNS,  new SimpleRoleType(name));
             }
             
             return new SimpleRoleType(name);
          }
       }
       catch (OperationNotSupportedException e)
       {
          throw new IdentityException("Role management not supported");
       }
 
       return null;
    }
 
    public Collection<RoleType> findRoleTypes(IdentitySearchCriteria criteria) throws IdentityException
    {
       if (cache != null)
       {
          RoleTypeSearch search = new RoleTypeSearchImpl();
          search.setSearchCriteria(criteria);
 
          Collection<RoleType> result = cache.getRoleTypeSearch(cacheNS, search);
          if (result != null)
          {
             return result;
          }
       }
       
       try
       {
          Set<String> names = getRepository().getRelationshipNames(getInvocationContext(), convertSearchControls(criteria));
          Set<RoleType> types = new HashSet<RoleType>();
 
          for (String name : names)
          {
             types.add(new SimpleRoleType(name));
          }
 
          if (cache != null)
          {
             RoleTypeSearch search = new RoleTypeSearchImpl();
             search.setSearchCriteria(criteria);
 
             cache.putRoleTypeSearch(cacheNS, search, types);
 
          }
 
          return types;
       }
       catch (OperationNotSupportedException e)
       {
          throw new IdentityException("Role management not supported");
       }
 
    }
 
    public Collection<RoleType> findRoleTypes() throws IdentityException
    {
       return findRoleTypes(null);
    }
 
    public Role createRole(RoleType roleType, User user, Group group) throws IdentityException
    {
       checkNotNullArgument(roleType, "RoleType");
       checkNotNullArgument(user, "User");
       checkNotNullArgument(group, "Group");
 
       //TODO: add createRoleType switch to the API
       Role _role = new SimpleRole(roleType, user, group);
 
       preCreate(_role);
 
       IdentityObjectRelationship rel = getRepository().createRelationship(getInvocationContext(), createIdentityObject(group), createIdentityObject(user), ROLE, roleType.getName(), false);
 
 
 
 
       Role role = null;
 
       if (rel != null)
       {
          role = new SimpleRole(roleType, user, group); 
       }
 
       if (cache != null)
       {
          cache.invalidateAllQueries(cacheNS);
          cache.invalidateAllSearches(cacheNS);
          cache.putRole(cacheNS, role);
       }
 
       postCreate(_role);
 
       return role;
 
    }
 
    public Role createRole(String roleTypeName, String userName, String groupId) throws IdentityException
    {
       checkNotNullArgument(roleTypeName, "RoleType name");
       checkNotNullArgument(userName, "User name");
       checkNotNullArgument(groupId, "Group Id");
 
       User user = createUserFromId(userName);
       Group group = createGroupFromId(groupId);
 
       return createRole(new SimpleRoleType(roleTypeName), user, group);
    }
 
    public void removeRole(RoleType roleType, User user, Group group) throws IdentityException
    {
       checkNotNullArgument(roleType, "RoleType");
       checkNotNullArgument(user, "User");
       checkNotNullArgument(group, "Group");
 
       Role _role = new SimpleRole(roleType, user, group);
 
       preRemove(_role);
 
       getRepository().removeRelationship(getInvocationContext(), createIdentityObject(group), createIdentityObject(user), ROLE, roleType.getName());
 
       if (cache != null)
       {
          cache.invalidateAllQueries(cacheNS);
          cache.invalidateAllSearches(cacheNS);
          cache.removeRole(cacheNS, _role);
       }
 
       postRemove(_role);
    }
 
    public void removeRole(String roleTypeName, String userName, String groupId) throws IdentityException
    {
       checkNotNullArgument(roleTypeName, "RoleType name");
       checkNotNullArgument(userName, "User name");
       checkNotNullArgument(groupId, "Group Id");
 
       User user = createUserFromId(userName);
       Group group = createGroupFromId(groupId);
 
       removeRole(new SimpleRoleType(roleTypeName), user, group);
    }
 
    public void removeRole(Role role) throws IdentityException
    {
       checkNotNullArgument(role, "Role");
 
       preRemove(role);
 
       getRepository().removeRelationship(getInvocationContext(), createIdentityObject(role.getGroup()), createIdentityObject(role.getUser()), ROLE, role.getRoleType().getName());
 
       if (cache != null)
       {
          cache.invalidateAllQueries(cacheNS);
          cache.invalidateAllSearches(cacheNS);
          cache.removeRole(cacheNS, role);
       }
 
       postRemove(role);
    }
 
    public boolean hasRole(User user, Group group, RoleType roleType) throws IdentityException
    {
       checkNotNullArgument(roleType, "RoleType");
       checkNotNullArgument(user, "User");
       checkNotNullArgument(group, "Group");
 
       if (cache != null)
       {
          Role role = new SimpleRole(roleType, user, group);
          role = cache.getRole(cacheNS, role);
          if (role != null)
          {
             return true;
          }
       }
 
       //TODO: does separate hasRelationship method in IdentityStore makes sense?
 
       Set<IdentityObjectRelationship> rels = getRepository().resolveRelationships(getInvocationContext(), createIdentityObject(group), createIdentityObject(user), ROLE);
 
       for (IdentityObjectRelationship rel : rels)
       {
          if (rel.getType().getName().equals(ROLE.getName()) && rel.getName() != null && rel.getName().equals(roleType.getName()))
          {
             if (cache != null)
             {
                Role role = new SimpleRole(roleType, user, group);
                cache.putRole(cacheNS, role);
             }
 
             return true;
          }
       }
 
       return false;
    }
 
    public boolean hasRole(String userName, String groupId, String roleTypeName) throws IdentityException
    {
       checkNotNullArgument(roleTypeName, "RoleType name");
       checkNotNullArgument(userName, "User name");
       checkNotNullArgument(groupId, "Group Id");
 
       User user = createUserFromId(userName);
       Group group = createGroupFromId(groupId);
 
       return hasRole(user, group, new SimpleRoleType(roleTypeName));
    }
 
    public Role getRole(RoleType roleType, User user, Group group) throws IdentityException
    {
       checkNotNullArgument(roleType, "RoleType");
       checkNotNullArgument(user, "User");
       checkNotNullArgument(group, "Group");
 
       if (cache != null)
       {
          Role role = new SimpleRole(roleType, user, group);
          role = cache.getRole(cacheNS, role);
          if (role != null)
          {
             return role;
          }
       }
 
       //TODO: does separate hasRelationship method in IdentityStore makes sense?
 
       Set<IdentityObjectRelationship> rels = getRepository().resolveRelationships(getInvocationContext(), createIdentityObject(group), createIdentityObject(user), ROLE);
 
       if (rels.size() == 0)
       {
          return null;
       }
 
       for (IdentityObjectRelationship relationship : rels)
       {
          if (roleType.getName().equals(relationship.getName()))
          {
 
             Role role = new SimpleRole(new SimpleRoleType(relationship.getName()),
                createUser(relationship.getToIdentityObject()),
                createGroup(relationship.getFromIdentityObject()));
 
             if (cache != null)
             {
                cache.putRole(cacheNS, role);
             }
 
             return role;
          }
       }
 
       return null;
 
 
    }
 
    public Role getRole(String roleTypeName, String userId, String groupId) throws IdentityException
    {
       checkNotNullArgument(roleTypeName, "RoleType name");
       checkNotNullArgument(userId, "User id");
       checkNotNullArgument(groupId, "Group Id");
 
       // TODO: Check if relationship is present in the store
 
       User user = createUserFromId(userId);
       Group group = createGroupFromId(groupId);
       RoleType roleType = new SimpleRoleType(roleTypeName);
 
       return getRole(roleType, user, group);
    }
 
    public Collection<RoleType> findRoleTypes(User user, Group group) throws IdentityException
    {
 
       checkNotNullArgument(user, "User");
       checkNotNullArgument(group, "Group");
 
       return findRoleTypes(user, group, null);
    }
 
    public Collection<RoleType> findRoleTypes(User user, Group group, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(user, "User");
       checkNotNullArgument(group, "Group");
 
       if (cache != null)
       {
          RoleTypeSearchImpl search = new RoleTypeSearchImpl();
          search.setUser(user);
          search.setGroup(group);
          search.setSearchCriteria(criteria);
 
          Collection<RoleType> results = cache.getRoleTypeSearch(cacheNS, search);
          if (results != null)
          {
             return results;
          }
       }
 
       Set<IdentityObjectRelationship> rels = getRepository().resolveRelationships(getInvocationContext(), createIdentityObject(group), createIdentityObject(user), ROLE);
       Set<RoleType> types = new HashSet<RoleType>();
 
       for (IdentityObjectRelationship rel : rels)
       {
          types.add(new SimpleRoleType(rel.getName()));
       }
 
       if (cache != null)
       {
          RoleTypeSearchImpl search = new RoleTypeSearchImpl();
          search.setUser(user);
          search.setGroup(group);
          search.setSearchCriteria(criteria);
 
          cache.putRoleTypeSearch(cacheNS, search, types);
 
       }
 
       return types;
 
 
    }
 
    public Collection<RoleType> findRoleTypes(String userName, String groupId, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(userName, "User name");
       checkNotNullArgument(groupId, "Group Id");
 
       User user = createUserFromId(userName);
       Group group = createGroupFromId(groupId);
 
       return findRoleTypes(user, group, criteria);
    }
 
    public Collection<RoleType> findUserRoleTypes(User user) throws IdentityException
    {
       checkNotNullArgument(user, "User");
 
       return findUserRoleTypes(user, null);
    }
 
    public Collection<RoleType> findUserRoleTypes(User user, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(user,  "User");
 
       Set<RoleType> types = new HashSet<RoleType>();
 
       if (cache != null)
       {
          RoleTypeSearchImpl search = new RoleTypeSearchImpl();
          search.setUser(user);
          search.setSearchCriteria(criteria);
 
          Collection<RoleType> results = cache.getRoleTypeSearch(cacheNS, search);
          if (results != null)
          {
             return results;
          }
       }
 
       try
       {
          Collection<String> names = getRepository().getRelationshipNames(getInvocationContext(), createIdentityObject(user), convertSearchControls(criteria));
 
          for (String name : names)
          {
             types.add(new SimpleRoleType(name));
          }
 
          if (cache != null)
          {
             RoleTypeSearchImpl search = new RoleTypeSearchImpl();
             search.setUser(user);
             search.setSearchCriteria(criteria);
 
             cache.putRoleTypeSearch(cacheNS, search, types);
          }
 
          return types;
 
       }
       catch (OperationNotSupportedException e)
       {
          throw new IdentityException("Role management not supported", e);
       }
 
    }
 
    public Collection<RoleType> findUserRoleTypes(String userName, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(userName, "User name");
 
       User user = createUserFromId(userName);
 
       return findUserRoleTypes(user, criteria);
    }
 
    public Collection<RoleType> findGroupRoleTypes(Group group) throws IdentityException
    {
       checkNotNullArgument(group, "Group");
       return findGroupRoleTypes(group, null);
    }
 
    public Collection<RoleType> findGroupRoleTypes(String groupId, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(groupId, "Group Id");
 
       Group group = createGroupFromId(groupId);
 
       return findGroupRoleTypes(group, criteria);
    }
 
    public Collection<RoleType> findGroupRoleTypes(Group group, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(group, "Group");
 
       Set<RoleType> types = new HashSet<RoleType>();
 
       if (cache != null)
       {
          RoleTypeSearchImpl search = new RoleTypeSearchImpl();
          search.setGroup(group);
          search.setSearchCriteria(criteria);
 
          Collection<RoleType> results = cache.getRoleTypeSearch(cacheNS, search);
          if (results != null)
          {
             return results;
          }
       }
 
       try
       {
          Collection<String> names = getRepository().getRelationshipNames(getInvocationContext(), createIdentityObject(group), convertSearchControls(criteria));
 
          for (String name : names)
          {
             types.add(new SimpleRoleType(name));
          }
 
          if (cache != null)
          {
             RoleTypeSearchImpl search = new RoleTypeSearchImpl();
             search.setGroup(group);
             search.setSearchCriteria(criteria);
 
             cache.putRoleTypeSearch(cacheNS, search, types);
 
          }
 
          return types;
 
       }
       catch (OperationNotSupportedException e)
       {
          throw new IdentityException("Role management not supported");
       }
 
    }
 
    public Collection<User> findUsersWithRelatedRole(Group group, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(group, "Group");
 
       List<User> identities = new LinkedList<User>();
 
       if (cache != null)
       {
          UserSearchImpl search = new UserSearchImpl();
          search.addRelatedGroupId(group.getKey());
          search.setSearchCriteria(criteria);
 
          Collection<User> results = cache.getUserSearch(cacheNS, search);
          if (results != null)
          {
             return results;
          }
       }
 
       Collection<IdentityObject> ios = null;
 
       ios = getRepository().findIdentityObject(getInvocationContext(), createIdentityObject(group), ROLE, true, convertSearchControls(criteria));
 
       for (IdentityObject io : ios)
       {
          identities.add(createUser(io));
       }
 
       if (cache != null)
       {
          UserSearchImpl search = new UserSearchImpl();
          search.addRelatedGroupId(group.getKey());
          search.setSearchCriteria(criteria);
 
          cache.putUserSearch(cacheNS, search, identities);
 
       }
 
       return identities;
    }
 
    public Collection<User> findUsersWithRelatedRole(String groupId, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(groupId, "Group id");
 
       Group group = createGroupFromId(groupId);
 
       return findUsersWithRelatedRole(group, criteria);
    }
 
    public Collection<Group> findGroupsWithRelatedRole(User user, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(user, "User");
 
       List<Group> identities = new LinkedList<Group>();
 
       if (cache != null)
       {
          GroupSearchImpl search = new GroupSearchImpl();
          search.addRelatedUserId(user.getKey());
          search.setSearchCriteria(criteria);
 
          Collection<Group> results = cache.getGroupSearch(cacheNS, search);
          if (results != null)
          {
             return results;
          }
       }
 
       Collection<IdentityObject> ios = null;
 
       ios = getRepository().findIdentityObject(getInvocationContext(), createIdentityObject(user), ROLE, false, convertSearchControls(criteria));
 
       for (IdentityObject io : ios)
       {
          identities.add(createGroup(io));
       }
 
       if (cache != null)
       {
          GroupSearchImpl search = new GroupSearchImpl();
          search.addRelatedUserId(user.getKey());
          search.setSearchCriteria(criteria);
 
          cache.putGroupSearch(cacheNS, search, identities);
 
       }
 
       return identities;
    }
 
    public Collection<Group> findGroupsWithRelatedRole(String userName, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(userName, "User name");
 
       User user = createUserFromId(userName);
 
       return findGroupsWithRelatedRole(user, criteria);
    }
 
    public Collection<Group> findGroupsWithRelatedRole(User user, String groupType, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(user, "User");
 //      checkNotNullArgument(groupType, "Group type");
 
       List<Group> identities = new LinkedList<Group>();
 
       if (cache != null)
       {
          GroupSearchImpl search = new GroupSearchImpl();
          search.addRelatedUserId(user.getKey());
          search.setGroupType(groupType);
          search.setSearchCriteria(criteria);
 
          Collection<Group> results = cache.getGroupSearch(cacheNS, search);
          if (results != null)
          {
             return results;
          }
       }
 
       Collection<IdentityObject> ios = null;
 
       ios = getRepository().findIdentityObject(getInvocationContext(), createIdentityObject(user), ROLE, false, convertSearchControls(criteria));
 
       if (groupType != null)
       {
          IdentityObjectType iot = getIdentityObjectType(groupType);
 
          for (IdentityObject io : ios)
          {
             if (io.getIdentityType().getName().equals(iot.getName()))
             {
                identities.add(createGroup(io));
             }
          }
       }
       else
       {
          for (IdentityObject io : ios)
          {
             identities.add(createGroup(io));
          }
       }
 
       if (cache != null)
       {
          GroupSearchImpl search = new GroupSearchImpl();
          search.addRelatedUserId(user.getKey());
          search.setGroupType(groupType);
          search.setSearchCriteria(criteria);
 
          cache.putGroupSearch(cacheNS, search, identities);
 
       }
 
 
       return identities;
    }
 
    public Collection<Group> findGroupsWithRelatedRole(String userName, String groupType, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(userName, "User name");
 //      checkNotNullArgument(groupType, "Group type");
 
 
       User user = createUserFromId(userName);
 
       return findGroupsWithRelatedRole(user, groupType, criteria);
    }
 
    public int getRolesCount(IdentityType identityType, RoleType roleType, IdentitySearchCriteria criteria) throws IdentityException
    {
       //TODO: cache support
 
       checkNotNullArgument(identityType, "IdentityType");
 
 
 //      if (cache != null)
 //      {
 //         RoleSearchImpl search = new RoleSearchImpl();
 //         search.setIdentityTypeId(identityType.getKey());
 //         search.setRoleType(roleType);
 //
 //         Collection<Role> results = cache.getRoleSearch(cacheNS, search);
 //         if (results != null)
 //         {
 //            return results;
 //         }
 //      }
 
       int relationshipsCount = 0;
 
       // If Identity then search for parent relationships
       if (identityType instanceof User)
       {
          relationshipsCount = getRepository().
             getRelationshipsCount(
                getInvocationContext(),
                createIdentityObject(identityType),
                ROLE,
                false,
                true,
                null,
                convertSearchControls(criteria));
       }
       // If Group then search for child relationships
       else
       {
          relationshipsCount = getRepository().
             getRelationshipsCount(
                getInvocationContext(),
                createIdentityObject(identityType),
                ROLE,
                true,
                true,
                null,
                convertSearchControls(criteria));
       }
 
 
 //      if (cache != null)
 //      {
 //         RoleSearchImpl search = new RoleSearchImpl();
 //         search.setIdentityTypeId(identityType.getKey());
 //         search.setRoleType(roleType);
 //
 //          cache.putRoleSearch(cacheNS, search, roles);
 //
 //      }
 
       return relationshipsCount;
 
    }
 
    public Collection<Role> findRoles(IdentityType identityType, RoleType roleType, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(identityType, "IdentityType");
       //checkNotNullArgument(roleType, "RoleType");
 
       Set<Role> roles = new HashSet<Role>();
 
       if (cache != null)
       {
          RoleSearchImpl search = new RoleSearchImpl();
          search.setIdentityTypeId(identityType.getKey());
          search.setRoleType(roleType);
 
          Collection<Role> results = cache.getRoleSearch(cacheNS, search);
          if (results != null)
          {
             return results;
          }
       }
 
       Set<IdentityObjectRelationship> relationships = null;
 
       // If Identity then search for parent relationships
       if (identityType instanceof User)
       {
          relationships = getRepository().
             resolveRelationships(
                getInvocationContext(),
                createIdentityObject(identityType),
                ROLE,
                false,
                true,
                null,
                convertSearchControls(criteria));
       }
       // If Group then search for child relationships
       else
       {
          relationships = getRepository().
             resolveRelationships(
                getInvocationContext(),
                createIdentityObject(identityType),
                ROLE,
                true,
                true,
                null,
                convertSearchControls(criteria));
       }
 
       for (IdentityObjectRelationship relationship : relationships)
       {
          if (roleType != null)
          {
             if (roleType.getName().equals(relationship.getName()))
             {
                roles.add(new SimpleRole(new SimpleRoleType(relationship.getName()), createUser(relationship.getToIdentityObject()), createGroup(relationship.getFromIdentityObject())));
             }
          }
          else
          {
             roles.add(new SimpleRole(new SimpleRoleType(relationship.getName()), createUser(relationship.getToIdentityObject()), createGroup(relationship.getFromIdentityObject())));
          }
       }
 
       if (cache != null)
       {
          RoleSearchImpl search = new RoleSearchImpl();
          search.setIdentityTypeId(identityType.getKey());
          search.setRoleType(roleType);
 
           cache.putRoleSearch(cacheNS, search, roles);
          
       }
 
       return roles;
 
    }
 
    public Collection<Role> findRoles(IdentityType identityType, RoleType roleType) throws IdentityException
    {
       return findRoles(identityType, roleType, null);
    }
 
    public Collection<Role> findRoles(String id, String roleTypeName, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(id, "Group id or User name");
 
       RoleType roleType = roleTypeName != null ? new SimpleRoleType(roleTypeName) : null;
       return findRoles(createIdentityTypeFromId(id), roleType, criteria);
    }
 
    public <T extends IdentityType> Collection<Role> findRoles(String key, String roleTypeName) throws IdentityException
    {
       return findRoles(key, roleTypeName, null);
    }
 
    public Map<String, String> getProperties(RoleType roleType)  throws IdentityException
    {
       checkNotNullArgument(roleType, "RoleType name");
 
       if (cache != null)
       {
          Map<String, String> result = cache.getProperties(cacheNS, roleType);
          if (result != null)
          {
             return result;
          }
       }
       
       Map<String, String> result = getRepository().getRelationshipNameProperties(getInvocationContext(), roleType.getName());
 
       if (cache != null)
       {
          cache.putProperties(cacheNS, roleType, result);
       }
 
       return result;
    }
 
    public Map<String, String> getProperties(String roleTypeName)  throws IdentityException
    {
       checkNotNullArgument(roleTypeName, "RoleType name");
 
       return getProperties(new SimpleRoleType(roleTypeName));
    }
 
    public Map<String, String> getProperties(Role role) throws IdentityException
    {
       checkNotNullArgument(role, "Role");
 
       if (cache != null)
       {
          Map<String, String> result = cache.getProperties(cacheNS, role);
          if (result != null)
          {
             return result;
          }
       }
 
       Map<String, String> result = getRepository().getRelationshipProperties(getInvocationContext(), createIdentityObjectRelationship(role));
 
       if (cache != null)
       {
          cache.putProperties(cacheNS, role, result);
       }
 
       return result;
    }
 
    public void setProperty(Role role, String name, String value) throws IdentityException
    {
       checkNotNullArgument(role, "Role");
       checkNotNullArgument(name, "Property name");
       checkNotNullArgument(value, "Property value");
 
       Map<String, String> props = new HashMap<String, String>();
       props.put(name, value);
 
       prePropertiesSet(role, props);
 
       getRepository().setRelationshipProperties(getInvocationContext(), createIdentityObjectRelationship(role), props);
 
       if (cache != null)
       {
          cache.invalidateRoleProperties(cacheNS, role);
          cache.putProperties(cacheNS, role, getProperties(role));
       }
 
       postPropertiesSet(role, props);
    }
 
    public void setProperty(RoleType roleType, String name, String value) throws IdentityException
    {
       checkNotNullArgument(roleType, "RoleType");
       checkNotNullArgument(name, "Property name");
       checkNotNullArgument(value, "Property value");
 
       Map<String, String> props = new HashMap<String, String>();
       props.put(name, value);
 
       prePropertiesSet(roleType, props);
 
       getRepository().setRelationshipNameProperties(getInvocationContext(), roleType.getName(), props);
 
       if (cache != null)
       {
          cache.invalidateRoleTypeProperties(cacheNS, roleType);
          cache.putProperties(cacheNS, roleType, getProperties(roleType));
       }
 
 
       postPropertiesSet(roleType, props);
    }
 
    public void setProperty(String roleTypeName, String name, String value) throws IdentityException
    {
       checkNotNullArgument(roleTypeName, "RoleType name");
       checkNotNullArgument(name, "Property name");
       checkNotNullArgument(value, "Property value");
 
       Map<String, String> props = new HashMap<String, String>();
       props.put(name, value);
 
       prePropertiesSet(new SimpleRoleType(roleTypeName), props);
 
       setProperty(new SimpleRoleType(roleTypeName), name, value);
 
       if (cache != null)
       {
          cache.invalidateRoleTypeProperties(cacheNS, new SimpleRoleType(roleTypeName));
          cache.putProperties(cacheNS, new SimpleRoleType(roleTypeName), getProperties(new SimpleRoleType(roleTypeName)));
       }
 
 
       postPropertiesSet(new SimpleRoleType(roleTypeName), props);
    }
 
    public void setProperties(Role role, Map<String, String> properties) throws IdentityException
    {
       checkNotNullArgument(role, "Role");
       checkNotNullArgument(properties, "Properties");
 
       prePropertiesSet(role, properties);
 
       getRepository().setRelationshipProperties(getInvocationContext(), createIdentityObjectRelationship(role), properties);
 
       if (cache != null)
       {
          cache.invalidateRoleProperties(cacheNS, role);
          cache.putProperties(cacheNS, role, getProperties(role));
       }
 
 
       postPropertiesSet(role, properties);
    }
 
    public void setProperties(RoleType roleType, Map<String, String> properties) throws IdentityException
    {
       checkNotNullArgument(roleType, "RoleType");
       checkNotNullArgument(properties, "Properties");
 
       prePropertiesSet(roleType, properties);
 
       getRepository().setRelationshipNameProperties(getInvocationContext(), roleType.getName(), properties);
 
       if (cache != null)
       {
          cache.invalidateRoleTypeProperties(cacheNS, roleType);
          cache.putProperties(cacheNS, roleType, getProperties(roleType));
       }
 
       postPropertiesSet(roleType, properties);
    }
 
    public void setProperties(String roleTypeName, Map<String, String> properties) throws IdentityException
    {
       checkNotNullArgument(roleTypeName, "RoleType name");
 
       prePropertiesSet(new SimpleRoleType(roleTypeName), properties);
 
       setProperties(new SimpleRoleType(roleTypeName), properties);
 
       if (cache != null)
       {
          cache.invalidateRoleTypeProperties(cacheNS, new SimpleRoleType(roleTypeName));
          cache.putProperties(cacheNS, new SimpleRoleType(roleTypeName), getProperties(new SimpleRoleType(roleTypeName)));
       }
 
       postPropertiesSet(new SimpleRoleType(roleTypeName), properties);
 
    }
 
    public void removeProperty(Role role, String name) throws IdentityException
    {
       checkNotNullArgument(role, "Role");
       checkNotNullArgument(name, "Property name");
 
       Set<String> names = new HashSet<String>();
       names.add(name);
 
       prePropertiesRemove(role, names);
 
       getRepository().removeRelationshipProperties(getInvocationContext(), createIdentityObjectRelationship(role), names);
 
       if (cache != null)
       {
          cache.invalidateRoleProperties(cacheNS, role);
          cache.putProperties(cacheNS, role, getProperties(role));
       }
 
 
       postPropertiesRemove(role, names);
    }
 
    public void removeProperty(RoleType roleType, String name) throws IdentityException
    {
       checkNotNullArgument(roleType, "RoleType");
       checkNotNullArgument(name, "Property name");
 
       Set<String> names = new HashSet<String>();
       names.add(name);
 
       prePropertiesRemove(roleType, names);
 
       getRepository().removeRelationshipNameProperties(getInvocationContext(), roleType.getName(), names);
 
       if (cache != null)
       {
          cache.invalidateRoleTypeProperties(cacheNS, roleType);
          cache.putProperties(cacheNS, roleType, getProperties(roleType));
       }
 
       postPropertiesRemove(roleType, names);
    }
 
    public void removeProperty(String roleTypeName, String name) throws IdentityException
    {
       checkNotNullArgument(roleTypeName, "RoleType name");
 
       Set<String> names = new HashSet<String>();
       names.add(name);
 
       prePropertiesRemove(new SimpleRoleType(roleTypeName), names);
 
       removeProperty(new SimpleRoleType(roleTypeName), name);
 
       if (cache != null)
       {
          cache.invalidateRoleTypeProperties(cacheNS, new SimpleRoleType(roleTypeName));
          cache.putProperties(cacheNS, new SimpleRoleType(roleTypeName), getProperties(new SimpleRoleType(roleTypeName)));
       }
 
       postPropertiesRemove(new SimpleRoleType(roleTypeName), names);
 
    }
 }
