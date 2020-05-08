 package org.usfirst.frc1318.calculators;
 
import org.usfirst.frc1318.data.TBGamepadData;
import org.usfirst.frc1318.runners.Runner;
 

public class TBKickerCalculator extends Runner
 {
 	boolean isAutomated;
 	int timer;
 	final int timeout = 50;
 	
 	public TBKickerCalculator()
 	{
 		
 	}
 	
 	private void checkShouldKick()
 	{
 		if(isAutomated)
 		{
			if(true)//insert beam sensor boolean here
 			{
 				//kickKicker();
				isKicked = true;
 				timer = timeout;
 			}
 		}
 		if(TBGamepadData.isKickButton())//insert button pressed boolean here
 		{
 			//kickKicker();
			isKicked=true;
 			timer = timeout;
 		}
 	}
 	
 	private void checkShouldNotKick()
 	{
 		timer -=1;
 		if(timer <= 0)
 		{
			isKicked = false;
 			//retractKicker();
 		}
 	}
 	
 	public void doLogic()
 	{
		if(isKicked)
 			checkShouldNotKick();
 		else
 			checkShouldKick();
 	}
 
 	
 	public boolean calculate(boolean buttonPressed) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
