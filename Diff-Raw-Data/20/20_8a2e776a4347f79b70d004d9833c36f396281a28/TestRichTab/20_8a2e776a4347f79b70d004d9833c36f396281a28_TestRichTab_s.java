 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010-2011, Red Hat, Inc. and individual contributors
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
 package org.richfaces.tests.metamer.ftest.richTab;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardHttp;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardNoRequest;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.net.URL;
 
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.locator.Attribute;
 import org.jboss.test.selenium.locator.AttributeLocator;
 import org.jboss.test.selenium.locator.ElementLocator;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page /faces/components/richTab/simple.xhtml
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestRichTab extends AbstractMetamerTest {
 
     private JQueryLocator panel = pjq("div[id$=tabPanel]");
    private JQueryLocator[] itemContents = {pjq("div[id$=tab1] > div.rf-tb-cnt"), pjq("div[id$=tab2] > div.rf-tb-cnt"),
        pjq("div[id$=tab3] > div.rf-tb-cnt"), pjq("div[id$=tab4] > div.rf-tb-cnt"), pjq("div[id$=tab5] > div.rf-tb-cnt")};
     private JQueryLocator[] activeHeaders = {pjq("td[id$=tab1:header:active]"), pjq("td[id$=tab2:header:active]"),
         pjq("td[id$=tab3:header:active]"), pjq("td[id$=tab4:header:active]"), pjq("td[id$=tab5:header:active]")};
     private JQueryLocator[] inactiveHeaders = {pjq("td[id$=tab1:header:inactive]"), pjq("td[id$=tab2:header:inactive]"),
         pjq("td[id$=tab3:header:inactive]"), pjq("td[id$=tab4:header:inactive]"), pjq("td[id$=tab5:header:inactive]")};
     private JQueryLocator[] disabledHeaders = {pjq("td[id$=tab1:header:disabled]"), pjq("td[id$=tab2:header:disabled]"),
         pjq("td[id$=tab3:header:disabled]"), pjq("td[id$=tab4:header:disabled]"), pjq("td[id$=tab5:header:disabled]")};
     private JQueryLocator tab = pjq("div[id$=tab1]");
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richTab/simple.xhtml");
     }
 
     @Test
     public void testInit() {
         boolean displayed = selenium.isDisplayed(panel);
         assertTrue(displayed, "Tab panel is not present on the page.");
 
         displayed = selenium.isDisplayed(activeHeaders[0]);
         assertTrue(displayed, "Header of tab1 should be active.");
         displayed = selenium.isDisplayed(inactiveHeaders[0]);
         assertFalse(displayed, "Header of tab1 should not be inactive.");
         displayed = selenium.isDisplayed(disabledHeaders[3]);
         assertTrue(displayed, "Header of tab4 should be disabled.");
 
         String text = selenium.getText(activeHeaders[0]);
         assertEquals(text, "tab1 header");
         text = selenium.getText(inactiveHeaders[1]);
         assertEquals(text, "tab2 header");
         text = selenium.getText(disabledHeaders[3]);
         assertEquals(text, "tab4 header");
         text = selenium.getText(itemContents[0]);
         assertEquals(text, "content of tab 1");
 
     }
 
     @Test
     public void testContentClass() {
         selenium.type(pjq("input[id$=contentClassInput]"), "metamer-ftest-class");
         selenium.waitForPageToLoad();
 
         assertTrue(selenium.belongsClass(itemContents[0], "metamer-ftest-class"), "contentClass does not work");
     }
 
     @Test
     public void testDir() {
         testDir(tab);
     }
 
     @Test
     public void testDisabled() {
         // disable the first tab
         selenium.click(pjq("input[type=radio][name$=disabledInput][value=true]"));
         selenium.waitForPageToLoad();
 
         boolean displayed = selenium.isDisplayed(activeHeaders[0]);
         assertFalse(displayed, "Header of tab1 should not be active.");
         displayed = selenium.isDisplayed(inactiveHeaders[0]);
         assertFalse(displayed, "Header of tab1 should not be inactive.");
         displayed = selenium.isDisplayed(disabledHeaders[0]);
         assertTrue(displayed, "Header of tab1 should be disabled.");
 
         String text = selenium.getText(disabledHeaders[0]);
         assertEquals(text, "tab1 header");
 
         // enable the first tab
         selenium.click(pjq("input[type=radio][name$=disabledInput][value=false]"));
         selenium.waitForPageToLoad();
 
         displayed = selenium.isDisplayed(activeHeaders[0]);
         assertTrue(displayed, "Header of tab1 should not be active.");
         displayed = selenium.isDisplayed(inactiveHeaders[0]);
         assertFalse(displayed, "Header of tab1 should not be inactive.");
         displayed = selenium.isDisplayed(disabledHeaders[0]);
         assertFalse(displayed, "Header of tab1 should be disabled.");
 
         text = selenium.getText(activeHeaders[0]);
         assertEquals(text, "tab1 header");
     }
 
     @Test
     public void testHeader() {
         selenium.type(pjq("input[type=text][id$=headerInput]"), "new header");
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(activeHeaders[0]), "new header", "Header of the first tab did not change.");
 
         selenium.type(pjq("input[type=text][id$=headerInput]"), "ľščťťžžôúňď ацущьмщфзщйцу");
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(activeHeaders[0]), "ľščťťžžôúňď ацущьмщфзщйцу", "Header of the first tab did not change.");
     }
 
     @Test
     public void testHeaderActiveClass() {
         selenium.type(pjq("input[id$=headerActiveClassInput]"), "metamer-ftest-class");
         selenium.waitForPageToLoad();
 
         assertTrue(selenium.belongsClass(activeHeaders[0], "metamer-ftest-class"), "headerActiveClass does not work");
         assertFalse(selenium.belongsClass(inactiveHeaders[0], "metamer-ftest-class"), "headerActiveClass does not work");
         assertFalse(selenium.belongsClass(disabledHeaders[0], "metamer-ftest-class"), "headerActiveClass does not work");
 
         assertFalse(selenium.belongsClass(activeHeaders[1], "metamer-ftest-class"), "headerActiveClass does not work");
         assertFalse(selenium.belongsClass(inactiveHeaders[1], "metamer-ftest-class"), "headerActiveClass does not work");
         assertFalse(selenium.belongsClass(disabledHeaders[1], "metamer-ftest-class"), "headerActiveClass does not work");
     }
 
     @Test
     public void testHeaderClass() {
         selenium.type(pjq("input[id$=headerClassInput]"), "metamer-ftest-class");
         selenium.waitForPageToLoad();
 
         assertTrue(selenium.belongsClass(activeHeaders[0], "metamer-ftest-class"), "tabHeaderClass does not work");
         assertTrue(selenium.belongsClass(inactiveHeaders[0], "metamer-ftest-class"), "tabHeaderClass does not work");
         assertTrue(selenium.belongsClass(disabledHeaders[0], "metamer-ftest-class"), "tabHeaderClass does not work");
 
         assertFalse(selenium.belongsClass(activeHeaders[1], "metamer-ftest-class"), "tabHeaderClass does not work");
         assertFalse(selenium.belongsClass(inactiveHeaders[1], "metamer-ftest-class"), "tabHeaderClass does not work");
         assertFalse(selenium.belongsClass(disabledHeaders[1], "metamer-ftest-class"), "tabHeaderClass does not work");
     }
 
     @Test
     public void testHeaderDisabledClass() {
         selenium.type(pjq("input[id$=headerDisabledClassInput]"), "metamer-ftest-class");
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.belongsClass(activeHeaders[0], "metamer-ftest-class"), "headerDisabledClass does not work");
         assertFalse(selenium.belongsClass(inactiveHeaders[0], "metamer-ftest-class"), "headerDisabledClass does not work");
         assertTrue(selenium.belongsClass(disabledHeaders[0], "metamer-ftest-class"), "headerDisabledClass does not work");
 
         assertFalse(selenium.belongsClass(activeHeaders[1], "metamer-ftest-class"), "headerDisabledClass does not work");
         assertFalse(selenium.belongsClass(inactiveHeaders[1], "metamer-ftest-class"), "headerDisabledClass does not work");
         assertFalse(selenium.belongsClass(disabledHeaders[1], "metamer-ftest-class"), "headerDisabledClass does not work");
     }
 
     @Test
     public void testHeaderInactiveClass() {
         selenium.type(pjq("input[id$=headerInactiveClassInput]"), "metamer-ftest-class");
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.belongsClass(activeHeaders[0], "metamer-ftest-class"), "headerInactiveClass does not work");
         assertTrue(selenium.belongsClass(inactiveHeaders[0], "metamer-ftest-class"), "headerInactiveClass does not work");
         assertFalse(selenium.belongsClass(disabledHeaders[0], "metamer-ftest-class"), "headerInactiveClass does not work");
 
         assertFalse(selenium.belongsClass(activeHeaders[1], "metamer-ftest-class"), "headerInactiveClass does not work");
         assertFalse(selenium.belongsClass(inactiveHeaders[1], "metamer-ftest-class"), "headerInactiveClass does not work");
         assertFalse(selenium.belongsClass(disabledHeaders[1], "metamer-ftest-class"), "headerInactiveClass does not work");
     }
 
     @Test
     public void testHeaderStyle() {
         ElementLocator<?> styleInput = pjq("input[id$=headerStyleInput]");
         final String value = "background-color: yellow; font-size: 1.5em;";
 
         selenium.type(styleInput, value);
         selenium.waitForPageToLoad();
 
         AttributeLocator<?> styleAttr = activeHeaders[0].getAttribute(Attribute.STYLE);
         assertTrue(selenium.getAttribute(styleAttr).contains(value), "Attribute style should contain \"" + value + "\"");
     }
 
     @Test
     public void testLang() {
         testLang(tab);
     }
 
     @Test
     public void testName() {
         selenium.type(pjq("input[type=text][id$=nameInput]"), "metamer");
         selenium.waitForPageToLoad();
 
         selenium.click(pjq("input[id$=lastTabButton]"));
         waitGui.failWith("Item 3 was not displayed.").until(isDisplayed.locator(itemContents[4]));
 
         selenium.click(pjq("input[id$=customTabButton]"));
         waitGui.failWith("Item 1 was not displayed.").until(isDisplayed.locator(itemContents[0]));
     }
 
     @Test
     public void testOnclick() {
         testFireEvent(Event.CLICK, tab);
     }
 
     @Test
     public void testOndblclick() {
         testFireEvent(Event.DBLCLICK, tab);
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9537")
     public void testOnenter() {
         selenium.type(pjq("input[type=text][id$=onenterInput]"), "metamerEvents += \"enter \"");
         selenium.waitForPageToLoad();
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
         String time1Value = selenium.getText(time);
 
         guardXhr(selenium).click(inactiveHeaders[1]);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time));
         guardXhr(selenium).click(inactiveHeaders[0]);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "enter", "Attribute onenter doesn't work");
     }
 
     @Test
     public void testOnheaderclick() {
         testFireEvent(Event.CLICK, activeHeaders[0], "headerclick");
     }
 
     @Test
     public void testOnheaderdblclick() {
         testFireEvent(Event.DBLCLICK, activeHeaders[0], "headerdblclick");
     }
 
     @Test
     public void testOnheadermousedown() {
         testFireEvent(Event.MOUSEDOWN, activeHeaders[0], "headermousedown");
     }
 
     @Test
     public void testOnheadermousemove() {
         testFireEvent(Event.MOUSEMOVE, activeHeaders[0], "headermousemove");
     }
 
     @Test
     public void testOnheadermouseup() {
         testFireEvent(Event.MOUSEUP, activeHeaders[0], "headermouseup");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9537")
     public void testOnleave() {
         selenium.type(pjq("input[type=text][id$=onleaveInput]"), "metamerEvents += \"leave \"");
         selenium.waitForPageToLoad();
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
         String time1Value = selenium.getText(time);
 
         guardXhr(selenium).click(inactiveHeaders[1]);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "leave", "Attribute onleave doesn't work");
     }
 
     @Test
     public void testOnmousedown() {
         testFireEvent(Event.MOUSEDOWN, tab);
     }
 
     @Test
     public void testOnmousemove() {
         testFireEvent(Event.MOUSEMOVE, tab);
     }
 
     @Test
     public void testOnmouseout() {
         testFireEvent(Event.MOUSEOUT, tab);
     }
 
     @Test
     public void testOnmouseover() {
         testFireEvent(Event.MOUSEOVER, tab);
     }
 
     @Test
     public void testOnmouseup() {
         testFireEvent(Event.MOUSEUP, tab);
     }
 
     @Test
     public void testRendered() {
         JQueryLocator input = pjq("input[type=radio][name$=renderedInput][value=false]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(activeHeaders[0]), "Tab should not be rendered when rendered=false.");
         assertFalse(selenium.isElementPresent(inactiveHeaders[0]), "Tab should not be rendered when rendered=false.");
         assertFalse(selenium.isElementPresent(disabledHeaders[0]), "Tab should not be rendered when rendered=false.");
         assertFalse(selenium.isElementPresent(tab), "Tab should not be rendered when rendered=false.");
     }
 
     @Test
     public void testStyle() {
         testStyle(tab, "style");
     }
 
     @Test
     public void testStyleClass() {
         testStyleClass(tab, "styleClass");
     }
 
     @Test
     public void testSwitchTypeNull() {
         guardXhr(selenium).click(inactiveHeaders[1]);
         waitGui.failWith("Tab 2 is not displayed.").until(isDisplayed.locator(itemContents[1]));
 
         guardXhr(selenium).click(inactiveHeaders[0]);
         waitGui.failWith("Tab 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
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
 
         guardXhr(selenium).click(inactiveHeaders[1]);
         waitGui.failWith("Tab 2 is not displayed.").until(isDisplayed.locator(itemContents[1]));
 
         guardNoRequest(selenium).click(inactiveHeaders[0]);
         waitGui.failWith("Tab 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
     }
 
     @Test
     public void testSwitchTypeServer() {
         JQueryLocator selectOption = pjq("input[name$=switchTypeInput][value=server]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(inactiveHeaders[1]);
         waitGui.failWith("Tab 2 is not displayed.").until(isDisplayed.locator(itemContents[1]));
 
         guardHttp(selenium).click(inactiveHeaders[0]);
         waitGui.failWith("Tab 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
     }
 
     @Test
     public void testTitle() {
         testTitle(tab);
     }
 }
