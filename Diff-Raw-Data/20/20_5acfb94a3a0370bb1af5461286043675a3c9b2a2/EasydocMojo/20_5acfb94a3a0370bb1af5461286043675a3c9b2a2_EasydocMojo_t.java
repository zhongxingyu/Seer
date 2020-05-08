 package com.github.easydoc;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;

 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.BuildPluginManager;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.jfrog.maven.annomojo.annotations.MojoComponent;
 import org.jfrog.maven.annomojo.annotations.MojoExecute;
 import org.jfrog.maven.annomojo.annotations.MojoGoal;
 import org.jfrog.maven.annomojo.annotations.MojoParameter;
 import org.jfrog.maven.annomojo.annotations.MojoPhase;
 import org.springframework.util.AntPathMatcher;
 import org.twdata.maven.mojoexecutor.MojoExecutor;
 
 import com.github.easydoc.exception.EasydocFatalException;
 import com.github.easydoc.exception.FileActionException;
 import com.github.easydoc.model.Model;
 import com.github.easydoc.param.CombineWithParam;
 import com.github.easydoc.param.SourceBrowserParam;
 import com.github.easydoc.semantics.EasydocSemantics;
 import com.github.easydoc.semantics.EasydocSemantics.CompilationResult;
 import com.github.easydoc.semantics.methods.IndexTextMethod;
 import com.github.easydoc.sourcebrowser.SourceBrowser;
 
 import freemarker.cache.ClassTemplateLoader;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 
 /*
  * This MOJO scans for all files and generates the documentation for project.
  *
 @@easydoc-start, id=easydoc-maven, format=markdown@@
 Maven integration
 =================
  
 To get started with Maven you only need to add the following snippet to your pom.xml in `build/plugins` section:
  
 	<plugin>
 		<groupId>com.github.weekens</groupId>
 		<artifactId>easydoc-maven-plugin</artifactId>
 		<version>0.4.17</version>
 		<executions>
 			<execution>
 				<goals>
 					<goal>generate</goal>
 				</goals>
 			</execution>
 		</executions>
 	</plugin>
 
 This will setup Easydoc for your project. By default, Easydoc will scan recursively your `src/` directory and
 `pom.xml` file. Any docs found will be generated into `target/easydoc/index.html` page.
 @@easydoc-end@@
  */
 @MojoGoal("generate")
 @MojoPhase("process-sources")
 @MojoExecute(phase = "process-sources")
 public class EasydocMojo extends AbstractMojo {
 	/*@@easydoc-start, belongs=easydoc-maven, format=markdown@@
 	Plugin configuration
 	====================
  
 	Below are plugin configuration options that you can specify inside the <i>configuration</i> section
 	of the plugin declaration.<br>
 	
 		<plugin>
 			<groupId>com.github</groupId>
 			<artifactId>easydoc-maven-plugin</artifactId>
 			<version>0.4.17</version>
 			<executions>
 				<execution>
 					<goals>
 						<goal>generate</goal>
 					</goals>
 				</execution>
 			</executions>
 			<b><configuration>
 			
 			  configuration options go here
 			
 			</configuration></b>
 		</plugin>
 	 
 	### outputDirectory ###
 	 
 	Specifies the output directory for the generated documentation.
 	 
 	**Default value:** `target/easydoc`
 	  
 	@@easydoc-end@@*/
 	@MojoParameter(required = true,	expression = "${project.build.directory}/easydoc")
 	private File outputDirectory;
 	
 	@MojoParameter(required = true,	expression = "${project.build.directory}/easydoc-dependencies")
 	private File depsDirectory;
 
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
 	
 	/*@@easydoc-start, id=easydoc-maven-source-browser, belongs=easydoc-maven@@
 	 <h3>sourceBrowser</h3>
 	 
 	 This parameter tells Easydoc how to generate the source links for your docs. If this parameter
 	 is present, Easydoc will generate HTML links to a place of origin of each of your docs, so that
 	 you can view them directly in the browser.
 	 <br><br>
 	 Generally, you specify it in the following manner:<br>
 	 <pre>
 &lt;configuration&gt;
 	...
 	&lt;sourceBrowser&gt;
 		&lt;baseUrl&gt;http://your.url.com/path_to_sources&lt;/baseUrl&gt;
 		&lt;type&gt;source_browser_type (covered below)&lt;/type&gt;
 		... (other params if necessary)
 	&lt;/sourceBrowser&gt;
 &lt;/configuration&gt;
 	 </pre>
 	 <br>
 	 <i>baseUrl</i> and <i>type</i> are the essential parameters that you will need to specify. The following
 	 <i>type</i> values are supported so far: 
 	 @@easydoc-end@@*/
 	@MojoParameter
 	private SourceBrowserParam sourceBrowser;
 	
 	/*@@easydoc-start, id=easydoc-maven-encoding, belongs=easydoc-maven@@
 	<h3>encoding</h3>
 	
 	Sets up the encoding for the source file and the resulting HTML.<br>
 	If the <i>project.build.sourceEncoding</i> property is defined, it is used by default. Otherwise,
 	the default encoding setting is taken from JVM.
 	@@easydoc-end@@*/
 	@MojoParameter(expression = "${encoding}", defaultValue = "${project.build.sourceEncoding}")
 	private String encoding = Charset.defaultCharset().toString();
 
 	private AntPathMatcher pathMatcher = new AntPathMatcher();
 	
 	@MojoParameter(required = true,	expression = "${basedir}")
 	private File projectDirectory;
 	
 	private File currentDirectory = new File("");
 	
 	private Properties versionProperties = new Properties();
 	
 	/*@@easydoc-start, belongs=easydoc-maven@@
 	<h3>generateIndex</h3>
 	
 	If set to 'true' (default), Easydoc will generate index for the documentation.
 	If 'false', the index won't be generated.
 	@@easydoc-end@@*/
 	@MojoParameter
 	private Boolean generateIndex = true;
 	
 	/*@@easydoc-start, id=easydoc-maven-default-format, belongs=easydoc-maven@@
 	<h3>defaultFormat</h3>
 	
 	Sets the default <a href="#easydoc-format-param">format</a> for the docs.
 	If not specified, 'html' is default.
 	@@easydoc-end@@*/
 	@MojoParameter
 	private String defaultFormat = "html";
 	
 	@MojoParameter(expression = "${project}", required = true, readonly = true)
 	private MavenProject mavenProject;
 	
 	@MojoParameter(expression = "${session}", required = true, readonly = true)
 	private MavenSession mavenSession;
 	
 	@MojoComponent
 	private BuildPluginManager pluginManager;
 	
 	/*@@easydoc-start, belongs=easydoc-maven@@
 	<h3>generateArtifact</h3>
 	
 	By default, Easydoc will generate an additional artifact for your documentation,
 	containing the generated pages along with docs model metadata. You can disable it
 	by setting this parameter to <code>false</code>.
 	@@easydoc-end@@*/
 	@MojoParameter(defaultValue = "true")
 	private Boolean generateArtifact = true;
 	
 	/*@@easydoc-start, belongs=easydoc-maven@@
 	<h3>artifactClassifier</h3>
 	
 	With this parameter, you may explicitly specify a classifier for an artifact, 
 	generated by Easydoc.
 	@@easydoc-end@@*/
 	@MojoParameter(defaultValue = "easydoc")
 	private String artifactClassifier = "easydoc";
 	
 	/*@@easydoc-start, id=easydoc-maven-combine-with, belongs=easydoc-maven, format=markdown@@
 	### combineWith ###
 	
 	Specifies Easydoc artifacts of other projects to be combined with current project
 	[Combining docs](#easydoc-combine).
 	
 	The configuration looks like this:
 	
 		<configuration>
 			...
 			<combineWith>
 				<item>
 					<groupId>com.mycompany</groupId>
 					<artifactId>my-remote-project</artifactId>
 					<version>0.0.1-SNAPSHOT</version>
 				</item>
 				<item>
 					<groupId>com.mycompany</groupId>
 					<artifactId>my-other-project</artifactId>
 					<version>0.0.1-SNAPSHOT</version>
 				</item>
 				<item>
 					<groupId>com.mycompany</groupId>
 					<artifactId>my-other-project</artifactId>
 					<version>0.0.1-SNAPSHOT</version>
 					<classifier>optionalClassifier</classifier>
 				</item>
 				...
 			</combineWith>
 			...
 		</configuration>
 	@@easydoc-end@@*/
 	@MojoParameter
 	private List<CombineWithParam> combineWith;
 
 	public void execute() throws MojoExecutionException {
 		try {
 			excludes.add("**" + File.separator + ".*"); //skip all entries starting with '.'
 			
 			versionProperties.load(getClass().getResourceAsStream("/version.properties"));
 			
 			getLog().debug("currentDirectory = " + currentDirectory.getAbsolutePath());
 			if(!inputDirectory.exists()) {
 				getLog().debug("Input directory does not exist. Skipping execution.");
 				return;
 			}
 
 			Configuration freemarkerCfg = new Configuration();
 			freemarkerCfg.setObjectWrapper(new DefaultObjectWrapper());
 			freemarkerCfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "/templates"));
 			
 			Model model = new Model();
 			
 			if(combineWith != null && combineWith.size() > 0) {
 				for(CombineWithParam cwParam : combineWith) {
 					Model combineModel = loadCombineDependency(cwParam);
 					model.getDocTree().addRoots(combineModel.getDocTree().getRoots());
 				}
 				getLog().debug("Combine model = " + model);
 			}
 
 			ParseDocumentationFileAction fileAction = new ParseDocumentationFileAction(model, getLog());
 			fileAction.setEncoding(encoding);
 			
 			//try to run this action for pom.xml file
 			File pomXml = new File(projectDirectory.getAbsoluteFile(), "pom.xml");
 			pomXml = toRelativeFile(currentDirectory, pomXml);
 			getLog().debug("pomXml = " + pomXml + ", isFile = " + pomXml.isFile() + ", skipCheck = " + skipCheck(pomXml));
 			if(pomXml.isFile() && !skipCheck(pomXml)) {
 				fileAction.run(pomXml);
 			}
 			//and also recursively in inputDirectory
 			recurseDirectory(toRelativeFile(currentDirectory, inputDirectory), fileAction);
 			
 			if(model.isEmpty()) {
 				getLog().debug("No docs were found. Skipping execution.");
 				return;
 			}
 			
 			//compile the model
 			EasydocSemantics semantics = new EasydocSemantics();
 			semantics.setDefaultFormat(defaultFormat);
 			CompilationResult compilationResult = semantics.compileModel(model);
 			if(compilationResult.isPositive()) {
 				Template template = freemarkerCfg.getTemplate("page.ftl");
 				outputDirectory.mkdirs();
 				BufferedWriter out = new BufferedWriter(
 						new OutputStreamWriter(
 								new FileOutputStream(new File(outputDirectory, "index.html")), 
 								encoding
 						)
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
 					
 					freemarkerModel.put("version", versionProperties.getProperty("version", ""));
 					
 					freemarkerModel.put("encoding", encoding);
 					template.setEncoding(encoding);
 					
 					freemarkerModel.put("indexText", new IndexTextMethod());
 					freemarkerModel.put("generateIndex", generateIndex);
 					
 					freemarkerModel.put("projName", mavenProject.getName());
 					
 					template.process(freemarkerModel, out);
 				}
 				finally {
 					out.close();
 				}
 				
 				//expose artifact if needed
 				if(generateArtifact) {
 					File dbDir = new File(outputDirectory, "META-INF");
 					dbDir.mkdir();
 					ObjectOutputStream oos = new ObjectOutputStream(
 							new FileOutputStream(
 									new File(dbDir, "easydoc.db")
 							)
 					);
 					try {
 						oos.writeObject(model);
 					}
 					finally {
 						oos.close();
 					}
 					
 					MojoExecutor.executeMojo(
 							MojoExecutor.plugin(
 									"org.apache.maven.plugins", 
 									"maven-jar-plugin", 
 									"2.3.2"
 							), 
 							MojoExecutor.goal("jar"), 
 							MojoExecutor.configuration(
 									MojoExecutor.element("classifier", artifactClassifier),
 									MojoExecutor.element("classesDirectory", outputDirectory.getPath())
 							), 
 							MojoExecutor.executionEnvironment(
 									mavenProject, 
 									mavenSession, 
 									pluginManager
 							)
 					);
 				}
 			}
 			else { //negative compilation result
 				for(String error : compilationResult.getErrors()) {
 					getLog().error(error);
 				}
 				throw new MojoExecutionException("Failed to compile documentation. See the error log.");
 			}
 		}
 		catch(MojoExecutionException e) { //allow this type of exceptions
 			throw e;
 		}
 		catch(Exception e) {
 			throw new EasydocFatalException(e);
 		}
 	}
 	
 	/*@@easydoc-start, id=easydoc-combine, belongs=easydoc-advanced, format=markdown@@
 	Combining docs
 	==============
 	
 	Sometimes you need to combine docs from different project into one single documentation site (page).
 	This section describes how you can easily do that.
 	
 	By default, Easydoc generates an additional artifact for your documentation named 
 	<your_default_artifact_name>-easydoc.jar. This artifact is installed, deployed and tracked by
 	Maven just like any other one.
 	
 	So, for the remote project documentation that you need to combine with the current, you already have
 	a special Maven artifact, and the only thing that is left is to declare that artifact as a dependency.
 	But not in the Maven `<dependencies>` section, but in Easydoc configuration element: 
 	[combineWith](#easydoc-maven-combine-with).
 	
 	After you've done that - the documenation from the remote project will get to your current project's
 	documentation page. More over, you can interact with docs from the remote project - use them in 
 	`\@\@include\@\@` directives and `belongs` parameters - because the artifact contains not only the
 	generated text, but the data model of the remote docs.
 	
 	Example:
 	
 	You've got 2 projects: my-lib and my-webapp (group id: `com.mycompany`, version 0.0.1-SNAPSHOT). 
 	Both are documented with Easydoc. But you need documentation from my-lib to be present in 
 	documentation page of my-webapp. You do the following:
 	
 	1. Build my-lib. The artifact my-lib-0.0.1-SNAPSHOT-easydoc.jar gets installed into local repository
 	(or deployed into remote repository).
 	
 	2. Specify the artifact from step 1 in [combineWith](#easydoc-maven-combine-with) parameter of 
 	Easydoc plugin configuration in my-webapp:
 	
 			<configuration>
 				...
 				<combineWith>
 					<item>
 						<groupId>com.mycompany</groupId>
 						<artifactId>my-lib</artifactId>
 						<version>0.0.1-SNAPSHOT</version>
 					</item>
 				</combineWith>
 				...
 			</configuration>
 		
 	3. Build my-webapp. The documentation of my-webapp will now contain documentation from my-lib.
 	
 	@@easydoc-end@@*/
 	private Model loadCombineDependency(CombineWithParam cwParam) throws MojoExecutionException {
 		try {
 			MojoExecutor.executeMojo(
 					MojoExecutor.plugin(
 							"org.apache.maven.plugins", 
 							"maven-dependency-plugin", 
 							"2.4"
 					), 
 					MojoExecutor.goal("copy"), 
 					MojoExecutor.configuration(
 							MojoExecutor.element(
 									"artifactItems", 
 									MojoExecutor.element(
 											"artifactItem", 
 											MojoExecutor.element("groupId", cwParam.getGroupId()),
 											MojoExecutor.element("artifactId", cwParam.getArtifactId()),
 											MojoExecutor.element("version", cwParam.getVersion()),
 											MojoExecutor.element("type", "jar"),
 											MojoExecutor.element(
 													"classifier", 
 													cwParam.getClassifier() != null ? cwParam.getClassifier() : "easydoc"
 											),
 											MojoExecutor.element("overWrite", "true"),
 											MojoExecutor.element("outputDirectory", depsDirectory.getPath()),
 											MojoExecutor.element("destFileName", cwParam.getArtifactId() + ".jar")
 									)
 							)
 					), 
 					MojoExecutor.executionEnvironment(
 							mavenProject, 
 							mavenSession, 
 							pluginManager
 					)
 			);
 			
 			JarFile jarFile = new JarFile(new File(depsDirectory, cwParam.getArtifactId() + ".jar"));
 			try {
 				JarEntry dbEntry = jarFile.getJarEntry("META-INF/easydoc.db");
 				ObjectInputStream ois = new ObjectInputStream(jarFile.getInputStream(dbEntry));
 				try {
 					return (Model)ois.readObject();
 				}
 				finally {
 					ois.close();
 				}
 			}
 			finally {
 				jarFile.close();
 			}
 		}
 		catch(IOException e) {
 			throw new MojoExecutionException("Failed to load combine dependency " + cwParam, e);
 		}
 		catch(ClassNotFoundException e) {
 			throw new MojoExecutionException("Failed to load combine dependency " + cwParam, e);
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
				//try to define MIME type of the file to skip binary files
				MagicMatch mimeMatch = null;
				try {
					mimeMatch = Magic.getMagicMatch(file, true, false);
				} catch (Exception e) {
					getLog().debug("Cannot define mime type for file " + file + ": " + e.getMessage());
				}
				
 				try {
					if(mimeMatch == null || mimeMatch.getMimeType().startsWith("text/")) {
						action.run(file);
					}
					else {
						getLog().debug("Skipping binary file " + file);
					}
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
 		if(relativePath.startsWith(File.separator)) {
 			if(relativePath.length() > File.separator.length()) {
 				return new File(relativePath.substring(File.separator.length()));
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
 				skip &= !pathMatcher.match(include, file.getPath());
 			}
 		}
 		if(skip) return true;
 		
 		for(String exclude : excludes) {
 			if(pathMatcher.match(exclude, file.getPath())) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 }
