 package smartpool.functional.page;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.How;
 
 public class CreateProfilePage extends Page {
 
     public static final String ADDRESS_NAME = "address";
 
     @FindBy(how = How.NAME, using = ADDRESS_NAME)
     private WebElement addressTextArea;
 
    @FindBy(how = How.NAME, using = "contactNumber")
     private WebElement contactNumberTextBox;
 
    @FindBy(how = How.NAME, using = "preferredPickupPoint")
     private WebElement preferredPickupPointTextBox;
 
    @FindBy(how = How.NAME, using = "preferredPickupTime")
     private WebElement preferredPickupTimeTextBox;
 
    @FindBy(how = How.NAME, using = "submit")
     private WebElement submitButton;
 
     public CreateProfilePage(WebDriver webDriver) {
         super(webDriver);
     }
 
     @Override
     public void waitForThePageToLoad() {
        waitForElementToLoad(By.name(ADDRESS_NAME));
     }
 
     public ViewProfilePage enterProfileDetails() {
         addressTextArea.sendKeys("ThoughtWorks");
         contactNumberTextBox.sendKeys("1234567890");
         preferredPickupPointTextBox.sendKeys("Ooty Chocolates");
         preferredPickupTimeTextBox.sendKeys("10:00");
         submitButton.click();
         return new ViewProfilePage(webDriver);
     }
 }
 
 
