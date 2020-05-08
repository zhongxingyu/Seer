 package com.pocketsunited.mangopayapiwrapper.model;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 /**
  * @author Michael Duergner <michael@pocketsunited.com>
  */
 public class Withdrawal extends AbstractDateBase {
 
     @JsonProperty(
             value = "UserID")
    private Long userId;
 
     @JsonProperty(
             value = "WalletID")
    private Long walletId;
 
     @JsonProperty(
             value = "Amount")
    private Long amount;
 
     @JsonProperty(
             value = "AmountWithoutFees")
    private Long amountWithoutFees;
 
     @JsonProperty(
             value = "ClientFeeAmount")
    private Long clientFeeAmount = 0l;
 
     @JsonProperty(
             value = "IsCompleted")
    private Boolean isCompleted = Boolean.FALSE;
 
     @JsonProperty(
             value = "IsSucceeeded")
    private Boolean isSucceeded = Boolean.FALSE;
 
     @JsonProperty(
             value = "BeneficiaryID")
    private Long beneficiaryId;
 
 
     public Long getUserId() {
         return userId;
     }
 
     public Long getWalletId() {
         return walletId;
     }
 
     public Long getAmount() {
         return amount;
     }
 
     public Long getAmountWithoutFees() {
         return amountWithoutFees;
     }
 
     public Long getClientFeeAmount() {
         return clientFeeAmount;
     }
 
     public Boolean getCompleted() {
         return isCompleted;
     }
 
     public Boolean getSucceeded() {
         return isSucceeded;
     }
 
     public Long getBeneficiaryId() {
         return beneficiaryId;
     }
 
     public static class WithdrawalValidator {
 
         boolean validate(Init init) {
             return true;
         }
     }
 
     public static WithdrawalBuilder builder() {
         return new WithdrawalBuilder();
     }
 
     public static class WithdrawalBuilder extends Init<WithdrawalBuilder,Withdrawal> {
 
         WithdrawalBuilder() {
             super(new Withdrawal());
         }
 
         @Override
         WithdrawalBuilder self() {
             return this;
         }
     }
 
     public static final WithdrawalValidator WITHDRAWAL_DEFAULT_VALIDATOR = new WithdrawalValidator();
 
     public static final WithdrawalValidator WITHDRAWAL_CREATE_VALIDATOR = new WithdrawalValidator() {
 
         @Override
         boolean validate(Init init) {
             return !init.hasId() && !init.hasCreationDate() && !init.hasUserId() && init.hasValidUserId() && init.hasValidWalletId() && init.hasValidAmount() && (!init.hasClientFeeAmount() || init.hasValidClientFeeAmount()) && init.hasValidBeneficiaryId();
         }
     };
 
     static abstract class Init<T extends Init<T,U>, U extends Withdrawal> extends AbstractDateBase.Init<T,U> {
 
         private WithdrawalValidator withdrawalValidator;
 
         protected Init(U object) {
             super(object);
         }
 
         public T withWithdrawalValidator(WithdrawalValidator withdrawalValidator) {
             if (null != withdrawalValidator) {
                 this.withdrawalValidator = withdrawalValidator;
             }
             else {
                 logger.warn("'withdrawalValidator' may not be null! Discarding input, 'withdrawalValidator' is still: {}", this.withdrawalValidator);
             }
             return self();
         }
 
         public T withUserId(Long userId) {
             if (null != userId) {
                 object.userId = userId;
             }
             else {
                 logger.warn("'userId' may not be null! Discarding input, 'userId' is still: {}", object.getUserId());
             }
             return self();
         }
 
         public T withWalletId(Long walletId) {
             if (null != walletId) {
                 object.walletId = walletId;
             }
             else {
                 logger.warn("'walletId' may not be null! Discarding input, 'walletId' is still: {}", object.getWalletId());
             }
             return self();
         }
 
         public T withAmount(Long amount) {
             if (null != amount) {
                 object.amount = amount;
             }
             else {
                 logger.warn("'amount' may not be null! Discarding input, 'amount' is still: {}", object.getAmount());
             }
             return self();
         }
 
         public T withClientFeeAmount(Long clientFeeAmount) {
             object.clientFeeAmount = clientFeeAmount;
             return self();
         }
 
         public T withBeneficiaryId(Long beneficiaryId) {
             if (null != beneficiaryId) {
                 object.beneficiaryId = beneficiaryId;
             }
             else {
                 logger.warn("'beneficiaryId' may not be null! Discarding input, 'beneficiaryId' is still: {}", object.getBeneficiaryId());
             }
             return self();
         }
 
         public boolean hasWithdrawalValidator() {
             return null != withdrawalValidator;
         }
 
         public boolean hasUserId() {
             return null != object.getUserId();
         }
 
         public boolean hasValidUserId() {
             return hasUserId() && 0l < object.getUserId();
         }
 
         public boolean hasWalletId() {
             return null != object.getWalletId();
         }
 
         public boolean hasValidWalletId() {
             return hasWalletId() && 0l <= object.getWalletId();
         }
 
         public boolean hasAmount() {
             return null != object.getAmount();
         }
 
         public boolean hasValidAmount() {
             return hasAmount() && 70l < object.getAmount();
         }
 
         public boolean hasClientFeeAmount() {
             return null != object.getClientFeeAmount();
         }
 
         public boolean hasValidClientFeeAmount() {
             return hasClientFeeAmount() && 0l < object.getClientFeeAmount();
         }
 
         public boolean hasBeneficiaryId() {
             return null != object.getBeneficiaryId();
         }
 
         public boolean hasValidBeneficiaryId() {
             return hasBeneficiaryId() && 0l < object.getBeneficiaryId();
         }
 
         @Override
         boolean validate() {
             return super.validate() && (null == withdrawalValidator || withdrawalValidator.validate(this));
         }
     }
 }
