 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package gov.usgs.cida.gdp.communication.bean;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author isuftin
  */
 public class EmailMessageTest {
 
     public EmailMessageTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     @Test
     public void testEmailMessageBeanDefault() {
         EmailMessage instance = new EmailMessage();
         assertEquals("", instance.getFrom());
         assertEquals("", instance.getTo());
         assertTrue(instance.getCc().isEmpty());
         assertEquals("", instance.getSubject());
         assertEquals("", instance.getContent());
     }
 
     /**
      * Test of getFrom method, of class EmailMessage.
      */
     @Test
     public void testGetFrom() {
         EmailMessage instance = new EmailMessage();
         String expResult = "";
         String result = instance.getFrom();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setFrom method, of class EmailMessage.
      */
     @Test
     public void testSetFrom() {
         String from = "";
         EmailMessage instance = new EmailMessage();
         instance.setFrom(from);
     }
 
     /**
      * Test of getTo method, of class EmailMessage.
      */
     @Test
     public void testGetTo() {
         EmailMessage instance = new EmailMessage();
         String expResult = "";
         String result = instance.getTo();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setTo method, of class EmailMessage.
      */
     @Test
     public void testSetTo() {
         String to = "";
         EmailMessage instance = new EmailMessage();
         instance.setTo(to);
     }
 
     /**
      * Test of getCc method, of class EmailMessage.
      */
     @SuppressWarnings("unchecked")
     @Test
     public void testGetCc() {
         EmailMessage instance = new EmailMessage();
         List expResult = new Vector<String>();
         List result = instance.getCc();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setCc method, of class EmailMessage.
      */
     @Test
     public void testSetCc() {
         List<String> cc = null;
         EmailMessage instance = new EmailMessage();
         instance.setCc(cc);
     }
 
     /**
      * Test of getSubject method, of class EmailMessage.
      */
     @Test
     public void testGetSubject() {
         EmailMessage instance = new EmailMessage();
         String expResult = "";
         String result = instance.getSubject();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setSubject method, of class EmailMessage.
      */
     @Test
     public void testSetSubject() {
         String subject = "";
         EmailMessage instance = new EmailMessage();
         instance.setSubject(subject);
     }
 
     /**
      * Test of getContent method, of class EmailMessage.
      */
     @Test
     public void testGetContent() {
         EmailMessage instance = new EmailMessage();
         String expResult = "";
         String result = instance.getContent();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setContent method, of class EmailMessage.
      */
     @Test
     public void testSetContent() {
         String content = "";
         EmailMessage instance = new EmailMessage();
         instance.setContent(content);
     }
 
     /**
      * Test of getBcc method, of class EmailMessage.
      */
     @SuppressWarnings("unchecked")
     @Test
     public void testGetBcc() {
         EmailMessage instance = new EmailMessage();
         List expResult = new ArrayList<String>();
         List result = instance.getBcc();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getBccToString method, of class EmailMessage.
      */
     @Test
     public void testGetBccToString() {
         EmailMessage instance = new EmailMessage();
         String expResult = "";
         String result = instance.getBccToString();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setBcc method, of class EmailMessage.
      */
     @Test
     public void testSetBcc() {
         List<String> bcc = null;
         EmailMessage instance = new EmailMessage();
         instance.setBcc(bcc);
     }
 
     @Test
     public void testGetBCCToStringWithEmptyBCC() {
         EmailMessage instance = new EmailMessage();
         instance.setBcc(new ArrayList<String>());
         String result = instance.getBccToString();
         assertEquals("", result);
     }
 
     @Test
     public void testGetBCCToString() {
         EmailMessage instance = new EmailMessage();
         List<String> input = new ArrayList<String>();
         input.add("test@test.com");
         instance.setBcc(input);
         String result = instance.getBccToString();
         assertFalse("".equals(result));
        assertEquals("test@test.com,", result);
     }
 
 }
