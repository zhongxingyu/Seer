 package org.jenkinsci.plugins.jobprofiles;
 
 import com.google.common.collect.Lists;
 import net.oneandone.sushi.fs.World;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Map;
 
 public class ProfileManager {
     private final Scm profileScm;
     private final PrintStream log;
     private World world;
     private ProfileFinder finder;
 
     public ProfileManager(World world, PrintStream log, String profileRoot) {
         this.profileScm = Scm.get(profileRoot, world);
         this.world = world;
         this.log = log;
     }
 
     public ProfileManager discover(Scm projectSCM, String forcedProfile) throws IOException {
         finder = ProfileFinder.find(world, log, projectSCM, this.profileScm, forcedProfile);
         return this;
     }
 
     public String find() throws IOException {
         return finder.possibleProfiles.get(finder.possibleProfiles.size() - 1);
     }
 
    public Map<String, String> getProfile() throws IOException {
         for (String profile : Lists.reverse(finder.possibleProfiles)) {
             if (finder.profileRoot.profileExists(profile, log)) {
                 return finder.profileRoot.getProfile(profile, log);
             }
         }
         return null;
     }
 }
