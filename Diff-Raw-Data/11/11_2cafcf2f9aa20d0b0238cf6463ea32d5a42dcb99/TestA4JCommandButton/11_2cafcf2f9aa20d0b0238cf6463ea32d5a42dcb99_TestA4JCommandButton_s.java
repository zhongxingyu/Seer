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
 
 package org.richfaces.tests.metamer.ftest.a4jCommandButton;
 
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.net.URL;
 
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.locator.Attribute;
 import org.jboss.test.selenium.locator.AttributeLocator;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page /faces/components/a4jCommandButton/simple.xhtml
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestA4JCommandButton extends AbstractMetamerTest {
 
     private JQueryLocator input = pjq("input[id$=input]");
     private JQueryLocator button = pjq("input[id$=a4jCommandButton]");
     private JQueryLocator output1 = pjq("span[id$=output1]");
     private JQueryLocator output2 = pjq("span[id$=output2]");
     private JQueryLocator output3 = pjq("span[id$=output3]");
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/a4jCommandButton/simple.xhtml");
     }
 
     @Test(groups = "client-side-perf")
     public void testSimpleClick() {
         selenium.typeKeys(input, "RichFaces 4");
         selenium.click(button);
 
         waitGui.until(textEquals.locator(output1).text("RichFaces 4"));
 
         String output = selenium.getText(output1);
         assertEquals(output, "RichFaces 4", "output1 when 'RichFaces 4' in input");
 
         output = selenium.getText(output2);
         assertEquals(output, "RichFa", "output2 when 'RichFaces 4' in input");
 
         output = selenium.getText(output3);
         assertEquals(output, "RICHFACES 4", "output3 when 'RichFaces 4' in input");
     }
 
     @Test
     public void testSimpleClickUnicode() {
         selenium.typeKeys(input, "ľščťžýáíéňô");
         selenium.click(button);
 
         waitGui.until(textEquals.locator(output1).text("ľščťžýáíéňô"));
 
         String output = selenium.getText(output1);
         assertEquals(output, "ľščťžýáíéňô", "output1 when 'ľščťžýáíéňô' in input");
 
         output = selenium.getText(output2);
         assertEquals(output, "ľščťžý", "output2 when 'ľščťžýáíéňô' in input");
 
         output = selenium.getText(output3);
         assertEquals(output, "ĽŠČŤŽÝÁÍÉŇÔ", "output3 when 'ľščťžýáíéňô' in input");
     }
 
     @Test
     public void testAction() {
         JQueryLocator doubleStringAction = pjq("input[value=doubleStringAction]");
         JQueryLocator first6CharsAction = pjq("input[value=first6CharsAction]");
         JQueryLocator toUpperCaseAction = pjq("input[value=toUpperCaseAction]");
 
         selenium.click(doubleStringAction);
         selenium.waitForPageToLoad(TIMEOUT);
         selenium.typeKeys(input, "RichFaces 4");
         selenium.click(button);
         waitGui.until(textEquals.locator(output1).text("RichFaces 4"));
         String output = selenium.getText(output2);
         assertEquals(output, "RichFaces 4RichFaces 4",
             "output2 when 'RichFaces 4' in input and doubleStringAction selected");
 
         selenium.click(first6CharsAction);
         selenium.waitForPageToLoad(TIMEOUT);
         selenium.typeKeys(input, "RichFaces 4ň");
         selenium.click(button);
         waitGui.until(textEquals.locator(output1).text("RichFaces 4ň"));
         output = selenium.getText(output2);
         assertEquals(output, "RichFa", "output2 when 'RichFaces 4ň' in input and first6CharsAction selected");
 
         selenium.click(toUpperCaseAction);
         selenium.waitForPageToLoad(TIMEOUT);
         selenium.typeKeys(input, "RichFaces 4ě");
         selenium.click(button);
         waitGui.until(textEquals.locator(output1).text("RichFaces 4ě"));
         output = selenium.getText(output2);
         assertEquals(output, "RICHFACES 4Ě", "output2 when 'RichFaces 4ě' in input and toUpperCaseAction selected");
     }
 
     @Test
     public void testActionListener() {
         JQueryLocator doubleStringActionListener = pjq("input[value=doubleStringActionListener]");
         JQueryLocator first6CharsActionListener = pjq("input[value=first6CharsActionListener]");
         JQueryLocator toUpperCaseActionListener = pjq("input[value=toUpperCaseActionListener]");
 
         selenium.click(doubleStringActionListener);
         selenium.waitForPageToLoad(TIMEOUT);
         selenium.typeKeys(input, "RichFaces 4");
         selenium.click(button);
         waitGui.until(textEquals.locator(output1).text("RichFaces 4"));
         String output = selenium.getText(output3);
         assertEquals(output, "RichFaces 4RichFaces 4",
             "output2 when 'RichFaces 4' in input and doubleStringActionListener selected");
 
         selenium.click(first6CharsActionListener);
         selenium.waitForPageToLoad(TIMEOUT);
         selenium.typeKeys(input, "RichFaces 4ň");
         selenium.click(button);
         waitGui.until(textEquals.locator(output1).text("RichFaces 4ň"));
         output = selenium.getText(output3);
         assertEquals(output, "RichFa", "output2 when 'RichFaces 4ň' in input and first6CharsActionListener selected");
 
         selenium.click(toUpperCaseActionListener);
         selenium.waitForPageToLoad(TIMEOUT);
         selenium.typeKeys(input, "RichFaces 4ě");
         selenium.click(button);
         waitGui.until(textEquals.locator(output1).text("RichFaces 4ě"));
         output = selenium.getText(output3);
         assertEquals(output, "RICHFACES 4Ě",
             "output2 when 'RichFaces 4ě' in input and toUpperCaseActionListener selected");
     }
 
     @Test
     public void testDisabled() {
         JQueryLocator disabledChecbox = pjq("input[id$=disabledInput]");
         AttributeLocator<?> disabledAttribute = button.getAttribute(new Attribute("disabled"));
 
         selenium.click(disabledChecbox);
         selenium.waitForPageToLoad(TIMEOUT);
 
         String isDisabled = selenium.getAttribute(disabledAttribute);
         assertEquals(isDisabled.toLowerCase(), "disabled", "The value of attribute disabled");
     }
 
     @Test
     public void testOnclick() {
         testFireEvent(Event.CLICK, button);
     }
 
     @Test
     public void testOndblclick() {
         testFireEvent(Event.DBLCLICK, button);
     }
 
     @Test
     public void testOnkeydown() {
         testFireEvent(Event.KEYDOWN, button);
     }
 
     @Test
     public void testOnkeypress() {
         testFireEvent(Event.KEYPRESS, button);
     }
 
     @Test
     public void testOneyup() {
         testFireEvent(Event.KEYUP, button);
     }
 
     @Test
     public void testOnmousedown() {
         testFireEvent(Event.MOUSEDOWN, button);
     }
 
     @Test
     public void testOnmousemove() {
         testFireEvent(Event.MOUSEMOVE, button);
     }
 
     @Test
     public void testOnmouseout() {
         testFireEvent(Event.MOUSEOUT, button);
     }
 
     @Test
     public void testOnmouseover() {
         testFireEvent(Event.MOUSEOVER, button);
     }
 
     @Test
     public void testOnmouseup() {
         testFireEvent(Event.MOUSEUP, button);
     }
 
     @Test
     public void testRender() {
         JQueryLocator renderInput = pjq("input[name$=renderInput]");
 
         selenium.type(renderInput, "output1");
         selenium.waitForPageToLoad(TIMEOUT);
 
         selenium.typeKeys(input, "aaa");
         selenium.click(button);
 
         waitGui.until(textEquals.locator(output1).text("aaa"));
 
         String output = selenium.getText(output1);
         assertEquals(output, "aaa", "output1 when 'aaa' in input and 'output1' set to be rerendered");
 
         output = selenium.getText(output2);
         assertEquals(output, "", "output2 when 'aaa' in input and 'output1' set to be rerendered");
 
         output = selenium.getText(output3);
         assertEquals(output, "", "output3 when 'aaa' in input and 'output1' set to be rerendered");
 
         selenium.type(renderInput, "output2 output3");
         selenium.waitForPageToLoad(TIMEOUT);
 
         selenium.typeKeys(input, "bbb");
         selenium.click(button);
 
         waitGui.until(textEquals.locator(output2).text("bbb"));
 
         output = selenium.getText(output1);
         assertEquals(output, "aaa", "output1 when 'bbb' in input and 'output2 output3' set to be rerendered");
 
         output = selenium.getText(output2);
         assertEquals(output, "bbb", "output2 when 'bbb' in input and 'output2 output3' set to be rerendered");
 
         output = selenium.getText(output3);
         assertEquals(output, "BBB", "output3 when 'bbb' in input and 'output2 output3' set to be rerendered");
 
     }
 
     @Test
     public void testRendered() {
         JQueryLocator renderedCheckbox = pjq("input[name$=renderedInput]");
 
         selenium.click(renderedCheckbox);
         selenium.waitForPageToLoad(TIMEOUT);
         assertFalse(selenium.isElementPresent(button), "Button should not be displayed");
     }
 
     @Test
     public void testStyleClass() {
         JQueryLocator wide = pjq("input[name$=styleClassInput][value=wide]");
         JQueryLocator big = pjq("input[name$=styleClassInput][value=big]");
         JQueryLocator none = pjq("input[name$=styleClassInput][value=]");
 
         final AttributeLocator<?> classAttribute = button.getAttribute(new Attribute("class"));
 
         selenium.click(wide);
         selenium.waitForPageToLoad(TIMEOUT);
         assertTrue(selenium.belongsClass(button, "wide"), "Button's class was not changed to 'wide'");
 
         selenium.click(big);
         selenium.waitForPageToLoad(TIMEOUT);
         assertTrue(selenium.belongsClass(button, "big"), "Button's class was not changed to 'big'");
 
         selenium.click(none);
         selenium.waitForPageToLoad(TIMEOUT);
         assertFalse(selenium.isAttributePresent(classAttribute), "Button's class was not removed.");
     }
 
     @Test
     public void testStyle() {
         JQueryLocator styleInput = pjq("input[id$=styleInput]");
         final AttributeLocator<?> attribute = button.getAttribute(new Attribute("style"));
         final String value = "font-size: 20px;";
 
         selenium.type(styleInput, value);
         selenium.waitForPageToLoad(TIMEOUT);
         
         assertEquals(selenium.getAttribute(attribute), value, "Style of the button did not change");
     }
 
     @Test
     public void testValue() {
         JQueryLocator valueInput = pjq("input[id$=valueInput]");
         final AttributeLocator<?> attribute = button.getAttribute(new Attribute("value"));
         final String value = "new label";
 
         selenium.type(valueInput, value);
         selenium.waitForPageToLoad(TIMEOUT);
         
         assertEquals(selenium.getAttribute(attribute), value, "Value of the button did not change");
     }
 
 }
