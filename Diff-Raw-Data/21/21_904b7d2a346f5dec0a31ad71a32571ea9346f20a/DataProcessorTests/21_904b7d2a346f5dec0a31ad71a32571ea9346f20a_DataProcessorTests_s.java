 package edu.brown.cs32.MFTG.tournament.data;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Objects;
 import java.util.Random;
 
 import org.junit.Test;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 import edu.brown.cs32.MFTG.monopoly.Game;
 import edu.brown.cs32.MFTG.monopoly.GameData;
 import edu.brown.cs32.MFTG.monopoly.Player;
 import edu.brown.cs32.MFTG.monopoly.PlayerWealthData;
 import edu.brown.cs32.MFTG.monopoly.PropertyData;
 import edu.brown.cs32.MFTG.monopoly.TimeStamp;
 
 
 public class DataProcessorTests {
 
 	@Test
 	public void generateConfirmationIndicesTest(){
 		Random rand = new Random();
 		List<Integer> confirmationIndices1 = DataProcessor.generateConfirmationIndices(100, .10,rand);
 		assertTrue(confirmationIndices1.size() == 10);
 		for(int i = 1; i < confirmationIndices1.size(); i++){
 			assertTrue(confirmationIndices1.get(i-1) < confirmationIndices1.get(i));
 		}
 		
 		List<Integer> confirmationIndices2 = DataProcessor.generateConfirmationIndices(10, .09,rand);
 		assertTrue(confirmationIndices2.size() == 1);
 		for(int i = 1; i < confirmationIndices2.size(); i++){
 			assertTrue(confirmationIndices2.get(i-1) < confirmationIndices2.get(i));
 		}
 	}
 	
 //	@Test
 	public void ConfirmationTest100Percent(){
 		Random rand = new Random();
 		int numGames1 = 5;
 		int numPlayers1 = 4;
 		List<Integer> confirmationIndices1 = DataProcessor.generateConfirmationIndices(numGames1, 1,rand);
 		List<List<Long>> seeds1 = DataProcessor.generateSeeds(numGames1, numPlayers1, confirmationIndices1,rand);
 		
 		long seed1;
 		long prevSeed = 0;
 		for(int i = 0; i < seeds1.size(); i++){
 			seed1 = seeds1.get(confirmationIndices1.get(i)).get(0);
 			for(int j = 0; j < numPlayers1; j++){
 				assertTrue(seeds1.get(confirmationIndices1.get(i)).get(j) == seed1);
 			}
 			assertTrue(prevSeed != seed1);
 			prevSeed = seed1;
 		}
 	}
 	
 	@Test
 	public void generateSeedsTest(){
 		Random rand = new Random();
 		int numGames1 = 1000;
 		int numPlayers1 = 4;
 		List<Integer> confirmationIndices1 = DataProcessor.generateConfirmationIndices(numGames1, .5,rand);
 		List<List<Long>> seeds1 = DataProcessor.generateSeeds(numGames1, numPlayers1, confirmationIndices1,rand);
 		long seed1;
 		for(int i = 0; i < confirmationIndices1.size(); i++){
 			seed1 = seeds1.get(0).get(confirmationIndices1.get(i));
 			for(int j = 0; j < numPlayers1; j++){
 				assertTrue(seeds1.get(j).get(confirmationIndices1.get(i)) == seed1);
 			}
 		}
 	}
 	
 //	@Test
 	public void generateGameDataReportSingleGame(){
 		Player p0=new Player(0,"");
 		Player p1=new Player(1,"");
 		
 		Game g = new Game(1,13,1000,500, false, false, p0, p1);
 		g.run();
 		
 		List<GameData> data = new ArrayList<>();
 		data.add(g.getGameData());
 		
 		System.out.println(DataProcessor.aggregate(data, 500).toGameDataReport().toString());
 	}
 	
//	@Test
 	public void combineAccumulatorsTest(){
 		Random rand = new Random();
 		Player p0=new Player(0,"");
 		Player p1=new Player(1,"");
 		Player p2=new Player(2,"");
 		Player p3=new Player(3,"");
 
 		List<GameData> data = new ArrayList<>();
 		List<GameData> data2 = new ArrayList<>();
 
 		Game g;
 		
 		for(int i = 0; i < 400; i++){ //play 400 games
 			g = new Game(1,rand.nextLong(),1000,500, false, false, p0, p1, p2, p3);
 			g.run();
 			data.add(g.getGameData());
 		}
 		
 		for(int i = 0; i < 400; i++){ //play 400 games
 			g = new Game(1,rand.nextLong(),1000,500, false, false, p0, p1, p2, p3);
 			g.run();
 			data2.add(g.getGameData());
 		}
 		
 		
 		GameDataAccumulator r = DataProcessor.aggregate(data, 50);
 		GameDataAccumulator r2 = DataProcessor.aggregate(data2, 50);
 		data.addAll(data2);
 		GameDataReport c = DataProcessor.aggregate(data, 50).toGameDataReport();
 		GameDataReport c2 = DataProcessor.combineAccumulators(r,r2).toGameDataReport();
 		System.out.println(c);
 		System.out.println(c2);
 		
 		assertTrue(c2.equals(c));
 	}
 	
 //	@Test
 	public void generateGameDataReportMany(){
 		Random rand = new Random();
 		Player p0=new Player(0,"");
 		Player p1=new Player(1,"");
 		
 		List<GameData> data = new ArrayList<>();
 		Game g;
 		
 		for(int i = 0; i < 400; i++){ //play 400 games
 			g = new Game(1,rand.nextLong(),1000,500, false, false, p0, p1);
 			g.run();
 			data.add(g.getGameData());
 		}
 //		data.get(0).printData();
 		System.out.println(DataProcessor.aggregate(data, 50).toGameDataReport().toString());
 	}
 	
	@Test
 	public void generateGameDataReportMany4Player(){
 		Random rand = new Random();
 		Player p0=new Player(0,"");
 		Player p1=new Player(1,"");
 		Player p2=new Player(2,"");
 		Player p3=new Player(3,"");
 
 		List<GameData> data = new ArrayList<>();
 		Game g;
 		
 		for(int i = 0; i < 500; i++){ //play 400 games
 			g = new Game(1,rand.nextLong(),1000,500, false, false, p0, p1, p2, p3);
 			g.run();
 			data.add(g.getGameData());
 		}
 		
 		System.out.println(DataProcessor.aggregate(data, 50).toGameDataReport().toString());
 	}
 	
 //	@Test
 	public void propertyConversionTest(){
 		PropertyDataReport prop = new PropertyDataReport("name", -1, 5, 10, 20, 30, 40, 50);
 		assertTrue(prop.toPropertyDataAccumulator().toPropertyDataReport().equals(prop));
 	}
 	
 //	@Test
 	public void wealthDataConversionTest(){
 		PlayerWealthDataReport w = new PlayerWealthDataReport(-1,200,300,50);
 		assertTrue(w.toPlayerWealthDataAccumulator().toPlayerWealthDataReport().equals(w));
 	}
 	
 //	@Test
 	public void timeStampConversionTest(){
 		Map<Integer,PlayerWealthDataReport> m = new HashMap<>();
 		for(int i = 0; i < 25; i++){
 			m.put(i, new PlayerWealthDataReport(i,Math.random()*200,Math.random()*300,(int)(Math.random()*50)));
 		}
 		TimeStampReport t = new TimeStampReport(5,m);
 		assertTrue(t.toTimeStampAccumulator().toTimeStampReport().equals(t));
 	}
 	
 //	@Test
 	public void gameDataConversionTests(){
 		
 		Random rand = new Random();
 		Player p0=new Player(0,"");
 		Player p1=new Player(1,"");
 		Player p2=new Player(2,"");
 		Player p3=new Player(3,"");
 
 		List<GameData> data = new ArrayList<>();
 		Game g;
 		
 		for(int i = 0; i < 5; i++){ //play 400 games
 			g = new Game(1,rand.nextLong(),1000,500, false, false, p0, p1, p2, p3);
 			g.run();
 			data.add(g.getGameData());
 		}
 		GameDataReport report = DataProcessor.aggregate(data, 1).toGameDataReport();	
 		assertTrue(report.toGameDataAccumulator().toGameDataReport().equals(report));
 	}
 	
 //	@Test
 	public void differentAggregationMethodsTest(){
 		
 		int size = 500;
 		Random rand = new Random();
 		Player p0=new Player(0,"");
 		Player p1=new Player(1,"");
 		Player p2=new Player(2,"");
 		Player p3=new Player(3,"");
 
 		List<GameData> data = new ArrayList<>();
 		GameDataAccumulator[] reports = new GameDataAccumulator[size];
 		Game g;
 		
 		List<GameData> tempList = new ArrayList<>();
 		for(int i = 0; i < size; i++){ //play 400 games
 			g = new Game(1,rand.nextLong(),1000,500, false, false, p0, p1, p2, p3);
 			g.run();
 			data.add(g.getGameData());
 			tempList.add(g.getGameData());
 			reports[i] = (DataProcessor.aggregate(tempList,50));
 			tempList.clear();
 		}
 //		GameDataReport report1 = DataProcessor.aggregate(data, 50).toGameDataReport();
 //		GameDataReport report2 = DataProcessor.aggregate(reports).toGameDataReport();
 //
 //		assertTrue(report1.equals(report2));	
 		
 	}
 	
 //	@Test
 	public void wealthAccumulationTest(){
 
 		int numGames = 1000;
 		int numTimestamps = 100;
 		int numPlayers = 4;
 		
 		List<List<List<Double>>> totCash = new ArrayList<>();
 		List<List<Double>> cash; //for each timestamp for all players
 		List<Double> cashAtT;
 		
 		List<List<List<Double>>> totWealth = new ArrayList<>();
 		List<List<Double>> wealth; //for each timestamp for all players
 		List<Double> wealthAtT;
 		
 		List<PlayerWealthData> tPWDList;
 		
 		ArrayList<TimeStamp> timeStamps;
 		TimeStamp tTS;
 		
 		List<GameData> gameData = new ArrayList<>();
 		GameData tGD;
 		for(int gameNum = 0; gameNum < numGames; gameNum ++){
 			cash = new ArrayList<>();
 			wealth = new ArrayList<>();
 			timeStamps = new ArrayList<>();
 			for(int i = 0; i < numTimestamps; i++){
 				cashAtT = new ArrayList<>();
 				wealthAtT = new ArrayList<>();
 				tPWDList = new ArrayList<>();
 				for(int j = 0; j < numPlayers; j++){
 					cashAtT.add(Math.random() * 200);
 					wealthAtT.add(Math.random() * 1000);
 	
 					tPWDList.add(new PlayerWealthData(j,cashAtT.get(j),wealthAtT.get(j))); //add the new player wealth data
 				}
 				wealth.add(wealthAtT);
 				cash.add(cashAtT);
 				tTS = new TimeStamp(i);
 				tTS.setWealthData(tPWDList);
 				timeStamps.add(tTS);
 			}
 			
 			tGD = new GameData(1,numPlayers);
 			tGD.setData(timeStamps);
 			gameData.add(tGD);
 			totCash.add(cash);
 			totWealth.add(wealth);
 		}
 		
 		GameDataReport calculated = DataProcessor.aggregate(gameData, numTimestamps).toGameDataReport();
 		
 		GameDataReport actual = createGDR(totCash,totWealth);
 		
 		assertTrue(calculated._timeStamps.size() == actual._timeStamps.size());
 		Map<Integer,PlayerWealthDataReport> c;
 		Map<Integer,PlayerWealthDataReport> a;
 		for(int i = 0; i < actual._timeStamps.size(); i++){
 			c = calculated._timeStamps.get(i).wealthData;
 			a = actual._timeStamps.get(i).wealthData;
 			
 			assertTrue(c.values().size() == a.values().size());
 			
 			PlayerWealthDataReport temp;
 			for(Entry<Integer, PlayerWealthDataReport> d : a.entrySet()){
 				temp = c.get(d.getKey());
 				assertTrue(temp != null);
 				assertTrue(temp.accCash == d.getValue().accCash);
 				assertTrue(temp.accTotalWealth == d.getValue().accTotalWealth);
 				assertTrue(temp.numDataPoints == d.getValue().numDataPoints);
 			}
 		}
 	}
 	
 	private GameDataReport createGDR(List<List<List<Double>>> cash, List<List<List<Double>>> wealth){ 
 		double cashSum;
 		double wealthSum;
 		PlayerWealthDataReport w;
 		Map<Integer,PlayerWealthDataReport> wAtT;
 		List<TimeStampReport> tStamps = new ArrayList<>();
 
 		for(int time = 0; time < cash.get(0).size(); time ++){
 			wAtT = new HashMap<>();
 			
 			for(int player = 0; player < cash.get(0).get(0).size(); player ++){
 				 cashSum = 0;
 				 wealthSum = 0;
 				
 				for(int gameNum = 0; gameNum < cash.size(); gameNum ++){
 					cashSum += cash.get(gameNum).get(time).get(player);
 					wealthSum += wealth.get(gameNum).get(time).get(player);
 				}
 
 				w = new PlayerWealthDataReport(-1, cashSum/cash.size(), wealthSum/cash.size(), cash.size());
 				wAtT.put(player,w);
 			}
 				
 			tStamps.add(new TimeStampReport(time, wAtT));
 		}
 		
 		return new GameDataReport(tStamps, null,null, null, null,false);		
 	}
 }
