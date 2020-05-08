 package br.com.expense.model;
 
 import java.math.BigDecimal;

 import java.util.Date;
 import static br.com.expense.model.TransactionType.DEBIT;
import static java.math.BigDecimal.ZERO;
 
 public class Transaction implements Comparable<Transaction> {
 	
 	private Date date;
 	private String description;
 	private TransactionType type;
 	private Category category;
 	private CurrencyInfo currencyInfo;
 
 	public Date getDate() {
 		return date;
 	}
 	
 	public BigDecimal getValue() {
 		BigDecimal value = null;
 		if (this.currencyInfo != null) {
 			value = currencyInfo.getTotalValue();
 		}
 		
		if (DEBIT == type && value.compareTo(ZERO) > 0) {
 			value = value.multiply(new BigDecimal("-1"));
 		}
 		
 		return value;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public TransactionType getType() {
 		return type;
 	}
 
 	public void setType(TransactionType type) {
 		this.type = type;
 	}
 
 	public Category getCategory() {
 		return category;
 	}
 
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 	
 	public CurrencyInfo getCurrencyInfo() {
 		return currencyInfo;
 	}
 
 	public void setCurrencyInfo(CurrencyInfo currencyInfo) {
 		this.currencyInfo = currencyInfo;
 	}
 
 	@Override
 	public int compareTo(Transaction other) {
 		return this.getDate().compareTo(other.getDate());
 	}
 
 }
