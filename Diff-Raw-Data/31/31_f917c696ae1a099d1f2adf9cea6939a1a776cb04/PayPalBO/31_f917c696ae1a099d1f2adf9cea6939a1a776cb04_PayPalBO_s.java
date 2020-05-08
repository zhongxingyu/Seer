 package com.ac;
 
 import java.math.BigDecimal;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Hashtable;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.mgi.paypal.inputbean.UserDataInputBean;
 import com.mgi.paypal.inputbean.UserLimitInputBean;
 import com.mgi.paypal.util.AccessToken;
 import com.mgi.paypal.util.Mgi_Paypal_Constants;
 import com.mgi.paypal.util.PropertyUtil;
 import com.mgi.paypal.util.UserData;
 import com.paypal.adaptivepayment.client.AccountIdentifier;
 import com.paypal.adaptivepayment.client.AdaptivePaymentsPortType_AdaptivePaymentsSOAP11Http_Client;
 import com.paypal.adaptivepayment.client.CurrencyType;
 import com.paypal.adaptivepayment.client.DetailLevelCode;
 import com.paypal.adaptivepayment.client.FundingConstraint;
 import com.paypal.adaptivepayment.client.FundingTypeInfo;
 import com.paypal.adaptivepayment.client.FundingTypeList;
 import com.paypal.adaptivepayment.client.GetUserLimitsRequest;
 import com.paypal.adaptivepayment.client.GetUserLimitsResponse;
 import com.paypal.adaptivepayment.client.PayRequest;
 import com.paypal.adaptivepayment.client.PayResponse;
 import com.paypal.adaptivepayment.client.PhoneNumberType;
 import com.paypal.adaptivepayment.client.Receiver;
 import com.paypal.adaptivepayment.client.ReceiverList;
 import com.paypal.adaptivepayment.client.RequestEnvelope;
 import com.paypal.adaptivepayment.client.SenderIdentifier;
 
 public class PayPalBO {
 
 	public PayPalBO() {
 	}
 
 	private static Logger LOGGER = Logger.getLogger(PayPalBO.class);
 
 	public static Hashtable<String, String> stateNameAndCodeHashtable = new Hashtable<String, String>();
 
 	public static PayResponse payToMoneyGram(
 			String token, String customerEmail, BigDecimal amount,
 			String customerPhoneNumber, String referenceNumber, BigDecimal fee)
 			throws Exception {
 
 		LOGGER.debug("Enter payToMoneyGram.");
 		
 		BigDecimal totalAmount = amount.add(fee);
 		RequestEnvelope requestEnvelopee = new RequestEnvelope();
 		requestEnvelopee.setDetailLevel(DetailLevelCode.RETURN_ALL);
 		requestEnvelopee.setErrorLanguage(PropertyUtil.constantFromProperties
 				.getString("ERROR_US"));
 		PayRequest payRequest = new PayRequest();
 		PhoneNumberType phoneNumberType = new PhoneNumberType();
 		phoneNumberType.setCountryCode(PropertyUtil.constantFromProperties
 				.getString("COUNTRY_CODE"));
 		String memo = PropertyUtil.constantFromProperties.getString("MEMO");
 		SenderIdentifier senderIdentifier = new SenderIdentifier();
 		senderIdentifier.setPhone(phoneNumberType);
 		DateFormat df = new SimpleDateFormat(Mgi_Paypal_Constants.DATE_FORMAT);
 
 		String TrackingId = referenceNumber.concat("-").concat(
 				df.format(Calendar.getInstance().getTime()));
 		payRequest.setTrackingId(TrackingId);
 		payRequest.setMemo(memo.concat(" ").concat(referenceNumber));
 		Receiver receiver = new Receiver();
 		receiver.setAmount(totalAmount);
 
 		receiver.setInvoiceId(referenceNumber);
 		receiver.setEmail(PropertyUtil.constantFromProperties
 				.getString("RECEIVER_EMAIL_PAY"));
 		receiver.setPaymentType(PropertyUtil.constantFromProperties
 				.getString("PAYMENT_TYPE"));
 		ReceiverList receiverList = new ReceiverList();
 		receiverList.getReceiver().add(receiver);
 		payRequest.setActionType(PropertyUtil.constantFromProperties
 				.getString("ACTION_TYPE"));
 		payRequest.setCurrencyCode(PropertyUtil.constantFromProperties
 				.getString("CURRENCY_CODE"));
 		payRequest.setRequestEnvelope(requestEnvelopee);
 		payRequest.setReturnUrl(PropertyUtil.constantFromProperties
 				.getString("RETURN_URL"));
 		payRequest.setReceiverList(receiverList);
 		payRequest.setCancelUrl(PropertyUtil.constantFromProperties
 				.getString("CANCEL_URL"));
 		payRequest.setFeesPayer(PropertyUtil.constantFromProperties
 				.getString("FEES_PAYER"));
 		payRequest.setSenderEmail(customerEmail);
 		FundingTypeInfo fundingTypeInfo = new FundingTypeInfo();
 		fundingTypeInfo.setFundingType(PropertyUtil.constantFromProperties
 				.getString("FUNDING_TYPE"));
 		FundingTypeList fundingTypeList = new FundingTypeList();
 		fundingTypeList.getFundingTypeInfo().add(fundingTypeInfo);
 		FundingConstraint fundingConstraint = new FundingConstraint();
 		fundingConstraint.setAllowedFundingType(fundingTypeList);
 		payRequest.setFundingConstraint(fundingConstraint);
 		PayResponse payResponse = new PayResponse();
 
 		AdaptivePaymentsPortType_AdaptivePaymentsSOAP11Http_Client client = new AdaptivePaymentsPortType_AdaptivePaymentsSOAP11Http_Client();
 
 		payResponse = client.getPay(payRequest, token);
 
 		LOGGER.debug("Exit payToMoneyGram.");
 
 		return payResponse;
 	}
 
 	public static String getUserLimits(
 			UserLimitInputBean userLimitInputBean) {
 
 		LOGGER.debug("Enter getUserLimits.");
 
 		com.mgi.paypal.response.GetUserLimitsResponse getUserLimitsResponseForUI = new com.mgi.paypal.response.GetUserLimitsResponse();
 		Gson gson = new Gson();
 		PhoneNumberType phoneNumberType = new PhoneNumberType();
 		phoneNumberType.setCountryCode(PropertyUtil.constantFromProperties
 				.getString("COUNTRY_CODE"));
 		phoneNumberType.setPhoneNumber(userLimitInputBean.getPhoneNumber());
 
 		AccountIdentifier accountIdentifier = new AccountIdentifier();
 		accountIdentifier.setEmail(userLimitInputBean.getEmailID());
 		accountIdentifier.setPhone(phoneNumberType);
 
 		RequestEnvelope requestEnvelope = new RequestEnvelope();
 		requestEnvelope.setDetailLevel(DetailLevelCode.RETURN_ALL);
 		requestEnvelope.setErrorLanguage(PropertyUtil.constantFromProperties
 				.getString("ERROR_LANGUAGE"));
 
 		GetUserLimitsRequest getUserLimitsRequest = new GetUserLimitsRequest();
 		getUserLimitsRequest.setUser(accountIdentifier);
 		getUserLimitsRequest.setRequestEnvelope(requestEnvelope);
 		getUserLimitsRequest.setCountry(PropertyUtil.constantFromProperties
 				.getString("PP_COUNTRY_CODE_US"));
 		getUserLimitsRequest
 				.setCurrencyCode(PropertyUtil.constantFromProperties
 						.getString("PP_CURRENCY_CODE_USA"));
 		getUserLimitsRequest.getLimitType().add(
 				PropertyUtil.constantFromProperties.getString("PP_LIMIT_TYPE"));
 
 		GetUserLimitsResponse getUserLimitsResponse = new GetUserLimitsResponse();
 
 		int retryCount = Mgi_Paypal_Constants.RETRY_COUNT;
 		while (retryCount >= 1) {
 			try {
 				AdaptivePaymentsPortType_AdaptivePaymentsSOAP11Http_Client client = new AdaptivePaymentsPortType_AdaptivePaymentsSOAP11Http_Client();
 				getUserLimitsResponse = client
 						.getUserLimit(getUserLimitsRequest);
 					
 				break;
 			} catch (Exception exception) {
 				LOGGER.error("Retrying GetUserLimits because of :" + exception);
 				retryCount--;
 				if (retryCount == 0) {
 					exception.printStackTrace();
					LOGGER.debug("Max number of retries for GetUserLimits reached. Call Failed.");
 					getUserLimitsResponseForUI.setTransactionSuccess(false);
 					getUserLimitsResponseForUI
 							.setErrorMessage(PropertyUtil.messageFromProperties
 									.getString("SESSION_EXPIRED"));
 
 					return new Gson().toJson(getUserLimitsResponse);
 				}
 			}
 
 		}
 		if (getUserLimitsResponse != null
 				&& getUserLimitsResponse.getUserLimit() != null
 				&& !getUserLimitsResponse.getUserLimit().isEmpty()) {
 			getUserLimitsResponseForUI.setTransactionSuccess(true);
 
 			getUserLimitsResponseForUI.setCurrencyType(getUserLimitsResponse
 					.getUserLimit().get(0).getLimitAmount());
 
 		} else {
 			LOGGER.warn("userLimitReponse is empty.Hardcoded value went to UI");
 			CurrencyType currencyType = new CurrencyType();
 			currencyType.setAmount(new BigDecimal(0));
 			currencyType.setCode("Invalid Code");
 			getUserLimitsResponseForUI.setCurrencyType(currencyType);
 			getUserLimitsResponseForUI.setTransactionSuccess(false);
 			getUserLimitsResponseForUI
 					.setErrorMessage("Sorry cannot complete transaction.");
 		}
 
 		LOGGER.debug("Exit getUserLimits.");
 
 		return gson.toJson(getUserLimitsResponseForUI);
 	}
 
 	private static String createToken(
 			String codeValue) throws Exception {
 
 		LOGGER.debug("Enter Create Token");
 		LOGGER.debug("Code Value : " + codeValue);
 		String uri = PropertyUtil.constantFromProperties
 				.getString("TOKEN_SERVICE_URL");
 		AccessToken accessToken = new AccessToken();
 		System.setProperty("javax.net.ssl.trustStoreType",
 				PropertyUtil.constantFromProperties.getString("trustStoreType"));
 		System.setProperty("javax.net.ssl.trustStore",
 				PropertyUtil.constantFromProperties.getString("trustStore"));
 		System.setProperty("javax.net.ssl.trustStorePassword",
 				PropertyUtil.constantFromProperties
 						.getString("trustStorePassword"));
 		HttpClient client = new HttpClient();
 		PostMethod postMethod = new PostMethod(uri);
 		String myQuery = PropertyUtil.constantFromProperties
 				.getString("MY_QUERY");
 		String ScopeValue = URLEncoder.encode(myQuery, "ISO-8859-1").toString();
 		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
 				new DefaultHttpMethodRetryHandler(3, false));
 		String AUTHORIZATION_BASIC_VALUE = PropertyUtil.constantFromProperties
 				.getString("AUTHORIZATION_BASIC");
 		postMethod.addRequestHeader("Authorization", AUTHORIZATION_BASIC_VALUE);
 		String authorization_code_value = PropertyUtil.constantFromProperties
 				.getString("AUTHORIZATION_CODE");
 		postMethod.addParameter("grant_type", authorization_code_value);
 		postMethod.addParameter("scope", ScopeValue);
 		postMethod.addParameter("code", codeValue);
 		LOGGER.debug(codeValue);
 
 		int statusCode = client.executeMethod(postMethod);
 		// TODO
 		LOGGER.debug(statusCode);
 		if (statusCode != HttpStatus.SC_NOT_IMPLEMENTED) {
 			String string = postMethod.getResponseBodyAsString();
 			// TODO
 			LOGGER.debug("string : " + string);
 			LOGGER.debug(string);
 			accessToken = (AccessToken) new Gson().fromJson(string,
 					AccessToken.class);
 		}
 
 		LOGGER.debug("Exit Create Token");
 
 		return accessToken.getAccess_token();
 	}
 	public static void logOutPayPal(
 			String id_Token) throws Exception {
 
 		LOGGER.debug("Enter logOutPayPal");
 		id_Token = id_Token.substring(0, id_Token.indexOf("&code"));
 		LOGGER.debug("id_Token : " + id_Token);
 
 		String url = PropertyUtil.constantFromProperties
 				.getString("TOKEN_END_SESSION_URL");
 		System.setProperty("javax.net.ssl.trustStoreType",
 				PropertyUtil.constantFromProperties.getString("trustStoreType"));
 		System.setProperty("javax.net.ssl.trustStore",
 				PropertyUtil.constantFromProperties.getString("trustStore"));
 		System.setProperty("javax.net.ssl.trustStorePassword",
 				PropertyUtil.constantFromProperties
 						.getString("trustStorePassword"));
 		url = url.concat(id_Token);
 		String reDirectUrl = PropertyUtil.constantFromProperties
 				.getString("REDIRECT_URL_LOGOUT");
 		url = url + reDirectUrl;
 
 		LOGGER.debug(url);
 
 		HttpClient httpClient = new HttpClient();
 		GetMethod getMethod = new GetMethod(url);
 		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
 				new DefaultHttpMethodRetryHandler(3, false));
 		String AUTHORIZATION_BASIC_VALUE = PropertyUtil.constantFromProperties
 				.getString("AUTHORIZATION_BASIC");
 		getMethod.addRequestHeader("Authorization", AUTHORIZATION_BASIC_VALUE);
 
 		int statusCode = 0;
 		try {
 			statusCode = httpClient.executeMethod(getMethod);
 		} catch (Exception exception) {
 
 			exception.printStackTrace();
 		}
 		LOGGER.debug(statusCode);
 
 		LOGGER.debug("Exit logOutPayPal");
 
 	}
 	private static String processToken(
 			String tokenData) throws Exception {
 
 		LOGGER.debug("Enter processToken");
 
 		LOGGER.debug(tokenData);
 
 		String responseBody = null;
 		String uri = PropertyUtil.constantFromProperties
 				.getString("PROCESS_TOKEN_URL");
 
 		HttpClient client = new HttpClient();
 		GetMethod method2 = new GetMethod(uri);
 		method2.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
 				new DefaultHttpMethodRetryHandler(3, false));
 		method2.addRequestHeader("Authorization", "Bearer ".concat(tokenData));
 		int statusCode = client.executeMethod(method2);
 
 		if (statusCode != HttpStatus.SC_NOT_IMPLEMENTED) {
 			responseBody = method2.getResponseBodyAsString();
 		}
 
 		LOGGER.debug("Exit processToken");
 
 		return responseBody;
 	}
 
 	public static UserData getUserData(
 			UserDataInputBean userDataInputBean) {
 
 		LOGGER.debug("Enter getUserData.");
 
 		UserData userData = new UserData();
 
 		Gson gson = new Gson();
 
 		LOGGER.debug(gson.toJson(userDataInputBean));
 		try {
 			String token = createToken(userDataInputBean.getCode());
 			LOGGER.debug(token);
 
 			String userDataString = processToken(token);
 			LOGGER.debug(userDataString);
 
 			if (token == null || token.trim().equals("")) {
 				userData.setTransactionSuccess(false);
 				userData.setErrorMessage(PropertyUtil.messageFromProperties
 						.getString("RETRY_IN_SOMETIME"));
 				return userData;
 			}
 
 			userData = (UserData) gson.fromJson(userDataString, UserData.class);
 			userData.setToken(token);
 			if (stateNameAndCodeHashtable.isEmpty()) {
 				CountryBO.getStateForUSA();
 			}
 
 			String stateName = stateNameAndCodeHashtable.get(userData
 					.getAddress().getRegion());
 			userData.getAddress().setStateName(stateName);
 
 		} catch (Exception exception) {
 			exception.printStackTrace();
 			LOGGER.error(exception.getLocalizedMessage());
 			userData.setTransactionSuccess(false);
 			userData.setErrorMessage(PropertyUtil.messageFromProperties
 					.getString("RETRY_IN_SOMETIME"));
 			return userData;
 		}
 
 		userData.setTransactionSuccess(true);
 
 		LOGGER.debug(gson.toJson(userData));
 
 		LOGGER.debug("Exit getUserData.");
 
 		return userData;
 	}
 }
