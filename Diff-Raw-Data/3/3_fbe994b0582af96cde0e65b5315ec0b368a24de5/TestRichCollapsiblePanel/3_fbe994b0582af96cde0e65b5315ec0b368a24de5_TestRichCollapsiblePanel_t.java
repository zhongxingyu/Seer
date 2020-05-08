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
 package org.richfaces.tests.metamer.ftest.richCollapsiblePanel;
 
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
 
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page /faces/components/richCollapsiblePanel/simple.xhtml
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestRichCollapsiblePanel extends AbstractMetamerTest {
 
     private JQueryLocator panel = pjq("div[id$=collapsiblePanel]");
     private JQueryLocator header = pjq("div[id$=collapsiblePanel:header]");
     private JQueryLocator headerExp = pjq("div[id$=collapsiblePanel:header] div.rf-cp-hdr-exp");
     private JQueryLocator headerColps = pjq("div[id$=collapsiblePanel:header] div.rf-cp-hdr-colps");
     private JQueryLocator content = pjq("div[id$=collapsiblePanel:content]");
     private JQueryLocator time = jq("span[id$=requestTime]");
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richCollapsiblePanel/simple.xhtml");
     }
 
     @Test
     public void testInit() {
         boolean displayed = selenium.isDisplayed(panel);
         assertTrue(displayed, "Collapsible panel is not present on the page.");
 
         verifyBeforeClick();
     }
 
     @Test
     public void testSwitchTypeNull() {
         // click to collapse
         String timeValue = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(timeValue, retrieveText.locator(time));
 
         verifyAfterClick();
 
         // click to expand
         timeValue = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(timeValue, retrieveText.locator(time));
 
         verifyBeforeClick();
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
 
         // click to collapse
         guardNoRequest(selenium).click(header);
         verifyAfterClick();
 
         // click to expand
         guardNoRequest(selenium).click(header);
         verifyBeforeClick();
     }
 
     @Test
     public void testSwitchTypeServer() {
         JQueryLocator selectOption = pjq("input[name$=switchTypeInput][value=server]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         // click to collapse
         guardHttp(selenium).click(header);
         verifyAfterClick();
 
         // click to expand
         guardHttp(selenium).click(header);
         verifyBeforeClick();
     }
 
     @Test
    @IssueTracking("https://jira.jboss.org/browse/RF-9421")
     public void testBypassUpdates() {
         JQueryLocator input = pjq("input[type=radio][name$=bypassUpdatesInput][value=true]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         String timeValue = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(timeValue, retrieveText.locator(time));
 
         assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.PROCESS_VALIDATIONS,
                 PhaseId.RENDER_RESPONSE);
     }
 
     @Test
     public void testHeader() {
         selenium.type(pjq("input[type=text][id$=headerInput]"), "new header");
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(header), "new header", "Header of the panel did not change.");
 
         selenium.type(pjq("input[type=text][id$=headerInput]"), "ľščťťžžôúňď ацущьмщфзщйцу");
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(header), "ľščťťžžôúňď ацущьмщфзщйцу", "Header of the panel did not change.");
     }
 
     @Test
    @IssueTracking("https://jira.jboss.org/browse/RF-9421")
     public void testImmediate() {
         JQueryLocator input = pjq("input[type=radio][name$=immediateInput][value=true]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         String timeValue = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(timeValue, retrieveText.locator(time));
 
         assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.RENDER_RESPONSE);
     }
 
     @Test
     @IssueTracking("https://jira.jboss.org/browse/RF-9535")
     public void testLimitToList() {
         JQueryLocator timeLoc = jq("span[id$=requestTime]");
 
         selenium.type(pjq("input[type=text][id$=renderInput]"), "@this");
         selenium.waitForPageToLoad();
 
         selenium.click(pjq("input[type=radio][name$=limitToListInput][value=true]"));
         selenium.waitForPageToLoad();
 
         String timeValue = selenium.getText(timeLoc);
 
         guardXhr(selenium).click(header);
         waitGui.failWith("Panel should be collapsed.").until(isDisplayed.locator(headerColps));
 
         String newTime = selenium.getText(timeLoc);
         assertNotSame(newTime, timeValue, "Panel with ajaxRendered=true should not be rerendered.");
     }
 
     @Test
     public void testRendered() {
         JQueryLocator input = pjq("input[type=radio][name$=renderedInput][value=false]");
         selenium.click(input);
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(panel), "Panel should not be rendered when rendered=false.");
     }
 
     private void verifyBeforeClick() {
         boolean displayed = selenium.isDisplayed(panel);
         assertTrue(displayed, "Collapsible panel is not present on the page.");
 
         displayed = selenium.isDisplayed(headerExp);
         assertTrue(displayed, "Expanded header should be visible.");
 
         displayed = selenium.isDisplayed(headerColps);
         assertFalse(displayed, "Collapsed header should not be visible.");
 
         displayed = selenium.isDisplayed(content);
         assertTrue(displayed, "Panel's content should be visible.");
 
         String text = selenium.getText(header);
         assertEquals(text, "collapsible panel header", "Header of the panel.");
 
         text = selenium.getText(content);
         assertTrue(text.startsWith("Lorem ipsum"), "Panel doesn't contain Lorem ipsum in its content.");
     }
 
     private void verifyAfterClick() {
         boolean displayed = selenium.isDisplayed(panel);
         assertTrue(displayed, "Collapsible panel is not present on the page.");
 
         displayed = selenium.isDisplayed(headerExp);
         assertFalse(displayed, "Expanded header should not be visible.");
 
         displayed = selenium.isDisplayed(headerColps);
         assertTrue(displayed, "Collapsed header should be visible.");
 
         if (selenium.isElementPresent(content)) {
             displayed = selenium.isDisplayed(content);
             assertFalse(displayed, "Panel's content should not be visible.");
         }
 
         String text = selenium.getText(header);
         assertEquals(text, "collapsible panel header", "Header of the panel.");
     }
 }
