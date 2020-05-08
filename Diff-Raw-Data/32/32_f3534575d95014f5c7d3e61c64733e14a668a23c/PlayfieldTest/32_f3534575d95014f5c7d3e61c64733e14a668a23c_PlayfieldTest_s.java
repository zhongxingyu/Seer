 package de.htwg.monopoly.controller.impl;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.htwg.monopoly.controller.impl.Playfield;
 import de.htwg.monopoly.entities.IFieldObject;
 import de.htwg.monopoly.entities.impl.Player;
 import de.htwg.monopoly.entities.impl.Street;
 import de.htwg.monopoly.util.IMonopolyFields;
 
 public class PlayfieldTest {
 
 	Playfield field;
 	Player testplayer;
 	IFieldObject testObject;
 
 	@Before
 	public void setUp() throws Exception {
 		field = new Playfield(25);
 		testplayer = new Player();
 	}
 
 	@Test
 	public void testMovePlayer() {
 		testplayer.setPosition(6);
 		field.movePlayer(testplayer, 1);
 		testplayer.setPosition(1);
 		field.movePlayer(testplayer, 1);
 		field.movePlayer(testplayer, 88);
 	}
 
 	@Test
 	public void testGetFieldSize() {
 		assertNotEquals(6, field.getfieldSize());
 	}
 
 	@Test
 	public void testGetCurrentField() {
 		assertEquals('l', field.getCurrentField(testplayer).getType());
 		field.getFieldNameAtIndex(1);
 
 	}
 
 	@Test
 	public void testNotMyStree2t() {
 
 		Player andererPlayer = new Player("hhh", "m", 2000);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 
 		field.movePlayer(testplayer, 1);
 		Street a = (Street) field.getCurrentField(testplayer);
 		a.setOwner(andererPlayer);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 	}
 
 	@Test
 	public void testNotMyStreet() {
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 	}
 
 	@Test
 	public void testInitialize() {
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 
 		field.movePlayer(testplayer, 1);
 		Street a = (Street) field.getCurrentField(testplayer);
 		a.setOwner(testplayer);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 		field.performActionAndAppendInfo(field.getCurrentField(testplayer), testplayer);
 		field.movePlayer(testplayer, 1);
 	}
 
 	@Test
 	public void testGetStack() {
 		assertEquals('e', field.getChanStack().getType());
 		assertEquals('g', field.getCommStack().getType());
 	}
 
 	@Test
 	public void testMovePlayerTo() {
		field.movePlayerTo(testplayer, "Bsys Labor");
 		assertTrue(testplayer.isInPrison());
 		testplayer.setInPrison(false);
 		testplayer.setPosition(5);
 		field.movePlayerTo(testplayer, IMonopolyFields.NAME[0]);
 		assertEquals(0, testplayer.getPosition());
 		field.movePlayerTo(testplayer, IMonopolyFields.NAME[1]);
 		assertEquals(1, testplayer.getPosition());
 
 		IFieldObject CurrentField = field.getFieldAtIndex(0);
 		assertEquals(0, CurrentField.getPosition());
 
 	}
 
 	@Test(expected = AssertionError.class)
 	public void secondTestMovePlayerTo() {
 		field = new Playfield(0);
 		field.movePlayerTo(testplayer, "go");
 	}
 
 }
