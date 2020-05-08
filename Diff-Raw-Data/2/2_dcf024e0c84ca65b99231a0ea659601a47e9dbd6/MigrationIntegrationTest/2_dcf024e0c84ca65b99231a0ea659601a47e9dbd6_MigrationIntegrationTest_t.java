 package migration;
 
 import integration.IntegrationTest;
 import kabbadi.domain.Invoice;
 import kabbadi.domain.db.GenericRepository;
 import kabbadi.migration.ColumnMapper;
 import kabbadi.migration.InvoiceCreator;
 import kabbadi.migration.SQLGenerator;
 import kabbadi.service.InvoiceService;
 import org.hibernate.SessionFactory;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.io.IOException;
 import java.util.*;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 public class MigrationIntegrationTest extends IntegrationTest{
 
     @Autowired
     private SessionFactory sessionFactory;
 
     @Autowired
     private InvoiceService invoiceService;
     @Autowired
     private GenericRepository<Invoice> invoiceRepository;
 
     @Test
     public void should_insert_single_invoice_record_from_finance_and_admin_with_same_invoice_number() throws IOException {
         String[] adminTestData = {
           "1234",
            "2000",
            "goods"
         };
         String[] financeTestData = {
           "1234",
            "23",
            "CHENNAI",
            "2345"
         };     
         String[] adminHeaders = {
           "invoiceNumber",
            "bondNumber",
             "descriptionOfGoods"    
         };
         String[] financeHeaders={
             "invoiceNumber",
              "quantity",
               "location",
                "april-10"
         };
         List<String[]> adminListOfTestData = new ArrayList<String[]>();
         List<String[]> financeListOfTestData = new ArrayList<String[]>();
         adminListOfTestData.add(adminTestData);
         financeListOfTestData.add(financeTestData);
         List<Map<String, String>> adminMappedEntries = null;
         List<Map<String, String>> financeMappedEntries = null;
         try {
             adminMappedEntries = new ColumnMapper(adminHeaders, adminListOfTestData).mappedList();
             financeMappedEntries = new ColumnMapper(financeHeaders, financeListOfTestData).mappedList();
         } catch (Exception e) {
             e.printStackTrace();
         }
         List<Map<String, String>>combinedEntries = new InvoiceCreator(adminMappedEntries, financeMappedEntries).createJoinEntry();
         List<String> insertStatements =  new SQLGenerator(combinedEntries).createInsertStatements();
        sessionFactory.getCurrentSession().createSQLQuery("delete from asset; delete from invoice;").executeUpdate();
         for(String insertStatement: insertStatements){
             sessionFactory.getCurrentSession().createSQLQuery(insertStatement).executeUpdate();
         }
        Invoice invoice = invoiceRepository.get(0);
         String[] expectedValue = {
           "1234", "2000", "goods", "23", "CHENNAI"
         };
         String[] actualData = {
           invoice.getInvoiceNumber(),
           invoice.getBondNumber(),
           invoice.getDescriptionOfGoods(),
           invoice.getQuantity().toString(),
           invoice.getLocation().toString()
         };
         assertThat(actualData, equalTo(expectedValue));
     }
 }
