 /*
  * Created On:  8-Sep-07 10:23:21 AM
  */
 package com.thinkparity.desdemona.model.contact;
 
 import com.thinkparity.codebase.email.EMail;
 
 import com.thinkparity.codebase.model.contact.OutgoingEMailInvitation;
 import com.thinkparity.codebase.model.contact.OutgoingUserInvitation;
 import com.thinkparity.codebase.model.profile.Profile;
 
 import com.thinkparity.service.AuthToken;
 
 /**
  * <b>Title:</b>thinkParity Desdemona Model Delete Contact Invitation Test<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class DeleteInvitationTest extends ContactTestCase {
 
     /** Test name. */
     private static final String NAME = "Test delete invitation";
 
     /** Test datum. */
     private Fixture datum;
 
     /**
      * Create DeleteInvitationTest.
      *
      */
     public DeleteInvitationTest() {
         super(NAME);
     }
 
     /**
      * Test of the delete outgoing e-mail test.
      * 
      */
     public void testDeleteOutgoingEMail() {
         TEST_LOGGER.logTraceId();
         TEST_LOGGER.logInfo("Test delete outgoing e-mail.");
         setUpDeleteOutgoingEMail();
         try {
             datum.validateInvitations(datum.inviteAuthToken, "invite", 1, 0, 0, 0);
            datum.validateInvitations(datum.deleteAuthToken, "delete", 0, 0, 1, 0);
             datum.validateContacts(datum.deleteAuthToken, datum.inviteAuthToken, Boolean.FALSE);
             datum.getContactModel(datum.deleteAuthToken).deleteInvitation(
                     datum.outgoingEMail, datum.now());
             datum.validateInvitations(datum.inviteAuthToken, "invite", 0, 0, 0, 0);
            datum.validateInvitations(datum.deleteAuthToken, "delete", 0, 0, 0, 0);
             datum.validateContacts(datum.deleteAuthToken, datum.inviteAuthToken, Boolean.FALSE);
         } finally {
             tearDownDelete();
         }
     }
 
     /**
      * Test of delete outgoing user.
      * 
      */
     public void testDeleteOutgoingUser() {
         TEST_LOGGER.logTraceId();
         TEST_LOGGER.logInfo("Test delete outgoing user.");
         setUpDeleteOutgoingUser();
         try {
             datum.validateInvitations(datum.inviteAuthToken, "invite", 0, 1, 0, 0);
             datum.validateInvitations(datum.deleteAuthToken, "delete", 0, 0, 0, 1);
             datum.validateContacts(datum.deleteAuthToken, datum.inviteAuthToken, Boolean.FALSE);
             datum.getContactModel(datum.deleteAuthToken).deleteInvitation(
                     datum.outgoingUser, datum.now());
             datum.validateInvitations(datum.inviteAuthToken, "invite", 0, 0, 0, 0);
             datum.validateInvitations(datum.deleteAuthToken, "delete", 0, 0, 0, 0);
             datum.validateContacts(datum.deleteAuthToken, datum.inviteAuthToken, Boolean.FALSE);
         } finally {
             tearDownDelete();
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactTestCase#setUp()
      *
      */
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         datum = new Fixture();
     }
 
     /**
      * @see com.thinkparity.desdemona.model.contact.ContactTestCase#tearDown()
      *
      */
     @Override
     protected void tearDown() throws Exception {
         datum = null;
 
         super.tearDown();
     }
 
     /**
      * Set up delete outgoing e-mail.
      * 
      */
     private void setUpDeleteOutgoingEMail() {
         final String deleteUsername = datum.newUniqueUsername();
         final Profile deleteProfile = datum.createProfile(deleteUsername);
         datum.deleteAuthToken = datum.login(deleteUsername);
         datum.verifyEMail(datum.deleteAuthToken);
 
         final String inviteUsername = datum.newUniqueUsername();
         datum.createProfile(inviteUsername);
         datum.inviteAuthToken = datum.login(inviteUsername);
         datum.verifyEMail(datum.inviteAuthToken);
         final EMail inviteEMail = datum.getProfileModel(datum.inviteAuthToken).readEMail().getEmail();
 
         datum.outgoingEMail = new OutgoingEMailInvitation();
         datum.outgoingEMail.setCreatedBy(deleteProfile);
         datum.outgoingEMail.setCreatedOn(datum.now());
         datum.outgoingEMail.setInvitationEMail(inviteEMail);
         datum.getContactModel(datum.deleteAuthToken).createInvitation(datum.outgoingEMail);
         /* setup an extra */
         final OutgoingEMailInvitation invitation = new OutgoingEMailInvitation();
         invitation.setCreatedBy(deleteProfile);
         invitation.setCreatedOn(datum.now());
         invitation.setInvitationEMail(datum.newEMail(datum.newUniqueUsername()));
        datum.getContactModel(datum.inviteAuthToken).createInvitation(invitation);
     }
 
     /**
      * Set up delete outgoing e-mail.
      * 
      */
     private void setUpDeleteOutgoingUser() {
         final String deleteUsername = datum.newUniqueUsername();
         final Profile deleteProfile = datum.createProfile(deleteUsername);
         datum.deleteAuthToken = datum.login(deleteUsername);
         datum.verifyEMail(datum.deleteAuthToken);
 
         final String inviteUsername = datum.newUniqueUsername();
         final Profile inviteProfile = datum.createProfile(inviteUsername);
         datum.inviteAuthToken = datum.login(inviteUsername);
         datum.verifyEMail(datum.inviteAuthToken);
 
         datum.outgoingUser = new OutgoingUserInvitation();
         datum.outgoingUser.setCreatedBy(deleteProfile);
         datum.outgoingUser.setCreatedOn(datum.now());
         datum.outgoingUser.setInvitationUser(inviteProfile);
         datum.getContactModel(datum.deleteAuthToken).createInvitation(datum.outgoingUser);
     }
 
     /**
      * Tear down delete.
      * 
      */
     private void tearDownDelete() {
         datum.deleteAuthToken = datum.inviteAuthToken = null;
         datum.outgoingEMail = null;
         datum.outgoingUser = null;
     }
 
     /** <b>Title:</b>Delete Invitation Test Fixture<br> */
     private class Fixture extends ContactTestCase.Fixture {
         private AuthToken deleteAuthToken;
         private AuthToken inviteAuthToken;
         private OutgoingEMailInvitation outgoingEMail;
         private OutgoingUserInvitation outgoingUser;
     }
 }
