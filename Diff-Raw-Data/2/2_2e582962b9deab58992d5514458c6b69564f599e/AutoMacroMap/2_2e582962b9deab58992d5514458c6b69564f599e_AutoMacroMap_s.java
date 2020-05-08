 package org.usfirst.frc1318.autonomous;
 
 import org.usfirst.frc1318.autonomous.macros.*;
 import org.usfirst.frc1318.generic.shared.GamePadData;
 import org.usfirst.frc1318.FRC2013.shared.ReferenceData;
 import org.usfirst.frc1318.components.RobotComponentBase;
 
 
 public class AutoMacroMap extends RobotComponentBase
 {
 	private AutoRunner autoRunner;
 	
 	public AutoMacroMap(AutoRunner autoRunner)
 	{
 		this.autoRunner = autoRunner;
 	}
 	
 	//reads from the button table.
 	//will use an ElseIf to see if buttons are pressed, 
 	//certain ones taking priority
 	
 	public void teleopPeriodic() // change to other update
 	{
		if(ReferenceData.getInstance().getUserInputData().getAutoAnyButton())//anybutton.ispressed()
 		{
 			if(!autoRunner.hasActiveTask())
 			{
 				if(ReferenceData.getInstance().getUserInputData().getAutoLift())
 				{
 					autoRunner.setTask(new AutoLiftingMacro());
 				}
 				else if(ReferenceData.getInstance().getUserInputData().getAutoTranslateRight())
 				{
 					autoRunner.setTask(new AutoTranslateMacro());
 				}
 			}
 			else
 			{
 				autoRunner.cancelTask();
 			}
 		}
 	}
 }
