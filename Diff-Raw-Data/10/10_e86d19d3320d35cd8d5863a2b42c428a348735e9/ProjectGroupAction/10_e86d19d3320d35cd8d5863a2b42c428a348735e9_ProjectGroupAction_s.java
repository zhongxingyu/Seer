 package org.apache.maven.continuum.web.action;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import org.apache.maven.continuum.ContinuumException;
 import org.apache.maven.continuum.model.project.BuildDefinition;
 import org.apache.maven.continuum.model.project.Project;
 import org.apache.maven.continuum.model.project.ProjectDependency;
 import org.apache.maven.continuum.model.project.ProjectGroup;
 import org.apache.maven.continuum.project.ContinuumProjectState;
 import org.apache.maven.continuum.web.bean.ProjectGroupUserBean;
 import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
 import org.codehaus.plexus.redback.rbac.RBACManager;
 import org.codehaus.plexus.redback.rbac.RbacManagerException;
 import org.codehaus.plexus.redback.rbac.RbacObjectNotFoundException;
 import org.codehaus.plexus.redback.rbac.Role;
 import org.codehaus.plexus.redback.role.RoleManager;
 import org.codehaus.plexus.redback.role.RoleManagerException;
 import org.codehaus.plexus.redback.users.User;
 import org.codehaus.plexus.redback.users.UserManager;
 import org.codehaus.plexus.util.StringUtils;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * ProjectGroupAction:
  *
  * @author Jesse McConnell <jmcconnell@apache.org>
  * @version $Id$
  * @plexus.component role="com.opensymphony.xwork.Action" role-hint="projectGroup"
  */
 public class ProjectGroupAction
     extends ContinuumConfirmAction
 {
     private final static Map FILTER_CRITERIA = new HashMap();
 
     static
     {
         FILTER_CRITERIA.put( "username", "Username contains" );
         FILTER_CRITERIA.put( "fullName", "Name contains" );
         FILTER_CRITERIA.put( "email", "Email contains" );
     }
 
     /**
      * @plexus.requirement role-hint="configurable"
      */
     private UserManager manager;
 
     /**
      * @plexus.requirement role-hint="cached"
      */
     private RBACManager rbac;
 
     /**
      * @plexus.requirement role-hint="default"
      */
     private RoleManager roleManager;
 
     private int projectGroupId;
 
     private ProjectGroup projectGroup;
 
     private String name;
 
     private String description;
 
     private Map projects = new HashMap();
 
     private Map projectGroups = new HashMap();
 
     private boolean confirmed;
 
     private boolean projectInCOQueue = false;
 
     private Collection projectList;
 
     private List projectGroupUsers;
 
     private String filterProperty;
 
     private String filterKey;
 
     private boolean ascending = true;
 
     private Collection groupProjects;
 
     private int releaseProjectId;
 
     private Map<String, Integer> buildDefinitions;
 
     private int buildDefinitionId;
 
     public String summary()
         throws ContinuumException
     {
         try
         {
             checkViewProjectGroupAuthorization( getProjectGroupName() );
         }
         catch ( AuthorizationRequiredException authzE )
         {
             addActionError( authzE.getMessage() );
             return REQUIRES_AUTHORIZATION;
         }
 
         projectGroup = getProjectGroup( projectGroupId );
 
         List<BuildDefinition> projectGroupBuildDefs =
             getContinuum().getBuildDefinitionsForProjectGroup( projectGroupId );
 
         if ( projectGroupBuildDefs != null )
         {
             this.buildDefinitions = new LinkedHashMap<String, Integer>( projectGroupBuildDefs.size() );
             for ( BuildDefinition buildDefinition : projectGroupBuildDefs )
             {
 
                 if ( !buildDefinition.isDefaultForProject() )
                 {
                     String key = StringUtils.isEmpty( buildDefinition.getDescription() ) ? buildDefinition.getGoals()
                         : buildDefinition
                             .getDescription();
                     buildDefinitions.put( key, Integer.valueOf( buildDefinition.getId() ) );
                 }
             }
         }
         else
         {
             this.buildDefinitions = Collections.EMPTY_MAP;
         }
 
         return SUCCESS;
     }
 
     public String members()
         throws ContinuumException
     {
         try
         {
             checkViewProjectGroupAuthorization( getProjectGroupName() );
         }
         catch ( AuthorizationRequiredException authzE )
         {
             addActionError( authzE.getMessage() );
             return REQUIRES_AUTHORIZATION;
         }
 
         projectGroup = getContinuum().getProjectGroupWithProjects( projectGroupId );
 
         groupProjects = projectGroup.getProjects();
 
         populateProjectGroupUsers( projectGroup );
 
         return SUCCESS;
     }
 
     public Collection getGroupProjects()
         throws ContinuumException
     {
         return groupProjects;
     }
 
     public String buildDefinitions()
         throws ContinuumException
     {
         return summary();
     }
 
     public String notifiers()
         throws ContinuumException
     {
         return summary();
     }
 
     public String remove()
         throws ContinuumException
     {
         try
         {
             checkRemoveProjectGroupAuthorization( getProjectGroupName() );
         }
         catch ( AuthorizationRequiredException authzE )
         {
             addActionError( authzE.getMessage() );
             return REQUIRES_AUTHORIZATION;
         }
 
         if ( confirmed )
         {
             getContinuum().removeProjectGroup( projectGroupId );
         }
         else
         {
             name = getProjectGroupName();
             return CONFIRM;
         }
 
         return SUCCESS;
     }
 
     public String edit()
         throws ContinuumException
     {
         try
         {
             checkModifyProjectGroupAuthorization( getProjectGroupName() );
         }
         catch ( AuthorizationRequiredException authzE )
         {
             addActionError( authzE.getMessage() );
             return REQUIRES_AUTHORIZATION;
         }
 
         projectGroup = getContinuum().getProjectGroupWithProjects( projectGroupId );
 
         name = projectGroup.getName();
 
         description = projectGroup.getDescription();
 
         projectList = projectGroup.getProjects();
 
         if ( projectList != null )
         {
             Iterator proj = projectList.iterator();
 
             while ( proj.hasNext() )
             {
                 Project p = (Project) proj.next();
                 if ( getContinuum().isInCheckoutQueue( p.getId() ) )
                 {
                     projectInCOQueue = true;
                 }
                 projects.put( p, new Integer( p.getProjectGroup().getId() ) );
             }
         }
 
         Iterator proj_group = getContinuum().getAllProjectGroupsWithProjects().iterator();
         while ( proj_group.hasNext() )
         {
             ProjectGroup pg = (ProjectGroup) proj_group.next();
             projectGroups.put( new Integer( pg.getId() ), pg.getName() );
         }
 
         return SUCCESS;
     }
 
     public String save()
         throws ContinuumException
     {
         try
         {
             checkModifyProjectGroupAuthorization( getProjectGroupName() );
         }
         catch ( AuthorizationRequiredException authzE )
         {
             addActionError( authzE.getMessage() );
             return REQUIRES_AUTHORIZATION;
         }
 
         if ( name != null && name.equals( "" ) )
         {
             addActionError( "projectGroup.error.name.required" );
             return INPUT;
         }
         else if ( name != null && name.trim().equals( "" ) )
         {
             addActionError( "projectGroup.error.name.cannot.be.spaces" );
             return INPUT;
         }
 
         projectGroup = getContinuum().getProjectGroupWithProjects( projectGroupId );
 
         // need to administer roles since they are based off of this
         // todo convert everything like to work off of string keys
         if ( !name.equals( projectGroup.getName() ) )
         {
             //CONTINUUM-1502
             name = name.trim();
             try
             {
                 roleManager.updateRole( "project-administrator", projectGroup.getName(), name );
                 roleManager.updateRole( "project-developer", projectGroup.getName(), name );
                 roleManager.updateRole( "project-user", projectGroup.getName(), name );
 
                 projectGroup.setName( name );
             }
             catch ( RoleManagerException e )
             {
                 throw new ContinuumException( "unable to rename the project group", e );
             }
 
         }
 
         projectGroup.setDescription( description );
 
         getContinuum().updateProjectGroup( projectGroup );
 
         Iterator keys = projects.keySet().iterator();
         while ( keys.hasNext() )
         {
             String key = (String) keys.next();
 
             String[] id = (String[]) projects.get( key );
 
             int projectId = Integer.parseInt( key );
 
             Project project = null;
             Iterator i = projectGroup.getProjects().iterator();
             while ( i.hasNext() )
             {
                 project = (Project) i.next();
                 if ( projectId == project.getId() )
                 {
                     break;
                 }
             }
 
             ProjectGroup newProjectGroup =
                 getContinuum().getProjectGroupWithProjects( new Integer( id[0] ).intValue() );
 
             if ( newProjectGroup.getId() != projectGroup.getId() )
             {
                 getLogger().info(
                     "Moving project " + project.getName() + " to project group " + newProjectGroup.getName() );
                 project.setProjectGroup( newProjectGroup );
                 getContinuum().updateProject( project );
             }
         }
 
         return SUCCESS;
     }
 
     public String build()
         throws ContinuumException
     {
         try
         {
             checkBuildProjectGroupAuthorization( getProjectGroupName() );
         }
         catch ( AuthorizationRequiredException authzE )
         {
             addActionError( authzE.getMessage() );
             return REQUIRES_AUTHORIZATION;
         }
 
         if ( this.getBuildDefinitionId() == -1 )
         {
             getContinuum().buildProjectGroup( projectGroupId );
         }
         else
         {
             getContinuum().buildProjectGroupWithBuildDefinition( projectGroupId, buildDefinitionId );
         }
         return SUCCESS;
     }
 
     public String release()
         throws ContinuumException
     {
         try
         {
             checkBuildProjectGroupAuthorization( getProjectGroupName() );
         }
         catch ( AuthorizationRequiredException authzE )
         {
             addActionError( authzE.getMessage() );
             return REQUIRES_AUTHORIZATION;
         }
 
         //get the parent of the group by finding the parent project
         //i.e., the project that doesn't have a parent, or it's parent is not in the group.
 
         Project parent = null;
 
         boolean allBuildsOk = true;
 
         projectList = getContinuum().getProjectsInGroupWithDependencies( projectGroupId );
 
         if ( projectList != null )
         {
             Iterator proj = projectList.iterator();
 
             while ( proj.hasNext() )
             {
                 Project p = (Project) proj.next();
 
                 if ( p.getState() != ContinuumProjectState.OK )
                 {
                     allBuildsOk = false;
                 }
 
                 if ( ( p.getParent() == null ) || ( !isParentInProjectGroup( p.getParent(), projectList ) ) )
                 {
                     if ( parent == null )
                     {
                         parent = p;
                     }
                     else
                     {
                         //currently, we have no provisions for releasing 2 or more parents
                         //at the same time, this will be implemented in the future
                         addActionError( "projectGroup.release.error.severalParentProjects" );
                         return INPUT;
                     }
                 }
             }
         }
 
         if ( parent == null )
         {
             addActionError( "projectGroup.release.error.emptyGroup" );
             return INPUT;
         }
 
         releaseProjectId = parent.getId();
 
         if ( allBuildsOk )
         {
             return SUCCESS;
         }
         else
         {
             addActionError( "projectGroup.release.error.projectNotInSuccess" );
             return INPUT;
         }
     }
 
     private boolean isParentInProjectGroup( ProjectDependency parent, Collection projectsInGroup )
         throws ContinuumException
     {
         boolean result = false;
 
         Iterator projectsIterator = projectsInGroup.iterator();
 
         while ( projectsIterator.hasNext() )
         {
             Project project = (Project) projectsIterator.next();
 
             if ( parent != null )
             {
                 if ( ( project.getArtifactId().equals( parent.getArtifactId() ) ) &&
                     ( project.getGroupId().equals( parent.getGroupId() ) ) &&
                     ( project.getVersion().equals( parent.getVersion() ) ) )
                 {
                     result = true;
                 }
             }
         }
 
         return result;
     }
 
     private void populateProjectGroupUsers( ProjectGroup group )
     {
         List users;
 
         if ( StringUtils.isEmpty( filterKey ) )
         {
             // REVIEW: for caching in the user manager
             users = manager.getUsers( ascending );
         }
         else
         {
             users = findUsers( filterProperty, filterKey, ascending );
         }
 
         projectGroupUsers = new ArrayList();
 
         for ( Iterator i = users.iterator(); i.hasNext(); )
         {
             ProjectGroupUserBean pgUser = new ProjectGroupUserBean();
 
             User user = (User) i.next();
 
             pgUser.setUser( user );
 
             pgUser.setProjectGroup( group );
 
             try
             {
                 Collection effectiveRoles = rbac.getEffectivelyAssignedRoles( user.getUsername() );
 
                 for ( Iterator j = effectiveRoles.iterator(); j.hasNext(); )
                 {
                     Role role = (Role) j.next();
 
                     if ( role.getName().indexOf( projectGroup.getName() ) > -1 )
                     {
                         pgUser.setRoles( effectiveRoles );
                         projectGroupUsers.add( pgUser );
                         break;
                     }
                 }
             }
             catch ( RbacObjectNotFoundException e )
             {
                 pgUser.setRoles( Collections.EMPTY_LIST );
             }
             catch ( RbacManagerException e )
             {
                 pgUser.setRoles( Collections.EMPTY_LIST );
             }
         }
     }
 
     private List findUsers( String searchProperty, String searchKey, boolean orderAscending )
     {
         List users = null;
 
         if ( "username".equals( searchProperty ) )
         {
             users = manager.findUsersByUsernameKey( searchKey, orderAscending );
         }
         else if ( "fullName".equals( getFilterProperty() ) )
         {
             users = manager.findUsersByFullNameKey( searchKey, orderAscending );
         }
         else if ( "email".equals( getFilterProperty() ) )
         {
             users = manager.findUsersByEmailKey( searchKey, orderAscending );
         }
         else
         {
             users = Collections.EMPTY_LIST;
         }
 
         return users;
     }
 
     public int getProjectGroupId()
     {
         return projectGroupId;
     }
 
     public void setProjectGroupId( int projectGroupId )
     {
         this.projectGroupId = projectGroupId;
     }
 
     public ProjectGroup getProjectGroup()
     {
         return projectGroup;
     }
 
     public void setProjectGroup( ProjectGroup projectGroup )
     {
         this.projectGroup = projectGroup;
     }
 
     public boolean isConfirmed()
     {
         return confirmed;
     }
 
     public void setConfirmed( boolean confirmed )
     {
         this.confirmed = confirmed;
     }
 
     public String getDescription()
     {
         return description;
     }
 
     public void setDescription( String description )
     {
         this.description = description;
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName( String name )
     {
         this.name = name;
     }
 
     public Map getProjects()
     {
         return projects;
     }
 
     public void setProjects( Map projects )
     {
         this.projects = projects;
     }
 
     public Map getProjectGroups()
     {
         return projectGroups;
     }
 
     public void setProjectGroups( Map projectGroups )
     {
         this.projectGroups = projectGroups;
     }
 
     public boolean isProjectInCOQueue()
     {
         return projectInCOQueue;
     }
 
     public void setProjectInCOQueue( boolean projectInQueue )
     {
         this.projectInCOQueue = projectInQueue;
     }
 
     public Collection getProjectList()
     {
         return projectList;
     }
 
     public List getProjectGroupUsers()
     {
         return projectGroupUsers;
     }
 
     public boolean isAscending()
     {
         return ascending;
     }
 
     public void setAscending( boolean ascending )
     {
         this.ascending = ascending;
     }
 
     public String getFilterKey()
     {
         return filterKey;
     }
 
     public void setFilterKey( String filterKey )
     {
         this.filterKey = filterKey;
     }
 
     public String getFilterProperty()
     {
         return filterProperty;
     }
 
     public void setFilterProperty( String filterProperty )
     {
         this.filterProperty = filterProperty;
     }
 
     public Map getCriteria()
     {
         return FILTER_CRITERIA;
     }
 
     public void setReleaseProjectId( int releaseProjectId )
     {
         this.releaseProjectId = releaseProjectId;
     }
 
     public int getReleaseProjectId()
     {
         return this.releaseProjectId;
     }
 
     public ProjectGroup getProjectGroup( int projectGroupId )
         throws ContinuumException
     {
         if ( projectGroup == null )
         {
             projectGroup = getContinuum().getProjectGroup( projectGroupId );
         }
         else
         {
             if ( projectGroup.getId() != projectGroupId )
             {
                 projectGroup = getContinuum().getProjectGroup( projectGroupId );
             }
         }
 
         return projectGroup;
     }
 
     public String getProjectGroupName()
         throws ContinuumException
     {
 
         return getProjectGroup( projectGroupId ).getName();
     }
 
     public Map<String, Integer> getBuildDefinitions()
     {
         return buildDefinitions;
     }
 
     public void setBuildDefinitions( Map<String, Integer> buildDefinitions )
     {
         this.buildDefinitions = buildDefinitions;
     }
 
     public int getBuildDefinitionId()
     {
         return buildDefinitionId;
     }
 
     public void setBuildDefinitionId( int buildDefinitionId )
     {
         this.buildDefinitionId = buildDefinitionId;
     }
 
 }
