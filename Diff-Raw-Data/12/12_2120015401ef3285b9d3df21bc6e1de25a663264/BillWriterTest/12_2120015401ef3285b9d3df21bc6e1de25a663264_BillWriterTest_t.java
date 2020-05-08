 package org.uva.training.io.writer;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.Collections;
 
 import org.apache.commons.lang.SystemUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.uva.training.entity.Bill;
 import org.uva.training.entity.Item;
 import org.uva.training.entity.Product;
 import org.uva.training.entity.Type;
 import org.uva.training.tax.TaxParser;
 
 @RunWith(JUnit4.class)
 public class BillWriterTest {
    private File file;
 
    @Before
    public void before() {
       file = new File(SystemUtils.getJavaIoTmpDir(), "bill.txt");
    }
 
    @After
    public void after() {
      file.delete();
    }
 
    @Test
    public void testWrite() {
       Item item = new Item(new Product(Type.CD, "CD", true), 1, 12.49f);
       new TaxParser().parse(item);
 
       Bill bill = new Bill(Collections.singletonList(item));
       BillWriter billWriter = new BillWriter();
       billWriter.write(bill, file);
       assertTrue(file.length() > 0);
    }
 }
