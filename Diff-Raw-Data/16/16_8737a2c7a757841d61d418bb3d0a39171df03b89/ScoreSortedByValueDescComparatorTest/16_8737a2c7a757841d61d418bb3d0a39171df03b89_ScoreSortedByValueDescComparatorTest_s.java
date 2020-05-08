 package com.anzymus.neogeo.hiscores.comparator;
 
 import com.anzymus.neogeo.hiscores.domain.Game;
 import com.anzymus.neogeo.hiscores.domain.Player;
 import com.anzymus.neogeo.hiscores.domain.Score;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class ScoreSortedByValueDescComparatorTest {
 
     ScoreSortedByValueDescComparator comparator = new ScoreSortedByValueDescComparator();
     Player player = new Player("player");
     String level = "MVS";
     Game game = new Game("Fatal Fury");
     String pictureUrl = "http://";
 
     @Test
     public void should_return_sorted_score_as_int() {
         Score score1 = new Score("1", player, level, game, pictureUrl);
         Score score2 = new Score("2", player, level, game, pictureUrl);
 
         int comparison = comparator.compare(score1, score2);
         assertEquals(1, comparison);
     }
     
     @Test
     public void should_return_sorted_score__for_neo_turf_masters() {
         Game gameNeoTurf = new Game("Neo Turf Masters / Big Tournament Golf");
         Score score1 = new Score("-2", player, level, gameNeoTurf, pictureUrl);
         Score score2 = new Score("-3", player, level, gameNeoTurf, pictureUrl);
 
         int comparison = comparator.compare(score1, score2);
         assertEquals(1, comparison);
     }
     
 }
