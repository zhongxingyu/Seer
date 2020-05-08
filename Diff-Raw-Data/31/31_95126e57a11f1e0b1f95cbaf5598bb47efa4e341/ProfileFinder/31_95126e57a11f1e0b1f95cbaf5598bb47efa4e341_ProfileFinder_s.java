 package org.jenkinsci.plugins.jobprofiles;
 
 import jenkins.model.Jenkins;
 import net.oneandone.sushi.fs.World;
 import org.apache.maven.project.MavenProject;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.*;
 
 public class ProfileFinder {
 
 
     public static ProfileFinder find(World world, PrintStream log, Scm projectScm, Scm profileScm, String forcedProfile) throws IOException {
         ProfileFinder finder;
 
         finder = new ProfileFinder(profileScm);
 
         finder.addPossibleProfile(findBuildSystem(projectScm));
         if (finder.possibleProfiles.get(finder.possibleProfiles.size() - 1).equals("maven") && Jenkins.getInstance() != null) {
             finder.addPossibleProfile(findMavenProperty(Context.getMavenProject(projectScm.getPom(), world)));
         }
         finder.addPossibleProfile(findJenkinsFile(projectScm));
         finder.addPossibleProfile(forcedProfile);
         return finder;
     }
 
     public static String findJenkinsFile(Scm scm) throws IOException {
         Properties properties;
         if (scm.findOne(JENKINS_FILE) != null) {
             properties = scm.findOne(JENKINS_FILE).readProperties();
             return properties.getProperty("profile");
         }
         return null;
     }
 
     public static String findBuildSystem(Scm scm) throws IOException {
         Map<String, String> buildSystems;
 
         buildSystems = new HashMap<String, String>();
 
         buildSystems.put("maven", "pom.xml");
         buildSystems.put("composer", "composer.json");
 
         for (Map.Entry<String, String> system : buildSystems.entrySet()) {
             if (scm.findOne(system.getValue()) != null) {
                 return system.getKey();
             }
         }
         return null;
     }
 
 
     private static String findMavenProperty(MavenProject project) throws IOException {
         if (project.getProperties() != null) {
             return project.getProperties().getProperty("jenkins.profile");
         }
         return null;
     }
 
     private final static String STANDARD = "implicit";
     private final static String JENKINS_FILE = ".jenkins";
     public final Scm profileRoot;
 
     public List<String> possibleProfiles;
 
     public ProfileFinder(Scm scm) {
         this.profileRoot = scm;
         this.possibleProfiles = new LinkedList<String>();
         this.possibleProfiles.add(STANDARD);
     }
 
     public void addPossibleProfile(String profile) {
         if (profile != null && !profile.isEmpty()) {
             possibleProfiles.add(profile);
         }
     }
 
 
 }
