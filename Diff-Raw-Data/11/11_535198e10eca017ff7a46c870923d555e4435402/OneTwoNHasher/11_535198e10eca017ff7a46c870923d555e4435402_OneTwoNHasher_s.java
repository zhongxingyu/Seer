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
		if (tier == numberOfTiers() - 1)
			return new BigInteger(String.valueOf(
					gameHeight % gameWidth == 0 ? gameWidth : gameHeight % gameWidth));
		return new BigInteger(String.valueOf(gameWidth));
 	}
 
 	@Override
 	public Integer gameStateForTierIndex(int tier, BigInteger index) {
		return tier + index.intValue();
 		
 	}
 
 	@Override
 	public int numberOfTiers() {
		return gameHeight / gameWidth + (gameHeight % gameWidth == 0 ? 0 : 1);
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
