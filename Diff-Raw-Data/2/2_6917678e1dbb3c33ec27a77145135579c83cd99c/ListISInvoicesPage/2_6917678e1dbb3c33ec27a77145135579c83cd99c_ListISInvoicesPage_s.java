 package pages;
 
 import forms.InvoiceForm;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.Map;
 
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 public class ListISInvoicesPage extends BasePage {
 
     protected ListISInvoicesPage(WebDriver driver) {
         super(driver);
         assertThat(driver.getTitle(), equalTo("List Invoices | Kabbadi"));
     }
 
     public ListISInvoicesPage viewInList(InvoiceForm invoiceForm) {
         Map<String, Object> fields = invoiceForm.getFields();
         WebElement tableRow = driver.findElement(By.id("is_invoice_" + fields.get("invoiceNumber")));
         assertThat(tableRow.getText(), containsString(fields.get("invoiceNumber") + ""));
         return this;
     }
 
     public AddAssetPage addANewAsset(String invoiceNumber) {
         WebElement tableRow = driver.findElement(By.id("is_invoice_" + invoiceNumber));
        tableRow.findElement(By.linkText("Add Assets")).click();
         return new AddAssetPage(driver);
     }
 
     public ViewAssetDetailsPage viewAssetDetailsPage(String invoiceNumber) {
         WebElement tableRow = driver.findElement(By.cssSelector("tr#is_invoice_" + invoiceNumber + " + tr"));
         tableRow.findElement(By.linkText("VIEW")).click();
         return new ViewAssetDetailsPage(driver);
     }
 }
