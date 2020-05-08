 
 package com.myfitnesspal.qa.pages.user;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.PageFactory;
 
 import com.myfitnesspal.qa.components.user.TopMainMenu;
 import com.myfitnesspal.qa.data.GeneratedUser;
 import com.myfitnesspal.qa.pages.Page;
 
 /*                                                     
  * FoodDiaryPage - page representation with element locators, validations and action methods. 
  *                                               
  * @author: Sergei Zagriychuk (sergeizagriychuk@gmail.com)
  */
 public class CreateAccountPage extends Page
 {
 
 	@FindBy(id = "user_username")
 	public WebElement fieldUserName;
 
 	@FindBy(id = "user_email")
 	public WebElement fieldUserEmail;
 
 	@FindBy(id = "user_password")
 	public WebElement fieldUserPassword;
 
	@FindBy(xpath = "//li[@class='submit']/input[contains(@value, 'continue ')]")
 	public WebElement btnContinue;
 
 	private TopMainMenu topMainMenu;
 
 	public CreateAccountPage(WebDriver driver)
 	{
 		super(driver);
 		initURL("/account/create");
 		initReportPrefix("CreateAccountPage");
 		topMainMenu = PageFactory.initElements(driver, TopMainMenu.class);
 	}
 
 	/************************* Actions ***************************/
 
 	public ProfilePage signUp(GeneratedUser user)
 	{
 		type("user name", fieldUserName, user.getFirstName());
 		type("user email", fieldUserEmail, user.getEmail());
 		type("user password", fieldUserPassword, user.getPassword());
 		click("btn Continue", btnContinue);
 		return PageFactory.initElements(driver, ProfilePage.class);
 	}
 
 	/************************* GET/SET methods ***********************/
 
 	public TopMainMenu getTopMainMenu()
 	{
 		return topMainMenu;
 	}
 
 	public void setTopMainMenu(TopMainMenu topMainMenu)
 	{
 		this.topMainMenu = topMainMenu;
 	}
 }
