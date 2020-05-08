 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.service.payments;
 
 import org.apache.commons.logging.Log;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.validation.BindException;
 
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.communication.Address;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.service.ErrorLogService;
 import com.orangeleap.tangerine.service.GiftService;
 import com.orangeleap.tangerine.service.OrangeleapJmxNotificationBean;
 import com.orangeleap.tangerine.service.SiteService;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
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
     /**
      * Logger for this class and subclasses
      */
     protected final Log logger = OLLogger.getLog(getClass());
 
     private String configFile;
     protected static ConfiguratorIF configurator = null;
     private TransactionProcessorIF tp = null;
 
     private ApplicationContext applicationContext;
 	private OrangeleapJmxNotificationBean orangeleapJmxNotificationBean;
 
     public OrbitalPaymentGateway() {
     }
 
     void Initialize() {
         ErrorLogService errorLogServce = (ErrorLogService) applicationContext.getBean("errorLogService");
         orangeleapJmxNotificationBean = (OrangeleapJmxNotificationBean) applicationContext.getBean("OrangeleapJmxNotificationBean");
 
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
         	orangeleapJmxNotificationBean.setStat(OrangeleapJmxNotificationBean.INIT, OrangeleapJmxNotificationBean.ORBITAL_PAYMENT_STATUS, OrangeleapJmxNotificationBean.OK);
         } catch (InitializationException iex) {
             if (logger.isErrorEnabled()) {
                 logger.error(iex.getMessage());
                 errorLogServce.addErrorMessage(iex.getMessage(), "OrbitalPaymentGateway.initialize");
             	orangeleapJmxNotificationBean.publishNotification(OrangeleapJmxNotificationBean.ORBITAL_PAYMENT_ERROR, ""+iex.getMessage());
             	orangeleapJmxNotificationBean.setStat(OrangeleapJmxNotificationBean.INIT, OrangeleapJmxNotificationBean.ORBITAL_PAYMENT_STATUS, OrangeleapJmxNotificationBean.ERROR);
             }
         }
 
     }
 
     @Override
     public void AuthorizeAndCapture(Gift gift) {
         RequestIF request = null;
         String month = null;
         String year = null;
 
         if (configurator == null) {
             Initialize();
         }
 
         try {
             //
             // make sure the site information is loaded
             SiteService ss = (SiteService) applicationContext.getBean("siteService");
 			Site site = ss.readSite(gift.getConstituent().getSite().getName());
 			if (site.getMerchantBin() == null || site.getMerchantNumber() == null || site.getMerchantTerminalId() == null){
 	            if (logger.isErrorEnabled()) {
 	                logger.error("General: " + "Some or all of the Merchant Site Settings are null.");
 	            }
 	            gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
 	        	gift.setPaymentMessage("Payment not processed: Merchant Site Settings are null. ");
 	            GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
 	            gift.setSuppressValidation(true);
 	            try {
 	                gs.maintainGift(gift);
 	            } catch (BindException be) {
 	                // Should not happen with suppressValidation = true.
 	                logger.error(be);
 	            }
 
 	            return;
 			}
 			gift.getConstituent().setSite(site);
 
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
             request.setFieldValue("AVSname", gift.getPaymentSource().getCreditCardHolderName());
             request.setFieldValue("AccountNum", gift.getPaymentSource()
                     .getCreditCardNumber());
 
             String amount = gift.getAmount().toString();
             if (amount.contains(".")) {
                 amount = amount.substring(0, amount.indexOf('.')) + amount.substring(amount.indexOf('.') + 1);
 
             } else {
                 amount += "00";
             }
 
             request.setFieldValue("Amount", amount);
 
             if (gift.getPaymentSource() != null) {
                 month = gift.getPaymentSource().getCreditCardExpirationMonthText();
                 year = gift.getPaymentSource().getCreditCardExpirationYear().toString();
             }
 
             if (month != null && month.length() == 1) {
                 month = "0" + month;
             }
             if (month != null && year != null) {
                 request.setFieldValue("Exp", year + month) ;
             }
 
             // AVS Information
             Address addr = gift.getAddress();
 
             if (addr != null && addr.isValid()) {
                 request.setFieldValue("AVSname", gift.getPaymentSource()
                         .getCreditCardHolderName());
                 request.setFieldValue("AVSaddress1", addr.getAddressLine1());
                 request.setFieldValue("AVSaddress2", addr.getAddressLine2());
                 request.setFieldValue("AVScity", addr.getCity());
                 request.setFieldValue("AVSstate", addr.getStateProvince());
                 request.setFieldValue("AVSzip", addr.getPostalCode());
 //                request.setFieldValue("AVScountrycode",addr.getCountry());
             }
 
             if (gift.getPaymentSource() != null && gift.getPaymentSource().getCreditCardSecurityCode() != null &&
                     !gift.getPaymentSource().getCreditCardSecurityCode().equals(StringConstants.EMPTY)) {
                request.setFieldValue("CardVerifyNumber", gift.getPaymentSource().getCreditCardSecurityCode().toString());
             }
 
             if (logger.isInfoEnabled()) {
                 logger.info(request.getXML());
             }
         } catch (InitializationException ie) {
             if (logger.isErrorEnabled()) {
                 logger.error("Unable to initialize request object.");
             }
         	gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
             gift.setPaymentMessage(ie.getMessage());
             GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
             gift.setSuppressValidation(true);
             try {
                 gs.maintainGift(gift);
             } catch (BindException e) {
                 // Should not happen with suppressValidation = true.
                 logger.error(e);
             }
 
             return;
         } catch (FieldNotFoundException fnfe) {
             if (logger.isErrorEnabled()) {
                 logger.error("Unable to find XML field in template.");
                 logger.error(fnfe.getMessage());
             }
         	gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
             gift.setPaymentMessage(fnfe.getMessage());
             GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
             gift.setSuppressValidation(true);
             try {
                 gs.maintainGift(gift);
             } catch (BindException e) {
                 // Should not happen with suppressValidation = true.
                 logger.error(e);
             }
 
             return;
         } catch (Exception e) {
             if (logger.isErrorEnabled()) {
                 logger.error("General: " + e.getMessage());
             }
         	gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
             gift.setPaymentMessage(e.getMessage());
             GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
             gift.setSuppressValidation(true);
             try {
                 gs.maintainGift(gift);
             } catch (BindException be) {
                 // Should not happen with suppressValidation = true.
                 logger.error(e);
             }
 
             return;
         }
 
         // Process the transaction
         ResponseIF response = null;
         try {
 
         	response = tp.process(request);
 
             orangeleapJmxNotificationBean.incrementStatCount(gift.getSite().getName(), OrangeleapJmxNotificationBean.AUTHORIZE_AND_CAPTURE);
         	orangeleapJmxNotificationBean.setStat(gift.getSite().getName(), OrangeleapJmxNotificationBean.ORBITAL_PAYMENT_STATUS, OrangeleapJmxNotificationBean.OK);
 
         } catch (Exception text) {
 
             if (logger.isErrorEnabled()) {
                 logger.error("Request: " + text.getMessage());
             	orangeleapJmxNotificationBean.publishNotification(OrangeleapJmxNotificationBean.ORBITAL_PAYMENT_ERROR, ""+text.getMessage());
             	orangeleapJmxNotificationBean.setStat(gift.getSite().getName(), OrangeleapJmxNotificationBean.ORBITAL_PAYMENT_STATUS, OrangeleapJmxNotificationBean.ERROR);
             }
 
         	gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
             gift.setPaymentMessage(text.getMessage());
 
             GiftService gs = (GiftService) applicationContext.getBean("giftService");
             gift.setSuppressValidation(true);
             try {
                 gs.maintainGift(gift);
             } catch (BindException e) {
                 // Should not happen with suppressValidation = true.
                 logger.error(e);
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
             gift.setGiftStatus(Gift.STATUS_PAID);
             gift.setPaymentStatus(Gift.PAY_STATUS_APPROVED);
 //			gift.setPaymentStatus(response.getStatus());
             gift.setPaymentMessage(response.getMessage());
 //			gift.setComments(response.getMessage());
             gift.setAvsMessage(response.getAVSResponseCode());
         } else {
             gift.setGiftStatus(Gift.STATUS_NOT_PAID);
             gift.setPaymentStatus(Gift.PAY_STATUS_DECLINED);
             gift.setPaymentMessage(response.getMessage() + " Paymentech response code: " + response.getResponseCode());
 //			gift.setComments(response.getMessage());
             gift.setAvsMessage(response.getAVSResponseCode());
             logger.error("Paymentech response code " + response.getResponseCode());
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
         String month = null;
         String year = null;
 
         if (configurator == null) {
             Initialize();
         }
 
         try {
             //
             // make sure the site information is loaded
             SiteService ss = (SiteService) applicationContext.getBean("siteService");
 			Site site = ss.readSite(gift.getConstituent().getSite().getName());
 			if (site.getMerchantBin() == null || site.getMerchantNumber() == null || site.getMerchantTerminalId() == null){
 	            if (logger.isErrorEnabled()) {
 	                logger.error("General: " + "Some or all of the Merchant Site Settings are null.");
 	            }
 	            gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
 	        	gift.setPaymentMessage("Payment not processed: Merchant Site Settings are null. ");
 	            GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
 	            gift.setSuppressValidation(true);
 	            try {
 	                gs.maintainGift(gift);
 	            } catch (BindException be) {
 	                // Should not happen with suppressValidation = true.
 	                logger.error(be);
 	            }
 
 	            return;
 			}
 			gift.getConstituent().setSite(site);
 
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
             request.setFieldValue("AVSname", gift.getPaymentSource().getCreditCardHolderName());
             request.setFieldValue("AccountNum", gift.getPaymentSource()
                     .getCreditCardNumber());
 
             String amount = gift.getAmount().toString();
             if (amount.contains(".")) {
                 amount = amount.substring(0, amount.indexOf('.')) + amount.substring(amount.indexOf('.') + 1);
 
             } else {
                 amount += "00";
             }
 
             request.setFieldValue("Amount", amount);
 
             if (gift.getPaymentSource() != null) {
                 month = gift.getPaymentSource().getCreditCardExpirationMonthText();
                 year = gift.getPaymentSource().getCreditCardExpirationYear().toString();
             }
 
             if (month != null && month.length() == 1) {
                 month = "0" + month;
             }
 
             if (month != null && year != null) {
                 request.setFieldValue("Exp", year + month);
             }
 
             // AVS Information
             Address addr = gift.getAddress();
             if (addr != null && addr.isValid()) {
                 request.setFieldValue("AVSname", gift.getPaymentSource()
                         .getCreditCardHolderName());
                 request.setFieldValue("AVSaddress1", addr.getAddressLine1());
                 request.setFieldValue("AVScity", addr.getCity());
                 request.setFieldValue("AVSstate", addr.getStateProvince());
                 request.setFieldValue("AVSzip", addr.getPostalCode());
             }
 
             if (gift.getPaymentSource() != null && gift.getPaymentSource().getCreditCardSecurityCode() != null &&
                     !gift.getPaymentSource().getCreditCardSecurityCode().equals("")) {
                 request.setFieldValue("CardVerifyNumber", gift.getPaymentSource().getCreditCardSecurityCode().toString());
             }
 
             if (logger.isInfoEnabled()) {
                 logger.info(request.getXML());
             }
         } catch (InitializationException ie) {
             if (logger.isErrorEnabled()) {
                 logger.error("Unable to initialize request object.");
             }
         	gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
             gift.setPaymentMessage(ie.getMessage());
             GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
             gift.setSuppressValidation(true);
             try {
                 gs.maintainGift(gift);
             } catch (BindException e) {
                 // Should not happen with suppressValidation = true.
                 logger.error(e);
             }
 
             return;
         } catch (FieldNotFoundException fnfe) {
             if (logger.isErrorEnabled()) {
                 logger.error("Unable to find XML field in template.");
                 logger.error(fnfe.getMessage());
             }
         	gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
             gift.setPaymentMessage(fnfe.getMessage());
             GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
             gift.setSuppressValidation(true);
             try {
                 gs.maintainGift(gift);
             } catch (BindException e) {
                 // Should not happen with suppressValidation = true.
                 logger.error(e);
             }
 
             return;
         } catch (Exception e) {
             if (logger.isErrorEnabled()) {
                 logger.error(e.getMessage());
             }
         	gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
             gift.setPaymentMessage(e.getMessage());
             GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
             gift.setSuppressValidation(true);
             try {
                 gs.maintainGift(gift);
             } catch (BindException be) {
                 // Should not happen with suppressValidation = true.
                 logger.error(e);
             }
 
             return;
         }
 
         // Process the transaction
         ResponseIF response = null;
         try {
 
         	response = tp.process(request);
 
         	orangeleapJmxNotificationBean.incrementStatCount(gift.getSite().getName(), OrangeleapJmxNotificationBean.AUTHORIZE);
         	orangeleapJmxNotificationBean.setStat(gift.getSite().getName(), OrangeleapJmxNotificationBean.ORBITAL_PAYMENT_STATUS, OrangeleapJmxNotificationBean.OK);
 
         } catch (Exception tex) {
             if (logger.isErrorEnabled()) {
                 logger.error(tex.getMessage());
             	orangeleapJmxNotificationBean.publishNotification(OrangeleapJmxNotificationBean.ORBITAL_PAYMENT_ERROR, ""+tex.getMessage());
             	orangeleapJmxNotificationBean.setStat(gift.getSite().getName(), OrangeleapJmxNotificationBean.ORBITAL_PAYMENT_STATUS, OrangeleapJmxNotificationBean.ERROR);
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
 			gift.setGiftStatus(Gift.STATUS_PENDING);
             gift.setPaymentStatus(Gift.PAY_STATUS_AUTHORIZED);
             gift.setPaymentMessage(response.getMessage());
         } else {
             gift.setGiftStatus(Gift.STATUS_NOT_PAID);
             gift.setPaymentStatus(Gift.PAY_STATUS_DECLINED);
             gift.setPaymentMessage(response.getMessage() + "Paymentech response code: " + response.getResponseCode());
             logger.error("Paymentech response code " + response.getResponseCode());
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
 			Site site = ss.readSite(gift.getConstituent().getSite().getName());
 			if (site.getMerchantBin() == null || site.getMerchantNumber() == null || site.getMerchantTerminalId() == null){
 	            if (logger.isErrorEnabled()) {
 	                logger.error("General: " + "Some or all of the Merchant Site Settings are null.");
 	            }
 	            gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
 	        	gift.setPaymentMessage("Payment not processed: Merchant Site Settings are null. ");
 	            GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
 	            gift.setSuppressValidation(true);
 	            try {
 	                gs.maintainGift(gift);
 	            } catch (BindException be) {
 	                // Should not happen with suppressValidation = true.
 	                logger.error(be);
 	            }
 
 	            return;
 			}
 			gift.getConstituent().setSite(site);
 
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
             if (amount.contains(".")) {
                 amount = amount.substring(0, amount.indexOf('.')) + amount.substring(amount.indexOf('.') + 1);
 
             } else {
                 amount += "00";
             }
             request.setFieldValue("Amount", amount);
 
             if (logger.isInfoEnabled()) {
                 logger.info(request.getXML());
             }
         } catch (InitializationException ie) {
             if (logger.isErrorEnabled()) {
                 logger.error("Unable to initialize request object.");
             }
         	gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
             gift.setPaymentMessage(ie.getMessage());
             GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
             gift.setSuppressValidation(true);
             try {
                 gs.maintainGift(gift);
             } catch (BindException e) {
                 // Should not happen with suppressValidation = true.
                 logger.error(e);
             }
 
             return;
         } catch (FieldNotFoundException fnfe) {
             if (logger.isErrorEnabled()) {
                 logger.error("Unable to find XML field in template.");
                 logger.error(fnfe.getMessage());
             }
             gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
             gift.setPaymentMessage(fnfe.getMessage());
             GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
             gift.setSuppressValidation(true);
             try {
                 gs.maintainGift(gift);
             } catch (BindException e) {
                 // Should not happen with suppressValidation = true.
                 logger.error(e);
             }
 
             return;
         } catch (Exception e) {
             if (logger.isErrorEnabled()) {
                 logger.error(e.getMessage());
             }
         	gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
             gift.setPaymentMessage(e.getMessage());
             GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
             gift.setSuppressValidation(true);
             try {
                 gs.maintainGift(gift);
             } catch (BindException be) {
                 // Should not happen with suppressValidation = true.
                 logger.error(e);
             }
 
             return;
         }
 
         // Process the transaction
         ResponseIF response = null;
         try {
 
         	response = tp.process(request);
 
         	orangeleapJmxNotificationBean.incrementStatCount(gift.getSite().getName(), OrangeleapJmxNotificationBean.CAPTURE);
         	orangeleapJmxNotificationBean.setStat(gift.getSite().getName(), OrangeleapJmxNotificationBean.ORBITAL_PAYMENT_STATUS, OrangeleapJmxNotificationBean.OK);
 
         } catch (Exception tex) {
             if (logger.isErrorEnabled()) {
                 logger.error(tex.getMessage());
             	orangeleapJmxNotificationBean.publishNotification(OrangeleapJmxNotificationBean.ORBITAL_PAYMENT_ERROR, ""+tex.getMessage());
             	orangeleapJmxNotificationBean.setStat(gift.getSite().getName(), OrangeleapJmxNotificationBean.ORBITAL_PAYMENT_STATUS, OrangeleapJmxNotificationBean.ERROR);
             }
 
         	gift.setPaymentStatus(Gift.PAY_STATUS_ERROR);
             gift.setPaymentMessage(tex.getMessage());
             GiftService gs = (GiftService) applicationContext.getBean("giftService");
 
             gift.setSuppressValidation(true);
             try {
                 gs.maintainGift(gift);
             } catch (BindException e) {
                 // Should not happen with suppressValidation = true.
                 logger.error(e);
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
             gift.setGiftStatus(Gift.STATUS_PAID);
             gift.setPaymentStatus(Gift.PAY_STATUS_APPROVED);
             gift.setPaymentMessage(response.getMessage());
             gift.setAvsMessage(response.getAVSResponseCode());
         } else {
             gift.setGiftStatus(Gift.STATUS_NOT_PAID);
             gift.setPaymentStatus(Gift.PAY_STATUS_DECLINED);
             gift.setPaymentMessage(response.getMessage() + "Paymentech response code: " + response.getResponseCode());
             gift.setAvsMessage(response.getAVSResponseCode());
             logger.error("Paymentech response code " + response.getResponseCode());
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
