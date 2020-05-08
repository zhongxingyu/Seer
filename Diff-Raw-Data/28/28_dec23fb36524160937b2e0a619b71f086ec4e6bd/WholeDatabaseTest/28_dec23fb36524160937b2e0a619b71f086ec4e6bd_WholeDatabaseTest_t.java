 package org.tmcdb.test;
 
 import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
 import junit.framework.Assert;
 import org.jetbrains.annotations.NotNull;
 import org.junit.After;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.tmcdb.Database;
 import org.tmcdb.utils.TestUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.List;
 
 import static org.tmcdb.utils.TestUtils.cleanDirectory;
 
 /**
  * @author Pavel Talanov
  */
 public class WholeDatabaseTest {
 
     private static final String SANDBOX = "testData/integration/sandbox";
 
     private static final String CASES = "testData/integration/cases";
 
 
     @Test
     public void simpleCreateTable() throws Exception {
         doTest("simpleCreate");
     }
 
     @Test
     public void createAndInsert() throws Exception {
         doTest("createAndInsert");
     }
 
     @Test
     public void createInsertAndSelect() throws Exception {
         doTest("createInsertAndSelect");
     }
 
    @Test
    public void example() throws Exception {
        doTest("example");
    }

    @Test
    public void persistingData() throws Exception {
        doTest("persistingData_1");
        doTest("persistingData_2");
    }

     private void doTest(@NotNull String testName) throws IOException {
         ByteOutputStream out = new ByteOutputStream();
         List<String> queries = TestUtils.readFileIntoList(CASES + "/" + testName + "/in.txt");
         Database db = new Database(new File(SANDBOX), new PrintStream(out));
         try {
             db.initialize();
             for (String query : queries) {
                 db.exec(query);
             }
             Assert.assertEquals(TestUtils.readFileIntoString(CASES + "/" + testName + "/out.txt"), out.toString());
         } finally {
             db.deinitialize();
         }
     }
 
     @BeforeClass
     public static void setUp() throws Exception {
         //noinspection ResultOfMethodCallIgnored
         new File(SANDBOX).mkdirs();
     }
 
     @After
     public void cleanup() throws IOException {
         cleanDirectory(SANDBOX);
     }
 }
