 package com.xored.fmaven;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.toolchain.ToolchainManager;
 import org.codehaus.plexus.util.DirectoryScanner;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 public abstract class FatomMojo extends AbstractMojo {
 
 	/**
 	 * @parameter expression="${project}"
 	 * @required
 	 * @readonly
 	 */
 	protected MavenProject project;
 
 	/**
 	 * The Maven Session Object
 	 * 
 	 * @parameter expression="${session}"
 	 * @required
 	 * @readonly
 	 */
 	protected MavenSession session;
 
 	/**
 	 * The toolchain manager to use.
 	 * 
 	 * @component
 	 * @required
 	 * @readonly
 	 */
 	protected ToolchainManager toolchainManager;
 
 	/**
 	 * Default location of .coffee source files.
 	 * 
	 * @parameter expression="${basedir}/src/main/fan"
 	 * @required
 	 */
 	protected File fanDir;
 
 	/**
 	 * Location of the output files from the Coffee Compiler. Defaults to
 	 * ${build.directory}/fan
 	 * 
	 * @parameter expression="${project.build.directory}/fan"
 	 * @required
 	 */
 	protected File fanOutputDir;
 
 	/**
 	 * What version of Fantom should we compile with?
 	 * 
 	 * @parameter default-value="1.0.63"
 	 */
 	private String version;
 
 	private List<String> acceptableVersions = ImmutableList.of("1.0.63");
 
 	/**
 	 * A list of inclusion filters for the compiler. ex :
 	 * 
 	 * <pre>
 	 *    &lt;includes&gt;
 	 *      &lt;include&gt;SomeFile.scala&lt;/include&gt;
 	 *    &lt;/includes&gt;
 	 * </pre>
 	 * 
 	 * @parameter
 	 */
 	protected Set<String> includes = new HashSet<String>();
 
 	/**
 	 * A list of exclusion filters for the compiler. ex :
 	 * 
 	 * <pre>
 	 *    &lt;excludes&gt;
 	 *      &lt;exclude&gt;SomeBadFile.scala&lt;/exclude&gt;
 	 *    &lt;/excludes&gt;
 	 * </pre>
 	 * 
 	 * @parameter
 	 */
 	protected Set<String> excludes = new HashSet<String>();
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		if (!acceptableVersions.contains(version)) {
 			String error = String
 					.format("Unsupported version of fanrom specified (%s) - supported versions: %s",
 							version, Joiner.on(", ").join(acceptableVersions));
 
 			throw new MojoExecutionException(error);
 		}
 
 		getLog().info(
 				String.format("fmaven-plugin using fantom version %s", version));
 
 		doExecute();
 	}
 
 	protected abstract void doExecute();
 
 	protected List<File> getSourceFiles() {
 		initFilters();
 
 		DirectoryScanner scanner = new DirectoryScanner();
 		scanner.setBasedir(fanDir);
 		scanner.setIncludes(includes.toArray(new String[includes.size()]));
 		scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
 		scanner.addDefaultExcludes();
 		scanner.scan();
 
 		List<File> sources = Lists.newArrayList();
 		for (String tmpLocalFile : scanner.getIncludedFiles()) {
 			sources.add(new File(fanDir, tmpLocalFile));
 		}
 
 		return sources;
 	}
 
 	protected void initFilters() {
 		if (includes.isEmpty()) {
 			includes.add("**/build.fan");
 		}
 		if (getLog().isDebugEnabled()) {
 			StringBuilder builder = new StringBuilder("includes = [");
 			for (String include : includes) {
 				builder.append(include).append(",");
 			}
 			builder.append("]");
 			getLog().debug(builder.toString());
 
 			builder = new StringBuilder("excludes = [");
 			for (String exclude : excludes) {
 				builder.append(exclude).append(",");
 			}
 			builder.append("]");
 			getLog().debug(builder.toString());
 		}
 	}
 }
