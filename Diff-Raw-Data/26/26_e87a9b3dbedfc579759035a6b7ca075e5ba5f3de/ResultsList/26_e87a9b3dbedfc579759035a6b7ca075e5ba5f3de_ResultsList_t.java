 package ua.com.mcgray.bdd.tools.pages;
 
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 import ru.yandex.qatools.htmlelements.annotations.Block;
 import ru.yandex.qatools.htmlelements.annotations.Name;
 import ru.yandex.qatools.htmlelements.element.HtmlElement;
 import ru.yandex.qatools.htmlelements.element.Link;
 
 /**
  * @author orezchykov
  * @since 01.03.13
  */
 @Name("Search result list placeholder")
 @Block(@FindBy(css = "div.main-content"))
 public class ResultsList extends HtmlElement {
 
     @Name("Users list")
    @FindBy(css = "div.user-list")
     private WebElement usersList;
 
     @Name("Profile Link")
    @FindBy(css = "div.user-list div.user-list-item:nth-child(1) a.gravatar")
     private Link profileLink;
 
     public String get_list_text() {
         return usersList.getText();
     }
 
     public void click_on_profile() {
         profileLink.click();
     }
 }
