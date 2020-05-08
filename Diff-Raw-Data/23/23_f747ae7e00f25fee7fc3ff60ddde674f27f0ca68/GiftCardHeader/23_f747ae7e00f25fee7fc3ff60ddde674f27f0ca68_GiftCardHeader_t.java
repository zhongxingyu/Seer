 package is.idega.idegaweb.pheidippides.data;
 
 import is.idega.idegaweb.pheidippides.business.Currency;
 import is.idega.idegaweb.pheidippides.business.GiftCardHeaderStatus;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.PrePersist;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import com.idega.core.idgenerator.business.UUIDGenerator;
 
 @Entity
 @Table(name = GiftCardHeader.ENTITY_NAME)
 @NamedQueries({
	@NamedQuery(name = "giftCardHeader.findAll", query = "select g from GiftCardHeader g"),
	@NamedQuery(name = "giftCardHeader.findByUniqueID", query = "select g from GiftCardHeader g where g.uuid = :uuid"),
	@NamedQuery(name = "giftCardHeader.findByStatus", query = "select g from GiftCardHeader g where g.status = :status")
 })
 public class GiftCardHeader implements Serializable {
 	private static final long serialVersionUID = 5852884348639636627L;
 
 	public static final String ENTITY_NAME = "ph_gift_card_header";
 	private static final String COLUMN_ENTRY_ID = "header_id";
 	private static final String COLUMN_UUID = "uuid";
 	private static final String COLUMN_BUYER_UUID = "buyer_uuid";
 	private static final String COLUMN_BUYER_EMAIL = "buyer_email";
 	private static final String COLUMN_CURRENCY = "currency";
 
 	private static final String COLUMN_SECURITY_STRING = "security_string";
 	private static final String COLUMN_CARD_TYPE = "card_type";
 	private static final String COLUMN_CARD_NUMBER = "card_number";
 	private static final String COLUMN_PAYMENT_DATE = "payment_date";
 	private static final String COLUMN_AUTHORIZATION_NUMBER = "authorization_number";
 	private static final String COLUMN_TRANSACTION_NUMBER = "transaction_number";
 	private static final String COLUMN_REFERENCE_NUMBER = "reference_number";
 	private static final String COLUMN_COMMENT = "comment";
 	private static final String COLUMN_SALE_ID = "sale_id";
 
 	private static final String COLUMN_VALID_FROM = "valid_from";
 	private static final String COLUMN_VALID_TO = "valid_to";
 	private static final String COLUMN_CREATED_DATE = "created";
 
 	private static final String COLUMN_STATUS = "status";
 
 	@PrePersist
 	public void setDefaultValues() {
 		if (getUuid() == null || "".equals(getUuid().trim())) {
 			String uuid = UUIDGenerator.getInstance().generateUUID();
 			setUuid(uuid);
 		}
 	}
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = GiftCardHeader.COLUMN_ENTRY_ID)
 	private Long id;
 
 	@Column(name = GiftCardHeader.COLUMN_UUID)
 	private String uuid;
 
 	@Column(name = GiftCardHeader.COLUMN_BUYER_UUID)
 	private String buyer;
 
 	@Column(name = GiftCardHeader.COLUMN_BUYER_EMAIL)
 	private String email;
 
 	@Column(name = GiftCardHeader.COLUMN_CURRENCY)
 	@Enumerated(EnumType.STRING)
 	private Currency currency;
 	
 	//Valitor
 	@Column(name = GiftCardHeader.COLUMN_SECURITY_STRING)
 	private String securityString;
 	
 	@Column(name = GiftCardHeader.COLUMN_CARD_TYPE)
 	private String cardType;
 	
 	@Column(name = GiftCardHeader.COLUMN_CARD_NUMBER)
 	private String cardNumber;
 	
 	@Column(name = GiftCardHeader.COLUMN_PAYMENT_DATE)
 	private String paymentDate;
 	
 	@Column(name = GiftCardHeader.COLUMN_AUTHORIZATION_NUMBER)
 	private String authorizationNumber;
 	
 	@Column(name = GiftCardHeader.COLUMN_TRANSACTION_NUMBER)
 	private String transactionNumber;
 	
 	@Column(name = GiftCardHeader.COLUMN_REFERENCE_NUMBER)
 	private String referenceNumber;
 	
 	@Column(name = GiftCardHeader.COLUMN_COMMENT)
 	private String comment;
 	
 	@Column(name = GiftCardHeader.COLUMN_SALE_ID)
 	private String saleId;	
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = GiftCardHeader.COLUMN_VALID_FROM)
 	private Date validFrom;
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = GiftCardHeader.COLUMN_VALID_TO)
 	private Date validTo;
 	
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = GiftCardHeader.COLUMN_CREATED_DATE)
 	private Date createdDate;
 
 	@Column(name = GiftCardHeader.COLUMN_STATUS)
 	@Enumerated(EnumType.STRING)
 	private GiftCardHeaderStatus status;
 
 	public String getBuyer() {
 		return buyer;
 	}
 
 	public void setBuyer(String buyer) {
 		this.buyer = buyer;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public Currency getCurrency() {
 		return currency;
 	}
 
 	public void setCurrency(Currency currency) {
 		this.currency = currency;
 	}
 
 	public String getSecurityString() {
 		return securityString;
 	}
 
 	public void setSecurityString(String securityString) {
 		this.securityString = securityString;
 	}
 
 	public String getCardType() {
 		return cardType;
 	}
 
 	public void setCardType(String cardType) {
 		this.cardType = cardType;
 	}
 
 	public String getCardNumber() {
 		return cardNumber;
 	}
 
 	public void setCardNumber(String cardNumber) {
 		this.cardNumber = cardNumber;
 	}
 
 	public String getPaymentDate() {
 		return paymentDate;
 	}
 
 	public void setPaymentDate(String paymentDate) {
 		this.paymentDate = paymentDate;
 	}
 
 	public String getAuthorizationNumber() {
 		return authorizationNumber;
 	}
 
 	public void setAuthorizationNumber(String authorizationNumber) {
 		this.authorizationNumber = authorizationNumber;
 	}
 
 	public String getTransactionNumber() {
 		return transactionNumber;
 	}
 
 	public void setTransactionNumber(String transactionNumber) {
 		this.transactionNumber = transactionNumber;
 	}
 
 	public String getReferenceNumber() {
 		return referenceNumber;
 	}
 
 	public void setReferenceNumber(String referenceNumber) {
 		this.referenceNumber = referenceNumber;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public String getSaleId() {
 		return saleId;
 	}
 
 	public void setSaleId(String saleId) {
 		this.saleId = saleId;
 	}
 
 	public String getUuid() {
 		return uuid;
 	}
 
 	public void setUuid(String uuid) {
 		this.uuid = uuid;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	public Date getValidFrom() {
 		return validFrom;
 	}
 
 	public void setValidFrom(Date validFrom) {
 		this.validFrom = validFrom;
 	}
 
 	public Date getValidTo() {
 		return validTo;
 	}
 
 	public void setValidTo(Date validTo) {
 		this.validTo = validTo;
 	}
 
 	public Date getCreatedDate() {
 		return createdDate;
 	}
 
 	public void setCreatedDate(Date createdDate) {
 		this.createdDate = createdDate;
 	}
 
 	public GiftCardHeaderStatus getStatus() {
 		return status;
 	}
 
 	public void setStatus(GiftCardHeaderStatus status) {
 		this.status = status;
 	}
 }
