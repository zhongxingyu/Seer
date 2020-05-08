 package com.acmetelecom;
 
 import com.acmetelecom.Utils.TimeUtils;
 import com.acmetelecom.customer.CentralCustomerDatabase;
 import com.acmetelecom.customer.CentralTariffDatabase;
 import com.acmetelecom.customer.Customer;
 import com.acmetelecom.customer.Tariff;
import org.joda.time.DateTime;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.*;
 
 public class BillingSystem {
 
     private List<CallEvent> callLog = new ArrayList<CallEvent>();
     private IBillGenerator billGenerator;
 
     public BillingSystem(IBillGenerator billGenerator) {
         this.billGenerator = billGenerator;
     }
 
     public void callInitiated(String caller, String callee) {
         callLog.add(new CallStart(caller, callee));
     }
 
     public void callCompleted(String caller, String callee) {
         callLog.add(new CallEnd(caller, callee));
     }
 
     public void createCustomerBills() {
         List<Customer> customers = CentralCustomerDatabase.getInstance().getCustomers();
         for (Customer customer : customers) {
             createBillFor(customer);
         }
         callLog.clear();
     }
 
     private void createBillFor(Customer customer) {
         List<CallEvent> customerEvents = new ArrayList<CallEvent>();
         for (CallEvent callEvent : callLog) {
             if (callEvent.getCaller().equals(customer.getPhoneNumber())) {
                 customerEvents.add(callEvent);
             }
         }
 
         List<Call> calls = new ArrayList<Call>();
 
         CallEvent start = null;
         for (CallEvent event : customerEvents) {
             if (event instanceof CallStart) {
                 start = event;
             }
             if (event instanceof CallEnd && start != null) {
                 calls.add(new Call(start, event));
                 start = null;
             }
         }
 
         BigDecimal totalBill = new BigDecimal(0);
         List<LineItem> items = new ArrayList<LineItem>();
 
         for (Call call : calls) {
 
             Tariff tariff = CentralTariffDatabase.getInstance().tarriffFor(customer);
 
             BigDecimal cost;
 
             /*
             * Utils.getPeakDuration(call.StartTime(),call.endTime())  = peakDuration
             * OffPeakDuration = call.Duration - peakDuration
             *
             *        offpeakCost = new BigDecimal(offpeakDuration.multiply(tariff.offPeakRate());
             *        peakCost    = new BigDecimal(peakDuration.multiply(tariff.peakRate());
             *
             * cost = offpeakCost + peakCost;
             * */
            int peakDurationSeconds = TimeUtils.getPeakDurationSeconds(new DateTime(call.startTime()), new DateTime(call.endTime()));
             int offPeakDurationSeconds = call.durationSeconds() - peakDurationSeconds;
             cost = new BigDecimal(peakDurationSeconds).multiply(tariff.peakRate()).add(new BigDecimal(offPeakDurationSeconds).multiply(tariff.offPeakRate()));
 
             cost = cost.setScale(0, RoundingMode.HALF_UP);
             BigDecimal callCost = cost;
             totalBill = totalBill.add(callCost);
             items.add(new LineItem(call, callCost));
         }
 
 		billGenerator.send(customer, items, MoneyFormatter.penceToPounds(totalBill));
     }
 
     public static class LineItem {
         private Call call;
         private BigDecimal callCost;
 
         public LineItem(Call call, BigDecimal callCost) {
             this.call = call;
             this.callCost = callCost;
         }
 
         public String date() {
             return call.date();
         }
 
         public String callee() {
             return call.callee();
         }
 
         public String durationMinutes() {
             return "" + call.durationSeconds() / 60 + ":" + String.format("%02d", call.durationSeconds() % 60);
         }
 
         public BigDecimal cost() {
             return callCost;
         }
     }
 }
