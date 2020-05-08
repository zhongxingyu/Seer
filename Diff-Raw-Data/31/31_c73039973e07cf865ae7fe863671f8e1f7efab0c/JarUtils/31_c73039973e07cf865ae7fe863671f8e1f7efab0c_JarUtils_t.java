 package org.codehaus.mojo.nbm.utils;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.jar.Attributes;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.IOUtil;
 
import com.google.common.io.Closer;

 public final class JarUtils
 {
     public static final String MANIFEST_JAR_ENTRY = "META-INF/MANIFEST.MF";
 
     private JarUtils() {}
 
     public static void unsignArchive( final File jarFile )
         throws IOException
     {
         archiveModifier( jarFile, null, null, true, null );
     }
 
     /**
      * Removes any existing signatures from the specified JAR file. We will stream from the input JAR directly to the
      * output JAR to retain as much metadata from the original JAR as possible.
      *
      * @param inJar The JAR file to unsign, must not be <code>null</code>.
      * @throws unsignArchive If the unsigning failed.
      */
     public static void archiveModifier(
             final File inJar, final File outJar,
             Integer compressionLevel,
             boolean unsign,
             Attributes attributes )
         throws IOException
     {
         File workJar =
                     outJar == null
                             ? new File( inJar.getAbsolutePath() + ".tmp" )
                             : outJar;
 
        Closer closer = Closer.create();
         try
         {
            JarInputStream jis =
                closer.register( new JarInputStream( new BufferedInputStream( new FileInputStream( inJar ) ) ) );

            BufferedOutputStream osBuff = new BufferedOutputStream( new FileOutputStream( workJar ) );

            JarOutputStream jos;

             Manifest manifest = null;
 
             // do not tamper with MANIFEST.MF if nothing has to be done to it
             if ( ( attributes != null ) && !attributes.isEmpty() )
             {
                 // Not using JarInputStream because it is flawed, might not find Manifest
                 // if MANIFEST.MF is not the first or second entry which is dumb.
                 // This will find it if it exist or create one otherwise.
                 // +p
                 manifest = getManifest( inJar );
 
                 manifest.getMainAttributes().putAll( attributes );
 
                jos = closer.register( new JarOutputStream( osBuff, manifest ) );
            }
            else
            {
                jos = closer.register( new JarOutputStream( osBuff ) );
            }
 
             if ( compressionLevel != null )
             {
                 jos.setLevel( compressionLevel );
             }
 
             for ( JarEntry je = jis.getNextJarEntry(); je != null; je = jis.getNextJarEntry() )
             {
                 // do not write the MANIFEST entry if we have already set one
                 if (  ( manifest != null ) && !je.getName().equals( MANIFEST_JAR_ENTRY ) )
                 {
                     if ( unsign && isSignatureFile( je.getName() ) )
                     {
                         continue;
                     }
 
                     jos.putNextEntry( new JarEntry( je.getName() ) );
 
                     IOUtil.copy( jis, jos );
                 }
             }
         }
         finally
         {
            closer.close();
         }
 
         if ( outJar == null )
         {
             FileUtils.rename( workJar, inJar );
         }
     }
 
     /**
      * Checks whether the specified JAR file entry denotes a signature-related file, i.e. matches
      * <code>META-INF/*.SF</code>, <code>META-INF/*.DSA</code> or <code>META-INF/*.RSA</code>.
      *
      * @param entryName The name of the JAR file entry to check, must not be <code>null</code>.
      * @return <code>true</code> if the entry is related to a signature, <code>false</code> otherwise.
      */
     private static boolean isSignatureFile( String entryName )
     {
         if ( entryName.regionMatches( true, 0, "META-INF", 0, 8 ) )
         {
             entryName = entryName.replace('\\', '/');
 
             if ( ( entryName.indexOf('/') == 8 ) && ( entryName.lastIndexOf('/') == 8 ) )
             {
                 if ( entryName.regionMatches( true, entryName.length() - 3, ".SF", 0, 3 ) )
                 {
                     return true;
                 }
                 if ( entryName.regionMatches( true, entryName.length() - 4, ".DSA", 0, 4 ) )
                 {
                     return true;
                 }
                 if ( entryName.regionMatches( true, entryName.length() - 4, ".RSA", 0, 4 ) )
                 {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     // +p
     public static Manifest getManifest( File file )
         throws IOException
     {
         JarInputStream jis = null;
         try
         {
             jis = new JarInputStream( new BufferedInputStream( new FileInputStream( file ) ) );
 
             Manifest manifest = jis.getManifest();
 
             if ( manifest != null )
             {
                 return manifest;
             }
 
             for ( JarEntry je = jis.getNextJarEntry(); je != null; je = jis.getNextJarEntry() )
             {
                 if ( je.getName().equals( MANIFEST_JAR_ENTRY ) )
                 {
                     return new Manifest( jis );
                 }
             }
         }
         finally
         {
             IOUtil.close( jis );
         }
 
         return new Manifest();
     }
 }
