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
 
     @Test
     public void AllowBidder() {
         //Given
        Auction auction = new Auction();
         String bidder = "Moneybags";
         int amount = 10;
 
         //When
         boolean result = auction.makeBid(bidder, amount);
 
         Assert.assertTrue(result);
     }
 }
