 /*
  * Created On: Aug 6, 2005
  */
 package com.thinkparity.model.parity.model;
 
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 import java.util.UUID;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.thinkparity.codebase.DateUtil;
 import com.thinkparity.codebase.NetworkUtil;
 import com.thinkparity.codebase.StackUtil;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.assertion.Assertion;
 import com.thinkparity.codebase.assertion.NotTrueAssertion;
 import com.thinkparity.codebase.l10n.L18n;
 import com.thinkparity.codebase.log4j.Log4JHelper;
 
 import com.thinkparity.model.ShutdownHook;
 import com.thinkparity.model.Constants.ShutdownHookNames;
 import com.thinkparity.model.Constants.ShutdownHookPriorities;
 import com.thinkparity.model.Constants.ThreadNames;
 import com.thinkparity.model.Constants.Versioning;
 import com.thinkparity.model.parity.ParityErrorTranslator;
 import com.thinkparity.model.parity.ParityException;
 import com.thinkparity.model.parity.ParityUncheckedException;
 import com.thinkparity.model.parity.model.artifact.Artifact;
 import com.thinkparity.model.parity.model.artifact.ArtifactFlag;
 import com.thinkparity.model.parity.model.artifact.ArtifactModel;
 import com.thinkparity.model.parity.model.artifact.ArtifactState;
 import com.thinkparity.model.parity.model.artifact.InternalArtifactModel;
 import com.thinkparity.model.parity.model.audit.AuditModel;
 import com.thinkparity.model.parity.model.audit.InternalAuditModel;
 import com.thinkparity.model.parity.model.contact.ContactModel;
 import com.thinkparity.model.parity.model.contact.InternalContactModel;
 import com.thinkparity.model.parity.model.container.ContainerModel;
 import com.thinkparity.model.parity.model.container.InternalContainerModel;
 import com.thinkparity.model.parity.model.document.DocumentModel;
 import com.thinkparity.model.parity.model.document.InternalDocumentModel;
 import com.thinkparity.model.parity.model.download.DownloadModel;
 import com.thinkparity.model.parity.model.download.InternalDownloadModel;
 import com.thinkparity.model.parity.model.index.IndexModel;
 import com.thinkparity.model.parity.model.index.InternalIndexModel;
 import com.thinkparity.model.parity.model.io.IOFactory;
 import com.thinkparity.model.parity.model.io.db.hsqldb.HypersonicUtil;
 import com.thinkparity.model.parity.model.io.handler.ConfigurationIOHandler;
 import com.thinkparity.model.parity.model.library.InternalLibraryModel;
 import com.thinkparity.model.parity.model.library.LibraryModel;
 import com.thinkparity.model.parity.model.message.system.InternalSystemMessageModel;
 import com.thinkparity.model.parity.model.message.system.SystemMessageModel;
 import com.thinkparity.model.parity.model.release.InternalReleaseModel;
 import com.thinkparity.model.parity.model.release.ReleaseModel;
 import com.thinkparity.model.parity.model.session.Credentials;
 import com.thinkparity.model.parity.model.session.Environment;
 import com.thinkparity.model.parity.model.session.InternalSessionModel;
 import com.thinkparity.model.parity.model.session.SessionModel;
 import com.thinkparity.model.parity.model.user.InternalUserModel;
 import com.thinkparity.model.parity.model.user.TeamMember;
 import com.thinkparity.model.parity.model.user.UserModel;
 import com.thinkparity.model.parity.model.workspace.Preferences;
 import com.thinkparity.model.parity.model.workspace.Workspace;
 import com.thinkparity.model.parity.util.Base64;
 import com.thinkparity.model.parity.util.MD5Util;
 import com.thinkparity.model.util.Localization;
 import com.thinkparity.model.util.LocalizationContext;
 import com.thinkparity.model.xmpp.JabberId;
 import com.thinkparity.model.xmpp.JabberIdBuilder;
 import com.thinkparity.model.xmpp.user.User;
 
 import com.thinkparity.migrator.Library;
 import com.thinkparity.migrator.Release;
 
 /**
  * AbstractModelImpl
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public abstract class AbstractModelImpl {
 
     /**
 	 * Assertion message to be displayed if the username is not set in the
 	 * parity preferences.
 	 */
 	private static final String ASSERT_IS_SET_USERNAME = new StringBuffer()
 		.append("Before you can create the first parity artifact; you will ")
 		.append("need to establish a parity session.").toString();
 
 	/**
 	 * The session model context
 	 * 
 	 * @see #getSessionModelContext()
 	 */
 	private static Context sessionModelContext;
 
 	/** A list of runnables to execute when the JVM shuts down. */
     private static final List<ShutdownHook> shutdownHooks;
 
 	/** A static logger instance. */
     private static final Logger sLogger;
 
     static {
         sLogger = Logger.getLogger(AbstractModelImpl.class);
 
         shutdownHooks = new ArrayList<ShutdownHook>();
         Runtime.getRuntime().addShutdownHook(new Thread(ThreadNames.SHUTDOWN_HOOK) {
             @Override
             public void run() {
                 synchronized(shutdownHooks) {
                     Collections.sort(shutdownHooks);
                     for(final ShutdownHook shutdownHook : shutdownHooks) {
                         sLogger.info(shutdownHook.getPriority() + ":" + shutdownHook.getName());
                         shutdownHook.run();
                     }
                 }
             }
         });
 
         addShutdownHook(new ShutdownHook() {
             @Override
             public String getDescription() {
                 return ShutdownHookNames.HYPERSONIC;
             }
             @Override
             public String getName() {
                 return ShutdownHookNames.HYPERSONIC;
             }
             @Override
             public Integer getPriority() {
                 return ShutdownHookPriorities.HYPERSONIC;
             }
             @Override
             public void run() {
                 HypersonicUtil.shutdown();
             }
         });
     }
 
     /**
      * Add a shutdown hook.
      * 
      * @param hook
      *            A runnable.
      */
     protected static boolean addShutdownHook(final ShutdownHook shutdownHook) {
         if (shutdownHooks.contains(shutdownHook)) {
             return false;
         }
         return shutdownHooks.add(shutdownHook);
     }
 
 	/**
 	 * Obtain the current date\time.
 	 * 
 	 * @return The current date\time.
 	 */
 	protected static Calendar currentDateTime() {
         return DateUtil.getInstance();
 	}
 
 	protected static StringBuffer getModelId(final String model) {
         return new StringBuffer("[LMODEL] [").append(model).append("]");
     }
 
 	/**
 	 * Obtain the session model context.
 	 * 
 	 * @return The session model context.
 	 */
 	protected static Context getSessionModelContext() {
 		if(null == sessionModelContext) {
 			sessionModelContext = new Context(SessionModel.class);
 		}
 		return sessionModelContext;
 	}
 
 	/**
      * Remove a shutdown hook.
      * 
      * @param hook
      *            A runnable.
      */
     protected static boolean removeShutdownHook(final ShutdownHook shutdownHook) {
         if (!shutdownHooks.contains(shutdownHook)) {
             return false;
         }
         return shutdownHooks.remove(shutdownHook);
     }
 
 	/** The configuration io. */
     protected ConfigurationIOHandler configurationIO;
 
 	/**
 	 * The parity model context.
 	 * 
 	 */
 	protected final Context context;
 
     /**
 	 * The model's l18n.
 	 * 
 	 */
 	protected final L18n l18n;
 
 	/**
 	 * Apache logger.
 	 * 
 	 */
 	protected final Logger logger;
 
 	/**
 	 * Handle to the parity model preferences.
 	 */
 	protected final Preferences preferences;
 
     /**
 	 * Handle to the parity model workspace.
 	 */
 	protected final Workspace workspace;
 
     /** The decryption cipher. */
     private transient Cipher decryptionCipher;
 
     /** The encryption cipher. */
     private transient Cipher encryptionCipher;
 
 	/** The secret key spec. */
     private transient SecretKeySpec secretKeySpec;
 
     /**
 	 * Create an AbstractModelImpl
 	 * 
 	 * @param workspace
 	 *            The parity workspace.
 	 */
 	protected AbstractModelImpl(final Workspace workspace) {
 		super();
 		this.context = new Context(getClass());
 		this.l18n = new Localization(LocalizationContext.MODEL);
         this.logger = Logger.getLogger(getClass());
 		this.workspace = workspace;
 		this.preferences = (null == workspace ? null : workspace.getPreferences());
 	}
 
     /**
      * Assert a draft doesn't exist for the container.
      * 
      * @param assertion
      *            An assertion.
      * @param containerId
      *            A container id.
      */
     protected void assertContainerDraftDoesNotExist(final Object assertion, final Long containerId) {
         Assert.assertIsNull(assertion, getInternalContainerModel().readDraft(containerId));
     }
 
     /**
      * Assert a draft exists for the container.
      * 
      * @param assertion
      *            An assertion.
      * @param containerId
      *            A container id.
      */
     protected void assertContainerDraftExists(final Object assertion, final Long containerId) {
         Assert.assertNotNull(assertion, getInternalContainerModel().readDraft(containerId));
     }
 
     /**
      * Assert that a latest version exists.
      * 
      * @param assertion
      *            An assertion <code>Object</code>.
      * @param artifactId
      *            An artifact id <code>Long</code>.
      * @see #doesExistLatestVersion(Long)
      */
     protected void assertDoesExistLatestVersion(final Object assertion,
             final Long artifactId) {
         Assert.assertTrue(assertion, doesExistLatestVersion(artifactId));
     }
 
 	/**
      * Assert that a version exists.
      * 
      * @param assertion
      *            An assertion <code>Object</code>.
      * @param artifactId
      *            An artifact id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @see #doesExistLatestVersion(Long)
      */
     protected void assertDoesExistVersion(final Object assertion,
             final Long artifactId, final Long versionId) {
         Assert.assertTrue(assertion, doesExistVersion(artifactId, versionId));
     }
 
 	/**
      * Assert that the list of team members does not contain the user.
      * 
      * @param assertion
      *            An assertion.
      * @param teamMembers
      *            A list of team members.
      * @param user
      *            A user.
      */
     protected void assertDoesNotContain(final Object assertion,
             final List<TeamMember> teamMembers, final User user) {
         Assert.assertNotTrue(assertion, contains(teamMembers, user));
     }
 
     /**
      * Assert that the artifact is closed.
      * 
      * @param assertion
      *            The assertion.
      * @param artifact
      *            The artifact.
      */
     protected void assertIsClosed(final String assertion,
             final Artifact artifact) {
         Assert.assertTrue(assertion, isClosed(artifact));
     }
 
     /**
      * Assert the user is the key holder. An assertion that the user is online
      * is also made.
      * 
      * @param assertion
      *            The assertion message.
      * @param artifactId
      *            The artifact id.
      * @see #isKeyHolder(Long)
      */
     protected void assertIsKeyHolder(final Object assertion,
             final Long artifactId) throws ParityException {
         Assert.assertTrue(assertion, isKeyHolder(artifactId));
     }
 
 	/**
      * Assert that the logged in user is not the key holder.
      * 
      * @param assertion
      *            The assertion message.
      * @param artifactId
      *            The artifact id.
      * @see #isKeyHolder(Long)
      */
 	protected void assertIsNotKeyHolder(final String assertion,
             final Long artifactId) throws ParityException {
 		Assert.assertNotTrue(assertion, isKeyHolder(artifactId));
 	}
 
     /**
      * Assert that the environment is online.
      * 
      * @param assertion
      *            An assertion.
      * @param environment
      *            An environment.
      */
     protected void assertIsReachable(final Object assertion,
             final Environment environment) {
         Assert.assertTrue(assertion, isReachable(environment));
     }
 
     /**
 	 * Assert that the model framework is initialized to a state where the user
 	 * can start to create artifacts. This requires:
 	 * <ol>
 	 * <li>The user has logged in at least once.</li>
 	 * </ol>
 	 * 
 	 */
 	protected void assertIsSetCredentials() {
 		Assert.assertTrue(ASSERT_IS_SET_USERNAME, isSetCredentials());
 	}
 
     /**
      * Ensure the user is not online.
      * 
      * @param assertion
      *            The assertion.
      */
     protected void assertNotIsOnline(final Object assertion) {
         Assert.assertNotTrue(assertion, isOnline());
     }
 
 	/**
      * Assert that the reference is not null.
      * 
      * @param assertion
      *            An assertion.
      * @param reference
      *            An object reference.
      */
     protected void assertNotNull(final Object assertion, final Object reference) {
         Assert.assertNotNull(assertion, reference);
     }
 
 	/**
      * Assert that the user is not a team member.
      * 
      * @param assertion
      *            An assertion.
      * @param artifactId
      *            An artifact id.
      * @param userId
      *            A user id.
      */
     protected void assertNotTeamMember(final Object assertion, final Long artifactId, final JabberId userId) {
         final List<TeamMember> team = getInternalArtifactModel().readTeam2(artifactId);
         final User user = getInternalUserModel().read(userId);
         if (null != user)
             Assert.assertNotTrue(assertion, contains(team, user));
     }
 
 	/**
      * Assert the user is online.
      *
      * @param assertion
      *      The assertion.
      */
     protected void assertOnline(final String assertion) {
         Assert.assertTrue(assertion, isOnline());
     }
 
     /**
      * Assert the user is online.
      *
      */
     protected void assertOnline() {
         assertOnline("USER NOT ONLINE");
     }
 
     protected void assertOnline(final StringBuffer api) {
         assertOnline(api.toString());
     }
 
     /**
 	 * Assert that the state transition from currentState to newState can be
 	 * made safely.
 	 * 
 	 * @param currentState The artifact's current state.
 	 * @param intentedState
 	 *            The artifact's intended state.
 	 * 
 	 * @throws NotTrueAssertion
 	 *             If the state cannot be moved.
 	 */
 	protected void assertStateTransition(final ArtifactState currentState,
 			final ArtifactState intendedState) {
 		switch(currentState) {
 		case ACTIVE:
 			// i can close it
 			Assert.assertTrue(
 					formatAssertion(currentState, intendedState, new ArtifactState[] {ArtifactState.CLOSED}),
 						ArtifactState.CLOSED == intendedState);
 			break;
 		case CLOSED:
 			// i can reactivate it
 			Assert.assertTrue(
 					formatAssertion(currentState, intendedState, new ArtifactState[] {ArtifactState.ACTIVE}),
 						ArtifactState.ACTIVE == intendedState);
 			break;
 		default: Assert.assertUnreachable("Unknown artifact state:  " + currentState);
 		}
 	}
 
     /**
      * Assert that the user is a team member.
      * 
      * @param assertion
      *            An assertion.
      * @param artifactId
      *            An artifact id.
      * @param userId
      *            A user id.
      */
     protected void assertTeamMember(final Object assertion, final Long artifactId, final JabberId userId) {
         final List<TeamMember> team = getInternalArtifactModel().readTeam2(artifactId);
         Assert.assertNotTrue(assertion, contains(team, getInternalUserModel().read(userId)));
     }
 
     /**
 	 * Build a jabber id from a parity user.
 	 * 
 	 * @param user
 	 *            The parity user.
 	 * @return The jabber id.
 	 */
 	protected JabberId buildJabberId(final User user) {
 		JabberId jabberId = null;
 		try { jabberId =
 			JabberIdBuilder.parseQualifiedJabberId(user.getUsername()); }
 		catch(final IllegalArgumentException iax) {}
 		if(null != jabberId) {
 			try {
 				jabberId =
 					JabberIdBuilder.parseQualifiedUsername(user.getUsername());
 			}
 			catch(final IllegalArgumentException iax) {}
 			if(null != jabberId)
 				jabberId = JabberIdBuilder.parseUsername(user.getUsername());
 		}
 		return jabberId;
 	}
 
     /**
      * Determine if the list of team members contains the user id..
      * 
      * @param team
      *            A <code>List&lt;TeamMember&gt;</code>.
      * @param user
      *            A user id <code>JabberId</code>.
      * @return True if the user id matches one of the team members.
      */
     protected Boolean contains(final List<TeamMember> team,
             final JabberId userId) {
         return -1 != indexOf(team, userId);
     }
 
     /**
      * Determine if the list of team members contains the user.
      * 
      * @param team
      *            A list of team members.
      * @param user
      *            A user.
      * @return True if the id of the user matches one of the team members.
      */
     protected Boolean contains(final List<TeamMember> team, final User user) {
         return -1 != indexOf(team, user);
     }
 
     /**
      * Determine if the list of users contains the team member.
      * 
      * @param users
      *            A list of users.
      * @param teamMember
      *            A team member.
      * @return True if the id of the team member matches one of the users.
      */
     protected Boolean contains(final List<User> users, final TeamMember teamMember) {
         return -1 != indexOf(users, teamMember);
     }
 
     /**
      * Create the user credentials.
      * 
      * @param username
      *            The user's username.
      * @param password
      *            The user's password.
      */
     protected Credentials createCredentials(final String username,
             final String password) {
         final String cipherKey = "18273-4897-12-53974-816523-49-81623-95-4-91-8723-56974812-63498-612395-498-7125-349871265-47892-1539784-1523954-19-287356-4";
         try {
             getConfigurationHandler().create(ConfigurationKeys.USERNAME, encrypt(cipherKey, username));
             getConfigurationHandler().create(ConfigurationKeys.PASSWORD, encrypt(cipherKey, password));
             return readCredentials();
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * Delete the user credentials.
      *
      */
     protected void deleteCredentials() {
         assertIsSetCredentials();
         try {
             getConfigurationHandler().delete(ConfigurationKeys.USERNAME);
             getConfigurationHandler().delete(ConfigurationKeys.PASSWORD);
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * Determine whether or not a latest version exists.
      * 
      * @param artifactId
      *            An artifact id <code>Long</code>.
      * @return True if a version exists; false otherwise.
      */
     protected Boolean doesExistLatestVersion(final Long artifactId) {
         return doesExistVersion(artifactId, Versioning.START);
     }
 
     /**
      * Find the user in a team.
      * 
      * @param team
      *            The team.
      * @param user
      *            The user to look for.
      * @return The team member.
      */
     protected TeamMember get(final List<TeamMember> team, final User user) {
         return team.get(indexOf(team, user));
     }
 
     /**
 	 * Obtain the model's context.
 	 * 
 	 * @return The model's context.
 	 */
 	protected Context getContext() { return context; }
 
 	/**
      * Obtain the internal parity artifact interface.
      * 
      * @return The internal parity artifact interface.
      */
 	protected InternalArtifactModel getInternalArtifactModel() {
 		return ArtifactModel.getInternalModel(context);
 	}
 
     /**
      * Obtain the internal parity audit interface.
      * 
      * @return The internal parity audit interface.
      */
     protected InternalAuditModel getInternalAuditModel() {
 		return AuditModel.getInternalModel(context);
 	}
 
 	/**
      * Obtain the internal thinkParity contact interface.
      * 
      * @return The internal thinkParity contact interface.
      */
     protected InternalContactModel getInternalContactModel() {
         return ContactModel.getInternalModel(context);
     };
 
 	/**
      * Obtain the internal thinkParity container interface.
      * 
      * @return The internal thinkParity container interface.
      */
     protected InternalContainerModel getInternalContainerModel() {
         return ContainerModel.getInternalModel(context);
     }
 
 	/**
      * Obtain the internal parity document interface.
      * 
      * @return The internal parity document interface.
      */
 	protected InternalDocumentModel getInternalDocumentModel() {
 		return DocumentModel.getInternalModel(context);
 	}
 
 	/**
      * Obtain the internal parity download interface.
      *
      * @return The internal parity download interface.
      */
     protected InternalDownloadModel getInternalDownloadModel() {
         return DownloadModel.getInternalModel(context);
     }
 
     protected InternalIndexModel getIndexModel() {
         return IndexModel.getInternalModel(context);
     }
 
     /**
      * Obtain the internal parity library interface.
      *
      * @return The internal parity library interface.
      */
     protected InternalLibraryModel getInternalLibraryModel() {
         return LibraryModel.getInternalModel(context);
     }
 
     /**
      * Obtain the thinkParity internal message interface.
      * 
      * @return The thinkParity internal message interface.
      */
     protected InternalSystemMessageModel getInternalMessageModel() {
         return getInternalSystemMessageModel();
     }
 
     /**
      * Obtain the internal parity release interface.
      *
      * @return The internal parity release interface.
      */
     protected InternalReleaseModel getInternalReleaseModel() {
         return ReleaseModel.getInternalModel(getContext());
     }
 
     /**
      * Obtain the internal parity session interface.
      * 
      * @return The internal parity session interface.
      */
 	protected InternalSessionModel getInternalSessionModel() {
 		return SessionModel.getInternalModel(getContext());
 	}
 
     /**
      * Obtain the internal parity system message interface.
      * 
      * @return The internal parity system message interface.
      */
 	protected InternalSystemMessageModel getInternalSystemMessageModel() {
 		return SystemMessageModel.getInternalModel(context);
 	}
 
     /**
      * Obtain the internal parity user interface.
      * 
      * @return The internal parity user interface.
      */
     protected InternalUserModel getInternalUserModel() {
         return UserModel.getInternalModel(context);
     }
 
     /**
 	 * Obtain the model's localization.
 	 * 
 	 * @return The model's localization.
 	 */
 	protected L18n getL18n() { return l18n; }
 
     protected StringBuffer getLogId(final Library library) {
         if(null == library) { return new StringBuffer("null"); }
         else {
             return new StringBuffer()
                 .append(library.getId())
                 .append(":").append(library.getGroupId())
                 .append(":").append(library.getArtifactId())
                 .append(":").append(library.getVersion())
                 .append(":").append(DateUtil.format(
                         library.getCreatedOn(), DateUtil.DateImage.ISO));
         }
     }
 
     protected StringBuffer getLogId(final Release release) {
         if(null == release) { return new StringBuffer("null"); }
         else {
             return new StringBuffer()
                 .append(release.getId())
                 .append(":").append(release.getGroupId())
                 .append(":").append(release.getArtifactId())
                 .append(":").append(release.getVersion())
                 .append(":").append(DateUtil.format(
                         release.getCreatedOn(), DateUtil.DateImage.ISO));
         }
     }
 
     /**
 	 * @see Localization#getString(String)
 	 * 
 	 */
 	protected String getString(final String localKey) {
 		return l18n.getString(localKey);
 	}
 
     /**
 	 * @see Localization#getString(String, Object[])
 	 * 
 	 */
 	protected String getString(final String localKey, final Object[] arguments) {
 		return l18n.getString(localKey, arguments);
 	}
 
     /**
      * Obtain the index of a user id in a team.
      * 
      * @param team
      *            A team.
      * @param userId
      *            A user id.
      * @return The index of the user id in the list; or -1 if the user does not
      *         exist in the list.
      */
     protected int indexOf(final List<TeamMember> team, final JabberId userId) {
         for (int i = 0; i < team.size(); i++) {
             if (team.get(i).getId().equals(userId)) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Obtain the index of the user in the team.
      * 
      * @param team
      *            The team.
      * @param user
      *            A user.
      * @return The index of the user in the team; or -1 if the user does not
      *         exist in the list.
      */
     protected int indexOf(final List<TeamMember> team, final User user) {
         for (int i = 0; i < team.size(); i++) {
             if (team.get(i).getId().equals(user.getId())) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Determine whether or not the artifact is closed.
      * 
      * @param artifact
      *            The artifact.
      * @return True if the artifact is closed; false otherwise.
      */
     protected Boolean isClosed(final Artifact artifact) {
         return ArtifactState.CLOSED == artifact.getState();
     }
 
     /**
      * Check the local flag; as well as the key holder on the server.
      * 
      * @param artifactId
      *            The artifact id.
      * @return True if the user is the keyholder; false otherwise.
      */
     protected Boolean isKeyHolder(final Long artifactId) throws ParityException {
         assertOnline("USER NOT ONLINE");
         return getInternalArtifactModel().isFlagApplied(artifactId, ArtifactFlag.KEY) &&
             getInternalSessionModel().isLoggedInUserKeyHolder(artifactId);
     }
 
     /**
      * Determine whether or not the user is online.
      *
      * @return True if the user is online; false otherwise.
      */
     protected Boolean isOnline() {
         return getInternalSessionModel().isLoggedIn();
     }
 
     /**
      * Obtain the team member for the local user.
      * 
      * @param artifactId
      *            An artifact id.
      * @return The team member.
      */
     protected TeamMember localTeamMember(final Long artifactId) {
         final List<TeamMember> team = getInternalArtifactModel().readTeam2(artifactId);
         return get(team, localUser());
     }
 
     /**
      * Obtain the local user.
      *
      * @return The current user.
      */
     protected User localUser() {
         final JabberId currentUserId = localUserId();
         if(null == currentUserId) { return null; }
         else { return getInternalUserModel().read(currentUserId); }
     }
 
     /**
 	 * Obtain the local user id.
 	 * 
 	 * @return The jabber id of the local user.
 	 */
 	protected JabberId localUserId() {
         final Credentials credentials = readCredentials();
         if(null == credentials) { return null; }
         else { return JabberIdBuilder.parseUsername(credentials.getUsername()); }
 	}
 
     /**
      * Log the api id of the caller.
      *
      */
     protected void logApiId() {
         if(logger.isInfoEnabled()) {
             logger.info(MessageFormat.format("{0}#{1}",
                     StackUtil.getCallerClassName(),
                     StackUtil.getCallerMethodName()));
         }
     }
 
     /**
      * Log a variable.  Note that only the variable value will be rendered.
      * 
      * @param name
      *            The variable name.
      * @param value
      *            The variable value.
      */
     protected void logVariable(final String name, final Object value) {
         if(logger.isDebugEnabled()) {
             logger.debug(MessageFormat.format("{0}:{1}",
                     name,
                     Log4JHelper.render(logger, value)));
         }
     }
 
     /**
      * Log a warning message.
      * 
      * @param message A warning message.
      */
     protected void logWarning(final Object message) {
         if(Level.WARN.isGreaterOrEqual(logger.getEffectiveLevel())) {
             logger.warn(Log4JHelper.render(logger, message));
         }
     }
 
     /**
      * Log a warning with a error.
      * 
      * @param message
      *            A warning message.
      * @param t
      *            An error.
      */
     protected void logWarning(final Object message, final Throwable t) {
         if(Level.WARN.isGreaterOrEqual(logger.getEffectiveLevel())) {
             logger.warn(Log4JHelper.render(logger, message), t);
         }
     }
 
     /**
      * Read the artifact unique id.
      * 
      * @param artifactId
      *            The artifact id.
      * @return The artifact unique id.
      */
     protected UUID readArtifactUniqueId(final Long artifactId) {
         return getInternalArtifactModel().readUniqueId(artifactId);
     }
 
     /**
      * Read the credentials from the configuration.
      * 
      * @return The user's credentials.
      */
     protected Credentials readCredentials() {
         final String cipherKey = "18273-4897-12-53974-816523-49-81623-95-4-91-8723-56974812-63498-612395-498-7125-349871265-47892-1539784-1523954-19-287356-4";
         final String username = getConfigurationHandler().read(ConfigurationKeys.USERNAME);
         final String password = getConfigurationHandler().read(ConfigurationKeys.PASSWORD);
 
         if (null == username || null == password) {
             return null;
         } else {
             final Credentials credentials = new Credentials();
             try {
                 credentials.setPassword(decrypt(cipherKey, password));
                 credentials.setUsername(decrypt(cipherKey, username));
                 return credentials;
             }
             catch(final Throwable t) { throw translateError(t); }
         }
     }
 
     /**
      * Read the next version id.
      * 
      * @param containerId
      *            A container id.
      * @return The next version id.
      */
     protected Long readNextVersionId(final Long artifactId) {
         final Long latestVersionId = getInternalArtifactModel().readLatestVersionId(artifactId);
         return null == latestVersionId ? Versioning.START : latestVersionId + Versioning.INCREMENT;
     }
 
     /**
      * Translate an error into a parity unchecked error.
      * 
      * @param t
      *            An error.
      */
     protected RuntimeException translateError(final Throwable t) {
         if (ParityUncheckedException.class.isAssignableFrom(t.getClass())) {
             return (ParityUncheckedException) t;
         } else if (Assertion.class.isAssignableFrom(t.getClass())) {
             return (Assertion) t;
         }
         else {
             final Object errorId = getErrorId(t);
             logger.error(errorId, t);
             return ParityErrorTranslator.translateUnchecked(context, errorId, t);
         }
     }
 
     /**
      * Update the credentials from the configuration.
      * 
      * @param credentials
      *            The user's credentials.
      */
     protected void updateCredentials(final Credentials credentials) {
         final String cipherKey = "18273-4897-12-53974-816523-49-81623-95-4-91-8723-56974812-63498-612395-498-7125-349871265-47892-1539784-1523954-19-287356-4";
         try {
             getConfigurationHandler().update(ConfigurationKeys.USERNAME, encrypt(cipherKey, credentials.getUsername()));
             getConfigurationHandler().update(ConfigurationKeys.PASSWORD, encrypt(cipherKey, credentials.getPassword()));
         }
         catch(final Throwable t) { throw translateError(t); }
     }
 
     /**
      * Decrypt the cipher text into clear text using the cipher key.
      * 
      * @param cipherKey
      *            The cipher key.
      * @param cipherText
      *            The cipher text.
      * @return The clear text.
      * @throws BadPaddingException
      * @throws IOException
      * @throws IllegalBlockSizeException
      * @throws InvalidKeyException
      * @throws NoSuchAlgorithmException
      * @throws NoSuchPaddingException
      */
     private String decrypt(final String cipherKey, final String cipherText)
             throws BadPaddingException, IOException, IllegalBlockSizeException,
             InvalidKeyException, NoSuchAlgorithmException,
             NoSuchPaddingException {
         final Cipher cipher = getDecryptionCipher();
         return new String(cipher.doFinal(Base64.decodeBytes(cipherText)));
     }
     /**
      * Determine whether or not a version exists.
      * 
      * @param artifactId
      *            An artifact id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @return True if a version exists; false otherwise.
      */
     private Boolean doesExistVersion(final Long artifactId, final Long versionId) {
         return getInternalArtifactModel().doesVersionExist(artifactId, versionId);
     }
 
     /**
      * Encrypt clear text into a base 64 encoded cipher text.
      * 
      * @param cipherKey
      *            The cipher key
      * @param clearText
      *            The clean text to encrypt.
      * @return The cipher text.
      * @throws BadPaddingException
      * @throws IllegalBlockSizeException
      * @throws InvalidKeyException
      * @throws NoSuchAlgorithmException
      * @throws NoSuchPaddingException
      */
     private String encrypt(final String cipherKey, final String clearText)
             throws BadPaddingException, IOException, IllegalBlockSizeException,
             InvalidKeyException, NoSuchAlgorithmException,
             NoSuchPaddingException {
         final Cipher cipher = getEncryptionCipher();
         return Base64.encodeBytes(cipher.doFinal(clearText.getBytes()));
     }
 
     private String formatAssertion(final ArtifactState currentState,
 			final ArtifactState intendedState,
 			final ArtifactState[] allowedStates) {
 		final StringBuffer assertion =
 			new StringBuffer("Cannot move artifact state.  ")
 			.append("Current State:  ").append(currentState)
 			.append("  Attempted State:  ").append(intendedState)
 			.append("  Allowed State(s):  ");
 		int index = 0;
 		for(final ArtifactState allowedState: allowedStates) {
 			if(0 != index++) { assertion.append(","); }
 			assertion.append(allowedState.toString());
 		}
 		return assertion.toString();
 	}
 
     /**
      * Obtain the configuration io interface.
      * 
      * @return The configuraion io interface.
      */
     private ConfigurationIOHandler getConfigurationHandler() {
         if(null == configurationIO) {
             configurationIO = IOFactory.getDefault().createConfigurationHandler();
         }
         return configurationIO;
     }
 
     /**
      * Obtain the decryption cipher; creating it if necessary.
      * 
      * @return A decryption cipher.
      * @throws IOException
      * @throws InvalidKeyException
      * @throws NoSuchAlgorithmException
      * @throws NoSuchPaddingException
      */
     private Cipher getDecryptionCipher() throws IOException,
             InvalidKeyException, NoSuchAlgorithmException,
             NoSuchPaddingException {
         if(null == decryptionCipher) {
             decryptionCipher = Cipher.getInstance("AES");
             decryptionCipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec());
         }
         return decryptionCipher;
     }
 
     /**
      * Obtain the encryption cipher; creating it if need be.
      * 
      * @return The encryption cipher.
      * @throws InvalidKeyException
      * @throws NoSuchAlgorithmException
      * @throws NoSuchPaddingException
      */
     private Cipher getEncryptionCipher() throws IOException,
             InvalidKeyException, NoSuchAlgorithmException,
             NoSuchPaddingException {
         if(null == encryptionCipher) {
             encryptionCipher = Cipher.getInstance("AES");
             encryptionCipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec());
         }
         return encryptionCipher;
     }
 
     /**
      * Obtain an error id.
      * 
      * @return An error id.
      */
     private Object getErrorId(final Throwable t) {
        return MessageFormat.format("[{0}] [{1}] - [{2}]",
                     StackUtil.getFrameClassName(2).toUpperCase(),
                     StackUtil.getFrameMethodName(2).toUpperCase(),
                     t.getMessage());
     }
 
     /**
      * Obtain the secret key; creating it if necessary.
      * 
      * @return The secret key.
      * @throws IOException
      * @throws NoSuchAlgorithmException
      */
     private SecretKeySpec getSecretKeySpec() throws IOException,
             NoSuchAlgorithmException {
         if(null == secretKeySpec) {
             final byte[] rawKey = MD5Util.md5("010932671-023769081237450981735098127-1280397-181-2387-6581972689-1728-9671-8276-892173-5971283-751-239875-182735-98712-85971-2897-867-9823-56823165-8365-89236-987-214981265-9-9-65623-5896-35-3296-289-65893-983-932-5928734-302894719825-99181-28497612-8375".getBytes());
             secretKeySpec = new SecretKeySpec(rawKey, "AES");
         }
         return secretKeySpec;
     }
 
     /**
      * Obtain the index of a team member in a user list.
      * 
      * @param users
      *            A user list.
      * @param teamMember
      *            A team member.
      * @return The index of the team member in the users list or -1 if the team
      *         member does not exist in the list.
      */
     private int indexOf(final List<User> users, final TeamMember teamMember) {
         for(int i = 0; i < users.size(); i++) {
             if(users.get(i).getId().equals(teamMember.getId())) { return i; }
         }
         return -1;
     }
 
     /**
      * Determine whether the environment is online.
      * 
      * @param environment
      *            An environment.
      * @return True if the environment is reachable; false otherwise.
      */
     private Boolean isReachable(final Environment environment) {
         return NetworkUtil.isTargetReachable(
                 environment.getServerHost(), environment.getServerPort());
     }
 
     /**
      * Determine whether or not the user's credentials have been set.
      * 
      * @return True if the credentials have been set; false otherwise.
      */
     private Boolean isSetCredentials() { return null != readCredentials(); }
 
     /** Configuration keys. */
     private static class ConfigurationKeys {
         private static final String PASSWORD = "PASSWORD";
         private static final String USERNAME = "USERNAME";
     }
 }
