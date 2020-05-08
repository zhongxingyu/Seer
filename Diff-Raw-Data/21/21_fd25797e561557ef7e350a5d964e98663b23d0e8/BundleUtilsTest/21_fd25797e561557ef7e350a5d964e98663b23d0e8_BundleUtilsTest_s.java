 package org.mosaic.util.osgi;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.HashMap;
 import javax.annotation.Nonnull;
 import org.apache.felix.framework.Felix;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 
 import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mosaic.util.osgi.BundleUtils.bundleContext;
 
 /**
  * @author arik
  */
 public class BundleUtilsTest
 {
     private BundleContext felixBundleContext;
 
     @Before
     public void setUp() throws Exception
     {
         Felix felix = new Felix( new HashMap() );
         felix.start();
 
         this.felixBundleContext = felix.getBundleContext();
         if( this.felixBundleContext == null )
         {
             throw new IllegalStateException( "could not find Felix bundle context" );
         }
     }
 
     @After
     public void tearDown() throws Exception
     {
         this.felixBundleContext.getBundle().stop();
     }
 
     @Test
     public void testRequireBundleContext() throws BundleException, IOException, ClassNotFoundException
     {
         Bundle guavaBundle = installGuava();
         guavaBundle.start();
 
         Class<?> mapsClass = guavaBundle.loadClass( "com.google.common.collect.Maps" );
        assertThat( bundleContext( mapsClass ).isPresent(), is( true ) );
        assertThat( bundleContext( mapsClass ).get(), equalTo( guavaBundle.getBundleContext() ) );
        assertThat( bundleContext( getClass() ).isPresent(), is( false ) );
 
         guavaBundle.stop();
        assertThat( bundleContext( mapsClass ).isPresent(), is( false ) );
     }
 
     @Nonnull
     private Bundle installGuava() throws IOException, BundleException
     {
         Path guavaJar = findGuavaJarInClasspath();
         return this.felixBundleContext.installBundle( guavaJar.toString(), Files.newInputStream( guavaJar ) );
     }
 
     @Nonnull
     private Path findGuavaJarInClasspath()
     {
         for( String cpToken : getRuntimeMXBean().getClassPath().split( File.pathSeparator ) )
         {
             if( cpToken.contains( "guava" ) )
             {
                 return Paths.get( cpToken );
             }
         }
         throw new IllegalStateException( "could not find guava JAR in classpath (used for testing in OSGi environment)" );
     }
 }
