 package org.sonatype.ziptest;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 
 import de.schlichtherle.truezip.file.TFile;
 
 public class App
 {
     public static final long KB = 1024;
 
     public static final long MB = 1024 * 1024;
 
     public static void main( String[] args )
         throws IOException
     {
         if ( args != null && args.length == 2 )
         {
             final File file = new File( args[1] );
 
             final String sw = args[0];
 
             if ( "all".equalsIgnoreCase( sw ) )
             {
                 test( file, "zf" );
                 test( file, "zis" );
                 test( file, "tzf" );
                 test( file, "tzt" );
             }
             else
             {
                 test( file, sw );
             }
         }
         else
         {
             System.out.println( "java -jar thisjar.jar [1|2] <aZipFile>" );
         }
 
     }
 
     public static void test( final File file, final String sw )
         throws IOException
     {
         final long started = System.currentTimeMillis();
 
         if ( "zf".equalsIgnoreCase( sw ) )
         {
             testZipFile( file );
         }
         else if ( "zis".equalsIgnoreCase( sw ) )
         {
             testZipInputStream( file );
         }
         else if ( "tzf".equalsIgnoreCase( sw ) )
         {
             testTrueZipZipFile( file );
         }
         else if ( "tzt".equalsIgnoreCase( sw ) )
         {
            testTrueZipZipFile( file );
         }
         else
         {
             System.out.println( "Boo!" );
         }
 
         System.out.println( String.format( "Done in %s millis.", System.currentTimeMillis() - started ) );
 
         Runtime r = Runtime.getRuntime();
 
         System.out.println( "Final Memory: " + ( r.totalMemory() - r.freeMemory() ) / KB + "kB/" + r.totalMemory() / KB
             + "kB" );
 
         System.gc();
 
         System.out.println( "Final Memory: " + ( r.totalMemory() - r.freeMemory() ) / KB + "kB/" + r.totalMemory() / KB
             + "kB (after GC)" );
 
     }
 
     public static void testZipFile( final File file )
         throws IOException
     {
         System.out.println( "=======" );
         System.out.println( "ZipFile" );
         System.out.println( "=======" );
 
         ZipFile zf = new ZipFile( file );
 
         Enumeration<? extends ZipEntry> entries = zf.entries();
 
         int count = 0;
 
         while ( entries.hasMoreElements() )
         {
             ZipEntry entry = entries.nextElement();
 
             // System.out.println( entry.getName() );
 
             count++;
         }
 
         // System.out.println( "=====" );
 
         System.out.println( String.format( "Total of %s entries.", count ) );
     }
 
     public static void testZipInputStream( final File file )
         throws IOException
     {
         System.out.println( "==============" );
         System.out.println( "ZipInputStream" );
         System.out.println( "==============" );
 
         ZipInputStream zi = new ZipInputStream( new BufferedInputStream( new FileInputStream( file ) ) );
 
         ZipEntry entry = null;
 
         int count = 0;
 
         while ( ( entry = zi.getNextEntry() ) != null )
         {
             // System.out.println( entry.getName() );
 
             count++;
         }
 
         // System.out.println( "=====" );
 
         System.out.println( String.format( "Total of %s entries.", count ) );
     }
 
     public static void testTrueZipZipFile( final File file )
         throws IOException
     {
         System.out.println( "===============" );
         System.out.println( "TrueZip ZipFile" );
         System.out.println( "===============" );
 
         de.schlichtherle.truezip.zip.ZipFile zf = new de.schlichtherle.truezip.zip.ZipFile( file );
 
         Enumeration<? extends de.schlichtherle.truezip.zip.ZipEntry> entries = zf.entries();
 
         int count = 0;
 
         while ( entries.hasMoreElements() )
         {
             de.schlichtherle.truezip.zip.ZipEntry entry = entries.nextElement();
 
             // System.out.println( entry.getName() );
 
             count++;
         }
 
         // System.out.println( "=====" );
 
         System.out.println( String.format( "Total of %s entries.", count ) );
     }
 
     public static void testTrueZipTFile( final File file )
         throws IOException
     {
         System.out.println( "=============" );
         System.out.println( "TrueZip TFile" );
         System.out.println( "=============" );
 
         TFile zf = new TFile( file );
 
         TFile[] entries = zf.listFiles();
 
         int count = 0;
 
         for ( TFile entry : entries )
         {
             // System.out.println( entry.getName() );
 
             count++;
         }
 
         // System.out.println( "=====" );
 
         System.out.println( String.format( "Total of %s entries.", count ) );
     }
 }
