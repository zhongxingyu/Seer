 
 package ai.cs4730;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.PriorityQueue;
 
 import rts.units.Unit;
 import rts.units.UnitAction;
 
 public class TownManager extends Manager
 {
     public HashMap<Integer, Integer>        buildPriority;    //Label and Priority, Bigger Priority == More likely to build
     public HashMap<Integer, Integer>		unitBuildPriority;  
     // (Use order of 1 - 100) Every time a unit is made its
     // priority will drop by 1
     public ArrayList<FarmUnitController>      farms;            // int correlating to int[] map location of a resource patch                                                          
     public ArrayList<WorkerUnitController>   workers;
     public ArrayList<BuildingUnitController> stockpiles;
     public ArrayList<BuildingUnitController> buildings;
     
     public ArrayList<Integer> requestedUnits; 
     
     public static final int                   STOCKPILE     = 0;
     public static final int                   SOLDIEROFFICE = 1;
     public static final int                   AIRPORT       = 2;
     
     public TownManager()
     {
         buildPriority = new HashMap<Integer, Integer>();
         
         farms = new ArrayList<FarmUnitController>();
         workers = new ArrayList<WorkerUnitController>();
         buildings = new ArrayList<BuildingUnitController>();
         stockpiles = new ArrayList<BuildingUnitController>();
         
         requestedUnits = new ArrayList<Integer>();
     }
     
     @Override
     public void update( AIController ai )
     {
         // update farms map
         for ( Unit res : ai.gameState.getNeutralUnits() )
         {
             if ( !farms.contains( res ) )
             {
                 farms.add( new FarmUnitController( res, ai ) );
             }
         }
         
         for ( WorkerUnitController worker : workers )
         {
             if ( worker.actions.size() <= 0 ) //no actions?!?!?
             {
                 for ( FarmUnitController farm : farms )
                 {
                     if ( farm.free )
                     {
                         worker.actions.add( new UnitAction( worker.unit, UnitAction.MOVE, farm.getHarvestX(), farm.getHarvestY(), -1 ) );
                         worker.actions.add( new UnitAction( worker.unit, UnitAction.HARVEST, farm.getX(), farm.getY(), -1 ) );
                         worker.actions.add( new UnitAction( worker.unit, UnitAction.MOVE, stockpiles.get( 0 ).unit.getX(), stockpiles.get( 0 ).unit.getY() - 1, -1 ) );
                         worker.actions.add( new UnitAction( worker.unit, UnitAction.RETURN, stockpiles.get( 0 ).unit.getX(), stockpiles.get( 0 ).unit.getY(), -1 ) );
                     }
                 }
             }
             
             if ( !worker.hasAction() )
             {
                 worker.setAction( worker.actions.get( 0 ) );
                 worker.actions.remove( 0 );
             }
         }
         
         for ( BuildingUnitController stock : stockpiles )
         {
             stock.setAction( new UnitAction( stock.unit, UnitAction.BUILD, stock.getX() + 1, stock.getY() + 1, stock.getProduce().get( 0 ) ) );
         }
     }
     
     @Override
     public void assignUnits( AIController ai )
     {
         //Grab my units!!!
         for ( UnitController unit : ai.freeUnits )
         {
             if ( unit.getClass() == WorkerUnitController.class )
             {
                 workers.add( ( WorkerUnitController ) unit );
             }
             else if ( unit.getClass() == BuildingUnitController.class )
             {
                 BuildingUnitController bu = ( BuildingUnitController ) unit;
                 if ( bu.isStockpile() )
                 {
                     stockpiles.add( bu );
                 }
                 else
                 {
                     buildings.add( bu );
                 }
             }
         }
         
        //give it the workers
        //give it any buildings that deal with workers / new buildings / non-military stuff
     }
     
     public int numWorkers(){return workers.size();}
 }
