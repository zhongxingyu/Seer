 /*
  * Copyright 2011 juliensmadja.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.anzymus.neogeo.hiscores.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 
 import com.anzymus.neogeo.hiscores.domain.Game;
 import com.anzymus.neogeo.hiscores.domain.Player;
 import com.anzymus.neogeo.hiscores.domain.Scores;
 import com.anzymus.neogeo.hiscores.service.GameService;
 import com.anzymus.neogeo.hiscores.service.PlayerService;
 import com.anzymus.neogeo.hiscores.service.ScoreService;
 import com.anzymus.neogeo.hiscores.service.TitleUnlockingService;
 import com.google.common.collect.Lists;
 
 @ManagedBean
 public class StatsBean {
 
     @EJB
     PlayerService playerService;
 
     @EJB
     TitleUnlockingService titleUnlockingService;
 
     @EJB
     ScoreService scoreService;
 
     @EJB
     GameService gameService;
 
     public long getNumberOfPlayers() {
         return playerService.getNumberOfPlayers();
     }
 
     public List<Player> getBestTitleUnlockers() {
         List<Player> bestTitleUnlockers = new ArrayList<Player>();
         List<Player> playersOrderByNumUnlockedTitles = titleUnlockingService.findPlayersOrderByNumUnlockedTitles();
         if (!playersOrderByNumUnlockedTitles.isEmpty()) {
             Player player = playersOrderByNumUnlockedTitles.get(0);
             int max = player.getUnlockedTitles().size();
             int currentNumUnlockedTitles = max;
             while (max == currentNumUnlockedTitles && !playersOrderByNumUnlockedTitles.isEmpty()) {
                 Player nextPlayer = playersOrderByNumUnlockedTitles.remove(0);
                 currentNumUnlockedTitles = nextPlayer.getUnlockedTitles().size();
                 if (currentNumUnlockedTitles == max) {
                     bestTitleUnlockers.add(nextPlayer);
                 }
             }
         }
         return bestTitleUnlockers;
     }
 
     public List<Player> getBestScorers() {
         List<Player> bestScorers = new ArrayList<Player>();
         List<Player> playersOrderByNumScores = scoreService.findPlayersOrderByNumScores();
         if (!playersOrderByNumScores.isEmpty()) {
             Player player = playersOrderByNumScores.get(0);
             Scores scores = scoreService.findAllByPlayer(player);
             int maxContributions = scores.count();
             int currentContributions = maxContributions;
             while (maxContributions == currentContributions && !playersOrderByNumScores.isEmpty()) {
                 player = playersOrderByNumScores.remove(0);
                 scores = scoreService.findAllByPlayer(player);
                 currentContributions = scores.count();
                 if (currentContributions == maxContributions) {
                     player.setContribution(maxContributions);
                     bestScorers.add(player);
                 }
             }
         }
         return bestScorers;
     }
 
     public List<Game> getMostPlayedGames() {
         List<Game> mostPlayedGames = Lists.newArrayList();
        List<Game> gamesOrderByNumScores = scoreService.findGamesOrderByNumScores();
         if (!gamesOrderByNumScores.isEmpty()) {
             Game game = gamesOrderByNumScores.get(0);
             Scores scores = scoreService.findAllByGame(game);
             int maxContributions = scores.count();
             int currentContributions = maxContributions;
             while (maxContributions == currentContributions && !gamesOrderByNumScores.isEmpty()) {
                 game = gamesOrderByNumScores.remove(0);
                 scores = scoreService.findAllByGame(game);
                 currentContributions = scores.count();
                 if (currentContributions == maxContributions) {
                     game.setContribution(maxContributions);
                     mostPlayedGames.add(game);
                 }
             }
         }
         return mostPlayedGames;
     }
 
     public long getNumberOfPlayedGames() {
         return scoreService.getNumberOfPlayedGames();
     }
 
     public long getNumberOfUnplayedGames() {
         return gameService.getNumberOfGames() - getNumberOfPlayedGames();
     }
 }
