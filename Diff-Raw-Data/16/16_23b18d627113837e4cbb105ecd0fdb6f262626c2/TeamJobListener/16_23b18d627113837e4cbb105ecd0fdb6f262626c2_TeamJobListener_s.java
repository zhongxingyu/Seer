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
 
 import hudson.Extension;
 import hudson.model.Hudson;
 import hudson.model.Item;
 import hudson.model.Job;
 import hudson.model.listeners.ItemListener;
 import java.io.IOException;
 import org.eclipse.hudson.security.team.TeamManager.TeamNotFoundException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A simple helper to listen to job creation, deletion and renaming and
  * accordingly add, delete or rename it in the current users team.
  *
  * @since 3.1.0
  * @author Winston Prakash
  */
 @Extension
 public class TeamJobListener extends ItemListener {
 
     private transient Logger logger = LoggerFactory.getLogger(TeamJobListener.class);
 
     @Override
     public void onCreated(Item item) {
        // If team management enabled, job was created in correct team
         if (item instanceof Job<?, ?>) {
             if (!Hudson.getInstance().isTeamManagementEnabled()) {
                 addToPublicTeam(item.getName());
             } else if (getTeamManager().findJobOwnerTeam(item.getName()) == null) {
                 addToCurrentUserTeam(item.getName());
             }
         }
     }
 
     @Override
     public void onRenamed(Item item, String oldJobName, String newJobName) {
         if (item instanceof Job<?, ?>) {
             removeJob(oldJobName);
            // If team management enabled, job has been added to correct team.
             if (!Hudson.getInstance().isTeamManagementEnabled()) {
                 addToPublicTeam(newJobName);
             } else if (getTeamManager().findJobOwnerTeam(newJobName) == null) {
                 addToCurrentUserTeam(newJobName);
             }
         }
     }
 
     @Override
     public void onDeleted(Item item) {
         if (item instanceof Job<?, ?>) {
             removeJob(item.getName());
         }
     }
     
     private void logFailedToAdd(String jobName, String teamName, Exception ex) {
         logger.error("Failed to add job "+jobName+" to "+teamName+" team", ex);
     }
     
     private void addToCurrentUserTeam(String jobName) {
         try {
             getTeamManager().addJobToCurrentUserTeam(jobName);
             // Log because this case shouldn't occur - could be a bug
             logger.info("Job "+jobName+" added to team "+getTeamManager().findJobOwnerTeam(jobName).getName());
         } catch (IOException ex) {
             logFailedToAdd(jobName, "current user", ex);
         } catch (TeamNotFoundException ex) {
             logFailedToAdd(jobName, "current user", ex);
         }
     }
     
     private void addToPublicTeam(String jobName) {
         Team publicTeam = getTeamManager().getPublicTeam();
         try {
             publicTeam.addJob(new TeamJob(jobName));
         } catch (IOException ex) {
             logFailedToAdd(jobName, "public", ex);
         }
     }
     
     private void removeJob(String jobName) {
         Team team = getTeamManager().findJobOwnerTeam(jobName);
         if (team != null) {
             try {
                 team.removeJob(jobName);
             } catch (IOException ex) {
                 logger.error("Failed to remove job "+jobName+" in team"+team.getName(), ex);
             }
         }
     }
 
     private TeamManager getTeamManager() {
         return Hudson.getInstance().getTeamManager();
     }
 }
