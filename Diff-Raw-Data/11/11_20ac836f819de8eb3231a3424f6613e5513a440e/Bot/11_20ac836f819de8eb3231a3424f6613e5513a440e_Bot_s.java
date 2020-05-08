 package com.tvelykyy.indexator;
 
import com.thoughtworks.selenium.Selenium;
 import com.tvelykyy.indexator.model.IndexState;
 import com.tvelykyy.indexator.model.Page;
 import com.tvelykyy.indexator.service.PageService;
 import org.joda.time.LocalDate;
 import org.openqa.selenium.By;
 import org.openqa.selenium.Cookie;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 @Component
 public class Bot {
 
     @Autowired
     PageService pageService;
 
     @Value("${gogetlinks.username}")
     private String gogetlinksUsername;
 
     @Value("${gogetlinks.password}")
     private String gogetlinksPassword;
 
     private static WebDriver driver = new FirefoxDriver();
 
     public void getNotIndexedPagesFromGgl() {
         driver.get("http://gogetlinks.net/");
         WebElement usernameField = driver.findElement(By.id("login_e_mail"));
         usernameField.sendKeys(this.gogetlinksUsername);
 
         WebElement passwordField = driver.findElement(By.id("login_password"));
         passwordField.sendKeys(this.gogetlinksPassword);
 
         WebElement submitButton = driver.findElement(By.id("ok_button"));
         submitButton.submit();
 
         Cookie ck = new Cookie("in_page", "1000");
         driver.manage().addCookie(ck);
 
         driver.get("http://gogetlinks.net/web_task.php?action=view_paid&in_index=2");
 
         List<WebElement> articles = driver.findElements(By.cssSelector("tr[id^='col_row']"));
 
        LocalDate currentDate = new LocalDate();
         for (WebElement article : articles) {
             WebElement labelElement = article.findElement(By.xpath(".//label[@onmouseout and @onmouseover]"));
             WebElement urlElement = article.findElement(By.xpath(".//a[@onmouseout and @onmouseover]"));
 
             String label = labelElement.getText();
             String url = urlElement.getAttribute("href");
 
             Pattern p = Pattern.compile("[^0-9]*([0-9]+)[^0-9]*");
             Matcher matcher = p.matcher(label);
             String matched = "null";
             if (matcher.matches()) {
                 matched = matcher.group(1);
             }
 
             IndexState indexState = IndexState.getFalseIndexStateForDate(new LocalDate().minusDays(Integer.parseInt(matched)));
             Page page = new Page();
             page.setUrl(url);
             page.setTitle("No Title");
 
             pageService.createNotIndexedPage(page);
         }
 
         driver.close();
 
     }
 }
