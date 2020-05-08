 package rky.gambles;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
 import rky.portfolio.gambles.Gamble;
 import rky.portfolio.gambles.Gambles;
 import rky.portfolio.gambles.Return;
 import rky.util.SetMap;
 
 public class GambleGenTest
 {
 	static final double AVERAGE_EXPECTED = 2.0;
 	
 	static final double HALF_PROBABILITY_OF_LINK = 0.4;
 	
 	@Test
 	public void generateGamblesTest()
 	{
 		final int numGambles = (int)(Math.random() * 20) + 190;
 		
 		ArrayList<Double> gambleExpectedReturns = new ArrayList<Double>(numGambles);
 		
 		double sum = 0;
 		for( int i = 0; i < numGambles; i++ )
 		{
 			double randomValue = Math.random();
 			gambleExpectedReturns.add( randomValue );
 			sum += randomValue;
 		}
 		
 		for( int i = 0; i < numGambles; i++ )
 		{
			gambleExpectedReturns.set( i, gambleExpectedReturns.get(i) * AVERAGE_EXPECTED / sum );
 		}
 		
 		ArrayList<Gamble> gambles = new ArrayList<Gamble>(numGambles);
 		
 		for( int i = 0; i < numGambles; i++ )
 		{
 			gambles.add( Gambles.randomGamble(gambleExpectedReturns.get(i)) );
 		}
 		
 		System.out.println("# gamble_id, class_id, h_ret, h_prob, m_ret, m_prob, l_ret, l_prob");
 		
 		for( int i = 0; i < numGambles; i++ )
 		{
 			Gamble g = gambles.get(i);
 			System.out.println(String.format(
					"%d, %d, %f, %f, %f, %f, %f",
 					i, (int)(Math.random() * 16),
 					g.getV(Return.high  ), g.getP(Return.high),
 					g.getV(Return.medium), g.getP(Return.medium),
 					g.getV(Return.low   ), g.getP(Return.low)
 					));
 		}
 		
 		Map<Gamble, Integer> gambleIds = new HashMap<Gamble, Integer>();
 		
 		for( int i = 0; i < numGambles; i++ )
 		{
 			gambleIds.put( gambles.get(i), i );
 		}
 		
 		SetMap<Integer, Integer> links = new SetMap<Integer, Integer>();
 		
 		for( Gamble g1 : gambles )
 		{
 			for( Gamble g2 : gambles )
 			{
 				if( g1 == g2 )   continue;
 				
 				if( Math.random() < HALF_PROBABILITY_OF_LINK )
 				{
 					links.put( gambleIds.get(g1), gambleIds.get(g2) );
 					links.put( gambleIds.get(g2), gambleIds.get(g1) );
 				}
 			}
 		}
 	}
 }
