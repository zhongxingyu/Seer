 /**
  * 
  */
 package sirius.utils.retriever;
 
 import java.io.File;
import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import net.masterthought.cucumber.ReportBuilder;
 
import org.apache.commons.io.FileUtils;
 import org.apache.maven.doxia.sink.Sink;
 import org.apache.maven.doxia.siterenderer.Renderer;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.reporting.AbstractMavenReport;
 import org.apache.maven.reporting.MavenReportException;
 
 /**
  * @author Myk Kolisnyk
  * @goal cucumber
  * @phase site
  */
 public class CucumberReportingPlugin extends AbstractMavenReport {
     public final String fs      = System.getProperty("file.separator");
 
     /**
      * Directory where reports will go.
      *
      * @parameter expression="${project.reporting.outputDirectory}"
      * @required
      * @readonly
      */
     private String outputDirectory;
  
     /**
      * @parameter default-value="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * @component
      * @required
      * @readonly
      */
     private Renderer siteRenderer;
     
     /**
      * @parameter
      * @required
      * @readonly
      */
     private String jsonFile;
     
     /**
      * @parameter
      * @readonly
      */
     private boolean skippedFails = false;
 
     /**
      * @parameter
      * @readonly
      */
     private boolean undefinedFails = false;
     
     /**
      * @parameter
      * @readonly
      */
     private boolean flashCharts = false;
     
     /**
      * @parameter
      * @readonly
      */
     private boolean runWithJenkins = false;
     
     private boolean artifactsEnabled = false;
     
     private boolean highCharts = false;
     
     /* (non-Javadoc)
      * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
      */
     public String getDescription(Locale arg0) {
         // TODO Auto-generated method stub
         return "HTML formatted Cucumber test results report";
     }
 
     /* (non-Javadoc)
      * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
      */
     public String getName(Locale arg0) {
         // TODO Auto-generated method stub
         return "Test Execution Report";
     }
 
     /* (non-Javadoc)
      * @see org.apache.maven.reporting.MavenReport#getOutputName()
      */
     public String getOutputName() {
         // TODO Auto-generated method stub
         return "cucumber-test-report";
     }
 
     /**
      * @return the jsonFile
      */
     public String getJsonFile() {
         return jsonFile;
     }
 
     /**
      * @param jsonFile the jsonFile to set
      */
     public void setJsonFile(String jsonFile) {
         this.jsonFile = jsonFile;
     }
 
     /**
      * @return the skippedFails
      */
     public boolean isSkippedFails() {
         return skippedFails;
     }
 
     /**
      * @param skippedFails the skippedFails to set
      */
     public void setSkippedFails(boolean skippedFails) {
         this.skippedFails = skippedFails;
     }
 
     /**
      * @return the undefinedFails
      */
     public boolean isUndefinedFails() {
         return undefinedFails;
     }
 
     /**
      * @param undefinedFails the undefinedFails to set
      */
     public void setUndefinedFails(boolean undefinedFails) {
         this.undefinedFails = undefinedFails;
     }
 
     /**
      * @return the flashCharts
      */
     public boolean isFlashCharts() {
         return flashCharts;
     }
 
     /**
      * @param flashCharts the flashCharts to set
      */
     public void setFlashCharts(boolean flashCharts) {
         this.flashCharts = flashCharts;
     }
 
     /**
      * @return the runWithJenkins
      */
     public boolean isRunWithJenkins() {
         return runWithJenkins;
     }
 
     /**
      * @param runWithJenkins the runWithJenkins to set
      */
     public void setRunWithJenkins(boolean runWithJenkins) {
         this.runWithJenkins = runWithJenkins;
     }
 
     /**
      * @param outputDirectory the outputDirectory to set
      */
     public void setOutputDirectory(String outputDirectory) {
         this.outputDirectory = outputDirectory;
     }
 
     /**
      * @param project the project to set
      */
     public void setProject(MavenProject project) {
         this.project = project;
     }
 
     /**
      * @param siteRenderer the siteRenderer to set
      */
     public void setSiteRenderer(Renderer siteRenderer) {
         this.siteRenderer = siteRenderer;
     }
 
     /* (non-Javadoc)
      * @see org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util.Locale)
      */
     @Override
     protected void executeReport(Locale arg0) throws MavenReportException {
         File reportOutputDirectory = new File(outputDirectory + fs + "cucumber" );
         List<String> jsonReportFiles = new ArrayList<String>();
         jsonReportFiles.add(this.jsonFile);
 
         ReportBuilder reportBuilder;
         try {
             reportBuilder = 
                     new ReportBuilder(
                             jsonReportFiles, 
                             reportOutputDirectory, 
                             "", 
                             project.getVersion(), 
                             project.getName(), 
                             skippedFails, 
                             undefinedFails, 
                             flashCharts, 
                             runWithJenkins, 
                             artifactsEnabled, 
                             "", 
                             highCharts);            
             reportBuilder.generateReports();
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         Sink sink = getSink();
         sink.head();
         sink.title();
         sink.text("Cucumber Test Results Report");
         sink.title_();
         sink.head_();
      
         sink.body();
         sink.section1();
         sink.sectionTitle1();
         sink.text("Features Overview");
         sink.sectionTitle1_();
         sink.paragraph();
         sink.text("Features overview report shows test results distributed by each specific feature with detailed information about tests included");
         sink.link("cucumber/feature-overview.html");
         sink.text("View Report");
         sink.link_();
         sink.paragraph_();
         sink.section1_();
         
         sink.section1();
         sink.sectionTitle1();
         sink.text("Tags Overview");
         sink.sectionTitle1_();
         sink.paragraph();
         sink.text("Tags overview report shows test results distributed by tag with detailed information about tests included");
         sink.link("cucumber/tag-overview.html");
         sink.text("View Report");
         sink.link_();
         sink.paragraph_();
         sink.section1_();
 
         sink.body_();
         sink.flush();
         sink.close();
 
     }
 
     /* (non-Javadoc)
      * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
      */
     @Override
     protected String getOutputDirectory() {
         // TODO Auto-generated method stub
         return outputDirectory;
     }
 
     /* (non-Javadoc)
      * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
      */
     @Override
     protected MavenProject getProject() {
         // TODO Auto-generated method stub
         return project;
     }
 
     /* (non-Javadoc)
      * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
      */
     @Override
     protected Renderer getSiteRenderer() {
         // TODO Auto-generated method stub
         return siteRenderer;
     }
 
 }
