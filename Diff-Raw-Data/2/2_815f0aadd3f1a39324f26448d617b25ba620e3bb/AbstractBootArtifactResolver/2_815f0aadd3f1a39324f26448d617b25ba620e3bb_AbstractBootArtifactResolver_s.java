 package org.mosaic.runner.boot.artifact.resolve;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import org.mosaic.runner.boot.artifact.BootArtifact;
 import org.mosaic.runner.boot.artifact.CannotInstallBootArtifactException;
 import org.mosaic.runner.util.BundleUtils;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Version;
 import org.osgi.framework.startlevel.BundleStartLevel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static org.apache.commons.lang.StringUtils.isBlank;
 
 /**
  * @author arik
  */
 public abstract class AbstractBootArtifactResolver implements BootArtifactResolver
 {
     private final Logger LOG = LoggerFactory.getLogger( getClass( ) );
 
     private final BundleContext bundleContext;
 
     protected AbstractBootArtifactResolver( BundleContext bundleContext )
     {
         this.bundleContext = bundleContext;
     }
 
     protected BundleContext getBundleContext( )
     {
         return this.bundleContext;
     }
 
     protected final Set<Bundle> installOrUpdateBundle( BootArtifact artifact, File bundleFile )
     throws CannotInstallBootArtifactException
     {
         String fileSymbolicName;
         Version fileVersion;
         boolean snapshot;
         try
         {
             JarFile jarFile = new JarFile( bundleFile, true, JarFile.OPEN_READ );
             Manifest manifest = jarFile.getManifest( );
             if( manifest == null )
             {
                 return null;
             }
 
             Attributes mainAttributes = manifest.getMainAttributes( );
             if( mainAttributes == null )
             {
                 return null;
             }
 
             fileSymbolicName = mainAttributes.getValue( "Bundle-SymbolicName" );
             if( isBlank( fileSymbolicName ) )
             {
                 return null;
             }
 
             try
             {
                 fileVersion = new Version( mainAttributes.getValue( "Bundle-Version" ) );
             }
             catch( IllegalArgumentException e )
             {
                 throw new CannotInstallBootArtifactException( artifact, "file '" +
                                                                         bundleFile +
                                                                         "' has an illegal 'Bundle-Version' header" );
             }
             snapshot = fileVersion.getQualifier( ).equalsIgnoreCase( "SNAPSHOT" );
 
         }
         catch( IOException e )
         {
             throw new CannotInstallBootArtifactException( artifact, e );
         }
 
         try
         {
             Collection<Bundle> bundles = new LinkedList<>( );
             for( Bundle bundle : BundleUtils.findBundlesBySymbolicName( getBundleContext( ), fileSymbolicName ) )
             {
                 int comparison = fileVersion.compareTo( bundle.getVersion( ) );
                 if( comparison < 0 )
                 {
                     LOG.debug( "Newer version of '{}' is already installed, skipping file '{}'", fileSymbolicName, bundleFile );
                     return null;
                 }
                else if( comparison == 0 && !snapshot )
                 {
                     if( snapshot )
                     {
                         // we will update this bundle since snapshots might differ
                         bundles.add( bundle );
                     }
                     else
                     {
                         LOG.debug( "Boot bundle '{}' is already installed", BundleUtils.toString( bundle ) );
                         return null;
                     }
                 }
                 else if( comparison > 0 )
                 {
                     // collect bundle
                     bundles.add( bundle );
                 }
             }
 
             if( bundles.size( ) == 1 )
             {
                 // since matching bundle - lets update it
                 Bundle bundle = bundles.iterator( ).next( );
                 bundle.update( new FileInputStream( bundleFile ) );
                 BundleStartLevel startLevel = bundle.adapt( BundleStartLevel.class );
                 startLevel.setStartLevel( 2 );
                 return new HashSet<>( Arrays.asList( bundle ) );
             }
             else if( !bundles.isEmpty( ) )
             {
                 // multiple matching bundles - uninstall all of them, and then install our own
                 for( Bundle bundle : bundles )
                 {
                     bundle.uninstall( );
                 }
             }
 
             // if we got here it means that either more than one bundle matched this symbolic name, or none matched it
             // if multiple, they were uninstalled above; otherwise its new. In both cases - we can install it fresh
             Bundle bundle = getBundleContext( ).installBundle( bundleFile.toURI( ).toString( ) );
             bundle.start( );
             BundleStartLevel startLevel = bundle.adapt( BundleStartLevel.class );
             startLevel.setStartLevel( 2 );
             return new HashSet<>( Arrays.asList( bundle ) );
         }
         catch( BundleException | FileNotFoundException e )
         {
             throw new CannotInstallBootArtifactException( artifact, e );
         }
     }
 }
