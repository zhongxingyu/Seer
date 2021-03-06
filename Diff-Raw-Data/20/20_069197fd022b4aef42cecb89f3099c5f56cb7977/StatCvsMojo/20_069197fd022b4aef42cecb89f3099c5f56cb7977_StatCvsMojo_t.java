 package de.berlios.statcvs.xml.maven;
 
 /*
  * Copyright 2005 Tammo van Lessen, Steffen Pingel
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 import java.io.File;
 import java.util.List;
 import java.util.Locale;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.reporting.AbstractMavenReport;
 import org.apache.maven.reporting.MavenReportException;
 import org.apache.maven.scm.manager.ScmManager;
 import org.codehaus.doxia.parser.Parser;
 import org.codehaus.doxia.sink.Sink;
 import org.codehaus.doxia.site.renderer.SiteRenderer;
 
 /**
  * Generates a StatCvs report.
  *
  * @goal report
  * 
  * @phase process-sources
  */
 public class StatCvsMojo extends AbstractMavenReport {
 
     /**
      * Specifies if pictures should be displayed on the author pages. 
      *
      * @parameter default-value="false"
      */
     private boolean authorPictures;
 
     /**
      * The SCM connection URL.
      * 
      * @parameter
      *  expression="${connectionUrl}"
      *  default-value="${project.scm.developerConnection}"
      */
     private String connectionUrl;
 
     /**
      * Comma separated list of excludes file pattern.
      * @parameter expression="${excludes}" 
      */
     private String excludes;
 
     /**
      * Specifies if the StatCvs should be invoked in a separate JVM.
      *
      * @parameter default-value="false"
      */
     private boolean fork;
 
     /**
      * Specifies if the report should include deleted directories.
      *
      * @parameter default-value="false"
      */
     private boolean history;
 
     /**
      * Temporary directory where log file is saved.
      *
      * @parameter expression="${project.build.directory}/statcvs"
      */
     private File buildDirectory;
 
     /**
      * Working directory when history is used.
      *
      * @parameter expression="${project.build.directory}/statcvs/history"
      */
     private File historyWorkingDirectory;
 
 	/**
      * Comma separated list of includes file pattern.
      * @parameter expression="${includes}" 
      */
     private String includes;
     
     private boolean initialized;
     
     private Locale locale = Locale.ENGLISH;
     
     /**
      * @parameter expression="${component.org.apache.maven.scm.manager.ScmManager}"
      * @required
      * @readonly
      */
     private ScmManager manager;
 
     /**
      * List of of plugin artifacts.
      *
      * @parameter expression="${plugin.artifacts}"
      */
     private List pluginArtifacts;
     
     /**
      * @parameter default-value="${project.reporting.outputDirectory}"
      * @required
      */
     private File reportingDirectory;
 
     /**
     * Report output directory.
     *
     * @parameter expression="${project.build.directory}/generated-site/xdoc/statcvs"
     * @required
     */
    private String outputDirectory;

    /**
      * Specifies the directory where the report will be generated
      *
      * @parameter default-value="${project.reporting.outputDirectory}/statcvs"
      * @required
      */
    private File htmlOutputDirectory;
 
     /**
      * Specifies if the pom should be used to determine real names.
      *
      * @parameter default-value="true"
      */
     private boolean parsePOM;
 
     /**
      * @component roleHint="xdoc"
      */
     private Parser parser;
 
     /**
      * @parameter default-value="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * Specifies the StatCvs output renderer.
      *
     * @parameter default-value="xdoc"
      * @required
      */
     private String renderer;
 
     /**
      * @component
      * @required
      * @readonly
      */
     private SiteRenderer siteRenderer;
 
 	/**
      * The working directory
      * 
      * @parameter expression="${basedir}"
      */
     private File workingDirectory;
     
     /**
      * Option to specify the jvm (or path to the java executable) to use with
      * the forking options. For the default we will assume that java is in the path.
      *
      * @parameter expression="${jvm}"
      * default-value="java" 
      */
     private String jvm;    
     
 	public boolean canGenerateReport() {
 		// TODO: check scm settings ...
 		return super.canGenerateReport(); // && outputDirectory.exists();
 	}
 
 	protected void executeReport(Locale locale) throws MavenReportException {
 		initialize();
 		
         // and start the report 
         Sink sink = getSink();
 
         sink.head();
         sink.title();
         sink.text("StatCvs Report");
         sink.title_();
         sink.head_();
 
         sink.body();
         sink.section1();
 
         createReport();
         
        String dest = outputDirectory;
         String base = reportingDirectory.getAbsolutePath();
         String relativPath = dest.substring(base.length() + 1);
         sink.link(relativPath + "/index.html");
         sink.text(relativPath + "/index.html");
         sink.link_();
 	
         sink.section1_();
         sink.body_();
         
         sink.flush();
         sink.close();
 	}
 	
 	private void createReport() throws MavenReportException {
 		try {
 			File logFile = new File(getBuildDirectory(), "cvs.log");
 			
 			CvsConnection conneciton = new CvsConnection(this, logFile);
 			conneciton.execute();
 			
 			StatCvsReport report = new StatCvsReport(this, logFile);
 			report.execute();
 		}
 		catch (Exception e) {
 			throw new MavenReportException("Could not fetch cvs log.", e);
 		}
 	}
 	
 	private void foo() {
 		/*
 		if (parser != null && resultsDirectory.exists()) {
 			File[] files = resultsDirectory.listFiles(new FilenameFilter() {
 				public boolean accept(File dir, String name) {
 					return name.endsWith(".xml");
 				}
 			});
 
 			for (int i = 0; i < files.length; i++) {
 				File file = files[i];
 				try {
 					Sink fsink = createSink(file.getName());
 					Reader r = new FileReader(file);
 					parser.parse(r, fsink);
 					r.close();
 					fsink.close();
 				} catch (IOException e) {
 					getLog().warn(e);
 				} catch (ParseException e) {
 					getLog().warn(e);
 				}
 			}
 			
 			// copy pngs
 			File[] images = resultsDirectory.listFiles(new FilenameFilter() {
 				public boolean accept(File dir, String name) {
 					return name.endsWith(".png");
 				}
 			});
 			
 			for (int i = 0; i < images.length; i++) {
 				File file = images[i];
 				try {
 					FileUtils.copyFile(new FileInputStream(file), new File(outputDirectory, file.getName()));
 				} catch (IOException e) {
 					getLog().warn("Could not copy statistics chart to output folder.");
 				}
 			}
 
 		} else {
 			getLog().info("No CVS statistics found.");
 		}
 		*/
 	}
 
     public String getConnectionUrl() {
         return connectionUrl;
     }
 
 	public File getBuildDirectory() {
 		return buildDirectory;
 	}
     
 	public String getDescription(Locale locale) {
 		//TODO: I18N
 		return "Statistics about CVS usage generated by StatCvs-XML.";
 	}
 		
 	public String getName(Locale locale) {
 		//TODO: I18N
 		return "StatCvs Report";
 	}
 
 	protected String getOutputDirectory() {
		return outputDirectory;
 	}
 
 	public String getOutputName() {
 		return "statcvs/index";
 	}
 
 	protected MavenProject getProject() {
 		return project;
 	}
 
     public ScmManager getScmManager() {
         return manager;
     }
     
 	protected SiteRenderer getSiteRenderer() {
 		return siteRenderer;
 	}
 	
 	
 	public File getWorkingDirectory() {
         if (isHistory()) {
         	return historyWorkingDirectory;
         }
         else {
         	return workingDirectory;
         }
 	}
 	
 	public String getIncludes() {
 		return includes;
 	}
 	
 	public String getExcludes()
 	{
 		return excludes;
 	}
 	
 	public boolean isParsePOM()
 	{
 		return parsePOM;
 	}
 	
 	public String getTitle() {
 		// TODO
 		return "StatCvs Report";
 	}
 	
 	public String getRenderer() {
 		return renderer;
 	}
 	
 	public boolean isAuthorPictures() {
 		return authorPictures;
 	}
 	
 	public boolean isVerbose() {
 		return true;
 	}
 	
 	private void initialize() {
 		if (initialized) {
 			return;
 		}
 		
 		if (history) {
 			
 		}
 		
 		initialized = true;
 	}
 
 	public boolean isFork() {
 		return fork;
 	}
 	
 	public boolean isExternalReport() {
 		return true;
 	}
 
 	public boolean isHistory() {
 		return history;
 	}
 	
 	public String getJvm() {
 		return jvm;
 	}
 	
 	public List getPluginArtifacts()
 	{
 		return pluginArtifacts;
 	}
 	
 }
