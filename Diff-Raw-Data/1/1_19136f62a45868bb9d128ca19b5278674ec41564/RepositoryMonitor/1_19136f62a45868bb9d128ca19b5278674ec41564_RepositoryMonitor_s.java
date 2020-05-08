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
 import be.mira.adastra3.server.repository.configurations.KioskConfiguration;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.UrlValidator;
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
     private File mSVNCheckout;
     private SVNClient mSVNClient;
     private long mSVNRevision;
     private Timer mSVNMonitor;
     private int mSVNMonitorInterval;
 
 
     //
     // Auxiliary classes
     //
 
     private class Monitor extends TimerTask {
         @Override
         public void run() {
             try {
                 // Get the data
                 getLogger().debug("Checking the SVN repository for changes");
                 long tPreviousRevision = mSVNRevision;
                 getData();
                 
                 // Check for changes
                 if (mSVNRevision != tPreviousRevision)
                 {
                     getLogger().debug("Repository has updated from revision " + tPreviousRevision + " to " + mSVNRevision + ", rereading the data");
                     processData();
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
         // Subversion checkout path
         mSVNCheckout = new File(getConfiguration().getString("repository.checkout"));
         if (!mSVNCheckout.exists() || !mSVNCheckout.canWrite())
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
             getLogger().debug("Checking out the repository");
             getData();
             
             getLogger().debug("Processing the data");
             processData();
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
     
     public final void getData() throws RepositoryException {
         // Check if the repository exists and is valid
         long tExistingRevision = -1;
         try {
             tExistingRevision = checkRepository(mSVNCheckout);
         } catch (RepositoryException tException) {
             // Do nothing
         }
         
         // Checkout or update
         try {
             if (tExistingRevision == -1) {
                 FileUtils.cleanDirectory(mSVNCheckout);
                 tExistingRevision = checkoutRepository(mSVNCheckout, mSVNLocation);            
             } else {
                 tExistingRevision = updateRepository(mSVNCheckout);              
             }
         } catch (RepositoryException tException) {
             throw new RepositoryException("could not download the repository", tException);
         } catch (IOException tException) {
             throw new RepositoryException("could not clean the existing (and seemingly invalid) copy of the repository", tException);
         }
         mSVNRevision = tExistingRevision;
     }
     
     public final void processData() throws RepositoryException {
         // Read the configurations
         // TODO: detect changes
         getLogger().debug("Reading configurations");
         List<Configuration> tConfigurations = new ArrayList<Configuration>();
         File tConfigurationDirectory = new File(mSVNCheckout, "configurations");
         for (File tConfigurationFile: tConfigurationDirectory.listFiles(new ConfigurationFilter())) {
             // Generate an identifier
             String tName = tConfigurationFile.getName();
             getLogger().trace("Processing '" + tName + "'");
             int tDotPosition = tName.lastIndexOf('.');
             String tNameSimple = tName.substring(0, tDotPosition);
             
             // Process the contents
             ConfigurationReader tReader = new ConfigurationReader(tNameSimple, tConfigurationFile);
             tReader.process();
             if (tReader.getConfiguration() != null) {
                 tReader.getConfiguration().setRevision(mSVNRevision);
                 tConfigurations.add(tReader.getConfiguration());
             } else {
                 throw new RepositoryException("found empty configuration file");
             }
         }        
             
         // Submit the configurations
         // TODO: only submit changes
         getLogger().debug("Submitting all configurations");
         Repository tRepository = Repository.getInstance();
         for (Configuration tConfiguration : tConfigurations) {
             try {
                 // KioskConfiguration processing
                 if (tConfiguration instanceof KioskConfiguration) {
                     KioskConfiguration tKioskConfiguration = (KioskConfiguration) tConfiguration;
                     getLogger().debug("Processing kiosk configuration " + tKioskConfiguration.getId());
                     KioskConfiguration tOldKioskConfiguration = tRepository.getKioskConfiguration(tKioskConfiguration.getId());
                     if (tOldKioskConfiguration == null) {
                         getLogger().debug("Configuration seems new, adding to repository");
                         tRepository.addKioskConfiguration(tKioskConfiguration);
                     } else if (tKioskConfiguration.getRevision() > tOldKioskConfiguration.getRevision()) {
                         getLogger().debug("New configuration is a more recent version of an existing configuration, updating the repository");
                         tRepository.updateKioskConfiguration(tKioskConfiguration);
                     } else {
                         getLogger().debug("Configuration hasn't been updated, ignoring");
                     }
                 } else {
                     throw new RepositoryException("unknown configuration type");
                 }
             } catch (RepositoryException tException) {
                 throw new RepositoryException("could not process configuration", tException);
             }
         }
         
         // Process the media
         //getLogger().debug("Reading media");
         // TODO: detect the changed media
         //       this is only needed when we detect the changed configurations,
         //       because now we reload each configuration anyhow
     }
 
 
     //
     // Auxiliary
     //
     
     private long checkRepository(final File iCheckout) throws RepositoryException {
         try
         {
             Info2[] tInfoList = mSVNClient.info2(
                     iCheckout.getAbsolutePath(),
                     Revision.HEAD,
                     Revision.HEAD,
                     false);
             if (tInfoList.length != 1)
                 throw new RepositoryException("unexpected amount of info entries");
             long tRevision = tInfoList[0].getRev();
             getLogger().debug("Repository at '" + iCheckout + "' is at revision " + tRevision);
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
             getLogger().debug("Checked revision " + tRevision + " from the repository at '" + iLocation + " out to '" + iCheckout);
             
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
             getLogger().debug("Updated the repository at '" + iCheckout + "' to revision " + tRevision);
             return tRevision;
         } catch (ClientException tException) {
             throw new RepositoryException("could not update the repository", tException);
         }
     }
 }
