 package pl.itcrowd.summer_code.test;
 
 import org.jboss.arquillian.graphene.context.GrapheneContext;
 import org.jboss.arquillian.graphene.enricher.findby.FindBy;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.interactions.Actions;
 
 import static org.jboss.arquillian.graphene.Graphene.guardHttp;
 import static org.jboss.arquillian.graphene.Graphene.waitGui;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Wybraniec
  * Date: 04.07.13
  * Time: 08:23
  * To change this template use File | Settings | File Templates.
  */
 
 public class Header {
 
     @FindBy(css = "div.row-fluid.logoWrapper")
     private WebElement vopLogo;
 
     @FindBy(css= "div.span12 div:nth-of-type(2) ul.nav > li:nth-of-type(1) > a")
     private WebElement psychicsButton;
 
     @FindBy(css= "div.span12 div:nth-of-type(2) ul.nav > li:nth-of-type(3) > a")
     private WebElement marketplaceButton;
 
    @FindBy(css= "div.span12 div:nth-of-type(2) ul.nav > li:nth-of-type(5) > a")
     private WebElement blogButton;
 
     @FindBy(css= "div.span12 div:nth-of-type(2) ul.nav > li:nth-of-type(7) > a")
     private WebElement myaccountButton;
 
     @FindBy(className = "searchTemplate")
     private WebElement searchInput;
 
     @FindBy(id = "searchBtn")
     private WebElement searchConfirmButton;
 
     @FindBy(css= "div.span12 div:nth-of-type(1) ul.nav > li:nth-of-type(1) > a")
     private WebElement loginButton;
 
     @FindBy(css= "div.span12 div:nth-of-type(1) ul.nav > li:nth-of-type(2) > a")
     private WebElement signupButton;
 
     @FindBy(css= "div.span12 div:nth-of-type(1) ul.nav > li:nth-of-type(4) > a")
     private WebElement shoppingcartButton;
 
     @FindBy(css = ".languageMenu")
     private WebElement languageMenu;
 
     @FindBy(css = "ul li:nth-of-type(9) ul li:nth-of-type(1) a")
     private WebElement languageMenuEnglish;
 
     @FindBy(css = "ul li:nth-of-type(9) ul li:nth-of-type(2) a")
     private WebElement languageMenuPolish;
 
     @FindBy(css = "div.span12 div:nth-of-type(1) ul.nav > li:nth-of-type(4) > a")
     private WebElement cartAmount;
 
     @FindBy(css = "[id = 'row_0'] div:nth-of-type(1) div a img")
     private WebElement itemToBuy;
 
     @FindBy(css = "[id = 'f'] div a")
     private WebElement itemToAddToCart;
 
     public void itemToAddToCartClick(){
         guardHttp(itemToAddToCart).click();
     }
     public void itemToBuyClick(){
         guardHttp(itemToBuy).click();
     }
     public void vopLogoClick(){
         guardHttp(vopLogo).click();
     }
     public void psychicsButtonClick(){
         guardHttp(psychicsButton).click();
     }
     public void marketplaceButtonClick(){
         guardHttp(marketplaceButton).click();
     }
     public void blogButtonClick(){
         guardHttp(blogButton).click();
     }
     public void myaccountButtonClick(){
         guardHttp(myaccountButton).click();
     }
     public void searchConfirmButtonClick(){
         guardHttp(searchConfirmButton).click();
     }
     public void loginButtonClick(){
         guardHttp(loginButton).click();
     }
     public void signUpButtonClick(){
         guardHttp(signupButton).click();
     }
     public void shoppingCartButtonClick(){
         guardHttp(shoppingcartButton).click();
     }
     public void englishMenuClick(){
         new Actions(GrapheneContext.getProxy()).moveToElement(languageMenu).build().perform();
         waitGui().until().element(languageMenuEnglish).is().visible();
         guardHttp(languageMenuEnglish).click();
     }
     public void polishMenuClick(){
         new Actions(GrapheneContext.getProxy()).moveToElement(languageMenu).build().perform();
         waitGui().until().element(languageMenuPolish).is().visible();
         guardHttp(languageMenuPolish).click();
     }
 
     //Setters
     public void setSearchInput(String string) {
         searchInput.clear();
         searchInput.sendKeys(string);
     }
 
     //Getters
     public WebElement getVopLogo() {
         return vopLogo;
     }
 
     public WebElement getPsychicsButton() {
         return psychicsButton;
     }
 
     public WebElement getMarketplaceButton() {
         return marketplaceButton;
     }
 
     public WebElement getBlogButton() {
         return blogButton;
     }
 
     public WebElement getMyaccountButton() {
         return myaccountButton;
     }
 
     public WebElement getSearchInput() {
         return searchInput;
     }
 
     public WebElement getSearchConfirmButton() {
         return searchConfirmButton;
     }
 
     public WebElement getLoginButton() {
         return loginButton;
     }
 
     public WebElement getSignupButton() {
         return signupButton;
     }
 
     public WebElement getShoppingcartButton() {
         return shoppingcartButton;
     }
 
     public WebElement getLanguageMenuEnglish(){
         return  languageMenuEnglish;
     }
 
     public WebElement getLanguageMenuPolish(){
         return languageMenuPolish;
     }
 
     public WebElement getCartAmount() {
         return cartAmount;
     }
 
     public WebElement getItemToBuy() {
         return itemToBuy;
     }
 
     public WebElement getItemToAddToCart() {
         return itemToAddToCart;
     }
 }
