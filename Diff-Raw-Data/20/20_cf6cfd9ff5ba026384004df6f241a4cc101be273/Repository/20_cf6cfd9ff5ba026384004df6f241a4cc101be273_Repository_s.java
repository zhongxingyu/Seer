 package net.sf.alchim.spoon.contrib.launcher.artifact;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 
 /**
  * A repository that use local file system to store/cache artifact's files (jar,
  * pom, md5). If remote repository url are added, then it will download the
  * artifact's files from remotes repositories (first found) into its local file
  * system.
  *
  * It's a very basic implementation, but enough for lot of case.
  *
  * <br>
  * The basic usage is :
  *
  * <pre>
  *    Repository repo = new Repository(); // use the m2 repository as default
  *    ...
  *    Artifact artifact = new Artifact(...);
  *    repo.load(artifact);
  *    ...
  *    artifact.getJarURL();
  * </pre>
  *
  * @author dwayne
  *
  */
 public class Repository {
     private final File localRoot_;
 
     private final List<URL> remoteRoots_;
 
     public Repository() throws Exception {
         this(null);
     }
 
     public Repository(File localroot) throws Exception {
         if (localroot == null) {
             localroot = searchLocalM2Repository();
         }
         if (localroot.exists()) {
             if (!localroot.isDirectory()) {
                 throw new IllegalStateException(localroot + " exists and is not a directory !");
             }
         } else {
             localroot.mkdirs();
         }
         localRoot_ = localroot;
         remoteRoots_ = new ArrayList<URL>();
     }
 
     private File searchLocalM2Repository() throws Exception {
         String back = null;
         File m2settings = new File(System.getProperty("user.home"), ".m2/settings.xml");
         if (m2settings.exists()) {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document document = builder.parse(m2settings);
             back = DomHelper.findFirstValue(document, "localRepository");
         }
         if (back == null) {
             back = System.getProperty("user.home") + "/.m2/repository";
         }
         return new File(back);
     }
 
     public void addRemoteRoot(URL root) throws Exception {
         remoteRoots_.add(root);
     }
 
     public void addRemoteRoot(List<URL> roots) throws Exception {
         remoteRoots_.addAll(roots);
     }
 
     private String buildRPath(Artifact artifact) throws Exception {
         return artifact.getGroupId().replace('.', '/') + '/' + artifact.getArtifactId().replace('.', '/') + '/' + artifact.getVersion() + '/';
     }
 
     private File buildLocalPath(Artifact artifact) throws Exception {
         return new File(localRoot_, buildRPath(artifact));
     }
 
     private String buildFilename(Artifact artifact) throws Exception {
         return artifact.getArtifactId() + '-' + artifact.getVersion();
     }
 
     public void load(Artifact artifact) throws Exception {
         File dir = buildLocalPath(artifact);
         dir.mkdirs();
         String basename = buildFilename(artifact);
         try {
             artifact.repository = this;
             artifact.jar = new File(dir, basename + ".jar");
             artifact.pom = new Pom(new File(dir, basename + ".pom"));
             artifact.md5 = new File(dir, basename + ".jar.md5");
             if (needDownload(artifact)) {
                 download(artifact);
             }
         } catch (Exception exc) {
             artifact.jar = null;
             artifact.pom = null;
             artifact.md5 = null;
             dir.delete();
             throw exc;
         }
     }
 
     private boolean needDownload(Artifact artifact) throws Exception {
         boolean back = !artifact.getVersion().endsWith("-SNAPSHOT");
         back = back && artifact.pom.getFile().exists();
         if (isRelocated(artifact)) {
             load(artifact);
             return false;
         }
         back = back && artifact.jar.exists();
         if (artifact.md5.exists()) {
            back = back && MD5Helper.checksum(artifact.jar, artifact.md5);
         }
         return back;
     }
 
     private void download(Artifact artifact) throws Exception {
         if (remoteRoots_ != null) {
             for(URL remoteRoot : remoteRoots_) {
                 URL url = new URL(remoteRoot, buildRPath(artifact));
                 if (UrlHelper.exists(url)) {
                     download(url, artifact);
                 }
             }
         }
     }
 
     private void download(URL url, Artifact artifact) throws Exception {
         UrlHelper.download(new URL(url, artifact.pom.getFile().getName()), artifact.pom.getFile());
         if (isRelocated(artifact)) {
             load(artifact);
             return;
         }
         UrlHelper.download(new URL(url, artifact.md5.getName()), artifact.md5);
         UrlHelper.download(new URL(url, artifact.jar.getName()), artifact.jar);
         if (!MD5Helper.checksum(artifact.jar, artifact.md5)) {
             throw new IllegalStateException("md5 doesn't match for " + artifact.jar);
         }
     }
 
     private boolean isRelocated(Artifact artifact) throws Exception {
         String newGroupId = artifact.pom.findRelocation();
         if ((newGroupId != null) && !artifact.getGroupId().equals(newGroupId)) {
             artifact.relocate(newGroupId);
             return true;
         }
         return false;
     }
 }
