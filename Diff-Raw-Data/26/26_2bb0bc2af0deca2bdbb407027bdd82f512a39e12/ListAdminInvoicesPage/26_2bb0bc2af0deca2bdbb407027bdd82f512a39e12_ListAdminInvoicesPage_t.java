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
 
 public class ListAdminInvoicesPage extends BasePage {
     public ListAdminInvoicesPage(WebDriver driver) {
         super(driver);
 
         assertThat(driver.getTitle(), equalTo("List Invoices | Kabbadi"));
     }
 
     public EditInvoicePage editFirstInvoice() {
         driver.findElement(By.id("invoices")).findElements(By.tagName("tr")).get(1)
                 .findElement(By.linkText("EDIT")).click();
         return new EditInvoicePage(driver);
     }
 
     public ListAdminInvoicesPage confirmFirstPONumberIs(String purchaseOrder) {
 
         String rowData = driver.findElement(By.id("invoices")).findElements(By.tagName("tr")).get(1).getText();
         assertThat(rowData, containsString(purchaseOrder));
         return this;
     }
 
     public AddInvoicePage goToAdminAddInvoicePage() {
         return addInvoiceOf("admin");
     }
 
     public ListAdminInvoicesPage confirmInvoiceHasBeenAddedToAdminList(InvoiceForm invoiceForm) {
         Map<String, String> fields = invoiceForm.getFields();
         WebElement tableRow = driver.findElement(By.id("admin_invoice_" + fields.get("invoiceNumber")));
 
         for (String fieldValue : fields.values()) {
             assertThat(tableRow.getText(), containsString(fieldValue));
         }
 
         return this;
     }
 
     public ViewSingleInvoiceDetailPage viewFirstInvoiceDetails() {
         driver.findElement(By.linkText("VIEW")).click();
         return new ViewSingleInvoiceDetailPage(driver);
     }
 
     public ListFinanceInvoicesPage goToFinanceInvoiceListPage() {
         driver.findElement(By.linkText("Finance")).click();
         return new ListFinanceInvoicesPage(driver);
     }
 
     public ListAdminInvoicesPage confirmFirstInvoiceNumberIs(String newInvoiceNumber) {
         return confirmFirstInvoiceContainsValue(newInvoiceNumber);
     }
 
     public AddInvoicePage goToFinanceAddInvoicePage() {
         return addInvoiceOf("finance");
     }
 
     private AddInvoicePage addInvoiceOf(String role) {
         driver.findElement(By.cssSelector("a[href='#" + role + "']")).click();
         driver.findElement(By.id(role +"_add_invoice")).click();
        assertThat(driver.getTitle(), containsString("Add/Edit invoice"));
         return new AddInvoicePage(driver);
     }
 
     public ViewSingleInvoiceDetailPage selectViewDetailsPageForInvoice(String invoiceNumber) {
         WebElement id = driver.findElement(By.id("admin_invoice_" + invoiceNumber));
         id.findElement(By.linkText("VIEW")).click();
         return new ViewSingleInvoiceDetailPage(driver);
     }
 
     private ListAdminInvoicesPage confirmFirstInvoiceContainsValue(String fieldValue) {
         String rowData = driver.findElement(By.id("invoices")).findElements(By.tagName("tr")).get(1).getText();
         assertThat(rowData, containsString(fieldValue));
         return this;
     }
 
     public ListAdminInvoicesPage financeTabShouldBeActive() {
         assertThat(driver.findElement(By.id("finance")).getAttribute("class"), containsString("active"));
         return this;
     }
 }
