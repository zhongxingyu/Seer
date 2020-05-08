 /**
  * Copyright (c) 2013 HAN University of Applied Sciences
  * Arjan Oortgiese
  * Joëll Portier
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package nl.han.dare2date.service.web;
 
 import junit.framework.Assert;
 import nl.han.dare2date.applyregistrationservice.Creditcard;
 import nl.han.dare2date.service.jms.util.JMSUtil;
 import org.junit.Test;
 
 import javax.jms.Connection;
 import javax.jms.JMSException;
 import javax.naming.NamingException;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import java.util.GregorianCalendar;
 
 public class ExternalCreditCardValidatorTest {
 
     private IExternalCreditCardValidator validator;
 
     @Test
     public void testValidateWithValidInformation() {
         Connection connection = JMSUtil.getConnection();
         try {
             validator = new ExternalCreditCardValidator(connection);
         } catch (JMSException e) {
             Assert.fail();  //To change body of catch statement use File | Settings | File Templates.
         } catch (NamingException e) {
             Assert.fail();
         }
         IExternalCreditCardValidator ecv = null;
         try {
             ecv = new ExternalCreditCardValidator(connection);
         }   catch(Exception e){
             Assert.fail();
         }
         Creditcard cc = new Creditcard();
         cc.setNumber(4539339738582406L);
         cc.setCvc(123);
         GregorianCalendar gregorianCalendar = new GregorianCalendar();
         DatatypeFactory datatypeFactory = null;
         try {
             datatypeFactory = DatatypeFactory.newInstance();
         } catch (DatatypeConfigurationException e) {
             Assert.fail();
         }
         XMLGregorianCalendar date = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
         date.setYear(2020);
         cc.setValidThrough(date);
         ecv.handleMessage(cc);
         try {
             Assert.assertFalse(ecv.getReplyMessage().getBooleanProperty("valid"));
         } catch (JMSException e) {
             Assert.fail();
         }
         try {
             connection.close();
         } catch (JMSException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     @Test
     public void testValidateWithInvalidDate() {
         Connection connection = JMSUtil.getConnection();
         try {
             validator = new ExternalCreditCardValidator(connection);
         } catch (JMSException e) {
             Assert.fail();  //To change body of catch statement use File | Settings | File Templates.
         } catch (NamingException e) {
             Assert.fail();
         }
         IExternalCreditCardValidator ecv = null;
         try {
             ecv = new ExternalCreditCardValidator(connection);
         }   catch(Exception e){
             Assert.fail();
         }
         Creditcard cc = new Creditcard();
        cc.setNumber(79927398713L);
         cc.setCvc(123);
         GregorianCalendar gregorianCalendar = new GregorianCalendar();
         DatatypeFactory datatypeFactory = null;
         try {
             datatypeFactory = DatatypeFactory.newInstance();
         } catch (DatatypeConfigurationException e) {
             Assert.fail();
         }
         XMLGregorianCalendar date = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        date.setYear(2020);
         cc.setValidThrough(date);
         ecv.handleMessage(cc);
         try {
            Assert.assertTrue(ecv.getReplyMessage().getBooleanProperty("valid"));
         } catch (JMSException e) {
             Assert.fail();
         }
         try {
             connection.close();
         } catch (JMSException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     @Test
     public void testValidateWithInvalidCreditcardNumber() {
         Connection connection = JMSUtil.getConnection();
         try {
             validator = new ExternalCreditCardValidator(connection);
         } catch (JMSException e) {
             Assert.fail();  //To change body of catch statement use File | Settings | File Templates.
         } catch (NamingException e) {
             Assert.fail();
         }
         IExternalCreditCardValidator ecv = null;
         try {
             ecv = new ExternalCreditCardValidator(connection);
         }   catch(Exception e){
             Assert.fail();
         }
         Creditcard cc = new Creditcard();
         cc.setNumber(4539339738582405L);
         cc.setCvc(123);
         GregorianCalendar gregorianCalendar = new GregorianCalendar();
         DatatypeFactory datatypeFactory = null;
         try {
             datatypeFactory = DatatypeFactory.newInstance();
         } catch (DatatypeConfigurationException e) {
             Assert.fail();
         }
         XMLGregorianCalendar date = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
         date.setYear(2020);
         cc.setValidThrough(date);
         ecv.handleMessage(cc);
         try {
             Assert.assertFalse(ecv.getReplyMessage().getBooleanProperty("valid"));
         } catch (JMSException e) {
             Assert.fail();
         }
         try {
             connection.close();
         } catch (JMSException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 }
 
