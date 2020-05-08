 package fr.xebia.blog;
 
 import com.google.common.base.Optional;
 import org.junit.Test;
 
 import java.math.BigDecimal;
 import java.util.Currency;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class AmountAccumulatorTest {
 
     public static final BigDecimal _5 = new BigDecimal("5");
     public static final BigDecimal _10 = new BigDecimal("10");
     public static final Currency USD = Currency.getInstance("USD");
 
     @Test
     public void should_get_absent_for_empty_accumulator() {
         Optional<Amount> actualAmount = new AmountAccumulator().getAmount();
 
        assertThat(actualAmount.isPresent()).isFalse();
     }
 
     @Test
     public void should_get_10_USD_when_accumulate_5_USD_2_times() {
         Amount _5_USD = new Amount(_5, USD);
 
         Optional<Amount> actualAmount = new AmountAccumulator()
                 .accumulate(_5_USD)
                 .accumulate(_5_USD)
                 .getAmount();
 
        assertThat(actualAmount.isPresent()).isTrue();
        assertThat(actualAmount.get().getCurrency()).isEqualTo(USD);
        assertThat(actualAmount.get().getValue()).isEqualTo(_10);
     }
 }
