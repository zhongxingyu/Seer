 package be.cegeka.rsvz.ui;
 
 import com.google.common.base.Predicate;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.interactions.Actions;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 public class HomePage extends AbstractParentPage {
 
     @FindBy(id = "selectLanguage")
     private WebElement selectLanguage;
 
     @FindBy(id = "selectLanguageItem2")
    private WebElement dutchLanguage;
 
     @FindBy(id = "languageLabel")
     private WebElement languageLabel;
 
     public HomePage(WebDriver driver) {
         super(driver);
     }
 
     public Boolean changeLanguage() {
         Actions builder = new Actions(driver);
         builder.moveToElement(selectLanguage).click().perform();
        builder.moveToElement(dutchLanguage).click().perform();
         WebDriverWait wait = new WebDriverWait(driver, DRIVER_WAIT_SECONDS);
         Predicate<WebDriver> isTrue = new Predicate<WebDriver>() {
             public boolean apply(WebDriver webdriver) {
                 return languageLabel.getText().equals("Langue");
             }
         };
         wait.until(isTrue);
         return true;
     }
 }
