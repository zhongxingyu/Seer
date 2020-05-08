 package kei10in.test.endtoend.auctionsniper;
 
 import org.junit.After;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 
 @Category(Runtime.class)
 public class AuctionSniperEndToEndTest {
     private final FakeAuctionServer auction =
         new FakeAuctionServer("item-54321");
     private final FakeAuctionServer auction2 =
         new FakeAuctionServer("item-65432");
     private final ApplicationRunner application = new ApplicationRunner();
 
     @Test
     public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
         auction.startSellingItem();
         application.startBiddingIn(auction);
         auction.hasReceivedJoinRequestFromSniper(
             ApplicationRunner.SNIPER_XMPP_ID);
         auction.announceClosed();
         application.showsSniperHasLostAution(auction, 0, 0);
     }
     
     @Test
     public void sniperMakesAHigherBidButLoses() throws Exception {
         auction.startSellingItem();
         
         application.startBiddingIn(auction);
         auction.hasReceivedJoinRequestFromSniper(
             ApplicationRunner.SNIPER_XMPP_ID);
         
         auction.reportPrice(1000, 98, "other bidder");
         
         application.hasShownSniperIsBidding(auction, 1000, 1098);
         auction.hasRecievedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);
         
         auction.announceClosed();
         application.showsSniperHasLostAution(auction, 1000, 1098);
     }
     
     @Test
     public void sniperWinsAnAuctionByBiddingHigher() throws Exception {
         auction.startSellingItem();
         
         application.startBiddingIn(auction);
         auction.hasReceivedJoinRequestFromSniper(
             ApplicationRunner.SNIPER_XMPP_ID);
         
         auction.reportPrice(1000, 98, "other bidder");
         
         application.hasShownSniperIsBidding(auction, 1000, 1098);
         
         auction.hasRecievedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);
         
         auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID);
         application.hasShownSniperIsWinning(auction, 1098);
         
         auction.announceClosed();
         application.showsSniperHasWonAution(auction, 1098);
     }
     
     @Test
     public void sniperBidsForMultipleItems() throws Exception { 
         auction.startSellingItem();
         auction2.startSellingItem();
         
         application.startBiddingIn(auction, auction2);
         auction.hasReceivedJoinRequestFromSniper(
             ApplicationRunner.SNIPER_XMPP_ID);
         auction2.hasReceivedJoinRequestFromSniper(
             ApplicationRunner.SNIPER_XMPP_ID);
         
         auction.reportPrice(1000, 98, "other bidder");
         auction.hasRecievedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);
 
         auction2.reportPrice(500, 21, "other bidder");
         auction2.hasRecievedBid(521, ApplicationRunner.SNIPER_XMPP_ID);
         
         auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID);
         auction2.reportPrice(521, 22, ApplicationRunner.SNIPER_XMPP_ID);
         
         application.hasShownSniperIsWinning(auction, 1098);
         application.hasShownSniperIsWinning(auction2, 521);
         
         auction.announceClosed();
         auction2.announceClosed();
         
         application.showsSniperHasWonAution(auction, 1098);
         application.showsSniperHasWonAution(auction2, 521);
     }
     
     @Test
     public void sniperLosesAnAuctionWhenThePriceIsToHigh() throws Exception {
         auction.startSellingItem();
         application.startBiddingWithStopPrice(auction, 1100);
         auction.hasReceivedJoinRequestFromSniper(
             ApplicationRunner.SNIPER_XMPP_ID);
         auction.reportPrice(1000, 98, "other bidder");
         application.hasShownSniperIsBidding(auction, 1000, 1098);
         
         auction.hasRecievedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);
         
         auction.reportPrice(1197, 10, "third party");
         application.hasShownSniperIsLosing(auction, 1197, 1098);
 
         auction.reportPrice(1207, 10, "fourth party");
         application.hasShownSniperIsLosing(auction, 1207, 1098);
         
         auction.announceClosed();
         application.showsSniperHasLostAution(auction, 1207, 1098);
     }
     
     @Test
     public void sniperReportsInvalidAuctionMessageAndStopsRespondingToEvents()
         throws Exception {
         String brokenMessage = "a broken message";
         auction.startSellingItem();
         auction2.startSellingItem();
         
         application.startBiddingIn(auction, auction2);
         auction.hasReceivedJoinRequestFromSniper(
             ApplicationRunner.SNIPER_XMPP_ID);
         
         auction.reportPrice(500, 20, "other bidder");
         auction.hasRecievedBid(520, ApplicationRunner.SNIPER_XMPP_ID);
         
         auction.sendInvalidMessageContaining(brokenMessage);
         application.showsSniperHasFailed(auction);
         
         auction.reportPrice(520, 21, "other bidder");
         waitForAnotherAuctionEvent();
         
         application.reportsInvalidMessage(auction, brokenMessage);
         application.showsSniperHasFailed(auction);
         
     }
     
     private void waitForAnotherAuctionEvent() throws Exception {
         auction2.hasReceivedJoinRequestFromSniper(
             ApplicationRunner.SNIPER_XMPP_ID);
         auction2.reportPrice(600, 6, "other bidder");
         application.hasShownSniperIsBidding(auction2, 600, 606);
         
     }
 
     @BeforeClass
     public static void setupKeyboardLayout() {
         System.setProperty("com.objogate.wl.keyboard", "US");
     }
 
     @After
     public void stopAuction() {
         auction.stop();
        auction2.stop();
     }
 
     @After
     public void stopApplication() {
         application.stop();
     }
 
 }
