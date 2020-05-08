 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertFalse;
 
 public class TicketLotteryTest {
 
     @Test
     public void testOnePeopleOneWinner() {
         String input = "1 1 1 1";
 
         assertEquals("1", TicketLottery.getProbabilities(input));
     }
 
     @Test
     public void testTwoPeopleOneTicketOneWinner() {
        String input = "1 1 1 2";
 
         assertEquals("0", TicketLottery.getProbabilities(input));
     }
 
     @Test
     public void testSampleInput1() {
         String input = "100 10 2 1";
 
         assertEquals("0.1", TicketLottery.getProbabilities(input));
     }
 
     @Test
     public void testSampleInput2() {
         String input = "100 10 2 2";
 
         assertEquals("0.1909090909", TicketLottery.getProbabilities(input));
     }
 
     @Test
     public void testSampleInput3() {
         String input = "10 10 5 1";
 
         assertEquals("1", TicketLottery.getProbabilities(input));
     }
 }
