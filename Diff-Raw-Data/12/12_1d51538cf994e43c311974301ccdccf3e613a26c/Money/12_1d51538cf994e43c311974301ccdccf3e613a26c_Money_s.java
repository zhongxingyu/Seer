 package com.aquiles.alexandre.money;
 
 public class Money implements Expression {
 	protected int amount;
 	protected String currency;
 	
 	Money(int amount, String currency) {
 		this.amount = amount;  
 		this.currency = currency;
 	}
 	
 	@Override
 	public boolean equals(Object object) {
 		Money money = (Money) object;
 		return this.amount == money.amount
 		&& currency().equals(money.currency());
 	}
 
 	@Override
 	public String toString() {
		return amount + " " + currency ;
 	}
 
 	static Money dollar(int amount) {
 		return new Money(amount, "USD");
 	}
 
 	static Money franc(int amount) {
 		return new Money(amount, "CHF");
 	}
 
 	public Expression times(int multiplier) {
 		return new Money(amount *  multiplier, currency);
 	}
 
 	public String currency() {
 		return currency;
 	}
 	
 	public Expression plus(Expression addend) {
 		return new Sum(this, addend);
 	}
 	
	@Override
 	public Money reduce(Bank bank, String to) {
 		int rate = bank.rate(currency, to);
 		return new Money(amount / rate, to);
 	}
 
 }
