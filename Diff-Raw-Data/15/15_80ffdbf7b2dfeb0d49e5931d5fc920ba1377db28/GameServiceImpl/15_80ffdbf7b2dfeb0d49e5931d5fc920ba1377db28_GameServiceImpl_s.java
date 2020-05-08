 package lv.k2611a.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.annotation.PostConstruct;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 
 import lv.k2611a.domain.Building;
 import lv.k2611a.domain.BuildingType;
 import lv.k2611a.domain.Map;
 import lv.k2611a.domain.Tile;
 import lv.k2611a.domain.Unit;
 import lv.k2611a.domain.UnitType;
 import lv.k2611a.domain.goals.Move;
 import lv.k2611a.jmx.ServerMonitor;
 import lv.k2611a.network.BuildingDTO;
 import lv.k2611a.network.MapDTO;
 import lv.k2611a.network.TileDTO;
 import lv.k2611a.network.UnitDTO;
 import lv.k2611a.network.req.GameStateChanger;
 import lv.k2611a.network.resp.UpdateMap;
 import lv.k2611a.network.resp.UpdateMapIncremental;
 import lv.k2611a.util.MapGenerator;
 import lv.k2611a.util.Point;
 
 @Service
 @Scope("singleton")
 public class GameServiceImpl implements GameService {
 
     private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);
     public static final int TICK_LENGTH = 1000 / 20;
 
 
     @Autowired
     private SessionsService sessionsService;
 
     @Autowired
     private IdGeneratorService idGeneratorService;
 
     @Autowired
     private UserActionService userActionService;
 
     @Autowired
     private ServerMonitor serverMonitor;
 
     private volatile Map map = MapGenerator.generateMap(128, 128);
     private volatile long tickCount = 0;
 
     @PostConstruct
     public void init() {
 
         Building building = new Building();
         building.setId(idGeneratorService.generateBuildingId());
         building.setX(1);
         building.setY(110);
         building.setType(BuildingType.POWERPLANT);
         map.getBuildings().add(building);
 
         Random r = new Random();
         for (int j = 1; j < 10; j++) {
             for (int i = 0; i < 100; i++) {
                 Unit unit = new Unit();
                 unit.setId(idGeneratorService.generateUnitId());
                 unit.setX(j);
                 unit.setY(i);
                 unit.setGoal(new Move(100 + j, i));
                 unit.setUnitType(UnitType.values()[r.nextInt(UnitType.values().length)]);
                 map.getUnits().add(unit);
             }
         }
 
     }
 
     public Map getMap() {
         return map;
     }
 
     public void setMap(Map map) {
         this.map = map;
     }
 
     @Override
     public synchronized UpdateMap getFullMapUpdate() {
         MapDTO mapDTO = new MapDTO();
         List<TileDTO> tileDTOList = getMapTiles();
         List<UnitDTO> unitDTOList = getMapUnits();
         List<BuildingDTO> buildingDTOList = getBuildings();
 
         mapDTO.setWidth(map.getWidth());
         mapDTO.setHeight(map.getHeight());
         mapDTO.setTiles(tileDTOList.toArray(new TileDTO[tileDTOList.size()]));
         mapDTO.setUnits(unitDTOList.toArray(new UnitDTO[unitDTOList.size()]));
         mapDTO.setBuildings(buildingDTOList.toArray(new BuildingDTO[buildingDTOList.size()]));
         UpdateMap updateMap = new UpdateMap();
         updateMap.setMap(mapDTO);
         updateMap.setTickCount(tickCount);
         return updateMap;
     }
 
     private List<TileDTO> getMapTiles() {
         List<TileDTO> tileDTOList = new ArrayList<TileDTO>();
         for (int x = 0; x < map.getWidth(); x++) {
             for (int y = 0; y < map.getHeight(); y++) {
                 Tile tile = map.getTile(x, y);
                 tileDTOList.add(TileDTO.fromTile(tile));
             }
         }
         return tileDTOList;
     }
 
     private List<UnitDTO> getMapUnits() {
         List<UnitDTO> unitDTOList = new ArrayList<UnitDTO>();
         for (Unit unit : map.getUnits()) {
             unitDTOList.add(UnitDTO.fromUnit(unit));
         }
         return unitDTOList;
     }
 
     @Scheduled(fixedRate = TICK_LENGTH)
     public synchronized void tick() {
         long startTime = System.currentTimeMillis();
 
         tickCount++;
 
         processUserClicks(map);
         processGoals(map);
         sendIncrementalUpdate();
 
         long endTime = System.currentTimeMillis();
         long duration = endTime - startTime;
 
         if (duration > TICK_LENGTH) {
             log.warn("Server is lagging, tick duration exceeded " + TICK_LENGTH + ", was " + duration + " tick count " + tickCount);
         }
 
        //serverMonitor.reportTickTime(duration);
 
     }
 
     private void processUserClicks(Map map) {
         for (GameStateChanger gameStateChanger : userActionService.drainActions()) {
             gameStateChanger.changeGameState(map);
         }
     }
 
     private void sendIncrementalUpdate() {
         UpdateMapIncremental update = new UpdateMapIncremental();
         List<UnitDTO> mapUnits = getMapUnits();
         update.setUnits(mapUnits.toArray(new UnitDTO[mapUnits.size()]));
         List<BuildingDTO> buildingDTOList = getBuildings();
         update.setBuildings(buildingDTOList.toArray(new BuildingDTO[buildingDTOList.size()]));
         update.setTickCount(tickCount);
         sessionsService.sendUpdate(update);
 
     }
 
     private void processGoals(Map map) {
         map.clearUsageFlag();
         for (Building building : map.getBuildings()) {
             for (int x = 0; x < building.getType().getWidth(); x++) {
                 for (int y = 0; y < building.getType().getHeight(); y++) {
                     map.setUsed(x,y,-2);
                 }
             }
         }
         for (Unit unit : map.getUnits()) {
             map.setUsed(unit.getX(), unit.getY(), unit.getId());
             if (unit.getTicksMovingToNextCell() > 0) {
                 Point unitMovingTo = unit.getViewDirection().apply(new Point(unit.getX(), unit.getY()));
                 map.setUsed(unitMovingTo.getX(), unitMovingTo.getY(), unit.getId());
             }
         }
         for (Unit unit : map.getUnits()) {
             if (unit.getCurrentGoal() != null) {
                 unit.getCurrentGoal().process(unit, map);
             }
         }
     }
 
     public List<BuildingDTO> getBuildings() {
         List<BuildingDTO> buildingDTOList = new ArrayList<BuildingDTO>();
         for (Building building : map.getBuildings()) {
             buildingDTOList.add(BuildingDTO.fromBuilding(building));
         }
         return buildingDTOList;
     }
 
     @Override
     public long getTickCount() {
         return tickCount;
     }
 
     @Override
     public void setTickCount(long tickCount) {
         this.tickCount = tickCount;
     }
 }
