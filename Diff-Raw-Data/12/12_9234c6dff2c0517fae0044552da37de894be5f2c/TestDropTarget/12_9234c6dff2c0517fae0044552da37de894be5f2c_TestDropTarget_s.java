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
 package org.richfaces.tests.metamer.ftest.richDropTarget;
 
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.richfaces.tests.metamer.ftest.richDragIndicator.Indicator.IndicatorState.ACCEPTING;
 import static org.richfaces.tests.metamer.ftest.richDragIndicator.Indicator.IndicatorState.DRAGGING;
 import static org.richfaces.tests.metamer.ftest.richDragIndicator.Indicator.IndicatorState.REJECTING;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import java.net.URL;
 
 import org.jboss.test.selenium.actions.Drag;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.jboss.test.selenium.request.RequestType;
 import org.jboss.test.selenium.waiting.retrievers.TextRetriever;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.richfaces.tests.metamer.ftest.richDragIndicator.AbstractDragNDropTest;
 import org.richfaces.tests.metamer.ftest.richDragIndicator.Draggable;
 import org.richfaces.tests.metamer.ftest.richDragIndicator.Indicator.IndicatorState;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestDropTarget extends AbstractDragNDropTest {
 
     private DropTargetAttributes attributes = new DropTargetAttributes();
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richDropTarget/simple.xhtml");
     }
 
     TextRetriever retrieveDrop1 = retrieveText.locator(drop1);
     TextRetriever retrieveDrop2 = retrieveText.locator(drop2);
 
     JQueryLocator clientId = jq("span[id$=:clientId]");
     JQueryLocator dragValue = jq("span[id$=:dragValue]");
     JQueryLocator dropValue = jq("span[id$=:dropValue]");
 
     @Test
     public void testAcceptedTypes() {
         attributes.setAcceptedTypes("drg2");
 
         testAcception(drg1, REJECTING);
         testAcception(drg2, ACCEPTING);
         testAcception(drg3, REJECTING);
 
         attributes.setAcceptedTypes("drg1, drg3");
 
         testAcception(drg1, ACCEPTING);
         testAcception(drg2, REJECTING);
         testAcception(drg3, ACCEPTING);
     }
 
     @Test
     public void testRender() {
         attributes.setRender("droppable1 droppable2 renderChecker");
 
         testAcception(drg1, ACCEPTING);
 
         retrieveDrop1.initializeValue();
         retrieveDrop2.initializeValue();
         drag.drop();
         waitAjax.waitForChange(retrieveDrop1);
         assertTrue(retrieveDrop2.isValueChanged());
     }
 
     @Test
     public void testRendered() {
         attributes.setRendered(false);
         selenium.getPageExtensions().install();
         selenium.getRequestInterceptor().clearRequestTypeDone();
 
         testAcception(drg1, DRAGGING);
 
         drag.drop();
 
         waitModel.timeout(5000).waitForTimeout();
         assertEquals(selenium.getRequestInterceptor().getRequestTypeDone(), RequestType.NONE);
     }
 
     @Test
     public void testDropListenerAndEvent() {
         testAcceptedDropping(drg1);
         assertTrue(selenium.getText(clientId).endsWith("richDropTarget1"));
         assertTrue(selenium.getText(dragValue).contains("1"));
         assertTrue(selenium.getText(dropValue).contains("1"));
 
         testAcceptedDropping(drg1);
         assertTrue(selenium.getText(clientId).endsWith("richDropTarget1"));
         assertTrue(selenium.getText(dragValue).contains("1"));
         assertTrue(selenium.getText(dropValue).contains("2"));
 
         testAcceptedDropping(drg2);
         assertTrue(selenium.getText(clientId).endsWith("richDropTarget1"));
         assertTrue(selenium.getText(dragValue).contains("2"));
         assertTrue(selenium.getText(dropValue).contains("3"));
 
         testAcceptedDropping(drg1);
         assertTrue(selenium.getText(clientId).endsWith("richDropTarget1"));
         assertTrue(selenium.getText(dragValue).contains("1"));
         assertTrue(selenium.getText(dropValue).contains("4"));
 
         drag = new Drag(drg3, drop2);
         drag.setDragIndicator(indicator);
         retrieveRequestTime.initializeValue();
         drag.drop();
         waitAjax.waitForChange(retrieveRequestTime);
         assertTrue(selenium.getText(clientId).endsWith("richDropTarget2"));
         assertTrue(selenium.getText(dragValue).contains("3"));
         assertTrue(selenium.getText(dropValue).contains("5"));
     }
 
     @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-10336")
    public void testAction() {
        fail("not working currently");
    }

    @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-10336")
    public void testActionListener() {
        fail("not working currently");
    }

    @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10334")
     public void testExecute() {
         fail("not working currently");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10334")
     public void testImmediate() {
         fail("not working currently");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10334")
     public void testBypassUpdates() {
         fail("not working currently");
     }
 
     private void testAcceptedDropping(Draggable draggable) {
         testAcception(draggable, ACCEPTING);
 
         retrieveRequestTime.initializeValue();
         drag.drop();
         waitAjax.waitForChange(retrieveRequestTime);
         assertFalse(indicator.isVisible());
     }
 
     private void testAcception(Draggable draggable, IndicatorState state) {
         drag = new Drag(draggable, drop1);
         drag.setDragIndicator(indicator);
         enterAndVerify(drop1, state);
     }
 }
