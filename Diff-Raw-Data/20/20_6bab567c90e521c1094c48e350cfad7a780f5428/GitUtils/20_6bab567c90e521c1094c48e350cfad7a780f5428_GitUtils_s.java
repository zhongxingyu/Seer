 package com.codingstory.polaris.repo;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.errors.GitAPIException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 /** Fetches code from GitHub. */
 public class GitUtils {
 
     private static final Log LOG = LogFactory.getLog(GitUtils.class);
 
     public static Repository mirrorOrSync(Repository remote, File repoBase) throws IOException {
         Preconditions.checkNotNull(remote);
         Preconditions.checkNotNull(repoBase);
         File localDir = new File(repoBase, remote.getName());
         if (localDir.isDirectory()) {
             Repository local = new Repository(remote.getName(), localDir.getPath());
             sync(local);
             return local;
         } else {
             return mirror(remote, repoBase);
         }
     }
 
     public static Repository mirror(Repository remote, File repoBase) throws IOException {
         Preconditions.checkNotNull(remote);
         Preconditions.checkNotNull(repoBase);
         Preconditions.checkArgument(repoBase.isDirectory());
         LOG.info("Mirroring " + remote);
         try {
             File output = new File(repoBase, remote.getName());
             Git.cloneRepository()
                     .setBare(true)
                     .setURI(remote.getUrl())
                     .setDirectory(output)
                     .call();

            // Fix a JGit bug that a bare clone cannot be synced with the remote.
            FileUtils.write(new File(output, "config"),
                    "[core]\n" +
                    "    repositoryformatversion = 0\n" +
                    "    filemode = true\n" +
                    "    bare = true\n" +
                    "[remote \"origin\"]\n" +
                    "    url = " + remote.getUrl() + "\n");
             return new Repository(remote.getName(), output.getPath());
         } catch (GitAPIException e) {
             throw new IOException(e);
         }
     }
 
     public static void sync(Repository local) throws IOException {
         Preconditions.checkNotNull(local);
         Preconditions.checkArgument(local.isLocal());
         LOG.info("Updating " + local);
        Git git = Git.open(new File(local.getUrl()));
        git.fetch();
     }
 
     public static List<Repository> openRepoBase(File repoBase) throws IOException {
         List<Repository> repos = Lists.newArrayList();
         File[] children = repoBase.listFiles();
         if (children == null) {
             return ImmutableList.of();
         }
         for (File dir : children) {
             if (dir.isDirectory()) {
                 repos.add(new Repository(dir.getName(), dir.getPath()));
             }
         }
         return repos;
     }
 
     public static void checkoutWorkTree(Repository repo, File outputDir) throws IOException {
         Preconditions.checkNotNull(repo);
         Preconditions.checkNotNull(outputDir);
         Preconditions.checkArgument(outputDir.isDirectory());
         LOG.info("Cloning " + repo + " into " + outputDir);
         try {
             Git.cloneRepository()
                     .setURI(repo.getUrl())
                     .setDirectory(outputDir)
                     .call();
         } catch (GitAPIException e) {
             throw new IOException(e);
         }
     }
 }
