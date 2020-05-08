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
 import hudson.model.Hudson;
 import hudson.util.QuotedStringTokenizer;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.Collection;
 import org.eclipse.hudson.security.HudsonSecurityManager;
 import org.eclipse.hudson.security.team.TeamManager;
 import org.kohsuke.args4j.Option;
 import org.springframework.security.Authentication;
 
 /**
  * Lists the teams and READ or CREATE job permissions of the current user.
  * @author Bob Foster
  * @since 3.1.0
  */
 @Extension
 public class ListTeamsCommand extends CLICommand {
     @Override
     public String getShortDescription() {
         return "Lists the teams and READ or CREATE job permissions of the current user";
     }
 
     private String getCurrentUser() {
         Authentication authentication = HudsonSecurityManager.getAuthentication();
         return authentication.getName();
     }
 
     enum Format {
         XML, CSV, PLAIN
     }
     @Option(name = "-format", usage = "Controls how the output from this command is printed.")
     public Format format = Format.PLAIN;
     @Option(name = "-u", aliases = {"--users"}, usage = "Users to report. \"a,b\" for users a and b. \"*\" for all. Available only to admins.")
     public String users = null;
 
     TeamManager teamManager;
     
     @Override
     protected int run() throws TeamManager.TeamNotFoundException {
         teamManager = Hudson.getInstance().getTeamManager();
         String[] teams = new String[0];
         String[] userArray = null;
         String[] adminTeams = null;
         if (teamManager.isTeamManagementEnabled()) {
             Collection<String> currentUserTeams = teamManager.getCurrentUserVisibleTeams();
             Arrays.sort(teams = currentUserTeams.toArray(new String[currentUserTeams.size()]));
             if (users != null) {
                 userArray = QuotedStringTokenizer.tokenize(users, ",");
                 for (String user : userArray) {
                     if ("*".equals(user)) {
                         userArray = new String[] {"*"};
                         break;
                     }
                 }
                 // NB: Users administered by current user must be in teams visible to current user
                 // but not necessarily all those teams are administered by the current user.
                 Collection<String> usersAdministered = teamManager.getCurrentUserAdminUsers();
                 if (userArray.length == 1 && "*".equals(userArray[0])) {
                     // all users administered by current user
                     userArray = usersAdministered.toArray(new String[usersAdministered.size()]);
                 } else {
                     for (String user : userArray) {
                         if (!usersAdministered.contains(user)) {
                             throw new IllegalArgumentException("User "+user+" is not in a team administered by current user");
                         }
                     }
                 }
                 Arrays.sort(userArray);
 
                 Collection<String> cuAdminTeams = teamManager.getCurrentUserAdminTeams();
                 adminTeams = cuAdminTeams.toArray(new String[cuAdminTeams.size()]);
                 Arrays.sort(adminTeams);
             }
         }
         switch (format) {
             case XML:
                 PrintWriter w = new PrintWriter(stdout);
                 if (userArray != null) {
                     w.println("<users>");
                     for (String user : userArray) {
                         w.println("  <user>");
                         w.print("    <name>");
                         w.print(user);
                         w.println("</name>");
                         w.println("    <teams>");
                         for (String team : adminTeams) {
                             if (teamManager.isUserHasAccessToTeam(user, team)) {
                                 printTeamXml(w, user, team, "      ");
                             }
                         }
                         w.println("    </teams>");
                         w.println("  </user>");
                     }
                     w.println("</users>");
                 } else {
                     Authentication authentication = HudsonSecurityManager.getAuthentication();
                     w.println("<teams>");
                     for (String team : teams) {
                         printTeamXml(w, authentication.getName(), team, "  ");
                     }
                     w.println("</teams>");
                 }
                 w.flush();
                 break;
             case CSV:
                 if (userArray != null) {
                     stdout.printf("User,Team,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", TeamManager.ALL_TEAM_PERMISSIONS);
                     for (String user : userArray) {
                         for (String team : adminTeams) {
                             if (teamManager.isUserHasAccessToTeam(user, team)) {
                                 stdout.printf(user+","+team+",%s,%s,%s,%s,%s,%s,%s,%s\n",
                                     convertToXArray(teamManager.getUserTeamPermissions(user, team)));
                             }
                         }
                     }
                 } else {
                     stdout.printf("Team,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", TeamManager.ALL_TEAM_PERMISSIONS);
                     for (String team : teams) {
                         stdout.printf(team+",%s,%s,%s,%s,%s,%s,%s,%s\n",
                             convertToXArray(teamManager.getCurrentUserTeamPermissions(team)));
                     }
                 }
                 break;
             case PLAIN:
                 if (userArray != null) {
                     int bigTeam = findBig(adminTeams);
                     int bigUser = findBig(userArray);
                     for (String user : userArray) {
                         for (String team : adminTeams) {
                             if (teamManager.isUserHasAccessToTeam(user, team)) {
                                 printPlain(user, team, bigUser, bigTeam);
                             }
                         }
                     }
                 } else {
                     int bigTeam = findBig(teams);
                     for (String team : teams) {
                         printPlain(getCurrentUser(), team, 0, bigTeam);
                     }
                 }
                 break;
         }
         return 0;
     }
     
     private int findBig(String[] sa) {
         int big = 0;
         for (String s : sa) {
             if (big < s.length()) {
                 big = s.length();
             }
         }
         return big;
     }
     
     private void printPlain(String user, String team, int bigUser, int bigTeam) throws TeamManager.TeamNotFoundException {
         if (bigUser > 0) {
             stdout.print(user);
             pad(stdout, bigUser-user.length()+1);
         }
         stdout.print(team);
         pad(stdout, bigTeam-team.length()+2);
         String[] permissions = teamManager.getUserTeamPermissions(user, team);
         for (int i = 0; i < permissions.length; i++) {
             if (i == permissions.length - 1) {
                 stdout.println(permissions[i]);
             } else {
                 stdout.print(permissions[i]);
                 stdout.print(" ");
             }
         }
     }
     
     private void printTeamXml(PrintWriter w, String user, String team, String indent) throws TeamManager.TeamNotFoundException {
         w.print(indent);
         w.println("<team>");
         w.print(indent);
         w.print("  <name>");
         w.print(team);
         w.println("</name>");
         String[] permissions = teamManager.getUserTeamPermissions(user, team);
         if (permissions.length > 0) {
             w.print(indent);
             w.println("  <permissions>");
             for (String permission : permissions) {
                 w.print(indent);
                 w.print("    <permission>");
                 w.print(permission);
                 w.println("</permission>");
             }
             w.print(indent);
             w.println("  </permissions>");
         }
         w.print(indent);
         w.println("</team>");
     }
 
     private String[] convertToXArray(String[] currentUserTeamPermissions) {
         String[] allPermissions = TeamManager.ALL_TEAM_PERMISSIONS;
         // Both arrays are sorted, so it's just a merge
         String[] xarray = new String[allPermissions.length];
         int currentIndex = 0;
         for (int i = 0; i < allPermissions.length; i++) {
             if (currentIndex < currentUserTeamPermissions.length
                     && allPermissions[i].equals(currentUserTeamPermissions[currentIndex])) {
                 xarray[i] = "X";
                 currentIndex++;
             } else {
                 xarray[i] = "-";
             }
         }
         return xarray;
     }
 
     private void pad(PrintStream out, int n) {
         while (n-- > 0) {
             out.print(" ");
         }
     }
     
 }
