 package net.continuumsecurity.web;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import java.net.URL;
 
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
    org.tempuri.TMWebServicesSoap port;
 
     @Before
     public void setup() {
        URL wsdlURL = org.tempuri.TMWebServices.WSDL_LOCATION;
        org.tempuri.TMWebServices ss = new org.tempuri.TMWebServices(wsdlURL, SERVICE_NAME);
         port = ss.getTMWebServicesSoap();
         ((BindingProvider)port).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
     }
 
 
     public void printDetails() {
        https.teammentor_securityinnovation_com._13415.TMUser user = port.currentUser();
        System.out.println(user.getFirstName()+ " "+user.getLastName());
        System.out.println("Group ID: "+user.getGroupID());
         System.out.println(port.currentSessionID());
     }
 
     @Test
    public void testLogin() {
         port.loginPwdInClearText("admin", "!!tmadmin");
         printDetails();
         port.logout();
 
         port.loginPwdInClearText("reader", "!!tmreader");
         printDetails();
         port.logout();
 
         port.loginPwdInClearText("editor", "!!tmeditor");
         printDetails();
         port.logout();
     }
 }
