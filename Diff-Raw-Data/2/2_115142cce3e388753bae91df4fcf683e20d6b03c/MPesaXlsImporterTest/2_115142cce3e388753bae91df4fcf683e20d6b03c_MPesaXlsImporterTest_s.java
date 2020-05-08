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
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.when;
 
 import java.io.FileInputStream;
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.poi.ss.usermodel.Cell;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mifos.accounts.api.AccountPaymentParametersDto;
 import org.mifos.accounts.api.AccountReferenceDto;
 import org.mifos.accounts.api.AccountService;
 import org.mifos.accounts.api.InvalidPaymentReason;
 import org.mifos.accounts.api.PaymentTypeDto;
 import org.mifos.accounts.api.UserReferenceDto;
 import org.mifos.spi.ParseResultDto;
 import org.mifos.spi.TransactionImport;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class MPesaXlsImporterTest {
     TransactionImport transactionImport;
     MPesaXlsImporter concreteImporter;
     @Mock
     AccountService accountService;
     @Mock
     AccountReferenceDto account;
     @Mock
     UserReferenceDto userReferenceDto;
     @Mock
     PaymentTypeDto paymentTypeDto;
 
     List<InvalidPaymentReason> noErrors = new ArrayList<InvalidPaymentReason>();
 
     private final int fakeMifosAccountId = 2;
 
     /**
      * Would rather use {@link BeforeClass}, but this causes Mockito to throw an exception insisting that
      * "MockitoRunner can only be used with Junit 4.4 or higher."
      */
     @Before
     public void setUpBeforeMethod() throws Exception {
         concreteImporter = new MPesaXlsImporter();
         transactionImport = concreteImporter;
         transactionImport.setAccountService(accountService);
         transactionImport.setUserReferenceDto(userReferenceDto);
         when(accountService.validatePayment(any(AccountPaymentParametersDto.class))).thenReturn(noErrors);
         when(
                 accountService.lookupLoanAccountReferenceFromClientGovernmentIdAndLoanProductShortName(anyString(),
                         anyString())).thenReturn(account);
         when(
                 accountService.lookupSavingsAccountReferenceFromClientGovernmentIdAndSavingsProductShortName(
                         anyString(), anyString())).thenReturn(account);
        when(accountService.getTotalPayementDueAmount(any(AccountReferenceDto.class))).thenReturn(
                 BigDecimal.valueOf(1000.0));
         when(account.getAccountId()).thenReturn(fakeMifosAccountId);
         when(paymentTypeDto.getName()).thenReturn(MPesaXlsImporter.PAYMENT_TYPE);
         List<PaymentTypeDto> paymentTypeList = new ArrayList<PaymentTypeDto>();
         paymentTypeList.add(paymentTypeDto);
         when(accountService.getLoanPaymentTypes()).thenReturn(paymentTypeList);
     }
 
     /**
      * Would rather use {@link AfterClass}, but this causes Mockito to throw an exception insisting that
      * "MockitoRunner can only be used with Junit 4.4 or higher."
      */
     @After
     public void tearDownAfterMethod() {
         transactionImport = null;
         concreteImporter = null;
     }
 
     @Test
     public void successfulImport() throws Exception {
         String testDataFilename = this.getClass().getResource("/example_import.xls").getFile();
         ParseResultDto result = transactionImport.parse(new FileInputStream(testDataFilename));
         assertThat(result.getParseErrors().toString(), result.getParseErrors().size(), is(0));
         assertThat(result.getSuccessfullyParsedRows().size(), is(9));
         assertThat(result.getSuccessfullyParsedRows().get(1).getAccount().getAccountId(), is(fakeMifosAccountId));
     }
 
     @Test
     public void canParseClientIdentifiers() {
         assertThat(concreteImporter.parseClientIdentifiers("28 bl"), is(new String[] { "28", "bl" }));
     }
 
     @Mock
     Cell cellWithDate;
 
     @Test
     public void canParseTextBasedDate() throws Exception {
         String fakeDateString = "2009-10-15 14:52:51";
         when(cellWithDate.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
         when(cellWithDate.getStringCellValue()).thenReturn(fakeDateString);
         Date expected = new SimpleDateFormat(MPesaXlsImporter.dateFormat).parse(fakeDateString);
         assertThat(concreteImporter.getDate(cellWithDate), is(expected));
     }
 }
