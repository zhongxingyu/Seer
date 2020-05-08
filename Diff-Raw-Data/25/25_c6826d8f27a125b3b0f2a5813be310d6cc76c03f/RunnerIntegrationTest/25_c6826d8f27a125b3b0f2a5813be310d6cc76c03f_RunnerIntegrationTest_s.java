 package solubris.marketmon.domain;
 
 import static org.junit.Assert.*;
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.roo.addon.test.RooIntegrationTest;
 import org.springframework.test.context.ContextConfiguration;
 
 import flexjson.JSONDeserializer;
 import flexjson.transformer.DateTransformer;
 
 import solubris.marketmon.ITestBase;
 import solubris.marketmon.domain.types.BettingType;
 import solubris.marketmon.domain.types.MarketStatus;
 import solubris.marketmon.domain.types.RunnerStatus;
 import solubris.marketmon.json.ByMarket;
 import solubris.marketmon.requestor.HttpRequestor;
 import solubris.marketmon.requestor.Requestor;
 import solubris.marketmon.requestor.RequestorException;
 
 //needed to force ROO not to generate another annotation which overrides definition in super class
 @ContextConfiguration(locations = { "classpath:/META-INF/spring/applicationContext-test-override.xml" })
 @RooIntegrationTest(entity = Runner.class)
 public class RunnerIntegrationTest extends ITestBase {
     @Autowired
     private RunnerDataOnDemand dod;
 
 	@Test
 	public void createFromJson() throws IOException {
 		String json = readFileContents("/runner.json");
 		Runner runner = Runner.fromJsonToRunner(json);
 //		runner.persist();
 		
 		assertEquals(new Long(131151), runner.getSelectionId());
 		assertEquals(new Double(0.0), runner.getHandicap());
 		assertEquals("Steven Gerrard", runner.getDescription().getRunnerName());
 		assertEquals(RunnerStatus.ACTIVE, runner.getState().getStatus());
 		assertEquals(new Double(4122.161457838543), runner.getState().getTotalMatched());
 		assertEquals(new Short((short) 1), runner.getState().getSortPriority());
 		assertEquals(new Double(2.04), runner.getState().getLastPriceTraded());
 		assertEquals(14, runner.getExchange().getAvailableToBack().size());
 		assertEquals(6, runner.getExchange().getAvailableToLay().size());
 		PriceSize[] availableToBack = runner.getExchange().getAvailableToBack().toArray(new PriceSize[]{});
 		PriceSize[] availableToLay = runner.getExchange().getAvailableToLay().toArray(new PriceSize[]{});
 		int i=0;
 		assertEquals(new Double(13), availableToBack[i].getSize());
 		assertEquals(new Double(2.04), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(55.67), availableToBack[i].getSize());
 		assertEquals(new Double(2.02), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(35.86), availableToBack[i].getSize());
 		assertEquals(new Double(2.0), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(2.0), availableToBack[i].getSize());
 		assertEquals(new Double(1.97), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(20), availableToBack[i].getSize());
 		assertEquals(new Double(1.95), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(72), availableToBack[i].getSize());
 		assertEquals(new Double(1.94), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(40.08), availableToBack[i].getSize());
 		assertEquals(new Double(1.92), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(20), availableToBack[i].getSize());
 		assertEquals(new Double(1.81), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(0.13), availableToBack[i].getSize());
 		assertEquals(new Double(1.61), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(100), availableToBack[i].getSize());
 		assertEquals(new Double(1.5), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(50), availableToBack[i].getSize());
 		assertEquals(new Double(1.2), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(10), availableToBack[i].getSize());
 		assertEquals(new Double(1.06), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(444), availableToBack[i].getSize());
 		assertEquals(new Double(1.02), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(1004), availableToBack[i].getSize());
 		assertEquals(new Double(1.01), availableToBack[i].getPrice());
 		i=0;
 		assertEquals(new Double(8), availableToLay[i].getSize());
 		assertEquals(new Double(2.08), availableToLay[i].getPrice());
 		i++;
 		assertEquals(new Double(14.02), availableToLay[i].getSize());
 		assertEquals(new Double(2.1), availableToLay[i].getPrice());
 		i++;
 		assertEquals(new Double(9.2), availableToLay[i].getSize());
 		assertEquals(new Double(2.12), availableToLay[i].getPrice());
 		i++;
 		assertEquals(new Double(5), availableToLay[i].getSize());
 		assertEquals(new Double(2.2), availableToLay[i].getPrice());
 		i++;
 		assertEquals(new Double(10), availableToLay[i].getSize());
 		assertEquals(new Double(4.0), availableToLay[i].getPrice());
 		i++;
 		assertEquals(new Double(2.01), availableToLay[i].getSize());
 		assertEquals(new Double(1000), availableToLay[i].getPrice());
 		i++;
 
 	}
 
 	@Test
 	public void bymarket() throws IOException {
 		String json = readFileContents("/bymarket.json");
 		ByMarket bm = ByMarket.fromJsonToByMarket(json);
 		List<EventType> eventTypeList = bm.getEventTypes();
 		EventType eventType=eventTypeList.get(0);
 
 		Market market=eventType.getEventNodes().get(0).getMarketNodes().get(0);
 		Runner runner=market.getRunners().get(0);
 //		runner.persist();
 
 		/////////// assert market \\\\\\\\\
 		assertEquals("1.104802827", market.getMarketId());
 		/////////// assert market.state \\\\\\\\\
 		assertEquals(new Long(299007935), market.getState().getVersion());
 		assertEquals(MarketStatus.OPEN, market.getState().getStatus());
 		assertEquals(new Short((short) 1), market.getState().getNumberOfWinners());
 		assertEquals(new Short((short) 0), market.getState().getBetDelay());
 		assertEquals(false, market.getState().getRunnersVoidable());
 		assertEquals(false, market.getState().getBspReconciled());
 		assertEquals(false, market.getState().getComplete());
 		assertEquals(false, market.getState().getInplay());
 		assertEquals(new Short((short) 9), market.getState().getNumberOfRunners());
 		assertEquals(new Short((short) 9), market.getState().getNumberOfActiveRunners());
 		assertEquals(new Double(7670.151262311788D), market.getState().getTotalMatched());
 		assertEquals(new Double(8856.691170788157D), market.getState().getTotalAvailable());
 		assertEquals(false, market.getState().getCrossMatching());
 		/////////// assert market.description \\\\\\\\\
 		assertEquals(false, market.getDescription().getTurnInPlayEnabled());
 		assertEquals("England Captain", market.getDescription().getMarketName());
 //		assertEquals("2012-06-11T16:00:00.000Z", market.getDescription().getSuspendTime());
 		assertEquals(true, market.getDescription().getPersistenceEnabled());
 		assertEquals(false, market.getDescription().getBspMarket());
 //		assertEquals("2012-06-11T16:00:00.000Z", market.getDescription().getMarketTime());
 		assertEquals(BettingType.ODDS, market.getDescription().getBettingType());
 
 		/////////// assert runner \\\\\\\\\
 		assertEquals(new Long(131151), runner.getSelectionId());
 		assertEquals(new Double(0.0), runner.getHandicap());
 		assertEquals("Steven Gerrard", runner.getDescription().getRunnerName());
 		assertEquals(RunnerStatus.ACTIVE, runner.getState().getStatus());
 		assertEquals(new Double(4122.161457838543), runner.getState().getTotalMatched());
 		assertEquals(new Short((short) 1), runner.getState().getSortPriority());
 		assertEquals(new Double(2.04), runner.getState().getLastPriceTraded());
 		assertEquals(14, runner.getExchange().getAvailableToBack().size());
 		assertEquals(6, runner.getExchange().getAvailableToLay().size());
 		PriceSize[] availableToBack = runner.getExchange().getAvailableToBack().toArray(new PriceSize[]{});
 		PriceSize[] availableToLay = runner.getExchange().getAvailableToLay().toArray(new PriceSize[]{});
 		int i=0;
 		assertEquals(new Double(13), availableToBack[i].getSize());
 		assertEquals(new Double(2.04), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(55.67), availableToBack[i].getSize());
 		assertEquals(new Double(2.02), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(35.86), availableToBack[i].getSize());
 		assertEquals(new Double(2.0), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(2.0), availableToBack[i].getSize());
 		assertEquals(new Double(1.97), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(20), availableToBack[i].getSize());
 		assertEquals(new Double(1.95), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(72), availableToBack[i].getSize());
 		assertEquals(new Double(1.94), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(40.08), availableToBack[i].getSize());
 		assertEquals(new Double(1.92), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(20), availableToBack[i].getSize());
 		assertEquals(new Double(1.81), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(0.13), availableToBack[i].getSize());
 		assertEquals(new Double(1.61), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(100), availableToBack[i].getSize());
 		assertEquals(new Double(1.5), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(50), availableToBack[i].getSize());
 		assertEquals(new Double(1.2), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(10), availableToBack[i].getSize());
 		assertEquals(new Double(1.06), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(444), availableToBack[i].getSize());
 		assertEquals(new Double(1.02), availableToBack[i].getPrice());
 		i++;
 		assertEquals(new Double(1004), availableToBack[i].getSize());
 		assertEquals(new Double(1.01), availableToBack[i].getPrice());
 		i=0;
 		assertEquals(new Double(8), availableToLay[i].getSize());
 		assertEquals(new Double(2.08), availableToLay[i].getPrice());
 		i++;
 		assertEquals(new Double(14.02), availableToLay[i].getSize());
 		assertEquals(new Double(2.1), availableToLay[i].getPrice());
 		i++;
 		assertEquals(new Double(9.2), availableToLay[i].getSize());
 		assertEquals(new Double(2.12), availableToLay[i].getPrice());
 		i++;
 		assertEquals(new Double(5), availableToLay[i].getSize());
 		assertEquals(new Double(2.2), availableToLay[i].getPrice());
 		i++;
 		assertEquals(new Double(10), availableToLay[i].getSize());
 		assertEquals(new Double(4.0), availableToLay[i].getPrice());
 		i++;
 		assertEquals(new Double(2.01), availableToLay[i].getSize());
 		assertEquals(new Double(1000), availableToLay[i].getPrice());
 		i++;
 
 	}
 
 	@Test
 	public void bymarketRequest() throws IOException, RequestorException {
 //		String json = readFileContents("/bymarket.json");
 		Requestor requestor=new HttpRequestor();
		String url="http://uk-api.betfair.com/www/sports/exchange/readonly/v1.0/bymarket?alt=json&currencyCode=GBP&locale=en_GB&types=EVENT,RUNNER_EXCHANGE_PRICES_ALL,MARKET_STATE,MARKET_DESCRIPTION,RUNNER_DESCRIPTION,RUNNER_STATE&marketIds=1.104802827";
 		String json = requestor.get(url);
 
 		// .use("eventTypes.eventNodes.marketNodes.runners", ArrayList.class)
 //		Map m = new JSONDeserializer<Map>().use(null, HashMap.class).use("eventTypes.eventNodes.marketNodes.runners.values", Runner.class).deserialize(json);
 //		JSONDeserializer<Map> jd = new JSONDeserializer<Map>().use(null, HashMap.class).use("runners", ArrayList.class).use("values.runners.values", Runner.class);
 //		Map m = jd.deserialize(json);
 //		Map eventType=(Map)((List)m.get("eventTypes")).get(0);
 //		Map eventNode=(Map)((List)eventType.get("eventNodes")).get(0);
 //		Map marketNode=(Map)((List)eventNode.get("marketNodes")).get(0);
 //		Runner runner=(Runner)((List)m.get("runners")).get(0);
 //		JSONDeserializer<ByMarket> jd = new JSONDeserializer<ByMarket>().use(Date.class, new DateTransformer("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")).use(null, ByMarket.class); //.use("eventTypes", ArrayList.class).use("eventTypes.values", EventType.class);
 		ByMarket bm = ByMarket.fromJsonToByMarket(json);
 		List<EventType> eventTypeList = bm.getEventTypes();
 		EventType eventType=eventTypeList.get(0);
 
 //		Runner runner=eventType.getEventNodes().get(0).getMarketNodes().get(0).getRunners().get(0);
 //		runner.persist();
 		Market market=eventType.getEventNodes().get(0).getMarketNodes().get(0);
 //		market.persist();
 
 		/////////// assert market \\\\\\\\\
		assertEquals("1.104802827", market.getMarketId());
 	}
 
     @Test
     public void testPersist() {
         Assert.assertNotNull("Data on demand for 'Runner' failed to initialize correctly", dod.getRandomRunner());
         Runner obj = dod.getNewTransientRunner(Integer.MAX_VALUE);
         Assert.assertNotNull("Data on demand for 'Runner' failed to provide a new transient entity", obj);
         Assert.assertNotNull("Expected 'Runner' identifier to be not null", obj.getSelectionId());
         obj.persist();
         obj.flush();
         Assert.assertNotNull("Expected 'Runner' identifier to no longer be null", obj.getSelectionId());
     }
 
 	
 }
