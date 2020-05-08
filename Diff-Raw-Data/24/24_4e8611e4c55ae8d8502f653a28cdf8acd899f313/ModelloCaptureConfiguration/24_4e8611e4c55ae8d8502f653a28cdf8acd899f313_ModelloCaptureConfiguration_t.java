 package com.redhat.tools.nexus.capture.config;
 
 import static org.codehaus.plexus.util.IOUtil.close;
 
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
 import org.sonatype.nexus.proxy.NoSuchRepositoryException;
 import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
 import org.sonatype.nexus.proxy.repository.GroupRepository;
 import org.sonatype.nexus.proxy.repository.Repository;
 
 import com.redhat.tools.nexus.capture.config.model.CaptureConfigModel;
 import com.redhat.tools.nexus.capture.config.model.io.xpp3.CaptureConfigXpp3Reader;
 import com.redhat.tools.nexus.capture.config.model.io.xpp3.CaptureConfigXpp3Writer;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 @Named( "modello" )
 public class ModelloCaptureConfiguration
     implements CaptureConfiguration
 {
 
     private static final String CONFIG_FILE = "capture.xml";
 
     private final Logger logger = LoggerFactory.getLogger( getClass() );
 
     @Inject
     private ApplicationConfiguration appConfig;
 
     @Inject
     private RepositoryRegistry unprotectedRepositoryRegistry;
 
     private CaptureConfigModel configModel;
 
     @Override
     public CaptureConfigModel getModel()
         throws InvalidConfigurationException
     {
         read();
 
         return configModel;
     }
 
     @Override
    public synchronized void save()
         throws InvalidConfigurationException
     {
         if ( configModel == null )
         {
             logger.warn( "Capture configuration is empty, and will NOT be saved." );
             return;
         }
 
         final File configFile = new File( appConfig.getConfigurationDirectory(), CONFIG_FILE );
         configFile.getParentFile().mkdirs();
 
         FileWriter writer = null;
         try
         {
             writer = new FileWriter( configFile );
             new CaptureConfigXpp3Writer().write( writer, configModel );
         }
         catch ( final IOException e )
         {
             throw new InvalidConfigurationException( "Failed to write capture configuration to file: %s\nReason: %s",
                                                      e, configFile, e.getMessage() );
         }
         finally
         {
             close( writer );
         }
     }
 
     @Override
     public void updateModel( final CaptureConfigModel model )
         throws InvalidConfigurationException
     {
         if ( model == null )
         {
             throw new InvalidConfigurationException( "Capture configuration is missing or invalid." );
         }
 
         configModel = model;
         save();
     }
 
    private synchronized void read()
         throws InvalidConfigurationException
     {
         if ( configModel != null )
         {
             return;
         }
 
         final File configFile = new File( appConfig.getConfigurationDirectory(), CONFIG_FILE );
 
         if ( configFile.isFile() && configFile.canRead() )
         {
             FileReader reader = null;
             try
             {
                 reader = new FileReader( configFile );
                 configModel = new CaptureConfigXpp3Reader().read( reader );
             }
             catch ( final IOException e )
             {
                 throw new InvalidConfigurationException(
                                                          "Failed to initialize capture configuration on disk.\nReason: %s",
                                                          e, e.getMessage() );
             }
             catch ( final XmlPullParserException e )
             {
                 throw new InvalidConfigurationException(
                                                          "Failed to initialize capture configuration on disk.\nReason: %s",
                                                          e, e.getMessage() );
             }
             finally
             {
                 close( reader );
             }
         }
         else
         {
             configModel = new CaptureConfigModel();
 
             Repository repo = null;
             try
             {
                 repo = unprotectedRepositoryRegistry.getRepositoryWithFacet( "public", GroupRepository.class );
             }
             catch ( final NoSuchRepositoryException e )
             {
                 final List<GroupRepository> groups =
                     unprotectedRepositoryRegistry.getRepositoriesWithFacet( GroupRepository.class );
 
                 List<Repository> repos;
                 if ( groups != null && !groups.isEmpty() )
                 {
                     repos = new ArrayList<Repository>( groups );
                 }
                 else
                 {
                     repos = unprotectedRepositoryRegistry.getRepositories();
                 }
 
                 if ( repos != null && !repos.isEmpty() )
                 {
                     Collections.sort( groups, new Comparator<Repository>()
                     {
                         @Override
                         public int compare( final Repository r1, final Repository r2 )
                         {
                             return r1.getId().compareTo( r2.getId() );
                         }
                     } );
 
                     repo = groups.get( 0 );
                 }
             }
 
             if ( repo != null )
             {
                 configModel.setCaptureSource( repo.getId() );
                 try
                 {
                     save();
                 }
                 catch ( final InvalidConfigurationException e )
                 {
                     throw new Error( "[NEXUS-3308] FAILURE to initialize XML configuration: " + e.getMessage(), e );
                 }
             }
         }
     }
 
 }
