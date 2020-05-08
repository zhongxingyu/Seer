 package ch.bfh.bti7301.w2013.battleship.tests;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 import ch.bfh.bti7301.w2013.battleship.game.Board.Coordinates;
 import ch.bfh.bti7301.w2013.battleship.game.Board.Direction;
 import ch.bfh.bti7301.w2013.battleship.game.Ship;
 import ch.bfh.bti7301.w2013.battleship.game.ships.AircraftCarrier;
 import ch.bfh.bti7301.w2013.battleship.game.ships.Battleship;
 import ch.bfh.bti7301.w2013.battleship.game.ships.Destroyer;
 import ch.bfh.bti7301.w2013.battleship.game.ships.PatrolBoat;
 import ch.bfh.bti7301.w2013.battleship.game.ships.Submarine;
 
 public class GenericShipTest {
 
 	@Test
 	public void testGenericShipCoordinatesCoordinatesIntSouth() {
 		// This should not throw an exception
 		Ship s = new AircraftCarrier(new Coordinates(3, 2), new Coordinates(3,
 				6));
 		assertEquals(s.getStartCoordinates().x, 3);
 		assertEquals(s.getStartCoordinates().y, 2);
 		assertEquals(s.getSize(), 5);
 	}
 
 	@Test
 	public void testGenericShipCoordinatesCoordinatesIntNorth() {
 		// This should not throw an exception
 		Ship s = new Submarine(new Coordinates(3, 4), new Coordinates(3, 2));
 		assertEquals(s.getStartCoordinates().x, 3);
 		assertEquals(s.getStartCoordinates().y, 4);
 		assertEquals(s.getSize(), 3);
 	}
 
 	@Test
 	public void testGenericShipCoordinatesCoordinatesIntEast() {
 		// This should not throw an exception
 		Ship s = new Battleship(new Coordinates(3, 3), new Coordinates(6, 3));
 		assertEquals(s.getDirection(), Direction.EAST);
 	}
 
 	@Test
 	public void testGenericShipCoordinatesCoordinatesIntWest() {
 		// This should not throw an exception
 		Ship s = new Battleship(new Coordinates(106, 103), new Coordinates(103,
 				103));
 		assertEquals(s.getDirection(), Direction.WEST);
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testGenericShipCoordinatesCoordinatesIntTooBig() {
 		// This should throw an exception (coordinates indicate size of 8)
 		Ship s = new PatrolBoat(new Coordinates(3, 3), new Coordinates(10, 3));
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testGenericShipCoordinatesCoordinatesIntTooSmall() {
 		// This should throw an exception
 		Ship s = new AircraftCarrier(new Coordinates(3, 3), new Coordinates(2,
 				3));
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testGenericShipCoordinatesCoordinatesIntDiagonal() {
 		// This should throw an exception
 		Ship s = new PatrolBoat(new Coordinates(3, 3), new Coordinates(5, 5));
 	}
 
 	@Test
 	public void testGenericShipCoordinatesDirectionInt() {
 		Ship a = new AircraftCarrier(new Coordinates(5, 5), Direction.EAST);
 
 		assertEquals(a.getDirection(), Direction.EAST);
 		assertEquals(a.getSize(), 5);
 		assertEquals(a.getStartCoordinates(), new Coordinates(5, 5));
 		assertEquals(a.getEndCoordinates(), new Coordinates(9, 5));
 	}
 
 	@Test
 	public void testGetStartCoordinates() {
 		Ship s = new Submarine(new Coordinates(3, 4), new Coordinates(3, 2));
 		assertEquals(s.getStartCoordinates().x, 3);
 		assertEquals(s.getStartCoordinates().y, 4);
 	}
 
 	@Test
 	public void testGetEndCoordinates() {
 		Ship s = new Submarine(new Coordinates(3, 4), new Coordinates(3, 2));
 		assertEquals(s.getEndCoordinates().x, 3);
 		assertEquals(s.getEndCoordinates().y, 2);
 	}
 
 	@Test
 	public void testGetSize() {
 		// An Aircraft Carrier should have a size of 5
 		Ship a = new AircraftCarrier(new Coordinates(5, 5), Direction.EAST);
 		assertEquals(a.getSize(), 5);
 
 		Ship b = new Battleship(new Coordinates(5, 5), Direction.WEST);
 		// A Battleship should have a size of 4
 		assertEquals(b.getSize(), 4);
 
 		Ship s = new Submarine(new Coordinates(5, 5), Direction.SOUTH);
 		// A Submarine should have a size of 3
 		assertEquals(s.getSize(), 3);
 
 		Ship d = new Destroyer(new Coordinates(5, 5), Direction.NORTH);
 		// A Destroyer should have a size of 3
 		assertEquals(d.getSize(), 3);
 
 		Ship p = new PatrolBoat(new Coordinates(5, 5), Direction.EAST);
 		// A Destroyer should have a size of 2
 		assertEquals(p.getSize(), 2);
 	}
 
 	@Test
 	public void testGetDirection() {
 		Ship n = new Destroyer(new Coordinates(5, 5), Direction.NORTH);
 		assertEquals(n.getDirection(), Direction.NORTH);
 
 		Ship e = new AircraftCarrier(new Coordinates(5, 5), Direction.EAST);
 		assertEquals(e.getDirection(), Direction.EAST);
 
 		Ship s = new PatrolBoat(new Coordinates(5, 5), Direction.SOUTH);
 		assertEquals(s.getDirection(), Direction.SOUTH);
 
 		Ship w = new Submarine(new Coordinates(5, 5), Direction.WEST);
 		assertEquals(w.getDirection(), Direction.WEST);
 	}
 
 	@Test
 	public void testGetCoordinatesForShip() {
 		Ship s = new AircraftCarrier(new Coordinates(2, 2), Direction.SOUTH);
 
 		// "Should" values
 		ArrayList<Coordinates> c = new ArrayList<Coordinates>();
 		c.add(new Coordinates(2, 2));
 		c.add(new Coordinates(2, 3));
 		c.add(new Coordinates(2, 4));
 		c.add(new Coordinates(2, 5));
 		c.add(new Coordinates(2, 6));
 
 		assertEquals(s.getSize(), s.getCoordinatesForShip().size());
 		assertEquals(s.getCoordinatesForShip(), c);
 
 	}
 
 	@Test
 	public void testGetCoordinatesForDamage() {
 		fail("Not yet implemented"); // TODO
 	}
 
 }
