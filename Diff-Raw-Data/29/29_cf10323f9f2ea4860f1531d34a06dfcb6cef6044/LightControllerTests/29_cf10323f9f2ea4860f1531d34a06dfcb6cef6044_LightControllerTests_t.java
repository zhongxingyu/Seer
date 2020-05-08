 package tests;
 
 /**
  * Mike
  * 	- MOTION_DETECTED
  *  - LAMP_TIMER_EXPIRED
  *  - SECURITY_ALARM_TRIPPED
  *  - ALARM_CLEARED
  * Matt
  *  - MANUAL_SWITCH_ON
  *  - MANUAL_SWITCH_OFF
  *  - LIGHT_SENSOR_LIGHTENED
  *  - LIGHT_SENSOR_DARKENED
  */
 
 import junit.framework.TestCase;
 import SecurityLightController.*;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 
 public class LightControllerTests extends TestCase {
 
 	private Mockery context;
 	
 	private LightTimerInterface timerMock;
 	private LightDeviceInterface deviceMock;
 	private LightControllerStateMachineObserverInterface observerMock;
 	
 	private LightControllerStateMachine classUnderTest;
 	
 	public void setUp() {
 		context = new Mockery();
 		
 		timerMock = context.mock(LightTimerInterface.class);
 		deviceMock = context.mock(LightDeviceInterface.class);
 		observerMock = context.mock(LightControllerStateMachineObserverInterface.class);
 		
 	}
 	
 	private void setupStateMachine() {
 		classUnderTest = new LightControllerStateMachine();
 		classUnderTest.setLight(deviceMock);
 		classUnderTest.setTmr(timerMock);
		classUnderTest.subscribe(observerMock);
 	}
 	
	public void testDaylightToNight() {
		setupStateMachine();
		
		
 		context.checking(new Expectations() {
 			{
 				oneOf(deviceMock).turnLightOff();
				oneOf(observerMock).updateLightState(LightControllerStateMachineObserverInterface.LAMP_OFF_NIGHTIME);
				never(timerMock).startTimer(0);
 			}
 		});
 		
		classUnderTest.signalAction(LightControllerStateMachine.LIGHT_SENSOR_DARKENED);
		
		context.assertIsSatisfied();
 		
 	}
	

 }
