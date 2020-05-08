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
 
 package org.richfaces.tests.metamer.ftest.a4jCommandLink;
 
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
  * Test case for page /faces/components/a4jCommandLink/simple.xhtml
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestA4JCommandLink extends AbstractMetamerTest {
 
     private JQueryLocator input = pjq("input[id$=input]");
     private JQueryLocator link = pjq("a[id$=a4jCommandLink]");
     private JQueryLocator output1 = pjq("span[id$=output1]");
     private JQueryLocator output2 = pjq("span[id$=output2]");
     private JQueryLocator output3 = pjq("span[id$=output3]");
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/a4jCommandLink/simple.xhtml");
     }
 
     @Test(groups = "client-side-perf")
     public void testSimpleClick() {
         selenium.typeKeys(input, "RichFaces 4");
         selenium.click(link);
 
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
         selenium.click(link);
 
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
         selenium.click(link);
         waitGui.until(textEquals.locator(output1).text("RichFaces 4"));
         String output = selenium.getText(output2);
         assertEquals(output, "RichFaces 4RichFaces 4",
             "output2 when 'RichFaces 4' in input and doubleStringAction selected");
 
         selenium.click(first6CharsAction);
         selenium.waitForPageToLoad(TIMEOUT);
         selenium.typeKeys(input, "RichFaces 4ň");
         selenium.click(link);
         waitGui.until(textEquals.locator(output1).text("RichFaces 4ň"));
         output = selenium.getText(output2);
         assertEquals(output, "RichFa", "output2 when 'RichFaces 4ň' in input and first6CharsAction selected");
 
         selenium.click(toUpperCaseAction);
         selenium.waitForPageToLoad(TIMEOUT);
         selenium.typeKeys(input, "RichFaces 4ě");
         selenium.click(link);
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
         selenium.click(link);
         waitGui.until(textEquals.locator(output1).text("RichFaces 4"));
         String output = selenium.getText(output3);
         assertEquals(output, "RichFaces 4RichFaces 4",
             "output2 when 'RichFaces 4' in input and doubleStringActionListener selected");
 
         selenium.click(first6CharsActionListener);
         selenium.waitForPageToLoad(TIMEOUT);
         selenium.typeKeys(input, "RichFaces 4ň");
         selenium.click(link);
         waitGui.until(textEquals.locator(output1).text("RichFaces 4ň"));
         output = selenium.getText(output3);
         assertEquals(output, "RichFa", "output2 when 'RichFaces 4ň' in input and first6CharsActionListener selected");
 
         selenium.click(toUpperCaseActionListener);
         selenium.waitForPageToLoad(TIMEOUT);
         selenium.typeKeys(input, "RichFaces 4ě");
         selenium.click(link);
         waitGui.until(textEquals.locator(output1).text("RichFaces 4ě"));
         output = selenium.getText(output3);
         assertEquals(output, "RICHFACES 4Ě",
             "output2 when 'RichFaces 4ě' in input and toUpperCaseActionListener selected");
     }
 
     @Test
     public void testDisabled() {
         JQueryLocator disabledChecbox = pjq("input[id$=disabledInput]");
         JQueryLocator newLink = pjq("span[id$=a4jCommandLink]");
 
         selenium.click(disabledChecbox);
         selenium.waitForPageToLoad(TIMEOUT);
 
         assertFalse(selenium.isElementPresent(link), link.getAsString()
             + " should not be on page when the link is disabled");
         assertTrue(selenium.isElementPresent(newLink), newLink.getAsString()
             + " should be on page when the link is disabled");
 
     }
 
     @Test
     public void testOnclick() {
         testFireEvent(Event.CLICK, link);
     }
 
     @Test
     public void testOndblclick() {
         testFireEvent(Event.DBLCLICK, link);
     }
 
     @Test
     public void testOnkeydown() {
         testFireEvent(Event.KEYDOWN, link);
     }
 
     @Test
     public void testOnkeypress() {
         testFireEvent(Event.KEYPRESS, link);
     }
 
     @Test
     public void testOneyup() {
         testFireEvent(Event.KEYUP, link);
     }
 
     @Test
     public void testOnmousedown() {
         testFireEvent(Event.MOUSEDOWN, link);
     }
 
     @Test
     public void testOnmousemove() {
         testFireEvent(Event.MOUSEMOVE, link);
     }
 
     @Test
     public void testOnmouseout() {
         testFireEvent(Event.MOUSEOUT, link);
     }
 
     @Test
     public void testOnmouseover() {
         testFireEvent(Event.MOUSEOVER, link);
     }
 
     @Test
     public void testOnmouseup() {
         testFireEvent(Event.MOUSEUP, link);
     }
 
     @Test
     public void testRender() {
         JQueryLocator renderInput = pjq("input[name$=renderInput]");
 
         selenium.type(renderInput, "output1");
         selenium.waitForPageToLoad(TIMEOUT);
 
         selenium.typeKeys(input, "aaa");
         selenium.click(link);
 
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
         selenium.click(link);
 
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
         assertFalse(selenium.isElementPresent(link), "Button should not be displayed");
     }
 
     @Test
     public void testStyleClass() {
         JQueryLocator bold = pjq("input[name$=styleClassInput][value=bold]");
         JQueryLocator strike = pjq("input[name$=styleClassInput][value=strike]");
         JQueryLocator none = pjq("input[name$=styleClassInput][value=]");
 
         final AttributeLocator<?> classAttribute = link.getAttribute(new Attribute("class"));
 
         selenium.click(bold);
         selenium.waitForPageToLoad(TIMEOUT);
         assertTrue(selenium.belongsClass(link, "bold"), "Button's class was not changed to 'bold'");
 
         selenium.click(strike);
         selenium.waitForPageToLoad(TIMEOUT);
         assertTrue(selenium.belongsClass(link, "strike"), "Button's class was not changed to 'strike'");
 
         selenium.click(none);
         selenium.waitForPageToLoad(TIMEOUT);
         assertFalse(selenium.isAttributePresent(classAttribute), "Button's class was not removed.");
     }
 
     @Test
     public void testStyle() {
         JQueryLocator styleInput = pjq("input[id$=styleInput]");
         final AttributeLocator<?> attribute = link.getAttribute(new Attribute("style"));
         final String value = "font-size: 20px;";
 
         selenium.type(styleInput, value);
         selenium.waitForPageToLoad(TIMEOUT);
         
         assertEquals(selenium.getAttribute(attribute), value, "Style of the button did not change");
     }
 
     @Test
     public void testValue() {
         JQueryLocator valueInput = pjq("input[id$=valueInput]");
         final String value = "new label";
 
         selenium.type(valueInput, value);
         selenium.waitForPageToLoad(TIMEOUT);
         
         assertEquals(selenium.getText(link), value, "Value of the button did not change");
     }
 }
