 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded.roles;
 
 import com.flexive.shared.FxContext;
 import com.flexive.shared.exceptions.FxAccountInUseException;
 import com.flexive.shared.exceptions.FxLoginFailedException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.security.UserTicket;
 import static com.flexive.tests.embedded.FxTestUtils.login;
 import static com.flexive.tests.embedded.FxTestUtils.logout;
 import com.flexive.tests.embedded.TestUser;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 
 /**
  * Role test base class.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public abstract class AbstractRoleTest {
     protected TestUser user;
     protected UserTicket ticket;
 
     void init(TestUser user) {
         this.user = user;
     }
 
    @BeforeMethod(groups = {"ejb", "security", "roles"})
     public void beforeTestMethod() throws FxLoginFailedException, FxAccountInUseException {
         if (user != null) {
             login(user);
             this.ticket = FxContext.getUserTicket();
         }
     }
 
    @AfterMethod(groups = {"ejb", "security", "roles"})
     public void afterTestMethod() throws Exception {
         logout();
     }
 
     protected void assertSuccess(Role role, long mandatorId) {
         final String msg = accessAllowed(role, mandatorId);
         if (msg == null) {
             assert false : "User should not be allowed to succeed on call"
                     + (role != null ? " without role " + role.name() : "");
         }
     }
 
     protected void assertNoAccess(FxNoAccessException nae, Role role, long mandatorId) {
         final String msg = accessAllowed(role, mandatorId);
         if (msg != null) {
             assert false : msg + " (" + nae.getMessage() + ")";
         }
     }
 
     private String accessAllowed(Role role, long mandatorId) {
         if (ticket.isGlobalSupervisor()) {
             return "Global supervisor should be allowed to call everything";
         }
         if ((role == null || ticket.isInRole(role)) && (mandatorId == -1 || mandatorId == ticket.getMandatorId())) {
             return "User has "
                     + (role != null ? "role " + role.name() + " and " : "")
                     + "mandator[id=" + mandatorId + "] matches,"
                     + " but still not allowed to call method.";
         }
         if (ticket.isMandatorSupervisor() && mandatorId == ticket.getMandatorId()) {
             return "The mandator supervisor should be able to perform "
                     + (role != null ? "role " + role.name() : "action")
                     + " for all objects of his own mandator (id=" + ticket.getMandatorId() + ").";
         }
         return null;
     }
 }
