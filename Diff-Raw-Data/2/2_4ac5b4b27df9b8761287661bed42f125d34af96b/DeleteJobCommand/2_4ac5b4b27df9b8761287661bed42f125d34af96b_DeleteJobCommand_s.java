 /*
  * Copyright (c) 2013 Hudson.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Hudson - initial API and implementation and/or initial documentation
  */
 
 package org.hudsonci.plugins.team.cli;
 
 import hudson.Extension;
 import hudson.cli.CLICommand;
 import hudson.model.Hudson;
 import hudson.model.Job;
 import hudson.model.TopLevelItem;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.eclipse.hudson.security.team.Team;
 import org.eclipse.hudson.security.team.TeamManager;
 import org.eclipse.hudson.security.team.TeamManager.TeamNotFoundException;
 import org.kohsuke.args4j.Argument;
 
 /**
  * Delete one or more jobs matching a regex pattern. The pattern is
  * implicitly bounded by ^ and $, and thus must match the entire string.
  * For example, <code>".*Job.*"</code> deletes all jobs that contain
  * the substring <code>"Job"</code>, while <code>"Job"</code> can
  * delete only a job named <code>"Job"</code>.
  * 
  * <p>The team option allows deleting jobs only within a single team.
  * E.g., to delete all jobs in team MyTeam, <code>delete-job ".*" MyTeam</code>.
  * This is more reliable than deleting based on the team name prefix
  * in the job name, because '.' is not a reserved character in job
  * names. There may be a job <code>A.Job</code> that is not in team <code>A</code>.
  * @author Bob Foster
  */
 @Extension
 public class DeleteJobCommand extends CLICommand {
 
     @Override
     public String getShortDescription() {
         return "Delete one or more jobs";
     }
 
     @Argument(metaVar = "JOB", usage = "Job(s) to delete", required=true)
     public String job;
    @Argument(metaVar = "TEAM", usage = "Only within team", index = 1, required=false)
     public String team;
     
     @Override
     protected int run() throws Exception {
         
         if (!job.startsWith("^")) {
             job = "^" + job;
         }
         if (!job.endsWith("$")) {
             job += "$";
         }
         Pattern pattern = Pattern.compile(job);
         
         Hudson h = Hudson.getInstance();
             
         if (team != null) {
             if (!h.isTeamManagementEnabled()) {
                 stderr.println("Team management is not enabled");
                 return -1;
             }
             TeamManager teamManager = h.getTeamManager();
             
             try {
                 Team targetTeam = teamManager.findTeam(team);
                 Set<String> jobs = targetTeam.getJobNames();
                 for (String jobName : jobs) {
                     deleteMatchingJob(pattern, jobName);
                 }
             } catch (TeamNotFoundException e) {
                 stderr.println("Team "+team+" not found");
                 return -1;
             }
         } else {
             for (TopLevelItem item : h.getItems()) {
                 if (item instanceof Job) {
                     deleteMatchingJob(pattern, item.getName());
                 }
             }
         }
         return 0;
     }
     
     private void deleteMatchingJob(Pattern pattern, String jobName) throws Exception {
         Matcher matcher = pattern.matcher(jobName);
         if (matcher.find()) {
             TopLevelItem item = Hudson.getInstance().getItem(jobName);
             item.delete();
         }
     }
 
 }
