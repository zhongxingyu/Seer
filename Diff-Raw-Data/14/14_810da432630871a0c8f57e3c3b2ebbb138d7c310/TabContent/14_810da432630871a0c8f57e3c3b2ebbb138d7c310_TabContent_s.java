 package ua.com.mcgray.bdd.tools.pages;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 import ru.yandex.qatools.htmlelements.annotations.Block;
 import ru.yandex.qatools.htmlelements.annotations.Name;
 import ru.yandex.qatools.htmlelements.element.HtmlElement;
 
 /**
  * @author orezchykov
  * @since 01.03.13
  */
 @Name("Profile page tab content")
 @Block(@FindBy(css = "div.tab-content"))
 public class TabContent extends HtmlElement {
 
     @Name("Repo tab")
     @FindBy(css = "ul.repolist")
     private WebElement repoList;
 
 
     public String get_repositories() {
         return repoList.getText();
     }
 
     public void proceed_to_repo() {
        repoList.findElements(By.cssSelector("li:nth-child(3) h3 a")).get(0).click();
     }
 }
