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
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.Matchers.containsString;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Mockito.when;
 
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mifos.accounts.api.AccountService;
 import org.mifos.accounts.api.InvalidPaymentReason;
 import org.mifos.accounts.api.TransactionImport;
 import org.mifos.dto.domain.AccountPaymentParametersDto;
 import org.mifos.dto.domain.AccountReferenceDto;
 import org.mifos.dto.domain.ParseResultDto;
 import org.mifos.dto.domain.PaymentTypeDto;
 import org.mifos.dto.domain.UserReferenceDto;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class AudiBankXlsImporterTest {
     TransactionImport transactionImport;
     AudiBankXlsImporter concreteImporter;
     @Mock
     AccountService accountService;
     @Mock
     AccountReferenceDto account;
     @Mock
     AccountReferenceDto accountFromGlobalAccountNum;
     @Mock
     UserReferenceDto userReferenceDto;
     @Mock
     PaymentTypeDto paymentTypeDto;
 
     List<InvalidPaymentReason> noErrors = new ArrayList<InvalidPaymentReason>();
 
     private final int idFromGlobalAccountNumber = 2;
 
     /**
      * Would rather use {@link BeforeClass}, but this causes Mockito to throw an exception insisting that
      * "MockitoRunner can only be used with Junit 4.4 or higher."
      */
     @Before
     public void setUpBeforeMethod() throws Exception {
         concreteImporter = new AudiBankXlsImporter();
         transactionImport = concreteImporter;
         transactionImport.setAccountService(accountService);
         transactionImport.setUserReferenceDto(userReferenceDto);
         when(accountService.validatePayment(any(AccountPaymentParametersDto.class))).thenReturn(noErrors);
         when(accountService.lookupLoanAccountReferenceFromId(anyInt())).thenReturn(account);
         when(accountService.lookupLoanAccountReferenceFromExternalId(anyString())).thenReturn(account);
         when(accountService.lookupLoanAccountReferenceFromGlobalAccountNumber(anyString())).thenReturn(
                 accountFromGlobalAccountNum);
         when(accountService.getMifosConfiguration("Localization.LanguageCode")).thenReturn("EN");
         when(accountService.getMifosConfiguration("Localization.CountryCode")).thenReturn("GB");
         when(accountFromGlobalAccountNum.getAccountId()).thenReturn(idFromGlobalAccountNumber);
         when(paymentTypeDto.getName()).thenReturn("Bank Audi sal");
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
         String testDataFilename = this.getClass().getResource("/audi_test.xls").getFile();
         ParseResultDto result = transactionImport.parse(new FileInputStream(testDataFilename));
         assertThat(result.getParseErrors().toString(), result.getParseErrors().size(), is(0));
         assertThat(result.getSuccessfullyParsedPayments().toString(), result.getSuccessfullyParsedPayments().size(), is(3));
     }
 
     @Test
     public void successfulImportWithMifosId() throws Exception {
         String testDataFilename = this.getClass().getResource("/audi_test_mifos_id.xls").getFile();
         ParseResultDto result = transactionImport.parse(new FileInputStream(testDataFilename));
         assertThat(result.getParseErrors().toString(), result.getParseErrors().size(), is(0));
         assertThat(result.getSuccessfullyParsedPayments().toString(), result.getSuccessfullyParsedPayments().size(), is(3));
         assertThat(result.getSuccessfullyParsedPayments().toString(), result.getSuccessfullyParsedPayments().get(1)
                 .getAccount().getAccountId(), is(idFromGlobalAccountNumber));
     }
 
     @Test
     public void missingSerialNumber() throws Exception {
         String testDataFilename = this.getClass().getResource("/missing_serial.xls").getFile();
         ParseResultDto result = transactionImport.parse(new FileInputStream(testDataFilename));
         assertThat(result.getParseErrors().toString(), result.getParseErrors().size(), is(1));
         assertThat(result.getParseErrors().toString(), result.getParseErrors().get(0), containsString("Serial value"));
     }
 }
