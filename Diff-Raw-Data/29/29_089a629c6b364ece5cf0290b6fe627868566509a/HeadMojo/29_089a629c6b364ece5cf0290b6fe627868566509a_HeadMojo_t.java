 package de.cubeisland.maven.plugins.githead.mojo;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * This mojo adds the git head hash as a variable
  *
  * @goal head
  * @phase process-sources
  * @threadSafe
  */
 public class HeadMojo extends AbstractMojo
 {
     /**
      * @parameter default-value="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * @parameter expression="${githead.repoLocation}"
      */
     public File repoLocation = new File(".");
 
     /**
      * @parameter
      */
     public boolean searchParentDirectories = true;
 
     /**
      * @parameter expression="${githead.defaultBranch}"
      */
     public String defaultBranch = "unknown";
 
     /**
      * @parameter expression="${githead.defaultCommit}"
      */
     public String defaultCommit = "unknown";
 
     private final String HEAD_FILE_NAME = "HEAD";
     private final String HEAD_REF_PREFIX = "ref:";
     private final String GIT_FOLDER = ".git";
 
     public void execute() throws MojoExecutionException, MojoFailureException
     {
        File repo = this.repoLocation.getAbsoluteFile();
        try
        {
            repo = repo.getCanonicalFile();
        }
        catch (IOException ignored)
        {}
         File tmp;
         do
         {
             tmp = new File(repo, GIT_FOLDER);
             if (tmp.exists() && tmp.isDirectory())
             {
                 break;
             }
         }
         while (this.searchParentDirectories && (repo = repo.getParentFile()) != null);
 
         if (repo == null || !repo.exists() || !repo.isDirectory())
         {
            throw new MojoFailureException("No valid git repository found! Started from: " + this.repoLocation.getAbsolutePath());
         }
 
         String branch = null;
         String commit = null;
 
        String ref = this.readRef(repo);
         if (ref != null)
         {
             branch = refToBranch(ref);
            commit = readHeadCommit(repo, ref);
         }
 
         if (branch == null)
         {
             branch = this.defaultBranch;
         }
         if (commit == null)
         {
             commit = this.defaultCommit;
         }
 
         Properties properties = this.project.getProperties();
         properties.put("githead.branch", branch);
         properties.put("githead.commit", commit);
 
         getLog().info("Found: " + branch + "/" + commit);
     }
 
     private static String refToBranch(String ref)
     {
         String[] parts = ref.split("/");
         return parts[parts.length - 1];
     }
 
    private String readHeadCommit(File repo, String ref)
     {
        File refFile = new File(repo, GIT_FOLDER + File.separator + ref);
 
         BufferedReader reader = null;
         String commitHash = null;
         try
         {
             reader = new BufferedReader(new FileReader(refFile));
 
             while ((commitHash = reader.readLine()) != null)
             {
                 commitHash = commitHash.trim();
                 if (!commitHash.isEmpty())
                 {
                     break;
                 }
             }
         }
         catch (FileNotFoundException e)
         {
             getLog().warn("The file " + refFile.getPath() + " could not be found!", e);
         }
         catch (IOException e)
         {
             getLog().warn("Failed to read the ref file", e);
         }
         finally
         {
             if (reader != null)
             {
                 try
                 {
                     reader.close();
                 }
                 catch (IOException ignored)
                 {}
             }
         }
         return commitHash;
     }
 
    private String readRef(File repo)
     {
        File headFile = new File(repo, GIT_FOLDER + File.separator + HEAD_FILE_NAME);
 
         BufferedReader reader = null;
         String refPath = null;
         try
         {
             reader = new BufferedReader(new FileReader(headFile));
 
             String line;
             while ((line = reader.readLine()) != null)
             {
                 line = line.trim();
                 if (line.toLowerCase().startsWith(HEAD_REF_PREFIX))
                 {
                     refPath = line.substring(HEAD_REF_PREFIX.length()).trim();
                     break;
                 }
             }
 
         }
         catch (FileNotFoundException e)
         {
             getLog().warn("The file " + headFile.getPath() + " could not be found!", e);
         }
         catch (IOException e)
         {
             getLog().warn("Failed to read the " + HEAD_FILE_NAME + " file", e);
         }
         finally
         {
             if (reader != null)
             {
                 try
                 {
                     reader.close();
                 }
                 catch (IOException ignored)
                 {}
             }
         }
         return refPath;
     }
 }
