 /*
  * Jan 10, 2006
  */
 package com.thinkparity.ophelia.browser.platform.action.container;
 
 import java.util.List;
 
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.model.artifact.Artifact;
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.user.TeamMember;
 import com.thinkparity.ophelia.browser.application.browser.Browser;
 import com.thinkparity.ophelia.browser.platform.action.AbstractBrowserAction;
 import com.thinkparity.ophelia.browser.platform.action.ActionId;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.browser.platform.action.ThinkParitySwingMonitor;
 import com.thinkparity.ophelia.browser.platform.action.ThinkParitySwingWorker;
 import com.thinkparity.ophelia.model.artifact.ArtifactModel;
 import com.thinkparity.ophelia.model.contact.ContactModel;
 import com.thinkparity.ophelia.model.container.ContainerDraft;
 import com.thinkparity.ophelia.model.container.ContainerModel;
 import com.thinkparity.ophelia.model.container.monitor.PublishStep;
 import com.thinkparity.ophelia.model.document.CannotLockException;
 import com.thinkparity.ophelia.model.session.OfflineException;
 import com.thinkparity.ophelia.model.util.ProcessAdapter;
 import com.thinkparity.ophelia.model.util.ProcessMonitor;
 import com.thinkparity.ophelia.model.util.Step;
 
 /**
  * Publish a document. This will send a given document version to every member
  * of the team.
  * 
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public class Publish extends AbstractBrowserAction {
 
     /** Data keys. */
 	public enum DataKey {
         COMMENT, CONTACTS, CONTAINER_ID, EMAILS, MONITOR, TEAM_MEMBERS
     }
 
     /** A publish action worker object. */
     private static class PublishWorker extends ThinkParitySwingWorker<Publish> {
         private final ArtifactModel artifactModel;
         private final String comment;
         private final ContactModel contactModel;
         private final List<Contact> contacts;
         private final Container container;
         private final ContainerModel containerModel;
         private final List<EMail> emails;
         private ProcessMonitor publishMonitor;
         private final List<TeamMember> teamMembers;
         private PublishWorker(final Publish publish, final Container container,
                 final String comment, final List<EMail> emails,
                 final List<Contact> contacts, final List<TeamMember> teamMembers) {
             super(publish);
             this.container = container;
             this.comment = comment;
             this.emails = emails;
             this.contacts = contacts;
             this.teamMembers = teamMembers;
 
             this.artifactModel = publish.getArtifactModel();
             this.contactModel = publish.getContactModel();
             this.containerModel = publish.getContainerModel();
             
             this.publishMonitor = newPublishMonitor();
         }
         @Override
 		public Runnable getErrorHandler(final Throwable t) {
 			return new Runnable() {
 				public void run() {
 					action.browser.displayErrorDialog("PublishError",
 							new Object[] {}, t);
 				}
 			};
 		}
         /**
          * Create a new publish monitor.
          * 
          * @return A <code>ProcessMonitor</code>.
          */
         private ProcessMonitor newPublishMonitor() {
             return new ProcessAdapter() {
                 private Integer stepIndex;
                 private Integer steps;
                 @Override
                 public void beginProcess() {
                     monitor.monitor();
                 }
                 @Override
                 public void beginStep(final Step step,
                         final Object data) {
                     if (null != steps && steps.intValue() > 0) {
                         if (PublishStep.UPLOAD_STREAM == step) {
                             monitor.setStep(stepIndex, action.getString("ProgressUploadStream", new Object[] {data}));
                         } else if (PublishStep.PUBLISH == step) {
                             monitor.setStep(stepIndex, action.getString("ProgressPublish"));
                         }
                     }
                 }
                 @Override
                 public void determineSteps(final Integer steps) {
                     this.stepIndex = 0;
                     this.steps = steps;
                     monitor.setSteps(steps);
                     monitor.setStep(stepIndex);
                 }
                 @Override
                 public void endProcess() {
                     monitor.complete();
                 }
                 @Override
                 public void endStep(final Step step) {
                     if (PublishStep.PUBLISH == step) {
                         // we're done
                         stepIndex = steps - 1;
                         monitor.setStep(stepIndex);
                     } else if (null != steps && steps.intValue() > 0) {
                         stepIndex++;
                         monitor.setStep(stepIndex);
                     }
                 }
             };
         }
 		@Override
         public Object run() {
             /* if any e-mail addresses are contact e-mails, convert them to
              * contact references */
             Contact contact;
             for (final EMail email : emails) {
                 if (contactModel.doesExist(email)) {
                     contact = contactModel.read(email);
                     if (!contacts.contains(contact)) {
                         contacts.add(contact);
                         emails.remove(email);
                     }
                 }
             }
             try {
                 containerModel.saveDraft(container.getId());
             } catch (final CannotLockException clx) {
                 monitor.reset();
                 publishMonitor = newPublishMonitor();
                 action.browser.retry(action, container.getName());
                 return null;
             }
             try {
                 containerModel.publish(publishMonitor, container.getId(),
                         comment, emails, contacts, teamMembers);
             } catch (final OfflineException ox) {
                 // TODO implement a "you are offline" notification
                 monitor.reset();
                 publishMonitor = newPublishMonitor();
                 return null;
             } catch (final CannotLockException clx) {
                 try {
                     containerModel.restoreDraft(container.getId());
                 } catch (final CannotLockException clx2) {
                 	// not a whole lot that can be done here
                 	action.logger.logFatal(clx2,
                 			"Could not restore draft for {0}.", container);
                 }
                 monitor.reset();
                 publishMonitor = newPublishMonitor();
                 action.browser.retry(action, container.getName());
                 return null;
             }
             artifactModel.applyFlagSeen(container.getId());
             return containerModel.readLatestVersion(container.getId());
         }
     }
 
     /** The <code>Browser</code> application. */
     private final Browser browser;
 
     /** The comment <code>String</code> used by invoke and retry invoke action. */
 	private String comment;
 
     /**
      * The <code>List</code> of <code>Contact</code>s used by invoke and
      * retry invoke action.
      */
 	private List<Contact> contacts;
 
     /** The <code>Container</code> used by invoke and retry invoke action. */
     private Container container;
 
     /**
      * The <code>List</code> of <code>EMail</code> addresses used by invoke and
      * retry invoke action.
      */
     private List<EMail> emails;
 
     /**
      * A <code>ThinkParitySwingMonitor</code>.  Used by invoke and retry invoke.
      * 
      */
     private ThinkParitySwingMonitor monitor;
 
     /**
      * The <code>List</code> of <code>TeamMember</code>s used by invoke and
      * retry invoke action.
      */
     private List<TeamMember> teamMembers;
 
     /**
      * Create Publish.
      * 
      * @param browser
      *            The <code>Browser</code> application.
      */
 	public Publish(final Browser browser) {
 		super(ActionId.CONTAINER_PUBLISH);
         this.browser = browser;
 	}
 
 	/**
 	 * @see com.thinkparity.ophelia.browser.platform.action.AbstractAction#invoke(com.thinkparity.ophelia.browser.platform.action.Data)
 	 * 
 	 */
 	public void invoke(final Data data) {
         final Long containerId = (Long) data.get(DataKey.CONTAINER_ID);
         final ContainerModel containerModel = getContainerModel();
         final Container container = containerModel.read(containerId);
         if (containerModel.isLocalDraftModified(containerId)) {
             final ContainerDraft draft = containerModel.readDraft(containerId);
             boolean isPublishable = false;
             for (final Artifact artifact : draft.getArtifacts()) {
                 switch (draft.getState(artifact)) {
                 case ADDED:
                 case MODIFIED:
                     isPublishable = true;
                     break;
                 }
                 if (isPublishable)
                     break;
             }
             if (isPublishable) {
                 final List<EMail> emails = data.getList(DataKey.EMAILS);
                 final List<Contact> contacts = data.getList(DataKey.CONTACTS);
                 final List<TeamMember> teamMembers = data.getList(DataKey.TEAM_MEMBERS);
                 if (0 == emails.size() && 0 == contacts.size()
                         && teamMembers.size() == 0) {
                     browser.displayPublishContainerDialog(containerId);
                 } else {
                     final String comment = (String) data.get(DataKey.COMMENT);
                     final ThinkParitySwingMonitor monitor = (ThinkParitySwingMonitor) data.get(DataKey.MONITOR);
                     invoke(monitor, container, comment, emails, contacts,
                             teamMembers);
                 }
             } else {
                 browser.displayErrorDialog("Publish.NoDocumentToPublish",
                     new Object[] {container.getName()});
             }
         } else {
 	        browser.displayErrorDialog("NoPublish.NoChangesToDraft",
 	                new Object[] {container.getName()});
         }
 	}
 
     /**
      * Invoke publish.
      * 
      * @param worker
      *            A <code>ThinkParitySwingWorker</code>.
      */
     private void invoke(final ThinkParitySwingMonitor monitor,
             final Container container, final String comment,
             final List<EMail> emails, final List<Contact> contacts,
             final List<TeamMember> teamMembers) {
         this.monitor = monitor;
         this.container = container;
         this.comment = comment;
         this.emails = emails;
         this.contacts = contacts;
         this.teamMembers = teamMembers;
         final ThinkParitySwingWorker worker = new PublishWorker(this, container,
                     comment, emails, contacts, teamMembers);
         worker.setMonitor(monitor);
         worker.start();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.action.AbstractAction#retryInvokeAction()
      *
      */
     @Override
     public void retryInvokeAction() {
         invoke(monitor, container, comment, emails, contacts, teamMembers);
     }
 }
