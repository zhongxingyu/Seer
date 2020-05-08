 package org.cdisource.maven.webrunner;
 
 import groovy.lang.GroovyClassLoader;
 import groovy.lang.GroovyCodeSource;
 import groovy.lang.GroovyObject;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 public abstract class AbstractWebRunnerMojo extends AbstractMojo {
 
 	/**
 	 * The maven project.
 	 * 
 	 * @parameter expression="${executedProject}"
 	 * @required
 	 * @readonly
 	 */
 	protected MavenProject project;
 	
 	/**
 	 * The maven local repo directory
 	 * 
 	 * @parameter expression="${maven.local.repo.directory}"
 	 */
 	protected File mavenLocalRepoDirectory;
 
 	/**
 	 * The maven project name
 	 * 
 	 * @parameter expression="${project.name}"
 	 * @required
 	 * @readonly
 	 */
 	protected String projectName;
 	
 	/**
 	 * The maven project name
 	 * 
 	 */
 	protected File projectBaseDir;
 
 	/**
 	 * 
 	 */
 	protected File projectBuildDir;
 	
 
 	/**
 	 * 
 	 */
 	protected String projectBuildFinalName;
 
 	
 	/**
 	 * The location of the web.xml file. If not set then it is assumed it is in
 	 * ${basedir}/src/main/webapp/WEB-INF
 	 * 
 	 * @parameter expression="${maven.war.webxml}"
 	 */
 	private File webXml;
 
 	/**
 	 * The directory containing generated classes.
 	 * 
 	 * @parameter expression="${project.build.outputDirectory}"
 	 * @required
 	 * 
 	 */
 	private File classesDirectory;
 	/**
 	 * The directory containing generated test classes.
 	 * 
 	 * @parameter expression="${project.build.testOutputDirectory}"
 	 * @required
 	 */
 	private File testClassesDirectory;
 	/**
 	 * Root directory for all html/jsp etc files
 	 * 
 	 * @parameter expression="${basedir}/src/main/webapp"
 	 * @required
 	 */
 	private File webAppSourceDirectory;
 
 	/**
 	 * Root directory for all java files
 	 * 
 	 * @parameter expression="${basedir}/src/main/java"
 	 * @required
 	 */
 	private File javaSourceDirectory;
 
 	/**
 	 * Root directory for all java files
 	 * 
 	 * @parameter expression="${basedir}/src/dev/runtimelib"
 	 * @required
 	 */
 	private File runtimeLibDirectory;
 
 	
 	/**
 	 * The context path for the webapp. 
 	 * Defaults to the name of the build final name and then 
 	 * artifact id.
 	 * 
 	 * @parameter expression=""
 	 */
 	protected String contextPath;
 	/**
 	 * The temporary directory to use for the webapp. Defaults to
 	 * target/resin-tmp
 	 * 
 	 * @parameter expression="${project.build.directory}/work"
 	 * @required
 	 */
 	protected File tmpDirectory;
 	/**
 	 * A webdefault.xml file to use instead of the default for the webapp.
 	 * Optional.
 	 * 
 	 * @parameter
 	 */
 	protected File webDefaultXml;
 	/**
 	 * File containing system properties to be set before execution
 	 * 
 	 * Note that these properties will NOT override System properties that have
 	 * been set on the command line, by the JVM, or directly in the POM via
 	 * systemProperties. Optional.
 	 * 
 	 * @parameter expression="${resin.systemPropertiesFile}"
 	 */
 	protected File systemPropertiesFile;
 	/**
 	 * <p>
 	 * If true, execution of the plugin is skipped.
 	 * </p>
 	 * 
 	 * @parameter expression="${resin.skip}" default-value="false"
 	 */
 	protected boolean skip;
 	/**
 	 * <p>
 	 * Resin maven plugin is a series of maven scripts.
 	 * </p>
 	 * 
 	 * @parameter expression="${resin.script}"
 	 *            default-value="org.cdisource.maven.resin/info.groovy"
 	 */
 	protected String script = "org.cdisource.maven.resin/info.groovy";
 
 	protected String warName;

	/**
	 * <p>
	 * Resin maven plugin is a series of maven scripts.
	 * </p>
	 * 
	 * @parameter expression="${resin.fork}"
	 *            default-value="true"
	 */
 	protected boolean runFork=true;
	/**
	 * <p>
	 * Resin maven plugin is a series of maven scripts.
	 * </p>
	 * 
	 * @parameter expression="${resin.spawn}"
	 *            default-value="false"
	 */
 	protected boolean runSpawn=false;
 	
 	
 	
 	
 	public String getProjectBuildFinalName() {
 		return projectBuildFinalName;
 	}
 
 	public void setProjectBuildFinalName(String projectBuildFinalName) {
 		this.projectBuildFinalName = projectBuildFinalName;
 	}
 
 	public boolean isRunFork() {
 		return runFork;
 	}
 
 	public void setRunFork(boolean runFork) {
 		this.runFork = runFork;
 	}
 
 	public boolean isRunSpawn() {
 		return runSpawn;
 	}
 
 	public void setRunSpawn(boolean runSpawn) {
 		this.runSpawn = runSpawn;
 	}
 
 	public AbstractWebRunnerMojo() {
 		super();
 	}
 
 	public String getWarName() {
 		return warName;
 	}
 
 	public void setWarName(String warName) {
 		this.warName = warName;
 	}
 
 	public String getProjectName() {
 		return projectName;
 	}
 
 	public void setProjectName(String projectName) {
 		this.projectName = projectName;
 	}
 
 	public File getWebXml() {
 		return webXml;
 	}
 
 	public void setWebXml(File webXml) {
 		this.webXml = webXml;
 	}
 
 	public File getClassesDirectory() {
 		return classesDirectory;
 	}
 
 	public void setClassesDirectory(File classesDirectory) {
 		this.classesDirectory = classesDirectory;
 	}
 
 	public File getTestClassesDirectory() {
 		return testClassesDirectory;
 	}
 
 	public void setTestClassesDirectory(File testClassesDirectory) {
 		this.testClassesDirectory = testClassesDirectory;
 	}
 
 	public File getWebAppSourceDirectory() {
 		return webAppSourceDirectory;
 	}
 
 	public void setWebAppSourceDirectory(File webAppSourceDirectory) {
 		this.webAppSourceDirectory = webAppSourceDirectory;
 	}
 
 	public String getScript() {
 		return script;
 	}
 
 	public void setScript(String script) {
 		this.script = script;
 	}
 
 	public MavenProject getProject() {
 		return project;
 	}
 
 	public void setProject(MavenProject project) {
 		this.project = project;
 	}
 
 	public String getContextPath() {
 		return contextPath;
 	}
 
 	public void setContextPath(String contextPath) {
 		this.contextPath = contextPath;
 	}
 
 	public File getTmpDirectory() {
 		return tmpDirectory;
 	}
 
 	public void setTmpDirectory(File tmpDirectory) {
 		this.tmpDirectory = tmpDirectory;
 	}
 
 	public File getWebDefaultXml() {
 		return webDefaultXml;
 	}
 
 	public void setWebDefaultXml(File webDefaultXml) {
 		this.webDefaultXml = webDefaultXml;
 	}
 
 	public File getSystemPropertiesFile() {
 		return systemPropertiesFile;
 	}
 
 	public void setSystemPropertiesFile(File systemPropertiesFile) {
 		this.systemPropertiesFile = systemPropertiesFile;
 	}
 
 	public boolean isSkip() {
 		return skip;
 	}
 
 	public void setSkip(boolean skip) {
 		this.skip = skip;
 	}
 
 	public File getProjectBaseDir() {
 		return projectBaseDir;
 	}
 
 	public void setProjectBaseDir(File projectBaseDir) {
 		this.projectBaseDir = projectBaseDir;
 	}
 
 	public File getProjectBuildDir() {
 		return projectBuildDir;
 	}
 
 	public void setProjectBuildDir(File projectBuildDir) {
 		this.projectBuildDir = projectBuildDir;
 	}
 	
 	
 
 	public File getJavaSourceDirectory() {
 		return javaSourceDirectory;
 	}
 
 	public void setJavaSourceDirectory(File javaSourceDirectory) {
 		this.javaSourceDirectory = javaSourceDirectory;
 	}
 	
 
 	public File getRuntimeLibDirectory() {
 		return runtimeLibDirectory;
 	}
 
 	public void setRuntimeLibDirectory(File runtimeLibDirectory) {
 		this.runtimeLibDirectory = runtimeLibDirectory;
 	}
 	
 	
 
 	public File getMavenLocalRepoDirectory() {
 		return mavenLocalRepoDirectory;
 	}
 
 	public void setMavenLocalRepoDirectory(File mavenLocalRepoDirectory) {
 		this.mavenLocalRepoDirectory = mavenLocalRepoDirectory;
 	}
 
 	public void execute() throws MojoExecutionException {
 		
 
 		if (skip) {
 			getLog().info("Skipping resin: resin.skip==true");
 			return;
 		}
 
 		String finalName = this.project.getBuild().getFinalName();
 		String artifactId = this.project.getArtifactId();
 		warName = finalName.startsWith(artifactId) ? artifactId : finalName;
 		if (this.contextPath==null || "".equals(contextPath.trim())) {
 			this.contextPath = "/" + warName;
 		}
 	}
 
 
 	public void callScript(String name) throws MojoExecutionException {
 		String script = "org.cdisource.maven.resin/"+name+".groovy";
 		runScript(script);
 	}
 
 	@SuppressWarnings("unchecked")
 	protected void runScript(String script) throws MojoExecutionException {
 		ClassLoader parent = getClass().getClassLoader();
 		GroovyClassLoader loader = new GroovyClassLoader(parent);
 		Class<GroovyObject> groovyClass;
 		try {
 
 			InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(script);
 			Reader reader = new InputStreamReader(resourceAsStream);
 			GroovyCodeSource groovyCodeSource = new GroovyCodeSource(reader, "mainResin", "mainResin");
 			groovyClass = loader.parseClass(groovyCodeSource);
 			GroovyObject groovyObject = groovyClass.newInstance();
 			groovyObject.setProperty("plugin", this);
 			Object[] args = {};
 			groovyObject.invokeMethod("run", args);
 
 		} catch (Exception ex) {
 			System.err.println("Unable to run script " + script);
 			throw new MojoExecutionException("Unable to run script " + script, ex);
 		}
 	}
 
 	public void prepareForExecute() {
 		this.projectBuildDir = new File(this.project.getBuild().getDirectory());
 		this.projectBuildFinalName = this.project.getBuild().getFinalName();
 		this.projectBaseDir = this.project.getBasedir();

 		String finalName = this.project.getBuild().getFinalName();
 		String artifactId = this.project.getArtifactId();
		warName = finalName.startsWith(artifactId) ? this.projectName : finalName;
 		if (this.contextPath==null || "".equals(contextPath.trim())) {
 			this.contextPath = "/" + warName;
 		}
 
 	}
 
 }
