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
 package org.richfaces.tests.metamer.ftest.a4jStatus;
 
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.encapsulated.JavaScript.js;
 import static org.testng.Assert.assertEquals;
 
 import java.net.URL;
 
 import org.jboss.cheiron.halt.XHRHalter;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.locator.ElementLocator;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestInstantAttributes extends AbstracStatusTest {
 
     StatusAttributes attributes = new StatusAttributes() {
         protected void applyText(ElementLocator<?> locator, String value) {
             guardXhr(selenium).type(locator, value);
 
         };
     };
 
     JavaScript alert = js("alert('{0}')");
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/a4jStatus/instantAttributes.xhtml");
     }
 
     @Test
     public void testOnStart() {
         for (int i = 0; i < 2; i++) {
             attributes.setOnStart(alert.parametrize("start" + 1));
 
             XHRHalter.enable();
             selenium.click(button1);
             selenium.waitForCondition(js("selenium.isAlertPresent()"));
             assertEquals(selenium.getAlert(), "start" + 1);
             getCurrentXHRHalter().complete();
             XHRHalter.disable();
         }
     }
 
     @Test
    @IssueTracking({ "https://issues.jboss.org/browse/RF-9118", "http://java.net/jira/browse/JAVASERVERFACES-1788" })
     public void testOnError() {
         for (int i = 0; i < 2; i++) {
             attributes.setOnError(alert.parametrize("error" + i));
 
             selenium.click(buttonError);
             waitAjax.until(alertPresent);
             assertEquals(selenium.getAlert(), "error" + i);
         }
     }
 
     @Test
     public void testOnSuccess() {
         for (int i = 0; i < 2; i++) {
             attributes.setOnSuccess(alert.parametrize("success" + 1));
 
             selenium.click(button1);
             waitAjax.until(alertPresent);
             assertEquals(selenium.getAlert(), "success" + 1);
         }
     }
 
     @Test
     public void testOnStop() {
         for (int i = 0; i < 2; i++) {
             attributes.setOnStop(alert.parametrize("stop" + i));
 
             selenium.click(button1);
             waitAjax.until(alertPresent);
             assertEquals(selenium.getAlert(), "stop" + i);
 
             selenium.click(buttonError);
             waitAjax.until(alertPresent);
             assertEquals(selenium.getAlert(), "stop" + i);
         }
     }
 }
