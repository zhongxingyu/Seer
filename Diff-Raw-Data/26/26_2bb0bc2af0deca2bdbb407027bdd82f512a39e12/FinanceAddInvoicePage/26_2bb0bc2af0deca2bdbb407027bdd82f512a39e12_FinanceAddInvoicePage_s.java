 package pages;
 
 import forms.InvoiceForm;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 import java.util.Map;
 
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 public class FinanceAddInvoicePage extends BasePage{
 
     protected FinanceAddInvoicePage(WebDriver driver) {
         super(driver);
        assertThat(driver.getTitle(), containsString("Add a new invoice"));
     }
     
     public FinanceAddInvoicePage fillForm(){
         Map<String, String> fields = constructInputData();
         for (String fieldName : fields.keySet()) {
             fillFieldWith(fieldName, fields.get(fieldName));
         }
         return this;
     }
 
     private Map<String,String> constructInputData() {
         InvoiceForm invoiceForm = new InvoiceForm();
         invoiceForm.fillDateOfInvoice("18\\12\\2011");
         invoiceForm.fillPurchaseOrderNumberWith("1111111");
         invoiceForm.fillLocationWith("bangalore");
         invoiceForm.fillInvoiceNumberWith("111111");
         invoiceForm.fillSupplierNameAndAddress("xxxxxx");
         invoiceForm.fillOpeningPurchaseValueAsOnApril01("123445");
         invoiceForm.fillAdditionsDuringTheYear("12222");
         invoiceForm.fillDeletionsDuringTheYear("123445");
         invoiceForm.fillDateOfCommissioning("18\\12\\2012");
         invoiceForm.fillGroupOfAssets("sssss");
         invoiceForm.fillCostCentre("skjj");
         return invoiceForm.getFields();
     }
 
     private void fillFieldWith(String fieldName, String fieldValue) {
         driver.findElement(By.cssSelector("input[name=" + fieldName + "]")).sendKeys(fieldValue);
     }
 
     public ListAdminInvoicesPage submit() {
 
         driver.findElement(By.cssSelector("input[name=submit]")).click();
         return new ListAdminInvoicesPage(driver);
     }
 
 }
