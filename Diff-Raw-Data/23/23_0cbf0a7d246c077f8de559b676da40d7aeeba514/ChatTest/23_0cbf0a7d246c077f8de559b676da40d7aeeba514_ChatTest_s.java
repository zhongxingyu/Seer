/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
  */
 package net.windwaker.chat;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 public class ChatTest {
 	/**
 	 * Test of tagSwap method, of class Chat.
 	 */
 	@Test
 	public void testTagSwap() {
 		Map<String, String> map = new HashMap<String, String>(3);
 		map.put("mother", "Melissa");
 		map.put("father", "Kevin");
 		map.put("brother", "Colin");
 		map.put("sister", "Caroline");
 		String msg = "My mother is %mother%, my father is %father%, my brother is %brother%, and my sister is %sister%";
 		String expected = "My mother is Melissa, my father is Kevin, my brother is Colin, and my sister is Caroline";
 		assertEquals(Chat.tagSwap(map, msg), expected);
 	}
 }
