 package com.coremedia.beanmodeller.maven;
 
 import com.coremedia.beanmodeller.processors.ContentBeanAnalyzationException;
 import com.coremedia.beanmodeller.processors.ContentBeanAnalyzerException;
 import com.coremedia.beanmodeller.processors.ContentBeanInformation;
 import com.coremedia.beanmodeller.processors.analyzator.ContentBeanAnalyzator;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import java.util.Set;
 
 /**
  * Telekom .COM Relaunch 2011
  * User: marcus
  * Date: 07.02.11
  * Time: 14:47
  */
 public abstract class AbstractBeanModellerMojo extends AbstractMojo {
   /**
    * Path for searching abstract content beans.
    *
    * @parameter
    */
   private String contentBeanPackage;
 
   /**
    * Path for searching abstract content beans.
    *
    * @parameter default-value="${project}
    */
   private MavenProject project;
 
   private long startTime;
   private Long lastMeasurement = null;
 
   protected Set<ContentBeanInformation> analyzeContentBeans() throws MojoFailureException, MojoExecutionException {
     //create the analyzator
     ContentBeanAnalyzator analyzer = new ContentBeanAnalyzator();
     //set the logging
     analyzer.setLog(getLog());
     analyzer.findContentBeans(contentBeanPackage);
     try {
       analyzer.analyzeContentBeanInformation();
     }
     catch (ContentBeanAnalyzationException e) {
      throw new MojoFailureException("Unable to analyze the content beans", e);
     }
     Set<ContentBeanInformation> roots;
     try {
       roots = analyzer.getContentBeanRoots();
     }
     catch (ContentBeanAnalyzerException e) {
      throw new MojoExecutionException("This should have never happened", e);
     }
     return roots;
   }
 
   public long getTimeSinceLastMeasurement() {
     if (lastMeasurement == null) {
       lastMeasurement = System.currentTimeMillis();
       return lastMeasurement - startTime;
     }
     else {
       long now = System.currentTimeMillis();
       long result = now - lastMeasurement;
       lastMeasurement = now;
       return result;
     }
   }
 
   protected void startTimeMeasurements() {
     startTime = System.currentTimeMillis();
   }
 
   public long getTimeSinceStart() {
     return System.currentTimeMillis() - startTime;
   }
 
   public String getContentBeanPackage() {
     return contentBeanPackage;
   }
 
   public void setContentBeanPackage(String contentBeanPackage) {
     this.contentBeanPackage = contentBeanPackage;
   }
 
   public MavenProject getProject() {
     return project;
   }
 
   public void setProject(MavenProject project) {
     this.project = project;
   }
 }
