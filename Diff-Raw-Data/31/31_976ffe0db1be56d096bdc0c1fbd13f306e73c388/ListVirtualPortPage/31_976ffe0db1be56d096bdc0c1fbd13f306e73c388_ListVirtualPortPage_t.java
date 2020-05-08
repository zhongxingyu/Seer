 /**
  * Copyright (c) 2012, 2013 SURFnet BV
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
 package nl.surfnet.bod.pages.user;
 
 import java.util.ArrayList;
 import java.util.List;
import java.util.concurrent.TimeUnit;
 
 import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
 
 import nl.surfnet.bod.domain.NsiVersion;
 import nl.surfnet.bod.pages.AbstractListPage;
 
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.PageFactory;
 
 public class ListVirtualPortPage extends AbstractListPage {
 
   private static final String PAGE = "/virtualports";
 
   @FindBy(id = "reqVpId")
   private WebElement requestVirtualPortLink;
 
   public ListVirtualPortPage(RemoteWebDriver driver) {
     super(driver);
   }
 
   public static ListVirtualPortPage get(RemoteWebDriver driver) {
     ListVirtualPortPage page = new ListVirtualPortPage(driver);
     PageFactory.initElements(driver, page);
 
     return page;
   }
 
   public static ListVirtualPortPage get(RemoteWebDriver driver, String urlUnderTest) {
     driver.get(urlUnderTest + PAGE);
     return get(driver);
   }
 
   public EditVirtualPortPage edit(String oldLabel) {
     editRow(oldLabel);
     return EditVirtualPortPage.get(getDriver());
   }
 
   public RequestNewVirtualPortSelectTeamPage requestVirtualPort() {
     requestVirtualPortLink.click();
 
     return RequestNewVirtualPortSelectTeamPage.get(getDriver());
   }
 
   public void verifyIsCurrentPage() {
     super.verifyIsCurrentPage(PAGE);
   }
 
   public List<String> getAllNsiStpIds(final NsiVersion nsiVersion) {
     String cssClassName = nsiVersion == NsiVersion.ONE ? "nsiV1StpId" : "nsiV2StpId";
 
     // their details which we are after actually have to be visible
     for (WebElement moreLink: getDriver().findElementsByCssSelector("i.icon-plus-sign")){
       moreLink.click();
     }
 
    Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

     List<WebElement> ddElements = getDriver().findElementsByClassName(cssClassName);
     List<String> virtualPortIds = new ArrayList<>();
     for (WebElement webElement: ddElements) {
       String id = webElement.getText();
       if (!Strings.isNullOrEmpty(id)) {
         virtualPortIds.add(id);
       }
     }
 
     return virtualPortIds;
   }
 
 }
