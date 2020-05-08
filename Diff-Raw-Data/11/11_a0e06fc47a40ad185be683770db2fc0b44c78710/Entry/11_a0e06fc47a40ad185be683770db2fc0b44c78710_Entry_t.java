 package org.team4159.frc2013;
 
 import org.team4159.support.Controller;
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.RobotBase;
 import org.team4159.frc2013.controllers.AutonomousController;
 import org.team4159.frc2013.controllers.DisabledController;
 import org.team4159.frc2013.controllers.OperatorController;
 import org.team4159.frc2013.controllers.TestController;
 
 public class Entry extends RobotBase
 {
 	private static final int
 		MODE_UNKNOWN = 0,
 		MODE_DISABLED = 1,
 		MODE_AUTONOMOUS = 2,
 		MODE_OPERATOR = 3,
 		MODE_TEST = 4;
 	
 	public static final int TICK_INTERVAL_MS = 20;
 	
 	public Entry ()
 	{
 		System.out.println ("Entry instantiated.");
		
		// wake up the IO class
		IO.class.getName ();
 	}
 	
 	private int getMode ()
 	{
 		DriverStation ds = DriverStation.getInstance ();
 		if (ds.isDisabled ())
 			return MODE_DISABLED;
 		if (ds.isTest ())
 			return MODE_TEST;
 		if (ds.isAutonomous())
 			return MODE_AUTONOMOUS;
 		else
 			return MODE_OPERATOR;
 	}
 	
 	private Controller createController (int ct)
 	{
 		switch (ct)
 		{
 			case MODE_DISABLED:
 				return new DisabledController ();
 			case MODE_AUTONOMOUS:
 				return new AutonomousController ();
 			case MODE_OPERATOR:
 				return new OperatorController ();
 			case MODE_TEST:
 				return new TestController ();
 			default:
 				throw new IllegalArgumentException ("unknown controller code");
 		}
 	}
 	
 	public void startCompetition ()
 	{
 		System.out.println ("Entry.startCompetition() called.");
 		
 		int current_mode = MODE_UNKNOWN;
 		Controller controller = null;
 		
 		while (true)
 		{
 			int next_mode = getMode ();
 			if (current_mode != next_mode)
 				controller = createController (current_mode = next_mode);
 			
 			System.out.println ("Controller to " + controller.getClass ().getName ());
 			controller.run ();
 		}
 	}
 }
