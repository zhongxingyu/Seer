 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2008 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.sapienter.jbilling.server.notification;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.activation.DataHandler;
 import javax.activation.FileDataSource;
 import javax.ejb.CreateException;
 import javax.ejb.FinderException;
 import javax.ejb.RemoveException;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Part;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.naming.NamingException;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 
 import org.apache.log4j.Logger;
 
 import sun.jdbc.rowset.CachedRowSet;
 
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.interfaces.InvoiceEntityLocal;
 import com.sapienter.jbilling.interfaces.NotificationMessageEntityLocal;
 import com.sapienter.jbilling.interfaces.NotificationMessageEntityLocalHome;
 import com.sapienter.jbilling.interfaces.NotificationMessageLineEntityLocal;
 import com.sapienter.jbilling.interfaces.NotificationMessageLineEntityLocalHome;
 import com.sapienter.jbilling.interfaces.NotificationMessageSectionEntityLocal;
 import com.sapienter.jbilling.interfaces.NotificationMessageSectionEntityLocalHome;
 import com.sapienter.jbilling.interfaces.NotificationMessageTypeEntityLocal;
 import com.sapienter.jbilling.interfaces.NotificationMessageTypeEntityLocalHome;
 import com.sapienter.jbilling.server.entity.CreditCardDTO;
 import com.sapienter.jbilling.server.invoice.InvoiceBL;
 import com.sapienter.jbilling.server.invoice.InvoiceDTOEx;
 import com.sapienter.jbilling.server.invoice.InvoiceLineDTOEx;
 import com.sapienter.jbilling.server.list.ResultList;
 import com.sapienter.jbilling.server.payment.PaymentBL;
 import com.sapienter.jbilling.server.payment.PaymentDTOEx;
 import com.sapienter.jbilling.server.pluggableTask.PaperInvoiceNotificationTask;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskBL;
 import com.sapienter.jbilling.server.user.ContactBL;
 import com.sapienter.jbilling.server.user.ContactDTOEx;
 import com.sapienter.jbilling.server.user.EntityBL;
 import com.sapienter.jbilling.server.user.UserBL;
 import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
 import com.sapienter.jbilling.server.user.db.CompanyDAS;
 import com.sapienter.jbilling.server.user.partner.PartnerBL;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.PreferenceBL;
 import com.sapienter.jbilling.server.util.Util;
 
 public class NotificationBL extends ResultList 
         implements NotificationSQL {
     //
     private JNDILookup EJBFactory = null;
     private NotificationMessageEntityLocalHome messageHome = null;
     private NotificationMessageSectionEntityLocalHome messageSectionHome = null;
     private NotificationMessageTypeEntityLocalHome messageTypeHome = null;
     private NotificationMessageLineEntityLocalHome messageLineHome = null;
     private NotificationMessageEntityLocal messageRow = null;
     private static final Logger LOG = Logger.getLogger(NotificationBL.class);
     
     public NotificationBL(Integer messageId) 
             throws NamingException, FinderException {
         init();
 
         messageRow = messageHome.findByPrimaryKey(messageId);
     }
     
     public NotificationBL() throws NamingException {
         init();
     }
     
     private void init() throws NamingException {
              
         EJBFactory = JNDILookup.getFactory(false);
         messageHome = (NotificationMessageEntityLocalHome) 
                 EJBFactory.lookUpLocalHome(
                 NotificationMessageEntityLocalHome.class,
                 NotificationMessageEntityLocalHome.JNDI_NAME);
         messageSectionHome = (NotificationMessageSectionEntityLocalHome) 
                 EJBFactory.lookUpLocalHome(
                 NotificationMessageSectionEntityLocalHome.class,
                 NotificationMessageSectionEntityLocalHome.JNDI_NAME);
         messageLineHome = (NotificationMessageLineEntityLocalHome) 
                 EJBFactory.lookUpLocalHome(
                 NotificationMessageLineEntityLocalHome.class,
                 NotificationMessageLineEntityLocalHome.JNDI_NAME);
         messageTypeHome = (NotificationMessageTypeEntityLocalHome) 
                 EJBFactory.lookUpLocalHome(
                 NotificationMessageTypeEntityLocalHome.class,
                 NotificationMessageTypeEntityLocalHome.JNDI_NAME);
     }
     
     public NotificationMessageEntityLocal getEntity() {
         return messageRow;
     }
     
     public void set(Integer type, Integer languageId, Integer entityId) 
             throws FinderException {
         messageRow = messageHome.findIt(type, entityId, languageId);
     }
     
     
     public MessageDTO getDTO() 
             throws SessionInternalError, FinderException, NamingException{
         MessageDTO retValue = new MessageDTO();
         retValue.setLanguageId(messageRow.getLanguageId());
         retValue.setTypeId(messageRow.getType().getId());
         retValue.setUseFlag(new Boolean(messageRow.getUseFlag().intValue() == 1));
         
         setContent(retValue);
         
         return retValue;
     }
     
     public Integer createUpdate(Integer entityId, MessageDTO dto) 
             throws CreateException, RemoveException {
         try {
             set(dto.getTypeId(), dto.getLanguageId(), entityId);
             // it's just so easy to delete cascade and recreate ...:D
             messageRow.remove(); 
         } catch (FinderException e) {
             // then it is a new one
         }
 
         messageRow = messageHome.create(dto.getTypeId(), entityId, 
                 dto.getLanguageId(), dto.getUseFlag());    
         //messageRow.
         
         // add the sections with the lines to the message entity
         for (int f = 0; f < dto.getContent().length; f++) {
             MessageSection section = dto.getContent()[f];
             
             // create the section bean
             NotificationMessageSectionEntityLocal sectionBean =
                     messageSectionHome.create(section.getSection());
             int index = 0;
             while (index < section.getContent().length()) {
                 String line;
                 if (index + MessageDTO.LINE_MAX.intValue() <= 
                         section.getContent().length()) {
                     line = section.getContent().substring(index, 
                         index + MessageDTO.LINE_MAX.intValue());
                 } else {
                     line = section.getContent().substring(index);
                 }
                 index += MessageDTO.LINE_MAX.intValue();
                 
                 sectionBean.getLines().add(messageLineHome.create(line));
             }
             messageRow.getSections().add(sectionBean);
         }
         
         return messageRow.getId();
     }
     
     /*
      * Getters.
      * These provide easy generation of messages by their type.
      * So each getter kows which type will generate, and gets as parameters
      * the information to generate that particular type of message.
      */
      
     public MessageDTO[] getInvoiceMessages(Integer entityId, Integer processId,
     		Integer languageId, InvoiceEntityLocal invoice) 
             throws SessionInternalError, FinderException, NamingException, 
 				NotificationNotFoundException {
         MessageDTO retValue[] = null;
         Integer deliveryMethod; 
         // now see what kind of invoice this customers wants
         if (invoice.getUser().getCustomer() == null) {
             // this shouldn't be necessary. The only reason is here is
             // because the test data has invoices for root users. In
             // reality, all users that will get an invoice have to be 
             // customers
             deliveryMethod = Constants.D_METHOD_EMAIL;
             LOG.warn("A user that is not a customer is getting an invoice." + 
                     " User id = " + invoice.getUser().getUserId());
         } else {
             deliveryMethod = invoice.getUser().getCustomer().
                     getInvoiceDeliveryMethod().getId();
         }
         
         int index = 0;
         if (deliveryMethod.equals(Constants.D_METHOD_EMAIL_AND_PAPER)) {
             retValue = new MessageDTO[2];
         } else {
             retValue = new MessageDTO[1];
         }
         if (deliveryMethod.equals(Constants.D_METHOD_EMAIL) || 
                 deliveryMethod.equals(Constants.D_METHOD_EMAIL_AND_PAPER)) {
             retValue[index] = getInvoiceEmailMessage(entityId, languageId, 
                     invoice);
             index++;
         } 
         
         if (deliveryMethod.equals(Constants.D_METHOD_PAPER) || 
                 deliveryMethod.equals(Constants.D_METHOD_EMAIL_AND_PAPER)) {
             retValue[index] =  getInvoicePaperMessage(entityId, processId, 
                     languageId, invoice);
             index++;
         }
         
         return retValue;
     }
 
     public MessageDTO getInvoicePaperMessage(Integer entityId, 
     		Integer processId, Integer languageId, 
             InvoiceEntityLocal invoice) 
             throws SessionInternalError, FinderException, NamingException {
         MessageDTO retValue = new MessageDTO();
         
         retValue.setTypeId(MessageDTO.TYPE_INVOICE_PAPER);
         retValue.setDeliveryMethodId(Constants.D_METHOD_PAPER);
         
         // put the whole invoice as a parameter
         InvoiceBL invoiceBl = new InvoiceBL(invoice);
         InvoiceDTOEx invoiceDto = invoiceBl.getDTOEx(languageId, true);
         retValue.getParameters().put("invoiceDto", invoiceDto);
         // the process id is needed to maintain the batch record
         if (processId != null) {
         	// single pdf invoices for the web-based app can ignore this
         	retValue.getParameters().put("processId", processId);
         }
         try {
             setContent(retValue, MessageDTO.TYPE_INVOICE_PAPER, entityId,
                     languageId);
         } catch (NamingException e) {
             throw new SessionInternalError(e);
         } catch (NotificationNotFoundException e1) {
         	// put blanks
         	MessageSection sectionContent = new MessageSection(
     				new Integer(1), null);
         	retValue.addSection(sectionContent);
         	sectionContent = new MessageSection(
     				new Integer(2), null);
         	retValue.addSection(sectionContent);
         }
         
         return retValue;
     }
     
     public MessageDTO getPaymentMessage(Integer entityId, PaymentDTOEx dto,
             boolean result) 
             throws SessionInternalError, NotificationNotFoundException {
         UserBL user = null;
         Integer languageId = null;
         MessageDTO message = initializeMessage(entityId, dto.getUserId());
         message.setTypeId(result ? MessageDTO.TYPE_PAYMENT :
                 new Integer(MessageDTO.TYPE_PAYMENT.intValue() + 1));
         
         try {
             user = new UserBL(dto.getUserId());
             languageId = user.getEntity().getLanguageIdField();
             setContent(message, message.getTypeId(), entityId, languageId);
             
             // find the description for the payment method
             PaymentBL payment = new PaymentBL();
             message.addParameter("method", payment.getMethodDescription(
                     dto.getMethodId(), languageId));
             message.addParameter("total", Util.formatMoney(dto.getAmount(), 
                     dto.getUserId(), dto.getCurrencyId(), true));
             // find an invoice in the list of invoices id
             if (dto.getInvoiceIds() != null && dto.getInvoiceIds().size() > 0) {
                 Integer invoiceId = (Integer) dto.getInvoiceIds().get(0);
                 InvoiceBL invoice = new InvoiceBL(invoiceId);
                 message.addParameter("invoice_number", 
                         invoice.getEntity().getNumber().toString());
             }
         } catch (NamingException e) {
             throw new SessionInternalError(e);
         } catch (FinderException e2) {
             throw new SessionInternalError(e2);
         }
         
         return message;
     }
 
     public MessageDTO getInvoiceRemainderMessage(Integer entityId, Integer userId,
             Integer days, Date dueDate, String number, Float total,
             Date date, Integer currencyId) 
             throws SessionInternalError, NotificationNotFoundException {
         UserBL user = null;
         Integer languageId = null;
         MessageDTO message = initializeMessage(entityId, userId);
         message.setTypeId(MessageDTO.TYPE_INVOICE_REMINDER);
         
         try {
             user = new UserBL(userId);
             languageId = user.getEntity().getLanguageIdField();
             setContent(message, message.getTypeId(), entityId, languageId);
             
             message.addParameter("days", days.toString());
             message.addParameter("dueDate", Util.formatDate(dueDate, userId));
             message.addParameter("number", number);
             message.addParameter("total", Util.formatMoney(total, userId,
                     currencyId, true));
             message.addParameter("date", Util.formatDate(date, userId));
             
         } catch (NamingException e) {
             throw new SessionInternalError(e);
         } catch (FinderException e2) {
             throw new SessionInternalError(e2);
         }
         
         return message;
     }
     
     public MessageDTO getForgetPasswordEmailMessage(Integer entityId,
     		Integer userId,
     		Integer languageId) throws SessionInternalError, 
     			FinderException, NotificationNotFoundException, NamingException {
     	MessageDTO message = initializeMessage(entityId, userId);
     	
     	message.setTypeId(MessageDTO.TYPE_FORGETPASSWORD_EMAIL);
     	
     	try {
     		setContent(message, MessageDTO.TYPE_FORGETPASSWORD_EMAIL,
     				entityId, languageId);
     	} catch (NamingException e) {
     		throw new SessionInternalError(e);
     	}
     	
     	return message;
     }
 
     public MessageDTO getInvoiceEmailMessage(Integer entityId, Integer languageId, 
             InvoiceEntityLocal invoice) 
             throws SessionInternalError, FinderException, 
                 NotificationNotFoundException {
         MessageDTO message = initializeMessage(entityId, 
                 invoice.getUser().getUserId());
         
         message.setTypeId(MessageDTO.TYPE_INVOICE_EMAIL);
 
         try {
             setContent(message, MessageDTO.TYPE_INVOICE_EMAIL, entityId,
                     languageId);
         } catch (NamingException e) {
             throw new SessionInternalError(e);
         } 
         
         message.addParameter("total", Util.formatMoney(
                 invoice.getTotal(), invoice.getUser().getUserId(),
                 invoice.getCurrencyId(), true));
         message.addParameter("id", invoice.getId().toString());
         message.addParameter("number", invoice.getNumber());
         // format the date depending of the customers locale
         
         message.addParameter("due date",Util.formatDate(invoice.getDueDate(),
                 invoice.getUser().getUserId()));
         String notes = invoice.getCustomerNotes();
         if (notes != null) {
             notes = notes.replaceAll("<br/>", "\r\n");
         }
         message.addParameter("notes", notes);
         
         // if the entity has the preference of pdf attachment, do it
         try {
             PreferenceBL pref = new PreferenceBL();
             
             try {
                 pref.set(entityId, Constants.PREFERENCE_PDF_ATTACHMENT);
             } catch (FinderException e1) {
                 // no problem, I'll get the defaults
             }
             if (pref.getInt() == 1) {
                 message.setAttachmentFile(generatePaperInvoiceAsFile(
                         invoice));
                 LOG.debug("Setted attachement " + message.getAttachmentFile());
             }
         } catch (Exception e) {
             LOG.error(e);
         }
         
         return message;
     }
     
     public MessageDTO getAgeingMessage(Integer entityId, Integer languageId,
             Integer statusId, Integer userId) 
             throws SessionInternalError, FinderException, 
             NotificationNotFoundException {
         MessageDTO message = initializeMessage(entityId, userId);
         message.setTypeId(new Integer(MessageDTO.TYPE_AGEING.intValue() 
                 + statusId.intValue() - 1));
 
         try {
             setContent(message, message.getTypeId(), entityId,
                     languageId);
             UserBL user = new UserBL(userId);
             InvoiceBL invoice = new InvoiceBL();
             Integer invoiceId = invoice.getLastByUser(userId);
             if (invoiceId != null) {
                 invoice.set(invoiceId);
             
                 message.addParameter("total", Util.float2string(
                         invoice.getEntity().getBalance(), user.getLocale()));
             } else {
                 LOG.warn("user " + userId + " has no invoice but an ageing " +
                         "message is being sent");
             }
         } catch (NamingException e) {
             throw new SessionInternalError(e);
         } catch (SQLException e1) {
             throw new SessionInternalError(e1);
         }
 
 
         return message;
     }
     
     public MessageDTO getOrderNotification(Integer entityId, Integer step,
     		Integer languageId, Date activeSince, Date activeUntil,
             Integer userId, Float total, Integer currencyId)
 			throws FinderException, SessionInternalError, 
 				NotificationNotFoundException {
     	MessageDTO retValue = initializeMessage(entityId, userId);
     	retValue.setTypeId(new Integer(MessageDTO.TYPE_ORDER_NOTIF.intValue() +
                 step.intValue() - 1));
         try {
 			setContent(retValue, retValue.getTypeId(), entityId, languageId);
             Locale locale;
             try {
                 UserBL user = new UserBL(userId);
                 locale = user.getLocale();
             } catch (Exception e) {
                 throw new SessionInternalError(e);
             }
             ResourceBundle bundle = ResourceBundle.getBundle("entityNotifications", 
                     locale);
             SimpleDateFormat formatter = new SimpleDateFormat(
                     bundle.getString("format.date"));
 			
 			retValue.addParameter("period_start", formatter.format(activeSince));
 			retValue.addParameter("period_end", formatter.format(activeUntil));
             retValue.addParameter("total", Util.formatMoney(total, userId,
                     currencyId, true));
 		} catch (ClassCastException e) {
 			throw new SessionInternalError(e);
 		} catch (NamingException e) {
 			throw new SessionInternalError(e);
 		} 
         
         return retValue;
    }
     public MessageDTO getPayoutMessage(Integer entityId, Integer languageId,
             double total, Date startDate, Date endDate, boolean clerk, 
             Integer partnerId) 
             throws FinderException, SessionInternalError, NotificationNotFoundException {
         
         MessageDTO message = new MessageDTO();
         if (!clerk) {
             message.setTypeId(MessageDTO.TYPE_PAYOUT);
         } else {
             message.setTypeId(MessageDTO.TYPE_CLERK_PAYOUT);
         }
 
         try {
             EntityBL en = new EntityBL(entityId);
 
 			setContent(message, message.getTypeId(), entityId, languageId);
 			message.addParameter("total", Util.float2string(new Float(total), en.getLocale()));
 			
 			message.addParameter("company", new CompanyDAS().find(entityId).
 			        getDescription()); 
             PartnerBL partner = new PartnerBL(partnerId);
             
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(endDate);
 			message.addParameter("period_end", Util.formatDate(cal.getTime(),
                     partner.getEntity().getUser().getUserId()));
 			cal.setTime(startDate);
 			message.addParameter("period_start", Util.formatDate(cal.getTime(),
                     partner.getEntity().getUser().getUserId()));
 			message.addParameter("partner_id", partnerId.toString());
 		} catch (ClassCastException e) {
 			throw new SessionInternalError(e);
 		} catch (NamingException e) {
 			throw new SessionInternalError(e);
 		} 
         
         return message;
     }
     
     public MessageDTO getCreditCardMessage(Integer entityId, Integer languageId,
             Integer userId, CreditCardDTO creditCard) 
             throws SessionInternalError, FinderException, 
             NotificationNotFoundException {
         MessageDTO message = initializeMessage(entityId, userId);
         message.setTypeId(MessageDTO.TYPE_CREDIT_CARD);
 
         try {
             setContent(message, message.getTypeId(), entityId,
                     languageId);
             SimpleDateFormat format = new SimpleDateFormat("MM/yy");
             message.addParameter("expiry_date", format.format(
                     creditCard.getExpiry()));
         } catch (NamingException e) {
             throw new SessionInternalError(e);
         } 
 
         return message;
     }
     
     private void setContent(MessageDTO newMessage, Integer type, 
             Integer entity, Integer language) 
             throws NamingException, FinderException, SessionInternalError,
 				NotificationNotFoundException {
     	try {
     		set(type, language, entity);
             if (messageRow.getUseFlag().intValue() == 0) {
                 throw new NotificationNotFoundException("Notification " +
                         "flaged for not use");
             }
     		setContent(newMessage);
     	} catch (FinderException e) {
     		String message = "Looking for notification message type " + type + 
 					" for entity " + entity + " language " + language +
 					" but could not find it. This entity has to specify " +
 					"this notification message.";
 			LOG.warn(message);
     		throw new NotificationNotFoundException(message);
     	}
     	
     }
 
     private void setContent(MessageDTO newMessage) 
             throws NamingException, FinderException, SessionInternalError {
         
         // go through the sections
         Collection sections = messageRow.getSections();
         for (Iterator it = sections.iterator(); it.hasNext();) {
             NotificationMessageSectionEntityLocal section = 
                     (NotificationMessageSectionEntityLocal) it.next();
             // then through the lines of this section
             StringBuffer completeLine = new StringBuffer();
             Collection lines = section.getLines();
             int checkOrder = 0; // there's nothing to assume that the lines
                                 // will be retrived in order, but the have to!
             Vector vLines = new Vector<NotificationMessageSectionEntityLocal>(lines);
             Collections.sort(vLines, new NotificationLineEntityComparator());
             for (Iterator it2 = vLines.iterator(); it2.hasNext();) {
                 NotificationMessageLineEntityLocal line = 
                         (NotificationMessageLineEntityLocal) it2.next();
                 if (line.getId().intValue() <= checkOrder) {
                     LOG.error("Lines have to be retreived in order. " +
                             "See class java.util.TreeSet for solution or " +
                             "Collections.sort()");
                     throw new SessionInternalError("Lines have to be " +
                             "retreived in order.");
                 } else {
                     checkOrder = line.getId().intValue();
                 }
                 completeLine.append(line.getContent());
             }
             // add the content of this section to the message
             MessageSection sectionContent = new MessageSection(section.getSection(),
                     completeLine.toString());
             newMessage.addSection(sectionContent);
         }
     }
     
     static public String parseParameters(String content, 
             HashMap parameters) {
         Logger log = Logger.getLogger(NotificationBL.class);
         StringBuffer result = new StringBuffer();
         
         StringTokenizer tokens = new StringTokenizer(content, "|");
         int toReplace = tokens.countTokens();
         if (toReplace == 0) {
             result.append(content);
         }
         else {
             while (tokens.hasMoreTokens()) {
                 String str = tokens.nextToken();
                 String value = (String) parameters.get(str);
                 if (value != null) {
                     // the variable is present with content
                     str = value; 
                 } else if (parameters.containsKey(str)){
                     // the variable is present but void
                     str = "";
                 }
                 result.append(str);
             }
         }
         
         return result.toString();             
     }
     
     public int getSections(Integer typeId) 
             throws FinderException {
         NotificationMessageTypeEntityLocal type = 
                 messageTypeHome.findByPrimaryKey(typeId);
     
         return type.getSections().intValue();
     }
 
     public static void main(String[] args) throws Exception {
         String message = "Hello |param1|. This is the |param2| test";
         HashMap parameters = new HashMap();
         
         parameters.put("param1", "Emil");
         parameters.put("param2", "first");
 
         System.out.println("Parsing:" + message);        
         String result = parseParameters(message, parameters);
         System.out.println("Result:" + result);
     }
 
     public CachedRowSet getTypeList(Integer languageId)
             throws SQLException, Exception{
 
         prepareStatement(NotificationSQL.listTypes);
         cachedResults.setInt(1,languageId.intValue());
         execute();
         conn.close();
         return cachedResults;
     }
 
     public String getEmails(String separator, Integer entityId) 
             throws SQLException, NamingException{
         StringBuffer retValue = new StringBuffer();
         conn = EJBFactory.lookUpDataSource().getConnection();
         PreparedStatement stmt = conn.prepareStatement(
                 NotificationSQL.allEmails);
         stmt.setInt(1, entityId.intValue());
         ResultSet res = stmt.executeQuery();
         boolean first = true;
         
         while (res.next()) {
             if (first) {
                 first = false;
             } else {
                 retValue.append(separator);
             } 
             retValue.append(res.getString(1));
         }
         
         res.close();
         stmt.close();
         conn.close();
         
         return retValue.toString();
     }
 
     public static byte[] generatePaperInvoiceAsStream(String design, 
     		InvoiceDTOEx invoice, ContactDTOEx from, ContactDTOEx to,
 			String message1, String message2, Integer entityId,
             String username, String password) 
     		throws FileNotFoundException, SessionInternalError {
     	JasperPrint report = generatePaperInvoice(design, invoice, from, to,
     			message1, message2, entityId, username, password);
     	try {
 			return JasperExportManager.exportReportToPdf(report);
 		} catch (JRException e) {
 			Logger.getLogger(NotificationBL.class).error(
 					"Exception generating paper invoice", e);
 			return null;
 		}
     }
 
     public static String generatePaperInvoiceAsFile(String design, 
     		InvoiceDTOEx invoice, ContactDTOEx from, ContactDTOEx to,
 			String message1, String message2, Integer entityId,
             String username, String password) 
     		throws FileNotFoundException, SessionInternalError {
     	JasperPrint report = generatePaperInvoice(design, invoice, from, to,
     			message1, message2, entityId, username, password);
         String fileName = null;
     	try {
             fileName = com.sapienter.jbilling.common.Util.getSysProp("base_dir") + "invoices/" +
                     entityId + "-" + invoice.getId() + "-invoice.pdf";
 			JasperExportManager.exportReportToPdfFile(report, fileName);
 		} catch (JRException e) {
 			Logger.getLogger(NotificationBL.class).error(
 				"Exception generating paper invoice", e);
 		}
         return fileName;
     }
     
     private static JasperPrint generatePaperInvoice(String design, 
     		InvoiceDTOEx invoice, ContactDTOEx from, ContactDTOEx to,
 			String message1, String message2, Integer entityId,
             String username, String password) 
     		throws FileNotFoundException, SessionInternalError {
     	try {
             Logger log = Logger.getLogger(NotificationBL.class);
     		// This is needed for JasperRerpots to work, for some twisted XWindows issue
     		System.setProperty("java.awt.headless", "true");
             String designFile = com.sapienter.jbilling.common.Util.getSysProp("base_dir") + 
                     "designs/" + design + ".jasper";
 			File compiledDesign = new File(designFile);
 			log.debug("Generating paper invoice with design file : " + designFile);
 			FileInputStream stream = new FileInputStream(compiledDesign);
             Locale locale = (new UserBL(invoice.getUserId())).getLocale();
 			
 			// add all the invoice data
 			HashMap<String, Object> parameters = new HashMap<String, Object>();
 			parameters.put("invoiceNumber", invoice.getNumber());
 			parameters.put("entityName", printable(from.getOrganizationName()));
 			parameters.put("entityAddress", printable(from.getAddress1()));
 			parameters.put("entityPostalCode", printable(from.getPostalCode()));
 			parameters.put("entityCity", printable(from.getCity()));
 			parameters.put("entityProvince", printable(from.getStateProvince()));
             parameters.put("customerOrganization", 
                     printable(to.getOrganizationName()));
 			parameters.put("customerName", printable(to.getFirstName()) + " " + 
                     printable(to.getLastName()));
 			parameters.put("customerAddress", printable(to.getAddress1()));
 			parameters.put("customerPostalCode", printable(to.getPostalCode()));
 			parameters.put("customerCity", printable(to.getCity()));
 			parameters.put("customerProvince", printable(to.getStateProvince()));
             parameters.put("customerUsername", username);
             parameters.put("customerPassword", password);
             parameters.put("customerId", invoice.getUserId().toString());
 			parameters.put("invoiceDate", Util.formatDate(
 					invoice.getCreateDateTime(), invoice.getUserId()));
 			parameters.put("invoiceDueDate", Util.formatDate(
 					invoice.getDueDate(), invoice.getUserId()));
 			log.debug("m1 = " + message1 + " m2 = " + message2);
 			System.out.println("m1 = " + message1 + " m2 = " + message2);
 			if (message1 == null || message1.length() == 0) {
 				message1 = " ";
 			}
 			parameters.put("customerMessage1", message1);
 			if (message2 == null || message2.length() == 0) {
 				message2 = " ";
 			}
 			parameters.put("customerMessage2", message2);
 			
 			String notes = invoice.getCustomerNotes();
 			if (notes != null) {
 				notes = notes.replaceAll("<br/>", "\r\n");
 			}
 			parameters.put("notes", notes);
             // now some info about payments
             try {
                 InvoiceBL invoiceBL = new InvoiceBL(invoice.getId());
                 try {
                     parameters.put("paid", 
                             Util.formatMoney(new Float(invoiceBL.getTotalPaid()),
                                     invoice.getUserId(), invoice.getCurrencyId(),
                                     false));
                     // find the previous invoice and its payment for extra info
                     invoiceBL.setPrevious();
                     parameters.put("prevInvoiceTotal", 
                             Util.formatMoney(invoiceBL.getEntity().getTotal(),
                                     invoice.getUserId(), invoice.getCurrencyId(),
                                     false));
                     parameters.put("prevInvoicePaid", 
                             Util.formatMoney(new Float(invoiceBL.getTotalPaid()),
                                     invoice.getUserId(), invoice.getCurrencyId(),
                                     false));
                 } catch (FinderException e1) {
                     parameters.put("prevInvoiceTotal", "0");
                     parameters.put("prevInvoicePaid", "0");
                 }
                 
             } catch (Exception e) {
                 Logger.getLogger(NotificationBL.class).error(
                         "Exception generating paper invoice", e);
                 return null;
             }
             
             // add all the custom contact fields
             // the from 
             for (ContactFieldDTO field: from.getFields()) {
                 String fieldName = field.getType().getPromptKey();
                 fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
                 parameters.put("from_custom_" + fieldName,
                         field.getContent());
             }
            for (ContactFieldDTO field: to.getFields()) {
                 String fieldName = field.getType().getPromptKey();
                 fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
                 parameters.put("to_custom_" + fieldName,
                         field.getContent());
             }
             
 			// the logo is a file
 			File logo = new File(com.sapienter.jbilling.common.Util.getSysProp("base_dir") + 
                     "logos/entity-" + entityId + ".jpg");
 			parameters.put("entityLogo", logo);
             
 			// the invoice lines go as the data source for the report
             // we need to extract the taxes from them, put the taxes as
             // an independent parameter, and add the taxes rates as more
             // parameters
             Vector lines = invoice.getInvoiceLines();
             BigDecimal taxTotal = new BigDecimal(0);
             int taxItemIndex = 0;
             for (int f = 0; f < lines.size(); f++) {
                 InvoiceLineDTOEx line = (InvoiceLineDTOEx) lines.get(f);
                 //log.debug("Processing line " + line);
                 // process the tax, if this line is one
                 if (line.getTypeId() != null && // for headers/footers 
                         line.getTypeId().equals(Constants.INVOICE_LINE_TYPE_TAX)) {
                     // update the total tax variable
                 	taxTotal = taxTotal.add(new BigDecimal(line.getAmount().toString()));
                     // add the tax amount as an array parameter
                     parameters.put("taxItem_" + taxItemIndex, 
                             Util.float2string(line.getPrice(), locale));
                     taxItemIndex++;
                     // taxes are not displayed as invoice lines
                     lines.remove(f);
                     f = 0; // has to start all over again
                 } else if (line.getIsPercentage().intValue() == 1) {
                     // if the line is a percentage, remove the price
                     line.setPrice(null);
                 }
             }
             // remove the last line, that is the total footer
             lines.remove(lines.size() - 1);
             // now add the tax
             parameters.put("tax", Util.formatMoney(new Float(taxTotal.floatValue()), 
                     invoice.getUserId(), invoice.getCurrencyId(), false));
             parameters.put("totalWithTax", Util.formatMoney(invoice.getTotal(), 
                     invoice.getUserId(), invoice.getCurrencyId(), false));
             parameters.put("totalWithoutTax", Util.formatMoney(
                     new Float(invoice.getTotal().floatValue() - taxTotal.floatValue()), 
                     invoice.getUserId(), invoice.getCurrencyId(), false));
             parameters.put("balance", Util.formatMoney(invoice.getBalance(), 
                     invoice.getUserId(), invoice.getCurrencyId(), false));
 
             log.debug("Parameter tax = " + parameters.get("tax") +
                     " totalWithTax = " + parameters.get("totalWithTax") +
                     " totalWithoutTax = " + parameters.get("totalWithoutTax") +
                     " balance = " + parameters.get("balance"));
 
             
 			JRBeanCollectionDataSource data = new JRBeanCollectionDataSource(
 					lines);
             
             // at last, generate the report
 			JasperPrint report = JasperFillManager.fillReport(stream, 
 					parameters, data);
             stream.close();
 			return report;
 		} catch (Exception e) {
 			Logger.getLogger(NotificationBL.class).error(
 					"Exception generating paper invoice", e);
 			return null;
 		}
     }
     
     private static String printable(String str) {
         if (str == null) {
             return "";
         } 
         return str;
     }
 
     public static void sendSapienterEmail(Integer entityId, 
             String messageKey, String attachmentFileName, String[] params) 
             throws MessagingException, NamingException, FinderException,
                 IOException {
         String address = null;
         
         ContactBL contactBL = new ContactBL();
         contactBL.setEntity(entityId);
         
         address = contactBL.getEntity().getEmail();
         if (address == null) {
             // can't send something to the ether
             Logger log = Logger.getLogger(NotificationBL.class);
             log.warn("Trying to send email to entity " + entityId + 
                     " but no address was found");
             return;
         }
         sendSapienterEmail(address, entityId, messageKey, attachmentFileName,
                 params);
     }
     /**
      * This method is intended to be used to send an email from the system
      * to the entity. This is different than from the entity to a customer,
      * which should use a notification pluggable task.
      * The file entityNotifications.properties has to have key + "_subject" and 
      * key + "_body"
      * Note: For any truble, the best documentation is the source code of the
      * MailTag of Jakarta taglibs
      */
     public static void sendSapienterEmail(String address, Integer entityId,
     		String messageKey, String attachmentFileName, String[] params) 
     		throws MessagingException, NamingException, FinderException,
 				IOException {
     	Logger log = Logger.getLogger(NotificationBL.class);
         Properties prop = new Properties();   
         
         log.debug("seding sapienter email " + messageKey + " to " + address + " of entity " 
                 + entityId);
         // tell the server that is has to authenticate to the maileer
         // (yikes, this was painfull to find out)
         prop.setProperty("mail.smtp.auth", "true");
         
         // create the session & message
         Session session = Session.getInstance(prop, null);
         Message msg = new MimeMessage(session);
         
         msg.setFrom(new InternetAddress(com.sapienter.jbilling.common.Util.getSysProp("email_from"), 
                 com.sapienter.jbilling.common.Util.getSysProp("email_from_name")));
         // the to address
         msg.setRecipients(Message.RecipientType.TO,
                 InternetAddress.parse(address, false));
         // the subject and body are international
         EntityBL entity = new EntityBL(entityId);
         Locale locale = entity.getLocale();
         
         ResourceBundle rBundle = ResourceBundle.getBundle("entityNotifications", 
                 locale);
         String subject = rBundle.getString(messageKey + "_subject");
         String message = rBundle.getString(messageKey + "_body");
         
         // if there are parameters, replace them
         if (params != null) {
             for (int f = 0; f < params.length; f++) {
                 message = message.replaceFirst("\\|X\\|", params[f]);
             }
         }
 
         msg.setSubject(subject);
         
         if (attachmentFileName == null) { 
         	msg.setText(message);
         } else {
         	// it is a 'multi part' email
         	MimeMultipart mp = new MimeMultipart();
         	
         	// the text message is one part
         	MimeBodyPart text = new MimeBodyPart();
         	text.setDisposition(Part.INLINE);
         	text.setContent(message, "text/plain");
         	mp.addBodyPart(text);
 
         	// the attachement is another.
         	MimeBodyPart file_part = new MimeBodyPart();
         	File file = (File) new File(attachmentFileName);
         	FileDataSource fds = new FileDataSource(file);
         	DataHandler dh = new DataHandler(fds);
         	file_part.setFileName(file.getName());
         	file_part.setDisposition(Part.ATTACHMENT);
         	file_part.setDescription("Attached file: " + file.getName());
         	file_part.setDataHandler(dh);
         	mp.addBodyPart(file_part);
 
         	msg.setContent(mp);
         }
         
         // the date
         msg.setSentDate(Calendar.getInstance().getTime());
         
         Transport transport = session.getTransport("smtp");
         transport.connect(com.sapienter.jbilling.common.Util.getSysProp("smtp_server"), 
                 Integer.parseInt(com.sapienter.jbilling.common.Util.getSysProp("smtp_port")), 
                 com.sapienter.jbilling.common.Util.getSysProp("smtp_username"), 
                 com.sapienter.jbilling.common.Util.getSysProp("smtp_password"));
         InternetAddress addresses[] = new InternetAddress[1];
         addresses[0] = new InternetAddress(address);
         transport.sendMessage(msg, addresses);
     }
     
     /**
      * Creates a message object with a set of standard parameters
      * @param entityId
      * @param userId
      * @return
      * The message object with many useful parameters
      */
     private MessageDTO initializeMessage(Integer entityId, Integer userId) 
             throws SessionInternalError {
         MessageDTO retValue = new MessageDTO();
         try {
             UserBL user = new UserBL(userId);
             ContactBL contact = new ContactBL();
             
             // this user's info
             contact.set(userId);
             retValue.addParameter("first_name", contact.getEntity().getFirstName());
             retValue.addParameter("last_name", contact.getEntity().getLastName());
             retValue.addParameter("address1", contact.getEntity().getAddress1());
             retValue.addParameter("address2", contact.getEntity().getAddress2());
             retValue.addParameter("city", contact.getEntity().getCity());
             retValue.addParameter("organization_name", contact.getEntity().
                     getOrganizationName());
             retValue.addParameter("postal_code", contact.getEntity().getPostalCode());
             retValue.addParameter("state_province", contact.getEntity().getStateProvince());
             retValue.addParameter("username", user.getEntity().getUserName());
             retValue.addParameter("password", user.getEntity().getPassword());
             retValue.addParameter("user_id", user.getEntity().getUserId().
                     toString());
             
             // the entity info
             contact.setEntity(entityId);
             retValue.addParameter("company_id", entityId.toString());
             retValue.addParameter("company_name", contact.getEntity().
                     getOrganizationName());
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
         return retValue;
     }
     
     public String generatePaperInvoiceAsFile(InvoiceEntityLocal invoice) 
             throws SessionInternalError{
 
 		try {
             Integer entityId = invoice.getUser().getEntity().getId();
 
             // the language doesn't matter when getting a paper invoice
             MessageDTO paperMsg = getInvoicePaperMessage(
                     entityId, null, invoice.getUser().
                         getLanguageIdField(), invoice);
             PaperInvoiceNotificationTask task = 
                     new PaperInvoiceNotificationTask();
             PluggableTaskBL taskBL = new PluggableTaskBL();
             taskBL.set(entityId, Constants.PLUGGABLE_TASK_T_PAPER_INVOICE);
             task.initializeParamters(taskBL.getDTO());
              
             String filename = task.getPDFFile(invoice.getUser(), paperMsg);
             
 	        return filename; 			
 		} catch (Exception e) {
 			throw new SessionInternalError(e);
 		}    	
     }
 }
