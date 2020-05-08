 package asteroids;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class ShipTest {
 	
 	private static Ship ship;
 	private static Ship defaultShip;
 	private static Ship shipFarAway;
 	private Ship mutableShip;
 	private Ship mutableShip2;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		ship = new Ship(new Vector(10,5), new Vector(5,10), 15, Math.PI/2);
 		shipFarAway = new Ship(new Vector(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY), new Vector(0,0), 15, Math.PI/2);
 		setDefaultShip(new Ship()); //TODO: ?
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		mutableShip = new Ship();
		mutableShip2 = new Ship(new Vector(0,0), new Vector(1,1), 15, Math.PI/2);
 	}
 
 	@Test
 	public void testShipVectorVectorDoubleDouble() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testShip() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetPosition() {
 		assert(ship.getPosition().equals(new Vector(10,5)));
 	}
 
 	@Test
 	public void testIsValidPosition_LegalCase() {
 		assert(Ship.isValidPosition(ship.getPosition()));
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testIsValidPosition_IllegalCase() {
		Ship invalidShip = new Ship(null,null,5,150);
 		assertFalse(Ship.isValidPosition(invalidShip.getPosition()));
 	}
 	//TODO: klopt dit wel? kijk naar p.269-270 en ook testMove_IllegalCase() + meot checker wel getest worden?
 	
 	@Test
 	public void testSetPosition_LegalCase() {
 		mutableShip.setPosition(new Vector(15,10));
 		assert(mutableShip.getPosition().equals(new Vector(15,10)));
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testSetPosition_IllegalCase() {
		mutableShip.setPosition(null);
 		assert(mutableShip.getPosition().equals(new Vector(0,0)));
 	}
 
 	@Test
 	public void testGetVelocity() {
 		assert(ship.getVelocity().equals(new Vector(5,10)));
 	}
 
 	@Test
 	public void testSetVelocity_LegalCase() {
 		mutableShip.setVelocity(new Vector(25,20));
 		assert(mutableShip.getVelocity().equals(new Vector(25,20)));
 	}
 	
	@Test		//TODO nullpointer verwijderd; gaf een error omdat er geen exception was;
 	public void testSetVelocity_NullCase() {
		mutableShip.setVelocity(null);
 		assert(mutableShip.getVelocity().equals(new Vector(0,0)));
 	}
 	
 	@Test
 	public void testSetVelocity_IllegalCase() {
 		mutableShip.setVelocity(new Vector(300000,300000));
 		assert(mutableShip.getVelocity().equals(new Vector(0,0)));
 	}
 	
 	@Test
 	public void testCanHaveAsVelocity() {
 		
 	}
 
 	@Test
 	public void testSetSpeedLimit_LegalCase() {
 		mutableShip.setSpeedLimit(150);
 		assert(Util.fuzzyEquals(mutableShip.getSpeedLimit(),150));
 	}
 	
 	@Test
 	public void testSetSpeedLimit_IllegalCase() {
 		mutableShip.setSpeedLimit(150);
 		mutableShip.setSpeedLimit(400000);
 		assert(Util.fuzzyEquals(mutableShip.getSpeedLimit(),150));
 	}
 
 
 	@Test
 	public void testCanHaveAsSpeedLimit() {
 		
 	}
 
 	@Test
 	public void testGetDirection() {
 		assert(ship.getDirection()==Math.PI/2);
 	}
 
 	@Test
 	public void testSetDirection_LegalCase() {
 		mutableShip.setDirection(Math.PI);
 		assert(Util.fuzzyEquals(mutableShip.getDirection(),Math.PI));
 	}
 	
 	@Test
 	public void testSetDirection_IllegalCase() {
 		mutableShip.setDirection(Math.PI*3);
 		assert(Util.fuzzyEquals(mutableShip.getDirection(),Math.PI/2));
 	}
 
 	@Test
 	public void testIsValidDirection() {
 		
 	}
 
 	@Test
 	public void testSetMinRadius_LegalCase() {
 		Ship.setMinRadius(25);
 		assert(Util.fuzzyEquals(Ship.getMinRadius(),25));
 	}
 	
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testSetMinRadius_IllegalCase() {
 		Ship.setMinRadius(-25);
		assert(Util.fuzzyEquals(Ship.getMinRadius(),25));
 	}
 
 	@Test
 	public void testIsValidMinRadius() {
 		
 	}
 
 	@Test
 	public void testCanHaveAsRadius() {
 		
 	}
 
 	@Test
 	public void testGetRadius() {
 		assert(Util.fuzzyEquals(ship.getRadius(),15));
 	}
 	
 	// move
 	
 	@Test 
 	public void testMove_LegalCaseZero() {
 		Vector oldPosition = mutableShip2.getPosition();
 		mutableShip2.move(0);
 		assertEquals(mutableShip2.getPosition(),oldPosition);
 	}
 	
 	@Test 
 	public void testMove_LegalCaseOne() {
 		Vector oldPosition = mutableShip2.getPosition();
 		mutableShip2.move(1);
 		assert(mutableShip2.getPosition().equals(oldPosition.add(mutableShip2.getVelocity()).scale(1)));
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testMove_IllegalCase() {
 		mutableShip2.move(-1);
 	}
 	
 	// turn
 	
 	@Test
 	public void testTurn_LegalCase() {
 		mutableShip.turn(Math.PI);
 		assert(Util.fuzzyEquals(mutableShip.getDirection(),Math.PI ));
 	}
 	
 	@Test
 	public void testTurn_IllegalCase() {
 		mutableShip.turn(Math.PI*3);
 		//assert(Util.fuzzyEquals(mutableShip.getDirection(),0.0));
 		assert(Util.fuzzyEquals(mutableShip.getDirection(),Math.PI*3));		//TODO moet nog gefixt worden
 	}
 	
 	// getDistanceBetween
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testGetDistanceBetween_NullCase() {
 		defaultShip.getDistanceBetween(null);
 	}
 	
 	@Test
 	public void testGetDistanceBetween_ThisCase() {
 		assert(Util.fuzzyEquals(defaultShip.getDistanceBetween(defaultShip),0.0));
 	}
 	
 	@Test
 	public void testGetDistanceBetween_LegalCase() {
 		assert(Util.fuzzyEquals(defaultShip.getDistanceBetween(ship),Math.sqrt(10.0*10.0+5.0*5.0)-15.0-10.0));
 	}
 	
 	@Test
 	public void testGetDistanceBetween_OverflowCase() {
 		assert(Util.fuzzyEquals(defaultShip.getDistanceBetween(shipFarAway),0.0));
 	}
 	
 	// overlap
 	
 	@Test
 	public void testOverlap_ThisCase() {
 		assertEquals(true,defaultShip.overlap(defaultShip));
 	}
 	
 	// ...
 	
 	public static Ship getDefaultShip() {
 		return defaultShip;
 	}
 
 	public static void setDefaultShip(Ship defaultShip) {
 		ShipTest.defaultShip = defaultShip;
 	}
 
 }
