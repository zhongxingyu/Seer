 package org.automation.dojo;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import org.automation.dojo.web.controllers.ReleaseLogView;
 import org.automation.dojo.web.scenario.BasicScenario;
 import org.automation.dojo.web.scenario.Release;
 import org.fest.assertions.ListAssert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.mockito.stubbing.OngoingStubbing;
 
 import java.util.Date;
 import java.util.List;
 
 import static junit.framework.Assert.*;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Mockito.when;
 
 /**
  * @author serhiy.zelenin
  */
 @RunWith(MockitoJUnitRunner.class)
 public class GameLogServiceTest {
 
     private static final String CLIENT_NAME = "test";
     private GameLogService gameLogService;
     @Mock TimeService timeService;
     
     @Before
     public void setUp() throws Exception {
         gameLogService = new GameLogService(timeService);
         gameLogService.registerPlayer(CLIENT_NAME);
     }
 
     @Test
     public void shouldStoreGameLogsWhenOneScenario() {
         MockScenario scenario = scenario(1);
 
         createGameLog(scenario);
 
         List<GameLog> gameLogs = gameLogService.getGameLogs("127.0.0.1", scenario);
         assertEquals(1, gameLogs.size());
         assertAllGameLogsForScenario(gameLogs, scenario);
         Iterables.all(gameLogs, new EmptyPlayerLogs());
     }
 
     private MockScenario scenario(int scenarioId) {
         return new MockScenario(scenarioId, "scenario" + scenarioId, null);
     }
 
     @Test
     public void shouldStoreGameLogsWhenSeveralScenario() {
         MockScenario scenario1 = scenario(1);
 
         gameLogService.createGameLog(new Release(scenario1, new MockScenario(2, "scenarios", null)));
 
         List<GameLog> gameLogs = gameLogService.getGameLogs("127.0.0.1", scenario1);
 
         assertEquals(1, gameLogs.size());
         assertAllGameLogsForScenario(gameLogs, scenario1);
         Iterables.all(gameLogs, new EmptyPlayerLogs());
     }
 
     @Test
     public void shouldReturnPlayerLogs() {
         MockScenario scenario = scenario(1);
         gameLogService.registerPlayer("vasya");
         createGameLog(scenario).playerLog(scenario, CLIENT_NAME).playerLog(scenario, "vasya");
 
         List<GameLog> gameLogs = gameLogService.getGameLogs("vasya", scenario);
 
         assertFalse(Iterables.all(gameLogs, new EmptyPlayerLogs()));
         assertTrue(Iterables.all(gameLogs, new AllRecordsFor("vasya")));
     }
 
     @Test
     public void shouldReturnPlayerLogsForScenario() {
         MockScenario scenario1 = scenario(1);
         MockScenario scenario2 = scenario(2);
 
         createGameLog(new Release(scenario1, scenario2))
                 .playerLog(scenario1).playerLog(scenario2);
         
         List<GameLog> gameLogs = gameLogService.getGameLogs(CLIENT_NAME, scenario1);
 
         assertEquals(1, gameLogs.size());
         assertAllGameLogsForScenario(gameLogs, scenario1);
         assertTrue(Iterables.all(gameLogs, new AllRecordsForScenario(scenario1)));
     }
 
     @Test
     public void shouldStoreUniqueAddresses() {
         gameLogService.registerPlayer("vasya");
         gameLogService.registerPlayer(CLIENT_NAME);
         gameLogService.registerPlayer(CLIENT_NAME);
 
         assertThat(gameLogService.getRegisteredPlayers()).doesNotHaveDuplicates().contains(CLIENT_NAME, "vasya");
     } 
     
     @Test
     public void shouldReturnGameLogsForPlayer() {
         createGameLog(new Release(scenario(1), scenario(2)))
                 .playerLog(scenario(1)).playerLog(scenario(2));
 
         List<ReleaseLogView> releaseLogs = gameLogService.getLastReleaseLogsForPlayer(CLIENT_NAME, -1);
         ReleaseLogView releaseLog = releaseLogs.get(0);
 
         List<PlayerRecord> records = releaseLog.getRecords();
 
         assertTrue(Iterables.all(records, new RecordsForPlayer(CLIENT_NAME)));
     }
 
     @Test
     public void shouldReturnPredefinedLogSize() {
         MockScenario scenario = scenario(2);
         createGameLog(new Release(scenario(1), scenario))
                 .playerLog(scenario(1)).playerLog(scenario);
 
         List<ReleaseLogView> releaseLogs = gameLogService.getLastReleaseLogsForPlayer(CLIENT_NAME, 1);
 
         assertEquals(1, releaseLogs.size());
         assertReleaseViewLogForScenarios(releaseLogs.get(0), scenario);
     }
 
     @Test
     public void shouldReturnPredefinedLogSizeWithPreviousRelease() {
         MockScenario scenario1 = scenario(1);
         createGameLog(scenario1).playerLog(scenario1);
 
         MockScenario scenario2 = scenario(2);
         createGameLog(scenario2).playerLog(scenario2);
 
         List<ReleaseLogView> releaseLogs = gameLogService.getLastReleaseLogsForPlayer(CLIENT_NAME, 2);
 
         assertEquals(2, releaseLogs.size());
         assertReleaseViewLogForScenarios(releaseLogs.get(0), scenario1);
         assertReleaseViewLogForScenarios(releaseLogs.get(1), scenario2);
     }
 
     @Test
     public void shouldIgnoreNonRequestedRecords() {
         MockScenario scenario1 = scenario(1);
         createGameLog(scenario1).playerLog(scenario1);
 
         MockScenario scenario2 = scenario(2);
         MockScenario scenario3 = scenario(3);
         createGameLog(new Release(scenario1, scenario2, scenario3))
                 .playerLog(scenario1).playerLog(scenario2).playerLog(scenario3);
 
         List<ReleaseLogView> releaseLogs = gameLogService.getLastReleaseLogsForPlayer(CLIENT_NAME, 2);
 
         assertEquals(1, releaseLogs.size());
        ReleaseLogView releaseLog = releaseLogs.get(0);
        assertEquals(2, releaseLog.getReleaseNumber());
        assertReleaseViewLogForScenarios(releaseLog, scenario2, scenario3);
     }
 
     @Test
     public void shouldReturnAllRequestedReleaseViewRecords() {
         MockScenario scenario1 = scenario(1);
         createGameLog(scenario1).playerLog(scenario1);
 
         List<ReleaseLogView> releaseLogs = gameLogService.getLastReleaseLogsForPlayer(CLIENT_NAME, -1);
 
         assertEquals(1, releaseLogs.size());
         assertReleaseViewLogForScenarios(releaseLogs.get(0), scenario1);
     }
 
     private void assertReleaseViewLogForScenarios(ReleaseLogView releaseLog, MockScenario ... scenarios) {
         assertEquals("Release log should contain expected amount of records", scenarios.length, releaseLog.getRecords().size());
         for (int i = 0; i < scenarios.length; i++) {
             assertEquals(scenarios[i], releaseLog.getRecords().get(i).getScenario());
         }
     }
 
     private GameLogHelper createGameLog(Release release) {
         gameLogService.createGameLog(release);
         return new GameLogHelper(release);
     }
 
     private GameLogHelper createGameLog(MockScenario scenario) {
         gameLogService.createGameLog(new Release(scenario));
         return new GameLogHelper(scenario);
     }
 
     @Test
     public void testShouldRegister(){
         assertTrue(gameLogService.registerPlayer("vasya"));
     }
 
     @Test
     public void testShouldNotRegisterWhenNameAndAddressRegistered(){
         assertTrue(gameLogService.registerPlayer("vasya"));
         assertFalse(gameLogService.registerPlayer("vasya"));
     }
 
     @Test
     public void shouldSkipLogWhenPlayerUnregistered(){
         MockScenario scenario = new MockScenario(1, "", null);
         createGameLog(scenario);
         try {
             gameLogService.playerLog(record(scenario, "vasya"));
             fail();
         } catch (IllegalArgumentException e) {
         }
     } 
     
     @Test
     public void shouldGetSortedResultWhenGetBoardRecords() {
         gameLogService.registerPlayer("petya");
         MockScenario scenario = scenario(1);
         createGameLog(scenario).playerLog(CLIENT_NAME, 100).playerLog("petya", 50);
 
         List<BoardRecord> boardRecords = gameLogService.getBoardRecords();
 
         assertThat(boardRecords).onProperty("player").containsExactly(CLIENT_NAME, "petya");
     }
 
 
     @Test
     public void shouldGetSortedResultWhenGetBoardRecordsForSeveralReleases() {
         gameLogService.registerPlayer("petya");
         MockScenario scenario = scenario(1);
         createGameLog(scenario).playerLog(CLIENT_NAME, 100).playerLog("petya", 100 - 1);
         createGameLog(scenario).playerLog(CLIENT_NAME, 50).playerLog("petya", 50 - 1);
 
         List<BoardRecord> boardRecords = gameLogService.getBoardRecords();
 
         assertThat(boardRecords).onProperty("player").containsExactly(CLIENT_NAME, "petya");
         assertThat(boardRecords).onProperty("total").containsExactly(100+50, 100-1+50-1);
     }
 
     @Test
     public void shouldGetSortedResultWhenGetBoardForSamePlayerSeveralRecords() {
         MockScenario scenario = scenario(1);
         createGameLog(scenario).playerLog(CLIENT_NAME, 100).playerLog(CLIENT_NAME, 50);
 
         List<BoardRecord> boardRecords = gameLogService.getBoardRecords();
 
         assertThat(boardRecords).onProperty("player").containsExactly(CLIENT_NAME);
         assertThat(boardRecords).onProperty("total").containsExactly(100+50);
     }
 
     @Test
     public void shouldStoreReleaseTimeWhenGameLogAsked() {
         setCurrentTime(100);
         MockScenario scenario = scenario(1);
         gameLogService.createGameLog(new Release(scenario));
 
         assertEquals(100, gameLogService.getGameLogs(CLIENT_NAME, scenario).get(0).getReleaseDateMilis());
     }
 
     @Test
     public void shouldStoreCurrentTimeWhenPlayerReport() {
         setCurrentTime(123);
         MockScenario scenario = scenario(1);
         createGameLog(scenario).playerLog(CLIENT_NAME, 0);
 
         List<GameLog> gameLogs = gameLogService.getGameLogs(CLIENT_NAME, scenario);
         assertEquals(123, gameLogs.get(0).getPlayerRecords().get(0).getLogTime());
     }
 
     private OngoingStubbing<Date> setCurrentTime(int currentTimeMilis) {
         return when(timeService.now()).thenReturn(new Date(currentTimeMilis));
     }
 
     private PlayerRecord record(BasicScenario scenario, String playerName) {
         return record(scenario, playerName, 100);
     }
 
     private PlayerRecord record(BasicScenario scenario, String playerName, int score) {
         return new PlayerRecord(playerName, scenario, false, score, "", PlayerRecord.Type.LIAR);
     }
 
     private ListAssert assertAllGameLogsForScenario(List<GameLog> gameLogs, MockScenario scenario) {
         return assertThat(gameLogs).onProperty("scenario").contains(scenario);
     }
 
     private static class EmptyPlayerLogs implements Predicate<GameLog> {
         public boolean apply(GameLog gameLog) {
             return Iterables.isEmpty(gameLog.getPlayerRecords());
         }
     }
 
     private static class AllRecordsFor implements Predicate<GameLog> {
         private final String expectedPlayer;
 
         public AllRecordsFor(String expectedPlayer) {
             this.expectedPlayer = expectedPlayer;
         }
 
         public boolean apply(GameLog gameLog) {
             return Iterables.all(gameLog.getPlayerRecords(), new RecordsForPlayer(expectedPlayer));
         }
 
     }
 
     private static class RecordsForPlayer implements Predicate<PlayerRecord> {
         private String expectedPlayer;
 
         public RecordsForPlayer(String expectedPlayer) {
             this.expectedPlayer = expectedPlayer;
         }
 
         public boolean apply(PlayerRecord input) {
             return input.getPlayerName().equals(expectedPlayer);
         }
     }
 
     private class AllRecordsForScenario implements Predicate<GameLog> {
         public BasicScenario scenario;
 
         private AllRecordsForScenario(BasicScenario scenario) {
             this.scenario = scenario;
         }
 
         public boolean apply(GameLog gameLog) {
             return Iterables.all(gameLog.getPlayerRecords(), new Predicate<PlayerRecord>() {
 
                 public boolean apply(PlayerRecord record  ) {
                     return record.getScenario().equals(scenario);
                 }
             });
         }
     }
 
     private class GameLogHelper extends GameLogServiceTest {
         private BasicScenario scenario;
         private Release release;
 
         public GameLogHelper(BasicScenario scenario) {
             this.scenario = scenario;
         }
 
         public GameLogHelper(Release release) {
             this.release = release;
         }
 
         public GameLogHelper playerLog(String playerName, int score) {
             gameLogService.playerLog(record(scenario, playerName, score));
             return this;
         }
 
         public GameLogHelper playerLog(BasicScenario scenario) {
             gameLogService.playerLog(record(scenario, CLIENT_NAME));
             return this;
         }
 
         public GameLogHelper playerLog(BasicScenario scenario, String playerName) {
             gameLogService.playerLog(record(scenario, playerName));
             return this;
         }
     }
 }
