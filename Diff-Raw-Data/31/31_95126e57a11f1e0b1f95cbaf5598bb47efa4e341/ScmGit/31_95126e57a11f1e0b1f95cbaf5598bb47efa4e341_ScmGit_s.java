 package org.jenkinsci.plugins.jobprofiles;
 
 
 import lombok.extern.slf4j.Slf4j;
 import net.oneandone.sushi.fs.FileNotFoundException;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.NodeInstantiationException;
 import net.oneandone.sushi.fs.World;
 import net.oneandone.sushi.fs.file.FileNode;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.errors.GitAPIException;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Slf4j
 public class ScmGit extends Scm {
 
     public static Scm create(World world, String uri) {
         FileNode localpath;
         try {
             localpath = world.getTemp().createTempDirectory();
             Git.cloneRepository()
                     .setDirectory(new File(localpath.getAbsolute()))
                     .setURI(uri)
                     .call();
             return new ScmGit(localpath);
         } catch (GitAPIException e) {
             throw new JobProfileException(e.getMessage(), e);
         } catch (IOException e) {
             throw new JobProfileException("Could not create temp directory.", e);
         }
 
     }
 
     public final FileNode localpath;
 
     public ScmGit(FileNode localpath) {
         this.localpath = localpath;
     }
 
     public String getPom() throws IOException {
         try {
             return localpath.findOne("pom.xml").readString();
         } catch (FileNotFoundException e) {
             return null;
         }
 
     }
 
     public Map<String, String> getProfile(String name, PrintStream log) throws IOException {
         HashMap<String, String> profileMap;
         Node theProfile;
 
         profileMap = new HashMap<String, String>();
         try {
             theProfile = localpath.findOne(name);
         } catch (FileNotFoundException e) {
             log.println(String.format("[Error] Profile %s not found. I'll going to continue with next Asset", name));
             return profileMap;
         }
 
         for (Node profileNode : theProfile.list()) {
 
             profileMap.put(profileNode.getName(), profileNode.readString());
 
         }
         return profileMap;
     }
 
     public List<Node> find(String seachString) {
         try {
 
             return localpath.find(seachString);
 
         } catch (NodeInstantiationException e) {
             throw new JobProfileException(e.getMessage(), e);
         } catch (IOException e) {
             throw new JobProfileException(e.getMessage(), e);
         }
     }
 
     public Node findOne(String seachString) {
         try {
 
             return localpath.findOne(seachString);
 
         } catch (NodeInstantiationException e) {
             return null;
         } catch (IOException e) {
             return null;
         }
     }
 }
