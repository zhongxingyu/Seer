 /**
  * Sonatype Nexus (TM) Open Source Version.
  * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
  * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
  * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License Version 3 for more details.
  * You should have received a copy of the GNU General Public License Version 3 along with this program.
  * If not, see http://www.gnu.org/licenses/.
  * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
  * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
  */
 package org.sonatype.nexus.configuration.application.upgrade;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.codehaus.plexus.component.annotations.Component;
 import org.codehaus.plexus.component.annotations.Requirement;
 import org.codehaus.plexus.logging.AbstractLogEnabled;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.StringUtils;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
 import org.sonatype.configuration.upgrade.SingleVersionUpgrader;
 import org.sonatype.configuration.upgrade.UpgradeMessage;
 import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
 import org.sonatype.nexus.configuration.model.CRepository;
 import org.sonatype.nexus.configuration.model.CScheduledTask;
 import org.sonatype.nexus.configuration.model.v1_4_4.upgrade.BasicVersionConverter;
 
 /**
  * Upgrades configuration model from version 1.4.3 to 1.4.4.
  * 
  * @author bdemers
  */
 @Component( role = SingleVersionUpgrader.class, hint = "1.4.3" )
 public class Upgrade143to144
     extends AbstractLogEnabled
     implements SingleVersionUpgrader
 {
     @Requirement
     private ApplicationConfiguration applicationConfiguration;
 
     public Object loadConfiguration( File file )
         throws IOException, ConfigurationIsCorruptedException
     {
         FileReader fr = null;
 
         org.sonatype.nexus.configuration.model.v1_4_3.Configuration conf = null;
 
         try
         {
             // reading without interpolation to preserve user settings as variables
             fr = new FileReader( file );
 
             org.sonatype.nexus.configuration.model.v1_4_3.io.xpp3.NexusConfigurationXpp3Reader reader =
                 new org.sonatype.nexus.configuration.model.v1_4_3.io.xpp3.NexusConfigurationXpp3Reader();
 
             conf = reader.read( fr );
         }
         catch ( XmlPullParserException e )
         {
             throw new ConfigurationIsCorruptedException( file.getAbsolutePath(), e );
         }
         finally
         {
             if ( fr != null )
             {
                 fr.close();
             }
         }
 
         return conf;
     }
 
     public void upgrade( UpgradeMessage message )
         throws ConfigurationIsCorruptedException
     {
         org.sonatype.nexus.configuration.model.v1_4_3.Configuration oldc =
             (org.sonatype.nexus.configuration.model.v1_4_3.Configuration) message.getConfiguration();
 
         org.sonatype.nexus.configuration.model.Configuration newc =
             new BasicVersionConverter().convertConfiguration( oldc );
 
         // NEXUS-3833
         for ( CScheduledTask task : newc.getTasks() )
         {
             if ( "ReindexTask".equals( task.getType() ) )
             {
                 task.setType( "UpdateIndexTask" );
             }
         }
 
         // Upgrade for Attributes
         final File oldAttributesBase = new File( applicationConfiguration.getWorkingDirectory( "proxy" ), "attributes" );
 
         File sourceDirectory;
 
         File destinationDirectory;
 
         for ( CRepository repository : newc.getRepositories() )
         {
             // we handle only repositories with "default" storage paths and "file" LS
             // all other cases should be covered by manual steps performed by Admin!
             if ( repository.getLocalStorage() != null && "file".equals( repository.getLocalStorage().getProvider() ) )
             {
                getLogger().info(
                    "Upgrading proxy attributes for repository \"" + repository.getName() + "\" (id="
                        + repository.getId() + ")..." );

                 sourceDirectory = new File( oldAttributesBase, repository.getId() );
 
                 if ( !sourceDirectory.isDirectory() )
                 {
                     // nothing to upgrade
                     continue;
                 }
 
                 destinationDirectory =
                    getRepositoryStorageBaseDirectory( repository.getId(), repository.getLocalStorage().getUrl() );
 
                 try
                 {
                     FileUtils.copyDirectoryStructure( sourceDirectory, destinationDirectory );
                 }
                 catch ( IOException e )
                 {
                     getLogger().warn(
                         "Could not delete old proxy attributes from " + sourceDirectory
                             + " but migration did succeed; manual cleanup needed." );
 
                     throw new IllegalStateException(
                         "Cannot upgrade proxy attributes! Upgrade stopped and manual intervention needed!", e );
                 }
                 try
                 {
                     FileUtils.forceDelete( sourceDirectory );
                 }
                 catch ( IOException e )
                 {
                     getLogger().warn(
                         "Could not delete old proxy attributes from " + sourceDirectory
                             + " but migration did succeed; manual cleanup needed." );
                 }
             }
             else
             {
                 getLogger().info(
                     "Skipping proxy upgrade for repository \""
                         + repository.getName()
                         + "\" (id="
                         + repository.getId()
                         + ") since it uses non-default storage provider. Here Nexus assumes manual upgrade steps are performed already!" );
             }
         }
 
         // one more try (in case of some failure above) to get rid of proxy folder
         try
         {
             FileUtils.forceDelete( oldAttributesBase.getParentFile() );
         }
         catch ( IOException e )
         {
             getLogger().warn(
                 "Could not delete old proxy folder from nexus work directory but migration did succeed; manual cleanup needed." );
         }
 
         newc.setVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
         message.setModelVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
         message.setConfiguration( newc );
     }
 
     //
 
     protected File getRepositoryStorageBaseDirectory( String repositoryId, String urlStr )
         throws ConfigurationIsCorruptedException
     {
         if ( StringUtils.isBlank( urlStr ) )
         {
            return new File( new File( new File( applicationConfiguration.getWorkingDirectory( "storage" ),
                repositoryId ), ".nexus" ), "attributes" );
         }
         else
         {
             URL url;
 
             try
             {
                 url = new URL( urlStr );
             }
             catch ( MalformedURLException e )
             {
                 try
                 {
                     url = new File( urlStr ).toURI().toURL();
                 }
                 catch ( MalformedURLException e1 )
                 {
                     throw new ConfigurationIsCorruptedException( "The local storage has a malformed URL as baseUrl!", e );
                 }
             }
 
             File file;
 
             try
             {
                 file = new File( url.toURI() );
             }
             catch ( Throwable t )
             {
                 file = new File( url.getPath() );
             }
 
             if ( file.exists() )
             {
                 if ( file.isFile() )
                 {
                     throw new ConfigurationIsCorruptedException( "The repository (ID=\"" + repositoryId
                         + "\") repository's baseDir is not a directory, path: " + file.getAbsolutePath() );
                 }
             }
 
             return file;
         }
     }
 
 }
