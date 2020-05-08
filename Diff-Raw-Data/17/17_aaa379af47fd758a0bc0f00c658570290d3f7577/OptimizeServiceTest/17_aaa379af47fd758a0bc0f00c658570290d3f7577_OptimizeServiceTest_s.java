 package jajascript;
 
 import org.junit.experimental.theories.DataPoints;
 import org.junit.experimental.theories.Theories;
 import org.junit.experimental.theories.Theory;
 import org.junit.runner.RunWith;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 /**
  * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
  */
 @RunWith(Theories.class)
 public class OptimizeServiceTest {
 
     OptimizeService s = new OptimizeService();
 
     @DataPoints
     public static Combination[] combinations = {
             new Combination("[\n" +
                     "    { \"VOL\": \"MONAD42\", \"DEPART\": 0, \"DUREE\": 5, \"PRIX\": 10 },\n" +
                     "    { \"VOL\": \"META18\", \"DEPART\": 3, \"DUREE\": 7, \"PRIX\": 14 },\n" +
                     "    { \"VOL\": \"LEGACY01\", \"DEPART\": 5, \"DUREE\": 9, \"PRIX\": 8 },\n" +
                     "    { \"VOL\": \"YAGNI17\", \"DEPART\": 5, \"DUREE\": 9, \"PRIX\": 7 }\n" +
                     "]",
                     "{\"gain\" : 18,\"path\" : [\"MONAD42\",\"LEGACY01\"]}"),
             new Combination("[\n" +
                     "  {\"VOL\": \"AF514\", \"DEPART\":0, \"DUREE\":5, \"PRIX\": 10}\n" +
                     "]",
                    "{\"gain\" : 10,\"path\" : [\"AF514\"]}")
     };
 
     @Theory
     public void should_compute_best_planing(Combination c) {
         assertThat( s.optimize(c.bookings).asJson(), equalTo(c.planing) );
     }
 
     private static class Combination {
         String bookings;
         String planing;
 
         private Combination(String bookings, String planing) {
             this.bookings = bookings;
             this.planing = planing;
         }
     }
 }
