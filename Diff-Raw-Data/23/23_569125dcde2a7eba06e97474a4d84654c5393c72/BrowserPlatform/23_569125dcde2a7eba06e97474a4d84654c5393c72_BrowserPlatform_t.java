 /*
  * Created On: Feb 4, 2006
  */
 package com.thinkparity.ophelia.browser.platform;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import com.thinkparity.codebase.Mode;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.event.EventNotifier;
 import com.thinkparity.codebase.filter.Filter;
 import com.thinkparity.codebase.filter.FilterManager;
 import com.thinkparity.codebase.log4j.Log4JWrapper;
 import com.thinkparity.codebase.sort.StringComparator;
 import com.thinkparity.codebase.swing.AbstractJFrame;
 
 import com.thinkparity.codebase.model.session.Credentials;
 import com.thinkparity.codebase.model.session.Environment;
 import com.thinkparity.codebase.model.session.InvalidCredentialsException;
 
 import com.thinkparity.ophelia.model.events.MigratorEvent;
 import com.thinkparity.ophelia.model.util.ProcessAdapter;
 import com.thinkparity.ophelia.model.util.ProcessMonitor;
 import com.thinkparity.ophelia.model.workspace.CannotLockException;
 import com.thinkparity.ophelia.model.workspace.InitializeMediator;
 import com.thinkparity.ophelia.model.workspace.Workspace;
 import com.thinkparity.ophelia.model.workspace.WorkspaceModel;
 
 import com.thinkparity.ophelia.browser.Version;
 import com.thinkparity.ophelia.browser.Constants.Directories;
 import com.thinkparity.ophelia.browser.Constants.EnvironmentVariable;
 import com.thinkparity.ophelia.browser.Constants.Files;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.AvatarFactory;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.AvatarId;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.AvatarRegistry;
 import com.thinkparity.ophelia.browser.platform.action.ActionFactory;
 import com.thinkparity.ophelia.browser.platform.action.ActionId;
 import com.thinkparity.ophelia.browser.platform.action.ActionInvocation;
 import com.thinkparity.ophelia.browser.platform.action.ActionRegistry;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.browser.platform.action.ThinkParitySwingMonitor;
 import com.thinkparity.ophelia.browser.platform.action.platform.LearnMore;
 import com.thinkparity.ophelia.browser.platform.action.platform.Login;
 import com.thinkparity.ophelia.browser.platform.application.Application;
 import com.thinkparity.ophelia.browser.platform.application.ApplicationFactory;
 import com.thinkparity.ophelia.browser.platform.application.ApplicationId;
 import com.thinkparity.ophelia.browser.platform.application.ApplicationRegistry;
 import com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar;
 import com.thinkparity.ophelia.browser.platform.avatar.ErrorAvatar;
 import com.thinkparity.ophelia.browser.platform.event.LifeCycleEvent;
 import com.thinkparity.ophelia.browser.platform.event.LifeCycleListener;
 import com.thinkparity.ophelia.browser.platform.firstrun.FirstRunHelper;
 import com.thinkparity.ophelia.browser.platform.migrator.MigratorHelper;
 import com.thinkparity.ophelia.browser.platform.online.OnlineHelper;
 import com.thinkparity.ophelia.browser.platform.plugin.PluginHelper;
 import com.thinkparity.ophelia.browser.platform.plugin.PluginRegistry;
 import com.thinkparity.ophelia.browser.platform.window.Window;
 import com.thinkparity.ophelia.browser.platform.window.WindowFactory;
 import com.thinkparity.ophelia.browser.profile.Profile;
 import com.thinkparity.ophelia.browser.util.ModelFactory;
 
 import org.apache.log4j.Logger;
 
 import com.thinkparity.ThinkParity;
 
 /**
  * <b>Title:</b>thinkParity OpheliaUI Platform Implementation<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.33
  */
 public final class BrowserPlatform implements Platform, LifeCycleListener {
 
     /** A singleton instance. */
     private static BrowserPlatform SINGLETON;
 
     /**
      * Create an instance of the platform.
      * 
      * @param environment
      *            The <code>Environment</code> to connect to.
      * @param profile
      *            The platform <code>Profile</code> to open.
      * 
      * @return The platform.
      * @throws CannotLockException
      *             if the profile cannot be exclusively locked
      */
     public static Platform create(final Mode mode,
             final Environment environment, final Profile profile)
             throws CannotLockException {
         Assert.assertIsNull("The platform has already been created.", SINGLETON);
         SINGLETON = new BrowserPlatform(mode, environment, profile);
         return BrowserPlatform.getInstance();
     }
 
     /**
      * Obtain the platform instance.
      * 
      * @return The thinkParity platform.
      */
     public static Platform getInstance() {
         Assert.assertNotNull("The platform has not yet been created.", SINGLETON);
         return SINGLETON;
     }
 
     /** The platform's log4j wrapper. */
 	protected final Log4JWrapper logger;
 
     /** Action registry. */
     private final ActionRegistry actionRegistry;
 
     /** An application factory. */
     private final ApplicationFactory applicationFactory;
 
     /** An application registry. */
 	private final ApplicationRegistry applicationRegistry;
 
     /** An avatar registry. */
 	private final AvatarRegistry avatarRegistry;
 
     /** The thinkParity <code>Environment</code>. */
     private final Environment environment;
 
     /** A platform <code>EventDispatcher</code>. */
     private EventDispatcher eventDispatcher;
 
     /** The platform's first run helper. */
     private final FirstRunHelper firstRunHelper;
 
     /** A life cycle state encapsulation. */
     private final BrowserPlatformLifeCycle lifeCycle;
 
     /**
      * The listener helper. Manages all listeners as well as the listener
      * notification.
      */
     private final ListenerHelper listenerHelper;
 
     /** The platform <code>MigratorHelper</code>. */
     private final MigratorHelper migratorHelper;
 
     /** A thinkParity <code>Mode</code>. */
     private final Mode mode;
 
     /** The parity model factory. */
 	private final ModelFactory modelFactory;
 
     /** The platform <code>OnlineHelper</code>. */
     private final OnlineHelper onlineHelper;
 
 	/** The platform settings. */
 	private final BrowserPlatformPersistence persistence;
 
 	/** The platform <code>PluginHelper</code>. */
     private final PluginHelper pluginHelper;
 
 	/** A thinkParity <code>Workspace</code>. */
     private final Workspace workspace;
 
     /**
      * Create BrowserPlatform.
      * 
      * @param profile
      *            A profile to open.
      */
 	private BrowserPlatform(final Mode mode, final Environment environment,
             final Profile profile) throws CannotLockException {
         super();
         this.environment = environment;
         this.mode = mode;
 
         this.workspace = WorkspaceModel.getInstance(
                 environment).getWorkspace(new File(profile.getParityWorkspace()));
         new BrowserPlatformInitializer(this).initialize(workspace);
         this.actionRegistry = new ActionRegistry();
         this.applicationFactory = ApplicationFactory.getInstance(this);
 		this.applicationRegistry = new ApplicationRegistry();
 		this.avatarRegistry = new AvatarRegistry();
         this.lifeCycle = new BrowserPlatformLifeCycle();
 		this.modelFactory = ModelFactory.getInstance();
 
 		this.logger = new Log4JWrapper();
 		this.persistence = new BrowserPlatformPersistence(this);
 
         this.firstRunHelper = new FirstRunHelper(this);
         this.listenerHelper = new ListenerHelper(this);
         this.onlineHelper = new OnlineHelper(this);
         this.pluginHelper = new PluginHelper(this);
         this.migratorHelper = new MigratorHelper(this);
 	}
 
 	/**
      * @see com.thinkparity.ophelia.browser.platform.Platform#addListener(com.thinkparity.ophelia.browser.platform.event.LifeCycleListener)
      * 
      */
     public void addListener(final LifeCycleListener listener) {
         listenerHelper.addListener(listener);
     }
 
 	/**
      * @see com.thinkparity.ophelia.browser.platform.Platform#createTempFile(java.lang.String)
      * 
      */
     public File createTempFile(final String suffix) throws IOException {
         return workspace.createTempFile(suffix);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#displayErrorDialog(com.thinkparity.ophelia.browser.platform.application.ApplicationId, java.lang.Throwable)
      *
      */
     public void displayErrorDialog(final ApplicationId applicationId, final Throwable error) {
         final Data input = new Data(1);
         if (null != error)
             input.set(ErrorAvatar.DataKey.ERROR, error);
         displayErrorDialog(applicationId, input);
     }
 
 	/**
      * @see com.thinkparity.ophelia.browser.platform.Platform#displayErrorDialog(com.thinkparity.ophelia.browser.platform.application.ApplicationId)
      * 
      */
     public void displayErrorDialog(final ApplicationId applicationId,
             final Throwable error, final String errorMessageKey,
             final Object... errorMessageArguments) {
         final Data input = new Data(3);
         if (null != errorMessageKey)
             input.set(ErrorAvatar.DataKey.ERROR_MESSAGE_KEY, errorMessageKey);
         if (null != errorMessageArguments)
             input.set(ErrorAvatar.DataKey.ERROR_MESSAGE_ARGUMENTS, errorMessageArguments);
         if (null != error)
             input.set(ErrorAvatar.DataKey.ERROR, error);
         displayErrorDialog(applicationId, input);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#displayErrorDialog(java.lang.Throwable)
      *
      */
     public void displayErrorDialog(final Throwable error) {
         final Data input = new Data(1);
         if (null != error)
             input.set(ErrorAvatar.DataKey.ERROR, error);
         displayErrorDialog(input);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#end()
      * 
      */
     public void end() {
         notifyLifeCycleEnding();
         endApplications();
         endPlugins();
         closeWorkspace();
         // remove the platform as its own lifecycle listener
         removeListener(this);
         notifyLifeCycleEnded();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.event.LifeCycleListener#ended(com.thinkparity.ophelia.browser.platform.event.LifeCycleEvent)
      *
      */
     public void ended(final LifeCycleEvent e) {
         lifeCycle.setEnded();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.event.LifeCycleListener#ending(com.thinkparity.ophelia.browser.platform.event.LifeCycleEvent)
      *
      */
     public void ending(final LifeCycleEvent e) {
         try {
             // end the event dispatcher
             eventDispatcher.end();
             eventDispatcher = null;
         } finally {
             lifeCycle.setEnding();
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#getAvailableLocales()
      *
      */
     public Locale[] getAvailableLocales() {
         final Locale[] locales = Locale.getAvailableLocales();
         final List<Locale> availableLocales = new ArrayList<Locale>(locales.length);
         boolean didHit = false;
         for (final Locale locale : locales) {
             didHit = false;
             for (final Locale availableLocale : availableLocales) {
                 if (availableLocale.getCountry().equals(locale.getCountry())) {
                     didHit = true;
                     break;
                 }
             }
             if (!didHit)
                 availableLocales.add(locale);
         }
         Collections.sort(availableLocales, new Comparator<Locale>() {
             final StringComparator stringComparator =
                 new StringComparator(Boolean.TRUE);
             public int compare(final Locale o1, final Locale o2) {
                 return stringComparator.compare(o1.getDisplayCountry(), o2.getDisplayCountry());
             }
         });
         return availableLocales.toArray(new Locale[] {});
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#getAvailableTimeZones()
      *
      */
     public TimeZone[] getAvailableTimeZones() {
         final String[] timeZoneIDs = TimeZone.getAvailableIDs();
         final List<TimeZone> available = new ArrayList<TimeZone>(timeZoneIDs.length);
         for (final String timeZoneID : timeZoneIDs) {
             available.add(TimeZone.getTimeZone(timeZoneID));
         }
         final List<TimeZone> clone = new ArrayList<TimeZone>(available.size());
         clone.addAll(available);
         FilterManager.filter(available, new Filter<TimeZone>() {
             public Boolean doFilter(final TimeZone o) {
                 for (final TimeZone timeZone : clone) {
                     if (o.hasSameRules(timeZone))
                         return Boolean.FALSE;
                 }
                 return Boolean.TRUE;
             }
         });
         Collections.sort(available, new Comparator<TimeZone>() {
             final StringComparator stringComparator =
                 new StringComparator(Boolean.TRUE);
             public int compare(final TimeZone o1, final TimeZone o2) {
                 return stringComparator.compare(o1.getDisplayName(),
                         o2.getDisplayName());
             }
         });
         return available.toArray(new TimeZone[] {});
     }
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.Platform#getAvatarRegistry()
 	 * 
 	 */
 	public AvatarRegistry getAvatarRegistry() { return avatarRegistry; }
 
     public String getBuildId() {
         return Version.getBuildId();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#getEnvironment()
      */
     public Environment getEnvironment() {
         return environment;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#getLocale()
      *
      */
     public Locale getLocale() {
         return persistence.getLocale();
     }
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.Platform#getLogger(java.lang.Class)
 	 * 
 	 */
 	public Logger getLogger(final Class<?> clasz) {
 		return Logger.getLogger(clasz);
 	}
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.Platform#getModelFactory()
 	 * 
 	 */
 	public ModelFactory getModelFactory() { return modelFactory; }
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.Platform#getPersistence()
 	 * 
 	 */
 	public BrowserPlatformPersistence getPersistence() {
 		return persistence;
 	}
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#getPluginRegistry()
      */
     public PluginRegistry getPluginRegistry() {
 //        return pluginHelper.getRegistry();
         return null;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#getReleaseName()
      *
      */
     public String getReleaseName() {
         return Version.getReleaseName();
     }
 
 	/**
      * @see com.thinkparity.ophelia.browser.platform.Platform#getTimeZone()
      *
      */
     public TimeZone getTimeZone() {
         return persistence.getTimeZone();
     }
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.Platform#hibernate(com.thinkparity.ophelia.browser.platform.application.ApplicationId)
 	 * 
 	 */
 	public void hibernate(final ApplicationId applicationId) {
 		applicationRegistry.get(applicationId).hibernate(this);
 	}
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#initializeWorkspace(com.thinkparity.ophelia.model.util.ProcessMonitor,
      *      com.thinkparity.ophelia.model.workspace.InitializeMediator,
      *      com.thinkparity.ophelia.model.workspace.Workspace,
      *      com.thinkparity.codebase.model.session.Credentials)
      */
     public void initializeWorkspace(final ProcessMonitor monitor,
             final InitializeMediator mediator, final Workspace workspace,
             final Credentials credentials) throws InvalidCredentialsException {
         WorkspaceModel.getInstance(environment).initialize(monitor, mediator,
                 workspace, credentials);
     }
 
 	/** @see com.thinkparity.ophelia.browser.platform.Platform#isDevelopmentMode() */
 	public Boolean isDevelopmentMode() {
         switch (mode) {
         case DEMO:
         case PRODUCTION:
         case TESTING:
             return Boolean.FALSE;
         case DEVELOPMENT:
             return Boolean.TRUE;
         default:
             throw Assert.createUnreachable("UNKNOWN MODE");
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#isInternal()
      *
      */
     @Override
     public Boolean isInternal() {
         final String value = System.getenv(EnvironmentVariable.ThinkParity.KEY);
         if (null == value) {
             return Boolean.FALSE;
         } else {
             return EnvironmentVariable.ThinkParity.VALUE_INTERNAL.equals(value);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#isOnline()
      */
     public Boolean isOnline() {
         return onlineHelper.isOnline();
     }
 
 	/**
      * @see com.thinkparity.ophelia.browser.platform.Platform#isSignUpAvailable()
      */
     @Deprecated
     public Boolean isSignUpAvailable() {
         return getModelFactory().getProfileModel(getClass()).isSignUpAvailable();
     }
 
     /** @see com.thinkparity.ophelia.browser.platform.Platform#isTestingMode() */
 	public Boolean isTestingMode() {
         switch (mode) {
         case DEMO:
         case DEVELOPMENT:
         case PRODUCTION:
             return Boolean.FALSE;
         case TESTING:
             return Boolean.TRUE;
         default:
             throw Assert.createUnreachable("UNKNOWN MODE");
         }
 	}
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#isWorkspaceInitialized(com.thinkparity.ophelia.model.workspace.Workspace)
      *
      */
     public Boolean isWorkspaceInitialized(final Workspace workspace) {
         return WorkspaceModel.getInstance(environment).isInitialized(workspace);
     }
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.application.ApplicationListener#notifyEnd(com.thinkparity.ophelia.browser.platform.application.Application)
 	 * 
 	 */
 	public void notifyEnd(final Application application) {
         logApiId();
         logVariable("application", application);
 	}
 
 	/**
 	 * @see com.thinkparity.ophelia.browser.platform.application.ApplicationListener#notifyHibernate(com.thinkparity.ophelia.browser.platform.application.Application)
 	 * 
 	 */
 	public void notifyHibernate(final Application application) {
 		logApiId();
         logVariable("application", application);
 	}
 
 	/**
 	 * @see com.thinkparity.ophelia.browser.platform.application.ApplicationListener#notifyRestore(com.thinkparity.ophelia.browser.platform.application.Application)
 	 * 
 	 */
 	public void notifyRestore(final Application application) {
         logger.logApiId();
         logger.logVariable("application", application);
 	}
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.application.ApplicationListener#notifyStart(com.thinkparity.ophelia.browser.platform.application.Application)
 	 * 
 	 */
 	public void notifyStart(final Application application) {
         logger.logApiId();
         logger.logVariable("application", application);
 	}
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#removeListener(com.thinkparity.ophelia.browser.platform.event.LifeCycleListener)
      */
     public void removeListener(final LifeCycleListener listener) {
         listenerHelper.addListener(listener);
     }
 
 	/**
      * @see com.thinkparity.ophelia.browser.platform.Platform#restart()
      *
      */
     public void restart() {
         Runtime.getRuntime().addShutdownHook(new Thread("TPS-OpheliaUI-Restart") {
             final String[] command = new String[] { "rundll32.exe",
                 "shell32.dll,ShellExec_RunDLL",
                 Files.EXECUTABLE.getAbsolutePath() };
             @Override
             public void run() {
                 try {
                     Runtime.getRuntime().exec(command, null, Directories.ThinkParity.DIRECTORY);
                 } catch (final IOException iox) {
                     logger.logFatal(iox, "Could not restart thinkParity.");
                 }
             }
         });
         end();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#restore(com.thinkparity.ophelia.browser.platform.application.ApplicationId)
      * 
      */
 	public void restore(final ApplicationId applicationId) {
 		logger.logApiId();
         logger.logVariable("applicationId", applicationId);
 		applicationRegistry.get(applicationId).restore(this);
 	}
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#runContactUs()
      * 
      */
     public void runContactUs() {
         invoke(ActionId.PLATFORM_CONTACT_US, Data.emptyData());
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#runLearnMore(com.thinkparity.ophelia.browser.platform.action.platform.LearnMore.Topic)
      */
     public void runLearnMore(final LearnMore.Topic topic) {
         final Data data = new Data(1);
         data.set(LearnMore.DataKey.TOPIC, topic);
         invoke(ActionId.PLATFORM_LEARN_MORE, data);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#runLogin(java.lang.String,
      *      java.lang.String,
      *      com.thinkparity.ophelia.browser.platform.action.ThinkParitySwingMonitor,
      *      com.thinkparity.ophelia.model.workspace.InitializeMediator)
      */
     public void runLogin(final String username, final String password,
             final ThinkParitySwingMonitor monitor,
             final InitializeMediator initializeMediator) {
         final Data data = new Data(4);
         data.set(Login.DataKey.MONITOR, monitor);
         data.set(Login.DataKey.INITIALIZE_MEDIATOR, initializeMediator);
         data.set(Login.DataKey.PASSWORD, password);
         data.set(Login.DataKey.USERNAME, username);
         invoke(ActionId.PLATFORM_LOGIN, data);
     }
 
 	/**
      * @see com.thinkparity.ophelia.browser.platform.Platform#runShowGettingStartedMovie()
      */
     public void runShowGettingStartedMovie() {
         invoke(ActionId.PLATFORM_SHOW_GETTING_STARTED_MOVIE, Data.emptyData());
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.Platform#start()
      * 
      */
 	public void start() {
         // add the platform as its own lifecycle listener
         addListener(this);
 	    notifyLifeCycleStarting();
         logApiId();
         if (!isWorkspaceInitialized()) {
             initializeWorkspace();
         } 
         if (!isWorkspaceInitialized()) {
             deleteWorkspace();
             terminate();
         } else {
             /* a release has been installed, and subsequently needs to be
              * initialized */
             if (!isReleaseInitialized()) {
                 initializeRelease();
             }
             /* a release has been downloaded after which the software has ended
              * and now the release needs to be installed */
             if (!isReleaseInstalled()) {
                 startInstallRelease();
             }
             startPlugins();
             startApplications();
             notifyLifeCycleStarted();
         }
 	}
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.event.LifeCycleListener#started(com.thinkparity.ophelia.browser.platform.event.LifeCycleEvent)
      *
      */
     public void started(final LifeCycleEvent e) {
         // start an event dispatcher
         eventDispatcher = new EventDispatcher(this);
         eventDispatcher.start();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.event.LifeCycleListener#starting(com.thinkparity.ophelia.browser.platform.event.LifeCycleEvent)
      *
      */
     public void starting(final LifeCycleEvent e) {}
 
     protected final void logApiId() {
         logger.logApiId();
     }
 
     /**
      * @see Log4JWrapper#logApiId()
      */
     protected final <V> V logVariable(final String name, final V value) {
         return logger.logVariable(name, value);
     }
 
     /**
      * Fire a product release installed event. We set the image name for the
      * launcher code.
      * 
      * @param e
      *            A <code>MigratorEvent</code>.
      */
     void fireProductReleaseInstalled(final MigratorEvent e) {
         try {
             ThinkParity.setImage(e.getReleaseName());
         } catch (final IOException iox) {
             logger.logFatal(iox, "Cannot upgrade to release {0}.",
                     e.getReleaseName());
         }
     }
 
     /**
      * Close the workspace.
      *
      */
     private void closeWorkspace() {
         WorkspaceModel.getInstance(environment).close(
                 modelFactory.getWorkspace(getClass()));
     }
 
     /**
      * Delete the workspace.
      *
      */
     private void deleteWorkspace() {
         WorkspaceModel.getInstance(environment).delete(
                 modelFactory.getWorkspace(getClass()));
     }
 
     /**
      * Display an application error dialog using the given input.
      * 
      * @param applicationId
      *            An <code>ApplicationId</code>.
      * @param input
      *            The error dialog input <code>Data</code>.
      */
     private void displayErrorDialog(final ApplicationId applicationId,
             final Data input) {
        if (lifeCycle.isEnding() || lifeCycle.isEnded()) {
            logger.logError("Platform ending.  Cannot display error dialog.  {0}",
                    input);
        } else {
            open(applicationId, isDevelopmentMode()
                    ? AvatarId.DIALOG_ERROR_DETAILS : AvatarId.DIALOG_ERROR, input);
        }
     }
 
     /**
      * Display an error dialog using the given input.
      * 
      * @param input
      *            The error dialog input <code>Data</code>.
      */
     private void displayErrorDialog(final Data input) {
         if (lifeCycle.isEnding() || lifeCycle.isEnded()) {
             logger.logError("Platform ending.  Cannot display error dialog.  {0}",
                     input);
         } else {
             open(isDevelopmentMode() ? AvatarId.DIALOG_ERROR_DETAILS
                     : AvatarId.DIALOG_ERROR, input);
         }
     }
 
     /**
      * End all applications.
      *
      */
     private void endApplications() {
         for (final ApplicationId id : ApplicationId.values()) {
             if(applicationRegistry.contains(id))
                 applicationRegistry.get(id).end(this);
         }
     }
 
     /**
      * End all plugins.
      *
      */
     private void endPlugins() {
         pluginHelper.end();
     }
 
     /**
      * Obtain the action from the controller's cache. If the action does not
      * exist in the cache it is created and stored.
      * 
      * @param id
      *            The action id.
      * @return The action.
      * 
      * @see ActionId
      */
     private ActionInvocation getAction(final ActionId id) {
         if (actionRegistry.contains(id)) {
             return actionRegistry.get(id);
         } else {
             return ActionFactory.create(id);
         }
     }
 
     /**
      * Initialized the installed release.
      *
      */
     private void initializeRelease() {
         migratorHelper.initializeRelease(new ProcessAdapter() {});
     }
 
     /**
      * Perform first run initialization.
      * 
      */
     private void initializeWorkspace() {
         firstRunHelper.firstRun();
     }
 
     /**
      * Invoke an action.
      * 
      * @param actionId
      *            The action id.
      * @param data
      *            The action data.
      */
     private void invoke(final ActionId actionId, final Data data) {
         getAction(actionId).invokeAction(this, data);
     }
 
     /**
      * Determine whether or not the installed release is initialized.
      * 
      * @return True if the installed release is initialized.
      */
     private boolean isReleaseInitialized() {
         return migratorHelper.isReleaseInitialized();
     }
 
     /**
      * Determine whether or not the installed release is initialized.
      * 
      * @return True if the installed release is initialized.
      */
     private boolean isReleaseInstalled() {
         return migratorHelper.isReleaseInstalled();
     }
 
     /**
      * Determine if the workspace has been initialized.
      * 
      * @return True if the workspace has been initialized.
      */
     private Boolean isWorkspaceInitialized() {
         return WorkspaceModel.getInstance(environment).isInitialized(
                 modelFactory.getWorkspace(getClass()));
     }
 
     /**
      * Lookup an application.
      * 
      * @param id
      *            An <code>ApplicationId</code>.
      * @return An <code>Application</code>.
      */
     private Application lookupApplication(final ApplicationId id) {
         return applicationRegistry.get(id);
     }
 
     /**
      * Lookup an avatar. If requested the avatar can be created if it does not
      * exist.
      * 
      * @param id
      *            An <code>AvatarId</code>.
      * @param create
      *            A <code>boolean</code> indicating whether or not to create
      *            the avatar.
      * @return An <code>Avatar</code> or null if the avatar has not been
      *         created and create is false.
      */
     private Avatar lookupAvatar(final AvatarId id, final boolean create) {
         final Avatar avatar = avatarRegistry.get(id);
         if (null == avatar) {
             if (create) {
                 return AvatarFactory.create(id);
             }
         }
         return avatar;
     }
 
     private void notifyLifeCycleEnded() {
         final LifeCycleEvent e = listenerHelper.createEvent();
         listenerHelper.notifyListeners(new EventNotifier<LifeCycleListener>() {
             public void notifyListener(final LifeCycleListener listener) {
                 listener.ended(e);
             }
         });
     }
 
     private void notifyLifeCycleEnding() {
         final LifeCycleEvent e = listenerHelper.createEvent();
         listenerHelper.notifyListeners(new EventNotifier<LifeCycleListener>() {
             public void notifyListener(final LifeCycleListener listener) {
                 listener.ending(e);
             }
         });
     }
 
     private void notifyLifeCycleStarted() {
         final LifeCycleEvent e = listenerHelper.createEvent();
         listenerHelper.notifyListeners(new EventNotifier<LifeCycleListener>() {
             public void notifyListener(final LifeCycleListener listener) {
                 listener.started(e);
             }
         });
     }
 
     private void notifyLifeCycleStarting() {
         final LifeCycleEvent e = listenerHelper.createEvent();
         listenerHelper.notifyListeners(new EventNotifier<LifeCycleListener>() {
             public void notifyListener(final LifeCycleListener listener) {
                 listener.starting(e);
             }
         });
     }
 
     /**
      * Display an application avatar in a window.
      * 
      * @param applicationId
      *            An <code>ApplicationId</code>.
      * @param windowId
      *            A <code>WindowId</code>.
      * @param avatarId
      *            An <code>AvatarId</code>.
      * @param input
      *            An avatar's input <code>Data</code>.
      */
     private void open(final ApplicationId applicationId,
             final AvatarId avatarId, final Data input) {
         final Application application = lookupApplication(applicationId);
         final AbstractJFrame mainApplicationWindow = application.getMainWindow();
         final Window window = WindowFactory.create(mainApplicationWindow);
         final Avatar avatar = lookupAvatar(avatarId, true);
         avatar.setInput(input);
         window.open(avatar);
     }
 
     /**
      * Display an avatar in a window.
      * 
      * @param windowId
      *            A <code>WindowId</code>.
      * @param avatarId
      *            An <code>AvatarId</code>.
      * @param input
      *            An avatar's input <code>Data</code>.
      */
     private void open(final AvatarId avatarId, final Data data) {
         final Window window = WindowFactory.create();
         final Avatar avatar = lookupAvatar(avatarId, true);
         avatar.setInput(data);
         window.open(avatar);
     }
 
     /**
      * Start all applications.
      *
      */
     private void startApplications() {
         applicationFactory.create(ApplicationId.SYSTEM).start(this);
         applicationFactory.create(ApplicationId.BROWSER).start(this);
         applicationFactory.create(ApplicationId.SESSION).start(this);
     }
 
     /**
      * Initialized the installed release.
      *
      */
     private void startInstallRelease() {
         migratorHelper.startInstallRelease(new ProcessAdapter() {});
     }
 
     /**
      * Start all plugins.
      *
      */
     private void startPlugins() {
         pluginHelper.start();
     }
 
     /**
      * Terminate the browser platform with prejudice.
      *
      */
     private void terminate() {
         closeWorkspace();
         notifyLifeCycleEnded();
         // remove the platform as its own lifecycle listener
         removeListener(this);
     }
 }
