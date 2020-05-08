 package smartpool.functional.page;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.How;
 
 public class CreateProfilePage extends Page {
 
     public static final String ADDRESS_NAME = "address";
    public static final String CONTACT_NUMBER = "contactNumber";
    public static final String PREFERRED_PICKUP_POINT = "preferredPickupPoint";
    public static final String PREFERRED_PICKUP_TIME = "preferredPickupTime";
    public static final String SUBMIT = "submit";
 
     @FindBy(how = How.NAME, using = ADDRESS_NAME)
     private WebElement addressTextArea;
 
    @FindBy(how = How.NAME, using = CONTACT_NUMBER)
     private WebElement contactNumberTextBox;
 
    @FindBy(how = How.NAME, using = PREFERRED_PICKUP_POINT)
     private WebElement preferredPickupPointTextBox;
 
    @FindBy(how = How.NAME, using = PREFERRED_PICKUP_TIME)
     private WebElement preferredPickupTimeTextBox;
 
    @FindBy(how = How.NAME, using = SUBMIT)
     private WebElement submitButton;
 
     public CreateProfilePage(WebDriver webDriver) {
         super(webDriver);
     }
 
     @Override
     public void waitForThePageToLoad() {
        waitForElementToLoad(By.name(CONTACT_NUMBER));
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
 
 
