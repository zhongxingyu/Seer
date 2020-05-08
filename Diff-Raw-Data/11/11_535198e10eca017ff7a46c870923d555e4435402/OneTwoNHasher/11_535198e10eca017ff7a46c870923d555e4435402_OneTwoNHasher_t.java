 package edu.berkeley.gamesman.hasher;
 
 import java.math.BigInteger;
 
 import edu.berkeley.gamesman.core.Game;
 import edu.berkeley.gamesman.core.TieredHasher;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 public class OneTwoNHasher extends TieredHasher<Integer> {
 	
 	private int gameWidth, gameHeight;
 	
 	@Override
 	public void setGame(Game<Integer> g, char[] p){		
 		super.setGame(g, p);
 		
 		gameWidth = g.getGameWidth();
 		gameHeight = g.getGameHeight();
 	}
 	
 	@Override
 	public BigInteger numHashesForTier(int tier) {
		return BigInteger.ONE;
 	}
 
 	@Override
 	public Integer gameStateForTierIndex(int tier, BigInteger index) {
		return tier + 1;
 		
 	}
 
 	@Override
 	public int numberOfTiers() {
		return gameHeight;
 	}
 	
 	@Override
 	public Pair<Integer, BigInteger> tierIndexForState(Integer state) {
 		return new Pair<Integer, BigInteger>(state / gameWidth, 
 				new BigInteger(String.valueOf(state % gameWidth)));
 	}
 
 	@Override
 	public String describe() {
 		return "OTNH";
 	}
 }
