 package speed;
 
 import java.util.Random;
 
 // The same as KatzmanAgent.java, only it directly draws valuation from Uniform[0, max_price] (to avoid discretization issues)
 public class KatzmanUniformAgent extends SeqAgent {
 	Random rng;
 
 	int agent_idx;
 	int no_goods_won;
 	
 	KatzHLValue v;
 	
 	public KatzmanUniformAgent(KatzHLValue v, int agent_idx) {
 		super(agent_idx, v);
 		this.agent_idx = agent_idx;
 		this.v = v;
 	}
 
 	// Calculate the first Bid
 	public double calculateFirstBid() {		
 //		double denominator = Math.pow(v.H/v.max_value, 2*v.N-1);
 //		double numerator = 0;	
 //		
 //		// We assume that lower bound of F is 0
 //		for (int i = 1; i*v.precision <= v.H; i++)
 //			numerator += Math.pow(i*v.precision/v.max_value, 2*v.N-1)*v.precision;
 //
 //		double bid = (v.H - numerator/denominator);
 //		// bid has to be non-negative
 //		if (bid < 0) {
 //			if (bid < -v.precision)
 //				System.out.println("Warning: agent " + agent_idx + ", bids " + bid + ", too negative");	// print warning
 //			bid = 0;
 //		}
 //		return bid;

//		System.out.println("v.H = " + v.H + ", bid = " + (1.0- (1.0/(2.0*v.N)) )*v.H);
		return (1.0- (1.0/(2.0*v.N)) )*v.H;		// I was dumb. We had a theoretical formula for calculating bids, why not use? 
 	}
 	
 	@Override
 	public void reset(SeqAuction auction) {
 		this.auction = auction;
 		no_goods_won = 0;
 	}
 	
 	@Override
 	public double getBid(int good_id) {
 		// Bid "truthfully" in the second round
 		if (good_id == 1) {
 			if (auction.winner[0] == agent_idx)
 				no_goods_won++;
 			
 			return (v.getValue(no_goods_won+1) - v.getValue(no_goods_won));
 		}
 		else
 			return calculateFirstBid();	// bid a specific amount in the first round (according to Katzman paper)
 	}
 	
 	// helpers. these may cheat.
 	public double getFirstRoundBid() {
 		// no goods won. good_id == 0. tmp_r[0] is a special global for good 0. 
 		return calculateFirstBid();
 	}
 	
 	public double getSecondRoundBid(int no_goods_won) {
 		// truthful bidding. realized prices don't matter.
 		return v.getValue(no_goods_won+1) - v.getValue(no_goods_won);
 	}
 }
 
