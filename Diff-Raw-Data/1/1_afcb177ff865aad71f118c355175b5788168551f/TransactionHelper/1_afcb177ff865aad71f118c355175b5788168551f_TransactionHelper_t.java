 package com.feefighers;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.feefighers.model.Options;
 import com.feefighers.model.Transaction;
 import com.feefighers.model.Transaction.TransactionRequestType;
 
 public class TransactionHelper {
 
 	public static Transaction generateTransactionAndSetOptions(Options options, boolean defaultCurrency) {
 		Transaction transaction = new Transaction(TransactionRequestType.purchase);
 		
 		if(options != null) {
 			if(options.get("amount") != null) {
 				transaction.setAmount(String.valueOf(options.get("amount")));
 			}
 			transaction.setPaymentMethodToken(options.get("payment_method_token"));						
 			
 			transaction.setDescriptor(options.get("descriptor"));
 			transaction.setCustom(options.get("custom"));
 			transaction.setCustomerReference(options.get("customer_reference"));
 			transaction.setBillingReference(options.get("billing_reference"));
			transaction.setCurrencyCode(options.get("currency_code"));
 		}
 		
 		if(defaultCurrency && StringUtils.isBlank(transaction.getCurrencyCode())) {
 			transaction.setCurrencyCode("USD");
 		}		
 		
 		return transaction;
 	}
 }
