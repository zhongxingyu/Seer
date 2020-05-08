 package com.bitclean.billscrape.eonenergy;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.lang.StringUtils;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import com.bitclean.billscrape.Scraper;
 import com.bitclean.billscrape.ScraperDefinition;
 import com.bitclean.billscrape.utils.IOUtils;
 import com.bitclean.billscrape.utils.MyHtmlUnitDriver;
 
 public class EonScraper implements Scraper {
   private final Options config_;
 
   public EonScraper(final Options options) {
     config_ = options;
   }
 
   public void run() {
     WebDriver driver = new MyHtmlUnitDriver();
 
     driver.get("http://www.eonenergy.com");
 
     LoginPage page = new FrontPage(driver).login();
     YourAccountPage accountPage = page.login(config_.getUsername(), config_.getPassword());
 
     BillsPage billsPage = accountPage.history().bills();
     billsPage.getBills();
 
 
   }
 
   public static class Options implements ScraperDefinition {
     boolean overwrite = false;
 
     private File baseDir = new File("/tmp");
 
     private String filenamePattern = "{0}-{1}.pdf";
 
     boolean verbose = false;
 
     private String password_;
 
     private String username_;
 
     boolean quiet;
 
     public Scraper getInstance() {
 
       if (StringUtils.isEmpty(password_)) {
         System.err.println("Missing password.");
         throw new IllegalArgumentException("Missing password");
       }
       if (StringUtils.isEmpty(username_)) {
         System.err.println("Missing username.");
         throw new IllegalArgumentException("Missing username");
       }
 
       return new EonScraper(this);
     }
 
     public String getUsername() {
       return username_;
     }
 
     public String getPassword() {
       return password_;
     }
 
     public void setPassword(final String password) {
       password_ = password;
     }
 
     public void setUsername(final String username) {
       username_ = username;
     }
 
     private File getFile(final Date date, final String accountNumber) {
       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
 
       return new File(baseDir, MessageFormat.format(filenamePattern, accountNumber, dateFormat.format(date)));
     }
 
     public void verboseLog(final String message) {
       if (verbose) {
         System.err.println(message);
       }
     }
 
     public void log(final String message) {
       if (!quiet) {
         System.out.println(message);
       }
     }
   }
 
   private class FrontPage extends PageObject {
     public FrontPage(final WebDriver driver) {
       super(driver);
     }
 
     public LoginPage login() {
       driver.findElement(By.linkText("Login")).click();
       return new LoginPage(driver);
     }
   }
 
   private class LoginPage extends PageObject {
     public LoginPage(final WebDriver driver) {
       super(driver);
     }
 
     public YourAccountPage login(final String username, final String password) {
       final WebElement login = driver.findElement(By.id("loginPage"));
       final List<WebElement> inputs = login.findElements(By.tagName("input"));
       WebElement submit = null;
       for (WebElement input : inputs) {
         if ("text".equals(input.getAttribute("type"))) {
           if (input.getAttribute("id").endsWith("UserName")) {
             input.sendKeys(username);
           }
         }
         else if ("password".equals(input.getAttribute("type"))) {
           if (input.getAttribute("id").endsWith("Password")) {
             input.sendKeys(password);
           }
         }
         else if ("submit".equals(input.getAttribute("type"))) {
           if (input.getAttribute("id").endsWith("Login")) {
             submit = input;
           }
         }
       }
       if (submit != null) {
         submit.click();
         return new YourAccountPage(driver);
       }
       else {
         throw new IllegalStateException("Can't find submit button on login form.");
       }
     }
   }
 
   private class YourAccountPage extends PageObject {
     public YourAccountPage(final WebDriver driver) {
       super(driver);
     }
 
     public AccountHistoryPage history() {
       driver.findElement(By.linkText("Account History")).click();
       return new AccountHistoryPage(driver);
     }
   }
 
   private class AccountHistoryPage extends PageObject {
     public AccountHistoryPage(final WebDriver driver) {
       super(driver);
     }
 
     public BillsPage bills() {
       final List<WebElement> buttons = driver.findElement(By.id("contentArea")).findElements(By.tagName("input"));
       for (WebElement button : buttons) {
         if ("Bills".equalsIgnoreCase(button.getAttribute("value"))) {
           button.click();
           break;
         }
       }
       return new BillsPage(driver);
     }
   }
 
   private class BillsPage extends PageObject {
     public BillsPage(final WebDriver driver) {
       super(driver);
     }
 
     public void getBills() {
       getAllBills();
 
 
     }
 
     SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy", Locale.ENGLISH);
 
     public void getAllBills() {
 
       final List<WebElement> bills = getBillElements();
       // Then we download each one.
       for (int i = 0; i < bills.size(); i++) {
         downloadBillOnPage(i);
       }
     }
 
     private void downloadBillOnPage(final int offset) {
       final List<WebElement> bills = getBillElements();
       downloadBill(bills.get(offset));
     }
 
     private void downloadBill(final WebElement row) {
       final String accountNumber = driver.findElement(By.id("AccountHistory")).findElement(By.tagName("span")).getText();
       config_.verboseLog("Account number: " + accountNumber);
       String dateStr = row.findElement(By.className("Date")).getText();
       Date date = null;
       try {
         date = dateFormat.parse(dateStr);
       }
       catch (ParseException e) {
         return;
       }
       final File saveFile = config_.getFile(date, accountNumber);
       if (saveFile.exists() && !config_.overwrite) {
         config_.log("File exists, skipping " + saveFile);
         return;
       }
       final WebElement input = row.findElement(By.className("Type")).findElement(By.tagName("input"));
 
       input.click();  // On statement page
       driver.findElement(By.xpath("//input[@value='View Paper Bill']")).click(); // On page with embedded iframe.
 
       final WebElement iframe = driver.findElement(By.tagName("iframe"));
 
 
       try {
         final String pdfUrl = iframe.getAttribute("src");
         config_.verboseLog("PDF URL: " + pdfUrl);
         config_.log("Saving to: " + saveFile);
         IOUtils.saveUrl(saveFile, pdfUrl, (MyHtmlUnitDriver) driver);
       }
       catch (IOException e) {
         config_.log("Unable to save pdf");
       }
 
       driver.navigate().back();
       config_.verboseLog("Back to: " + driver.getTitle() + " ; " + driver.getCurrentUrl());
     }
 
 
     private List<WebElement> getBillElements() {
       final List<WebElement> tables = driver.findElements(By.tagName("table"));
       for (WebElement table : tables) {
         if (table.getAttribute("id").endsWith("PaymentGrid1")) {
           return table.findElements(By.xpath("//tr/td[@class='Date']/.."));
         }
       }
       return Collections.emptyList();
     }
   }
 
   public class PageObject {
     WebDriver driver;
 
     public PageObject(final WebDriver driver) {
       this.driver = driver;
       config_.verboseLog("On page: " + driver.getTitle());
     }
   }
 
 
 }
