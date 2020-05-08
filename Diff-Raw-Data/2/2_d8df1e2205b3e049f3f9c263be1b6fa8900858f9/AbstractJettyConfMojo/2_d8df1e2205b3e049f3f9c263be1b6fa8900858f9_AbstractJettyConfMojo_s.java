 package net.uvavru.maven.plugins.jettyconf.internals;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import net.uvavru.maven.plugins.jettyconf.types.ArtifactCandidates;
 import net.uvavru.maven.plugins.jettyconf.types.JettyFiles;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.DefaultArtifact;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 public abstract class AbstractJettyConfMojo extends AbstractMojo {
 
 
 	/**
 	 * Comma Separated list of artifact types current project should mock as
 	 * 
 	 * @since 1.0
 	 * @parameter expression="${classpathPatterns}" default-value=""
 	 * @optional
 	 */
 	protected String mockingTypes = "";
 
 	/**
 	 * If true, the &lt;testOutputDirectory&gt; and the dependencies of
 	 * &lt;scope&gt;test&lt;scope&gt; will be put first on the runtime
 	 * classpath.
 	 * 
 	 * @parameter alias="useTestClasspath" default-value="false"
 	 */
 	private boolean useTestScope;
 
 	/**
 	 * Comma Separated list of regexp patterns indicating how webapp resources
 	 * paths should be translated into.
 	 * 
 	 * @since 1.0
 	 * @parameter expression="${webAppReplacements}" default-value=""
 	 * @optional
 	 */
 	protected String webAppReplacements = "";
 
 	/**
 	 * Comma Separated list of regexp patterns indicating how webapp resources
 	 * paths should be translated.
 	 * 
 	 * @since 1.0
 	 * @parameter expression="${webAppPatterns}" default-value=""
 	 * @optional
 	 */
 	protected String webAppPatterns = "";
 
 	/**
 	 * Comma Separated list of regexp patterns indicating how classpath
 	 * resources paths should be translated.
 	 * 
 	 * @since 1.0
 	 * @parameter expression="${classpathPatterns}" default-value=""
 	 * @optional
 	 */
 	protected String classpathPatterns = "";
 
 	/**
 	 * Regexp pattern that will match layout
 	 * 'groupId:artifactId:type:classifier' string. This is how to indicate
 	 * which artifacts should be considered for the classpath.
 	 * 
 	 * @since 1.0
 	 * @parameter expression="${classpathMatchArtifactPattern}"
 	 *            default-value="[^:]*:[^:]*:jar:[^:]*"
 	 * @optional
 	 */
 	protected String classpathMatchArtifactPattern = "[^:]*:[^:]*:jar:[^:]*";
 
 	/**
 	 * Regexp pattern that will match layout
 	 * 'groupId:artifactId:type:classifier' string. This is how to indicate
 	 * which artifacts should be considered for the web app resources.
 	 * 
 	 * @since 1.0
 	 * @parameter expression="${webAppMatchArtifactPattern}"
 	 *            default-value="[^:]*:[^:]*:war:[^:]*"
 	 * @optional
 	 */
 	protected String webAppMatchArtifactPattern = "[^:]*:[^:]*:war:[^:]*";
 
 	/**
 	 * Comma Separated list of regexp patterns indicating how classpath
 	 * resources paths should be translated into.
 	 * 
 	 * @since 1.0
 	 * @parameter expression="${classpathReplacements}" default-value=""
 	 * @optional
 	 */
 	protected String classpathReplacements = "";
 
 	/**
 	 * Filters artifacts according to the properties
 	 * 
 	 * @param artifacts
 	 * @return
 	 */
 	private Set<Artifact> filterArtifacts(List<Artifact> artifacts,
 			Pattern pattern) {
 		Set<Artifact> artifactsFiltered = new HashSet<Artifact>();
 
 		for (Artifact artifact : artifacts) {
 			getLog().debug(
 					"resolved: " + artifact + ", path: " + artifact.getFile());
 			String artifactDescriptor = String.format("%s:%s:%s:%s",
 					artifact.getGroupId(), artifact.getArtifactId(),
 					artifact.getType(), artifact.getClassifier());
 			getLog().debug("Matching against descriptor: " + artifactDescriptor);
 
 			if (pattern.matcher(artifactDescriptor).matches()) {
 				getLog().debug("matched: " + artifactDescriptor);
 				artifactsFiltered.add(artifact);
 			}
 		}
 		return artifactsFiltered;
 	}
 
 	/**
 	 * Conditionally adds artifact of this current project.<br>
 	 * Depends on {@link #mockingTypes} field.
 	 * 
 	 * @param dependencyArtifacts
 	 * @return conditionally modified list
 	 */
 	private List<Artifact> conditionallyAddCurrentProjectArtifact(
 			List<Artifact> dependencyArtifacts) {
 		// conditionally add current artifact
 		for (String type : mockingTypes.split(",")) {
 			Artifact active = getProject().getArtifact();
 			Artifact current = new DefaultArtifact(active.getGroupId(),
 					active.getArtifactId(), active.getVersionRange(),
 					active.getScope(), type, active.getClassifier(),
 					active.getArtifactHandler());
 			current.setFile(new File(getProject().getBuild().getOutputDirectory()));
 			dependencyArtifacts.add(current);
 		}
 		return dependencyArtifacts;
 	}
 	
 	/**
 	 * Whether to add only directories into web app resources.
 	 * Note that jetty accepts only directories!
 	 * 
 	 * @since 1.0
 	 * @parameter expression="${webAppResourcesAsDirsOnly}" default-value="true"
 	 * @optional
 	 */
 	protected boolean webAppResourcesAsDirsOnly = true;
 	
 	/**
 	 * Whether to treat web app paths as windows path.
 	 * In this case single backslash characters are doubled.
 	 * 
 	 * @since 1.0
 	 * @parameter expression="${webappDirNonexistentTreatAsWindowsPath}" default-value="false"
 	 * @optional
 	 */
 	protected boolean webappDirNonexistentTreatAsWindowsPath = false;
 	
 	
 	
 	/**
 	 * If directory for a web app resource doesn't exist this pattern
 	 * is used to transform the file path to a different path.<br>
 	 * Implies {@code webAppResourcesAsDirsOnly} is set to {@code true}
 	 * 
 	 * @since 1.0
 	 * @parameter expression="${webAppDirNonexistentAlternatePattern}" default-value="null"
 	 * @optional
 	 */
 	protected String webAppDirNonexistentAlternatePattern = null;
 	
 	/**
 	 * If directory for a web app resource doesn't exist this pattern
 	 * is used to transform the file path to a different path.
 	 * Implies {@code webAppResourcesAsDirsOnly} is set to {@code true}
 	 * 
 	 * @since 1.0
 	 * @parameter expression="${webAppDirNonexistentAlternateReplacement}" default-value="null"
 	 * @optional
 	 */
 	protected String webAppDirNonexistentAlternateReplacement = null;
 	
 	
 	/**
 	 * Filters and translate artifacts according to plugin configuration.
 	 * 
 	 * @param artifacts
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	public JettyFiles filterAndTranslateClasspathArtifacts(ArtifactCandidates artifacts) throws MojoExecutionException {
 		return filterAndTranslateArtifacts(artifacts, Pattern.compile(classpathMatchArtifactPattern), classpathPatterns, classpathReplacements);
 	}
 	
 	public JettyFiles filterAndTranslateWebAppArtifacts(ArtifactCandidates artifacts) throws MojoExecutionException {
 		JettyFiles webAppFiles = filterAndTranslateArtifacts(artifacts, Pattern.compile(webAppMatchArtifactPattern), webAppPatterns, webAppReplacements);
 		
 		Pattern webAppAlternativePattern = null;
 		if (webAppDirNonexistentAlternatePattern != null) {
 			if (webAppDirNonexistentAlternateReplacement == null) {
 				getLog().warn(String.format("Not using %s as web app alternate pattern because 'replacement' is null", webAppDirNonexistentAlternatePattern));
 			} else {
 				webAppAlternativePattern = Pattern.compile(webAppDirNonexistentAlternatePattern);
 			}
 		}
 		if (webappDirNonexistentTreatAsWindowsPath) {
 			webAppDirNonexistentAlternateReplacement = webAppDirNonexistentAlternateReplacement.replace("\\", "\\\\");
 		}
 		for (ListIterator<File> fileIt = webAppFiles.listIterator(); fileIt.hasNext(); ) {
 			File file = fileIt.next();
 			try {
 				if (webAppAlternativePattern != null && !file.isDirectory() && webAppResourcesAsDirsOnly) {
 					if (webAppAlternativePattern.matcher(file.getCanonicalPath()).matches()) {
 						String alternateFilePath = webAppAlternativePattern.matcher(file.getCanonicalPath()).replaceAll(webAppDirNonexistentAlternateReplacement);
 						File alternateFile = new File(alternateFilePath);
 						if (alternateFile.exists() && alternateFile.isDirectory()) {
							getLog().warn(String.format("transforming '%s' into '%s'", file.getCanonicalPath(), alternateFile.getCanonicalPath()));
 							fileIt.set(alternateFile);
 						} else {
 							getLog().debug("File " + alternateFilePath + " doesn't exist .. skipping alternate path");
 						}
 					} else {
 						getLog().debug("File " + file.getCanonicalPath() + " doesn't match web alternate pattern.");
 					}
 				} else {
 					getLog().debug("Condition for: " + file + " not met..");
 				}
 			} catch (IOException e) {
 				throw new MojoExecutionException("Cannot generate alternate path for: " + file.getName(), e);
 			}
 		}
 		
 		for (Iterator<File> fileIt = webAppFiles.iterator(); fileIt.hasNext(); ) {
 			File file = fileIt.next();
 			if (!file.isDirectory() && webAppResourcesAsDirsOnly) {
 				fileIt.remove();
 				getLog().warn(
 						"Not adding artifact file: '" + file
 								+ "' into webapps because it's not a directory");
 			}
 		}
 		
 		return webAppFiles;
 	}
 	
 	private  JettyFiles filterAndTranslateArtifacts(List<Artifact> artifacts, Pattern pattern, String patterns, String replacements) throws MojoExecutionException {
 		Set<Artifact> filteredArtifacts = filterArtifacts(artifacts, pattern
 				);
 		
 		JettyFiles translatedFiles = translatePathsAndReduceArtifacts(filteredArtifacts,
 				patterns, replacements);
 		
 		return translatedFiles;
 	}
 	
 	private void addToFilesAsCanonical(Set<Artifact> artifacts, List<File> files)
 			throws MojoExecutionException {
 		for (Artifact artifact : artifacts) {
 			try {
 				files.add(artifact.getFile().getCanonicalFile());
 			} catch (IOException e) {
 				throw new MojoExecutionException(
 						"Cannot generate path for an artifact: " + artifact, e);
 			}
 		}
 	}
 
 	/**
 	 * Translate paths and reduces artifacts set if matched.
 	 * 
 	 * @param artifacts
 	 * @param patterns
 	 * @param replacements
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	private JettyFiles translatePathsAndReduceArtifacts(Set<Artifact> artifacts, String patterns,
 			String replacements) throws MojoExecutionException {
 		JettyFiles files = new JettyFiles();
 
 		if (patterns == null | replacements == null) {
 			return files;
 		}
 
 		Artifact artifact = null;
 
 		try {
 			String[] patternStrings = patterns.split(",");
 			String[] replacementsStrings = replacements.split(",");
 			for (int i = 0; i < patternStrings.length
 					&& i < replacementsStrings.length; ++i) {
 				Pattern pattern = Pattern.compile(patternStrings[i]);
 
 				Iterator<Artifact> it = artifacts.iterator();
 				while (it.hasNext()) {
 					artifact = it.next();
 
 					String path = artifact.getFile().getCanonicalPath();
 
 					File file = translatePath(path, pattern,
 							replacementsStrings[i]);
 					if (file != null) {
 						files.add(file);
 						it.remove();
 					}
 				}
 			}
 			
 			// adds the rest of artifacts to the files
 			addToFilesAsCanonical(artifacts, files);
 
 		} catch (IOException e) {
 			throw new MojoExecutionException(
 					"Cannot generate path for an artifact: " + artifact, e);
 		}
 		return files;
 	}
 
 	private File translatePath(String path, Pattern pattern, String replacement) {
 		if (pattern.matcher(path).matches()) {
 			return new File(pattern.matcher(path).replaceAll(replacement));
 		}
 		return null;
 	}
 
 	/**
 	 * Returns all projects dependency artifacts.<br>
 	 * Doesn't include current project.
 	 * 
 	 * @see AbstractJettyConfMojo#jettyArtifactCandidates()
 	 * @return dependency artifacts
 	 */
 	private void fetchDependencyArtifacts(List<Artifact> dependencyArtifacts) {
 
 		for (Artifact artifact : (Set<Artifact>) getProject().getArtifacts()) {
 
 			if (((!Artifact.SCOPE_PROVIDED.equals(artifact.getScope())) && (!Artifact.SCOPE_TEST
 					.equals(artifact.getScope())))
 					|| (useTestScope && Artifact.SCOPE_TEST.equals(artifact
 							.getScope()))) {
 				dependencyArtifacts.add(artifact);
 			}
 		}
 
 	}
 	
 	/**
 	 * Returns all artifact candidates to be considered in the configuration.
 	 * 
 	 * @return
 	 */
 	public ArtifactCandidates jettyArtifactCandidates() {
 		ArtifactCandidates candidates = new ArtifactCandidates();
 		
 		fetchDependencyArtifacts(candidates);
 		conditionallyAddCurrentProjectArtifact(candidates);
 		
 		return candidates;
 	}
 	
 	 /**
      * The maven project.
      *
      * @parameter expression="${project}"
      * @readonly
      */
     protected MavenProject project;
     
 	public MavenProject getProject()
     {
         return this.project;
     }
 
     public void setProject(MavenProject project) {
         this.project = project;
     }
     
     /**
      * Initializes properties:
      * <ul><li>{@code jetty.conf-plugin.classpath}</li> and <li>{@code jetty.conf-plugin.webapp}</li></ul>
      * 
      * @param artifactCandidates
      * @throws MojoExecutionException
      */
     public void initializeJettyConfProperties(ArtifactCandidates artifactCandidates) throws MojoExecutionException {
     	JettyFiles classpathFiles = filterAndTranslateClasspathArtifacts(artifactCandidates);
 		
 		JettyFiles webappFiles = filterAndTranslateWebAppArtifacts(artifactCandidates);
 		
 		String webapp = "";
 		for (File file : webappFiles) {
 			webapp += "\n<Item>" + file.toURI() + "</Item>";
 		}
 		project.getProperties()
 				.setProperty("jetty.conf-plugin.webapp", webapp);
 
 		String classpath = "";
 		for (File file : classpathFiles) {
 			classpath += "\n" + file.toURI() + ";";
 		}
 		project.getProperties()
 				.setProperty("jetty.conf-plugin.classpath", classpath);
 
 		getLog().info(
 				"Generated properties 'jetty.conf-plugin.classpath' and 'jetty.conf-plugin.webapp'");
     }
 
 }
