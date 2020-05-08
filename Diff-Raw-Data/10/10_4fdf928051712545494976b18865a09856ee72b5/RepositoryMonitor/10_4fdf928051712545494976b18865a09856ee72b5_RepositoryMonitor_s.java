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
 import be.mira.adastra3.server.repository.configurations.Kiosk30Configuration;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.commons.io.FileUtils;
 import org.tigris.subversion.javahl.ClientException;
 import org.tigris.subversion.javahl.Depth;
 import org.tigris.subversion.javahl.Info2;
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
 
 
     //
     // Auxiliary classes
     //
 
     private class Monitor extends TimerTask {
         @Override
         public void run() {
             try {
                 // Check configurations
                 getLogger().debug("Checking the configurations");
                 long tConfigurationsRevision = checkConfigurations();
                 if (mConfigurationRevision != tConfigurationsRevision) {
                     getLogger().info("Configurations changed to revision " + tConfigurationsRevision);
                     mConfigurationRevision = getConfigurations();
                     processConfigurations();
                 }
             } catch (RepositoryException tException) {
                 Repository.getInstance().emitError("could not update repository", tException);
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
         // Do a checkout
         try {
             getLogger().debug("Checking out and processing the configurations");
             mConfigurationRevision = getConfigurations();
             processConfigurations();
         } catch (RepositoryException tException) {
             throw new ServiceRunException("Could not fetch initial data", tException);
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
                 tReader.getConfiguration().setRevision(tConfigurationRevision);
                 Configuration tConfiguration = tReader.getConfiguration();
                 tConfigurations.put(tConfiguration.getId(), tReader.getConfiguration());
             } else {
                 throw new RepositoryException("found empty configuration file");
             }
         }        
             
         // Submit the configurations
         getLogger().debug("Submitting changed configurations");
         Repository tRepository = Repository.getInstance();
         for (Configuration tOldConfiguration: tRepository.getConfigurations()) {
             if (! tConfigurations.containsKey(tOldConfiguration.getId())) {
                 getLogger().debug("Configuration "
                         + tOldConfiguration.getId()
                         + "seems to have been deleted (last known rev "
                         + tOldConfiguration.getRevision()
                        + ", removing from repository");
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
                            + ", adding to repository");
                     tRepository.addConfiguration(tConfiguration);
                 } else if (tConfiguration.getRevision() > tOldConfiguration.getRevision()) {
                     getLogger().debug("Configuration "
                             + tConfiguration.getId()
                             + " is a more recent version (rev "
                             + tConfiguration.getRevision()
                             + ") of an existing configuration (rev "
                            + tOldConfiguration.getRevision() + 
                            "), updating the repository");
                     tRepository.updateConfiguration(tConfiguration);
                 }
             } catch (RepositoryException tException) {
                 throw new RepositoryException("could not process configuration", tException);
             }
         }
     }
 
 
     //
     // Auxiliary
     //
     
     private long getPathRevision(final String iPath) throws RepositoryException {
         try
         {
             Info2[] tInfoList = mSVNClient.info2(
                     mSVNLocation + "/" + iPath,
                     Revision.HEAD,
                     Revision.HEAD,
                     false);
             if (tInfoList.length != 1)
                 throw new RepositoryException("unexpected amount of info entries");
             long tRevision = tInfoList[0].getLastChangedRev();
             getLogger().trace("Repository entry '" + iPath + "' is at revision " + tRevision);
             return tRevision;
         } catch (ClientException tException) {
             throw new RepositoryException("could not check the repository", tException);
         }
     }
 
     private long checkoutRepository(final File iCheckout, final String iLocation) throws RepositoryException  {
         try
         {
             long tRevision = mSVNClient.checkout(
                     iLocation,
                     iCheckout.getAbsolutePath(),
                     Revision.HEAD,
                     Revision.HEAD,
                     Depth.infinity,
                     false,
                     false);
             getLogger().trace("Checked revision " + tRevision + " from the repository at '" + iLocation + " out to '" + iCheckout);
             
             return tRevision;
         } catch (ClientException tException) {
             throw new RepositoryException("could not checkout the repository", tException);
         }
     }
     
     private long updateRepository(final File iCheckout) throws RepositoryException {
         try
         {
             long tRevision = mSVNClient.update(
                     iCheckout.getAbsolutePath(),
                     Revision.HEAD,
                     Depth.infinity,
                     true,
                     false,
                     false);
             getLogger().trace("Updated the repository at '" + iCheckout + "' to revision " + tRevision);
             return tRevision;
         } catch (ClientException tException) {
             throw new RepositoryException("could not update the repository", tException);
         }
     }
 }
