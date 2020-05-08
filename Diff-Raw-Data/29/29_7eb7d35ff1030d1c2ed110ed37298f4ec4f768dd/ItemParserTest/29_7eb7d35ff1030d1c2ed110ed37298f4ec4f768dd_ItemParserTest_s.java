 package com.comphenix.xp.lookup;
 
 import static org.junit.Assert.*;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.junit.Test;
 
 public class ItemParserTest {
 
 	@Test
	public void testItems() {
 
 		ItemQuery universal = new ItemQuery();
 		ItemQuery stoneQuery = new ItemQuery(Material.STONE);
 		ItemQuery redWool = new ItemQuery(Material.WOOL, (int) DyeColor.RED.getData());
 		ItemQuery blueStuff = new ItemQuery((Material) null, (int) DyeColor.BLUE.getData());
	
 		
 	}

 }
