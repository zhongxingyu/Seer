 /**
  * Copyright (C) 2012 Tim Besard <tim.besard@gmail.com>
  *
  * All rights reserved.
  */
 
 package be.mira.adastra3.server.bo;
 
 import be.mira.adastra3.server.bo.repository.processors.ConfigurationProcessor;
 import be.mira.adastra3.server.exceptions.RepositoryException;
 import be.mira.adastra3.server.bo.repository.RepositoryEntity;
 import be.mira.adastra3.server.bo.repository.configuration.Configuration;
 import be.mira.adastra3.server.bo.repository.connection.Connection;
 import be.mira.adastra3.server.bo.repository.presentation.Presentation;
 import be.mira.adastra3.server.bo.repository.processors.ConnectionProcessor;
 import be.mira.adastra3.spring.Slf4jLogger;
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
 import javax.annotation.PostConstruct;
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.beans.factory.annotation.Value;
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
 public abstract class RepositoryMonitor {
     //
     // Data members
     //
     
     @Slf4jLogger
     private Logger mLogger;
     
     private Repository mRepository;
 
     private @Value("${repository.location}") String mSVNLocation;
     private @Value("${repository.checkout}") File mSVNCheckoutRoot;
     private @Value("${repository.interval}") int mSVNMonitorInterval;
     
     private SVNClient mSVNClient;
     private Timer mSVNMonitor;
     
     private long mConnectionsRevision;
     private long mConfigurationsRevision;
     private long mPresentationsRevision;
 
 
     //
     // Construction and destruction
     //
     
     @Required
     @Autowired
     public void setRepository(Repository iRepository) {
         mRepository = iRepository;
     }
 
     @PostConstruct
     public final void init() throws Exception {
         // Subversion checkout root
         if (!mSVNCheckoutRoot.exists()) {
             mSVNCheckoutRoot.mkdirs();
         }
         if (!mSVNCheckoutRoot.exists() || !mSVNCheckoutRoot.canWrite()) {
             throw new Exception("checkout path does not exist or is not writable");
         }
         
         // Subversion location
         Pattern tLocationPattern = Pattern.compile("^(https?|file|svn)://");
         Matcher tLocationMatcher = tLocationPattern.matcher(mSVNLocation);
         if (!tLocationMatcher.find()) {
             throw new Exception("repository location '" + mSVNLocation + "' is not a valid URL");
         }
         mRepository.setServer(mSVNLocation);
         mSVNClient = new SVNClient();
 
         // Monitor timer
         if (mSVNMonitorInterval <= 0) {
             throw new Exception("Update interval out of valid range");
         }
         mLogger.debug("Scheduling SVN monitor with interval of {}s", mSVNMonitorInterval);
         mSVNMonitor = new Timer();
         
        // TODO: reduce to get() and mRevision = -1, no processing
         
         // Get the connections
         try {
             mLogger.debug("Checking out and processing the connections");
             mConnectionsRevision = getConnections();
             processConnections();
         } catch (RepositoryException tException) {
             throw new Exception("could not fetch the connections", tException);
         }
         
         // Get the configurations
         try {
             mLogger.debug("Checking out and processing the configurations");
             mConfigurationsRevision = getConfigurations();
             processConfigurations();
         } catch (RepositoryException tException) {
             throw new Exception("could not fetch the configurations", tException);
         }
         
         // Get the media
         try {
             mLogger.debug("Processing the media");
             mConfigurationsRevision = checkPresentations();
             processPresentations();
         } catch (RepositoryException tException) {
             throw new Exception("could not fetch the media", tException);
         }
 
         // Schedule the monitor
         mSVNMonitor.schedule(
                 new Monitor(),
                 mSVNMonitorInterval * 1000, // Initial delay
                 mSVNMonitorInterval * 1000  // Period
             );
     }
 
 
     //
     // Auxiliary classes
     //
 
     private class Monitor extends TimerTask {
         @Override
         public void run() {
             // TODO: squash these three cases in something using the 
             //       RepositoryEntity interface
             // Check the connections
             try {
                 mLogger.debug("Checking the connections");
                 long tConnectionsRevision = checkConnections();
                 if (mConnectionsRevision != tConnectionsRevision) {
                     mLogger.info("Connections changed to revision {}", tConnectionsRevision);
                     mConnectionsRevision = tConnectionsRevision;
                     getConnections();
                     processConnections();
                 }
             } catch (RepositoryException tException) {
                 mLogger.error("could not update the connections", tException);
             }
             
             // Check the configurations
             try {
                 mLogger.debug("Checking the configurations");
                 long tConfigurationsRevision = checkConfigurations();
                 if (mConfigurationsRevision != tConfigurationsRevision) {
                     mLogger.info("Configurations changed to revision {}", tConfigurationsRevision);
                     mConfigurationsRevision = tConfigurationsRevision;
                     getConfigurations();
                     processConfigurations();
                 }
             } catch (RepositoryException tException) {
                 mLogger.error("could not update the configurations", tException);
             }
             
             // Check the presentations
             try {
                 mLogger.debug("Checking the presentations");
                 long tPresentationRevision = checkPresentations();
                 if (mPresentationsRevision != tPresentationRevision) {
                     mLogger.info("Presentations changed to revision {}", tPresentationRevision);
                     mPresentationsRevision = tPresentationRevision;
                     processPresentations();
                 }
             } catch (RepositoryException tException) {
                 mLogger.error("could not update the presentations", tException);
             }
         }
         
     }
     
     private class XMLFilter implements FilenameFilter {
       private Pattern mPattern = Pattern.compile("\\.xml$", Pattern.CASE_INSENSITIVE);
 
       @Override
       public final boolean accept(final File iDirectory, final String iFilename) {
           return mPattern.matcher(iFilename).find();
       }
     }
     
     
     //
     // Presentation helpers
     //
     
     private long checkPresentations() throws RepositoryException {
         return getRevision(mSVNLocation + "/presentations");
     }
     
     private void processPresentations() throws RepositoryException {        
         // List
         mLogger.debug("Listing presentations");
         Map<String, Presentation> tNewPresentations = new HashMap<String, Presentation>();
         Map<String, Long> tPathEntries = getChildrenRevisions(mSVNLocation + "/presentations");
         for (Map.Entry<String, Long> tEntry: tPathEntries.entrySet()) {
             String tPath = "/presentations/" + tEntry.getKey();
             Presentation tMedia = new Presentation(tEntry.getValue(), tPath);
             tNewPresentations.put(tEntry.getKey(), tMedia);
         }
         
         // Update
         mLogger.debug("Updating presentations");
         Repository tRepository = mRepository;
         RepositoryChangeset<Presentation> tChangeset = new RepositoryChangeset<Presentation>(tRepository.getPresentations(), tNewPresentations);
         for (Map.Entry<String, Presentation> tEntry: tChangeset.getRemovals().entrySet()) {
             tRepository.removePresentation(tEntry.getKey(), tEntry.getValue());
         }
         for (Map.Entry<String, Presentation> tEntry: tChangeset.getAdditions().entrySet()) {
             tRepository.addPresentation(tEntry.getKey(), tEntry.getValue());
         }
         for (Map.Entry<String, Presentation> tEntry: tChangeset.getUpdates().entrySet()) {
             tRepository.updatePresentation(tEntry.getKey(), tEntry.getValue());
         }
     }
     
     
     //
     // Configuration helpers
     //
     
     private long checkConfigurations() throws RepositoryException {
         return getRevision(mSVNLocation + "/configurations");
     }
     
     private long getConfigurations() throws RepositoryException {
         // Get a local checkout and location
         final File tCheckout =  new File(mSVNCheckoutRoot, "configurations");
         final String tLocation = mSVNLocation + "/configurations";
         
         // Check if the repository exists and is valid
         Long tConfigurationRevision = null;
         try {
             tConfigurationRevision = getRevision(tCheckout);
         } catch (RepositoryException tException) {
             // Do nothing
         }
         
         // Checkout or update
         try {
             if (tConfigurationRevision == null) {
                 mLogger.trace("Fetching configurations");
                 if (tCheckout.exists()) {
                     FileUtils.cleanDirectory(tCheckout);
                 }
                 tConfigurationRevision = checkoutRepository(tCheckout, tLocation);            
             } else {
                 mLogger.trace("Updating configurations");
                 tConfigurationRevision = updateRepository(tCheckout);              
             }
         } catch (RepositoryException tException) {
             throw new RepositoryException("could not download the repository", tException);
         } catch (IOException tException) {
             throw new RepositoryException("could not clean the existing (and seemingly invalid) copy of the repository", tException);
         }
         
         return tConfigurationRevision;
     }
     
     private void processConfigurations() throws RepositoryException {        
         // Read
         mLogger.debug("Reading configurations");
         Map<String, Configuration> tNewConfigurations = new HashMap<String, Configuration>();
         File tDirectory = new File(mSVNCheckoutRoot, "configurations");
         for (File tFile: tDirectory.listFiles(new XMLFilter())) {
             // Generate an identifier
             String tFilename = tFile.getName();
             mLogger.trace("Processing '{}'", tFilename);
             int tDotPosition = tFilename.lastIndexOf('.');
             String tId = tFilename.substring(0, tDotPosition);
             final long tRevision = getRevision(tFile);
             
             // Process the contents
             String tRepositoryPath = "/configurations/" + tFilename;
             ConfigurationProcessor tReader = createConfigurationProcessor(tRevision, tRepositoryPath, tFile);
             tReader.process();
             Configuration tConfiguration = tReader.getConfiguration();
             if (tConfiguration == null) {
                 throw new RepositoryException("found empty configuration file");
             }
             tNewConfigurations.put(tId, tConfiguration);
         }
         
         // Save
         mLogger.debug("Saving configurations");
         Repository tRepository = mRepository;
         RepositoryChangeset<Configuration> tChangeset = new RepositoryChangeset<Configuration>(tRepository.getConfigurations(), tNewConfigurations);
         for (Map.Entry<String, Configuration> tEntry: tChangeset.getRemovals().entrySet()) {
             tRepository.removeConfiguration(tEntry.getKey(), tEntry.getValue());
         }
         for (Map.Entry<String, Configuration> tEntry: tChangeset.getAdditions().entrySet()) {
             tRepository.addConfiguration(tEntry.getKey(), tEntry.getValue());
         }
         for (Map.Entry<String, Configuration> tEntry: tChangeset.getUpdates().entrySet()) {
             tRepository.addConfiguration(tEntry.getKey(), tEntry.getValue());
         }
     }
     
     
     //
     // Connection helpers
     //
     
     // TODO: Remove the quite identical Connection/Configuration/Presentation setters
     //       somehow make it using the RepositoryEntity interface
     
     private long checkConnections() throws RepositoryException {
         return getRevision(mSVNLocation + "/connections");
     }
     
     private long getConnections() throws RepositoryException {
         // Get a local checkout and location
         final File tCheckout =  new File(mSVNCheckoutRoot, "connections");
         final String tLocation = mSVNLocation + "/connections";
         
         // Check if the repository exists and is valid
         Long tConnectionRevision = null;
         try {
             tConnectionRevision = getRevision(tCheckout);
         } catch (RepositoryException tException) {
             // Do nothing
         }
         
         // Checkout or update
         try {
             if (tConnectionRevision == null) {
                 mLogger.trace("Fetching connections");
                 if (tCheckout.exists()) {
                     FileUtils.cleanDirectory(tCheckout);
                 }
                 tConnectionRevision = checkoutRepository(tCheckout, tLocation);            
             } else {
                 mLogger.trace("Updating connections");
                 tConnectionRevision = updateRepository(tCheckout);              
             }
         } catch (RepositoryException tException) {
             throw new RepositoryException("could not download the repository", tException);
         } catch (IOException tException) {
             throw new RepositoryException("could not clean the existing (and seemingly invalid) copy of the repository", tException);
         }
         
         return tConnectionRevision;
     }
     
     private void processConnections() throws RepositoryException {        
         // Read
         mLogger.debug("Reading connections");
         Map<String, Connection> tNewConnections = new HashMap<String, Connection>();
         File tDirectory = new File(mSVNCheckoutRoot, "connections");
         for (File tFile: tDirectory.listFiles(new XMLFilter())) {
             // Generate an identifier
             String tFilename = tFile.getName();
             mLogger.trace("Processing '{}'", tFilename);
             int tDotPosition = tFilename.lastIndexOf('.');
             String tId = tFilename.substring(0, tDotPosition);
             final long tRevision = getRevision(tFile);
             
             // Process the contents
             String tRepositoryPath = "/connections/" + tFilename;
             ConnectionProcessor tReader = createConnectionProcessor(tRevision, tRepositoryPath, tFile);
             tReader.process();
             Connection tConnection = tReader.getConnection();
             if (tConnection == null) {
                 throw new RepositoryException("found empty connection file");
             }
             tNewConnections.put(tId, tConnection);
         }
         
         // Save
         mLogger.debug("Saving connections");
         Repository tRepository = mRepository;
         RepositoryChangeset<Connection> tChangeset = new RepositoryChangeset<Connection>(tRepository.getConnections(), tNewConnections);
         for (Map.Entry<String, Connection> tEntry: tChangeset.getRemovals().entrySet()) {
             tRepository.removeConnection(tEntry.getKey(), tEntry.getValue());
         }
         for (Map.Entry<String, Connection> tEntry: tChangeset.getAdditions().entrySet()) {
             tRepository.addConnection(tEntry.getKey(), tEntry.getValue());
         }
         for (Map.Entry<String, Connection> tEntry: tChangeset.getUpdates().entrySet()) {
             tRepository.addConnection(tEntry.getKey(), tEntry.getValue());
         }
     }
 
 
     //
     // Auxiliary
     //
     
     private Long getRevision(final File iFile) throws RepositoryException {
         if (! iFile.exists()) {
             return null;
         }
         return getRevision(iFile.getAbsolutePath());
     }
     
     // TODO: case for an URL
     
     private Long getRevision(final String iPath) throws RepositoryException {
         try {
             final List<Long> tRevisions = new ArrayList<Long>();
             mSVNClient.info2(
                     iPath,
                     Revision.HEAD,
                     Revision.HEAD,
                     Depth.empty,
                     null,
                     new InfoCallback() {
                         @Override
                         public void singleInfo(final Info2 iInfo) {
                             tRevisions.add(iInfo.getLastChangedRev());
                         }
                     });
             if (tRevisions.size() != 1) {
                 throw new RepositoryException("unexpected amount of info entries");
             }
             return tRevisions.get(0);
         } catch (ClientException tException) {
             throw new RepositoryException("could not check the repository", tException);
         }
     }
     
     private Map<String, Long> getChildrenRevisions(final File iFile) throws RepositoryException {
         if (! iFile.exists()) {
             return null;
         }
         return getChildrenRevisions(iFile.getAbsoluteFile());
     }
     
     // TODO: case for an URL
     
     private Map<String, Long> getChildrenRevisions(final String iPath) throws RepositoryException {
         try {
             final Map<String, Long> tChildren = new HashMap<String, Long>();
             mSVNClient.info2(
                     iPath,
                     Revision.HEAD,
                     Revision.HEAD,
                     Depth.immediates,
                     null,
                     new InfoCallback() {
                         @Override
                         public void singleInfo(final Info2 iInfo) {
                             if (iInfo.getPath().equals(iPath)) {
                                 return;
                             }
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
     
     private class RepositoryChangeset<T extends RepositoryEntity> {
         //
         // Member data
         //
         
         private final Map<String, T> mAdditions;
         private final Map<String, T> mRemovals;
         private final Map<String, T> mUpdates;
         
         
         //
         // Construction and destruction
         //
         
         public RepositoryChangeset(final Map<String, T> iOldEntities, final Map<String, T> iCurrentEntities) {
             // Check for removed entities
             mRemovals = new HashMap<String, T>();
             for (Map.Entry<String, T> tOldEntry: iOldEntities.entrySet()) {
                 if (! iCurrentEntities.containsKey(tOldEntry.getKey())) {
                     mLogger.debug("Entity "
                             + tOldEntry.getKey()
                             + " seems to have been deleted (last known rev "
                             + tOldEntry.getValue().getRevision()
                             + "), removing from repository");
                     mRemovals.put(tOldEntry.getKey(), tOldEntry.getValue());
                 }
             }
 
             // Check for new and updated entities      
             mAdditions = new HashMap<String, T>();
             mUpdates = new HashMap<String, T>();
             for (Map.Entry<String, T> tCurrentEntry: iCurrentEntities.entrySet()) {
                 T tOldEntity = iOldEntities.get(tCurrentEntry.getKey());
                 if (tOldEntity == null) {
                     mLogger.debug("Entity "
                             + tCurrentEntry.getKey()
                             + " seems new (rev "
                             + tCurrentEntry.getValue().getRevision()
                             + "), adding to repository");
                     mAdditions.put(tCurrentEntry.getKey(),  tCurrentEntry.getValue());
                 } else if ( tCurrentEntry.getValue().getRevision() > tOldEntity.getRevision()) {
                     mLogger.debug("Entity "
                             + tCurrentEntry.getKey()
                             + " is a more recent version (rev "
                             +  tCurrentEntry.getValue().getRevision()
                             + ") of an existing media (rev "
                             + tOldEntity.getRevision()
                             + "), updating the repository");
                     mUpdates.put(tCurrentEntry.getKey(),  tCurrentEntry.getValue());
                 }
             }
         }
         
         
         //
         // Getters and setters
         //
         
         public final Map<String, T> getAdditions() {
             return mAdditions;
         }
         
         public final Map<String, T> getRemovals() {
             return mRemovals;
         }
         
         public final Map<String, T> getUpdates() {
             return mUpdates;
         }
     }
     
     
     //
     // Abstract method injectors
     //
     
     protected abstract ConnectionProcessor createConnectionProcessor(final long iRevision, final String iPath, final File iFile);
     protected abstract ConfigurationProcessor createConfigurationProcessor(final long iRevision, final String iPath, final File iFile);
 }
