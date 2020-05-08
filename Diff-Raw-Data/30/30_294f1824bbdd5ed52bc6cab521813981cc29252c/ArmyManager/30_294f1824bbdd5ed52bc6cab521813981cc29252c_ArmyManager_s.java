 
 package ai.cs4730;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 
 import rts.units.UnitAction;
 
 public class ArmyManager extends Manager
 {
     
 	ArrayList<UnitController> units = new ArrayList<UnitController>();
 	ArrayList<UnitController> scouts = new ArrayList<UnitController>();
 	
 	private int wantedScouts;
 	
 	//game logic variable
 	private boolean foundEnemyBase = false;
 	
     private enum STATE
     {
         Explore, Attack, Defend
     };
     
     private STATE state;
     
     public ArmyManager()
     {
         state = STATE.Explore;
         
         wantedScouts = 0;
     }
     
     @Override
     public void update( AIController ai )
     {
     	switch(state){
     		case Attack:
     			break;
     		case Defend:
     			break;
     		case Explore:
     			//request a scout after some workers are gathering resources
     	    	if(ai.townManager.numWorkers() > 4){
     	    		wantedScouts = 1;
     	    	}
     	    	
     	    	for ( UnitController scout : scouts )
     	        {
     	            if ( scout.actions.size() <= 0 ){
     	            	if(!foundEnemyBase){
     	            		//find the enemy base first, count on it being in the opposite corner
     	            		int targetX = MapUtil.WIDTH - ai.townManager.buildings.get(0).getX();
     	            		int targetY = MapUtil.HEIGHT - ai.townManager.buildings.get(0).getY();
     	            		scout.actions.add( new UnitAction( scout.unit, UnitAction.MOVE, targetX, targetY, -1 ) );
     	            	}
     	            }
     	            
     	            if ( !scout.hasAction() )
     	            {
     	                scout.setAction( scout.actions.get( 0 ) );
     	                scout.actions.remove( 0 );
     	            }
     	        }
     			break;
     	}
     }
     
     @Override
     public void assignUnits( AIController ai )
     {
        //give it all military units and worker scouts
    	
     	for(UnitController u : ai.freeUnits)
     	{
     		if(u.getClass() == ArmyUnitController.class){
     			units.add(u);
     		}
     		else if(wantedScouts > scouts.size() && u.getClass() == WorkerUnitController.class)
     		{
     			units.add(u);
     		}
     	}
     }
     
 }
