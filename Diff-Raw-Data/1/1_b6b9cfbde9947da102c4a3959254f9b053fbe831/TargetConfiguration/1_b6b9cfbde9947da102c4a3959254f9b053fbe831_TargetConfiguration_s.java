 /*
  * Maven Packaging Plugin,
  * Maven plugin to package a Project (deb, ipk, izpack)
  * Copyright (C) 2000-2009 tarent GmbH
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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 
 /**
  * A <code>TargetConfiguration</code> provides the properties to configure the
  * packaging for a particular target.
  * 
  * <p>A target is a much more fine granular entity than a distribution. E.g. it may
  * denote a certain piece of hardware.<p> 
  * 
  * <p>
  * Except for the boolean properties every field can be accessed directly. The
  * boolean properties are using <code>Boolean</code> to allow them to be
  * <code>null</code> which means 'not set' and is an important state for the
  * merging of two <code>DistroConfiguration</code> instances.
  * 
  * @author Robert Schuster (robert.schuster@tarent.de)
  * 
  */
 public class TargetConfiguration {
 	
 	public TargetConfiguration(String target) {
 		this.target = target;
 	}
 	
 	/**
 	 * Merges the <code>Collection</code>-based instances as follows:
 	 * <ul>
 	 * <li>if parent is non-null take parent else teh default collection</li>
 	 * <li>if child is non-null add all its contents</li>
 	 * </ul>
 	 * <p>
 	 * That way you get either parent, default, parent plus child or default
 	 * plus child
 	 * </p>
 	 */
 	private static <T> Collection<T> merge(Collection<T> child, Collection<T> parent,
 			Collection<T> def) {
 		Collection<T> c = (parent != null ? parent : def);
 
 		if (child != null)
 			c.addAll(child);
 
 		return c;
 	}
 	
 	
 	private static Properties merge(Properties child, Properties parent,
 			Properties def) {
 		Properties c = (parent != null ? parent : def);
 
 		if (child != null)
 			c.putAll(child);
 
 		return c;
 	}
 
 	/**
 	 * If child != null, take child (overridden parent), else if parent != null,
 	 * take parent (overridden default), else take default.
 	 * 
 	 * @param child
 	 * @param parent
 	 * @param def
 	 * @return
 	 */
 	private static Object merge(Object child, Object parent, Object def) {
 		return (child != null) ? child : (parent != null ? parent : def);
 	}
 	
 	/**
 	 * Denotes the target this configuration is for.
 	 */
 	private String target;
 	
 	Boolean createWindowsExecutable;
 
 	Boolean createOSXApp;
 
 	/**
 	 * Denotes whether the packager should use a special starter class to run
 	 * the application which allows working around platform limitations as fixed
 	 * command-line length.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is <code>false</code> or
 	 * the parent's value.
 	 * </p>
 	 */
 	Boolean advancedStarter;
 
 	/**
 	 * Denotes wether the packager should invoke ahead of time compilation (if
 	 * it supports this).
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is <code>false</code> or
 	 * the parent's value.
 	 * </p>
 	 */
 	Boolean aotCompile;
 
 	/**
 	 * Denotes the architecure string to be used. This is only effective for
 	 * packagers supporting this feature (= ipk, deb).
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is <code>all</code> or the
 	 * parent's value.
 	 * </p>
 	 */
 	String architecture;
 
 	/**
 	 * Denotes a list of {@link AuxFile} instances specifying additional files
 	 * that need to be added to the package.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is an empty list or the
 	 * parent's value.
 	 * </p>
 	 */
 	List<AuxFile> auxFiles;
 
 	/**
 	 * Denotes a path that is used for user-level executables (usually
 	 * /usr/bin). If <code>prefix</code> is used it is overriden by this value
 	 * for binaries.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is the empty string or the
 	 * parent's value. In case the value is empty the distribution's default
 	 * bindir prepended by the prefix is used for executables!
 	 * </p>
 	 */
 	String bindir;
 	
 	/**
 	 * List of files which are installed into the directory for executable.
 	 */
 	List<BinFile> binFiles;
 
 	/**
 	 * Denotes whether the packager should bundle every dependency regardless of
 	 * whether a particular item is available from the system's native package
 	 * management or not. This can be used to work around problems with those
 	 * packages.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is <code>false</code> or
 	 * the parent's value.
 	 * </p>
 	 */
 	Boolean bundleAll;
 
 	/**
 	 * Denotes a set of dependencies (in Maven's artifact id naming) that should
 	 * be bundled with the application regardless of their existence in the
 	 * target system's native package management.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is an empty set or the
 	 * parent's value.
 	 * </p>
 	 */
 	Set bundleDependencies;
 
 	/**
 	 * Denotes the directory in the target system where the bundled jar files
 	 * are put.
 	 * 
 	 * <p>
 	 * Default value is <code>null</code>, after merging it is the empty string
 	 * (meaning the default bundled jar dir is used) or the parent's value.
 	 */
 	private String bundledJarDir;
 
 	/**
 	 * The distribution which is chosen to be built. This is not handled by
 	 * Maven2 but only by the Packaging class.
 	 */
 	private String chosenDistro;
 
 	/**
 	 * Denotes the directory in the target system where application specific
 	 * data files are put.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is the empty string or the
 	 * parent's value. In case the value is empty the distribution's default
 	 * datadir prepended by the prefix is used.
 	 * </p>
 	 */
 	String datadir;
 
 	/**
 	 * List of files which are installed into the application-specific data
 	 * files directory.
 	 */
 	List<DataFile> dataFiles;
 
 	/**
 	 * Denotes the root directory in the target system where application
 	 * specific data files are put. This is usually the directory one-level
 	 * above the datadir.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is the empty string or the
 	 * parent's value. In case the value is empty the distribution's default
 	 * datarootdir prepended by the prefix is used.
 	 * </p>
 	 */
 	String datarootdir;
 
 	/**
 	 * List of files which are installed into the root directory of
 	 * application-specific data files directory.
 	 * 
 	 * <p>
 	 * By using this property one can install files into another application's
 	 * datadir, e.g. /usr/share/dbus-1
 	 */
 	List<DatarootFile> datarootFiles;
 
 	/**
 	 * Denotes the distributions this configuration is used for.
 	 */
 	private Set<String> distros = new HashSet<String>();
 
 
 	/**
 	 * Set default distribution to package for.
 	 * 
 	 */
 	private String defaultDistro;
 	
 	/**
 	 * Denotes the name of the gcj-dbtool executable. This allows the use of
 	 * e.g. "gcj-dbtool-4.2" or "gcj-dbtool-4.3" depending on the targeted
 	 * distribution.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is <code>gcj-dbtool</code>
 	 * or the parent's value.
 	 * </p>
 	 */
 	String gcjDbToolExec;
 
 	/**
 	 * Denotes the name of the gcj executable. This allows the use of e.g.
 	 * "gcj-4.2" or "gcj-4.3" depending on the targeted distribution.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is <code>gcj</code> or the
 	 * parent's value.
 	 * </p>
 	 */
 	String gcjExec;
 
 	/**
 	 * Denotes the name of the IzPack descriptor file.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is
 	 * <code>installer.xml</code> or the parent's value.
 	 * </p>
 	 */
 	String izPackInstallerXml;
 
 	/**
 	 * Denotes a list of custom jar files. These are copied to their respective
 	 * destination suitable for the chosen target system.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is an empty list or the
 	 * parent's value.
 	 * </p>
 	 */
 	List<JarFile> jarFiles;
 
 	/**
 	 * Denotes a list of native libraries. These are copied to their respective
 	 * destination suitable for the chosen target system.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is an empty list or the
 	 * parent's value.
 	 * </p>
 	 */
 	List<JniFile> jniFiles;
 
 	/**
 	 * Denotes the <code>java.library.path</code> value of the application. In
 	 * case of IzPack packaging do not forget to use the "$install_path"
 	 * variable.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is an empty string or the
 	 * parent's value.
 	 * </p>
 	 */
 	String jniLibraryPath;
 
 	/**
 	 * Denotes the applications' main class. It can be different per
 	 * distribution, which might be handy for different start screens or
 	 * workarounds.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is <code>null</code> or
 	 * the parent's value.
 	 * </p>
 	 */
 	String mainClass;
 
 	/**
 	 * Denotes the value of the maintainer field in common packaging systems. It
 	 * is basically an email address.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is <code>null</code> or
 	 * the parent's value.
 	 * </p>
 	 */
 	String maintainer;
 
 	/**
 	 * Denotes a list of dependency strings which should be added to the
 	 * automatically generated ones. This allows to specify dependencies which
 	 * Maven does not know about.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is an empty list or the
 	 * parent's value.
 	 * </p>
 	 */
 	List<String> manualDependencies;
 
 	/**
 	 * Denotes a list of strings which should be added to the "Recommends"-field
 	 * of the package.
 	 * 
 	 * From the Debian Policy Manual
 	 * (http://www.debian.org/doc/debian-policy/ch-relationships.html):
 	 * 
 	 * "This declares a strong, but not absolute, dependency. The Recommends
 	 * field should list packages that would be found together with this one in
 	 * all but unusual installations."
 	 * 
 	 * <p>
 	 * Default is <code>null</code>
 	 */
 	List<String> recommends;
 
 	/**
 	 * Denotes a list of strings which should be added to the "Suggests"-field
 	 * of the package.
 	 * 
 	 * From the Debian Policy Manual
 	 * (http://www.debian.org/doc/debian-policy/ch-relationships.html):
 	 * 
 	 * "This is used to declare that one package may be more useful with one or
 	 * more others. Using this field tells the packaging system and the user
 	 * that the listed packages are related to this one and can perhaps enhance
 	 * its usefulness, but that installing this one without them is perfectly
 	 * reasonable."
 	 * 
 	 * <p>
 	 * Default is <code>null</code>
 	 */
 	List<String> suggests;
 
 	/**
 	 * Denotes a list of strings which should be added to the "Provides"-field
 	 * of the package.
 	 * 
 	 * From the Debian Policy Manual
 	 * (http://www.debian.org/doc/debian-policy/ch-relationships.html):
 	 * 
 	 * "A virtual package is one which appears in the Provides control file
 	 * field of another package. The effect is as if the package(s) which
 	 * provide a particular virtual package name had been listed by name
 	 * everywhere the virtual package name appears."
 	 * 
 	 * <p>
 	 * Default is <code>null</code>
 	 */
 	List<String> provides;
 
 	/**
 	 * Denotes a list of strings which should be added to the "Conflicts"-field
 	 * of the package.
 	 * 
 	 * From the Debian Policy Manual
 	 * (http://www.debian.org/doc/debian-policy/ch-relationships.html):
 	 * 
 	 * "When one binary package declares a conflict with another using a
 	 * Conflicts field, dpkg will refuse to allow them to be installed on the
 	 * system at the same time."
 	 * 
 	 * <p>
 	 * Default is <code>null</code>
 	 */
 	List<String> conflicts;
 
 	/**
 	 * Denotes a list of strings which should be added to the "Replaces"-field
 	 * of the package.
 	 * 
 	 * From the Debian Policy Manual
 	 * (http://www.debian.org/doc/debian-policy/ch-relationships.html):
 	 * 
 	 * "Packages can declare in their control file that they should overwrite
 	 * files in certain other packages, or completely replace other packages"
 	 * 
 	 * <p>
 	 * Default is <code>null</code>
 	 */
 	List<String> replaces;
 
 	/**
 	 * Denotes the value of the "-Xmx" argument.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is <code>null</code> or
 	 * the parent's value.
 	 * </p>
 	 */
 	String maxJavaMemory;
 
 	/**
 	 * Specifies the distroconfiguration from which this one inherits all
 	 * non-set values or from which collections are merged.
 	 * 
 	 * <p>
 	 * If unset it is <code>null</code> meaning the default distro configuration
 	 * is the sole parent.
 	 * </p>
 	 */
 	String parent;
 
 	/**
 	 * Specifies the name of a file which is used as a post installation script.
 	 * 
 	 * <p>
 	 * The base directory to look for the script is the aux files directory!
 	 * </p>
 	 * 
 	 * <p>
 	 * It is only valid for packaging system which support such scripts.
 	 * </p>
 	 * 
 	 * <p>
 	 * If unset it is <code>null</code> and no script is used.
 	 * </p>
 	 */
 	String postinstScript;
 
 	/**
 	 * Specifies the name of a file which is used as a post removal script.
 	 * 
 	 * <p>
 	 * The base directory to look for the script is the aux files directory!
 	 * </p>
 	 * 
 	 * <p>
 	 * It is only valid for packaging system which support such scripts.
 	 * </p>
 	 * 
 	 * <p>
 	 * If unset it is <code>null</code> and no script is used.
 	 * </p>
 	 */
 	String postrmScript;
 
 	/**
 	 * Denotes a path that is prepended before all application paths.
 	 * 
 	 * <p>
 	 * This allows installation to different directories as "/".
 	 * </p>
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is <code>/</code> or the
 	 * parent's value.
 	 * </p>
 	 */
 	String prefix;
 
 	/**
 	 * Specifies the name of a file which is used as a pre-installlation script.
 	 * 
 	 * <p>
 	 * The base directory to look for the script is the aux files directory!
 	 * </p>
 	 * 
 	 * <p>
 	 * It is only valid for packaging system which support such scripts.
 	 * </p>
 	 * 
 	 * <p>
 	 * If unset it is <code>null</code> and no script is used.
 	 * </p>
 	 */
 	String preinstScript;
 
 	/**
 	 * Specifies the name of a file which is used as a pre-removal script.
 	 * 
 	 * <p>
 	 * The base directory to look for the script is the aux files directory!
 	 * </p>
 	 * 
 	 * <p>
 	 * It is only valid for packaging system which support such scripts.
 	 * </p>
 	 * 
 	 * <p>
 	 * If unset it is <code>null</code> and no script is used.
 	 * </p>
 	 */
 	String prermScript;
 
 	/**
 	 * Denotes the packages revision. This is a version number which appended
 	 * after the real package version and can be used to denote a change to the
 	 * packaging (e.g. moved a file to the correct location).
 	 * 
 	 * <p>
 	 * It is possible to use all kinds of strings for that. The ordering rules
 	 * of those is dependent on the underlying packaging system. Try to use
 	 * something sane like "r0", "r1" and so on.
 	 * </p>
 	 * 
 	 * <p>
 	 * If this value is not set or set to the empty string, no revision is
 	 * appended.
 	 * </p>
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is the empty or the
 	 * parent's value.
 	 * </p>
 	 */
 	String revision;
 
 	/**
 	 * Denotes the value of the section property supported by packaging systems.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is "libs" or the parent's
 	 * value.
 	 * </p>
 	 */
 	String section;
 
 	/**
 	 * Denotes the directory in which the packager looks for auxiliary files to
 	 * copy into the package.
 	 * 
 	 * <p>
 	 * By default the aux files directory is meant to contain all the other
 	 * kinds of files like sysconf, dataroot and data files.
 	 * </p>
 	 * 
 	 * <p>
 	 * By using this property one can define a common filename set which has to
 	 * be copied but works on different files since the
 	 * <code>srcAuxFilesDir</code> property can be changed on a per distribution
 	 * basis.
 	 * </p>
 	 * 
 	 * <p>
 	 * Note: The path must be relative to the project's base dir.
 	 * </p>
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is the empty string
 	 * (meaning the default location (= <code<src/main/auxfiles</code>) is used
 	 * or the parent's value.
 	 * </p>
 	 */
 	String srcAuxFilesDir;
 
 	/**
 	 * Denotes the source directory into which the packager looks for
 	 * executable files.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is the empty string
 	 * (meaning the default location (= {@link #srcAuxFilesDir}) is used or the
 	 * parent's value.
 	 * </p>
 	 */
 	String srcBinFilesDir;
 
 	/**
 	 * Denotes the source directory into which the packager looks for
 	 * application specific data files.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is the empty string
 	 * (meaning the default location (= {@link #srcAuxFilesDir}) is used or the
 	 * parent's value.
 	 * </p>
 	 */
 	String srcDataFilesDir;
 
 	/**
 	 * Denotes the source directory into which the packager looks for data files
 	 * which will be copied into the root directory of application specific data
 	 * files.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is the empty string
 	 * (meaning the default location (= {@link #srcAuxFilesDir}) is used or the
 	 * parent's value.
 	 * </p>
 	 */
 	String srcDatarootFilesDir;
 
 	/**
 	 * Denotes the source directory into which the packager looks for IzPack
 	 * specific datafiles.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is the empty string
 	 * (meaning the default location (= {@link #srcAuxFilesDir}) is used or the
 	 * parent's value.
 	 * </p>
 	 */
 	String srcIzPackFilesDir;
 
 	/**
 	 * Denotes the directory in which the packager looks for Jar library files
 	 * to copy into the package.
 	 * 
 	 * <p>
 	 * By using this property one can define a common filename set which has to
 	 * be copied but works on different files since the
 	 * <code>srcJarFilesDir</code> property can be changed on a per distribution
 	 * basis.
 	 * </p>
 	 * 
 	 * <p>
 	 * Note: The path must be relative to the project's base dir.
 	 * </p>
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is an empty string or the
 	 * parent's value.
 	 * </p>
 	 */
 	String srcJarFilesDir;
 
 	/**
 	 * Denotes the directory in which the packager looks for JNI library files
 	 * to copy into the package.
 	 * 
 	 * <p>
 	 * By using this property one can define a common filename set which has to
 	 * be copied but works on different files since the
 	 * <code>srcJNIFilesDir</code> property can be changed on a per distribution
 	 * basis.
 	 * </p>
 	 * 
 	 * <p>
 	 * Note: The path must be relative to the project's base dir.
 	 * </p>
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is an empty string or the
 	 * parent's value.
 	 * </p>
 	 */
 	String srcJNIFilesDir;
 
 	String srcSysconfFilesDir;
 
 	/**
 	 * Denotes a path that is used for user-level configuration data. If
 	 * <code>prefix</code> is used it is overriden by this value..
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is the empty string or the
 	 * parent's value. In case the value is empty default sysconfdir (= /etc) is
 	 * prepended by the prefix!
 	 * </p>
 	 */
 	String sysconfdir;
 	List<SysconfFile> sysconfFiles;
 
 	/**
 	 * Denotes a bunch of system properties keys and their values which are
 	 * added to the starter script and thus provided to the application.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is an empty
 	 * <code>Properties</code> instance or the parent's value.
 	 * </p>
 	 */
 	Properties systemProperties;
 
 	/**
 	 * Denotes the name of the wrapper script that is used to run the
 	 * application. This property is optional and will default to the
 	 * <code>artifactId</code> is the Maven project. For Windows targets ".bat"
 	 * is appended to this name.
 	 * 
 	 * <p>
 	 * Default is <code>null</code>, after merging it is <code>null</code> or
 	 * the parent's value.
 	 * </p>
 	 */
 	String wrapperScriptName;
 
 	/**
 	 * Denotes a piece of shell script code which is added to the (Unix) start
 	 * script.
 	 * 
 	 * <p>
 	 * The code is executed <em>after</em> the variables have been set
 	 * (classpath, bootclasspath, system properties, ...) and right
 	 * <em>before</em> the VM is started.
 	 * </p>
 	 */
 	String customCodeUnix;
 
 	/**
 	 * Denotes a piece of batch file code which is added to the (Windows) start
 	 * script.
 	 * 
 	 * <p>
 	 * The code is executed <em>after</em> the variables have been set
 	 * (classpath, bootclasspath, system properties, ...) and right
 	 * <em>before</em> the VM is started.
 	 * </p>
 	 */
 	String customCodeWindows;
 	
 	/**
 	 * Denothes wether the package should be signed or not. As of now, this value is
 	 * only taken in consideration when building RPM packages.
 	 * 
 	 * <p>Default value is <code>false</code>.</p>
 	 * 
 	 */
 	Boolean sign;
 	
 	/**
 	 * Denothes the release of the package to build. As of now, this value is
 	 * only taken in consideration when building RPM packages.
 	 * 
 	 * <p>Default value is <code>unknown</code>.</p>
 	 * 
 	 */
 	String release;
 	
 	/**
 	 * Denothes the source of the package to build. As of now, this value is
 	 * only taken in consideration when building RPM packages.
 	 * 
 	 * <p>Default value is <code>unknown</code>.</p>
 	 * 
 	 */
 	String source;
 	
 	UploadParameters uploadParameters;
 	
 	/**
 	 * Denotes dependencies to other target configurations.
 	 */
 	List<String> relations;
 
 	public TargetConfiguration() {
 		// For instantiation.
 	}
 
 	public String getArchitecture() {
 		return architecture;
 	}
 
 	public List<? extends AuxFile> getAuxFiles() {
 		return auxFiles;
 	}
 
 	public String getBindir() {
 		return bindir;
 	}
 
 	public Set getBundleDependencies() {
 		return bundleDependencies;
 	}
 
 	public String getDatadir() {
 		return datadir;
 	}
 
 	public List<? extends DataFile> getDataFiles() {
 		return dataFiles;
 	}
 	
 	public List<? extends BinFile> getBinFiles() {
 		return binFiles;
 	}
 
 	public String getDatarootdir() {
 		return datarootdir;
 	}
 
 	public List<? extends DatarootFile> getDatarootFiles() {
 		return datarootFiles;
 	}
 
 	public Set getDistros() {
 		return distros;
 	}
 
 	public String getGcjDbToolExec() {
 		return gcjDbToolExec;
 	}
 
 	public String getGcjExec() {
 		return gcjExec;
 	}
 
 	public String getIzPackInstallerXml() {
 		return izPackInstallerXml;
 	}
 
 	public List<JarFile> getJarFiles() {
 		return jarFiles;
 	}
 
 	public List<? extends AuxFile> getJniFiles() {
 		return jniFiles;
 	}
 
 	public String getJniLibraryPath() {
 		return jniLibraryPath;
 	}
 
 	public String getMainClass() {
 		return mainClass;
 	}
 
 	public String getMaintainer() {
 		return maintainer;
 	}
 	
 	public String getTarget() {
 		return target;
 	}
 
 	public List<String> getManualDependencies() {
 		return manualDependencies;
 	}
 
 	public List<String> getRecommends() {
 		return recommends;
 	}
 
 	public List<String> getSuggests() {
 		return recommends;
 	}
 
 	public List<String> getProvides() {
 		return provides;
 	}
 
 	public List<String> getConflicts() {
 		return conflicts;
 	}
 
 	public List<String> getReplaces() {
 		return replaces;
 	}
 
 	public String getMaxJavaMemory() {
 		return maxJavaMemory;
 	}
 
 	public String getPostinstScript() {
 		return postinstScript;
 	}
 
 	public String getPostrmScript() {
 		return postrmScript;
 	}
 
 	public String getPrefix() {
 		return prefix;
 	}
 
 	public String getPreinstScript() {
 		return preinstScript;
 	}
 
 	public String getPrermScript() {
 		return prermScript;
 	}
 
 	public String getSection() {
 		return section;
 	}
 
 	public String getRevision() {
 		if(revision == null){
 			return "";
 		}else{
 			return revision;
 		}
 	}
 
 	public void setRevision(String revision) {
 		this.revision = revision;
 	}
 
 	public String getChosenDistro() {
 		return chosenDistro;
 	}
 
 	public String getSrcAuxFilesDir() {
 		return srcAuxFilesDir;
 	}
 
 	public String getSrcBinFilesDir() {
 		return srcBinFilesDir;
 	}
 
 	public String getSrcDataFilesDir() {
 		return srcDataFilesDir;
 	}
 
 	public String getSrcDatarootFilesDir() {
 		return srcDatarootFilesDir;
 	}
 
 	public String getSrcIzPackFilesDir() {
 		return srcIzPackFilesDir;
 	}
 
 	public String getSrcJarFilesDir() {
 		return srcJarFilesDir;
 	}
 
 	public String getSrcJNIFilesDir() {
 		return srcJNIFilesDir;
 	}
 
 	public String getSrcSysconfFilesDir() {
 		return srcSysconfFilesDir;
 	}
 
 	public String getSysconfdir() {
 		return sysconfdir;
 	}
 
 	public List<SysconfFile> getSysconfFiles() {
 		return sysconfFiles;
 	}
 
 	public Properties getSystemProperties() {
 		return systemProperties;
 	}
 
 	public String getWrapperScriptName() {
 		return wrapperScriptName;
 	}
 
 	public boolean isAdvancedStarter() {
 		return advancedStarter.booleanValue();
 	}
 
 	public boolean isAotCompile() {
 		return aotCompile.booleanValue();
 	}
 
 	public boolean isBundleAll() {
 		return bundleAll.booleanValue();
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
 	 * @param parent
 	 * @return
 	 */
 	TargetConfiguration merge(TargetConfiguration parent) {
 		
 		/*
 		 * Note: The fields chosenDistro, distros and parent are not merged
 		 * because they are the header or descriptor of the configuration not
 		 * its data.
 		 */
 		target = (String) merge(target, parent.target, "default");
 		distros = (Set<String>) merge(distros, parent.distros, new HashSet<String>());
 
 		aotCompile = (Boolean) merge(aotCompile, parent.aotCompile,
 				Boolean.FALSE);
 		bundleAll = (Boolean) merge(bundleAll, parent.bundleAll, Boolean.FALSE);
 		advancedStarter = (Boolean) merge(advancedStarter,
 				parent.advancedStarter, Boolean.FALSE);
 
 		createOSXApp = (Boolean) merge(createOSXApp, parent.createOSXApp,
 				Boolean.TRUE);
 		createWindowsExecutable = (Boolean) merge(createWindowsExecutable,
 				parent.createWindowsExecutable, Boolean.TRUE);
 
 		prefix = (String) merge(prefix, parent.prefix, "/");
 		bindir = (String) merge(bindir, parent.bindir, "");
 		sysconfdir = (String) merge(sysconfdir, parent.sysconfdir, "");
 		datarootdir = (String) merge(datarootdir, parent.datarootdir, "");
 		datadir = (String) merge(datadir, parent.datadir, "");
 
 		setBundledJarDir((String) merge(getBundledJarDir(), parent.getBundledJarDir(), ""));
 
 		architecture = (String) merge(architecture, parent.architecture, "all");
 		gcjDbToolExec = (String) merge(gcjDbToolExec, parent.gcjDbToolExec,
 				"gcj");
 		gcjExec = (String) merge(gcjExec, parent.gcjExec, "gcj-dbtool");
 
 		jniLibraryPath = (String) merge(jniLibraryPath, parent.jniLibraryPath,
 				"/usr/lib/jni");
 
 		mainClass = (String) merge(mainClass, parent.mainClass, null);
 		
 		// Note: For .debs there *MUST* always be a revision otherwise the last
 		// component after a dash (-) needs a number. By enforcing that there is a
 		// revision we prevent that problem to occur.
 		revision = (String) merge(revision, parent.revision, "r0");
 		wrapperScriptName = (String) merge(wrapperScriptName,
 				parent.wrapperScriptName, null);
 		maintainer = (String) merge(maintainer, parent.maintainer, null);
 		maxJavaMemory = (String) merge(maxJavaMemory, parent.maxJavaMemory,
 				null);
 		section = (String) merge(section, parent.section, "libs");
 		izPackInstallerXml = (String) merge(izPackInstallerXml,
 				parent.izPackInstallerXml, "installer.xml");
 
 		preinstScript = (String) merge(preinstScript, parent.preinstScript,
 				null);
 		prermScript = (String) merge(prermScript, parent.prermScript, null);
 		postinstScript = (String) merge(postinstScript, parent.postinstScript,
 				null);
 		postrmScript = (String) merge(postrmScript, parent.postrmScript, null);
 
 		srcAuxFilesDir = (String) merge(srcAuxFilesDir, parent.srcAuxFilesDir,
 				"");
 		srcSysconfFilesDir = (String) merge(srcSysconfFilesDir,
 				parent.srcSysconfFilesDir, "");
 		srcJarFilesDir = (String) merge(srcJarFilesDir, parent.srcJarFilesDir,
 				"");
 		srcJNIFilesDir = (String) merge(srcJNIFilesDir, parent.srcJNIFilesDir,
 				"");
 		srcDatarootFilesDir = (String) merge(srcDatarootFilesDir,
 				parent.srcDatarootFilesDir, "");
 		srcDataFilesDir = (String) merge(srcDataFilesDir,
 				parent.srcDataFilesDir, "");
 		srcBinFilesDir = (String) merge(srcBinFilesDir,
 				parent.srcBinFilesDir, "");
 
 		srcIzPackFilesDir = (String) merge(srcIzPackFilesDir,
 				parent.srcIzPackFilesDir, "");
 
 		customCodeUnix = (String) merge(customCodeUnix,
 				parent.customCodeUnix, null);
 
 		customCodeWindows = (String) merge(customCodeWindows,
 				parent.customCodeWindows, null);
 
 		auxFiles = (List) merge(auxFiles, parent.auxFiles, new ArrayList<AuxFile>());
 
 		sysconfFiles = (List) merge(sysconfFiles, parent.sysconfFiles,
 				new ArrayList());
 
 		jarFiles = (List) merge(jarFiles, parent.jarFiles, new ArrayList<JarFile>());
 
 		jniFiles = (List) merge(jniFiles, parent.jniFiles, new ArrayList<JniFile>());
 
 		datarootFiles = (List) merge(datarootFiles, parent.datarootFiles,
 				new ArrayList<DatarootFile>());
 
 		dataFiles = (List) merge(dataFiles, parent.dataFiles, new ArrayList<DataFile>());
 		
 		binFiles = (List) merge(binFiles, parent.binFiles, new ArrayList<BinFile>());
 
 		bundleDependencies = (Set) merge(bundleDependencies,
 				parent.bundleDependencies, new HashSet());
 
 		manualDependencies = (List) merge(manualDependencies,
 				parent.manualDependencies, new ArrayList());
 
 		recommends = (List) merge(recommends, parent.recommends,
 				new ArrayList());
 
 		suggests = (List) merge(suggests, parent.suggests, new ArrayList());
 
 		provides = (List) merge(provides, parent.provides, new ArrayList());
 
 		conflicts = (List) merge(conflicts, parent.conflicts, new ArrayList());
 
 		replaces = (List) merge(replaces, parent.replaces, new ArrayList());
 
 		systemProperties = merge(systemProperties,
 				parent.systemProperties, new Properties());
 
 		relations = (List) merge(relations, parent.relations, new ArrayList<String>());
 
 		// RPM sign configuration
 		sign = (Boolean)merge(sign, parent.sign, Boolean.FALSE);
 		
 		// RPM License configuration (it must always contain a value)
 		defaultDistro = (String) merge(defaultDistro, parent.defaultDistro,"unknown");
 
 		return this;
 	}
 
 	public void setAdvancedStarter(boolean advancedStarter) {
 		this.advancedStarter = Boolean.valueOf(advancedStarter);
 	}
 
 	public void setAotCompile(boolean aotCompile) {
 		this.aotCompile = Boolean.valueOf(aotCompile);
 	}
 
 	public void setArchitecture(String architecture) {
 		this.architecture = architecture;
 	}
 
 	public void setAuxFiles(List auxFiles) {
 		this.auxFiles = auxFiles;
 	}
 
 	public void setBindir(String bindir) {
 		this.bindir = bindir;
 	}
 
 	public void setBundleAll(boolean bundleAll) {
 		this.bundleAll = Boolean.valueOf(bundleAll);
 	}
 
 	public void setBundleDependencies(Set bundleDependencies) {
 		this.bundleDependencies = bundleDependencies;
 	}
 
 	public void setDatadir(String datadir) {
 		this.datadir = datadir;
 	}
 
 	public void setDataFiles(List<DataFile> dataFiles) {
 		this.dataFiles = dataFiles;
 	}
 	
 	public void setBinFiles(List<BinFile> binFiles) {
 		this.binFiles = binFiles;
 	}
 
 	public void setDatarootdir(String datarootdir) {
 		this.datarootdir = datarootdir;
 	}
 
 	public void setDatarootFiles(List<DatarootFile> datarootFiles) {
 		this.datarootFiles = datarootFiles;
 	}
 
 	public void setDistro(String distro) {
 		distros.add(distro);
 	}
 
 	public void setDistros(Set distros) {
 		this.distros = distros;
 	}
 
 	public void setGcjDbToolExec(String gcjDbToolExec) {
 		this.gcjDbToolExec = gcjDbToolExec;
 	}
 
 	public void setGcjExec(String gcjExec) {
 		this.gcjExec = gcjExec;
 	}
 
 	public void setIzPackInstallerXml(String izPackInstallerXml) {
 		this.izPackInstallerXml = izPackInstallerXml;
 	}
 
 	public void setJarFiles(List jarLibraries) {
 		this.jarFiles = jarLibraries;
 	}
 
 	public void setJniFiles(List jniLibraries) {
 		this.jniFiles = jniLibraries;
 	}
 
 	public void setJniLibraryPath(String jniLibraryPath) {
 		this.jniLibraryPath = jniLibraryPath;
 	}
 
 	public void setMainClass(String mainClass) {
 		this.mainClass = mainClass;
 	}
 
 	public void setMaintainer(String maintainer) {
 		this.maintainer = maintainer;
 	}
 
 	public void setManualDependencies(List<String> manualDependencies) {
 		this.manualDependencies = manualDependencies;
 	}
 
 	public void setRecommends(List<String> recommends) {
 		this.recommends = recommends;
 	}
 
 	public void setSuggests(List<String> suggests) {
 		this.suggests = suggests;
 	}
 
 	public void setProvides(List<String> provides) {
 		this.provides = provides;
 	}
 
 	public void setConflicts(List<String> conflicts) {
 		this.conflicts = conflicts;
 	}
 
 	public void setReplaces(List<String> replaces) {
 		this.replaces = replaces;
 	}
 
 	public void setMaxJavaMemory(String maxJavaMemory) {
 		this.maxJavaMemory = maxJavaMemory;
 	}
 
 	public void setPostinstScript(String postinstScript) {
 		this.postinstScript = postinstScript;
 	}
 
 	public void setPostrmScript(String postrmScript) {
 		this.postrmScript = postrmScript;
 	}
 
 	public void setPrefix(String prefix) {
 		this.prefix = prefix;
 	}
 
 	public void setPreinstScript(String preinstScript) {
 		this.preinstScript = preinstScript;
 	}
 
 	public void setPrermScript(String prermScript) {
 		this.prermScript = prermScript;
 	}
 
 	public void setSection(String section) {
 		this.section = section;
 	}
 
 	public void setSrcAuxFilesDir(String auxFileSrcDir) {
 		this.srcAuxFilesDir = auxFileSrcDir;
 	}
 
 	public void setSrcBinFilesDir(String srcBinFilesDir) {
 		this.srcBinFilesDir = srcBinFilesDir;
 	}
 
 	public void setSrcDataFilesDir(String srcDataFilesDir) {
 		this.srcDataFilesDir = srcDataFilesDir;
 	}
 
 	public void setSrcDatarootFilesDir(String srcDatarootFilesDir) {
 		this.srcDatarootFilesDir = srcDatarootFilesDir;
 	}
 
 	public void setSrcIzPackFilesDir(String srcIzPackFilesDir) {
 		this.srcIzPackFilesDir = srcIzPackFilesDir;
 	}
 
 	public void setSrcJarFilesDir(String srcJarFilesDir) {
 		this.srcJarFilesDir = srcJarFilesDir;
 	}
 
 	public void setSrcJNIFilesDir(String srcJNIFilesDir) {
 		this.srcJNIFilesDir = srcJNIFilesDir;
 	}
 
 	public void setSrcSysconfFilesDir(String srcSysconfFilesDir) {
 		this.srcSysconfFilesDir = srcSysconfFilesDir;
 	}
 
 	public void setSysconfdir(String sysconfdir) {
 		this.sysconfdir = sysconfdir;
 	}
 
 	public void setSysconfFiles(List sysconfFiles) {
 		this.sysconfFiles = sysconfFiles;
 	}
 
 	public void setSystemProperties(Properties systemProperties) {
 		this.systemProperties = systemProperties;
 	}
 
 	public void setWrapperScriptName(String wrapperScriptName) {
 		this.wrapperScriptName = wrapperScriptName;
 	}
 
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		appendStringDefault(sb, "target", target);
 		appendStringDefault(sb, "parent", parent);
 		appendStringDefault(sb, "defaultDistro", defaultDistro);
 		appendStringDefault(sb, "chosenDistro", chosenDistro);
 		
 		sb.append("\n");
 		appendStringCollection(sb, "distros", distros);
 		
 		sb.append("\n");
 		sb.append("basic packaging options:\n");
 		appendStringDefault(sb, "maintainer", maintainer);
 		appendStringDefault(sb, "section", section);
 		appendStringDefault(sb, "architecture", architecture);
 		appendStringDefault(sb, "prefix", prefix);
 		appendStringDefault(sb, "bindir", bindir);
 		appendStringDefault(sb, "sysconfdir", sysconfdir);
 		appendStringDefault(sb, "datarootdir", datarootdir);
 		appendStringDefault(sb, "datadir", datadir);
 		appendStringNotSet(sb, "bundledJarDir", getBundledJarDir());
 		appendStringNotSet(sb, "jniLibraryPath", jniLibraryPath);
 		appendStringDefault(sb, "izPackInstallerXml", izPackInstallerXml);
 		
 		sb.append("\n");
 		sb.append("packaging scripts:\n");
 		appendStringNotSet(sb, "preinstScript", preinstScript);
 		appendStringNotSet(sb, "prermScript", prermScript);
 		appendStringNotSet(sb, "postinstScript", postinstScript);
 		appendStringNotSet(sb, "postrmScript", postrmScript);
 		
 		sb.append("\n");
 		sb.append("packaging flags:\n");
 		appendBoolean(sb, "aotCompile", aotCompile);
 		appendBoolean(sb, "bundleAll", bundleAll);
 		appendBoolean(sb, "advancedStarter", advancedStarter);
 		appendBoolean(sb, "sign", sign);
 		appendStringDefault(sb, "release", release);
 		appendStringDefault(sb, "source", source);
 		// TODO rschuster: To my knowledge this is not implemented yet.
 //		sb.append("createWindowsExecutable: " + createWindowsExecutable + "\n");
 //		sb.append("createOSXApp: " + createOSXApp + "\n");
 
 		sb.append("\n");
 		sb.append("dependencies and packaged files:\n");
 		appendAuxFileList(sb, "jarFiles", jarFiles);
 		appendAuxFileList(sb, "jniFiles", jniFiles);
 		
 		appendStringCollection(sb, "manualDependencies", manualDependencies);
 		appendStringCollection(sb, "bundleDependencies", bundleDependencies);
 		appendStringCollection(sb, "recommends", recommends);
 		appendStringCollection(sb, "suggests", suggests);
 		appendStringCollection(sb, "provides", provides);
 		appendStringCollection(sb, "conflicts", conflicts);
 		appendStringCollection(sb, "replaces", replaces);
 		
 		appendAuxFileList(sb, "auxFiles", auxFiles);
 		appendAuxFileList(sb, "binFiles", binFiles);
 		appendAuxFileList(sb, "datarootFiles", datarootFiles);
 		appendAuxFileList(sb, "dataFiles", dataFiles);
 		appendAuxFileList(sb, "sysconfFiles", sysconfFiles);
 		
 		sb.append("\n");
 		sb.append("start script options:\n");
 		appendStringNotSet(sb, "wrapperScriptName", wrapperScriptName);
 		appendStringNotSet(sb, "mainClass", mainClass);
 		appendStringNotSet(sb, "maxJavaMemory", maxJavaMemory);
 		appendStringNotSet(sb, "customCodeUnix", customCodeUnix);
 		appendStringNotSet(sb, "customCodeWindows", customCodeWindows);
 		sb.append("systemProperties:\n");
 		if (systemProperties != null) {
 			Iterator ite = systemProperties.entrySet().iterator();
 			while (ite.hasNext())
 			{
 				sb.append("\t" + ite.next() + "\n");
 			}
 		} else
 			sb.append("\t(not set)\n");
 
 		sb.append("\n");
 		sb.append("auxfile locations:\n");
 		appendStringDefault(sb, "srcAuxFilesDir", srcAuxFilesDir);
 		appendStringDefault(sb, "srcSysconfFilesDir", srcSysconfFilesDir);
 		appendStringDefault(sb, "srcBinFilesDir", srcBinFilesDir);
 		appendStringDefault(sb, "srcDataFilesDir", srcDataFilesDir);
 		appendStringDefault(sb, "srcDatarootFilesDir", srcDatarootFilesDir);
 		appendStringDefault(sb, "srcIzPackFilesDir", srcIzPackFilesDir);
 		appendStringDefault(sb, "srcJarFilesDir", srcJarFilesDir);
 		appendStringDefault(sb, "srcJNIFilesDir", srcJNIFilesDir);
 		
 		sb.append("\n");
 		sb.append("tool locations:\n");
 		appendStringDefault(sb, "gcjDbToolExec", gcjDbToolExec);
 		appendStringDefault(sb, "gcjExec", gcjExec);
 
 		return sb.toString();
 	}
 	
 	private void appendBoolean(StringBuilder sb, String label, Boolean b)
 	{
 		sb.append(label);
 		sb.append(": ");
 		sb.append((b == null || b.equals(Boolean.FALSE) ? "no" : "yes"));
 		sb.append("\n");
 	}
 	
 	private void appendStringNotSet(StringBuilder sb, String label, String string)
 	{
 		sb.append(label);
 		sb.append(": ");
 		sb.append((string == null ? "(not set)" : string));
 		sb.append("\n");
 	}
 	
 	private void appendStringDefault(StringBuilder sb, String label, String string)
 	{
 		sb.append(label);
 		sb.append(": ");
 		sb.append((string == null ? "(default)" : string));
 		sb.append("\n");
 	}
 	
 	private void appendStringCollection(StringBuilder sb, String label, Collection collection)
 	{
 		sb.append(label + ":\n");
 		if (collection != null && !collection.isEmpty()) {
 			Iterator ite = collection.iterator();
 			while (ite.hasNext())
 			{
 				sb.append("\t");
 				sb.append(ite.next());
 				sb.append("\n");
 			}
 		} else
 			sb.append("\t(not set)\n");
 	}
 	
 	private void appendAuxFileList(StringBuilder sb, String name, List<? extends AuxFile> list)
 	{
 		sb.append(name + ":\n");
 		if (list != null && !list.isEmpty()) {
 			Iterator<? extends AuxFile> ite = list.iterator();
 			while (ite.hasNext()) {
 				AuxFile af = (AuxFile) ite.next();
 				sb.append("\t");
 				sb.append(af.from);
 				sb.append("\n");
 				sb.append("\t  ");
 				sb.append("-> " + (af.to == null ? "(default dir)" : af.to));
 				sb.append("\n");
 			}
 		} else
 			sb.append("\t(not set)\n");
 
 	}
 
 	public boolean isCreateOSXApp() {
 		return createOSXApp.booleanValue();
 	}
 
 	public void setCreateOSXApp(boolean createOSXApp) {
 		this.createOSXApp = Boolean.valueOf(createOSXApp);
 	}
 
 	public boolean isCreateWindowsExecutable() {
 		return createWindowsExecutable.booleanValue();
 	}
 
 	public void setCreateWindowsExecutable(boolean createWindowsExecutable) {
 		this.createWindowsExecutable = Boolean.valueOf(createWindowsExecutable);
 	}
 
 	public String getCustomCodeUnix() {
 		return customCodeUnix;
 	}
 
 	public void setCustomCodeUnix(String customCodeUnix) {
 		this.customCodeUnix = customCodeUnix;
 	}
 
 	public String getCustomCodeWindows() {
 		return customCodeWindows;
 	}
 
 	public void setCustomCodeWindows(String customCodeWindows) {
 		this.customCodeWindows = customCodeWindows;
 	}
 
 
 	public boolean isSign() {
 		return sign.booleanValue();
 	}
 
 
 	public void setSign(boolean sign) {
 		this.sign = Boolean.valueOf(sign);
 	}
 
 	public String getRelease() {
 		return release;
 	}
 
 
 	public void setRelease(String release) {
 		this.release = release;
 	}
 
 
 	public String getSource() {
 		return source;
 	}
 
 
 	public void setSource(String source) {
 		this.source = source;
 	}
 
 	public String getBundledJarDir() {
 		return bundledJarDir;
 	}
 
 
 	public void setBundledJarDir(String bundledJarDir) {
 		this.bundledJarDir = bundledJarDir;
 	}
 
 
 	public UploadParameters getUploadParameters() {
 		return uploadParameters;
 	}
 
 	public List<String> getRelations() {
 		return relations;
 	}
 
 
 	public void setRelations(List<String> relations) {
 		this.relations = relations;
 	}
 
 	public void setChosenDistro(String distro) {
 		this.chosenDistro=distro;
 		
 	}
 
 
 	public void setTarget(String target) {
 		this.target=target;		
 	}
 
 
 	public String getDefaultDistro() {
 		return defaultDistro;
 	}
 	
 	public void setDefaultDistro(String distro) {
 		this.defaultDistro = distro;
 	}
 
 }
