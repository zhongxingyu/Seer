 /**
  * 
  */
 package tests;
 
 import static org.junit.Assert.*;
 
 
 import org.junit.Test;
 
 import vehicles.AirPlaneFactory;
 import vehicles.Airplane;
 
 import vehicles.APDirector;
 import vehicles.AirplaneBuilder;
 import vehicles.Flying;
 import vehicles.HarrierBuilder;
 import vehicles.HarrierFactory;
 import vehicles.ModelAirPlaneFactory;
 import vehicles.ModelAirplaneBuilder;
 import vehicles.PassengerPlaneFactory;
 
 import vehicles.LiftOff;
 
 
 /**
  * @author oded
  *
  */
 public class AirplaneTest {
 
 
 
 	@Test
 	public void test1() {
 				
 		String expectedOutput = "Like a fighter jet";
 		String stringReturned = null;
 		
 		AirplaneBuilder harrierBuilder = new HarrierBuilder();
 		
 		APDirector director = new APDirector(harrierBuilder);
 		
 		director.buildAirPlane();
 
 		Airplane classUnderTest = harrierBuilder.getAirplane();
 
 		stringReturned = classUnderTest.howDoYouFly();
 
 		assertEquals("Wrong Answer !", expectedOutput, stringReturned);
 
 		}
 
 	
 	@Test
 	public void test2() {
 				
 		String expectedOutput = "I don't Fly";
 		String stringReturned = null;
 	
 		AirplaneBuilder harrierBuilder = new ModelAirplaneBuilder();
 		
 		APDirector director = new APDirector(harrierBuilder);
 		
 		director.buildAirPlane();
 
 		Airplane classUnderTest = harrierBuilder.getAirplane();
 
 
 		stringReturned = classUnderTest.howDoYouFly();
 
 		assertEquals("Wrong Answer !", expectedOutput, stringReturned);
 
 		}
 	
 	@Test
 	public void test3() {
 				
		String expectedOutput = "Like a  passenger airplane";
 		String stringReturned = null;
 		
 		AirPlaneFactory passengerPlaneFactory = new PassengerPlaneFactory();
 		
 		Flying fly = passengerPlaneFactory.createFlying();
 		LiftOff liftOff =  passengerPlaneFactory.createLiftOff();
 
 		Airplane classUnderTest = new Airplane(liftOff,fly);
 
 		stringReturned = classUnderTest.howDoYouFly();
 
 		assertEquals("Wrong Answer !", expectedOutput, stringReturned);
 
 		}
 	
 	
 	@Test
 	public void test4() {
 				
 		String expectedOutput = "Vertically";
 		String stringReturned = null;
 
 		AirPlaneFactory harrierFactory = new HarrierFactory();
 		
 		Flying fly = harrierFactory.createFlying();
 
 		LiftOff liftOff = harrierFactory.createLiftOff();
 
 		Airplane classUnderTest = new Airplane(liftOff,fly);
 
 		stringReturned = classUnderTest.howDoYouLiftOff();
 
 		assertEquals("Wrong Answer !", expectedOutput, stringReturned);
 
 		}
 	
 	@Test
 	public void test5() {
 				
 		String expectedOutput = "I don't LiftOff";
 		String stringReturned = null;
 
 		AirPlaneFactory modelAirPlaneFactory = new ModelAirPlaneFactory();
 		
 		Flying fly = modelAirPlaneFactory.createFlying();
 		LiftOff liftOff = modelAirPlaneFactory.createLiftOff();
 
 		Airplane classUnderTest = new Airplane(liftOff,fly);
 
 		stringReturned = classUnderTest.howDoYouLiftOff();
 
 		assertEquals("Wrong Answer !", expectedOutput, stringReturned);
 
 		}
 
 	@Test
 	public void test6() {
 				
 		String expectedOutput = "Horizontally";
 		String stringReturned = null;
 
 		AirPlaneFactory passengerPlaneFactory = new PassengerPlaneFactory();
 		
 		Flying fly = passengerPlaneFactory.createFlying();
 		LiftOff liftOff =  passengerPlaneFactory.createLiftOff();
 
 		Airplane classUnderTest = new Airplane(liftOff,fly);
 
 		stringReturned = classUnderTest.howDoYouLiftOff();
 
 		assertEquals("Wrong Answer !", expectedOutput, stringReturned);
 
 		}
 
 
 }
