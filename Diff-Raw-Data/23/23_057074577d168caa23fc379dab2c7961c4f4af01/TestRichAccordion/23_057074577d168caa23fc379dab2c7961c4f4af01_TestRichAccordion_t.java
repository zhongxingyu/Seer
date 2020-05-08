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
 package org.richfaces.tests.metamer.ftest.richAccordion;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardHttp;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardNoRequest;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertNotSame;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import java.net.URL;
 
 import javax.faces.event.PhaseId;
 
 import org.jboss.test.selenium.css.CssProperty;
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.locator.Attribute;
 import org.jboss.test.selenium.locator.AttributeLocator;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page /faces/components/richAccordion/simple.xhtml
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestRichAccordion extends AbstractMetamerTest {
 
     private JQueryLocator accordion = pjq("div[id$=accordion]");
     private JQueryLocator[] itemHeaders = {pjq("div[id$=item1:header]"), pjq("div[id$=item2:header]"),
         pjq("div[id$=item3:header]"), pjq("div[id$=item4:header]"), pjq("div[id$=item5:header]")};
     private JQueryLocator[] itemContents = {pjq("div[id$=item1:content]"), pjq("div[id$=item2:content]"),
         pjq("div[id$=item3:content]"), pjq("div[id$=item4:content]"), pjq("div[id$=item5:content]")};
     private JQueryLocator[] activeHeaders = {pjq("div[id$=item1:header] div.rf-ac-itm-lbl-act"),
         pjq("div[id$=item2:header] div.rf-ac-itm-lbl-act"), pjq("div[id$=item3:header] div.rf-ac-itm-lbl-act"),
         pjq("div[id$=item4:header] div.rf-ac-itm-lbl-act"), pjq("div[id$=item5:header] div.rf-ac-itm-lbl-act")};
     private JQueryLocator[] inactiveHeaders = {pjq("div[id$=item1:header] div.rf-ac-itm-lbl-inact"),
         pjq("div[id$=item2:header] div.rf-ac-itm-lbl-inact"), pjq("div[id$=item3:header] div.rf-ac-itm-lbl-inact"),
         pjq("div[id$=item4:header] div.rf-ac-itm-lbl-inact"), pjq("div[id$=item5:header] div.rf-ac-itm-lbl-inact")};
     private JQueryLocator[] disabledHeaders = {pjq("div[id$=item1:header] div.rf-ac-itm-lbl-dis"),
         pjq("div[id$=item2:header] div.rf-ac-itm-lbl-dis"), pjq("div[id$=item3:header] div.rf-ac-itm-lbl-dis"),
         pjq("div[id$=item4:header] div.rf-ac-itm-lbl-dis"), pjq("div[id$=item5:header] div.rf-ac-itm-lbl-dis")};
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richAccordion/simple.xhtml");
     }
 
     @Test
     public void testInit() {
         boolean accordionDisplayed = selenium.isDisplayed(accordion);
         assertTrue(accordionDisplayed, "Accordion is not present on the page.");
 
         for (int i = 0; i < 5; i++) {
             accordionDisplayed = selenium.isDisplayed(itemHeaders[i]);
             assertTrue(accordionDisplayed, "Item" + (i + 1) + "'s header should be visible.");
         }
 
         accordionDisplayed = selenium.isDisplayed(itemContents[0]);
         assertTrue(accordionDisplayed, "Content of item1 should be visible.");
 
         for (int i = 1; i < 5; i++) {
             accordionDisplayed = selenium.isDisplayed(itemContents[i]);
             assertFalse(accordionDisplayed, "Item" + (i + 1) + "'s content should not be visible.");
         }
     }
 
     @Test
     public void testSwitchTypeNull() {
         for (int i = 2; i >= 0; i--) {
             final int index = i;
             guardXhr(selenium).click(itemHeaders[index]);
             waitGui.failWith("Item " + index + " is not displayed.").until(isDisplayed.locator(itemContents[index]));
         }
     }
 
     @Test
     public void testSwitchTypeAjax() {
         selenium.click(pjq("input[type=radio][name$=switchTypeInput][value=ajax]"));
         selenium.waitForPageToLoad();
 
         testSwitchTypeNull();
     }
 
     @Test
     public void testSwitchTypeClient() {
         selenium.click(pjq("input[type=radio][name$=switchTypeInput][value=client]"));
         selenium.waitForPageToLoad();
 
         for (int i = 2; i >= 0; i--) {
             final int index = i;
             guardNoRequest(selenium).click(itemHeaders[index]);
             waitGui.failWith("Item " + index + " is not displayed.").until(isDisplayed.locator(itemContents[index]));
         }
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10040")
     public void testSwitchTypeServer() {
         selenium.click(pjq("input[type=radio][name$=switchTypeInput][value=server]"));
         selenium.waitForPageToLoad();
 
         for (int i = 2; i >= 0; i--) {
             final int index = i;
             guardHttp(selenium).click(itemHeaders[index]);
             waitGui.failWith("Item " + index + " is not displayed.").until(isDisplayed.locator(itemContents[index]));
         }
     }
 
     @Test
     public void testActiveItem() {
         selenium.type(pjq("input[type=text][id$=activeItemInput]"), "item5");
         selenium.waitForPageToLoad();
 
         boolean accordionDisplayed = selenium.isDisplayed(accordion);
         assertTrue(accordionDisplayed, "Accordion is not present on the page.");
 
         for (int i = 0; i < 5; i++) {
             accordionDisplayed = selenium.isDisplayed(itemHeaders[i]);
             assertTrue(accordionDisplayed, "Item" + (i + 1) + "'s header should be visible.");
         }
 
         accordionDisplayed = selenium.isDisplayed(itemContents[4]);
         assertTrue(accordionDisplayed, "Content of item5 should be visible.");
 
         for (int i = 0; i < 4; i++) {
             accordionDisplayed = selenium.isDisplayed(itemContents[i]);
             assertFalse(accordionDisplayed, "Item" + (i + 1) + "'s content should not be visible.");
         }
 
         selenium.type(pjq("input[type=text][id$=activeItemInput]"), "item4");
         selenium.waitForPageToLoad();
 
         for (int i = 0; i < 5; i++) {
             accordionDisplayed = selenium.isDisplayed(itemHeaders[i]);
             assertTrue(accordionDisplayed, "Item" + (i + 1) + "'s header should be visible.");
         }
 
         for (int i = 0; i < 5; i++) {
             accordionDisplayed = selenium.isDisplayed(itemContents[i]);
             assertFalse(accordionDisplayed, "Item" + (i + 1) + "'s content should not be visible.");
         }
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10054")
     public void testBypassUpdates() {
         selenium.click(pjq("input[type=radio][name$=bypassUpdatesInput][value=true]"));
         selenium.waitForPageToLoad();
 
         selenium.click(itemHeaders[2]);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(itemContents[2]));
 
         assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.PROCESS_VALIDATIONS,
                 PhaseId.RENDER_RESPONSE);
     }
 
     @Test
     public void testCycledSwitching() {
         String accordionId = selenium.getEval(new JavaScript("window.testedComponentId"));
         String result = null;
 
         // RichFaces.$('form:accordion').nextItem('item4') will be null
         result = selenium.getEval(new JavaScript("window.RichFaces.$('" + accordionId + "').nextItem('item4')"));
         assertEquals(result, "null", "Result of function nextItem('item4')");
 
         // RichFaces.$('form:accordion').prevItem('item1') will be null
         result = selenium.getEval(new JavaScript("window.RichFaces.$('" + accordionId + "').prevItem('item1')"));
         assertEquals(result, "null", "Result of function prevItem('item1')");
 
         selenium.click(pjq("input[type=radio][name$=cycledSwitchingInput][value=true]"));
         selenium.waitForPageToLoad();
 
         // RichFaces.$('form:accordion').nextItem('item5') will be item1
         result = selenium.getEval(new JavaScript("window.RichFaces.$('" + accordionId + "').nextItem('item5')"));
         assertEquals(result, "item1", "Result of function nextItem('item5')");
 
         // RichFaces.$('form:accordion').prevItem('item1') will be item5
         result = selenium.getEval(new JavaScript("window.RichFaces.$('" + accordionId + "').prevItem('item1')"));
         assertEquals(result, "item5", "Result of function prevItem('item1')");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10061")
     public void testData() {
         selenium.type(pjq("input[type=text][id$=dataInput]"), "RichFaces 4");
         selenium.waitForPageToLoad();
 
         selenium.type(pjq("input[type=text][id$=onitemchangeInput]"), "data = event.data");
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(itemHeaders[2]);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(itemContents[2]));
 
         String data = selenium.getEval(new JavaScript("window.data"));
         assertEquals(data, "RichFaces 4", "Data sent with ajax request");
     }
 
     @Test
     public void testDir() {
         testDir(accordion);
     }
 
     @Test
     public void testExecute() {
         selenium.type(pjq("input[type=text][id$=executeInput]"), "@this executeChecker");
         selenium.waitForPageToLoad();
 
         guardXhr(selenium).click(itemHeaders[2]);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(itemContents[2]));
 
         JQueryLocator logItems = jq("ul.phases-list li:eq({0})");
         for (int i = 0; i < 6; i++) {
             if ("* executeChecker".equals(selenium.getText(logItems.format(i)))) {
                 return;
             }
         }
 
         fail("Attribute execute does not work");
     }
 
     @Test
     public void testHeight() {
         AttributeLocator<?> attribute = accordion.getAttribute(new Attribute("style"));
 
         // height = null
         assertFalse(selenium.isAttributePresent(attribute), "Attribute style should not be present.");
 
         // height = 300px
         selenium.type(pjq("input[type=text][id$=heightInput]"), "300px");
         selenium.waitForPageToLoad(TIMEOUT);
 
         assertTrue(selenium.isAttributePresent(attribute), "Attribute style should be present.");
         String value = selenium.getStyle(accordion, CssProperty.HEIGHT);
         assertEquals(value, "300px", "Attribute width");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10054")
     public void testImmediate() {
         selenium.click(pjq("input[type=radio][name$=immediateInput][value=true]"));
         selenium.waitForPageToLoad();
 
         selenium.click(itemHeaders[2]);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(itemContents[2]));
 
         assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.RENDER_RESPONSE);
 
         String listenerOutput = selenium.getText(jq("div#phasesPanel li:eq(2)"));
         assertEquals(listenerOutput, "* item changed item1 -> item3", "Item change listener's output");
     }
 
     @Test
     public void testItemChangeListener() {
         selenium.click(itemHeaders[2]);
         waitGui.failWith("Item 3 is not displayed.").until(isDisplayed.locator(itemContents[2]));
 
         String listenerOutput = selenium.getText(jq("div#phasesPanel li:eq(5)"));
         assertEquals(listenerOutput, "* item changed: item1 -> item3", "Item change listener's output");
     }
 
     @Test
     public void testItemContentClass() {
         testStyleClass(itemContents[2], "itemContentClass");
     }
 
     @Test
     public void testItemHeaderClass() {
         testStyleClass(itemHeaders[2], "itemHeaderClass");
     }
 
     @Test
     public void testItemHeaderClassActive() {
         testStyleClass(activeHeaders[0], "itemHeaderClassActive");
     }
 
     @Test
     public void testItemHeaderClassDisabled() {
         testStyleClass(disabledHeaders[3], "itemHeaderClassDisabled");
     }
 
     @Test
     public void testItemHeaderClassInactive() {
         testStyleClass(inactiveHeaders[1], "itemHeaderClassInactive");
     }
 
     @Test
     public void testItemchangeEvents() {
         selenium.type(pjq("input[type=text][id$=onbeforeitemchangeInput]"), "metamerEvents += \"beforeitemchange \"");
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[type=text][id$=onitemchangeInput]"), "metamerEvents += \"itemchange \"");
         selenium.waitForPageToLoad();
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
         String time1Value = selenium.getText(time);
 
         guardXhr(selenium).click(itemHeaders[2]);
         waitGui.failWith("Page was not updated").waitForChange(time1Value, retrieveText.locator(time));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "beforeitemchange", "Attribute onbeforeitemchange doesn't work");
         assertEquals(events[1], "itemchange", "Attribute onbeforeitemchange doesn't work");
     }
 
     @Test
     public void testLang() {
         testLang(accordion);
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9934")
     public void testLimitRender() {
         selenium.click(pjq("input[type=radio][name$=limitRenderInput][value=true]"));
         selenium.waitForPageToLoad();
 
         String timeValue = selenium.getText(time);
 
         guardXhr(selenium).click(itemHeaders[1]);
         waitGui.failWith("Item 2 is not displayed.").until(isDisplayed.locator(itemContents[1]));
 
         String newTime = selenium.getText(time);
         assertNotSame(newTime, timeValue, "Panel with ajaxRendered=true should not be rerendered.");
     }
 
     @Test
     public void testOnclick() {
         testFireEvent(Event.CLICK, accordion);
     }
 
     @Test
     public void testOndblclick() {
         testFireEvent(Event.DBLCLICK, accordion);
     }
 
     @Test
     public void testOnmousedown() {
         testFireEvent(Event.MOUSEDOWN, accordion);
     }
 
     @Test
     public void testOnmousemove() {
         testFireEvent(Event.MOUSEMOVE, accordion);
     }
 
     @Test
     public void testOnmouseout() {
         testFireEvent(Event.MOUSEOUT, accordion);
     }
 
     @Test
     public void testOnmouseover() {
         testFireEvent(Event.MOUSEOVER, accordion);
     }
 
     @Test
     public void testOnmouseup() {
         testFireEvent(Event.MOUSEUP, accordion);
     }
 
     @Test
     public void testRendered() {
         selenium.click(pjq("input[type=radio][name$=renderedInput][value=false]"));
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(accordion), "Accordion should not be rendered when rendered=false.");
     }
 
     @Test
     public void testStyle() {
         testStyle(accordion, "style");
     }
 
     @Test
     public void testStyleClass() {
         testStyleClass(accordion, "styleClass");
     }
 
     @Test
     public void testTitle() {
         testTitle(accordion);
     }
 
     @Test
     public void testWidth() {
         AttributeLocator<?> attribute = accordion.getAttribute(new Attribute("style"));
 
         // width = null
         assertFalse(selenium.isAttributePresent(attribute), "Attribute style should not be present.");
 
         // width = 50%
        selenium.type(pjq("input[type=text][id$=widthInput]"), "356px");
         selenium.waitForPageToLoad(TIMEOUT);
 
         assertTrue(selenium.isAttributePresent(attribute), "Attribute style should be present.");
         String value = selenium.getStyle(accordion, CssProperty.WIDTH);
        assertEquals(value, "356px", "Attribute width");
     }
 }
