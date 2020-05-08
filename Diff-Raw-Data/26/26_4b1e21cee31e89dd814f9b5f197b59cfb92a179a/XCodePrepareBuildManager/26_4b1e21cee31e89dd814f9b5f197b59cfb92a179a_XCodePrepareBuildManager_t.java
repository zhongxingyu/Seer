 /*
  * #%L
  * xcode-maven-plugin
  * %%
  * Copyright (C) 2012 SAP AG
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 package com.sap.prd.mobile.ios.mios;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.UnArchiver;
 import org.codehaus.plexus.archiver.manager.ArchiverManager;
 import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.RepositorySystemSession;
 import org.sonatype.aether.repository.RemoteRepository;
 
 class XCodePrepareBuildManager
 {
 
   private final static String TYPE_HEADERS = "headers.tar", TYPE_ARCHIVE = "a";
 
   private final Log log;
   private final ArchiverManager archiverManager;
   private final XCodeDownloadManager downloadManager;
 
   XCodePrepareBuildManager(final Log log, final ArchiverManager archiverManager,
         final RepositorySystemSession repoSystemSession, final RepositorySystem repoSystem,
         final List<RemoteRepository> projectRepos)
   {
     this.log = log;
     this.archiverManager = archiverManager;
     this.downloadManager = new XCodeDownloadManager(projectRepos, repoSystem, repoSystemSession);
   }
 
   void prepareBuild(final MavenProject project, Set<String> configurations,
         final Set<String> sdks) throws MojoExecutionException, XCodeException, IOException
   {
     for (@SuppressWarnings("rawtypes")
     final Iterator it = project.getCompileArtifacts().iterator(); it.hasNext();) {
 
       final Artifact mainArtifact = (Artifact) it.next();
 
       if (PackagingType.LIB.getMavenPackaging().equals(mainArtifact.getType())) {
 
         for (final String xcodeConfiguration : configurations) {
 
           for (final String sdk : sdks) {
 
             try {
               prepareHeaders(project, xcodeConfiguration, sdk, mainArtifact);
             }
             catch (SideArtifactNotFoundException e) {
               log.info("Headers not found for: '" + mainArtifact.getGroupId() + ":" + mainArtifact.getArtifactId()
                     + ":"
                     + mainArtifact.getVersion() + ":" + mainArtifact.getType() + "'.");
             }
 
             try {
               prepareLibrary(project, xcodeConfiguration, sdk, mainArtifact);
             }
             catch (SideArtifactNotFoundException e) {
               throw new XCodeException("Library not found for: " + mainArtifact.getGroupId() + ":"
                     + mainArtifact.getArtifactId() + ":" + mainArtifact.getVersion() + ":"
                     + mainArtifact.getClassifier()
                     + ":" + mainArtifact.getType(), e);
             }
           }
         }
 
         try {
           prepareBundles(project, mainArtifact);
         }
         catch (SideArtifactNotFoundException e) {
           log.info("Bundle not found for: '" + mainArtifact.getGroupId() + ":" + mainArtifact.getArtifactId() + ":"
                 + mainArtifact.getVersion() + ":" + mainArtifact.getType() + "'.");
         }
       }
       else if ("xcode-framework".equals(mainArtifact.getType())) {
         prepareFramework(project, mainArtifact);
       }
       else
         continue;
     }
   }
 
   private void prepareBundles(MavenProject project, final Artifact primaryArtifact) throws MojoExecutionException,
         SideArtifactNotFoundException, IOException
   {
     List<String> bundles = readBundleInformation(project, primaryArtifact);
     if (bundles == null)
       return;
 
     for (String coords : bundles) {
       prepareBundle(project, primaryArtifact, coords);
     }
   }
 
   private void prepareBundle(MavenProject project, final Artifact primaryArtifact, String coords)
         throws SideArtifactNotFoundException, MojoExecutionException
   {
     Artifact bundleArtifact = GAVUtil.getArtifact(coords);
 
     final org.sonatype.aether.artifact.Artifact artifact = downloadManager.resolveSideArtifact(bundleArtifact);
 
     if (artifact != null) {
       final File source = artifact.getFile();
 
       final File target = new File(FolderLayout.getFolderForExtractedBundlesWithGA(project,
             primaryArtifact.getGroupId(), primaryArtifact.getArtifactId()), bundleArtifact.getClassifier().replaceAll(
             "~", File.separator)
             + ".bundle");
 
       createDirectory(target);
       unarchive("zip", source, target);
 
       log.info("Bundle unarchived from " + source + " to " + target);
 
     }
   }
 
   @SuppressWarnings("unchecked")
   private List<String> readBundleInformation(MavenProject project, Artifact primaryArtifact) throws IOException
   {
 
     final File mainArtifactExtracted = FolderLayout.getFolderForExtractedPrimaryArtifact(project,
           primaryArtifact);
 
     if (!mainArtifactExtracted.mkdirs())
       throw new IOException("Cannot create directory for expanded mainartefact of " + primaryArtifact.getGroupId()
             + ":" + primaryArtifact.getArtifactId() + " (" + mainArtifactExtracted + ").");
 
     unarchive("tar", primaryArtifact.getFile(), mainArtifactExtracted);
 
     File bundleFile = new File(mainArtifactExtracted, "bundles.txt");
     if (!bundleFile.exists())
       return null;
 
     try {
       return FileUtils.readLines(bundleFile);
     }
     catch (IOException e) {
       throw new RuntimeException(e);
     }
 
   }
 
   private void prepareLibrary(MavenProject project, final String xcodeConfiguration,
         final String sdk, final Artifact primaryArtifact) throws MojoExecutionException, SideArtifactNotFoundException
   {
 
     final File source = downloadManager.resolveSideArtifact(primaryArtifact, xcodeConfiguration + "-" + sdk,
           TYPE_ARCHIVE).getFile();
 
     final File target = new File(FolderLayout.getFolderForExtractedLibsWithGA(project, xcodeConfiguration,
           sdk,
           primaryArtifact.getGroupId(), primaryArtifact.getArtifactId()), getArchiveFileName(primaryArtifact));
 
     try {
      com.sap.prd.mobile.ios.mios.FileUtils.createSymbolicLink(source, target);
     }
     catch (IOException e) {
       throw new RuntimeException(e);
     }    
   }
 
   private void prepareHeaders(MavenProject project, String xcodeConfiguration,
         final String sdk, final Artifact primaryArtifact) throws MojoExecutionException, SideArtifactNotFoundException
   {
 
     final org.sonatype.aether.artifact.Artifact headersArtifact = downloadManager.resolveSideArtifact(primaryArtifact,
           xcodeConfiguration + "-" + sdk, TYPE_HEADERS);
 
     if (headersArtifact != null) {
 
       final File headersDirectory = FolderLayout.getFolderForExtractedHeadersWithGA(project,
             xcodeConfiguration, sdk,
             primaryArtifact.getGroupId(), primaryArtifact.getArtifactId());
 
       createDirectory(headersDirectory);
 
       extractHeaders(headersDirectory, headersArtifact.getFile());
     }
   }
 
   private void prepareFramework(MavenProject project, final Artifact primaryArtifact)
         throws MojoExecutionException
   {
     if (primaryArtifact != null) {
       final File source = primaryArtifact.getFile();
 
       final File target = FolderLayout.getFolderForExtractedFrameworkswithGA(project,
             primaryArtifact.getGroupId(), primaryArtifact.getArtifactId());
 
       createDirectory(target);
       unarchive("zip", source, target);
 
       try {
         extractFileWithShellScript(source, target, new File(project.getBuild().getDirectory()));
       }
       catch (IOException ioe) {
         throw new MojoExecutionException("Cannot unarchive framework from " + source + " to " + target);
       }
 
       log.info("Framework unarchived from " + source + " to " + target);
 
     }
   }
 
   private static String getArchiveFileName(final Artifact primaryArtifact)
   {
     return "lib" + primaryArtifact.getArtifactId() + ".a";
   }
 
   private void extractHeaders(final File headersDirectory, final File headers)
   {
     unarchive("tar", headers, headersDirectory);
   }
 
   /**
    * Creates a directory. If the directory already exists the directory is deleted beforehand.
    * 
    * @param directory
    * @throws MojoExecutionException
    */
   private static void createDirectory(final File directory) throws MojoExecutionException
   {
 
     try {
       FileUtils.deleteDirectory(directory);
     }
     catch (IOException ex) {
       throw new MojoExecutionException("", ex);
     }
 
     if (!directory.mkdirs())
       throw new MojoExecutionException("Cannot create directory (" + directory + ")");
   }
 
   private void unarchive(final String archiverType, final File source, final File destinationDirectory)
   {
     try {
       UnArchiver unarchiver = archiverManager.getUnArchiver(archiverType);
       unarchiver.setSourceFile(source);
       unarchiver.setDestDirectory(destinationDirectory);
       unarchiver.extract();
     }
     catch (NoSuchArchiverException e) {
       throw new RuntimeException(e);
     }
     catch (ArchiverException e) {
       throw new RuntimeException(e);
     }
   }
 
   private void extractFileWithShellScript(File sourceFile, File destinationFolder, File tmpFolder) throws IOException
   {
     File workingDirectory = new File(tmpFolder, "scriptWorkingDir");
     workingDirectory.deleteOnExit();
     ScriptRunner.copyAndExecuteScript(System.out, "/com/sap/prd/mobile/ios/mios/unzip.sh", workingDirectory, sourceFile.getCanonicalPath(),
           destinationFolder.getCanonicalPath());
   }
 }
