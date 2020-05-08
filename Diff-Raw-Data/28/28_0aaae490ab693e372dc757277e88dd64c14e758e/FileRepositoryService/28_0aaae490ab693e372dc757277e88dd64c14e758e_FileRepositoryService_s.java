 package gitoscope.service.impl;
 
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import gitoscope.domain.Commit;
 import gitoscope.domain.Project;
 import gitoscope.exception.ProjectNotFoundException;
 import gitoscope.service.ConfigurationService;
 import gitoscope.service.ProjectService;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.LogCommand;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.util.*;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 
 public class FileRepositoryService implements ProjectService {
 
     private ConfigurationService configurationService;
     private Cache<String, Project> cachedProjects;
 
     private static final Logger LOG =
             LoggerFactory.getLogger(FileRepositoryService.class);
 
     public FileRepositoryService() {
         this.cachedProjects = CacheBuilder.newBuilder().build();
     }
 
     public List<Project> listProjects() {
         return new ArrayList<Project>(cachedProjects.asMap().values());
     }
 
     public Project findProjectByName(String projectName) {
         try {
             return cachedProjects.get(projectName, new ProjectLoader(projectName));
         } catch (ExecutionException ex) {
             if (ex.getCause() instanceof ProjectNotFoundException) {
                 throw (ProjectNotFoundException) ex.getCause();
             } else {
                 throw new RuntimeException(ex);
             }
         }
     }
 
     public List<Commit> listCommits(
             Project project,
             Map<String, Integer> paginateParams) {
         Repository repo = project.getRepository();
         Git git = new Git(repo);
         LogCommand log = git.log();
 
         List<Commit> commits = new ArrayList<Commit>();
         try {
             for (RevCommit commit : log.call()) {
                 commits.add(new Commit(commit));
             }
         } catch (Exception e) {
             LOG.error("Error during commit log iteration", e);
         }
 
         return commits;
     }
 
     private Project makeProject(File dir) {
         File gitDir = new File(dir.getAbsolutePath() + "/.git");
         if (dir.getName().endsWith(".git")) {
             // It is a bare repository
             return makeProjectFromGitDirectory(dir);
         } else if (gitDir.exists() && gitDir.isDirectory()) {
             // It is a development repository
             return makeProjectFromGitDirectory(gitDir);
         } else {
             LOG.warn("{} is not a valid git directory", gitDir.getAbsolutePath());
             return null;
         }
     }
 
     private Project makeProjectFromGitDirectory(File gitDir) {
         try {
             FileRepositoryBuilder builder = new FileRepositoryBuilder();
             Repository repo = builder.setGitDir(gitDir).build();
             String name = gitDir.getName();
            return new Project(name, repo);
         } catch (Exception e) {
             return null;
         }
     }
 
     private static String cutOffDotGit(String dirName) {
         int indexOfDotGit = dirName.indexOf(".git");
         if (indexOfDotGit > 0) {
             return dirName.substring(0, indexOfDotGit);
         } else {
             return dirName;
         }
     }
 
     @SuppressWarnings("unused")
     public void setConfigurationService(ConfigurationService configurationService) {
         this.configurationService = configurationService;
         prepareCache();
     }
 
     private class ProjectLoader implements Callable<Project> {
 
         private final String projectName;
 
         ProjectLoader(String projectName) {
             this.projectName = projectName;
         }
 
         public Project call() throws Exception {
             for (File dir : configurationService.listConfiguredProjectPaths()) {
                 String projectDirPath = dir.getAbsolutePath() + "/" + projectName;
                 File projectDir = new File(projectDirPath);
                 Project project = makeProject(projectDir);
                 if (project != null) {
                     return project;
                 }
             }
             throw new ProjectNotFoundException(projectName);
         }
     }
 
     private void prepareCache() {
         Collection<File> dirs = configurationService.listConfiguredProjectPaths();
         for (File dir : dirs) {
             File[] files = dir.listFiles();
             if (files == null) {
                 continue;
             }
             for (File projectDir : files) {
                 Project project = makeProject(projectDir);
                 if (project != null) {
                     cachedProjects.put(project.getName(), project);
                 }
             }
         }
     }
 }
