 package test.java;
 
 import main.java.*;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Tests the functionality of the Auction class.
  *
  */
 public class AuctionTest {
 
     private Auction openAuctionFor(String seller) {
         Auction auction = new Auction(seller);
         try {
             auction.setDescription("Action Figure");
             auction.setQuantity(1);
             auction.open();
         }
         catch (Exception e) {}
 
         return auction;
     }
 
     /**
      * Tests that the user (seller) that created the auction
      * cannot bid on that auction.
      */
     @Test
     public void canSellerBidOnOwnAuction() {
         String seller = "sellerx";
         Auction auction = new Auction(seller);
         boolean result = auction.isValidBidder(seller);
 
         Assert.assertFalse(result);
     }
 
     /**
      * Tests that a user can bid on an auction.
      */
     @Test
     public void canUserBidOnAuction() {
         String seller = "sellerx";
         String bidder = "bidderx";
         Auction auction = new Auction(seller);
         boolean result = auction.isValidBidder(bidder);
 
         Assert.assertTrue(result);
     }
 
     @Test
     public void checkAuctionUpdateRestriction() {
         Auction auction = new Auction("sellerDude");
 
         Assert.assertFalse(auction.canModify("otherDude"));
     }
 
     @Test
     public void checkAuctionUpdatePermission() {
         Auction auction = new Auction("sellerDude");
 
         Assert.assertTrue(auction.canModify("sellerDude"));
     }
 
     @Test
     public void noBidUntilAuctionOpens() {
         Auction auction = new Auction("sellerx");
         Assert.assertFalse(auction.canBid());
     }
 
     @Test
     public void canBidWhenOpen() throws AuctionNotReadyException {
         Auction auction = openAuctionFor("seller");
 
         Assert.assertTrue(auction.canBid());
     }
 
     @Test
     public void noBidOnClosedAuction() {
         Auction auction = openAuctionFor("sellerx");
         auction.close();
 
         Assert.assertFalse(auction.canBid());
     }
 
     @Test
     public void AllowBidder() {
         //Given
        Auction auction = openAuctionFor("seller");
         String bidder = "Moneybags";
         int amount = 10;
 
         //When
         boolean result = auction.makeBid(bidder, amount);
 
         Assert.assertTrue(result);
     }
 
     @Test
     public void PreventBidder() {
         //Given
         String bidder = "seller";
        Auction auction = openAuctionFor(bidder);
         int amount = 10;
 
         //When
         boolean result = auction.makeBid(bidder, amount);
 
         Assert.assertFalse("Bidding on my own Auction", result);
     }
 
     @Test
     public void disallowInsufficientBidder() {
         //Given
        Auction auction = openAuctionFor("seller");
         int amount = 10;
         auction.setCurrentBidAmount(amount);
         String new_bidder = "new bidder";
 
         //When
         boolean result = auction.makeBid(new_bidder, amount);
 
         Assert.assertFalse("Bid was not greater than current", result);
     }
 
     @Test
     public void allowLargerBidder() {
         //Given
        Auction auction = openAuctionFor("seller");
         int amount = 10;
         auction.setCurrentBidAmount(amount);
         String new_bidder = "new bidder";
 
         //When
         boolean result = auction.makeBid(new_bidder, amount + 1);
 
         Assert.assertTrue("Bid should have been acceptable",
                           result);
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void conditionSetOnOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         String condition = "Awesome!";
         Auction auction = openAuctionFor(bidder);
         auction.setCondition(condition);
     }
 
     @Test
     public void conditionSetOnNotOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         String condition = "Awesome!";
         Auction auction = new Auction(bidder);
         auction.setCondition(condition);
         Assert.assertEquals(condition, auction.getCondition());
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void minBidSetOnOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         Auction auction = openAuctionFor(bidder);
         auction.setMinBid(100);
     }
 
     @Test
     public void minBidSetOnNotOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         int bid = 100;
         Auction auction = new Auction(bidder);
         auction.setMinBid(bid);
 
         Assert.assertEquals(bid, auction.getMinBid());
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void descriptionSetOnOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         Auction auction = openAuctionFor(bidder);
         auction.setDescription("My description");
     }
 
     @Test
     public void descriptionSetOnNotOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         String descr = "My description";
         Auction auction = new Auction(bidder);
         auction.setDescription(descr);
 
         Assert.assertEquals(descr, auction.getDescription());
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void quantitySetOnOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         Auction auction = openAuctionFor(bidder);
         auction.setQuantity(100);
     }
 
     @Test
     public void quantitySetOnNotOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         Auction auction = new Auction(bidder);
         auction.setQuantity(100);
         Assert.assertEquals(100, auction.getQuantity());
     }
 
     @Test
     public void cantOpenUnlessMinimumFields() {
         Auction auction = new Auction("seller");
 
         boolean exceptionCaught = false;
         try {
             auction.open();
         }
         catch (AuctionNotReadyException e) {
             exceptionCaught = true;
         }
 
         Assert.assertTrue(exceptionCaught);
     }
 
 }
