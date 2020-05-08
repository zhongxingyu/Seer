 package test.java;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 import main.java.Auction;
 
 public class BiddingTest {
     @Test
     public void CanBid() {
        Auction auction = new Auction("seller");
         Assert.assertTrue(auction.canBid());
     }
 }
