 /*
  * Copyright (c) 2005-2011 Grameen Foundation USA
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
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.joda.time.LocalDate;
 import org.mifos.accounts.api.InvalidPaymentReason;
 import org.mifos.dto.domain.AccountPaymentParametersDto;
 import org.mifos.dto.domain.AccountReferenceDto;
 import org.mifos.dto.domain.ParseResultDto;
 
 public class AudiBankXlsImporter extends AudiBankImporter {
 
     private static final String LANGUAGECODE = "Localization.LanguageCode";
     private static final String COUNTRYCODE = "Localization.CountryCode";
     
     @Override
     public String getDisplayName() {
         return "Audi Bank (Excel 2007)";
     }
 
     @Override
     public ParseResultDto parse(final InputStream input) {
         String language = getAccountService().getMifosConfiguration(LANGUAGECODE).toString();
         String country = getAccountService().getMifosConfiguration(COUNTRYCODE).toString();
         Locale currentLocale = new Locale(language, country);
         ResourceBundle messages = ResourceBundle.getBundle("MessagesAudiBank", currentLocale);
         
         final List<String> errorsList = new ArrayList<String>();
         final List<AccountPaymentParametersDto> pmts = new ArrayList<AccountPaymentParametersDto>();
         int friendlyRowNum = 0;
         Map<AccountReferenceDto, BigDecimal> cumulativeAmountByAccount = new HashMap<AccountReferenceDto, BigDecimal>();
 
         try {
             final HSSFWorkbook workbook = new HSSFWorkbook(input);
             final HSSFSheet sheet = workbook.getSheetAt(0);
 
             Row row = sheet.getRow(0);
             if (null == row) {
                 errorsList.add(messages.getString(AudiBankConstants.NOT_ENOUGH_INPOUT_ROW));
             }
 
             final Cell topLeftCell = row.getCell(0);
             if (errorsList.isEmpty() && null == topLeftCell) {
                 errorsList.add(messages.getString(AudiBankConstants.NOT_ENOUGH_INPOUT_CELL));
             }
 
             if (errorsList.isEmpty() && topLeftCell.getCellType() != Cell.CELL_TYPE_STRING) {
                 errorsList.add(messages.getString(AudiBankConstants.INVALID_CELL_TYPE));
             }
 
             String topLeftCellAsString = "";
             if (errorsList.isEmpty()) {
                 topLeftCellAsString = topLeftCell.getStringCellValue();
                 if (StringUtils.isBlank(topLeftCellAsString)) {
                     errorsList.add(messages.getString(AudiBankConstants.PAYMENT_TYPE_NOT_FOUND));
                 }
             }
 
             if (errorsList.isEmpty()) {
                 setPaymentTypeDto(findPaymentType(topLeftCellAsString));
                 if (getPaymentTypeDto() == null) {
                     errorsList.add(messages.getString(AudiBankConstants.NO_PAYMENT_TYPE_FOUND)+" '" + topLeftCellAsString + "'.");
                 }
             }
             row = null;
 
             final Iterator<Row> rowIterator = sheet.iterator();
 
             while (errorsList.isEmpty()) {
                 if (!rowIterator.hasNext()) {
                     errorsList.add(messages.getString(AudiBankConstants.NO_ROWS_FOUND_WITH_IMPORT_DATA));
                     break;
                 }
                 row = rowIterator.next();
                 // skip first 5 lines
                 if (row.getRowNum() >= 4) {
                     break;
                 }
             }
 
             if (errorsList.isEmpty()) {
                 row = null;
 
                 while (true) {
                     if (rowIterator.hasNext()) {
                         row = rowIterator.next();
                     } else {
                         break;
                     }
                     friendlyRowNum = (row.getRowNum() + 1);
 
                     Cell firstCell = row.getCell(0);
                     if (null == firstCell || StringUtils.isBlank(firstCell.toString())) {
                         /*
                          * Justification: this is similar to skipping blank input lines in a text file.
                          */
                         continue;
                     }
 
                     if (row.getLastCellNum() < MAX_CELL_NUM) {
                         errorsList.add(String.format(messages.getString(AudiBankConstants.NOT_ENOUGH_FIELDS), friendlyRowNum));
                         continue;
                     }
 
                     final Cell debitOrCreditCell = row.getCell(DEBIT_OR_CREDIT);
                     String debitOrCredit = null;
                     if (null != debitOrCreditCell) {
                         debitOrCredit = debitOrCreditCell.getStringCellValue().trim();
                         if (StringUtils.isBlank(debitOrCredit)) {
                             debitOrCredit = null;
                         } else {
                             if (!debitOrCredit.equalsIgnoreCase("C")) {
                                 /* not a credit: ignore */
                                 continue;
                             }
                         }
                     }
                     if (null == debitOrCredit) {
                         errorsList.add(String.format(messages.getString(AudiBankConstants.DEBIT_CREDIT_NOT_SPECIFIED), friendlyRowNum));
                         continue;
                     }
 
                     final Cell descriptionCell = row.getCell(DESCRIPTION);
                     String accountId = "";
                     if (null != descriptionCell) {
                         accountId = getAccountId(descriptionCell.getStringCellValue());
                     }
 
                     if ("".equals(accountId)) {
                         errorsList.add(messages.getString(AudiBankConstants.LOAN_ACCOUNT_ID_COULD_NOT_BE_EXTRACTED) + " " + friendlyRowNum);
                         continue;
                     }
 
                     final Cell serialCell = row.getCell(SERIAL);
                     String serial = null;
                     if (null != serialCell) {
                         serialCell.setCellType(1);
                         String serialNumericValue = serialCell.getStringCellValue();
                         if (null != serialNumericValue) {
                             serial = "" + serialNumericValue;
                         }
                     }
                     if (null == serial) {
                         errorsList.add(String.format(messages.getString(AudiBankConstants.INVALID_FORMAT_SERIAL), friendlyRowNum));
                         continue;
                     }
 
                     final Cell amountCell = row.getCell(AMOUNT);
                     BigDecimal paymentAmount = null;
                     if (null == amountCell) {
                         errorsList.add(messages.getString(AudiBankConstants.INVALID_AMOUNT) + " " + friendlyRowNum);
                         continue;
                     } else {
                         // FIXME: possible data loss converting double to BigDecimal?
                         paymentAmount = BigDecimal.valueOf(amountCell.getNumericCellValue());
                         int acceptableScale = Integer.parseInt(getAccountService().getMifosConfiguration("AccountingRules.DigitsAfterDecimal").toString());
                         if (paymentAmount.scale() > acceptableScale){
                             errorsList.add(messages.getString(AudiBankConstants.INVALID_NUMBER_OF_DECIMALS) + " " + friendlyRowNum);
                             continue;
                         }
                     }
 
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
                         errorsList
                                 .add(messages.getString(AudiBankConstants.INVALID_ACCOUNT_ID) + " " + friendlyRowNum + ": " + e.getMessage());
                         continue;
                     }
 
                    if (getAccountService().isAccountGroupLoanMember(account.getAccountId())){
                        errorsList.add(String.format(messages.getString(AudiBankConstants.ACCOUNT_IS_GROUP_LOAN_ACCOUNT_MEMBER), friendlyRowNum));
                        continue;
                    }

                     final Cell transDateCell = row.getCell(TRANS_DATE);
                     if (null == transDateCell) {
                         errorsList.add(messages.getString(AudiBankConstants.NO_VALID_TRANSACTION_DATE) + " " + friendlyRowNum);
                         continue;
                     }
                     final Date transDate = transDateCell.getDateCellValue();
                     final LocalDate paymentDate = LocalDate.fromDateFields(transDate);
                     final BigDecimal totalPaymentAmountForAccount = addToRunningTotalForAccount(paymentAmount,
                             cumulativeAmountByAccount, account);
 
                     AccountPaymentParametersDto cumulativePayment = new AccountPaymentParametersDto(
                             getUserReferenceDto(), account, totalPaymentAmountForAccount, paymentDate,
                             getPaymentTypeDto(), "serial=" + serial, serial);
 
                     AccountPaymentParametersDto payment = new AccountPaymentParametersDto(getUserReferenceDto(),
                             account, paymentAmount, paymentDate, getPaymentTypeDto(), "serial=" + serial, serial);
 
                     payment.addPaymentOption(AccountPaymentParametersDto.PaymentOptions.ALLOW_OVERPAYMENTS);
                     cumulativePayment.addPaymentOption(AccountPaymentParametersDto.PaymentOptions.ALLOW_OVERPAYMENTS);
                     List<InvalidPaymentReason> errors = getAccountService().validatePayment(cumulativePayment);
                     if (!errors.isEmpty()) {
                         for (InvalidPaymentReason error : errors) {
                             switch (error) {
                             case INVALID_DATE:
                                 errorsList.add(messages.getString(AudiBankConstants.INVALID_TRANSACTION_DATE) + " " + friendlyRowNum);
                                 break;
                             case UNSUPPORTED_PAYMENT_TYPE:
                                 errorsList.add(messages.getString(AudiBankConstants.UNSUPPORTED_PAYMENT_TYPE) + " " + friendlyRowNum);
                                 break;
                             case INVALID_PAYMENT_AMOUNT:
                                 errorsList.add(messages.getString(AudiBankConstants.INVALID_PAYMENT_AMOUNT) + " " + friendlyRowNum);
                                 break;
                             case INVALID_LOAN_STATE:
                                 errorsList.add(messages.getString(AudiBankConstants.INVALID_LOAN_STATE) + " " + friendlyRowNum);
                                 break;
                             default:
                                 errorsList.add(String.format(messages.getString(AudiBankConstants.INVALID_PAYMENT_REASON_UNKNOWN), friendlyRowNum));
                                 break;
                             }
                         }
 
                         continue;
                     }
 
                     pmts.add(payment);
                 }
             }
 
         } catch (Exception e) {
             e.printStackTrace(System.err);
             errorsList.add(e + "." + messages.getString(AudiBankConstants.INPUT_LINE_NUMBER) + " " + friendlyRowNum);
         }
 
         return new ParseResultDto(errorsList, pmts);
     }
 
     @Override
     public void store(InputStream input) throws Exception {
         // TODO Auto-generated method stub
         
     }
 }
