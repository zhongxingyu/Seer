 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.peterlavalle.degen.mojos;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.peterlavalle.degen.extractors.util.FileHook;
 import com.peterlavalle.degen.extractors.util.Files;
 import com.peterlavalle.degen.extractors.util.MasterURL;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 /**
  *
  * @author Peter LaValle
  * @goal degenerate
  * @phase generate-sources
  * @version $Id$
  */
 public class DegenMojo extends AMojo {
 
 	/**
 	 * Where do we look for things? Each of these strings should be "URL of a zip file [
 	 *
 	 * @some zip file inside of the URL] a regular expression of what to extract"
 	 *
 	 * @parameter expression="${sources}"
 	 * @required
 	 */
 	private String[] sources;
 
 	public File getCacheDir(final MavenProject project) {
 		return project.getParent() != null ? getCacheDir(project.getParent()) : new File(project.getBuild().getDirectory());
 	}
 
 	@Override
 	public void execute() throws MojoExecutionException, MojoFailureException {
 
 
 		final Map<String, FileHook> hooks = new TreeMap<String, FileHook>();
 
 		// collect all "normal" source files
 		for (final String src : (List<String>) getProject().getCompileSourceRoots()) {
 
 			if (src == null) {
 				continue;
 			}
 
 			for (final String sourceFile : scanFolderForSourceFiles(src, "")) {
 				hooks.put(sourceFile, null);
 			}
 		}
 
 		for (final String src : hooks.keySet()) {
 			getLog().info("src=\t" + src);
 		}
 
 
 		// find all hooks. uses MasterURL to determine which files from jars to include or exclude
 		for (final String source : sources) {
 			final MasterURL masterURL;
 			try {
 				masterURL = new MasterURL(source);
 			} catch (final MalformedURLException ex) {
 				throw new MojoExecutionException("MasterURL(`" + source + "`)", ex);
 			}
 			try {
 				for (final FileHook hook : masterURL.listFiles(getCacheDir(getProject()))) {
 					final String name = hook.getName();
 
 					// if we've already got this one, skip it
 					if (hooks.containsKey(name)) {
 						getLog().info("skip: source (" + source + ") contians an extra copy of `" + hook.getName() + "`");
 						continue;
 					}
 
 					// if this is a .class file and we've already got the .java file - skip this one
 					if (name.endsWith(".class") && hooks.containsKey(name.replaceAll("\\.class$", "\\.java"))) {
 						getLog().info("skip: class `" + hook.getName() + "` already has a source file");
 						continue;
 					}
 
 					// save it
 					hooks.put(name, hook);
 
 					// if this was a .java file, we may need to remove keys (sorry)
 					if (name.endsWith(".java")) {
 						final String replaceAll = name.replaceAll("\\.java", "");
 
						for (final String hookName : hooks.keySet()) {
 							if (!hookName.endsWith(".class")) {
 								continue;
 							}
 							if (!hookName.startsWith(replaceAll)) {
 								continue;
 							}
 							if (hookName.substring(replaceAll.length()).matches("^(\\$|\\.).*class$")) {
 								getLog().info("skip: removing class  `" + hook.getName() + "` becasuse of `" + name + "`");
 								hooks.remove(hookName);
 							}
 						}
 					}
 				}
 			} catch (final IOException ex) {
 				throw new MojoExecutionException("MasterURL(`" + source + "`).listFiles()", ex);
 			}
 		}
 
 		final Set<String> activeSources = Sets.newHashSet();
 		final Set<String> activeResources = Sets.newHashSet();
 		for (final FileHook hook : hooks.values()) {
 
 			// if it was a "normal" one - ignore
 			if (hook == null) {
 				continue;
 			}
 
 			final String name = hook.getName();
 			(hook.getName().matches(getSourceFilter()) ? activeSources : activeResources).add(name);
 
 			try {
 				pullHook(hook, hook.getName().matches(getSourceFilter()));
 			} catch (final IOException ex) {
 				throw new MojoExecutionException("Problem with `" + name + "`", ex);
 			}
 		}
 
 		getProject().addCompileSourceRoot(getGeneratedSources());
 		getProjectHelper().addResource(getProject(), getGeneratedResources(), new ArrayList(), Collections.singletonList("**/**.java"));
 	}
 
 	private void pullHook(FileHook hook, final boolean isSource) throws IOException {
 		final File finalName = new File(isSource ? getGeneratedSourcesFile() : getGeneratedResourcesFile(), hook.getName());
 
 		if (finalName.lastModified() < hook.lastModified()) {
 			Files.copyStream(hook.openInputStream(), finalName);
 		}
 	}
 
 	/**
 	 * Scans the folder for files matching the configured regular expression.
 	 *
 	 * @param folder the folder to scan, relative to the project's basedir
 	 *
 	 */
 	public List<String> scanFolderForSourceFiles(String root, String folder) {
 
 		final File folderFile = new File(root, folder);
 		final List<String> strings = Lists.newLinkedList();
 
 		if (folderFile.exists()) {
 			for (final File file : folderFile.listFiles()) {
 
 				if (file.isDirectory()) {
 					strings.addAll(scanFolderForSourceFiles(root, folder + file.getName() + '/'));
 				} else {
 					strings.add(folder + file.getName());
 				}
 			}
 		}
 
 		return strings;
 	}
 }
