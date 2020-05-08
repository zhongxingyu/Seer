 
 package org.eclipse.releng.build.tools.comparator.ant;
 
 import java.io.IOException;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.eclipse.releng.build.tools.comparator.Extractor;
 
 public class ComparatorSummaryReport extends Task {
 
     private Extractor extractor;
 
     public ComparatorSummaryReport() {
         extractor = new Extractor();
     }
 
    public void setComparatorRepo(final String comparatorRepo) {
        extractor.setComparatorRepo(comparatorRepo);
    }
    
     public void setBuildDirectory(final String buildDirectory) {
         extractor.setBuildDirectory(buildDirectory);
     }
 
    public String getComparatorRepo() {
        // if specified as system property, it overrides settings
        // in the task itself.
        String comparatorRepo = System.getProperty(Extractor.COMPARATOR_REPO_PROPERTY);
        if (comparatorRepo != null) {
            extractor.setComparatorRepo(comparatorRepo);
        } else {
            comparatorRepo = extractor.getComparatorRepo();
            // comparator repo is allowed to null, though typically sould not be.
        }
        return comparatorRepo;
    }
    
     public String getBuildDirectory() {
         // if specified as system property, it overrides settings
         // in the task itself.
         String buildDirectory = System.getProperty(Extractor.BUILD_DIRECTORY_PROPERTY);
         if (buildDirectory != null) {
             extractor.setBuildDirectory(buildDirectory);
         } else {
             buildDirectory = extractor.getBuildDirectory();
             if (buildDirectory == null) {
                 throw new BuildException("buildDirectory must be set");
             }
         }
         return buildDirectory;
     }
 
     @Override
     public void execute() throws BuildException {
         try {
             extractor.processBuildfile();
         }
         catch (IOException e) {
             throw new BuildException(e);
         }
     }
 }
