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
 import java.util.Enumeration;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 
 import de.tarent.maven.plugins.pkg.DistroConfiguration;
 import de.tarent.maven.plugins.pkg.Packaging;
 import de.tarent.maven.plugins.pkg.Utils;
 import de.tarent.maven.plugins.pkg.map.PackageMap;
 
 public class IzPackPackager extends Packager
 {
   private static final String IZPACK_EMBEDDED_JAR = "izpack-embedded.jar";
   
   public void execute(Log l,
                       Packaging.Helper ph,
                       DistroConfiguration distroConfig,
                       PackageMap packageMap) throws MojoExecutionException
   {
     // The root directory into which everything from srcRoot is copied
     // into (inside the outputDirectory).
     File packagingBaseDir = new File(ph.getTempRoot(), "izpack-packaging");
     ph.setBasePkgDir(packagingBaseDir);
     ph.setDstAuxDir(packagingBaseDir);
     
     ph.setTargetSysconfDir(new File("${INSTALL_PATH}"));
     ph.setDstSysconfDir(packagingBaseDir);
 
     ph.setTargetDatarootDir(new File("${INSTALL_PATH}"));
     ph.setDstDatarootDir(packagingBaseDir);
     
     ph.setTargetDataDir(new File("${INSTALL_PATH}"));
     ph.setDstDataDir(packagingBaseDir);
     
     // The root directory into which the jars from the dependencies
     // are put.
     ph.setDstBundledJarDir(new File(packagingBaseDir, "lib"));
     ph.setTargetBundledJarDir(new File("%{INSTALL_PATH}", "lib"));
     
     // Sets where to copy the JNI libraries
     ph.setTargetJNIDir(new File("%{INSTALL_PATH}", "lib"));
     ph.setDstJNIDir(new File(packagingBaseDir, "lib"));
     
     // Overrides default dst artifact file.
     ph.setDstArtifactFile(new File(ph.getDstBundledJarDir(), ph.getArtifactId() + ".jar"));
     
     // The root directory into which the starter and the classpath
     // properties file are put.
     ph.setDstStarterDir(new File(packagingBaseDir, "_starter"));
     ph.setTargetStarterDir(new File("%{INSTALL_PATH}", "_starter"));
     
     // The XML file for IzPack which describes how to generate the installer. 
     File installerXmlFile = new File(packagingBaseDir, distroConfig.getIzPackInstallerXml());
     File modifiedInstallerXmlFile = new File(packagingBaseDir, "modified-" + distroConfig.getIzPackInstallerXml());
     
     // The resulting Jar file which contains the runnable installer.
     File resultFile = new File(ph.getOutputDirectory(), ph.getPackageName() + "-" + ph.getPackageVersion() + "-installer.jar");
 
     File resultFileWindows = new File(ph.getOutputDirectory(), ph.getPackageName() + "-" + ph.getPackageVersion() + "-installer.exe");
 
     File resultFileOSX = new File(ph.getOutputDirectory(), ph.getPackageName() + "-" + ph.getPackageVersion() + "-installer.app");
     
     // targetBinDir does not occur within any script. Therefore there is no need to
     // fumble with ${INSTALL_PATH}. The targetBinDir property will still be used to create
     // dstWrapperScriptFile but by setting it to "" it does not have any negative effect.
     // TODO: By splitting the target/dst variants from the actual filename this could
     // implemented more elegantly.
     ph.setTargetBinDir(new File(""));
     ph.setDstBinDir(packagingBaseDir);
     
     File wrapperScriptFile = ph.getDstWrapperScriptFile();
     File windowsWrapperScriptFile = ph.getDstWindowsWrapperScriptFile();
 
      // The destination file for the embedded IzPack installation.
     File izPackEmbeddedJarFile = new File(ph.getTempRoot(), IZPACK_EMBEDDED_JAR);
     
     // The directory in which the embedded IzPack installation is unpacked
     // at runtime.
     File izPackEmbeddedRoot = new File(ph.getTempRoot(), "izpack-embedded");
     
     Set bundledArtifacts = null;
     StringBuilder bcp = new StringBuilder();
     StringBuilder cp = new StringBuilder();
     
     try
       {
         prepareDirectories(l,
                            ph.getTempRoot(),
                            izPackEmbeddedRoot,
                            ph.getSrcIzPackFilesDir(),
                            packagingBaseDir,
                            ph.getDstBundledJarDir());
         
         unpackIzPack(l, izPackEmbeddedJarFile, izPackEmbeddedRoot);
         
         bundledArtifacts = ph.createClasspathLine(bcp, cp);
         
         ph.copyProjectArtifact();
         
         ph.copyArtifacts(bundledArtifacts);
         
         ph.copyFiles();
 
         l.info("parsing installer xml file: " + installerXmlFile);
         IzPackDescriptor desc = new IzPackDescriptor(installerXmlFile, "Unable to parse installer xml file.");
 
         l.info("adding/modifying basic information");
         desc.fillInfo(l, ph.getPackageName(), ph.getPackageVersion(), ph.getProjectUrl());
         
         desc.removeAotPack();
 
         ph.generateWrapperScript(bundledArtifacts, bcp.toString(), cp.toString(), true);
         
         if (distroConfig.isAdvancedStarter())
           desc.addStarter("_starter", "_classpath");
 
         l.info("adding wrapper script information.");
         desc.addUnixWrapperScript(wrapperScriptFile.getName(), ph.getProjectDescription());
         desc.addWindowsWrapperScript(windowsWrapperScriptFile.getName(), ph.getProjectDescription());
         
         l.info("writing modified installer xml file.");
         desc.finish(modifiedInstallerXmlFile, "Unable to write modified installer xml file.");
         
         createInstaller(l,
                         ph.getJavaExec(),
                         izPackEmbeddedRoot,
                         packagingBaseDir,
                         modifiedInstallerXmlFile,
                         resultFile);
         
 				if (distroConfig.isCreateWindowsExecutable())
 					createWindowsExecutable(l, izPackEmbeddedRoot, resultFile, resultFileWindows);
 
 				if (distroConfig.isCreateOSXApp())
 					createOSXExecutable(l, izPackEmbeddedRoot, resultFile, resultFileOSX);
       }
     catch (MojoExecutionException badMojo)
       {
         throw badMojo;
       }
     
     /* When the Mojo fails to complete its task the work directory will be left
      * in an unclean state to make it easier to debug problems.
      * 
      * However the work dir will be cleaned up when the task is run next time.
      */
   }
 
   /** Validates arguments and tests tools.
    * 
    * @throws MojoExecutionException
    */
   public void checkEnvironment(Log l, DistroConfiguration dc) throws MojoExecutionException
   {
     // TODO: On GNU/Linux, Unix check for python, 7za if and configure for calling the python script
 		// On Windows configure to run the exe file
   }
 
   /**
    * Creates the temporary and package base directory.
    * 
    * @param l
    * @param basePkgDir
    * @throws MojoExecutionException
    */
   private void prepareDirectories(Log l,
                                   File tempRoot,
                                   File izPackEmbeddedRoot,
                                   File srcDir,
                                   File tempDescriptorRoot,
                                   File libraryRoot)
       throws MojoExecutionException
   {
     l.info("creating temporary directory: " + tempRoot.getAbsolutePath());
 
     if (!tempRoot.exists() && !tempRoot.mkdirs())
       throw new MojoExecutionException("Could not create temporary directory.");
 
     l.info("cleaning the temporary directory");
     try
     {
       FileUtils.cleanDirectory(tempRoot);
     }
     catch (IOException ioe)
     {
       throw new MojoExecutionException("Exception while cleaning temporary directory.",
                                        ioe);
     }
 
     l.info("creating IzPack base directory: " + izPackEmbeddedRoot.getAbsolutePath());
     if (!izPackEmbeddedRoot.mkdirs())
       throw new MojoExecutionException("Could not create directory for the embedded IzPack installation.");
     
     if (!tempDescriptorRoot.mkdirs())
       throw new MojoExecutionException("Could not create base directory for the IzPack descriptor.");
     
     l.info("copying IzPack descriptor data");
     try
     {
       FileUtils.copyDirectory(srcDir, tempDescriptorRoot, Utils.FILTER);
     }
     catch (IOException ioe)
     {
       throw new MojoExecutionException("IOException while copying IzPack descriptor data.");
     }
     
     l.info("creating directory for dependencies: " + libraryRoot.getAbsolutePath());
     if (!libraryRoot.mkdirs())
       throw new MojoExecutionException("Could not create directory for the dependencies.");
 
   }
   
   /**
    * Puts the embedded izpack jar from the (resource) classpath into the work directory
    * and unpacks it there.
    * 
    * @param l
    * @param izPackEmbeddedFile
    * @param izPackEmbeddedHomeDir
    * @throws MojoExecutionException
    */
   private void unpackIzPack(Log l, File izPackEmbeddedFile, File izPackEmbeddedHomeDir)
   throws MojoExecutionException
   {
     l.info("storing embedded IzPack installation in " + izPackEmbeddedFile);
     Utils.storeInputStream(IzPackPackager.class.getResourceAsStream(IZPACK_EMBEDDED_JAR),
                            izPackEmbeddedFile,
                            "IOException while unpacking embedded IzPack installation.");
     
     l.info("unzipping embedded IzPack installation to" + izPackEmbeddedHomeDir);
     int count = 0;
     try
     {
     ZipFile zip = new ZipFile(izPackEmbeddedFile);
     Enumeration e = zip.entries();
     
     while (e.hasMoreElements())
       {
         count++;
         ZipEntry entry = (ZipEntry) e.nextElement();
         File unpacked = new File(izPackEmbeddedHomeDir,
                                    entry.getName());
         if (entry.isDirectory())
           unpacked.mkdirs(); // TODO: Check success.
         else
           {
             Utils.createFile(unpacked, "Unable to create ZIP file entry ");
             Utils.storeInputStream(zip.getInputStream(entry), unpacked,
                                    "IOException while unpacking ZIP file entry.");
           }
             
       }
     }
     catch (IOException ioe)
     {
       throw new MojoExecutionException("IOException while unpacking embedded IzPack installation.");
     }
     
     l.info("unpacked " + count + " entries");
   }
 
   private void createInstaller(Log l,
                                String javaExec,
                                File izPackHomeDir,
                                File izPackBaseDir,
                                File izPackDescriptorFile,
                                File izPackInstallerFile) throws MojoExecutionException
   {
     l.info("calling IzPack compiler to create installer package");
     
     // Command-line argument ordering and naming suitable for IzPack 3.9.0
     Utils.exec(new String[] { 
                              javaExec,
                              "-jar",
                              izPackHomeDir.getAbsolutePath() + "/lib/compiler.jar",
                              izPackDescriptorFile.getAbsolutePath(),
                              "-h",
                              izPackHomeDir.getAbsolutePath(),
                              "-b",
                              izPackBaseDir.getAbsolutePath(),
                              "-o",
                              izPackInstallerFile.getAbsolutePath()
     }, izPackHomeDir,
     "Unable to run IzPack.",
     "IOException while trying to run IzPack.");
   
   }
 
 	private void createWindowsExecutable(Log l,
                                          File izPackHomeDir,
                                          File installerFile,
                                          File windowsInstallerFile)
 	throws MojoExecutionException
 	{
 		l.info("calling izpack2exe.py to create Windows installer binary");
 
 		Utils.exec(new String[] {
		                         "python", "izpack2exe.py",
		                         "--file=" + installerFile.getAbsolutePath(),
		                         "--output=" + windowsInstallerFile.getAbsolutePath(),
		                         "--with-7z=7zr",
                                 "--no-upx"
		}, new File(izPackHomeDir, "utils/izpack2exe"),
     "Unable to run izpack2exe script",
     "IOException while trying to run iz2pack2exe script.");
 
 	}
  
 	private void createOSXExecutable(Log l,
                                      File izPackHomeDir,
                                      File installerFile,
                                      File osxInstallerFile)
 	throws MojoExecutionException
 	{
 		l.info("calling izpack2app.py to create OS X installer binary");
 
 		Utils.exec(new String[] {
       "python", "izpack2app.py",
        installerFile.getAbsolutePath(),
        osxInstallerFile.getAbsolutePath(),
		}, new File(izPackHomeDir, "utils/izpack2app"),
     "Unable to run izpack2app script",
     "IOException while trying to run iz2pack2app script.");
 
 	}
  
    
 }
