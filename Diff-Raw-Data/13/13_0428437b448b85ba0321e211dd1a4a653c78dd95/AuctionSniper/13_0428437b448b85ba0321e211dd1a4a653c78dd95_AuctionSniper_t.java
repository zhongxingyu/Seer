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
		notifyChange(snapshot.closed());
 	}
 
 	public void currentPrice(int currentPrice, int increment, PriceSource priceSource) {
 		switch(priceSource) {
 		case FromSniper:
			notifyChange(snapshot.winning(currentPrice));
 			break;
 		case FromOtherBidder:
 			int bid = currentPrice + increment;
 			auction.bid(bid);
			notifyChange(snapshot.bidding(currentPrice, bid));
 		}
	}

	private void notifyChange(SniperSnapshot snapshot) {
		this.snapshot = snapshot;
 		sniperListener.sniperStateChanged(snapshot);
 	}
 }
