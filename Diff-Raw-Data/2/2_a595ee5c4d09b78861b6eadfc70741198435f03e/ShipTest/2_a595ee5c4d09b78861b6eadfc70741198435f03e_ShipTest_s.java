 package asteroids.test;
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import asteroids.Ship;
 import asteroids.Util;
 
 public class ShipTest {
 	Ship ship;
 	Ship otherShip;
 	@Before
 	public void setUp() throws Exception {
 		ship =  new Ship(100, 200, 10, -10, 20, -Math.PI);
 		otherShip = new Ship(100, 100, 30, -15, 20, 0);
 	}
 
 	@SuppressWarnings("unused")
 	@Test (expected=IllegalArgumentException.class)
 	public void testConstructorInvalidRadiusSmall() {
 		Ship testship = new Ship(100, 200, 10, -10, 5, -Math.PI);
 	}
 
 	@SuppressWarnings("unused")
 	@Test (expected=IllegalArgumentException.class)
 	public void testConstructorInvalidRadiusNegative() {
 		Ship testship = new Ship(100, 200, 10, -10, -5, -Math.PI);
 	}
 
 	@Test
 	public void moveTest() {
 		Double time = 1.0;
 		ship.move(time);
 		assertEquals(110.0, ship.getX(), Util.EPSILON);
 		assertEquals(190.0, ship.getY(), Util.EPSILON);
 	}
 	@Test(expected=IllegalArgumentException.class)
 	public void moveTestNaN(){
 		Double time = Double.NaN;
 		ship.move(time);
 	}
 	@Test(expected=IllegalArgumentException.class)
 	public void moveTestNegativeTime() {
 		Double negTime = -1.0;
 		ship.move(negTime);
 	}
 	
 	@Test
 	public void moveTestZeroTime() {
 		Double zeroTime = 0.0;
 		ship.move(zeroTime);
 	}
 	
 	@Test
 	public void isValidTimeTest() {
 		double time = 3.0;
 		assertEquals(true, ship.isValidTime(time));
 	}
 	
 	@Test()
 	public void isValidTimeNaN(){
 		Double time = Double.NaN;
 		assertEquals(false, ship.isValidTime(time));
 	}
 	
 	@Test
 	public void isValidTimeBoundaryValues() {
 		double time = -5.0;
 		assertEquals(false, ship.isValidTime(time));
 		time = 0.0;
 		assertEquals(true, ship.isValidTime(time));
 	}
 	
 	
 	
 	@Test
 	public void turnTest(){
 		double angle = Math.PI;
 		ship.turn(angle);
 		assertEquals(0.0,ship.getAngle(),Util.EPSILON);
 		angle = 2*Math.PI;
 		ship.turn(angle);
 		assertEquals(2*Math.PI, ship.getAngle(), Util.EPSILON);
 	}
 				
 	@Test
 	public void thrustTestNormal(){
 		double amount = 1;
 		ship.thrust(amount);
 		assertEquals(9,ship.getXVelocity(),Util.EPSILON);
 		assertEquals(-10,ship.getYVelocity(),Util.EPSILON);
 		
 	}
 	
 	@Test
 	public void thrustTestSpeedOfLight() {
 		double amount = 500000.0;
 		ship.thrust(amount);
 		assertEquals(300000.0, ship.calcVelocity(ship.getXVelocity(), ship.getYVelocity()), Util.EPSILON);
 	}
 	
 	@Test
 	public void thrustTestNegativeAmount() {
 		double amount = -500;
 		ship.thrust(amount);
 		assertEquals(10.0, ship.getXVelocity(), Util.EPSILON);
 		assertEquals(-10.0, ship.getYVelocity(), Util.EPSILON);
 	}
 	@Test
 	public void thrustTestZero() {
 		double amount = 0.0;
 		ship.thrust(amount);
 		assertEquals(10.0, ship.getXVelocity(), Util.EPSILON);
 		assertEquals(-10.0, ship.getYVelocity(), Util.EPSILON);
 	}
 	@Test()
 	public void thrustTestNaN(){
 		double amount = Double.NaN;
 		ship.thrust(amount);
 		assertEquals(10.0, ship.getXVelocity(), Util.EPSILON);
 		assertEquals(-10.0, ship.getYVelocity(), Util.EPSILON);
 	}
 	@Test
 	public void testGetXSetX(){
 		ship.setX(5.0);
 		assertEquals(5.0,ship.getX(),Util.EPSILON);
 	}
 	@Test
 	public void testGetYSetY(){
 		ship.setY(2.0);
 		assertEquals(2.0,ship.getY(),Util.EPSILON);
 	}
 	
 	@Test
 	public void testGetXVelocitySetXVelocity(){
 		ship.setXVelocity(5.0);
 		assertEquals(5.0,ship.getXVelocity(),Util.EPSILON);
 	}
 	
 	@Test
 	public void testGetYVelocitySetYVelocity(){
 		ship.setYVelocity(2.0);
 		assertEquals(2.0,ship.getYVelocity(),Util.EPSILON);
 	}
 	
 	@Test
 	public void testGetAngleSetAngle(){
 		ship.setAngle(Math.PI);
 		assertEquals(Math.PI,ship.getAngle(),Util.EPSILON);
 	}
 	
 	@Test
 	public void testGetRadiusSetRadius(){
 		ship.setRadius(20.0);
 		assertEquals(20.0,ship.getRadius(),Util.EPSILON);
 	}
 	
 	@Test
 	public void testCalcVelocity() {
 		assertEquals(Math.sqrt(128.0), ship.calcVelocity(8.0, 8.0), Util.EPSILON);
 		assertEquals(Math.sqrt(128.0), ship.calcVelocity(8.0, -8.0), Util.EPSILON);
 		assertEquals(Math.sqrt(128.0), ship.calcVelocity(-8.0, 8.0), Util.EPSILON);
 		assertEquals(Math.sqrt(128.0), ship.calcVelocity(-8.0, -8.0), Util.EPSILON);
 	}
 	
 	@Test
 	public void testCalcVelocityNaN(){
 		double velocity = Double.NaN;
 		assertEquals(0.0, ship.calcVelocity(velocity,velocity), Util.EPSILON);		
 	}
 	
 	
 	@Test
 	public void testGetDistanceBetween(){
 		assertEquals(100.0,ship.getDistanceBetween(otherShip),Util.EPSILON);
 	}
 	
 	@Test (expected=IllegalArgumentException.class) 
 	public void testGetDistanceBetweenNullPointer() {
 		otherShip = null;
 		ship.getDistanceBetween(otherShip);
 	}
 	
 	@Test 
 	public void testGetDistanceBetweenNegativeX() {
 		ship =  new Ship(-100, 200, 10, -10, 20, -Math.PI);
 		otherShip = new Ship(-100, 100, 30, -15, 20, 0);
 		assertEquals(100.0,ship.getDistanceBetween(otherShip),Util.EPSILON);
 	}
 	
 	@Test 
 	public void testGetDistanceBetweenNegativeY() {
 		ship =  new Ship(100, -200, 10, -10, 20, -Math.PI);
 		otherShip = new Ship(100, -100, 30, -15, 20, 0);
 		assertEquals(100.0,ship.getDistanceBetween(otherShip),Util.EPSILON);
 	}
 	
 	@Test 
 	public void testGetDistanceBetweenNegativeXY() {
 		ship =  new Ship(-100, -200, 10, -10, 20, -Math.PI);
 		otherShip = new Ship(-100, -100, 30, -15, 20, 0);
 		assertEquals(100.0,ship.getDistanceBetween(otherShip),Util.EPSILON);
 	}
 	
 	@Test
 	public void testOverlapFalse(){
 		assertEquals(false,ship.overlap(otherShip));
 	}
 	
 	@Test
 	public void testOverlapTrue(){
 		Ship overlappingShip = new Ship(105, 190, 10, -10, 20, -Math.PI);
 		assertEquals(true,ship.overlap(overlappingShip));
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void testOverlapNullPointer() {
 		otherShip = null;
 		ship.overlap(otherShip);
 	}
 	
 	@Test
 	public void testSelfOverlap(){
 		assertEquals(true,ship.overlap(ship));
 	}
 	
 	@Test
 	public void testTimeToCollisionFalse(){
 		assertEquals(Double.POSITIVE_INFINITY , ship.getTimeToCollision(otherShip), Util.EPSILON);
 		
 	}
 	
 	@Test
 	public void testTimeToCollision(){
 		ship =  new Ship(100, 200, -10, 0, 20, -Math.PI);
 		Ship shipToCollide =  new Ship(0, 200, 10, 0, 20, -Math.PI);
 		assertEquals(3.0, ship.getTimeToCollision(shipToCollide),Util.EPSILON);
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void testTimeToCollisionNullPointer() {
 		otherShip = null;
 		ship.getTimeToCollision(otherShip);
 	}
 	
 	@Test
 	public void testGetCollisionPosition() {
 		ship =  new Ship(100, 200, -10, 0, 20, -Math.PI);
 		Ship shipToCollide =  new Ship(0, 200, 10, 0, 20, -Math.PI);
		assertEquals(70.0, ship.getCollisionPosition(shipToCollide)[0],Util.EPSILON);
 		assertEquals(200, ship.getCollisionPosition(shipToCollide)[1],Util.EPSILON);
 	}
 	
 	@Test
 	public void testGetCollisionPositionNoCollision() {
 		assertEquals(null, ship.getCollisionPosition(otherShip));
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void testGetCollisionPositionNullPointer() {
 		otherShip = null;
 		ship.getCollisionPosition(otherShip);
 	}
 }
