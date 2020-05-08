 package org.selenium.base;
 
 import org.openqa.selenium.Alert;
 import org.openqa.selenium.NoAlertPresentException;
 import org.openqa.selenium.WebDriver;
 import org.selenium.util.Selenium2Utils;
 
 import com.google.common.base.Preconditions;
 
 public abstract class AbstractDriverNew<D extends AbstractDriverNew<D>> {
 
     protected final WebDriver driver;
 
     public AbstractDriverNew(final WebDriver driverToSet) {
         super();
 
         Preconditions.checkNotNull(driverToSet);
         this.driver = driverToSet;
     }
 
     //
 
     public final WebDriver getWebDriver() {
         return this.driver;
     }
 
     // API - errors of any kind
     public final boolean anyProblem() {
         // check out: http://code.google.com/p/selenium/issues/detail?id=2438
         /*
         if( this.isAlertPresent() ){
         	return true;
         }
         // TODO: at some point, this could check if an basic element of the page is active and if it is not, only then check for an alert
         */
 
         if (this.isErrorPresent()) {
             return true;
         }
 
         return false;
     }
 
     @SuppressWarnings("static-method")
     public boolean isErrorPresent() {
         throw new UnsupportedOperationException();
     }
 
     public final boolean isAlertPresent() {
         final long step0 = System.currentTimeMillis();
         Alert alert = null;
         try {
             alert = this.driver.switchTo().alert();
         } catch (final NoAlertPresentException e) {
             final long step1 = System.currentTimeMillis();
             System.out.println("1: " + (step1 - step0));
 
             return false;
         }
 
         return Selenium2Utils.isAlertPresent(alert);
     }
 
     public final void dismissAlertIfPresent() {
         if (!this.anyProblem()) {
             return;
         }
         this.driver.switchTo().alert().dismiss();
     }
 
     //
     /**
      * Verifies that the current status of the page indeed matches this driver <br>
      * - note: this is meant to be overridden <br>
      */
     public boolean isHere() {
         return this.getWebDriver().getCurrentUrl().equals(this.getBaseUri());
     }
 
     public D wait(final int seconds) {
         Selenium2Utils.Wait.waitForElementFoundById(this.getWebDriver(), this.getElementId(), seconds);
         return (D) this;
     }
 
     public final D tryWait(final int seconds) {
         try {
             this.wait(seconds);
         } catch (final Exception e) {
             // ignore
         }
 
         return (D) this;
     }
 
     // checks
 
     public final boolean containsPartialText(final String text) {
         return Selenium2Utils.isElementDisplayedByPartialText(this.getWebDriver(), text);
     }
 
     public final boolean containsLinkText(final String linkText) {
         return Selenium2Utils.isElementDisplayedByLinkText(this.getWebDriver(), linkText);
     }
 
     public final boolean containsPartialLinkText(final String partialLinkText) {
         return Selenium2Utils.isElementDisplayedByPartialLinkText(this.getWebDriver(), partialLinkText);
     }
 
     // navigation
 
     public D navigateToCurrent() {
         this.getWebDriver().get(this.getBaseUri());
        return this.wait(3);
     }
 
     public final String getCurrentUrl() {
         return this.getWebDriver().getCurrentUrl();
     }
 
     public final void back() {
         this.getWebDriver().navigate().back();
     }
 
     public final void forward() {
         this.getWebDriver().navigate().forward();
     }
 
     // template
 
     protected String getBaseUri() {
         throw new UnsupportedOperationException("This is meant to be extended");
     }
 
     protected String getElementId() {
         throw new UnsupportedOperationException("This is meant to be extended");
     }
 
 }
