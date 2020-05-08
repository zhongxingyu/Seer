 /**
  *     Copyright (C) 2011 Julien SMADJA <julien dot smadja at gmail dot com>
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *             http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  */
 
 package com.anzymus.neogeo.hiscores.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.anzymus.neogeo.hiscores.domain.Game;
 import com.anzymus.neogeo.hiscores.domain.Player;
 import com.anzymus.neogeo.hiscores.domain.Score;
 import com.anzymus.neogeo.hiscores.domain.Scores;
 
 public class ScoreServiceIT extends AbstractTest {
 
     private static Game game1, game2;
 
     private static Player player;
     private static String level;
 
     String pictureUrl = "http://www.imageshack.com";
 
     @BeforeClass
     public static void init() {
         game1 = gameService.store(new Game(RandomStringUtils.randomAlphabetic(10)));
         game2 = gameService.store(new Game(RandomStringUtils.randomAlphabetic(10)));
         player = playerService.store(new Player(RandomStringUtils.randomAlphabetic(5)));
         level = "MVS";
     }
 
     @Test
     public void should_add_hiscore() {
         Score score = new Score("100", player, level, game1, pictureUrl);
         scoreService.store(score);
 
         Scores scores = scoreService.findAllByGame(game1);
 
         assertTrue(scores.contains(score));
     }
 
     @Test
     public void should_not_add_same_score() {
         Scores scoresInitial = scoreService.findAll();
 
         Player newPlayer = playerService.store(new Player(RandomStringUtils.randomAlphabetic(5)));
 
         Score score = new Score("999", newPlayer, level, game1, pictureUrl);
         scoreService.store(score);
 
         score = new Score("999", newPlayer, level, game1, pictureUrl);
         scoreService.store(score);
 
         Scores scoresFinal = scoreService.findAll();
         assertEquals(scoresFinal.count(), scoresInitial.count() + 1);
     }
 
     @Test
     public void should_find_scores_by_player() {
         Player newPlayer = playerService.store(new Player(RandomStringUtils.randomAlphabetic(5)));
         int initialCount = scoreService.findAllByPlayer(newPlayer).count();
 
         Score score1 = new Score("123", newPlayer, "MVS", game1, pictureUrl);
         Score score2 = new Score("456", newPlayer, "Normal", game1, pictureUrl);
         Score score3 = new Score("1mn32", newPlayer, "Easy", game2, pictureUrl);
 
         scoreService.store(score1);
         scoreService.store(score2);
         scoreService.store(score3);
 
         Scores scores = scoreService.findAllByPlayer(newPlayer);
 
         assertEquals(initialCount + 3, scores.count());
 
         assertTrue(scores.contains(score1));
         assertTrue(scores.contains(score2));
         assertTrue(scores.contains(score3));
     }
 
     @Test
     public void should_find_last_scores_order_by_date_desc() {
         List<Score> scores = scoreService.findLastScoresOrderByDateDesc();
         assertNotNull(scores);
     }
 
     @Test
     public void should_count_scores_by_game() {
         long initialCount = scoreService.findCountByGame(game1);
 
         Score score1 = new Score("9999", player, "MVS", game1, pictureUrl);
         Score score2 = new Score("99991", player, "MVS", game1, pictureUrl);
         Score score3 = new Score("99992", player, "MVS", game1, pictureUrl);
 
         scoreService.store(score1);
         scoreService.store(score2);
         scoreService.store(score3);
 
         long count = scoreService.findCountByGame(game1);
 
         assertEquals(initialCount + 3, count);
     }
 
     @Ignore
     @Test
     public void should_find_players_order_by_num_scores() {
         Player player = createPlayer();
         Game game = createGame();
         for (int i = 0; i < 50; i++) {
             createScore(player, game);
         }
 
         List<Player> playersOrderByNumScores = scoreService.findPlayersOrderByNumScores();
         Player firstPlayer = playersOrderByNumScores.get(0);
 
         assertEquals(player, firstPlayer);
     }
 
     @Ignore
     @Test
     public void should_find_games_order_by_num_scores() {
         Player player = createPlayer();
         Game game = createGame();
         for (int i = 0; i < 50; i++) {
             createScore(player, game);
         }
 
        List<Game> gamesOrderByNumScores = scoreService.findGamesOrderByNumPlayers();
         Game firstGame = gamesOrderByNumScores.get(0);
 
         assertEquals(game, firstGame);
     }
 
     @Test
     public void should_get_number_of_played_games() {
         long initialPlayedGames = scoreService.getNumberOfPlayedGames();
 
         Player player = createPlayer();
         Game game = createGame();
         createScore(player, game);
 
         long actualPlayedGames = scoreService.getNumberOfPlayedGames();
 
         assertEquals(initialPlayedGames + 1, actualPlayedGames);
     }
 }
