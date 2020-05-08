 package kabbadi.domain;
 
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;
 
 import javax.persistence.*;
 import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
 import java.util.Date;
 
 @Entity
 @Access(AccessType.FIELD)
 @Getter
 @Setter
 @NoArgsConstructor
 public class Invoice {
 
    public static final String INVOICE_NUMBER_PROPERTY = "invoiceNumber";
 
     private String invoiceNumber;
 
     private String STPIApprovalNumberAndDate;
     private String descriptionOfGoods;
     private String currency;
     private BigDecimal foreignCurrency;
     private BigDecimal amountSTPIApproval;
     private BigDecimal CIFValueInINR;
     private String bondNumber;
 
     private Date bondDate;
     private String billOfEntryNumber;
     private Date billOfEntryDate;
     private BigDecimal assessableValueInINR;
     private BigDecimal dutyExempt;
     private BigDecimal twentyFivePercentDF;
     private BigDecimal CGApprovedInINR;
     private BigDecimal dutyForgone;
     private BigDecimal runningBalance;
     private BigDecimal outrightPurchase;
     private boolean loanBasis;
     private boolean freeOfCharge;
     private String status;
     private String remarks;
     private String purchaseOrderNumber;
     private String location;
     
     //Finance Fields
     private Date dateOfInvoice;
     private String supplierNameAndAddress;
 
     //TODO Finance Calculated fields
     // openingPurchaseValueAsOnApril01
     // additionsDuringTheYear
     // deletionDuringTheYear
     // totalPurchaseValueOnDecember31
     // totalDepreciation
 
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private int id;
 
     public boolean valid() {
         return invoiceNumber!=null && !invoiceNumber.isEmpty();
     }
 }
