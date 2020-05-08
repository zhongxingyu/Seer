 package itc;
 import java.util.*;
 
 import robocode.HitByBulletEvent;
 import robocode.ScannedRobotEvent;
 
 
 public class CTactic {
 
 	
 	//This is the sucess thershold that a tactic most to be labelled as efftive 
 	protected final byte GAUGING_THERSHOLD = 60; 
 	//protected List gaugingList = new ArrayList();
 	protected List<Byte> gaugingList  = new ArrayList<Byte>();
 
 	
	public void run(solomon s)
 	{
 		
 	}
 	
 
 	public void onScannedRobot_(solomon s, ScannedRobotEvent e)
 	{
 		
 	}
 	
 	
 	public void onHitByBullet_(solomon s, HitByBulletEvent e)
 	{
 		
 	}
 	
 	/**
 	 * Calcuates the effecncey of the tactic using the gaugingList
 	 * and returns true and false
 	 * @return
 	 */
 	public boolean isGoodTactic()
 	{
 		boolean result = false;
 		int sumOfGauging = 0;		
 		int sumOfElements = gaugingList.size();
 		
 		for(int i = 0; i < sumOfElements; i++)
 		{
 		  sumOfGauging += gaugingList.get(i);
 		}
 		
 		sumOfGauging = (sumOfGauging/sumOfElements)*100; 
 		
 		if(sumOfGauging < GAUGING_THERSHOLD)
 		{
 			result = true;
 		}
 		
 		return result;
 	}
 }
