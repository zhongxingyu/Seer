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
 package org.richfaces.tests.metamer.ftest.richCollapsiblePanel;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardHttp;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardNoRequest;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.locator.option.OptionLocatorFactory.optionLabel;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import java.net.URL;
 
 import javax.faces.event.PhaseId;
 
 import org.jboss.test.selenium.css.CssProperty;
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.locator.Attribute;
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
     private JQueryLocator headerExp = pjq("div[id$=collapsiblePanel:header] div.rf-cp-lbl-exp");
     private JQueryLocator headerColps = pjq("div[id$=collapsiblePanel:header] div.rf-cp-lbl-colps");
     private JQueryLocator content = pjq("div[id$=collapsiblePanel:content]");
     private JQueryLocator leftIcon = pjq("td.rf-cp-ico");
     private JQueryLocator rightIcon = pjq("td.rf-cp-exp-ico");
 
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
     public void testBodyClass() {
         testStyleClass(content, "bodyClass");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10054")
     public void testBypassUpdates() {
         selenium.click(pjq("input[type=radio][name$=bypassUpdatesInput][value=true]"));
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         phaseInfo.assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.PROCESS_VALIDATIONS,
                 PhaseId.RENDER_RESPONSE);
         phaseInfo.assertListener(PhaseId.APPLY_REQUEST_VALUES, "panel collapsed");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10061")
     public void testData() {
         selenium.type(pjq("input[type=text][id$=dataInput]"), "RichFaces 4");
         selenium.waitForPageToLoad();
 
         selenium.type(pjq("input[type=text][id$=oncompleteInput]"), "data = event.data");
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         String data = selenium.getEval(new JavaScript("window.data"));
         assertEquals(data, "RichFaces 4", "Data sent with ajax request");
     }
 
     @Test
     public void testDir() {
         testDir(panel);
     }
 
     @Test
     public void testExecute() {
         selenium.type(pjq("input[type=text][id$=executeInput]"), "input executeChecker");
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         JQueryLocator logItems = jq("ul.phases-list li:eq({0})");
         for (int i = 0; i < 6; i++) {
             if ("* executeChecker".equals(selenium.getText(logItems.format(i)))) {
                 return;
             }
         }
 
         fail("Attribute execute does not work");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10312")
     public void testExpanded() {
         verifyBeforeClick();
 
         selenium.click(pjq("input[type=radio][name$=expandedInput][value=false]"));
         selenium.waitForPageToLoad();
 
         verifyAfterClick();
     }
 
     @Test
     public void testHeader() {
         selenium.type(pjq("input[type=text][id$=headerInput]"), "new header");
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(headerExp), "new header", "Header of the panel did not change.");
 
         selenium.type(pjq("input[type=text][id$=headerInput]"), "ľščťťžžôúňď ацущьмщфзщйцу");
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(headerExp), "ľščťťžžôúňď ацущьмщфзщйцу", "Header of the panel did not change.");
     }
 
     @Test
     public void testHeaderClass() {
         testStyleClass(header, "headerClass");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-10054")
     public void testImmediate() {
         selenium.click(pjq("input[type=radio][name$=immediateInput][value=true]"));
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         phaseInfo.assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.RENDER_RESPONSE);
         phaseInfo.assertListener(PhaseId.APPLY_REQUEST_VALUES, "panel collapsed");
     }
 
     @Test
     public void testLang() {
         testLang(panel);
     }
 
     @Test
     public void testLeftCollapsedIcon() {
         JQueryLocator icon = leftIcon.getDescendant(jq("div.rf-cp-ico-colps"));
         JQueryLocator input = pjq("select[id$=leftCollapsedIconInput]");
         JQueryLocator image = leftIcon.getChild(jq("img:nth-child(1)"));
 
         // icon=null
         assertTrue(selenium.belongsClass(icon, "rf-ico-chevron-up-hdr"), "Div should have set class rf-ico-chevron-up-hdr.");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("ChevronUp.png"), "Icon should contain a chevron up.");
 
         verifyStandardIcons(input, icon, image);
     }
 
     @Test
     public void testLeftExpandedIcon() {
         JQueryLocator icon = leftIcon.getDescendant(jq("div.rf-cp-ico-exp"));
         JQueryLocator input = pjq("select[id$=leftExpandedIconInput]");
         JQueryLocator image = leftIcon.getChild(jq("img:nth-child(2)"));
 
         // icon=null
         assertTrue(selenium.belongsClass(icon, "rf-ico-chevron-down-hdr"), "Div should have set class rf-ico-chevron-down-hdr.");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("ChevronDown.png"), "Icon should contain a chevron down.");
 
         verifyStandardIcons(input, icon, image);
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9535")
     public void testLimitRender() {
         selenium.type(pjq("input[type=text][id$=renderInput]"), "@this");
         selenium.waitForPageToLoad();
 
         selenium.click(pjq("input[type=radio][name$=limitRenderInput][value=true]"));
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
 
         guardXhr(selenium).click(header);
         waitGui.failWith("Panel should be collapsed.").until(isDisplayed.locator(headerColps));
 
         String newTime = selenium.getText(time);
         assertTrue(newTime.equals(reqTime), "Panel with ajaxRendered=true should not be rerendered (old and new time should be same).");
     }
 
     @Test
     public void testAjaxEvents() {
         selenium.type(pjq("input[type=text][id$=onbeginInput]"), "metamerEvents += \"begin \"");
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[type=text][id$=onbeforedomupdateInput]"), "metamerEvents += \"beforedomupdate \"");
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[type=text][id$=oncompleteInput]"), "metamerEvents += \"complete \"");
         selenium.waitForPageToLoad();
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events.length, 3, "3 events should be fired.");
         assertEquals(events[0], "begin", "Attribute onbegin doesn't work");
         assertEquals(events[1], "beforedomupdate", "Attribute onbeforedomupdate doesn't work");
         assertEquals(events[2], "complete", "Attribute oncomplete doesn't work");
     }
 
     @Test
     public void testOnbeforeswitchOnswitch() {
         selenium.type(pjq("input[type=text][id$=onbeforeswitchInput]"), "metamerEvents += \"beforeswitch \"");
         selenium.waitForPageToLoad();
         selenium.type(pjq("input[type=text][id$=onswitchInput]"), "metamerEvents += \"switch \"");
         selenium.waitForPageToLoad();
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).click(header);
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events.length, 2, "2 events should be fired.");
         assertEquals(events[0], "beforeswitch", "Attribute onbeforeswitch doesn't work");
         assertEquals(events[1], "switch", "Attribute onswitch doesn't work");
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
     public void testRender() {
         selenium.type(pjq("input[type=text][id$=renderInput]"), "renderChecker");
         selenium.waitForPageToLoad();
 
         String renderCheckerTime = selenium.getText(renderChecker);
         guardXhr(selenium).click(header);
         waitGui.failWith("Attribute render doesn't work").waitForChange(renderCheckerTime, retrieveText.locator(renderChecker));
     }
 
     @Test
     public void testRendered() {
         selenium.click(pjq("input[type=radio][name$=renderedInput][value=false]"));
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(panel), "Panel should not be rendered when rendered=false.");
     }
 
     @Test
     public void testRightCollapsedIcon() {
         JQueryLocator icon = rightIcon.getDescendant(jq("div.rf-cp-ico-colps"));
         JQueryLocator input = pjq("select[id$=rightCollapsedIconInput]");
         JQueryLocator image = rightIcon.getChild(jq("img:nth-child(1)"));
 
         // icon=null
         assertFalse(selenium.isElementPresent(rightIcon), "Right icon should not be present on the page.");
 
         verifyStandardIcons(input, icon, image);
     }
 
     @Test
     public void testRightExpandedIcon() {
         JQueryLocator icon = rightIcon.getDescendant(jq("div.rf-cp-ico-exp"));
         JQueryLocator input = pjq("select[id$=rightExpandedIconInput]");
         JQueryLocator image = rightIcon.getChild(jq("img:nth-child(1)"));
 
         // icon=null
         assertFalse(selenium.isElementPresent(rightIcon), "Right icon should not be present on the page.");
 
         verifyStandardIcons(input, icon, image);
     }
 
     @Test
     public void testStatus() {
         selenium.type(pjq("input[type=text][id$=statusInput]"), "statusChecker");
         selenium.waitForPageToLoad();
 
         String statusCheckerTime = selenium.getText(statusChecker);
         guardXhr(selenium).click(header);
         waitGui.failWith("Attribute status doesn't work").waitForChange(statusCheckerTime, retrieveText.locator(statusChecker));
     }
 
     @Test
     public void testStyle() {
         testStyle(panel, "style");
     }
 
     @Test
     public void testStyleClass() {
         testStyleClass(panel, "styleClass");
     }
 
     @Test
     public void testSwitchTypeNull() {
         // click to collapse
         String reqTime = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         verifyAfterClick();
 
         // click to expand
         reqTime = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         verifyBeforeClick();
     }
 
     @Test
     public void testSwitchTypeAjax() {
         selenium.click(pjq("input[name$=switchTypeInput][value=ajax]"));
         selenium.waitForPageToLoad();
 
         testSwitchTypeNull();
     }
 
     @Test
     public void testSwitchTypeClient() {
         selenium.click(pjq("input[name$=switchTypeInput][value=client]"));
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
         selenium.click(pjq("input[name$=switchTypeInput][value=server]"));
         selenium.waitForPageToLoad();
 
         // click to collapse
         guardHttp(selenium).click(header);
         verifyAfterClick();
 
         // click to expand
         guardHttp(selenium).click(header);
         verifyBeforeClick();
     }
 
     @Test
     public void testTitle() {
         testTitle(panel);
     }
 
     @Test
     public void testToggleListener() {
         String reqTime = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         phaseInfo.assertListener(PhaseId.INVOKE_APPLICATION, "panel collapsed");
 
         reqTime = selenium.getText(time);
         guardXhr(selenium).click(header);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         phaseInfo.assertListener(PhaseId.INVOKE_APPLICATION, "panel expanded");
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
 
         String text = selenium.getText(headerExp);
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
 
         String text = selenium.getText(headerColps);
         assertEquals(text, "collapsible panel header", "Header of the panel.");
     }
 
     private void verifyStandardIcons(JQueryLocator input, JQueryLocator icon, JQueryLocator image) {
         selenium.select(input, optionLabel("chevronDown"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-chevron-down-hdr"), "Div should have set class rf-ico-chevron-down-hdr.");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("ChevronDown.png"), "Icon should contain a chevron down.");
 
         selenium.select(input, optionLabel("chevronUp"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-chevron-up-hdr"), "Div should have set class rf-ico-chevron-up-hdr.");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("ChevronUp.png"), "Icon should contain a chevron up.");
 
         selenium.select(input, optionLabel("disc"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-disc-hdr"), "Div should have set class rf-ico-disc-hdr.");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("Disc.png"), "Icon should contain a disc.");
 
         selenium.select(input, optionLabel("grid"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-grid-hdr"), "Div should have set class rf-ico-grid-hdr.");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("Grid.png"), "Icon should contain a grid.");
 
         selenium.select(input, optionLabel("triangle"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-triangle-hdr"), "Div should have set class rf-ico-triangle-hdr.");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("Triangle.png"), "Icon should contain a triangle.");
 
         selenium.select(input, optionLabel("triangleDown"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-triangle-down-hdr"), "Div should have set class rf-ico-triangle-down-hdr.");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("TriangleDown.png"), "Icon should contain a triangle down.");
 
         selenium.select(input, optionLabel("triangleUp"));
         selenium.waitForPageToLoad();
         assertTrue(selenium.belongsClass(icon, "rf-ico-triangle-up-hdr"), "Div should have set class rf-ico-triangle-up-hdr.");
         assertTrue(selenium.getStyle(icon, CssProperty.BACKGROUND_IMAGE).contains("TriangleUp.png"), "Icon should contain a triangle up.");
 
         selenium.select(input, optionLabel("none"));
         selenium.waitForPageToLoad();
         assertFalse(selenium.isElementPresent(icon), "Icon should not be present when icon=none.");
 
         selenium.select(input, optionLabel("star"));
         selenium.waitForPageToLoad();
         assertFalse(selenium.isElementPresent(icon), "Icon's div should not be present when icon=star.");
         assertTrue(selenium.isElementPresent(image), "Icon's image should be rendered.");
         assertTrue(selenium.getAttribute(image.getAttribute(Attribute.SRC)).contains("star.png"), "Icon's src attribute should contain star.png.");
 
         selenium.select(input, optionLabel("nonexisting"));
         selenium.waitForPageToLoad();
         assertFalse(selenium.isElementPresent(icon), "Icon's div should not be present when icon=nonexisting.");
         assertTrue(selenium.isElementPresent(image), "Icon's image should be rendered.");
         assertTrue(selenium.getAttribute(image.getAttribute(Attribute.SRC)).contains("nonexisting"), "Icon's src attribute should contain nonexisting.");
     }
 }
