 package no.feide.moria.directory;
 
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 import java.util.Timer;
 
 import no.feide.moria.directory.DirectoryManagerConfigurationException;
 import no.feide.moria.directory.backend.AuthenticationFailedException;
 import no.feide.moria.directory.backend.BackendException;
 import no.feide.moria.directory.backend.DirectoryManagerBackend;
 import no.feide.moria.directory.backend.DirectoryManagerBackendFactory;
 import no.feide.moria.directory.index.DirectoryManagerIndex;
 import no.feide.moria.log.MessageLogger;
 
 /**
  * The Directory Manager (sometimes referred to as DM) component in Moria 2.
  * Responsible for all backend operations, such as user authentication and
  * attribute retrieval from the backend sources.
  */
 public class DirectoryManager {
 
     /** The message logger. */
     private final MessageLogger log = new MessageLogger(DirectoryManager.class);
 
     /** Internal representation of the index. */
     private DirectoryManagerIndex index = null;
 
     /**
      * This timer uses <code>IndexUpdater</code> to periodically call
      * <code>updateIndex()</code>.
      */
     private Timer indexTimer = null;
 
     /** Internal representation of the backend factory. */
     private DirectoryManagerBackendFactory backendFactory = null;
 
     /** The currently used (valid) Directory Manager configuration. */
     private DirectoryManagerConfiguration configuration = null;
 
 
     /**
      * Destructor. Cancels the index update timer, given that it has been
      * initialized.
      */
     public void destroy() {
 
         if (indexTimer != null)
             indexTimer.cancel();
 
     }
 
 
     /**
      * Set or update the Directory Manager's configuration. The first time this
      * method is used it will force an initial index update by reading the index
      * through <code>IndexUpdater.readIndex()</code>.
      * @param config
      *            The configuration. The actual parsing is done by the
      *            <code>DirectoryManagerConfiguration</code> constructor.
      * @throws IllegalArgumentException
      *             If <code>config</code> is <code>null</code>.
      * @throws DirectoryManagerConfigurationException
      *             If unable to set the initial configuration; that is, the
      *             Directory Manager has not previous working configuration to
      *             fall back on (in which case a warning will be logged
      *             instead). Also thrown if unable to resolve the backend
      *             factory class (as specified in the configuration file) or if
      *             unable to instantiate this class.
      * @see DirectoryManagerConfiguration#DirectoryManagerConfiguration(Properties)
      * @see IndexUpdater#readIndex()
      */
     public void setConfig(final Properties config) {
 
         // Update current configuration.
         try {
 
             final DirectoryManagerConfiguration newConfiguration = new DirectoryManagerConfiguration(config);
             configuration = newConfiguration;
 
         } catch (Exception e) {
 
             // Something happened while updating the configuration; can we
             // recover?
             if (configuration == null) {
 
                 // Critical error; we don't have a working configuration.
                 throw new DirectoryManagerConfigurationException("Unable to set initial configuration", e);
 
             } else {
 
                 // Non-critical error; we still have a working configuration.
                 log.logWarn("Unable to update existing configuration", e);
 
             }
 
         }
 
         // Update the index and (re-)start the index update timer.
         IndexUpdater indexUpdater = new IndexUpdater(this, configuration.getIndexFilename());
         if (indexTimer != null) {
 
             // Stop the currently running index update timer.
             indexTimer.cancel();
 
         } else {
 
             // The first time we set the configuration we manually force an
             // index update to ensure we have a working index.
             indexTimer = new Timer(true); // Daemon.
             updateIndex(indexUpdater.readIndex());
 
         }
         indexTimer.scheduleAtFixedRate(indexUpdater, configuration.getIndexUpdateFrequency(), configuration.getIndexUpdateFrequency());
 
         // Set the backend factory class.
         // TODO: Gracefully handle switch between backend factories? Unlikely...
         Constructor constructor = null;
         try {
 
             constructor = configuration.getBackendFactoryClass().getConstructor(null);
             backendFactory = (DirectoryManagerBackendFactory) constructor.newInstance(null);
 
         } catch (NoSuchMethodException e) {
             log.logCritical("Cannot find backend factory constructor", e);
             throw new DirectoryManagerConfigurationException("Cannot find backend factory constructor", e);
         } catch (Exception e) {
             log.logCritical("Unable to instantiate backend factory object", e);
             throw new DirectoryManagerConfigurationException("Unable to instantiate backend factory object", e);
         }
 
     }
 
 
     /**
      * Set or update the internal index structure. Used by
      * <code>IndexUpdater.run()</code> to periodically update the index.
      * @param newIndex
      *            The new index object. A <code>null</code> value is taken to
      *            indicate that the index should <em>not</em> be updated.
      * @throws DirectoryManagerConfigurationException
      *             If <code>newIndex</code> is <code>null</code> and the
      *             index has not been previously set.
      * @see IndexUpdater#run()
      */
     protected synchronized void updateIndex(DirectoryManagerIndex newIndex) {
 
         // Sanity check.
         if ((newIndex == null) && (index == null))
             throw new DirectoryManagerConfigurationException("Index not initialized but file " + configuration.getIndexFilename() + " still marked as outdated");
 
         // Update existing index.
         index = newIndex;
 
     }
 
 
     /**
      * Forwards an authentication attempt to the underlying backend.
      * @param userCredentials
      *            The user credentials passed on for authentication.
      * @param attributeRequest
      *            An array containing the attribute names requested for
      *            retrieval after successful authentication.
      * @return The user attributes matching the attribute request, if those were
      *         available. The keys will be <code>String</code> objects, while
      *         the values will be <code>String</code> arrays containing one or
      *         more attribute values. Note that if any of the requested
      *         attributes could not be retrieved from the backend following a
      *         successful authentication (for example, if they simply do not
      *         exist in the backend in question), the <code>HashMap</code>
      *         will still include those attributes that <em>could</em> be
      *         retrieved. If no attributes were requested, or if no attributes
      *         were retrievable from the backend, an empty <code>HashMap</code>
      *         will be returned. This still indicates a successful
      *         authentication.
      * @throws BackendException
      *             Subclasses of <code>BackendException</code> is thrown if an
      *             error is encountered when operating the backend.
      * @throws AuthenticationFailedException
      *             If we managed to access the backend, and the authentication
      *             failed. In other words, the user credentials are incorrect.
      * @throws DirectoryManagerConfigurationException
      *             If attempting to use this method without successfully using
      *             <code>setConfig(Properties)</code> first.
     * @see setConfig(Properties)
      * @see DirectoryManagerBackend#authenticate(Credentials, String[])
      */
     public HashMap authenticate(final Credentials userCredentials, final String[] attributeRequest)
     throws AuthenticationFailedException, BackendException {
 
         // Sanity check.
         if (configuration == null)
             throw new DirectoryManagerConfigurationException("Configuration not set");
 
         // TODO: Implement a backend pool.
 
         // Do the call through a temporary backend instance.
         DirectoryManagerBackend backend = backendFactory.createBackend();
         List references = index.lookup(userCredentials.getUsername());
         if (references != null) {
 
             // Found a reference. Now open it.
             // TODO: Use secondary references as fallback if the first fails.
             backend.open((String) references.get(0));
 
         } else {
 
             // Could not locate the user in the index.
             throw new AuthenticationFailedException("User " + userCredentials.getUsername() + " is unknown");
 
         }
 
         // Authenticate the user.
         HashMap attributes = backend.authenticate(userCredentials, attributeRequest);
 
         // Close the backend and return any attributes.
         backend.close();
         return attributes;
 
     }
 
 }
