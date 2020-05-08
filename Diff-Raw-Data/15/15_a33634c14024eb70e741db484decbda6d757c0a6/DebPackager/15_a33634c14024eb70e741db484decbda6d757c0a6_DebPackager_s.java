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
 
 /*
  * Maven Packaging Plugin,
  * Maven plugin to package a Project (deb and izpack)
  * Copyright (C) 2000-2007 tarent GmbH
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
  * Signature of Elmar Geese, 14 June 2007
  * Elmar Geese, CEO tarent GmbH.
  */
 
 package de.tarent.maven.plugins.pkg.packager;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 
 import de.tarent.maven.plugins.pkg.AbstractPackagingMojo;
 import de.tarent.maven.plugins.pkg.AotCompileUtils;
 import de.tarent.maven.plugins.pkg.Path;
 import de.tarent.maven.plugins.pkg.SysconfFile;
 import de.tarent.maven.plugins.pkg.TargetConfiguration;
 import de.tarent.maven.plugins.pkg.Utils;
 import de.tarent.maven.plugins.pkg.WorkspaceSession;
 import de.tarent.maven.plugins.pkg.generator.ControlFileGenerator;
 import de.tarent.maven.plugins.pkg.helper.ArtifactInclusionStrategy;
 import de.tarent.maven.plugins.pkg.helper.Helper;
 import de.tarent.maven.plugins.pkg.map.PackageMap;
 import de.tarent.maven.plugins.pkg.signing.DebianSigner;
 
 /**
  * Creates a Debian package file (.deb)
  * 
  * TODO: Description needs to formatted in a Debian-specific way
  */
 public class DebPackager extends Packager {
 
 	private static final String GPGORIGIN_AR = "_gpgorigin.ar";
 	private static final String GPGORIGIN = "_gpgorigin";
 	private static final String COMBINEDCONTENTSFILENAME = "combined-contents";
 
 	public void execute(Log l, WorkspaceSession workspaceSession)
 			throws MojoExecutionException {
 
 		TargetConfiguration targetConfiguration = workspaceSession
 				.getTargetConfiguration();
 		Helper ph = workspaceSession.getHelper();
 		PackageMap packageMap = workspaceSession.getPackageMap();
 
 		String packageName = ph.getPackageName();
 		String packageVersion = ph.getPackageVersion();
 
 		File basePkgDir = ph.getBasePkgDir();
 
 		// Provide a proper default value to make script file copying work.
 		ph.setDstScriptDir(new File(basePkgDir, "DEBIAN"));
 
 		// The Debian control file (package name, dependencies etc).
 		File controlFile = new File(basePkgDir, "DEBIAN/control");
 
 		// The file listing the config files.
 		File conffilesFile = new File(basePkgDir, "DEBIAN/conffiles");
 
 		File srcArtifactFile = ph.getSrcArtifactFile();
 
 		String gcjPackageName = ph.getAotPackageName();
 
 		File aotPkgDir = ph.getAotPkgDir();
 
 		// The file extension for aot-compiled binaries.
 		String aotExtension = ".jar.so";
 
 		// The destination directory for all aot-compiled binaries.
 		File aotDstDir = new File(aotPkgDir, packageMap.getDefaultJarPath());
 
 		// The file name of the aot-compiled binary of the project's own
 		// artifact.
 		File aotCompiledBinaryFile = new File(aotDstDir, ph.getArtifactId()
 				+ aotExtension);
 
 		// The destination directory for all classmap files.
 		File aotDstClassmapDir = new File(aotPkgDir,
 				"usr/share/gcj-4.1/classmap.d");
 
 		// The file name of the classmap of the project's own artifact.
 		File aotClassmapFile = new File(aotDstClassmapDir, ph.getArtifactId()
 				+ ".db");
 
 		// The destination file for the 'postinst' script.
 		File aotPostinstFile = new File(aotPkgDir, "DEBIAN/postinst");
 
 		// The destination file for the 'control' file.
 		File aotControlFile = new File(aotPkgDir, "DEBIAN/control");
 
 		// A set which will be filled with the artifacts which need to be
 		// bundled with the
 		// application.
 		Set<Artifact> bundledArtifacts = new HashSet<Artifact>();
 		Path bcp = new Path();
 		Path cp = new Path();
 
 		long byteAmount = 0;
 
 		// The following section does the coarse-grained steps
 		// to build the package(s). It is meant to be kept clean
 		// from simple Java statements. Put additional functionality
 		// into the existing methods or create a new one and add it
 		// here.
 
 		ph.prepareInitialDirectories();
 
 		byteAmount += ph.copyFiles();
 
 		generateConffilesFile(l, conffilesFile, targetConfiguration, ph);
 
 		byteAmount += ph.copyScripts();
 
 		byteAmount += ph.createCopyrightFile();
 
 		ArtifactInclusionStrategy aiStrategy = workspaceSession
 				.getArtifactInclusionStrategy();
 		ArtifactInclusionStrategy.Result result = aiStrategy
 				.processArtifacts(ph);
 
 		byteAmount += result.getByteAmount();
 
 		// Create classpath line, copy bundled jars and generate wrapper
 		// start script only if the project is an application.
 		if (targetConfiguration.getMainClass() != null) {
 			// TODO: Handle native library artifacts properly.
 			// bundledArtifacts = ph.createClasspathLine(bcp, cp);
 			ph.createClasspathLine(bcp, cp);
 			ph.generateWrapperScript(bcp, cp, false);
 		}
 
		// The user may want to avoid including dependencies
		if (targetConfiguration.isBundleDependencyArtifacts()) {
			bundledArtifacts = ph.bundleDependencies(
					result.getResolvedDependencies(), bcp, cp);
			byteAmount += ph.copyArtifacts(bundledArtifacts);
		}

 		generateControlFile(l, targetConfiguration, ph, controlFile,
 				packageName, packageVersion,
 				ph.createDependencyLine(result.getResolvedDependencies()),
 				ph.createRecommendsLine(), ph.createSuggestsLine(),
 				ph.createProvidesLine(), ph.createConflictsLine(),
 				ph.createReplacesLine(), byteAmount);
 
 		createPackage(l, workspaceSession, basePkgDir, targetConfiguration);
 
 		if (targetConfiguration.isAotCompile()) {
 			ph.prepareAotDirectories();
 			// At this point anything created in the basePkgDir cannot be used
 			// any more as it was removed by the above method.
 
 			byteAmount = aotCompiledBinaryFile.length()
 					+ aotClassmapFile.length();
 
 			AotCompileUtils.compile(l, srcArtifactFile, aotCompiledBinaryFile);
 
 			AotCompileUtils.generateClassmap(l, aotClassmapFile,
 					srcArtifactFile, aotCompiledBinaryFile,
 					packageMap.getDefaultJarPath());
 
 			// AOT-compile and classmap generation for bundled Jar libraries
 			// are only needed for applications.
 			if (targetConfiguration.getMainClass() != null) {
 				byteAmount += AotCompileUtils.compileAndMap(l,
 						bundledArtifacts, aotDstDir, aotExtension,
 						aotDstClassmapDir, packageMap.getDefaultJarPath());
 			}
 
 			// The dependencies of a "-gcj" package are always java-gcj-compat
 			// and the corresponding 'bytecode' version of the package.
 			// GCJ can only compile for one architecture.
 			targetConfiguration.setArchitecture(System.getProperty("os.arch"));
 			generateControlFile(l, targetConfiguration, ph, aotControlFile,
 					gcjPackageName, packageVersion, "java-gcj-compat", null,
 					null, null, null, null, byteAmount);
 
 			AotCompileUtils.depositPostinstFile(l, aotPostinstFile);
 
 			createPackage(l, workspaceSession, aotPkgDir, targetConfiguration);
 		}
 
 		/*
 		 * When the Mojo fails to complete its task the work directory will be
 		 * left in an unclean state to make it easier to debug problems.
 		 * 
 		 * However the work dir will be cleaned up when the task is run next
 		 * time.
 		 */
 	}
 
 	/**
 	 * Validates arguments and test tools.
 	 * 
 	 * @throws MojoExecutionException
 	 */
 	@Override
 	public void checkEnvironment(Log l, WorkspaceSession workspaceSession)
 			throws MojoExecutionException {
 
 		Utils.checkProgramAvailability("dpkg-deb");
 		String output = Utils.getProgramVersionOutput("dpkg-deb");
 
 		/*
 		 * We will check if the minor version number is smaller than 15.
 		 * Problems have been reported regarding older versions of dpkg-deb and
 		 * - what dpkg-deb understands as version numbers - not containing
 		 * numbers (bug #2912), e.g. "-SNAPSHOT".
 		 */
 
 		Pattern p = Pattern
 				.compile("version (1\\.([0-9]{2})\\.([0-9]*\\.*)*)* ",
 						Pattern.MULTILINE);
 		Matcher m = p.matcher(output);
 		if (m.find()) {
 			l.info("dpkg-deb version: " + m.group(1));
 			int versionNumber = Integer.parseInt(m.group(2));
 			if (versionNumber < 15
 					&& workspaceSession.getHelper().getPackageVersion()
 							.contains("-SNAPSHOT")) {
 				throw new MojoExecutionException(
 						"You are trying to build a snapshot with an older version of dpkg-deb. "
 								+ "You are advised to add a revision number to your package (e.g. r1) or "
 								+ "your package will not be built correctly.");
 			}
 		}
 		// Some external tools are only needed if the package is to be signed
 		if (workspaceSession.getTargetConfiguration().isSign()) {
 			Utils.checkProgramAvailability("dpkg-distaddfile");
 			Utils.checkProgramAvailability("gpg");
 			Utils.checkProgramAvailability("ar");
 		}
 	}
 
 	/**
 	 * Creates a control file whose dependency line can be provided as an
 	 * argument.
 	 * 
 	 * @param l
 	 * @param controlFile
 	 * @param packageName
 	 * @param packageVersion
 	 * @param installedSize
 	 * @throws MojoExecutionException
 	 */
 	private void generateControlFile(Log l,
 			TargetConfiguration targetConfiguration, Helper ph,
 			File controlFile, String packageName, String packageVersion,
 			String dependencyLine, String recommendsLine, String suggestsLine,
 			String providesLine, String conflictsLine, String replacesLine,
 			long byteAmount) throws MojoExecutionException {
 		ControlFileGenerator cgen = new ControlFileGenerator();
 		cgen.setPackageName(packageName);
 		cgen.setVersion(packageVersion);
 		cgen.setSection(targetConfiguration.getSection());
 		cgen.setDependencies(dependencyLine);
 		cgen.setRecommends(recommendsLine);
 		cgen.setSuggests(suggestsLine);
 		cgen.setProvides(providesLine);
 		cgen.setConflicts(conflictsLine);
 		cgen.setReplaces(replacesLine);
 		cgen.setMaintainer(targetConfiguration.getMaintainer());
 		cgen.setShortDescription(ph.getProjectDescription());
 		cgen.setDescription(ph.getProjectDescription());
 		cgen.setArchitecture(targetConfiguration.getArchitecture());
 		cgen.setInstalledSize(Utils.getInstalledSize(byteAmount));
 
 		l.info("creating control file: " + controlFile.getAbsolutePath());
 		Utils.createFile(controlFile, "control");
 
 		try {
 			cgen.generate(controlFile);
 		} catch (IOException ioe) {
 			throw new MojoExecutionException(
 					"IOException while creating control file.", ioe);
 		}
 
 	}
 
 	/**
 	 * Iterates over the sysconf files and creates the Debian 'conffiles' file
 	 * for them.
 	 * 
 	 * <p>
 	 * If no sysconf files exists nothing is done however.
 	 * </p>
 	 * 
 	 * @param l
 	 * @param conffilesFile
 	 * @param ph
 	 * @param tc
 	 * @throws MojoExecutionException
 	 */
 	private void generateConffilesFile(Log l, File conffilesFile,
 			TargetConfiguration targetConfiguration, Helper ph)
 			throws MojoExecutionException {
 		List<SysconfFile> sysconffiles = (List<SysconfFile>) targetConfiguration
 				.getSysconfFiles();
 		if (sysconffiles.isEmpty()) {
 			l.info("No sysconf files defined - not creating file.");
 			return;
 		}
 
 		StringBuilder sb = new StringBuilder(sysconffiles.size() * 10);
 		for (SysconfFile scf : sysconffiles) {
 			File targetFile;
 			if (scf.isRename()) {
 				targetFile = new File(ph.getTargetSysconfDir(), scf.getTo());
 			} else {
 				File srcFile = new File(ph.getSrcSysconfFilesDir(),
 						scf.getFrom());
 				File targetPath = new File(ph.getTargetSysconfDir(),
 						scf.getTo());
 				targetFile = new File(targetPath, srcFile.getName());
 			}
 			sb.append(targetFile.getAbsolutePath());
 			sb.append("\n");
 		}
 
 		if (!conffilesFile.getParentFile().mkdirs()) {
 			throw new MojoExecutionException(
 					"Could not create directory for conffiles file.");
 		}
 		try {
 			conffilesFile.createNewFile();
 		} catch (IOException ioe) {
 			throw new MojoExecutionException(
 					"IOException while creating conffiles file.", ioe);
 		}
 
 		try {
 			FileUtils.writeStringToFile(conffilesFile, sb.toString());
 		} catch (IOException ioe) {
 			throw new MojoExecutionException(
 					"IOException while writing to conffiles file.", ioe);
 		}
 	}
 
 	private void createPackage(Log l, WorkspaceSession workspaceSession,
 			File base, TargetConfiguration targetConfiguration)
 			throws MojoExecutionException {
 		l.info("calling dpkg-deb to create binary package");
 		Helper ph = workspaceSession.getHelper();
 
 		Utils.exec(
 				new String[] { "fakeroot", "dpkg-deb", "--build",
 						base.getName(),
 						ph.getOutputDirectory().getAbsolutePath() },
 				base.getParentFile(), "'fakeroot dpkg --build' failed.",
 				"Error creating the .deb file.");
 
 		if (targetConfiguration.isSign()) {
 			// This bundles the signature with the package
 			bundleSignatureWithPackage(workspaceSession);
 
 			/*
 			 * This creates a signed .changes file - needed when uploading to
 			 * repository. Unfortunatelly debsign, the program used to perform
 			 * this action won't allow entering the passphrase through an
 			 * automated process. TODO:Find a way to do this, or change the
 			 * packager executable to also sign
 			 */
 
 			DebianSigner db = new DebianSigner(workspaceSession, false);
 			db.start(l);
 
 		}
 
 	}
 
 	/**
 	 * Puts a pgp signature in the package based on the maintainer name
 	 * <p>
 	 * The signature for the maintainer name must have been importend in the gpg
 	 * keyring and the name of the maintainer must match with the one set in the
 	 * POM
 	 * </p>
 	 * <p>
 	 * These are the steps followed by this method:<br/>
 	 * <ul>
 	 * <li>Take an existing .deb and unpack it:<br/>
 	 * $ ar x my_package_1_0_0.deb</li>
 	 * <li>Concatenate its contents (the order is important), and output to a
 	 * temp file:<br/>
 	 * $ cat debian-binary control.tar.gz data.tar.gz > /tmp/combined-contents<br/>
 	 * </li>
 	 * <li>Create a GPG signature of the concatenated file, calling it
 	 * _gpgorigin:<br/>
 	 * $ gpg -abs -o _gpgorigin /tmp/combined-contents</li>
 	 * <li>Finally, archive the _gpgorigin file and append it to the original
 	 * deb file<br/>
 	 * $ ar rc _gpgorigin.ar _gpgorigin<br/>
 	 * </li>
 	 * </ul>
 	 * </p>
 	 * 
 	 * @param workspaceSession
 	 * @throws MojoExecutionException
 	 */
 	private void bundleSignatureWithPackage(WorkspaceSession workspaceSession)
 			throws MojoExecutionException {
 
 		File tempRoot = workspaceSession.getMojo().getTempRoot();
 		String packageFilename = workspaceSession.getHelper()
 				.getPackageFileName();
 		String maintainer = workspaceSession.getTargetConfiguration()
 				.getMaintainer();
 		AbstractPackagingMojo apm = workspaceSession.getMojo();
 		Utils.exec(new String[] { "ar", "x", packageFilename },
 				tempRoot.getParentFile(), "Error extracting package",
 				"Error extracting package");
 		try {
 			File combinedContents = new File(tempRoot.getParentFile(),
 					COMBINEDCONTENTSFILENAME);
 			FileWriter fw = new FileWriter(combinedContents);
 
 			InputStream stream = Utils.exec(new String[] { "cat",
 					"debian-binary", "control.tar.gz", "data.tar.gz" },
 					tempRoot.getParentFile(),
 					"Error creating concatenated content",
 					"Error creating concatenated content");
 			IOUtils.copy(stream, fw);
 			fw.close();
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 
 		// If signPassPhrase has been set we don't expect any interaction with
 		// the user
 		// therefore we call gpg with --tty
 		if (apm.getSignPassPhrase() != null) {
 			Utils.exec(
 					new String[] { "gpg", "--no-tty", "--passphrase",
 							apm.getSignPassPhrase(), "--default-key",
 							maintainer, "--no-use-agent", "-abs", "-o",
 							GPGORIGIN, COMBINEDCONTENTSFILENAME },
 					tempRoot.getParentFile(),
 					"Error signing concatenated file",
 					"Error writing concatenated file");
 
 		} else {
 			Utils.exec(new String[] { "gpg", "--default-key", maintainer,
 					"-abs", "-o", GPGORIGIN, COMBINEDCONTENTSFILENAME },
 					tempRoot.getParentFile(),
 					"Error signing concatenated file",
 					"Error writing concatenated file");
 		}
 
 		Utils.exec(new String[] { "ar", "rc", GPGORIGIN_AR, GPGORIGIN },
 				tempRoot.getParentFile(),
 				"Error putting package back together",
 				"Error writing package parts into package");
 
 		// We will create a FileInputStream in order to read the ar containing
 		// the signature
 		FileInputStream fis = null;
 		try {
 			fis = new FileInputStream(new File(tempRoot.getParentFile(),
 					GPGORIGIN_AR));
 		} catch (FileNotFoundException e1) {
 			throw new MojoExecutionException(
 					"Ar containing signature not found");
 		}
 
 		// We will avoid copying the header of the ar file
 		try {
 			fis.skip(8);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 		/*
 		 * We will create a FileOutputStream in order to write in the debian
 		 * package
 		 */
 		FileOutputStream fos = null;
 		try {
 			fos = new FileOutputStream(new File(tempRoot.getParentFile(),
 					packageFilename), true);
 		} catch (FileNotFoundException e) {
 			IOUtils.closeQuietly(fis);
 			throw new MojoExecutionException(
 					"Package file to be signed could not be found");
 		}
 
 		// We copy one stream into the other
 		try {
 			int unit;
 			while ((unit = fis.read()) != -1) {
 				fos.write(unit);
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException(
 					"Error appending _gpgorigin to archive");
 		} finally {
 			try {
 				fis.close();
 			} catch (IOException e) {
 				throw new MojoExecutionException(
 						"Error closing input stream from _gpgorigin archive");
 			}
 			try {
 				fos.close();
 			} catch (IOException e) {
 				throw new MojoExecutionException(
 						"Error closing output stream to debian package");
 			}
 		}
 
 		// Here we clean the artifacts created while signing
 		File f = new File(tempRoot.getParentFile(), "debian-binary");
 		f.delete();
 		f = new File(tempRoot.getParentFile(), "control.tar.gz");
 		f.delete();
 		f = new File(tempRoot.getParentFile(), "data.tar.gz");
 		f.delete();
 		f = new File(tempRoot.getParentFile(), GPGORIGIN);
 		f.delete();
 		f = new File(tempRoot.getParentFile(), GPGORIGIN_AR);
 		f.delete();
 		f = new File(tempRoot.getParentFile(), COMBINEDCONTENTSFILENAME);
 		f.delete();
 	}
 
 }
