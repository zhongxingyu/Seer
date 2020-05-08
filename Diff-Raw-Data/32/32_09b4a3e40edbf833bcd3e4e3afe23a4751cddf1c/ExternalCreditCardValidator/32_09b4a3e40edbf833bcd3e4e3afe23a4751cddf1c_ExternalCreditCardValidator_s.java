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
 
 import nl.han.dare2date.applyregistrationservice.Creditcard;
 import nl.han.dare2date.service.jms.util.JMSUtil;
 import nl.han.dare2date.service.jms.util.Queues;
 import nl.han.dare2date.service.jms.util.Replier;
 
 import javax.jms.Connection;
 import javax.jms.JMSException;
 import javax.jms.ObjectMessage;
 import javax.naming.NamingException;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 
 public class ExternalCreditCardValidator extends Replier implements IExternalCreditCardValidator {
 
     private Creditcard cc = null;
 
     public ExternalCreditCardValidator(Connection con) throws JMSException, NamingException {
         super(con, Queues.REQUEST_QUEUE, Queues.REPLY_QUEUE);
     }
 
     @Override
     public ObjectMessage getReplyMessage() {
         ObjectMessage msg = null;
         try {
             msg = getSession().createObjectMessage(validateCreditCard(cc));
             msg.setBooleanProperty("valid",validateCreditCard(cc));
         } catch(JMSException e) {
             e.printStackTrace();
         }
         return msg;
     }
 
     private boolean validateCreditCard(Creditcard cc) {
         return validateDate(cc) && validateCreditCardNumber(cc);
     }
 
     private boolean validateCreditCardNumber(Creditcard cc) {
         List<Long> numbers = longToList(cc.getNumber());
         Long result = 0L;
        for(int i = 0;i < numbers.size();i++) {
             if(i%2 == 1) {
                 if(((numbers.get(i) * 2) <= 9)) {
                result += (numbers.get(i)*2);
                 } else {
                     List<Long> rest = longToList((numbers.get(i)*2));
                     result += rest.get(0) + rest.get(1) ;
                 }
             } else {
                 result += numbers.get(i);
             }
         }
        return result%10 == 0;
     }
 
     private List<Long> longToList(long num) {
         LinkedList<Long> numbers = new LinkedList<Long>();
         numbers.addFirst(num%10);
         return longToList(num/10,numbers);
     }
 
     private List<Long> longToList(long num, LinkedList<Long> numbers) {
         numbers.addFirst(num%10);
         if(num < 10) {
             return numbers;
         } else {
             return longToList(num/10,numbers);
         }
     }
 
     private boolean validateDate(Creditcard cc) {
         GregorianCalendar gregorianCalendar = new GregorianCalendar();
         DatatypeFactory datatypeFactory = null;
         try {
             datatypeFactory = DatatypeFactory.newInstance();
         } catch (DatatypeConfigurationException e) {
             e.printStackTrace();
         }
         XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
         return cc.getValidThrough().compare(now) >= 0 ;
     }
 
     @Override
     public void handleMessage(Serializable contents) {
         cc = (Creditcard)contents;
     }
 }
