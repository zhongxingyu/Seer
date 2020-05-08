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
 package org.richfaces.tests.metamer.ftest.richAccordionItem;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardHttp;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardNoRequest;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.net.URL;
 
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page faces/components/richAccordionItem/simple.xhtml.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestRichAccordionItem extends AbstractMetamerTest {
 
     private JQueryLocator accordion = pjq("div[id$=accordion]");
     private JQueryLocator item1 = pjq("div[id$=item1].rf-ac-itm");
     private JQueryLocator[] itemHeaders = {pjq("div[id$=item1:header]"), pjq("div[id$=item2:header]"),
         pjq("div[id$=item3:header]"), pjq("div[id$=item4:header]"), pjq("div[id$=item5:header]")};
     private JQueryLocator[] itemContents = {pjq("div[id$=item1:content]"), pjq("div[id$=item2:content]"),
         pjq("div[id$=item3:content]"), pjq("div[id$=item4:content]"), pjq("div[id$=item5:content]")};
     private JQueryLocator[] activeHeaders = {pjq("div.rf-ac-itm-hdr-act:eq(0)"), pjq("div.rf-ac-itm-hdr-act:eq(1)"),
         pjq("div.rf-ac-itm-hdr-act:eq(2)"), pjq("div.rf-ac-itm-hdr-act:eq(3)"), pjq("div.rf-ac-itm-hdr-act:eq(4)")};
     private JQueryLocator[] inactiveHeaders = {pjq("div.rf-ac-itm-hdr-inact:eq(0)"),
         pjq("div.rf-ac-itm-hdr-inact:eq(1)"), pjq("div.rf-ac-itm-hdr-inact:eq(2)"),
         pjq("div.rf-ac-itm-hdr-inact:eq(3)"), pjq("div.rf-ac-itm-hdr-inact:eq(4)")};
     private JQueryLocator[] disabledHeaders = {pjq("div.rf-ac-itm-hdr-dis:eq(0)"), pjq("div.rf-ac-itm-hdr-dis:eq(1)"),
         pjq("div.rf-ac-itm-hdr-dis:eq(2)"), pjq("div.rf-ac-itm-hdr-dis:eq(3)"), pjq("div.rf-ac-itm-hdr-dis:eq(4)")};
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richAccordionItem/simple.xhtml");
     }
 
     @Test
     public void testInit() {
         boolean accordionDisplayed = selenium.isDisplayed(accordion);
         assertTrue(accordionDisplayed, "Accordion is not present on the page.");
 
         accordionDisplayed = selenium.isDisplayed(itemHeaders[2]);
         assertTrue(accordionDisplayed, "Item3's header should be visible.");
 
 
         accordionDisplayed = selenium.isDisplayed(itemContents[2]);
         assertTrue(accordionDisplayed, "Content of item3 should be visible.");
 
         accordionDisplayed = selenium.isDisplayed(itemContents[0]);
         assertFalse(accordionDisplayed, "Item1's content should not be visible.");
     }
 
     @Test
     public void testContentClass() {
         testStyleClass(itemContents[0], "contentClass");
     }
 
     @Test
     public void testDir() {
         testDir(item1);
     }
 
     @Test
     public void testDisabled() {
        JQueryLocator selectOption = pjq("input[type=radio][name$=disabledInput][value=true]");
        selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         guardNoRequest(selenium).click(itemHeaders[0]);
         boolean accordionDisplayed = selenium.isDisplayed(itemContents[0]);
         assertFalse(accordionDisplayed, "Item1's content should not be visible.");
     }
 
     @Test
     public void testHeader() {
         selenium.type(pjq("input[id$=headerInput]"), "new header");
         selenium.waitForPageToLoad();
 
         String header = selenium.getText(itemHeaders[0]);
         assertEquals(header, "new header", "Header of item1 did not change.");
     }
 
     @Test
     public void testHeaderClass() {
         testStyleClass(itemHeaders[0], "headerClass");
         assertFalse(selenium.belongsClass(itemHeaders[1], "metamer-ftest-class"), "headerClass should be set only on first item");
         assertFalse(selenium.belongsClass(itemHeaders[2], "metamer-ftest-class"), "headerClass should be set only on first item");
         assertFalse(selenium.belongsClass(itemHeaders[3], "metamer-ftest-class"), "headerClass should be set only on first item");
     }
 
     @Test
     public void testHeaderClassActive() {
         testStyleClass(activeHeaders[0], "headerClassActive");
         assertFalse(selenium.belongsClass(activeHeaders[1], "metamer-ftest-class"), "headerClassActive should be set only on first item");
         assertFalse(selenium.belongsClass(activeHeaders[2], "metamer-ftest-class"), "headerClassActive should be set only on first item");
         assertFalse(selenium.belongsClass(activeHeaders[3], "metamer-ftest-class"), "headerClassActive should be set only on first item");
     }
 
     @Test
     public void testHeaderClassDisabled() {
         testStyleClass(disabledHeaders[0], "headerClassDisabled");
         assertFalse(selenium.belongsClass(disabledHeaders[1], "metamer-ftest-class"), "headerClassDisabled should be set only on first item");
         assertFalse(selenium.belongsClass(disabledHeaders[2], "metamer-ftest-class"), "headerClassDisabled should be set only on first item");
         assertFalse(selenium.belongsClass(disabledHeaders[3], "metamer-ftest-class"), "headerClassDisabled should be set only on first item");
     }
 
     @Test
     public void testHeaderClassInactive() {
         testStyleClass(inactiveHeaders[0], "headerClassInactive");
         assertFalse(selenium.belongsClass(inactiveHeaders[1], "metamer-ftest-class"), "headerClassInactive should be set only on first item");
         assertFalse(selenium.belongsClass(inactiveHeaders[2], "metamer-ftest-class"), "headerClassInactive should be set only on first item");
         assertFalse(selenium.belongsClass(inactiveHeaders[3], "metamer-ftest-class"), "headerClassInactive should be set only on first item");
     }
 
     @Test
     public void testHeaderStyle() {
         testStyle(itemHeaders[0], "headerStyle");
     }
 
     @Test
     public void testLang() {
         testLang(item1);
     }
 
     @Test
     public void testName() {
         selenium.type(pjq("input[id$=nameInput]"), "new name");
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(pjq("input[type=submit][name$=switchButtonCustom]"));
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
     }
 
     @Test
     public void testOnclick() {
         testFireEvent(Event.CLICK, item1);
     }
 
     @Test
     public void testOndblclick() {
         testFireEvent(Event.DBLCLICK, item1);
     }
 
     @Test
     @IssueTracking("https://jira.jboss.org/browse/RF-9821")
     public void testOnenter() {
        testFireEvent(Event.CLICK, item1, "enter");
     }
 
     @Test
     public void testOnheaderclick() {
         testFireEvent(Event.CLICK, itemHeaders[0], "headerclick");
     }
 
     @Test
     public void testOnheaderdblclick() {
         testFireEvent(Event.DBLCLICK, itemHeaders[0], "headerdblclick");
     }
 
     @Test
     public void testOnheadermousedown() {
         testFireEvent(Event.MOUSEDOWN, itemHeaders[0], "headermousedown");
     }
 
     @Test
     public void testOnheadermousemove() {
         testFireEvent(Event.MOUSEMOVE, itemHeaders[0], "headermousemove");
     }
 
     @Test
     public void testOnheadermouseup() {
         testFireEvent(Event.MOUSEUP, itemHeaders[0], "headermouseup");
     }
 
     @Test
     @IssueTracking("https://jira.jboss.org/browse/RF-9821")
     public void testOnleave() {
         JQueryLocator time = jq("span[id$=requestTime]");
 
         selenium.type(pjq("input[type=text][id$=onleaveInput]"), "metamerEvents += \"leave \"");
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(itemHeaders[0]);
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
         String time1Value = selenium.getText(time);
 
         guardXhr(selenium).click(itemHeaders[1]);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "leave", "Attribute onleave doesn't work");
     }
 
     @Test
     public void testOnmousedown() {
         testFireEvent(Event.MOUSEDOWN, item1);
     }
 
     @Test
     public void testOnmousemove() {
         testFireEvent(Event.MOUSEMOVE, item1);
     }
 
     @Test
     public void testOnmouseout() {
         testFireEvent(Event.MOUSEOUT, item1);
     }
 
     @Test
     public void testOnmouseover() {
         testFireEvent(Event.MOUSEOVER, item1);
     }
 
     @Test
     public void testOnmouseup() {
         testFireEvent(Event.MOUSEUP, item1);
     }
 
     @Test
     public void testRendered() {
        JQueryLocator input = pjq("input[type=radio][name$=renderedInput][value=false]");
        selenium.click(input);
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(item1), "Item1 should not be rendered when rendered=false.");
     }
 
     @Test
     public void testStyle() {
         testStyle(item1, "style");
     }
 
     @Test
     public void testStyleClass() {
         testStyleClass(item1, "styleClass");
     }
 
     @Test
     public void testSwitchTypeNull() {
         guardXhr(selenium).click(itemHeaders[0]);
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
     }
 
     @Test
     public void testSwitchTypeAjax() {
        JQueryLocator selectOption = pjq("input[type=radio][name$=switchTypeInput][value=ajax]");
        selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         testSwitchTypeNull();
     }
 
     @Test
     public void testSwitchTypeClient() {
        JQueryLocator selectOption = pjq("input[type=radio][name$=switchTypeInput][value=client]");
        selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         guardNoRequest(selenium).click(itemHeaders[0]);
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
     }
 
     @Test
     public void testSwitchTypeServer() {
        JQueryLocator selectOption = pjq("input[type=radio][name$=switchTypeInput][value=server]");
        selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         guardHttp(selenium).click(itemHeaders[0]);
         waitGui.failWith("Item 1 is not displayed.").until(isDisplayed.locator(itemContents[0]));
     }
 
     @Test
     public void testTitle() {
         testTitle(item1);
     }
 }
