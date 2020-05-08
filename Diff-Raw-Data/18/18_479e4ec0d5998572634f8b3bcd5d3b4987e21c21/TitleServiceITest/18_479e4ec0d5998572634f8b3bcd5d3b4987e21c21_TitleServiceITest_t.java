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
 import org.apache.commons.lang.RandomStringUtils;
 import org.junit.Before;
 import org.junit.Test;
import static org.junit.Assert.*;
 import com.anzymus.neogeo.hiscores.domain.Game;
 import com.anzymus.neogeo.hiscores.domain.Player;
 import com.anzymus.neogeo.hiscores.domain.Score;
 
 public class TitleServiceITest extends AbstractTest {
 
     private Game game;
     private String pictureUrl;
     private Player player;
     private String level;
 
     @Before
     public void init() {
         game = new Game(RandomStringUtils.randomAlphabetic(5));
         pictureUrl = "http://";
         player = new Player(RandomStringUtils.randomAlphabetic(10));
         level = "MVS";
 
         gameService.store(game);
         playerService.store(player);
     }
 
     @Test
     public void should_get_score_count() {
         Score score1 = new Score("123456", player, level, game, pictureUrl);
         Score score2 = new Score("76535", player, level, game, pictureUrl);
         scoreService.store(score1);
         scoreService.store(score2);
 
         long scoreCount = titleService.getNumScoresByPlayer(player);
 
         assertEquals(2, scoreCount);
     }
 
     @Test
     public void should_get_0_as_score_count() {
         long scoreCount = titleService.getNumScoresByPlayer(player);
 
         assertEquals(0, scoreCount);
     }
    
    @Test
    public void should_find_score_in_game() {
        Score score = new Score("1234", player, "MVS", game, "http://");
        scoreService.store(score);
        
        boolean hasScoreInGame = titleService.hasScoreInGame(player, game.getName());
        assertTrue(hasScoreInGame);
    }
 
 }
