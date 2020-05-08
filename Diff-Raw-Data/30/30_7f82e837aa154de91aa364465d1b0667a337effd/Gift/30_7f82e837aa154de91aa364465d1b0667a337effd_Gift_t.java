 package com.orangeleap.tangerine.domain.paymentInfo;
 
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import javax.xml.bind.annotation.XmlType;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.core.style.ToStringCreator;
 
 import com.orangeleap.tangerine.type.GiftEntryType;
 import com.orangeleap.tangerine.type.GiftType;
 import com.orangeleap.tangerine.util.StringConstants;
 @XmlType (namespace="http://www.orangeleap.com/orangeleap/schemas")
 public class Gift extends AbstractPaymentInfoEntity {
 
 	private static final long serialVersionUID = 1L;
 	public static final String PAID = "Paid";
 	public static final String NOT_PAID = "Not Paid";
 	private String giftStatus;
 	private GiftType giftType;
 	private Long recurringGiftId;
 	private BigDecimal amount;
 	private BigDecimal deductibleAmount;
 	private Date donationDate = new Date();
	private boolean posted;
	private Date postedDate = new Date();
 	private Date postmarkDate;
 	private boolean deductible = false;
 	private Date transactionDate;
 	private String authCode = StringConstants.EMPTY;
 	private String txRefNum;
 	private String paymentStatus = StringConstants.EMPTY;
 	private String paymentMessage;
 	private GiftEntryType entryType = GiftEntryType.MANUAL;
 
 	private List<Long> associatedPledgeIds;
 	private List<Long> associatedRecurringGiftIds;
 
 	private List<AdjustedGift> adjustedGifts;
 
 	public Gift() {
 		super();
 	}
 
 	public Gift(GiftInKind giftInKind) {
 		this();
 		setGiftForGiftInKind(giftInKind);
 	}
 
 	public Gift(RecurringGift recurringGift) {
 		this();
 		setGiftForRecurringGift(recurringGift);
 	}
 
 	public String getGiftStatus() {
 		return giftStatus;
 	}
 
 	public void setGiftStatus(String giftStatus) {
 		this.giftStatus = giftStatus;
 	}
 
 	public GiftType getGiftType() {
 		return giftType;
 	}
 
 	public void setGiftType(GiftType giftType) {
 		this.giftType = giftType;
 	}
 
 	public Long getRecurringGiftId() {
 		return recurringGiftId;
 	}
 
 	public void setRecurringGiftId(Long recurringGiftId) {
 		this.recurringGiftId = recurringGiftId;
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
 
 	public Date getDonationDate() {
 		return donationDate;
 	}
 
 	public void setDonationDate(Date donationDate) {
 		this.donationDate = donationDate;
 	}
 
	public boolean isPosted() {
		return posted;
	}

	public void setPosted(boolean posted) {
		this.posted = posted;
	}

	public Date getPostedDate() {
		return postedDate;
	}

	public void setPostedDate(Date postedDate) {
		this.postedDate = postedDate;
	}

 	public Date getPostmarkDate() {
 		return postmarkDate;
 	}
 
 	public void setPostmarkDate(Date postmarkDate) {
 		this.postmarkDate = postmarkDate;
 	}
 
 	public String getAuthCode() {
 		return authCode;
 	}
 
 	public void setAuthCode(String authCode) {
 		this.authCode = authCode;
 	}
 
 	public boolean isDeductible() {
 		return deductible;
 	}
 
 	public void setDeductible(boolean deductible) {
 		this.deductible = deductible;
 	}
 
 	public GiftEntryType getEntryType() {
 		return entryType;
 	}
 
 	public void setEntryType(GiftEntryType entryType) {
 		this.entryType = entryType;
 	}
 
 	public String getTxRefNum() {
 		return txRefNum;
 	}
 
 	public void setTxRefNum(String txRefNum) {
 		this.txRefNum = txRefNum;
 	}
 
 	public String getPaymentStatus() {
 		return paymentStatus;
 	}
 
 	public void setPaymentStatus(String status) {
 		this.paymentStatus = status;
 	}
 
 	public String getPaymentMessage() {
 		return paymentMessage;
 	}
 
 	public void setPaymentMessage(String message) {
 		this.paymentMessage = message;
 	}
 
 	public List<Long> getAssociatedPledgeIds() {
 		return associatedPledgeIds;
 	}
 
 	public void setAssociatedPledgeIds(List<Long> associatedPledgeIds) {
 		this.associatedPledgeIds = associatedPledgeIds;
 	}
 
 	public void addAssociatedPledgeId(Long pledgeId) {
 		if (associatedPledgeIds == null) {
 			setAssociatedPledgeIds(new ArrayList<Long>());
 		}
 		getAssociatedPledgeIds().add(pledgeId);
 	}
 
 	public List<Long> getAssociatedRecurringGiftIds() {
 		return associatedRecurringGiftIds;
 	}
 
 	public void setAssociatedRecurringGiftIds(List<Long> associatedRecurringGiftIds) {
 		this.associatedRecurringGiftIds = associatedRecurringGiftIds;
 	}
 
 	public void addAssociatedRecurringGiftId(Long recurringGiftId) {
 		if (associatedRecurringGiftIds == null) {
 			setAssociatedRecurringGiftIds(new ArrayList<Long>());
 		}
 		getAssociatedRecurringGiftIds().add(recurringGiftId);
 	}
 
 	public List<AdjustedGift> getAdjustedGifts() {
 		return adjustedGifts;
 	}
 
 	public void setAdjustedGifts(List<AdjustedGift> adjustedGifts) {
 		this.adjustedGifts = adjustedGifts;
 	}
 
 	public Boolean getIsAuthorized() {
 		return !StringUtils.trimToEmpty(authCode).equals("");
 	}
 
 	public Boolean getIsCaptured() {
 		return !StringUtils.trimToEmpty(txRefNum).equals("");
 	}
 
 	public Boolean getIsProcessed() {
 		return !StringUtils.trimToEmpty(txRefNum).equals("");
 	}
 
 	public Boolean getIsDeclined() {
 		return !StringUtils.trimToEmpty(paymentStatus).equals("");
 	}
 
 	public void setGiftForGiftInKind(GiftInKind giftInKind) {
 		this.giftType = GiftType.GIFT_IN_KIND;
 		this.currencyCode = giftInKind.getCurrencyCode();
 		this.donationDate = giftInKind.getDonationDate();
 		this.transactionDate = giftInKind.getTransactionDate();
 		this.person = giftInKind.getPerson();
 	}
 
 	public void setGiftForRecurringGift(RecurringGift recurringGift) {
 		this.setPerson(recurringGift.getPerson());
 		this.addAssociatedRecurringGiftId(recurringGift.getId());
 
 		this.setComments(recurringGift.getComments());
 		this.setAmount(recurringGift.getAmountPerGift());
 		this.setPaymentType(recurringGift.getPaymentType());
 		this.setGiftType(GiftType.MONETARY_GIFT);
 		this.setEntryType(GiftEntryType.AUTO);
 		this.setGiftStatus(Commitment.STATUS_PENDING);
 
 		if (recurringGift.getDistributionLines() != null) {
 			List<DistributionLine> list = new ArrayList<DistributionLine>();
 			for (DistributionLine oldLine : recurringGift.getDistributionLines()) {
 				DistributionLine newLine = new DistributionLine(oldLine, recurringGift);
 				list.add(newLine);
 			}
 			this.setDistributionLines(list);
 		}
 		this.setCurrencyCode(recurringGift.getCurrencyCode());
 
 		this.setSelectedPaymentSource(recurringGift.getSelectedPaymentSource());
 		this.setSelectedAddress(recurringGift.getSelectedAddress());
 		this.setSelectedPhone(recurringGift.getSelectedPhone());
 	}
 
 	public String getShortDescription() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(getAmount()).append(", ");
 		sb.append(getGiftStatus()).append(", ");
 		sb.append(DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(donationDate));
 		return sb.toString();
 	}
 
 	@Override
 	public void prePersist() {
 		super.prePersist();
 		if (this.giftType == null) {
 			this.giftType = GiftType.MONETARY_GIFT;
 		}
 		if (deductibleAmount == null) {
 			deductibleAmount = amount;
 		}
		if (posted == false) {
			setPostedDate(null);
		}
 	}
 
 	@Override
 	public String toString() {
 		return new ToStringCreator(this).append(super.toString()).append("giftStatus", giftStatus).append("giftType", giftType).append("recurringGiftId", recurringGiftId).append("amount", amount).append(super.toString()).append("deductibleAmount", deductibleAmount).append("transactionDate",
 				transactionDate).append(super.toString()).append("donationDate", donationDate).append("postmarkDate", postmarkDate).append(super.toString()).append("authCode", authCode).append(super.toString()).append("deductible", deductible).append("txRefNum", txRefNum).append(super.toString())
 				.append("paymentStatus", paymentStatus).append("paymentMessage", paymentMessage).append(super.toString()).append("entryType", entryType).toString();
 	}
 
 }
