 package ai.cs4730;
 
 import java.util.ArrayList;
 
 import rts.units.Unit;
 import rts.units.UnitAction;
 
 public class ArmyManager extends Manager{
 
    AIController    ai;
    private STATE   state;
 
    public ArmyManager(AIController ai){
       this.ai = ai;
       state = STATE.Explore;
    }
 
    @Override public void update(){
       for(Unit unit : ai.gameState.getOtherUnits()){
          if(unit.isBuilding()){
             BuildingUnitController bc = new BuildingUnitController(unit, ai);
             if(!ai.enemyBuildings.contains(bc)){
                ai.enemyBuildings.add(bc);
                if(AIController.DEBUG){
                   System.out.println("AM: found enemy building");
                }
             }
          }
       }
 
       switch (state){
          case Attack:
             break;
          case Buildup:
             break;
          case Cheese:
             break;
          case Explore:
             // request a scout after some workers are gathering resources
             if(ai.workers.size() >= ai.wantedWorkers){
                ai.wantedScouts = 1;
             }
 
             ArrayList<UnitController> toRemove = new ArrayList<UnitController>();
             for(UnitController scout : ai.scouts){
                if(scout.actions.size() <= 0){
                   if(ai.enemyBuildings.size() == 0){
                      // find the enemy base first, count on it being in the opposite corner
                      int targetX = MapUtil.WIDTH - ai.stockpiles.get(0).getX();
                      int targetY = MapUtil.HEIGHT - ai.stockpiles.get(0).getY();
                      ArrayList<Integer> destination = new ArrayList<Integer>();
                      destination.add(targetX + targetY * MapUtil.WIDTH);
                      // path to estimated location of enemy base
                      ArrayList<Integer[]> path = MapUtil.get_path(scout.unit, MapUtil.position(scout), ai.currentTurn, destination);
 
                      if(path != null){ // is possible to reach goal
                         boolean there = false;
                         if(path.size() == 0){
                            path.add(new Integer[]{MapUtil.position(scout), ai.currentTurn});
                            there = true;
                         }
 
                         // set order queue
                         if(!there){
                            for(int i = path.size() - 1; i >= 0; i--){
                               scout.addAction(new UnitAction(scout.unit, UnitAction.MOVE, path.get(i)[0] % MapUtil.WIDTH, path.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + scout.unit.getMoveSpeed());
                            }
                         }
                      }
                   }
                   else{
                      // turn scout into builder (for cheesing)
                      ai.wantedScouts = 0;
                      WorkerUnitController wc = (WorkerUnitController) ai.scouts.get(0);
                      toRemove.add(ai.scouts.get(0));
                      ai.builders.add(wc);
 
                      state = STATE.Cheese;
                   }
                }
                scout.act(ai);
             }
            for(UnitController uc : toRemove){
               ai.scouts.remove(uc);
            }
             break;
       }
    }
 
    @Override public void assignUnits(){
       ArrayList<UnitController> toRemove = new ArrayList<UnitController>();
       for(UnitController u : ai.freeUnits){
          if(u.getClass() == ArmyUnitController.class){
             ai.groundUnits.add(u);
             if(AIController.DEBUG){
                System.out.println("AM: acquired army unit");
             }
             toRemove.add(u);
          }
          else
             if(ai.wantedScouts > ai.scouts.size() && u.getClass() == WorkerUnitController.class){
                ai.scouts.add(u);
                if(AIController.DEBUG){
                   System.out.println("AM: acquired scout");
                }
                toRemove.add(u);
             }
       }
 
       // remove any units from freeUnits that were assigend
       for(UnitController u : toRemove){
          ai.freeUnits.remove(u);
       }
    }
 
    private enum STATE{
       Explore, Attack, Buildup, Cheese
    }
 
 }
