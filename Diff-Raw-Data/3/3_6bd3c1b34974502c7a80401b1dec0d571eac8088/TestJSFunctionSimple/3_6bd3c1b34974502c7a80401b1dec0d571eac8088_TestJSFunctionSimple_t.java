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
 package org.richfaces.tests.metamer.ftest.a4jJSFunction;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardNoRequest;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertNotSame;
 
 import java.net.URL;
 import javax.faces.event.PhaseId;
 
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page /faces/components/a4jJSFunction/simple.xhtml
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestJSFunctionSimple extends AbstractMetamerTest {
 
     private JQueryLocator link = pjq("a[id$=callFunctionLink]");
     private JQueryLocator time1 = pjq("span[id$=time1]");
     private JQueryLocator time2 = pjq("span[id$=time2]");
     private JQueryLocator year = pjq("span[id$=year]");
     private JQueryLocator ajaxRenderedTime = pjq("span[id$=autoTime]");
     private String[] phasesNames = {"RESTORE_VIEW 1", "APPLY_REQUEST_VALUES 2", "PROCESS_VALIDATIONS 3",
         "UPDATE_MODEL_VALUES 4", "INVOKE_APPLICATION 5", "RENDER_RESPONSE 6"};
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/a4jJSFunction/simple.xhtml");
     }
 
     @Test
     public void testSimpleClick() {
         String time1Value = selenium.getText(time1);
         String time2Value = selenium.getText(time2);
         String yearValue = selenium.getText(year);
         String ajaxRenderedTimeValue = selenium.getText(ajaxRenderedTime);
 
         guardXhr(selenium).click(link);
 
         String newTime1Value = waitGui.failWith("Page was not updated").waitForChangeAndReturn(time1Value,
                 retrieveText.locator(time1));
         String newTime2Value = selenium.getText(time2);
         String newYearValue = selenium.getText(year);
         String newAjaxRenderedTimeValue = selenium.getText(ajaxRenderedTime);
 
         assertNotSame(newTime1Value, time1Value, "Time1 did not change");
         assertNotSame(newTime2Value, time2Value, "Time2 did not change");
         assertEquals(newYearValue, yearValue, "Year should not change");
         assertNotSame(newAjaxRenderedTimeValue, ajaxRenderedTimeValue, "Ajax rendered time did not change");
     }
 
     @Test
     public void testAction() {
         int yearValue = Integer.parseInt(selenium.getText(year));
         String time1Value = selenium.getText(time1);
 
         JQueryLocator incYearActionOption = pjq("input[id$=actionInput:1]");
         selenium.click(incYearActionOption);
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(link);
         String newTime1Value = waitGui.failWith("Page was not updated").waitForChangeAndReturn(time1Value,
                 retrieveText.locator(time1));
         assertNotSame(time1Value, newTime1Value, "Time1 did not change");
         assertEquals(Integer.parseInt(selenium.getText(year)), yearValue + 1, "Action was not called");
 
         guardXhr(selenium).click(link);
         newTime1Value = waitGui.failWith("Page was not updated").waitForChangeAndReturn(time1Value,
                 retrieveText.locator(time1));
         assertNotSame(time1Value, newTime1Value, "Time1 did not change");
         assertEquals(Integer.parseInt(selenium.getText(year)), yearValue + 2, "Action was not called");
     }
 
     @Test
     public void testActionListener() {
         int yearValue = Integer.parseInt(selenium.getText(year));
         String time1Value = selenium.getText(time1);
 
         JQueryLocator incYearActionListenerOption = pjq("input[id$=actionListenerInput:1]");
         selenium.click(incYearActionListenerOption);
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(link);
         String newTime1Value = waitGui.failWith("Page was not updated").waitForChangeAndReturn(time1Value,
                 retrieveText.locator(time1));
         assertNotSame(time1Value, newTime1Value, "Time1 did not change");
         assertEquals(Integer.parseInt(selenium.getText(year)), yearValue + 1, "Action was not called");
 
         guardXhr(selenium).click(link);
         newTime1Value = waitGui.failWith("Page was not updated").waitForChangeAndReturn(time1Value,
                 retrieveText.locator(time1));
         assertNotSame(time1Value, newTime1Value, "Time1 did not change");
         assertEquals(Integer.parseInt(selenium.getText(year)), yearValue + 2, "Action was not called");
     }
 
     @Test
     public void testBypassUpdates() {
         String time1Value = selenium.getText(time1);
 
         JQueryLocator input = pjq("input[type=radio][name$=bypassUpdatesInput][value=true]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(link);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time1));
 
         JQueryLocator[] phases = {jq("div#phasesPanel li"), jq("div#phasesPanel li:eq(0)"),
             jq("div#phasesPanel li:eq(1)"), jq("div#phasesPanel li:eq(2)"), jq("div#phasesPanel li:eq(3)")};
 
         assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.PROCESS_VALIDATIONS,
                 PhaseId.RENDER_RESPONSE);
     }
 
     @Test
     public void testImmediate() {
         String time1Value = selenium.getText(time1);
 
         JQueryLocator input = pjq("input[type=radio][name$=immediateInput][value=true]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(link);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time1));
 
         assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.RENDER_RESPONSE);
     }
 
     @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-10011")
     public void testLimitRender() {
         // set limitRender=true
         JQueryLocator limitRenderInput = pjq("input[type=radio][name$=limitRenderInput][value=true]");
         selenium.click(limitRenderInput);
         selenium.waitForPageToLoad();
 
         // get all values
         String time1Value = selenium.getText(time1);
         String time2Value = selenium.getText(time2);
         String yearValue = selenium.getText(year);
         String ajaxRenderedTimeValue = selenium.getText(ajaxRenderedTime);
 
         // invoke the function and get new values
         guardXhr(selenium).click(link);
 
         String newTime1Value = waitGui.failWith("Page was not updated").waitForChangeAndReturn(time1Value,
                 retrieveText.locator(time1));
         String newTime2Value = selenium.getText(time2);
         String newYearValue = selenium.getText(year);
         String newAjaxRenderedTimeValue = selenium.getText(ajaxRenderedTime);
 
         assertNotSame(newTime1Value, time1Value, "Time1 did not change");
         assertNotSame(newTime2Value, time2Value, "Time2 did not change");
         assertEquals(newYearValue, yearValue, "Year should not change");
         assertEquals(newAjaxRenderedTimeValue, ajaxRenderedTimeValue, "Ajax rendered time should not change");
     }
 
     @Test
     public void testEvents() {
         selenium.type(pjq("input[type=text][id$=onbeginInput]"), "metamerEvents += \"begin \"");
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[type=text][id$=onbeforedomupdateInput]"), "metamerEvents += \"beforedomupdate \"");
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[type=text][id$=oncompleteInput]"), "metamerEvents += \"complete \"");
         selenium.waitForPageToLoad();
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
         String time1Value = selenium.getText(time1);
 
         selenium.fireEvent(link, Event.CLICK);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time1));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "begin", "Attribute onbegin doesn't work");
         assertEquals(events[1], "beforedomupdate", "Attribute onbeforedomupdate doesn't work");
         assertEquals(events[2], "complete", "Attribute oncomplete doesn't work");
     }
 
     @Test
     public void testRender() {
         selenium.type(pjq("input[type=text][id$=renderInput]"), "time1");
         selenium.waitForPageToLoad();
 
         // get all values
         String time1Value = selenium.getText(time1);
         String time2Value = selenium.getText(time2);
         String yearValue = selenium.getText(year);
         String ajaxRenderedTimeValue = selenium.getText(ajaxRenderedTime);
 
         // invoke the function and get new values
         guardXhr(selenium).click(link);
 
         String newTime1Value = waitGui.failWith("Page was not updated").waitForChangeAndReturn(time1Value,
                 retrieveText.locator(time1));
         String newTime2Value = selenium.getText(time2);
         String newYearValue = selenium.getText(year);
         String newAjaxRenderedTimeValue = selenium.getText(ajaxRenderedTime);
 
         assertNotSame(newTime1Value, time1Value, "Time1 should not change");
         assertEquals(newTime2Value, time2Value, "Time2 did not change");
         assertEquals(newYearValue, yearValue, "Year should not change");
         assertNotSame(newAjaxRenderedTimeValue, ajaxRenderedTimeValue, "Ajax rendered time should change");
     }
 
     @Test
     public void testRendered() {
         selenium.click(pjq("input[type=radio][name$=renderedInput][value=false]"));
         selenium.waitForPageToLoad();
 
         // get all values
         String time1Value = selenium.getText(time1);
         String time2Value = selenium.getText(time2);
         String yearValue = selenium.getText(year);
         String ajaxRenderedTimeValue = selenium.getText(ajaxRenderedTime);
 
         guardNoRequest(selenium).click(link);
 
         // get new values
         String newTime1Value = selenium.getText(time1);
         String newTime2Value = selenium.getText(time2);
         String newYearValue = selenium.getText(year);
         String newAjaxRenderedTimeValue = selenium.getText(ajaxRenderedTime);
 
         assertEquals(newTime1Value, time1Value, "Time1 should not change");
         assertEquals(newTime2Value, time2Value, "Time2 should not change");
         assertEquals(newYearValue, yearValue, "Year should not change");
         assertEquals(newAjaxRenderedTimeValue, ajaxRenderedTimeValue, "Ajax rendered time should not change");
     }
 }
