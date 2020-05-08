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
 package org.richfaces.tests.metamer.ftest.richInputNumberSpinner;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardNoRequest;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.net.URL;
 
 import javax.faces.event.PhaseId;
 
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.jboss.test.selenium.locator.Attribute;
 import org.jboss.test.selenium.locator.AttributeLocator;
 import org.jboss.test.selenium.locator.ElementLocator;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.annotations.Inject;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.richfaces.tests.metamer.ftest.annotations.Use;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page /faces/components/richInputNumberSpinner/simple.xhtml
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestRichSpinner extends AbstractMetamerTest {
 
     private JQueryLocator spinner = pjq("span[id$=spinner]");
     private JQueryLocator input = pjq("span[id$=spinner] input.rf-insp-inp");
     private JQueryLocator up = pjq("span[id$=spinner] span.rf-insp-inc");
     private JQueryLocator down = pjq("span[id$=spinner] span.rf-insp-dec");
     private JQueryLocator output = pjq("span[id$=output]");
     private JQueryLocator time = jq("span[id$=requestTime]");
     private String[] correctNumbers = {"-10", "-5", "-1", "0", "1", "5", "10"};
     private String[] smallNumbers = {"-11", "-15", "-100"};
     private String[] bigNumbers = {"11", "15", "100"};
     private String[] decimalNumbers = {"1.4999", "5.6", "7.0001", "-5.50001", "-9.9", "1.222e0", "0e0", "-5.50001e0"};
     @Inject
     @Use(empty = true)
     private String number;
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richInputNumberSpinner/simple.xhtml");
     }
 
     @Test
     public void testInit() {
         assertTrue(selenium.isDisplayed(spinner), "Spinner is not present on the page.");
         assertTrue(selenium.isDisplayed(input), "Spinner's input is not present on the page.");
         assertTrue(selenium.isDisplayed(up), "Spinner's up button is not present on the page.");
         assertTrue(selenium.isDisplayed(down), "Spinner's down button is not present on the page.");
 
     }
 
     @Test
     @Use(field = "number", value = "correctNumbers")
     public void testTypeIntoInputCorrect() {
         String reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, number);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         assertEquals(selenium.getText(output), number, "Output was not updated.");
     }
 
     @Test
     @Use(field = "number", value = "smallNumbers")
     public void testTypeIntoInputSmall() {
         String reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, number);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         assertEquals(selenium.getText(output), "-10", "Output was not updated.");
         assertEquals(getInputValue(), "-10", "Input was not updated.");
     }
 
     @Test
     @Use(field = "number", value = "bigNumbers")
     public void testTypeIntoInputBig() {
         String reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, number);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         assertEquals(selenium.getText(output), "10", "Output was not updated.");
         assertEquals(getInputValue(), "10", "Input was not updated.");
     }
 
     @Test
     @Use(field = "number", value = "decimalNumbers")
     public void testTypeIntoInputDecimal() {
         String reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, number);
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         int roundedNumber = (int) Math.round(Double.parseDouble(number));
 
         assertEquals(selenium.getText(output), Integer.toString(roundedNumber), "Output was not updated.");
         assertEquals(getInputValue(), Integer.toString(roundedNumber), "Input was not updated.");
     }
 
     @Test
     public void testTypeIntoInputNotNumber() {
         guardNoRequest(selenium).type(input, "aaa");
         assertEquals(selenium.getText(output), "2", "Output should not be updated.");
         assertEquals(getInputValue(), "2", "Input should not be updated.");
     }
 
     @Test
     public void testClickUp() {
         clickArrow(up, 4);
         assertEquals(selenium.getText(output), "6", "Output was not updated.");
 
         clickArrow(up, 4);
         assertEquals(selenium.getText(output), "10", "Output was not updated.");
 
         selenium.mouseDown(up);
         guardNoRequest(selenium).mouseUp(up);
 
         assertEquals(selenium.getText(output), "10", "Output was not updated.");
     }
 
     @Test
     public void testClickDown() {
         clickArrow(down, 4);
         assertEquals(selenium.getText(output), "-2", "Output was not updated.");
 
         clickArrow(down, 8);
         assertEquals(selenium.getText(output), "-10", "Output was not updated.");
 
         selenium.mouseDown(down);
         guardNoRequest(selenium).mouseUp(down);
 
         assertEquals(selenium.getText(output), "-10", "Output was not updated.");
     }
 
     @Test
     public void testCycled() {
         JQueryLocator selectOption = pjq("input[type=radio][name$=cycledInput][value=true]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, "10");
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         // test that value change to min value (10 -> -10)
         clickArrow(up, 1);
         assertEquals(selenium.getText(output), "-10", "Output was not updated.");
 
         // test that value change to max value (-10 -> 10)
         clickArrow(down, 1);
         assertEquals(selenium.getText(output), "10", "Output was not updated.");
     }
 
     @Test
     public void testDisabled() {
         JQueryLocator selectOption = pjq("input[type=radio][name$=disabledInput][value=true]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         AttributeLocator disabledAttribute = input.getAttribute(new Attribute("disabled"));
         assertEquals(selenium.getAttribute(disabledAttribute), "disabled", "Input should be disabled.");
 
         assertFalse(selenium.isElementPresent(up), "Arrow up should be disabled.");
         assertFalse(selenium.isElementPresent(down), "Arrow down should be disabled.");
 
         JQueryLocator upDisabled = pjq("span[id$=spinner] span.rf-insp-inc-dis");
         JQueryLocator downDisabled = pjq("span[id$=spinner] span.rf-insp-dec-dis");
 
         assertTrue(selenium.isElementPresent(upDisabled), "An disabled up arrow should be displayed.");
         assertTrue(selenium.isElementPresent(downDisabled), "An disabled downarrow should be displayed.");
     }
 
     @Test
     public void testEnableManualInput() {
         JQueryLocator selectOption = pjq("input[type=radio][name$=enableManualInputInput][value=false]");
         selenium.click(selectOption);
         selenium.waitForPageToLoad();
 
         AttributeLocator readonlyAttribute = input.getAttribute(new Attribute("readonly"));
         assertEquals(selenium.getAttribute(readonlyAttribute), "readonly", "Input should be read-only.");
 
         assertTrue(selenium.isElementPresent(up), "Arrow up should be displayed.");
         assertTrue(selenium.isElementPresent(down), "Arrow down should be displayed.");
     }
 
     @Test
     public void testImmediate() {
         JQueryLocator immediateInput = pjq("input[type=radio][name$=immediateInput][value=true]");
         selenium.click(immediateInput);
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, "4");
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "4", "Output was not updated.");
 
         assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.PROCESS_VALIDATIONS, PhaseId.UPDATE_MODEL_VALUES,
                 PhaseId.INVOKE_APPLICATION, PhaseId.RENDER_RESPONSE);
     }
 
     @Test
     public void testItemContentClass() {
         testStyleClass(input, "inputClass");
     }
 
     @Test
     public void testInputSize() {
         JQueryLocator selectOption = pjq("input[type=text][id$=inputSizeInput]");
 
         selenium.type(selectOption, "3");
         selenium.waitForPageToLoad();
         AttributeLocator sizeAttribute = input.getAttribute(new Attribute("size"));
         assertEquals(selenium.getAttribute(sizeAttribute), "3", "Input's size attribute.");
 
         selenium.type(selectOption, "40");
         selenium.waitForPageToLoad();
         assertEquals(selenium.getAttribute(sizeAttribute), "40", "Input's size attribute.");
     }
 
     @Test
     public void testMaxValueType() {
         JQueryLocator selectOption = pjq("input[type=text][id$=maxValueInput]");
         selenium.type(selectOption, "13");
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, "11");
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "11", "Output was not updated.");
 
         reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, "13");
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "13", "Output was not updated.");
 
     }
 
     @Test
     public void testMaxValueClick() {
         JQueryLocator selectOption = pjq("input[type=text][id$=maxValueInput]");
         selenium.type(selectOption, "13");
         selenium.waitForPageToLoad();
 
         clickArrow(up, 9);
         assertEquals(selenium.getText(output), "11", "Output was not updated.");
 
         clickArrow(up, 2);
         assertEquals(selenium.getText(output), "13", "Output was not updated.");
     }
 
     @Test
     public void testMinValueType() {
         JQueryLocator selectOption = pjq("input[type=text][id$=minValueInput]");
         selenium.type(selectOption, "-13");
         selenium.waitForPageToLoad();
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, "-11");
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "-11", "Output was not updated.");
 
         reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, "-13");
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         assertEquals(selenium.getText(output), "-13", "Output was not updated.");
 
     }
 
     @Test
     public void testMinValueClick() {
         JQueryLocator selectOption = pjq("input[type=text][id$=minValueInput]");
         selenium.type(selectOption, "-13");
         selenium.waitForPageToLoad();
 
         clickArrow(down, 13);
         assertEquals(selenium.getText(output), "-11", "Output was not updated.");
 
         clickArrow(down, 2);
         assertEquals(selenium.getText(output), "-13", "Output was not updated.");
     }
 
     @Test
     public void testOnblur() {
         testFireEvent(Event.BLUR, input);
     }
 
     @Test
     public void testOnchangeType() {
         ElementLocator<?> eventInput = pjq("input[id$=onchangeInput]");
         String value = "metamerEvents += \"change \"";
         selenium.type(eventInput, value);
         selenium.waitForPageToLoad(TIMEOUT);
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
 
         String reqTime = selenium.getText(time);
         guardXhr(selenium).type(input, "4");
         waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "change", "Attribute onchange doesn't work");
         assertEquals(events.length, 1, "Only one event should be fired");
     }
 
     @Test
     public void testOnchangeClick() {
         ElementLocator<?> eventInput = pjq("input[id$=onchangeInput]");
         String value = "metamerEvents += \"change \"";
         selenium.type(eventInput, value);
         selenium.waitForPageToLoad(TIMEOUT);
 
         selenium.getEval(new JavaScript("window.metamerEvents = \"\";"));
 
         clickArrow(up, 1);
 
         String[] events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "change", "Attribute onchange doesn't work");
         assertEquals(events.length, 1, "Only one event should be fired");
 
         clickArrow(down, 1);
 
         events = selenium.getEval(new JavaScript("window.metamerEvents")).split(" ");
 
         assertEquals(events[0], "change", "Attribute onchange doesn't work.");
         assertEquals(events[1], "change", "Attribute onchange doesn't work.");
         assertEquals(events.length, 2, "Two events should be fired after two clicks on arrows.");
     }
 
     @Test
     public void testOnclick() {
         testFireEvent(Event.CLICK, spinner);
     }
 
     @Test
     public void testOndblclick() {
         testFireEvent(Event.DBLCLICK, spinner);
     }
 
     @Test
     public void testOndownclick() {
         testFireEvent(Event.CLICK, down, "downclick");
     }
 
     @Test
     public void testOnfocus() {
         testFireEvent(Event.FOCUS, input);
     }
 
     @Test
     public void testOninputclick() {
         testFireEvent(Event.CLICK, input, "inputclick");
     }
 
     @Test
     @IssueTracking("https://jira.jboss.org/browse/RF-9568")
     public void testOninputdblclick() {
         testFireEvent(Event.DBLCLICK, input, "inputdblclick");
     }
 
     @Test
     public void testOninputkeydown() {
         testFireEvent(Event.KEYDOWN, input, "inputkeydown");
     }
 
     @Test
     public void testOninputkeypress() {
         testFireEvent(Event.KEYPRESS, input, "inputkeypress");
     }
 
     @Test
     public void testOninputkeyup() {
         testFireEvent(Event.KEYUP, input, "inputkeyup");
     }
 
     @Test
     public void testOninputmousedown() {
         testFireEvent(Event.MOUSEDOWN, input, "inputmousedown");
     }
 
     @Test
     public void testOninputmousemove() {
         testFireEvent(Event.MOUSEMOVE, input, "inputmousemove");
     }
 
     @Test
     public void testOninputmouseout() {
         testFireEvent(Event.MOUSEOUT, input, "inputmouseout");
     }
 
     @Test
     public void testOninputmouseover() {
         testFireEvent(Event.MOUSEOVER, input, "inputmouseover");
     }
 
     @Test
     public void testOninputmouseup() {
         testFireEvent(Event.MOUSEUP, input, "inputmouseup");
     }
 
     @Test
     public void testOnmousedown() {
         testFireEvent(Event.MOUSEDOWN, spinner);
     }
 
     @Test
     public void testOnmousemove() {
         testFireEvent(Event.MOUSEMOVE, spinner);
     }
 
     @Test
     public void testOnmouseout() {
         testFireEvent(Event.MOUSEOUT, spinner);
     }
 
     @Test
     public void testOnmouseover() {
         testFireEvent(Event.MOUSEOVER, spinner);
     }
 
     @Test
     public void testOnmouseup() {
         testFireEvent(Event.MOUSEUP, spinner);
     }
 
     @Test
     public void testOnselect() {
         testFireEvent(Event.SELECT, input);
     }
 
     @Test
     public void testOnupclick() {
         testFireEvent(Event.CLICK, up, "upclick");
     }
 
     @Test
     public void testRendered() {
         JQueryLocator renderedInput = pjq("input[type=radio][name$=renderedInput][value=false]");
         selenium.click(renderedInput);
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(spinner), "Spinner should not be rendered when rendered=false.");
     }
 
     @Test
     public void testStyle() {
         testStyle(spinner);
     }
 
     @Test
     public void testStyleClass() {
         testStyleClass(spinner, "styleClass");
     }
 
     @Test
     @Use(field = "number", value = "correctNumbers")
     public void testValueCorrect() {
         selenium.type(pjq("input[id$=valueInput]"), number);
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(output), number, "Output was not updated.");
     }
 
     @Test
     @Use(field = "number", value = "smallNumbers")
     public void testValueSmall() {
         selenium.type(pjq("input[id$=valueInput]"), number);
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(output), number, "Output was not updated.");
         assertEquals(getInputValue(), "-10", "Input was not updated.");
     }
 
     @Test
     @Use(field = "number", value = "bigNumbers")
     public void testValueBig() {
         selenium.type(pjq("input[id$=valueInput]"), number);
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(output), number, "Output was not updated.");
         assertEquals(getInputValue(), "10", "Input was not updated.");
     }
 
     /**
      * Getter for value that is displayed in spinner input.
      * @return spinner input value
      */
     private String getInputValue() {
        String id = selenium.getEval(new JavaScript("window.testedComponentId"));
        return selenium.getEval(new JavaScript("window.RichFaces.$('" + id + "').value"));
     }
 
     /**
      * Clicks on spinner's arrow.
      * @param arrow spinner's up or down arrow locator
      * @param clicks how many times should it be clicked
      */
     private void clickArrow(ElementLocator<?> arrow, int clicks) {
         String reqTime = null;
 
         for (int i = 0; i < clicks; i++) {
             reqTime = selenium.getText(time);
             guardXhr(selenium).runScript(new JavaScript("jQuery(\"" + arrow.getRawLocator() + "\").mousedown().mouseup()"));
 
             waitGui.failWith("Page was not updated").waitForChange(reqTime, retrieveText.locator(time));
         }
     }
 }
