 package gov.usgs.cida.gdp.utilities.bean;
 
 import org.junit.AfterClass;
 import java.util.Date;
 import static org.junit.Assert.*;
 import org.junit.BeforeClass;
 
 
 import org.junit.Test;
 
 public class ErrorTest {
 
     private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ErrorTest.class);
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         log.debug("Started testing class.");
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
         log.debug("Ended testing class.");
     }
 
     @Test
     public void testInitializeWithInteger() {
         Error errBean = new Error(Integer.valueOf(0));
         assertNotNull(errBean.getErrorMessage());
         assertEquals(errBean.getErrorMessage(), "No Command Has Been Provided - To list all available commands, use command=commandlist");
     }
 
     @Test
     public void testInitializeWithIntegerAndStacktrace() {
         RuntimeException ex = new ArrayStoreException();
 
         Error errBean = new Error(Integer.valueOf(0), ex);
         assertNotNull(errBean.getErrorMessage());
         assertEquals(errBean.getErrorMessage(), "No Command Has Been Provided - To list all available commands, use command=commandlist");
         assertNotNull(errBean.getException());
         assertEquals(errBean.getException().getClass(), ArrayStoreException.class);
     }
 
     @Test
     public void testInitializeWithString() {
         Error errBean = new Error("test");
         assertNotNull(errBean.getErrorMessage());
         assertEquals(errBean.getErrorMessage(), "test");
     }
 
     @Test
     public void testSetGetMessage() {
         Error errBean = new Error();
         errBean.setErrorMessage("test");
         assertNotNull(errBean.getErrorMessage());
         assertEquals(errBean.getErrorMessage(), "test");
     }
 
     @Test
     public void testSetGetException() {
         RuntimeException ex = new ArrayStoreException();
         Error errBean = new Error();
         errBean.setException(ex);
         assertNotNull(errBean.getException());
         assertEquals(errBean.getException().getClass(), ArrayStoreException.class);
     }
 
     @Test
     public void testSetGetErrorCreated() {
         Error errBean = new Error();
         Date date = new Date();
         long longDate = date.getTime();
         errBean.setErrorCreated(date);
         assertNotNull(errBean.getErrorCreated());
         assertEquals(errBean.getErrorCreated().getTime(), longDate);
     }
 
     @Test
     public void testSetGetErrorClassParam() {
         Error errBean = new Error();
         errBean.setErrorClass("test");
         assertNotNull(errBean.getErrorClass());
         assertEquals(errBean.getErrorClass(), "test");
     }
 
     @Test
     public void testToString() {
         Error errBean = new Error();
         errBean.setErrorClass("test");
         assertNotNull(errBean.toString());
         assertNotSame("", errBean.toString());
     }
 
     @Test
     public void testSetGetErrorNumber() {
         Error errBean = new Error();
         errBean.setErrorNumber(Integer.MIN_VALUE);
         assertNotNull(errBean.getErrorNumber());
         assertTrue(errBean.getErrorNumber() == Integer.MIN_VALUE);
     }
 
     @Test
     public void testErrorConstructor() {
         Error test = new Error(ErrorEnum.ERR_FILE_LIST);
         assertNotNull(test);
        assertTrue(test.getErrorNumber() == 1);
     }
 }
