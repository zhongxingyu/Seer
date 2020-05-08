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
 package org.richfaces.tests.metamer.ftest.richTabPanel;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardHttp;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardNoRequest;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.assertNotSame;
 
 import java.net.URL;
 
 import javax.faces.event.PhaseId;
 
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.locator.Attribute;
 import org.jboss.test.selenium.locator.AttributeLocator;
 import org.jboss.test.selenium.locator.ElementLocator;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.jboss.test.selenium.waiting.EventFiredCondition;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page /faces/components/richTabPanel/simple.xhtml
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestRichTabPanel extends AbstractMetamerTest {
 
     private JQueryLocator panel = pjq("div[id$=tabPanel]");
     private JQueryLocator[] itemContents = {pjq("div[id$=tab1] > div.rf-tb-cnt"), pjq("div[id$=tab2] > div.rf-tb-cnt"),
         pjq("div[id$=tab3] > div.rf-tb-cnt"), pjq("div[id$=tab4] > div.rf-tb-cnt"), pjq("div[id$=tab5] > div.rf-tb-cnt")};
     private JQueryLocator[] activeHeaders = {pjq("td[id$=tab1:header:active]"), pjq("td[id$=tab2:header:active]"),
         pjq("td[id$=tab3:header:active]"), pjq("td[id$=tab4:header:active]"), pjq("td[id$=tab5:header:active]")};
     private JQueryLocator[] inactiveHeaders = {pjq("td[id$=tab1:header:inactive]"), pjq("td[id$=tab2:header:inactive]"),
         pjq("td[id$=tab3:header:inactive]"), pjq("td[id$=tab4:header:inactive]"), pjq("td[id$=tab5:header:inactive]")};
     private JQueryLocator[] disabledHeaders = {pjq("td[id$=tab1:header:disabled]"), pjq("td[id$=tab2:header:disabled]"),
         pjq("td[id$=tab3:header:disabled]"), pjq("td[id$=tab4:header:disabled]"), pjq("td[id$=tab5:header:disabled]")};
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richTabPanel/simple.xhtml");
     }
 
     @Test
     public void testInit() {
         boolean displayed = selenium.isDisplayed(panel);
        assertTrue(displayed, "Accordion is not present on the page.");
 
         displayed = selenium.isDisplayed(activeHeaders[0]);
         assertTrue(displayed, "Header of tab1 should be active.");
         for (int i = 1; i < 5; i++) {
             displayed = selenium.isDisplayed(activeHeaders[i]);
             assertFalse(displayed, "Header of tab " + (i + 1) + " should not be active.");
         }
 
         displayed = selenium.isDisplayed(inactiveHeaders[0]);
         assertFalse(displayed, "Header of tab1 should not be inactive.");
         displayed = selenium.isDisplayed(inactiveHeaders[1]);
         assertTrue(displayed, "Header of tab2 should be inactive.");
 
         displayed = selenium.isDisplayed(disabledHeaders[3]);
         assertTrue(displayed, "Header of tab4 should be disabled.");
         for (int i = 0; i < 3; i++) {
             displayed = selenium.isDisplayed(disabledHeaders[i]);
             assertFalse(displayed, "Header of tab " + (i + 1) + " should not be disabled.");
         }
 
         displayed = selenium.isDisplayed(itemContents[0]);
         assertTrue(displayed, "Content of item1 should be visible.");
 
         for (int i = 1; i < 5; i++) {
             displayed = selenium.isDisplayed(itemContents[i]);
             assertFalse(displayed, "Tab" + (i + 1) + "'s content should not be visible.");
         }
     }
 
     @Test
     public void testSwitchTypeNull() {
         for (int i = 2; i >= 0; i--) {
             final int index = i;
             guardXhr(selenium).click(inactiveHeaders[index]);
             waitGui.failWith("Tab " + (index + 1) + " is not displayed.").until(isDisplayed.locator(itemContents[index]));
         }
     }
 
     @Test
     public void testSwitchTypeAjax() {
         JQueryLocator selectOption = pjq("input[name$=switchTypeInput][value=ajax]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         testSwitchTypeNull();
     }
 
     @Test
     public void testSwitchTypeClient() {
         JQueryLocator selectOption = pjq("input[name$=switchTypeInput][value=client]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         for (int i = 2; i >= 0; i--) {
             final int index = i;
             guardNoRequest(selenium).click(inactiveHeaders[index]);
             waitGui.failWith("Tab " + (index + 1) + " is not displayed.").until(isDisplayed.locator(itemContents[index]));
         }
     }
 
     @Test
     public void testSwitchTypeServer() {
         JQueryLocator selectOption = pjq("input[name$=switchTypeInput][value=server]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         for (int i = 2; i >= 0; i--) {
             final int index = i;
             guardHttp(selenium).click(inactiveHeaders[index]);
             waitGui.failWith("Tab " + (index + 1) + " is not displayed.").until(isDisplayed.locator(itemContents[index]));
         }
     }
 
     @Test
     public void testBypassUpdates() {
         JQueryLocator input = pjq("input[type=radio][name$=bypassUpdatesInput][value=true]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         selenium.click(inactiveHeaders[2]);
         waitGui.failWith("Tab 3 is not displayed.").until(isDisplayed.locator(itemContents[2]));
 
         assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.PROCESS_VALIDATIONS,
                 PhaseId.RENDER_RESPONSE);
     }
 
     @Test
     public void testCycledSwitching() {
         String panelId = selenium.getEval(new JavaScript("window.testedComponentId"));
         String result = null;
 
         // RichFaces.$('form:tabPanel').nextItem('tab4') will be null
         result = selenium.getEval(new JavaScript("window.RichFaces.$('" + panelId + "').nextItem('tab4')"));
         assertEquals(result, "null", "Result of function nextItem('tab4')");
 
         // RichFaces.$('form:tabPanel').prevItem('tab1') will be null
         result = selenium.getEval(new JavaScript("window.RichFaces.$('" + panelId + "').prevItem('tab1')"));
         assertEquals(result, "null", "Result of function prevItem('tab1')");
 
         JQueryLocator input = pjq("input[type=radio][name$=cycledSwitchingInput][value=true]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         // RichFaces.$('form:tabPanel').nextItem('tab5') will be item1
         result = selenium.getEval(new JavaScript("window.RichFaces.$('" + panelId + "').nextItem('tab5')"));
         assertEquals(result, "tab1", "Result of function nextItem('tab5')");
 
         // RichFaces.$('form:tabPanel').prevItem('tab1') will be item5
         result = selenium.getEval(new JavaScript("window.RichFaces.$('" + panelId + "').prevItem('tab1')"));
         assertEquals(result, "tab5", "Result of function prevItem('tab1')");
     }
 
     @Test
     public void testDir() {
         JQueryLocator ltrInput = pjq("input[type=radio][name$=dirInput][value=LTR]");
         JQueryLocator rtlInput = pjq("input[type=radio][name$=dirInput][value=RTL]");
         AttributeLocator<?> dirAttribute = panel.getAttribute(new Attribute("dir"));
 
         // dir = null
         assertFalse(selenium.isAttributePresent(dirAttribute), "Attribute dir should not be present.");
 
         // dir = ltr
         selenium.click(ltrInput);
         selenium.waitForPageToLoad();
         assertTrue(selenium.isAttributePresent(dirAttribute), "Attribute dir should be present.");
         String value = selenium.getAttribute(dirAttribute);
         assertEquals(value, "LTR", "Attribute dir");
 
         // dir = rtl
         selenium.click(rtlInput);
         selenium.waitForPageToLoad();
         assertTrue(selenium.isAttributePresent(dirAttribute), "Attribute dir should be present.");
         value = selenium.getAttribute(dirAttribute);
         assertEquals(value, "RTL", "Attribute dir");
     }
 
     @Test
     public void testImmediate() {
         JQueryLocator input = pjq("input[type=radio][name$=immediateInput][value=true]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         selenium.click(inactiveHeaders[2]);
         waitGui.failWith("Tab 3 is not displayed.").until(isDisplayed.locator(itemContents[2]));
 
         assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.RENDER_RESPONSE);
     }
 
     @Test
     public void testLang() {
         JQueryLocator langInput = pjq("input[type=text][id$=langInput]");
 
         // lang = null
         AttributeLocator<?> langAttr = panel.getAttribute(new Attribute("xml|lang"));
         assertFalse(selenium.isAttributePresent(langAttr), "Attribute xml:lang should not be present.");
 
         selenium.type(langInput, "sk");
         selenium.waitForPageToLoad();
 
         // lang = sk
         langAttr = panel.getAttribute(new Attribute("lang"));
         assertTrue(selenium.isAttributePresent(langAttr), "Attribute xml:lang should be present.");
         assertEquals(selenium.getAttribute(langAttr), "sk", "Attribute xml:lang should be present.");
     }
 
     @Test
     @IssueTracking("https://jira.jboss.org/browse/RF-9535")
     public void testLimitToList() {
         JQueryLocator timeLoc = jq("span[id$=requestTime]");
 
         selenium.type(pjq("input[type=text][id$=renderInput]"), "@this");
         selenium.waitForPageToLoad();
 
         selenium.click(pjq("input[type=radio][name$=limitToListInput][value=true]"));
         selenium.waitForPageToLoad();
 
         String time = selenium.getText(timeLoc);
 
         guardXhr(selenium).click(inactiveHeaders[1]);
         waitGui.failWith("Tab 2 is not displayed.").until(isDisplayed.locator(itemContents[1]));
 
         String newTime = selenium.getText(timeLoc);
         assertNotSame(newTime, time, "Panel with ajaxRendered=true should not be rerendered.");
     }
 
     @Test
     public void testOnbeforeitemchange() {
         selenium.type(pjq("input[id$=onbeforeitemchangeInput]"), "metamerEvents += \"onbeforeitemchange \"");
         selenium.waitForPageToLoad(TIMEOUT);
 
         guardXhr(selenium).click(inactiveHeaders[1]);
         waitGui.failWith("Tab 2 is not displayed.").until(isDisplayed.locator(itemContents[1]));
 
         waitGui.failWith("onbeforeitemchange attribute does not work correctly").until(new EventFiredCondition(new Event("beforeitemchange")));
     }
 
     @Test
     public void testItemchangeEvents() {
         JQueryLocator time = jq("span[id$=requestTime]");
 
         selenium.type(pjq("input[type=text][id$=onbeforeitemchangeInput]"), "metamerEvents += \"beforeitemchange \"");
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[type=text][id$=onitemchangeInput]"), "metamerEvents += \"itemchange \"");
         selenium.waitForPageToLoad();
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
         String time1Value = selenium.getText(time);
 
         guardXhr(selenium).click(inactiveHeaders[1]);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "beforeitemchange", "Attribute onbeforeitemchange doesn't work");
         assertEquals(events[1], "itemchange", "Attribute onbeforeitemchange doesn't work");
     }
 
     @Test
     public void testOnclick() {
         testFireEvent(Event.CLICK, panel);
     }
 
     @Test
     public void testOndblclick() {
         testFireEvent(Event.DBLCLICK, panel);
     }
 
     @Test
     public void testOnitemchange() {
         selenium.type(pjq("input[id$=onitemchangeInput]"), "metamerEvents += \"onitemchange \"");
         selenium.waitForPageToLoad(TIMEOUT);
 
         guardXhr(selenium).click(inactiveHeaders[1]);
         waitGui.failWith("Tab 2 is not displayed.").until(isDisplayed.locator(itemContents[1]));
 
         waitGui.failWith("onitemchange attribute does not work correctly").until(new EventFiredCondition(new Event("itemchange")));
     }
 
     @Test
     public void testOnmousedown() {
         testFireEvent(Event.MOUSEDOWN, panel);
     }
 
     @Test
     public void testOnmousemove() {
         testFireEvent(Event.MOUSEMOVE, panel);
     }
 
     @Test
     public void testOnmouseout() {
         testFireEvent(Event.MOUSEOUT, panel);
     }
 
     @Test
     public void testOnmouseover() {
         testFireEvent(Event.MOUSEOVER, panel);
     }
 
     @Test
     public void testOnmouseup() {
         testFireEvent(Event.MOUSEUP, panel);
     }
 
     @Test
     public void testRendered() {
         JQueryLocator input = pjq("input[type=radio][name$=renderedInput][value=false]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(panel), "Tab panel should not be rendered when rendered=false.");
     }
 
     @Test
     public void testStyle() {
         testStyle(panel);
     }
 
     @Test
     public void testStyleClass() {
         testStyleClass(panel, "styleClass");
     }
 
     @Test
     public void testTabContentClass() {
         ElementLocator<?> classInput = pjq("input[id$=tabContentClassInput]");
         final String value = "metamer-ftest-class";
 
         selenium.type(classInput, value);
         selenium.waitForPageToLoad();
 
         for (JQueryLocator loc : itemContents) {
             assertTrue(selenium.belongsClass(loc, value), "tabContentClass does not work");
         }
     }
 
     @Test
     public void testTabHeaderClass() {
         selenium.type(pjq("input[id$=tabHeaderClassInput]"), "metamer-ftest-class");
         selenium.waitForPageToLoad();
 
         for (JQueryLocator loc : activeHeaders) {
             assertTrue(selenium.belongsClass(loc, "metamer-ftest-class"), "tabHeaderClass does not work");
         }
 
         for (JQueryLocator loc : inactiveHeaders) {
             assertTrue(selenium.belongsClass(loc, "metamer-ftest-class"), "tabHeaderClass does not work");
         }
 
         for (JQueryLocator loc : disabledHeaders) {
             assertTrue(selenium.belongsClass(loc, "metamer-ftest-class"), "tabHeaderClass does not work");
         }
     }
 
     @Test
     public void testTabHeaderClassActive() {
         selenium.type(pjq("input[id$=tabHeaderClassActiveInput]"), "metamer-ftest-class");
         selenium.waitForPageToLoad();
 
         for (JQueryLocator loc : activeHeaders) {
             assertTrue(selenium.belongsClass(loc, "metamer-ftest-class"), "tabHeaderClassActive does not work");
         }
 
         for (JQueryLocator loc : inactiveHeaders) {
             assertFalse(selenium.belongsClass(loc, "metamer-ftest-class"), "tabHeaderClassActive does not work");
         }
 
         for (JQueryLocator loc : disabledHeaders) {
             assertFalse(selenium.belongsClass(loc, "metamer-ftest-class"), "tabHeaderClassActive does not work");
         }
     }
 
     @Test
     public void testTabHeaderClassDisabled() {
         selenium.type(pjq("input[id$=tabHeaderClassDisabledInput]"), "metamer-ftest-class");
         selenium.waitForPageToLoad();
 
         for (JQueryLocator loc : activeHeaders) {
             assertFalse(selenium.belongsClass(loc, "metamer-ftest-class"), "tabHeaderClassDisabled does not work");
         }
 
         for (JQueryLocator loc : inactiveHeaders) {
             assertFalse(selenium.belongsClass(loc, "metamer-ftest-class"), "tabHeaderClassDisabled does not work");
         }
 
         for (JQueryLocator loc : disabledHeaders) {
             assertTrue(selenium.belongsClass(loc, "metamer-ftest-class"), "tabHeaderClassDisabled does not work");
         }
     }
 
     @Test
     public void testTabHeaderClassInactive() {
         selenium.type(pjq("input[id$=tabHeaderClassInactiveInput]"), "metamer-ftest-class");
         selenium.waitForPageToLoad();
 
         for (JQueryLocator loc : activeHeaders) {
             assertFalse(selenium.belongsClass(loc, "metamer-ftest-class"), "tabHeaderClassInactive does not work");
         }
 
         for (JQueryLocator loc : inactiveHeaders) {
             assertTrue(selenium.belongsClass(loc, "metamer-ftest-class"), "tabHeaderClassInactive does not work");
         }
 
         for (JQueryLocator loc : disabledHeaders) {
             assertFalse(selenium.belongsClass(loc, "metamer-ftest-class"), "tabHeaderClassInactive does not work");
         }
     }
 
     @Test
     public void testTitle() {
         JQueryLocator input = pjq("input[type=text][id$=titleInput]");
         AttributeLocator<?> attribute = panel.getAttribute(new Attribute("title"));
 
         // title = null
         assertFalse(selenium.isAttributePresent(attribute), "Attribute title should not be present.");
 
         // title = "RichFaces Tab Panel"
         selenium.type(input, "RichFaces Tab Panel");
         selenium.waitForPageToLoad(TIMEOUT);
 
         assertTrue(selenium.isAttributePresent(attribute), "Attribute title should be present.");
         String value = selenium.getAttribute(attribute);
         assertEquals(value, "RichFaces Tab Panel", "Attribute title");
     }
 }
