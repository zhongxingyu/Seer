 package pages;
 
import forms.InvoiceForm;
 import org.hamcrest.CoreMatchers;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.Map;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 public class ListInvoicePage extends BasePage {
     public ListInvoicePage(WebDriver driver) {
         super(driver);
 
         assertThat(driver.getTitle(), equalTo("List Invoices | Kabbadi"));
     }
 
     public EditInvoicePage editFirstInvoice() {
         driver.findElement(By.id("invoices")).findElements(By.tagName("tr")).get(1)
         .findElement(By.linkText("EDIT")).click();
         return new EditInvoicePage(driver);
     }
 
     public ListInvoicePage confirmFirstPONumberIs(String purchaseOrder) {
         String rowData = driver.findElement(By.id("invoices")).findElements(By.tagName("tr")).get(1).getText();
         assertThat(rowData, containsString(purchaseOrder));
         return this;
     }
 
     public AdminAddInvoicePage goToAdminAddInvoicePage() {
         driver.findElement(By.id("admin_add_invoice")).click();
         assertThat(driver.getTitle(), containsString("Add a new invoice"));
         return new AdminAddInvoicePage(driver);
     }
 
     public ListInvoicePage confirmInvoiceHasBeenAddedToAdminList(InvoiceForm invoiceForm) {
         Map<String, String> fields = invoiceForm.getFields();
         WebElement tableRow = driver.findElement(By.id("admin_invoice_" + fields.get("invoiceNumber")));
 
         for (String fieldValue : fields.values()) {
             assertThat(tableRow.getText(), containsString(fieldValue));
         }
 
         return this;
     }
 
     public ListInvoicePage confirmFirstPONumberIsNot(String purchaseOrder) {
         String rowData = driver.findElement(By.id("invoices")).findElements(By.tagName("tr")).get(1).getText();
         assertThat(rowData, not(containsString(purchaseOrder)));
         return this;
     }
 
     public FinanceListInvoicePage goToFinanceInvoiceListPage() {
         driver.findElement(By.linkText("Finance")).click();
         return new FinanceListInvoicePage(driver);
     }
 }
