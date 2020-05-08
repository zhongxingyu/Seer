/* Copyright (c) 2012 Walker Crouse, http://windwaker.net/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
