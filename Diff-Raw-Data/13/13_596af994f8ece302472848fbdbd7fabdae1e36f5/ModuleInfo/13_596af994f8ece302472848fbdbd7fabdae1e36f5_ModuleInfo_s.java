 //$HeadURL$
 /*----------------------------------------------------------------------------
  This file is part of deegree, http://deegree.org/
  Copyright (C) 2001-2009 by:
  Department of Geography, University of Bonn
  and
  lat/lon GmbH
 
  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option)
  any later version.
  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  details.
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation, Inc.,
  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
  Contact information:
 
  lat/lon GmbH
  Aennchenstr. 19, 53177 Bonn
  Germany
  http://lat-lon.de/
 
  Department of Geography, University of Bonn
  Prof. Dr. Klaus Greve
  Postfach 1147, 53001 Bonn
  Germany
  http://www.geographie.uni-bonn.de/deegree/
 
  e-mail: info@deegree.org
  ----------------------------------------------------------------------------*/
 package org.deegree.commons.modules;
 
 import static org.apache.commons.io.IOUtils.closeQuietly;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Collection;
 import java.util.Properties;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 
 import org.reflections.Reflections;
 import org.reflections.scanners.ResourcesScanner;
 import org.reflections.util.ClasspathHelper;
 import org.reflections.util.ConfigurationBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Provides access to deegree module metadata (e.g. Maven artifact identifier and build information).
  * <p>
  * The information is extracted from the following resources on the classpath:
  * <ul>
  * <li><code>META-INF/deegree/buildinfo.properties</code></li>
  * <li><code>META-INF/maven/$groupId/$artifactId/pom.properties</code></li>
  * </ul>
  * </p>
  * 
  * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
  * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  */
 public final class ModuleInfo implements Comparable<ModuleInfo> {
 
     private static final Logger LOG = LoggerFactory.getLogger( ModuleInfo.class );
 
     private static Collection<ModuleInfo> modulesInfo;
 
     static {
         try {
             modulesInfo = extractModulesInfo( ClasspathHelper.getUrlsForCurrentClasspath() );
         } catch ( IOException e ) {
             LOG.error( "Error extracting module info: " + e.getMessage(), e );
         }
     }
 
     private final URL classpath;
 
     private final String groupId;
 
     private final String artifactId;
 
     private final String version;
 
     private final String scmRevision;
 
     private final String buildDate;
 
     private final String buildBy;
 
     private ModuleInfo( URL classpath, String groupId, String artifactId, String version, String scmRevision,
                         String buildDate, String buildBy ) {
         this.classpath = classpath;
         this.groupId = groupId;
         this.artifactId = artifactId;
         this.version = version;
         this.scmRevision = scmRevision;
         this.buildDate = buildDate;
         this.buildBy = buildBy;
     }
 
     /**
      * Returns the URL of the module's classpath.
      * 
      * @return url, never <code>null</code>
      */
     public URL getClasspath() {
         return classpath;
     }
 
     public String getGroupId() {
         return groupId;
     }
 
     public String getArtifactId() {
         return artifactId;
     }
 
     /**
      * Returns the module's version.
      * 
      * @return the version number
      */
     public String getVersion() {
         return version;
     }
 
     /**
      * Returns the date string when the current build was created.
      * 
      * @return the date as String
      */
     public String getBuildDate() {
         return buildDate;
     }
 
     /**
      * Returns the name of the builder.
      * 
      * @return the name of the builder
      */
     public String getBuildBy() {
         return buildBy;
     }
 
     /**
      * @return the svn revision number
      */
     public String getSvnRevision() {
         return scmRevision;
     }
 
     /**
      * Returns the {@link ModuleInfo}s for the deegree modules accessible by the default classloader.
      * 
      * @return module infos, never <code>null</code>, but can be empty
      */
     public static Collection<ModuleInfo> getModulesInfo() {
         return modulesInfo;
     }
 
     /**
      * Returns the {@link ModuleInfo}s for the deegree modules on the given classpathes.
      * 
      * @param classpathURLs
      *            classpath urls, must not be <code>null</code>
      * @return module infos, never <code>null</code>, but can be empty (if no deegree module information is present on
      *         the given classpathes)
      * @throws IOException
      *             if accessing <code>META-INF/deegree/buildinfo.properties</code> or
      *             <code>META-INF/maven/[..]/pom.properties</code> fails
      */
     public static Collection<ModuleInfo> extractModulesInfo( Set<URL> classpathURLs )
                             throws IOException {
         SortedSet<ModuleInfo> modules = new TreeSet<ModuleInfo>();
         for ( URL classpathURL : classpathURLs ) {
            ModuleInfo moduleInfo = extractModuleInfo( classpathURL );
            if ( moduleInfo != null ) {
                modules.add( moduleInfo );
             }
         }
         return modules;
     }
 
     /**
      * Returns the {@link ModuleInfo} for the deegree module on the given classpath.
      * 
      * @param classpathURL
      *            classpath url, must not be <code>null</code>
      * @return module info or <code>null</code> (if the module does not have file META-INF/deegree/buildinfo.properties)
      * @throws IOException
      *             if accessing <code>META-INF/deegree/buildinfo.properties</code> or
      *             <code>META-INF/maven/[..]/pom.properties</code> fails
      */
     public static ModuleInfo extractModuleInfo( URL classpathURL )
                             throws IOException {
 
         ModuleInfo moduleInfo = null;
 
         ConfigurationBuilder builder = new ConfigurationBuilder();
         builder = builder.setUrls( classpathURL );
         builder = builder.setScanners( new ResourcesScanner() );
         Reflections r = new Reflections( builder );
 
         Set<String> resources = r.getResources( Pattern.compile( "buildinfo\\.properties" ) );
         if ( !resources.isEmpty() ) {
             URLClassLoader classLoader = new URLClassLoader( new URL[] { classpathURL }, null );
             String resourcePath = resources.iterator().next();
             InputStream buildInfoStream = null;
             try {
                 Properties props = new Properties();
                 buildInfoStream = classLoader.getResourceAsStream( resourcePath );
                 props.load( buildInfoStream );
                 String buildBy = props.getProperty( "build.by" );
                 String buildArtifactId = props.getProperty( "build.artifactId" );
                 String buildDate = props.getProperty( "build.date" );
                 String buildRev = props.getProperty( "build.svnrev" );
                 String pomGroupId = null;
                 String pomVersion = null;
 
                 resources = r.getResources( Pattern.compile( "pom\\.properties" ) );
                 InputStream pomInputStream = null;
                 if ( !resources.isEmpty() ) {
                     resourcePath = resources.iterator().next();
                     try {
                         props = new Properties();
                         pomInputStream = classLoader.findResource( resourcePath ).openStream();
                         props.load( pomInputStream );
                         String pomArtifactId = props.getProperty( "artifactId" );
                         if ( !pomArtifactId.equals( buildArtifactId ) ) {
                             LOG.warn( "ArtifactId mismatch for module on path: " + classpathURL
                                       + " (buildinfo.properties vs. pom.properties)." );
                         }
                         pomGroupId = props.getProperty( "groupId" );
                         pomVersion = props.getProperty( "version" );
                     } finally {
                         closeQuietly( pomInputStream );
                     }
                 }
                 moduleInfo = new ModuleInfo( classpathURL, pomGroupId, buildArtifactId, pomVersion, buildRev,
                                              buildDate, buildBy );
             } finally {
                 closeQuietly( buildInfoStream );
             }
         }
         return moduleInfo;
     }
 
     @Override
     public int compareTo( ModuleInfo that ) {
         return toString().compareTo( that.toString() );
     }
 
     @Override
     public boolean equals( Object o ) {
         if ( o instanceof ModuleInfo ) {
             return this.toString().equals( o.toString() );
         }
         return false;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         // if ( groupId != null ) {
         // sb.append( groupId );
         // sb.append( "." );
         // }
         sb.append( artifactId );
         if ( version != null ) {
             sb.append( "-" );
             sb.append( version );
         }
         sb.append( " (svn revision " );
         sb.append( scmRevision );
         sb.append( " build@" );
         sb.append( buildDate );
         sb.append( " by " );
         sb.append( buildBy );
         sb.append( ")" );
         return sb.toString();
     }
 }
