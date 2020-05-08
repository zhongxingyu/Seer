 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /*
  * Created on Dec 18, 2003
  *
  */
 package com.sapienter.jbilling.server.user;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Random;
 
 import junit.framework.TestCase;
 
 import com.sapienter.jbilling.common.Util;
 import com.sapienter.jbilling.server.entity.CreditCardDTO;
 import com.sapienter.jbilling.server.entity.PaymentInfoChequeDTO;
 import com.sapienter.jbilling.server.invoice.InvoiceWS;
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.payment.PaymentWS;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.api.JbillingAPI;
 import com.sapienter.jbilling.server.util.api.JbillingAPIException;
 import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
 import com.sapienter.jbilling.server.util.api.WebServicesConstants;
 import java.math.BigDecimal;
 
 /**
  * @author Emil
  */
 public class WSTest extends TestCase {
       
     public void testGetUser() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             System.out.println("Getting user 2");
             UserWS ret = api.getUserWS(new Integer(2));
             assertEquals(2, ret.getUserId());
             try {
                 System.out.println("Getting invalid user 13");
                 ret = api.getUserWS(new Integer(13));
                 fail("Shouldn't be able to access user 13");
             } catch(Exception e) {
             }
             
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     public void testOwningBalance() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             System.out.println("Getting balance of user 2");
             UserWS ret = api.getUserWS(new Integer(2));
             assertEquals("Balance of Gandlaf starts at 1376765", 1376765.0, ret.getOwingBalance());
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
 
     public void testCreateUpdateDeleteUser() {
         try {
         	JbillingAPI api = JbillingAPIFactory.getAPI();
         	
             /*
              * Create - This passes the password validation routine.
              */
             UserWS newUser = createUser(true, 43, null);
             Integer newUserId = newUser.getUserId();
             String newUserName = newUser.getUserName();
             assertNotNull("The user was not created", newUserId);
             
             System.out.println("Getting the id of the new user");
             Integer ret = api.getUserId(newUserName);
             assertEquals("Id of new user found", newUserId, ret);
             
             //verify the created user       
             System.out.println("Getting created user " + newUserId);
             UserWS retUser = api.getUserWS(newUserId);
             assertEquals("created username", retUser.getUserName(),
                     newUser.getUserName());
             assertEquals("created user first name", retUser.getContact().getFirstName(),
                     newUser.getContact().getFirstName());     
             assertEquals("create user parent id", new Integer(43), retUser.getParentId());
             assertEquals("created user with no dynamic balance type", Constants.BALANCE_NO_DYNAMIC,
                     retUser.getBalanceType());
             System.out.println("My user: " + retUser);
             assertEquals("created credit card name", "Frodo Baggins", retUser.getCreditCard().getName());
             
             
             /*
              * Make a create mega call
              */
             System.out.println("Making mega call");
             retUser.setUserName("MU" + Long.toHexString(System.currentTimeMillis()));
             // need to reset the password, it came encrypted
             // let's use a long one
             retUser.setPassword("0fu3js8wl1;a$e2w)xRQ"); 
             // the new user shouldn't be a child
             retUser.setParentId(null);
             
             // need an order for it
             OrderWS newOrder = getOrder();
 
             CreateResponseWS mcRet = api.create(retUser,newOrder);
 
             System.out.println("Validating new invoice");
             // validate that the results are reasonable
             assertNotNull("Mega call result can't be null", mcRet);
             assertNotNull("Mega call invoice result can't be null", mcRet.getInvoiceId());
             // there should be a successfull payment
             assertEquals("Payment result OK", true, mcRet.getPaymentResult().getResult().booleanValue());
             assertEquals("Processor code", "fake-code-default", mcRet.getPaymentResult().getCode1());
             // get the invoice
             InvoiceWS retInvoice = api.getInvoiceWS(mcRet.getInvoiceId());
             assertNotNull("New invoice not present", retInvoice);
             assertEquals("Balance of invoice should be zero, is paid", retInvoice.getBalance(),
                     new Float(0));
             assertEquals("Total of invoice should be total of order", retInvoice.getTotal(),
                     new Float(20));
             assertEquals("New invoice paid", retInvoice.getToProcess(), new Integer(0));
             
             // TO-DO test that the invoice total is equal to the order total
  
             /*
              * Update
              */
             // now update the created user
             System.out.println("Updating user - Pass 1 - Should succeed");
             retUser.setPassword("newPassword1");
             retUser.getCreditCard().setNumber("4111111111111152");
             retUser.setBalanceType(Constants.BALANCE_CREDIT_LIMIT);
             retUser.setCreditLimit(112233.0);
             System.out.println("Updating user...");
             api.updateUser(retUser);
             
             // and ask for it to verify the modification
             System.out.println("Getting updated user ");
             retUser = api.getUserWS(newUserId);
             assertNotNull("Didn't get updated user", retUser);
             // The password should be the same as in the first step, no update happened.
             assertEquals("Password ", retUser.getPassword(),
             		"33aa7e0850c4234ff03beb205b9ea728");
             assertEquals("Contact name", retUser.getContact().getFirstName(),
                     newUser.getContact().getFirstName());
             assertEquals("Credit card updated", "4111111111111152",
                     retUser.getCreditCard().getNumber());
             assertEquals("Balance type updated", Constants.BALANCE_CREDIT_LIMIT,
                     retUser.getBalanceType());
             assertEquals("credit limit updated", 112233.0,
                     retUser.getCreditLimit());
 
             System.out.println("Updating user - Pass 2 - Should fail due to invalid password");
             retUser.setPassword("newPassword");
             System.out.println("Updating user...");
             boolean catched = false;
             try {
             	api.updateUser(retUser);
             } catch (Throwable e) {
             	catched = true;
             }
             if (!catched) {
             	fail("User was updated - Password validation not working!");
             } else {
             	System.out.println("User was not updated. Password validation worked.");
             }
 
             // again, for the contact info, and no cc
             retUser.getContact().setFirstName("New Name");
             retUser.getContact().setLastName("New L.Name");
             retUser.setCreditCard(null);
             // call the update
             retUser.setPassword(null); // should not change the password
             api.updateUser(retUser);
             // fetch the user
             UserWS updatedUser = api.getUserWS(newUserId);
             assertEquals("updated f name", retUser.getContact().getFirstName(),
                     updatedUser.getContact().getFirstName());
             assertEquals("updated l name", retUser.getContact().getLastName(),
                     updatedUser.getContact().getLastName());
             assertEquals("Credit card should stay the same", "4111111111111152",
                     updatedUser.getCreditCard().getNumber());
             assertEquals("Password should stay the same", "33aa7e0850c4234ff03beb205b9ea728",
                     updatedUser.getPassword());
             
             System.out.println("Update result:" + updatedUser);
 
             // now update the contact only
             retUser.getContact().setFirstName("New Name2");
             api.updateUserContact(retUser.getUserId(),new Integer(2),retUser.getContact());
              // fetch the user
             updatedUser = api.getUserWS(newUserId);
             assertEquals("updated contact f name", retUser.getContact().getFirstName(),
                     updatedUser.getContact().getFirstName());
             
             // now update with a bogus contact type
             try {
                 System.out.println("Updating with invalid contact type");
                 api.updateUserContact(retUser.getUserId(),new Integer(1),retUser.getContact());
                 fail("Should not update with an invalid contact type");
             } catch(Exception e) {
                 // good
                 System.out.println("Type rejected " + e.getMessage());
             }
 
             // update credit card details
             System.out.println("Removing credit card");
             api.updateCreditCard(newUserId, null);
             assertNull("Credit card removed",api.getUserWS(newUserId).getCreditCard());
             
             System.out.println("Creating credit card");
             String ccName = "New ccName";
             String ccNumber = "4012888888881881";
             Date ccExpiry = Util.truncateDate(Calendar.getInstance().getTime());
 
             CreditCardDTO cc = new CreditCardDTO();
             cc.setName(ccName);
             cc.setNumber(ccNumber);
             cc.setExpiry(ccExpiry);
             api.updateCreditCard(newUserId,cc);
 
             // check updated cc details
             retUser = api.getUserWS(newUserId);
             CreditCardDTO retCc = retUser.getCreditCard();
             assertEquals("new cc name", ccName, retCc.getName());
             assertEquals("updated cc number", ccNumber, retCc.getNumber());
             assertEquals("updated cc expiry", ccExpiry, retCc.getExpiry());
             
             System.out.println("Updating credit card");
             cc.setName("Updated ccName");
             cc.setNumber(null);
             api.updateCreditCard(newUserId,cc);
             retUser = api.getUserWS(newUserId);
             assertEquals("updated cc name", "Updated ccName", retUser.getCreditCard().getName());
             assertNotNull("cc number still there", retUser.getCreditCard().getNumber());
 
             // try to update cc of user from different company
             System.out.println("Attempting to update cc of a user from " 
                     + "a different company");
             try {
             	api.updateCreditCard(new Integer(13),cc);
                 fail("Shouldn't be able to update cc of user 13");
             } catch(Exception e) {
             }
 
             /*
              * Delete
              */
             // now delete this new guy
             System.out.println("Deleting user..." + newUserId);
             api.deleteUser(newUserId);
             
             // try to fetch the deleted user
             System.out.println("Getting deleted user " + newUserId);
             updatedUser = api.getUserWS(newUserId);
             assertEquals(updatedUser.getDeleted(), 1);
             
             // verify I can't delete users from another company 
             try {
                 System.out.println("Deleting user base user ... 13");
                 api.getUserWS(new Integer(13));
                 fail("Shouldn't be able to access user 13");
             } catch(Exception e) {
             }
             
             
             /*
              * Get list of active customers
              */
             System.out.println("Getting active users...");
             Integer[] users = api.getUsersInStatus(new Integer(1));
             assertEquals(1017,users.length);
             assertEquals("First return user ", 1, users[0].intValue());
             assertEquals("Last returned user ", 10751, users[1016].intValue());
 
             /*
              * Get list of not active customers
              */
             System.out.println("Getting NOTactive users...");
             users = api.getUsersNotInStatus(new Integer(1));
             assertEquals(users.length, 1);
             for (int f = 0; f < users.length; f++) {
                 System.out.println("Got user " + users[f]);
             }
 
             /*
              * Get list using a custom field
              */
             System.out.println("Getting by custom field...");
             users = api.getUsersByCustomField(new Integer(1),new String("serial-from-ws"));
             
             // the one from the megacall is not deleted and has the custom field
             assertEquals(users.length, 1001); 
             assertEquals(users[1000], mcRet.getUserId());
             
             System.out.println("Done");
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     public void testLanguageId() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             UserWS newUser = new UserWS();
             newUser.setUserName("language-test");
             newUser.setPassword("asdfasdf1");
             newUser.setLanguageId(new Integer(2)); // French
             newUser.setMainRoleId(new Integer(5));
             newUser.setIsParent(new Boolean(true));
             newUser.setStatusId(UserDTOEx.STATUS_ACTIVE);
 
             // add a contact
             ContactWS contact = new ContactWS();
             contact.setEmail("frodo@shire.com");
             newUser.setContact(contact);
 
             System.out.println("Creating user ...");
             // do the creation
             Integer newUserId = api.createUser(newUser);
 
             // get user
             UserWS createdUser = api.getUserWS(newUserId);
             assertEquals("Language id", 2, 
                     createdUser.getLanguageId().intValue());
 
             // clean up
             api.deleteUser(newUserId);
             
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     public void testUserTransitions() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             System.out.println("Getting complete list of user transitions");
             UserTransitionResponseWS[] ret = api.getUserTransitions(null, null);
             
             if (ret == null)
             	fail("Transition list should not be empty!");
             assertEquals(6, ret.length);
             
             // Check the ids of the returned transitions
             assertEquals(ret[0].getId().intValue(), 1);
             assertEquals(ret[1].getId().intValue(), 2);
             // Check the value of returned data
             assertEquals(ret[0].getUserId().intValue(), 2);
             assertEquals(ret[0].getFromStatusId().intValue(), 2);
             assertEquals(ret[0].getToStatusId().intValue(), 1);
             assertEquals(ret[1].getUserId().intValue(), 2);
             assertEquals(ret[1].getFromStatusId().intValue(), 2);
             assertEquals(ret[1].getToStatusId().intValue(), 1);
             
             // save an ID for later
             Integer myId = ret[4].getId();
 
             System.out.println("Getting first partial list of user transitions");
             ret =  api.getUserTransitions(new Date(2000 - 1900,0,0), 
             				new Date(2007 - 1900, 0, 1));
             if (ret == null)
             	fail("Transition list should not be empty!");
             assertEquals(ret.length, 1);
             assertEquals(ret[0].getId().intValue(), 1);
             assertEquals(ret[0].getUserId().intValue(), 2);
             assertEquals(ret[0].getFromStatusId().intValue(), 2);
             assertEquals(ret[0].getToStatusId().intValue(), 1);
             
             System.out.println("Getting second partial list of user transitions");
             ret = api.getUserTransitions(null,null);
             if (ret == null)
             	fail("Transition list should not be empty!");
             assertEquals(5, ret.length);
             assertEquals(ret[0].getId().intValue(), 2);
             assertEquals(ret[0].getUserId().intValue(), 2);
             assertEquals(ret[0].getFromStatusId().intValue(), 2);
             assertEquals(ret[0].getToStatusId().intValue(), 1);
             
             System.out.println("Getting list after id");
             ret = api.getUserTransitionsAfterId(myId);
             if (ret == null)
                 fail("Transition list should not be empty!");
             assertEquals("Only one transition after id " + myId, 1,ret.length);
             
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
     
     public void testAuthentication() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             System.out.println("Auth with wrong credentials");
             Integer result = api.authenticate("authuser", "notAGoodOne");
             assertEquals("Authentication has to fail", 
                     WebServicesConstants.AUTH_WRONG_CREDENTIALS, result);
             result = api.authenticate("authuser", "notAGoodOne");
             assertEquals("Authentication has to fail", 
                     WebServicesConstants.AUTH_WRONG_CREDENTIALS, result);
             System.out.println("Too many retries");
             result = api.authenticate("authuser", "notAGoodOne");
             assertEquals("Now locked out", 
                     WebServicesConstants.AUTH_LOCKED, result);
 
             // it is locked, but we know the secret password
             System.out.println("Auth for expired");
             result = api.authenticate("authuser", "totalSecret");
             assertEquals("Password should be expired", 
                     WebServicesConstants.AUTH_EXPIRED, result);
             
             // update the user's password
             Integer userId = api.getUserId("authuser");
             UserWS user = api.getUserWS(userId);
             user.setPassword("234qwe");
             api.updateUser(user);
             
             // try again ...
             System.out.println("Auth after password update");
             result = api.authenticate("authuser", "234qwe");
             assertEquals("Should auth ok now", 
                     WebServicesConstants.AUTH_OK, result);
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
         
     }
 
 /*
           Parent 1 10752
            /\   |
           /  \  +--------+
  10753 iCh1  Ch2 10754   |
         /\    |         Ch6
        /  \   |
     Ch3 iCh4 Ch5
   10755 10756 10757
 
 Ch3->Ch1
 Ch4->Ch4
 Ch1->Ch1
 Ch5->P1
 Ch2->P1
      */
     public void testParentChild() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
             /*
              * Create - This passes the password validation routine.
              */
             UserWS newUser = new UserWS();
             newUser.setUserName("parent1");
             newUser.setPassword("asdfasdf1");
             newUser.setLanguageId(new Integer(1));
             newUser.setMainRoleId(new Integer(5));
             newUser.setIsParent(new Boolean(true));
             newUser.setStatusId(UserDTOEx.STATUS_ACTIVE);
             
             // add a contact
             ContactWS contact = new ContactWS();
             contact.setEmail("frodo@shire.com");
             newUser.setContact(contact);
             
             System.out.println("Creating parent user ...");
             // do the creation
             Integer parentId = api.createUser(newUser);
             assertNotNull("The user was not created", parentId);
             
             // verify the created user
             System.out.println("Getting created user ");
             UserWS retUser = api.getUserWS(parentId);
             assertEquals("created username", retUser.getUserName(),
                     newUser.getUserName());
             assertEquals("create user is parent", new Boolean(true), retUser.getIsParent());
             
             System.out.println("Creating child1 user ...");
             // now create the child
             newUser.setIsParent(new Boolean(true));
             newUser.setParentId(parentId);
             newUser.setUserName("child1");
             newUser.setPassword("asdfasdf1");
             newUser.setInvoiceChild(Boolean.TRUE);
             Integer child1Id = api.createUser(newUser);
             //test
             System.out.println("Getting created user ");
             retUser = api.getUserWS(child1Id);
             assertEquals("created username", retUser.getUserName(),
                     newUser.getUserName());
             assertEquals("created user parent", parentId, retUser.getParentId());
             assertEquals("created do not invoice child", Boolean.TRUE, retUser.getInvoiceChild());
 
             // test parent has child id
             retUser = api.getUserWS(parentId);
             Integer[] childIds = retUser.getChildIds();
             assertEquals("1 child", 1, childIds.length);
             assertEquals("created user child", child1Id, childIds[0]);
             
             System.out.println("Creating child2 user ...");
             // now create the child
             newUser.setIsParent(new Boolean(true));
             newUser.setParentId(parentId);
             newUser.setUserName("child2");
             newUser.setPassword("asdfasdf1");
             newUser.setInvoiceChild(Boolean.FALSE);
             Integer child2Id = api.createUser(newUser);
             //test
             System.out.println("Getting created user ");
             retUser = api.getUserWS(child2Id);
             assertEquals("created username", retUser.getUserName(),
                     newUser.getUserName());
             assertEquals("created user parent", parentId, retUser.getParentId());
             assertEquals("created do not invoice child", Boolean.FALSE, retUser.getInvoiceChild());
 
             // test parent has child id
             retUser = api.getUserWS(parentId);
             childIds = retUser.getChildIds();
             assertEquals("2 child", 2, childIds.length);
             assertEquals("created user child", child2Id, 
                     childIds[0].equals(child2Id) ? childIds[0] : childIds[1]);
 
             System.out.println("Creating child6 user ...");
             // now create the child
             newUser.setIsParent(new Boolean(true));
             newUser.setParentId(parentId);
             newUser.setUserName("child6");
             newUser.setPassword("asdfasdf1");
             newUser.setInvoiceChild(Boolean.FALSE);
             Integer child6Id = api.createUser(newUser);
             //test
             System.out.println("Getting created user ");
             retUser = api.getUserWS(child6Id);
             assertEquals("created username", retUser.getUserName(),
                     newUser.getUserName());
             assertEquals("created user parent", parentId, retUser.getParentId());
             assertEquals("created do not invoice child", Boolean.FALSE, retUser.getInvoiceChild());
 
             // test parent has child id
             retUser = api.getUserWS(parentId);
             childIds = retUser.getChildIds();
             assertEquals("3 child", 3, childIds.length);
             assertEquals("created user child", child6Id,
                     childIds[0].equals(child6Id) ? childIds[0] : 
                         childIds[1].equals(child6Id) ? childIds[1] : childIds[2]);
 
             System.out.println("Creating child3 user ...");
             // now create the child
             newUser.setIsParent(new Boolean(false));
             newUser.setParentId(child1Id);
             newUser.setUserName("child3");
             newUser.setPassword("asdfasdf1");
             newUser.setInvoiceChild(Boolean.FALSE);
             Integer child3Id = api.createUser(newUser);
             //test
             System.out.println("Getting created user ");
             retUser = api.getUserWS(child3Id);
             assertEquals("created username", retUser.getUserName(),
                     newUser.getUserName());
             assertEquals("created user parent", child1Id, retUser.getParentId());
             assertEquals("created do not invoice child", Boolean.FALSE, retUser.getInvoiceChild());
 
             // test parent has child id
             retUser = api.getUserWS(child1Id);
             childIds = retUser.getChildIds();
             assertEquals("1 child", 1, childIds.length);
             assertEquals("created user child", child3Id, childIds[0]);
 
             System.out.println("Creating child4 user ...");
             // now create the child
             newUser.setIsParent(new Boolean(false));
             newUser.setParentId(child1Id);
             newUser.setUserName("child4");
             newUser.setPassword("asdfasdf1");
             newUser.setInvoiceChild(Boolean.TRUE);
             Integer child4Id = api.createUser(newUser);
             //test
             System.out.println("Getting created user ");
             retUser = api.getUserWS(child4Id);
             assertEquals("created username", retUser.getUserName(),
                     newUser.getUserName());
             assertEquals("created user parent", child1Id, retUser.getParentId());
             assertEquals("created do not invoice child", Boolean.TRUE, retUser.getInvoiceChild());
 
             // test parent has child id
             retUser = api.getUserWS(child1Id);
             childIds = retUser.getChildIds();
             assertEquals("2 child for child1", 2, childIds.length);
             assertEquals("created user child", child4Id, childIds[0].equals(child4Id) ? childIds[0] : childIds[1]);
 
             System.out.println("Creating child5 user ...");
             // now create the child
             newUser.setIsParent(new Boolean(false));
             newUser.setParentId(child2Id);
             newUser.setUserName("child5");
             newUser.setPassword("asdfasdf1");
             newUser.setInvoiceChild(Boolean.FALSE);
             Integer child5Id = api.createUser(newUser);
             //test
             System.out.println("Getting created user ");
             retUser = api.getUserWS(child5Id);
             assertEquals("created username", retUser.getUserName(),
                     newUser.getUserName());
             assertEquals("created user parent", child2Id, retUser.getParentId());
             assertEquals("created do not invoice child", Boolean.FALSE, retUser.getInvoiceChild());
 
             // test parent has child id
             retUser = api.getUserWS(child2Id);
             childIds = retUser.getChildIds();
             assertEquals("1 child for child2", 1, childIds.length);
             assertEquals("created user child", child5Id, childIds[0]);
 
             // test authentication of two of them
             System.out.println("Authenticating new users ");
             assertEquals("auth of parent", new Integer(0), 
                     api.authenticate("parent1", "asdfasdf1"));
             assertEquals("auth of child", new Integer(0), 
                     api.authenticate("child1", "asdfasdf1"));
 
             // create an order for all these users
             System.out.println("Creating orders for all users");
             OrderWS order = getOrder();
             order.setUserId(parentId);
             api.createOrder(order);
             order = getOrder();
             order.setUserId(child1Id);
             api.createOrder(order);
             order = getOrder();
             order.setUserId(child2Id);
             api.createOrder(order);
             order = getOrder();
             order.setUserId(child3Id);
             api.createOrder(order);
             order = getOrder();
             order.setUserId(child4Id);
             api.createOrder(order);
             order = getOrder();
             order.setUserId(child5Id);
             api.createOrder(order);
             order = getOrder();
             order.setUserId(child6Id);
             api.createOrder(order);
             // run the billing process for each user, validating the results
             System.out.println("Invoicing and validating...");
             // parent1
             Integer[] invoices = api.createInvoice(parentId, false);
             assertNotNull("invoices cant be null", invoices);
             assertEquals("there should be one invoice", 1, invoices.length);
             InvoiceWS invoice = api.getInvoiceWS(invoices[0]);
             assertEquals("invoice should be 80$", 80F, invoice.getTotal());
             // child1
             invoices = api.createInvoice(child1Id, false);
             assertNotNull("invoices cant be null", invoices);
             assertEquals("there should be one invoice", 1, invoices.length);
             invoice = api.getInvoiceWS(invoices[0]);
             assertEquals("invoice should be 40$", 40F, invoice.getTotal());
             // child2
             invoices = api.createInvoice(child2Id, false);
             // CXF returns null for empty arrays
             if (invoices != null) {
                 assertEquals("there should be no invoice", 0, invoices.length);
             }
             // child3
             invoices = api.createInvoice(child3Id, false);
             if (invoices != null) {
                 assertEquals("there should be no invoice", 0, invoices.length);
             }
             // child4
             invoices = api.createInvoice(child4Id, false);
             assertNotNull("invoices cant be null", invoices);
             assertEquals("there should be one invoice", 1, invoices.length);
             invoice = api.getInvoiceWS(invoices[0]);
             assertEquals("invoice should be 20$", 20F, invoice.getTotal());
             // child5
             invoices = api.createInvoice(child5Id, false);
             if (invoices != null) {
                 assertEquals("there should be no invoice", 0, invoices.length);
             }
             // child6
             invoices = api.createInvoice(child6Id, false);
             if (invoices != null) {
                 assertEquals("there should be one invoice", 0, invoices.length);
             }
      
             // clean up
             api.deleteUser(parentId);
             api.deleteUser(child1Id);
             api.deleteUser(child2Id);
             api.deleteUser(child3Id);
             api.deleteUser(child4Id);
             api.deleteUser(child5Id);
             api.deleteUser(child6Id);
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
         
     }
     
     public void testGetByCC() {
         // note: this method getUsersByCreditCard seems to have a bug. It does
         // not reutrn Gandlaf if there is not an updateUser call before
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
             Integer[] ids = api.getUsersByCreditCard("1152");
             assertNotNull("Four customers with CC", ids);
             assertEquals("Four customers with CC", 5, ids.length);
             assertEquals("Created user with CC", 10751,
                     ids[ids.length - 1].intValue());
                     
             // get the user
             assertNotNull("Getting found user",api.getUserWS(ids[0]));
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
     
     private UserWS createUser(boolean goodCC, Integer parentId, Integer currencyId) throws JbillingAPIException, IOException {
             JbillingAPI api = JbillingAPIFactory.getAPI();
             
             /*
              * Create - This passes the password validation routine.
              */
             UserWS newUser = new UserWS();
             newUser.setUserName("testUserName-" + Calendar.getInstance().getTimeInMillis());
             newUser.setPassword("asdfasdf1");
             newUser.setLanguageId(new Integer(1));
             newUser.setMainRoleId(new Integer(5));
             newUser.setParentId(parentId); // this parent exists
             newUser.setStatusId(UserDTOEx.STATUS_ACTIVE);
             newUser.setCurrencyId(currencyId);
             newUser.setBalanceType(Constants.BALANCE_NO_DYNAMIC);
             
             // add a contact
             ContactWS contact = new ContactWS();
             contact.setEmail("frodo@shire.com");
             contact.setFirstName("Frodo");
             contact.setLastName("Baggins");
             String fields[] = new String[2];
             fields[0] = "1";
             fields[1] = "2"; // the ID of the CCF for the processor
             String fieldValues[] = new String[2];
             fieldValues[0] = "serial-from-ws";
             fieldValues[1] = "FAKE_2"; // the plug-in parameter of the processor
             contact.setFieldNames(fields);
             contact.setFieldValues(fieldValues);
             newUser.setContact(contact);
             
             // add a credit card
             CreditCardDTO cc = new CreditCardDTO();
             cc.setName("Frodo Baggins");
             cc.setNumber(goodCC ? "4111111111111152" : "4111111111111111");
             cc.setExpiry(new Date(new Date().getTime() + 1000000));
             newUser.setCreditCard(cc);
             
             System.out.println("Creating user ...");
             newUser.setUserId(api.createUser(newUser));
             
             return newUser;
     }
     
     private OrderWS getOrder() {
         // need an order for it
         OrderWS newOrder = new OrderWS();
         newOrder.setUserId(new Integer(-1)); // it does not matter, the user will be created
         newOrder.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
         newOrder.setPeriod(new Integer(1)); // once
         newOrder.setCurrencyId(new Integer(1));
         
         // now add some lines
         OrderLineWS lines[] = new OrderLineWS[2];
         OrderLineWS line;
         
         line = new OrderLineWS();
         line.setPrice(new Float(10));
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(new Integer(1));
         line.setAmount(new Float(10));
         line.setDescription("First line");
         line.setItemId(new Integer(1));
         lines[0] = line;
         
         line = new OrderLineWS();
         line.setPrice(new Float(10));
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(new Integer(1));
         line.setAmount(new Float(10));
         line.setDescription("Second line");
         line.setItemId(new Integer(2));
         lines[1] = line;
         
         newOrder.setOrderLines(lines);
 
         return newOrder;
     }
     
     public void testPendingUnsubscription() {
     	try {
     		JbillingAPI api = JbillingAPIFactory.getAPI();
     		OrderWS order = api.getLatestOrder(1055);
     		order.setActiveUntil(new Date(2008 - 1900, 11 - 1, 1)); // sorry 
     		api.updateOrder(order);
     		assertEquals("User 1055 should be now in pending unsubscription", 
     				UserDTOEx.SUBSCRIBER_PENDING_UNSUBSCRIPTION, api.getUserWS(1055).getSubscriberStatusId());
     	} catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
     	}
     }
 
     public void testCurrency() {
      	try {
     		JbillingAPI api = JbillingAPIFactory.getAPI();
             UserWS myUser = createUser(true, null, 11);
             Integer myId = myUser.getUserId();
             System.out.println("Checking currency of new user");
             myUser = api.getUserWS(myId);
             assertEquals("Currency should be A$", 11, myUser.getCurrencyId().intValue());
             myUser.setCurrencyId(1);
             System.out.println("Updating currency to US$");
             myUser.setPassword(null); // otherwise it will try the encrypted password
             api.updateUser(myUser);
             System.out.println("Checking currency ...");
             myUser = api.getUserWS(myId);
             assertEquals("Currency should be US$", 1, myUser.getCurrencyId().intValue());
             System.out.println("Removing");
             api.deleteUser(myId);
     	} catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
     	}
     }
 
     public void testPrePaidBalance() {
         try {
     		JbillingAPI api = JbillingAPIFactory.getAPI();
             UserWS myUser = createUser(true, null, null);
             Integer myId = myUser.getUserId();
 
             // update to pre-paid
             myUser.setBalanceType(Constants.BALANCE_PRE_PAID);
             api.updateUser(myUser);
 
             // get the current balance, it should be null or 0
             System.out.println("Checking initial balance type and dynamic balance");
             myUser = api.getUserWS(myId);
             assertEquals("user should be pre-paid", Constants.BALANCE_PRE_PAID,
                     myUser.getBalanceType());
             assertEquals("user should have 0 balance", 0.0,
                         myUser.getDynamicBalance());
 
             // validate. room = 0, price = 7
             System.out.println("Validate with fields...");
             PricingField pf[] =  { new PricingField("src", "604"),
                 new PricingField("dst", "512")};
             ValidatePurchaseWS result = api.validatePurchase(myId, 1, pf);
             assertEquals("validate purchase success 1", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 1", 
                     Boolean.valueOf(false), result.getAuthorized());
             assertEquals("validate purchase quantity 1", 0.0, 
                     result.getQuantity());
 
 
             // add a payment
             PaymentWS payment = new PaymentWS();
             payment.setAmount(new Float(20));
             payment.setIsRefund(new Integer(0));
             payment.setMethodId(Constants.PAYMENT_METHOD_CHEQUE);
             payment.setPaymentDate(Calendar.getInstance().getTime());
             payment.setResultId(Constants.RESULT_ENTERED);
             payment.setCurrencyId(new Integer(1));
             payment.setUserId(myId);
 
             PaymentInfoChequeDTO cheque = new PaymentInfoChequeDTO();
             cheque.setBank("ws bank");
             cheque.setDate(Calendar.getInstance().getTime());
             cheque.setNumber("2232-2323-2323");
             payment.setCheque(cheque);
 
             System.out.println("Applying payment");
             api.applyPayment(payment, null);
             // check new balance is 20
             System.out.println("Validating new balance");
             myUser = api.getUserWS(myId);
             assertEquals("user should have 20 balance", 20.0,
                     myUser.getDynamicBalance());
 
             // validate. room = 20, price = 7
             System.out.println("Validate with fields...");
             result = api.validatePurchase(myId, 1, pf);
             assertEquals("validate purchase success 2", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 2", 
                     Boolean.valueOf(true), result.getAuthorized());
             assertEquals("validate purchase quantity 2", 2.8571, 
                     result.getQuantity());
 
             // validate without item id (mediation should set item)
             // duration field needed for rule to fire
             PricingField[] pf2 = { new PricingField("src", "604"),
                     new PricingField("dst", "512"), 
                     new PricingField("duration", 1) };
             System.out.println("Validate with fields and without itemId...");
             result = api.validatePurchase(myId, null, pf2);
             assertEquals("validate purchase success 2", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 2", 
                     Boolean.valueOf(true), result.getAuthorized());
             assertEquals("validate purchase quantity 2", 2.8571, 
                     result.getQuantity());
 
             // now create a one time order, the balance should decrease
             OrderWS order = getOrder();
             order.setUserId(myId);
             System.out.println("creating one time order");
             Integer orderId = api.createOrder(order);
             System.out.println("Validating new balance");
             myUser = api.getUserWS(myId);
             assertEquals("user should have 0 balance", 0.0,
                     myUser.getDynamicBalance());
 
             // for the following, use line 2 with item id 2. item id 1 has
             // cancellation fees rules that affect the balance.
             // increase the quantity of the one-time order
             System.out.println("adding quantity to one time order");
            pause(2000); // pause while provisioning status is being updated
             order = api.getOrder(orderId);
             OrderLineWS line = order.getOrderLines()[0].getItemId() == 2 ?
                 order.getOrderLines()[0] : order.getOrderLines()[1];
             line.setQuantity(7);
             line.setAmount(7 * line.getPrice());
             Float delta = 6 * line.getPrice();
             api.updateOrder(order);
             myUser = api.getUserWS(myId);
             assertEquals("user should have new balance", 0.0 - delta,
                     myUser.getDynamicBalance());
 
             // decrease the quantity of the one-time order
             System.out.println("remove quantity to one time order");
             order = api.getOrder(orderId);
             line = order.getOrderLines()[0].getItemId() == 2 ?
                 order.getOrderLines()[0] : order.getOrderLines()[1];
             line.setQuantity(1);
             line.setAmount(1 * order.getOrderLines()[1].getPrice());
             api.updateOrder(order);
             myUser = api.getUserWS(myId);
             assertEquals("user should have new balance", 0.0,
                     myUser.getDynamicBalance());
 
             // delete one line from the one time order
             System.out.println("remove one line from one time order");
             order = api.getOrder(orderId);
             line = order.getOrderLines()[0].getItemId() == 1 ?
                 order.getOrderLines()[0] : order.getOrderLines()[1];
             order.setOrderLines(new OrderLineWS[] { line });
             api.updateOrder(order);
             myUser = api.getUserWS(myId);
             assertEquals("user should have new balance", 10.0,
                     myUser.getDynamicBalance());
 
             // validate. room = 10, price = 10
             System.out.println("Validate with fields...");
             result = api.validatePurchase(myId, 1, null);
             assertEquals("validate purchase success 3", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 3", 
                     Boolean.valueOf(true), result.getAuthorized());
             assertEquals("validate purchase quantity 3", 1.0, 
                     result.getQuantity());
 
 
             // delete the order, the balance has to go back to 20
             System.out.println("deleting one time order");
             api.deleteOrder(orderId);
             System.out.println("Validating new balance");
             myUser = api.getUserWS(myId);
             assertEquals("user should have 20 balance", 20.0,
                     myUser.getDynamicBalance());
 
             // now create a recurring order with invoice, the balance should decrease
             order = getOrder();
             order.setUserId(myId);
             order.setPeriod(2);
             System.out.println("creating recurring order and invoice");
             api.createOrderAndInvoice(order);
             System.out.println("Validating new balance");
             myUser = api.getUserWS(myId);
             assertEquals("user should have 0 balance", 0.0,
                     myUser.getDynamicBalance());
             
             System.out.println("Removing");
             api.deleteUser(myId);
     	} catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
     	}
     }
 
      public void testCreditLimit() {
         try {
     		JbillingAPI api = JbillingAPIFactory.getAPI();
             UserWS myUser = createUser(true, null, null);
             Integer myId = myUser.getUserId();
 
             // update to pre-paid
             myUser.setBalanceType(Constants.BALANCE_CREDIT_LIMIT);
             myUser.setCreditLimit(1000.0);
             api.updateUser(myUser);
 
             // validate. room = 1000, price = 7
             System.out.println("Validate with fields...");
             PricingField pf[] =  { new PricingField("src", "604"),
                 new PricingField("dst", "512")};
             ValidatePurchaseWS result = api.validatePurchase(myId, 1, pf);
             assertEquals("validate purchase success 1", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 1", 
                     Boolean.valueOf(true), result.getAuthorized());
             assertEquals("validate purchase quantity 1", 142.8571, 
                     result.getQuantity());
 
 
             // get the current balance, it should be null or 0
             System.out.println("Checking initial balance type and dynamic balance");
             myUser = api.getUserWS(myId);
             assertEquals("user should be pre-paid", Constants.BALANCE_CREDIT_LIMIT,
                     myUser.getBalanceType());
             assertEquals("user should have 0 balance", 0.0,
                         myUser.getDynamicBalance());
 
             // now create a one time order, the balance should increase
             OrderWS order = getOrder();
             order.setUserId(myId);
             System.out.println("creating one time order");
             Integer orderId = api.createOrder(order);
             System.out.println("Validating new balance");
             myUser = api.getUserWS(myId);
             assertEquals("user should have 20 balance", 20.0,
                     myUser.getDynamicBalance());
 
              // validate. room = 980, price = 10
             System.out.println("Validate with fields...");
             result = api.validatePurchase(myId, 1, null);
             assertEquals("validate purchase success 2", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 2", 
                     Boolean.valueOf(true), result.getAuthorized());
             assertEquals("validate purchase quantity 2", 98.0, 
                     result.getQuantity());
 
             // delete the order, the balance has to go back to 0
             System.out.println("deleting one time order");
             api.deleteOrder(orderId);
             System.out.println("Validating new balance");
             myUser = api.getUserWS(myId);
             assertEquals("user should have 0 balance", 0.0,
                     myUser.getDynamicBalance());
 
             // now create a recurring order with invoice, the balance should increase
             order = getOrder();
             order.setUserId(myId);
             order.setPeriod(2);
             System.out.println("creating recurring order and invoice");
             Integer invoiceId = api.createOrderAndInvoice(order);
             System.out.println("Validating new balance");
             myUser = api.getUserWS(myId);
             assertEquals("user should have 20 balance", 20.0,
                     myUser.getDynamicBalance());
 
              // validate. room = 980, price = 7
             System.out.println("Validate with fields...");
             result = api.validatePurchase(myId, 1, pf);
             assertEquals("validate purchase success 3", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 3", 
                     Boolean.valueOf(true), result.getAuthorized());
             // rules limit max to 3 lemonades
             assertEquals("validate purchase quantity 3", 2.0, 
                     result.getQuantity());
 
 
             // add a payment. I'd like to call payInvoice but it's not finding the CC
             PaymentWS payment = new PaymentWS();
             payment.setAmount(new Float(20));
             payment.setIsRefund(new Integer(0));
             payment.setMethodId(Constants.PAYMENT_METHOD_CHEQUE);
             payment.setPaymentDate(Calendar.getInstance().getTime());
             payment.setResultId(Constants.RESULT_ENTERED);
             payment.setCurrencyId(new Integer(1));
             payment.setUserId(myId);
 
             PaymentInfoChequeDTO cheque = new PaymentInfoChequeDTO();
             cheque.setBank("ws bank");
             cheque.setDate(Calendar.getInstance().getTime());
             cheque.setNumber("2232-2323-2323");
             payment.setCheque(cheque);
 
             System.out.println("Applying payment");
             api.applyPayment(payment, invoiceId);
             // check new balance is 20
             System.out.println("Validating new balance");
             myUser = api.getUserWS(myId);
             assertEquals("user should have 0 balance", 0.0,
                     myUser.getDynamicBalance());
 
             System.out.println("Removing");
             api.deleteUser(myId);
     	} catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
     	}
     }
 
     public void testRulesValidatePurchaseTask() {
         try {
             // see ValidatePurchaseRules.drl
 
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             // create user
             UserWS user = createUser(true, null, null);
             Integer userId = user.getUserId();
 
             // update to credit limit
             user.setBalanceType(Constants.BALANCE_CREDIT_LIMIT);
             user.setCreditLimit(1000.0);
             api.updateUser(user);
 
             // create an order for this user
             OrderWS order = new OrderWS();
             order.setUserId(userId);
             order.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
             order.setPeriod(2); // monthly
             order.setCurrencyId(1); // USD
 
             // a main subscription order
             order.setIsCurrent(1);
             Calendar cal = Calendar.getInstance();
             cal.clear();
             cal.set(2009, 1, 1);
             order.setActiveSince(cal.getTime());
 
             // order lines
             OrderLineWS[] lines = new OrderLineWS[2];
             lines[0] = new OrderLineWS();
             lines[0].setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
             lines[0].setQuantity(1); 
             lines[0].setItemId(2); // lemonade plan
             // take the price and description from the item
             lines[0].setUseItem(true);
 
             lines[1] = new OrderLineWS();
             lines[1].setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
             lines[1].setQuantity(3); 
             lines[1].setItemId(1); // lemonade
             // take the price and description from the item
             lines[1].setUseItem(true);
 
             // attach lines to order
             order.setOrderLines(lines);
 
             // create the order
             Integer orderId = api.createOrder(order);
 
 
             // try to get another lemonde
             ValidatePurchaseWS result = api.validatePurchase(userId, 1, null);
             assertEquals("validate purchase success 1", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 1", 
                     Boolean.valueOf(false), result.getAuthorized());
             assertEquals("validate purchase quantity 1", 0.0, 
                     result.getQuantity());
             assertEquals("validate purchase message 1", 
                     "No more than 3 lemonades are allowed.", 
                     result.getMessage()[0]);
 
 
             // exception should be thrown
             PricingField pf[] = { new PricingField("fail", "fail") };
             result = api.validatePurchase(userId, 1, pf);
             assertEquals("validate purchase success 2", Boolean.valueOf(false), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 2", 
                     Boolean.valueOf(false), result.getAuthorized());
             assertEquals("validate purchase quantity 2", 0.0, 
                     result.getQuantity());
             assertEquals("validate purchase message 2", 
                     "Error: java.lang.RuntimeException: Throw exception rule", 
                     result.getMessage()[0]);
 
 
             // coffee quantity available should be 20
             result = api.validatePurchase(userId, 3, null);
             assertEquals("validate purchase success 3", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 3", 
                     Boolean.valueOf(true), result.getAuthorized());
             assertEquals("validate purchase quantity 3", 20.0, 
                     result.getQuantity());
 
 
             // add 10 coffees to current order
             OrderLineWS newLine = new OrderLineWS();
             newLine.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
             newLine.setItemId(new Integer(3));
             newLine.setQuantity(new Double(10.0));
             // take the price and description from the item
             newLine.setUseItem(new Boolean(true));
 
             // update the current order
             OrderWS currentOrderAfter = api.updateCurrentOrder(userId, 
                     new OrderLineWS[] { newLine }, null, new Date(), 
                     "Event from WS");
 
             // quantity available should be 10
             result = api.validatePurchase(userId, 3, null);
             assertEquals("validate purchase success 3", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 3", 
                     Boolean.valueOf(true), result.getAuthorized());
             assertEquals("validate purchase quantity 3", 10.0, 
                     result.getQuantity());
 
             // add another 10 coffees to current order
             currentOrderAfter = api.updateCurrentOrder(userId, 
                     new OrderLineWS[] { newLine }, null, new Date(), 
                     "Event from WS");
 
             // quantity available should be 0
             result = api.validatePurchase(userId, 3, null);
             assertEquals("validate purchase success 4", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 4", 
                     Boolean.valueOf(false), result.getAuthorized());
             assertEquals("validate purchase quantity 4", 0.0, 
                     result.getQuantity());
             assertEquals("validate purchase message 4", 
                     "No more than 20 coffees are allowed.", 
                     result.getMessage()[0]);
 
 
             // clean up
             api.deleteOrder(orderId);
             api.deleteUser(userId);
     	} catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
     	}
     }
 
     public void testUserBalancePurchaseTaskHierarchical() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             // create 2 users, child and parent
             UserWS newUser = new UserWS();
             newUser.setUserName("parent1");
             newUser.setPassword("asdfasdf1");
             newUser.setLanguageId(new Integer(1));
             newUser.setMainRoleId(new Integer(5));
             newUser.setIsParent(new Boolean(true));
             newUser.setStatusId(UserDTOEx.STATUS_ACTIVE);
             newUser.setBalanceType(Constants.BALANCE_CREDIT_LIMIT);
             newUser.setCreditLimit(2000.0);
             
             // add a contact
             ContactWS contact = new ContactWS();
             contact.setEmail("frodo@shire.com");
             newUser.setContact(contact);
             
             System.out.println("Creating parent user ...");
             // do the creation
             Integer parentId = api.createUser(newUser);
 
             // now create the child
             newUser.setIsParent(new Boolean(false));
             newUser.setParentId(parentId);
             newUser.setUserName("child1");
             newUser.setPassword("asdfasdf1");
             newUser.setInvoiceChild(Boolean.FALSE);
             newUser.setBalanceType(Constants.BALANCE_NO_DYNAMIC);
             newUser.setCreditLimit(null);
             Integer childId = api.createUser(newUser);
  
             // validate a purchase for the child
             // validate. room = 2000, price = 7
             System.out.println("Validate with fields...");
             PricingField pf[] =  { new PricingField("src", "604"),
                 new PricingField("dst", "512")};
             ValidatePurchaseWS result = api.validatePurchase(childId, 1, pf);
             assertEquals("validate purchase success", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized", 
                     Boolean.valueOf(true), result.getAuthorized());
             assertEquals("validate purchase quantity", 285.7143, 
                     result.getQuantity());
 
             // create an order for the child
             OrderWS order = getOrder();
             order.setUserId(childId);
             System.out.println("creating one time order");
             Integer orderId = api.createOrder(order);
 
             // validate the balance of the parent
             System.out.println("Validating new balance");
             UserWS parentUser = api.getUserWS(parentId);
             assertEquals("user should have 20 balance", 20.0,
                     parentUser.getDynamicBalance());
 
             // validate another purchase for the child
             // validate. room = 1980, price = 10
             System.out.println("Validate with fields...");
             result = api.validatePurchase(childId, 1, null);
             assertEquals("validate purchase success 2", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 2", 
                     Boolean.valueOf(true), result.getAuthorized());
             assertEquals("validate purchase quantity 2", 198.0, 
                     result.getQuantity());
 
 
             // clean up
             api.deleteUser(parentId);
             api.deleteUser(childId);
 
     	} catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
     	}
     }
 
     public void testValidateMultiPurchase() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
             UserWS myUser = createUser(true, null, null);
             Integer myId = myUser.getUserId();
 
             // update to credit limit
             myUser.setBalanceType(Constants.BALANCE_CREDIT_LIMIT);
             myUser.setCreditLimit(1000.0);
             api.updateUser(myUser);
 
             // validate with items only
             // validate. room = 1000, price = 10 + 20 + 15 = 45
             ValidatePurchaseWS result = api.validateMultiPurchase(myId, 
                     new Integer[] { 1, 2, 251 }, null);
             assertEquals("validate purchase success 1", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 1", 
                     Boolean.valueOf(true), result.getAuthorized());
             assertEquals("validate purchase quantity 1", 22.2222, 
                     result.getQuantity());
 
             // validate with pricing fields
             // validate. room = 1000, price = 7 * 3 = 21
             PricingField[] pf = { new PricingField("src", "604"),
                 new PricingField("dst", "512") };
             result = api.validateMultiPurchase(myId, new Integer[] { 1, 1, 1 }, 
                     new PricingField[][] { pf, pf, pf } );
             assertEquals("validate purchase success 1", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 1", 
                     Boolean.valueOf(true), result.getAuthorized());
             assertEquals("validate purchase quantity 1", 47.6190, 
                     result.getQuantity());
 
             // validate without item ids (mediation should set item)
             // duration field needed for rule to fire
             // validate. room = 1000, price = 7 * 3 = 21
             pf = new PricingField[] { new PricingField("src", "604"),
                     new PricingField("dst", "512"), 
                     new PricingField("duration", 1) };
             System.out.println("Validate with fields and without itemId...");
             result = api.validateMultiPurchase(myId, null,
                     new PricingField[][] { pf, pf, pf } );
             assertEquals("validate purchase success 2", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized 2", 
                     Boolean.valueOf(true), result.getAuthorized());
             assertEquals("validate purchase quantity 2", 47.6190, 
                     result.getQuantity());
 
 
             System.out.println("Removing");
             api.deleteUser(myId);
     	} catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
     	}
     }
 
     // name changed so it is not called in normal test runs
     public void XXtestLoad() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
             for (int i = 0; i < 1000; i++) {
                 Random rnd = new Random();
                 UserWS newUser = createUser(rnd.nextBoolean(), null, null);
                 OrderWS newOrder = getOrder();
                 // change the quantities for viarety
                 newOrder.getOrderLines()[0].setQuantity(rnd.nextInt(100) + 1);
                 //newOrder.getLines().first().setUseItem(true);
                 newOrder.getOrderLines()[newOrder.getOrderLines().length - 1].setQuantity(rnd.nextInt(100) + 1);
                 //newOrder.getLines().last().setUseItem(true);
                 newOrder.setUserId(newUser.getUserId());
                 api.createOrder(newOrder);
             }
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
    }
 
     public void testPenaltyTaskOrder() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         final Integer USER_ID = 53;
         final Integer ORDER_ID = 35;
         final Integer PENALTY_ITEM_ID = 270;
 
         // pluggable BasicPenaltyTask is configured for ageing_step 6
         // test that other status changes will not add a new order item
         UserWS user = api.getUserWS(USER_ID);
         user.setPassword(null);
         user.setStatusId(2);
         api.updateUser(user);
 
         assertEquals("Status was changed", 2, api.getUserWS(USER_ID).getStatusId().intValue());
         assertEquals("No new order was created", ORDER_ID, api.getLatestOrder(USER_ID).getId());
 
 
         // new order will be created with the penalty item when status id = 6
         user.setStatusId(6);
         api.updateUser(user);
 
         assertEquals("Status was changed", 6, api.getUserWS(USER_ID).getStatusId().intValue());
 
         OrderWS order = api.getLatestOrder(USER_ID);
         assertFalse("New order was created, id does not equal original", ORDER_ID.equals(order.getId()));
         assertEquals("New order has one item", 1, order.getOrderLines().length);
 
         OrderLineWS line = order.getOrderLines()[0];
         assertEquals("New order contains penalty item", PENALTY_ITEM_ID, line.getItemId());
         assertEquals("Order penalty value is the item price (not a percentage)", 10.00f, line.getAmount().floatValue());
 
         // delete order and invoice
         api.deleteOrder(order.getId());       
     }    

    private void pause(long t) {
        System.out.println("pausing for " + t + " ms...");
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
        }
    }
 }
