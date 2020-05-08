 package pl.itcrowd.summer_code.test;
 
 import org.jboss.arquillian.graphene.enricher.findby.FindBy;
 import org.openqa.selenium.WebElement;
 
 import java.util.List;
 
 import static org.jboss.arquillian.graphene.Graphene.guardHttp;
 
 public class LoginPage {
 
    @FindBy(id = "lF:e:i")
     private WebElement emailInput;
 
    @FindBy(id = "lF:p:i")
     private WebElement passwordInput;
 
     @FindBy(css = "#lF div.form-actions > input")
     private WebElement submitButton;
 
     @FindBy(css = "#row-fluid form.headerForm ul:nth-of-type(1) li:nth-of-type(3) > a")
     private WebElement welcomeUserButton;
 
    @FindBy(id = "lF:e:message1")
     private WebElement emptyEmailInputMessage;
 
    @FindBy(id = "lF:p:message1")
     private  WebElement emptyPasswordInputMessage;
 
     @FindBy(css = "div.rf-ntf-cnt")
     private List<WebElement> popUps;
 
     @FindBy(css = "#lF div.form-actions div:nth-of-type(1) a:nth-of-type(1)")
     private WebElement registerAsCustomerLink;
 
     @FindBy(css = "#lF div.form-actions div:nth-of-type(1) a:nth-of-type(2)")
     private WebElement registerAsPsychicLink;
 
     @FindBy(css = "#lF div.form-actions div:nth-of-type(2) > a")
     private WebElement forgotYourPasswordLink;
 
     @FindBy(css = "#lF div.form-actions dLinkiv:nth-of-type(2) > a")
     private WebElement resendActivationMailLink;
 
 
     public void setEmailInput(String email){
         emailInput.clear();
         emailInput.sendKeys(email);
     }
     public void setPasswordInput(String password){
         passwordInput.clear();
         emailInput.sendKeys(password);
     }
     public void submitButtonClick(){
         guardHttp(submitButton).click();
     }
     public boolean isLoggedIn(){
         return welcomeUserButton.isDisplayed();
     }
     public boolean isEmptyEmailMessageDisplayed(){
         return emptyEmailInputMessage.isDisplayed();
     }
     public  boolean isEmptyPasswordMessageDisplayed(){
         return emptyPasswordInputMessage.isDisplayed();
     }
     public String getEmptyEmailMessage(){
         return emptyEmailInputMessage.getText();
     }
     public String getEmptyPasswordMEssage(){
         return emptyPasswordInputMessage.getText();
     }
     public void registerAsCustomerClick(){
         guardHttp(registerAsCustomerLink).click();
     }
     public void registerAsPsychicClick(){
         guardHttp(registerAsPsychicLink).click();
     }
     public void forgotYourPasswordLinkClick(){
         guardHttp(forgotYourPasswordLink).click();
     }
     public void resendActivationMailClick(){
         guardHttp(resendActivationMailLink).click();
     }
     public int countPopUps(){
         return popUps.size();
     }
     public String getPopUpText(int number){
         return popUps.get(number).getText();
     }
 }
