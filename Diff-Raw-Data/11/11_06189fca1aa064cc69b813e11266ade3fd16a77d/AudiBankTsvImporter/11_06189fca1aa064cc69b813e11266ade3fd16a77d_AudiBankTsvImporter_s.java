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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.LocalDate;
 import org.mifos.accounts.api.AccountPaymentParametersDto;
 import org.mifos.accounts.api.AccountReferenceDto;
 import org.mifos.accounts.api.InvalidPaymentReason;
 import org.mifos.framework.util.UnicodeUtil;
 import org.mifos.spi.ParseResultDto;
 
 public class AudiBankTsvImporter extends AudiBankImporter {
     /**
      * {@link DateFormat} is not thread safe, that's why this is an instance variable.
      */
     final DateFormat dateFormat;
 
     static final String audiDateFormatString = "yyyy/MM/dd";
 
     public AudiBankTsvImporter() {
         dateFormat = new SimpleDateFormat(audiDateFormatString);
         dateFormat.setLenient(false);
     }
 
     @Override
     public String getDisplayName() {
         return "Audi Bank (tab-delimited)";
     }
 
     static final Pattern serialPattern = Pattern.compile("^[0-9]+$");
 
     @Override
     public ParseResultDto parse(final InputStream rawInput) {
         BufferedReader input = null;
         final List<String> errorsList = new ArrayList<String>();
         final List<AccountPaymentParametersDto> pmts = new ArrayList<AccountPaymentParametersDto>();
         final int headerLines = 5;
         int linesRead = 0;
         Map<AccountReferenceDto, BigDecimal> cumulativeAmountByAccount = new HashMap<AccountReferenceDto, BigDecimal>();
         try {
             input = UnicodeUtil.getUnicodeAwareBufferedReader(rawInput);
 
             // read the first line and look for the payment type string in the
             // top left "cell"
             String line = input.readLine();
             linesRead++;
             if (null == line) {
                 errorsList.add("Not enough input. Couldn't read first line.");
             } else {
                 String topLeftCell = line.trim();
                 if (StringUtils.isBlank(topLeftCell)) {
                     errorsList.add("No payment type name found on first line.");
                 } else {
                     setPaymentTypeDto(findPaymentType(topLeftCell));
                     if (null == getPaymentTypeDto()) {
                         errorsList.add("No payment type found named '" + topLeftCell + "'.");
                     }
                 }
             }
 
             while (errorsList.isEmpty() && linesRead < headerLines) {
                 if (null == input.readLine()) {
                     errorsList.add("Not enough input. Only received " + linesRead + " rows.");
                     break;
                 }
                 linesRead++;
             }
 
             if (errorsList.isEmpty()) {
 
                 while (true) {
                     line = input.readLine();
                     if (null == line) {
                         break;
                     }
 
                     linesRead++;
 
                     /* skip blank lines */
                     if (line.trim().equals("")) {
                         continue;
                     }
 
                     final String fields[] = line.split("\\t");
                     if (fields.length < 8) {
                         errorsList.add("Row " + linesRead + " is missing data: not enough fields.");
                         continue;
                     }
 
                     if (StringUtils.isNotBlank(fields[4])) {
                         if (!fields[DEBIT_OR_CREDIT].trim().equalsIgnoreCase("C")) {
                             /* not a credit: ignore */
                             continue;
                         }
                     } else {
                         errorsList.add("Row " + linesRead + " is missing data: debit/credit not specified.");
                         continue;
                     }
 
                     final String accountId = getAccountId(fields[DESCRIPTION]);
                     if ("".equals(accountId)) {
                         errorsList.add("Loan account ID could not be extracted from row " + linesRead);
                         continue;
                     }
 
                     final String serial = fields[SERIAL].trim();
                     if (StringUtils.isBlank(serial) || !serialPattern.matcher(serial).matches()) {
                         errorsList.add("Serial value in row " + linesRead + " does not follow expected format.");
                         continue;
                     }
 
                     final BigDecimal paymentAmount = new BigDecimal(fields[AMOUNT].trim());
                     final AccountReferenceDto account;
 
                     try {
                         if (accountIdIsAnInternalId(accountId)) {
                             account = getAccountService().lookupLoanAccountReferenceFromId(Integer.valueOf(accountId));
                         } else if (accountIdIsAnExternalId(accountId)) {
                             account = getAccountService().lookupLoanAccountReferenceFromExternalId(accountId);
                         } else {
                             account = getAccountService().lookupLoanAccountReferenceFromGlobalAccountNumber(accountId);
                         }
                     } catch (Exception e) {
                         errorsList.add("Error looking up account ID from row " + linesRead + ": " + e.getMessage());
                         continue;
                     }
 
                     final Date transDate;
 
                     try {
                         transDate = dateFormat.parse(fields[TRANS_DATE]);
                     } catch (ParseException pe) {
                         errorsList.add("Transaction date value in row " + linesRead
                                 + " does not follow expected format (" + audiDateFormatString + ").");
                         continue;
                     }
 
                     final LocalDate paymentDate = LocalDate.fromDateFields(transDate);
                     final BigDecimal totalPaymentAmountForAccount = addToRunningTotalForAccount(paymentAmount,
                             cumulativeAmountByAccount, account);
 
                     AccountPaymentParametersDto cumulativePayment = new AccountPaymentParametersDto(
                             getUserReferenceDto(), account, totalPaymentAmountForAccount, paymentDate,
                             getPaymentTypeDto(), "serial=" + serial);
 
                     AccountPaymentParametersDto singlePayment = new AccountPaymentParametersDto(getUserReferenceDto(),
                             account, paymentAmount, paymentDate, getPaymentTypeDto(), "serial=" + serial);
 
                     List<InvalidPaymentReason> errors = getAccountService().validatePayment(cumulativePayment);
                     if (!errors.isEmpty()) {
                         for (InvalidPaymentReason error : errors) {
                             switch (error) {
                             case INVALID_DATE:
                                 errorsList.add("Invalid transaction date in row " + linesRead);
                                 break;
                             case UNSUPPORTED_PAYMENT_TYPE:
                                 errorsList.add("Unsupported payment type in row " + linesRead);
                                 break;
                             case INVALID_PAYMENT_AMOUNT:
                                 errorsList.add("Invalid payment amount in row " + linesRead);
                                 break;
                             default:
                                 errorsList.add("Invalid payment in row " + linesRead + " (reason unknown).");
                                 break;
                             }
                         }
 
                         continue;
                     }
 
                     pmts.add(singlePayment);
                 }
             }
 
         } catch (Exception e) {
             e.printStackTrace(System.err);
             errorsList.add(e + ". Input line number: " + linesRead);
         } finally {
             if (null != input) {
                 try {
                     input.close();
                 } catch (IOException e) {
                     e.printStackTrace(System.err);
                 }
             }
         }
 
         return new ParseResultDto(errorsList, pmts);
     }
 
 }
