 package com.comphenix.xp.lookup;
 
 import static org.junit.Assert.*;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.junit.Test;
 
 public class ItemParserTest {
 
 	@Test
	public void testItems() throws ParsingException {
 
 		ItemQuery universal = new ItemQuery();
 		ItemQuery stoneQuery = new ItemQuery(Material.STONE);
 		ItemQuery redWool = new ItemQuery(Material.WOOL, (int) DyeColor.RED.getData());
 		ItemQuery blueStuff = new ItemQuery((Material) null, (int) DyeColor.BLUE.getData());
 		
		ItemParser parser = new ItemParser();
		
		assertEquals(universal, parser.fromItemString("?"));
		assertEquals(stoneQuery, parser.fromItemString("stone"));
		assertEquals(stoneQuery, parser.fromItemString("1"));
		assertEquals(redWool, parser.fromItemString("wool|14"));
		assertEquals(redWool, parser.fromItemString("wool|red"));
		assertEquals(blueStuff, parser.fromItemString("?|blue"));
 	}
 }
