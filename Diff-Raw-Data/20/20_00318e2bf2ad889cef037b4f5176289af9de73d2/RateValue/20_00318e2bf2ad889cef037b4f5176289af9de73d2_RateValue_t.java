 package org.strangeforest.currencywatch.core;
 
 import java.math.*;
import java.util.*;
 
 public class RateValue {
 
 	private final BigDecimal bid;
 	private final BigDecimal ask;
 	private final BigDecimal middle;
 
 	public RateValue(BigDecimal bid, BigDecimal ask, BigDecimal middle) {
 		super();
 		this.bid = bid;
 		this.ask = ask;
 		this.middle = middle;
 	}
 
 	public RateValue(String bid, String ask, String middle) {
 		this(new BigDecimal(bid), new BigDecimal(ask), new BigDecimal(middle));
 	}
 
 	public BigDecimal getBid() {
 		return bid;
 	}
 
 	public BigDecimal getAsk() {
 		return ask;
 	}
 
 	public BigDecimal getMiddle() {
 		return middle;
 	}
 
 	public RateValue setScale(int scale) {
 		return new RateValue(bid.setScale(scale), ask.setScale(scale), middle.setScale(scale));
 	}
 
 
 	// Object methods
 
 	@Override public boolean equals(Object o) {
 		if (this == o) return true;
 		if (!(o instanceof RateValue)) return false;
 		RateValue rateValue = (RateValue)o;
		return Objects.equals(bid, rateValue.bid) && Objects.equals(ask, rateValue.ask) && Objects.equals(middle, rateValue.middle);
 	}
 
 	@Override public int hashCode() {
		return 31 * (31 * Objects.hashCode(bid) + Objects.hashCode(ask)) + Objects.hashCode(middle);
 	}
 
 	@Override public String toString() {
 		return String.format("RateValue{bid=%1$f, ask=%2$f, middle=%3$f}", bid, ask, middle);
 	}
 }
