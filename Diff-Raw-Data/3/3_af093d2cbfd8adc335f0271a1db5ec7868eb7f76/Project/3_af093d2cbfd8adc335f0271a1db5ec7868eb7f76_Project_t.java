 /*
  * Copyright luntsys (c) 2004-2005,
  * Date: 2004-5-20
  * Time: 15:18
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
 
 package com.luntsys.luntbuild.db;
 
 import com.luntsys.luntbuild.builders.Builder;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.facades.lb12.BuilderFacade;
 import com.luntsys.luntbuild.facades.lb12.ProjectFacade;
 import com.luntsys.luntbuild.security.SecurityHelper;
 import com.luntsys.luntbuild.utility.*;
 import com.luntsys.luntbuild.vcs.Vcs;
 import org.acegisecurity.AccessDeniedException;
 import org.acegisecurity.acl.basic.AclObjectIdentity;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.File;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * The class represents a luntbuild project.
  *
  * @author robin shine
  */
 public class Project implements AclObjectIdentity, VariableHolder {
 	private static Log logger = LogFactory.getLog(Project.class);
 
 	private long id;
 	private String name;
 	private String description;
 
 	/**
 	 * List of version control systems configured for this project
 	 */
 	private List vcsList;
 
 	/**
 	 * List of builders configured for this project
 	 */
 	private List builderList;
 
 	/**
 	 * Set of schedules configured for this project
 	 */
 	private Set schedules;
 
 	/**
 	 * Set of vcs logins configured for this project
 	 */
 	private Set vcsLogins;
 
 	/**
 	 * persistent field
 	 */
 	private Set rolesMappings;
 
 	/**
 	 * List of notifier class names applicable for sending build notification of this
 	 * project
 	 */
 	private List notifiers;
 
 	private Set notifyMappings;
 
 	private String variables = "versionIterator=1";
 	private int logLevel = com.luntsys.luntbuild.facades.Constants.LOG_LEVEL_NORMAL;
 
     private static ArrayList dateFormats = new ArrayList();
     static {
         dateFormats.add(new SimpleDateFormat("MM/dd/yyyy"));
         dateFormats.add(new SimpleDateFormat("MM-dd-yyyy"));
         dateFormats.add(new SimpleDateFormat("d MMM yyyy"));
         dateFormats.add(new SimpleDateFormat("d-MMM-yyyy"));
         dateFormats.add(new SimpleDateFormat("d MMM. yyyy"));
         dateFormats.add(new SimpleDateFormat("MMM d, yyyy"));
         dateFormats.add(new SimpleDateFormat("MMM. d, yyyy"));
         dateFormats.add(DateFormat.getDateInstance(SimpleDateFormat.DEFAULT));
         dateFormats.add(DateFormat.getDateInstance(SimpleDateFormat.SHORT));
         dateFormats.add(DateFormat.getDateInstance(SimpleDateFormat.MEDIUM));
         dateFormats.add(DateFormat.getDateInstance(SimpleDateFormat.LONG));
         dateFormats.add(DateFormat.getDateInstance(SimpleDateFormat.FULL));
     }
 
 
 	/**
 	 * set the unique identity of this project, will be called by hibernate
 	 *
 	 * @param id
 	 */
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	/**
 	 * Get identifer of this build
 	 * @return identifer of this build
 	 */
 	public long getId() {
 		return id;
 	}
 
 	/**
 	 * set the name of this project
 	 *
 	 * @param name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Get name of this project
 	 * @return name of this project
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * set the description of this project
 	 *
 	 * @param description
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * Get description of this project
 	 * @return description of this project
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * Get vcs list of this project
 	 * @return vcs list of this project
 	 */
 	public List getVcsList() {
 		if (vcsList == null)
 			vcsList = new ArrayList();
 		return vcsList;
 	}
 
 	/**
 	 * Set vcs list of this project
 	 * @param vcsList
 	 */
 	public void setVcsList(List vcsList) {
 		this.vcsList = vcsList;
 	}
 
 	/**
 	 * Get builder list of this project
 	 * @return builder list of this project
 	 */
 	public List getBuilderList() {
 		if (builderList == null)
 			builderList = new ArrayList();
 		return builderList;
 	}
 
 	/**
 	 * Set builder list of this project
 	 * @param builderList
 	 */
 	public void setBuilderList(List builderList) {
 		this.builderList = builderList;
 	}
 
 	/**
 	 * Get schedules list of this project
 	 * @return schedules list of this project
 	 */
 	public Set getSchedules() {
 		if (schedules == null)
 			schedules = new HashSet();
 		return schedules;
 	}
 
 	/**
 	 * Set schedules of this project
 	 * @param schedules
 	 */
 	public void setSchedules(Set schedules) {
 		this.schedules = schedules;
 	}
 
 	/**
 	 * Get schedule with the specified name
 	 * @param scheduleName
 	 * @return schedule with the specified name
 	 */
 	public Schedule getSchedule(String scheduleName) {
 		return Luntbuild.getDao().loadSchedule(getName(), scheduleName);
 	}
 
 	/**
 	 * Empty method, only want to be conform with ognl indexed property
 	 * @param scheduleName
 	 * @param schedule
 	 */
 	public void setSchedule(String scheduleName, Schedule schedule) {
 		// empty method, only want to conform to ognl indexed property
 	}
 
 	/**
 	 * Get system object. Mainly used for ognl evaluation
 	 * @return system object
 	 */
 	public OgnlHelper getSystem() {
 		return new OgnlHelper();
 	}
 
 	public boolean equals(Object obj) {
 		if (obj != null && obj instanceof Project) {
 			if (getId() == ((Project) obj).getId())
 				return true;
 		}
 		return false;
 	}
 
 	public int hashCode() {
 		return (int) getId();
 	}
 
 	/**
 	 * Get Vcs login mappings
 	 * @return Vcs login mappings
 	 */
 	public Set getVcsLogins() {
 		if (vcsLogins == null)
 			vcsLogins = new HashSet();
 		return vcsLogins;
 	}
 
 	/**
 	 * Set vcs login mappings
 	 * @param vcsLogins
 	 */
 	public void setVcsLogins(Set vcsLogins) {
 		this.vcsLogins = vcsLogins;
 	}
 
 	/**
 	 * This function determines the luntbuild user by the vcs login string
 	 *
 	 * @param login version control system login name
 	 * @param users luntbuild users list
 	 * @return maybe null if no user found to match this vcs login
 	 */
 	public User getUserByVcsLogin(String login, List users) {
 		VcsLogin vcsLogin = VcsLogin.findVcsLogin(getVcsLogins(), login);
 		if (vcsLogin != null)
 			return vcsLogin.getUser();
 
 		// continue to find based on global users
 		Iterator it = users.iterator();
 		while (it.hasNext()) {
 			User user = (User) it.next();
 			if (user.getName().equals(User.CHECKIN_USER_NAME))
 				continue;
 			if (login.equalsIgnoreCase(user.getName()))
 				return user;
 		}
 		return null;
 	}
 
 	/**
 	 * Validate all properties of this project
 	 */
 	public void validate() {
 		validateBasic();
 		Iterator it = getVcsList().iterator();
 		while (it.hasNext()) {
 			Vcs vcs = (Vcs) it.next();
 			vcs.validate();
 		}
 		it = getBuilderList().iterator();
 		while (it.hasNext()) {
 			Builder builder = (Builder) it.next();
 			builder.validate();
 		}
 	}
 
 	/**
 	 * Validate all properties of this project at build time. It is different from validate()
 	 * method in the way that it enforces vcsList and builderList not empty
 	 */
 	public void validateAtBuildTime() {
 		validate();
 		if (getVcsList().size() == 0)
 			throw new ValidationException("No Version Control System defined for project: " + getName());
 	}
 
 	/**
 	 * Validate project basic properties. Complicate properties such as {@link Project#getVcsList()}, {@link Project#getBuilderList()},
 	 *  will not get validated
 	 */
 	public void validateBasic() {
 		try {
 			Luntbuild.validatePathElement(getName());
 		} catch (ValidationException e) {
 			throw new ValidationException("Invalid name: " + e.getMessage());
 		}
 		setName(getName().trim());
 
 		if (logLevel != com.luntsys.luntbuild.facades.Constants.LOG_LEVEL_BRIEF && logLevel != com.luntsys.luntbuild.facades.Constants.LOG_LEVEL_NORMAL &&
 				logLevel != Constants.LOG_LEVEL_VERBOSE)
 			throw new ValidationException("Invalid log level!");
 	}
 
 	/**
 	 * Get facade of this project
 	 * @return facade of this project
 	 */
 	public com.luntsys.luntbuild.facades.lb12.ProjectFacade getFacade() {
 		ProjectFacade facade = new com.luntsys.luntbuild.facades.lb12.ProjectFacade();
 		facade.setId(getId());
 		facade.setName(getName());
 		facade.setDescription(getDescription());
 		facade.setVariables(getVariables());
 		facade.setLogLevel(getLogLevel());
 		facade.setNotifiers(getNotifiers());
 		Iterator it = getVcsList().iterator();
 		while (it.hasNext()) {
 			Vcs vcs = (Vcs) it.next();
 			facade.getVcsList().add(vcs.getFacade());
 		}
 		it = getBuilderList().iterator();
 		while (it.hasNext()) {
 			Builder builder = (Builder) it.next();
 			facade.getBuilderList().add(builder.getFacade());
 		}
 		return facade;
 	}
 
 	/**
 	 * Set facade of this project
 	 * @param facade
 	 */
 	public void setFacade(ProjectFacade facade) {
 		setDescription(facade.getDescription());
 		setVariables(facade.getVariables());
 		setLogLevel(facade.getLogLevel());
 		setNotifiers(facade.getNotifiers());
 		try {
 			getVcsList().clear();
 			Iterator it = facade.getVcsList().iterator();
 			while (it.hasNext()) {
 				com.luntsys.luntbuild.facades.lb12.VcsFacade vcsFacade = (com.luntsys.luntbuild.facades.lb12.VcsFacade) it.next();
 				Vcs vcs = (Vcs) Class.forName(vcsFacade.getVcsClassName()).newInstance();
 				vcs.setFacade(vcsFacade);
 				getVcsList().add(vcs);
 			}
 			getBuilderList().clear();
 			it = facade.getBuilderList().iterator();
 			while (it.hasNext()) {
 				BuilderFacade builderFacade = (com.luntsys.luntbuild.facades.lb12.BuilderFacade) it.next();
 				Builder builder = (Builder) Class.forName(builderFacade.getBuilderClassName()).newInstance();
 				builder.setFacade(builderFacade);
 				getBuilderList().add(builder);
 			}
 		} catch (InstantiationException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Get notifiers configured for this project
 	 * @return notifiers configured for this project
 	 */
 	public List getNotifiers() {
 		if (notifiers == null)
 			notifiers = new ArrayList();
 		return notifiers;
 	}
 
 	/**
 	 * Set list of configured notifiers
 	 * @param notifiers
 	 */
 	public void setNotifiers(List notifiers) {
 		this.notifiers = notifiers;
 	}
 
 	/**
 	 * Determines if vcs contents of this project has changed. This function will use value
 	 * of the following thread local variables in  {@link com.luntsys.luntbuild.utility.OgnlHelper}
 	 * <i> workingSchedule, this variable denotes the project which this method is initiated by
 	 * <i> antProject, this variable denotes the logging ant project this method should use
 	 * <i> revisions, this variable denotes the revisions for working schedule
 	 *
      * @param sinceDate date
 	 * @return if vcs contents of this project has changed
 	 */
 	public boolean isVcsModifiedSince(Date sinceDate) {
 		org.apache.tools.ant.Project antProject = OgnlHelper.getAntProject();
 		Schedule workingSchedule = OgnlHelper.getWorkingSchedule();
 		Revisions revisions;
 
 		antProject.log("Getting revisions for project \"" + getName() + "\"...");
 		Revisions allRevisions = new Revisions();
 		Iterator it = getVcsList().iterator();
 		while (it.hasNext()) {
 			Vcs vcs = (Vcs) it.next();
 			revisions = vcs.deriveBuildTimeVcs(antProject).getRevisionsSince(sinceDate, workingSchedule, antProject);
 			allRevisions.setFileModified(allRevisions.isFileModified() || revisions.isFileModified());
 			allRevisions.getChangeLogs().add("*************************************************************");
 			allRevisions.getChangeLogs().add(vcs.toString());
 			allRevisions.getChangeLogs().add("");
 			allRevisions.getChangeLogs().addAll(revisions.getChangeLogs());
 			allRevisions.getChangeLogins().addAll(revisions.getChangeLogins());
 		}
 		if (OgnlHelper.getRevisions() == null && workingSchedule.getProject() == this)
 			OgnlHelper.setRevisions(allRevisions);
 		return allRevisions.isFileModified();
 	}
 
     /** Returns true if VCS for this project has been modified since given date.
      * @param sinceDate
      * @return true if vcs modified
      */
     public boolean isVcsModifiedSince(String sinceDate) {
         Date date = new Date();
         for (Iterator iter = dateFormats.iterator(); iter.hasNext();) {
             DateFormat df = (DateFormat) iter.next();
 
             try {
                 date = df.parse(sinceDate);
                 return isVcsModifiedSince(date);
             } catch (ParseException ex) {
                 logger.error("Unable to parse date: " + sinceDate);
                 return false;
             } catch (NumberFormatException nfe) {
                 logger.error("Unable to parse date: " + sinceDate);
                 return false;
             }
         }
         return false;
     }
 
     /** Returns true if VCS for given schedule last build has been modified.
      * @param scheduleName
      * @return true if vcs modified
      */
     public boolean isVcsModified(String scheduleName) {
         Schedule s = getSchedule(scheduleName);
         Build build = Luntbuild.getDao().loadLastBuild(s);

        return (build == null) ? true : isVcsModifiedSince(build.getStartDate());
     }
 
 	/**
 	 * Get configured notification mappings
 	 * @return configured notification mappings
 	 */
 	public Set getNotifyMappings() {
 		if (notifyMappings == null)
 			notifyMappings = new HashSet();
 		return notifyMappings;
 	}
 
 	/**
 	 * Set notification mappings
 	 * @param notifyMappings
 	 */
 	public void setNotifyMappings(Set notifyMappings) {
 		this.notifyMappings = notifyMappings;
 	}
 
 	/**
 	 * Get log level of this project
 	 * @return one value of
 	 * {@link com.luntsys.luntbuild.facades.Constants#LOG_LEVEL_BRIEF},
 	 * {@link com.luntsys.luntbuild.facades.Constants#LOG_LEVEL_NORMAL},
 	 * {@link com.luntsys.luntbuild.facades.Constants#LOG_LEVEL_VERBOSE}
 	 */
 	public int getLogLevel() {
 		return logLevel;
 	}
 
 	/**
 	 * Set log level of this project
 	 *
 	 * @param logLevel refer to {@link Project#getLogLevel()}
 	 */
 	public void setLogLevel(int logLevel) {
 		this.logLevel = logLevel;
 	}
 
 	/**
 	 * Get roles mappings
 	 * @return roles mappings
 	 */
 	public Set getRolesMappings() {
 		if (rolesMappings == null)
 			rolesMappings = new HashSet();
 		return rolesMappings;
 	}
 
 	/**
 	 * Set roles mappings
 	 * @param rolesMappings
 	 */
 	public void setRolesMappings(Set rolesMappings) {
 		this.rolesMappings = rolesMappings;
 	}
 
 	/**
 	 * split project related rolemapping to
 	 * separate list for handling by model
 	 */
 	public List getMappedRolesUserList(String roleName) {
 		List usersWithAssignedRoles = new ArrayList();
 
 		Set rml = getRolesMappings();
 
 		if (rml != null) {
 			Iterator iter = rml.iterator();
 
 			while (iter.hasNext()) {
 				RolesMapping rm = (RolesMapping) iter.next();
 
 				User user = rm.getUser();
 				Role role = rm.getRole();
 
 				if (role.getName().equals(roleName)) {
 					usersWithAssignedRoles.add(user);
 				}
 			}
 		}
 
 		return usersWithAssignedRoles;
 	}
 
 	/**
 	 * save all new mapped roles to project
 	 *
 	 * @param userlist
 	 * @param roleName
 	 */
 	public void putMappedRolesUserList(List userlist, String roleName) {
 		// remove all existing roles
 		Set rolemappings = getRolesMappings();
 
 		if (rolemappings != null) {
 			Set mappingsToRemove = new HashSet();
 			Iterator iter = rolemappings.iterator();
 			while (iter.hasNext()) {
 				RolesMapping rolemapping = (RolesMapping) iter.next();
 				if (rolemapping.getRole().getName().equals(roleName)) {
 					mappingsToRemove.add(rolemapping);
 				}
 			}
 			rolemappings.removeAll(mappingsToRemove);
 		} else {
 			rolemappings = new HashSet();
 			setRolesMappings(rolemappings);
 		}
 
 		if (userlist != null) {
 			// find dbbased matching role
 			Role role = getMatchingRole(roleName);
 
 			Iterator iter = userlist.iterator();
 
 			while (iter.hasNext()) {
 				User user = (User) iter.next();
 
 				RolesMapping rm = new RolesMapping();
 
 				rm.setUser(user);
 				rm.setProject(this);
 				rm.setRole(role);
 
 				rolemappings.add(rm);
 			}
 		}
 	}
 
 	/**
 	 * Get Role object matching specified role name
 	 * @param roleName
 	 * @return Role object matching specified role name
 	 */
 	private Role getMatchingRole(String roleName) {
 		List internalRoles = Role.getRoles();
 
 		Iterator iter = internalRoles.iterator();
 		boolean found = false;
 		Role role = null;
 
 		while (iter.hasNext() && (found == false)) {
 			role = (Role) iter.next();
 
 			found = role.getName().equals(roleName);
 		}
 
 		return role;
 	}
 
 	public String toString() {
 		return getName();
 	}
 
 	/**
 	 * Create new project by copy current project
 	 * @return new project by copy current project
 	 */
 	public Project createNewByCopy() {
 		Project newProject = new Project();
 		newProject.setBuilderList(getBuilderList());
 		newProject.setDescription(getDescription());
 		newProject.setVariables(getVariables());
 		newProject.setLogLevel(getLogLevel());
 		newProject.setName(getName());
 		newProject.setNotifiers(getNotifiers());
 		newProject.setVcsList(getVcsList());
 		Iterator it = getRolesMappings().iterator();
 		while (it.hasNext()) {
 			RolesMapping mapping = (RolesMapping) it.next();
 			RolesMapping newMapping = new RolesMapping(mapping.getUser(), newProject, mapping.getRole());
 			newProject.getRolesMappings().add(newMapping);
 		}
 		it = getNotifyMappings().iterator();
 		while (it.hasNext()) {
 			NotifyMapping mapping = (NotifyMapping) it.next();
 			NotifyMapping newMapping = new NotifyMapping(newProject, mapping.getUser());
 			newProject.getNotifyMappings().add(newMapping);
 		}
 
 		it = getVcsLogins().iterator();
 		while (it.hasNext()) {
 			VcsLogin vcsLogin = (VcsLogin) it.next();
 			VcsLogin newVcsLogin = new VcsLogin(newProject, vcsLogin.getUser(), vcsLogin.getLogin());
 			newVcsLogin.setId(vcsLogin.getId());
 			newProject.getVcsLogins().add(newVcsLogin);
 		}
 		it = getSchedules().iterator();
 		while (it.hasNext()) {
 			Schedule schedule = (Schedule) it.next();
 			Schedule newSchedule = new Schedule();
 			newSchedule.setId(schedule.getId());
 			newSchedule.setProject(newProject);
 			newSchedule.setName(schedule.getName());
 			newSchedule.setFacade(schedule.getFacade());
 			newProject.getSchedules().add(newSchedule);
 		}
 		return newProject;
 	}
 
 	public List getUsersToNotify() {
 		List usersToNotify = new ArrayList();
 		Iterator it = getNotifyMappings().iterator();
 		while (it.hasNext()) {
 			NotifyMapping mapping = (NotifyMapping) it.next();
 			usersToNotify.add(mapping.getUser());
 		}
 		return usersToNotify;
 	}
 
 	public void putUsersToNotify(List usersToNotify) {
 		getNotifyMappings().clear();
 		Iterator it = usersToNotify.iterator();
 		while (it.hasNext()) {
 			User user = (User) it.next();
 			NotifyMapping mapping = new NotifyMapping();
 			mapping.setProject(this);
 			mapping.setUser(user);
 			getNotifyMappings().add(mapping);
 		}
 	}
 
 	/**
 	 * Get all variables encoded in a string
 	 * @return all variables encoded in a string
 	 */
 	public String getVariables() {
 		return variables;
 	}
 
 	/**
 	 * Set all variables encoded in a string
 	 * @param variables
 	 */
 	public void setVariables(String variables) {
 		this.variables = variables;
 	}
 
 	/**
 	 * Get Variable with specified variable name
 	 * @param name
 	 * @return Variable with specified variable name
 	 */
 	public Variable getVar(String name) {
 		if (!Luntbuild.isEmpty(getVariables())) {
 			BufferedReader reader = new BufferedReader(new StringReader(getVariables()));
 			try {
 				String line;
 				while ((line = reader.readLine()) != null) {
 					if (line.trim().equals(""))
 						continue;
 					String varName = Luntbuild.getAssignmentName(line);
 					String varValue = Luntbuild.getAssignmentValue(line);
 					if (name.trim().equals(varName)) {
 						return new Variable(this, name.trim(), varValue);
 					}
 				}
 			} catch (IOException e) {
 				// ignores
 			}
 		}
 		return new Variable(this, name.trim(), "");
 	}
 
 	/**
 	 * Set value of specified variable
 	 * @param name name of the variable to set
 	 * @param var value of variable is stored in this parameter
 	 */
 	public void setVar(String name, Variable var) {
 		if (!SecurityHelper.isPrjAdministrable(getId()))
 			throw new AccessDeniedException("Permission denied!");
 		if (OgnlHelper.isTestMode())
 			return;
 		String newVariables = "";
 		boolean varFound = false;
 		if (!Luntbuild.isEmpty(getVariables())) {
 			BufferedReader reader = new BufferedReader(new StringReader(getVariables()));
 			try {
 				String line;
 				while ((line = reader.readLine()) != null) {
 					if (line.trim().equals(""))
 						continue;
 					String varName = Luntbuild.getAssignmentName(line);
 					if (name.trim().equals(varName)) {
 						newVariables += name.trim() + "=" + var.getValue() + "\n";
 						varFound = true;
 					} else
 						newVariables += line + "\n";
 				}
 			} catch (IOException e) {
 				// ignores
 			}
 		}
 		if (!varFound)
 			newVariables += name.trim() + "=" + var.getValue() + "\n";
 		setVariables(newVariables);
 		Luntbuild.getDao().saveProject(this);
 	}
 
 	public Builder getBuilderByName(String name) {
 		Iterator it = getBuilderList().iterator();
 		while (it.hasNext()) {
 			Builder builder = (Builder) it.next();
 			if (builder.getName().equals(name))
 				return builder;
 		}
 		return null;
 	}
 
 	/**
 	 * Get publishing directory of this project, builds generated in this project will be put under this
 	 * directory
 	 * @return publishing directory of this project
 	 */
 	public String getPublishDir() {
 		String publishDir = (String) Luntbuild.getProperties().get("publishDir");
 		if (Luntbuild.isEmpty(publishDir))
 			publishDir = new File(Luntbuild.installDir + "/publish").getAbsolutePath();
 		publishDir = publishDir + File.separator + getName();
 		try {
 			publishDir = new File(publishDir).getCanonicalPath();
 			return publishDir.replaceAll("\\\\", "\\\\\\\\"); // in order to keep back slash for ognl expression evaluation
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
