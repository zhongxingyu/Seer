 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * Creates a new bare git repository
  *
  * @author Martin Gajdos
  */
 public class GitRepository {
     private String repositoryDirectory;
 
     public static void main(String[] args) {
        if (args[0] != null) {
             String repositoryName;
             // if the provided repository name doesn't yet include the ending '.git', add it now
             repositoryName = (args[0].indexOf(".git") == -1) ? args[0] + ".git" : args[0];
 
             System.out.println("Creating new repository: " + repositoryName);
             GitRepository gitRepository = new GitRepository(repositoryName);
         } else {
             System.err.println("Wrong number of arguments: Please provide a repository name!");
         }
 
     }
 
     GitRepository(String repositoryDirectory) {
         // initializes the new bare repository
         this.repositoryDirectory = repositoryDirectory + "/";
 
         mkdir(repositoryDirectory);
 
         mkdirs("refs/heads");
         mkdirs("refs/tags");
         mkdirs("objects/info");
         mkdirs("objects/pack");
         mkdirs("branches");
 
         addFileWithContent("description", "Unnamed repository; edit this file to name it for gitweb.");
         addFileWithContent("HEAD", "ref: refs/heads/master\n");
 
         mkdir("hooks");
         addFileWithContent("hooks/applypatch-msg", "# add shell script and make executable to enable");
         addFileWithContent("hooks/post-commit", "# add shell script and make executable to enable");
         addFileWithContent("hooks/post-receive", "# add shell script and make executable to enable");
         addFileWithContent("hooks/post-update", "# add shell script and make executable to enable");
         addFileWithContent("hooks/pre-applypatch", "# add shell script and make executable to enable");
         addFileWithContent("hooks/pre-commit", "# add shell script and make executable to enable");
         addFileWithContent("hooks/pre-rebase", "# add shell script and make executable to enable");
         addFileWithContent("hooks/update", "# add shell script and make executable to enable");
 
         mkdir("info");
         addFileWithContent("info/exclude", "# *.[oa]\n# *~");
 
         String config = "[core]\n\trepositoryformatversion = 0\n\tfilemode = true\n\tbare = true\n\tlogallrefupdates = true";
         addFileWithContent("config", config);
     }
 
     private void addFileWithContent(String fileName, String content) {
         try {
             if (content == null)
                 throw new Exception();
 
             // Create file
             System.out.println("About to create: " + this.repositoryDirectory + fileName);
             FileWriter fstream = new FileWriter(this.repositoryDirectory + fileName);
             BufferedWriter out = new BufferedWriter(fstream);
             out.write(content);
 
             out.close();
         } catch (Exception e) {//Catch exception if any
             System.err.println("Error: " + e.getMessage());
         }
     }
 
     private void mkdir(String directoryName) {
         boolean success = (new File(this.repositoryDirectory + directoryName)).mkdir();
         if (success)
             System.out.println("Directory: " + this.repositoryDirectory + directoryName + " created");
     }
 
     private void mkdirs(String directoryNames) {
         try {
             // Create multiple directories
             boolean success = (new File(this.repositoryDirectory + directoryNames)).mkdirs();
             if (success) {
                 System.out.println("Directories: " + this.repositoryDirectory + directoryNames + " created");
             }
 
         } catch (Exception e) {//Catch exception if any
             System.err.println("Error: " + e.getMessage());
         }
     }
 }
