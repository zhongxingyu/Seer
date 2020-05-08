 package itc;
import java.util.*;
 
 import robocode.HitByBulletEvent;
 import robocode.ScannedRobotEvent;
 
 
 public class CTactic {
 
 	
	//This is the sucess thershold that a tactic most to be labelled as efftive 
	protected final byte GAUGING_THERSHOLD = 60; 
 	//protected List gaugingList = new ArrayList();
 	protected List<Byte> gaugingList  = new ArrayList<Byte>();
 
 	
 	public void run_(solomon s)
 	{
 		
 	}
 	
 
 	public void onScannedRobot_(solomon s, ScannedRobotEvent e)
 	{
 		
 	}
 	
 	
 	public void onHitByBullet_(solomon s, HitByBulletEvent e)
 	{
 		
 	}
 	
 	
 	/**
 	 * Uses the distance of the enemy robot to figure out how
 	 * much energy to expend when firing. If it's close, it
 	 * fires with strength 3, and far away, less.
 	 * 
 	 * @param s
 	 * @param enemyDist
 	 */
 	protected void fire(solomon s, double enemyDist) {
 		// TODO: This can be simplified (probably very easily so it doesn't require a case statement);
 	
 		int bias = 0;
 		switch (s.getStatus()) {
 			case 0 :
 				bias = 400;
 				break;
 			case 1 :
 				bias = 300;
 				break;
 			case 2 :
 				bias = 200;
 				break;
 			case 3 :
 				bias = 100;
 				break;
 			default :
 				break;
 		}
 		
 		double firePower = 0;
 		
 		if (enemyDist > 300)
 		{
 			firePower = 0.1;
 		}
 		else if (enemyDist < 50)
 		{
 			firePower = 3.0;
 		}
 		else
 		{
 			firePower = bias/enemyDist;
 		}
 		
 		s.fire(firePower);
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
 		
 		if(sumOfElements != 0)
 		{
		sumOfGauging = (sumOfGauging/sumOfElements)*100; 
 		}
 		else
 		{
 			sumOfGauging = 100;
 		}
 		
		if(sumOfGauging < GAUGING_THERSHOLD)
 		{
 			result = true;
 		}
 		
 		return result;
 	}



 }
