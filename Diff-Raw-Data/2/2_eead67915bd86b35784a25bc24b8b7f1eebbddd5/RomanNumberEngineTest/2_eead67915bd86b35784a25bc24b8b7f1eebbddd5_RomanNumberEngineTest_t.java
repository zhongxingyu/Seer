 package no.anderska.wta.engines;
 
 import org.junit.Test;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class RomanNumberEngineTest {
 
    private final RomanNumberEngine romanNumberEngine = new RomanNumberEngine(10,10);
 
     @Test
     public void oneIsI() throws Exception {
         assertThat(romanNumberEngine.romanNumber(1)).isEqualTo("I");
     }
 
     @Test
     public void twoIsII() throws Exception {
         assertThat(romanNumberEngine.romanNumber(2)).isEqualTo("II");
     }
 
     @Test
     public void threeIsIII() throws Exception {
         assertThat(romanNumberEngine.romanNumber(3)).isEqualTo("III");
     }
 
     @Test
     public void fourIsIV() throws Exception {
         assertThat(romanNumberEngine.romanNumber(4)).isEqualTo("IV");
     }
 
     @Test
     public void fiveIsV() throws Exception {
         assertThat(romanNumberEngine.romanNumber(5)).isEqualTo("V");
     }
 
     @Test
     public void shouldMatch() throws Exception {
         assertThat(romanNumberEngine.romanNumber(9)).isEqualTo("IX");
         assertThat(romanNumberEngine.romanNumber(10)).isEqualTo("X");
         assertThat(romanNumberEngine.romanNumber(30)).isEqualTo("XXX");
         assertThat(romanNumberEngine.romanNumber(40)).isEqualTo("XL");
         assertThat(romanNumberEngine.romanNumber(42)).isEqualTo("XLII");
         assertThat(romanNumberEngine.romanNumber(50)).isEqualTo("L");
         assertThat(romanNumberEngine.romanNumber(56)).isEqualTo("LVI");
         assertThat(romanNumberEngine.romanNumber(99)).isEqualTo("XCIX");
         assertThat(romanNumberEngine.romanNumber(200)).isEqualTo("CC");
         assertThat(romanNumberEngine.romanNumber(400)).isEqualTo("CD");
         assertThat(romanNumberEngine.romanNumber(500)).isEqualTo("D");
         assertThat(romanNumberEngine.romanNumber(744)).isEqualTo("DCCXLIV");
         assertThat(romanNumberEngine.romanNumber(999)).isEqualTo("CMXCIX");
     }
 
 }
