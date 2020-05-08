 package com.sas.comp.models;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.sas.comp.hibernate.Hibernate;
 
 public class GameTest {
 
 	@Test
 	public void testGame() {
 		final Game game = Hibernate.getInstance().find(Game.class, 1);
 		Assert.assertNotNull(game);
 		Assert.assertTrue(game.getId() == 1);
 		Assert.assertNotNull(game.getNewDate());
 		Assert.assertTrue(game.getPlayoff() == 0);
 		Assert.assertTrue(game.getAwayScore() > 0);
 		Assert.assertTrue(game.getHomeScore() > 0);
 	}
 }
