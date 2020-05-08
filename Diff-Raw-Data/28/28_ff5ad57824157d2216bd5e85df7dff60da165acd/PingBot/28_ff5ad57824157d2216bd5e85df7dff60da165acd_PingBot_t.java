 package ch.nksa.pu.g2007e.pingbot;
 
 import ch.nksa.pu.g2007e.pingbot.SensorMount;
 import ch.nksa.pu.robotics.libs.BaseUnit;
 import ch.nksa.pu.robotics.libs.Uplink;
 import ch.nksa.pu.robotics.libs.BaseUnit.Orientation;
 import ch.nksa.pu.g2007e.pingbot.NxtCommunication;
 import lejos.nxt.*;
 
 public class PingBot {
 
 	/**
 	 * @param args
 	 */
 	//public static BaseUnit base = new BaseUnit();
	public static Uplink uplink = new Uplink(true);
 	public static SensorMount sMount = new SensorMount();
 	
 	public static void main(String[] args) throws Exception{
 		// TODO Auto-generated method stub
 		//base.setOrientation(Orientation.BACKWARD);
 		sMount.enablePositionLight();
		Thread.sleep(100000);
 	}
 }
