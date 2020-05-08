 package state_machine;
 
import map.Map;
 import core.Config;
 
 public class TimeoutState extends State{
     
     
     public TimeoutState() {
         tooLong = Config.CHALLENGE_TIME;
     }
 
     protected State transition() {
    	Map map = Map.getInstance();
    	if (prev.getClass() == ExploreState.class ||prev.getClass() == CollectState.class){
    		map.removeBlock(map.closestBlock());
     		return new ExploreState();
    	}
    	    	
    	else if (prev.getClass() == FindShelterState.class){
    		map.ShelterLocation = map.bot.pose;
    		return new AssemblyState();
    	}
    	else if (prev.getClass() == AssemblyState.class )
    		return new StopState();
         return this;
     }
     
     protected void run() {
         
     }
 }
