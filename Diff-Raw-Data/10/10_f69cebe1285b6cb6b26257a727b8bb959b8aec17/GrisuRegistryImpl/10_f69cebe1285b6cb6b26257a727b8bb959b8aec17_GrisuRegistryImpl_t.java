 package grisu.model;
 
 import grisu.control.ServiceInterface;
 import grisu.control.TemplateManager;
import grisu.jcommons.constants.Constants;
 import grisu.model.info.ApplicationInformation;
 import grisu.model.info.ApplicationInformationImpl;
 import grisu.model.info.ResourceInformation;
 import grisu.model.info.ResourceInformationImpl;
 import grisu.model.info.UserApplicationInformation;
 import grisu.model.info.UserApplicationInformationImpl;
 import grisu.settings.Environment;
 import grith.jgrith.credential.Credential;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vpac.historyRepeater.DummyHistoryManager;
 import org.vpac.historyRepeater.HistoryManager;
 import org.vpac.historyRepeater.SimpleHistoryManager;
 
 /**
  * The GrisuRegistry provides access to all kinds of information via an
  * easy-to-use api.
  * 
  * You can access the following information objects via the registry:
  * 
  * {@link UserEnvironmentManager}: to find out what resources the current user
  * can access <br/>
  * {@link UserApplicationInformation}: information about the
  * applications/versions of applications this user has got access to<br/>
  * {@link ApplicationInformation}: information about the applications that are
  * provided grid-wide<br/>
  * {@link ResourceInformation}: general information about the available
  * resources like queues, submissionlocations, stagingfilesystems<br/>
  * {@link HistoryManager}: can be used to store / retrieve data that the user
  * used in past jobs<br/>
  * {@link FileManager}: to do file transfer and such<br/>
  * 
  * @author Markus Binsteiner
  * 
  */
 public class GrisuRegistryImpl implements GrisuRegistry {
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(GrisuRegistryImpl.class.getName());
 
 	// caching the registries for different serviceinterfaces. for a desktop
 	// application this most
 	// likely only ever contains only one registry object. But for
 	// webapplications it can hold more
 	// than that. Advantage is that several users can share for example the
 	// ResourceInformation object
 	// but can have (or must have) seperate UserApplicationInformation objects
 
 	private final ServiceInterface serviceInterface;
 
 	private final Map<String, Object> objects = new HashMap<String, Object>();
 
 	private HistoryManager historyManager = null;
 	private final Map<String, ApplicationInformation> cachedApplicationInformationObjects = new HashMap<String, ApplicationInformation>();
 	private final Map<String, UserApplicationInformation> cachedUserInformationObjects = new HashMap<String, UserApplicationInformation>();
 	private UserEnvironmentManager cachedUserInformation;
 	private ResourceInformation cachedResourceInformation;
 	private FileManager cachedFileHelper;
 	private TemplateManager templateManager;
 	private final Credential credential;
 
 	public GrisuRegistryImpl(final ServiceInterface serviceInterface,
 			final Credential credential) {
 		this.serviceInterface = serviceInterface;
 		this.credential = credential;
 	}
 
 	public Object get(String key) {
 		return objects.get(key);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.model.GrisuRegistryInterface#getApplicationInformation
 	 * (java.lang.String)
 	 */
 	public final ApplicationInformation getApplicationInformation(
			String applicationName) {

		if (StringUtils.isBlank(applicationName)) {
			applicationName = Constants.GENERIC_APPLICATION_NAME;
		}
 
 		synchronized (applicationName) {
 
 			if (cachedApplicationInformationObjects.get(applicationName) == null) {
 				final ApplicationInformation temp = new ApplicationInformationImpl(
 						serviceInterface, applicationName);
 				cachedApplicationInformationObjects.put(applicationName, temp);
 			}
 		}
 		return cachedApplicationInformationObjects.get(applicationName);
 	}
 
 	public Credential getCredential() {
 		return this.credential;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.model.GrisuRegistryInterface#getFileManager()
 	 */
 	public synchronized final FileManager getFileManager() {
 		if (cachedFileHelper == null) {
 			cachedFileHelper = new FileManager(serviceInterface);
 		}
 		return cachedFileHelper;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.model.GrisuRegistryInterface#getHistoryManager()
 	 */
 	public synchronized final HistoryManager getHistoryManager() {
 		if (historyManager == null) {
 			final File historyFile = new File(Environment
 					.getGrisuClientDirectory().getPath(),
 					GRISU_HISTORY_FILENAME);
 			if (!historyFile.exists()) {
 				try {
 					historyFile.createNewFile();
 
 				} catch (final IOException e) {
 					myLogger.debug(e.getLocalizedMessage(), e);
 				}
 			}
 			if (!historyFile.exists()) {
 				historyManager = new DummyHistoryManager();
 			} else {
 				historyManager = new SimpleHistoryManager(historyFile);
 			}
 		}
 		return historyManager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.model.GrisuRegistryInterface#getResourceInformation()
 	 */
 	public final synchronized ResourceInformation getResourceInformation() {
 		if (cachedResourceInformation == null) {
 			cachedResourceInformation = new ResourceInformationImpl(
 					serviceInterface);
 		}
 		return cachedResourceInformation;
 	}
 
 	public synchronized TemplateManager getTemplateManager() {
 
 		if (templateManager == null) {
 			templateManager = new TemplateManager(serviceInterface);
 		}
 		return templateManager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.model.GrisuRegistryInterface#getUserApplicationInformation
 	 * (java.lang.String)
 	 */
 	public final UserApplicationInformation getUserApplicationInformation(
 			final String applicationName) {
 
 		synchronized (applicationName) {
 
 			if (cachedUserInformationObjects.get(applicationName) == null) {
 				final UserApplicationInformation temp = new UserApplicationInformationImpl(
 						serviceInterface, getUserEnvironmentManager(),
 						applicationName);
 				cachedUserInformationObjects.put(applicationName, temp);
 			}
 		}
 		return cachedUserInformationObjects.get(applicationName);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.model.GrisuRegistryInterface#getUserEnvironmentManager()
 	 */
 	public final synchronized UserEnvironmentManager getUserEnvironmentManager() {
 
 		if (cachedUserInformation == null) {
 			this.cachedUserInformation = new UserEnvironmentManagerImpl(
 					serviceInterface);
 		}
 		return cachedUserInformation;
 	}
 
 	public void set(String key, Object object) {
 		objects.put(key, object);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.model.GrisuRegistryInterface#setUserEnvironmentManager
 	 * (grisu.model.UserEnvironmentManager)
 	 */
 	public final synchronized void setUserEnvironmentManager(
 			final UserEnvironmentManager ui) {
 		this.cachedUserInformation = ui;
 	}
 
 }
