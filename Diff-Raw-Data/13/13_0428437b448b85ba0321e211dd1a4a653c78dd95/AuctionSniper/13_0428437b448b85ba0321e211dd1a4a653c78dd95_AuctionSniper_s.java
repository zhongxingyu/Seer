 package auctionsniper;
 
 public class AuctionSniper implements AuctionEventListener {
 	private final Auction auction;
 	private final SniperListener sniperListener;
 	
 	private SniperSnapshot snapshot;
 
 	public AuctionSniper(String itemId, Auction auction, SniperListener sniperListener) {
 		this.auction = auction;
 		this.sniperListener = sniperListener;
 		this.snapshot = SniperSnapshot.joining(itemId);
 	}
 
 	public void auctionClosed() {
		sniperListener.sniperStateChanged(snapshot.closed());
 	}
 
 	public void currentPrice(int currentPrice, int increment, PriceSource priceSource) {
 		switch(priceSource) {
 		case FromSniper:
			snapshot = snapshot.winning(currentPrice);
 			break;
 		case FromOtherBidder:
 			int bid = currentPrice + increment;
 			auction.bid(bid);
			snapshot = snapshot.bidding(currentPrice, bid);
 		}
 		sniperListener.sniperStateChanged(snapshot);
 	}
 }
