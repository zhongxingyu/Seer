 package org.usfirst.frc1318.calculators;
 
import org.usfirst.frc1318.shared.TBGamepadData;
import org.usfirst.frc1318.shared.TBThroughBeamData;
import org.usfirst.frc1318.shared.TBSolenoidData;
import org.usfirst.frc1318.components.RobotComponentBase;
 
public class TBKickerCalculator extends RobotComponentBase
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
			if(TBThroughBeamData.get())//insert beam sensor boolean here
 			{
 				//kickKicker();
				TBSolenoidData.setIfKicked(true);
 				timer = timeout;
 			}
 		}
 		if(TBGamepadData.isKickButton())//insert button pressed boolean here
 		{
 			//kickKicker();
			TBSolenoidData.setIfKicked(true);
 			timer = timeout;
 		}
 	}
 	
 	private void checkShouldNotKick()
 	{
 		timer -=1;
 		if(timer <= 0)
 		{
			TBSolenoidData.setIfKicked(false);
 			//retractKicker();
 		}
 	}
 	
 	public void doLogic()
 	{
		if(TBSolenoidData.kicked())
 			checkShouldNotKick();
 		else
 			checkShouldKick();
 	}
 
 	
 	public boolean calculate(boolean buttonPressed) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
