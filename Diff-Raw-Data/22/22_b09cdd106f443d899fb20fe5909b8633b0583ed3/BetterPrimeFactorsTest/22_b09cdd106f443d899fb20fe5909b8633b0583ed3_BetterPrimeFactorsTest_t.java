 package pl.pragmatists;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static pl.pragmatists.PrimeFactors.generate;
 
 @SuppressWarnings("ALL")
 @RunWith(Parameterized.class)
 public class BetterPrimeFactorsTest {
 
   private List<Integer> expected;
  private Integer number;
 
  public BetterPrimeFactorsTest(int number, List<Integer> expected) {
     this.expected = expected;
     this.number = number;
   }
 
   @Parameterized.Parameters
   public static Collection<Object[]> data() {
     Object[][] data = new Object[][]{
         {1, listOf()},
         {2, listOf(2)},
         {3, listOf(3)},
         {4, listOf(2, 2)},
         {6, listOf(2, 3)},
         {8, listOf(2, 2, 2)},
         {9, listOf(3, 3)},
         {42, listOf(2, 3, 7)}
     };
     return Arrays.asList(data);
   }
 
   @Test
  public void testTheprimeFactors() {
     assertEquals(expected, generate(number));
   }
 
   private static ArrayList<Integer> listOf(Integer... numbers) {
     ArrayList<Integer> result = new ArrayList<Integer>();
     result.addAll(Arrays.asList(numbers));
     return result;
   }
 }
