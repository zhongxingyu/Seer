 package com.leetchi.api.client.model;
 
 import com.leetchi.api.client.Leetchi;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 public class Transfer extends Entity<Transfer> {
 
     public static final String PATH = "transfers";
     public static final Long PERSONAL_ACCOUNT = 0L;
 
     @JsonProperty("BeneficiaryWalletID")
     private Long beneficiaryWalletId = PERSONAL_ACCOUNT;
     @JsonProperty("PayerWalletID")
     private Long payerWalletId = PERSONAL_ACCOUNT;
     @JsonProperty("Amount")
     private Long amount;
     @JsonProperty("ClientFeeAmount")
     private Long clientFeeAmount;
     @JsonProperty("BeneficiaryID")
     private Long beneficiaryId;
     @JsonProperty("PayerID")
     private Long payerId;
 
     @Override
     public String path() {
         return PATH;
     }
 
     @Override
     public String path(Long id) {
         return path(PATH, id);
     }
 
     public Transfer beneficiaryWalletId(Long beneficiaryWalletId) {
         this.beneficiaryWalletId = beneficiaryWalletId;
         return this;
     }
 
     public Transfer payerWalletId(Long payerWalletId) {
         this.payerWalletId = payerWalletId;
         return this;
     }
 
     public Transfer amount(Long amount) {
         this.amount = amount;
         return this;
     }
 
     public Transfer clientFeeAmount(Long clientFeeAmount) {
         this.clientFeeAmount = clientFeeAmount;
         return this;
     }
 
     public Transfer beneficiaryId(Long beneficiaryId) {
         this.beneficiaryId = beneficiaryId;
         return this;
     }
 
     public Transfer payerId(Long payerId) {
        this.payerId = payerId;
         return this;
     }
 
     public static Transfer fetch(Long transfer) throws Exception {
         return Leetchi.fetch(path(PATH, transfer), Transfer.class);
     }
 
     public Long getClientFeeAmount() {
         return clientFeeAmount;
     }
 
     public Long getAmount() {
         return amount;
     }
 
     public Long getBeneficiaryWalletId() {
         return beneficiaryWalletId;
     }
 
     public Long getBeneficiaryId() {
         return beneficiaryId;
     }
 
     public static Transfer newTransfer() {
         return new Transfer();
     }
 
     public Long getPayerWalletId() {
         return payerWalletId;
     }
 
     public Long getPayerId() {
         return payerId;
     }
 }
