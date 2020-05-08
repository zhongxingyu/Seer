 package org.apache.maven.surefire.osgi;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilterOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import org.apache.maven.surefire.booter.BooterDeserializer;
 import org.apache.maven.surefire.booter.ClasspathConfiguration;
 import org.apache.maven.surefire.booter.ForkedBooter;
 import org.apache.maven.surefire.booter.ProviderConfiguration;
 import org.apache.maven.surefire.booter.StartupConfiguration;
 import org.apache.maven.surefire.booter.SystemPropertyManager;
 import org.apache.maven.surefire.booter.TypeEncodedValue;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.Version;
 import org.osgi.framework.wiring.BundleRevision;
 import org.osgi.framework.wiring.BundleRevisions;
 import org.osgi.framework.wiring.BundleWire;
 import org.osgi.framework.wiring.BundleWiring;
 
 public class Booter
     extends ForkedBooter
 {
 
     private static class CurrentThreadOutputStream
         extends FilterOutputStream
     {
 
         private InheritableThreadLocal<Boolean> currentThread;
 
         private Queue<Object> buffers;
 
         public CurrentThreadOutputStream( OutputStream out )
         {
             super( out );
             this.currentThread = new InheritableThreadLocal<Boolean>();
             this.currentThread.set( Boolean.TRUE );
             this.buffers = new LinkedList<Object>();
         }
 
         public void flush()
             throws IOException
         {
             this.flushBuffers();
             this.flush();
         }
 
         public void flushBuffers()
             throws IOException
         {
             if ( !this.buffers.isEmpty() )
             {
                 for ( Object buffer : this.buffers )
                 {
                     if ( buffer instanceof Integer )
                     {
                         int b = (Integer) buffer;
                         this.out.write( b );
                     }
                     else if ( buffer instanceof byte[] )
                     {
                         byte[] bytes = (byte[]) buffer;
                         this.out.write( bytes );
                     }
                     else
                     {
                         Object[] array = (Object[]) buffer;
                         byte[] bytes = (byte[]) array[0];
                         int offset = (Integer) array[1];
                         int length = (Integer) array[2];
                         this.out.write( bytes, offset, length );
                     }
                 }
                 this.buffers.clear();
             }
         }
 
         @Override
         public synchronized void write( int b )
             throws IOException
         {
             if ( this.currentThread.get() == null )
             {
                 this.buffers.add( Integer.valueOf( b ) );
             }
             else
             {
                 this.flushBuffers();
                 this.out.write( b );
             }
         }
 
         @Override
         public synchronized void write( byte[] bytes )
             throws IOException
         {
             if ( this.currentThread.get() == null )
             {
                 this.buffers.add( bytes );
             }
             else
             {
                 this.flushBuffers();
                 this.out.write( bytes );
             }
         }
 
         @Override
         public synchronized void write( byte[] bytes, int offset, int length )
             throws IOException
         {
             if ( this.currentThread.get() == null )
             {
                 Object[] buffer = new Object[3];
                 buffer[0] = bytes;
                 buffer[1] = offset;
                 buffer[2] = length;
                 this.buffers.add( buffer );
             }
             else
             {
                 this.flushBuffers();
                 this.out.write( bytes, offset, length );
             }
         }
 
     }
 
     public Booter()
     {
         super();
     }
 
     public static synchronized void _main( String[] args )
         throws Throwable
     {
         PrintStream out = System.out;
         out.print( Booter.class.getPackage().getName() );
         out.print( ':' );
         out.print( Booter.class.getSimpleName() );
         for ( int i = 1; i < args.length; i++ )
         {
             System.out.print( ' ' );
             out.print( '\'' );
             out.print( args[i].replace( "'", "'\\''" ) );
             out.print( '\'' );
         }
         out.println();
         PrintStream err = System.err;
         try
         {
             if ( args.length > 2 )
             {
                 SystemPropertyManager.setSystemProperties( new File( args[2] ) );
             }
             File surefirePropertiesFile = new File( args[1] );
             InputStream stream = surefirePropertiesFile.exists() ? new FileInputStream( surefirePropertiesFile ) : null;
             BooterDeserializer booterDeserializer = new BooterDeserializer( stream );
             ProviderConfiguration providerConfiguration = booterDeserializer.deserialize();
             StartupConfiguration startupConfiguration = booterDeserializer.getProviderConfiguration();
             ClasspathConfiguration classpathConfiguration = startupConfiguration.getClasspathConfiguration();
             ClassLoader testClassLoader = classpathConfiguration.createTestClassLoader();
             Set<BundleWiring> bundleWirings = Collections.emptySet();
             {
                 String symbolicName;
                 Version version;
                 URL resource = testClassLoader.getResource( JarFile.MANIFEST_NAME );
                 InputStream inputStream = resource.openStream();
                 try
                 {
                     Manifest manifest = new Manifest( inputStream );
                     Attributes mainAttributes = manifest.getMainAttributes();
                     symbolicName = mainAttributes.getValue( Constants.BUNDLE_SYMBOLICNAME );
                     String bundleVersion = mainAttributes.getValue( Constants.BUNDLE_VERSION );
                     version = new Version( bundleVersion );
                 }
                 finally
                 {
                     inputStream.close();
                 }
                 Bundle bundle = FrameworkUtil.getBundle( Booter.class );
                 BundleContext bundleContext = bundle.getBundleContext();
                 Bundle[] candidateBundles = bundleContext.getBundles();
                 for ( Bundle candidateBundle : candidateBundles )
                 {
                     String bundleSymbolicName = candidateBundle.getSymbolicName();
                     Version bundleVersion = candidateBundle.getVersion();
                     if ( symbolicName.equals( bundleSymbolicName ) && version.equals( bundleVersion ) )
                     {
                         Dictionary<String, String> headers = candidateBundle.getHeaders();
                         String fragmentHost = headers.get( Constants.FRAGMENT_HOST );
                         if ( fragmentHost == null )
                         {
                             BundleWiring bundleWiring = candidateBundle.adapt( BundleWiring.class );
                             bundleWirings = Collections.singleton( bundleWiring );
                         }
                         else
                         {
                             BundleRevisions bundleRevisions = candidateBundle.adapt( BundleRevisions.class );
                             if ( bundleRevisions != null )
                             {
                                 List<BundleRevision> candidateBundleRevisions = bundleRevisions.getRevisions();
                                 bundleWirings = new HashSet<BundleWiring>( candidateBundleRevisions.size() );
                                 for ( BundleRevision candidateBundleRevision : candidateBundleRevisions )
                                 {
                                     BundleWiring candidateBundleWiring = candidateBundleRevision.getWiring();
                                     if ( candidateBundleWiring != null )
                                     {
                                         List<BundleWire> candidateBundleWires =
                                             candidateBundleWiring.getRequiredWires( null );
                                         if ( candidateBundleWires != null )
                                         {
                                             for ( BundleWire candidateBundleWire : candidateBundleWires )
                                             {
                                                 BundleWiring bundleWiring = candidateBundleWire.getProviderWiring();
                                                 bundleWirings.add( bundleWiring );
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                         break;
                     }
                 }
             }
             CurrentThreadOutputStream currentOutputStream = new CurrentThreadOutputStream( out );
             PrintStream outputStream = new PrintStream( currentOutputStream );
             PrintStream errorStream =
                 out.equals( err ) ? outputStream : new PrintStream( new CurrentThreadOutputStream( err ) );
             System.setOut( outputStream );
             System.setErr( errorStream );
             try
             {
                 for ( BundleWiring bundleWiring : bundleWirings )
                 {
                     ClassLoader classLoader = bundleWiring.getClassLoader();
                     TypeEncodedValue forkedTestSet = providerConfiguration.getTestForFork();
                     Object testSet = forkedTestSet != null ? forkedTestSet.getDecodedValue( classLoader ) : null;
                     runSuitesInProcess( testSet, classLoader, startupConfiguration, providerConfiguration );
                 }
             }
             finally
             {
                 currentOutputStream.flushBuffers();
                 System.setOut( out );
                 System.setErr( err );
             }
             out.println( "Z,0,BYE!" );
             out.flush();
         }
         catch ( Throwable t )
         {
             t.printStackTrace( err );
             err.flush();
            throw t;
         }
     }
 
 }
