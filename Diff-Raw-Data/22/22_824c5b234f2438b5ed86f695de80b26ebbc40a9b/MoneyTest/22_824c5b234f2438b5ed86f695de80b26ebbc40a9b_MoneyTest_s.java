 package com.sgcib.currencyconverter.converter;
 
 
 import org.junit.Test;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class MoneyTest {
 
    @Test
    public void should_compare_with_error_margin() {
        Money moneyWithDecimal = new Money(48.000001, "USD");
        Money moneyWithoutDecimal = new Money(48,"USD");
        assertThat(moneyWithDecimal).isEqualTo(moneyWithoutDecimal);
    }

 }
