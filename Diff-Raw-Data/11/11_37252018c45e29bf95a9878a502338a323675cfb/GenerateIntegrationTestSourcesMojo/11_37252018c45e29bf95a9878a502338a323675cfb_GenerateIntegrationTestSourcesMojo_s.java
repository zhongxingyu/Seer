 package dridco.maven;
 
 import static java.lang.String.format;
 import static java.lang.System.currentTimeMillis;
 import static org.apache.commons.lang.StringUtils.isNotEmpty;
 import static org.apache.commons.lang.StringUtils.remove;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import dridco.seleniumhtmltojava.Globals;
 import dridco.seleniumhtmltojava.JavaTestCompiler;
 import dridco.seleniumhtmltojava.SeleniumBuilder;
 import dridco.seleniumhtmltojava.SupportedBrowsers;
 import dridco.seleniumhtmltojava.TestCaseName;
 
 /**
  * @goal run
  * @phase="generate-test-sources"
  */
 public class GenerateIntegrationTestSourcesMojo extends AbstractMojo {
 
 	/**
 	 * @parameter default-value="${basedir}/src/test/html"
 	 */
 	private File htmlTestsLocation;
 	/**
 	 * @parameter default-value="${project.build.directory}/generated-test-sources/selenium-htmltojava"
 	 * @readonly
 	 */
 	private String javaTestsLocation;
 	/**
 	 * @parameter expression="${htmltojava.skip}" default-value="true"
 	 */
 	private Boolean skip;
 	/**
 	 * @parameter expression="${project}"
 	 * @readonly
 	 */
 	private MavenProject project;
 	/**
 	 * @parameter expression="${htmltojava.host}" default-value="localhost"
 	 */
 	private String seleniumServerHost;
 	/**
 	 * @parameter expression="${htmltojava.port}" default-value="4444"
 	 * @required
 	 */
 	private Integer seleniumServerPort;
 	/**
 	 * @parameter expression="${htmltojava.browser}" default-value="firefox"
 	 */
 	private String seleniumServerBrowser;
 	/**
 	 * @parameter
 	 * @required
 	 */
 	private String seleniumServerBaseUrl;
 	/**
 	 * @parameter default-value="ITCase"
 	 */
 	private String testClassesSuffix;
 	/**
 	 * @parameter default-value="UTF-8"
 	 */
 	private String htmlTestsEncoding;
 	/**
 	 * @parameter default-value="UTF-8"
 	 */
 	private String javaTestsEnconding;
 	/**
 	 * @parameter
 	 */
 	private String excludedFilesPattern;
 	/**
 	 * @parameter default-value="30000"
 	 */
 	private Integer timeoutForPageLoad;
 	/**
 	 * @parameter default-value="0"
 	 */
 	private Integer speed;
 	/**
 	 * @parameter expression="${htmltojava.forced_timeout}" default-value="-1"
 	 */
 	private Integer forcedTimeout;
 
 	private JavaTestCompiler compiler;
 
 	// these variables make the execution of this plugin non-threadsafe
 	private int compiledTests;
 	private long start;
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		if (canExecute()) {
 			initializeGlobals();
 			initializeCompiler();
 			initializeMetrics();
 			generateSources();
 			reportMetrics();
 		} else {
 			getLog().info("Skipping HTML tests compilation");
 		}
 	}
 
 	private void initializeGlobals() {
 		Globals.timeout = timeoutForPageLoad;
 		Globals.speed = speed;
 		Globals.forcedTimeout = forcedTimeout;
 	}
 
 	private void reportMetrics() {
 		getLog().info(format("Compiled %d tests in %s milliseconds", //
 				compiledTests, currentTimeMillis() - start));
 	}
 
 	private void initializeMetrics() {
 		compiledTests = 0;
 		start = currentTimeMillis();
 	}
 
 	private void initializeCompiler() {
 		SeleniumBuilder seleniumBuilder = new SeleniumBuilder( //
 				seleniumServerHost, //
 				seleniumServerPort, //
 				SupportedBrowsers.valueOf(seleniumServerBrowser), //
 				seleniumServerBaseUrl);
 		compiler = new JavaTestCompiler(seleniumBuilder, testClassesSuffix);
 	}
 
 	private void generateSources() throws MojoExecutionException {
 		recurse(htmlTestsLocation);
 		addTestSourceDirectory();
 	}
 
 	private void recurse(File location) throws MojoExecutionException {
 		if (location.isDirectory()) {
 			if (mustBeCrawled(location)) {
 				for (File child : location.listFiles()) {
 					recurse(child);
 				}
 			}
 		} else {
 			if (mustBeCompiled(location)) {
 				compile(location);
 			}
 		}
 	}
 
 	private boolean mustBeCrawled(File location) {
 		// excluding hidden directories avoids crawling .svn for instance
 		return !location.isHidden();
 	}
 
 	private boolean mustBeCompiled(File location) {
 		return !(isNotEmpty(excludedFilesPattern) //
 		&& location.getName().matches(excludedFilesPattern));
 	}
 
 	private void compile(File location) throws MojoExecutionException {
 		String name = location.getName();
 		try {
 			String html = IOUtils.toString(new FileInputStream(location),
 					htmlTestsEncoding);
 			String pkg = remove(location.getParentFile().getAbsolutePath(),
 					htmlTestsLocation.getAbsolutePath());
 			getLog().info("Compiling from source " + name);
 			String java = compiler.compile(html, name, pkg);
 			String targetName = new TestCaseName(name, testClassesSuffix)
 					.normalize() + ".java";
 			File targetPath = new File(javaTestsLocation + pkg);
 			File targetFile = new File(targetPath, targetName);
			targetPath.mkdirs();
 			IOUtils.write(java, new FileOutputStream(targetFile),
 					javaTestsEnconding);
 			compiledTests++;
 		} catch (FileNotFoundException e) {
 			throw wrapIOException(e);
 		} catch (IOException e) {
 			throw wrapIOException(e);
 		} catch (Exception e) {
 			getLog().warn("Cound not compile " + name, e);
 		}
 	}
 
 	private MojoExecutionException wrapIOException(Throwable e) {
 		return new MojoExecutionException("Could not open source file", e);
 	}
 
 	private void addTestSourceDirectory() {
 		project.addTestCompileSourceRoot(javaTestsLocation);
 	}
 
 	private boolean canExecute() {
 		return !skip;
 	}
 
 }
