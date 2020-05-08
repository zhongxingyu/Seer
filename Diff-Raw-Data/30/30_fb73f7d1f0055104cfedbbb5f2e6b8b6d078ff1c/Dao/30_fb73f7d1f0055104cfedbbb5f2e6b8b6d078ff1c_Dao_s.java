 /*
  * Copyright luntsys (c) 2004-2005,
  * Date: 2004-5-10
  * Time: 8:45:44
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: 1.
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 
 package com.luntsys.luntbuild.dao;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.dao.DataAccessException;
 
 import com.luntsys.luntbuild.db.Build;
 import com.luntsys.luntbuild.db.Project;
 import com.luntsys.luntbuild.db.Property;
 import com.luntsys.luntbuild.db.Role;
 import com.luntsys.luntbuild.db.Schedule;
 import com.luntsys.luntbuild.db.User;
 import com.luntsys.luntbuild.db.VcsLogin;
 import com.luntsys.luntbuild.facades.SearchCriteria;
 import com.luntsys.luntbuild.facades.lb12.DataCollection;
 import com.luntsys.luntbuild.facades.lb12.ProjectCollection;
 
 /**
  * Data access interface of this application.
  *
  * @author robin shine
  */
 public interface Dao {
 	/**
 	 * Saves the system level properties to the persistent data store.
 	 * 
 	 * @param properties the properties
 	 * @throws DataAccessException if an error occurs durring the operation
 	 * @see Property
 	 */
 	void saveProperties(Map properties);
 
 	/**
 	 * Loads the system level properties from the persistent data store.
 	 * 
 	 * @return the properties
 	 * @throws DataAccessException if an error occurs durring the operation
 	 * @see Property
 	 */
 	Map loadProperties();
 
 	/**
 	 * Saves the given project.
 	 * 
      * @param project the project
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void saveProject(Project project);
 
     /**
      * Saves the given project - for internal use only to avoid security.
      * 
      * @param project the project
      * @throws DataAccessException if an error occurs durring the operation
      */
     void saveProjectInternal(Project project);
 
 	/**
 	 * Loads all projects from the persistent data store.
 	 * Collections of the project may not be loaded.
 	 * 
 	 * @return a list of project objects ordered by name
 	 * @throws DataAccessException if an error occurs durring the operation
 	 * @see Project
  	 */
 	List loadProjects();
 
     /**
      * Loads all projects from the persistent data store - for INTERNAL use so it is not authorized.
      * Collections of the project may not be loaded.
      * 
      * @return a list of project objects order by name
      * @throws DataAccessException if an error occurs durring the operation
 	 * @see Project
      */
     List loadProjectsInternal();
 
 	/**
 	 * Loads the project that matches the project identifier.
 	 * Collections of the project should be loaded.
 	 * 
 	 * @param projectId the identifier of the project
 	 * @return the project or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Project loadProject(long projectId);
 	
 	/**
 	 * Loads the project that matches the project identifier.
 	 * Collections of the project should be loaded.
 	 * 
 	 * @param projectId the identifier of the project
 	 * @return the project or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
     public Project loadProjectInternal(long projectId);
     
 	/**
 	 * Loads the project that matches the project name.
 	 * Collections of the project should be loaded.
 	 * 
 	 * @param projectName the name of the project
 	 * @return the project or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Project loadProject(String projectName);
 
 	/**
 	 * Deletes the given project.
 	 * 
 	 * @param project the project
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void deleteProject(Project project);
 
 	/**
 	 * Saves the given user.
 	 * 
      * @param user the user
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void saveUser(User user);
 
     /**
      * Saves the given user - for internal use only to avoid security.
      * 
      * @param user the user
      * @throws DataAccessException if an error occurs durring the operation
      */
     void saveUserInternal(User user);
 
     /**
      * Loads all users from the persistent data store.
      * Collections of the user may not be loaded.
      * 
      * @return a list of user object ordered by name
 	 * @throws DataAccessException if an error occurs durring the operation
 	 * @see User
  	 */
 	List loadUsers();
 
 	/**
 	 * Loads the user that matches the user name.
 	 * Collections of the user should be loaded.
 	 * <p>For authentication purpose we need to search for loginName.</p>
 	 * 
 	 * @param userName the name of the user
 	 * @return the user or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	User loadUser(String userName);
 
 	/**
 	 * Loads the user that matches the user identifier.
 	 * Collections of the user should be loaded.
 	 * 
 	 * @param userId the identifier of the user
 	 * @return the user or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	User loadUser(long userId);
 
 	/**
 	 * Deletes the user that matches the user identifier.
 	 * 
 	 * @param userId the identifier of the user
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void deleteUser(long userId);
 
 	/**
 	 * Saves the given schedule.
 	 * 
      * @param schedule the schedule
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void saveSchedule(Schedule schedule);
 
 	/**
 	 * Loads all schedules from the persistent data store.
 	 * Collections of each schedule may not be loaded, but parent objects should be loaded.
 	 * 
 	 * @return a list of schedules object ordered by name
 	 * @throws DataAccessException if an error occurs durring the operation
 	 * @see Schedule
 	 */
 	List loadSchedules();
 
 	/**
 	 * Loads the schedule that matches the schedule identifier.
 	 * Parent objects and collections of the schedule should be loaded.
 	 * 
 	 * @param scheduleId the identifier of the schedule
 	 * @return the schedule or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Schedule loadSchedule(long scheduleId);
 
 	/**
 	 * Loads the schedule that matches the project name and schedule name.
 	 * Parent objects and collections of this schedule will also get loaded.
 	 * 
 	 * @param projectName name of the parent project
 	 * @param scheduleName name of the schedule
 	 * @return the schedule or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Schedule loadSchedule(String projectName, String scheduleName);
 
 	/**
 	 * Deletes the given schedule.
 	 * 
 	 * @param schedule the schedule
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void deleteSchedule(Schedule schedule);
 
 	/**
 	 * Loads the VCS login that matches the VCS login identifier.
 	 * Collections of the VCS login should be loaded.
 	 * 
 	 * @param vcsLoginId the identifier of the VCS login
 	 * @return the VCS login or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	VcsLogin loadVcsLogin(long vcsLoginId);
 
 	/**
 	 * Saves the given VCS login.
 	 * 
      * @param vcsLogin the VCS login
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void saveVcsLogin(VcsLogin vcsLogin);
 
 	/**
 	 * Deletes the given VCS login.
 	 * 
 	 * @param vcsLogin the VCS login
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void deleteVcsLogin(VcsLogin vcsLogin);
 
 	/**
 	 * Saves the given build.
 	 * 
      * @param build the build
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void saveBuild(Build build);
 
 	/**
 	 * Loads the build that matches the build identifier.
 	 * All parent object of this build should be loaded.
 	 * 
 	 * @param buildId the identifier of the build
 	 * @return the build or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Build loadBuild(long buildId);
 
 	/**
 	 * Loads the build that matches the project name, schedule name and build version.
 	 * All parent object and lazy collections of this build will be loaded also.
 	 * 
 	 * @param projectName the name of the parent project
 	 * @param scheduleName the name of the parent schedule
 	 * @param buildVersion the build version of the build
 	 * @return the build or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Build loadBuild(String projectName, String scheduleName, String buildVersion);
 
 	/**
 	 * Loads the build that matches the project name, schedule name and build version
 	 * - for INTERNAL use so it is not authorized.
 	 * All parent object and lazy collections of this build will be loaded also.
 	 * 
 	 * @param projectName the name of the parent project
 	 * @param scheduleName the name of the parent schedule
 	 * @param buildVersion the build version of the build
 	 * @return the build or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Build loadBuildInternal(String projectName, String scheduleName, String buildVersion);
 
 	/**
 	 * Deletes the given build.
 	 * 
 	 * @param build the build
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void deleteBuild(Build build);
 
 	/**
 	 * Loads the last build generated by the specified schedule.
 	 * 
 	 * @param schedule the schedule to load from
 	 * @return the last build or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Build loadLastBuild(Schedule schedule);
 
 	/**
 	 * Loads the last build for specified schedule name in specified project name.
 	 * 
 	 * @param projectName the name of the parent project
 	 * @param scheduleName the name of the schedule to load from
 	 * @return the last build or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Build loadLastBuild(String projectName, String scheduleName);
 
 	/**
 	 * Loads the last build for specified schedule name in specified project name
 	 * - for INTERNAL use so it is not authorized.
 	 * 
 	 * @param projectName the name of the parent project
 	 * @param scheduleName the name of the schedule to load from
 	 * @return the last build or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Build loadLastBuildInternal(String projectName, String scheduleName);
 
 	/**
 	 * Loads last successful build for specified schedule.
 	 * 
 	 * @param schedule the schedule to load from
 	 * @return the last successful build or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Build loadLastSuccessBuild(Schedule schedule);
 
 	/**
 	 * Loads last successful build for specified schedule name in specified project name.
 	 * 
 	 * @param projectName the name of the parent project
 	 * @param scheduleName the name of the schedule to load from
 	 * @return the last successful build or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Build loadLastSuccessBuild(String projectName, String scheduleName);
 
 	/**
 	 * Loads the build previous to the specified build.
 	 * 
 	 * @param build the after the desired build
 	 * @return the previous build or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Build loadPreviousBuild(Build build);
 
 	/**
 	 * Determines how many builds can be found using the specified search criteria.
 	 * 
 	 * @param searchCriteria the search criteria
 	 * @return the number of builds found
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	int countBuilds(SearchCriteria searchCriteria);
 
 	/**
 	 * Searches builds by specifying a search criteria.
 	 * 
 	 * @param searchCriteria the search criteria
 	 * @param start the start index (0 based) for the retrieved builds
 	 * @param count the number of builds to retrieve. If this value equals 0, no limits will be set
 	 * on the number of retrieved builds.
 	 * @return the list of builds matching the search criteria
 	 * @throws DataAccessException if an error occurs durring the operation
 	 * @see Build
 	 */
 	List searchBuilds(SearchCriteria searchCriteria,  int start, int count);
 
 	/**
 	 * Checks if there are any un-finished builds in the system,
 	 * and mark them as failed. This method is called at start up of the
 	 * system in order to clear status of un-finished builds.
 	 * 
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void processUnfinishedBuilds();
 
 	/**
 	 * Checks if there are any running schedules in the system,
 	 * and mark them as failed. This method is called at start up of the
 	 * system in order to clear status of un-finished schedule executions.
 	 * 
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void processUnfinishedSchedules();
 
 	/**
 	 * Deletes all builds matching the specified search criteria.
 	 * 
 	 * @param searchCriteria the search criteria
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void deleteBuilds(SearchCriteria searchCriteria);
 
 	/**
 	 * Loads the currently running build for specified schedule.
 	 * 
 	 * @param schedule the schedule
 	 * @return the running build or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Build loadRunningBuild(Schedule schedule);
 
 	/**
 	 * Moves all builds matching specified search criteria to specified schedule.
 	 * 
 	 * @param searchCriteria the search criteria
 	 * @param scheduleId the identifier of the destination schedule
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void moveBuilds(SearchCriteria searchCriteria, long scheduleId);
 
 	/**
 	 * Moves the specified build to specified schedule.
 	 * 
 	 * @param buildId the identifier of the build to move
 	 * @param scheduleId the identifier of the destination schedule
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void moveBuild(long buildId, long scheduleId);
 
     /**
      * Loads all security roles from the persistent data store.
      * 
      * @return all defined security roles
      * @throws DataAccessException if an error occurs durring the operation
      */
     List loadRoles();
 
 	/**
 	 * Loads the role that matches the role name.
 	 * Collections of the role should be loaded.
 	 * 
 	 * @param roleName the name of the role
 	 * @return the role or <code>null</code>
 	 * @throws DataAccessException if an error occurs durring the operation
 	 */
 	Role loadRole(String roleName);
 
 	/**
 	 * Checks if the user name of the specified, unsaved user is already used
 	 * by a saved user.
 	 * 
 	 * @param user the unsaved user
 	 * @return <code>true</code> if the user name is already used
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	boolean isUserNameUsed(User user);
 
 	/**
 	 * Checks if a user with the specified name exists.
 	 * 
 	 * @param userName the name to search for
 	 * @return <code>true</code> if a user with that name exists
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	boolean isUserExist(String userName);
 	boolean isUserExistInternal(String userName);
 
 	/**
 	 * Initializes the persistent data store with required data.
 	 * 
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void initialize();
 
 	/**
 	 * Checks if the project name of the specified, unsaved project is already used
 	 * by a saved project.
 	 * 
 	 * @param project the unsaved project
 	 * @return <code>true</code> if the project name is already in use
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	boolean isProjectNameUsed(Project project);
 
 	/**
 	 * Checks if the schedule name of the specified, unsaved schedule is already used
 	 * by a saved schedule.
 	 * 
 	 * @param schedule the unsaved schedule
 	 * @return <code>true</code> if the schedule name is already in use
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	boolean isScheduleNameUsed(Schedule schedule);
 
 	/**
 	 * Checks if the VCS login name of the specified, unsaved VCS login is already used
 	 * by a saved VCS login.
 	 * 
 	 * @param vcsLogin the unsaved VCS login
 	 * @return <code>true</code> if the VCS login name is already in use
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	boolean isVcsLoginNameUsed(VcsLogin vcsLogin);
 
 	/**
 	 * Removes all builds from a schedule except for the latest number of builds as
 	 * defined by <code>reserveCount</code>.
 	 * <p>If <code>reserveCount</code> is <code>5</code>, then this function would delete
 	 * all builds except the last <code>5</code> builds from the schedule.</p>
 	 * 
 	 * @param schedule the schedule
 	 * @param reserveCount the number of builds to keep
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void reserveBuildsByCount(Schedule schedule, int reserveCount);
 
 	/**
 	 * Deletes all builds from a schedule that started before the specified date.
 	 * 
 	 * @param schedule the schedule
 	 * @param date the cut off date for deleting builds
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void removeBuildsBefore(Schedule schedule, Date date);
 
 	/**
 	 * Loads all data from the persistent data store into a version 1.2 compatable facade collection object.
 	 * 
 	 * @return the facade collection object
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	DataCollection loadDataCollection12();
 
 	/**
 	 * Saves all data from a version 1.2 compatable facade collection object to the persistent data store.
 	 * 
 	 * @param data12 the facade collection
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void saveDataCollection12(DataCollection data12);
 
 	/**
 	 * Loads all data for a project from the persistent data store into a version 1.2 compatable project facade collection object.
 	 * 
 	 * @param projectId the identifier of the project
 	 * @return the project facade collection object
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	ProjectCollection loadProjectCollection12(long projectId);
 
 	/**
 	 * Saves all data from a version 1.2 compatable project facade collection object to the persistent project data store.
 	 * 
 	 * @param project12 the project facade collection
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void saveProjectCollection12(ProjectCollection project12);
 
 	/**
 	 * Erases all data stored in the persistent data store.
 	 * 
      * @throws DataAccessException if an error occurs durring the operation
 	 */
 	void eraseExistingData();
 }
