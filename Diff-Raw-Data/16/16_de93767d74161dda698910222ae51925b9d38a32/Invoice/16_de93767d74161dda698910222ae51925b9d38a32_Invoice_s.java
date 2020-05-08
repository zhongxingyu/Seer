 package kabbadi.domain;
 
 import lombok.Getter;
 import lombok.Setter;
 
 import javax.persistence.*;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Entity
 @Access(AccessType.FIELD)
 @Getter
 @Setter
 public class Invoice implements Comparable<Invoice> {
 
     public static final String INVOICE_NUMBER = "invoiceNumber";
 
     private String invoiceNumber;
 
     private String STPIApprovalNumberAndDate;
     private String descriptionOfGoods;
     private String currency;
     private BigDecimal foreignCurrency;
     private BigDecimal amountSTPIApproval;
     private BigDecimal CIFValueInINR;
     private String bondNumber;
     private Date dateOfArrival;
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
     private String loanBasis;
     private BigDecimal freeOfCharge;
     private String status;
     private String remarks;
     private String purchaseOrderNumber;
     private String location;
     private Date dateOfCommissioning;
     private String groupOfAssets;
     private String costCenter;
     private Date dateOfInvoice;
     private String supplierNameAndAddress;
     private BigDecimal openingPurchaseValueAsOnApril01;
     private BigDecimal additionsDuringTheYear;
     private BigDecimal deletionsDuringTheYear;
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Integer id;
     
     @OneToMany(
          cascade = {CascadeType.ALL},
          fetch = FetchType.EAGER,
            mappedBy = "invoice"
     )
     private List<Asset> assetList = new ArrayList<Asset>();
 
     public Invoice() {
     }
 
     public boolean valid() {
         return invoiceNumber != null && !invoiceNumber.isEmpty();
     }
 
     public BigDecimal totalPurchaseValue() {
         if(openingPurchaseValueAsOnApril01 == null || additionsDuringTheYear == null || deletionsDuringTheYear == null)
             return null;
 
         return openingPurchaseValueAsOnApril01.add(additionsDuringTheYear).subtract(deletionsDuringTheYear);
     }
 
     @Override
     public int compareTo(Invoice invoice) {
         return this.invoiceNumber.compareTo(invoice.invoiceNumber);
     }
 }
