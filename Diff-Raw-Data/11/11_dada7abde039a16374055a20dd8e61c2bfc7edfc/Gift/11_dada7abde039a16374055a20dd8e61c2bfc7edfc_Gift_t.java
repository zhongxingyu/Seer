 package com.mpower.domain;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityListeners;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.PostLoad;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import org.apache.commons.collections.FactoryUtils;
 import org.apache.commons.collections.list.LazyList;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.mpower.domain.listener.TemporalTimestampListener;
 import com.mpower.type.GiftEntryType;
 import com.mpower.util.GiftCustomFieldMap;
 
 @Entity
 @EntityListeners(value = { TemporalTimestampListener.class })
 @Table(name = "GIFT")
 public class Gift implements SiteAware, PaymentSourceAware, AddressAware, PhoneAware, EmailAware, Customizable, Viewable, Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @SuppressWarnings("unused")
     @Transient
     private final Log logger = LogFactory.getLog(getClass());
 
     @Id
     @GeneratedValue
     @Column(name = "GIFT_ID")
     private Long id;
 
     @ManyToOne
     @JoinColumn(name = "PERSON_ID")
     private Person person;
 
     @ManyToOne(optional = true)
     @JoinColumn(name = "COMMITMENT_ID")
     private Commitment commitment;
 
     @Column(name = "COMMENTS")
     private String comments;
 
     @Column(name = "AMOUNT")
     private BigDecimal amount;
 
     @Column(name = "DEDUCTIBLE_AMOUNT")
     private BigDecimal deductibleAmount;
 
     @Column(name = "TRANSACTION_DATE", updatable = false)
     @Temporal(TemporalType.TIMESTAMP)
     private Date transactionDate;
 
     @Column(name = "POSTMARK_DATE")
     @Temporal(TemporalType.TIMESTAMP)
     private Date postmarkDate;
 
     @Column(name = "PAYMENT_TYPE")
     private String paymentType;
 
     @Column(name = "CHECK_NUMBER")
     private Integer checkNumber;
 
     @Column(name = "AUTH_CODE")
     private String authCode;
 
     @Column(name = "ORIGINAL_GIFT_ID")
     private Long originalGiftId;
 
     @Column(name = "REFUND_GIFT_ID")
     private Long refundGiftId;
 
     @Column(name = "REFUND_GIFT_TRANSACTION_DATE")
     @Temporal(TemporalType.TIMESTAMP)
     private Date refundGiftTransactionDate;
 
     @OneToMany(mappedBy = "gift", cascade = CascadeType.ALL)
     private List<GiftCustomField> giftCustomFields;
 
     @OneToMany(mappedBy = "gift", cascade = CascadeType.ALL)
     private List<DistributionLine> distributionLines;
 
     @Column(name = "DEDUCTIBLE")
     private boolean deductible = false;
 
    @ManyToOne(cascade = CascadeType.PERSIST)
     @JoinColumn(name = "PAYMENT_SOURCE_ID")
     private PaymentSource paymentSource;
 
    @ManyToOne(cascade = CascadeType.PERSIST)
     @JoinColumn(name = "ADDRESS_ID")
     private Address address;
 
    @ManyToOne(cascade = CascadeType.PERSIST)
     @JoinColumn(name = "PHONE_ID")
     private Phone phone;
 
    @ManyToOne(cascade = CascadeType.PERSIST)
     @JoinColumn(name = "EMAIL_ID")
     private Email email;
 
     @Column(name = "ENTRY_TYPE")
     @Enumerated(EnumType.STRING)
     private GiftEntryType entryType = GiftEntryType.MANUAL;
 
     @Transient
     private Map<String, CustomField> customFieldMap = null;
 
     @Transient
     private Map<String, String> fieldLabelMap = null;
 
     @Transient
     private Map<String, Object> fieldValueMap = null;
 
     @Transient
     private PaymentSource selectedPaymentSource = new PaymentSource();
 
     @Transient
     private Address selectedAddress = new Address();
 
     @Transient
     private Phone selectedPhone = new Phone();
 
     @Transient
     private Email selectedEmail = new Email();
 
     public Gift() {
     }
 
     public Gift(Commitment commitment, Date transactionDate) {
         this.commitment = commitment;
         this.person = commitment.getPerson();
         this.transactionDate = transactionDate;
         this.amount = commitment.getAmountPerGift();
     }
 
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
 
     public Commitment getCommitment() {
         return commitment;
     }
 
     public void setCommitment(Commitment commitment) {
         this.commitment = commitment;
     }
 
     public String getComments() {
         return comments;
     }
 
     public void setComments(String comments) {
         this.comments = comments;
     }
 
     public BigDecimal getAmount() {
         return amount;
     }
 
     public void setAmount(BigDecimal amount) {
         this.amount = amount;
     }
 
     public BigDecimal getDeductibleAmount() {
         return deductibleAmount;
     }
 
     public void setDeductibleAmount(BigDecimal deductibleAmount) {
         this.deductibleAmount = deductibleAmount;
     }
 
     public Date getTransactionDate() {
         return transactionDate;
     }
 
     public void setTransactionDate(Date transactionDate) {
         this.transactionDate = transactionDate;
     }
 
     public Date getPostmarkDate() {
         return postmarkDate;
     }
 
     public void setPostmarkDate(Date postmarkDate) {
         this.postmarkDate = postmarkDate;
     }
 
     public void setPaymentType(String paymentType) {
         this.paymentType = paymentType;
     }
 
     public String getPaymentType() {
         return paymentType;
     }
 
     public void setCheckNumber(Integer checkNumber) {
         this.checkNumber = checkNumber;
     }
 
     public Integer getCheckNumber() {
         return checkNumber;
     }
 
     public String getAuthCode() {
         return authCode;
     }
 
     public void setAuthCode(String authCode) {
         this.authCode = authCode;
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
 
     public Date getRefundGiftTransactionDate() {
         return refundGiftTransactionDate;
     }
 
     public void setRefundGiftTransactionDate(Date refundGiftTransactionDate) {
         this.refundGiftTransactionDate = refundGiftTransactionDate;
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
 
     @SuppressWarnings("unchecked")
     public List<DistributionLine> getDistributionLines() {
         if (distributionLines == null) {
             distributionLines = LazyList.decorate(new ArrayList<DistributionLine>(), FactoryUtils.instantiateFactory(DistributionLine.class, new Class[] { Gift.class }, new Object[] { this }));
         }
         return distributionLines;
     }
 
     public void setDistributionLines(List<DistributionLine> distributionLines) {
         this.distributionLines = distributionLines;
     }
 
     public void addDistributionLine(DistributionLine distributionLine) {
         getDistributionLines().add(distributionLine);
     }
 
     @Override
     public Map<String, String> getFieldLabelMap() {
         return fieldLabelMap;
     }
 
     @Override
     public void setFieldLabelMap(Map<String, String> fieldLabelMap) {
         this.fieldLabelMap = fieldLabelMap;
     }
 
     @Override
     public Map<String, Object> getFieldValueMap() {
         return fieldValueMap;
     }
 
     @Override
     public void setFieldValueMap(Map<String, Object> fieldValueMap) {
         this.fieldValueMap = fieldValueMap;
     }
 
     @Override
     public Site getSite() {
         return person != null ? person.getSite() : null;
     }
 
     public boolean isDeductible() {
         return deductible;
     }
 
     public void setDeductible(boolean deductible) {
         this.deductible = deductible;
     }
 
     public PaymentSource getPaymentSource() {
         if (paymentSource == null) {
             paymentSource = new PaymentSource(this.getPerson());
         }
         return paymentSource;
     }
 
     public void setPaymentSource(PaymentSource paymentSource) {
         this.paymentSource = paymentSource;
     }
 
     public Address getAddress() {
         if (address == null) {
             address = new Address(this.getPerson());
         }
         return address;
     }
 
     public void setAddress(Address address) {
         this.address = address;
     }
 
     public Phone getPhone() {
         if (phone == null) {
             phone = new Phone(this.getPerson());
         }
         return phone;
     }
 
     public void setPhone(Phone phone) {
         this.phone = phone;
     }
 
     public Email getEmail() {
         if (email == null) {
             email = new Email(this.getPerson());
         }
         return email;
     }
 
     public void setEmail(Email email) {
         this.email = email;
     }
 
     public GiftEntryType getEntryType() {
         return entryType;
     }
 
     public void setEntryType(GiftEntryType entryType) {
         this.entryType = entryType;
     }
 
     public PaymentSource getSelectedPaymentSource() {
         return selectedPaymentSource;
     }
 
     public void setSelectedPaymentSource(PaymentSource selectedPaymentSource) {
         this.selectedPaymentSource = selectedPaymentSource;
     }
 
     public Address getSelectedAddress() {
         return selectedAddress;
     }
 
     public void setSelectedAddress(Address selectedAddress) {
         this.selectedAddress = selectedAddress;
     }
 
     public Phone getSelectedPhone() {
         return selectedPhone;
     }
 
     public void setSelectedPhone(Phone selectedPhone) {
         this.selectedPhone = selectedPhone;
     }
 
     public Email getSelectedEmail() {
         return selectedEmail;
     }
 
     public void setSelectedEmail(Email selectedEmail) {
         this.selectedEmail = selectedEmail;
     }
 
     @PrePersist
     @PreUpdate
     public void normalize() {
         if (deductibleAmount == null) {
             deductibleAmount = amount;
         }
     }
     
     @PostLoad
     public void initTransient() {
         if (address != null) selectedAddress = address;
         if (phone != null) selectedPhone = phone;
         if (email != null) selectedEmail = email;
     }
 
 }
