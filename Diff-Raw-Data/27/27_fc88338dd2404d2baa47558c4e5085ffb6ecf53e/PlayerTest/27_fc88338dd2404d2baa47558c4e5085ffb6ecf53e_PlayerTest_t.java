 package com.github.joukojo.testgame;
 

import org.junit.Assert;
 
 import org.junit.Test;
 
 public class PlayerTest {
 
 	@Test
 	public void testDirectionEast() {
 
 		final Player player = new Player();
 
 		final double direction = player.direction(1.0, 0.0);
 
		Assert.assertEquals("wrong direction", 90.0, direction,0.0);
 	}
 
 	@Test
 	public void testDirectionSouth() {
 
 		final Player player = new Player();
 
 		final double direction = player.direction(0.0, 1.0);
 
		Assert.assertEquals("wrong direction", 180.0, direction,0.0);
 	}
 	
 	@Test
 	public void testDirectionNorth() {
 
 		final Player player = new Player();
 
 		final double direction = player.direction(0.0, -1.0);
 
		Assert.assertEquals("wrong direction", 0.0, direction,0.0);
 	}
 	
 	@Test
 	public void testDirectionWest() {
 
 		final Player player = new Player();
 
 		final double direction = player.direction(-1.0, 0.0);
 
		Assert.assertEquals("wrong direction", 270.0, direction,0.0);
 	}
 	
 	@Test
 	public void testDirectionNorthEast() {
 		final Player player = new Player();
 
 		final double direction = player.direction(1.0, -1.0);
 
		Assert.assertEquals("wrong direction", 45.0, direction ,0.0 );
 		
 	}
 	
 	@Test
 	public void testDirectionSouthEast() {
 		final Player player = new Player();
 
 		final double direction = player.direction(1.0, 1.0);
 
		Assert.assertEquals("wrong direction", 135.0, direction,0.0);
 		
 	}
 	
 	@Test
 	public void testDirectionSouthWest() {
 		final Player player = new Player();
 
 		final double direction = player.direction(-1.0, 1.0);
 
		Assert.assertEquals("wrong direction", 225.0, direction,0.0);
 		
 	}
 	
 	@Test
 	public void testDirectionNorthWest() {
 		final Player player = new Player();
 
 		final double direction = player.direction(-1.0, -1.0);
 
		Assert.assertEquals("wrong direction", 315.0, direction,0.0);
 		
 	}		
 	
 
 }
