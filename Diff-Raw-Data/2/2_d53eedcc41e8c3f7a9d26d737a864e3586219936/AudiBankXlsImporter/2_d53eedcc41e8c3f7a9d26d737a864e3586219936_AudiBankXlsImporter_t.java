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
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.joda.time.LocalDate;
 import org.mifos.accounts.api.AccountPaymentParametersDto;
 import org.mifos.accounts.api.AccountReferenceDto;
 import org.mifos.accounts.api.InvalidPaymentReason;
 import org.mifos.spi.ParseResultDto;
 
 public class AudiBankXlsImporter extends AudiBankImporter {
 
     @Override
     public String getDisplayName() {
         return "Audi Bank (Excel 2007)";
     }
 
     @Override
     public ParseResultDto parse(final InputStream input) {
         final List<String> errorsList = new ArrayList<String>();
         final List<AccountPaymentParametersDto> pmts = new ArrayList<AccountPaymentParametersDto>();
         int friendlyRowNum = 0;
         Map<AccountReferenceDto, BigDecimal> cumulativeAmountByAccount = new HashMap<AccountReferenceDto, BigDecimal>();
 
         try {
             final HSSFWorkbook workbook = new HSSFWorkbook(input);
             final HSSFSheet sheet = workbook.getSheetAt(0);
 
             Row row = sheet.getRow(0);
             if (null == row) {
                 errorsList.add("Not enough input. Couldn't read first row.");
             }
 
             final Cell topLeftCell = row.getCell(0);
             if (errorsList.isEmpty() && null == topLeftCell) {
                 errorsList.add("Not enough input. Couldn't read first cell.");
             }
 
             if (errorsList.isEmpty() && topLeftCell.getCellType() != Cell.CELL_TYPE_STRING) {
                 errorsList.add("First cell must be a string containing a payment type.");
             }
 
             String topLeftCellAsString = "";
             if (errorsList.isEmpty()) {
                 topLeftCellAsString = topLeftCell.getStringCellValue();
                 if (StringUtils.isBlank(topLeftCellAsString)) {
                     errorsList.add("Payment type not found in first cell.");
                 }
             }
 
             if (errorsList.isEmpty()) {
                 setPaymentTypeDto(findPaymentType(topLeftCellAsString));
                 if (getPaymentTypeDto() == null) {
                     errorsList.add("No payment type found named '" + topLeftCellAsString + "'.");
                 }
             }
             row = null;
 
             final Iterator<Row> rowIterator = sheet.iterator();
 
             while (errorsList.isEmpty()) {
                 if (!rowIterator.hasNext()) {
                     errorsList.add("No rows found with import data.");
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
                         errorsList.add("Row " + friendlyRowNum + " is missing data: not enough fields.");
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
                         errorsList.add("Row " + friendlyRowNum + " is missing data: debit/credit not specified.");
                         continue;
                     }
 
                     final Cell descriptionCell = row.getCell(DESCRIPTION);
                     String accountId = "";
                     if (null != descriptionCell) {
                         accountId = getAccountId(descriptionCell.getStringCellValue());
                     }
 
                     if ("".equals(accountId)) {
                         errorsList.add("Loan account ID could not be extracted from row " + friendlyRowNum);
                         continue;
                     }
 
                     final Cell serialCell = row.getCell(SERIAL);
                     String serial = null;
                     if (null != serialCell) {
                         Double serialNumericValue = serialCell.getNumericCellValue();
                         if (null != serialNumericValue) {
                             serial = "" + serialNumericValue.intValue();
                         }
                     }
                     if (null == serial) {
                         errorsList.add("Serial value in row " + friendlyRowNum + " does not follow expected format.");
                         continue;
                     }
 
                     final Cell amountCell = row.getCell(AMOUNT);
                     BigDecimal paymentAmount = null;
                     if (null == amountCell) {
                         errorsList.add("Invalid amount in row " + friendlyRowNum);
                         continue;
                     } else {
                         // FIXME: possible data loss converting double to BigDecimal?
                         paymentAmount = new BigDecimal(amountCell.getNumericCellValue());
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
                                 .add("Error looking up account ID from row " + friendlyRowNum + ": " + e.getMessage());
                         continue;
                     }
 
                     final Cell transDateCell = row.getCell(TRANS_DATE);
                     if (null == transDateCell) {
                         errorsList.add("No valid transaction date in row " + friendlyRowNum);
                         continue;
                     }
                     final Date transDate = transDateCell.getDateCellValue();
                     final LocalDate paymentDate = LocalDate.fromDateFields(transDate);
                     final BigDecimal totalPaymentAmountForAccount = addToRunningTotalForAccount(paymentAmount,
                             cumulativeAmountByAccount, account);
 
                     AccountPaymentParametersDto cumulativePayment = new AccountPaymentParametersDto(
                             getUserReferenceDto(), account, totalPaymentAmountForAccount, paymentDate,
                             getPaymentTypeDto(), "serial=" + serial);
 
                     AccountPaymentParametersDto payment = new AccountPaymentParametersDto(getUserReferenceDto(),
                             account, paymentAmount, paymentDate, getPaymentTypeDto(), "serial=" + serial);
 
                     List<InvalidPaymentReason> errors = getAccountService().validatePayment(cumulativePayment);
                     if (!errors.isEmpty()) {
                         for (InvalidPaymentReason error : errors) {
                             switch (error) {
                             case INVALID_DATE:
                                 errorsList.add("Invalid transaction date in row " + friendlyRowNum);
                                 break;
                             case UNSUPPORTED_PAYMENT_TYPE:
                                 errorsList.add("Unsupported payment type in row " + friendlyRowNum);
                                 break;
                             case INVALID_PAYMENT_AMOUNT:
                                 errorsList.add("Invalid payment amount in row " + friendlyRowNum);
                                 break;
                            case INVALID_LOAN_STATE:
                                 errorsList.add("Invalid account state in row " + friendlyRowNum);
                                 break;
                             default:
                                 errorsList.add("Invalid payment in row " + friendlyRowNum + " (reason unknown).");
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
             errorsList.add(e + ". Input line number: " + friendlyRowNum);
         }
 
         return new ParseResultDto(errorsList, pmts);
     }
 }
