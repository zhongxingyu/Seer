 /**
  * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.osgify.core.packaging;
 
 import static org.sourcepit.common.utils.io.IOResources.buffIn;
 import static org.sourcepit.common.utils.io.IOResources.buffOut;
 import static org.sourcepit.common.utils.io.IOResources.fileIn;
 import static org.sourcepit.common.utils.io.IOResources.fileOut;
 import static org.sourcepit.common.utils.io.IOResources.jarIn;
 import static org.sourcepit.common.utils.io.IOResources.jarOut;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.JarInputStream;
 import java.util.jar.JarOutputStream;
 
 import javax.inject.Named;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sourcepit.common.manifest.Manifest;
 import org.sourcepit.common.manifest.osgi.resource.GenericManifestResourceImpl;
 import org.sourcepit.common.utils.io.IOOperation;
 import org.sourcepit.common.utils.lang.Exceptions;
 import org.sourcepit.common.utils.lang.PipedIOException;
 import org.sourcepit.common.utils.path.PathMatcher;
 
 /**
  * @author Bernd Vogt <bernd.vogt@sourcepit.org>
  */
 @Named
 public class Repackager
 {
    private final static PathMatcher JAR_CONTENT_MATCHER = createJarContentMatcher();
 
    private final Logger logger = LoggerFactory.getLogger(Repackager.class);
 
    public void injectManifest(final File jarFile, final Manifest manifest) throws PipedIOException
    {
       try
       {
          final File tmpFile = move(jarFile);
          copyJarAndInjectManifest(tmpFile, jarFile, manifest);
          org.apache.commons.io.FileUtils.forceDelete(tmpFile);
       }
       catch (IOException e)
       {
          throw Exceptions.pipe(e);
       }
    }
 
    private File move(final File srcFile) throws IOException
    {
       String prefix = ".rejar";
       String dirName = srcFile.getParentFile().getAbsolutePath();
       String fileName = srcFile.getName();
 
       File destFile = createTmpFile(dirName, fileName, prefix);
       final boolean rename = srcFile.renameTo(destFile);
       if (!rename)
       {
          org.apache.commons.io.FileUtils.copyFile(srcFile, destFile);
          if (!srcFile.delete())
          {
             FileUtils.deleteQuietly(destFile);
             throw new IOException("Failed to delete original file '" + srcFile + "' after copy to '" + destFile + "'");
          }
       }
       return destFile;
    }
 
    private static File createTmpFile(String dirName, String fileName, String prefix) throws IOException
    {
       int unique = 0;
       File file;
       do
       {
          file = genFile(dirName, fileName, prefix, unique++);
       }
       while (!file.createNewFile());
 
       return file;
    }
 
    private static File genFile(String dirName, String fileName, String prefix, int counter)
    {
       StringBuilder path = new StringBuilder();
       path.append(dirName);
       path.append(File.separatorChar);
       path.append(fileName);
       if (counter > 0)
       {
          final String n = String.valueOf(counter);
          int lead = 5 - n.length();
          for (int i = 0; i < lead; i++)
          {
             path.append('0');
          }
          path.append(n);
       }
       path.append(prefix);
       return new File(path.toString());
    }
 
    public void copyJarAndInjectManifest(final File srcJarFile, final File destJarFile, final Manifest manifest)
       throws PipedIOException
    {
       new IOOperation<JarOutputStream>(jarOut(buffOut(fileOut(destJarFile))))
       {
          @Override
          protected void run(JarOutputStream destJarOut) throws IOException
          {
             rePackageJarFile(srcJarFile, manifest, destJarOut);
          }
       }.run();
    }
 
    private void rePackageJarFile(File srcJarFile, final Manifest manifest, final JarOutputStream destJarOut)
       throws IOException
    {
       destJarOut.putNextEntry(new JarEntry(JarFile.MANIFEST_NAME));
       writeManifest(manifest, destJarOut);
       destJarOut.closeEntry();
 
       new IOOperation<JarInputStream>(jarIn(buffIn(fileIn(srcJarFile))))
       {
          @Override
          protected void run(JarInputStream srcJarIn) throws IOException
          {
             copyJarContents(srcJarIn, destJarOut);
          }
       }.run();
    }
 
    private void writeManifest(Manifest manifest, OutputStream out) throws IOException
    {
       Resource manifestResource = new GenericManifestResourceImpl();
       manifestResource.getContents().add(EcoreUtil.copy(manifest));
       manifestResource.save(out, null);
    }
 
    private void copyJarContents(JarInputStream srcJarIn, final JarOutputStream destJarOut) throws IOException
    {
       final Set<String> processedEntires = new HashSet<String>();
 
       JarEntry srcEntry = srcJarIn.getNextJarEntry();
       while (srcEntry != null)
       {
          final String entryName = srcEntry.getName();
          if (JAR_CONTENT_MATCHER.isMatch(entryName))
          {
             if (processedEntires.add(entryName))
             {
               destJarOut.putNextEntry(srcEntry);
                IOUtils.copy(srcJarIn, destJarOut);
                destJarOut.closeEntry();
             }
             else
             {
                logger.warn("Ignored duplicate jar entry: " + entryName);
             }
          }
          srcJarIn.closeEntry();
          srcEntry = srcJarIn.getNextJarEntry();
       }
    }
 
    private static PathMatcher createJarContentMatcher()
    {
       final Set<String> excludes = new HashSet<String>();
       excludes.add(JarFile.MANIFEST_NAME); // will be set manually
       excludes.add("META-INF/*.SF");
       excludes.add("META-INF/*.DSA");
       excludes.add("META-INF/*.RSA");
 
       final String matcherPattern = toPathMatcherPattern(excludes);
 
       return PathMatcher.parse(matcherPattern, "/", ",");
    }
 
    private static String toPathMatcherPattern(Set<String> excludes)
    {
       final StringBuilder sb = new StringBuilder();
       for (String exclude : excludes)
       {
          sb.append('!');
          sb.append(exclude);
          sb.append(',');
       }
       if (sb.length() > 0)
       {
          sb.deleteCharAt(sb.length() - 1);
       }
       return sb.toString();
    }
 }
