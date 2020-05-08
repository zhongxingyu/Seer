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
 import java.io.File;
 import org.eclipse.hudson.security.team.TeamManager;
 import org.eclipse.hudson.security.team.TeamManager.TeamAlreadyExistsException;
 import org.kohsuke.args4j.Argument;
 
 /**
  *
  * @author Bob Foster
  */
 @Extension
 public class CreateTeamCommand extends CLICommand {
 
     @Override
     public String getShortDescription() {
         return "Create a new team";
     }
     @Argument(metaVar = "TEAM", usage = "Team to create", required=true)
     public String team;
     @Argument(metaVar = "DESCRIPTION", usage = "Team Description", index=1, required=false)
     public String description;
     @Argument(metaVar = "LOCATION", usage = "Team location (custom folder)", index=2, required=false)
     public String location;
 
     protected int run() throws Exception {
         Hudson h = Hudson.getInstance();
         
         if (!h.isTeamManagementEnabled()) {
             stderr.println("Team management is not enabled");
             return -1;
         }
         
         TeamManager teamManager = h.getTeamManager();
         
         if (!teamManager.isCurrentUserSysAdmin()) {
             stderr.println("User not authorized to create team");
             return -1;
         }
         
         for (int i = 0; i < team.length(); i++) {
             if (!isAlphaNumeric(team.charAt(i))) {
                 stderr.println("Only Alpha-Numeric characters allowed in team name");
                 return -1;
             }
         }
         
         if (description == null) {
             description = team;
         }
         
         if (location != null) {
             File loc = new File(location);
             if (loc.exists() && !loc.isDirectory()) {
                 stderr.println("Custom folder name \""+location+"\" is not a directory");
                 return -1;
             }
             if (!loc.exists() && !loc.mkdirs()) {
                 stderr.println("Could not create custom folder \""+location+"\"");
                 return -1;
             }
         }
         
         try {
             teamManager.createTeam(team, description, location);
         } catch (TeamAlreadyExistsException e) {
             stderr.println("Team "+team+" already exists");
             return -1;
         }
         
         return 0;
     }
     
     private boolean isAlphaNumeric(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
     }
 }
