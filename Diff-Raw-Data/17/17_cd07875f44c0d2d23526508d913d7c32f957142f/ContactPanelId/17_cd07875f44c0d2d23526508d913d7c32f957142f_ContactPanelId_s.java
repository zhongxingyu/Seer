 /*
  * Created On:  17-Jan-07 3:40:57 PM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.contact;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.jabber.JabberId;
 
 /**
  * <b>Title:</b>thinkParity Contact Panel Id<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public class ContactPanelId {
 
     /** The contact id <code>JabberId</code>. */
     private final JabberId contactId;
 
     /** The invitation id <code>Long</code>. */
     private final Long invitationId;
 
     /**
      * Create ContactPanelId.
      * 
      * @param contactId
      *            A contact id <code>JabberId</code>.
      */
     ContactPanelId(final JabberId contactId) {
         this(contactId, null);
     }
 
     /**
      * Create ContactPanelId.
      * 
      * @param invitationId
      *            A contact invitation id <code>Long</code>.
      */
     ContactPanelId(final Long invitationId) {
         this(null, invitationId);
     }
 
     /**
      * Create ContactPanelId.
      * 
      * @param contactId
      *            A contact id <code>JabberId</code>.
      * @param invitationId
      *            An invitation id <code>Long</code>.
      */
     private ContactPanelId(final JabberId contactId, final Long invitationId) {
         super();
         this.contactId = contactId;
         this.invitationId = invitationId;
     }
 
     /**
      * @see java.lang.Object#equals(java.lang.Object)
      *
      */
     @Override
     public boolean equals(final Object obj) {
         if (null == obj)
             return false;
         if (this == obj)
             return true;
         if (getClass() != obj.getClass())
             return false;
         final ContactPanelId contactPanelId = (ContactPanelId) obj;
        if (isSetContactId() && contactPanelId.isSetContactId())
             return contactId.equals(contactPanelId.contactId);
         else if (isSetInvitationId() && contactPanelId.isSetInvitationId())
             return invitationId.equals(contactPanelId.invitationId);
         return true;
     }
 
     /**
      * @see java.lang.Object#hashCode()
      *
      */
     @Override
     public int hashCode() {
         if (isSetContactId())
             return contactId.toString().hashCode();
         else if (isSetInvitationId())
             return invitationId.toString().hashCode();
         else
             throw Assert.createUnreachable("Unknown contact panel id type.");
     }
 
     /**
      * Obtain the contact id.
      * 
      * @return A contact id <code>JabberId</code>.
      */
     JabberId getContactId() {
         return contactId;
     }
 
     /**
      * Obtain the invitation id.
      * 
      * @return An invitation id <code>Long</code>.
      */
     Long getInvitationId() {
         return invitationId;
     }
 
     /**
      * Determine if the contact id is set.
      * 
      * @return True if the contact id is set.
      */
     Boolean isSetContactId() {
         return null != contactId;
     }
 
     /**
      * Determine if the invitation id is set.
      * 
      * @return True if the invitation id is set.
      */
     Boolean isSetInvitationId() {
         return null != invitationId;
     }
 }
