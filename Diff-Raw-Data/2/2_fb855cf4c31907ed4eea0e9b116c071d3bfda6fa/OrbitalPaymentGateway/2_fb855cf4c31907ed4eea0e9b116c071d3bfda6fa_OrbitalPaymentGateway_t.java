 package com.orangeleap.tangerine.service.payments;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.validation.BindException;
 
 import com.orangeleap.tangerine.domain.communication.Address;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.service.ErrorLogService;
 import com.orangeleap.tangerine.service.GiftService;
 import com.orangeleap.tangerine.service.SiteService;
 import com.paymentech.orbital.sdk.configurator.Configurator;
 import com.paymentech.orbital.sdk.configurator.ConfiguratorIF;
 import com.paymentech.orbital.sdk.interfaces.RequestIF;
 import com.paymentech.orbital.sdk.interfaces.ResponseIF;
 import com.paymentech.orbital.sdk.interfaces.TransactionProcessorIF;
 import com.paymentech.orbital.sdk.request.FieldNotFoundException;
 import com.paymentech.orbital.sdk.request.Request;
 import com.paymentech.orbital.sdk.transactionProcessor.TransactionProcessor;
 import com.paymentech.orbital.sdk.util.exceptions.InitializationException;
 
 //@Service("paymentGateway")
 public class OrbitalPaymentGateway implements CreditCardPaymentGateway {
 	/** Logger for this class and subclasses */
 	protected final Log logger = LogFactory.getLog(getClass());
 
 	private String configFile;
 	protected static ConfiguratorIF configurator = null;
 	private TransactionProcessorIF tp = null;
 
 	private ApplicationContext applicationContext;
 
 	public OrbitalPaymentGateway() {
 	}
 
 	void Initialize() {
 		ErrorLogService errorLogServce = (ErrorLogService)applicationContext.getBean("errorLogService");
 		
 		try {
 			configurator = Configurator.getInstance(configFile);
 		} catch (InitializationException ie) {
 			if (logger.isErrorEnabled()) {
 				logger.error("Configurator initialization failed.");
 				errorLogServce.addErrorMessage(ie.getMessage(), "OrbitalPaymentGateway.config");
 			}
 		}
 
 		try {
 			tp = new TransactionProcessor();
 		} catch (InitializationException iex) {
 			if (logger.isErrorEnabled()) {
 				logger.error(iex.getMessage());
 				errorLogServce.addErrorMessage(iex.getMessage(), "OrbitalPaymentGateway.initialize");
 			}
 		}
 	}
 
 	@Override
 	public void AuthorizeAndCapture(Gift gift) {
 		RequestIF request = null;
 		String month;
 		String year;
 
 		if (configurator == null) {
             Initialize();
         }
 
 		try {
 			//
 			// make sure the site information is loaded
 			SiteService ss = (SiteService) applicationContext.getBean("siteService");
 			gift.getConstituent().setSite(ss.readSite(gift.getConstituent().getSite().getName()));
 			
 			//
 			// Create the request
 			// Tell the request object which template to use (see
 			// RequestIF.java)
 			request = new Request(RequestIF.NEW_ORDER_TRANSACTION);
 
 			// Basic Information
 			request.setFieldValue("IndustryType", "EC");
 			request.setFieldValue("MessageType", "AC");
 			request.setFieldValue("MerchantID", gift.getSite().getMerchantNumber());
 			request.setFieldValue("BIN", gift.getSite().getMerchantBin());
 			request.setFieldValue("TerminalID", gift.getSite().getMerchantTerminalId());
 			request.setFieldValue("OrderID", gift.getId().toString());
 			request.setFieldValue("AccountNum", gift.getSelectedPaymentSource()
 					.getCreditCardNumber());
 			
 			String amount = gift.getAmount().toString();
 			if(amount.contains(".")) { 
 				amount = amount.substring(0,amount.indexOf('.')) + amount.substring(amount.indexOf('.') + 1);
 				
 			}
 			else amount += "00";
 			
 			request.setFieldValue("Amount", amount);
 
 			month = gift.getSelectedPaymentSource().getCreditCardExpirationMonthText();
 			year  = gift.getSelectedPaymentSource().getCreditCardExpirationYear().toString();
 			
 			if (month.length() == 1) month = "0" + month;
 			
 			request.setFieldValue("Exp", month + year);
 
 			// AVS Information
 			Address addr = gift.getSelectedAddress();
 			
 			if (addr != null && addr.isValid()) {
 				request.setFieldValue("AVSname", gift.getSelectedPaymentSource()
 						.getCreditCardHolderName());
 				request.setFieldValue("AVSaddress1", addr.getAddressLine1());
                 request.setFieldValue("AVSaddress2", addr.getAddressLine2());
 				request.setFieldValue("AVScity", addr.getCity());
 				request.setFieldValue("AVSstate", addr.getStateProvince());
 				request.setFieldValue("AVSzip", addr.getPostalCode());
//                request.setFieldValue("AVScountrycode",addr.getCountry());
 			}
 
 			if (logger.isInfoEnabled()) {
 				logger.info(request.getXML());
 			}
 		} catch (InitializationException ie) {
 			if (logger.isErrorEnabled()) {
 				logger.error("Unable to initialize request object.");
 			}
 			return;
 		} catch (FieldNotFoundException fnfe) {
 			if (logger.isErrorEnabled()) {
 				logger.error("Unable to find XML field in template.");
 				logger.error(fnfe.getMessage());
 			}
 			return;
 		} catch (Exception e) {
 			if (logger.isErrorEnabled()) {
 				logger.error("General: " + e.getMessage());
 			}
 			return;
 		}
 
 		// Process the transaction
 		ResponseIF response = null;
 		try {
 			response = tp.process(request);
 		} catch (Exception text) {
 			gift.setPaymentStatus("Error");
 			gift.setPaymentMessage(text.getMessage());
 			GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
 			gift.setSuppressValidation(true);
 	        try {
 	        	gs.maintainGift(gift);
 	        } catch (BindException e) {
 	        	// Should not happen with suppressValidation = true.
 	        	logger.error(e);
 	        }
 
 			if (logger.isErrorEnabled()) {
 				logger.error("Request: "+ text.getMessage());
 			}
 			return;
 		}
 
 		if (logger.isInfoEnabled()) {
 			logger.info(response.toXmlString());
 		}
 
 		// if you get here the card has been charged and everything is groovy
 		// set the auth code on the gift and return
 		if (response.isApproved()) {
 			gift.setAuthCode(response.getAuthCode());
 			gift.setTxRefNum(response.getTxRefNum());
 			gift.setGiftStatus("Paid");
 			gift.setPaymentStatus("Approved");
 //			gift.setPaymentStatus(response.getStatus());
 			gift.setPaymentMessage(response.getMessage());
 //			gift.setComments(response.getMessage());
 		} else {
 			gift.setGiftStatus("Not Paid");
 			gift.setPaymentStatus("Declined");
 			gift.setPaymentMessage(response.getMessage());
 //			gift.setComments(response.getMessage());
 		}
 		GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
         gift.setSuppressValidation(true);
         try {
         	gs.maintainGift(gift);
         } catch (BindException e) {
         	// Should not happen with suppressValidation = true.
         	logger.error(e);
         }
 
 
 	}
 
 	public void setConfigFile(String f) {
 		configFile = f;
 	}
 
 	public String getConfigFile() {
 		return configFile;
 	}
 
 	@Override
 	public void Authorize(Gift gift) {
 		RequestIF request = null;
 		String month;
 		String year;
 		
 		if (configurator == null) {
             Initialize();
         }
 
 		try {
 			//
 			// make sure the site information is loaded
 			SiteService ss = (SiteService) applicationContext.getBean("siteService");
 			gift.getConstituent().setSite(ss.readSite(gift.getConstituent().getSite().getName()));
 			
 			//
 			// Create the request
 			// Tell the request object which template to use (see
 			// RequestIF.java)
 			request = new Request(RequestIF.NEW_ORDER_TRANSACTION);
 
 			// Basic Information
 			request.setFieldValue("IndustryType", "EC");
 			request.setFieldValue("MessageType", "A");
 			request.setFieldValue("MerchantID", gift.getSite()
 					.getMerchantNumber());
 			request.setFieldValue("BIN", gift.getSite().getMerchantBin());
 			request.setFieldValue("TerminalID", gift.getSite().getMerchantTerminalId());
 			request.setFieldValue("OrderID", gift.getId().toString());
 			request.setFieldValue("AccountNum", gift.getSelectedPaymentSource()
 					.getCreditCardNumber());
 
             String amount = gift.getAmount().toString();
 			if(amount.contains(".")) {
 				amount = amount.substring(0,amount.indexOf('.')) + amount.substring(amount.indexOf('.') + 1);
 
 			}
 			else amount += "00";
 
 			request.setFieldValue("Amount", amount);
 			
 			month = gift.getSelectedPaymentSource().getCreditCardExpirationMonthText();
 			year  = gift.getSelectedPaymentSource().getCreditCardExpirationYear().toString();
 			
 			if (month.length() == 1) month = "0" + month;
 			
 			request.setFieldValue("Exp", month + year);
 
 			// AVS Information
 			Address addr = gift.getSelectedAddress();
 			if (addr != null && addr.isValid()) {
 				request.setFieldValue("AVSname", gift.getSelectedPaymentSource()
 						.getCreditCardHolderName());
 				request.setFieldValue("AVSaddress1", addr.getAddressLine1());
 				request.setFieldValue("AVScity", addr.getCity());
 				request.setFieldValue("AVSstate", addr.getStateProvince());
 				request.setFieldValue("AVSzip", addr.getPostalCode());
 			}
 
 			if (logger.isInfoEnabled()) {
 				logger.info(request.getXML());
 			}
 		} catch (InitializationException ie) {
 			if (logger.isErrorEnabled()) {
 				logger.error("Unable to initialize request object.");
 			}
 			return;
 		} catch (FieldNotFoundException fnfe) {
 			if (logger.isErrorEnabled()) {
 				logger.error("Unable to find XML field in template.");
 				logger.error(fnfe.getMessage());
 			}
 			return;
 		} catch (Exception e) {
 			if (logger.isErrorEnabled()) {
 				logger.error(e.getMessage());
 			}
 			return;
 		}
 
 		// Process the transaction
 		ResponseIF response = null;
 		try {
 			response = tp.process(request);
 		} catch (Exception tex) {
 			if (logger.isErrorEnabled()) {
 				logger.error(tex.getMessage());
 			}
 			return;
 		}
 
 		if (logger.isInfoEnabled()) {
 			logger.info(response.toXmlString());
 		}
 
 		// if you get here the card has been charged and everything is groovy
 		// set the auth code on the gift and return
 		if (response.isApproved()) {
 			gift.setAuthCode(response.getAuthCode());
 			gift.setTxRefNum(response.getTxRefNum());
 //			gift.setGiftStatus("Paid");
 			gift.setPaymentStatus("Approved");
 			gift.setPaymentMessage(response.getMessage());
 		} else {
 			gift.setGiftStatus("Not Paid");
 			gift.setPaymentStatus("Declined");
 			gift.setPaymentMessage(response.getMessage());
 		}
 		
 		GiftService gs = (GiftService) applicationContext.getBean("giftService");
 		
         gift.setSuppressValidation(true);
         try {
         	gs.maintainGift(gift);
         } catch (BindException e) {
         	// Should not happen with suppressValidation = true.
         	logger.error(e);
         }
 
 
 	}
 
 	@Override
 	public void Capture(Gift gift) {
 		RequestIF request = null;
 
 		if (configurator == null) {
             Initialize();
         }
 
 		try {
 			//
 			// make sure the site information is loaded
 			SiteService ss = (SiteService) applicationContext.getBean("siteService");
 			gift.getConstituent().setSite(ss.readSite(gift.getConstituent().getSite().getName()));
 			
 			//
 			// Create the request
 			// Tell the request object which template to use (see
 			// RequestIF.java)
 			request = new Request(RequestIF.MARK_FOR_CAPTURE_TRANSACTION);
 
 			// Basic Information
 			request.setFieldValue("TxRefNum", gift.getTxRefNum());
 			request.setFieldValue("MerchantID", gift.getSite()
 					.getMerchantNumber());
 			request.setFieldValue("BIN", gift.getSite().getMerchantBin());
 			request.setFieldValue("TerminalID", gift.getSite().getMerchantTerminalId());
 			request.setFieldValue("OrderID", gift.getId().toString());
 
             String amount = gift.getAmount().toString();
 			if(amount.contains(".")) {
 				amount = amount.substring(0,amount.indexOf('.')) + amount.substring(amount.indexOf('.') + 1);
 
 			}
 			else amount += "00";
 			request.setFieldValue("Amount", amount);
 
 			if (logger.isInfoEnabled()) {
 				logger.info(request.getXML());
 			}
 		} catch (InitializationException ie) {
 			if (logger.isErrorEnabled()) {
 				logger.error("Unable to initialize request object.");
 			}
 			return;
 		} catch (FieldNotFoundException fnfe) {
 			if (logger.isErrorEnabled()) {
 				logger.error("Unable to find XML field in template.");
 				logger.error(fnfe.getMessage());
 			}
 			return;
 		} catch (Exception e) {
 			if (logger.isErrorEnabled()) {
 				logger.error(e.getMessage());
 			}
 			return;
 		}
 
 		// Process the transaction
 		ResponseIF response = null;
 		try {
 			response = tp.process(request);
 		} catch (Exception tex) {
 			if (logger.isErrorEnabled()) {
 				logger.error(tex.getMessage());
 			}
 			return;
 		}
 
 		if (logger.isInfoEnabled()) {
 			logger.info(response.toXmlString());
 		}
 
 		// if you get here the card has been charged and everything is groovy
 		// set the auth code on the gift and return
 		if (response.isApproved()) {
 			gift.setAuthCode(response.getAuthCode());
 			gift.setTxRefNum(response.getTxRefNum());
 			gift.setGiftStatus("Paid");
 			gift.setPaymentStatus("Approved");
 			gift.setPaymentMessage(response.getMessage());
 		} else {
 			gift.setGiftStatus("Not Paid");
 			gift.setPaymentStatus("Declined");
 			gift.setPaymentMessage(response.getMessage());
 		}
 		GiftService gs = (GiftService) applicationContext.getBean("giftService");
 	
         gift.setSuppressValidation(true);
         try {
         	gs.maintainGift(gift);
         } catch (BindException e) {
         	// Should not happen with suppressValidation = true.
         	logger.error(e);
         }
 
 	}
 
 	@Override
 	public void Refund(Gift gift) {
 
 	}
 
 	private ResponseIF process(RequestIF request) {
 		return null;
 	}
 
 	@Override
 	public void setApplicationContext(ApplicationContext applicationContext)
 			throws BeansException {
 		this.applicationContext = applicationContext;
 		
 	}
 }
