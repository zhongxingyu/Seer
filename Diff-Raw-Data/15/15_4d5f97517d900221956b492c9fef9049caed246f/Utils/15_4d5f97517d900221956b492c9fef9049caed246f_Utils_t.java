 /*
  * Maven Packaging Plugin,
  * Maven plugin to package a Project (deb, ipk, izpack)
  * Copyright (C) 2000-2008 tarent GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License,version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  *
  * tarent GmbH., hereby disclaims all copyright
  * interest in the program 'Maven Packaging Plugin'
  * Signature of Elmar Geese, 11 March 2008
  * Elmar Geese, CEO tarent GmbH.
  */
 
 package de.tarent.maven.plugins.pkg;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.filefilter.FileFilterUtils;
 import org.apache.commons.io.filefilter.IOFileFilter;
 import org.apache.commons.io.filefilter.NotFileFilter;
 import org.apache.commons.io.filefilter.SuffixFileFilter;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
 import org.apache.maven.model.License;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.artifact.InvalidDependencyVersionException;
 import org.codehaus.plexus.archiver.ArchiveFile;
 import org.codehaus.plexus.archiver.ArchiveFile.Entry;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.UnArchiver;
 import org.codehaus.plexus.archiver.manager.ArchiverManager;
 import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
 
 import de.tarent.maven.plugins.pkg.annotations.MergeMe;
 import de.tarent.maven.plugins.pkg.merger.CollectionMerger;
 import de.tarent.maven.plugins.pkg.merger.IMerge;
 import de.tarent.maven.plugins.pkg.merger.ObjectMerger;
 import de.tarent.maven.plugins.pkg.merger.PropertiesMerger;
 import de.tarent.maven.plugins.pkg.packager.DebPackager;
 import de.tarent.maven.plugins.pkg.packager.IpkPackager;
 import de.tarent.maven.plugins.pkg.packager.IzPackPackager;
 import de.tarent.maven.plugins.pkg.packager.Packager;
 import de.tarent.maven.plugins.pkg.packager.RPMPackager;
 
 /**
  * A bunch of method with often used functionality. There is nothing special
  * about them they just make other code more readable.
  * 
  * @author Robert Schuster (robert.schuster@tarent.de)
  */
 public final class Utils {
 	private static final String STARTER_CLASS = "_Starter.class";
 
 	  /**
 	   * Look up Archiver/UnArchiver implementations.
 	   * @component role="org.codehaus.plexus.archiver.manager.ArchiverManager"
 	   * @required
 	   * @readonly
 	   */
 	  protected static ArchiverManager archiverManager;
 
 	/**
 	 * File filter that ignores files ending with "~", ".cvsignore" and CVS and
 	 * SVN files.
 	 */
 	public static final IOFileFilter FILTER = FileFilterUtils.makeSVNAware(FileFilterUtils
 			.makeCVSAware(new NotFileFilter(new SuffixFileFilter(new String[] { "~", ".cvsignore" }))));
 
 	public static void createParentDirs(File f, String item) throws MojoExecutionException {
 		File p = f.getParentFile();
 		if (!p.exists() && !p.mkdirs()){
 			throw new MojoExecutionException("Cannot create parent dirs for the " + item + " .");
 		}
 	}
 
 	/**
 	 * Creates a file and the file's parent directories.
 	 * 
 	 * @param f
 	 * @param item
 	 * @throws MojoExecutionException
 	 */
 	public static void createFile(File f, String item) throws MojoExecutionException {
 		try {
 			createParentDirs(f, item);
 
 			f.createNewFile();
 		} catch (IOException ioe) {
 			throw new MojoExecutionException("IOException while creating " + item + " file.", ioe);
 		}
 	}
 
 	/**
 	 * This wraps doing a "chmod +x" on a file + logging.
 	 * 
 	 * @param l
 	 * @param f
 	 * @param item
 	 * @throws MojoExecutionException
 	 */
 	public static void makeExecutable(Log l, String f) throws MojoExecutionException {
 		l.info("make executable " + f);
 		exec(new String[] { "chmod", "+x", f }, "Changing the " + f + " file attributes failed.", "Unable to make " + f
 				+ " file executable.");
 
 	}
 
 	/**
 	 * This wraps doing a "chmod +x" on a file.
 	 * 
 	 * @param f
 	 * @param item
 	 * @throws MojoExecutionException
 	 */
 	public static void makeExecutable(File f, String item) throws MojoExecutionException {
 		exec(new String[] { "chmod", "+x", f.getAbsolutePath() }, "Changing the " + item + " file attributes failed.",
 				"Unable to make " + item + " file executable.");
 
 	}
 
 	/**
 	 * Checks whether a certain program is available.
 	 * 
 	 * <p>
 	 * It fails with a {@link MojoExecutionException} if the program is not
 	 * available.
 	 * 
 	 * @param programName
 	 * @throws MojoExecutionException
 	 */
 	public static void checkProgramAvailability(String programName) throws MojoExecutionException {
 		exec(new String[] { "which", programName }, null, programName
 				+ " is not available on your system. Check your installation!", "Error executing " + programName
 				+ ". Aborting!",null);
 	}
 
 	/**
 	 * Checks whether a certain program is available.
 	 * 
 	 * <p>
 	 * It fails with a {@link MojoExecutionException} if the program is not
 	 * available.
 	 * 
 	 * @param programName
 	 * @throws MojoExecutionException
 	 */
 	public static String getProgramVersionOutput(String programName) throws MojoExecutionException {
 		return inputStreamToString(exec(new String[] { programName, "--version" }, null,
 				    " dpkg-deb is not available on your system. Check your installation!", "Error executing dpkg-deb"
 				    + ". Aborting!",null)).trim();
 	}
 
	private static String inputStreamToString(InputStream in) throws MojoExecutionException {
 		
 		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
 		StringBuilder stringBuilder = new StringBuilder();
 		String line = null;
 		try{
 			while ((line = bufferedReader.readLine()) != null) {
 			stringBuilder.append(line + "\n");
 			}
 		}catch(IOException ex){
			throw new MojoExecutionException("Error reading input stream");
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				throw new MojoExecutionException("Error closing reader");
			}
		}
 		return stringBuilder.toString();
 	}
 
 	/**
 	 * A method which makes executing programs easier.
 	 * 
 	 * @param args
 	 * @param failureMsg
 	 * @param ioExceptionMsg
 	 * @throws MojoExecutionException
 	 */
 	public static void exec(String[] args, String failureMsg, String ioExceptionMsg) throws MojoExecutionException {
 		exec(args, null, failureMsg, ioExceptionMsg, null);
 	}
 	
 	public static InputStream exec(String[] args, File workingDir, String failureMsg, 
 			   String ioExceptionMsg) throws MojoExecutionException {
 		return exec(args, workingDir, failureMsg, ioExceptionMsg, null);
 	}
 	
 	public static InputStream exec(String[] args, String failureMsg, 
 			   String ioExceptionMsg, String userInput) throws MojoExecutionException {
 		return exec(args, null, failureMsg, ioExceptionMsg, userInput);
 	}
 	/**
 	 * A method which makes executing programs easier.
 	 * 
 	 * @param args
 	 * @param failureMsg
 	 * @param ioExceptionMsg
 	 * @throws MojoExecutionException
 	 */
 	public static InputStream exec(String[] args, File workingDir, String failureMsg, 
 								   String ioExceptionMsg, String userInput)
 			throws MojoExecutionException {
 		/*
 		 * debug code which prints out the execution command-line. Enable if
 		 * neccessary. for(int i=0;i<args.length;i++) { System.err.print(args[i]
 		 * + " "); } System.err.println();
 		 */
 		
 		// Creates process with the defined language setting of LC_ALL=C
 		// That way the textual output of certain commands is predictable.
 		ProcessBuilder pb = new ProcessBuilder(args);
 		pb.directory(workingDir);
 		Map<String, String> env = pb.environment();
 		env.put("LC_ALL", "C");
 
 		Process p = null;
 		
 		try {
 			
 			p = pb.start();
 			
 			if(userInput!=null){
 				PrintWriter writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(p.getOutputStream())), true);
 				writer.println(userInput);
 				writer.flush();
 				writer.close();
 			}
 			int exitValue = p.waitFor();
 			if (exitValue != 0) {				
 				print(p);
 				throw new MojoExecutionException(String.format("(Subprocess exit value = %s) %s", exitValue, failureMsg));
 			}
 		} catch (IOException ioe) {
 			throw new MojoExecutionException(ioExceptionMsg + " :" + ioe.getMessage(), ioe);
 		} catch (InterruptedException ie) {
 			// Cannot happen.
 			throw new MojoExecutionException("InterruptedException", ie);
 		}
 		return p.getInputStream();
 	}
 
 	/**
 	 * Stores the contents of an input stream in a file. This is used to copy a
 	 * resource from the classpath.
 	 * 
 	 * @param is
 	 * @param file
 	 * @param ioExceptionMsg
 	 * @throws MojoExecutionException
 	 */
 	public static void storeInputStream(InputStream is, File file, String ioExceptionMsg) throws MojoExecutionException {
 		if (is == null){
 			throw new MojoExecutionException("InputStream must not be null.");
 		}
 		try {
 			FileOutputStream fos = new FileOutputStream(file);
 
 			IOUtils.copy(is, fos);
 
 			is.close();
 			fos.close();
 		} catch (IOException ioe) {
 			throw new MojoExecutionException(ioExceptionMsg, ioe);
 		}
 	}
 
 	/**
 	 * This method can be used to debug the output of processes.
 	 * 
 	 * @param p
 	 */
 	public static void print(Process p) {
 		BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
 		try {
 			while (br.ready()) {
 				System.err.println("*** Process output ***");
 				System.err.println(br.readLine());
 				System.err.println("**********************");
 			}
 		} catch (IOException ioe) {
 			// foo
 		}
 
 	}
 
 	public static long copyProjectArtifact(Log l, File src, File dst) throws MojoExecutionException {
 		
 		if (l!=null){
 			l.info("copying artifact: " + src.getAbsolutePath());
 			l.info("destination: " + dst.getAbsolutePath());
 		}
 		Utils.createFile(dst, "destination artifact");
 
 		try {
 			FileUtils.copyFile(src, dst);
 	        return src.length();
 		} catch (IOException ioe) {
 			throw new MojoExecutionException("IOException while copying artifact file.", ioe);
 		}
 
 	}
 
 	/**
 	 * Convert the artifactId into a Debian package name which contains gcj
 	 * precompiled binaries.
 	 * 
 	 * @param artifactId
 	 * @return
 	 */
 	public static String gcjise(String artifactId, String section, boolean debianise) {
 		return debianise && section.equals("libs") ? "lib" + artifactId + "-gcj" : artifactId + "-gcj";
 	}
 
 	/**
 	 * Concatenates two dependency lines. If both parts (prefix and suffix) are
 	 * non-empty the lines will be joined with a comma in between.
 	 * <p>
 	 * If one of the parts is empty the other will be returned.
 	 * </p>
 	 * 
 	 * @param prefix
 	 * @param suffix
 	 * @return
 	 */
 	public static String joinDependencyLines(String prefix, String suffix) {
 		return (prefix.length() == 0) ? suffix : (suffix.length() == 0) ? prefix : prefix + ", " + suffix;
 	}
 
 	public static long copyFiles(Log l, File srcDir, File dstDir, List<? extends AuxFile> auxFiles, String type)
 			throws MojoExecutionException {
 		return copyFiles(l, srcDir, dstDir, auxFiles, type, false);
 	}
 
 	/**
 	 * Copies the <code>AuxFile</code> instances contained within the set. It
 	 * takes the <code>srcAuxFilesDir</code> and <code>auxFileDstDir</code>
 	 * arguments into account to specify the parent source and destination
 	 * directory of the files.
 	 * 
 	 * By default files are copied into directories. If the <code>rename</code>
 	 * property of the <code>AuxFile</code> instance is set however the file is
 	 * copied and renamed to the last part of the path.
 	 * 
 	 * The return value is the amount of copied bytes.
 	 * 
 	 * @param l
 	 * @param srcAuxFilesDir
 	 * @param dstDir
 	 * @param auxFiles
 	 * @param makeExecutable
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	public static long copyFiles(Log l, File srcDir, File dstDir, List<? extends AuxFile> auxFiles, String type,
 			boolean makeExecutable) throws MojoExecutionException {
 		long size = 0;
 
 		Iterator<? extends AuxFile> ite = auxFiles.iterator();
 		while (ite.hasNext()) {
 			AuxFile af = (AuxFile) ite.next();
 			File from = new File(srcDir, af.from);
 			File to = new File(dstDir, af.to);
 
 			l.info("copying " + type + ": " + from.toString());
 			l.info("destination: " + to.toString());
 
 			if (!from.exists()){
 				throw new MojoExecutionException("File to copy does not exist: " + from.toString());
 			}
 			createParentDirs(to, type);
 
 			try {
 				if (from.isDirectory()) {
 					to = new File(to, from.getName());
 					FileUtils.copyDirectory(from, to, FILTER);
 	                for (final Iterator<File> files = FileUtils.iterateFiles(from, FILTER, FILTER); files.hasNext(); ) {
 	                	final File nextFile = files.next();
 	                    size += nextFile.length();
 	                }
 				} else if (af.isRename()) {
 					FileUtils.copyFile(from, to);
 					size += from.length();
 
 					if (makeExecutable){
 						makeExecutable(l, to.getAbsolutePath());
 					}
 				} else {
 					FileUtils.copyFileToDirectory(from, to);
 					size += from.length();
 
 					if (makeExecutable){
 						makeExecutable(l, to.getAbsolutePath() + File.separator + from.getName());
 					}
 				}
 			} catch (IOException ioe) {
 				throw new MojoExecutionException("IOException while copying " + type, ioe);
 			}
 		}
 
 		return size;
 	}
 
 	/**
 	 * Converts the artifactId into a package name. Currently this only
 	 * applies to libraries which get a "lib" prefix and a "-java" suffix.
 	 * 
 	 * <p>An optional packageNameSuffix can be specified which is appended
 	 * to the artifact id.</p>
 	 * 
 	 * <p>When <code>debianise</code> is set the name will be lowercased.</p>
 	 * 
 	 * @param artifactId
 	 * @return
 	 */
 	public static String createPackageName(String artifactId, String packageNameSuffix, String section, boolean debianise) {
 		String baseName = (packageNameSuffix != null) ? (artifactId + "-" + packageNameSuffix) : artifactId;
 		if (debianise) {
 			// Debian naming does not allow any uppercase characters.
 			baseName = baseName.toLowerCase();
 			
 			// Debian java libraries are called lib${FOO}-java
 			if (section.equals("libs")) {
 				baseName = "lib" + baseName + "-java";
 			}
 		}
 		
 		return baseName;
 	}
 	
 	/**
 	 * Batch creates the package names for the given {@link TargetConfiguration} instances using the method {@link #createPackageName}.
 	 * 
 	 * @param artifactId
 	 * @param tcs
 	 * @param debianise
 	 * @return
 	 */
 	public static List<String> createPackageNames(String artifactId, List<TargetConfiguration> tcs, boolean debianise) {
 		ArrayList<String> pns = new ArrayList<String>(tcs.size());
 		
 		for (TargetConfiguration tc : tcs) {
 			pns.add(createPackageName(artifactId, tc.getPackageNameSuffix(), tc.getSection(), debianise));
 		}
 		
 		return pns;
 	}
 
 	/**
 	 * Copies the starter classfile to the starter path, prepares the classpath
 	 * properties file and stores it at that location, too.
 	 * 
 	 * @param dstStarterRoot
 	 * @param dependencies
 	 * @param libraryPrefix
 	 * @throws MojoExecutionException
 	 */
 	public static void setupStarter(Log l, String mainClass, File dstStarterRoot, Path classpath)
 			throws MojoExecutionException {
 		File destStarterClassFile = new File(dstStarterRoot, STARTER_CLASS);
 
 		Utils.createFile(destStarterClassFile, "starter class");
 		Utils.storeInputStream(Utils.class.getResourceAsStream("/" + STARTER_CLASS), destStarterClassFile,
 				"Unable to store starter class file in destination.");
 
 		File destClasspathFile = new File(dstStarterRoot, "_classpath");
 		Utils.createFile(destClasspathFile, "starter classpath");
 
 		PrintWriter writer = null;
 		try {
 			writer = new PrintWriter(destClasspathFile);
 
 			writer.println("# This file controls the application's classpath and is autogenerated.");
 			writer.println("# Slashes (/) in the filenames are replaced with the platform-specific");
 			writer.println("# separator char at runtime.");
 			writer.println("# The next line is the fully-classified name of the main class:");
 			writer.println(mainClass);
 			writer.println("# The following lines are the classpath entries:");
 
 			for (String e : classpath) {
 				writer.println(e);
 			}
 
 			l.info("created library entries");
 		} catch (IOException e) {
 			throw new MojoExecutionException("storing the classpath entries failed", e);
 		} finally {
 			if (writer != null){
 				writer.close();
 			}
 		}
 	}
 
 	/**
 	 * Copies the Artifacts contained in the set to the folder denoted by
 	 * <code>dst</code> and returns the amount of bytes copied.
 	 * 
 	 * <p>
 	 * If an artifact is a zip archive it is unzipped in this folder.
 	 * </p>
 	 * 
 	 * @param l
 	 * @param artifacts
 	 * @param dst
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	public static long copyArtifacts(Log l, Set<Artifact> artifacts, File dst) throws MojoExecutionException {
 		long byteAmount = 0;
 
 		if (artifacts.size() == 0) {
 			l.info("no artifact to copy.");
 			return byteAmount;
 		}
 
 		l.info("copying " + artifacts.size() + " dependency artifacts.");
 		l.info("destination: " + dst.toString());
 
 		try {
 			Iterator<Artifact> ite = artifacts.iterator();
 			while (ite.hasNext()) {
 				Artifact a = (Artifact) ite.next();
 				l.info("copying artifact: " + a);
 				File f = a.getFile();
 				if (f != null) {
 					l.debug("from file: " + f);
 					if (a.getType().equals("zip")) {
 						// Assume that this is a ZIP file with native libraries
 						// inside.
 
 						// TODO: Determine size of all entries and add this
 						// to the byteAmount.
 						unpack(a.getFile(), dst);
 					} else {
 						FileUtils.copyFileToDirectory(f, dst);
 						byteAmount += (long) f.length();
 					}
 
 				} else {
 					throw new MojoExecutionException("Unable to copy Artifact " + a
 							+ " because it is not locally available.");
 				}
 			}
 		} catch (IOException ioe) {
 			throw new MojoExecutionException("IOException while copying dependency artifacts.", ioe);
 		}
 
 		return byteAmount;
 	}
 
 	/**
 	 * Unpacks the given file.
 	 * 
 	 * @param file
 	 *            File to be unpacked.
 	 * @param dst
 	 *            Location where to put the unpacked files.
 	 */
 	protected static void unpack(File file, File dst) throws MojoExecutionException {
 		try {
 			dst.mkdirs();
 
 			UnArchiver unArchiver = archiverManager.getUnArchiver(file);
 			unArchiver.setSourceFile(file);
 			unArchiver.setDestDirectory(dst);
 			unArchiver.extract();
 		} catch (NoSuchArchiverException e) {
 			throw new MojoExecutionException("Unknown archiver type", e);
 		} catch (ArchiverException e) {
 			throw new MojoExecutionException("Error unpacking file: " + file + " to: " + dst + "\r\n" + e.toString(), e);
 		}
 	}
 
 	/**
 	 * Makes a valid and useful package version string from one that cannot be
 	 * used or is unsuitable.
 	 * 
 	 * <p>
 	 * At the moment the method removes underscores only.
 	 * </p>
 	 * 
 	 * @param string
 	 *            The string which might contain underscores
 	 * @return The given string without underscores
 	 */
 	public static String sanitizePackageVersion(String string) {
 
 		return string.replaceAll("_", "");
 	}
 
 	/**
 	 * Gathers the project's artifacts and the artifacts of all its (transitive)
 	 * dependencies filtered by the given filter instance.
 	 * 
 	 * @param filter
 	 * @return
 	 * @throws ArtifactResolutionException
 	 * @throws ArtifactNotFoundException
 	 * @throws ProjectBuildingException
 	 * @throws InvalidDependencyVersionException
 	 */
 	@SuppressWarnings("unchecked")
 	public static Set<Artifact> findArtifacts(ArtifactFilter filter, 
 										  ArtifactFactory factory,
 										  ArtifactResolver resolver,
 										  MavenProject project, 
 										  Artifact artifact,
 										  ArtifactRepository local, 
 										  List<ArtifactRepository> remoteRepos,
 										  ArtifactMetadataSource metadataSource)
 					throws ArtifactResolutionException,ArtifactNotFoundException, 
 					ProjectBuildingException,InvalidDependencyVersionException {
 
 		ArtifactResolutionResult result = resolver.resolveTransitively(project.getDependencyArtifacts(), 
 																		artifact,
 																		local,
 																		remoteRepos,
 																		metadataSource,
 																		filter);
 
 		return (Set<Artifact>) result.getArtifacts();
 	}
 	
 	/**
 	 * Returns a TargetConfiguration object that matches the desired target String.
 	 * @param target
 	 * @param targetConfigurations
 	 * @return
 	 * @throws MojoExecutionException 
 	 */
 	public static TargetConfiguration getTargetConfigurationFromString(String target, 
 											List<TargetConfiguration> targetConfigurations) throws MojoExecutionException{
 		
 		for (TargetConfiguration currentTargetConfiguration : targetConfigurations) {
 			if (currentTargetConfiguration.getTarget().equals(target)) {
 				return currentTargetConfiguration;
 			}
 		}
 		throw new MojoExecutionException("Target " + target + " not found. Check your spelling or configuration (is this target being defined in relation to another, but does not exist anymore?).");
 	}
 	
 	  /**
 	   * Returns the default Distro to use for a certain TargetConfiguration.
 	   * @param configuration
 	   * @return
 	   * @throws MojoExecutionException 
 	   */
 	public static String getDefaultDistro(String targetString, List<TargetConfiguration> targetConfigurations, Log l) throws MojoExecutionException {
 		String distro = null;
 		TargetConfiguration target = Utils.getTargetConfigurationFromString(targetString, targetConfigurations);
 		
 		if (target.getDefaultDistro() != null) {
 			distro = target.getDefaultDistro();
 			l.info("Default distribution is set to \"" + distro + "\".");
 		} else 
 			switch (target.getDistros().size()) {
 			case 0:
 				throw new MojoExecutionException(
 						"No distros defined for configuration " + targetString);
 			case 1:
 				distro = (String) target.getDistros().iterator().next();
 				l.info(String.format("Only one distro defined, using '%s' as default", distro));
 				break;
 			default:
 				String m = "No default configuration given for distro '"
 						+ targetString
 						+ "', and more than one distro is supported. Please provide one.";
 				l.error(m);
 				throw new MojoExecutionException(m);
 			}
 		return distro;
 	}
 	
 	/**
 	 * Returns a string with the license(s) of a MavenProject.
 	 * 
 	 * @param project
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	@SuppressWarnings("unchecked")
 	public static String getConsolidatedLicenseString(MavenProject project) throws MojoExecutionException {
 		StringBuilder license = new StringBuilder();
 		Iterator<License> ite = null;
 		try {
 			ite = (Iterator<License>) project.getLicenses().iterator();
 			license.append(((License) ite.next()).getName());
 		} catch (Exception ex) {
 			throw new MojoExecutionException("Please provide at least one license in your POM.",ex);
 		}
 		while (ite.hasNext()) {
 			license.append(", ");
 			license.append(((License) ite.next()).getName());
 		}
 		return license.toString();
 
 	}
 	
 	/**
 	 * Simple function to grab information from an URL
 	 * @param url
 	 * @return
 	 * @throws IOException 
 	 */
 	public static String getTextFromUrl(String url) throws IOException{
 		InputStream stream = null;
 		try {
 			stream = new URL(url).openStream();
 			
 			return IOUtils.toString(stream);
 		} finally {
 			IOUtils.closeQuietly(stream);
 		}
 	}
 
 	/**
 	 * Converts a byte amount to the unit used by the Debian control file
 	 * (usually KiB). That value can then be used in a ControlFileGenerator
 	 * instance.
 	 * 
 	 * @param byteAmount
 	 * @return
 	 */
 	public static long getInstalledSize(long byteAmount) {
 		return byteAmount / 1024L;
 	}
 	  
 	  /**
 	   * A <code>TargetConfiguration</code> can depend on another and so multiple
 	   * build processes might need to be run for a single <code>TargetConfiguration</code>.
 	   * 
 	   * <p>The list of <code>TargetConfiguration</code> instance that need to be built is
 	   * called a build chain. A build chain with <code>n</code> entries contains <code>n-1</code>
 	   * instances that need to be built before. The last element in the build chain is the
 	   * one that was initially requested and must be build last.</p>
 	   * 
 	   * @param target
 	   * @param distro
 	   * @return
 	   * @throws MojoExecutionException
 	   */
 	  public static List<TargetConfiguration> createBuildChain(
 			  String target,
 			  String distro,
 			  List<TargetConfiguration> targetConfigurations)
 	  	  throws MojoExecutionException
 	  {
 		  LinkedList<TargetConfiguration> tcs = new LinkedList<TargetConfiguration>();
 		  
 		  // Merges vertically, that means through the 'parent' property.
 		  TargetConfiguration tc = Utils.getTargetConfigurationFromString(target, targetConfigurations); 
 				  //Utils.getMergedConfigurationImpl(target, distro, targetConfigurations, true);
 		  
 		  // In getMergedConfiguraion we check if targets that are hierarchically related
 		  // support the same distro. Here we will have to check again, as there may not
 		  // be parent-child relationship between them.
 		  
 		  if(tc.getDistros().contains(distro)){
 			  tcs.addFirst(tc);
 			  
 			  List<String> relations = tc.getRelations();
 			  for (String relation : relations) {
 				  tcs.addAll(0, createBuildChain(relation, distro, targetConfigurations));
 			  }		  
 		  	  return tcs;
 		  }else{
 			  throw new MojoExecutionException("target configuration '" + tc.getTarget() + 
 					                           "' does not support distro: " + distro);
 		  }
 	  }
 
 	  /**
 	   * Returns a Packager Object for a certain packaging type (deb, rpm, etc.)
 	   * 
 	   * <p>Conveniently throws a {@link MojoExecutionException} if there is no
 	   * {@link Packager} instance for the given package type.</p>
 	   *  
 	   * @param packaging
 	   * @return
 	   */
 	  public static Packager getPackagerForPackaging(String packaging) 
 			  throws MojoExecutionException {
 		    Map<String, Class<? extends Packager>> extPackagerMap = new HashMap<String, Class<? extends Packager>>();
 		    extPackagerMap.put("deb", DebPackager.class);
 		    extPackagerMap.put("ipk", IpkPackager.class);
 		    extPackagerMap.put("izpack", IzPackPackager.class);
 		    extPackagerMap.put("rpm", RPMPackager.class);
 		    
 		    Class<? extends Packager> klass = extPackagerMap.get(packaging);
 		    try {
 		    	return klass.newInstance();
 		    } catch (InstantiationException e) {
 			      throw new MojoExecutionException("Unsupported packaging type: "+ packaging, e);
 			} catch (IllegalAccessException e) {
 			      throw new MojoExecutionException("Unsupported packaging type: "+ packaging, e);
 			}
 	  }
 	  
 	  /**
 	   * Conveniently converts a list of target configurations into a map where
 	   * each instance can be accessed by its name (ie. the target property).
 	   * 
 	   * <p>Super conveniently this method throws an exception if during the
 	   * conversion it is found out that there is more than one entry with the
 	   * same target.</p>
 	   * 
 	   * @param tcs
 	   */
 	  public static Map<String, TargetConfiguration> toMap(List<TargetConfiguration> tcs)
 		  throws MojoExecutionException {
 		  HashMap<String, TargetConfiguration> m = new HashMap<String, TargetConfiguration>();
 		  
 		  for (TargetConfiguration tc : tcs) {
 			  if (m.put(tc.getTarget(), tc) != null) {
 				  throw new MojoExecutionException("Target with name '" + tc.getTarget() + " exists more than once. Fix the plugin configuration!");
 			  }
 		  }
 		  
 		  return m;
 	  }
 	  
 	  /**
 	   * Given a list of target names resolves them into their actual {@link TargetConfiguration}
 	   * instances. The resolution is done using the given map.
 	   * 
 	   * <p>Additionally this method throws an exception if an entry could not be found in the map
 	   * as this means something is configured wrongly.</p>
 	   * 
 	   * @param targetNames
 	   * @param map
 	   * @return
 	   * @throws MojoExecutionException
 	   */
 	  public static List<TargetConfiguration> resolveConfigurations(List<String> targetNames, Map<String, TargetConfiguration> map) 
 	  	throws MojoExecutionException {
 		  ArrayList<TargetConfiguration> tcs = new ArrayList<TargetConfiguration>(targetNames.size());
 		  
 		  for (String s : targetNames) {
 			  TargetConfiguration tc = map.get(s);
 			  if (tc == null) {
 				  throw new MojoExecutionException("Target configuration '" + tc + "' is requested as a related target configuration but does not exist. Fix the plugin configuration!");
 			  }
 			  tcs.add(tc);
 		  }
 		  
 		  return tcs;
 	  }
 	  
 	  /**
 		* Sets all unset properties, either to the values of the parent or to a
 		* (hard-coded) default value if the property is not set in the parent.
 		* 
 		* <p>
 		* Using this method the packaging plugin can generate a merge of the
 		* default and a distro-specific configuration.
 		* </p>
 		*	   
 		* @param child
 		* @param parent
 		* @return
 		* @throws MojoExecutionException
 		*/
 	  public static TargetConfiguration mergeConfigurations(TargetConfiguration child, 
 			  TargetConfiguration parent) throws MojoExecutionException{
 		  
 		  if (child.isReady()){
 			  throw new MojoExecutionException(String.format("target configuration '%s' is already merged.", child.getTarget()));
 		  }
 			
 			Field[] allFields = TargetConfiguration.class.getDeclaredFields();
 			for (Field field : allFields){
 				field.setAccessible(true);
 				if(field.getAnnotation(MergeMe.class) != null){
 					try {
 						Object defaultValue = new Object();						
 						IMerge merger;
 
 						if(field.getAnnotation(MergeMe.class).defaultValueIsNull()){
 							defaultValue=null;
 						}
 						
 						if(field.getType()==Properties.class){
 							if(defaultValue!=null){
 								defaultValue = new Properties();								
 							}
 							merger = new PropertiesMerger();
 							
 						}else if(field.getType()==List.class){
 							if(defaultValue!=null){
 								defaultValue = new ArrayList<Object>();
 							}
 							merger = new CollectionMerger();
 							
 						}else if(field.getType()==Set.class){
 							if(defaultValue!=null){								
 								defaultValue = new HashSet<Object>();								
 							}
 							merger = new CollectionMerger();
 							
 						}else if(field.getType()==String.class){
 							if(defaultValue!=null){
 								defaultValue =field.getAnnotation(MergeMe.class).defaultString();								
 							}
 							merger = new ObjectMerger();
 							
 						}else if(field.getType()==Boolean.class){							
 							defaultValue = field.getAnnotation(MergeMe.class).defaultBoolean();
 							merger = new ObjectMerger();
 							
 						}else{
 							merger = new ObjectMerger();
 						}
 						Object childValue = field.get(child);
 						Object parentValue =  field.get(parent);
 						try {
 							field.set(child, merger.merge(childValue,
 											       		  parentValue,
 											       		  defaultValue));
 						} catch (InstantiationException e) {
 							throw new MojoExecutionException("Error merging configurations",e);
 						}
 				
 					}catch (SecurityException e){
 						throw new MojoExecutionException(e.getMessage(),e);
 					}catch (IllegalArgumentException e){
 						throw new MojoExecutionException(e.getMessage(),e);
 					}catch (IllegalAccessException e){
 						throw new MojoExecutionException(e.getMessage(),e);
 					}
 						
 				}
 			}
 			child.setReady(true);
 			return child;
 	  }
 	  
 	/**
 	 * Recursively merges all configurations with their parents and returns a
 	 * list of the available targetConfigurations ready to be consumed by the
 	 * plugin.
 	 * 
 	 * @param targetConfigurations
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	public static void mergeAllConfigurations(List<TargetConfiguration> targetConfigurations) 
 			throws MojoExecutionException {
 
 		// We will loop through all targets
 		for (TargetConfiguration tc : targetConfigurations) {
 			// If tc is ready it means that this list has already been merged
 			if (tc.isReady()) {
 				throw new MojoExecutionException("This targetConfiguration list has already been merged.");
 			} else {
 				// Check if we are at the top of the hierarchy
 				if (tc.parent == null) {
 					// If we are at the top we will just fixate the configuration
 					tc.fixate();
 				} else {
 					// If there is a parent we will merge recursivelly tc's hierarchy
 					mergeAncestorsRecursively(tc,targetConfigurations);
 				}
 			}				
 		}
 	}
 
 	/**
 	 * Recursively merge the ancestors for a certain configuration.
 	 * 
 	 * @param tc
 	 * @param parent
 	 * @param targetConfigurations
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	private static void mergeAncestorsRecursively(TargetConfiguration tc, List<TargetConfiguration> targetConfigurations) throws MojoExecutionException {
 		
 		TargetConfiguration parent = getTargetConfigurationFromString(tc.parent, targetConfigurations);
 		
 		// If the top of the hierarchy has not been reached yet, 
 		// all remaining ancestors will be merged. 
 		// The ready flag of the parent will also be checked 
 		// in order to avoid redundant work.
 		
 		if (parent.parent != null && !parent.isReady()) {
 			mergeAncestorsRecursively(parent, targetConfigurations);
 		}
 		// Once done checking all ancestors 
 		// the child will be merged with the parent
 		mergeConfigurations(tc, parent);
 	}
 	
 	/**
 	 * Extracts a file from an archive and stores it in a temporary location.
 	 * The file will be deleted when the virtual machine exits.
 	 * 
 	 * @param archive
 	 * @param needle
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	@SuppressWarnings("unchecked")
 	public static File getFileFromArchive(ArchiveFile archive, String needle) throws MojoExecutionException{
 		
 			
 			final int BUFFER = 2048;
 			BufferedOutputStream dest = null;
 			BufferedInputStream is = null;
 			Entry entry;
 
 			Enumeration<? extends Entry> e;
 			try {
 				e = archive.getEntries();
 			} catch (IOException ex) {
 				throw new MojoExecutionException("Error getting entries from archive",ex);
 			}
 			
 			while (e.hasMoreElements()){
 				entry = e.nextElement();
 				// If the entry we are in matches the needle we will store its contents to a temp file
 				if (entry.getName().equals(needle)){
 					// The file will be saved in the temporary directory 
 					File tempDir = new File(System.getProperty("java.io.tmpdir"));
 					
 					File extractedFile;
 					try {
 						extractedFile = File.createTempFile("mvn-pkg-plugin",
 																"temp",
 																tempDir);
 					} catch (IOException ex) {
 						throw new MojoExecutionException("Error creating temporary file found",ex);
 					}
 					
 			        try {
 						is = new BufferedInputStream
 						  (archive.getInputStream(entry));
 					} catch (IOException ex) {
 						throw new MojoExecutionException("Error reading entry from archive",ex);
 					}
 			        
 			        int count;
 			        byte data[] = new byte[BUFFER];
 			        FileOutputStream fos;
 			        
 					try {
 						fos = new FileOutputStream(extractedFile);
 					} catch (FileNotFoundException ex) {
 						throw new MojoExecutionException("Error reading entry from archive",ex);
 					}
 					
 			        dest = new 
 			           BufferedOutputStream(fos, BUFFER);
 			        
 			        try {
 						while ((count = is.read(data, 0, BUFFER)) != -1) {
 						    dest.write(data, 0, count);
 						 }
 					} catch (IOException ex) {
 						throw new MojoExecutionException("Error writing to temporary file",ex);
 					}finally{
 				         try {
 							dest.flush();
 							dest.close();
 							is.close();
 						} catch (IOException ex) {
 							throw new MojoExecutionException("Error closing streams.",ex);
 						}						
 					}
 			         extractedFile.deleteOnExit();
 					return extractedFile;
 				}
 			}
 		throw new MojoExecutionException("Desired file not found");
 	}
 
 	/**
 	 * Tries to match a string representing a debian package name against the convention.
 	 * <a href="http://www.debian.org/doc/debian-policy/ch-controlfields.html#s-f-Source">http://www.debian.org/doc/debian-policy/ch-controlfields.html#s-f-Source</a>
 	 * @param string
 	 * @return True if matches
 	 */
 	public static boolean checkDebianPackageNameConvention(String string){
 		Pattern pattern = Pattern.compile("[a-z0-9][a-z0-9+.-]*[a-z0-9+.]"); 
 		Matcher m = pattern.matcher(string);
 		
 		if(m.matches()){
 			return true;
 		}else{
 			return false;
 		}		
 	}
 
 	/**
 	 * Tries to match a string representing a debian package version against the convention.<br/>
 	 * <a href="http://www.debian.org/doc/debian-policy/ch-controlfields.html#s-f-Version">http://www.debian.org/doc/debian-policy/ch-controlfields.html#s-f-Version</a>
 	 * @param string
 	 * @return True if matches
 	 */
 	public static boolean checkDebianPackageVersionConvention(String string){
 		boolean compliant = true;
 		Pattern pattern = Pattern.compile("[0-9][A-Za-z0-9.+~:-]*"); 
 		Matcher m = pattern.matcher(string);
 		
 		if(!m.matches()){
 			compliant = false;
 		}
 		
 		// A version must never end with a hyphen
 		if(string.endsWith("-")){
 			compliant = false;
 		}
 		
 		// A version should never contain a colon if it does not start with an epoch
 		Pattern epoch = Pattern.compile("^[0-9][0-9]*:.*"); 
 		m = epoch.matcher(string);		
 		if(!m.matches() && string.contains(":")){
 			compliant= false;
 		}
 		
 		return compliant;
 		
 	}	
 	
 	
 	
 }
