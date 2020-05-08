 package pages;
 
 import config.Configuration;
 import forms.InvoiceForm;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.Map;
 
 public class BasePage {
     protected WebDriver driver;
 
     protected BasePage(WebDriver driver) {
         this.driver = driver;
     }
 
     public BasePage goToGoogle() {
         driver.get("http://www.google.com");
         return new BasePage(driver);
     }
 
     public ListAdminInvoicesPage returnToKabbadi() {
         driver.get(Configuration.KABBADI_URL);
         return new ListAdminInvoicesPage(driver);
     }
 
     public String getTitle(){
         return  driver.getTitle();
     }
 
     protected void fillFields(String fieldName, String fieldValue) {
         WebElement field = driver.findElement(By.name(fieldName));
        if (field.getTagName().contains("option"))
            driver.findElement(By.id(fieldName)).findElement(By.name(fieldValue)).submit();
         else
             field.sendKeys(fieldValue);
     }
 
     protected void fillFormWith(InvoiceForm invoiceForm) {
         Map<String, Object> fields = invoiceForm.getFields();
         for (String fieldName : fields.keySet()) {
             fillFields(fieldName, (fields.get(fieldName) + ""));
         }
         driver.findElement(By.cssSelector("input[name=submit]")).click();
     }
 }
