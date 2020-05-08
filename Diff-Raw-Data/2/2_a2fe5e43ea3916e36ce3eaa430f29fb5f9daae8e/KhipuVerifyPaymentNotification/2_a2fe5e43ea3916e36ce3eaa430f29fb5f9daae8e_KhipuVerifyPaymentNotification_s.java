 /* Copyright (c) 2013, khipu SpA
  * All rights reserved.
  * Released under BSD LICENSE, please refer to LICENSE.txt
  */
 
 package com.khipu.lib.java;
 
 import com.khipu.lib.java.exception.JSONException;
 import com.khipu.lib.java.exception.KhipuException;
 import com.khipu.lib.java.response.KhipuVerifyPaymentNotificationResponse;
 import org.apache.http.ParseException;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Servicio verificar la autenticidad de una notificación instantanea hecha por
  * khipu.
  * 
  * @author Alejandro Vera (alejandro.vera@khipu.com)
  * @version 1.2
  * @since 2013-05-24
  */
 public class KhipuVerifyPaymentNotification extends KhipuService {
 
 	private String _postReceiverId;
 	private String _apiVersion;
 	private String _notificationId;
 	private String _subject;
 	private String _amount;
 	private String _currency;
 	private String _custom;
 	private String _transaction_id;
 	private String _payerEmail;
 	private String _notificationSignature;
 
 	KhipuVerifyPaymentNotification(long receiverId, String secret) {
 		super(receiverId, secret);
 	}
 
 	KhipuVerifyPaymentNotification(int receiverId) {
 		super(receiverId, null);
 	}
 
 	@Override
 	String getMethodEndpoint() {
 		return "verifyPaymentNotification";
 	}
 
 	@Override
 	public KhipuVerifyPaymentNotificationResponse execute() throws KhipuException, IOException {
 		
		if (("" + getReceiverId()).equals(getPostReceiverId())) {
 			return new KhipuVerifyPaymentNotificationResponse(false);
 		}
 		
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("receiver_id", "" + getReceiverId());
 		map.put("api_version", _apiVersion);
 		map.put("notification_id", _notificationId);
 		map.put("subject", _subject);
 		map.put("amount", _amount);
 		map.put("currency", _currency);
 		map.put("transaction_id", _transaction_id);
 		map.put("payer_email", _payerEmail);
 		map.put("custom", _custom);
 		map.put("notification_signature", _notificationSignature);
 		try {
 			return new KhipuVerifyPaymentNotificationResponse(post(map).equals("VERIFIED"));
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} catch (JSONException xmlException) {
 			throw Khipu.getErrorsException(xmlException.getJSON());
 		}
 		return null;
 	}
 
 	public String getApiVersion() {
 		return _apiVersion;
 	}
 
 	public void setApiVersion(String apiVersion) {
 		_apiVersion = apiVersion;
 	}
 
 	public String getNotificationId() {
 		return _notificationId;
 	}
 
 	public void setNotificationId(String notificationId) {
 		_notificationId = notificationId;
 	}
 
 	public String getSubject() {
 		return _subject;
 	}
 
 	public void setSubject(String subject) {
 		_subject = subject;
 	}
 
 	public String getAmount() {
 		return _amount;
 	}
 
 	public void setAmount(String amount) {
 		_amount = amount;
 	}
 
 	public String getCustom() {
 		return _custom;
 	}
 
 	public void setCustom(String custom) {
 		_custom = custom;
 	}
 
 	public String getTransaction_id() {
 		return _transaction_id;
 	}
 
 	public void setTransaction_id(String transaction_id) {
 		_transaction_id = transaction_id;
 	}
 
 	public String getPayerEmail() {
 		return _payerEmail;
 	}
 
 	public void setPayerEmail(String payerEmail) {
 		_payerEmail = payerEmail;
 	}
 
 	public String getNotificationSignature() {
 		return _notificationSignature;
 	}
 
 	public void setNotificationSignature(String notificationSignature) {
 		_notificationSignature = notificationSignature;
 	}
 
 	public String getCurrency() {
 		return _currency;
 	}
 
 	public void setCurrency(String currency) {
 		_currency = currency;
 	}
 
 	public String getPostReceiverId() {
 		return _postReceiverId;
 	}
 
 	public void setPostReceiverId(String postReceiverId) {
 		_postReceiverId = postReceiverId;
 	}
 
 }
