 /*
  * Created On: Jan 18, 2006
  */
 package com.thinkparity.ophelia.browser.application.browser;
 
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 import org.apache.log4j.Logger;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.user.TeamMember;
 import com.thinkparity.codebase.swing.JFileChooserUtil;
 import com.thinkparity.codebase.swing.SwingUtil;
 
 import com.thinkparity.ophelia.browser.Constants.Keys;
 import com.thinkparity.ophelia.browser.application.AbstractApplication;
 import com.thinkparity.ophelia.browser.application.browser.display.DisplayId;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.AvatarId;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.MainStatusAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.MainTitleAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.ConfirmAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.ErrorAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.ErrorDetailsAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.contact.UserInfoAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.ContainerVersionCommentAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.CreateContainerAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishContainerAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.RenameContainerAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.RenameDocumentAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.profile.VerifyEMailAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabListAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.archive.ArchiveTabAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.contact.ContactTabAvatar;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.container.ContainerTabAvatar;
 import com.thinkparity.ophelia.browser.application.browser.window.WindowFactory;
 import com.thinkparity.ophelia.browser.application.browser.window.WindowId;
 import com.thinkparity.ophelia.browser.platform.Platform;
 import com.thinkparity.ophelia.browser.platform.Platform.Connection;
 import com.thinkparity.ophelia.browser.platform.action.*;
 import com.thinkparity.ophelia.browser.platform.action.artifact.ApplyFlagSeen;
 import com.thinkparity.ophelia.browser.platform.action.artifact.RemoveFlagSeen;
 import com.thinkparity.ophelia.browser.platform.action.contact.AcceptIncomingInvitation;
 import com.thinkparity.ophelia.browser.platform.action.contact.CreateIncomingInvitation;
 import com.thinkparity.ophelia.browser.platform.action.contact.DeclineIncomingInvitation;
 import com.thinkparity.ophelia.browser.platform.action.contact.Delete;
 import com.thinkparity.ophelia.browser.platform.action.contact.DisplayContactInvitationInfo;
 import com.thinkparity.ophelia.browser.platform.action.contact.Read;
 import com.thinkparity.ophelia.browser.platform.action.container.*;
 import com.thinkparity.ophelia.browser.platform.action.document.Open;
 import com.thinkparity.ophelia.browser.platform.action.document.OpenVersion;
 import com.thinkparity.ophelia.browser.platform.action.document.UpdateDraft;
 import com.thinkparity.ophelia.browser.platform.action.profile.AddEmail;
 import com.thinkparity.ophelia.browser.platform.action.profile.RemoveEmail;
 import com.thinkparity.ophelia.browser.platform.action.profile.Update;
 import com.thinkparity.ophelia.browser.platform.action.profile.UpdatePassword;
 import com.thinkparity.ophelia.browser.platform.action.profile.VerifyEmail;
 import com.thinkparity.ophelia.browser.platform.application.ApplicationId;
 import com.thinkparity.ophelia.browser.platform.application.ApplicationStatus;
 import com.thinkparity.ophelia.browser.platform.application.L18nContext;
 import com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar;
 import com.thinkparity.ophelia.browser.platform.application.window.Window;
 import com.thinkparity.ophelia.browser.platform.plugin.extension.TabListExtension;
 import com.thinkparity.ophelia.browser.platform.plugin.extension.TabPanelExtension;
 import com.thinkparity.ophelia.browser.platform.util.State;
 import com.thinkparity.ophelia.browser.platform.util.persistence.Persistence;
 import com.thinkparity.ophelia.browser.platform.util.persistence.PersistenceFactory;
 import com.thinkparity.ophelia.model.events.ContainerEvent;
 
 /**
  * The controller is used to manage state as well as control display of the
  * parity browser.
  * 
  * 
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public class Browser extends AbstractApplication {
 
     /** Action registry. */
 	private final ActionRegistry actionRegistry;
 
 	/**
 	 * Provides a map of all avatar input.
 	 * 
 	 */
 	private final Map<AvatarId, Object> avatarInputMap;
 
 	/** The browser's connection. */
     private Connection connection;
 
 	/** The browser controller's display helper. */
     private final BrowserDisplayHelper displayHelper;
 
     /** The browser's event dispatcher. */
 	private EventDispatcher ed;
 	
 	/**
 	 * The file chooser.
 	 * 
 	 * @see #getJFileChooser()
 	 */
 	private JFileChooser jFileChooser;
 
 	/** The thinkParity browser application window. */
 	private BrowserWindow mainWindow;
 
 	/** A persistence for browser settings. */
     private final Persistence persistence;
     
     /** The browser's session implementation. */
 	private final BrowserSessionImpl sessionImpl;
 
 	/**
 	 * Create Browser.
 	 * 
 	 */
 	public Browser(final Platform platform) {
 		super(platform, L18nContext.BROWSER2);
 		this.actionRegistry = new ActionRegistry();
 		this.avatarInputMap = new Hashtable<AvatarId, Object>(AvatarId.values().length, 1.0F);
         this.displayHelper = new BrowserDisplayHelper(this);
         this.persistence = PersistenceFactory.getPersistence(getClass());
 		this.sessionImpl = new BrowserSessionImpl();
 	}
 
     /**
      * Apply a search expression to a given tab.
      * 
      * @param expression
      *            The search expression <code>String</code>.
      */
 	public void applySearch(final String expression) {
 	    final Data data = new Data(1);
 	    if (null != expression) {
 	        data.set(TabListAvatar.DataKey.SEARCH_EXPRESSION, expression);
         }
 	    setInput(AvatarId.TAB_ARCHIVE, data);
 	    setInput(AvatarId.TAB_CONTACT, data);
 	    setInput(AvatarId.TAB_CONTAINER, data);
         switch(getMainTitleAvatarTab()) {
         case ARCHIVE:
             break;
         case CONTACT:
             runDisplayContactInvitationInfo(expression);
             break;
         case CONTAINER:
             runDisplayContainerSeenFlagInfo(expression);
             break;
         default:
             Assert.assertUnreachable("Unknown main title tab id.");
         }
 	}
 
     /** Close the main window. */
     public void closeBrowserWindow() {
         Assert.assertNotNull(mainWindow, "Main window is null.");
         mainWindow.dispatchEvent(new WindowEvent(mainWindow, WindowEvent.WINDOW_CLOSING));
     }
 
     /**
      * Collapse the contact.
      *
      * @param contactId
      *            The contact id.      
      */
     public void collapseContact(final JabberId contactId) {
         getTabContactAvatar().collapseContact(contactId);
     }
 
     /**
      * Collapse the container.
      *
      * @param containerId
      *            The containerId.
      * @param archiveTab
      *            true for archive tab, false for container tab.        
      */
     public void collapseContainer(final Long containerId, final Boolean archiveTab) {
         if (archiveTab) {
             getTabArchiveAvatar().collapseContainer(containerId);
         } else {
             getTabContainerAvatar().collapseContainer(containerId);
         }
     }
 
     /**
      * Open a confirmation dialog.
      * 
      * @param messageKey
      *            The confirmation message localization key.
      * @return True if the user confirmed in the affirmative.
      */
     public Boolean confirm(final String messageKey) {
         final Data input = new Data(1);
         input.set(ConfirmAvatar.DataKey.MESSAGE_KEY, messageKey);
         return confirm(input);
     }
 
     /**
      * Open a confirmation dialog.
      * 
      * @param messageKey
      *            The confirmation message localization key.
      * @param messageArguments
      *            A list of confirmation message arguments.
      * @return True if the user confirmed in the affirmative.
      */
     public Boolean confirm(final String messageKey,
             final Object[] messageArguments) {
         final Data input = new Data(2);
         input.set(ConfirmAvatar.DataKey.MESSAGE_KEY, messageKey);
         input.set(ConfirmAvatar.DataKey.MESSAGE_ARGUMENTS, messageArguments);
         return confirm(input);
     }
 
     /**
      * Open a confirmation dialog.
      * 
      * @param confirmButtonKey
      *            The confirm button text key <code>String</code>.
      * @param denyButtonKey
      *            The deny button text key <code>String</code>.
      * @param messageKey
      *            The message key <code>String</code>.
      * @param messageArguments
      *            The message arguments <code>Object[]</code>.
      * @return True if the user confirms the response.
      */
     public Boolean confirm(final String confirmButtonKey,
             final String denyButtonKey, final String messageKey,
             final Object... messageArguments) {
         final Data input = new Data(4);
         input.set(ConfirmAvatar.DataKey.CONFIRM_BUTTON_KEY, confirmButtonKey);
         input.set(ConfirmAvatar.DataKey.DENY_BUTTON_KEY, denyButtonKey);
         input.set(ConfirmAvatar.DataKey.MESSAGE_KEY, messageKey);
         input.set(ConfirmAvatar.DataKey.MESSAGE_ARGUMENTS, messageArguments);
         return confirm(input);
 
     }
 
     /**
      * Open a confirm dialog using an already localized message.
      * 
      * @param localizedMessage
      *            A localized message.
      * @return True if the user confirmed in the affirmative.
      */
     public Boolean confirmLocalized(final String localizedMessage) {
         final Data input = new Data(1);
         input.set(ConfirmAvatar.DataKey.LOCALIZED_MESSAGE, localizedMessage);
         return confirm(input);
     }
 
     /**
      * Display the archive tab avatar.
      *
      */
     public void displayArchiveTabAvatar() {
         displayTab(AvatarId.TAB_ARCHIVE);
     }
 
     /**
      * Display the invite dialogue.
      *
      */   
     public void displayContactCreateInvitation() {
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_CONTACT_CREATE_OUTGOING_INVITATION);
     }
 
     /**
      * Display the contact tab avatar.
      *
      */
     public void displayContactTabAvatar() {
         displayTab(AvatarId.TAB_CONTACT);
     }
     
     /**
      * Display the container tab avatar.
      *
      */
     public void displayContainerTabAvatar() {
         displayTab(AvatarId.TAB_CONTAINER);
     }
 
     /**
      * Display the container version comment dialog.
      * 
      * @param containerId
      *            The container id.
      * @param versionId
      *            A version id.        
      */
     public void displayContainerVersionInfoDialog(final Long containerId, final Long versionId) {
         final Data input = new Data(2);
         input.set(ContainerVersionCommentAvatar.DataKey.CONTAINER_ID, containerId);
         input.set(ContainerVersionCommentAvatar.DataKey.VERSION_ID, versionId);
         setInput(AvatarId.DIALOG_CONTAINER_VERSION_COMMENT, input);
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_CONTAINER_VERSION_COMMENT);
     }
         
     /**
      * Display the "new container" dialog (to create new packages).
      * If the user presses OK, runCreateContainer() is called and
      * provided with the container name.
      * 
      */
     public void displayCreateContainerDialog() {
         final Integer numFiles = 0;
         final Data input = new Data(1);
         input.set(CreateContainerAvatar.DataKey.NUM_FILES, numFiles);
         setInput(AvatarId.DIALOG_CONTAINER_CREATE, input);
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_CONTAINER_CREATE);
     }
     
     /**
      * Display the "new container" dialog (to create new packages).
      * If the user presses OK, runCreateContainer() is called and
      * provided with the container name.
      * This version has a list of files that will be added after the
      * container is created.
      * 
      * @param files
      *          List of files that will be added later
      */    
     public void displayCreateContainerDialog(final List<File> files) {
         final Integer numFiles = files.size();
         final Data input = new Data(2);
         input.set(CreateContainerAvatar.DataKey.NUM_FILES, numFiles);
         input.set(CreateContainerAvatar.DataKey.FILES, files);        
         setInput(AvatarId.DIALOG_CONTAINER_CREATE, input);
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_CONTAINER_CREATE);
     }
     
     /**
      * Handle a user error (show an error dialog).
      * 
      * @param errorMessageKey
      *            The error message localization key.
      */
     public void displayErrorDialog(final String errorMessageKey) {
         displayErrorDialog(errorMessageKey, null, null);
     }
 
     /**
      * Display an error dialog
      * 
      * @param errorMessageKey
      *            The error message localization key.
      * @param errorMessageArguments
      *            The error message arguments.
      */
     public void displayErrorDialog(final String errorMessageKey,
             final Object[] errorMessageArguments) {
         displayErrorDialog(errorMessageKey, errorMessageArguments, null);
     }
 
     /**
      * Display an error dialog
      * 
      * @param errorMessageKey
      *            The error message localization key (optional).
      * @param errorMessageArguments
      *            The error message arguments (optional).
      * @param error
      *            An error (optional).     
      */
     public void displayErrorDialog(final String errorMessageKey,
             final Object[] errorMessageArguments,
             final Throwable error) {
         if ((null != error) && (getPlatform().isDevelopmentMode() || getPlatform().isTestingMode())) {
             displayErrorDetailsDialog(errorMessageKey, errorMessageArguments, error);
         } else {        
             final Data input = new Data(3);
             if (null != errorMessageKey)
                 input.set(ErrorAvatar.DataKey.ERROR_MESSAGE_KEY, errorMessageKey);
             if (null != errorMessageArguments)
                 input.set(ErrorAvatar.DataKey.ERROR_MESSAGE_ARGUMENTS, errorMessageArguments);
             if (null != error) {
                 input.set(ErrorAvatar.DataKey.ERROR, error);
             }
             open(WindowId.ERROR, AvatarId.DIALOG_ERROR, input);
         }
     }
 
     /**
      * Display an error dialog.
      * 
      * @param error
      *            An error.
      */
     public void displayErrorDialog(final Throwable error) {
         displayErrorDialog(null, null, error);
     }
 
     /**
      * Display the info (or Help About) dialog.
      */
     public void displayInfoDialog() {
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_PLATFORM_DISPLAY_INFO);
     }
 
     /**
      * Display the "publish container" dialog.
      * If the user presses OK, the CONTAINER_PUBLISH action is invoked.
      * 
      * @param containerId
      *            The container id.        
      */
     public void displayPublishContainerDialog(final Long containerId) {
         final Data input = new Data(3);
         input.set(PublishContainerAvatar.DataKey.PUBLISH_TYPE, PublishContainerAvatar.PublishType.PUBLISH);
         input.set(PublishContainerAvatar.DataKey.CONTAINER_ID, containerId);
         setInput(AvatarId.DIALOG_CONTAINER_PUBLISH, input);
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_CONTAINER_PUBLISH);
     }
 
     /**
      * Display the "publish container" dialog for a version.
      * If the user presses OK, the CONTAINER_PUBLISH_VERSION action is invoked.
      * 
      * @param containerId
      *            The container id.
      * @param versionId
      *            A version id.
      */
     public void displayPublishContainerDialog(final Long containerId,
             final Long versionId) {
         final Data input = new Data(4);
         input.set(PublishContainerAvatar.DataKey.PUBLISH_TYPE, PublishContainerAvatar.PublishType.PUBLISH_VERSION);
         input.set(PublishContainerAvatar.DataKey.CONTAINER_ID, containerId);
         input.set(PublishContainerAvatar.DataKey.VERSION_ID, versionId);
         setInput(AvatarId.DIALOG_CONTAINER_PUBLISH, input);
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_CONTAINER_PUBLISH);
     }
     
     /**
      * Display the contact info dialogue.
      *
      * @param contactId
      *            A contact id.
      */
     public void displayReadContactDialog(JabberId contactId) {
         final Data input = new Data(1);
         input.set(UserInfoAvatar.DataKey.USER_ID, contactId);
         setInput(AvatarId.DIALOG_CONTACT_READ, input);
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_CONTACT_READ);        
     }
     
     /**
      * Display a container rename dialog.
      * 
      * @param containerId
      *            The container id.
      * @param containerName
      *            A container name.
      */
     public void displayRenameContainerDialog(final Long containerId,
             final String containerName) {
         final Data input = new Data(2);
         input.set(RenameContainerAvatar.DataKey.CONTAINER_ID, containerId);
         input.set(RenameContainerAvatar.DataKey.CONTAINER_NAME, containerName);
         setInput(AvatarId.DIALOG_CONTAINER_RENAME, input);
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_CONTAINER_RENAME);
     }
 
     /**
      * Display a document rename dialog.
      * 
      * @param containerId
      *            The container id.
      * @param documentId
      *            A document id.
      * @param documentName
      *            A document name.
      */
     public void displayRenameDocumentDialog(final Long containerId,
             final Long documentId, final String documentName) {
         final Data input = new Data(3);
         input.set(RenameDocumentAvatar.DataKey.CONTAINER_ID, containerId);
         input.set(RenameDocumentAvatar.DataKey.DOCUMENT_ID, documentId);
         input.set(RenameDocumentAvatar.DataKey.DOCUMENT_NAME, documentName);
         setInput(AvatarId.DIALOG_CONTAINER_RENAME_DOCUMENT, input);
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_CONTAINER_RENAME_DOCUMENT);
     }
 
     /**
      * Display the update profile dialog.
      *
      */
     public void displayResetProfilePasswordDialog() {
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_PROFILE_RESET_PASSWORD);
     }
 
     /** Display a tab list extension. */
     public void displayTabExtension(final TabListExtension tabListExtension) {
         displayTab(tabListExtension);
     }
     
     /** Display a tab panel extension. */
     public void displayTabExtension(final TabPanelExtension tabPanelExtension) {
         displayTab(tabPanelExtension);
     }
 
     /**
      * Display the update password dialog.
      */
     public void displayUpdatePasswordDialog() {
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_PROFILE_UPDATE_PASSWORD);
     }
     
     /**
      * Display the update profile dialog.
      *
      */
     public void displayUpdateProfileDialog() {
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_PROFILE_UPDATE);
     }
 
     /**
      * Display the verify profile email dialog.
      * 
      * @param emailId
      *            An email id <code>Long</code>.
      */
     public void displayVerifyProfileEmailDialog(final Long emailId) {
         final Data input = new Data(1);
         input.set(VerifyEMailAvatar.DataKey.EMAIL_ID, emailId);
         setInput(AvatarId.DIALOG_PROFILE_VERIFY_EMAIL, input);
         displayAvatar(WindowId.POPUP, AvatarId.DIALOG_PROFILE_VERIFY_EMAIL);        
     }
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.application.Application#end()
 	 * 
 	 */
 	public void end(final Platform platform) {
 		logApiId();
 		assertStatusChange(ApplicationStatus.ENDING);
 
 		ed.end();
 		ed = null;
 
 		if (isBrowserWindowOpen()) {
             closeBrowserWindow();
             disposeBrowserWindow();
 		}
 
         setStatus(ApplicationStatus.ENDING);        
 		notifyEnd();
 	}
 
     /**
      * Expand the contact.
      *
      * @param contactId
      *            The contact id.       
      */
     public void expandContact(final JabberId contactId) {
         getTabContactAvatar().expandContact(contactId);
     }
 
     /**
      * Expand the container.
      *
      * @param containerId
      *            The container id.
      * @param archiveTab
      *            true for archive tab, false for container tab.        
      */
     public void expandContainer(final Long containerId, final Boolean archiveTab) {
         if (archiveTab) {
             getTabArchiveAvatar().expandContainer(containerId);
         } else {
             getTabContainerAvatar().expandContainer(containerId);
         }
     }
 
     /**
      * Notify the application that an actin has been invoked.
      */
     public void fireActionInvoked() {
         clearStatus();
     }
 
     /**
      * Notify the application that the "seen" flag has been updated.
      * 
      * @param artifactId
      *            The artifact id.
      */
     public void fireArtifactSeenFlagUpdated(final Long artifactId) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContainerAvatar().fireSeenFlagUpdated(artifactId);
             }
         });    
     }
 
     /**
      * Notify the application that a contact has been added.
      * 
      * @param contactId
      *            The contact id.
      * @param remote
      *            True if the action was the result of a remote event; false if
      *            the action was a local event.  
      */
     public void fireContactAdded(final JabberId contactId, final Boolean remote) {
         // refresh the contact list
         SwingUtilities.invokeLater(new Runnable() {
             public void run() { getTabContactAvatar().syncContact(contactId, remote); }
         });
     }
     
     /**
      * Notify the application that a contact has been deleted.
      * 
      * @param contactId
      *           The contact id.
      */
     public void fireContactDeleted(final JabberId contactId,
             final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncContact(contactId, remote);
             }
         });
     }
 
     /**
      * Notify the application that a contact has been updated.
      * 
      * @param contactId
      *           The contact id.
      */
     public void fireContactUpdated(final JabberId contactId) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncContact(contactId, Boolean.TRUE);
             }
         });
     }
 
     /**
      * Handle the container restored event.
      * 
      * @param e
      *            A <code>ContainerEvent</code>.
      */
     public void fireContainerRestored(final ContainerEvent e) {
         if (!e.getContainer().isSeen())
             runApplyContainerFlagSeen(e.getContainer().getId());
     }
 
     /**
      * Notify the browser that a document draft has been updated (ie. documents changed)
      * 
      * @param documentId
      *            A document id.
      */
     public void fireDocumentDraftUpdated(final Long documentId) {
         syncDocumentTabContainer(documentId, Boolean.FALSE);
     }
 
     /**
      * Notify the application that a document has in some way been updated.
      *
      * @param containerId
      *            The container id.
      * @param documentId
      *            The document that has changed.
      */
     public void fireDocumentUpdated(final Long documentId, final Boolean remote) {
         syncDocumentTabContainer(documentId, remote);
     }
     
     /**
      * Notify the application that an incoming contact invitation has been
      * accepted.
      * 
      * @param contactId
      *            A contact id.
      * @param invitationId
      *            An invitation id.
      * @param remote
      *            A remote event indicator.
      */
     public void fireIncomingContactInvitationAccepted(final JabberId contactId,
             final Long invitationId, final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncContact(contactId, remote);
                 getTabContactAvatar().syncIncomingInvitation(invitationId, remote);
             }
         });
     }
 
     /**
      * Notify the application than an incoming contact invitation has been
      * created.
      * 
      * @param invitationId
      *            An invitation id.
      * @param remote
      *            True if the notification is the result of a remote event.
      */
     public void fireIncomingContactInvitationCreated(final Long invitationId,
             final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncIncomingInvitation(invitationId, remote);
             }
         });
     }
 
     /**
      * Notify the application that an incoming contact invitation has been
      * accepted.
      * 
      * @param contactId
      *            A contact id.
      * @param invitationId
      *            An invitation id.
      * @param remote
      *            A remote event indicator.
      */
     public void fireIncomingContactInvitationDeclined(final Long invitationId,
             final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncIncomingInvitation(invitationId, remote);
             }
         });
     }
 
     /**
      * Notify the application that an incoming contact invitation has been
      * deleted.
      * 
      * @param contactId
      *            A contact id.
      * @param invitationId
      *            An invitation id.
      * @param remote
      *            A remote event indicator.
      */
     public void fireIncomingContactInvitationDeleted(final Long invitationId,
             final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncIncomingInvitation(invitationId, remote);
             }
         });
     }
 
     /**
      * Notify the application that an outgoing invitation has been accepted.
      * 
      * @param contactId
      *            A contact id.
      * @param invitationId
      *            An invitation id.
      * @param remote
      *            A remote event indicator.
      */
     public void fireOutgoingContactInvitationAccepted(final JabberId contactId,
             final Long invitationId, final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncContact(contactId, remote);
                 getTabContactAvatar().syncOutgoingInvitation(invitationId, remote);
             }
         });
     }
 
     /**
      * Notify the application than an outgoing contact invitation has been
      * created.
      * 
      * @param invitationId
      *            An invitation id.
      * @param remote
      *            True if the notification is the result of a remote event.
      */
     public void fireOutgoingContactInvitationCreated(final Long invitationId,
             final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncOutgoingInvitation(invitationId, remote);
             }
         });
     }
 
     /**
      * Notify the application than an outgoing contact invitation has been
      * created.
      * 
      * @param invitationId
      *            An invitation id.
      * @param remote
      *            True if the notification is the result of a remote event.
      */
     public void fireOutgoingContactInvitationDeclined(final Long invitationId,
             final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncOutgoingInvitation(invitationId, remote);
             }
         });
     }
 
     /**
      * Notify the application that an outgoing contact invitation has been
      * deleted.
      * 
      * @param invitationId
      *            An invitation id.
      * @param remote
      *            True if the notification is the result of a remote event.
      */
     public void fireOutgoingContactInvitationDeleted(final Long invitationId,
             final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncOutgoingInvitation(invitationId, remote);
             }
         });
     }
 
     /**
      * Notify the application that a profile email has been added.
      * 
      * @param remote
      *            True if the notification is the result of a remote event.
      */
     public void fireProfileEmailAdded(final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncProfile(remote);
             }
         });
     }
 
     /**
      * Notify the application that a profile email has been removed.
      * 
      * @param remote
      *            True if the notification is the result of a remote event.
      */
     public void fireProfileEmailRemoved(final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncProfile(remote);
             }
         });
     }
 
     /**
      * Notify the application that a profile email has been verified.
      * 
      * @param remote
      *            True if the notification is the result of a remote event.
      */
     public void fireProfileEmailVerified(final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncProfile(remote);
             }
         });
     }
 
     /**
      * Notify the application that a profile password has been reset.
      * 
      * @param remote
      *            True if the notification is the result of a remote event.
      */
     public void fireProfilePasswordReset(final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncProfile(remote);
             }
         });
     }
 
     /**
      * Notify the application that a profile password has been updated.
      * 
      * @param remote
      *            True if the notification is the result of a remote event.
      */
     public void fireProfilePasswordUpdated(final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncProfile(remote);
             }
         });
     }
 
     /**
      * Notify the application that the user profile has been updated.
      * 
      * @param remote
      *            True if the notification is the result of a remote event.
      */
     public void fireProfileUpdated(final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().syncProfile(remote);
             }
         });
     }
 
     /**
      * Obtain the browser's locales.
      * 
      * @return The browser's available <code>Locale[]</code>s.
      */
     public Locale[] getAvailableLocales() {
         return getPlatform().getAvailableLocales();
     }
 
     /**
      * Obtain the browser's locale.
      * 
      * @return The browser's <code>Locale</code>.
      */
     public TimeZone[] getAvailableTimeZones() {
         return getPlatform().getAvailableTimeZones();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.Application#getConnection()
      */
     public Connection getConnection() { return connection; }
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.application.Application#getId()
 	 * 
 	 */
 	public ApplicationId getId() { return ApplicationId.BROWSER; }
 
     /**
      * Obtain the browser's locale.
      * 
      * @return The browser's <code>Locale</code>.
      */
     public Locale getLocale() {
         return getPlatform().getLocale();
     }
 
     /**
      * Obtain a logger for the class from the applilcation.
      *
      *
      * @param clasz The class for which to obtain the logger.
      * @return An apache logger.
      */
     public Logger getLogger(final Class clasz) {
         return getPlatform().getLogger(clasz);
     }
 
     /**
 	 * Obtain the platform.
 	 * 
 	 * @return The platform the application is running on.
 	 */
 	public Platform getPlatform() { return super.getPlatform(); }
 
     /**
 	 * Obtain the selected system message.
 	 * 
 	 * @return The selected system message id.
 	 */
 	public Object getSelectedSystemMessage() { return null; }
 
 	/**
      * Obtain a session for an avatar type; and if one does not already exist
      * for the type create one.
      * 
      * @param avatarId
      *            An <code>AvatarId</code>.
      * @return A <code>BrowserSession</code>.
      */
     public BrowserSession getSession(final AvatarId avatarId) {
         return sessionImpl.getSession(new BrowserContext(avatarId), Boolean.TRUE);
     }
 
 	/**
      * Obtain a session for an avatar type.
      * 
      * @param avatarId
      *            An <code>AvatarId</code>.
      * @param create
      *            Whether or not to create the session if it does not already
      *            exist.
      * @return A <code>BrowserSession</code>.
      */
     public BrowserSession getSession(final AvatarId avatarId, final Boolean create) {
         return sessionImpl.getSession(new BrowserContext(avatarId), create);
     }
 
 	/**
      * Obtain the browser's locales.
      * 
      * @return The browser's available <code>Locale[]</code>s.
      */
     public TimeZone getTimeZone() {
         return getPlatform().getTimeZone();
     }
 
 	/**
 	 * Close the main window.
 	 *
 	 */
 	public void hibernate() { getPlatform().hibernate(getId()); }
 
 	/**
 	 * @see com.thinkparity.ophelia.browser.platform.application.Application#hibernate()
 	 * 
 	 */
 	public void hibernate(final Platform platform) {
 		assertStatusChange(ApplicationStatus.HIBERNATING);
 
 		disposeBrowserWindow();
 
         setStatus(ApplicationStatus.HIBERNATING);
 		notifyHibernate();
 	}
 
     /**
      * Iconify (or uniconify) the browser application.
      * 
      * @param iconify
      *            A <code>Boolean</code> indicating to iconify or uniconify.
      */
     public void iconify(final Boolean iconify) {
         if (iconify != isBrowserWindowIconified()) {
             int state = mainWindow.getExtendedState();
             if (iconify) {
                 state |= JFrame.ICONIFIED;
             } else {
                 state &= ~JFrame.ICONIFIED;
             }
             mainWindow.setExtendedState(state);
         }
     }
 
     /**
      * Initialize the status message.
      */
     public void initializeStatus() {
         runDisplayContainerSeenFlagInfo();
         runDisplayContactInvitationInfo();
     }
 
     public Boolean isBrowserWindowMaximized() {
         return JFrame.MAXIMIZED_BOTH == mainWindow.getExtendedState();
     }
     
     /** @see com.thinkparity.ophelia.browser.platform.application.Application#isDevelopmentMode() */
     public Boolean isDevelopmentMode() { 
         return getPlatform().isDevelopmentMode();
     }
 
     /**
      * Maximize (or unmaximize) the browser application.
      * 
      * @param maximize
      *            A <code>Boolean</code> indicating to maximize or unmaximize.
      */
     public void maximize(final Boolean maximize) {
         mainWindow.maximizeMainWindow(maximize);
     }
 
 	/**
      * Move and resize the browser window.
      * (See moveBrowserWindow, resizeBrowserWindow)
      *
      * @param l
      *            The new relative location of the window.
      * @param s
      *            The new browser's relative size.
      * 
      */
     public void moveAndResizeBrowserWindow(final Point l, final Dimension s) {
         final Point newL = mainWindow.getLocation();
         newL.x += l.x;
         newL.y += l.y;
         final Dimension newS = mainWindow.getSize();
         newS.width += s.width;
         newS.height += s.height;
         mainWindow.setMainWindowSizeAndLocation(newL, newS);
     }
 
 	/**
 	 * Move the browser window.
 	 * 
 	 * @param l
 	 *            The new relative location of the window.
 	 */
 	public void moveBrowserWindow(final Point l) {
 		final Point newL = mainWindow.getLocation();
 		newL.x += l.x;
 		newL.y += l.y;
 		mainWindow.setLocation(newL);
 	}
     
     /**
      * Call <code>toFront()</code> on the browser's main window.
      *
      */
     public void moveToFront() { mainWindow.toFront(); }
     
     /**
      * Resize the browser window.
      * 
      * @param s
      *            The new browser's relative size.
      */
     public void resizeBrowserWindow(final Dimension s) {
         final Dimension newS = mainWindow.getSize();
         newS.width += s.width;
         newS.height += s.height;
         mainWindow.setMainWindowSize(newS);
     }
 
 	/**
 	 * @see com.thinkparity.ophelia.browser.platform.application.Application#restore(com.thinkparity.ophelia.browser.platform.Platform)
 	 * 
 	 */
 	public void restore(final Platform platform) {
 		assertStatusChange(ApplicationStatus.RESTORING);
         
 		reOpenMainWindow();
 
         setStatus(ApplicationStatus.RESTORING);
         notifyRestore();
 
         assertStatusChange(ApplicationStatus.RUNNING);
         setStatus(ApplicationStatus.RUNNING);
 	}
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.Saveable#restoreState(com.thinkparity.ophelia.browser.platform.util.State)
 	 * 
 	 */
 	public void restoreState(final State state) {}
 
     /**
      * Confirm an attempt to retry an invocation of an action with the user and
      * if positive retry.
      * 
      * @param action
      *            An <code>AbstractAction</code>.
      */
     public void retry(final AbstractAction action,
             final Object... retryMessageArguments) {
         final String messageKey = "Retry." + action.getId();
         if (confirm("Retry.Confirm", "Retry.Deny", messageKey,
                 retryMessageArguments)) {
             action.retryInvokeAction();
         }
     }
     
     /**
 	 * Accept an invitation.
 	 * 
 	 * @param systemMessageId
 	 *            The system message id.
 	 */
 	public void runAcceptContactIncomingInvitation(final Long invitationId) {
 		final Data data = new Data(1);
 		data.set(AcceptIncomingInvitation.DataKey.INVITATION_ID, invitationId);
 		invoke(ActionId.CONTACT_ACCEPT_INCOMING_INVITATION, data);
 	}
     
     /**
      * Run the add bookmark action.
      * 
      * @param containerId
      *            The container id.
      */
     public void runAddContainerBookmark(final Long containerId) {
         final Data data = new Data(1);
         data.set(AddBookmark.DataKey.CONTAINER_ID, containerId);
         invoke(ActionId.CONTAINER_ADD_BOOKMARK, data);
     }
   
 	/**
      * Run the create document action, browse to select the document.
      * 
      * @param containerId
      *            The container id.
      *
      */
     public void runAddContainerDocuments(final Long containerId) {
         if(JFileChooser.APPROVE_OPTION == getJFileChooserForFileSelection().showOpenDialog(mainWindow)) {
             persistence.set(Keys.Persistence.CONTAINER_ADD_DOCUMENT_CURRENT_DIRECTORY,
                     jFileChooser.getCurrentDirectory());
             runAddContainerDocuments(containerId, jFileChooser.getSelectedFiles());
         }
     }
 
     /**
      * Run the add document action. This adds and/or updates documents.
      * 
      * @param containerId
      *            The container id.
      * @param file
      *            The document file.
      */
     public void runAddContainerDocuments(final Long containerId, final File[] files) {
         final Data data = new Data(2);
         data.set(AddDocument.DataKey.CONTAINER_ID, containerId);
         data.set(AddDocument.DataKey.FILES, files);
         invoke(ActionId.CONTAINER_ADD_DOCUMENT, data);
     }
 
     /**
      * Run the profile's add email action.
      *
      */
     public void runAddProfileEmail(final EMail email) {
         final Data data = new Data(1);
         data.set(AddEmail.DataKey.EMAIL, email);
         invoke(ActionId.PROFILE_ADD_EMAIL, data);
     }
 
     /**
      * Run the apply flag seen action.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      */
     public void runApplyContainerFlagSeen(final Long containerId) {
         final Data data = new Data(1);
         data.set(ApplyFlagSeen.DataKey.ARTIFACT_ID, containerId);
         invoke(ActionId.ARTIFACT_APPLY_FLAG_SEEN, data);         
     }
 
     /**
      * Run the apply flag seen action.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      */
     public void runApplyDocumentFlagSeen(final Long documentId) {
         final Data data = new Data(1);
         data.set(ApplyFlagSeen.DataKey.ARTIFACT_ID, documentId);
         invoke(ActionId.ARTIFACT_APPLY_FLAG_SEEN, data);         
     }
 
     /**
      * Run the add contact action.
      *
      */
     public void runCreateContactInvitation() {
         runCreateContactInvitation(null);
     }
 
     /**
      * Run the add contact action.
      * 
      * @param newContactEmail
      *              New contact email.
      */
     public void runCreateContactInvitation(final EMail email) {
         final Data data = new Data(1);
         if(null != email)
             data.set(CreateIncomingInvitation.DataKey.CONTACT_EMAIL, email);
         invoke(ActionId.CONTACT_CREATE_INCOMING_INVITATION, data);
     }
 
     /**
      * Run the create container (package) action. The user will
      * determine the container name.
      * 
      */
     public void runCreateContainer() {
         runCreateContainer(null, null);
     }
 
     /**
      * Create a container (package) with one or more new documents.
      * The user will determine the container name.
      * 
      * @param files
      *          List of files that will be added later      
      */
     public void runCreateContainer(final List<File> files) {
         runCreateContainer(null, files);
     }
 
     /**
      * Create a container (package) with a specified name.
      * 
      * @param name
      *            The container name.
      */
     public void runCreateContainer(final String name) {
         runCreateContainer(name, null);
     }
     
     /**
      * Run the create container action. If name and files are both not set; a
      * dialog will be used to prompt the user.
      * 
      * @param name
      *            The container name.
      * @param files
      *            A list of files to add to the container post creation.
      */
     public void runCreateContainer(final String name, final List<File> files) {
         final Data data = new Data(2);
         if(null != name)
             data.set(Create.DataKey.NAME, name);
         if(null != files)
             data.set(Create.DataKey.FILES, files);
         invoke(ActionId.CONTAINER_CREATE, data);
     }
 
     /**
      * Create a draft for the container.
      * 
      * @param containerId
      *            The container id.
      */
     public void runCreateContainerDraft(final Long containerId) {
         final Data data = new Data(1);
         data.set(CreateDraft.DataKey.CONTAINER_ID, containerId);
         invoke(ActionId.CONTAINER_CREATE_DRAFT, data);         
     }
     
     /**
      * Decline an invitation.
      * 
      * @param systemMessageId
      *            The system message id.
      */
     public void runDeclineContactIncomingInvitation(final Long invitationId) {
         final Data data = new Data(1);
         data.set(DeclineIncomingInvitation.DataKey.INVITATION_ID, invitationId);
         invoke(ActionId.CONTACT_DECLINE_INCOMING_INVITATION, data);
     }
 
     /**
      * Run the delete contact action.
      * 
      * @param contactId
      *            The contact id.
      */
     public void runDeleteContact(final JabberId contactId) {
         Assert.assertNotNull("Cannot delete null contact.", contactId);
         final Data data = new Data(1);
         data.set(Delete.DataKey.CONTACT_ID, contactId);
         invoke(ActionId.CONTACT_DELETE, data);        
     }
 
     /**
      * Run the display contact invitation info action.
      */
     public void runDisplayContactInvitationInfo() {
         invoke(ActionId.CONTACT_DISPLAY_INVITATION_INFO, Data.emptyData());     
     }    
     
     /**
      * Run the display contact invitation info action.
      * 
      * @param searchExpression
      *            A search expression.
      */
     public void runDisplayContactInvitationInfo(final String searchExpression) {
         final Data data = new Data(1);
         if (null != searchExpression) {
             data.set(DisplayContactInvitationInfo.DataKey.SEARCH_EXPRESSION, searchExpression);
         }
         invoke(ActionId.CONTACT_DISPLAY_INVITATION_INFO, data);     
     }
     
     /**
      * Run the display container flag seen info action.
      */
     public void runDisplayContainerSeenFlagInfo() {
         invoke(ActionId.CONTAINER_DISPLAY_FLAG_SEEN_INFO, Data.emptyData());   
     }
     
     /**
      * Run the display container flag seen info action.
      * 
      * @param searchExpression
      *            A search expression.
      */
     public void runDisplayContainerSeenFlagInfo(final String searchExpression) {
         final Data data = new Data(1);
         if (null != searchExpression) {
             data.set(DisplayFlagSeenInfo.DataKey.SEARCH_EXPRESSION, searchExpression);
         }
         invoke(ActionId.CONTAINER_DISPLAY_FLAG_SEEN_INFO, data);  
     }
     
     /**
 	 * Run the open document action.
 	 *
      * @param containerId
      *            A container id.
 	 * @param documentId
 	 *            The document id.
 	 */
 	public void runOpenDocument(final Long documentId) {
 		final Data data = new Data(1);
 		data.set(Open.DataKey.DOCUMENT_ID, documentId);
 		invoke(ActionId.DOCUMENT_OPEN, data);
 	}
     
     /**
 	 * Run the open document version action.
 	 *
 	 * @param documentId
 	 *            The document id.
 	 * @param versionId
 	 *            The document's version id.
 	 */
 	public void runOpenDocumentVersion(final Long documentId,
             final Long versionId) {
         Assert.assertNotNull("Cannot open null document.", documentId);
         Assert.assertNotNull("Cannot open null document version.", versionId);
 		final Data data = new Data(2);
 		data.set(OpenVersion.DataKey.DOCUMENT_ID, documentId);
 		data.set(OpenVersion.DataKey.VERSION_ID, versionId);
 		invoke(ActionId.DOCUMENT_OPEN_VERSION, data);
 	}
 
     /**
      * Run the platform browser restore action.
      *
      */
     public void runPlatformBrowserRestore() {
         invoke(ActionId.PLATFORM_BROWSER_RESTORE, Data.emptyData());
     }
 
     /**
      * Run the profile sign-up action.
      *
      */
     public void runProfileSignUp() {
         invoke(ActionId.PROFILE_SIGN_UP, Data.emptyData());
     }
     
     /**
      * Publish the container.
      * 
      *  @param monitor
      *              The monitor.
      *  @param containerId
      *              The container id.
      *  @param teamMembers
      *              The team members. 
      *  @param contacts
      *              The contacts. 
      *  @param comment
      *              The comment.                                          
      */
     public void runPublishContainer(final ThinkParitySwingMonitor monitor,
             final Long containerId, final List<TeamMember> teamMembers,
             final List<Contact> contacts, final String comment) {
         final Data data = new Data(5);
         data.set(Publish.DataKey.CONTAINER_ID, containerId);
         data.set(Publish.DataKey.CONTACTS, contacts);
         if (null != comment) {
             data.set(Publish.DataKey.COMMENT, comment);
         }
         data.set(Publish.DataKey.MONITOR, monitor);
         data.set(Publish.DataKey.TEAM_MEMBERS, teamMembers);
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 invoke(ActionId.CONTAINER_PUBLISH, data);
             }
         });
     }
     
     /**
      * Publish the container version (ie. forward).
      * 
      *  @param monitor
      *              The monitor.
      *  @param containerId
      *              The container id.
      *  @param versionId
      *              The version id.            
      *  @param teamMembers
      *              The team members. 
      *  @param contacts
      *              The contacts. 
      *  @param comment
      *              The comment.                             
      */
     public void runPublishContainerVersion(final ThinkParitySwingMonitor monitor, final Long containerId, final Long versionId,
             final List<TeamMember> teamMembers, final List<Contact> contacts) {
         final Data data = new Data(5);
         data.set(PublishVersion.DataKey.CONTAINER_ID, containerId);
         data.set(PublishVersion.DataKey.VERSION_ID, versionId);
         data.set(PublishVersion.DataKey.CONTACTS, contacts);
         data.set(PublishVersion.DataKey.MONITOR, monitor);
         data.set(PublishVersion.DataKey.TEAM_MEMBERS, teamMembers);
         SwingUtilities.invokeLater(new Runnable() {
             public void run() { invoke(ActionId.CONTAINER_PUBLISH_VERSION, data); }
         });
     }
     
     /**
      * Run the open contact action.
      * 
      * @param contactId
      *            The contact id.
      */
     public void runReadContact(final JabberId contactId) {
         Assert.assertNotNull("Cannot open null contact.", contactId);
         final Data data = new Data(1);
         data.set(Read.DataKey.CONTACT_ID, contactId);
         invoke(ActionId.CONTACT_READ, data);
     }
     
     /**
      * Run the read container version action.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      */
     public void runReadContainerVersion(final Long containerId,
             final Long versionId) {
         final Data data = new Data(2);
         data.set(ReadVersion.DataKey.CONTAINER_ID, containerId);
         data.set(ReadVersion.DataKey.VERSION_ID, versionId);
         invoke(ActionId.CONTAINER_READ_VERSION, data);
     }
     
     /**
      * Run the remove bookmark action.
      * 
      * @param containerId
      *            The container id.
      */
     public void runRemoveContainerBookmark(final Long containerId) {
         final Data data = new Data(1);
         data.set(RemoveBookmark.DataKey.CONTAINER_ID, containerId);
         invoke(ActionId.CONTAINER_REMOVE_BOOKMARK, data);
     }
 
     /**
      * Run the remove flag seen action.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      */
     public void runRemoveContainerFlagSeen(final Long containerId) {
         final Data data = new Data(1);
         data.set(RemoveFlagSeen.DataKey.ARTIFACT_ID, containerId);
         invoke(ActionId.ARTIFACT_REMOVE_FLAG_SEEN, data);         
     }
 
 	/**
      * Run the profile's remove email action.
      *
      */
     public void runRemoveProfileEmail(final Long emailId) {
         final Data data = new Data(1);
         data.set(RemoveEmail.DataKey.EMAIL_ID, emailId);
         invoke(ActionId.PROFILE_REMOVE_EMAIL, data);
     }
 
     /**
      * Run the container rename action.
      * 
      * @param containerId
      *            A container id.
      * @param containerName
      *            A container name.
      */
     public void runRenameContainer(final Long containerId,
             final String containerName) {
         final Data data = new Data(2);
         data.set(Rename.DataKey.CONTAINER_ID, containerId);
         data.set(Rename.DataKey.CONTAINER_NAME, containerName);
         invoke(ActionId.CONTAINER_RENAME, data);
     }
     
     /**
      * Run the document rename action.
      * 
      * @param containerId
      *            A container id.
      * @param documentId
      *            A document id.
      * @param documentName
      *            An document name.
      */
     public void runRenameDocument(final Long documentId,
             final String documentName) {
         final Data data = new Data(2);
         data.set(RenameDocument.DataKey.DOCUMENT_ID, documentId);
         data.set(RenameDocument.DataKey.DOCUMENT_NAME, documentName);
         invoke(ActionId.CONTAINER_RENAME_DOCUMENT, data);
     }
 
     /**
      * Run the reset profile password action.
      *
      */
     public void runResetProfilePassword() {
         invoke(ActionId.PROFILE_RESET_PASSWORD, Data.emptyData());
     }
 
     /**
      * Update a document draft.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      * @param file
      *            A <code>File</code>.
      */
     public void runUpdateDocumentDraft(final Long documentId, final File file) {
         final Data data = new Data(2);
         data.set(UpdateDraft.DataKey.DOCUMENT_ID, documentId);
         data.set(UpdateDraft.DataKey.FILE, file);
         invoke(ActionId.DOCUMENT_UPDATE_DRAFT, data);
     }
 
     /**
      * Update the user's profile.
      *
      * @param name
      *            The user's name <code>String</code>.
      * @param address
      *            The user's address <code>String</code>. 
      * @param city
      *            The user's city <code>String</code>.   
      * @param country
      *            The user's country <code>String</code>.       
      * @param mobilePhone
      *            The user's mobile phone <code>String</code>.
      * @param organization
      *            The user's organization <code>String</code>.
      * @param phone
      *            The user's phone <code>String</code>.
      * @param postalCode
      *            The user's postal code <code>String</code>.
      * @param province
      *            The user's province <code>String</code>.
      * @param title
      *            The user's title <code>String</code>.
      */
     public void runUpdateProfile(final String name,
             final String address, final String city,
             final String country, final String mobilePhone,
             final String organization, final String phone,
             final String postalCode, final String province,
             final String title) {
         final Data data = new Data(11);
         data.set(Update.DataKey.DISPLAY_AVATAR, Boolean.FALSE);
         data.set(Update.DataKey.NAME, name);
         if (null != address)
             data.set(Update.DataKey.ADDRESS, address);
         if (null != city)
             data.set(Update.DataKey.CITY, city);
         if (null != country)
             data.set(Update.DataKey.COUNTRY, country);
         if (null != mobilePhone)
             data.set(Update.DataKey.MOBILE_PHONE, mobilePhone);
         if (null != organization)
             data.set(Update.DataKey.ORGANIZATION, organization);
         if (null != phone)
             data.set(Update.DataKey.PHONE, phone);
         if (null != postalCode)
             data.set(Update.DataKey.POSTAL_CODE, postalCode);
         if (null != province)
             data.set(Update.DataKey.PROVINCE, province);
         if (null != title)
             data.set(Update.DataKey.TITLE, title);
         invoke(ActionId.PROFILE_UPDATE, data);
     }
 
     /**
      * Update the user's profile.
      * 
      * @param oldPassword
      *            The user's password <code>String</code>.
      * @param newPassword
      *            The user's updated password <code>String</code>.
      * @param confirmNewPassword
      *            The user's updated password again <code>String</code>.
      */
     public void runUpdateProfilePassword(final String password,
             final String newPassword, final String confirmNewPassword) {     
         final Data data = new Data(4); 
         data.set(UpdatePassword.DataKey.DISPLAY_AVATAR, Boolean.FALSE);
         data.set(UpdatePassword.DataKey.PASSWORD, password);
         data.set(UpdatePassword.DataKey.NEW_PASSWORD, newPassword);
         data.set(UpdatePassword.DataKey.NEW_PASSWORD_CONFIRM, confirmNewPassword);
         invoke(ActionId.PROFILE_UPDATE_PASSWORD, data);
     }
 
     /**
      * Run the profile's verify email action.  Since no key is specified; this
      * will display a dialog.
      *
      */
     public void runVerifyProfileEmail(final Long emailId) {
         runVerifyProfileEmail(emailId, null);
     }
 
     /**
      * Run the profile's verify email action.
      * 
      * @param emailId
      *            An email id <code>Long</code>.
      * @param key
      *            An email verification key <code>String</code>.
      */
     public void runVerifyProfileEmail(final Long emailId, final String key) {
         final Data data = new Data(3);
         data.set(VerifyEmail.DataKey.EMAIL_ID, emailId);
         if (null == key) {
             data.set(VerifyEmail.DataKey.DISPLAY_AVATAR, Boolean.TRUE);
         } else {
             data.set(VerifyEmail.DataKey.DISPLAY_AVATAR, Boolean.FALSE);
             data.set(VerifyEmail.DataKey.KEY, key);
         }
         invoke(ActionId.PROFILE_VERIFY_EMAIL, data);
     }
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.Saveable#saveState(com.thinkparity.ophelia.browser.platform.util.State)
 	 * 
 	 */
 	public void saveState(final State state) {}
 
     /**
      * Select a tab. This displays the tab and also causes the
      * tab buttons to update.
      */
     public void selectTab(final MainTitleAvatar.TabId tabId) {
         final Data data = (Data) ((Data) getMainTitleAvatar().getInput()).clone();
         data.set(MainTitleAvatar.DataKey.TAB_ID, tabId);
         getMainTitleAvatar().setInput(data); 
     }
 
 	/**
      * Set the cursor.
      * 
      * @param cursor
      *          The cursor to use.
      */
     public void setCursor(Cursor cursor) {
         SwingUtil.setCursor(mainWindow.getContentPane(), cursor);
     }
         
     /**
      * Set a custom status message with a link.
      * 
      * @param linkAction
      *            A link action.    
      */
     public void setStatus(final LinkAction linkAction) {
         setStatus("Empty", null, linkAction);
     }
 
     /**
      * Show the contact invitation.
      * 
      * @param invitationId
      *            The invitationId.
      */
     public void showContactInvitation(final Long invitationId) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().showContactInvitation(invitationId);
             }
         });   
     }
 
     /**
      * Show the contact invitation.
      * 
      * @param invitationIds
      *            The list of invitationIds.
      * @param index
      *            The index of the invitation to show (0 indicates the invitation displayed at top).   
      */
     public void showContactInvitation(final List<Long> invitationIds, final int index) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContactAvatar().showContactInvitation(invitationIds, index);
             }
         });   
     }
     
     /**
      * Show the container.
      * This expands the container without animation, and scrolls so it is visible.
      * 
      * @param containerIds
      *            The list of containerIds.
      * @param index
      *            The index of the container to show (0 indicates the container displayed at top).         
      */
     public void showContainer(final List<Long> containerIds, final int index) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContainerAvatar().showContainer(containerIds, index);
             }
         });   
     }
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.application.Application#start()
 	 * 
 	 */
     public void start(final Platform platform) {
 		logApiId();
 
 		assertStatusChange(ApplicationStatus.STARTING);
         setStatus(ApplicationStatus.STARTING);
 
         connection = getSessionModel().isLoggedIn() ?
                 Connection.ONLINE : Connection.OFFLINE;
         setStatus(connection);
 
 		ed = new EventDispatcher(this);
 		ed.start();
 
 		assertStatusChange(ApplicationStatus.RUNNING);
 		openMainWindow();
 
         setStatus(ApplicationStatus.RUNNING);
 		notifyStart();
 	}
 
     public void toggleStatusImage() {
         ((com.thinkparity.ophelia.browser.application.browser.display.StatusDisplay) mainWindow.getDisplay(DisplayId.STATUS)).toggleImage();
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.AbstractApplication#getAvatar(com.thinkparity.ophelia.browser.application.browser.display.avatar.AvatarId)
      */
     @Override
     protected Avatar getAvatar(final AvatarId id) {
         return super.getAvatar(id);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.AbstractApplication#getAvatar(com.thinkparity.ophelia.browser.platform.plugin.extension.TabExtension)
      */
     @Override
     protected Avatar getAvatar(final TabListExtension tabListExtension) {
         return super.getAvatar(tabListExtension);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.AbstractApplication#getAvatar(com.thinkparity.ophelia.browser.platform.plugin.extension.TabExtension)
      */
     @Override
     protected Avatar getAvatar(final TabPanelExtension tabPanelExtension) {
         return super.getAvatar(tabPanelExtension);
     }
 
     /** Display the main status avatar. */
     void displayMainStatusAvatar() {
         final Data input = new Data(2);
         input.set(MainStatusAvatar.DataKey.PROFILE, getProfile());
         input.set(MainStatusAvatar.DataKey.CONNECTION, getConnection());
         setInput(AvatarId.MAIN_STATUS, input);
         displayStatus(AvatarId.MAIN_STATUS);
     }
 
     /** Display the main title avatar. */
 	void displayMainTitleAvatar() {
         final Data input = new Data(2);
         input.set(MainTitleAvatar.DataKey.PROFILE, getProfile());
         input.set(MainTitleAvatar.DataKey.TAB_ID, MainTitleAvatar.TabId.CONTAINER);
         setInput(AvatarId.MAIN_TITLE, input);
         displayTitle(AvatarId.MAIN_TITLE);
 	}
 
     /** Notify the session has been terminated. */
     void fireConnectionOffline() {
         connection = Connection.OFFLINE;
         setStatus(connection);
     }
 
     /** Notify the session has been established. */
     void fireConnectionOnline() {
         connection = Connection.ONLINE;
         setStatus(connection);
     }
 
     /**
 	 * Obtain the input for an avatar.
 	 * 
 	 * @param avatarId
 	 *            The avatar id.
 	 * @return The avatar input.
 	 */
 	Object getAvatarInput(final AvatarId id) {
 		return avatarInputMap.get(id);
 	}
     
     /**
      * Clear the custom status message.
      * 
      */
     private void clearStatus() {
         setStatus("Empty");
     }
 
     /**
      * Open a confirmation dialogue.
      * 
      * @param input
      *            The dialogue's input.
      * @return True if the user confirmed.
      */
     private Boolean confirm(final Data input) {
         open(WindowId.CONFIRM, AvatarId.DIALOG_CONFIRM, input);
         return getConfirmAvatar().didConfirm();
     }
     
     /**
 	 * Display an avatar.
 	 * 
 	 * @param displayId
 	 *            The display to use.
 	 * @param avatarId
 	 *            The avatar to display.
 	 */
 	private void displayAvatar(final WindowId windowId, final AvatarId avatarId) {
 		Assert.assertNotNull("Cannot display on a null window.", windowId);
 		Assert.assertNotNull("Cannot display a null avatar.", avatarId);
 		final Window window = WindowFactory.create(windowId, mainWindow);
 
 		final Avatar avatar = getAvatar(avatarId);
 
 		final Object input = getAvatarInput(avatarId);
 		if (null == input) {
             logger.logInfo("Avatar {0}'s input is null.", avatarId);
 		} else {
             avatar.setInput(getAvatarInput(avatarId));
 		}
         
         setUpSemiTransparentLayer(window);
         
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() { window.open(avatar); }
 		});
 	}
     
     /**
      * Display an error dialog including details of the Throwable error.
      * 
      * @param errorMessageKey
      *            An error message key (optional).
      * @param errorMessageArguments
      *            Error message arguments (optional).
      * @param error
      *            An error.         
      */
     private void displayErrorDetailsDialog(final String errorMessageKey,
         final Object[] errorMessageArguments, final Throwable error) {
         final Data input = new Data(3);
         if (null != errorMessageKey)
             input.set(ErrorDetailsAvatar.DataKey.ERROR_MESSAGE_KEY, errorMessageKey);
         if (null != errorMessageArguments)
             input.set(ErrorDetailsAvatar.DataKey.ERROR_MESSAGE_ARGUMENTS, errorMessageArguments);
         if (null != error)
             input.set(ErrorDetailsAvatar.DataKey.ERROR, error);
         open(WindowId.ERROR, AvatarId.DIALOG_ERROR_DETAILS, input);
     }
 
     /**
      * Display an avatar on the status display.
      * 
      * @param id
      *            An avatar id.
      */
     private void displayStatus(final AvatarId id) {
         displayHelper.displayStatus(id);
     }
 
     /**
      * Display a tab.
      * 
      * @param id
      *            An avatar id.
      */
     private void displayTab(final AvatarId id) {
         displayHelper.displayTab(id);
     }
 
     /**
      * Display a tab.
      * 
      * @param tabListExtension
      *            A tab list extension.
      */
     private void displayTab(final TabListExtension tabListExtension) {
         displayHelper.displayTab(tabListExtension);
     }
 
     /**
      * Display a tab.
      * 
      * @param tabExtension
      *            A tab panel extension.
      */
     private void displayTab(final TabPanelExtension tabPanelExtension) {
         displayHelper.displayTab(tabPanelExtension);
     }
 
 	/**
      * Display an avatar on the title display.
      * 
      * @param id
      *            An avatar id.
      */
     private void displayTitle(final AvatarId id) {
         displayHelper.displayTitle(id);
     }
 
     /** Dispose the main window. */
     private void disposeBrowserWindow() {
         Assert.assertNotNull(mainWindow, "Main window is null.");
         mainWindow.dispose();
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
      * Obtain the confirmation avatar.
      * @return The confirmation avatar.
      */
     private ConfirmAvatar getConfirmAvatar() {
         return (ConfirmAvatar) getAvatar(AvatarId.DIALOG_CONFIRM);
     }
 
 	/**
 	 * Obtain the file chooser.
 	 * 
 	 * @return The file chooser.
 	 */
 	private JFileChooser getJFileChooserForFileSelection() {
         jFileChooser = JFileChooserUtil.getJFileChooser(JFileChooser.FILES_ONLY, Boolean.TRUE, null,
                 persistence.get(Keys.Persistence.CONTAINER_ADD_DOCUMENT_CURRENT_DIRECTORY,
                         (File) null));
 		return jFileChooser;
 	}
 
     /**
      * Obtain the main status avatar.
      * 
      * @return The main status avatar.
      */
     private MainStatusAvatar getMainStatusAvatar() {
         return (MainStatusAvatar) getAvatar(AvatarId.MAIN_STATUS);
     }
 
     /**
      * Obtain the main title avatar.
      * 
      * @return The main title avatar.
      */
     private MainTitleAvatar getMainTitleAvatar() {
         return (MainTitleAvatar) getAvatar(AvatarId.MAIN_TITLE);
     }
 
 	/**
      * Obtain the tab input from the main title avatar.
      * 
      * @return A tab.
      */
     private MainTitleAvatar.TabId getMainTitleAvatarTab() {
         return (MainTitleAvatar.TabId) ((Data) getMainTitleAvatar().getInput()).get(MainTitleAvatar.DataKey.TAB_ID);
     }
 
     /**
      * Obtain the archive tab avatar.
      * 
      * @return A <code>ArchiveAvatar</code>.
      */
     private ArchiveTabAvatar getTabArchiveAvatar() {
         return (ArchiveTabAvatar) getAvatar(AvatarId.TAB_ARCHIVE);
     }
 
     /**
      * Obtain the contact tab avatar.
      * 
      * @return The contact tab avatar.
      */
     private ContactTabAvatar getTabContactAvatar() {
         return (ContactTabAvatar) getAvatar(AvatarId.TAB_CONTACT);
     }
 
     /**
      * Obtain the container tab avatar.
      * 
      * @return A <code>ContainerAvatar</code>.
      */
     private ContainerTabAvatar getTabContainerAvatar() {
         return (ContainerTabAvatar) getAvatar(AvatarId.TAB_CONTAINER);
     }
 
 	private void invoke(final ActionId actionId, final Data data) {
 		try {
 			getAction(actionId).invokeAction(data);
 		} catch(final Throwable t) {
             logger.logError(t, "Could not invoke action {0} with data {1}.", actionId, data);
             // TODO Provide meaningful error messages
             displayErrorDialog(t);
 		}
 	}
 
     private Boolean isBrowserWindowIconified() {
		return JFrame.ICONIFIED == mainWindow.getExtendedState();
 	}
    
     private Boolean isBrowserWindowOpen() {
 		return null != mainWindow && mainWindow.isVisible();
 	}
 
     /**
      * Open a window.
      * 
      * @param windowId
      *            A <code>WindowId</code>.
      * @param avatarId
      *            An <code>AvatarId</code>.
      * @param input
      *            Input <code>Data</code>.
      */
 	private void open(final WindowId windowId,
             final AvatarId avatarId, final Data input) {
         final Window window = WindowFactory.create(windowId, mainWindow);
         final Avatar avatar = getAvatar(avatarId);
         avatar.setInput(input);
         setUpSemiTransparentLayer(window);
         window.open(avatar);
     }
 
     /**
 	 * Open the main browser window.
 	 *
 	 */
 	private void openMainWindow() {
 		mainWindow = new BrowserWindow(this);
 		displayHelper.setBrowserWindow(mainWindow);
 		mainWindow.open();
 	}
 
 	private void reOpenMainWindow() {
         mainWindow = new BrowserWindow(this);
         displayHelper.setBrowserWindow(mainWindow);
         mainWindow.reOpen();
     }
 
     /**
 	 * Set the input for an avatar. If the avatar is currently being displayed;
 	 * it will be set immediately; otherwise it will be stored in the local
 	 * hash; and set when the avatar is displayed.
 	 * 
 	 * @param avatarId
 	 *            The avatar id.
 	 * @param input
 	 *            The avatar input.
 	 */
 	private void setInput(final AvatarId avatarId, final Object input) {
 		final Avatar avatar = getAvatar(avatarId);
 		if(null == avatar) {
 			logger.logWarning("Avatar {0} does not exist.", avatarId);
 			avatarInputMap.put(avatarId, input);
 		}
 		else {
 			avatarInputMap.remove(avatarId);
 			avatar.setInput(input);
 		}
 	}
 
     /**
      * Set the connection status.
      * 
      * @param connection
      *            A platform connection.
      */
     private void setStatus(final Connection connection) {
         Data input = (Data) getMainStatusAvatar().getInput();
         if (null == input) {
             input = new Data(4);
         }
         input.set(MainStatusAvatar.DataKey.CONNECTION, connection);
         getMainStatusAvatar().setInput((Data) input.clone());
     } 
 
     /**
      * Set a custom status message.
      * 
      * @param customMessage
      *            A status message.
      */
     private void setStatus(final String customMessage) {
         setStatus(customMessage, null, null);
     }
 
     /**
      * Set a custom status message.
      * 
      * @param message
      *            A status message.
      * @param arguments
      *            Status message arguments.
      * @param linkAction
      *            A link action.                  
      */
     private void setStatus(final String customMessage,
             final Object[] customMessageArguments,
             final LinkAction linkAction) {
         Data input = (Data) getMainStatusAvatar().getInput();
         if (null == input) {
             input = new Data(4);
         }
         input.set(MainStatusAvatar.DataKey.CUSTOM_MESSAGE, customMessage);
         if (null != customMessageArguments) {
             input.set(MainStatusAvatar.DataKey.CUSTOM_MESSAGE_ARGUMENTS, customMessageArguments);
         }
         else {
             input.unset(MainStatusAvatar.DataKey.CUSTOM_MESSAGE_ARGUMENTS);
         }
         if (null != linkAction) {
             input.set(MainStatusAvatar.DataKey.LINK_ACTION, linkAction);
         }
         else {
             input.unset(MainStatusAvatar.DataKey.LINK_ACTION);
         }
         getMainStatusAvatar().setInput((Data) input.clone());
     }
 
     /**
      * Set up the semi-transparent layer on the browser window.
      * It will turn on immediately and turn off when the window
      * is closed.
      * 
      * @param window
      *          A window.
      */
     private void setUpSemiTransparentLayer(final Window window) {
         mainWindow.enableSemiTransparentLayer(Boolean.TRUE);
         window.addWindowListener(new WindowAdapter() {
             public void windowClosed(WindowEvent e) {
                 mainWindow.enableSemiTransparentLayer(Boolean.FALSE);
             }
         });
     }
 
     /**
      * Synchronize a container within the container tab.
      * 
      * @param containerId
      *            The container id.
      * @param remote
      *            The remote event indicator.
      * @param select
      *            The selection indicator.
      */
     private void syncDocumentTabContainer(final Long documentId,
             final Boolean remote) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 getTabContainerAvatar().syncDocument(documentId, remote);
             }
         });        
     }
 }
