 package test;


import org.junit.Ignore;
 import org.junit.Test;
 
 public class AdminAddInvoicePageTest extends BaseTest{
    @Test
<<<<<<< HEAD
    @Ignore
    public void should_stay_on_admin_add_invoice() {
//        launchKabbadi()
//                .loginWithValidCredentials()
//                .goToAdminAddInvoicePage()
//                .invalidBlankInvoiceNumber();
    }
 
     @Test
     public void should_successfully_add_invoice_with_valid_data() {
         launchKabbadi()
                 .loginWithValidCredentials()
                 .goToAdminAddInvoicePage();
//                .shouldSubmitValidData();
     }
 
     @Test
 	public void should_fail_to_add_invoice_with_invalid_data() {
 		launchKabbadi()
         .loginWithValidCredentials()
        .goToAdminAddInvoicePage()
        .invalidBlankInvoiceNumber();
 	}








 }
