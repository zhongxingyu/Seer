 package pages;
 
 import forms.InvoiceForm;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.Map;
 
 import static junit.framework.Assert.assertTrue;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
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
 
     public FinanceListInvoicePage goToFinanceInvoiceListPage() {
         driver.findElement(By.linkText("Finance")).click();
         return new FinanceListInvoicePage(driver);
     }
 
     public ListInvoicePage confirmFirstInvoiceNumberIs(String newInvoiceNumber) {
         return confirmFirstInvoiceContainsValue(newInvoiceNumber);
     }
 
     public ListInvoicePage confirmFirstPONumberIs(String purchaseOrder) {
         return confirmFirstInvoiceContainsValue(purchaseOrder);
     }
 
     public ViewSingleInvoiceDetailPage selectViewDetailsPageForInvoice(String invoiceNumber) {
         WebElement id = driver.findElement(By.id("admin_invoice_" + invoiceNumber));
        id.findElement(By.linkText("VIEW DETAILS")).click();
         return new ViewSingleInvoiceDetailPage(driver);
     }
 
     private ListInvoicePage confirmFirstInvoiceContainsValue(String fieldValue) {
         String rowData = driver.findElement(By.id("invoices")).findElements(By.tagName("tr")).get(1).getText();
         assertThat(rowData, containsString(fieldValue));
         return this;
     }
 }
