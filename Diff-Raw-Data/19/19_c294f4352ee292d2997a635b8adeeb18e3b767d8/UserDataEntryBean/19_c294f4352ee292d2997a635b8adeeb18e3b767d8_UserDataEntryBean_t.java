 package org.ees.accountancy.bean;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 @SuppressWarnings("serial")
 public class UserDataEntryBean implements Serializable {
 
     private String date = "";
     private String invalidDate = "";
    private String value = "";
     private String invalidValue = "";
     private String debit = "";
     private String credit = "";
     private String invalidCreditDebit = "";
     private String description = "";
     private List<AccountBean> accounts = null;
     private List<String> invalidMessages = new ArrayList<String>(3);
 
     public String getDate() {
 	return date;
     }
 
     public void setDate(String date) {
 	this.date = date;
     }
 
     public String getValue() {
 	return value;
     }
 
     public void setValue(String value) {
 	this.value = value;
     }
 
     public String getDebit() {
 	return debit;
     }
 
     public void setDebit(String debit) {
 	this.debit = debit;
     }
 
     public String getCredit() {
 	return credit;
     }
 
     public void setCredit(String credit) {
 	this.credit = credit;
     }
 
     public String getDescription() {
 	return description;
     }
 
     public void setDescription(String description) {
 	this.description = description;
     }
 
     public List<AccountBean> getAccounts() {
 	return accounts;
     }
 
     public void setAccounts(List<AccountBean> accounts) {
 	this.accounts = accounts;
     }
 
     public final String getInvalidDate() {
 	return invalidDate;
     }
 
     public final void setInvalidDate(String invalidDate) {
 	this.invalidDate = invalidDate;
 	invalidMessages.add(invalidDate);
     }
 
     public final String getInvalidValue() {
 	return invalidValue;
     }
 
     public final void setInvalidValue(String invalidValue) {
 	this.invalidValue = invalidValue;
 	invalidMessages.add(invalidValue);
     }
 
     public final String getInvalidCreditDebit() {
         return invalidCreditDebit;
     }
 
     public final void setInvalidCreditDebit(String invalidCreditDebit) {
         this.invalidCreditDebit = invalidCreditDebit;
         invalidMessages.add(invalidCreditDebit);
     }
 
     public List<String> getInvalidMessages() {
 	return this.invalidMessages;
     }
     
     public void setInvalidMessages(List<String> invalidMessages) {
 	this.invalidMessages = invalidMessages;
     }
     
     @Override
     public String toString() {
 	return "UserDataEntryBean [date=" + date + ", invalidDate=" + invalidDate + ", value="
 		+ value + ", invalidValue=" + invalidValue + ", debit=" + debit + ", credit="
 		+ credit + ", invalidCreditDebit=" + invalidCreditDebit + ", description="
 		+ description + ", accounts=" + accounts + "]";
     }
 
 
 }
