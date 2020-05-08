 package ncl.military.controller.handle.executors;
 
 import ncl.military.controller.handle.HandlerFactory;
 import ncl.military.dao.DAO;
 import ncl.military.dao.tools.Alias;
 import ncl.military.dao.tools.EntityValue;
 import ncl.military.entity.Soldier;
 import ncl.military.entity.Unit;
 import org.apache.log4j.Logger;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: Silvan
  * Date: 27.04.12
  * Time: 6:06
  */
 public class SoldierAdder extends Executor {
 
     public SoldierAdder(DAO dao) {
         super(dao);
     }
 
     public Map<String, Object> execute(Map<String, Object> params) {
         Map<String, Object> result = new HashMap<String, Object>();
 
         String action = (String) params.get("action");
 
         List<Soldier> soldierList = getDao().getAllSoldiers();
         result.put("listOfSoldiers", soldierList);
         Logger.getLogger("controller").info("listOfSoldier added to context");
 
         List<Unit> unitList = getDao().getAllUnits();
         result.put("listOfUnits", unitList);
         Logger.getLogger("controller").info("listOfUnits added to context");
 
         if ((HandlerFactory.ADD_SOLDIER).equals(action)) {
             List<EntityValue> values = getValuesOfSolider(params);
 
             if (values.size() != 0) {
                 result.put("success", getDao().addSoldier(values));
                String commanderIdMatch = (String) params.get(Alias.SOLDIER_COMMANDER.getLabelAsQueried());
                result.put("commanderIdMatch", ("TOP".equals(commanderIdMatch)) ? null : commanderIdMatch);
             }
         }
 
         if (params.get("queriedCommanderId") != null) {
             result.put("queriedCommanderId", params.get("queriedCommanderId"));
             Logger.getLogger("controller").info("queriedCommanderId added to context");
         }
 
         return result;
     }
 }
