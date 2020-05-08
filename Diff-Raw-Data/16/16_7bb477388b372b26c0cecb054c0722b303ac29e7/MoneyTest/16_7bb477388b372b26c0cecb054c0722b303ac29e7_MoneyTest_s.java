 package com.sgcib.currencyconverter.converter;
 
 
 import org.junit.Test;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class MoneyTest {
 
@Test
    public void shouldCompareEqualsWithAMargin() {
    Money money1 = new Money(4807,"EUR");
    Money money2 = new Money(4807.00001,"EUR");
    assertThat(money1).isEqualTo(money2);
}
 }
