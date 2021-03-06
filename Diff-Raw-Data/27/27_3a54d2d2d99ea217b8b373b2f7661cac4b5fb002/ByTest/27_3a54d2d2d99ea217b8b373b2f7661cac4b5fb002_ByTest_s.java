 /*
 Copyright 2007-2009 WebDriver committers
 Copyright 2007-2009 Google Inc.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
  */
 
 package org.openqa.selenium;
 
 import org.jmock.Expectations;
 import org.junit.Test;
 import org.openqa.selenium.internal.FindsById;
 import org.openqa.selenium.internal.FindsByLinkText;
 import org.openqa.selenium.internal.FindsByName;
 import org.openqa.selenium.internal.FindsByXPath;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 
 public class ByTest extends MockTestBase {
 
   @Test
   public void shouldUseFindsByNameToLocateElementsByName() {
     final AllDriver driver = mock(AllDriver.class);
 
     checking(new Expectations() {
       {
         one(driver).findElementByName("cheese");
       }
     });
 
     By by = By.name("cheese");
     by.findElement(driver);
   }
 
   @Test
   @org.junit.Ignore
   public void xtestShouldUseXPathToFindByNameIfDriverDoesNotImplementFindsByName() {
     final OnlyXPath driver = mock(OnlyXPath.class);
 
     checking(new Expectations() {
       {
         one(driver).findElementByXPath("//*[@name='cheese']");
       }
     });
 
     By by = By.name("cheese");
 
     by.findElement(driver);
   }
 
   @Test
   public void innerClassesArePublicSoThatTheyCanBeReusedElsewhere() {
     assertThat(new By.ByXPath("a").toString(), equalTo("By.xpath: a"));
     assertThat(new By.ById("a").toString(), equalTo("By.id: a"));
     assertThat(new By.ByClassName("a").toString(), equalTo("By.className: a"));
     assertThat(new By.ByLinkText("a").toString(), equalTo("By.linkText: a"));
     assertThat(new By.ByName("a").toString(), equalTo("By.name: a"));
     assertThat(new By.ByTagName("a").toString(), equalTo("By.tagName: a"));
     assertThat(new By.ByCssSelector("a").toString(), equalTo("By.selector: a"));
     assertThat(new By.ByPartialLinkText("a").toString(), equalTo("By.partialLinkText: a"));
   }
 
   private interface AllDriver
       extends FindsById, FindsByLinkText, FindsByName, FindsByXPath, SearchContext {
     // Place holder
   }
 
   private interface OnlyXPath extends FindsByXPath, SearchContext {
 
   }
 }
