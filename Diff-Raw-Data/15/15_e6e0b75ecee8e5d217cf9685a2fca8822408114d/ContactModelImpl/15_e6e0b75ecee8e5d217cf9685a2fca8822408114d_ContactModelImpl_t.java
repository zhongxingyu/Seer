 /*
  * Created On: Nov 29, 2005
  */
 package com.thinkparity.desdemona.model.contact;
 
 import java.io.UnsupportedEncodingException;
 import java.util.*;
 
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.filter.Filter;
 import com.thinkparity.codebase.filter.FilterManager;
 import com.thinkparity.codebase.jabber.JabberId;
 
 import com.thinkparity.codebase.model.artifact.Artifact;
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.contact.*;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.document.DocumentVersion;
 import com.thinkparity.codebase.model.user.User;
 import com.thinkparity.codebase.model.util.xmpp.event.*;
 
 import com.thinkparity.desdemona.model.AbstractModelImpl;
 import com.thinkparity.desdemona.model.Constants;
 import com.thinkparity.desdemona.model.backup.InternalBackupModel;
 import com.thinkparity.desdemona.model.contact.invitation.Attachment;
 import com.thinkparity.desdemona.model.contact.invitation.ContainerVersionAttachment;
 import com.thinkparity.desdemona.model.io.sql.ContactSql;
 import com.thinkparity.desdemona.model.io.sql.InvitationSql;
 import com.thinkparity.desdemona.model.io.sql.UserSql;
 import com.thinkparity.desdemona.model.node.Node;
 import com.thinkparity.desdemona.model.node.NodeService;
 import com.thinkparity.desdemona.model.profile.InternalProfileModel;
 import com.thinkparity.desdemona.model.user.InternalUserModel;
 import com.thinkparity.desdemona.model.user.UserModel;
 
 import com.thinkparity.desdemona.util.DesdemonaProperties;
 import com.thinkparity.desdemona.util.smtp.SMTPService;
 
 /**
  * <b>Title:</b>thinkParity DesdemonaModel Contact Model Implementation<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.5
  */
 public final class ContactModelImpl extends AbstractModelImpl implements
         ContactModel, InternalContactModel {
 
     /** The thinkParity contact io. */
 	private ContactSql contactSql;
 
     /** A default (filters none) invitation attachment filter. */
     private final Filter<Attachment> defaultInvitationAttachmentFilter;
 
     /** The thinkParity invitation io. */
 	private InvitationSql invitationSql;
 
     /** A number of milliseconds to wait for a lock. */
     private long lockInvitationsWait;
 
     /** A node service. */
     private final NodeService nodeService;
 
     /** An instance of <code>SMTPService</code>. */
     private final SMTPService smtpService;
 
     /** A user sql interface. */
     private UserSql userSql;
 
     /**
 	 * Create ContactModelImpl.
 	 * 
 	 */
 	public ContactModelImpl() {
 		super();
         this.defaultInvitationAttachmentFilter = FilterManager.createDefault();
         this.nodeService = NodeService.getInstance();
         this.smtpService = SMTPService.getInstance();
 	}
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#acceptInvitation(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.contact.IncomingEMailInvitation,
      *      java.util.Calendar)
      * 
      */
 	public void acceptInvitation(final IncomingEMailInvitation invitation,
             final Calendar acceptedOn) {
 		try {
 		    final Node node = nodeService.getNode();
 		    final User invitationUser = getUserModel().read(
 		            invitation.getExtendedBy().getId());
 		    final List<ContactInvitation> invitationList = lockInvitationsForAccept(
 		            node, invitation);
             try {
                 if (0 < invitationList.size()) {
                     final IncomingEMailInvitation localInvitation =
                         invitationSql.readIncomingEMail(user,
                                 invitation.getInvitationEMail(), invitationUser);
                     if (null == localInvitation) {
                         logger.logInfo("Incoming e-mail invitation ({0}) has been processed.",
                                 invitation.getInvitationEMail());
                     } else {
                         logger.logInfo("Processing e-mail invitation ({0}).",
                                 invitation.getInvitationEMail());
 
                         // update permanent record
                        userSql.updateInvitation(user, localInvitation, acceptedOn);
 
                         // create contact
                         contactSql.create(user, invitationUser, user, acceptedOn);
                         contactSql.create(invitationUser, user, user, acceptedOn);
 
                         /* check the outgoing e-mail invitation for any attachments, and
                          * send to the invitation user if they exist */
                         final OutgoingEMailInvitation outgoingEMailInvitation =
                             invitationSql.readOutgoingEMail(invitationUser,
                                     localInvitation.getInvitationEMail());
                         final List<Attachment> attachments = invitationSql.readAttachments(
                                 outgoingEMailInvitation);
 
                         /* check for an attachments on an outgoing e-mail invitation in the
                          * other direction; if it exists */
                         final List<EMail> invitationUserEMails =
                             getProfileModel().readEMails(user.getId(), invitationUser);
                         for (final EMail invitationUserEMail : invitationUserEMails) {
                             final OutgoingEMailInvitation outgoingPrime =
                                 invitationSql.readOutgoingEMail(user, invitationUserEMail);
                             logger.logDebug("outgoingPrime", outgoingPrime);
                             if (null != outgoingPrime) {
                                 final List<Attachment> attachmentsPrime =
                                     invitationSql.readAttachments(outgoingPrime);
                                 for (final Attachment attachmentPrime : attachmentsPrime) {
                                     invitationSql.deleteAttachment(attachmentPrime);
                                     send(user, invitationUser, outgoingPrime, attachmentPrime);
                                 }
                             }
                         }
 
                         // delete all invitations
                         deleteAllInvitations(node, user.getId(), user, invitationUser);
 
                         // fire event invitation event
                         final ContactInvitationAcceptedEvent event = new ContactInvitationAcceptedEvent();
                         event.setAcceptedBy(user.getId());
                         event.setAcceptedOn(acceptedOn);
                         event.setDate(event.getAcceptedOn());
                         enqueueEvent(invitationUser, event);
                         // send attachments
                         for (final Attachment attachment : attachments) {
                             send(invitationUser, user, localInvitation, attachment);
                         }
                     }
                 } else {
                     invitationSql.unlock(node, invitationList);
                     logger.logInfo("Incoming e-mail invitation ({0}) has been processed.",
                             invitation.getInvitationEMail());
                 }
             } catch (final Exception x) {
                 invitationSql.unlock(node, invitationList);
                 throw x;
             }
         }
 		catch(final Throwable t) {
             throw translateError(t);
 		}
 	}
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#acceptInvitation(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.contact.IncomingUserInvitation,
      *      java.util.Calendar)
      * 
      */
     public void acceptInvitation(final IncomingUserInvitation invitation,
             final Calendar acceptedOn) {
         try {
             final Node node = nodeService.getNode();
             final User invitationUser = getUserModel().read(invitation.getExtendedBy().getId());
             final List<ContactInvitation> invitationList = lockInvitationsForAccept(
                     node, invitation);
             try {
                 if (0 < invitationList.size()) {
                     final IncomingUserInvitation localInvitation =
                         invitationSql.readIncomingUser(user, invitationUser);
                     if (null == localInvitation) {
                         logger.logInfo("Incoming user invitation ({0}) has been processed.",
                                 invitation.getInvitationUser().getSimpleUsername());
                     } else {
                         logger.logInfo("Processing incoming user invitation ({0}).",
                                 invitation.getInvitationUser().getSimpleUsername());
 
                         // update permanent record
                        userSql.updateInvitation(user, localInvitation, acceptedOn);
 
                         // create contact
                         contactSql.create(user, invitationUser, user, acceptedOn);
                         contactSql.create(invitationUser, user, user, acceptedOn);
 
                         // delete incoming/outgoing/user/e-mail
                         deleteAllInvitations(node, user.getId(), user, invitationUser);
                 
                         // fire event
                         final ContactInvitationAcceptedEvent event = new ContactInvitationAcceptedEvent();
                         event.setAcceptedBy(user.getId());
                         event.setAcceptedOn(acceptedOn);
                         event.setDate(event.getAcceptedOn());
                         enqueueEvent(invitationUser, event);
                     }
                 } else {
                     invitationSql.unlock(node, invitationList);
                     logger.logInfo("Incoming user invitation ({0}) has been processed.",
                             invitation.getInvitationUser().getSimpleUsername());
                 }
             } catch (final Exception x) {
                 invitationSql.unlock(node, invitationList);
                 throw x;
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.InternalContactModel#createInvitation(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.contact.IncomingEMailInvitation)
      * 
      */
     public void createInvitation(final JabberId userId,
             final JabberId invitationUserId, final IncomingEMailInvitation invitation) {
         try {
             final UserModel userModel = getUserModel();
             final User user = userModel.read(userId);
             // create invitation
             invitationSql.create(user, invitation);
             // fire event
             final ContactEMailInvitationExtendedEvent event = new ContactEMailInvitationExtendedEvent();
             event.setInvitedAs(invitation.getInvitationEMail());
             event.setInvitedBy(invitation.getExtendedBy().getId());
             event.setInvitedOn(invitation.getCreatedOn());
             enqueueEvent(userId, event);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#createInvitation(com.thinkparity.codebase.model.contact.OutgoingEMailInvitation)
      * 
      */
     public void createInvitation(final OutgoingEMailInvitation invitation) {
         try {
             final UserModel userModel = getUserModel();
             final User createdBy = userModel.read(invitation.getCreatedBy().getId());
 
             // create outgoing e-mail invitation
             invitation.setCreatedBy(createdBy);
             invitationSql.create(user, invitation);
 
             final User invitationUser = userModel.read(invitation.getInvitationEMail());
             if (null == invitationUser) {
                 logger.logInfo("User for {0} does not exist.",
                         invitation.getInvitationEMail());
             } else {
                 // create incoming
                 final IncomingEMailInvitation incoming = new IncomingEMailInvitation();
                 incoming.setCreatedBy(createdBy);
                 incoming.setCreatedOn(invitation.getCreatedOn());
                 incoming.setInvitationEMail(invitation.getInvitationEMail());
                 incoming.setExtendedBy(user);
                 invitationSql.create(invitationUser, incoming);
 
                 // fire event
                 final ContactEMailInvitationExtendedEvent event = new ContactEMailInvitationExtendedEvent();
                 event.setInvitedAs(incoming.getInvitationEMail());
                 event.setInvitedBy(incoming.getExtendedBy().getId());
                 event.setInvitedOn(incoming.getCreatedOn());
                 enqueueEvent(invitationUser, event);
             }
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#createInvitation(com.thinkparity.codebase.jabber.JabberId, com.thinkparity.codebase.model.contact.OutgoingUserInvitation)
      *
      */
     public void createInvitation(final OutgoingUserInvitation invitation) {
         try {
             final UserModel userModel = getUserModel();
             final User invitationUser = userModel.read(invitation.getInvitationUser().getId());
 
             // create outgoing user invitation
             final OutgoingUserInvitation outgoingUser = new OutgoingUserInvitation();
             outgoingUser.setCreatedBy(user);
             outgoingUser.setCreatedOn(invitation.getCreatedOn());
             outgoingUser.setInvitationUser(invitationUser);
             invitationSql.create(user, outgoingUser);
 
             // create incoming
             final IncomingUserInvitation incomingUser = new IncomingUserInvitation();
             incomingUser.setCreatedBy(user);
             incomingUser.setCreatedOn(invitation.getCreatedOn());
             incomingUser.setExtendedBy(incomingUser.getCreatedBy());
             invitationSql.create(invitationUser, incomingUser);
 
             // fire event
             final ContactUserInvitationExtendedEvent event = new ContactUserInvitationExtendedEvent();
             event.setDate(invitation.getCreatedOn());
             event.setInvitedBy(incomingUser.getExtendedBy().getId());
             event.setInvitedOn(incomingUser.getCreatedOn());
             enqueueEvent(invitationUser, event);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.InternalContactModel#createInvitationAttachment(com.thinkparity.codebase.model.contact.OutgoingEMailInvitation,
      *      com.thinkparity.desdemona.model.contact.invitation.ContainerVersionAttachment)
      * 
      */
     public void createInvitationAttachment(
             final OutgoingEMailInvitation invitation,
             final ContainerVersionAttachment attachment) {
         try {
             invitationSql.createAttachment(attachment);
 
             final InternalUserModel userModel = getUserModel();
             final User invitationUser = userModel.read(invitation.getInvitationEMail());
             if (null == invitationUser) {
                 final ContainerVersion version =
                     getBackupModel().readContainerVersionAuth(
                             attachment.getUniqueId(), attachment.getVersionId());
                 // send e-mail
                 final MimeMessage mimeMessage = smtpService.createMessage();
                 inject(mimeMessage, userModel.read(invitation.getCreatedBy().getLocalId()), version);
                 addRecipient(mimeMessage, invitation.getInvitationEMail());
                 smtpService.deliver(mimeMessage);
             } else {
                 logger.logInfo("User {0} for {1} exists.", user.getUsername(),
                         invitation.getInvitationEMail());
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#declineInvitation(com.thinkparity.codebase.model.contact.IncomingEMailInvitation,
      *      java.util.Calendar)
      * 
      */
     public void declineInvitation(final IncomingEMailInvitation invitation,
             final Calendar declinedOn) {
         try {
             final Node node = nodeService.getNode();
             final User invitationUser = getUserModel().read(
                     invitation.getExtendedBy().getId());
             final List<ContactInvitation> invitationList = lockInvitationsForDecline(
                     node, invitation);
             try {
                 if (0 < invitationList.size()) {
                     // delete incoming e-mail invitation
                     final IncomingEMailInvitation incomingEMailInvitation =
                         invitationSql.readIncomingEMail(user,
                                 invitation.getInvitationEMail(), invitationUser);
                     invitationSql.delete(node, user, incomingEMailInvitation);
 
                     // delete outgoing e-mail invitation
                     final OutgoingEMailInvitation outgoingEMailInvitation =
                         invitationSql.readOutgoingEMail(invitationUser,
                                 invitation.getInvitationEMail());
                     final List<ContainerVersionAttachment> attachments =
                         invitationSql.readContainerVersionAttachments(outgoingEMailInvitation);
                     invitationSql.deleteAttachments(outgoingEMailInvitation);
                     invitationSql.delete(node, outgoingEMailInvitation);
                     final InternalBackupModel backupModel = getBackupModel();
                     for (final ContainerVersionAttachment attachment : attachments) {
                         if (!doesExistAttachment(attachment.getUniqueId())) {
                             backupModel.delete(attachment.getUniqueId());
                         }
                     }
         
                     // fire event
                     final ContactEMailInvitationDeclinedEvent event = new ContactEMailInvitationDeclinedEvent();
                     event.setDate(declinedOn);
                     event.setDeclinedBy(user.getId());
                     event.setDeclinedOn(event.getDate());
                     event.setInvitedAs(invitation.getInvitationEMail());
                     enqueueEvent(invitationUser, event);
                 } else {
                     logger.logInfo("Invitation {0} has been processed.",
                             invitation.getId());
                 }
             } catch (final Exception x) {
                 invitationSql.unlock(node, invitationList);
                 throw x;
             }
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#declineInvitation(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.contact.IncomingUserInvitation,
      *      java.util.Calendar)
      * 
      */
     public void declineInvitation(final IncomingUserInvitation invitation,
             final Calendar declinedOn) {
         try {
             final Node node = nodeService.getNode();
             final User invitationUser = getUserModel().read(
                     invitation.getExtendedBy().getId());
             final List<ContactInvitation> invitationList = lockInvitationsForDecline(
                     node, invitation);
             try {
                 if (0 < invitationList.size()) {
                     // delete incoming user invitation
                     final IncomingUserInvitation incomingUserInvitation =
                         invitationSql.readIncomingUser(user, invitationUser);
                     invitationSql.delete(node, user, incomingUserInvitation);
         
                     // delete outgoing user invitation
                     final OutgoingUserInvitation outgoingUserInvitation =
                         invitationSql.readOutgoingUser(invitationUser, user);
                     invitationSql.delete(node, outgoingUserInvitation);
         
                     // fire event
                     final ContactUserInvitationDeclinedEvent event = new ContactUserInvitationDeclinedEvent();
                     event.setDate(declinedOn);
                     event.setDeclinedBy(user.getId());
                     event.setDeclinedOn(event.getDate());
                     enqueueEvent(invitationUser, event);
                 } else {
                     logger.logInfo("Invitation {0} has been processed.",
                             invitation.getId());
                 }
             } catch (final Exception x) {
                 invitationSql.unlock(node, invitationList);
                 throw x;
             }
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#delete(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.jabber.JabberId)
      * 
      */
     public void delete(final JabberId id) {
         try {
             delete(user.getId(), id);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#deleteInvitation(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.contact.OutgoingEMailInvitation,
      *      java.util.Calendar)
      * 
      */
     public void deleteInvitation(final OutgoingEMailInvitation invitation,
             final Calendar deletedOn) {
         try {
             final Node node = nodeService.getNode();
             final User invitationUser = getUserModel().read(
                     invitation.getInvitationEMail());
             final List<ContactInvitation> invitationList = lockInvitationsForDelete(
                     node, invitation);
             try {
                 if (0 < invitationList.size()) {
                     // delete incoming e-mail invitation
                     if (null == invitationUser) {
                         logger.logInfo("No inivation user exists.");
                     } else {
                         final IncomingEMailInvitation incomingEMail =
                             invitationSql.readIncomingEMail(invitationUser,
                                     invitation.getInvitationEMail(), user);
                         invitationSql.delete(node, invitationUser, incomingEMail);
                     }
         
                     final OutgoingEMailInvitation outgoingEMail =
                         invitationSql.readOutgoingEMail(user,
                                 invitation.getInvitationEMail());
                     // delete attachments
                     final List<Attachment> attachments =
                         invitationSql.readAttachments(outgoingEMail);
                     for (final Attachment attachment : attachments)
                         invitationSql.deleteAttachment(attachment);
                     // delete outgoing e-mail invitation
                     invitationSql.delete(node, outgoingEMail);
         
                     // fire event
                     // delete incoming e-mail invitation
                     if (null == invitationUser) {
                         logger.logInfo("No inivation user exists.");
                     } else {
                         final ContactEMailInvitationDeletedEvent event = new ContactEMailInvitationDeletedEvent();
                         event.setDate(deletedOn);
                         event.setDeletedBy(user.getId());
                         event.setDeletedOn(event.getDate());
                         event.setInvitedAs(invitation.getInvitationEMail());
                         enqueueEvent(invitationUser, event);
                     }
                 } else {
                     logger.logInfo("Invitation {0} has been processed.",
                             invitation.getId());
                 }
             } catch (final Exception x) {
                 invitationSql.unlock(node, invitationList);
                 throw x;
             }
         } catch(final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#deleteOutgoingUserInvitation(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.contact.OutgoingUserInvitation,
      *      java.util.Calendar)
      * 
      */
     public void deleteInvitation(final OutgoingUserInvitation invitation,
             final Calendar deletedOn) {
         try {
             final Node node = nodeService.getNode();
             final User invitationUser = getUserModel().read(
                     invitation.getInvitationUser().getId());
             final List<ContactInvitation> invitationList = lockInvitationsForDelete(
                     node, invitation);
             try {
                 if (0 < invitationList.size()) {
                     // delete incoming user invitation
                     final IncomingUserInvitation incomingUserInvitation =
                         invitationSql.readIncomingUser(invitationUser, user);
                     invitationSql.delete(node, invitationUser, incomingUserInvitation);
         
                     // delete outgoing user invitation
                     final OutgoingUserInvitation outgoingUserInvitation =
                         invitationSql.readOutgoingUser(user, invitationUser);
                     invitationSql.delete(node, outgoingUserInvitation);
         
                     // fire event
                     final ContactUserInvitationDeletedEvent event = new ContactUserInvitationDeletedEvent();
                     event.setDate(deletedOn);
                     event.setDeletedBy(user.getId());
                     event.setDeletedOn(event.getDate());
                     enqueueEvent(invitationUser, event);
                 } else {
                     logger.logInfo("Invitation {0} has been processed.",
                             invitation.getId());
                 }
             } catch (final Exception x) {
                 invitationSql.unlock(node, invitationList);
                 throw x;
             }
         } catch(final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.InternalContactModel#deleteInvitationAttachment(com.thinkparity.desdemona.model.contact.invitation.Attachment)
      *
      */
     public void deleteInvitationAttachment(final Attachment attachment) {
         try {
             invitationSql.deleteAttachment(attachment);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.InternalContactModel#doesExistAttachment(com.thinkparity.codebase.model.container.Container)
      * 
      */
     public Boolean doesExistAttachment(final Artifact artifact) {
         return Boolean.valueOf(doesExistAttachment(artifact.getUniqueId()));
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#read(com.thinkparity.codebase.jabber.JabberId)
      * 
      */
     public List<Contact> read() {
 		try {
 			final List<JabberId> contactIds = contactSql.readIds(user.getLocalId());
 			final List<Contact> contacts = new LinkedList<Contact>();
 			for (final JabberId contactId : contactIds) {
 				contacts.add(read(user.getId(), contactId));
 			}
 			return contacts;
 		} catch (final Throwable t) {
 			throw panic(t);
 		}
 	}
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#read(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.jabber.JabberId)
      * 
      */
 	public Contact read(final JabberId contactId) {
 		try {
             return read(user.getId(), contactId);
 	    } catch (final Throwable t) {
             throw panic(t);
         }
 	}
 
     /**
      * @see com.thinkparity.desdemona.model.contact.InternalContactModel#readContainerVersionInvitationAttachments(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.contact.ContactInvitation)
      * 
      */
     public List<ContainerVersionAttachment> readContainerVersionInvitationAttachments(
             final JabberId userId, final ContactInvitation invitation) {
         try {
             final List<Attachment> attachments = readInvitationAttachments(
                     userId, invitation, new Filter<Attachment>() {
                         public Boolean doFilter(final Attachment o) {
                             return Attachment.ReferenceType.CONTAINER_VERSION !=
                                 o.getReferenceType();
                         }
                     });
             final List<ContainerVersionAttachment> cvas =
                 new ArrayList<ContainerVersionAttachment>(attachments.size());
             for (final Attachment attachment : attachments) {
                 final ContainerVersionAttachment cva = new ContainerVersionAttachment();
                 cva.setInvitationId(attachment.getInvitationId());
                 cva.setReferenceId(attachment.getReferenceId());
                 cvas.add(cva);
             }
             return cvas;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#readIncomingEMailInvitations(com.thinkparity.codebase.jabber.JabberId)
      * 
      */
     public List<IncomingEMailInvitation> readIncomingEMailInvitations() {
         try {
             return invitationSql.readIncomingEMail(user);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#readIncomingUserInvitations(com.thinkparity.codebase.jabber.JabberId)
      * 
      */
     public List<IncomingUserInvitation> readIncomingUserInvitations() {
         try {
             return invitationSql.readIncomingUser(user);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.InternalContactModel#readInvitationAttachments(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.contact.ContactInvitation)
      * 
      */
     public List<Attachment> readInvitationAttachments(final JabberId userId,
             final ContactInvitation invitation) {
         try {
             return readInvitationAttachments(userId, invitation,
                     defaultInvitationAttachmentFilter);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#readOutgoingEMailInvitations(com.thinkparity.codebase.jabber.JabberId)
      * 
      */
     public List<OutgoingEMailInvitation> readOutgoingEMailInvitations() {
         try {
             return readOutgoingEMailInvitations(user);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#readOutgoingEMailInvitations(com.thinkparity.codebase.model.container.ContainerVersion)
      *
      */
     public List<OutgoingEMailInvitation> readOutgoingEMailInvitations(
             final ContainerVersion version) {
         try {
             final ContainerVersionAttachment attachment = new ContainerVersionAttachment();
             attachment.setUniqueId(version.getArtifactUniqueId());
             attachment.setVersionId(version.getVersionId());
             return invitationSql.readOutgoingEMail(user, attachment);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.InternalContactModel#readOutgoingEMailInvitations(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.email.EMail)
      * 
      */
     public List<OutgoingEMailInvitation> readOutgoingEMailInvitations(
             final JabberId userId, final EMail email) {
         try {
             return invitationSql.readOutgoingEMail(email);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactModel#readOutgoingUserInvitations(com.thinkparity.codebase.jabber.JabberId)
      * 
      */
     public List<OutgoingUserInvitation> readOutgoingUserInvitations() {
         try {
             return invitationSql.readOutgoingUser(user);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.InternalContactModel#readProxyOutgoingEMailInvitations(com.thinkparity.codebase.jabber.JabberId)
      *
      */
     public List<OutgoingEMailInvitation> readProxyOutgoingEMailInvitations(
             final JabberId proxyId) {
         try {
             final User proxyUser = getUserModel().read(proxyId);
 
             return readOutgoingEMailInvitations(proxyUser);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.AbstractModelImpl#initialize()
      *
      */
     @Override
     protected void initialize() {
         contactSql = new ContactSql();
         invitationSql = new InvitationSql();
         userSql = new UserSql();
 
         final DesdemonaProperties properties = DesdemonaProperties.getInstance();
         lockInvitationsWait = Long.valueOf(properties.getProperty("thinkparity.contact.lockinvitationswait"));
     }
 
     /**
      * Read a user.
      * 
      * @param userId
      *            A user id <code>JabberId</code>.
      * @return A <code>User</code>.
      */
     protected final User readUser(final JabberId userId) {
         return getUserModel().read(userId);
     }
 
     /**
      * Obtain the invitation sql.
      * 
      * @return An <code>InvitationSql</code>.
      */
     InvitationSql getInvitationSql() {
         return invitationSql;
     }
 
     /**
      * Obtain the node service.
      * 
      * @return A <code>NodeService</code>.
      */
     NodeService getNodeService() {
         return nodeService;
     }
 
     /**
      * Delete a contact for a user.
      * 
      * @param userId
      *            A user id <code>JabberId</code>.
      * @param contactId
      *            A contact id <code>JabberId</code>.
      */
     private void delete(final JabberId userId, final JabberId contactId) {
         final UserModel userModel = getUserModel();
         final User user = userModel.read(userId);
         final User contact = userModel.read(contactId);
 
         // delete contact
         contactSql.delete(user.getLocalId(), contact.getLocalId());
 
         // fire event
         final ContactDeletedEvent deleted = new ContactDeletedEvent();
         deleted.setDeletedBy(userId);
         deleted.setDeletedOn(currentDateTime());
         enqueueEvent(contactId, deleted);
     }
 
     /**
      * Delete all incoming and outgoing user and e-mail invitations between two
      * users.
      * 
      * @param userId
      *            The current user id <code>JabberId</code>.
      * @param user
      *            A <code>User</code>.
      * @param invitationUser
      *            A <code>User</code>.
      */
     private void deleteAllInvitations(final Node node, final JabberId userId,
             final User user, final User invitationUser) {
         final InternalProfileModel profileModel = getProfileModel();
         final List<EMail> userEMails = profileModel.readEMails(userId, user);
         final List<EMail> invitationUserEMails = profileModel.readEMails(userId,
                 invitationUser);
 
         // delete incoming e-mail invitations
         IncomingEMailInvitation incomingEMailInvitation;
         for (final EMail email : userEMails) {
             incomingEMailInvitation = invitationSql.readIncomingEMail(user,
                     email, invitationUser);
             if (null != incomingEMailInvitation)
                 invitationSql.delete(node, user, incomingEMailInvitation);
         }
         for (final EMail email : invitationUserEMails) {
             incomingEMailInvitation = invitationSql.readIncomingEMail(
                     invitationUser, email, user);
             if (null != incomingEMailInvitation)
                 invitationSql.delete(node, invitationUser, incomingEMailInvitation);
         }
 
         // delete outgoing e-mail invitations
         OutgoingEMailInvitation outgoingEMailInvitation;
         for (final EMail email : invitationUserEMails) {
             outgoingEMailInvitation = invitationSql.readOutgoingEMail(user,
                     email);
             if (null != outgoingEMailInvitation) {
                 invitationSql.deleteAttachments(outgoingEMailInvitation);
                 invitationSql.delete(node, outgoingEMailInvitation);
             }
         }
         for (final EMail email : userEMails) {
             outgoingEMailInvitation = invitationSql.readOutgoingEMail(
                     invitationUser, email);
             if (null != outgoingEMailInvitation) {
                 invitationSql.deleteAttachments(outgoingEMailInvitation);
                 invitationSql.delete(node, outgoingEMailInvitation);
             }
         }
 
         // delete incoming user invitations
         IncomingUserInvitation incomingUserInvitation;
         incomingUserInvitation = invitationSql.readIncomingUser(user,
                 invitationUser);
         if (null != incomingUserInvitation)
             invitationSql.delete(node, user, incomingUserInvitation);
         incomingUserInvitation = invitationSql.readIncomingUser(
                 invitationUser, user);
         if (null != incomingUserInvitation)
             invitationSql.delete(node, invitationUser, incomingUserInvitation);
 
         // delete outgoing user invitations
         OutgoingUserInvitation outgoingUserInvitation;
         outgoingUserInvitation = invitationSql.readOutgoingUser(user,
                 invitationUser);
         if (null != outgoingUserInvitation)
             invitationSql.delete(node, outgoingUserInvitation);
         outgoingUserInvitation = invitationSql.readOutgoingUser(
                 invitationUser, user);
         if (null != outgoingUserInvitation)
             invitationSql.delete(node, outgoingUserInvitation);
     }
 
     /**
      * Determine whether or not an the container is attached to an outgoing
      * e-mail invitation for the user.
      * 
      * @param uniqueId
      *            A <code>UUID</code>.
      * @return True if at least one attachment exists.
      */
     private boolean doesExistAttachment(final UUID uniqueId) {
         final List<OutgoingEMailInvitation> invitations =
             readOutgoingEMailInvitations();
         final List<ContainerVersionAttachment> attachments =
             new ArrayList<ContainerVersionAttachment>();
         ContainerVersionAttachment attachment;
         for (final OutgoingEMailInvitation invitation : invitations) {
             attachments.clear();
             attachments.addAll(readContainerVersionInvitationAttachments(
                     user.getId(), invitation));
             for (final Iterator<ContainerVersionAttachment> iAttachments =
                     attachments.iterator(); iAttachments.hasNext();) {
                 attachment = iAttachments.next();
                 if (attachment.getUniqueId().equals(uniqueId)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Inject invitation text into the e-mail mime message.
      * 
      * @param mimeMessage
      *            A <code>MimeMessage</code>.
      * @param invitation
      *            An <code>OutgoingEMailInvitation</code>.
      * @param invitedBy
      *            An invitation <code>User</code>.
      * @param attachment
      *            A <code>ContainerVersion</code>.
      * @throws MessagingException
      * @throws UnsupportedEncodingException
      */
 	private void inject(final MimeMessage mimeMessage, final User invitedBy,
             final ContainerVersion attachment) throws MessagingException,
             UnsupportedEncodingException {
 	    final Locale locale = Locale.getDefault();
 	    final InvitationText text = new InvitationText(locale, invitedBy,
 	            attachment);
 
         final InternetAddress fromInternetAddress = new InternetAddress();
         fromInternetAddress.setAddress(Constants.Internet.Mail.FROM_ADDRESS);
         fromInternetAddress.setPersonal(text.getFromPersonal());
         mimeMessage.setFrom(fromInternetAddress);
 	    mimeMessage.setSubject(text.getSubject());
 
         final MimeBodyPart mimeBodyPart = new MimeBodyPart();
         mimeBodyPart.setContent(text.getBody(), text.getBodyType());
 
         final Multipart mimeMultipart = new MimeMultipart();
         mimeMultipart.addBodyPart(mimeBodyPart);
         mimeMessage.setContent(mimeMultipart);
     }
 
     /**
      * Lock invitations for a node.
      * 
      * @param node
      *            A <code>Node</code>.
      * @param lockReader
      *            A <code>InvitationLockReader</code>.
      * @return A <code>List<ContactInvitation></code>.
      * @throws CannotLockException
      */
     private List<ContactInvitation> lockInvitations(final Node node,
             final InvitationLockReader lockReader) throws CannotLockException {
         final List<ContactInvitation> invitationList = lockReader.read();
         if (invitationSql.lock(nodeService.getNode(), invitationList)) {
             return invitationList;
         } else {
             try {
                 Thread.sleep(lockInvitationsWait);
             } catch (final InterruptedException ix) {
                 logger.logWarning("Could not wait for invitation lock release.",
                         ix.getMessage());
             }
             /* in the interim; invitations may have been deleted */
             invitationList.clear();
             invitationList.addAll(lockReader.read());
             if (invitationSql.lock(node, invitationList)) {
                 return invitationList;
             } else {
                 logger.logError("Cannot lock invitations.");
                 throw new CannotLockException();
             }
         }
     }
 
     /**
      * Lock all invitations for accepting an incoming e-mail invitation.
      * 
      * @param invitation
      *            An <code>IncomingEMailInvitation</code>.
      * @return A <code>List<ContactInvitation></code>.
      */
     private List<ContactInvitation> lockInvitationsForAccept(final Node node,
             final IncomingEMailInvitation invitation) throws CannotLockException {
         final List<ContactInvitation> invitationList = new ArrayList<ContactInvitation>();
         return lockInvitations(node, new InvitationLockReader() {
             /**
              * @see com.thinkparity.desdemona.model.contact.InvitationLockReader#read()
              *
              */
             @Override
             public List<ContactInvitation> read() {
                 invitationList.clear();
                 final User invitationUser = getUserModel().read(invitation.getExtendedBy().getId());
                 final IncomingEMailInvitation localInvitation =
                     invitationSql.readIncomingEMail(user,
                             invitation.getInvitationEMail(), invitationUser);
                 if (null == localInvitation) {
                     logger.logInfo("Incoming e-mail invitation ({0}) has been processed.",
                             invitation.getInvitationEMail());
                 } else {
                     logger.logInfo("Processing incoming e-mail invitation ({0}).",
                             invitation.getInvitationEMail());
                     invitationList.addAll(readAll(user, invitationUser));
                 }
                 return invitationList;
             }
         });
     }
 
     /**
      * Lock all invitations for accepting an incoming user invitation.
      * 
      * @param invitation
      *            An <code>IncomingUserInvitation</code>.
      * @return A <code>List<ContactInvitation></code>.
      */
     private List<ContactInvitation> lockInvitationsForAccept(final Node node,
             final IncomingUserInvitation invitation) throws CannotLockException {
         final List<ContactInvitation> invitationList = new ArrayList<ContactInvitation>();
         return lockInvitations(node, new InvitationLockReader() {
             /**
              * @see com.thinkparity.desdemona.model.contact.InvitationLockReader#read()
              *
              */
             @Override
             public List<ContactInvitation> read() {
                 invitationList.clear();
                 final User invitationUser = getUserModel().read(invitation.getExtendedBy().getId());
                 final IncomingUserInvitation localInvitation =
                     invitationSql.readIncomingUser(user, invitationUser);
                 if (null == localInvitation) {
                     logger.logInfo("Incoming user invitation ({0}) has been processed.",
                             invitation.getInvitationUser().getSimpleUsername());
                 } else {
                     logger.logInfo("Processing incoming user invitation ({0}).",
                             invitation.getInvitationUser().getSimpleUsername());
                     invitationList.addAll(readAll(user, invitationUser));
                 }
                 return invitationList;
             }
         });
     }
 
     /**
      * Lock invitations for decline.
      * 
      * @param node
      *            A <code>Node</code>.
      * @param invitation
      *            A <code>IncomingEMailInvitation</code>.
      * @return A <code>List<ContactInvitation></code>.
      * @throws CannotLockException
      */
     private List<ContactInvitation> lockInvitationsForDecline(final Node node,
             final IncomingEMailInvitation invitation) throws CannotLockException {
         final List<ContactInvitation> invitationList = new ArrayList<ContactInvitation>();
         return lockInvitations(node, new InvitationLockReader() {
             /**
              * @see com.thinkparity.desdemona.model.contact.InvitationLockReader#read()
              *
              */
             @Override
             public List<ContactInvitation> read() {
                 final User invitationUser = getUserModel().read(invitation.getExtendedBy().getId());
                 invitationList.clear();
                 final IncomingEMailInvitation localInvitation =
                     invitationSql.readIncomingEMail(user,
                             invitation.getInvitationEMail(), invitationUser);
                 if (null == localInvitation) {
                     logger.logInfo("Incoming e-mail invitation ({0}) has been processed.",
                             invitation.getInvitationEMail());
                 } else {
                     logger.logInfo("Processing incoming e-mail invitation ({0}).",
                             invitation.getInvitationEMail());
                     invitationList.add(localInvitation);
                     invitationList.add(invitationSql.readOutgoingEMail(invitationUser,
                             invitation.getInvitationEMail()));
                 }
                 return invitationList;
             }
         });
     }
 
     /**
      * Lock invitations for decline.
      * 
      * @param node
      *            A <code>Node</code>.
      * @param invitation
      *            A <code>IncomingUserInvitation</code>.
      * @return A <code>List<ContactInvitation></code>.
      * @throws CannotLockException
      */
     private List<ContactInvitation> lockInvitationsForDecline(final Node node,
             final IncomingUserInvitation invitation) throws CannotLockException {
         final List<ContactInvitation> invitationList = new ArrayList<ContactInvitation>();
         return lockInvitations(node, new InvitationLockReader() {
             /**
              * @see com.thinkparity.desdemona.model.contact.InvitationLockReader#read()
              *
              */
             @Override
             public List<ContactInvitation> read() {
                 final User invitationUser = getUserModel().read(invitation.getExtendedBy().getId());
                 invitationList.clear();
                 final IncomingUserInvitation localInvitation =
                     invitationSql.readIncomingUser(user, invitationUser);
                 if (null == localInvitation) {
                     logger.logInfo("Incoming user invitation ({0}) has been processed.",
                             invitation.getInvitationUser().getSimpleUsername());
                 } else {
                     logger.logInfo("Processing incoming user invitation ({0}).",
                             invitation.getInvitationUser().getSimpleUsername());
                     invitationList.add(localInvitation);
                     invitationList.add(invitationSql.readOutgoingUser(invitationUser, user));
                 }
                 return invitationList;
             }
         });
     }
 
     /**
      * Lock invitations for delete.
      * 
      * @param node
      *            A <code>Node</code>.
      * @param invitation
      *            A <code>OutgoingEMailInvitation</code>.
      * @return A <code>List<ContactInvitation></code>.
      * @throws CannotLockException
      */
     private List<ContactInvitation> lockInvitationsForDelete(final Node node,
             final OutgoingEMailInvitation invitation) throws CannotLockException {
         final List<ContactInvitation> invitationList = new ArrayList<ContactInvitation>();
         return lockInvitations(node, new InvitationLockReader() {
             /**
              * @see com.thinkparity.desdemona.model.contact.InvitationLockReader#read()
              *
              */
             @Override
             public List<ContactInvitation> read() {
                 final User invitationUser = getUserModel().read(invitation.getInvitationEMail());
                 invitationList.clear();
                 final OutgoingEMailInvitation localInvitation =
                     invitationSql.readOutgoingEMail(user, invitation.getInvitationEMail());
                 if (null == localInvitation) {
                     logger.logInfo("Outgoing e-mail invitation ({0}) has been processed.",
                             invitation.getInvitationEMail());
                 } else {
                     logger.logInfo("Processing outgoing e-mail invitation ({0}).",
                             invitation.getInvitationEMail());
                     invitationList.add(localInvitation);
                     if (null == invitationUser) {
                         logger.logInfo("E-mail invitation ({0}) does not correspond to a user.",
                                 invitation.getInvitationEMail());
                     } else {
                         logger.logInfo("E-mail invitation ({0}) corresponds to {1}.",
                                 invitation.getInvitationEMail(), invitationUser.getSimpleUsername());
                         invitationList.add(invitationSql.readIncomingEMail(invitationUser, invitation.getInvitationEMail(), user));
                     }
                 }
                 return invitationList;
             }
         });
     }
 
     /**
      * Lock invitations for delete.
      * 
      * @param node
      *            A <code>Node</code>.
      * @param invitation
      *            A <code>OutgoingUserInvitation</code>.
      * @return A <code>List<ContactInvitation></code>.
      * @throws CannotLockException
      */
     private List<ContactInvitation> lockInvitationsForDelete(final Node node,
             final OutgoingUserInvitation invitation) throws CannotLockException {
         final List<ContactInvitation> invitationList = new ArrayList<ContactInvitation>();
         return lockInvitations(node, new InvitationLockReader() {
             /**
              * @see com.thinkparity.desdemona.model.contact.InvitationLockReader#read()
              *
              */
             @Override
             public List<ContactInvitation> read() {
                 final User invitationUser = getUserModel().read(invitation.getInvitationUser().getId());
                 invitationList.clear();
                 final OutgoingUserInvitation localInvitation =
                     invitationSql.readOutgoingUser(user, invitationUser);
                 if (null == localInvitation) {
                     logger.logInfo("Outgoing user invitation ({0}) has been processed.",
                             invitation.getInvitationUser().getSimpleUsername());
                 } else {
                     logger.logInfo("Processing outgoing user invitation ({0}).",
                             invitation.getInvitationUser().getSimpleUsername());
                     invitationList.add(localInvitation);
                     invitationList.add(invitationSql.readIncomingUser(invitationUser, user));
                 }
                 return invitationList;
             }
         });
     }
 
     /**
      * Read a contact for a user.
      * 
      * @param userId
      *            A user id <code>JabberId</code>.
      * @param contactId
      *            A contact id <code>JabberId</code>.
      * @return A <code>Contact</code>.
      */
     private Contact read(final JabberId userId, final JabberId contactId) {
         final Contact contact = inject(new Contact(), getUserModel().read(contactId));
         contact.setVCard(getUserModel(contact).readVCard(new ContactVCard()));
         contact.addAllEmails(getProfileModel().readEMails(userId, contact));
         return contact;
     }
 
     /**
      * Read all invitations between two users.
      * 
      * @param one
      *            A <code>User</code>.
      * @param two
      *            A <code>User</code>.
      * @return A <code>List<ContactInvitation></code>.
      */
     private List<ContactInvitation> readAll(final User one, final User two) {
         final List<ContactInvitation> invitationList = new ArrayList<ContactInvitation>();
         ContactInvitation invitation = null;
         /* from user two to user one via e-mail */
         final List<EMail> oneEMailList = getProfileModel(one).readEMails(one.getId(), one);
         for (final EMail email : oneEMailList) {
             invitation = invitationSql.readIncomingEMail(one, email, two);
             if (null == invitation) {
                 logger.logInfo("No incoming e-mail invitation from {0} to {1} ({2}).",
                         two.getSimpleUsername(), one.getSimpleUsername(), email);
             } else {
                 logger.logInfo("Incoming e-mail invitation from {0} to {1} ({2}).",
                         two.getSimpleUsername(), one.getSimpleUsername(), email);
                 invitationList.add(invitation);
                 invitationList.add(invitationSql.readOutgoingEMail(two, email));
             }
         }
         /* from user one to user two via e-mail */
         final List<EMail> twoEMailList = getProfileModel(two).readEMails(two.getId(), two);
         for (final EMail email : twoEMailList) {
             invitation = invitationSql.readIncomingEMail(two, email, one);
             if (null == invitation) {
                 logger.logInfo("No incoming e-mail invitation from {0} to {1} ({2}).",
                         one.getSimpleUsername(), two.getSimpleUsername(), email);
             } else {
                 logger.logInfo("Incoming e-mail invitation from {0} to {1} ({2}).",
                         one.getSimpleUsername(), two.getSimpleUsername(), email);
                 invitationList.add(invitation);
                 invitationList.add(invitationSql.readOutgoingEMail(one, email));
             }
         }
         /* from user one to user two */
         invitation = invitationSql.readIncomingUser(two, one);
         if (null == invitation) {
             logger.logInfo("No incoming user invitation from {0} to {1}.",
                     one.getSimpleUsername(), two.getSimpleUsername());
         } else {
             logger.logInfo("Incoming user invitation from {0} to {1}.",
                     one.getSimpleUsername(), two.getSimpleUsername());
             invitationList.add(invitation);
             invitationList.add(invitationSql.readOutgoingUser(one, two));
         }
         /* from user two to user one */
         invitation = invitationSql.readIncomingUser(one, two);
         if (null == invitation) {
             logger.logInfo("No incoming user invitation from {0} to {1}.",
                     two.getSimpleUsername(), one.getSimpleUsername());
         } else {
             logger.logInfo("Incoming user invitation from {0} to {1}.",
                     two.getSimpleUsername(), one.getSimpleUsername());
             invitationList.add(invitation);
             invitationList.add(invitationSql.readOutgoingUser(two, one));
         }
         return invitationList;
     }
 
     /**
      * Read a list of invitation attachments.
      * 
      * @param userId
      *            A user id <code>JabberId</code>.
      * @param invitation
      *            A <code>ContactInvitation</code>.
      * @param filter
      *            A <code>Filter<Attachment></code>.
      * @return A <code>List<Attachment></code>.
      */
     private List<Attachment> readInvitationAttachments(final JabberId userId,
             final ContactInvitation invitation, final Filter<Attachment> filter) {
         try {
             // TODO - Add createdBy criteria
             final List<Attachment> attachments = invitationSql.readAttachments(
                     invitation);
             FilterManager.filter(attachments, filter);
             return attachments;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Read the outgoing e-mail address invitations for a user.
      * 
      * @param user
      *            A <code>User</code>.
      * @return A <code>List<OutgoingEMailInvitation></code>.
      */
     private List<OutgoingEMailInvitation> readOutgoingEMailInvitations(
             final User user) {
         return invitationSql.readOutgoingEMail(user);
     }
 
     /**
      * Send an invitation attachment to a user.
      * 
      * @param sendAs
      *            A <code>User</code> to send as.
      * @param sendTo
      *            A <code>User</code> to send to.
      * @param invitation
      *            A <code>ContactInvitation</code>.
      * @param attachment
      *            An <code>Attachment</code>.
      */
     private void send(final User sendAs, final User sendTo,
             final ContactInvitation invitation, final Attachment attachment) {
         switch (attachment.getReferenceType()) {
         case CONTAINER_VERSION:
             final ContainerVersionAttachment containerVersionAttachment =
                 new ContainerVersionAttachment();
             containerVersionAttachment.setReferenceId(attachment.getReferenceId());
             send(sendAs, sendTo, invitation, containerVersionAttachment);
             break;
         default:
             Assert.assertUnreachable("Unknown attachment reference type.");
         }
     }
 
     /**
      * Send a container version invitation attachment to a user.
      * 
      * @param sendAs
      *            A <code>User</code>.
      * @param sendTo
      *            A <code>User</code>.
      * @param invitation
      *            A <code>ContactInvitation</code>.
      * @param attachment
      *            A <code>ContainerVersionAttachment</code>.
      */
     private void send(final User sendAs, final User sendTo,
             final ContactInvitation invitation,
             final ContainerVersionAttachment attachment) {
         final InternalBackupModel backupModel = getBackupModel(sendAs);
         // grab from backup
         final Container container = backupModel.readContainerAuth(
                 attachment.getUniqueId());
         final ContainerVersion version = backupModel.readContainerVersionAuth(
                 container.getUniqueId(), attachment.getVersionId());
         final List<DocumentVersion> documentVersions =
             backupModel.readContainerDocumentVersionsAuth(container.getUniqueId(),
                     version.getVersionId());
         final List<ArtifactReceipt> receivedBy = backupModel.readPublishedToAuth(
                 container.getUniqueId(), version.getVersionId());
         final Calendar publishedOn = version.getCreatedOn();
         final List<EMail> publishToEMails = Collections.emptyList();
         final List<User> publishToUsers = new ArrayList<User>(1);
         publishToUsers.add(sendTo);
         // publish version
         getContainerModel(sendAs).publishVersion(version, documentVersions,
                 receivedBy, publishedOn, publishToEMails, publishToUsers);
     }
 }
