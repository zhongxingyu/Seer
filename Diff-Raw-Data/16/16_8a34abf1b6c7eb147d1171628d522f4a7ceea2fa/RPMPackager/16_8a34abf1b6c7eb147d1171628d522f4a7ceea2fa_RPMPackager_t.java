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
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 
 import de.tarent.maven.plugins.pkg.AbstractPackagingMojo;
 import de.tarent.maven.plugins.pkg.Path;
 import de.tarent.maven.plugins.pkg.TargetConfiguration;
 import de.tarent.maven.plugins.pkg.Utils;
 import de.tarent.maven.plugins.pkg.WorkspaceSession;
 import de.tarent.maven.plugins.pkg.generator.SpecFileGenerator;
 import de.tarent.maven.plugins.pkg.helper.ArtifactInclusionStrategy;
 import de.tarent.maven.plugins.pkg.helper.Helper;
 
 /**
  * Creates a RPM package file
  * 
  * @author plafue
  */
 public class RPMPackager extends Packager {
 
 	private static final String UNKNOWN = "unknown";
 
 	@Override
 	public void execute(Log l, WorkspaceSession workspaceSession)
 			throws MojoExecutionException {
 		TargetConfiguration distroConfig = workspaceSession
 				.getTargetConfiguration();
 		Helper ph = workspaceSession.getHelper();
 
 		// Configure the Helper for RPM use.
 		ph.setStrategy(Helper.RPM_STRATEGY);
 
 		ph.prepareInitialDirectories();
 
 		// Setting all destination directories to /BUILD/ + target name
 		ph.setDstSBinDir(new File(ph.getBaseBuildDir(), ph.getTargetSBinDir()
 				.toString()));
 		ph.setDstBinDir(new File(ph.getBaseBuildDir(), ph.getTargetBinDir()
 				.toString()));
 		ph.setDstSysconfDir(new File(ph.getBaseBuildDir(), ph
 				.getTargetSysconfDir().toString()));
 		ph.setDstDatarootDir(new File(ph.getBaseBuildDir(), ph
 				.getTargetDatarootDir().toString()));
 		ph.setDstDataDir(new File(ph.getBaseBuildDir(), ph.getTargetDataDir()
 				.toString()));
 		ph.setDstJNIDir(new File(ph.getBaseBuildDir(), ph.getTargetJNIDir()
 				.toString()));
 		ph.setDstBundledJarDir(new File(ph.getBaseBuildDir(), ph
 				.getTargetBundledJarDir().toString()));
 		ph.setDstStarterDir(new File(ph.getBaseBuildDir(), ph
 				.getTargetStarterDir().toString()));
 		ph.setDstWrapperScriptFile(new File(ph.getBaseBuildDir(), ph
 				.getTargetWrapperScriptFile().toString()));
 
 		ph.copyFiles();
 
 		l.debug(ph.getPackageName());
 		l.debug(ph.getPackageVersion());
 		l.debug(ph.getBasePkgDir().getPath());
 
 		// A set which will be filled with the artifacts which need to be
 		// bundled with the
 		// application.
 		Set<Artifact> bundledArtifacts = null;
 		Path bcp = new Path();
 		Path cp = new Path();
 
 		ArtifactInclusionStrategy aiStrategy = workspaceSession
 				.getArtifactInclusionStrategy();
 		ArtifactInclusionStrategy.Result result = aiStrategy
 				.processArtifacts(ph);
 
 		// The user may want to avoid including dependencies
 		if (distroConfig.isBundleDependencyArtifacts()) {
 			bundledArtifacts = ph.bundleDependencies(
 					result.getResolvedDependencies(), bcp, cp);
 			ph.copyArtifacts(bundledArtifacts);
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
 
 		File specFile = new File(ph.getBaseSpecsDir(), ph.getPackageName()
 				+ ".spec");
 
 		try {
 
 			generateSPECFile(l, ph, distroConfig,
 					result.getResolvedDependencies(), specFile);
 			l.info("SPEC file generated.");
 			createPackage(l, workspaceSession, specFile);
 			l.info("Package created.");
 
 			File resultingPackage = copyRPMToTargetFolder(workspaceSession);
 
 			l.info("Output of rpm -pqi :");
 			String out = IOUtils.toString(Utils.exec(new String[] { "rpm",
 					"-pqi", resultingPackage.getAbsolutePath() },
 					resultingPackage.getParentFile(), "RPM not found",
 					"RPM not found"));
 
 			l.info("=======================================");
 			for (String s : out.split("\\r?\\n")) {
 				l.info(s);
 			}
 			l.info("=======================================");
 
 		} catch (Exception ex) {
 			throw new MojoExecutionException(ex.toString(), ex);
 		} finally {
 			try {
 				ph.restoreRpmMacrosFileBackup(l);
 			} catch (IOException e) {
 				throw new MojoExecutionException(e.toString(), e);
 			}
 		}
 	}
 
 	/**
 	 * Copies the created artifact from
 	 * 
 	 * @param l
 	 * @param ph
 	 * @param distroConfig
 	 * @return
 	 * @throws IOException
 	 */
 	private File copyRPMToTargetFolder(WorkspaceSession ws)
 			throws MojoExecutionException, IOException {
 		Helper ph = ws.getHelper();
 		Log l = ws.getMojo().getLog();
 		StringBuilder rpmPackagePath = new StringBuilder(ph.getBaseBuildDir()
 				.getParent());
 		rpmPackagePath.append("/RPMS/");
 		rpmPackagePath.append(ph.getArchitecture());
 		rpmPackagePath.append("/");
 		String rpmPackageName = ph.getPackageFileName();
 
 		File targetFile = new File(ws.getMojo().getTempRoot().getParentFile(),
 				rpmPackageName);
 
 		l.debug("Attempting to copy from " + rpmPackagePath.toString()
 				+ rpmPackageName + " to " + targetFile.getAbsolutePath());
 
 		FileUtils.copyFile(new File(rpmPackagePath.toString(), rpmPackageName),
 				targetFile);
 
 		l.info("RPM file copied to " + targetFile.getAbsolutePath());
 		return targetFile;
 	}
 
 	/**
 	 * Will prepare the custom Build Area by creating the .rpmmacrosfile and
 	 * will check for rpmbuild to exist.
 	 */
 	@Override
 	public void checkEnvironment(Log l, WorkspaceSession workspaceSession)
 			throws MojoExecutionException {
 		Helper ph = workspaceSession.getHelper();
 
 		checkneededfields(ph, workspaceSession.getTargetConfiguration());
 		try {
 			Utils.checkProgramAvailability("gpg");
 			Utils.checkProgramAvailability("rpmbuild");
 			l.info(IOUtils.toString(
 					Utils.exec(new String[] { "rpm", "--version" }, null,
 							"Calling rpm --version failed", "ioError", null))
 					.trim());
 			ph.createRpmMacrosFile();
 
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage());
 		}
 	}
 
 	/**
 	 * Takes the parameters inside Packaging.Helper and generates the spec file
 	 * needed for rpmbuild to work.
 	 * 
 	 * @param l
 	 * @param ph
 	 * @param dc
 	 * @param specFile
 	 * @throws MojoExecutionException
 	 * @throws IOException
 	 */
 	private void generateSPECFile(Log l, Helper ph, TargetConfiguration dc,
 			Set<Artifact> resolvedDependencies, File specFile)
 			throws MojoExecutionException, IOException {
 		l.info("Creating SPEC file: " + specFile.getAbsolutePath());
 		SpecFileGenerator sgen = new SpecFileGenerator();
 		sgen.setLogger(l);
 		try {
 			sgen.setLogger(l);
 			sgen.setBuildroot("%{_builddir}");
 			// sgen.setCleancommands(generateCleanCommands(ph, dc));
 
 			// Following parameters MUST be provided for rpmbuild to work:
 			l.info("Adding mandatory parameters to SPEC file.");
 			sgen.setPackageName(ph.getPackageName());
 			sgen.setVersion(ph.getPackageVersion());
 			sgen.setSummary(ph.getProjectDescription());
 			sgen.setDescription(ph.getProjectDescription());
 			sgen.setLicense(ph.getLicense());
 			sgen.setRelease(dc.getRevision());
 			sgen.setSource(dc.getSource());
 			sgen.setUrl(ph.getProjectUrl());
 			sgen.setGroup(dc.getSection());
 			sgen.setDependencies(ph.createDependencyLine(resolvedDependencies));
 
 			// Following parameters are not mandatory
 			l.info("Adding optional parameters to SPEC file.");
 			sgen.setArch(ph.getArchitecture());
 			sgen.setPrefix(dc.getPrefix());
 			sgen.setPackager(dc.getMaintainer());
 			sgen.setFiles(ph.generateFilelist());
 
 			sgen.setPreinstallcommandsFromFile(ph.getSrcAuxFilesDir(),
 					dc.getPreinstScript());
 			sgen.setPostinstallcommandsFromFile(ph.getSrcAuxFilesDir(),
 					dc.getPostinstScript());
 			sgen.setPreuninstallcommandsFromFile(ph.getSrcAuxFilesDir(),
 					dc.getPrermScript());
 			sgen.setPostuninstallcommandsFromFile(ph.getSrcAuxFilesDir(),
 					dc.getPostrmScript());
 
 			l.info("Creating SPEC file: " + specFile.getAbsolutePath());
 			Utils.createFile(specFile, "spec");
 			sgen.generate(specFile);
 
 		} catch (IOException ioe) {
 			throw new MojoExecutionException(
 					"IOException while creating SPEC file.", ioe);
 		}
 
 	}
 
 	/**
 	 * Executes rpmbuild, that will generate the final rpm package.
 	 * 
 	 * If the parameter "sign" is set as true in pom, package will be signed
 	 * with the Maintainer (Packager) name provided.
 	 * 
 	 * @param l
 	 * @param ph
 	 * @param specFile
 	 * @throws MojoExecutionException
 	 */
 	private void createPackage(Log l, WorkspaceSession workspaceSession,
 			File specFile) throws MojoExecutionException {
 
 		Helper ph = workspaceSession.getHelper();
 		TargetConfiguration dc = workspaceSession.getTargetConfiguration();
 		AbstractPackagingMojo apm = workspaceSession.getMojo();
 		l.info("Calling rpmbuild to create binary package");
 		l.info("Builddir is " + ph.getBaseBuildDir().toString());
 		String[] command;
 		if (dc.isSign()) {
 			command = new String[] { "rpmbuild", "-bb", "--sign",
 					"--buildroot", ph.getBaseBuildDir().toString(),
 					specFile.toString() };
 
 		} else {
 			command = new String[] { "rpmbuild", "-bb", "--buildroot",
 					ph.getBaseBuildDir().toString(), specFile.toString() };
 		}
 
 		if (apm.getSignPassPhrase() != null && dc.isSign()) {
 			Utils.exec(command, "'rpmbuild -bb' failed.",
 					"Error creating rpm file.", apm.getSignPassPhrase());
 		} else {
 			Utils.exec(command, "'rpmbuild -bb' failed.",
 					"Error creating rpm file.");
 		}
 
 	}
 
 	private void checkneededfields(Helper ph, TargetConfiguration dc)
 			throws MojoExecutionException {
 		if (ph.getPackageName() == null || ph.getPackageName().equals(UNKNOWN)
 				|| ph.getPackageVersion() == null
 				|| ph.getPackageVersion().equals(UNKNOWN)
 				|| ph.getProjectDescription() == null
 				|| ph.getProjectDescription().equals(UNKNOWN)
 				|| ph.getLicense() == null || ph.getLicense().equals(UNKNOWN)) {
 			String message = "At least PackageName, Version, Description and Summary "
 					+ "are needed for the spec file.";
 			throw new MojoExecutionException(message);
 		}
 	}
 }
