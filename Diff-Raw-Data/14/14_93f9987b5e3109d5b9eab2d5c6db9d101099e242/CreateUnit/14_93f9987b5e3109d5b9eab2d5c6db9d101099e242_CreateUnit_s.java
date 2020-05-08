 package lv.k2611a.domain.buildinggoals;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import lv.k2611a.domain.Building;
 import lv.k2611a.domain.Map;
 import lv.k2611a.domain.Player;
 import lv.k2611a.domain.Tile;
 import lv.k2611a.domain.Unit;
 import lv.k2611a.domain.UnitType;
 import lv.k2611a.domain.ViewDirection;
 
 public class CreateUnit implements BuildingGoal {
 
     private static final Logger log = LoggerFactory.getLogger(CreateUnit.class);
 
     private UnitType unitType;
 
     public UnitType getUnitType() {
         return unitType;
     }
 
     public void setUnitType(UnitType unitType) {
         this.unitType = unitType;
     }
 
     @Override
     public void process(Building building, Map map, long tickCount) {
         Player player = map.getPlayerById(building.getOwnerId());
 
         if (building.getTicksAccumulated() >= unitType.getTicksToBuild()) {
             if (placeUnit(map, building)) {
                 building.setTicksAccumulated(0);
                 building.removeGoal(this);
             }
         } else {
             if (player.getElectricity() < 0) {
                 // throttle 3/4 of ticks, if not enough electricity
                 if (tickCount % 4 != 3) {
                     return;
                 }
             }
 
             if (player.getMoney() >= unitType.getCostPerTick()) {
                 player.setMoney(player.getMoney() - unitType.getCostPerTick());
                 building.setTicksAccumulated(building.getTicksAccumulated() + 1);
                 if (building.getTicksAccumulated() >= unitType.getTicksToBuild()) {
                     if (placeUnit(map, building)) {
                         building.setTicksAccumulated(0);
                         building.removeGoal(this);
                     }
                 }
             }
         }
     }
 
     private boolean placeUnit(Map map, Building building) {
         int minX = building.getX() - 1;
         int maxX = building.getX() + building.getType().getWidth();
         int minY = building.getY() - 1;
         int maxY = building.getY() + building.getType().getHeight();
 
         Tile freeTile = map.getNearestFreeTileForUnitPlacement(minX, maxX, minY, maxY);
         if (freeTile == null) {
             return false;
         }
        log.warn("BUILDING: " + String.valueOf(building.getX()) + " " + String.valueOf(building.getY()));
        log.warn("X: " + String.valueOf(minX) + " " + String.valueOf(maxX));
        log.warn("Y: " + String.valueOf(minY) + " " + String.valueOf(maxY));
        log.warn("U: " + String.valueOf(freeTile.getX()) + " " + String.valueOf(freeTile.getY()));
         Unit unit = new Unit();
         unit.setOwnerId(building.getOwnerId());
 
         unit.setX(freeTile.getX());
         unit.setY(freeTile.getY());
         unit.setUnitType(unitType);
         unit.setViewDirection(ViewDirection.BOTTOM);
         map.addUnit(unit);
 
         return true;
     }
 }
