 package units;
 
 import java.util.LinkedHashMap;
 import server_battle.Battle;
 import server_battle.Position;
 
 public class GameUnits {
 	LinkedHashMap<Integer, Unit> unitList = new LinkedHashMap<Integer, Unit>();
 	UnitLocation unitLocation = new UnitLocation();
 	
 	public void CreateUnit(Unit unit, Position position) {
 		// should probably have mutex but #yolo
 		Battle.Log.logln("UnitStatus","Creating unit: " + unit.getName());
 		unitList.put(unit.uniqueId, unit);
 		unitLocation.AddUnit(unit.uniqueId, position);
		Battle.Log.logln("UnitStatus", "Check: " + unitList.containsKey(unit.uniqueId) + ", stuff: " + unitList.get(unit.uniqueId).getName());
 	}
 	
 	public void RemoveUnit (int uniqueId) {
 		// should probably have mutex but #yolo
 		Battle.Log.logln("UnitStatus","Removing unit: " + Integer.toString(uniqueId));
 		unitList.remove(uniqueId);
 		unitLocation.DeleteUnit(uniqueId);
 	}
 	
 	public void MoveUnit (int uniqueId, Position newPosition) {
 		Battle.Log.logln("UnitStatus","Moving unit: " + Integer.toString(uniqueId));
 		unitLocation.MoveUnit(uniqueId, newPosition);
 		//set unit location in unit class 
 		//unitList.get(unit.uniqueId).set(newPosition);
 	}
 	
 	public Unit GetUnit (Position newPosition) { // gets unit closest to position
 		Battle.Log.logln("UnitStatus","Looking up unit at position: " + newPosition.toString());
 		return null;
 	}
 	
 	public Unit GetUnit (int uniqueId) {
 		Battle.Log.logln("UnitStatus","Moving unit: " + Integer.toString(uniqueId));
 		return unitList.get(uniqueId);
 		
 	}
 }
