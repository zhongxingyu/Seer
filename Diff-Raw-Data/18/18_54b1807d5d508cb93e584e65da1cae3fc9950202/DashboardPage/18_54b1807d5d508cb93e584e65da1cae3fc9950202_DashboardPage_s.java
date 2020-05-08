 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.pages.appmanager;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import nl.surfnet.bod.pages.AbstractListPage;
 
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.PageFactory;
 
 public class DashboardPage extends AbstractListPage {
 
   private static final String PAGE = "/appmanager";
 
   public DashboardPage(RemoteWebDriver driver) {
     super(driver);
   }
 
   public static DashboardPage get(RemoteWebDriver driver) {
     DashboardPage page = new DashboardPage(driver);
     PageFactory.initElements(driver, page);
 
     return page;
   }
 
   public static DashboardPage get(RemoteWebDriver driver, String urlUnderTest) {
     driver.get(urlUnderTest + PAGE);
     return get(driver);
   }
 
   /**
    * Asserts the number of menu items, if it fails don't just update the number,
    * but add new test cases for the new menu
    */
   public void verifyNumberOfMenuItems() {
    assertThat(getCountMenuItems(), is(1));
   }
 
   public void verifyMenuOverview() {
     clickMenuLink("Overview");
     verifyIsCurrentPage();
     verifyHasNoTimeZone();
   }
 
   public void verifyIsCurrentPage() {
     super.verifyIsCurrentPage(PAGE);
   }
 }
