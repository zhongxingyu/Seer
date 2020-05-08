 package domain;
 
 import domain.builder.InvoiceTestBuilder;
 import integration.IntegrationTest;
 import kabbadi.domain.Invoice;
 import kabbadi.domain.Location;
import kabbadi.domain.Money;
 import kabbadi.domain.RunningBalanceCalculator;
 import kabbadi.service.InvoiceService;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.math.BigDecimal;
 import java.util.Arrays;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 public class RunningBalanceCalculatorTest extends IntegrationTest {
 
     @Autowired
     InvoiceService invoiceService;
 
     @Test
     public void should_calculate_the_running_balance() {
         Invoice oldInvoice = invoiceWith("11/98-99", 123);
         invoiceService.saveOrUpdate(oldInvoice);
 
         invoiceService.saveOrUpdate(invoiceWith("12/98-99", 123));
         invoiceService.saveOrUpdate(invoiceWith("13/98-99", 123));
 
         Invoice newInvoice = invoiceWith("14/98-99", 123);
         invoiceService.saveOrUpdate(newInvoice);
 
         BigDecimal value = new RunningBalanceCalculator(invoiceService).calculateStartingFrom(newInvoice);
         assertEquals(new BigDecimal(492), value);
 
         oldInvoice.setCgApprovedInINR(null);
         invoiceService.saveOrUpdate(oldInvoice);
 
         BigDecimal value1 = new RunningBalanceCalculator(invoiceService).calculateStartingFrom(newInvoice);
         assertEquals(new BigDecimal(492 - 123), value1);
 
         oldInvoice.setCgApprovedInINR(new BigDecimal(0));
         invoiceService.saveOrUpdate(oldInvoice);
 
         BigDecimal value2 = new RunningBalanceCalculator(invoiceService).calculateStartingFrom(newInvoice);
         assertEquals(new BigDecimal(492 - 123), value2);
 
        oldInvoice.setCIFValueInINR(new Money("INR", new BigDecimal(1000)));
         invoiceService.saveOrUpdate(oldInvoice);
 
         BigDecimal value3 = new RunningBalanceCalculator(invoiceService).calculateStartingFrom(newInvoice);
         assertEquals(new BigDecimal(492 - 123 - 1000), value3);
 
     }
 
     @Test
     public void should_inject_itself_on_a_invoice() {
         Invoice invoice = new Invoice();
         new RunningBalanceCalculator(invoiceService).injectInto(invoice);
 
         assertThat(invoice.getRunningBalanceCalculator(), is(not(nullValue())));
 
         new RunningBalanceCalculator(invoiceService).injectInto((Invoice) null);
     }
 
 
     @Test
     public void should_inject_itself_on_a_invoice_list() {
         Invoice invoice = new Invoice();
         new RunningBalanceCalculator(invoiceService).injectInto(Arrays.asList(invoice));
 
         assertThat(invoice.getRunningBalanceCalculator(), is(not(nullValue())));
 
         new RunningBalanceCalculator(invoiceService).injectInto((List<Invoice>) null);
     }
 
     private Invoice invoiceWith(String invoiceNumber, int amount) {
         return new InvoiceTestBuilder()
                 .withInvoiceNumber(invoiceNumber)
                 .withBondNumber(invoiceNumber)
                 .withCGApprovedInINR(amount)
                 .withLocation(Location.BANGALORE)
                 .build();
     }
 }
