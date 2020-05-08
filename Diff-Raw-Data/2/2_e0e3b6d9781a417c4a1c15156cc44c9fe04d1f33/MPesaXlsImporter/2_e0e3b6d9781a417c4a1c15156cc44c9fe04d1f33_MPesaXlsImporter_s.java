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
 
 package ke.co.safaricom;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import org.apache.commons.io.IOUtils;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.joda.time.LocalDate;
 import org.mifos.StandardImport;
 import org.mifos.accounts.api.AccountPaymentParametersDto;
 import org.mifos.accounts.api.AccountReferenceDto;
 import org.mifos.accounts.api.CustomerDto;
 import org.mifos.accounts.api.InvalidPaymentReason;
 import org.mifos.accounts.api.PaymentTypeDto;
 import org.mifos.spi.ParseResultDto;
 
 /**
  * This class implements mpesa plugin which export transactions from an XLS sheet to Mifos database.
  * It uses the standard mifos API/SPI. <br>
  * <a href='http://www.mifos.org/developers/wiki/PluginManagement'>http://www.mifos.org/developers/wiki/PluginManagement</a>
  * 
  * 
  * 
  */
 public class MPesaXlsImporter extends StandardImport {
     private static final String IMPORT_TRANSACTION_ORDER = "ImportTransactionOrder";
     private static final String EXPECTED_STATUS = "Completed";
     protected static final String PAYMENT_TYPE = "MPESA";
     protected static final String EXPECTED_TRANSACTION_TYPE = "Pay Utility";
     protected static final int RECEIPT = 0;
     protected static final int TRANSACTION_DATE = 1;
     protected static final int DETAILS = 2;
     protected static final int STATUS = 3;
     protected static final int WITHDRAWN = 4;
     protected static final int PAID_IN = 5;
     protected static final int BALANCE = 6;
     protected static final int BALANCE_CONFIRMED = 7;
     protected static final int TRANSACTION_TYPE = 8;
     protected static final int OTHER_PARTY_INFO = 9;
     protected static final int TRANSACTION_PARTY_DETAILS = 10;
     protected static final int MAX_CELL_NUM = 11;
 
     private static Map<AccountReferenceDto, BigDecimal> cumulativeAmountByAccount;
     private static List<AccountPaymentParametersDto> pmts;
     private static List<String> errorsList;
     private static List<String> importTransactionOrder;
     private static int successfullyParsedRows;
 
     private Set<Integer> ignoredRowNums;
 	private Set<Integer> errorRowNums;
 
 	private BigDecimal totalAmountOfErrorRows;
 	
     @Override
     public String getDisplayName() {
         return "M-PESA Excel 97(-2007)";
     }
 
 	@Override
 	public String getPropertyNameForAdminDisplay() {
 		return "MPESA transaction order";
 	}
 
 	@Override
 	public String getPropertyValueForAdminDisplay() {
 		List<String> order = getImportTransactionOrder();
 		if (order == null || order.isEmpty())
 			return "NOT DEFINED";
 		return StringUtils.join(order, ", ");
 	}
 
     @SuppressWarnings("unchecked")
     protected List<String> getImportTransactionOrder() {
         if (importTransactionOrder == null) {
             final String importTransactionOrderKey = MPesaXlsImporter.class.getCanonicalName() + "."
                     + IMPORT_TRANSACTION_ORDER;
             importTransactionOrder = (List<String>) getAccountService()
                     .getMifosConfiguration(importTransactionOrderKey);
             if (importTransactionOrder == null) {
                 importTransactionOrder = new ArrayList<String>();
             }
         }
         return importTransactionOrder;
     }
     
     private String cellStringValue(Cell cell) {
     	if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
     		return Double.toString(cell.getNumericCellValue());
     	} else {
     		return cell.getStringCellValue();
     	}
     }
     
     private String formatErrorMessage(Row row, String message) {
     	if (row == null) {
     		return String.format("Error - %s", message);
     	}
     	if (row.getCell(RECEIPT) == null) {
     		return String.format("Row <%d> error - %s",
         			row.getRowNum() + 1,
         			message);
     	}
     	return String.format("Row <%d> error - %s - %s",
     			row.getRowNum() + 1,
     			cellStringValue(row.getCell(RECEIPT)),
     			message);
     }
     
     private String formatIgnoredErrorMessage(Row row, String message) {
     	return String.format("Row <%d> ignored - %s - %s",
     			row.getRowNum() + 1,
     			cellStringValue(row.getCell(RECEIPT)),
     			message);
     }
 
     private void addError(Row row, String message) {
 	    errorsList.add(formatErrorMessage(row, message));
 		if (!errorRowNums.contains(row.getRowNum())) {
 			try {
 				BigDecimal paidIn = BigDecimal.valueOf(row.getCell(PAID_IN).getNumericCellValue());
 				totalAmountOfErrorRows = totalAmountOfErrorRows.add(paidIn);
 			} catch (Exception e) {
 				// paid in couldn't be extracted, so skip this row
 			}
 		}
 		errorRowNums.add(row.getRowNum());
     }
 
 	private void addIgnoredMessage(Row row, String message) {
 		errorsList.add(formatIgnoredErrorMessage(row, message));
 		ignoredRowNums.add(row.getRowNum());
 	}
 
     private ByteArrayInputStream copyInputIntoByteInput(InputStream input) throws IOException {
         return new ByteArrayInputStream(IOUtils.toByteArray(input));
     }
 
     private String getPhoneNumberCandidate(Row row) {
 		String cellContents = cellStringValue(row.getCell(OTHER_PARTY_INFO));
 		String[] splitted = cellContents.split(" ");
 		if (splitted == null || splitted.length == 0) {
 			return null;
 		}
 		return splitted[0];
 	}
 
     /**
      * Returns validated phone number or null if there is no valid phone number in the row
      */
    private String validatePhoneNumber(Row row) {
 		String phoneNumber = getPhoneNumberCandidate(row);
 		if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
 			addError(row, "Cannot read client's phone number");
 			return null;
 		}
 		List<CustomerDto> customers = getCustomerSearchService().findCustomersWithGivenPhoneNumber(phoneNumber);
 		if (customers == null || customers.isEmpty()) {
 			addError(row, String.format("Client with mobile number %s was not found", phoneNumber));
 			return null;
 		} else if (customers.size() >= 2) {
 			addError(row, String.format("More than 1 client with mobile number %s was found", phoneNumber));
 			return null;
 		}
 		return phoneNumber;
     }
 
    private CustomerDto customerWithPhoneNumber(String phoneNumber) {
        return getCustomerSearchService().findCustomersWithGivenPhoneNumber(phoneNumber).get(0);
    }
 
    private void initializeParser() {
 		cumulativeAmountByAccount = new HashMap<AccountReferenceDto, BigDecimal>();
 		pmts = new ArrayList<AccountPaymentParametersDto>();
 		errorsList = new LinkedList<String>();
 		successfullyParsedRows = 0;
 		errorRowNums = new HashSet<Integer>();
 		ignoredRowNums = new HashSet<Integer>();
 		totalAmountOfErrorRows = BigDecimal.ZERO;
 	}
 
    protected boolean userDefinedProductValid(String userDefinedProduct, String phoneNumber) throws Exception {
 		AccountReferenceDto userDefinedAcc = getSavingsAccount(phoneNumber, userDefinedProduct);
 		if (userDefinedAcc != null)
 			return true;
 
 		userDefinedAcc = getLoanAccount(phoneNumber, userDefinedProduct);
 		if (userDefinedAcc != null)
 			return true;
 
 		return false;
 	}
 
    private boolean moreThanOneAccountMatchesProductCode(Row row, String phoneNumber, List<String> productNames) {
        for (String productName : productNames) {
            if (getAccountService().existsMoreThanOneLoanAccount(phoneNumber, productName) ||
                getAccountService().existsMoreThanOneSavingsAccount(phoneNumber, productName)) {
                addError(row, "More than one account matches product code " + productName);
                return true;
            }
        }
        return false;
    }
 
     @Override
     public ParseResultDto parse(final InputStream input) {
 		initializeParser();
 
         try {
             Iterator<Row> rowIterator = null;
             // Copy input into byte input to try two implementations of POI parsers: HSSF and XSSF (XML formats)
             ByteArrayInputStream copiedInput = copyInputIntoByteInput(input);
             copiedInput.mark(0);
             try {
                 rowIterator = new HSSFWorkbook(copiedInput).getSheetAt(0).iterator();
             } catch (Exception e) {
                 copiedInput.reset();
                 try {
                     rowIterator = new XSSFWorkbook(copiedInput).getSheetAt(0).iterator();
                 } catch (Exception e2) {
                     throw new MPesaXlsImporterException("Unknown file format. Supported file formats are: XLS (from Excel 2003 or older), XLSX");
                 }
             }
             int friendlyRowNum = 0;
             Row row = null;
 
             setPaymentType();
 
             skipToTransactionData(rowIterator);
 
             if (!errorsList.isEmpty()) {
                 return new ParseResultDto(errorsList, pmts);
             }
 
             /* Parse transaction data */
 
             while (rowIterator.hasNext()) {
                 try {
                     row = rowIterator.next();
 
                     friendlyRowNum = row.getRowNum() + 1;
 
                     if (!isRowValid(row, friendlyRowNum, errorsList)) {
                         continue;
                     }
                     
                     String receipt = cellStringValue(row.getCell(RECEIPT));
 
                     Date transDate;
                     try {
                         transDate = getDate(row.getCell(TRANSACTION_DATE));
                     } catch (Exception e) {
                         addError(row, "Date does not begin with expected format (YYYY-MM-DD)");
                         continue;
                     }
 
 					String phoneNumber = validatePhoneNumber(row);
 					if (phoneNumber == null) {
 						continue;
 					}
 
                     final LocalDate paymentDate = LocalDate.fromDateFields(transDate);
                     String transactionPartyDetails = null;
                     
                     if(row.getCell(TRANSACTION_PARTY_DETAILS).getCellType() == Cell.CELL_TYPE_NUMERIC) {
                         transactionPartyDetails = row.getCell(TRANSACTION_PARTY_DETAILS).getNumericCellValue() +"";
                         if(transactionPartyDetails.endsWith(".0")){
                             transactionPartyDetails = transactionPartyDetails.replace(".0", "");
                         } else {
                             throw new IllegalArgumentException("Unknown format of cell "+ TRANSACTION_PARTY_DETAILS);
                         }
                     } else if (row.getCell(TRANSACTION_PARTY_DETAILS).getCellType() == Cell.CELL_TYPE_STRING) {
                     transactionPartyDetails = row.getCell(TRANSACTION_PARTY_DETAILS).getStringCellValue();
                     }
 
 					String userDefinedProduct = getUserDefinedProduct(transactionPartyDetails);
 					List<String> parameters;
 					if (userDefinedProduct != null && !userDefinedProduct.isEmpty() &&
 								userDefinedProductValid(userDefinedProduct, phoneNumber)) {
 						parameters = Arrays.asList(userDefinedProduct);
 					} else {
 						parameters = getConfiguredProducts();
 					}
 
                     if (moreThanOneAccountMatchesProductCode(row, phoneNumber, parameters)) {
                         continue;
                     }
 					
                     List<String> loanPrds = new LinkedList<String>();
                     String lastInTheOrderProdSName = parameters.get(parameters.size() - 1);
                     loanPrds.addAll(parameters.subList(0, parameters.size() - 1));
 
                     checkBlank(lastInTheOrderProdSName, "Savings product short name", row);
 
                     BigDecimal paidInAmount = BigDecimal.ZERO;
 
                     // FIXME: possible data loss converting double to BigDecimal?
                     paidInAmount = BigDecimal.valueOf(row.getCell(PAID_IN).getNumericCellValue());
                     boolean cancelTransactionFlag = false;
 
                     List<AccountPaymentParametersDto> loanPaymentList = new ArrayList<AccountPaymentParametersDto>();
 
                     for (String loanPrd : loanPrds) {
                         BigDecimal loanAccountPaymentAmount = BigDecimal.ZERO;
                         BigDecimal loanAccountTotalDueAmount = BigDecimal.ZERO;
 
                         final AccountReferenceDto loanAccountReference = getLoanAccount(phoneNumber, loanPrd);
                         
                      // skip not found accounts as per specs P1 4.9 M-Pesa plugin
                         if(loanAccountReference == null){
                             continue;
                         }
                         
                         loanAccountTotalDueAmount = getTotalPaymentDueAmount(loanAccountReference);
 
                         if (paidInAmount.compareTo(BigDecimal.ZERO) > 0) {
                             if (paidInAmount.compareTo(loanAccountTotalDueAmount) > 0) {
                                 loanAccountPaymentAmount = loanAccountTotalDueAmount;
                                 paidInAmount = paidInAmount.subtract(loanAccountTotalDueAmount);
                             } else {
                                 loanAccountPaymentAmount = paidInAmount;
                                 paidInAmount = BigDecimal.ZERO;
                             }
                         } else {
                             loanAccountPaymentAmount = BigDecimal.ZERO;
                         }
 
                         AccountPaymentParametersDto cumulativeLoanPayment = createPaymentParametersDto(
                                 loanAccountReference, loanAccountPaymentAmount, paymentDate);
 
                         if (!isPaymentValid(cumulativeLoanPayment, row)) {
                             cancelTransactionFlag = true;
                             break;
                         }
                         loanPaymentList.add(new AccountPaymentParametersDto(getUserReferenceDto(),
                                 loanAccountReference, loanAccountPaymentAmount, paymentDate, getPaymentTypeDto(), "", new LocalDate(), receipt,
                                 customerWithPhoneNumber(phoneNumber)));
 
                     }
 
                     if (cancelTransactionFlag) {
                         continue;
                     }
 
                     BigDecimal lastInOrderAmount;
                     AccountReferenceDto lastInOrderAcc;
                     lastInOrderAcc = getSavingsAccount(phoneNumber, lastInTheOrderProdSName);
                     
                     if(lastInOrderAcc == null) {
                     	lastInOrderAcc = getLoanAccount(phoneNumber, lastInTheOrderProdSName);
                     	if (lastInOrderAcc != null) {
                     		BigDecimal totalPaymentDueAmount = getTotalPaymentDueAmount(lastInOrderAcc);
                     		if(paidInAmount.compareTo(totalPaymentDueAmount) > 0) {
                     			addError(row, "Last account is a loan account but the total paid in amount is greater than the total due amount");
                     			continue;  
                     		}
                     	}
                     }
                     
                     if(lastInOrderAcc == null) {
                         addError(row, "No valid accounts found with this transaction");
                         continue;
                     }
 
                     if (paidInAmount.compareTo(BigDecimal.ZERO) > 0) {
                         lastInOrderAmount = paidInAmount;
                         paidInAmount = BigDecimal.ZERO;
                     } else {
                         lastInOrderAmount = BigDecimal.ZERO;
                     }
                     
                     final AccountPaymentParametersDto cumulativePaymentlastAcc = createPaymentParametersDto(lastInOrderAcc,
                             lastInOrderAmount, paymentDate);
                     final AccountPaymentParametersDto lastInTheOrderAccPayment = new AccountPaymentParametersDto(
                             getUserReferenceDto(), lastInOrderAcc, lastInOrderAmount, paymentDate, getPaymentTypeDto(), "", new LocalDate(), receipt,
                             customerWithPhoneNumber(phoneNumber));
 
                     if (!isPaymentValid(cumulativePaymentlastAcc, row)) {
                         continue;
                     }
                     successfullyParsedRows+=1;
                     for (AccountPaymentParametersDto loanPayment : loanPaymentList) {
                         pmts.add(loanPayment);
                     }
                     pmts.add(lastInTheOrderAccPayment);
                 } catch (Exception e) {
                     /* catch row specific exception and continue for other rows */
                 	e.printStackTrace();
                     addError(row, e.getMessage());
                     continue;
                 }
             }
         } catch (Exception e) {
             /* Catch any exception in the process */
             e.printStackTrace();
             errorsList.add(e.getMessage() + ". Got error before reading rows");
 
         }
         return parsingResult();
     }
 
 	private ParseResultDto parsingResult() {
 		ParseResultDto result = new ParseResultDto(errorsList, pmts);
 		result.setNumberOfErrorRows(errorRowNums.size());
 		result.setNumberOfIgnoredRows(ignoredRowNums.size());
 		result.setNumberOfReadRows(result.getNumberOfErrorRows() + result.getNumberOfIgnoredRows() +
 				successfullyParsedRows);
 		if (result.getNumberOfReadRows() == 0) {
 			errorsList.add("No rows found with import data");
 		}
 		result.setTotalAmountOfTransactionsWithError(totalAmountOfErrorRows);
 		result.setTotalAmountOfTransactionsImported(sumAmountsOfPayments());
 		return result;
 	}
 
 	private BigDecimal sumAmountsOfPayments() {
 		BigDecimal result = BigDecimal.ZERO;
 		for (AccountPaymentParametersDto payment : pmts) {
 			result = result.add(payment.getPaymentAmount());
 		}
 		return result;
 	}
 
 	protected String getUserDefinedProduct(String transactionPartyDetails) {
 		if (transactionPartyDetails == null || transactionPartyDetails.trim().isEmpty())
 			return null;
 		String[] words = transactionPartyDetails.split(" ");
 		if (words.length == 0)
 			return null;
 		return words[0];
 	}
 
 	protected List<String> getConfiguredProducts() {
 		List<String> products = getImportTransactionOrder();
 		if (products == null || products.isEmpty()) {
 			throw new MPesaXlsImporterException("Account in \"Transaction Party Details\" field not found");
 		}
 		return products;
 	}
 
     private AccountPaymentParametersDto createPaymentParametersDto(final AccountReferenceDto accountReference,
             final BigDecimal paymentAmount, final LocalDate paymentDate) {
         BigDecimal totalPaymentAmountForAccount = addToRunningTotalForAccount(paymentAmount, cumulativeAmountByAccount,
                 accountReference);
         return new AccountPaymentParametersDto(getUserReferenceDto(), accountReference, totalPaymentAmountForAccount,
                 paymentDate, getPaymentTypeDto(), "");
     }
 
     /**
      * @throws Exception
      */
     private void setPaymentType() throws Exception {
         final PaymentTypeDto paymentType = findPaymentType(PAYMENT_TYPE);
 
         if (paymentType == null) {
             throw new MPesaXlsImporterException("Payment type " + PAYMENT_TYPE + " not found. Have you configured"
                     + " this payment type?");
         }
         setPaymentTypeDto(paymentType);
     }
 
     private boolean isRowValid(final Row row, final int friendlyRowNum, List<String> errorsList) throws Exception {
     	if (row.getLastCellNum() < MAX_CELL_NUM) {
             addError(row, "Missing required data");
             return false;
         }
 		if (row.getCell(STATUS) == null || row.getCell(STATUS).getStringCellValue() == null) {
 			addError(row, "Missing required data");
             return false;
 		}
     	if (!row.getCell(STATUS).getStringCellValue().trim().equals(EXPECTED_STATUS)) {
     		addIgnoredMessage(row, "Status of " + row.getCell(STATUS) + " instead of Completed");
     		return false;
     	}
 	if (!row.getCell(TRANSACTION_TYPE).getStringCellValue().trim().equalsIgnoreCase(EXPECTED_TRANSACTION_TYPE)) {
     		addIgnoredMessage(row, "Transaction type \"" + row.getCell(TRANSACTION_TYPE) +
 					"\" instead of \"" + EXPECTED_TRANSACTION_TYPE + "\"");
     		return false;
     	}
     	
         if (null == row.getCell(TRANSACTION_DATE)) {
             addError(row, "Date field is empty");
             return false;
         }
         if (null == row.getCell(TRANSACTION_PARTY_DETAILS)) {
             addError(row, "\"Transaction party details\" field is empty.");
             return false;
         }
         if (null == row.getCell(PAID_IN)) {
             addError(row, "\"Paid in\" field is empty.");
             return false;
         }
         if (row.getCell(STATUS) == null) {
             addError(row, "Status field is empty");
             return false;
         }
         else {
             String receiptNumber = cellStringValue(row.getCell(RECEIPT));
 			if (receiptNumber != null && !receiptNumber.isEmpty()) {
             	if (getAccountService().receiptExists(receiptNumber)) {
             		addError(row, "Transactions with same Receipt ID have already been imported");
             		return false;
             	}
             }
         }
         return true;
     }
 
     private void checkBlank(final String value, final String name, final Row row) {
         if (StringUtils.isBlank(value)) {
             addError(row, name + " could not be extracted");
         }
     }
 
     private boolean isPaymentValid(final AccountPaymentParametersDto cumulativePayment, final Row row)
             throws Exception {
         final List<InvalidPaymentReason> errors = getAccountService().validatePayment(cumulativePayment);
 
         if (!errors.isEmpty()) {
             for (InvalidPaymentReason error : errors) {
                 switch (error) {
                 case INVALID_DATE:
                     addError(row, "Invalid transaction date");
                     break;
                 case UNSUPPORTED_PAYMENT_TYPE:
                     addError(row, "Unsupported payment type");
                     break;
                 case INVALID_PAYMENT_AMOUNT:
                     addError(row, "Invalid payment amount");
                     break;
                 case INVALID_LOAN_STATE:
                     addError(row, "Invalid account state");
                     break;
                 default:
                     addError(row, "Invalid payment (reason unknown)");
                     break;
                 }
             }
             return false;
         }
         return true;
     }
 
     private BigDecimal getTotalPaymentDueAmount(final AccountReferenceDto advanceLoanAccount) throws Exception {
         return getAccountService().getTotalPaymentDueAmount(advanceLoanAccount);
 
     }
 
     protected AccountReferenceDto getSavingsAccount(final String phoneNumber, final String savingsProductShortName) throws Exception {
         AccountReferenceDto account = null;
         try {
             account = getAccountService().lookupSavingsAccountReferenceFromClientPhoneNumberAndSavingsProductShortName(phoneNumber, savingsProductShortName);
         } catch (Exception e) {
             if (!e.getMessage().equals("savings not found for client phone number " + phoneNumber + " and savings product short name " + savingsProductShortName)) {
                 throw e;
             }
         }
         return account;
     }
 
     protected AccountReferenceDto getLoanAccount(final String phoneNumber, final String loanProductShortName) throws Exception {
         AccountReferenceDto account = null;
         try {
             account = getAccountService().lookupLoanAccountReferenceFromClientPhoneNumberAndLoanProductShortName(phoneNumber, loanProductShortName);
         } catch (Exception e) {
             if(!e.getMessage().equals("loan not found for client phone number " + phoneNumber  + " and loan product short name " + loanProductShortName)) {
                 throw e;
             }
         }
         return account;
     }
 
     private void skipToTransactionData(final Iterator<Row> rowIterator) {
         boolean skippingRowsBeforeTransactionData = true;
         while (errorsList.isEmpty() && skippingRowsBeforeTransactionData) {
             if (!rowIterator.hasNext()) {
                 errorsList.add("No rows found with import data.");
                 break;
             }
             final Row row = rowIterator.next();
            if (row.getCell(0).getStringCellValue().trim().equals("Transactions")) {
                 skippingRowsBeforeTransactionData = false;
                 /* skip row with column descriptions */
                 rowIterator.next();
             }
         }
     }
 
     protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
 
     protected Date getDate(final Cell transDateCell) throws ParseException {
         Date date = null;
         if (transDateCell.getCellType() == Cell.CELL_TYPE_STRING) {
             final SimpleDateFormat dateAsText = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
             dateAsText.setLenient(false);
             date = dateAsText.parse(transDateCell.getStringCellValue());
         } else if (transDateCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
             date = transDateCell.getDateCellValue();
         }
         return date;
     }
 
     /**
      * M-PESA imports have multiple transactions per row. Two loan accounts and
      * one savings account, I think.
      * 
      * See <a href="http://mifosforge.jira.com/browse/MIFOS-2909">MIFOS-2909</a>.
      */
     @Override
     public int getSuccessfullyParsedRows() {
         return successfullyParsedRows;
     }
 
     class MPesaXlsImporterException extends RuntimeException {
 
         private static final long serialVersionUID = 731436914098659043L;
 
         MPesaXlsImporterException(final String msg) {
             super(msg);
         }
 
     }
 }
