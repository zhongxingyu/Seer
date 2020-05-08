 package org.strangeforest.currencywatch.core;
 
 import java.math.*;

import static org.strangeforest.util.ObjectUtil.*;
 
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
		return equal(bid, rateValue.bid) && equal(ask, rateValue.ask) && equal(middle, rateValue.middle);
 	}
 
 	@Override public int hashCode() {
		int result = bid != null ? bid.hashCode() : 0;
		result = 31 * result + (ask != null ? ask.hashCode() : 0);
		return 31 * result + (middle != null ? middle.hashCode() : 0);
 	}
 
 	@Override public String toString() {
 		return String.format("RateValue{bid=%1$f, ask=%2$f, middle=%3$f}", bid, ask, middle);
 	}
 }
