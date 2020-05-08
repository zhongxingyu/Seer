 package com.adito.maven.plugins.extension;
 
import com.adito.extension.Bundle;
 import com.adito.extension.PluginType;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import javax.xml.bind.JAXB;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.jar.ManifestException;
 import org.codehaus.plexus.archiver.zip.ZipArchiver;
 
 /**
  *
  * @author sebastian
  */
 class ExtensionArchiverBuilder {
 
     private static boolean isFile(final File file) {
         return file != null && file.exists() && file.isFile();
     }
 
     private static boolean isDirectory(final File file) {
         return file != null && file.exists() && file.isDirectory();
     }
     private final String extensionName;
     private final ZipArchiver archiver;
     private final Set<String> classpathEntries;
    private Bundle extension;
 
     public ExtensionArchiverBuilder(final String extensionName,
             final ZipArchiver archiver, final File destFile) {
         this.archiver = archiver;
         archiver.setDestFile(destFile);
         archiver.setCompress(true);
         archiver.setDuplicateBehavior(ZipArchiver.DUPLICATES_ADD);
         this.extensionName = extensionName;
         classpathEntries = new HashSet<String>();
     }
 
     public ExtensionArchiverBuilder addExtensionDirectory(final File dir)
             throws ArchiverException {
         return addExtensionDirectory(dir, null);
     }
 
     public ExtensionArchiverBuilder addExtensionDirectory(final File dir,
             final String targetDirectory) throws ArchiverException {
         if (isDirectory(dir)) {
             archiver.addDirectory(dir, asExtensionDirectoryName(targetDirectory));
         }
         return this;
     }
 
     public ExtensionArchiverBuilder addExtensionClasspathFile(final Collection<Artifact> artifacts,
             final String targetDirectory) throws ArchiverException {
         for (Artifact artifact : artifacts) {
             addExtensionClasspathFile(artifact.getFile(), targetDirectory);
         }
         return this;
     }
 
     public ExtensionArchiverBuilder addExtensionClasspathFile(final File file,
             final String targetDirectory) throws ArchiverException {
         final ExtensionArchiverBuilder result = addExtensionFile(file, targetDirectory);
         addToClassPath(file, targetDirectory);
         return result;
     }
 
     public ExtensionArchiverBuilder addExtensionFile(final File file) throws ArchiverException {
         return addExtensionFile(file, null);
     }
 
     public ExtensionArchiverBuilder addExtensionFile(final File file,
             final String targetDirectory) throws ArchiverException {
         if (isFile(file)) {
             archiver.addFile(file, asInExtensionFileName(file, targetDirectory));
         }
         return this;
     }
 
     private void addToClassPath(final File file, final String targetDirectory) {
         if (isFile(file)) {
             final StringBuilder sb = new StringBuilder();
             if (targetDirectory != null) {
                 sb.append(targetDirectory).append('/');
             }
             sb.append(file.getName());
             classpathEntries.add(sb.toString());
         }
     }
 
     private String asInExtensionFileName(final File file, final String inpaths) {
         return String.format("%s%s", asExtensionDirectoryName(inpaths), file.getName());
     }
 
     private String asExtensionDirectoryName(final String subpath) {
         final StringBuilder sb = new StringBuilder(extensionName);
         if (subpath != null) {
             sb.append('/').append(subpath);
         }
         return sb.append('/').toString();
     }
 
     public File createArchive(final MavenProject project) throws ArchiverException,
             ManifestException, IOException {
         File file = null;
         try {
             if (extension != null) {
                 file = writeClassPath();
                 addExtensionFile(file);
             }
             archiver.createArchive();
             return archiver.getDestFile();
         } finally {
             if (file != null) {
                 file.delete();
             }
         }
     }
 
     private File writeClassPath() throws IOException {
         final PluginType plugin = extension.getExtension().getPlugin();
         classpathEntries.removeAll(plugin.getClasspath());
         plugin.getClasspath().addAll(classpathEntries);
         final File file = new File(archiver.getDestFile().getParentFile(), "extension.xml");
         JAXB.marshal(extension, file);
         return file;
     }
 
    public ExtensionArchiverBuilder addExtensionBundle(final Bundle extension) {
         this.extension = extension;
         return this;
     }
 }
