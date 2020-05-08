 /*
  * Copyright (c) 2013 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Winston Prakash
  */
 package org.eclipse.hudson.security.team;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 import hudson.BulkChange;
 import hudson.XmlFile;
 import hudson.model.Hudson;
 import hudson.model.Item;
 import hudson.model.Job;
 import hudson.model.Saveable;
 import hudson.model.TopLevelItem;
 import hudson.model.listeners.SaveableListener;
 import hudson.security.ACL;
 import hudson.security.AccessControlled;
 import hudson.security.Permission;
 import hudson.security.AuthorizationStrategy;
 import hudson.security.SecurityRealm;
 import hudson.util.FormValidation;
 import hudson.util.XStream2;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 import org.eclipse.hudson.security.HudsonSecurityEntitiesHolder;
 import org.eclipse.hudson.security.HudsonSecurityManager;
 import org.kohsuke.stapler.HttpResponse;
 import org.kohsuke.stapler.HttpResponses;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.AccessDeniedException;
 import org.springframework.security.Authentication;
 import org.springframework.security.GrantedAuthority;
 
 /**
  * Manager that manages the teams and their persistence
  *
  * @since 3.1.0
  * @author Winston Prakash
  */
 public final class TeamManager implements Saveable, AccessControlled {
 
     public static final String TEAM_SEPARATOR = ".";
     private List<String> sysAdmins = new CopyOnWriteArrayList<String>();
     private List<Team> teams = new CopyOnWriteArrayList<Team>();
     private transient final XStream xstream = new XStream2();
     private transient Logger logger = LoggerFactory.getLogger(TeamManager.class);
     private transient File hudsonHomeDir;
     private transient File teamsFolder;
     private transient final String teamsConfigFileName = "teams.xml";
     private transient PublicTeam publicTeam;
     private transient final String TEAMS_FOLDER_NAME = "teams";
 
     public TeamManager(File homeDir) {
         hudsonHomeDir = homeDir;
         teamsFolder = new File(hudsonHomeDir, TEAMS_FOLDER_NAME);
         if (!teamsFolder.exists()) {
             teamsFolder.mkdirs();
         }
         initializeXstream();
         load();
         ensurePublicTeam();
     }
 
     public boolean isTeamManagementEnabled() {
         HudsonSecurityManager hudsonSecurityManager = HudsonSecurityEntitiesHolder.getHudsonSecurityManager();
         if (hudsonSecurityManager != null) {
             AuthorizationStrategy authorizationStrategy = hudsonSecurityManager.getAuthorizationStrategy();
             if (authorizationStrategy instanceof TeamBasedAuthorizationStrategy) {
                 return true;
             }
         }
         return false;
     }
 
     public void addSysAdmin(String adminName) throws IOException {
         if (!sysAdmins.contains(adminName)) {
             sysAdmins.add(adminName);
             save();
         }
     }
 
     public void removeSysAdmin(String adminName) throws IOException {
         if (sysAdmins.contains(adminName)) {
             sysAdmins.remove(adminName);
             save();
         }
     }
 
     public List<String> getSysAdmins() {
         return sysAdmins;
     }
 
     public boolean isCurrentUserSysAdmin() {
         String user = getCurrentUser();
         logger.debug("Checking if principal " + user + " is a System Admin");
         if (isSysAdmin(user)) {
             return true;
         } else {
             for (GrantedAuthority ga : getCurrentUserRoles()) {
                 logger.debug("Checking if the principal's role " + ga.toString() + " is a System Admin Role");
                 if (isSysAdmin(ga.getAuthority())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public boolean isCurrentUserTeamAdmin() {
         if (isCurrentUserSysAdmin()) {
             return true;
         }
         String user = getCurrentUser();
         for (Team team : teams) {
             if (team.isAdmin(user)) {
                 return true;
             } else {
                 // Check if any of the group the user is a memmber has given
                 // Team Admin Role
                 for (GrantedAuthority ga : getCurrentUserRoles()) {
                     logger.debug("Checking if the principal's role " + ga.toString() + " is a Team Admin Role");
                     if (team.isAdmin(ga.getAuthority())) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     public boolean isSysAdmin(String userName) {
         boolean isSysAdmin;
         HudsonSecurityManager hudsonSecurityManager = HudsonSecurityEntitiesHolder.getHudsonSecurityManager();
         SecurityRealm securityRealm = null;
         if (hudsonSecurityManager != null) {
             securityRealm = hudsonSecurityManager.getSecurityRealm();
         }
         if ((securityRealm != null) && securityRealm instanceof TeamAwareSecurityRealm) {
             TeamAwareSecurityRealm teamAwareSecurityRealm = (TeamAwareSecurityRealm) securityRealm;
             isSysAdmin = teamAwareSecurityRealm.isCurrentUserSysAdmin();
         } else {
             isSysAdmin = sysAdmins.contains(userName);
         }
         return isSysAdmin;
     }
 
     //Used by TeamManager Jelly to display team details in master-details fashion
     public Map<String, Team> getTeams() {
         Map<String, Team> teamMap = new HashMap<String, Team>();
         for (Team team : teams) {
             teamMap.put(team.getName(), team);
         }
         return teamMap;
     }
 
     public List<String> getTeamNames() {
         List<String> teamList = new ArrayList<String>();
         for (Team team : teams) {
             teamList.add(team.getName());
         }
         return teamList;
     }
 
     public Team createTeam(String teamName, String description, String customFolder) throws IOException, TeamAlreadyExistsException {
         for (Team team : teams) {
             if (teamName.equals(team.getName())) {
                 throw new TeamAlreadyExistsException(teamName);
             }
         }
 
         Team newTeam = new Team(teamName, description, customFolder, this);
         addTeam(newTeam);
         return newTeam;
     }
 
     @Override
     public ACL getACL() {
         AuthorizationStrategy authorizationStrategy = HudsonSecurityEntitiesHolder.getHudsonSecurityManager().getAuthorizationStrategy();
         if (authorizationStrategy instanceof TeamBasedAuthorizationStrategy) {
             TeamBasedAuthorizationStrategy teamBasedAuthorizationStrategy = (TeamBasedAuthorizationStrategy) authorizationStrategy;
             return teamBasedAuthorizationStrategy.getACL(this);
         }
         // Team will not be used if Team Based Authorization Strategy is not used
         return new ACL() {
             @Override
             public boolean hasPermission(Authentication a, Permission permission) {
                 return false;
             }
         };
     }
 
     @Override
     public boolean hasPermission(Permission permission) {
         return getACL().hasPermission(permission);
     }
 
     @Override
     public void checkPermission(Permission permission) throws AccessDeniedException {
         getACL().checkPermission(permission);
     }
 
     private void addTeam(Team team) throws IOException {
         teams.add(team);
         save();
     }
 
     public Team createTeam(String teamName) throws IOException, TeamAlreadyExistsException {
         return createTeam(teamName, teamName, null);
     }
 
     public void deleteTeam(String teamName) throws TeamNotFoundException, IOException {
         Team team = findTeam(teamName);
         if (Team.PUBLIC_TEAM_NAME.equals(team.getName())) {
             throw new IOException("Cannot delete public team");
         }
         // Make deleted team jobs public
         Team publicTeam = getPublicTeam();
         for (TeamJob job : team.getJobs()) {
             TopLevelItem item = Hudson.getInstance().getItem(job.getId());
             if (item != null && (item instanceof Job)) {
                 moveJob((Job) item, team, publicTeam);
             }
         }
         teams.remove(team);
         save();
     }
 
     public HttpResponse doCreateTeam(@QueryParameter String teamName, @QueryParameter String description, @QueryParameter String customFolder) throws IOException {
         if (!isCurrentUserSysAdmin()) {
             return new TeamUtils.ErrorHttpResponse("No permission to create team.");
         }
         if ((teamName == null) || "".equals(teamName.trim())) {
             return new TeamUtils.ErrorHttpResponse("Team name required.");
         }
         if ((customFolder != null) && !"".equals(customFolder.trim())) {
             File folder = new File(customFolder.trim());
            if (!folder.mkdirs()) {
                 return new TeamUtils.ErrorHttpResponse("Could not create custom team folder - " + customFolder);
             }
         }
         try {
             createTeam(teamName, description, customFolder);
             return HttpResponses.ok();
         } catch (TeamAlreadyExistsException ex) {
             return new TeamUtils.ErrorHttpResponse(ex.getLocalizedMessage());
         }
     }
 
     public HttpResponse doDeleteTeam(@QueryParameter String teamName) throws IOException {
         if (!isCurrentUserSysAdmin()) {
             return new TeamUtils.ErrorHttpResponse("No permission to delete team.");
         }
         if ((teamName == null) || "".equals(teamName.trim())) {
             return new TeamUtils.ErrorHttpResponse("Team name required.");
         }
         try {
             deleteTeam(teamName);
             return HttpResponses.ok();
         } catch (TeamNotFoundException ex) {
             return new TeamUtils.ErrorHttpResponse(ex.getLocalizedMessage());
         }
     }
 
     public HttpResponse doAddTeamMember(@QueryParameter String teamName,
             @QueryParameter String teamMemberSid,
             @QueryParameter boolean isTeamAdmin,
             @QueryParameter boolean canCreate,
             @QueryParameter boolean canDelete,
             @QueryParameter boolean canConfigure,
             @QueryParameter boolean canBuild) throws IOException {
         if (!isCurrentUserTeamAdmin()) {
             return new TeamUtils.ErrorHttpResponse("No permission to add team member.");
         }
         if ((teamName == null) || "".equals(teamName.trim())) {
             return new TeamUtils.ErrorHttpResponse("Team name required.");
         }
         if ((teamMemberSid == null) || "".equals(teamMemberSid.trim())) {
             return new TeamUtils.ErrorHttpResponse("Team member name required.");
         }
         Team team;
         try {
             team = findTeam(teamName);
             if (team.findMember(teamMemberSid) == null) {
                 team.addMember(teamMemberSid, isTeamAdmin, canCreate, canDelete, canConfigure, canBuild);
                 return FormValidation.respond(FormValidation.Kind.OK, TeamUtils.getIcon(teamMemberSid));
             } else {
                 return new TeamUtils.ErrorHttpResponse(teamMemberSid + " is already a team member.");
             }
         } catch (TeamNotFoundException ex) {
             return new TeamUtils.ErrorHttpResponse(teamName + " is not a valid team.");
         }
     }
 
     public HttpResponse doUpdateTeamMember(@QueryParameter String teamName,
             @QueryParameter String teamMemberSid,
             @QueryParameter boolean isTeamAdmin,
             @QueryParameter boolean canCreate,
             @QueryParameter boolean canDelete,
             @QueryParameter boolean canConfigure,
             @QueryParameter boolean canBuild) throws IOException {
         if (!isCurrentUserTeamAdmin()) {
             return new TeamUtils.ErrorHttpResponse("No permission to add team member.");
         }
         if ((teamName == null) || "".equals(teamName.trim())) {
             return new TeamUtils.ErrorHttpResponse("Team name required.");
         }
         if ((teamMemberSid == null) || "".equals(teamMemberSid.trim())) {
             return new TeamUtils.ErrorHttpResponse("Team member name required.");
         }
         Team team;
         try {
             team = findTeam(teamName);
             TeamMember currentMember = team.findMember(teamMemberSid);
             if (currentMember != null) {
                 team.updateMember(teamMemberSid, isTeamAdmin, canCreate, canDelete, canConfigure, canBuild);
                 return FormValidation.respond(FormValidation.Kind.OK, TeamUtils.getIcon(teamMemberSid));
             } else {
                 return new TeamUtils.ErrorHttpResponse(teamMemberSid + " is not a team member.");
             }
         } catch (TeamNotFoundException ex) {
             return new TeamUtils.ErrorHttpResponse(teamName + " is not a valid team.");
         }
 
     }
 
     public HttpResponse doRemoveTeamMember(@QueryParameter String teamName,
             @QueryParameter String teamMemberSid) throws IOException {
         if (!isCurrentUserTeamAdmin()) {
             return new TeamUtils.ErrorHttpResponse("No permission to remove team member");
         }
         if ((teamName == null) || "".equals(teamName.trim())) {
             return new TeamUtils.ErrorHttpResponse("Team name required.");
         }
         if ((teamMemberSid == null) || "".equals(teamMemberSid.trim())) {
             return new TeamUtils.ErrorHttpResponse("Team member name required.");
         }
         Team team;
         try {
             team = findTeam(teamName);
         } catch (TeamNotFoundException ex) {
             return new TeamUtils.ErrorHttpResponse(teamName + " is not a valid team.");
         }
         TeamMember currentMember = team.findMember(teamMemberSid);
         if (currentMember != null) {
             team.removeMember(teamMemberSid);
             return HttpResponses.ok();
         } else {
             return new TeamUtils.ErrorHttpResponse(teamMemberSid + " is not a team member.");
         }
     }
 
     public HttpResponse doMoveJob(@QueryParameter String jobName, @QueryParameter String teamName) throws IOException {
         if (!isCurrentUserTeamAdmin()) {
             return new TeamUtils.ErrorHttpResponse("No permission to remove team member");
         }
         if ((teamName == null) || "".equals(teamName.trim())) {
             return new TeamUtils.ErrorHttpResponse("Team name required.");
         }
         if ((jobName == null) || "".equals(jobName.trim())) {
             return new TeamUtils.ErrorHttpResponse("Job  required.");
         }
         Team newTeam;
         try {
             newTeam = findTeam(teamName);
         } catch (TeamNotFoundException ex) {
             return new TeamUtils.ErrorHttpResponse(teamName + " is not a valid team.");
         }
 
         Team oldTeam = findJobOwnerTeam(jobName);
         if (oldTeam == null) {
             return new TeamUtils.ErrorHttpResponse(jobName + " does not belong to any team.");
         }
 
         Item item = Hudson.getInstance().getItem(jobName);
         Job job;
         if (item instanceof Job<?, ?>) {
             job = (Job) item;
             if (job.isBuilding()) {
                 return new TeamUtils.ErrorHttpResponse(job.getName() + " is building.");
             }
             try {
                 moveJob(job, oldTeam, newTeam);
                 return HttpResponses.ok();
             } catch (IOException ex) {
                 return new TeamUtils.ErrorHttpResponse("Faile to move the job " + jobName
                         + " to the team " + teamName + ". " + ex.getLocalizedMessage());
             }
         } else {
             return new TeamUtils.ErrorHttpResponse(jobName + " not a valid job Id.");
         }
     }
 
     public HttpResponse doSetJobVisibility(@QueryParameter String jobName, @QueryParameter String teamNames) throws IOException {
         if (!isCurrentUserTeamAdmin()) {
             return new TeamUtils.ErrorHttpResponse("No permission to set job visibility.");
         }
         if ((jobName == null) || "".equals(jobName.trim())) {
             return new TeamUtils.ErrorHttpResponse("Job id required.");
         }
         Team ownerTeam = findJobOwnerTeam(jobName);
         if (ownerTeam == null) {
             return new TeamUtils.ErrorHttpResponse(jobName + " does not belong to any team.");
         } else {
             TeamJob job = ownerTeam.findJob(jobName);
             job.removeAllVisibilities();
             for (String teamName : teamNames.split(":")) {
                 job.addVisibility(teamName);
             }
             save();
         }
         return HttpResponses.ok();
     }
     
     public HttpResponse doCheckSid(@QueryParameter String sid) throws IOException {
         return FormValidation.respond(FormValidation.Kind.OK, TeamUtils.getIcon(sid));
     }
 
     public void ensureJobInTeam(TopLevelItem item, Team team) throws IOException {
         Job job = (Job) item;
         Team ownerTeam = findJobOwnerTeam(job.getName());
         if (!team.equals(ownerTeam)) {
             moveJob(job, ownerTeam, team);
         }
     }
 
     private void moveJob(Job job, Team oldTeam, Team newTeam) throws IOException {
         try {
             String oldJobName = job.getName();
             String unqualifiedJobName = getUnqualifiedJobName(oldTeam, job.getName());
             String qualifiedNewJobName = getTeamQualifiedJobName(newTeam, unqualifiedJobName);
 
             // Add the new job, rename before removing the old job
             // ensures team manager will find correct locations.
             newTeam.addJob(new TeamJob(qualifiedNewJobName));
             job.renameTo(qualifiedNewJobName);
             oldTeam.removeJob(oldJobName);
 
             Hudson.getInstance().replaceItem(job.getName(), qualifiedNewJobName);
         } catch (Exception exc) {
             throw new IOException(exc);
         }
     }
 
     private String getUnqualifiedJobName(Team team, String jobName) {
         if (!Team.PUBLIC_TEAM_NAME.equals(team.getName()) && jobName.startsWith(team.getName() + TEAM_SEPARATOR)) {
             return jobName.substring(team.getName().length() + 1);
         }
         return jobName;
     }
 
     /**
      * Get the name of the current user admin teams as JSON
      *
      * @return HttpResponse with JSON as content type
      */
     public HttpResponse doGetTeamsJson() {
         return new HttpResponse() {
             @Override
             public void generateResponse(StaplerRequest sr, StaplerResponse rsp, Object o) throws IOException, ServletException {
                 rsp.setStatus(HttpServletResponse.SC_OK);
                 rsp.setContentType("application/json");
                 PrintWriter w = new PrintWriter(rsp.getWriter());
                 w.println("{");
                 List<String> teams = (List<String>) getCurrentUserAdminTeams();
                 for (int i = 0; i < teams.size(); i++) {
                     w.print("\"" + teams.get(i) + "\":\"" + teams.get(i) + "\"");
                     if (i < teams.size() - 1) {
                         w.println(",");
                     }
                 }
                 w.println("}");
                 w.close();
             }
         };
 
     }
 
     /* For Unit Test */
     void addUser(String teamName, String userName) throws TeamNotFoundException, IOException {
         Team team = findTeam(teamName);
         team.addMember(userName, false, false, false, false, false);
         save();
     }
 
     public Team findTeam(String teamName) throws TeamNotFoundException {
         for (Team team : teams) {
             if (teamName.equals(team.getName())) {
                 return team;
             }
         }
         throw new TeamNotFoundException(teamName);
     }
 
     public void removeTeam(String teamName) throws IOException, TeamNotFoundException {
         Team team = findTeam(teamName);
         teams.remove(team);
         save();
     }
 
     /**
      * Return true if current user has access to team. Team management must be
      * enabled.
      *
      * @param teamName
      * @return
      */
     public boolean isCurrentUserHasAccessToTeam(String teamName) {
         if (isTeamManagementEnabled()) {
             try {
                 Team team = findTeam(teamName);
                 if (isCurrentUserSysAdmin()) {
                     return true;
                 }
                 HudsonSecurityManager hudsonSecurityManager = HudsonSecurityEntitiesHolder.getHudsonSecurityManager();
                 SecurityRealm securityRealm = null;
                 if (hudsonSecurityManager != null) {
                     securityRealm = hudsonSecurityManager.getSecurityRealm();
                 }
                 if ((securityRealm != null) && securityRealm instanceof TeamAwareSecurityRealm) {
                     TeamAwareSecurityRealm teamAwareSecurityRealm = (TeamAwareSecurityRealm) securityRealm;
                     if (team.equals(teamAwareSecurityRealm.GetCurrentUserTeam())) {
                         return true;
                     }
                 } else {
                     if (team.isMember(getCurrentUser())) {
                         return true;
                     } else {
                         for (GrantedAuthority ga : getCurrentUserRoles()) {
                             if (team.isMember(ga.getAuthority())) {
                                 return true;
                             }
                         }
                     }
                 }
             } catch (TeamNotFoundException ex) {
                 // no access
             }
         }
         return false;
     }
 
     private String getCurrentUser() {
         Authentication authentication = HudsonSecurityManager.getAuthentication();
         return authentication.getName();
     }
 
     private GrantedAuthority[] getCurrentUserRoles() {
         Authentication authentication = HudsonSecurityManager.getAuthentication();
         return authentication.getAuthorities();
     }
 
     /**
      * Check if current user is not sys admin and is admin of exactly one team.
      *
      * @return
      */
     public boolean isCurrentUserAdminInSingleTeam() {
         return getCurrentUserAdminTeams().size() == 1;
     }
 
     /**
      * Check if current user is admin in more than one team
      */
     public boolean isCurrentUserAdminInMultipleTeams() {
         return getCurrentUserAdminTeams().size() > 1;
     }
     
     /**
      * Get the team in the case where current user is admin of only one team.
      */
     public String getCurrentUserAdminTeam() {
         List<String> teams = (List<String>) getCurrentUserAdminTeams();
         if (teams.size() == 1) {
             return teams.get(0);
         }
         throw new IllegalStateException("Current user is admin of "+teams.size()+" teams");
     }
 
     /**
      * Get all the teams current user is admin of. Sys admin is considered to be
      * admin of all teams.
      */
     public Collection<String> getCurrentUserAdminTeams() {
         List<String> list = new ArrayList<String>();
         boolean admin = isCurrentUserSysAdmin();
         String user = getCurrentUser();
         for (Team team : teams) {
             if (admin || team.isAdmin(user)) {
                 list.add(team.getName());
             } else {
                 // Check if any of the group the user is a memmber, has given Team Admin Role
                 for (GrantedAuthority ga : getCurrentUserRoles()) {
                     logger.debug("Checking if the principal's role " + ga.toString() + " is a Team Admin Role");
                     if (team.isAdmin(ga.getAuthority())) {
                         list.add(team.getName());
                     }
                 }
             }
         }
         return list;
     }
 
     public Collection<Job> getCurrentUserAdminJobs() {
         Hudson hudson = Hudson.getInstance();
         List<Job> jobs = new ArrayList<Job>();
         boolean sysAdmin = isCurrentUserSysAdmin();
         String user = getCurrentUser();
         for (Team team : teams) {
             boolean teamAdmin = false;
             if (team.isAdmin(user)) {
                 teamAdmin = true;
             } else {
                 // Check if any of the group the user is a memmber, has Team Admin Role
                 // and 
                 for (GrantedAuthority ga : getCurrentUserRoles()) {
                     if (team.isAdmin(ga.getAuthority())) {
                         teamAdmin = true;
                     }
                 }
             }
             if (sysAdmin || teamAdmin) {
                 for (TeamJob teamJob : team.getJobs()) {
                     TopLevelItem item = hudson.getItem(teamJob.getId());
                     if (item != null && (item instanceof Job)) {
                         jobs.add((Job) item);
                     }
                 }
             }
         }
         return jobs;
     }
 
     /**
      * Check if current user is in more than one team.
      */
     public boolean isCurrentUserInMultipleTeams() {
         return getCurrentUserTeams().size() > 1;
     }
 
     /**
      * Get all the teams current user is a member of. Sys admin is considered to
      * be a member of all teams.
      */
     public Collection<String> getCurrentUserTeams() {
         List<String> list = new ArrayList<String>();
         boolean admin = isCurrentUserSysAdmin();
         String user = getCurrentUser();
         for (Team team : teams) {
             if (admin || team.isMember(user)) {
                 list.add(team.getName());
             } else {
                 // Check if any of the group the user is a memmber, is also a member of the team
                 for (GrantedAuthority ga : getCurrentUserRoles()) {
                     logger.debug("Checking if the principal's role " + ga.toString() + " is a Team Admin Role");
                     if (team.isMember(ga.getAuthority())) {
                         list.add(team.getName());
                     }
                 }
             }
         }
         return list;
     }
 
     public Team findCurrentUserTeam() {
         Team team;
         HudsonSecurityManager hudsonSecurityManager = HudsonSecurityEntitiesHolder.getHudsonSecurityManager();
         SecurityRealm securityRealm = null;
         if (hudsonSecurityManager != null) {
             securityRealm = hudsonSecurityManager.getSecurityRealm();
         }
         if ((securityRealm != null) && securityRealm instanceof TeamAwareSecurityRealm) {
             TeamAwareSecurityRealm teamAwareSecurityRealm = (TeamAwareSecurityRealm) securityRealm;
             team = teamAwareSecurityRealm.GetCurrentUserTeam();
         } else {
             Authentication authentication = HudsonSecurityManager.getAuthentication();
             team = findUserTeam(authentication.getName());
             if (team != null && !isPublicTeam(team)) {
                 return team;
             } else {
                 for (GrantedAuthority ga : authentication.getAuthorities()) {
                     String grantedAuthority = ga.getAuthority();
                     team = findUserTeam(grantedAuthority);
                     if ((team != null) && !isPublicTeam(team)) {
                         return team;
                     }
                 }
             }
             team = getPublicTeam();
         }
         return team;
     }
 
     public boolean isCurrentUserHasAccess(String jobName) {
         Team userTeam = findCurrentUserTeam();
         if (userTeam != null) {
             if (userTeam.isJobOwner(jobName)) {
                 return true;
             } else {
                 return isAnonymousJob(jobName);
             }
         } else {
             return isAnonymousJob(jobName);
         }
     }
 
     public boolean isUserHasAccess(String userName, String jobName) {
         Team userTeam = findUserTeam(userName);
         if (userTeam != null) {
             return userTeam.isJobOwner(jobName);
         } else {
             for (Team team : teams) {
                 if (team.isJobOwner(jobName)) {
                     // Job belongs to a team so has no access
                     return false;
                 }
             }
             // Job does not belong to any team, so has access
             return true;
         }
     }
 
     public Team findUserTeam(String userName) {
         for (Team team : teams) {
             if (team.isMember(userName)) {
                 return team;
             }
         }
         HudsonSecurityManager hudsonSecurityManager = HudsonSecurityEntitiesHolder.getHudsonSecurityManager();
         SecurityRealm securityRealm = null;
         if (hudsonSecurityManager != null) {
             securityRealm = hudsonSecurityManager.getSecurityRealm();
         }
         if ((securityRealm != null) && securityRealm instanceof TeamAwareSecurityRealm) {
             TeamAwareSecurityRealm teamAwareSecurityRealm = (TeamAwareSecurityRealm) securityRealm;
             return teamAwareSecurityRealm.GetCurrentUserTeam();
         }
         try {
             return findTeam(Team.PUBLIC_TEAM_NAME);
         } catch (TeamNotFoundException ex) {
             //Should never happen. Public team creation is ensured
             ex.printStackTrace();
         }
         return null;
     }
 
     public Team findJobOwnerTeam(String jobName) {
         for (Team team : teams) {
             if (team.isJobOwner(jobName)) {
                 return team;
             }
         }
         return null;
     }
 
     public void addJobToCurrentUserTeam(String jobName) throws IOException, TeamNotFoundException {
         addJob(findCurrentUserTeam(), jobName);
     }
 
     public void addJobToUserTeam(String userName, String jobName) throws IOException, TeamNotFoundException {
         addJob(findUserTeam(userName), jobName);
 
     }
 
     public void addJob(Team team, String jobName) throws IOException, TeamNotFoundException {
         if (team != null) {
             team.addJob(new TeamJob(jobName));
         } else {
             findTeam(Team.PUBLIC_TEAM_NAME).addJob(new TeamJob(jobName));
         }
         save();
     }
 
     public void removeJobFromCurrentUserTeam(String jobName) throws IOException {
         removeJob(findCurrentUserTeam(), jobName);
     }
 
     public void removeJobFromUserTeam(String userName, String jobName) throws IOException {
         removeJob(findUserTeam(userName), jobName);
     }
 
     public void removeJob(Team team, String jobName) throws IOException {
         if (team != null) {
             team.removeJob(jobName);
             save();
         }
     }
 
     public void renameJobInCurrentUserTeam(String oldJobName, String newJobName) throws IOException {
         renameJob(findCurrentUserTeam(), oldJobName, newJobName);
     }
 
     public void renameJobInUserTeam(String userName, String oldJobName, String newJobName) throws IOException {
         renameJob(findUserTeam(userName), oldJobName, newJobName);
     }
 
     public void renameJob(Team team, String oldJobName, String newJobName) throws IOException {
         if (team != null) {
             team.renameJob(oldJobName, newJobName);
             save();
         }
     }
 
     /**
      * Save the settings to the configuration file.
      */
     @Override
     public synchronized void save() throws IOException {
         if (useBulkSaveFlag && BulkChange.contains(this)) {
             return;
         }
         getConfigFile().write(this);
         if (useBulkSaveFlag) {
             SaveableListener.fireOnChange(this, getConfigFile());
         }
     }
 
     /**
      * Get the current user team qualified Id for the job name
      *
      * @param jobName
      * @return String, Qualified Job ID
      */
     public String getTeamQualifiedJobName(String jobName) {
         Team team = findCurrentUserTeam();
         if (team != null) {
             return getTeamQualifiedJobName(team, jobName);
         }
         return jobName;
     }
 
     /**
      * Get the current user team qualified Id for the job name
      *
      * @param team
      * @param jobName
      * @return String, Team qualified Job ID
      */
     public String getTeamQualifiedJobName(Team team, String jobName) {
         String teamName = team.getName();
         if (Team.PUBLIC_TEAM_NAME.equals(teamName)) {
             return jobName;
         }
         StringBuilder sb = new StringBuilder(teamName + TEAM_SEPARATOR + jobName);
         // Make sure the name is unique
         Hudson h = Hudson.getInstance();
         int postfix = 2;
         int baseLength = sb.length();
         while (true) {
             String qualifiedName = sb.toString();
             if (findJobOwnerTeam(qualifiedName) == null) {
                 break;
             }
             sb.setLength(baseLength);
             sb.append("_" + postfix++);
         }
         return sb.toString();
     }
 
     /**
      * Get the part of job name that is unique within the team. That is, given
      * <team-name>.<job-part> return job part if the job is already in
      * <team-name> or <team-name> is the current user team and team is not the
      * public team. Otherwise, return jobName.
      *
      * @param jobName
      * @return
      */
     public String getUnqualifiedJobName(String jobName) {
         Team team = findJobOwnerTeam(jobName);
         if (team == null) {
             team = findCurrentUserTeam();
         }
         if (team != null) {
             return getUnqualifiedJobName(team, jobName);
         }
         return jobName;
     }
 
     /**
      * The Folder where all the jobs of the team to which this jobName belongs
      * to are stored.
      *
      * <p>This method should be called to determine the jobs folder whether or
      * not team management is enabled, as team manager alone knows where team
      * jobs are.
      *
      * @param jobName
      * @return File, team jobs folder
      */
      
     public File getRootFolderForJob(String jobName) {
         Team team = findJobOwnerTeam(jobName);
         // May be just created job
         if ((team == null) && isTeamManagementEnabled()) {
             team = findCurrentUserTeam();
         }
         if (isPublicTeam(team)) {
             return new File(team.getJobsFolder(hudsonHomeDir), jobName);
         } else {
             return new File(team.getJobsFolder(teamsFolder), jobName);
         }
     }
     /**
      * Get the root folders of all the jobs known to this Team manager
      *
      * @return
      */
     public File[] getJobsRootFolders() {
         List<File> jobsRootFolders = new ArrayList<File>();
         for (Team team : teams) {
             if (isPublicTeam(team)) {
                 jobsRootFolders.addAll(team.getJobsRootFolders(hudsonHomeDir));
             } else {
                 jobsRootFolders.addAll(team.getJobsRootFolders(teamsFolder));
             }
         }
         return jobsRootFolders.toArray(new File[jobsRootFolders.size()]);
     }
 
     public String getCurrentUserTeamName() {
         return findCurrentUserTeam().getName();
     }
 
     public static class TeamNotFoundException extends Exception {
 
         public TeamNotFoundException(String teamName) {
             super("Team " + teamName + " does not exist.");
         }
     }
 
     public static class TeamAlreadyExistsException extends Exception {
 
         public TeamAlreadyExistsException(String teamName) {
             super("Team " + teamName + " already exists.");
         }
     }
 
     void setUseBulkSaveFlag(boolean flag) {
         useBulkSaveFlag = flag;
     }
 
     Team getPublicTeam() {
         try {
             return findTeam(PublicTeam.PUBLIC_TEAM_NAME);
         } catch (TeamNotFoundException ex) {
             // Should never happen. Public team creation is ensured
             ex.printStackTrace();
         }
         return null;
     }
 
     private boolean isAnonymousJob(String jobName) {
         for (Team team : teams) {
             if (team.isJobOwner(jobName)) {
                 // job belongs to another team so has no access
                 return false;
             }
         }
         // Not belong to any team, so has access
         return true;
     }
 
     /**
      * The file where the teams settings are saved.
      */
     private XmlFile getConfigFile() {
         return new XmlFile(xstream, new File(teamsFolder, teamsConfigFileName));
     }
     // This is purely fo unit test. Since Hudson is not fully loaded during
     // test BulkChange saving mode is not available
     private transient boolean useBulkSaveFlag = true;
 
     /**
      * Load the settings from the configuration file
      */
     private void load() {
         XmlFile config = getConfigFile();
         try {
             if (config.exists()) {
                 config.unmarshal(this);
             }
         } catch (IOException e) {
             logger.error("Failed to load " + config, e);
         }
     }
 
     private void ensurePublicTeam() {
         publicTeam = new PublicTeam(this);
         try {
             Team team = findTeam(PublicTeam.PUBLIC_TEAM_NAME);
             teams.remove(team);
         } catch (TeamNotFoundException ex) {
             // It's ok, we are going to remove it any way
         }
         try {
             publicTeam.loadExistingJobs(hudsonHomeDir);
         } catch (IOException ex) {
             logger.error("Failed to load existing jobs", ex);
         }
         teams.add(publicTeam);
     }
 
     boolean isPublicTeam(Team team) {
         return Team.PUBLIC_TEAM_NAME.equals(team.getName());
     }
 
     private void initializeXstream() {
         xstream.alias("teamManager", TeamManager.class);
         xstream.alias("team", Team.class);
         xstream.alias("publicTeam", PublicTeam.class);
     }
 
     public static class ConverterImpl implements Converter {
 
         @Override
         public boolean canConvert(Class type) {
             return type == TeamManager.class;
         }
 
         @Override
         public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
             TeamManager teamManager = (TeamManager) source;
             for (String sid : teamManager.sysAdmins) {
                 writer.startNode("sysAdmin");
                 writer.setValue(sid);
                 writer.endNode();
             }
             for (Team team : teamManager.teams) {
                 writer.startNode("team");
                 context.convertAnother(team);
                 writer.endNode();
             }
         }
 
         @Override
         public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
             TeamManager teamManager = (TeamManager) uc.currentObject();
             while (reader.hasMoreChildren()) {
                 reader.moveDown();
                 if ("sysAdmin".equals(reader.getNodeName())) {
                     teamManager.sysAdmins.add(reader.getValue());
                 } else if ("team".equals(reader.getNodeName())) {
                     Team team = (Team) uc.convertAnother(teamManager, Team.class);
                     teamManager.teams.add(team);
                 }
                 reader.moveUp();
             }
             return teamManager;
         }
     }
 }
