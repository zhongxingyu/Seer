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
 package org.richfaces.tests.metamer.ftest.richInplaceInput;
 
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardNoRequest;
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.net.URL;
 import javax.faces.event.PhaseId;
 import org.jboss.test.selenium.css.CssProperty;
 
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.locator.Attribute;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.jboss.test.selenium.waiting.EventFiredCondition;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page faces/components/richInplaceInput/simple.xhtml.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestRichInplaceInput extends AbstractMetamerTest {
 
     private JQueryLocator inplaceInput = pjq("span[id$=inplaceInput]");
     private JQueryLocator label = pjq("span.rf-ii-lbl");
     private JQueryLocator input = pjq("input[id$=inplaceInputInput]");
     private JQueryLocator edit = pjq("span.rf-ii-edit");
     private JQueryLocator okButton = pjq("input.rf-ii-btn[id$=Okbtn]");
     private JQueryLocator cancelButton = pjq("input.rf-ii-btn[id$=Cancelbtn]");
     private JQueryLocator output = pjq("span[id$=output]");
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richInplaceInput/simple.xhtml");
     }
 
     @Test
     public void testInit() {
         assertTrue(selenium.isElementPresent(inplaceInput), "Inplace input is not on the page.");
         assertTrue(selenium.isElementPresent(label), "Default label should be present on the page.");
         assertEquals(selenium.getText(label), "RichFaces 4", "Default label");
         assertTrue(selenium.isElementPresent(inplaceInput), "Input should be present on the page.");
         assertFalse(selenium.isElementPresent(okButton), "OK button should not be present on the page.");
         assertFalse(selenium.isElementPresent(cancelButton), "Cancel button should not be present on the page.");
         assertEquals(selenium.getValue(input), "RichFaces 4", "Value of inplace input.");
     }
 
     @Test
     public void testClick() {
         guardNoRequest(selenium).click(inplaceInput);
         assertFalse(selenium.belongsClass(edit, "rf-ii-none"), "Edit should not contain class rf-is-none when popup is open.");
         assertTrue(selenium.isDisplayed(input), "Input should be displayed.");
 
         selenium.type(input, "new value");
         selenium.fireEvent(input, Event.BLUR);
         assertTrue(selenium.belongsClass(inplaceInput, "rf-ii-c-s"), "New class should be added to inplace input.");
         assertTrue(selenium.belongsClass(edit, "rf-ii-none"), "Edit should contain class rf-is-none when popup is closed.");
 
         assertEquals(selenium.getText(label), "new value", "Label should contain selected value.");
         assertEquals(selenium.getText(output), "new value", "Output did not change.");
 
         String listenerText = selenium.getText(jq("div#phasesPanel li:eq(3)"));
         assertEquals(listenerText, "* value changed: RichFaces 4 -> new value", "Value change listener was not invoked.");
     }
 
     @Test
     public void testDefaultLabel() {
         selenium.type(pjq("input[type=text][id$=valueInput]"), "");
         selenium.waitForPageToLoad();
         assertEquals(selenium.getText(label), "Click here to edit", "Default label should change");
 
         selenium.type(pjq("input[type=text][id$=defaultLabelInput]"), "");
         selenium.waitForPageToLoad();
         assertEquals(selenium.getText(label), "", "Default label should change");
 
         assertTrue(selenium.isElementPresent(inplaceInput), "Inplace select is not on the page.");
         assertTrue(selenium.isElementPresent(label), "Default label should be present on the page.");
         assertTrue(selenium.isElementPresent(input), "Input should be present on the page.");
     }
 
     @Test
     public void testEditClass() {
         testStyleClass(edit, "editClass");
     }
 
     @Test
     public void testEditEvent() {
         selenium.type(pjq("input[type=text][id$=editEventInput]"), "mouseup");
         selenium.waitForPageToLoad();
 
         selenium.mouseDown(inplaceInput);
         assertTrue(selenium.belongsClass(edit, "rf-ii-none"), "Inplace input should not be in edit state.");
         selenium.mouseUp(inplaceInput);
         assertFalse(selenium.belongsClass(edit, "rf-ii-none"), "Inplace input should be in edit state.");
     }
 
     @Test
     public void testImmediate() {
         selenium.click(pjq("input[type=radio][name$=immediateInput][value=true]"));
         selenium.waitForPageToLoad();
 
         String timeValue = selenium.getText(time);
         selenium.click(inplaceInput);
         selenium.type(input, "new value");
         selenium.fireEvent(input, Event.BLUR);
         waitGui.failWith("Page was not updated").waitForChange(timeValue, retrieveText.locator(time));
 
         assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.PROCESS_VALIDATIONS,
                 PhaseId.UPDATE_MODEL_VALUES, PhaseId.INVOKE_APPLICATION, PhaseId.RENDER_RESPONSE);
         String listenerText = selenium.getText(jq("div#phasesPanel li:eq(2)"));
         assertEquals(listenerText, "* value changed: RichFaces 4 -> new value", "Value change listener was not invoked.");
     }
 
     @Test
     @IssueTracking("http://java.net/jira/browse/JAVASERVERFACES-1805")
     public void testInputWidth() {
         selenium.type(pjq("input[type=text][id$=inputWidthInput]"), "300px");
         selenium.waitForPageToLoad();
 
         String width = selenium.getStyle(input, CssProperty.WIDTH);
         assertEquals(width, "300px", "Width of input did not change.");
 
         selenium.type(pjq("input[type=text][id$=inputWidthInput]"), "");
         selenium.waitForPageToLoad();
 
         // it cannot handle null because of a bug in Mojarra and Myfaces and
         // generates style="width: ; " instead of default value
         assertTrue(selenium.isAttributePresent(input.getAttribute(Attribute.STYLE)), "Input doesn't have attribute style.");
         width = selenium.getAttribute(input.getAttribute(Attribute.STYLE));
         assertTrue(!width.contains("width: ;"), "Default width of input was not set (was " + width + ").");
     }
 
     @Test
     public void testNoneClass() {
         selenium.type(pjq("input[type=text][id$=valueInput]"), "");
         selenium.waitForPageToLoad();
 
         testStyleClass(edit, "noneClass");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9868")
     public void testOnblur() {
         selenium.type(pjq("input[id$=onblurInput]"), "metamerEvents += \"blur \"");
         selenium.waitForPageToLoad(TIMEOUT);
 
         selenium.click(inplaceInput);
         selenium.fireEvent(input, Event.BLUR);
 
         waitGui.failWith("Attribute onblur does not work correctly").until(new EventFiredCondition(Event.BLUR));
     }
 
     @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-10044")
     public void testOnchange() {
         selenium.type(pjq("input[type=text][id$=onchangeInput]"), "metamerEvents += \"change \"");
         selenium.waitForPageToLoad();
 
         String timeValue = selenium.getText(time);
         selenium.click(inplaceInput);
         selenium.type(input, "new value");
         selenium.fireEvent(input, Event.BLUR);
         waitFor(5000);
         waitGui.failWith("Page was not updated").waitForChange(timeValue, retrieveText.locator(time));
 
         waitGui.failWith("Attribute onchange does not work correctly").until(
                 new EventFiredCondition(new Event("change")));
     }
 
     @Test
     public void testOnclick() {
         testFireEvent(Event.CLICK, inplaceInput);
     }
 
     @Test
     public void testOndblclick() {
         testFireEvent(Event.DBLCLICK, inplaceInput);
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9868")
     public void testOnfocus() {
         selenium.type(pjq("input[id$=onfocusInput]"), "metamerEvents += \"focus \"");
         selenium.waitForPageToLoad(TIMEOUT);
 
         selenium.click(inplaceInput);
 
         waitGui.failWith("Attribute onfocus does not work correctly").until(new EventFiredCondition(Event.FOCUS));
     }
 
     @Test
     public void testOninputclick() {
         testFireEvent(Event.CLICK, input, "inputclick");
     }
 
     @Test
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
     public void testOninputselect() {
         testFireEvent(Event.SELECT, input, "inputselect");
     }
 
     @Test
     public void testOnkeydown() {
         testFireEvent(Event.KEYDOWN, inplaceInput);
     }
 
     @Test
     public void testOnkeypress() {
         testFireEvent(Event.KEYPRESS, inplaceInput);
     }
 
     @Test
     public void testOnkeyup() {
         testFireEvent(Event.KEYUP, inplaceInput);
     }
 
     @Test
     public void testOnmousedown() {
         testFireEvent(Event.MOUSEDOWN, inplaceInput);
     }
 
     @Test
     public void testOnmousemove() {
         testFireEvent(Event.MOUSEMOVE, inplaceInput);
     }
 
     @Test
     public void testOnmouseout() {
         testFireEvent(Event.MOUSEOUT, inplaceInput);
     }
 
     @Test
     public void testOnmouseover() {
         testFireEvent(Event.MOUSEOVER, inplaceInput);
     }
 
     @Test
     public void testOnmouseup() {
         testFireEvent(Event.MOUSEUP, inplaceInput);
     }
 
     @Test
     public void testRendered() {
         selenium.click(pjq("input[type=radio][name$=renderedInput][value=false]"));
         selenium.waitForPageToLoad();
 
         assertFalse(selenium.isElementPresent(inplaceInput), "Component should not be rendered when rendered=false.");
     }
 
     @Test
     public void testShowControls() {
         selenium.click(pjq("input[type=radio][name$=showControlsInput][value=true]"));
         selenium.waitForPageToLoad();
 
         selenium.click(inplaceInput);
         assertTrue(selenium.isVisible(okButton), "OK button should be visible.");
         assertTrue(selenium.isVisible(cancelButton), "Cancel button should be visible.");
     }
 
     @Test
     public void testClickOkButton() {
         selenium.click(pjq("input[type=radio][name$=showControlsInput][value=true]"));
         selenium.waitForPageToLoad();
 
         String timeValue = selenium.getText(time);
         selenium.click(inplaceInput);
         guardNoRequest(selenium).keyPress(input, "x");
         guardXhr(selenium).mouseDown(okButton);
         waitGui.failWith("Page was not updated").waitForChange(timeValue, retrieveText.locator(time));
 
         assertEquals(selenium.getText(label), "RichFaces 4x", "Label");
         assertEquals(selenium.getValue(input), "RichFaces 4x", "Value of inplace input");
         assertEquals(selenium.getText(output), "RichFaces 4x", "Output");
     }
 
     @Test
     @IssueTracking("https://issues.jboss.org/browse/RF-9872")
     public void testClickCancelButton() {
         selenium.click(pjq("input[type=radio][name$=showControlsInput][value=true]"));
         selenium.waitForPageToLoad();
 
         selenium.click(inplaceInput);
         guardNoRequest(selenium).keyPress(input, "x");
         guardNoRequest(selenium).mouseDown(cancelButton);
 
         assertEquals(selenium.getText(label), "RichFaces 4", "Label");
         assertEquals(selenium.getValue(input), "RichFaces 4", "Value of inplace input.");
         assertEquals(selenium.getText(output), "RichFaces 4", "Output did not change.");
     }
 
     @Test
     public void testValue() {
         selenium.type(pjq("input[type=text][id$=valueInput]"), "new value");
         selenium.waitForPageToLoad();
 
         assertEquals(selenium.getText(label), "new value", "Default label");
         assertEquals(selenium.getValue(input), "new value", "Value of inplace input.");
     }
 }
