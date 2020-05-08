 package odometer;
 
 import robot.Robot;
 import lejos.nxt.LCD;
 import lejos.nxt.LightSensor;
 import lejos.nxt.MotorPort;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.SensorPort;
 
 public class OdometerTest {
 	
 	private static LightSensor light = new LightSensor(SensorPort.S1);
 	private static NXTRegulatedMotor motorA = new NXTRegulatedMotor(MotorPort.A);
 	private static NXTRegulatedMotor motorB = new NXTRegulatedMotor(MotorPort.B);
 	private static NXTRegulatedMotor motorC = new NXTRegulatedMotor(MotorPort.C);
 	
 	public static void main(String[] args) throws InterruptedException
 	{
 		Odometer odo = new Odometer();
 		odo.start();
 		LCDInfo info = new LCDInfo(odo);
 		Robot robot = new Robot(motorA, motorB, motorC);
 		robot.moveRobotForward(100, 100);
 		while(true)
 		{
 			LCD.drawString("Odometer test", 0, 7);
 		}
 		/*while(true)
 		{
 			double[] data = new double[40];
 			for(int i = 0; i < data.length; i++)
 			{
 				data[i] = light.readValue();
 				Thread.sleep(25);
 			}
 			LCD.clear();
 			double[] medData = Data_Filtering.medianFiltering(data, 3);
 			double[] difData = Data_Filtering.differential(medData);
 			double[] med2Data = Data_Filtering.medianFiltering(difData, 3);
 			for(int i =0; i<med2Data.length;i++)
 			{
 				if(i<=med2Data.length-3)
 				{
 					if(med2Data[i]<0 && med2Data[i+1]<0)
 					{
 						LCD.drawString("BLACK",0,0);
 					}else{
 						LCD.drawString("White",0,1);
 					}
 					
 				}
 			}
 		}	
 		//Console.openUSB(5000);*/
 	}
 
 }
