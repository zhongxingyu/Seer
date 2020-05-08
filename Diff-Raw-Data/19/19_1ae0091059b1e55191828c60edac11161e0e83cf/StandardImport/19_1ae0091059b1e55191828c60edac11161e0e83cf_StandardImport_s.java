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
 
 package org.mifos;
 
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.Map;
 
 import org.mifos.accounts.api.AccountReferenceDto;
 import org.mifos.accounts.api.PaymentTypeDto;
 import org.mifos.spi.TransactionImport;
 
 public abstract class StandardImport extends TransactionImport {
 
     private PaymentTypeDto paymentTypeDto = null;
 
     public PaymentTypeDto getPaymentTypeDto() {
         return this.paymentTypeDto;
     }
 
     public void setPaymentTypeDto(PaymentTypeDto paymentTypeDto) {
         this.paymentTypeDto = paymentTypeDto;
     }
 
     /**
      * @param paymentAmount
      *            amount to be added to the running total
      * @return total so far, including passed in paymentAmount (never <code>null</code>)
      */
     public static BigDecimal addToRunningTotalForAccount(BigDecimal paymentAmount,
             Map<AccountReferenceDto, BigDecimal> cumulativeAmountByAccount, AccountReferenceDto account) {
         BigDecimal currentTotal = cumulativeAmountByAccount.get(account);
         if (null == currentTotal) {
             currentTotal = new BigDecimal(0);
         }
         currentTotal = currentTotal.add(paymentAmount);
         cumulativeAmountByAccount.put(account, currentTotal);
         return currentTotal;
     }
 
     public PaymentTypeDto findPaymentType(String paymentTypeName) throws Exception {
         PaymentTypeDto p = null;
         List<PaymentTypeDto> supportedPaymentTypes = getAccountService().getLoanPaymentTypes();
         for (PaymentTypeDto t : supportedPaymentTypes) {
             if (t.getName().contains(paymentTypeName)) {
                 p = t;
             }
         }
         return p;
     }
 
     @Override
     public void store(InputStream input) throws Exception {
         getAccountService().makePayments(parse(input).getSuccessfullyParsedRows());
     }
 
 }
