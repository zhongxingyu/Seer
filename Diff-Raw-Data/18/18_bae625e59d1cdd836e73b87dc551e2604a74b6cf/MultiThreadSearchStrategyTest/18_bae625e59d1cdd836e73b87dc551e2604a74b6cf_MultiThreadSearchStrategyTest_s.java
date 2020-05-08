 package ca.mcgill.music.ddmal.meisearch;
 
import static org.junit.Assert.*;

 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 public class MultiThreadSearchStrategyTest {
 
     @Test
     public void testSplitting() {
         List<String> ss = new ArrayList<String>();
         for (int i = 0; i < 69; i++) {
             ss.add(String.valueOf((i+1)));
         }
 
        MultiThreadSearchStrategy search = new MultiThreadSearchStrategy(ss, "foo", 4);

     }
 
 }
