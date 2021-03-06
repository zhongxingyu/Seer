 /*
  * Copyright (c) 2006 - 2009 Open Source Strategies, Inc.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the Honest Public License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * Honest Public License for more details.
  *
  * You should have received a copy of the Honest Public License
  * along with this program; if not, write to Funambol,
  * 643 Bair Island Road, Suite 305 - Redwood City, CA 94063, USA
  */
 
 // A portion of this file may have come from the Apache OFBIZ project
 
 /*******************************************************************************
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  *******************************************************************************/
 
 /* This file has been modified by Open Source Strategies, Inc. */
 
 package com.opensourcestrategies.financials.accounts;
 
 import java.sql.Timestamp;
 import java.util.Map;
 import java.util.List;
 
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.UtilDateTime;
 import org.ofbiz.base.util.UtilMisc;
 import org.ofbiz.entity.GenericDelegator;
 import org.ofbiz.entity.GenericEntityException;
 import org.ofbiz.entity.GenericValue;
 import org.ofbiz.entity.util.EntityUtil;
 import org.ofbiz.service.DispatchContext;
 import org.ofbiz.service.GenericServiceException;
 import org.ofbiz.service.LocalDispatcher;
 import org.ofbiz.service.ServiceUtil;
 import org.ofbiz.service.ModelService;
 import org.ofbiz.accounting.payment.PaymentGatewayServices;
 import org.opentaps.common.util.UtilCommon;
 
 public class BillingAccountServices {
 
     public static String module = BillingAccountServices.class.getName();
     
     public static Map createCustomerBillingAccount(DispatchContext dctx, Map context) {
         LocalDispatcher dispatcher = dctx.getDispatcher();
         GenericDelegator delegator = dctx.getDelegator();
 
         String organizationPartyId = (String) context.get("organizationPartyId");
         String customerPartyId = (String) context.get("customerPartyId");
         Double accountLimit = (Double) context.get("accountLimit");
         String description = (String) context.get("description");
         Timestamp fromDate = (Timestamp) context.get("fromDate");
         Timestamp thruDate = (Timestamp) context.get("thruDate");
         
         GenericValue userLogin = (GenericValue) context.get("userLogin");
         
     	try {
     		String billingAccountId = null;
     		
     		// set the default accountLimit if needed
     		if (accountLimit == null) {
     			Debug.logWarning("No account limit specified for new billing account, assuming zero", module);
     			accountLimit = new Double(0.0);
     		}
     		
     		// get the currencyUomId of the organizationPartyId
     		String baseCurrencyUomId = UtilCommon.getOrgBaseCurrency(organizationPartyId, delegator);
     		if (baseCurrencyUomId == null) {
                 return ServiceUtil.returnError("No base currency configured for organization [" + organizationPartyId + "]");
     		}
     		
     		// create the billing account
     		Map billingAccountParams = UtilMisc.toMap("accountLimit", accountLimit,
     				"description", description, "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin);
     		billingAccountParams.put("accountCurrencyUomId", baseCurrencyUomId);
     		Map tmpResult = dispatcher.runSync("createBillingAccount", billingAccountParams);
     		if (ServiceUtil.isError(tmpResult)) {
     			return tmpResult;
     		} else {
     			billingAccountId = (String) tmpResult.get("billingAccountId");
     		}
     		
     		// check if the customer party is already a BILL_TO_CUSTOMER.  If not create that role for him first  No caching - make sure it's up to date
     		GenericValue customerRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId", customerPartyId, "roleTypeId", "BILL_TO_CUSTOMER"));
     		if (customerRole == null) {
     			tmpResult = dispatcher.runSync("createPartyRole", UtilMisc.toMap("partyId", customerPartyId, "roleTypeId", "BILL_TO_CUSTOMER", "userLogin", userLogin));
     			if (ServiceUtil.isError(tmpResult)) {
         			return tmpResult;
         		}
     		}
     		
     		// associate the customer party with billing account as BILL_TO_CUSTOMER
     		tmpResult = dispatcher.runSync("createBillingAccountRole", UtilMisc.toMap("billingAccountId", billingAccountId, "partyId", customerPartyId,
     				"roleTypeId", "BILL_TO_CUSTOMER", "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin));
     		if (ServiceUtil.isError(tmpResult)) {
     			return tmpResult;
     		}
 		
     		tmpResult = ServiceUtil.returnSuccess();
     		tmpResult.put("billingAccountId", billingAccountId);
     		return tmpResult;
     		
      	} catch (GenericEntityException ex) {
     		return ServiceUtil.returnError(ex.getMessage());
     	} catch (GenericServiceException ex) {
     		return ServiceUtil.returnError(ex.getMessage());
     	}
     	
     }
 
     public static Map receiveBillingAccountPayment(DispatchContext dctx, Map context) {
         LocalDispatcher dispatcher = dctx.getDispatcher();    	
         GenericDelegator delegator = dispatcher.getDelegator();
         GenericValue userLogin = (GenericValue) context.get("userLogin");
 
         try {
             // create a received payment
             ModelService service = dctx.getModelService("createPayment");
             Map input = service.makeValid(context, "IN");
             input.put("statusId", "PMNT_RECEIVED");
             input.put("userLogin", userLogin);
             input.put("effectiveDate", context.get("effectiveDate") == null ? UtilDateTime.nowTimestamp() : context.get("effectiveDate"));
             Map serviceResults = dispatcher.runSync("createPayment", input);
             if (ServiceUtil.isError(serviceResults)) return serviceResults;
             String paymentId = (String) serviceResults.get("paymentId");
 
             // create a payment application for this amount
             input = UtilMisc.toMap("paymentId", paymentId, "billingAccountId", context.get("billingAccountId"), "amountApplied", context.get("amount"), "userLogin", userLogin);
             serviceResults = dispatcher.runSync("createPaymentApplication", input);
             if (ServiceUtil.isError(serviceResults)) return serviceResults;
         } catch (GenericServiceException e) {
             Debug.logError(e, e.getMessage(), module);
             return ServiceUtil.returnError("Failed to receive payment: " + e.getMessage());
         }
         return ServiceUtil.returnSuccess();
     }
 
     public static Map captureBillingAccountPayment(DispatchContext dctx, Map context) {
            GenericDelegator delegator = dctx.getDelegator();
            LocalDispatcher dispatcher = dctx.getDispatcher();
            GenericValue userLogin = (GenericValue) context.get("userLogin");
            String invoiceId = (String) context.get("invoiceId");
            String billingAccountId = (String) context.get("billingAccountId");
            Double captureAmount = (Double) context.get("captureAmount");
            String orderId = (String) context.get("orderId");
            Map results = ServiceUtil.returnSuccess();
 
            try {
                // Note that the partyIdFrom of the Payment should be the partyIdTo of the invoice, since you're receiving a payment from the party you billed
                GenericValue invoice = delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId", invoiceId));
                Map paymentParams = UtilMisc.toMap("paymentTypeId", "CUSTOMER_PAYMENT", "paymentMethodTypeId", "EXT_BILLACT",
                        "partyIdFrom", invoice.getString("partyId"), "partyIdTo", invoice.getString("partyIdFrom"),
                        "statusId", "PMNT_RECEIVED", "effectiveDate", UtilDateTime.nowTimestamp());
                paymentParams.put("amount", captureAmount);
                paymentParams.put("currencyUomId", invoice.getString("currencyUomId"));
                paymentParams.put("userLogin", userLogin);
                Map tmpResult = dispatcher.runSync("createPayment", paymentParams);
                if (ServiceUtil.isError(tmpResult)) {
                    return tmpResult;
                }
 
                String paymentId = (String) tmpResult.get("paymentId");
                tmpResult = dispatcher.runSync("createPaymentApplication", UtilMisc.toMap("paymentId", paymentId, "invoiceId", invoiceId, "billingAccountId", billingAccountId,
                        "amountApplied", captureAmount, "userLogin", userLogin));
                if (ServiceUtil.isError(tmpResult)) {
                    return tmpResult;
                }
                if (paymentId == null) {
                    return ServiceUtil.returnError("No payment created for invoice [" + invoiceId + "] and billing account [" + billingAccountId + "]");
                }
                results.put("paymentId", paymentId);
                results.put("captureAmount", captureAmount);
 
                if (orderId != null && captureAmount.doubleValue() > 0) {
                    // Create a paymentGatewayResponse, if necessary
                    GenericValue order = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
                    if (order == null) {
                        return ServiceUtil.returnError("No paymentGatewayResponse created for invoice [" + invoiceId + "] and billing account [" + billingAccountId + "]: Order with ID [" + orderId + "] not found!");
                    }
                    // See if there's an orderPaymentPreference - there should be only one OPP for EXT_BILLACT per order
                    List orderPaymentPreferences = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId, "paymentMethodTypeId", "EXT_BILLACT"));
                    if (orderPaymentPreferences != null && orderPaymentPreferences.size() > 0) {
                        GenericValue orderPaymentPreference = EntityUtil.getFirst(orderPaymentPreferences);
 
                        // Check the productStore setting to see if we need to do this explicitly
                        GenericValue productStore = order.getRelatedOne("ProductStore");
                        if (productStore.getString("manualAuthIsCapture") == null || (! productStore.getString("manualAuthIsCapture").equalsIgnoreCase("Y"))) {
                            String responseId = delegator.getNextSeqId("PaymentGatewayResponse");
                           GenericValue pgResponse = delegator.makeValue("PaymentGatewayResponse", null);
                            pgResponse.set("paymentGatewayResponseId", responseId);
                            pgResponse.set("paymentServiceTypeEnumId", PaymentGatewayServices.CAPTURE_SERVICE_TYPE);
                            pgResponse.set("orderPaymentPreferenceId", orderPaymentPreference.getString("orderPaymentPreferenceId"));
                            pgResponse.set("paymentMethodTypeId", "EXT_BILLACT");
                            pgResponse.set("transCodeEnumId", "PGT_CAPTURE");
                            pgResponse.set("amount", captureAmount);
                            pgResponse.set("currencyUomId", invoice.getString("currencyUomId"));
                            pgResponse.set("transactionDate", UtilDateTime.nowTimestamp());
                            // referenceNum holds the relation to the order.
                            // todo: Extend PaymentGatewayResponse with a billingAccountId field?
                            pgResponse.set("referenceNum", billingAccountId);
                            pgResponse.set("gatewayMessage", "Applied [" + captureAmount + "] towards invoice [" + invoiceId + "] from order [" + orderId +"]");
 
                            // save the response.
                            tmpResult = dispatcher.runSync("savePaymentGatewayResponse", UtilMisc.toMap("paymentGatewayResponse", pgResponse, "userLogin", userLogin));
 
                            // Update the orderPaymentPreference
                            orderPaymentPreference.set("statusId", "PAYMENT_SETTLED");
                            orderPaymentPreference.store();
 
                            results.put("paymentGatewayResponseId", responseId);
                        }
                    }
                }
            } catch (GenericEntityException ex) {
                return ServiceUtil.returnError(ex.getMessage());
            } catch (GenericServiceException ex) {
                return ServiceUtil.returnError(ex.getMessage());
            }
 
            return results;
        }
 
 
     public static Map calcBillingAccountBalance(DispatchContext dctx, Map context) {
            GenericDelegator delegator = dctx.getDelegator();
            String billingAccountId = (String) context.get("billingAccountId");
            Map result = ServiceUtil.returnSuccess();
 
            try {
                GenericValue billingAccount = delegator.findByPrimaryKey("BillingAccount", UtilMisc.toMap("billingAccountId", billingAccountId));
                if (billingAccount == null) {
                    return ServiceUtil.returnError("Unable to locate billing account #" + billingAccountId);
                }
 
                result.put("billingAccount", billingAccount);
                result.put("accountBalance", new Double(com.opensourcestrategies.financials.accounts.BillingAccountWorker.getBillingAccountAvailableBalance(billingAccount).doubleValue()));
                
                return result;
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError("Error getting billing account or calculating balance for billing account #" + billingAccountId);
            }
        }
 
 }
