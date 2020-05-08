 package com.paymium.paytunia.PaytuniaAPI;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import com.google.gson.annotations.SerializedName;
 
 /**
  *
  * @author David FRANCOIS
  */
 public class Transfer {
 
   @SerializedName("id")
   private Integer id;
   
   @SerializedName("amount")
   private BigDecimal amount;
 
   @SerializedName("currency")
   private Currency currency;
 
   @SerializedName("bt_tx_confirmations")
   private Integer confirmations;
 
   @SerializedName("confirmed?")
   private Boolean isConfirmed;
 
   @SerializedName("required_confirmations")
   private Integer confirmationsToGo;
 
   @SerializedName("address")
   private String address;
 
   @SerializedName("email")
   private String email;
 
   @SerializedName("comment")
   private String comment;
 
   @SerializedName("bt_tx_id")
   private String bitcoinTransactionId;
 
   @SerializedName("unread")
   private Boolean isUnread;
 
   @SerializedName("created_at")
   private Date createdAt;
 
   @SerializedName("updated_at")
   private Date updatedAt;

   public String getAddress() {
     return address;
   }
 
   public void setAddress(String address) {
     this.address = address;
   }
 
   public BigDecimal getAmount() {
     return amount;
   }
 
   public void setAmount(BigDecimal amount) {
     this.amount = amount;
   }
 
   public String getBitcoinTransactionId() {
     return bitcoinTransactionId;
   }
 
   public void setBitcoinTransactionId(String bitcoinTransactionId) {
     this.bitcoinTransactionId = bitcoinTransactionId;
   }
 
   public String getComment() {
     return comment;
   }
 
   public void setComment(String comment) {
     this.comment = comment;
   }
 
   public Integer getConfirmations() {
     return confirmations;
   }
 
   public void setConfirmations(Integer confirmations) {
     this.confirmations = confirmations;
   }
 
   public Integer getConfirmationsToGo() {
     return confirmationsToGo;
   }
 
   public void setConfirmationsToGo(Integer confirmationsToGo) {
     this.confirmationsToGo = confirmationsToGo;
   }
 
   public Date getCreatedAt() {
     return createdAt;
   }
 
   public void setCreatedAt(Date createdAt) {
     this.createdAt = createdAt;
   }
 
   public Currency getCurrency() {
     return currency;
   }
 
   public void setCurrency(Currency currency) {
     this.currency = currency;
   }
 
   public String getEmail() {
     return email;
   }
 
   public void setEmail(String email) {
     this.email = email;
   }
 
   public Integer getId() {
     return id;
   }
 
   public void setId(Integer id) {
     this.id = id;
   }
 
   public Boolean getIsConfirmed() {
     return isConfirmed;
   }
 
   public void setIsConfirmed(Boolean isConfirmed) {
     this.isConfirmed = isConfirmed;
   }
 
   public Boolean getIsUnread() {
     return isUnread;
   }
 
   public void setIsUnread(Boolean isUnread) {
     this.isUnread = isUnread;
   }
 
   public Date getUpdatedAt() {
     return updatedAt;
   }
 
   public void setUpdatedAt(Date updatedAt) {
     this.updatedAt = updatedAt;
   }
 
  public String toString() {
     StringBuilder stringBuilder = new StringBuilder();
 
     stringBuilder.append("Transfer\n=============================\n");
     stringBuilder.append("Id                   : ");
     stringBuilder.append(id.toString());
     stringBuilder.append("\n");
 
     stringBuilder.append("Currency             : ");
     stringBuilder.append(currency.toString());
     stringBuilder.append("\n");
 
     stringBuilder.append("Amount               : ");
     stringBuilder.append(amount.toString());
     stringBuilder.append("\n");
 
     stringBuilder.append("Confirmations        : ");
     if (confirmations != null) {
       stringBuilder.append(confirmations.toString());
     }
     stringBuilder.append("\n");
 
     stringBuilder.append("IsConfirmed          : ");
     if (isConfirmed != null) {
       stringBuilder.append(isConfirmed.toString());
     }
     stringBuilder.append("\n");
 
     stringBuilder.append("ConfirmationsToGo    : ");
     if (confirmationsToGo != null) {
       stringBuilder.append(confirmationsToGo.toString());
     }
     stringBuilder.append("\n");
 
     stringBuilder.append("Address              : ");
     if (address != null) {
       stringBuilder.append(address.toString());
     }
     stringBuilder.append("\n");
 
     stringBuilder.append("Comment              : ");
     if (comment != null) {
       stringBuilder.append(comment.toString());
     }
     stringBuilder.append("\n");
 
     stringBuilder.append("BitcoinTransactionId : ");
     if (bitcoinTransactionId != null) {
       stringBuilder.append(bitcoinTransactionId.toString());
     }
     stringBuilder.append("\n");
 
     stringBuilder.append("isUnread             : ");
     if (isUnread != null) {
       stringBuilder.append(isUnread.toString());
     }
     stringBuilder.append("\n");
 
     stringBuilder.append("CreatedAt            : ");
     if (createdAt != null) {
       stringBuilder.append(createdAt.toString());
     }
     stringBuilder.append("\n");
 
     stringBuilder.append("UpdatedAt            : ");
     if (updatedAt != null) {
       stringBuilder.append(updatedAt.toString());
     }
     stringBuilder.append("\n\n");
 
 
     return (stringBuilder.toString());
   }
 }
