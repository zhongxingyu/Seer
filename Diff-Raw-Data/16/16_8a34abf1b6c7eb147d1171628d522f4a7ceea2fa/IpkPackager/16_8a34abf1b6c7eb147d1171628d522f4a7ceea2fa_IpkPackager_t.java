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
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.codehaus.plexus.archiver.ArchiveFile;
 import org.codehaus.plexus.archiver.tar.GZipTarFile;
 
 import de.tarent.maven.plugins.pkg.Path;
 import de.tarent.maven.plugins.pkg.TargetConfiguration;
 import de.tarent.maven.plugins.pkg.Utils;
 import de.tarent.maven.plugins.pkg.WorkspaceSession;
 import de.tarent.maven.plugins.pkg.generator.ControlFileGenerator;
 import de.tarent.maven.plugins.pkg.helper.ArtifactInclusionStrategy;
 import de.tarent.maven.plugins.pkg.helper.Helper;
 
 /**
  * Creates a Debian package file (.deb)
  * 
  */
 public class IpkPackager extends Packager {
 
 	File IPKGBUILD;
 
 	public void execute(Log l, WorkspaceSession workspaceSession)
 			throws MojoExecutionException {
 		TargetConfiguration distroConfig = workspaceSession
 				.getTargetConfiguration();
 		Helper ph = workspaceSession.getHelper();
 
 		String packageName = ph.getPackageName();
 		String packageVersion = ph.getPackageVersion();
 
 		ph.setDstScriptDir(new File(ph.getBasePkgDir(), "CONTROL"));
 
 		File controlFile = new File(ph.getBasePkgDir(), "CONTROL/control");
 
 		// A set which will be filled with the artifacts which need to be
 		// bundled with the
 		// application.
 		Set<Artifact> bundledArtifacts = null;
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
 
 		byteAmount += ph.copyScripts();
 
 		byteAmount += ph.createCopyrightFile();
 
 		ArtifactInclusionStrategy aiStrategy = workspaceSession
 				.getArtifactInclusionStrategy();
 		ArtifactInclusionStrategy.Result result = aiStrategy
 				.processArtifacts(ph);
 
 		// The user may want to avoid including dependencies
 		if (distroConfig.isBundleDependencyArtifacts()) {
 			bundledArtifacts = ph.bundleDependencies(
 					result.getResolvedDependencies(), bcp, cp);
 			byteAmount += ph.copyArtifacts(bundledArtifacts);
 		}
 
 		// Create classpath line, copy bundled jars and generate wrapper
 		// start script only if the project is an application.
 		if (distroConfig.getMainClass() != null) {
 			// TODO: Handle native library artifacts properly.
			if (!distroConfig.isBundleDependencyArtifacts()) {
 				ph.createClasspathLine(bcp, cp);
 			}
 			ph.generateWrapperScript(bcp, cp, false);
 		}
 
 		generateControlFile(l, ph, distroConfig, controlFile, packageName,
 				packageVersion,
 				ph.createDependencyLine(result.getResolvedDependencies()),
 				byteAmount);
 
 		createPackage(l, ph, ph.getBasePkgDir());
 
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
 	public void checkEnvironment(Log l, WorkspaceSession workspaceSession)
 			throws MojoExecutionException {
 		TargetConfiguration targetConfiguration = workspaceSession
 				.getTargetConfiguration();
 
 		boolean error = false;
 
 		l.info("Maintainer               : "
 				+ targetConfiguration.getMaintainer());
 
 		if (targetConfiguration.getMaintainer() == null) {
 			l.error("The maintainer field of the distro configuration is not set, however this is mandatory for IPK packaging!");
 			error = true;
 		}
 
 		/*
 		 * The ipkg-build tool, needed by this class is delivered within this
 		 * package in the ipkg-utils-050831.tar.gz archive. It will be extracted
 		 * to a temporary location and deleted as soon as the vm exits.
 		 */
 
 		ArchiveFile IpkUtilsGZip;
 		try {
 			IpkUtilsGZip = new GZipTarFile(new File(IpkPackager.class
 					.getResource("ipkg-utils-050831.tar.gz").toURI()));
 		} catch (URISyntaxException ex) {
 			throw new MojoExecutionException(
 					"Location for Zip file is malformed.", ex);
 		}
 
 		IPKGBUILD = Utils.getFileFromArchive(IpkUtilsGZip,
 				"ipkg-utils-050831/ipkg-build");
 		// The tool needs to be executable
 		IPKGBUILD.setExecutable(true);
 
 		if (error) {
 			throw new MojoExecutionException("Aborting due to earlier errors.");
 		}
 	}
 
 	/**
 	 * Creates a control file whose dependency line can be provided as an
 	 * argument.
 	 * 
 	 */
 	private void generateControlFile(Log l, Helper ph, TargetConfiguration dc,
 			File controlFile, String packageName, String packageVersion,
 			String dependencyLine, long byteAmount)
 			throws MojoExecutionException {
 		ControlFileGenerator cgen = new ControlFileGenerator();
 		cgen.setPackageName(packageName);
 		cgen.setVersion(packageVersion);
 		cgen.setSection(dc.getSection());
 		cgen.setDependencies(dependencyLine);
 		cgen.setMaintainer(dc.getMaintainer());
 		cgen.setArchitecture(dc.getArchitecture());
 		cgen.setOE(packageName + "-" + packageVersion);
 
 		String url = ph.getProjectUrl();
 		if (url == null) {
 			l.warn("Project has no <url> field. However IPK packages require this. Using a dummy for now.");
 			url = "http://not-yet-set.org/" + packageName;
 		}
 		cgen.setSource(url);
 		cgen.setHomepage(url);
 
 		String desc = ph.getProjectDescription();
 		if (desc == null) {
 			l.warn("Project has no <description> field. However IPK packages require this. Using a placeholder for now.");
 			desc = "No description given yet.";
 		}
 		cgen.setShortDescription(desc);
 
 		l.info("creating control file: " + controlFile.getAbsolutePath());
 		Utils.createFile(controlFile, "control");
 
 		try {
 			cgen.generate(controlFile);
 		} catch (IOException ioe) {
 			throw new MojoExecutionException(
 					"IOException while creating control file.", ioe);
 		}
 	}
 
 	private void createPackage(Log l, Helper ph, File base)
 			throws MojoExecutionException {
 		l.info("calling ipkg-build to create binary package");
 
 		Utils.exec(new String[] { IPKGBUILD.getAbsolutePath(), "-o", "root",
 				"-g", "root", base.getName(),
 				ph.getOutputDirectory().getAbsolutePath() },
 				base.getParentFile(), "'ipkg-build failed.",
 				"Error creating the .ipk file.");
 	}
 
 }
