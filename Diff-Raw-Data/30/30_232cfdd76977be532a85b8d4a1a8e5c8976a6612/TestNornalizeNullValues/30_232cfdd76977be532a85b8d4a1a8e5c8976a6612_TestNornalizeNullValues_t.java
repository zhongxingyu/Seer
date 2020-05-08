 package com.barchart.util.value.impl;
 
 import org.junit.Test;
 
 import com.barchart.util.value.api.Decimal;
 import com.barchart.util.value.api.Price;
 
 public class TestNornalizeNullValues {
 
 	private static final long TIMEOUT_MILLIS = 250;
 
 	@Test(timeout = TIMEOUT_MILLIS)
 	public void testNormalizeNullPrice() {
 		Price price = ValueConst.NULL_PRICE;
 		price.norm();
 	}
 
 	@Test(timeout = TIMEOUT_MILLIS)
 	public void testNormalizeZeroPrice() {
 		Price price = ValueBuilder.newPrice(0.0);
 		price.norm();
 	}
 	
 	@Test(timeout = TIMEOUT_MILLIS)
 	public void testNormalizeNullDecimal() {
 		Decimal decimal = ValueConst.NULL_DECIMAL;
 		decimal.norm();
 	}
 
 	@Test(timeout = TIMEOUT_MILLIS)
 	public void testNormalizeNullFraction() {
 		Decimal decimal = ValueConst.NULL_FRACTION;
 		decimal.norm();
 	}
 	
 }
