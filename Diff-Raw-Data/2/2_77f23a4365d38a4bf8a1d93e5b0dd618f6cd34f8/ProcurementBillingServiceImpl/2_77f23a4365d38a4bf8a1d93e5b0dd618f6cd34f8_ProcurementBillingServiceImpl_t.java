 package com.bis.procurement.services;
 
 import com.bis.common.DateUtils;
 import com.bis.domain.BillingProcurement;
 import com.bis.domain.PaymentHistoryProcurement;
 import com.bis.domain.ProcurementTransaction;
 import com.bis.domain.Vendor;
 import com.bis.procurement.repository.ProcurementBillingRepository;
 import com.bis.procurement.repository.ProcurementPaymentRepository;
 import com.bis.procurement.repository.ProcurementTransactionRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.Date;
 import java.util.List;
 
 @Service
 public class ProcurementBillingServiceImpl implements ProcurementBillingService {
 
     private ProcurementBillingRepository procurementBillingRepository;
     private ProcurementTransactionRepository procurementTransactionRepository;
     private ProcurementPaymentRepository procurementPaymentRepository;
 
     @Autowired
     public ProcurementBillingServiceImpl(ProcurementBillingRepository procurementBillingRepository, ProcurementTransactionRepository procurementTransactionRepository, ProcurementPaymentRepository procurementPaymentRepository) {
         this.procurementBillingRepository = procurementBillingRepository;
         this.procurementTransactionRepository = procurementTransactionRepository;
         this.procurementPaymentRepository = procurementPaymentRepository;
     }
 
     public ProcurementBillingServiceImpl(ProcurementBillingRepository procurementBillingRepository) {
         this.procurementBillingRepository = procurementBillingRepository;
     }
 
     public void addProcurementBill(BillingProcurement billingProcurement) {
         procurementBillingRepository.save(billingProcurement);
     }
 
     @Override
     public void updateProcurementBill(BillingProcurement billingProcurement) {
         procurementBillingRepository.save(billingProcurement);
     }
 
     @Override
     public BillingProcurement getProcurementBill(int billID) {
         return procurementBillingRepository.get(billID);
     }
 
     @Override
     public List<BillingProcurement> getProcurementBillList(Date fromDate, Date toDate) {
         return procurementBillingRepository.getProcurementBillList(fromDate, toDate);
     }
 
     private Float getPaymentAmountForCycle(Vendor vendor, Date fromDate, Date toDate) {
         Float totalAmount = 0F;
         List<PaymentHistoryProcurement> procurementPayments = procurementPaymentRepository.getProcurementPayments(vendor, fromDate, toDate);
         for (PaymentHistoryProcurement paymentHistoryProcurement : procurementPayments) {
             totalAmount = totalAmount + paymentHistoryProcurement.getAmount();
         }
         return totalAmount;
     }
 
     @Override
     public BillingProcurement generateProcurementBill(Vendor vendor) {
         Float totalAmount = 0f;
         Float purchaseAmount = 0f;
         Date fromDate = null;
         List<ProcurementTransaction> procurementTransactions = null;
         BillingProcurement billingProcurement = procurementBillingRepository.getLastBill(vendor);
         if (billingProcurement != null) {
             totalAmount = billingProcurement.getBalanceAmount();
             fromDate = billingProcurement.getEndDate();
             procurementTransactions = procurementTransactionRepository.getProcurementTransactions(vendor, fromDate, DateUtils.currentDate());
         } else {
             procurementTransactions = procurementTransactionRepository.getProcurementTransactions(vendor, DateUtils.currentDate());
             if (procurementTransactions != null) {
                 fromDate = DateUtils.addSecond(procurementTransactions.get(0).getDate(), 1);
             }
         }
         if (procurementTransactions != null) {
             for (ProcurementTransaction procurementTransaction : procurementTransactions) {
                 if (procurementTransaction.getTransactionType().equals('S')) {
                     purchaseAmount += procurementTransaction.getTotalAmount();
                 } else if (procurementTransaction.getTransactionType().equals('R')) {
                     purchaseAmount -= procurementTransaction.getTotalAmount();
                 }
             }
         }
         totalAmount += purchaseAmount;
         totalAmount -= getPaymentAmountForCycle(vendor, fromDate, DateUtils.currentDate());
         return new BillingProcurement(fromDate, DateUtils.currentDate(), totalAmount, vendor, purchaseAmount);
     }
 
     @Override
     public BillingProcurement getLastBill(Vendor vendor) {
         return procurementBillingRepository.getLastBill(vendor);
     }
 
     @Override
     public Date getNextBillDate(Vendor vendor) {
         Date nextBillDate = null;
         List<ProcurementTransaction> procurementTransactions = null;
         BillingProcurement billingProcurement = procurementBillingRepository.getLastBill(vendor);
         if (billingProcurement != null) {
             nextBillDate = billingProcurement.getEndDate();
             nextBillDate = DateUtils.addSecond(nextBillDate,1);
         } else {
            procurementTransactions = procurementTransactionRepository.getProcurementTransactions(vendor, DateUtils.currentDate());
             if (procurementTransactions != null) {
                 nextBillDate = procurementTransactions.get(0).getDate();
             }
         }
         return nextBillDate;
     }
 
     @Override
     public List<BillingProcurement> getProcurementBillList(Vendor vendor, Date fromDate, Date toDate) {
         return procurementBillingRepository.getProcurementBillList(vendor, fromDate, toDate);
     }
 }
