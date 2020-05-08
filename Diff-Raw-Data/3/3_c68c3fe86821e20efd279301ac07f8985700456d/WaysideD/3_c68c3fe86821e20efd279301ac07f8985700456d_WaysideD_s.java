 package wayside;
 
 import global.*;
 import trackmodel.*;
 
 public class WaysideD extends Wayside
 {
 	public WaysideD(ID id)
 	{
 		super(id);
 	}
 
 	void runLogic()
 	{
 	/* 
 	 * Don't yet know the train/track implementation to
 	 * determine if a train is headed for the train yard.
 	 */
 	 
 	/*
 		Switch tySwitch = (Switch) track.get(trackEnd());
 		Train train = tySwitch.getTrain();
 		if ((train != null) && train.needsToGoToTrainYard())
 		{
 			tySwitch.setSwitchState(Switch.SwitchState.LEFT);
 			spreadAuthority(1);
 		}
 		else
 		{
 			tySwitch.setSwitchState(Switch.SwitchState.RIGHT);
 			if (nextRight().clearToReceiveFrom(this))
 			{
 				spreadAuthority(0);
 			}
 			else
 			{
 				Switch s = (Switch) track.get(trackEnd()).getNext(direction);
 				s.setSwitchState(Switch.SwitchState.RIGHT);
 				spreadAuthority(1);
 			}
 		}
	}
 	*/
 }
