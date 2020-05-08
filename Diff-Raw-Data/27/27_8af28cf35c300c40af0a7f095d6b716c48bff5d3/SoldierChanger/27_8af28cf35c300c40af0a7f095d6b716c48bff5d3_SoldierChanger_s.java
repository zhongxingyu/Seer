 package ncl.military.controller.handle.executors;
 
 import ncl.military.dao.DAO;
 import ncl.military.dao.tools.EntityValue;
 import ncl.military.entity.Soldier;
 import ncl.military.entity.Unit;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author gural
  * @version 1.0
  *          Date: 26.04.12
  *          Time: 16:38
  */
 public class SoldierChanger extends Executor {
 
     public SoldierChanger(DAO dao) {
         super(dao);
     }
 
     public Map<String, Object> execute(Map<String, Object> params) {
         Map<String, Object> result = new HashMap<String, Object>();
 
         String action = (String) params.get("action");
         String subAction = (String) params.get("subAction");
         String soldierIdMatch = (String) params.get("soldierIdMatch");
 
         List<Soldier> soldierList = getDao().getNotSubsSoldiers(soldierIdMatch);
         List<Unit> unitList = getDao().getAllUnits();
 
         if (soldierIdMatch != null) {
 
             List<EntityValue> values = getValuesOfSolider(params);
 
             if (values.size() != 0) {
                 result.put("success", getDao().setSoldierAttributes(soldierIdMatch, values));
             }
         }
 
         result.put("queriedSoldier", getDao().getSoldierById(soldierIdMatch));
         result.put("listOfSoldiers", soldierList);
         result.put("listOfUnits", unitList);
         result.put("subAction", subAction);
         result.put("soldierIdMatch", soldierIdMatch);
 
         return result;
     }
 }
