 package ncl.military.controller.handle.executors;
 
 import ncl.military.controller.handle.HandlerFactory;
import ncl.military.dao.tools.Alias;
 
 import java.util.Map;
 
 /**
  * @author gural
  * @version 1.0
  *          Date: 14.05.12
  *          Time: 17:13
  */
 public class SoldierOnMoveChanger extends Executor {
 
     public SoldierOnMoveChanger(Executor executor) {
         super(executor);
     }
 
     public Map<String, Object> execute(Map<String, Object> params) {
         Map<String, Object> result = executor.execute(params);
 
         // action for mover (EditOnMoveDecorator)
         if ((Boolean) result.get("success")) {
             result.put("subAction", null);
             result.put("soldierIdMatch", null);
         }
 
        String commanderId = (String) params.get(Alias.SOLDIER_COMMANDER.getLabelAsQueried());
         result.put("action", HandlerFactory.GET_SUBS_OF_SOLDIER);
         result.put("listOfSoldiers", getDao().getSubSoldiersOfByID(commanderId));
         result.put("hierarchyList", getDao().getHierarchy(commanderId));
         result.put("queriedSoldier", getDao().getSoldierById(commanderId));
 
         return result;
     }
 }
