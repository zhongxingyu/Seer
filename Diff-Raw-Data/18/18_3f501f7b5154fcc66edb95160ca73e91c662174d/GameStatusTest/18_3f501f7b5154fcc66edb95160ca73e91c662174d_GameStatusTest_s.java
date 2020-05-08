 package de.unipotsdam.nexplorer.client.android.rest.test;
 
 import org.springframework.http.client.SimpleClientHttpRequestFactory;
 import org.springframework.http.converter.json.GsonHttpMessageConverter;
 import org.springframework.web.client.RestTemplate;
 
 import android.test.AndroidTestCase;
 import de.unipotsdam.nexplorer.client.android.rest.GameStatus;
 
 public class GameStatusTest extends AndroidTestCase {
 
 	private static final String url = "http://routeme.dnsdynamic.com:8080/rest/mobile_test/get_game_status";
 	private static final int timeout = 4000;
 
 	private GameStatus status;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		SimpleClientHttpRequestFactory http = new SimpleClientHttpRequestFactory();
 		http.setConnectTimeout(timeout);
 		RestTemplate template = new RestTemplate(true, http);
 		template.getMessageConverters().add(new GsonHttpMessageConverter());
 		this.status = template.getForObject(url, GameStatus.class);
 	}
 
 	public void testHint() {
 		assertEquals("hint", status.getHint());
 	}
 
 	public void testBatterieLevel() {
 		assertEquals(80.0, status.getNode().getBatterieLevel());
 	}
 
 	public void testHasRangeBooster() {
 		assertEquals((Integer) 1, status.getNode().getHasRangeBooster());
 	}
 
 	public void testHasRangeBoosterBoolean() {
 		assertEquals((Boolean) true, status.getNode().hasRangeBoosterBoolean());
 	}
 
 	public void testItemInCollectionRange() {
 		assertEquals(1, status.getNode().getItemInCollectionRange());
 	}
 
 	public void testItemInCollectionRangeBoolean() {
 		assertEquals(true, status.getNode().isItemInCollectionRangeBoolean());
 	}
 
 	public void testNearbyItemsCount() {
 		assertEquals((Integer) 2, status.getNode().getNearbyItemsCount());
 	}
 
 	public void testNeighbourCount() {
 		assertEquals((Integer) 1, status.getNode().getNeighbourCount());
 	}
 
 	public void testNextItemDistance() {
 		assertEquals((Integer) 3, status.getNode().getNextItemDistance());
 	}
 
 	public void testRange() {
 		assertEquals((Integer) 10, status.getNode().getRange());
 	}
 
 	public void testScore() {
 		assertEquals((Integer) 180, status.getNode().getScore());
 	}
 
 	public void testNearbyItemsSize() {
 		assertEquals(2, status.getNode().getNearbyItems().getItems().size());
 	}
 
 	public void testFirstNearbyItemType() {
 		assertEquals("BATTERY", status.getNode().getNearbyItems().getItems().get(1).getItemType());
 	}
 
 	public void testFirstNearbyItemLatitude() {
 		assertEquals(52.14, status.getNode().getNearbyItems().getItems().get(1).getLatitude());
 	}
 
 	public void testFirstNearbyItemLongitude() {
 		assertEquals(13.77, status.getNode().getNearbyItems().getItems().get(1).getLongitude());
 	}
 
 	public void testSecondNearbyItemType() {
 		assertEquals("BOOSTER", status.getNode().getNearbyItems().getItems().get(2).getItemType());
 	}
 
 	public void testSecondNearbyItemLatitude() {
 		assertEquals(12.34, status.getNode().getNearbyItems().getItems().get(2).getLatitude());
 	}
 
 	public void testSecondNearbyItemLongitude() {
 		assertEquals(56.78, status.getNode().getNearbyItems().getItems().get(2).getLongitude());
 	}
 
 	public void testBaseNodeRange() {
 		assertEquals((Integer) 9, status.getStats().getBaseNodeRange());
 	}
 
 	public void testDidEnd() {
 		assertEquals((Integer) 0, status.getStats().getDidEnd());
 	}
 
 	public void testHasEndedBoolean() {
 		assertEquals((Boolean) false, status.getStats().hasEndedBoolean());
 	}
 
 	public void testGameDifficulty() {
 		assertEquals("2", status.getStats().getGameDifficulty());
 	}
 
 	public void testGameExists() {
 		assertEquals((Integer) 1, status.getStats().getGameExists());
 	}
 
 	public void testGameExistingBoolean() {
 		assertEquals((Boolean) true, status.getStats().isGameExistingBoolean());
 	}
 
 	public void testRemainingPlayingTime() {
 		assertEquals((Long) 1234567890l, status.getStats().getRemainingPlayingTime());
 	}
 
 	public void testIsPingActive() {
		assertTrue(status.getNode().isPingActive());
 	}
 
 	public void testIsNeighbourPingActive() {
 		assertTrue(status.getNode().getNeighbours().get(7).isPingActive());
 	}
 
 	public void testNeighbourPingDuration() {
 		assertEquals(1000, status.getNode().getNeighbours().get(7).getPingDuration());
 	}
 }
