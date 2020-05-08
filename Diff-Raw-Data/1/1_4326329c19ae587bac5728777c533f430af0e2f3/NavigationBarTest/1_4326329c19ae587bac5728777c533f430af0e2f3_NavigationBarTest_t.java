 import org.junit.Test;
 import org.openqa.selenium.By;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class NavigationBarTest extends InsertClass{
 
     private String URLhomepage = "http://10.10.4.121:8080/Donor-Connect-App/home";
     private String URLprojectpage ="http://10.10.4.121:8080/Donor-Connect-App/projects";
     @Test
     public void checkLogoPicOnHomePage() {
         webDriver.get(URLhomepage);
         assertThat((webDriver.findElement(By.xpath("//div[@class='col6']/a/img[@class='logopic']")).isDisplayed()),is(true));
     }
 
     @Test
     public void checkLogoOnHomePage() {
         webDriver.get(URLhomepage);
         assertThat((webDriver.findElement(By.xpath("//div[@class='col6']/a/p[@class='logo']")).isDisplayed()),is(true));
     }
 
     @Test
     public void checkLogoOnHomePageRedirectsToHomePage() {
         webDriver.get(URLhomepage);
         webDriver.findElement(By.xpath("//div[@class='col6']/a/p[@class='logo']")).click();
         assertThat(webDriver.getCurrentUrl(),is(URLhomepage));
     }
 
     @Test
     public void checkLogoPicOnHomePageRedirectsToHomePage() {
         webDriver.get(URLhomepage);
         webDriver.findElement(By.xpath("//div[@class='col6']/a/img[@class='logopic']")).click();
         assertThat(webDriver.getCurrentUrl(),is(URLhomepage));
     }
 
     @Test
     public void homeLinkOnHomePageRedirectsToHomePage() {
         webDriver.get(URLhomepage);
         webDriver.findElement(By.xpath("//nav[@class='menubar']/ul/li/a[@href='/Donor-Connect-App/home']")).click();
         assertThat(webDriver.getCurrentUrl(),is(URLhomepage));
     }
 
     @Test
     public void projectsLinkOnHomePageRedirectsToAllProjectsPage() {
         webDriver.get(URLhomepage);
         webDriver.findElement(By.xpath("//nav[@class='menubar']/ul/li/a[@href='/Donor-Connect-App/projects']")).click();
         assertThat(webDriver.getCurrentUrl(),is(URLprojectpage));
     }
 
     @Test
     public void homeLinkOnAllProjectPageRedirectsToHomePage() {
         webDriver.get(URLprojectpage);
         webDriver.findElement(By.xpath("//nav[@class='menubar']/ul/li/a[@href='/Donor-Connect-App/home']")).click();
         assertThat(webDriver.getCurrentUrl(),is(URLhomepage));
     }
 
     @Test
     public void projectsLinkOnAllProjectPageRedirectsToAllProjectsPage() {
         webDriver.get(URLprojectpage);
         webDriver.findElement(By.xpath("//nav[@class='menubar']/ul/li/a[@href='/Donor-Connect-App/projects']")).click();
         assertThat(webDriver.getCurrentUrl(),is(URLprojectpage));
     }
 
     @Test
     public void homeLinkOnProjectPageRedirectstoHomePage() {
         String project_id = insertDataForCurrentProject("Books for children","This is project for children who want to study.So please help by donating for their books","image/children.jpg","image/children_thumbnail.png","Donate for kids","2012-12-12","20000");
         webDriver.get("http://10.10.4.121:8080/Donor-Connect-App/project?id=" + project_id);
         webDriver.findElement(By.xpath("//nav[@class='menubar']/ul/li/a[@href='/Donor-Connect-App/home']")).click();
        waitForElementToLoad(webDriver,By.className("welcomeText") );
         assertThat(webDriver.getCurrentUrl(),is(URLhomepage));
     }
 
 }
