 package com.golddigger.services;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import java.io.PrintWriter;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.golddigger.model.Game;
 import com.golddigger.model.Player;
 import com.golddigger.model.Point2D;
 import com.golddigger.model.Tile;
 import com.golddigger.model.Unit;
 import com.golddigger.model.tiles.GoldTile;
 import com.golddigger.templates.CustomizableGameTemplate;
 
 public class CannonMockitoTest {
 	private static final String URL = "http://localhost/golddigger/digger/test1/";
 	private static final Point2D EAST = new Point2D(0,1);
 	PrintWriter writer = mock(PrintWriter.class);
 	CannonService service;
 	Player player1, player2;
 	Game game;
 	
 	@Before
 	public void before(){
 		service = new CannonService();
 		player1 = new Player("test1", "secret");
 		player2 = new Player("test2", "secret");
 		CustomizableGameTemplate template = new CustomizableGameTemplate();
 		template.setMap("wwwww\nwb.bw\nwwwww");
 		game = spy(template.build());
 		game.add(service);
 		game.add(player1);
 		game.add(player2);
 	}
 	
 	@Test
 	public void buyWithNoScore(){
 		buy();
 		
		verify(writer).println("FAILED: Dont have enough gold");
 	}
 	
 	@Test
 	public void buyWithScore(){
 		player1.setScore(2*CannonService.COST);
 		
 		buy();
 		buy();
 		
 		verify(writer).println("OK you have 1 rounds left");
 		verify(writer).println("OK you have 2 rounds left");	
 	}
 	
 	@Test
 	public void buyNotOnBase(){
 		player1.setScore(2*CannonService.COST);
 		game.getUnit(player1).setPosition(1,2);
 		
 		buy();
 		
 		verify(writer).println("FAILED: Not on your bank");
 	}
 	
 	@Test
 	public void buyOnOpponentsBase(){
 		player1.setScore(2*CannonService.COST);
 		game.getUnit(player1).setPosition(1,3);
 		
 		buy();
 		
 		verify(writer).println("FAILED: Not on your bank");
 	}
 	
 	@Test
 	public void shootHaventBroughtAmmo() {
 		shoot(EAST);
 		
 		verify(writer).println("FAILED: out of ammo");
 	}
 	
 	@Test
 	public void shootRunOutOfAmmo(){
 		player1.setScore(CannonService.COST);
 		
 		buy();
 		shoot(EAST);
 		shoot(EAST);
 		
 		verify(writer).println("OK you have 1 rounds left");
 		verify(writer).println("MISSED");
 		verify(writer).println("FAILED: out of ammo");
 	}
 	
 	@Test
 	public void shootMiss(){
 		player1.setScore(CannonService.COST);
 		
 		buy();
 		shoot(EAST);
 		
 		verify(writer).println("OK you have 1 rounds left");
 		verify(writer).println("MISSED");
 	}
 	
 	@Test
 	public void shootHitWithNoGold(){
 		Unit unit = game.getUnit(player2);
 		unit.setPosition(1,2);
 		
 		player1.setScore(CannonService.COST);
 		buy();
 		shoot(EAST);
 		
 		verify(writer).println("OK you have 1 rounds left");
 		verify(writer).println("HIT");
 		assertEquals(new Point2D(1,3), unit.getPosition());
 	}
 	
 	@Test
 	public void shootHitWithGold(){
 		Unit unit = game.getUnit(player2);
 		Tile tile = game.getMap().get(1,2);
 		unit.setPosition(1,2);
 		unit.setGold(3);
 		
 		player1.setScore(CannonService.COST);
 		buy();
 		shoot(EAST);
 		
 		verify(writer).println("OK you have 1 rounds left");
 		verify(writer).println("HIT");
 		assertEquals(new Point2D(1,3), unit.getPosition());
 		assertEquals(0, unit.getGold());
 		assertTrue(tile instanceof GoldTile);
 		assertEquals(3, ((GoldTile) tile).getGold());
 	}
 	
 	@Test
 	public void shootHitWithTooMuchGold(){
 		Unit unit = game.getUnit(player2);
 		GoldTile tile = (GoldTile) game.getMap().get(1,2);
 		tile.setGold(9);
 		unit.setPosition(1,2);
 		unit.setGold(3);
 		
 		player1.setScore(CannonService.COST);
 		buy();
 		shoot(EAST);
 		
 		verify(writer).println("OK you have 1 rounds left");
 		verify(writer).println("HIT");
 		assertEquals(new Point2D(1,3), unit.getPosition());
 		assertEquals(0, unit.getGold());
 		assertEquals(9, tile.getGold());
 	}
 	
 	@Test
 	public void shootHitWithTrimmedGold(){
 		Unit unit = game.getUnit(player2);
 		GoldTile tile = (GoldTile) game.getMap().get(1,2);
 		tile.setGold(7);
 		unit.setPosition(1,2);
 		unit.setGold(3);
 		
 		player1.setScore(CannonService.COST);
 		buy();
 		shoot(EAST);
 		
 		verify(writer).println("OK you have 1 rounds left");
 		verify(writer).println("HIT");
 		assertEquals(new Point2D(1,3), unit.getPosition());
 		assertEquals(0, unit.getGold());
 		assertEquals(9, tile.getGold());
 	}
 	
 	@Test
 	public void shootInvalidDirection(){
 		player1.setScore(CannonService.COST);
 		
 		buy();
 		doCommand("cannon/shoot/invalid/target");
 		
 		verify(writer).println("OK you have 1 rounds left");
 		verify(writer).println("FAILED: invalid target");
 	}
 	
 	@Test
 	public void shootOutOfRange(){
 		player1.setScore(CannonService.COST);
 		
 		buy();
 		shoot(new Point2D(0,3));
 		
 		verify(writer).println("OK you have 1 rounds left");
 		verify(writer).println("FAILED: out of range");
 	}
 	
 	@Test
 	public void invalidCommand(){
 		doCommand("cannon/invalid");
 		verify(writer, never()).println(anyString());
 	}
 	
 	public void shoot(Point2D target){
 		doCommand("cannon/shoot/"+target.lat+"/"+target.lng);
 	}
 	
 	public void buy(){
 		doCommand("cannon/buy");
 	}
 	
 	public void doCommand(String command){
 		String url = URL+command;
 		assertTrue(service.runnable(url));
 		assertTrue(service.execute(url, writer));
 	}
 
 }
