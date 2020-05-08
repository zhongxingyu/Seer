 package com.github.easydoc;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.jfrog.maven.annomojo.annotations.MojoExecute;
 import org.jfrog.maven.annomojo.annotations.MojoGoal;
 import org.jfrog.maven.annomojo.annotations.MojoParameter;
 import org.jfrog.maven.annomojo.annotations.MojoPhase;
 import org.springframework.util.AntPathMatcher;
 
 import com.github.easydoc.exception.EasydocFatalException;
 import com.github.easydoc.exception.FileActionException;
 import com.github.easydoc.model.Model;
 import com.github.easydoc.param.SourceBrowserParam;
 import com.github.easydoc.semantics.EasydocSemantics;
 import com.github.easydoc.semantics.EasydocSemantics.CompilationResult;
 import com.github.easydoc.sourcebrowser.SourceBrowser;
 
 import freemarker.cache.ClassTemplateLoader;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 
 /*
  * This MOJO scans for all files and generates the documentation for project.
  *
  @@easydoc-start, id=easydoc-maven@@
  <h1>Maven integration</h1>
  
  To get started with Maven you only need to add the following snippet to your pom.xml in build/plugins section:
  
  <pre>
  	&lt;plugin&gt;
 		&lt;groupId&gt;com.github&lt;/groupId&gt;
 		&lt;artifactId&gt;easydoc-maven-plugin&lt;/artifactId&gt;
 		&lt;version&gt;0.0.5&lt;/version&gt;
 		&lt;executions&gt;
 			&lt;execution&gt;
 				&lt;goals&gt;
 					&lt;goal&gt;generate&lt;/goal&gt;
 				&lt;/goals&gt;
 			&lt;/execution&gt;
 		&lt;/executions&gt;
 	&lt;/plugin&gt;
  </pre>
  
  This will setup Easydoc for your project. By default, Easydoc will scan recursively your src/ directory and
  pom.xml file. Any docs found will be generated into target/easydoc/index.html page.
  @@easydoc-end@@
  */
 @MojoGoal("generate")
 @MojoPhase("process-sources")
 @MojoExecute(phase = "process-sources")
 public class EasydocMojo extends AbstractMojo {
 	/*@@easydoc-start, belongs=easydoc-maven@@
 	 <h2>Plugin configuration</h2>
 	 
 	 Below are plugin configuration options that you can specify inside the <i>configuration</i> section
 	 of the plugin declaration.<br>
 	 
  <pre>
  	&lt;plugin&gt;
 		&lt;groupId&gt;com.github&lt;/groupId&gt;
 		&lt;artifactId&gt;easydoc-maven-plugin&lt;/artifactId&gt;
 		&lt;version&gt;0.0.5&lt;/version&gt;
 		&lt;executions&gt;
 			&lt;execution&gt;
 				&lt;goals&gt;
 					&lt;goal&gt;generate&lt;/goal&gt;
 				&lt;/goals&gt;
 			&lt;/execution&gt;
 		&lt;/executions&gt;
 		<b>&lt;configuration&gt;
 		
 		  configuration options go here
 		
 		&lt;/configuration&gt;</b>
 	&lt;/plugin&gt;
  </pre>
 	 
 	 <h3>outputDirectory</h3>
 	 
 	 Specifies the output directory for the generated documentation.
 	 <br><br>
 	 <b>Default value:</b> target/easydoc
 	  
 	 @@easydoc-end@@*/
 	@MojoParameter(required = true,	expression = "${project.build.directory}/easydoc")
 	private File outputDirectory;
 
 	/*@@easydoc-start, belongs=easydoc-maven@@
 	 <h3>inputDirectory</h3>
 	 
 	 The input directory to scan for docs.
 	 <br><br>
 	 <b>Default value:</b> src
 	 @@easydoc-end@@*/
 	@MojoParameter(required = true,	expression = "${basedir}/src")
 	private File inputDirectory;
 
 	/*@@easydoc-start, belongs=easydoc-maven@@
 	 <h3>excludes</h3>
 	 
 	 Files or directories that should be excluded from the scan. Follows the standard Maven path pattern syntax.
 	 @@easydoc-end@@*/
 	@MojoParameter
 	private List<String> excludes = new ArrayList<String>();
 	
 	/*@@easydoc-start, belongs=easydoc-maven@@
 	 <h3>includes</h3>
 	 
 	 Files or directories that should only be scanned. All the other files will be omited. Follows the 
 	 standard Maven path pattern syntax.
 	 @@easydoc-end@@*/
 	@MojoParameter
 	private List<String> includes;
 	
 	/*@@easydoc-start, belongs=easydoc-maven@@
 	 <h3>customCss</h3>
 	 
 	 A custom CSS style to use in generated HTML. 
 	 @@easydoc-end@@*/
 	@MojoParameter
 	private File customCss;
 	
 	@MojoParameter
 	private SourceBrowserParam sourceBrowser;
 
 	private AntPathMatcher pathMatcher = new AntPathMatcher();
 	
 	private File currentDirectory = new File("");
 
 	public EasydocMojo() {
 	}
 
 	public void execute() throws MojoExecutionException {
 		excludes.add("**/.*"); //skip all entries starting with '.'
 		
 		try {
 			getLog().debug("Current directory = " + currentDirectory.getAbsolutePath());
 			getLog().debug("inputDirectory = " + inputDirectory.getAbsolutePath());
 			if(!inputDirectory.exists()) {
 				getLog().debug("Input directory does not exist. Skipping execution.");
 				return;
 			}
 
 			Configuration freemarkerCfg = new Configuration();
 			freemarkerCfg.setObjectWrapper(new DefaultObjectWrapper());
 			freemarkerCfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "/templates"));
 			
 			Model model = new Model();
 
 			ParseDocumentationFileAction fileAction = new ParseDocumentationFileAction(model, getLog());
 			//try to run this action for pom.xml file
 			File pomXml = new File("pom.xml");
 			if(pomXml.isFile() && !skipCheck(pomXml)) {
 				fileAction.run(pomXml);
 			}
 			//and also recursively in inputDirectory
			recurseDirectory(inputDirectory, fileAction);
 			
 			if(model.getDocs().isEmpty()) {
 				getLog().debug("No docs were found. Skipping execution.");
 				return;
 			}
 			
 			//compile the model
 			EasydocSemantics semantics = new EasydocSemantics();
 			CompilationResult compilationResult = semantics.compileModel(model);
 			if(compilationResult.isPositive()) {
 				Template template = freemarkerCfg.getTemplate("page.ftl");
 				outputDirectory.mkdirs();
 				BufferedWriter out = new BufferedWriter(
 						new FileWriter(new File(outputDirectory, "index.html"))
 						);
 				try {
 					Map<String, Object> freemarkerModel = compilationResult.getModel().toFreemarkerModel();
 					
 					String cssContent;
 					if(customCss != null) {
 						cssContent = FileUtils.readFileToString(customCss);
 					}
 					else {
 						cssContent = IOUtils.toString(getClass().getResourceAsStream("/css/easydoc.css"));
 					}
 					freemarkerModel.put("css", cssContent);
 					
 					if(sourceBrowser != null) {
 						freemarkerModel.put("sourceBrowser", createSourceBrowser(sourceBrowser));
 					}
 
 					template.process(freemarkerModel, out);
 				}
 				finally {
 					out.close();
 				}
 			}
 			else { //negative compilation result
 				for(String error : compilationResult.getErrors()) {
 					getLog().error(error);
 				}
 				throw new MojoExecutionException("Failed to compile documentation. See the error log.");
 			}
 		}
 		catch(Exception e) {
 			throw new MojoExecutionException("Execution failed", e);
 		}
 	}
 
 	private SourceBrowser createSourceBrowser(SourceBrowserParam sbParam) {
 		try {
 			if(sbParam.getType() != null) {
 				return sbParam.getType()
 						.getSourceBrowserClass()
 						.getConstructor(SourceBrowserParam.class)
 						.newInstance(sbParam);
 			}
 			else throw new IllegalArgumentException("The required parameter sourceBrowser/type is not specified");
 		}
 		catch(Exception e) {
 			throw new EasydocFatalException(e);
 		}
 	}
 
 	private void recurseDirectory(File dir, FileAction action) {
 		for(File file : dir.listFiles()) {
 			if(skipCheck(file)) continue;
 
 			if(!file.canRead()) {
 				getLog().warn("File " + file.getAbsolutePath() + " is not readable.");
 				continue;
 			}
 
 			if(file.isFile()) {
 				try {
					action.run(toRelativeFile(currentDirectory, file));
 				}
 				catch(FileActionException e) {
 					getLog().warn("Error", e);
 				}
 			}
 			else if(file.isDirectory()) {
 				recurseDirectory(file, action);
 			}
 		}
 	}
 	
 	private File toRelativeFile(File currentDirectory, File file) {
 		String absolutePath = file.getAbsolutePath();
 		String cdAbsolutePath = currentDirectory.getAbsolutePath();
 		
 		if(!absolutePath.startsWith(cdAbsolutePath)) {
 			return file;
 		}
 		if(absolutePath.equals(cdAbsolutePath)) { //to avoid StringIndexOutOfBoundsException
 			return currentDirectory;
 		}
 		
 		String relativePath = absolutePath.substring(cdAbsolutePath.length());
 		if(relativePath.startsWith("/")) {
 			if(relativePath.length() > 1) {
 				return new File(relativePath.substring(1));
 			}
 			else {
 				return currentDirectory;
 			}
 		}
 		else {
 			return new File(relativePath);
 		}
 	}
 
 	private boolean skipCheck(File file) {
 		boolean skip = false;
 		if(includes != null && !file.isDirectory()) {
 			skip = true;
 			for(String include : includes) {
				skip &= !pathMatcher.match("/" + include, file.getAbsolutePath());
 			}
 		}
 		if(skip) return true;
 		
 		for(String exclude : excludes) {
			if(pathMatcher.match("/" + exclude, file.getAbsolutePath())) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 }
