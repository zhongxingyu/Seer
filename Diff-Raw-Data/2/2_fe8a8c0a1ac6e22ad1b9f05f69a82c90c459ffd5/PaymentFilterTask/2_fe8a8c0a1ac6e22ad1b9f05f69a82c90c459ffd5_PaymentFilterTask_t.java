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
 package com.sapienter.jbilling.server.payment.tasks;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.server.entity.PaymentAuthorizationDTO;
 import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;
 import com.sapienter.jbilling.server.payment.PaymentDTOEx;
 import com.sapienter.jbilling.server.payment.blacklist.AddressFilter;
 import com.sapienter.jbilling.server.payment.blacklist.BlacklistFilter;
 import com.sapienter.jbilling.server.payment.blacklist.NameFilter;
 import com.sapienter.jbilling.server.payment.blacklist.CreditCardFilter;
 import com.sapienter.jbilling.server.payment.blacklist.IpAddressFilter;
 import com.sapienter.jbilling.server.payment.blacklist.PhoneFilter;
 import com.sapienter.jbilling.server.payment.blacklist.UserIdFilter;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
 import com.sapienter.jbilling.server.pluggableTask.PaymentTask;
 import com.sapienter.jbilling.server.pluggableTask.PaymentTaskBase;
 import com.sapienter.jbilling.server.util.Constants;
 
 /**
  * Blacklist filter payment processor, which calls enabled filters (configured 
  * through parameters) to test if payments are blacklisted. Only if a payment
  * isn't blacklisted is it sent onto the next configured payment processor 
  * (perhaps the real processor).
  */
 public class PaymentFilterTask extends PaymentTaskBase implements PaymentTask {
     public static final String PARAM_ENABLE_FILTER_USER_ID = "enable_filter_user_id";
     public static final String PARAM_ENABLE_FILTER_NAME = "enable_filter_name";
    public static final String PARAM_ENABLE_FILTER_CC_NUMBER = "enable_filter_cc_number";
     public static final String PARAM_ENABLE_FILTER_ADDRESS = "enable_filter_address";
     public static final String PARAM_ENABLE_FILTER_IP_ADDRESS = "enable_filter_ip_address";
     public static final String PARAM_ENABLE_FILTER_PHONE_NUMBER = "enable_filter_phone_number";
 
     public static final String PARAM_IP_ADDRESS_CCF_ID = "ip_address_ccf_id";
 
     private static final String PAYMENT_PROCESSOR_NAME = "Payment filter task";
 
     private static final Logger LOG = Logger.getLogger(PaymentFilterTask.class);
 
     public void failure(Integer userId, Integer retry) {
     }
 
     public boolean process(PaymentDTOEx paymentInfo) throws PluggableTaskException {
         return callFilters(paymentInfo);
     }
     
     public boolean preAuth(PaymentDTOEx paymentInfo) throws PluggableTaskException {
         return callFilters(paymentInfo);
     }
     
     public boolean confirmPreAuth(PaymentAuthorizationDTO auth, 
             PaymentDTOEx paymentInfo) throws PluggableTaskException {
         return callFilters(paymentInfo);
     }
 
     /**
      * Calls each blacklist filter and if any fail, returns payment failure, 
      * otherwise returns payment processor unavailable (so a real processor
      * can handle the payment).
      */
     private boolean callFilters(PaymentDTOEx paymentInfo) 
             throws PluggableTaskException{
         // Get all enabled filters and check the payment through each one
         List<BlacklistFilter> filters = getEnabledFilters();
         for (BlacklistFilter filter : filters) {
             BlacklistFilter.Result result = filter.checkPayment(paymentInfo);
             if (result.isBlacklisted()) {
                 // payment failed a blacklist filter, return result failed
                 LOG.debug("Blacklisted result: " + result.getMessage());
         		PaymentAuthorizationDTOEx authInfo = new PaymentAuthorizationDTOEx();
         		authInfo.setProcessor(PAYMENT_PROCESSOR_NAME);
 		        authInfo.setResult(false);
         		authInfo.setCode1(filter.getName());
                 authInfo.setResponseMessage(result.getMessage());
 		        storeProcessedAuthorization(paymentInfo, authInfo);
 		        paymentInfo.setAuthorization(authInfo);
                 paymentInfo.setResultId(Constants.RESULT_FAIL);
                 return false;
             } 
         }
         // All filters passed, continue onto a real payment processor
         // next in the chain.
         LOG.debug("Payment continuing on to next processor");
         paymentInfo.setResultId(Constants.RESULT_UNAVAILABLE);
         return true;
     }
 
     /**
      * Calls each blacklist filter to test the user id. If any fail, the 
      * response message is added to the returned results vector. An empty
      * returned vector means the user id didn't match any blacklist entries.
      */
     public Vector<String> getBlacklistMatches(Integer userId) {
         Vector<String> results = new Vector<String>();
         List<BlacklistFilter> filters = getEnabledFilters();
         for (BlacklistFilter filter : filters) {
             BlacklistFilter.Result result = filter.checkUser(userId);
             if (result.isBlacklisted()) {
                 results.add(result.getMessage());
             }
         }
         return results;
     }
 
     /**
      * Returns a list of enabled blacklist filters
      */
     private List<BlacklistFilter> getEnabledFilters() {
         List<BlacklistFilter> filters = new LinkedList<BlacklistFilter>();
 
         if (getBooleanParameter(PARAM_ENABLE_FILTER_USER_ID)) {
             filters.add(new UserIdFilter());
             LOG.debug("UserIdFilter enabled");
         } 
         if (getBooleanParameter(PARAM_ENABLE_FILTER_NAME)) {
             filters.add(new NameFilter());
             LOG.debug("NameFilter enabled");
         } 
         if (getBooleanParameter(PARAM_ENABLE_FILTER_CC_NUMBER)) {
             filters.add(new CreditCardFilter());
             LOG.debug("CreditCardFilter enabled");
         } 
         if (getBooleanParameter(PARAM_ENABLE_FILTER_ADDRESS)) {
             filters.add(new AddressFilter());
             LOG.debug("AddressFilter enabled");
         } 
         if (getBooleanParameter(PARAM_ENABLE_FILTER_IP_ADDRESS)) {
             // try to get the ip address ccf id
             Integer ccfId = getIpAddressCcfId();
 
             if (ccfId != null) {
                 filters.add(new IpAddressFilter(ccfId));
                 LOG.debug("IpAddressFilter enabled");
             } else {
                 // ccf id wasn't there or couldn't be parsed from a string param
                 LOG.warn("Invalid " + PARAM_IP_ADDRESS_CCF_ID + " parameter " +
                         " - skipping IpAddresFilter");
             }
         } 
         if (getBooleanParameter(PARAM_ENABLE_FILTER_PHONE_NUMBER)) {
             filters.add(new PhoneFilter());
             LOG.debug("PhoneFilter enabled");
         }
 
         return filters;
     }
 
     /**
      * Returns the IP Address CCF Id plug-in parameter
      * or null if it doesn't exist or invalid.
      */
     public Integer getIpAddressCcfId() {
         // try to get the ip address ccf id
         Integer ccfId = null;
         Object param = parameters.get(PARAM_IP_ADDRESS_CCF_ID);
         if (param instanceof Integer) {
             ccfId = (Integer) param;
         } else if (param instanceof String) {
             try {
                 ccfId = new Integer((String) param);
             } catch (NumberFormatException nfe) {
             }
         }
         return ccfId;
     }
 }
