 package net.anzix.snipsnap2xwiki.migrator;
 
 import net.anzix.snipsnap2xwiki.MigrationContext;
 import net.anzix.snipsnap2xwiki.Page;
 import org.eclipse.jgit.api.AddCommand;
 import org.eclipse.jgit.api.CommitCommand;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.ResetCommand;
 import org.eclipse.jgit.lib.PersonIdent;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
 
 import java.io.File;
 import java.util.Date;
 import java.util.TimeZone;
 
 /**
  * Commit files to a git repository with the right author and author date.
  */
 public class GitOutputter implements Outputter {
     private MigrationContext context;
     private File rootDir;
     private Repository repo;
     private Git git;
     private DirOutputter dirOutput;
     private int i = 10;
 
 
     public GitOutputter(MigrationContext context) {
         this.rootDir = context.getOutputDir();
         dirOutput = new DirOutputter(context);
         this.context = context;
         try {
             FileRepositoryBuilder builder = new FileRepositoryBuilder();
             repo = builder.setGitDir(new File(rootDir, ".git"))
                     .setWorkTree(rootDir)
                     .readEnvironment() // scan environment GIT_* variables
 //                    .findGitDir(new File("c:/tmp/sp/jh2"))
                     .build();
             git = new Git(repo);
         } catch (Exception ex) {
             throw new RuntimeException("Can't initialize git repository", ex);
         }
 
     }
 
     public GitOutputter() {
 
     }
 
     public File writeToDir(Page page) {
         File output = dirOutput.getFile(page);
         if (!output.getParentFile().exists()) {
             output.getParentFile().mkdirs();
         }
         page.write(output);
         context.pageMigratedSuccesfully(page.getName());
         return output;
     }
 
     /**
      * Save page and commit to the repository.
      *
      * @param page
      * @param first
      * @throws Exception
      */
     public void commitVersion(Page page, boolean first) throws Exception {
         File f = writeToDir(page);
 
         AddCommand a = git.add();
         String relPath = rootDir.toURI().relativize(f.toURI()).getPath();
         a.addFilepattern(relPath);
         a.call();
 
         CommitCommand c = git.commit();
         if (first) {
             c.setMessage("Creating " + page.getName());
         } else {
             c.setMessage("Updating " + page.getName());
         }
         String author = (String) page.getMeta("author");
         String email = author + "@migrated.jhacks.hu";
         Date date = page.getDate();
         PersonIdent i = new PersonIdent(author, email, date, TimeZone.getDefault());
         c.setAuthor(i);
         c.setCommitter(i);
         c.setAll(true);
         c.call();
 
         ResetCommand reset = git.reset();
         reset.setMode(ResetCommand.ResetType.HARD);
         reset.call();
 
 
     }
 
     @Override
     public void write(Page page) {
         try {
             commitVersion(page, false);
         } catch (Exception e) {
             throw new RuntimeException("Can't commit to the git repo ", e);
         }
 
 
     }
 }
