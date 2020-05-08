 import models.Transaction;
 import models.TransactionTag;
 import models.TransactionType;
 import org.junit.Test;
 import play.test.UnitTest;
 
 public class ImportTest extends UnitTest {
 
     @Test
     public void testImportTransaction() {
         assertTrue(Transaction.count() > 0);
     }
 
     @Test
     public void testImportTransactionTag() {
         assertTrue(TransactionTag.count() > 0);
     }
 
     @Test
     public void testImportTransactionType() {
         assertTrue(TransactionType.count() > 0);
     }
 }
