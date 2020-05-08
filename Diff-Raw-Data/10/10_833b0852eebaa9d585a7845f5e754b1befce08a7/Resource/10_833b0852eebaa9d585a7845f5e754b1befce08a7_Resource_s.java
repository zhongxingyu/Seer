 package net.meisen.general.genmisc.resources;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.regex.Pattern;
 
 import net.meisen.general.genmisc.types.Files;
 
 /**
  * Utility class for resources
  * 
  * @author pmeisen
  * 
  */
 public class Resource {
 
 	/**
 	 * Tries to load a resource from the file-system (relative files are resolved
 	 * against the working directory). If the resource cannot be found on the
 	 * file-system it is resolved against the class-path.
 	 * 
 	 * @param path
 	 *          the path to be resolved
 	 * @return the resource as {@link InputStream} or <code>null</code> if it
 	 *         cannot be found
 	 */
 	public final static InputStream getResourceAsStream(final String path) {
 		final ResourceInfo resInfo = new ResourceInfo(path, true);
 
 		return getResourceAsStream(resInfo);
 	}
 
 	/**
 	 * Tries to load a resource provided by a {@link ResourceInfo}. If the
 	 * resource cannot be loaded or does not exist <code>null</code> will be
 	 * returned.
 	 * 
 	 * @param resInfo
 	 *          the <code>ResourceInfo</code> loaded
 	 * @return the resource as {@link InputStream} or <code>null</code> if it
 	 *         cannot be found
 	 */
 	public final static InputStream getResourceAsStream(final ResourceInfo resInfo) {
 
 		// check if the resource is available
 		if (resInfo == null || !resInfo.exists() || !resInfo.isFile()) {
 			return null;
 		}
 
 		InputStream is = null;
 
 		// check if we have a file
 		if (ResourceType.FILE_SYSTEM_FILE.equals(resInfo.getType())) {
 
 			try {
 				is = new FileInputStream(new File(resInfo.getFullPath()));
 			} catch (final FileNotFoundException e) {
 				is = null;
 			}
 		} else {
 			try {
 				final JarFile jarFile = new JarFile(URLDecoder.decode(
 						resInfo.getJarPath(), "UTF-8"));
 				final JarEntry jarEntry = jarFile.getJarEntry(resInfo.getInJarPath());
 
 				is = jarFile.getInputStream(jarEntry);
 			} catch (final UnsupportedEncodingException e) {
 				throw new IllegalStateException("Cannot read jar at '"
 						+ resInfo.getJarPath() + "'", e);
 			} catch (final IOException e) {
 				throw new IllegalStateException("Cannot read jar at '"
 						+ resInfo.getJarPath() + "'", e);
 			}
 		}
 
 		// return the InputStream
 		return is;
 	}
 
 	/**
 	 * Checks if the specified resource is a file and if it exists.
 	 * 
 	 * @param path
 	 *          the relative or absolute file to be checked
 	 * @return <code>true</code> if the resource is a file and available (i.e.
 	 *         readable on the file-system or available on the class-path,
 	 *         otherwise <code>false</code>
 	 * 
 	 * @see Resource#isPathResolvable(String)
 	 */
 	public final static boolean hasResource(final String path) {
 		final ResourceInfo resInfo = new ResourceInfo(path, true);
 
 		return hasResource(resInfo);
 	}
 
 	/**
 	 * Checks if the specified resource is a file and if it exists.
 	 * 
 	 * @param resInfo
 	 *          the <code>ResourceInfo</code> of the file which should be checked
 	 * @return <code>true</code> if the resource exists and if it is a file,
 	 *         otherwise <code>false</code>
 	 * 
 	 * @see ResourceInfo#exists()
 	 * @see ResourceInfo#isFile()
 	 */
 	public final static boolean hasResource(final ResourceInfo resInfo) {
 
 		// check if the file exists
 		if (resInfo == null) {
 			return false;
 		} else if (resInfo.exists() && resInfo.isFile()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Checks if a specified path points to a valid directory of the file-system
 	 * or on the class-path
 	 * 
 	 * @param path
 	 *          the relative or absolute path to be checked
 	 * @return <code>true</code> if the path can be resolved, otherwise
 	 *         <code>false</code>
 	 * 
 	 * @see Resource#hasResource(String)
 	 */
 	public final static boolean isPathResolvable(final String path) {
 		final ResourceInfo resInfo = new ResourceInfo(path, false);
 
 		return isPathResolvable(resInfo);
 	}
 
 	/**
 	 * Checks if a path is resolvable against the file-system or the class-path.
 	 * This means that the resource is a directory (i.e. not a file) and exists.
 	 * 
 	 * @param resInfo
 	 *          the <code>ResourceInfo</code> with defines the resource which
 	 *          should be checked
 	 * @return <code>true</code> if the resource is a directory (i.e. not a file)
 	 *         and exists, <code>false</code> otherwise
 	 * 
 	 * @see ResourceInfo#exists()
 	 * @see ResourceInfo#isFile()
 	 */
 	public final static boolean isPathResolvable(final ResourceInfo resInfo) {
 
 		// check if the directory exists and is one
 		if (resInfo == null) {
 			return false;
 		} else if (resInfo.exists() && !resInfo.isFile()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * This method is used to resolve a path to the path used to access the path.
 	 * 
 	 * @param path
 	 *          the resolved path
 	 * @return the path pointing to the resolved path
 	 * 
 	 * @see Resource#resolveResource(String)
 	 */
 	public final static String resolvePath(final String path) {
 		final ResourceInfo resInfo = new ResourceInfo(path, false);
 
 		return resInfo.getFullPath();
 	}
 
 	/**
 	 * This method is used to resolve a path to the path used to retrieve the file
 	 * from the file-system or the class-path.
 	 * 
 	 * @param path
 	 *          the path to be resolved
 	 * @return the path pointing to the resolved resource
 	 * 
 	 * @see Resource#resolvePath(String)
 	 */
 	public final static String resolveResource(final String path) {
 		final ResourceInfo resInfo = new ResourceInfo(path, true);
 
 		return resInfo.getFullPath();
 	}
 
 	/**
 	 * Determines all the available files under the specified path. Files in
 	 * sub-directories or sub-directories are not included.
 	 * 
 	 * @param path
 	 *          the path to get all the available resources for (sub-directories
 	 *          are excluded and not added)
 	 * @return the list of all the resources within the specified path,
 	 *         <code>null</code> if a location was passed, which does not exist
 	 */
 	public static Collection<String> getAvailableResources(final String path) {
 		final ResourceInfo resInfo = new ResourceInfo(path, false);
 
 		return getAvailableResources(resInfo, false);
 	}
 
 	/**
 	 * Determines all the available files under the specified path
 	 * 
 	 * @param path
 	 *          the path to get all the available resources for (sub-directories
 	 *          are excluded and not added)
 	 * @param includeSubs
 	 *          <code>true</code> if the sub-files and directories should be
 	 *          included, otherwise <code>false</code>
 	 * 
 	 * @return the list of all the resources within the specified path,
 	 *         <code>null</code> if a location was passed, which does not exist
 	 */
 	public static Collection<String> getAvailableResources(final String path,
 			final boolean includeSubs) {
 		final ResourceInfo resInfo = new ResourceInfo(path, false);
 
 		return getAvailableResources(resInfo, includeSubs);
 	}
 
 	/**
 	 * Determines all the available files under the specified path
 	 * 
 	 * @param resInfo
 	 *          the <code>ResourceInfo</code> which points to the directory to be
 	 *          checked for available resources
 	 * @param includeSubs
 	 *          <code>true</code> if the sub-files and directories should be
 	 *          included, otherwise <code>false</code>
 	 * 
 	 * @return the list of all the resources within the specified path,
 	 *         <code>null</code> if a location was passed, which does not exist or
 	 *         which points to a file
 	 */
 	public static Collection<String> getAvailableResources(
 			final ResourceInfo resInfo, final boolean includeSubs) {
 		final HashSet<String> result;
 
 		// check if the directory exists and is one
 		if (resInfo == null) {
 			result = null;
 		} else if (!resInfo.exists() || resInfo.isFile()) {
 			result = null;
 		} else if (ResourceType.FILE_SYSTEM_PATH.equals(resInfo.getType())) {
 			final String fullPath = resInfo.getFullPath();
 			final File dir = new File(fullPath);
 			final List<File> files = includeSubs ? Files.getDirectoryContent(dir)
 					: Files.getCurrentFilelist(dir);
 
 			// add all the files
 			result = new HashSet<String>();
 
 			for (final File file : files) {
 				final String filePath = Files.getCanonicalPath(file);
 				final String entry = filePath.replaceFirst(Pattern.quote(fullPath), "");
 				result.add(entry);
 			}
 		} else if (ResourceType.IN_JAR_PATH.equals(resInfo.getType())) {
 			final String inJarPath = resInfo.getInJarPath();
 			final Enumeration<JarEntry> entries;
 
 			try {
 				final JarFile jarFile = new JarFile(URLDecoder.decode(
 						resInfo.getJarPath(), "UTF-8"));
 				entries = jarFile.entries();
 			} catch (final UnsupportedEncodingException e) {
 				throw new IllegalStateException("Cannot read jar at '"
 						+ resInfo.getJarPath() + "'", e);
 			} catch (final IOException e) {
 				throw new IllegalStateException("Cannot read jar at '"
 						+ resInfo.getJarPath() + "'", e);
 			}
 
 			// run throw all the files
 			result = new HashSet<String>();
 			while (entries.hasMoreElements()) {
 				final String name = entries.nextElement().getName();
 
 				// filter according to the path
 				if (name.startsWith(inJarPath)) {
 					final String entry = name.substring(inJarPath.length());
 
 					// only add the files
 					if (!entry.equals("") && (includeSubs || entry.indexOf("/") < 0)) {
 						result.add(entry);
 					}
 				}
 			}
 		} else {
 			result = null;
 		}
 
 		return result;
 	}
 
 	/**
 	 * Get all the resources of the specified file-name available on the
 	 * class-path
 	 * 
 	 * @param fileName
 	 *          the file-name to look for
 	 * 
 	 * @return the <code>Collection</code> of all the resources found on the
 	 *         class-path with the specified filename
 	 */
 	public static Collection<ResourceInfo> getResources(final String fileName) {
 		return getResources(fileName, true, false);
 	}
 
 	/**
 	 * Get all the resources of the specified file-name available on the
 	 * class-path and/or working-directory.
 	 * 
 	 * @param fileName
 	 *          the file-name to look for
 	 * @param lookOnClassPath
 	 *          <code>true</code> to look for the resource on the classpath,
 	 *          otherwise <code>false</code>
 	 * @param lookInWorkingDir
 	 *          <code>true</code> to look for the resource in the
 	 *          working-directory, otherwise <code>false</code>
 	 * 
 	 * @return the <code>Collection</code> of all the resources found on the
 	 *         class-path with the specified filename
 	 */
 	public static Collection<ResourceInfo> getResources(final String fileName,
 			final boolean lookOnClassPath, final boolean lookInWorkingDir) {
 		final Pattern pattern = Pattern.compile("(?:^|.*[/\\\\])\\Q" + fileName
 				+ "\\E$");
 		return getResources(pattern, lookOnClassPath, lookInWorkingDir);
 	}
 
 	/**
 	 * Get all the resources matching the specified <code>Pattern</code> and are
 	 * available on the class-path
 	 * 
 	 * @param pattern
 	 *          the <code>Pattern</code> to match against
 	 * 
 	 * @return the <code>Collection</code> of all the resources found on the
 	 *         class-path with the specified filename
 	 */
 	public static Collection<ResourceInfo> getResources(final Pattern pattern) {
 		return getResources(pattern, true, false);
 	}
 
 	/**
 	 * Retrieves all the resources which match the specified <code>Pattern</code>.
 	 * The resources must be files and must be located (depending on the passed
 	 * booleans) on the classpath or in the working directory.
 	 * 
 	 * @param pattern
 	 *          the <code>Pattern</code> to match the files against
 	 * @param lookOnClassPath
 	 *          <code>true</code> if the resource should be searched on the
 	 *          class-path, otherwise <code>false</code>
 	 * @param lookInWorkingDir
 	 *          <code>true</code> if the resource should be searched on the
 	 *          working directory, otherwise <code>false</code>
 	 * 
 	 * @return the <code>Collection</code> of found resources
 	 */
 	public static Collection<ResourceInfo> getResources(final Pattern pattern,
 			final boolean lookOnClassPath, final boolean lookInWorkingDir) {
 		final Set<ResourceInfo> retval = new HashSet<ResourceInfo>();
 
 		if (lookOnClassPath) {

 			// look on the classpath for the pattern
 			final String classPath = System.getProperty("java.class.path", ".");
			final String[] classPathElements = classPath.split(";");
 			for (final String element : classPathElements) {
 				final File file = new File(element);
 				if (file.isDirectory()) {
 					final List<File> files = Files.getFilelist(file, null, pattern);
 					retval.addAll(ResourceInfo.transformFromFileCollection(files));
 				} else {
 					retval.addAll(getResourcesFromJarFile(file, pattern));
 				}
 			}
		} 
 		if (lookInWorkingDir) {
 
 			// look in the working directory for the pattern
 			final List<File> workDirFiles = Files.getFilelist(new File("."), null,
 					pattern);
 			for (final File workDirFile : workDirFiles) {
 				if (workDirFile.isFile()) {
 					final String fullPath = Files.getCanonicalPath(workDirFile);
 					retval.add(new ResourceInfo(fullPath, true));
 				}
 			}
 		}
 		return retval;
 	}
 
 	/**
 	 * Retrieves all the resources which match the specified <code>Pattern</code>
 	 * and are available on the classpath.
 	 * 
 	 * @param file
 	 *          the jar-<code>File</code> to look for resources
 	 * @param pattern
 	 *          the <code>Pattern</code> to match the files against
 	 * 
 	 * @return the <code>Collection</code> of found resources
 	 */
 	public static Collection<ResourceInfo> getResourcesFromJarFile(
 			final File file, final Pattern pattern) {
 
 		// get the array an the path to the jar
 		final Set<ResourceInfo> retval = new HashSet<ResourceInfo>();
 		final String pathToJar = Files.getCanonicalPath(file);
 
 		JarFile jarFile;
 		try {
 			jarFile = new JarFile(URLDecoder.decode(pathToJar, "UTF-8"));
 
 			// iterate over the entries
 			final Enumeration<?> e = jarFile.entries();
 			while (e.hasMoreElements()) {
 				final JarEntry je = (JarEntry) e.nextElement();
 
 				final String fileName = je.getName();
 				final boolean accept = pattern.matcher(fileName).matches();
 				if (accept) {
 					retval.add(new ResourceInfo(fileName, pathToJar, !je.isDirectory()));
 				}
 			}
 			jarFile.close();
 		} catch (final UnsupportedEncodingException e) {
 			throw new IllegalStateException("Cannot read jar at '" + pathToJar + "'",
 					e);
 		} catch (final IOException e) {
 			throw new IllegalStateException("Cannot read jar at '" + pathToJar + "'",
 					e);
 		}
 		return retval;
 	}
 }
