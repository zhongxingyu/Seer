 package fr.xebia.blog;
 
 import com.google.common.base.Optional;
 import org.junit.Test;
 
 import java.math.BigDecimal;
 import java.util.Currency;
 
import static java.lang.String.format;
 import static org.fest.assertions.Assertions.assertThat;
 
 public class AmountAccumulatorTest {
 
     public static final BigDecimal _5 = new BigDecimal("5");
     public static final BigDecimal _10 = new BigDecimal("10");
     public static final Currency USD = Currency.getInstance("USD");
 
    private static final String ABSENCE_ERROR = "The amount shouldn't exists";
    private static final String PRESENCE_ERROR = "The amount should exists";
    private static final String CURRENCY_ERROR = "Currency of the amount";
    private static final String VALUE_ERROR = "Value of the amount";

     @Test
     public void should_get_absent_for_empty_accumulator() {
         Optional<Amount> actualAmount = new AmountAccumulator().getAmount();
 
        assertThat(actualAmount.isPresent()).overridingErrorMessage(ABSENCE_ERROR).isFalse();
     }
 
     @Test
     public void should_get_10_USD_when_accumulate_5_USD_2_times() {
         Amount _5_USD = new Amount(_5, USD);
 
         Optional<Amount> actualAmount = new AmountAccumulator()
                 .accumulate(_5_USD)
                 .accumulate(_5_USD)
                 .getAmount();
 
        //PRESENCE OF AMOUNT
        assertThat(actualAmount.isPresent()).overridingErrorMessage(PRESENCE_ERROR).isTrue();

        //CURRENCY
        Currency actualCurrency = actualAmount.get().getCurrency();
        Currency expectedCurrency = USD;
        assertThat(actualCurrency).as(CURRENCY_ERROR).isEqualTo(expectedCurrency);

        //VALUE
        BigDecimal actualValue = actualAmount.get().getValue();
        BigDecimal expectedAmount = _10;
        assertThat(actualValue).as(VALUE_ERROR).isEqualTo(expectedAmount);
     }
 }
