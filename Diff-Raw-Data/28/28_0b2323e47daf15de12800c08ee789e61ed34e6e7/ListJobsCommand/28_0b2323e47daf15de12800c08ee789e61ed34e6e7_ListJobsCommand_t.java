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
 
 package hudson.cli;
 
 import hudson.Extension;
 import hudson.XmlFile;
 import static hudson.cli.ListTeamsCommand.Format.XML;
 import hudson.model.AbstractItem;
 import hudson.model.Hudson;
 import hudson.model.Item;
 import hudson.model.Job;
 import hudson.model.TopLevelItem;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 import org.eclipse.hudson.security.HudsonSecurityManager;
 import org.eclipse.hudson.security.team.Team;
 import org.eclipse.hudson.security.team.TeamManager;
 import org.kohsuke.args4j.Option;
 import org.springframework.security.Authentication;
 
 /**
  * List the jobs in Hudson.
  * <p>
  * If team management is enabled, list the jobs by team.
  * 
  * @author Bob Foster
  */
 @Extension
 public class ListJobsCommand extends CLICommand {
     @Override
     public String getShortDescription() {
         return "Lists the jobs in Hudson";
     }
 
     private String getCurrentUser() {
         Authentication authentication = HudsonSecurityManager.getAuthentication();
         return authentication.getName();
     }
 
     enum Format {
         XML, CSV, PLAIN
     }
     @Option(name = "-team", usage = "Team to list; if omitted, all visible teams.")
     public String team;
     @Option(name = "-job", usage = "Fully-qualified job name. The config.xml for the job is returned.")
     public String job;
     @Option(name = "-format", usage = "Controls how the output from this command is printed. Always xml with -job option.")
     public ListTeamsCommand.Format format = ListTeamsCommand.Format.PLAIN;
     
     @Override
     protected int run() throws TeamManager.TeamNotFoundException {
         Team targetTeam = validateReadAccessToTeam(team, stderr);
 
         TeamManager teamManager = Hudson.getInstance().getTeamManager();
         String[] jobs = null;
         
         if (team != null && targetTeam == null) {
             return -1;
         }
 
         if (job != null && targetTeam != null && !targetTeam.isJobOwner(job)) {
             stderr.println("Job "+job+" is not in team "+team);
             return -1;
         }
         
         if (job != null) {
             if (targetTeam == null) {
                 targetTeam = teamManager.findJobOwnerTeam(job);
             }
             if (targetTeam == null) {
                 stderr.println("Job "+job+" does not exist");
                 return -1;
             }
             if (!targetTeam.hasPermission(Item.EXTENDED_READ)) {
                 stderr.println("User does not have permission to read config.xml");
                 return -1;
             }
             TopLevelItem item = Hudson.getInstance().getItem(job);
             if (item instanceof AbstractItem) {
                 XmlFile file = ((AbstractItem)item).getConfigFile();
                 try {
                     file.writeRawTo(stdout);
                 } catch (IOException ex) {
                     stderr.println("Error reading config.xml for job "+job);
                     return -1;
                } finally {
                    stdout.flush();
                 }
             } else {
                 stderr.println("Cannot read config.xml");
                 return -1;
             }
             return 0;
         } else if (targetTeam != null) {
             Set<String> aTeamJobs = targetTeam.getJobNames();
             Arrays.sort(jobs = aTeamJobs.toArray(new String[aTeamJobs.size()]));
         } else {
             // Get items user can READ
             List<TopLevelItem> items = Hudson.getInstance().getItems();
             List<String> itemNames = new ArrayList<String>();
             for (TopLevelItem item : items) {
                 if (item instanceof Job) {
                     itemNames.add(item.getName());
                 }
             }
             Arrays.sort(jobs = itemNames.toArray(new String[itemNames.size()]));
         }
         switch (format) {
             case XML:
                 PrintWriter w = new PrintWriter(stdout);
                 w.println("<jobs>");
                 for (String job : jobs) {
                     w.print("  <job>");
                     w.print(job);
                     w.println("  </job>");
                 }
                 w.println("</jobs>");
                 w.flush();
                 break;
             case CSV:
             case PLAIN:
                 for (String job : jobs) {
                     stdout.println(job);
                 }
                stdout.flush();
                 break;
         }
         return 0;
     }
     
     public static Team validateReadAccessToTeam(String team, PrintStream stderr) {
         Hudson h = Hudson.getInstance();
         TeamManager teamManager = h.getTeamManager();
         Team targetTeam = null;
         if (team != null) {
             if (!teamManager.isTeamManagementEnabled()) {
                 stderr.println("team may not be specified unless team management is enabled");
             } else {
                 try {
                     // check team exists first for better error message
                     targetTeam = teamManager.findTeam(team);
                     if (!team.equals(Team.PUBLIC_TEAM_NAME) && !teamManager.getCurrentUserTeams().contains(team)) {
                         stderr.println("Current user does not have read access to team "+team);
                         targetTeam = null;
                     }
                 } catch (TeamManager.TeamNotFoundException e) {
                     stderr.println("Team "+team+" does not exist");
                 }
             }
         }
         return targetTeam;
     }
 
     public static String rtrim(String s) {
         int i = s.length()-1;
         while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
             i--;
         }
         return s.substring(0, i+1);
     }
 }
