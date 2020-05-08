 /**
  * 
  */
 package com.konakart.bl.modules.payment.gestpay;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author lrkwz
  */
 public class GestpayConstants {
 
 	/**
 	 * 
 	 */
 	public static final String MODULE_PAYMENT_GESTPAY_SHOP_ID = "MODULE_PAYMENT_GESTPAY_SHOP_ID";
 	/**
 	 * 
 	 */
 	public static final String MODULE_PAYMENT_GESTPAY_TEST_MODE = "MODULE_PAYMENT_GESTPAY_TEST_MODE";
 	/**
 	 * 
 	 */
 	public static final String MODULE_PAYMENT_GESTPAY_REQUEST_URL = "MODULE_PAYMENT_GESTPAY_REQUEST_URL";
 	/**
 	 * 
 	 */
 	public static final String MODULE_PAYMENT_GESTPAY_CALLBACK_PASSWORD = "MODULE_PAYMENT_GESTPAY_CALLBACK_PASSWORD";
 	/**
 	 * 
 	 */
 	public static final String MODULE_PAYMENT_GESTPAY_CALLBACK_USERNAME = "MODULE_PAYMENT_GESTPAY_CALLBACK_USERNAME";
 	/**
 	 * 
 	 */
 	public static final String MODULE_PAYMENT_GESTPAY_ZONE = "MODULE_PAYMENT_GESTPAY_ZONE";
 	/**
 	 * 
 	 */
 	public static final String MODULE_PAYMENT_GESTPAY_SORT_ORDER = "MODULE_PAYMENT_GESTPAY_SORT_ORDER";
 	/**
 		 * 
 		 */
 	public static final String MODULE_PAYMENT_GESTPAY_STATUS = "MODULE_PAYMENT_GESTPAY_STATUS";
 
 	// Message Catalogue Keys
 	public final static String MODULE_PAYMENT_GESTPAY_TEXT_TITLE = "module.payment.gestpay.text.title";
 	public final static String MODULE_PAYMENT_GESTPAY_TEXT_DESCRIPTION = "module.payment.gestpay.text.description";
 	public static final String EURO = "242";
 	public static final String ITALIAN_LIRA = "18";
 	public static final String US_DOLLAR = "1";
 	public static final String UK_POUND = "2";
 	public static final String YEN = "71";
 	public static final String HONGKONG_DOLLAR = "103";
 	static final String BRAZIL_REAL = "234";
 	public static final String GESTPAY_LANGUAGE = "GESTPAY_LANGUAGE";
 	// public static final String BASEPACKAGE = "org.lrkwz.konakart.bl.modules";
 	public static final Map<String, String> currencyCode = Collections
 			.unmodifiableMap(new HashMap<String, String>() {
 				{
 					put("EUR", EURO);
 					put("ITL", ITALIAN_LIRA);
 					put("USD", US_DOLLAR);
 					put("GBP", UK_POUND);
 					put("JPY", YEN);
 					put("GBP", UK_POUND);
 					put("HKD", HONGKONG_DOLLAR);
 					put("BRL", BRAZIL_REAL);
 				}
 			});
 
 	// Module name must be the same as the class name although it can be all in
 	// lowercase in order to remain compatible with osCommerce.
 	public static String code = "gestpay";
 
 	// Return codes and descriptions
 	public static final int RET0 = 0;
 	public static final String RET0_DESC = "Transaction OK";
 	public static final int RET2 = -2;
 	public static final String RET2_DESC = "Error decoding results: ";
 	public static final int RET3 = -3;
 	public static final String RET3_DESC = "We could not retrieve the order id from the secret key";
 	public static final int RET4 = -4;
 	public static final String RET4_DESC = "There has been an unexpected exception. Please look at the log.";
 	public static final String SUCCESSFUL_TRANSACTION_VALUE = "OK";
 	public static final String ORDER_HISTORY_COMMENT_OK = "GestPay payment successful. GestPay TransactionId = ";
 	public static final String ORDER_HISTORY_COMMENT_KO = "GestPay payment not successful. GestPay Payment Status = ";
 	public static final String RET9_DESC = "Callback error: no encrypted string was sent by the gateway";
 	public static final int RET9 = -9;
 	public static final String RET7_DESC = "Callback error: no shop id sent by the gateway";
 	public static final int RET7 = -7;
	public static final String MODULE_PAYMENT_GESTPAY_SEND_BUYER_NAME = "MODULE_PAYMENT_GESTPAY_SEND_BUYER_NAME";
	public static final String MODULE_PAYMENT_GESTPAY_SEND_BUYER_EMAIL = "MODULE_PAYMENT_GESTPAY_SEND_BUYER_EMAIL";
 	public static final String MODULE_PAYMENT_GESTPAY_SEND_BUYER_LANGUAGE = "MODULE_PAYMENT_GESTPAY_SEND_BUYER_LANGUAGE";
	public static final String MODULE_PAYMENT_GESTPAY_SEND_CUSTOMINFO = "MODULE_PAYMENT_GESTPAY_SEND_CUSTOMINFO";
	public static final String MODULE_PAYMENT_GESTPAY_SEND_CURRENCY = "MODULE_PAYMENT_GESTPAY_SEND_CURRENCY";
 
 }
