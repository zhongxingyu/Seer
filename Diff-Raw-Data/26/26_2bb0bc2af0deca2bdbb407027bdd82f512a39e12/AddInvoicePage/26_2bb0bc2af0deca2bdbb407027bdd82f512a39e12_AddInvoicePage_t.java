 package pages;
 
 import forms.InvoiceForm;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 import java.util.Map;
 
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 public class AddInvoicePage extends BasePage {
     public AddInvoicePage(WebDriver driver) {
         super(driver);
        assertThat(driver.getTitle(), containsString("Add/Edit invoice"));
     }
 
     public AddInvoicePage invalidBlankInvoiceNumber() {
         driver.findElement(By.name("purchaseOrderNumber")).sendKeys("123123");
         driver.findElement(By.name("STPIApprovalNumberAndDate")).sendKeys("123123");
         driver.findElement(By.name("descriptionOfGoods")).sendKeys("bill");
         driver.findElement(By.name("currency")).sendKeys("usd");
         driver.findElement(By.name("foreignCurrency")).sendKeys("123123");
         driver.findElement(By.name("amountSTPIApproval")).sendKeys("123123");
         driver.findElement(By.name("CIFValueInINR")).sendKeys("123123");
         driver.findElement(By.name("bondNumber")).sendKeys("123123");
         driver.findElement(By.name("bondDate")).sendKeys("1/1/2012");
         driver.findElement(By.name("dateOfArrival")).sendKeys("1/1/2012");
         driver.findElement(By.name("billOfEntryNumber")).sendKeys("123123");
         driver.findElement(By.name("billOfEntryDate")).sendKeys("1/1/2012");
         driver.findElement(By.name("assessableValueInINR")).sendKeys("123123");
         driver.findElement(By.name("dutyExempt")).sendKeys("123123");
         driver.findElement(By.name("twentyFivePercentDF")).sendKeys("123123");
         driver.findElement(By.name("CGApprovedInINR")).sendKeys("123123");
         driver.findElement(By.name("dutyForgone")).sendKeys("123123");
         driver.findElement(By.name("runningBalance")).sendKeys("123123");
         driver.findElement(By.name("outrightPurchase")).sendKeys("123123");
         driver.findElement(By.name("loanBasis")).sendKeys("123123");
         driver.findElement(By.name("freeOfCharge")).sendKeys("123123");
         driver.findElement(By.name("status")).sendKeys("bill");
         driver.findElement(By.name("remarks")).sendKeys("bill");
         driver.findElement(By.name("location")).sendKeys("bill");
 
         driver.findElement(By.name("submit")).click();
 
         return new AddInvoicePage(driver);
     }
 
 
     public AddInvoicePage fillFieldWith(String fieldName, String fieldValue) {
         driver.findElement(By.cssSelector("input[name=" + fieldName + "]")).sendKeys(fieldValue);
         return this;
     }
 
     public ListAdminInvoicesPage submit(InvoiceForm invoiceForm) {
         Map<String, String> fields = invoiceForm.getFields();
         for (String fieldName : fields.keySet()) {
             fillFieldWith(fieldName, fields.get(fieldName));
         }
         driver.findElement(By.cssSelector("input[name=submit]")).click();
         return new ListAdminInvoicesPage(driver);
     }
 
     public boolean idExists(String id) {
         return driver.findElement(By.id(id)) != null;
     }
 
     public AddInvoicePage confirmAddInvoicePage() {
        assertThat(getTitle(), containsString("Add/Edit invoice"));
         return this;
     }
 
 }
 
