 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.tests.metamer.ftest.richPanelMenuGroup;
 
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.richfaces.PanelMenuMode.ajax;
 import static org.richfaces.PanelMenuMode.client;
 import static org.richfaces.PanelMenuMode.server;
 
 import java.net.URL;
 
 import org.richfaces.tests.metamer.ftest.annotations.Inject;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.richfaces.tests.metamer.ftest.annotations.Use;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 @IssueTracking("https://issues.jboss.org/browse/RF-10317")
 public class TestPanelMenuGroupClientSideHandlers extends AbstractPanelMenuGroupTest {
 
     @Inject
     @Use(empty = true)
     String event;
     String[] ajaxExpansionEvents = new String[] { "beforeswitch", "beforeexpand", "beforeselect", "begin",
         "beforedomupdate", "select", "expand", "switch", "complete" };
     String[] ajaxCollapsionEvents = new String[] { "beforeswitch", "beforecollapse", "beforeselect", "begin",
         "beforedomupdate", "select", "collapse", "switch", "complete" };
     String[] clientExpansionEvents = new String[] { "beforeswitch", "beforeexpand", "beforeselect", "select", "expand",
         "switch" };
     String[] clientCollapsionEvents = new String[] { "beforeswitch", "beforecollapse", "beforeselect", "select",
         "collapse", "switch" };
     String[] serverExpansionEvents = new String[] { "beforeswitch", "beforeexpand" };
     String[] serverCollapsionEvents = new String[] { "beforeswitch", "beforecollapse" };
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richPanelMenuGroup/simple.xhtml");
     }
 
     @Test
     @Use(field = "event", value = "ajaxCollapsionEvents")
     public void testClientSideCollapsionEvent() {
         attributes.setMode(ajax);
         menu.setGroupMode(ajax);
         super.testRequestEventsBefore(event);
         topGroup.toggle();
         super.testRequestEventsAfter(event);
     }
 
     @Test
     @Use(field = "event", value = "ajaxExpansionEvents")
     public void testClientSideExpansionEvent() {
         attributes.setMode(ajax);
         menu.setGroupMode(ajax);
        topGroup.toggle();
         super.testRequestEventsBefore(event);
         topGroup.toggle();
         super.testRequestEventsAfter(event);
     }
 
     @Test
     public void testClientSideExpansionEventsOrderClient() {
         attributes.setMode(client);
         menu.setGroupMode(client);
         super.testRequestEventsBefore(serverExpansionEvents);
         topGroup.toggle();
         super.testRequestEventsAfter(serverExpansionEvents);
     }
 
     @Test
     public void testClientSideCollapsionEventsOrderClient() {
         attributes.setMode(client);
         menu.setGroupMode(client);
         topGroup.toggle();
         super.testRequestEventsBefore(clientCollapsionEvents);
         topGroup.toggle();
         super.testRequestEventsAfter(clientCollapsionEvents);
     }
 
     @Test
     public void testClientSideExpansionEventsOrderAjax() {
         attributes.setMode(ajax);
         menu.setGroupMode(ajax);
         super.testRequestEventsBefore(ajaxExpansionEvents);
         topGroup.toggle();
         super.testRequestEventsAfter(ajaxExpansionEvents);
     }
 
     @Test
     public void testClientSideCollapsionEventsOrderAjax() {
         attributes.setMode(ajax);
         menu.setGroupMode(ajax);
         topGroup.toggle();
         super.testRequestEventsBefore(ajaxCollapsionEvents);
         topGroup.toggle();
         super.testRequestEventsAfter(ajaxCollapsionEvents);
     }
 
     @Test
     public void testClientSideExpansionEventsOrderServer() {
         attributes.setMode(server);
         menu.setGroupMode(server);
         topGroup.toggle();
         super.testRequestEventsBefore(serverExpansionEvents);
         topGroup.toggle();
         super.testRequestEventsAfter(serverExpansionEvents);
     }
 
     @Test
     public void testClientSideCollapsionEventsOrderServer() {
         attributes.setMode(server);
         menu.setGroupMode(server);
         topGroup.toggle();
         super.testRequestEventsBefore(serverCollapsionEvents);
         topGroup.toggle();
         super.testRequestEventsAfter(serverCollapsionEvents);
     }
 }
