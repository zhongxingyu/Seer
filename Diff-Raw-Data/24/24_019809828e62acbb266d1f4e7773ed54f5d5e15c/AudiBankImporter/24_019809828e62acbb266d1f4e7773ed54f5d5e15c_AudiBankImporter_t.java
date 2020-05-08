 /*
  * Copyright (c) 2005-2010 Grameen Foundation USA
  * All rights reserved.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  * 
  * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
  * explanation of the license and how it is applied.
  */
 
 package org.almajmoua;
 
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.mifos.accounts.api.AccountReferenceDto;
 import org.mifos.accounts.api.PaymentTypeDto;
 import org.mifos.spi.TransactionImport;
 
 public abstract class AudiBankImporter extends TransactionImport {
     static final int TRANS_DATE = 0, SERIAL = 1, VALUE_DATE = 2, REFERENCE = 3, DEBIT_OR_CREDIT = 4, AMOUNT = 5,
             BALANCE = 6, DESCRIPTION = 7, MAX_CELL_NUM = 8;
 
     @Override
     public void store(InputStream input) throws Exception {
         getAccountService().makePayments(parse(input).getSuccessfullyParsedRows());
     }
 
     private PaymentTypeDto paymentTypeDto = null;
 
     PaymentTypeDto getPaymentTypeDto() {
         return this.paymentTypeDto;
     }
 
     void setPaymentTypeDto(PaymentTypeDto paymentTypeDto) {
         this.paymentTypeDto = paymentTypeDto;
     }
 
     private static final Pattern descriptionPatternForExternalId = Pattern
             .compile("^PMTMAJ \\w([AZC])([0-9]{5})[0-9 ]{3} ");
     private static final Pattern descriptionPatternForAccountId = Pattern.compile("^PMTMAJ ([0-9]{7}) ");
     private static final Pattern descriptionPatternForGlobalAccountNumber = Pattern.compile("^PMTMAJ ([0-9]{15}) ");
 
     /**
      * If the second letter in the "account code" (the two letters following "PMTMAJ") is "Z" it is a group loan, "A" is
      * an individual loan, "C" is Lebanese pounds. So, if the second letter is "A" the plugin should continue working as
      * is. If the second letter is "Z" we should prepend "GL " to the external_id before looking up and trying to apply
      * the payment to that account. If the second letter is "C" we should prepend "LL " to the external_id before
      * looking up and trying to apply the payment to that account.
      */
     static String getAccountId(String stringWithEmbeddedId) {
         final Matcher matcherGlobalAccountNum = descriptionPatternForGlobalAccountNumber.matcher(stringWithEmbeddedId);
         if (matcherGlobalAccountNum.find()) {
             return matcherGlobalAccountNum.group(1);
         }
 
         final Matcher matcherInternalId = descriptionPatternForAccountId.matcher(stringWithEmbeddedId);
         if (matcherInternalId.find()) {
             return matcherInternalId.group(1);
         }
 
         final Matcher matcherExternalId = descriptionPatternForExternalId.matcher(stringWithEmbeddedId);
         if (matcherExternalId.find()) {
             if (matcherExternalId.group(1).equals("Z")) {
                 // group loan
                 return GROUP_PREFIX + " " + matcherExternalId.group(2);
             } else if (matcherExternalId.group(1).equals("C")) {
                 // loan in Lebanese pounds
                 return LBP_PREFIX + " " + matcherExternalId.group(2);
             } else {
                 return matcherExternalId.group(2);
             }
         }
 
         return "";
     }
 
     /**
      * @param paymentAmount
      *            amount to be added to the running total
      * @return total so far, including passed in paymentAmount (never <code>null</code>)
      */
     static BigDecimal addToRunningTotalForAccount(BigDecimal paymentAmount,
             Map<AccountReferenceDto, BigDecimal> cumulativeAmountByAccount, AccountReferenceDto account) {
         BigDecimal currentTotal = cumulativeAmountByAccount.get(account);
         if (null == currentTotal) {
             currentTotal = new BigDecimal(0);
         }
         currentTotal = currentTotal.add(paymentAmount);
         cumulativeAmountByAccount.put(account, currentTotal);
         return currentTotal;
     }
 
     static boolean accountIdIsAnInternalId(String accountId) {
         return accountId.length() == 7;
     }
 
    /** Group loan account external IDs start with this string. */
     private static final String GROUP_PREFIX = "GL";
    
    /** External IDs for accounts in Lebanese pounds start with this string. */
     private static final String LBP_PREFIX = "LL";
 
     static boolean accountIdIsAnExternalId(String accountId) {
         return accountId.length() == 5 || accountId.startsWith(GROUP_PREFIX) || accountId.startsWith(LBP_PREFIX);
     }
 
     PaymentTypeDto findPaymentType(String paymentTypeName) throws Exception {
         PaymentTypeDto p = null;
         List<PaymentTypeDto> supportedPaymentTypes = getAccountService().getLoanPaymentTypes();
         for (PaymentTypeDto t : supportedPaymentTypes) {
             if (t.getName().contains(paymentTypeName)) {
                 p = t;
             }
         }
         return p;
     }
 }
