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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TimeZone;
 
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
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.model.License;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.artifact.InvalidDependencyVersionException;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.UnArchiver;
 import org.codehaus.plexus.archiver.manager.ArchiverManager;
 import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
 
 import de.tarent.maven.plugins.pkg.helper.DebHelper;
 import de.tarent.maven.plugins.pkg.helper.Helper;
 import de.tarent.maven.plugins.pkg.helper.IpkHelper;
 import de.tarent.maven.plugins.pkg.helper.IzPackHelper;
 import de.tarent.maven.plugins.pkg.helper.RpmHelper;
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
 public class Utils {
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
 		if (!p.exists() && !p.mkdirs())
 			throw new MojoExecutionException("Cannot create parent dirs for the " + item + " .");
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
 				+ ". Aborting!");
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
 		exec(args, null, failureMsg, ioExceptionMsg);
 	}
 
 	/**
 	 * A method which makes executing programs easier.
 	 * 
 	 * @param args
 	 * @param failureMsg
 	 * @param ioExceptionMsg
 	 * @throws MojoExecutionException
 	 */
 	public static InputStream exec(String[] args, File workingDir, String failureMsg, String ioExceptionMsg)
 			throws MojoExecutionException {
 		/*
 		 * debug code which prints out the execution command-line. Enable if
 		 * neccessary. for(int i=0;i<args.length;i++) { System.err.print(args[i]
 		 * + " "); } System.err.println();
 		 */
 
 		Process p = null;

 		try {
			p = Runtime.getRuntime().exec(args, null, workingDir);
 
 			if (p.waitFor() != 0) {
 				print(p);
 				throw new MojoExecutionException(failureMsg);
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
 		if (is == null)
 			throw new MojoExecutionException("InputStream must not be null.");
 
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
 
 	public static final long copyProjectArtifact(Log l, File src, File dst) throws MojoExecutionException {
 		
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
 
 			if (!from.exists())
 				throw new MojoExecutionException("File to copy does not exist: " + from.toString());
 
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
 
 					if (makeExecutable)
 						makeExecutable(l, to.getAbsolutePath());
 				} else {
 					FileUtils.copyFileToDirectory(from, to);
 					size += from.length();
 
 					if (makeExecutable)
 						makeExecutable(l, to.getAbsolutePath() + File.separator + from.getName());
 				}
 			} catch (IOException ioe) {
 				throw new MojoExecutionException("IOException while copying " + type, ioe);
 			}
 		}
 
 		return size;
 	}
 
 	/**
 	 * Convert the artifactId into a Debian package name. Currently this only
 	 * applies to libraries which get a "lib" prefix and a "-java" suffix.
 	 * 
 	 * @param artifactId
 	 * @return
 	 */
 	public static String createPackageName(String artifactId, String section, boolean debianise) {
 		return debianise && section.equals("libs") ? "lib" + artifactId + "-java" : artifactId;
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
 			if (writer != null)
 				writer.close();
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
 	static public final long copyArtifacts(Log l, Set<Artifact> artifacts, File dst) throws MojoExecutionException {
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
 
 				} else
 					throw new MojoExecutionException("Unable to copy Artifact " + a
 							+ " because it is not locally available.");
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
 	 * Makes the version string compatible to the system's requirements.
 	 * 
 	 * @param v
 	 * @return
 	 */
 	public final static String fixVersion(String v) {
 		int i = v.indexOf("-SNAPSHOT");
 		if (i > 0)
 			return v.substring(0, i) + "~SNAPSHOT~" + createSnapshotTimestamp();
 
 		return v;
 	}
 
 	/**
 	 * Returns a String representing the current time (UTC) in format
 	 * yyyyMMddHHmmss
 	 * 
 	 * @return
 	 */
 	private final static String createSnapshotTimestamp() {
 
 		return new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance(TimeZone.getTimeZone("UTC"))
 				.getTime());
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
 	public final static String sanitizePackageVersion(String string) {
 
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
 	public final static Set findArtifacts(ArtifactFilter filter, 
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
 
 		return result.getArtifacts();
 	}
 	
 	/**
 	 * Returns a TargetConfiguration object that matches the desired target String.
 	 * @param target
 	 * @param targetConfigurations
 	 * @return
 	 */
 	public final static TargetConfiguration getTargetConfigurationFromString(String target, 
 											List<TargetConfiguration> targetConfigurations){
 		
 		for (TargetConfiguration currentTargetConfiguration : targetConfigurations) {
 			if (currentTargetConfiguration.getTarget().equals(target)) {
 				return currentTargetConfiguration;
 			}
 		}
 		return null;
 	}
 	
 	  /**
 	   * Returns the default Distro to use for a certain TargetConfiguration.
 	   * @param configuration
 	   * @return
 	   * @throws MojoExecutionException 
 	   */
 	public static String getDefaultDistro(String targetString, List<TargetConfiguration> targetConfigurations, Log l) throws MojoExecutionException {
 		String distro = new String();
 		TargetConfiguration target = Utils.getTargetConfigurationFromString(targetString, targetConfigurations);
 
 		if (target.getDefaultDistro() != null) {
 			distro = target.getDefaultDistro();
 			l.info("Default distribution is set to \"" + distro + "\".");
 		} else if (target.getDistros() != null) {
 			if (target.getDistros().size() == 1) {
 				distro = (String) target.getDistros().iterator().next();
 				l.info("Size of \"Distros\" list is one. Using \"" + distro + "\" as default.");
 			} else if (target.getDistros().size() > 1) {
 				String m = "No default configuration given for" + targetString
 						+ ", and more than one distro is supported. Please provide one.";
 				l.error(m);
 				throw new MojoExecutionException(m);
 			}
 		} else {
 			throw new MojoExecutionException("No distros defined for configuration " + targetString);
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
 	public static String getConsolidatedLicenseString(MavenProject project) throws MojoExecutionException {
 		StringBuilder license = new StringBuilder();
 		Iterator<License> ite = null;
 		try {
 			ite = (Iterator<License>) project.getLicenses().iterator();
 		} catch (Exception ex) {
 			throw new MojoExecutionException("Please provide at least one license in your POM.");
 		}
 		license.append(((License) ite.next()).getName());
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
 		
 	    String s;
 	    StringBuilder sb = new StringBuilder();
 	    BufferedReader r = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
 	    while ((s = r.readLine()) != null) {
 	        sb.append(s);
 	        sb.append("\n");
 	    }
 		return sb.toString();
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
 	   * Returns a Packager Object for a certain packaging type (deb, rpm, etc.) 
 	   * @param packaging
 	   * @return
 	   */
 	  public static Packager getPackagerForPackaging(String packaging) {
 		    Map<String, Packager> extPackagerMap = new HashMap<String,Packager>();
 		    extPackagerMap.put("deb", new DebPackager());
 		    extPackagerMap.put("ipk", new IpkPackager());
 		    extPackagerMap.put("izpack", new IzPackPackager());
 		    extPackagerMap.put("rpm", new RPMPackager());
 		    return extPackagerMap.get(packaging);
 	  }
 
 	  /**
 	   * Returns a PackagingHelper object that supports a caertain packaging type
 	   * @param packaging
 	   * @param dc
 	   * @param pm
 	   * @return
 	   */
 	  public static Helper getPackagingHelperForPackaging(String packaging, TargetConfiguration dc, Packaging p){
 	  		Map<String, Helper> extPackagerHelperMap = new HashMap<String,Helper>();
 		    extPackagerHelperMap.put("deb", new DebHelper(dc, (Packaging) p));
 		    extPackagerHelperMap.put("ipk", new IpkHelper(dc, (Packaging) p));
 		    extPackagerHelperMap.put("izpack", new IzPackHelper(dc, (Packaging) p));
 		    extPackagerHelperMap.put("rpm", new RpmHelper(dc, (Packaging) p));
 		    return extPackagerHelperMap.get(packaging);
 	  }
 }
