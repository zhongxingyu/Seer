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
 
 package org.mifos.application.servicefacade;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.LocalDate;
 import org.mifos.accounts.business.AccountActionDateEntity;
 import org.mifos.accounts.exceptions.AccountException;
 import org.mifos.accounts.productdefinition.util.helpers.InterestCalcType;
 import org.mifos.accounts.savings.business.SavingsBO;
 import org.mifos.accounts.savings.interest.EndOfDayDetail;
 import org.mifos.accounts.savings.interest.InterestCalculationInterval;
 import org.mifos.accounts.savings.interest.InterestCalculationIntervalHelper;
 import org.mifos.accounts.savings.interest.InterestCalculationPeriodDetail;
 import org.mifos.accounts.savings.interest.InterestCalculationPeriodResult;
 import org.mifos.accounts.savings.interest.InterestCalculator;
 import org.mifos.accounts.savings.interest.SavingsInterestCalculatorFactory;
 import org.mifos.accounts.savings.persistence.SavingsDao;
 import org.mifos.accounts.util.helpers.AccountPaymentData;
 import org.mifos.accounts.util.helpers.PaymentData;
 import org.mifos.accounts.util.helpers.SavingsPaymentData;
 import org.mifos.application.master.business.MifosCurrency;
 import org.mifos.customers.business.CustomerBO;
 import org.mifos.customers.persistence.CustomerDao;
 import org.mifos.customers.personnel.business.PersonnelBO;
 import org.mifos.customers.personnel.persistence.PersonnelDao;
 import org.mifos.dto.domain.SavingsAdjustmentDto;
 import org.mifos.dto.domain.SavingsDepositDto;
 import org.mifos.dto.domain.SavingsWithdrawalDto;
 import org.mifos.framework.hibernate.helper.HibernateTransactionHelper;
 import org.mifos.framework.hibernate.helper.HibernateTransactionHelperForStaticHibernateUtil;
 import org.mifos.framework.util.helpers.Money;
 import org.mifos.schedule.SavingsInterestScheduledEventFactory;
 import org.mifos.schedule.ScheduledEvent;
 import org.mifos.security.MifosUser;
 import org.mifos.security.util.UserContext;
 import org.mifos.service.BusinessRuleException;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 public class SavingsServiceFacadeWebTier implements SavingsServiceFacade {
 
     private final SavingsDao savingsDao;
     private final PersonnelDao personnelDao;
     private final CustomerDao customerDao;
     private HibernateTransactionHelper transactionHelper = new HibernateTransactionHelperForStaticHibernateUtil();
     private InterestCalculationIntervalHelper interestCalculationIntervalHelper = new InterestCalculationIntervalHelper();
 
     public SavingsServiceFacadeWebTier(SavingsDao savingsDao, PersonnelDao personnelDao, CustomerDao customerDao) {
         this.savingsDao = savingsDao;
         this.personnelDao = personnelDao;
         this.customerDao = customerDao;
     }
 
     @Override
     public void deposit(SavingsDepositDto savingsDeposit) {
 
         MifosUser user = (MifosUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 
         UserContext userContext = new UserContext();
         userContext.setBranchId(user.getBranchId());
         userContext.setId(Short.valueOf((short) user.getUserId()));
         userContext.setName(user.getUsername());
 
         SavingsBO savingsAccount = this.savingsDao.findById(savingsDeposit.getSavingsId());
 
         PersonnelBO createdBy = this.personnelDao.findPersonnelById(Short.valueOf((short) user.getUserId()));
 
         CustomerBO customer = this.customerDao.findCustomerById(savingsDeposit.getCustomerId().intValue());
 
         Money totalAmount = new Money(savingsAccount.getCurrency(), BigDecimal.valueOf(savingsDeposit.getAmount()));
         PaymentData payment = PaymentData.createPaymentData(totalAmount, createdBy, savingsDeposit.getModeOfPayment()
                 .shortValue(), savingsDeposit.getDateOfDeposit().toDateMidnight().toDate());
         if (savingsDeposit.getDateOfReceipt() != null) {
             payment.setReceiptDate(savingsDeposit.getDateOfReceipt().toDateMidnight().toDate());
         }
         payment.setReceiptNum(savingsDeposit.getReceiptId());
         payment.setCustomer(customer);
 
         for (AccountActionDateEntity installment : savingsAccount.getTotalInstallmentsDue(savingsDeposit
                 .getCustomerId().intValue())) {
             AccountPaymentData accountPaymentData = new SavingsPaymentData(installment);
             payment.addAccountPaymentData(accountPaymentData);
         }
 
         try {
             this.transactionHelper.startTransaction();
             savingsAccount.applyPayment(payment);
 
             this.savingsDao.save(savingsAccount);
             this.transactionHelper.commitTransaction();
         } catch (AccountException e) {
             this.transactionHelper.rollbackTransaction();
             throw new BusinessRuleException(e.getKey(), e);
         } finally {
             this.transactionHelper.closeSession();
         }
     }
 
     public void setTransactionHelper(HibernateTransactionHelper transactionHelper) {
         this.transactionHelper = transactionHelper;
     }
 
     @Override
     public void withdraw(SavingsWithdrawalDto savingsWithdrawal) {
 
         MifosUser user = (MifosUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 
         UserContext userContext = new UserContext();
         userContext.setBranchId(user.getBranchId());
         userContext.setId(Short.valueOf((short) user.getUserId()));
         userContext.setName(user.getUsername());
 
         SavingsBO savingsAccount = this.savingsDao.findById(savingsWithdrawal.getSavingsId());
 
         PersonnelBO createdBy = this.personnelDao.findPersonnelById(Short.valueOf((short) user.getUserId()));
 
         CustomerBO customer = this.customerDao.findCustomerById(savingsWithdrawal.getCustomerId().intValue());
 
         Money totalAmount = new Money(savingsAccount.getCurrency(), BigDecimal.valueOf(savingsWithdrawal.getAmount()));
         PaymentData payment = PaymentData.createPaymentData(totalAmount, createdBy, savingsWithdrawal
                 .getModeOfPayment().shortValue(), savingsWithdrawal.getDateOfWithdrawal().toDateMidnight().toDate());
         if (savingsWithdrawal.getDateOfReceipt() != null) {
             payment.setReceiptDate(savingsWithdrawal.getDateOfReceipt().toDateMidnight().toDate());
         }
         payment.setReceiptNum(savingsWithdrawal.getReceiptId());
         payment.setCustomer(customer);
 
         try {
             this.transactionHelper.startTransaction();
             savingsAccount.withdraw(payment, false);
 
             this.savingsDao.save(savingsAccount);
             this.transactionHelper.commitTransaction();
         } catch (AccountException e) {
             this.transactionHelper.rollbackTransaction();
             throw new BusinessRuleException(e.getKey(), e);
         } finally {
             this.transactionHelper.closeSession();
         }
     }
 
     @Override
     public void adjustTransaction(SavingsAdjustmentDto savingsAdjustment) {
 
         MifosUser user = (MifosUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 
         UserContext userContext = new UserContext();
         userContext.setBranchId(user.getBranchId());
         userContext.setId(Short.valueOf((short) user.getUserId()));
         userContext.setName(user.getUsername());
 
         SavingsBO savingsAccount = this.savingsDao.findById(savingsAdjustment.getSavingsId());
         savingsAccount.updateDetails(userContext);
 
         Money amountAdjustedTo = new Money(savingsAccount.getCurrency(), BigDecimal.valueOf(savingsAdjustment
                 .getAdjustedAmount()));
 
         try {
             this.transactionHelper.startTransaction();
 
             savingsAccount.adjustLastUserAction(amountAdjustedTo, savingsAdjustment.getNote());
 
             this.savingsDao.save(savingsAccount);
             this.transactionHelper.commitTransaction();
         } catch (AccountException e) {
             this.transactionHelper.rollbackTransaction();
             throw new BusinessRuleException(e.getKey(), e);
         } finally {
             this.transactionHelper.closeSession();
         }
     }
 
     @Override
     public void handleInterestCalculationAndPosting(Long savingsId) {
         SavingsBO savingsAccount = this.savingsDao.findById(savingsId);
 
         List<EndOfDayDetail> allEndOfDayDetailsForAccount = savingsDao.retrieveAllEndOfDayDetailsFor(savingsAccount
                 .getCurrency(), savingsId);
 
         if (!allEndOfDayDetailsForAccount.isEmpty()) {
             InterestCalcType interestCalcType = InterestCalcType.fromInt(savingsAccount.getInterestCalcType().getId());
             InterestCalculator interestCalculator = SavingsInterestCalculatorFactory.create(interestCalcType);
 
             // NOTE: for first interest calculation period, calculation starts from the first deposit date and not
             // activation date
            // NOTE: interest calculation and posting date are always the last day of the month (no matter what!)
             ScheduledEvent interestCalculationEvent = SavingsInterestScheduledEventFactory
                     .createScheduledEventFrom(savingsAccount.getTimePerForInstcalc());
 
             LocalDate firstDepositDate = allEndOfDayDetailsForAccount.get(0).getDate();
 
             Money totalBalanceBeforePeriod = Money.zero(savingsAccount.getCurrency());
 
             List<InterestCalculationInterval> allPossible = interestCalculationIntervalHelper.determineAllPossibleInterestCalculationPeriods(firstDepositDate, interestCalculationEvent,new LocalDate());
             for (InterestCalculationInterval interval : allPossible) {
 
                 InterestCalculationPeriodDetail interestCalculationPeriodDetail = createInterestCalculationPeriodDetail(interval,
                                                                                     allEndOfDayDetailsForAccount,
                                                                                     savingsAccount.getMinAmntForInt(),
                                                                                     savingsAccount.getCurrency(),
                                                                                     savingsAccount.getInterestRate(), totalBalanceBeforePeriod);
 
                 InterestCalculationPeriodResult periodResults = interestCalculator.calculateSavingsDetailsForPeriod(interestCalculationPeriodDetail);
 
                 totalBalanceBeforePeriod = totalBalanceBeforePeriod.add(periodResults.getTotalPrincipal());
                 // TODO - update fee calculation and do fee posting if applicable
                 // TODO - log interest information for checking on SECDEP
                 System.out.println("interest for interval " + interval + periodResults);
 
             }
         }
     }
 
     @Override
     public void calculateInterest(LocalDate startDate, LocalDate endDate, Long savingsId) {
         SavingsBO savingsAccount = this.savingsDao.findById(savingsId);
 
         InterestCalculationInterval interval = new InterestCalculationInterval(startDate, endDate);
 
         List<EndOfDayDetail> allEndOfDayDetailsForAccount = savingsDao.retrieveAllEndOfDayDetailsFor(savingsAccount
                 .getCurrency(), savingsId);
 
         if (!allEndOfDayDetailsForAccount.isEmpty()) {
             InterestCalcType interestCalcType = InterestCalcType.fromInt(savingsAccount.getInterestCalcType().getId());
             InterestCalculator interestCalculator = SavingsInterestCalculatorFactory.create(interestCalcType);
 
             LocalDate firstDepositDate = allEndOfDayDetailsForAccount.get(0).getDate();
 
             if (interval.dateFallsWithin(firstDepositDate)) {
                 interval = new InterestCalculationInterval(firstDepositDate, interval.getEndDate());
             }
 
             Money balanceBeforeInterval = new Money(savingsAccount.getCurrency());
             InterestCalculationPeriodDetail interestCalculationPeriodDetail = createInterestCalculationPeriodDetail(
                     interval, allEndOfDayDetailsForAccount, savingsAccount.getMinAmntForInt(), savingsAccount
                             .getCurrency(), savingsAccount.getInterestRate(), balanceBeforeInterval);
 
             InterestCalculationPeriodResult result = interestCalculator
                     .calculateSavingsDetailsForPeriod(interestCalculationPeriodDetail);
             savingsAccount.setInterestToBePosted(savingsAccount.getInterestToBePosted().add(result.getInterest()));
         }
     }
 
     private InterestCalculationPeriodDetail createInterestCalculationPeriodDetail(InterestCalculationInterval interval,
             List<EndOfDayDetail> allEndOfDayDetailsForAccount,
             Money minBalanceRequired,
             MifosCurrency mifosCurrency,
             Double interestRate, Money balanceBeforeInterval) {
 
         Money balance = balanceBeforeInterval;
 
         List<EndOfDayDetail> applicableDailyDetailsForPeriod = new ArrayList<EndOfDayDetail>();
         Boolean isFirstActivityBeforeInterval = Boolean.FALSE;
 
         if(!applicableDailyDetailsForPeriod.isEmpty()) {
             if(applicableDailyDetailsForPeriod.get(0).getDate().isBefore(interval.getStartDate())) {
                 isFirstActivityBeforeInterval = Boolean.TRUE;
             }
         }
 
         for (EndOfDayDetail endOfDayDetail : allEndOfDayDetailsForAccount) {
 
 //            NOTE - keithw - calculate total balance for period within calculator
             if (endOfDayDetail.getDate().isBefore(interval.getStartDate())) {
                 balance = balance.add(endOfDayDetail.getResultantAmountForDay());
                 // System.out.println(balanceBeforeInterval+", "+endOfDayDetail.getResultantAmountForDay()+" ,"+endOfDayDetail.getDate()+", "+range.getLowerDate());
             }
 
             if (interval.dateFallsWithin(endOfDayDetail.getDate())) {
                 applicableDailyDetailsForPeriod.add(endOfDayDetail);
             }
         }
 
         return new InterestCalculationPeriodDetail(interval, applicableDailyDetailsForPeriod, minBalanceRequired, balance, mifosCurrency, interestRate, isFirstActivityBeforeInterval);
     }
 }
