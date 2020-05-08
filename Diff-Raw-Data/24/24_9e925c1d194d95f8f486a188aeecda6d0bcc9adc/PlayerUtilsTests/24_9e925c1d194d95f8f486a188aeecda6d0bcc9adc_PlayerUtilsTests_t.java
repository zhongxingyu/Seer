 package com.roketgamer.test;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import com.roketgamer.util.PlayerUtils;
 
 public class PlayerUtilsTests {
 
 	@Test
 	public void testUsernameToId() {
		assertEquals("Result", 1, PlayerUtils.toId("TexasGamer"));
		assertEquals("Result", -1, PlayerUtils.toId("notauser"));
 	}
 	
 	@Test
 	public void testIdToUsername() {
 		assertEquals("Result", "TexasGamer", PlayerUtils.toString(1));
		assertEquals("Result", "", PlayerUtils.toString(98765));
 	}
 }
