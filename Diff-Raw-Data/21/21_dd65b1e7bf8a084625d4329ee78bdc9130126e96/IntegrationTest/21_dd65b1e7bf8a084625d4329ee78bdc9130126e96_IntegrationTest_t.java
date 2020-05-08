 package com.sgcib.currencyconverter;
 
 
 import com.google.common.base.Predicate;
 import org.fluentlenium.adapter.FluentTest;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.fluentlenium.core.filter.FilterConstructor.withText;
 
 public class IntegrationTest extends FluentTest {
     private static EmbeddedJetty jetty;
 
     @BeforeClass
     public static void startupContainer() throws Exception {
         jetty = new EmbeddedJetty();
         jetty.start();
     }
 
     @AfterClass
     public static void shutdownContainer() throws Exception {
         jetty.stop();
     }
 
     @Test
     public void should_convert_EUR_to_USD() throws Exception {
         goTo("http://localhost:8181/");
         fill("#quantity").with("4");
         click("#convert");
         waitForValue(2000, "#result");
         assertThat(find("#result").getValue()).isEqualTo("5.24");
     }
 
     @Test
     public void should_convert_USD_to_EUR() throws Exception {
         goTo("http://localhost:8181/");
         fill("#quantity").with("4");
         click("#fromCurrency option", withText("USD"));
         click("#toCurrency option", withText("EUR"));
         click("#convert");
         waitForValue(2000, "#result");
         assertThat(find("#result").getValue()).isEqualTo("2.99");
     }
 
     private void waitForValue(int timeInMillis, final String target) {
         await().atMost(timeInMillis).until(new Predicate() {
             @Override
             public boolean apply(Object input) {
                 return !isNullOrEmpty(find(target).getValue());
             }
         });
     }
 
 }
