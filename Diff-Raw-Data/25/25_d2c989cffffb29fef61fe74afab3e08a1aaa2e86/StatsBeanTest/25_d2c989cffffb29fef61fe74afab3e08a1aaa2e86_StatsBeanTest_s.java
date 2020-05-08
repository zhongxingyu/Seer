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
 
 package com.anzymus.neogeo.hiscores.controller;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import com.anzymus.neogeo.hiscores.domain.Game;
 import com.anzymus.neogeo.hiscores.domain.Player;
 import com.anzymus.neogeo.hiscores.domain.Score;
 import com.anzymus.neogeo.hiscores.domain.Scores;
 import com.anzymus.neogeo.hiscores.domain.UnlockedTitle;
 import com.anzymus.neogeo.hiscores.service.GameService;
 import com.anzymus.neogeo.hiscores.service.PlayerService;
 import com.anzymus.neogeo.hiscores.service.ScoreService;
 import com.anzymus.neogeo.hiscores.service.TitleUnlockingService;
 import com.google.common.collect.Lists;
 
 public class StatsBeanTest {
 
 	@InjectMocks
 	StatsBean statsBean;
 
 	@Mock
 	PlayerService playerService;
 
 	@Mock
 	TitleUnlockingService titleUnlockingService;
 
 	@Mock
 	ScoreService scoreService;
 
 	@Mock
 	GameService gameService;
 
 	@Before
 	public void init() {
 		statsBean = new StatsBean();
 		MockitoAnnotations.initMocks(this);
 	}
 
 	@Test
 	public void should_return_number_of_players() {
 		when(playerService.getNumberOfPlayers()).thenReturn(5L);
 
 		long numberOfPlayers = statsBean.getNumberOfPlayers();
 
 		assertEquals(5, numberOfPlayers);
 	}
 
 	@Test
 	public void should_return_best_title_unlockers() {
 		Set<UnlockedTitle> unlockedTitles = new HashSet<UnlockedTitle>();
 		unlockedTitles.add(new UnlockedTitle(new Player("a"), null));
 		unlockedTitles.add(new UnlockedTitle(new Player("b"), null));
 		assertEquals(2, unlockedTitles.size());
 
 		List<Player> bestTitleUnlockers = new ArrayList<Player>();
 		Player p = new Player("player");
 		p.setUnlockedTitles(unlockedTitles);
 		bestTitleUnlockers.add(p);
 
 		when(titleUnlockingService.findPlayersOrderByNumUnlockedTitles()).thenReturn(bestTitleUnlockers);
 
 		Player player = statsBean.getBestTitleUnlockers().get(0);
 
 		assertEquals("player", player.getFullname());
 		assertEquals(2, player.getUnlockedTitles().size());
 	}
 
 	@Test
 	public void should_return_best_scorers() {
 		Player player1 = new Player("player");
 
 		List<Player> playersOrderByNumberScores = new ArrayList<Player>();
 		playersOrderByNumberScores.add(player1);
 
 		Scores scores = new Scores();
 		Game game1 = new Game("name1");
 		Game game2 = new Game("name2");
 
 		scores.add(new Score("123", player1, "MVS", game1, "http://"));
 		scores.add(new Score("456", player1, "MVS", game2, "http://"));
 
 		when(scoreService.findAllByPlayer(player1)).thenReturn(scores);
 		when(scoreService.findPlayersOrderByNumScores()).thenReturn(playersOrderByNumberScores);
 
 		List<Player> bestScorers = statsBean.getBestScorers();
 
 		Player player = bestScorers.get(0);
 
 		assertEquals("player", player.getFullname());
 		assertEquals(2, player.getContribution());
 	}
 
 	@Test
 	public void should_return_the_most_played_game() {
 		Game game1 = new Game("name1");
 		Game game2 = new Game("name2");
 
 		List<Game> games = Lists.newArrayList();
 		games.add(game1);
 		games.add(game2);
 
 		Scores scoresGame1 = new Scores();
 		Scores scoresGame2 = new Scores();
 
 		Player player1 = new Player("player1");
 		Player player2 = new Player("player2");
 
 		scoresGame1.add(new Score("123", player1, "MVS", game1, "http://"));
 		scoresGame1.add(new Score("123", player2, "MVS", game1, "http://"));
 		scoresGame2.add(new Score("456", player1, "MVS", game2, "http://"));
 
 		when(scoreService.findAllByGame(game1)).thenReturn(scoresGame1);
 		when(scoreService.findAllByGame(game2)).thenReturn(scoresGame2);
 
		when(scoreService.findGamesOrderByNumScores()).thenReturn(games);
 
 		Game mostPlayedGame = statsBean.getMostPlayedGames().get(0);
 
 		assertEquals(game1, mostPlayedGame);
 	}
 
 	@Test
 	public void should_return_number_of_played_games() {
 		when(scoreService.getNumberOfPlayedGames()).thenReturn(10L);
 
 		long numberOfPlayedGames = statsBean.getNumberOfPlayedGames();
 
 		assertEquals(10L, numberOfPlayedGames);
 	}
 
 	@Test
 	public void should_return_number_of_unplayed_games() {
 		when(scoreService.getNumberOfPlayedGames()).thenReturn(10L);
 		when(gameService.getNumberOfGames()).thenReturn(30L);
 
 		long numberOfUnplayedGames = statsBean.getNumberOfUnplayedGames();
 
 		assertEquals(20L, numberOfUnplayedGames);
 	}
 }
