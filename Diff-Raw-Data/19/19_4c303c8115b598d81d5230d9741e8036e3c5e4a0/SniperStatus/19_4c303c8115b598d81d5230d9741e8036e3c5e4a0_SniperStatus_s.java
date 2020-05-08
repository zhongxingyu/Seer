 package auctionsniper;
 
 public enum SniperStatus {
 	JOINING {
 		@Override
 		public SniperStatus whenAuctionClosed() {
 			return LOST;
 		}
 	},
 	BIDDING {
 		@Override
 		public SniperStatus whenAuctionClosed() {
 			return LOST;
 		}
 	},
 	WINNING {
 		@Override
 		public SniperStatus whenAuctionClosed() {
 			return WON;
 		}
 	},
 	LOST, WON;
 
 	public SniperStatus whenAuctionClosed() {
		return null;
 	}
 }
