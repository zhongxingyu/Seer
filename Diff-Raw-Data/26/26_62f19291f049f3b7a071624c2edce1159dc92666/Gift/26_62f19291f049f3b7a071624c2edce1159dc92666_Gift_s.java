 package com.mpower.domain;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityListeners;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import com.mpower.domain.annotation.AutoPopulate;
 import com.mpower.domain.listener.EmptyStringNullifyerListener;
 import com.mpower.domain.listener.TemporalTimestampListener;
 import com.mpower.util.GiftCustomFieldMap;
 
 @Entity
 @EntityListeners(value = { EmptyStringNullifyerListener.class, TemporalTimestampListener.class })
 @Table(name = "GIFT")
 public class Gift implements Customizable, Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue
     @Column(name = "GIFT_ID")
     private Long id;
 
     @ManyToOne
     @JoinColumn(name = "PERSON_ID")
     private Person person;
 
     @Column(name = "COMMENTS")
     private String comments;
 
     @Column(name = "VALUE")
     private BigDecimal value;
 
     @Column(name = "PAYMENT_TYPE")
     private String paymentType;
 
     @Column(name = "CREDIT_CARD_TYPE")
     private String creditCardType;
 
     @Column(name = "CREDIT_CARD_NUMBER")
     private String creditCardNumber;
 
     @Column(name = "CREDIT_CARD_EXPIRATION_DATE")
     private Date creditCardExpirationDate;
 
     @Column(name = "CHECK_NUMBER")
     private Integer checkNumber;
 
     @Column(name = "ACH_TYPE")
     private String achType;
 
     @Column(name = "ACH_ROUTING_NUMBER")
     private String achRoutingNumber;
 
     @Column(name = "ACH_ACCOUNT_NUMBER")
     private String achAccountNumber;
 
     @Column(name = "TRANSACTION_DATE", updatable = false)
     @Temporal(TemporalType.TIMESTAMP)
     @AutoPopulate
    private Date giftEnteredDate;
 
     @Column(name = "CONFIRMATION")
     private String confirmation;
 
     @Column(name = "ORIGINAL_GIFT_ID")
     private Long originalGiftId;
 
     @Column(name = "REFUND_GIFT_ID")
     private Long refundGiftId;
 
     @OneToMany(mappedBy = "gift", cascade = CascadeType.ALL)
     private List<GiftCustomField> giftCustomFields;
 
     @Transient
     private Integer creditCardExpirationMonth;
 
     @Transient
     private Integer creditCardExpirationYear;
 
     @Transient
     private Map<String, CustomField> customFieldMap = null;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Person getPerson() {
         return person;
     }
 
     public void setPerson(Person person) {
         this.person = person;
     }
 
     public String getComments() {
         return comments;
     }
 
     public void setComments(String comments) {
         this.comments = comments;
     }
 
     public BigDecimal getValue() {
         return value;
     }
 
     public void setValue(BigDecimal value) {
         this.value = value;
     }
 
     public String getPaymentType() {
         return paymentType;
     }
 
     public void setPaymentType(String paymentType) {
         this.paymentType = paymentType;
     }
 
     public String getCreditCardType() {
         return creditCardType;
     }
 
     public void setCreditCardType(String creditCardType) {
         this.creditCardType = creditCardType;
     }
 
     public String getCreditCardNumber() {
         return creditCardNumber;
     }
 
     public void setCreditCardNumber(String creditCardNumber) {
         this.creditCardNumber = creditCardNumber;
     }
 
     public Date getCreditCardExpirationDate() {
         return creditCardExpirationDate;
     }
 
     public void setCreditCardExpirationDate(Date creditCardExpirationDate) {
         this.creditCardExpirationDate = creditCardExpirationDate;
     }
 
     public Integer getCreditCardExpirationMonth() {
         if (creditCardExpirationDate != null) {
             Calendar calendar = Calendar.getInstance();
             calendar.setTime(creditCardExpirationDate);
             creditCardExpirationMonth = calendar.get(Calendar.MONTH) + 1;
         }
         return creditCardExpirationMonth;
     }
 
     public void setCreditCardExpirationMonth(Integer creditCardExpirationMonth) {
         setExpirationDate(creditCardExpirationMonth, null);
         this.creditCardExpirationMonth = creditCardExpirationMonth;
     }
 
     public Integer getCreditCardExpirationYear() {
         if (creditCardExpirationDate != null) {
             Calendar calendar = Calendar.getInstance();
             calendar.setTime(creditCardExpirationDate);
             creditCardExpirationYear = calendar.get(Calendar.YEAR);
         }
         return creditCardExpirationYear;
     }
 
     public void setCreditCardExpirationYear(Integer creditCardExpirationYear) {
         setExpirationDate(null, creditCardExpirationYear);
         this.creditCardExpirationYear = creditCardExpirationYear;
     }
 
     private void setExpirationDate(Integer month, Integer year) {
         Calendar calendar = Calendar.getInstance();
         if (creditCardExpirationDate != null) {
             calendar.setTime(creditCardExpirationDate);
         }
         if (month != null) {
             calendar.set(Calendar.MONTH, month - 1);
         }
         if (year != null) {
             calendar.set(Calendar.YEAR, year);
         }
         calendar.set(Calendar.DAY_OF_MONTH, calendar.getMaximum(Calendar.DAY_OF_MONTH));
         calendar.set(Calendar.HOUR, calendar.getMaximum(Calendar.HOUR));
         calendar.set(Calendar.MINUTE, calendar.getMaximum(Calendar.MINUTE));
         calendar.set(Calendar.SECOND, calendar.getMaximum(Calendar.SECOND));
         creditCardExpirationDate = calendar.getTime();
         this.creditCardExpirationDate = calendar.getTime();
     }
 
    public Date getGiftEnteredDate() {
        return giftEnteredDate;
     }
 
    public void setGiftEnteredDate(Date giftEnteredDate) {
        this.giftEnteredDate = giftEnteredDate;
     }
 
     public Integer getCheckNumber() {
         return checkNumber;
     }
 
     public void setCheckNumber(Integer checkNumber) {
         this.checkNumber = checkNumber;
     }
 
     public String getAchType() {
         return achType;
     }
 
     public void setAchType(String achType) {
         this.achType = achType;
     }
 
     public String getAchRoutingNumber() {
         return achRoutingNumber;
     }
 
     public void setAchRoutingNumber(String achRoutingNumber) {
         this.achRoutingNumber = achRoutingNumber;
     }
 
     public String getAchAccountNumber() {
         return achAccountNumber;
     }
 
     public void setAchAccountNumber(String achAccountNumber) {
         this.achAccountNumber = achAccountNumber;
     }
 
     public String getConfirmation() {
         return confirmation;
     }
 
     public void setConfirmation(String confirmation) {
         this.confirmation = confirmation;
     }
 
     public List<String> getExpirationMonthList() {
         List<String> monthList = new ArrayList<String>();
         for (int i = 1; i <= 12; i++) {
             String month = String.valueOf(i);
             if (month.length() == 1) {
                 month = "0" + month;
             }
             monthList.add(month);
         }
         return monthList;
     }
 
     public List<String> getExpirationYearList() {
         List<String> yearList = new ArrayList<String>();
         int year = Calendar.getInstance().get(Calendar.YEAR);
         for (int i = 0; i < 10; i++) {
             yearList.add(String.valueOf(year + i));
         }
         return yearList;
     }
 
     public Long getOriginalGiftId() {
         return originalGiftId;
     }
 
     public void setOriginalGiftId(Long originalGiftId) {
         this.originalGiftId = originalGiftId;
     }
 
     public Long getRefundGiftId() {
         return refundGiftId;
     }
 
     public void setRefundGiftId(Long refundGiftId) {
         this.refundGiftId = refundGiftId;
     }
 
     public List<GiftCustomField> getCustomFields() {
         if (giftCustomFields == null) {
             giftCustomFields = new ArrayList<GiftCustomField>();
         }
         return giftCustomFields;
     }
 
     @SuppressWarnings("unchecked")
     public Map<String, CustomField> getCustomFieldMap() {
         if (customFieldMap == null) {
             customFieldMap = GiftCustomFieldMap.buildCustomFieldMap(getCustomFields(), this);
         }
         return customFieldMap;
     }
 }
