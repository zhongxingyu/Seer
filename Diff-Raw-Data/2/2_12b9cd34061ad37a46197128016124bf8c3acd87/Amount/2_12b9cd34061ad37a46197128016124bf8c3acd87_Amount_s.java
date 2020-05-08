 package de.switajski.priebes.flexibleorders.domain;
 
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 
 import javax.persistence.Embeddable;
 import javax.validation.constraints.NotNull;
 
 @Embeddable
 public class Amount {
 
 	private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat(",##0.00");
 	
 	@NotNull
 	private BigDecimal value = BigDecimal.ZERO;
 	
 	@NotNull
 	private Currency currency = Currency.EUR;
 	
 	public Amount() {}
 	
 	public Amount(BigDecimal value, Currency currency) {
 		this.value = value;
 		this.currency = currency;
 	}
 	
 	public Amount(BigDecimal value){
 		this.value = value;
 	}
 
 	public BigDecimal getValue() {
 		return value;
 	}
 
 	public void setValue(BigDecimal value) {
 		this.value = value;
 	}
 
 	public Currency getCurrency() {
 		return currency;
 	}
 
 	public void setCurrency(Currency currency) {
 		this.currency = currency;
 	}
 
 	/**
 	 * 
 	 * @param negotiatedPriceNet not null and has currency
 	 * @return
 	 */
 	public Amount add(Amount negotiatedPriceNet) {
 		if (this.currency == null)
 			currency = negotiatedPriceNet.getCurrency();
 		
 		if (sameCurrency(negotiatedPriceNet))
 			return new Amount(negotiatedPriceNet.getValue().add(this.getValue()), this.currency);
 		else throw new IllegalArgumentException("tried to add amounts with different currencies");
 	}
 
 	public boolean sameCurrency(Amount negotiatedPriceNet) {
 		return negotiatedPriceNet.getCurrency().equals(this.currency);
 	}
 	
 	public String toString(){
 		String currencyChar = "";
 		switch (getCurrency()) {
			case EUR: currencyChar+= " "; break;
 			case PLN: currencyChar+= " zl";
 		}
 		String s = DECIMAL_FORMAT.format(value) + currencyChar;
 		return s;
 	}
 
 	public Amount multiply(Integer quantity) {
 		return new Amount(value.multiply(new BigDecimal(quantity)), currency);
 		
 	}
 
 	public Amount devide(double divisor) {
 		if (divisor == 0d) throw new IllegalArgumentException("Cannot devide by zero");
 		return new Amount(this.value.divide(new BigDecimal(divisor)), this.currency);
 	}
 	
 	public boolean isGreaterZero(){
 		if (this.getValue() == null)
 			return false;
 		if (this.getValue().compareTo(BigDecimal.ZERO) > 0)
 			return true;
 		return false;
 	}
 }
