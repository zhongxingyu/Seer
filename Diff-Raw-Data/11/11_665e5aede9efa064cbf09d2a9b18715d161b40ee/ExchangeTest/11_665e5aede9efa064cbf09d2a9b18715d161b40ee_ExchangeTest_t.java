 package com.telstra.webtest.acceptance.webdriver;
 
 import com.telstra.webtest.acceptance.pages.ExchangePage;
 import com.telstra.webtest.acceptance.pages.ExchangeResultPage;
 import com.telstra.webtest.domain.Currency;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class ExchangeTest extends BaseTest {
 
     @Test
     public void should_exchange_1_AUD_to_6_RMB() {
         ExchangePage exchangePage = navigator.toExchangePage();
        ExchangeResultPage resultPage = exchangePage.exchange(Currency.AUD, Currency.RMB, 1);
 
        double toAmount = resultPage.getToAmount();
         assertThat(toAmount, is(6.0));
     }
 
     @Test
     public void should_exchange_6_RMB_to_1_AUD() {
         ExchangePage exchangePage = navigator.toExchangePage();
         ExchangeResultPage resultPage = exchangePage.exchange(Currency.RMB, Currency.AUD, 6);
 
         double toAmount = resultPage.getToAmount();
         assertThat(toAmount, is(1.0));
     }
 
     @Test
     public void should_exchange_100_RMB_to_16_67_AUD() {
         ExchangePage exchangePage = navigator.toExchangePage();
        ExchangeResultPage resultPage = exchangePage.exchange(Currency.RMB, Currency.AUD, 100);
 
        double toAmount = resultPage.getToAmount();
         assertThat(toAmount, is(16.67));
     }
 
     @Test
     public void should_show_error_message_when_exchange_minus_100_RMB_to_AUD() {
         ExchangePage exchangePage = navigator.toExchangePage();
         exchangePage.exchange(Currency.RMB, Currency.AUD, -100);
 
         assertThat(exchangePage.hasErrorMessage("Please check your amount"), is(true));
     }
 }
