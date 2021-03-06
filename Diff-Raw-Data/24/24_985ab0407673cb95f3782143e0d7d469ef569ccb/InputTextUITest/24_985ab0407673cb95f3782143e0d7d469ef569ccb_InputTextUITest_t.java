 /*
  * Copyright (C) 2013 salesforce.com, inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.auraframework.components.ui;
 
 import org.auraframework.def.ComponentDef;
 import org.auraframework.def.DefDescriptor;
 import org.auraframework.test.WebDriverTestCase;
 import org.auraframework.test.WebDriverUtil.BrowserType;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.interactions.Actions;
 
 /**
  * UI tests for inputText Component
  */
 public class InputTextUITest extends WebDriverTestCase {
 
     public static final String TEST_CMP = "/uitest/inputtextupdateontest.cmp";
 
     public InputTextUITest(String name) {
         super(name);
     }
 
     @ExcludeBrowsers({ BrowserType.ANDROID_PHONE, BrowserType.ANDROID_TABLET })
     public void testUpdateOnAttribute_UsingStringSource() throws Exception {
         String event = "blur";
         String baseTag = "<aura:component  model=\"java://org.auraframework.impl.java.model.TestJavaModel\"> "
                 + "<div id=\"%s\">" + event + ":"
                 + "<ui:inputText aura:id=\"%s\" value=\"{!m.String}\" updateOn=\"%s\"/>" + "</div>"
                 + "<div id=\"output\">" + "output: <ui:outputText value=\"{!m.String}\"/>" + "</div>"
                 + "</aura:component>";
         DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(ComponentDef.class, baseTag.replaceAll("%s", event));
         open(String.format("/%s/%s.cmp", cmpDesc.getNamespace(), cmpDesc.getName()));
         WebDriver d = getDriver();
         String value = getCurrentModelValue();
         WebElement input = auraUITestingUtil.findElementAndTypeEventNameInIt(event);
         WebElement outputDiv = d.findElement(By.id("output"));
         assertModelValue(value); // value shouldn't be updated yet
         input.click();
         outputDiv.click();// to simulate tab behavior for touch browsers
         value = assertModelValue(event); // value should have been updated
     }
 
     @ExcludeBrowsers({ BrowserType.ANDROID_PHONE, BrowserType.ANDROID_TABLET, BrowserType.IPAD, BrowserType.SAFARI,
             BrowserType.IPHONE })
     // Change event not picked up on IOS devices
     public void testUpdateOnAttributeForNonIosAndroidDevice() throws Exception {
         open(TEST_CMP);
         WebDriver d = getDriver();
         WebElement outputDiv = d.findElement(By.id("output"));
         String eventName = "change";
         auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         outputDiv.click();
         assertModelValue(eventName);
     }
 
     @ExcludeBrowsers({ BrowserType.ANDROID_PHONE, BrowserType.ANDROID_TABLET })
     public void testUpdateOnAttribute() throws Exception {
         open(TEST_CMP);
         String value = getCurrentModelValue();
         WebDriver d = getDriver();
         WebElement outputDiv = d.findElement(By.id("output"));
         String eventName = "blur";
         WebElement input = auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         assertModelValue(value); // value shouldn't be updated yet
         input.click();
         outputDiv.click(); // to simulate tab behavior for touch browsers
         value = assertModelValue(eventName); // value should have been updated
         assertDomEventSet();
 
         eventName = "click";
         input = auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         assertModelValue(value);
         outputDiv.click();
         assertModelValue(value);
         input.click();
         value = assertModelValue(eventName);
         assertDomEventSet();
 
         eventName = "focus";
         input = auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         outputDiv.click();
         input.click();
         value = assertModelValue(eventName);
         assertDomEventSet();
 
         eventName = "keydown";
         input = auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         value = assertModelValue(eventName.substring(0, eventName.length() - 1));
         assertDomEventSet();
 
         eventName = "keypress";
         input = auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         value = assertModelValue(eventName.substring(0, eventName.length() - 1));
         assertDomEventSet();
 
         eventName = "keyup";
         input = auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         value = assertModelValue(eventName);
         assertDomEventSet();
     }
 
     @TargetBrowsers({ BrowserType.GOOGLECHROME })
     public void testUpdateOnAttributeWithCertainEventsChrome() throws Exception {
         open(TEST_CMP);
         String value = getCurrentModelValue();
         WebDriver d = getDriver();
         Actions a = new Actions(d);
 
         WebElement outputDiv = d.findElement(By.id("output"));
 
         String eventName = "dblclick";
         WebElement input = auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         assertModelValue(value);
         a.doubleClick(input).build().perform();
         value = assertModelValue(eventName);
         assertDomEventSet();
 
         eventName = "mousemove";
         input = auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         assertModelValue(value);
         a.moveToElement(input).moveByOffset(0, 100).build().perform();
         value = assertModelValue(eventName);
         assertDomEventSet();
 
         eventName = "mouseout";
         input = auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         assertModelValue(value);
         a.moveToElement(input).moveToElement(outputDiv).build().perform();
         value = assertModelValue(eventName);
         assertDomEventSet();
 
         eventName = "mouseover";
         input = auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         assertModelValue(value);
         outputDiv.click();
         a.moveToElement(input).build().perform();
         value = assertModelValue(eventName);
         assertDomEventSet();
 
         eventName = "mouseup";
         input = auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         assertModelValue(value);
         input.click();
         value = assertModelValue(eventName);
         assertDomEventSet();
 
         eventName = "select";
         input = auraUITestingUtil.findElementAndTypeEventNameInIt(eventName);
         assertModelValue(value);
         a.doubleClick(input).build().perform();
         value = assertModelValue(eventName);
     }
 
     /**
      * Different browsers support different events, so this case tests an event supported by all browsers.
      * testUpdateOnAttributeWithCertainEventsChrome() more extensively tests different event types, but only on Chrome
      * where we know they are all supported.
      * 
      * Note we are not using auraUITestingUtil.findElementAndTypeEventNameInIt(eventName) in this test because the
      * Android driver sends a mousedown event when clearing the text field.
      */
     public void testUpdateOnAttributeWithCertainEventsAllBrowsers() throws Exception {
         open(TEST_CMP);
         String value = getCurrentModelValue();
         String eventName = "mousedown";
 
         String locatorTemplate = "#%s > input.uiInputText.uiInput";
         String locator = String.format(locatorTemplate, eventName);
         WebElement input = getDriver().findElement(By.cssSelector(locator));
         input.click();
         input.sendKeys(eventName);
 
         assertModelValue(value);
         input.click();
         value = assertModelValue(value + eventName);
         assertDomEventSet();
     }
 
     // W-1551077: Issue with Webdriver API ignores maxlength HTML5 attribute (iOS/Safari)
     @ExcludeBrowsers({ BrowserType.IPAD, BrowserType.IPHONE, BrowserType.ANDROID_PHONE, BrowserType.ANDROID_TABLET,
             BrowserType.SAFARI })
     public void testMaxLength() throws Exception {
         open("/uitest/inputTextMaxLength.cmp");
         WebElement input = findDomElement(By.cssSelector("input.uiInputText.uiInput"));
         input.click();
         input.sendKeys("1234567890");
         assertEquals("Text not truncated to 5 chars correctly", "12345", input.getAttribute("value"));
     }
 
     public void testNoMaxLength() throws Exception {
         open("/uitest/inputTextNoMaxLength.cmp");
         WebElement input = findDomElement(By.cssSelector("input.uiInputText.uiInput"));
         input.click();
         String inputText = "1234567890";
         input.sendKeys(inputText);
         assertEquals("Expected untruncated text", inputText, input.getAttribute("value"));
     }
 
     private String assertModelValue(String expectedValue) {
         String value = getCurrentModelValue();
         assertEquals("Model value is not what we expected", expectedValue, value);
         return value;
     }
 
     private String getCurrentModelValue() {
         String valueExpression = auraUITestingUtil.prepareReturnStatement(auraUITestingUtil
                 .getValueFromRootExpr("m.string"));
         String value = (String) auraUITestingUtil.getEval(valueExpression);
         return value;
     }
 
     private void assertDomEventSet() {
         String valueExpression = auraUITestingUtil.prepareReturnStatement(auraUITestingUtil
                 .getValueFromRootExpr("v.isDomEventSet"));
         boolean value = auraUITestingUtil.getBooleanEval(valueExpression);
         assertTrue("domEvent attribute on event should have been set.", value);
     }
 
     public void testNullValue() throws Exception {
         String cmpSource = "<aura:component  model=\"java://org.auraframework.impl.java.model.TestJavaModel\"> "
                 + "<ui:inputText value=\"{!m.StringNull}\"/>" + "</aura:component>";
         DefDescriptor<ComponentDef> inputTextNullValue = addSourceAutoCleanup(ComponentDef.class, cmpSource);
         open(String.format("/%s/%s.cmp", inputTextNullValue.getNamespace(), inputTextNullValue.getName()));
 
         WebElement input = getDriver().findElement(By.tagName("input"));
         assertEquals("Value of input is incorrect", "", input.getText());
     }
 
     public void testBaseKeyboardEventValue() throws Exception {
         open(TEST_CMP);
         String inputText = "z";
         WebElement input = findDomElement(By.cssSelector(".keyup"));
         WebElement outputValue = findDomElement(By.cssSelector(".outputValue"));
         input.click();
         input.sendKeys(inputText);
         try {
             char outputText = (char) Integer.parseInt(outputValue.getText());
             assertEquals("InputChar and outputChar are different ", inputText.charAt(0), outputText);
         } catch (Exception e) {
             fail("ParseInt failed with following error" + e.getMessage());
         }
     }
 
     // W-1625895: Safari WebDriver bug- cannot right click because interactions API not implemented
     @ExcludeBrowsers({ BrowserType.IPAD, BrowserType.IPHONE, BrowserType.SAFARI, BrowserType.ANDROID_PHONE,
             BrowserType.ANDROID_TABLET })
     public void testBaseMouseClickEventValue() throws Exception {
         open(TEST_CMP);
         WebElement input = findDomElement(By.cssSelector(".keyup"));
         WebElement outputValue = findDomElement(By.cssSelector(".outputValue"));
 
         // IE < 9 uses values 1, 2, 4 for left, right, middle click (respectively)
         String expectedVal = (checkBrowserType("IE7") || checkBrowserType("IE8")) ? "1" : "0";
         input.click();
         assertEquals("Left click not performed ", expectedVal, outputValue.getText());
 
         // right click behavior
         Actions actions = new Actions(getDriver());
         actions.contextClick(input).perform();
         assertEquals("Right click not performed ", "2", outputValue.getText());
     }
    
    /**
     * Test Case for W-1689213
     * @throws Exception
     */
    public void testInputTextWithLabel() throws Exception {
        open(TEST_CMP);
        WebElement div = findDomElement(By.id("inputwithLabel"));
        WebElement input = div.findElement(By.tagName("input"));
        WebElement outputDiv = findDomElement(By.id("output"));
        
        String inputAuraId = "inputwithLabel";
        String valueExpression = auraUITestingUtil.getValueFromCmpRootExpression(inputAuraId,"v.value");
        String defExpectedValue = (String) auraUITestingUtil.getEval(valueExpression);
        assertEquals("Default value should be the same", inputAuraId, defExpectedValue);
        
        String inputText = "UpdatedText";
        input.clear();
        input.click();
        input.sendKeys(inputText);
        outputDiv.click(); // to simulate tab behavior for touch browsers
        String expectedValue = (String) auraUITestingUtil.getEval(valueExpression);
        assertEquals("Value of Input text shoud be updated", inputText, expectedValue);
    }
 }
