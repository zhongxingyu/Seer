 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package be.mira.adastra3.server.repository;
 
 import be.mira.adastra3.server.Service;
 import be.mira.adastra3.server.exceptions.RepositoryException;
 import be.mira.adastra3.server.exceptions.ServiceRunException;
 import be.mira.adastra3.server.exceptions.ServiceSetupException;
 import be.mira.adastra3.server.repository.configurations.Configuration;
 import be.mira.adastra3.server.repository.media.Media;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.commons.io.FileUtils;
 import org.tigris.subversion.javahl.ClientException;
 import org.tigris.subversion.javahl.Depth;
 import org.tigris.subversion.javahl.Info2;
 import org.tigris.subversion.javahl.InfoCallback;
 import org.tigris.subversion.javahl.Revision;
 import org.tigris.subversion.javahl.SVNClient;
 
 /**
  *
  * @author tim
  */
 public class RepositoryMonitor extends Service {
     //
     // Data members
     //
 
     private String mSVNLocation;
     private File mSVNCheckoutRoot;
     private SVNClient mSVNClient;
     private Timer mSVNMonitor;
     private int mSVNMonitorInterval;
     
     private long mConfigurationRevision;
     private long mMediaRevision;
 
 
     //
     // Auxiliary classes
     //
 
     private class Monitor extends TimerTask {
         @Override
         public void run() {
             // Check the configurations
             try {
                 getLogger().debug("Checking the configurations");
                 long tConfigurationsRevision = checkConfigurations();
                 if (mConfigurationRevision != tConfigurationsRevision) {
                     getLogger().info("Configurations changed to revision " + tConfigurationsRevision);
                     mConfigurationRevision = tConfigurationsRevision;
                     getConfigurations();
                     processConfigurations();
                 }
             } catch (RepositoryException tException) {
                 Repository.getInstance().emitError("could not update the configurations", tException);
             }
             
             // Check the media
             try {
                 getLogger().debug("Checking the media");
                 long tMediaRevision = checkMedia();
                 if (mMediaRevision != tMediaRevision) {
                     getLogger().info("Media changed to revision " + tMediaRevision);
                     mMediaRevision = tMediaRevision;
                     processMedia();
                 }
             } catch (RepositoryException tException) {
                 Repository.getInstance().emitError("could not update the media", tException);
             }
         }
         
     }
     
     public class ConfigurationFilter implements FilenameFilter {
       protected Pattern mPattern = Pattern.compile("\\.xml$", Pattern.CASE_INSENSITIVE);
 
       @Override
       public boolean accept(File iDirectory, String iFilename) {
           return mPattern.matcher(iFilename).find();
       }
     }
 
 
     //
     // Construction and destruction
     //
 
     public RepositoryMonitor() throws ServiceSetupException {
         // Subversion checkout root
         mSVNCheckoutRoot = new File(getConfiguration().getString("repository.checkout"));
         if (!mSVNCheckoutRoot.exists() || !mSVNCheckoutRoot.canWrite())
             throw new ServiceSetupException("checkout path does not exist or is not writable");
         
         // Subversion location
         Pattern tLocationPattern = Pattern.compile("^(https?|file|svn)://");
         mSVNLocation = getConfiguration().getString("repository.location");
         Matcher tLocationMatcher = tLocationPattern.matcher(mSVNLocation);
         if (!tLocationMatcher.find()) {
             throw new ServiceSetupException("repository location '" + mSVNLocation + "' is not a valid URL");
         }
         Repository.getInstance().setServer(mSVNLocation);
         mSVNClient = new SVNClient();
 
         // Monitor timer
         Integer tInterval = getConfiguration().getInt("repository.interval");
         if (tInterval <= 0) {
             throw new ServiceSetupException("Update interval out of valid range");
         }
         mSVNMonitorInterval = tInterval;
         getLogger().debug("Scheduling SVN monitor with interval of " + tInterval + " seconds");
         mSVNMonitor = new Timer();
     }
 
 
     //
     // Service interface
     //
 
     @Override
     public final void run() throws ServiceRunException {
         // Get the configurations
         try {
             getLogger().debug("Checking out and processing the configurations");
             mConfigurationRevision = getConfigurations();
             processConfigurations();
         } catch (RepositoryException tException) {
             throw new ServiceRunException("could not fetch the configurations", tException);
         }
         
         // Get the media
         try {
             getLogger().debug("Processing the media");
             mConfigurationRevision = checkMedia();
             processMedia();
         } catch (RepositoryException tException) {
             throw new ServiceRunException("could not fetch the media", tException);
         }
 
         // Schedule the monitor
         mSVNMonitor.schedule(
                 new Monitor(),
                 mSVNMonitorInterval * 1000, // Initial delay
                 mSVNMonitorInterval * 1000  // Period
             );
     }
 
     @Override
     public final void stop() throws ServiceRunException {
     }
     
     
     //
     // Configuration helpers
     //
     
     public final long checkConfigurations() throws RepositoryException {
         return getPathRevision("configurations");
     }
     
     public final long getConfigurations() throws RepositoryException {
         // Get a local checkout and location
         final File tCheckout =  new File(mSVNCheckoutRoot, "configurations");
         final String tLocation = mSVNLocation + "/configurations";
         
         // Check if the repository exists and is valid
         Long tConfigurationRevision = null;
         try {
             tConfigurationRevision = checkConfigurations();
         } catch (RepositoryException tException) {
             // Do nothing
         }
         
         // Checkout or update
         try {
             if (tConfigurationRevision == null) {
                 FileUtils.cleanDirectory(tCheckout);
                 tConfigurationRevision = checkoutRepository(tCheckout, tLocation);            
             } else {
                 tConfigurationRevision = updateRepository(tCheckout);              
             }
         } catch (RepositoryException tException) {
             throw new RepositoryException("could not download the repository", tException);
         } catch (IOException tException) {
             throw new RepositoryException("could not clean the existing (and seemingly invalid) copy of the repository", tException);
         }
         
         return tConfigurationRevision;
     }
     
     public final void processConfigurations() throws RepositoryException {
         // TODO: the actual diff code in processConfigurations and processMedia
         //       is more or less the same, maybe move the getRevision interface
         //       from Media & Configuration to RepositoryEntry, and deduplicate
         //       the diff code?
         
         // Read the configurations
         getLogger().debug("Reading configurations");
         Map<String, Configuration> tConfigurations = new HashMap<String, Configuration>();
         File tConfigurationDirectory = new File(mSVNCheckoutRoot, "configurations");
         for (File tConfigurationFile: tConfigurationDirectory.listFiles(new ConfigurationFilter())) {
             // Generate an identifier
             String tName = tConfigurationFile.getName();
             getLogger().trace("Processing '" + tName + "'");
             int tDotPosition = tName.lastIndexOf('.');
             String tNameSimple = tName.substring(0, tDotPosition);
             
             // Get the local revision
             final long tConfigurationRevision = getPathRevision("configurations/" + tConfigurationFile.getName());
             
             // Process the contents
             ConfigurationReader tReader = new ConfigurationReader(tNameSimple, tConfigurationFile);
             tReader.process();
             if (tReader.getConfiguration() != null) {
                 Configuration tConfiguration = tReader.getConfiguration();
                 tConfiguration.setRevision(tConfigurationRevision);
                 tConfigurations.put(tConfiguration.getId(), tConfiguration);
             } else {
                 throw new RepositoryException("found empty configuration file");
             }
         }        
             
         // Submit the configurations
         getLogger().debug("Submitting changed configurations");
         Repository tRepository = Repository.getInstance();
         for (Configuration tOldConfiguration: tRepository.getAllConfigurations()) {
             if (! tConfigurations.containsKey(tOldConfiguration.getId())) {
                 getLogger().debug("Configuration "
                         + tOldConfiguration.getId()
                        + "seems to have been deleted (last known rev "
                         + tOldConfiguration.getRevision()
                         + "), removing from repository");
                 tRepository.removeConfiguration(tOldConfiguration);
                 
             }
         }
         for (Configuration tConfiguration : tConfigurations.values()) {
             try {
                 Configuration tOldConfiguration = tRepository.getConfiguration(tConfiguration.getId());
                 if (tOldConfiguration == null) {
                     getLogger().debug("Configuration "
                             + tConfiguration.getId()
                            + "seems new (rev "
                             + tConfiguration.getRevision()
                             + "), adding to repository");
                     tRepository.addConfiguration(tConfiguration);
                 } else if (tConfiguration.getRevision() > tOldConfiguration.getRevision()) {
                     getLogger().debug("Configuration "
                             + tConfiguration.getId()
                             + " is a more recent version (rev "
                             + tConfiguration.getRevision()
                             + ") of an existing configuration (rev "
                             + tOldConfiguration.getRevision()
                             + "), updating the repository");
                     tRepository.updateConfiguration(tConfiguration);
                 }
             } catch (RepositoryException tException) {
                 throw new RepositoryException("could not process configuration", tException);
             }
         }
     }
     
     
     //
     // Media helpers
     //
     
     public final long checkMedia() throws RepositoryException {
         return getPathRevision("media");
     }
     
     public final void processMedia() throws RepositoryException {        
         // List the media
         getLogger().debug("Listing media");
         Map<String, Media> tAllMedia = new HashMap<String, Media>();
         Map<String, Long> tPathEntries = getChildrenRevisions("media");
         for (String tName: tPathEntries.keySet()) {
             long tRevision = tPathEntries.get(tName);
             String tLocation = Repository.getInstance().getServer() + "/media/" + tName;
             
             Media tMedia = new Media(tName, tLocation);
             tMedia.setRevision(tRevision);
             tAllMedia.put(tName, tMedia);
         }
         
         // Submit the media        
         getLogger().debug("Submitting media");
         Repository tRepository = Repository.getInstance();
         for (Media tOldMedia: tRepository.getAllMedia()) {
             if (! tAllMedia.containsKey(tOldMedia.getId())) {
                 getLogger().debug("Media "
                         + tOldMedia.getId()
                        + "seems to have been deleted (last known rev "
                         + tOldMedia.getRevision()
                         + "), removing from repository");
                 tRepository.removeMedia(tOldMedia);
             }
         }
         for (Media tMedia : tAllMedia.values()) {
             try {
                 Media tOldMedia = tRepository.getMedia(tMedia.getId());
                 if (tOldMedia == null) {
                     getLogger().debug("Media "
                             + tMedia.getId()
                            + "seems new (rev "
                             + tMedia.getRevision()
                             + "), adding to repository");
                     tRepository.addMedia(tMedia);
                 } else if (tMedia.getRevision() > tOldMedia.getRevision()) {
                     getLogger().debug("Configuration "
                             + tMedia.getId()
                             + " is a more recent version (rev "
                             + tMedia.getRevision()
                             + ") of an existing media (rev "
                             + tOldMedia.getRevision()
                             + "), updating the repository");
                     tRepository.updateMedia(tMedia);
                 }
             } catch (RepositoryException tException) {
                 throw new RepositoryException("could not process media", tException);
             }
         }
     }
 
 
     //
     // Auxiliary
     //
     
     private long getPathRevision(final String iPath) throws RepositoryException {
         try {
             final List<Long> tRevisions = new ArrayList<Long>();
             mSVNClient.info2(
                     mSVNLocation + "/" + iPath,
                     Revision.HEAD,
                     Revision.HEAD,
                    1,
                     null,
                     new InfoCallback() {
                         @Override
                         public void singleInfo(Info2 iInfo) {
                             tRevisions.add(iInfo.getLastChangedRev());
                         }
                     });
             if (tRevisions.size() != 1)
                 throw new RepositoryException("unexpected amount of info entries");
             return tRevisions.get(0);
         } catch (ClientException tException) {
             throw new RepositoryException("could not check the repository", tException);
         }
     }
     
     private Map<String, Long> getChildrenRevisions(final String iPath) throws RepositoryException {
         try {
             final Map<String, Long> tChildren = new HashMap<String, Long>();
             mSVNClient.info2(
                     mSVNLocation + "/" + iPath,
                     Revision.HEAD,
                     Revision.HEAD,
                    2,
                     null,
                     new InfoCallback() {
                         @Override
                         public void singleInfo(Info2 iInfo) {
                             if (iInfo.getPath().equals(iPath))
                                 return;
                             tChildren.put(iInfo.getPath(), iInfo.getLastChangedRev());
                         }
                     });
             return tChildren;
         } catch (ClientException tException) {
             throw new RepositoryException("could not check the repository", tException);
         }
     }
 
     private long checkoutRepository(final File iCheckout, final String iLocation) throws RepositoryException {
         try {
             long tRevision = mSVNClient.checkout(
                     iLocation,
                     iCheckout.getAbsolutePath(),
                     Revision.HEAD,
                     Revision.HEAD,
                     Depth.infinity,
                     false,
                     false);            
             return tRevision;
         } catch (ClientException tException) {
             throw new RepositoryException("could not checkout the repository", tException);
         }
     }
     
     private long updateRepository(final File iCheckout) throws RepositoryException {
         try {
             long tRevision = mSVNClient.update(
                     iCheckout.getAbsolutePath(),
                     Revision.HEAD,
                     Depth.infinity,
                     true,
                     false,
                     false);
             return tRevision;
         } catch (ClientException tException) {
             throw new RepositoryException("could not update the repository", tException);
         }
     }
 }
