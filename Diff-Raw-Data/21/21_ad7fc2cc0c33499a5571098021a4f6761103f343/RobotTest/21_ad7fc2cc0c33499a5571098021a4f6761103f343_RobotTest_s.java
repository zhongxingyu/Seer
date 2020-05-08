 package roborally.test;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 import roborally.model.Robot;
 
 
 public class RobotTest {

 	private Robot robot;
 	private Robot robot1;
 	private Robot robot2;
 	private Robot robot3;
 	private Robot robot1_0_0;
 	private Robot robot1_0_MAXVALUE;
 	private Robot robot1_MAXVALUE_0;
 	private Robot robot1_MAXVALUE_MAXVALUE;
 	private Robot robot2_0_0;
 	private Robot robot2_0_MAXVALUE;
 	private Robot robot2_MAXVALUE_0;
 	private Robot robot2_MAXVALUE_MAXVALUE;
 	private Robot robot7;
 	private Robot robot6;
 	private Robot robot5;
 	private Robot robot4;
 
 	@org.junit.Before
 	public void setUp() {
 		this.robot = new Robot(10,10,0,10000);
 		this.robot1 = new Robot(10,10,1,10000);
 		this.robot2 = new Robot(10,10,2,10000);
 		this.robot3 = new Robot(10,10,3,10000);
 		this.robot4= new Robot(10,10,0,10000);
 		this.robot5 = new Robot(10,10,1,10000);
 		this.robot6 = new Robot(10,10,2,10000);
 		this.robot7 = new Robot(10,10,3,10000);
 		this.robot1_0_0 = new Robot(0,0,0,12345);
 		this.robot2_0_0 = new Robot(0,0,3,9456);
 		this.robot1_0_MAXVALUE = new Robot(0,Long.MAX_VALUE,2,20000);
 		this.robot2_0_MAXVALUE = new Robot(0,Long.MAX_VALUE,2,20000);
 		this.robot1_MAXVALUE_0 = new Robot(Long.MAX_VALUE,0,1,20000);
 		this.robot2_MAXVALUE_0 = new Robot(Long.MAX_VALUE,0,0,20000);
 		this.robot1_MAXVALUE_MAXVALUE = new Robot(Long.MAX_VALUE,Long.MAX_VALUE,1,20000);
 		this.robot2_MAXVALUE_MAXVALUE = new Robot(Long.MAX_VALUE,Long.MAX_VALUE,2,20000);
 
 	}
 
 	@Test
 	public void testRobotConstructor() {
 		this.robot = new Robot(10,10,1,18000);
 		assertNotNull(robot);
 		assertEquals(10, robot.getX());
 		assertEquals(10, robot.getY());
 		assertEquals(1, robot.getDirection());
 		assertEquals(18000, robot.getEnergy(), 0);
 	}
 
 	@Test
 	public void turnClockWiseTest(){
 		this.robot = new Robot(1,1,0,18000);
 		robot.turnClockWise();
 		assertEquals(1,robot.getDirection());
 		robot.turnClockWise();
 		assertEquals(2,robot.getDirection());
 		robot.turnClockWise();
 		assertEquals(3,robot.getDirection());
 		robot.turnClockWise();
 		assertEquals(0,robot.getDirection());	
 	}
 
 	@Test
 	public void turnCounterClockWiseTest(){
 		this.robot = new Robot(1,1,0,18000);
 		robot.turnCounterClockWise();
 		assertEquals(3,robot.getDirection());
 		robot.turnCounterClockWise();
 		assertEquals(2,robot.getDirection());
 		robot.turnCounterClockWise();
 		assertEquals(1,robot.getDirection());
 		robot.turnCounterClockWise();
 		assertEquals(0,robot.getDirection());	
 	}
 
 	// kvraag mij af waarom die methode nuttig is ?l
 	@Test
 	public void testCalculateEnergyFraction2(){
 		assertEquals(18000./20000 , robot.calculateEnergyFraction(18000), 0);
 	}
 
 	@Test
 	public void testRobotMove_UP() {
 		this.robot = new Robot(12,12,0,18000);
 		this.robot.move();
 		assertEquals(12, robot.getX());
 		assertEquals(11, robot.getY());
 		assertEquals(0, robot.getDirection());
 		assertEquals(18000 - 500, robot.getEnergy(),0);
 	}
 
 	@Test
 	public void testRobotMove_RIGHT() {
 		this.robot = new Robot(12,12,1,18000);
 		this.robot.move();
 		assertEquals(13, robot.getX());
 		assertEquals(12, robot.getY());
 		assertEquals(1, robot.getDirection());
 		assertEquals(18000 - 500, robot.getEnergy(),0);
 	}
 
 	@Test
 	public void testRobotMove_DOWN() {
 		this.robot = new Robot(12,12,2,18000);
 		this.robot.move();
 		assertEquals(12, robot.getX());
 		assertEquals(13, robot.getY());
 		assertEquals(2, robot.getDirection());
 		assertEquals(18000 - 500, robot.getEnergy(),0);
 	}
 
 	@Test
 	public void testRobotMove_LEFT() {
 		this.robot = new Robot(12,12,3,18000);
 		this.robot.move();
 		assertEquals(11, robot.getX());
 		assertEquals(12, robot.getY());
 		assertEquals(3, robot.getDirection());
 		assertEquals(18000 - 500, robot.getEnergy(),0);
 	}
 
 	@Test
 	public void testIsValidDirection_TRUE(){
 		assertTrue(this.robot.isValidDirection(0));
 		assertTrue(this.robot.isValidDirection(1));
 		assertTrue(this.robot.isValidDirection(2));
 		assertTrue(this.robot.isValidDirection(3));
 	}
 
 	@Test
 	public void testIsValidDirection_FALSE(){
 		assertFalse(this.robot.isValidDirection(-1));
 		assertFalse(this.robot.isValidDirection(4));
 		assertFalse(this.robot.isValidDirection(12346));
 	}
 
 	@Test 
 	public void testIsValidPosition_TRUE(){
 		assertTrue(this.robot.isValidPosition(110,122));
 		assertTrue(this.robot.isValidPosition(0,0));
 		assertTrue(this.robot.isValidPosition(Long.MAX_VALUE, Long.MAX_VALUE));
 	}
 
 	@Test 
 	public void testIsValidPosition_FALSE(){
 		assertFalse(this.robot.isValidPosition(-1,-1));
 		assertFalse(this.robot.isValidPosition(-1,1));
 		assertFalse(this.robot.isValidPosition(1,-1));
 		assertFalse(this.robot.isValidPosition(-129292, -112121212));
 		assertFalse(this.robot.isValidPosition(Long.MAX_VALUE + 1, Long.MAX_VALUE + 1));
 		assertFalse(this.robot.isValidPosition(Long.MAX_VALUE + 1, 1));
 		assertFalse(this.robot.isValidPosition(4, Long.MAX_VALUE + 1));
 		assertFalse(this.robot.isValidPosition(-4, Long.MAX_VALUE + 1));
 		assertFalse(this.robot.isValidPosition(Long.MAX_VALUE + 1, -4));
 
 		}
 
 	@Test 
 	public void testIsValidEnergy_TRUE(){
 		assertTrue(this.robot.isValidEnergy(0));
 		assertTrue(this.robot.isValidEnergy(19999));
 		assertTrue(this.robot.isValidEnergy(20000));		
 	}
 
 	@Test 
 	public void testIsValidEnergy_FALSE(){
 		assertFalse(this.robot.isValidEnergy(-1));
 		assertFalse(this.robot.isValidEnergy(20001));		
 	}
 
 	@Test 
 	public void testCalculateEnergyFraction1(){
 		this.robot = new Robot(12,12,0,4000);
 		assertEquals( 4000./20000 ,this.robot.calculateEnergyFraction(),0);
 	}
 
 	@Test
 	public void testCalculateMinimalEnergy(){
 		this.robot = new Robot(12,12, 0, 12000);
 		this.robot2 = new Robot(12,20,0,12000);
 		double result = robot2.getEnergyRequiredToReach(robot.getX(),robot.getY());
 		assertEquals(8*500, result , 0);
 
 		this.robot = new Robot(12,12, 0, 12000);
 		this.robot2 = new Robot(12,20,0,12000);
 		result = robot.getEnergyRequiredToReach(robot2.getX(),robot2.getY());
 		assertEquals(8*500 + 200, result , 0);
 
 	}
 
 	@Test 
 	public void testRecharge(){
 		this.robot = new Robot( 0, 0,0, 10000);
 		this.robot.recharge(5000);
 		assertEquals(15000, this.robot.getEnergy(),0);
 		this.robot.recharge(10000);
 		assertEquals(20000, this.robot.getEnergy(),0);
		/*this.robot.recharge(-500);
		assertEquals(20000,this.robot.getEnergy(),0);*/
 	}
 
 
 	@Test
 	public void testGetEnergyRequiredToReach(){
 		this.robot = new Robot(10,10,0,20000);
 		assertEquals( 0, this.robot.getEnergyRequiredToReach(10, 10) , 0);
 		assertEquals( 10*500 + 200, this.robot.getEnergyRequiredToReach(10, 20),0);
 		assertEquals( 20*500 + 200, this.robot.getEnergyRequiredToReach(20, 20),0);
 	}
 
 
 	@Test
 	public void testMoveNextTo(){
 		this.robot1 = new Robot(20,20,2,20000);
 		robot.moveNextTo(robot1);
 		assertTrue(isNextTo(this.robot,this.robot1));
 		this.robot2.moveNextTo(robot1);
 		assertTrue(isNextTo(this.robot2,this.robot1));
 		this.robot = new Robot(25,44,3,5000);
 		this.robot1 = new Robot(10,44,0,12000);
 		this.robot.moveNextTo(robot1);
 		assertTrue(isNextTo(this.robot,this.robot1));
 
 		this.robot = new Robot(25,44,3,3000);
 		this.robot1 = new Robot(10,44,0,2000);
 		this.robot.moveNextTo(robot1);
 		assertFalse(isNextTo(this.robot,this.robot1));
 
 		this.robot2 = new Robot(10,10,0,10000);
 		this.robot3 = new Robot (10,10,3,10000);
 		robot2.moveNextTo(robot3);
 		assertTrue(isNextTo(this.robot2,this.robot3));
 
 		this.robot = new Robot(0,2,0,1500);
 		this.robot1 = new Robot (6,2,0,19700);
 		this.robot2 = new Robot(0,0,3,600);
 		this.robot3 = new Robot( 2,0,1,2000);
 		robot.moveNextTo(robot1);
 		robot2.moveNextTo(robot3);
 
 		this.robot = new Robot(0,0,1,20000);
 		this.robot1 = new Robot(5,5,0,20000);
 		robot.moveNextTo(robot1);
 		assertEquals(0,robot.getY());
 		assertEquals(1,robot1.getY());
 		assertEquals(5,robot.getX());
 
 		//energietests
 		this.robot = new Robot(6, 6, 0, 20000);
 		this.robot1 = new Robot(6, 12, 0, 1000);
 		robot.moveNextTo(robot1);
 		assertEquals(6, robot.getX());
 		assertEquals(11, robot.getY());
 
 		//eerste test op de hoekpunten
 		this.robot1_0_0.moveNextTo(robot2_0_0);
 		assertEquals(1,this.robot1_0_0.getX());
 		assertEquals(0,this.robot1_0_0.getY());
 		this.robot1_0_MAXVALUE.moveNextTo(robot2_0_MAXVALUE);
 		assertEquals(1,this.robot1_0_MAXVALUE.getX());
 		assertEquals(Long.MAX_VALUE,this.robot1_0_MAXVALUE.getY());
 		this.robot1_MAXVALUE_0.moveNextTo(robot2_MAXVALUE_0);
 		assertEquals(Long.MAX_VALUE,this.robot1_MAXVALUE_0.getX());
 		assertEquals(1,this.robot1_MAXVALUE_0.getY());
 		this.robot1_MAXVALUE_MAXVALUE.moveNextTo(this.robot2_MAXVALUE_MAXVALUE);
 		assertEquals(Long.MAX_VALUE, robot1_MAXVALUE_MAXVALUE.getX());
 		assertEquals(Long.MAX_VALUE - 1,robot1_MAXVALUE_MAXVALUE.getY());
 
 		//hoekpunten terug resetten
 		this.robot1_0_0 = new Robot(0,0,0,100);
 		this.robot2_0_0 = new Robot(0,0,3,9456);
 		this.robot1_0_MAXVALUE = new Robot(0,Long.MAX_VALUE,2,100);
 		this.robot2_0_MAXVALUE = new Robot(0,Long.MAX_VALUE,2,20000);
 		this.robot1_MAXVALUE_0 = new Robot(Long.MAX_VALUE,0,1,100);
 		this.robot2_MAXVALUE_0 = new Robot(Long.MAX_VALUE,0,0,20000);
 		this.robot1_MAXVALUE_MAXVALUE = new Robot(Long.MAX_VALUE,Long.MAX_VALUE,1,100);
 		this.robot2_MAXVALUE_MAXVALUE = new Robot(Long.MAX_VALUE,Long.MAX_VALUE,2,20000);
 
 		//tweede test op de hoekpunten
 		this.robot1_0_0.moveNextTo(robot2_0_0);
 		assertEquals(0,this.robot2_0_0.getX());
 		assertEquals(1,this.robot2_0_0.getY());
 		this.robot1_0_MAXVALUE.moveNextTo(robot2_0_MAXVALUE);
 		assertEquals(1,this.robot2_0_MAXVALUE.getX());
 		assertEquals(Long.MAX_VALUE,this.robot2_0_MAXVALUE.getY());
 		this.robot1_MAXVALUE_0.moveNextTo(robot2_MAXVALUE_0);
 		assertEquals(Long.MAX_VALUE - 1,this.robot2_MAXVALUE_0.getX());
 		assertEquals(0,this.robot2_MAXVALUE_0.getY());
 		this.robot1_MAXVALUE_MAXVALUE.moveNextTo(this.robot2_MAXVALUE_MAXVALUE);
 		assertEquals(Long.MAX_VALUE - 1, robot2_MAXVALUE_MAXVALUE.getX());
 		assertEquals(Long.MAX_VALUE,robot2_MAXVALUE_MAXVALUE.getY());
 
 		//hoekpunten terug resetten
 		this.robot1_0_0 = new Robot(0,0,0,100);
 		this.robot2_0_0 = new Robot(0,0,3,100);
 		this.robot1_0_MAXVALUE = new Robot(0,Long.MAX_VALUE,2,100);
 		this.robot2_0_MAXVALUE = new Robot(0,Long.MAX_VALUE,2,100);
 		this.robot1_MAXVALUE_0 = new Robot(Long.MAX_VALUE,0,1,100);
 		this.robot2_MAXVALUE_0 = new Robot(Long.MAX_VALUE,0,0,100);
 		this.robot1_MAXVALUE_MAXVALUE = new Robot(Long.MAX_VALUE,Long.MAX_VALUE,1,100);
 		this.robot2_MAXVALUE_MAXVALUE = new Robot(Long.MAX_VALUE,Long.MAX_VALUE,2,100);
 
 		//tweede test op de hoekpunten
 		this.robot1_0_0.moveNextTo(robot2_0_0);
 		assertEquals(0,this.robot2_0_0.getX());
 		assertEquals(0,this.robot2_0_0.getY());
 		this.robot1_0_MAXVALUE.moveNextTo(robot2_0_MAXVALUE);
 		assertEquals(0,this.robot2_0_MAXVALUE.getX());
 		assertEquals(Long.MAX_VALUE,this.robot2_0_MAXVALUE.getY());
 		this.robot1_MAXVALUE_0.moveNextTo(robot2_MAXVALUE_0);
 		assertEquals(Long.MAX_VALUE,this.robot2_MAXVALUE_0.getX());
 		assertEquals(0,this.robot2_MAXVALUE_0.getY());
 		this.robot1_MAXVALUE_MAXVALUE.moveNextTo(this.robot2_MAXVALUE_MAXVALUE);
 		assertEquals(Long.MAX_VALUE, robot2_MAXVALUE_MAXVALUE.getX());
 		assertEquals(Long.MAX_VALUE,robot2_MAXVALUE_MAXVALUE.getY());		
 	}
 
 	@Test
 	public void testMoveNextTo_Robot2onderRobot_gngEnergy(){
 		//8 robot cases initialiseren
 		this.robot = new Robot(10,10,0,20000);
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot4= new Robot(10,20,0,20000);
 		this.robot5 = new Robot(10,20,1,20000);
 		this.robot6 = new Robot(10,20,2,20000);
 		this.robot7 = new Robot(10,20,3,20000);
 		//laten moven 0
 		this.robot.moveNextTo(this.robot4);
 		assertEquals(10,robot4.getX());
 		assertEquals(11,robot4.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot5);
 		assertEquals(10,robot5.getX());
 		assertEquals(11,robot5.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot6);
 		assertEquals(10,robot.getX());
 		assertEquals(19,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot7);
 		assertEquals(10,robot7.getX());
 		assertEquals(11,robot7.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(10,20,0,20000);
 		this.robot5 = new Robot(10,20,1,20000);
 		this.robot6 = new Robot(10,20,2,20000);
 		this.robot7 = new Robot(10,20,3,20000);
 		//laten moven 1
 		this.robot1.moveNextTo(this.robot4);
 		assertEquals(10,robot4.getX());
 		assertEquals(11,robot4.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot5);
 		assertEquals(10,robot1.getX());
 		assertEquals(19,robot1.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot6);
 		assertEquals(10,robot1.getX());
 		assertEquals(19,robot1.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot7);
 		assertEquals(10,robot1.getX());
 		assertEquals(19,robot1.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(10,20,0,20000);
 		this.robot5 = new Robot(10,20,1,20000);
 		this.robot6 = new Robot(10,20,2,20000);
 		this.robot7 = new Robot(10,20,3,20000);
 		//laten moven 2
 		this.robot2.moveNextTo(this.robot4);
 		assertEquals(10,robot2.getX());
 		assertEquals(19,robot2.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot5);
 		assertEquals(10,robot2.getX());
 		assertEquals(19,robot2.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot6);
 		assertEquals(10,robot2.getX());
 		assertEquals(19,robot2.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot7);
 		assertEquals(10,robot2.getX());
 		assertEquals(19,robot2.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(10,20,0,20000);
 		this.robot5 = new Robot(10,20,1,20000);
 		this.robot6 = new Robot(10,20,2,20000);
 		this.robot7 = new Robot(10,20,3,20000);
 		//laten moven 3
 		this.robot3.moveNextTo(this.robot4);
 		assertEquals(10,robot4.getX());
 		assertEquals(11,robot4.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot5);
 		assertEquals(10,robot3.getX());
 		assertEquals(19,robot3.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot6);
 		assertEquals(10,robot3.getX());
 		assertEquals(19,robot3.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot7);
 		assertEquals(10,robot3.getX());
 		assertEquals(19,robot3.getY());
 	}
 
 	@Test
 	public void testMoveNextTo_Robot2BovenRobot_gngEnergy(){
 		//8 robot cases initialiseren
 		this.robot = new Robot(10,10,0,20000);
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot4= new Robot(10,0,0,20000);
 		this.robot5 = new Robot(10,0,1,20000);
 		this.robot6 = new Robot(10,0,2,20000);
 		this.robot7 = new Robot(10,0,3,20000);
 		//laten moven 0
 		this.robot.moveNextTo(this.robot4);
 		assertEquals(10,robot.getX());
 		assertEquals(1,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot5);
 		assertEquals(10,robot.getX());
 		assertEquals(1,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot6);
 		assertEquals(10,robot.getX());
 		assertEquals(1,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot7);
 		assertEquals(10,robot.getX());
 		assertEquals(1,robot.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(10,0,0,20000);
 		this.robot5 = new Robot(10,0,1,20000);
 		this.robot6 = new Robot(10,0,2,20000);
 		this.robot7 = new Robot(10,0,3,20000);
 		//laten moven 1
 		this.robot1.moveNextTo(this.robot4);
 		assertEquals(10,robot1.getX());
 		assertEquals(1,robot1.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot5);
 		assertEquals(10,robot1.getX());
 		assertEquals(1,robot1.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot6);
 		assertEquals(10,robot6.getX());
 		assertEquals(9,robot6.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot7);
 		assertEquals(10,robot.getX());
 		assertEquals(1,robot.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(10,0,0,20000);
 		this.robot5 = new Robot(10,0,1,20000);
 		this.robot6 = new Robot(10,0,2,20000);
 		this.robot7 = new Robot(10,0,3,20000);
 		//laten moven 2
 		this.robot2.moveNextTo(this.robot4);
 		assertEquals(10,robot.getX());
 		assertEquals(1,robot.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot5);
 		assertEquals(10,robot5.getX());
 		assertEquals(9,robot5.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot6);
 		assertEquals(10,robot6.getX());
 		assertEquals(9,robot6.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot7);
 		assertEquals(10,robot7.getX());
 		assertEquals(9,robot7.getY());
 		//resetten om safe te zijn 
 		this.robot4 = new Robot(10,0,0,20000);
 		this.robot5 = new Robot(10,0,1,20000);
 		this.robot6 = new Robot(10,0,2,20000);
 		this.robot7 = new Robot(10,0,3,20000);
 		//laten moven 3
 		this.robot3.moveNextTo(this.robot4);
 		assertEquals(10,robot3.getX());
 		assertEquals(1,robot3.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot5);
 		assertEquals(10,robot3.getX());
 		assertEquals(1,robot3.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot6);
 		assertEquals(10,robot6.getX());
 		assertEquals(9,robot6.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot7);
 		assertEquals(10,robot3.getX());
 		assertEquals(1,robot3.getY());
 	}
 
 	@Test
 	public void testMoveNextTo_Robot2LinksRobot_gngEnergy(){
 		//8 robot cases initialiseren
 		this.robot = new Robot(10,10,0,20000);
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot4= new Robot(0,10,0,20000);
 		this.robot5 = new Robot(0,10,1,20000);
 		this.robot6 = new Robot(0,10,2,20000);
 		this.robot7 = new Robot(0,10,3,20000);
 		//laten moven 0
 		this.robot.moveNextTo(this.robot4);
 		assertEquals(1,robot.getX());
 		assertEquals(10,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot5);
 		assertEquals(9,robot5.getX());
 		assertEquals(10,robot5.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot6);
 		assertEquals(1,robot.getX());
 		assertEquals(10,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot7);
 		assertEquals(1,robot.getX());
 		assertEquals(10,robot.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(0,10,0,20000);
 		this.robot5 = new Robot(0,10,1,20000);
 		this.robot6 = new Robot(0,10,2,20000);
 		this.robot7 = new Robot(0,10,3,20000);
 		//laten moven 1
 		this.robot1.moveNextTo(this.robot4);
 		assertEquals(9,robot4.getX());
 		assertEquals(10,robot4.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot5);
 		assertEquals(9,robot4.getX());
 		assertEquals(10,robot4.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot6);
 		assertEquals(9,robot4.getX());
 		assertEquals(10,robot4.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot7);
 		assertEquals(1,robot1.getX());
 		assertEquals(10,robot1.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(0,10,0,20000);
 		this.robot5 = new Robot(0,10,1,20000);
 		this.robot6 = new Robot(0,10,2,20000);
 		this.robot7 = new Robot(0,10,3,20000);
 		//laten moven 2
 		this.robot2.moveNextTo(this.robot4);
 		assertEquals(1,robot2.getX());
 		assertEquals(10,robot2.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot5);
 		assertEquals(9,robot5.getX());
 		assertEquals(10,robot5.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot6);
 		assertEquals(1,robot2.getX());
 		assertEquals(10,robot2.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot7);
 		assertEquals(1,robot2.getX());
 		assertEquals(10,robot2.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(0,10,0,20000);
 		this.robot5 = new Robot(0,10,1,20000);
 		this.robot6 = new Robot(0,10,2,20000);
 		this.robot7 = new Robot(0,10,3,20000);
 		//laten moven 3
 		this.robot3.moveNextTo(this.robot4);
 		assertEquals(1,robot3.getX());
 		assertEquals(10,robot3.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot5);
 		assertEquals(1,robot3.getX());
 		assertEquals(10,robot3.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot6);
 		assertEquals(1,robot3.getX());
 		assertEquals(10,robot3.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot7);
 		assertEquals(1,robot3.getX());
 		assertEquals(10,robot3.getY());
 	}
 
 	@Test
 	public void testMoveNextTo_Robot2RechtsRobot_gngEnergy(){
 		//8 robot cases initialiseren
 		this.robot = new Robot(10,10,0,20000);
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot4= new Robot(20,10,0,20000);
 		this.robot5 = new Robot(20,10,1,20000);
 		this.robot6 = new Robot(20,10,2,20000);
 		this.robot7 = new Robot(20,10,3,20000);
 		//laten moven 0
 		this.robot.moveNextTo(this.robot4);
 		assertEquals(19,robot.getX());
 		assertEquals(10,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot5);
 		assertEquals(19,robot.getX());
 		assertEquals(10,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot6);
 		assertEquals(19,robot.getX());
 		assertEquals(10,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot7);
 		assertEquals(11,robot7.getX());
 		assertEquals(10,robot7.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(20,10,0,20000);
 		this.robot5 = new Robot(20,10,1,20000);
 		this.robot6 = new Robot(20,10,2,20000);
 		this.robot7 = new Robot(20,10,3,20000);
 		//laten moven 1
 		this.robot1.moveNextTo(this.robot4);
 		assertEquals(19,robot1.getX());
 		assertEquals(10,robot1.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot5);
 		assertEquals(19,robot1.getX());
 		assertEquals(10,robot1.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot6);
 		assertEquals(19,robot1.getX());
 		assertEquals(10,robot1.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot7);
 		assertEquals(19,robot1.getX());
 		assertEquals(10,robot1.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(20,10,0,20000);
 		this.robot5 = new Robot(20,10,1,20000);
 		this.robot6 = new Robot(20,10,2,20000);
 		this.robot7 = new Robot(20,10,3,20000);
 		//laten moven 2
 		this.robot2.moveNextTo(this.robot4);
 		assertEquals(19,robot2.getX());
 		assertEquals(10,robot2.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot5);
 		assertEquals(19,robot2.getX());
 		assertEquals(10,robot2.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot6);
 		assertEquals(19,robot2.getX());
 		assertEquals(10,robot2.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot7);
 		assertEquals(11,robot7.getX());
 		assertEquals(10,robot7.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(20,10,0,20000);
 		this.robot5 = new Robot(20,10,1,20000);
 		this.robot6 = new Robot(20,10,2,20000);
 		this.robot7 = new Robot(20,10,3,20000);
 		//laten moven 3
 		this.robot3.moveNextTo(this.robot4);
 		assertEquals(11,robot4.getX());
 		assertEquals(10,robot4.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot5);
 		assertEquals(19,robot3.getX());
 		assertEquals(10,robot3.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot6);
 		assertEquals(11,robot6.getX());
 		assertEquals(10,robot6.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot7);
 		assertEquals(11,robot7.getX());
 		assertEquals(10,robot7.getY());
 	}
 
 	@Test
 	public void testMoveNextTo_Robot2LinksBovenRobot_gngEnergy(){
 		//8 robot cases initialiseren
 		this.robot = new Robot(10,10,0,20000);
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot4= new Robot(0,0,0,20000);
 		this.robot5 = new Robot(0,0,1,20000);
 		this.robot6 = new Robot(0,0,2,20000);
 		this.robot7 = new Robot(0,0,3,20000);
 		//laten moven 0
 		this.robot.moveNextTo(this.robot4);
 		assertEquals(1,robot.getX());
 		assertEquals(0,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot5);
 		assertEquals(10,robot.getX());
 		assertEquals(0,robot.getY());
 		assertEquals(9,robot5.getX());
 		assertEquals(0,robot5.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot6);
 		assertEquals(1,robot.getX());
 		assertEquals(0,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot7);
 		assertEquals(1,robot.getX());
 		assertEquals(0,robot.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(0,0,0,20000);
 		this.robot5 = new Robot(0,0,1,20000);
 		this.robot6 = new Robot(0,0,2,20000);
 		this.robot7 = new Robot(0,0,3,20000);
 		//laten moven 1
 		this.robot1.moveNextTo(this.robot4);
 		assertEquals(1,robot1.getX());
 		assertEquals(0,robot1.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot5);
 		assertEquals(10,robot5.getX());
 		assertEquals(9,robot5.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot6);
 		assertEquals(9,robot6.getX());
 		assertEquals(10,robot6.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot7);
 		assertEquals(1,robot1.getX());
 		assertEquals(0,robot1.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(0,0,0,20000);
 		this.robot5 = new Robot(0,0,1,20000);
 		this.robot6 = new Robot(0,0,2,20000);
 		this.robot7 = new Robot(0,0,3,20000);
 		//laten moven 2
 		this.robot2.moveNextTo(this.robot4);
 		assertEquals(0,robot2.getX());
 		assertEquals(1,robot2.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot5);
 		assertEquals(10,robot5.getX());
 		assertEquals(9,robot5.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot6);
 		assertEquals(9,robot6.getX());
 		assertEquals(10,robot6.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot7);
 		assertEquals(0,robot2.getX());
 		assertEquals(1,robot2.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(0,0,0,20000);
 		this.robot5 = new Robot(0,0,1,20000);
 		this.robot6 = new Robot(0,0,2,20000);
 		this.robot7 = new Robot(0,0,3,20000);
 		//laten moven 3
 		this.robot3.moveNextTo(this.robot4);
 		assertEquals(0,robot3.getX());
 		assertEquals(1,robot3.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot5);
 		assertEquals(0,robot3.getX());
 		assertEquals(1,robot3.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot6);
 		assertEquals(0,robot3.getX());
 		assertEquals(10,robot3.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot7);
 		assertEquals(0,robot3.getX());
 		assertEquals(1,robot3.getY());
 	}
 
 	@Test
 	public void testMoveNextTo_Robot2RechtsBovenRobot_gngEnergy(){
 		//8 robot cases initialiseren
 		this.robot = new Robot(10,10,0,20000);
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot4= new Robot(20,0,0,20000);
 		this.robot5 = new Robot(20,0,1,20000);
 		this.robot6 = new Robot(20,0,2,20000);
 		this.robot7 = new Robot(20,0,3,20000);
 		//laten moven 0
 		this.robot.moveNextTo(this.robot4);
 		assertEquals(19,robot.getX());
 		assertEquals(0,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot5);
 		assertEquals(19,robot.getX());
 		assertEquals(0,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot6);
 		assertEquals(19,robot.getX());
 		assertEquals(0,robot.getY());
 		this.robot = new Robot(10,10,0,20000);
 		this.robot.moveNextTo(this.robot7);
 		assertEquals(10,robot.getX());
 		assertEquals(0,robot.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(20,0,0,20000);
 		this.robot5 = new Robot(20,0,1,20000);
 		this.robot6 = new Robot(20,0,2,20000);
 		this.robot7 = new Robot(20,0,3,20000);
 		//laten moven 1
 		this.robot1.moveNextTo(this.robot4);
 		assertEquals(20,robot1.getX());
 		assertEquals(1,robot1.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot5);
 		assertEquals(20,robot1.getX());
 		assertEquals(1,robot1.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot6);
 		assertEquals(20,robot1.getX());
 		assertEquals(10,robot1.getY());
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot1.moveNextTo(this.robot7);
 		assertEquals(20,robot1.getX());
 		assertEquals(1,robot1.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(20,0,0,20000);
 		this.robot5 = new Robot(20,0,1,20000);
 		this.robot6 = new Robot(20,0,2,20000);
 		this.robot7 = new Robot(20,0,3,20000);
 		//laten moven 2
 		this.robot2.moveNextTo(this.robot4);
 		assertEquals(20,robot2.getX());
 		assertEquals(1,robot2.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot5);
 		assertEquals(20,robot2.getX());
 		assertEquals(1,robot2.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot6);
 		assertEquals(11,robot6.getX());
 		assertEquals(10,robot6.getY());
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot2.moveNextTo(this.robot7);
 		assertEquals(20,robot1.getX());
 		assertEquals(1,robot1.getY());
 	//resetten om safe te zijn 
 		this.robot4= new Robot(20,0,0,20000);
 		this.robot5 = new Robot(20,0,1,20000);
 		this.robot6 = new Robot(20,0,2,20000);
 		this.robot7 = new Robot(20,0,3,20000);
 		//laten moven 3
 		this.robot3.moveNextTo(this.robot4);
 		assertEquals(19,robot3.getX());
 		assertEquals(0, robot3.getY());
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot3.moveNextTo(this.robot5);
 		assertEquals(19,robot3.getX());
 		assertEquals(0, robot3.getY());
 	}
 
 	@Test
 	public void testMoveNextTo_Robot2RechtsOnderRobot_gngEnergy(){
 		//8 robot cases initialiseren
 		this.robot = new Robot(10,10,0,20000);
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot4= new Robot(20,20,0,20000);
 		this.robot5 = new Robot(20,20,1,20000);
 		this.robot6 = new Robot(20,20,2,20000);
 		this.robot7 = new Robot(20,20,3,20000);
 		//laten moven 0
 		this.robot.moveNextTo(this.robot5);
 		assertEquals(20, robot.getX());
 		assertEquals(19, robot.getY());
 		//resetten om safe te zijn 
 		this.robot4= new Robot(20,20,0,20000);
 		this.robot5 = new Robot(20,20,1,20000);
 		this.robot6 = new Robot(20,20,2,20000);
 		this.robot7 = new Robot(20,20,3,20000);
 		//laten moven 3
 		this.robot3.moveNextTo(this.robot5);
 		assertEquals(19, robot3.getX());
 		assertEquals(20, robot3.getY());
 	}
 
 	@Test
 	public void testMoveNextTo_Robot2LinksOnderRobot_gngEnergy(){
 		//8 robot cases initialiseren
 		this.robot = new Robot(10,10,0,20000);
 		this.robot1 = new Robot(10,10,1,20000);
 		this.robot2 = new Robot(10,10,2,20000);
 		this.robot3 = new Robot(10,10,3,20000);
 		this.robot4= new Robot(0,20,0,20000);
 		this.robot5 = new Robot(0,20,1,20000);
 		this.robot6 = new Robot(0,20,2,20000);
 		this.robot7 = new Robot(0,20,3,20000);
 		//laten moven 0
 		this.robot.moveNextTo(this.robot6);
 		assertEquals(0, robot.getX());
 		assertEquals(19, robot.getY());
 		this.robot4= new Robot(5,1,1,20000);
 		this.robot5 = new Robot(3,5,1,1);
 		robot4.moveNextTo(robot5);
 		assertEquals(4, robot4.getX());
 		assertEquals(5, robot4.getY());
 	}
 
 	public boolean isNextTo(Robot robotA,Robot robotB){
 		 if ((robotA.getX() == robotB.getX()) && ((robotA.getY() + 1 == robotB.getY())||(robotA.getY() == robotB.getY()+ 1)))
 			 return true;
 		 else if ((robotA.getY() == robotB.getY()) && ((robotA.getX() + 1 == robotB.getX())|| (robotA.getX() == robotB.getX() + 1)))
 			 return true;
 		return false; 
 	 }
 }
