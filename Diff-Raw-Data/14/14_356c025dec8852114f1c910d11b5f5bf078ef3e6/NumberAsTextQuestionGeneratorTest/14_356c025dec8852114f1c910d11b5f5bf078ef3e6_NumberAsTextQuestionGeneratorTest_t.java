 package no.anderska.wta.questions;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.junit.Test;
 
 public class NumberAsTextQuestionGeneratorTest {
 
     private final NumberAsTextQuestionGenerator generator = new NumberAsTextQuestionGenerator(10, null);
 
     @Test
     public void shouldDealWithSmallNumbers() {
         assertThat(generator.asText(0)).isEqualTo("zero");
         assertThat(generator.asText(1)).isEqualTo("one");
         assertThat(generator.asText(5)).isEqualTo("five");
         assertThat(generator.asText(10)).isEqualTo("ten");
         assertThat(generator.asText(16)).isEqualTo("sixteen");
     }
 
     @Test
     public void shouldDealWithTens() {
         assertThat(generator.asText(21)).isEqualTo("twenty one");
         assertThat(generator.asText(30)).isEqualTo("thirty");
         assertThat(generator.asText(99)).isEqualTo("ninety nine");
     }
 
     @Test
     public void shouldDealWithHundreds() {
         assertThat(generator.asText(100)).isEqualTo("one hundred");
         assertThat(generator.asText(419)).isEqualTo("four hundred and nineteen");
     }
 
     @Test
     public void shouldDealWithThousands() {
         assertThat(generator.asText(1000)).isEqualTo("one thousand");
         assertThat(generator.asText(12414)).isEqualTo("twelve thousand four hundred and fourteen");
         assertThat(generator.asText(15017)).isEqualTo("fifteen thousand and seventeen");
        assertThat(generator.asText(15107)).isEqualTo("fifteen thousand one hundred and seven");
     }
 
     @Test
     public void shouldDealWithMillions() {
         assertThat(generator.asText(1000000)).isEqualTo("one million");
         assertThat(generator.asText(72887312)).isEqualTo("seventy two million eight hundred and eighty seven thousand three hundred and twelve");
         assertThat(generator.asText(4000005)).isEqualTo("four million and five");
         assertThat(generator.asText(4600005)).isEqualTo("four million six hundred thousand and five");
     }
 
     @Test
     public void shouldDealWithBillions() {
         assertThat(generator.asText(1000000000)).isEqualTo("one billion");
     }
 
 }
