 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.podd.restlet;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.restlet.security.Role;
 
 /**
  * Provides constants to use in the authentication interface, including whether authentication is
  * required for a particular action and what the error message should be if a request fails.
  * 
  * FIXME: Roles are currently hardcoded into each action.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public enum PoddAction
 {
     /**
      * An action by a user asking to create a new artifact.
      * 
      * By default both project and repository administrator users are allowed to create artifacts.
      */
     ARTIFACT_CREATE(true, "Could not create artifact.", new HashSet<Role>(Arrays.asList(
            PoddRoles.ADMIN.getRole(), PoddRoles.PROJECT_CREATOR.getRole())), false),
     
     /**
      * An action by a user asking to update an existing artifact.
      * 
      * By default administrator, project-admin and project-member users are allowed to update
      * artifacts.
      */
     ARTIFACT_EDIT(true, "Could not edit artifact.", new HashSet<Role>(Arrays.asList(PoddRoles.ADMIN.getRole(),
             PoddRoles.PROJECT_MEMBER.getRole(), PoddRoles.PROJECT_ADMIN.getRole())), true),
     
     /**
      * An action by a user asking to delete an unpublished artifact.
      * 
      * By default only administrators and project-admin users are allowed to delete artifacts.
      */
     UNPUBLISHED_ARTIFACT_DELETE(true, "Could not delete artifact", new HashSet<Role>(Arrays.asList(
             PoddRoles.PROJECT_ADMIN.getRole(), PoddRoles.ADMIN.getRole())), true),
     
     /**
      * An action by a user asking to read an unpublished artifact.
      * 
      * By default only project-member, project-admin, and administrator users are allowed to read
      * unpublished artifacts.
      */
     UNPUBLISHED_ARTIFACT_READ(true, "Failed to read unpublished artifact", new HashSet<Role>(Arrays.asList(
             PoddRoles.PROJECT_OBSERVER.getRole(), PoddRoles.PROJECT_ADMIN.getRole(),
             PoddRoles.PROJECT_MEMBER.getRole(), PoddRoles.ADMIN.getRole())), true),
     
     UNPUBLISHED_ARTIFACT_LIST(true, "Failed to list unpublished artifacts", new HashSet<Role>(Arrays.asList(
             PoddRoles.PROJECT_OBSERVER.getRole(), PoddRoles.PROJECT_ADMIN.getRole(),
             PoddRoles.PROJECT_MEMBER.getRole(), PoddRoles.ADMIN.getRole())), false),
     
     /**
      * An action by a user asking to read a published artifact.
      * 
      * By default all unauthenticated users are allowed to read published artifacts.
      */
     PUBLISHED_ARTIFACT_READ(false, "Failed to read published artifact", Collections.<Role> emptySet(), true),
     
     /**
      * An action by a user asking to publish an artifact.
      * 
      * By default only the administrators and project-admin users are allowed to publish projects.
      */
     ARTIFACT_PUBLISH(true, "Could not publish artifact", new HashSet<Role>(Arrays.asList(
             PoddRoles.PROJECT_ADMIN.getRole(), PoddRoles.ADMIN.getRole())), true),
     
     /**
      * An action by an administrator asking to create a new user, or update an existing user.
      * 
      * By default only the administrators are allowed to create new users.
      */
     USER_CREATE(true, "Could not create/update user.", Collections.singleton(PoddRoles.ADMIN.getRole()), false),
     
     /**
      * An action by an administrator asking to delete an existing user.
      * 
      * By default only administrators are allowed to delete existing users.
      */
     USER_DELETE(true, "Could not delete user", Collections.singleton(PoddRoles.ADMIN.getRole()), true),
     
     /**
      * An action by a user asking to fetch their details
      * 
      * By default all authenticated users can request their user details.
      */
     CURRENT_USER_READ(true, "Could not retrieve current user details", Collections.<Role> emptySet(), false),
     
     /**
      * An action by a user asking to fetch information about another user.
      * 
      * By default if they are not administrators, they will not be able to see information about
      * other users.
      */
     OTHER_USER_READ(true, "Could not retrieve other user details", Collections.singleton(PoddRoles.ADMIN.getRole()),
             false),
     
     /**
      * An action by a user asking to edit their details
      * 
      * By default all authenticated users can edit their user details.
      */
     CURRENT_USER_EDIT(true, "Could not edit current user details", Collections.<Role> emptySet(), false),
     
     /**
      * An action by a user asking to edit information about another user.
      * 
      * By default if they are not administrators, they will not be able to edit information about
      * other users.
      */
     OTHER_USER_EDIT(true, "Could not edit other user details", Collections.singleton(PoddRoles.ADMIN.getRole()), false),
             
     /**
      * An action by an administrator asking to edit repository roles for a user.
      * 
      * By default only administrators are allowed to edit repository roles.
      */
     REPOSITORY_ROLE_EDIT(true, "Could not assign role", Collections.singleton(PoddRoles.ADMIN.getRole()), false),
     
     /**
      * An action by a project administrator to assign a role on a project.
      * 
      * By default only the administrators and project-admin users are allowed to edit project roles.
      */
     PROJECT_ROLE_EDIT(true, "Could not assign role", new HashSet<Role>(Arrays.asList(PoddRoles.PROJECT_ADMIN.getRole(),
             PoddRoles.ADMIN.getRole())), true),
     
     /**
      * An action by a user asking to fetch information about a data repository.
      * 
      * By default if they are not administrators, they will not be able to see information about
      * other data repositories.
      */
     DATA_REPOSITORY_READ(true, "Could not retrieve data repository details", Collections.singleton(PoddRoles.ADMIN
             .getRole()), false),
     
     ;
     
     private final boolean authRequired;
     private final String errorMessage;
     private final Set<Role> roles;
     private final boolean requiresObjectUris;
     
     PoddAction(final boolean authenticationRequired, final String errorMessage, final Set<Role> roles,
             final boolean requiresObjectUris)
     {
         this.authRequired = authenticationRequired;
         this.errorMessage = errorMessage;
         this.roles = roles;
         this.requiresObjectUris = requiresObjectUris;
     }
     
     public String getErrorMessage()
     {
         return this.errorMessage;
     }
     
     /**
      * @return the authRequired
      */
     public boolean isAuthRequired()
     {
         return this.authRequired;
     }
     
     public boolean isRoleRequired()
     {
         return !this.roles.isEmpty();
     }
     
     /**
      * Returns true if isRoleRequired() returns true and any of the roles in authenticatedRoles are
      * in the set of roles for this action.
      * 
      * @param authenticatedRoles
      *            The set of roles that the user currently has.
      * @return True if this action requires a role and the collection of authenticated roles matches
      *         one of the roles for this action.
      */
     public boolean matchesForRoles(final Collection<Role> authenticatedRoles)
     {
         if(!this.isRoleRequired())
         {
             return true;
         }
         
         for(final Role nextAuthenticatedRole : authenticatedRoles)
         {
             if(this.roles.contains(nextAuthenticatedRole))
             {
                 return true;
             }
         }
         
         return false;
     }
     
     public boolean requiresObjectUris(final List<Role> roles)
     {
         if(roles != null && roles.contains(PoddRoles.ADMIN.getRole()))
         {
             // if client has ADMIN role, no need to match object URIs
             return false;
         }
         return this.requiresObjectUris;
     }
 }
