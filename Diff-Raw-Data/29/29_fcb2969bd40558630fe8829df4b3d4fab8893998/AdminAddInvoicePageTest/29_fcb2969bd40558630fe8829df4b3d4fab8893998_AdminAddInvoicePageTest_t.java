 package test;
 import org.junit.Test;
 
 public class AdminAddInvoicePageTest extends BaseTest{
 
     @Test
     public void should_successfully_add_invoice_with_valid_data() {
         launchKabbadi()
                 .loginWithValidCredentials()
                 .goToAdminAddInvoicePage();
     }
 
     @Test
 	public void should_fail_to_add_invoice_with_invalid_data() {
 		launchKabbadi()
         .loginWithValidCredentials()
        .goToAdminAddInvoicePage();
//        .invalidBlankInvoiceNumber();
 	}
 }
