 package net.continuumsecurity.web;
 
import https.teammentor_securityinnovation_com._13415.TMUser;
 import org.junit.Before;
 import org.junit.Test;
import org.tempuri.TMWebServices;
import org.tempuri.TMWebServicesSoap;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import java.net.URL;
 
import static junit.framework.Assert.assertEquals;

 /**
  * ****************************************************************************
  * BDD-Security, application security testing framework
  * <p/>
  * Copyright (C) `2012 Stephen de Vries`
  * <p/>
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * <p/>
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
  * ****************************************************************************
  */
 public class TeamMentorWSTest {
     private static final QName SERVICE_NAME = new QName("http://tempuri.org/", "TM_WebServices");
    TMWebServicesSoap port;
    TMUser currentUser;
 
     @Before
     public void setup() {
        URL wsdlURL = TMWebServices.WSDL_LOCATION;
        TMWebServices ss = new TMWebServices(wsdlURL, SERVICE_NAME);
         port = ss.getTMWebServicesSoap();
         ((BindingProvider)port).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
     }
 
 
     public void printDetails() {
        currentUser = port.currentUser();
        System.out.println(currentUser.getFirstName() + " " + currentUser.getLastName());
        System.out.println("Group ID: " + currentUser.getGroupID());
         System.out.println(port.currentSessionID());
     }
 
     @Test
    public void testLoginOk() {
         port.loginPwdInClearText("admin", "!!tmadmin");
         printDetails();
        assertEquals("John",currentUser.getFirstName());
         port.logout();
 
         port.loginPwdInClearText("reader", "!!tmreader");
         printDetails();
        assertEquals("Peter",currentUser.getFirstName());
         port.logout();
 
         port.loginPwdInClearText("editor", "!!tmeditor");
         printDetails();
        assertEquals("Joe",currentUser.getFirstName());
         port.logout();
     }
 }
