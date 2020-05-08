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
 package nl.surfnet.bod.pages.noc;
 
 import java.util.List;
 
 import nl.surfnet.bod.pages.AbstractFormPage;
 import nl.surfnet.bod.support.Probes;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.noc.PhysicalResourceGroupController;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.PageFactory;
 
 public class NewPhysicalResourceGroupPage extends AbstractFormPage {
 
   private static final String PAGE = "/noc/" + PhysicalResourceGroupController.PAGE_URL + WebUtils.CREATE;
 
   private final Probes probes;
 
   @FindBy(css = "input[name='instituteId_search']")
   private WebElement instituteInput;
 
   @FindBy(css = "input[name='adminGroup']")
   private WebElement adminGroupInput;
 
   @FindBy(css = "input[name='managerEmail']")
   private WebElement managerEmailInput;
 
  @FindBy(css = "input[value='api']")
  private WebElement apiRadio;

  @FindBy(css = "input[value='sab']")
  private WebElement sabRadio;

   public NewPhysicalResourceGroupPage(RemoteWebDriver driver) {
     super(driver);
     this.probes = new Probes(driver);
   }
 
   public static NewPhysicalResourceGroupPage get(RemoteWebDriver driver, String host) {
     driver.get(host + PAGE);
     NewPhysicalResourceGroupPage page = new NewPhysicalResourceGroupPage(driver);
     PageFactory.initElements(driver, page);
 
     return page;
   }
 
   public void sendInstitute(String institute) {
     instituteInput.clear();
     instituteInput.sendKeys(institute);
 
     By resultListSelector = By.cssSelector(".as-results ul.as-list");
     probes.assertTextPresent(resultListSelector, institute);
 
     List<WebElement> results = getDriver().findElement(resultListSelector).findElements(By.className("as-result-item"));
     for (WebElement result : results) {
       if (result.getText().equals(institute)) {
         result.click();
         return;
       }
     }
 
     throw new NoSuchElementException("Could not find institute for name " + institute);
   }
 
   public void sendAdminGroup(String adminGroup) {
     adminGroupInput.clear();
     adminGroupInput.sendKeys(adminGroup);
   }
 
   public void sendEmail(String email) {
     managerEmailInput.clear();
     managerEmailInput.sendKeys(email);
   }
 
   public void verifyIsCurrentPage() {
     super.verifyIsCurrentPage(PAGE);
   }
 
  public void selectApiMethod() {
    apiRadio.click();
  }

  public void selectSabMethod() {
    sabRadio.click();
  }
}
