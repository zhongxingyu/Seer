 package com.atlassian.plugin.osgi.factory.transform;
 
 import static com.google.common.collect.Iterators.forEnumeration;
 
 import com.atlassian.plugin.util.collect.Function;
 
 import com.google.common.collect.ImmutableList;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import java.util.zip.ZipEntry;
 
 /**
  * Utility methods for getting stuff out of a jar.
  */
 final class JarUtils
 {
     /**
      * Get the {@link Manifest} from a Jar file, or create a new one if there isn't one already.
      * 
      * @param file the file that is the jar contents
      * @return the manifest or an empty new one if one can't be found.
      */
     static Manifest getManifest(final File file)
     {
         final Manifest result = withJar(file, ManifestExtractor.INSTANCE);
         return (result == null) ? new Manifest() : result;
     }
 
     /**
     * Get the {@link JarEntry entries} from a Jar file, or create a new one if there isn't one already.
      * 
      * @param file the file that is the jar contents
      * @return the entries the jar contains.
      */
     static Iterable<JarEntry> getEntries(final File file)
     {
         return withJar(file, JarEntryExtractor.INSTANCE);
     }
 
     /**
     * Get the {@link JarEntry entries} from a Jar file, or create a new one if there isn't one already.
      * 
      * @param file the file that is the jar contents
     * @return the entries the jar contains.
      */
     static ZipEntry getEntry(final File file, final String path)
     {
         return withJar(file, new Extractor<ZipEntry>()
         {
             public ZipEntry get(final JarFile jarFile)
             {
                 return jarFile.getEntry(path);
             }
         });
     }
 
     /**
      * Extract something from a jar file.
      * <p>
      * Correctly opens and closes the Jar file. Must not lazily access the Jar as it has an open/closed state.
      * 
      * @param <T> the type of the thing to extract
      * @param file the file that is the jar contents
      * @param extractor 
      * @return the result of the extractor
      * @throws RuntimeException if there are problems accessing the jar contents.
      */
     static <T> T withJar(final File file, final Extractor<T> extractor)
     {
         JarFile jarFile = null;
         try
         {
             jarFile = new JarFile(file);
             return extractor.get(jarFile);
         }
         catch (final IOException e)
         {
             throw new IllegalArgumentException("File must be a jar", e);
         }
         finally
         {
             if (jarFile != null)
             {
                 try
                 {
                     jarFile.close();
                 }
                 catch (final IOException ignore)
                 {}
             }
         }
     }
 
     interface Extractor<T> extends Function<JarFile, T>
     {}
 
     enum ManifestExtractor implements Extractor<Manifest>
     {
         INSTANCE;
 
         public Manifest get(final JarFile input)
         {
             try
             {
                 return input.getManifest();
             }
             catch (final IOException e)
             {
                 throw new RuntimeException(e);
             }
         }
     }
 
     enum JarEntryExtractor implements Extractor<Iterable<JarEntry>>
     {
         INSTANCE;
 
         public Iterable<JarEntry> get(final JarFile jarFile)
         {
             return ImmutableList.copyOf(forEnumeration(jarFile.entries()));
         }
     }
 }
