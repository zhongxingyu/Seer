 //
 // CppWrapMojo.java
 //
 
 /*
 C++ Wrapper Maven plugin for generating C++ proxy classes for a Java library.
 
 Copyright (c) 2011, UW-Madison LOCI
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the UW-Madison LOCI nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package loci.maven.plugin.cppwrap;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Arrays;
 
 import loci.jar2lib.Jar2Lib;
 import loci.jar2lib.VelocityException;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 /**
  * Goal which creates a C++ project wrapping a Maven Java project.
  *
  * Portions of this mojo were adapted from exec-maven-plugin's ExecJavaMojo.
  *
  * <dl><dt><b>Source code:</b></dt>
  * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/cppwrap-maven-plugin/src/main/java/loci/maven/plugin/cppwrap/CppWrapMojo.java">Trac</a>,
  * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/cppwrap-maven-plugin/src/main/java/loci/maven/plugin/cppwrap/CppWrapMojo.java">SVN</a></dd></dl>
  *
  * @author Curtis Rueden
  *
  * @goal wrap
  */
 public class CppWrapMojo extends AbstractMojo {
 
 	/**
 	 * The Maven project to wrap.
 	 *
 	 * @parameter expression="${project}"
 	 * @required
 	 * @readonly
 	 */
 	private MavenProject project;
 
 	/**
 	 * Additional dependencies to wrap as part of the C++ project.
 	 *
 	 * For example, if a project human:body:jar:1.0 depends on projects
 	 * human:head:jar:1.0, human:arms:jar:1.0 and human:legs:jar:1.0,
 	 * and you wish to wrap human and head, but not arms or legs,
 	 * you could specify human:head:jar:1.0 as an extra artifact here.
 	 *
 	 * @parameter expression="${cppwrap.libraries}"
 	 */
 	private String[] libraries;
 
 	/**
 	 * Path to conflicts list of Java constants to rename,
 	 * to avoid name collisions.
 	 *
 	 * @parameter expression="${cppwrap.conflictsFile}"
 	 *   default-value="src/main/cppwrap/conflicts.txt"
 	 */
 	private File conflictsFile;
 
 	/**
 	 * Path to header file to prepend to each C++ source file.
 	 *
 	 * @parameter expression="${cppwrap.headerFile}"
 	 *   default-value="LICENSE.txt"
 	 */
 	private File headerFile;
 
 	/**
 	 * Path to folder containing additional C++ source code.
 	 *
 	 * Each .cpp file in the folder should contain a main method.
 	 * These files will then be compiled as part of the build process,
 	 * as individual executables.
 	 *
 	 * @parameter expression="${cppwrap.sourceDir}"
 	 *   default-value="src/main/cppwrap"
 	 */
 	private File sourceDir;
 
 	/**
 	 * Path to output folder for C++ project.
 	 *
 	 * @parameter expression="${cppwrap.outputDir}"
 	 *   default-value="target/cppwrap"
 	 */
 	private File outputDir;
 
 	/**
 	 * Path to a text file listing core Java classes to be ensured
 	 * proxied.
 	 *
 	 * @parameter expression="${cppwrap.coreFile}"
 	 *   default-value="src/main/cppwrap/core.txt"
 	 */
 	private File coreFile;
 
 	/**
 	 * Path to text file, the contents of which will be
 	 * appended to resulting CMakeLists.txt for this project.
 	 *
 	 * @parameter expression="${cppwrap.extrasFile}"
 	 *   default-value="src/main/cppwrap/extras.txt"
 	 */
 	private File extrasFile;
 
 	@Override
 	public void execute() throws MojoExecutionException {
 		final String artifactId = project.getArtifactId();
 
 		final String projectId = artifactId.replaceAll("[^\\w\\-]", "_");
 		final String projectName = project.getName();
 		final List<String> libraryJars = getLibraryJars();
 		final List<String> classpathJars = getClasspathJars();
 		final String conflictsPath = conflictsFile.exists() ?
 			conflictsFile.getPath() : null;
 		final String headerPath = headerFile.exists() ?
 			headerFile.getPath() : null;
 		final String sourcePath = sourceDir.isDirectory() ?
 			sourceDir.getPath() : null;
 		final String outputPath = outputDir.getPath();
 		final String extrasPath = extrasFile.exists() ?
 				extrasFile.getPath() : null;
 		final String corePath = coreFile.exists() ?
 				coreFile.getPath() : null;
 
 		final Jar2Lib jar2lib = new Jar2Lib() {
 			@Override
 			protected void log(String message) {
 				getLog().info(message);
 			}
 		};
 		jar2lib.setProjectId(projectId);
 		jar2lib.setProjectName(projectName);
 		jar2lib.setLibraryJars(libraryJars);
 		jar2lib.setClasspathJars(classpathJars);
 		jar2lib.setConflictsPath(conflictsPath);
 		jar2lib.setHeaderPath(headerPath);
 		jar2lib.setSourcePath(sourcePath);
 		jar2lib.setOutputPath(outputPath);
 		jar2lib.setExtrasPath(extrasPath);
 		jar2lib.setCorePath(corePath);
 		try {
 			jar2lib.execute();
 		}
 		catch (IOException e) {
 			throw new MojoExecutionException("Error invoking jar2lib", e);
 		}
 		catch (VelocityException e) {
 			throw new MojoExecutionException("Error invoking jar2lib", e);
 		}
 	}
 
 	private List<String> getLibraryJars() throws MojoExecutionException {
 		final List<String> jars = new ArrayList<String>();
 
 		// add project artifact
		// TODO: Try project.getArtifacts()?
 		final File projectArtifact = project.getArtifact().getFile();
 		if (projectArtifact == null || !projectArtifact.exists()) {
 			throw new MojoExecutionException(
 				"Must execute package target first (e.g., mvn package cppwrap:wrap).");
 		}
 		jars.add(projectArtifact.getPath());
 
 		// add explicitly enumerated dependencies
 		if (libraries != null) {
 			@SuppressWarnings("unchecked")
 			final List<Artifact> artifacts = project.getRuntimeArtifacts();
 			ArrayList<String> libs = new ArrayList<String>(Arrays.asList(libraries));
 
 			Collections.sort(artifacts, new ArtComparator());
 			Collections.sort(libs);
 			int libIndex = 0;
 			int artIndex = 0;
 
 			boolean done = artIndex == artifacts.size();
 			while (!done)
 			{
 				if(libs.get(libIndex).compareTo(artifacts.get(artIndex).getId()) == 0)
 				{
 					File artifactFile = artifacts.get(artIndex).getFile();
 					if (!artifactFile.exists()) {
 						throw new MojoExecutionException("Artifact not found: " +
 							artifactFile);
 					}
 					jars.add(artifactFile.getPath());
 					libIndex++;
 				}
 				else
 				{
 					artIndex++;
 				}
 
 				if(artIndex == artifacts.size())
 				{
 					throw new MojoExecutionException("Invalid library dependency: " +
 							libs.get(libIndex));
 				}
 				done = libIndex == libraries.length;
 			}
 		}
 		return jars;
 	}
 
 	private List<String> getClasspathJars() {
 		final List<String> jars = new ArrayList<String>();
 
 		// add project runtime dependencies
 		@SuppressWarnings("unchecked")
 		final List<Artifact> artifacts = project.getRuntimeArtifacts();
 		for (final Artifact classPathElement : artifacts) {
 			jars.add(classPathElement.getFile().getPath());
 		}
 
 		return jars;
 	}
 
 	private class ArtComparator implements Comparator {
 		public int compare (Object obj1, Object obj2) {
 			Artifact art1 = (Artifact)obj1;
 			Artifact art2 = (Artifact)obj2;
 
 			return art1.getId().compareTo(art2.getId());
 		}
 	}
 
 }
