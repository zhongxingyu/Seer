 package com.ac;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.apache.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.mgi.complaintproxyservice.client.AgentHeader;
 import com.mgi.complaintproxyservice.client.ComplaintProxyServicePortType_ComplaintProxyServiceSoap_Client;
 import com.mgi.complaintproxyservice.client.Header;
 import com.mgi.complaintproxyservice.client.InsertRecsIntoCRMExtWebFormRequest;
 import com.mgi.complaintproxyservice.client.InsertRecsIntoCRMExtWebFormResponse;
 import com.mgi.complaintproxyservice.client.ProcessingInstruction;
 import com.mgi.paypal.inputbean.SendMailInputBean;
 import com.mgi.paypal.response.SendMailOutputBean;
 import com.mgi.paypal.response.TransactionInformationMailResponse;
 import com.mgi.paypal.util.PropertyUtil;
 import com.thoughtworks.xstream.XStream;
 
 public class MailServiceBO {
 
 	Properties props = new Properties();
 	Session session;
 	public String subject;
 
 	private static Logger LOGGER = Logger.getLogger(MailServiceBO.class);
 
 	public MailServiceBO() {
 		props.put("mail.smtp.auth", "true");
 		props.put("mail.smtp.starttls.enable", "true");
 		props.put("mail.smtp.host", PropertyUtil.constantFromProperties.getString("AMAZON_MAIL_HOST"));
 		props.put("mail.smtp.port", PropertyUtil.constantFromProperties.getProperty("AMAZON_PORT"));
 		this.subject = PropertyUtil.constantFromProperties.getString("MAIL_SUBJECT");
 
 		session = Session.getInstance(props, new javax.mail.Authenticator() {
 			protected PasswordAuthentication getPasswordAuthentication() {
 				return new PasswordAuthentication(PropertyUtil.constantFromProperties.getString("MAIL_USER_NAME"),
 						PropertyUtil.constantFromProperties.getString("MAIL_PASSWORD"));
 			}
 		});
 
 	}
 
 	public String sendTransactionInformationMail(String customerEmailID,
 			String amount, String referenceNumber, String customerName,
 			String stateName, String fee) {
 
 		LOGGER.debug("Enter sendTransactionInformationMail.");
 
 		TransactionInformationMailResponse transactionInformationMailResponse = new TransactionInformationMailResponse();
 		Message message = new MimeMessage(session);
 
 		BigDecimal totalAmount = BigDecimal.ZERO;
 		if (amount != null && fee != null) {
 			totalAmount = new BigDecimal(amount).add(new BigDecimal(fee));
 		}
 
 		LOGGER.debug("Total Amount: " + totalAmount.toString());
 
 		try {
 			message.setFrom(new InternetAddress(PropertyUtil.constantFromProperties.getString("MAIL_FROM_ID")));
 			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
 					customerEmailID));
 
 			message.setSubject(subject);
 			
 			String hostUrl = PropertyUtil.constantFromProperties
 					.getString("HOST_URL");
 			String moneyGramLocator = PropertyUtil.constantFromProperties
 					.getString("MONEYGRAM_LOCATOR");

			String space = PropertyUtil.constantFromProperties
							.getString("SPACE");
 			
 			LOGGER.debug("host URL from Properties file : " + hostUrl);
 			LOGGER.debug("moneyGram Locator URL from properties file : " + moneyGramLocator);
 
 			String msg = "<html><head></head><style type='text/css'>body {width: 800px;}</style> "
 					+ "<body><img width='229' height='54' alt='MoneyGram' style='padding-right:5px' src='http://"
 					+ hostUrl
 					+ "/images/logo.png' /> "
					+ space
 					+ "<img width='139' height='41' alt='PayPal' src='http://"
 					+ hostUrl
 					+ "/images/logo_paypal.png' />"
 					+ " <p>Your money was <strong>Sent Successfully!</strong></p><p>"
 					+ customerName
 					+ "</p><h2>Thank you for using MoneyGram & PayPal to withdraw cash</h2>"
 					+ " <h3>Your cash was sent successfully and is available for pickup at "
 					+ "any <a href='http://"
 					+ moneyGramLocator
 					+ "/moneygram/en.html'>MoneyGram agent location.</a></h3>"
 					+ " <p>At the agent location, you will need to complete a simple Receive Money "
 					+ "Form with your Reference Number and show a valid photo ID with the name that matches "
 					+ "the Sender/Receiver of your transaction.</p>"
 					+ " <p>Please be sure to pick up your money within 45 days.</p> <div id='box'>"
 					+ " <table style='border: 1px solid #dddddc;' cellspacing='2'><tr style='background: #e0f3fb;'> <td>Sender/Receiver:</td>"
 					+ " <td>"
 					+ customerName
 					+ "</td> </tr><tr><td>Destination Country:</td><td>United States</td></tr> <tr style='background: #e0f3fb;'>"
 					+ " <td>Pickup Location:</td><td>"
 					+ stateName
 					+ "</td></tr><tr><td>Availability:</td><td>10 Minutes</td></tr>"
 					+ " <tr style='background: #e0f3fb;'><td>Receive Amount:</td><td>"
 					+ amount
 					+ " USD</td></tr><tr><td>Fee:</td>"
 					+ " <td>"
 					+ fee
 					+ " USD</td></tr><tr style='background: #e0f3fb;'><td><strong>Total to Withdraw from Balance:&nbsp;</strong>"
 					+ "</td><td>"
 					+ totalAmount.toString()
 					+ " USD</td></tr></table>"
 					+ " </div> <p><a href='http://"
 					+ moneyGramLocator
 					+ "/moneygram/en.html'>Find a MoneyGram location</a></p><p><strong>Thank you for using MoneyGram & PayPal</strong></p>"
					+ "  <div style='margin:auto; padding-top:1%; background: #303030; color: #999999; font-size: 12px; margin-top: 3em;'><span>&copy2013 MoneyGram."
 					+ " All rights reserved&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href='#'  title='Back to "
 					+ "Top'></a></span><a style='color:#999; border-left: 1px solid #999;float:right;padding: 0 5px;cursor:pointer;' href='https://"
 					+ hostUrl
 					+ "/privacypolicy.html'>Privacy "
 					+ "Policy</a>&nbsp;<a style='color:#999; border-left: 1px solid #999;float:right;padding: 0 5px;cursor:pointer;' href='https://"
 					+ hostUrl
 					+ "/termsofuse.html'>Terms"
 					+ " of Use</a>&nbsp;<a style='color:#999; border-left: 1px solid #999;float:right;padding: 0 5px;cursor:pointer;' href='https://"
 					+ hostUrl
 					+ "/terms_conditions.html'>Terms"
 					+ " and Conditions</a>&nbsp;<a style='color:#999; border-left: 1px solid #999;float:right;padding: 0 5px;cursor:pointer;' href='https://"
 					+ hostUrl
 					+ "/about-moneygram-paypal.html'>About MoneyGram</a>&nbsp;"
 					+ "<p style='padding-top:1%'>Licensed as a Money Transmitter by the Banking Department of the State of New York</p></div>"
 					+ "</body></html>";
 
 			message.setContent(msg, "text/html; charset=utf-8");
 			message.setSentDate(new Date());
 			Transport.send(message);
 		} catch (Exception exception) {
 			exception.printStackTrace();
			LOGGER.error("Mail not sent!!!. Please try again.");
 			LOGGER.error(exception.getLocalizedMessage());
 			transactionInformationMailResponse.setTransactionSuccess(false);
 			transactionInformationMailResponse.setMessageToUser(PropertyUtil.constantFromProperties.getString("MAIL_FAILED_MESSAGE"));
 			return new Gson().toJson(transactionInformationMailResponse);
 
 		}
 		transactionInformationMailResponse.setTransactionSuccess(true);
 		transactionInformationMailResponse.setMessageToUser(PropertyUtil.constantFromProperties.getString("MAIL_SUCCESS_MESSAGE"));
 
 		LOGGER.debug("Enter sendTransactionInformationMail.");
 
 		return new Gson().toJson(transactionInformationMailResponse);
 	}
 
 	private boolean sendMailIfRefund(SendMailInputBean sendMailInputBean) {
 		
 		LOGGER.debug("Enter sendMailIfRefund.");
 
 		Message message = new MimeMessage(session);
 
 		try {
 
 			message.setFrom(new InternetAddress(PropertyUtil.constantFromProperties.getString("MAIL_FROM_ID")));
 			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
 					PropertyUtil.constantFromProperties.getString("RECIPIENT_ID_IF_REFUND")));
 			message.setSubject(PropertyUtil.constantFromProperties.getString("SUBJECT_INQUIRY"));
 
 			LOGGER.debug(new Gson().toJson(sendMailInputBean));
 			String bodyOfMail = "";
 			String mailText = sendMailInputBean.getMailText();
 
 			if (mailText == null) {
 				mailText = "";
 			}
 
 			String customerName = "Name                  : "
 					+ sendMailInputBean.getFirstname().concat(" ")
 							.concat(sendMailInputBean.getLastName());
 			String refrenceNumber = "Reference Number      : "
 					+ sendMailInputBean.getReferenceNumber();
 			String customerEmailID = "EmailID               : "
 					+ sendMailInputBean.getCustomerEmailId();
 			String phoneNumber = sendMailInputBean.getPhoneNumber();
 			String amount = sendMailInputBean.getAmount();
 			if (phoneNumber == null) {
 				phoneNumber = "";
 			}
 			if (amount == null) {
 				amount = "";
 			}
 			phoneNumber = "Phone Number          : " + phoneNumber;
 			amount = "Amount                : " + amount;
 			bodyOfMail = "<html><body>"
 					     +customerName
 					     +"<br></br>"
 					     +refrenceNumber
 					     +"<br></br>"
 					     +amount
 					     +"<br></br>"
 					     +customerEmailID
 					     +"<br></br>"
 					     +phoneNumber
 					     +"<br></br>"
 					     +mailText
 					     +"</body></html>";
 
 			LOGGER.debug(bodyOfMail);
 
 			message.setContent(bodyOfMail, "text/html; charset=utf-8");
 			message.setSentDate(new Date());
 			Transport.send(message);
 
 		} catch (Exception exception) {
 			exception.printStackTrace();
 			LOGGER.error(exception.getLocalizedMessage());
 			return false;
 
 		}
 
 		LOGGER.debug("Exit sendMail Refund.");
 
 		return true;
 
 	}
 
 	public String sendReportInformationMail(SendMailInputBean sendMailInputBean) {
 
 		LOGGER.debug("Enter sendReportInformationMail.");
 
 		if (sendMailInputBean.getMailSubject().trim().toLowerCase()
 				.equals("general")) {
 			return sendReportInformationMailIfGeneral(sendMailInputBean);
 		} else {
 			SendMailOutputBean sendMailOutputBean = new SendMailOutputBean();
 			if (sendMailIfRefund(sendMailInputBean)) {
 				sendMailOutputBean.setTransactionSuccess(true);
 				sendMailOutputBean
 						.setMessageToUser(PropertyUtil.constantFromProperties
 								.getString("MAIL_SUCCESS_MESSAGE"));
 
 			} else {
 				sendMailOutputBean.setTransactionSuccess(false);
 				sendMailOutputBean.setMailSubject(sendMailInputBean
 						.getMailSubject());
 				sendMailOutputBean.setMailText(sendMailInputBean.getMailText());
 				sendMailOutputBean.setCustomerEmailId(sendMailInputBean
 						.getCustomerEmailId());
 				sendMailOutputBean
 						.setMessageToUser(PropertyUtil.constantFromProperties
 								.getString("MAIL_FAILED_MESSAGE"));
 
 			}
 
 			LOGGER.debug("Exit sendReportInformationMail.");
 
 			return new Gson().toJson(sendMailOutputBean);
 		}
 	}
 
 	private String sendReportInformationMailIfGeneral(
 			SendMailInputBean sendMailInputBean) {
 
 		LOGGER.debug("Enter sendReportInformationMailIfGeneral.");
 
 		SendMailOutputBean sendMailOutputBean = new SendMailOutputBean();
 		InsertRecsIntoCRMExtWebFormResponse insertRecsIntoCRMExtWebFormResponse = null;
 		try {
 			InsertRecsIntoCRMExtWebFormRequest insertRecsIntoCRMExtWebFormRequest = new InsertRecsIntoCRMExtWebFormRequest();
 			insertRecsIntoCRMExtWebFormRequest
 					.setWhoCompletingForm("MoneyGram Consumer");
 			insertRecsIntoCRMExtWebFormRequest.setFirstname(sendMailInputBean
 					.getFirstname());
 			insertRecsIntoCRMExtWebFormRequest.setLastname(sendMailInputBean
 					.getLastName());
 			insertRecsIntoCRMExtWebFormRequest
 					.setEmailaddress(sendMailInputBean.getCustomerEmailId());
 			insertRecsIntoCRMExtWebFormRequest.setDescription(sendMailInputBean
 					.getMailText());
 			insertRecsIntoCRMExtWebFormRequest.setTrnRefNbr(sendMailInputBean
 					.getReferenceNumber());
 			insertRecsIntoCRMExtWebFormRequest.setTelephone(sendMailInputBean
 					.getPhoneNumber());
 			insertRecsIntoCRMExtWebFormRequest
 					.setComplaintRequestType(PropertyUtil.constantFromProperties
 							.getString("CPS_REQUEST_TYPE"));
 			insertRecsIntoCRMExtWebFormRequest.setPreferContMethod("Email");
 			Header header = new Header();
 			AgentHeader agentHeader = new AgentHeader();
 			agentHeader.setAgentId(PropertyUtil.constantFromProperties
 					.getString("AGENT_ID"));
 			header.setAgent(agentHeader);
 			ProcessingInstruction processingInstruction = new ProcessingInstruction();
 			processingInstruction.setAction("InsertRecsIntoCRMExtWebForm");
 			processingInstruction.setRollbackTransaction(false);
 			header.setProcessingInstruction(processingInstruction);
 			insertRecsIntoCRMExtWebFormRequest.setHeader(header);
 			
 			LOGGER.debug(new XStream().toXML(insertRecsIntoCRMExtWebFormRequest));
 			
 			ComplaintProxyServicePortType_ComplaintProxyServiceSoap_Client client 
 			= new ComplaintProxyServicePortType_ComplaintProxyServiceSoap_Client();
 			insertRecsIntoCRMExtWebFormResponse = client
 			.insertRecsIntoCRMExtWebForm(insertRecsIntoCRMExtWebFormRequest);
 			
 			LOGGER.debug(new XStream().toXML(insertRecsIntoCRMExtWebFormResponse));
 			
 		} catch (Exception exception) {
 			exception.printStackTrace();
 			return cpsFailed(sendMailInputBean, sendMailOutputBean);
 		}
 
 		if (insertRecsIntoCRMExtWebFormResponse == null) {
 			return cpsFailed(sendMailInputBean, sendMailOutputBean);
 		}
 
 		sendMailOutputBean.setTransactionSuccess(true);
 		sendMailOutputBean.setMessageToUser(PropertyUtil.messageFromProperties
 				.getString("MAIL_SUCCESS").concat(
 						insertRecsIntoCRMExtWebFormResponse
 								.getInsertRecsIntoCRMExtWebFormResult()));
 
 		LOGGER.debug("Exit sendReportInformationMailIfGeneral.");
 
 		return new Gson().toJson(sendMailOutputBean);
 	}
 
 	private String cpsFailed(SendMailInputBean sendMailInputBean,
 			SendMailOutputBean sendMailOutputBean) {
 
 		LOGGER.debug(new Gson().toJson(sendMailInputBean));
 		sendMailOutputBean.setTransactionSuccess(false);
 		sendMailOutputBean.setMailSubject(sendMailInputBean.getMailSubject());
 		sendMailOutputBean.setMailText(sendMailInputBean.getMailText());
 		sendMailOutputBean.setCustomerEmailId(sendMailInputBean
 				.getCustomerEmailId());
 		sendMailOutputBean.setMessageToUser(PropertyUtil.messageFromProperties
 				.getString("RESEND_MAIL"));
 		return new Gson().toJson(sendMailOutputBean);
 	}
 
 }
