 package lv.k2611a.service.game;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.PreDestroy;
 
 import lv.k2611a.domain.unitgoals.Chase;
 import lv.k2611a.domain.unitgoals.Guard;
 import lv.k2611a.domain.unitgoals.Move;
 import lv.k2611a.domain.unitgoals.UnitGoal;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.context.annotation.Scope;
 import org.springframework.context.annotation.ScopedProxyMode;
 import org.springframework.stereotype.Service;
 
 import lv.k2611a.App;
 import lv.k2611a.ClientConnection;
 import lv.k2611a.domain.Building;
 import lv.k2611a.domain.BuildingType;
 import lv.k2611a.domain.Bullet;
 import lv.k2611a.domain.ConstructionOption;
 import lv.k2611a.domain.EntityType;
 import lv.k2611a.domain.Map;
 import lv.k2611a.domain.Player;
 import lv.k2611a.domain.RefineryEntrance;
 import lv.k2611a.domain.Tile;
 import lv.k2611a.domain.Unit;
 import lv.k2611a.domain.UnitType;
 import lv.k2611a.domain.buildinggoals.CreateBuilding;
 import lv.k2611a.domain.buildinggoals.CreateUnit;
 import lv.k2611a.jmx.ServerMonitor;
 import lv.k2611a.network.BuildingDTO;
 import lv.k2611a.network.BulletDTO;
 import lv.k2611a.network.MapDTO;
 import lv.k2611a.network.OptionDTO;
 import lv.k2611a.network.TileDTO;
 import lv.k2611a.network.TileWithCoordinatesDTO;
 import lv.k2611a.network.UnitDTO;
 import lv.k2611a.network.req.GameStateChanger;
 import lv.k2611a.network.resp.Lost;
 import lv.k2611a.network.resp.UpdateConstructionOptions;
 import lv.k2611a.network.resp.UpdateMap;
 import lv.k2611a.network.resp.UpdateMapIncremental;
 import lv.k2611a.network.resp.UpdateMoney;
 import lv.k2611a.service.connection.ConnectionState;
 import lv.k2611a.service.scope.ContextService;
 import lv.k2611a.service.scope.GameKey;
 import lv.k2611a.util.Point;
 
 @Service
 @Scope(value = "game", proxyMode = ScopedProxyMode.INTERFACES)
 public class GameServiceImpl implements GameService {
 
     private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);
     public static final int TICK_LENGTH = 1000 / 20;
     public static final int BUILDING_USAGE_FLAG = -2;
 
     @Autowired
     private AutowireCapableBeanFactory autowireCapableBeanFactory;
 
     @Autowired
     private GameSessionsService gameSessionsService;
 
     @Autowired
     private UserActionService userActionService;
 
     @Autowired
     private ServerMonitor serverMonitor;
 
     @Autowired
     private ContextService contextService;
 
     @Autowired
     private ConnectionState connectionState;
 
     private boolean testMode = false;
 
     public void setTestMode(boolean testMode) {
         this.testMode = testMode;
     }
 
     private ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
 
     private volatile Map map;
     private volatile long tickCount = 0;
 
     private Set<Point> changedTiles;
     private volatile boolean started = false;
 
     @Override
     public synchronized void start(Map map) {
         if (started) {
             return;
         }
         log.info("Initializing game");
         this.map = map;
         Runnable ticker = createTicker(contextService.getCurrentGameKey());
         if (!testMode) {
             exec.scheduleAtFixedRate(ticker, 0, TICK_LENGTH, TimeUnit.MILLISECONDS);
             log.info("Scheduler started");
         }
         started = true;
     }
 
     private Runnable createTicker(final GameKey currentContextKey) {
         return new Runnable() {
             @Override
             public void run() {
                 ContextService contextService = App.autowireCapableBeanFactory.getBean(ContextService.class);
                 try {
                     contextService.setGameKey(currentContextKey);
                     tick();
                 } catch (Exception e) {
                     log.error("Exception while processing tick", e);
                 } finally {
                     contextService.clearCurrentGameKey();
                 }
             }
         };
     }
 
     @PreDestroy
     public void destroy() {
         log.info("Destroying game");
         exec.shutdown();
         log.info("Scheduler stopped");
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
         List<BulletDTO> bulletDTOList = getBullets();
 
         mapDTO.setWidth((short) map.getWidth());
         mapDTO.setHeight((short) map.getHeight());
         mapDTO.setTiles(tileDTOList.toArray(new TileDTO[tileDTOList.size()]));
         mapDTO.setUnits(unitDTOList.toArray(new UnitDTO[unitDTOList.size()]));
         mapDTO.setBullets(bulletDTOList.toArray(new BulletDTO[bulletDTOList.size()]));
         mapDTO.setBuildings(buildingDTOList.toArray(new BuildingDTO[buildingDTOList.size()]));
 
         UpdateMap updateMap = new UpdateMap();
         updateMap.setMap(mapDTO);
         updateMap.setTickCount(tickCount);
         return updateMap;
     }
 
 
     @Override
     public synchronized boolean isOwner(int buildingId, int playerId) {
         Building building = map.getBuilding(buildingId);
         if (building == null) {
             return false;
         }
         return building.getOwnerId() == playerId;
     }
 
     @Override
     public synchronized boolean playerExist(int playerId) {
         if (map.getPlayerById(playerId) == null) {
             return false;
         }
         return true;
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
             if (unit != null) {
                 unitDTOList.add(UnitDTO.fromUnit(unit));
             }
         }
         return unitDTOList;
     }
 
     private List<BulletDTO> getBullets() {
         List<BulletDTO> bulletDTOList = new ArrayList<BulletDTO>();
         for (Bullet bullet : map.getBullets()) {
             bulletDTOList.add(BulletDTO.fromBullet(bullet));
         }
         return bulletDTOList;
     }
 
     public synchronized void tick() {
         long startTime = System.currentTimeMillis();
 
         tickCount++;
 
         changedTiles = new HashSet<Point>();
 
         fillPlayerBuildingTypes();
         mapFillTileUsage(map);
         updatePlayerElectricity();
 
         processUserClicks(map);
         processBuildingGoals(map);
         processUnitsGoals(map);
         processUnitReloads(map);
         processBullets(map);
         processPlayerStatus(map);
 
         sendIncrementalUpdate();
         sendAvailaibleConstructionOptionsUpdate();
         sendUpdateMoney();
 
         long endTime = System.currentTimeMillis();
         long duration = endTime - startTime;
 
         if (duration > TICK_LENGTH) {
             log.warn("Server is lagging, tick duration exceeded " + TICK_LENGTH + ", was " + duration + " tick count " + tickCount);
         }
 
         serverMonitor.reportTickTime(duration);
 
     }
 
     private void processPlayerStatus(Map map) {
         for (Player player : map.getPlayers()) {
             if (!player.hasLost()) {
                 if (map.getBuildingsByOwner(player.getId()).isEmpty()) {
                     player.setLost(true);
                     Lost lost = new Lost();
                     lost.setUsername("player : " + player.getId());
                     gameSessionsService.sendUpdate(lost);
                 }
             }
         }
     }
 
     private void processBullets(Map map) {
         List<Bullet> bulletsToRemove = new ArrayList<Bullet>();
         for (Bullet bullet : map.getBullets()) {
             if (bullet.getTicksToMove() > 0) {
                 bullet.setTicksToMove(bullet.getTicksToMove() - 1);
             } else {
                 Unit unit = map.getUnitAt(bullet.getGoalX(), bullet.getGoalY());
                 if (unit != null) {
                     unit.setHp(unit.getHp() - bullet.getDamageToDeal());
                     if (unit.getHp() <= 0) {
                         map.removeUnit(unit);
                     }
                 } else {
                     Building building = map.getBuildingAt(bullet.getGoalX(), bullet.getGoalY());
                     if (building != null) {
                         building.setHp(building.getHp() - bullet.getDamageToDeal());
                         if (building.getHp() <= 0) {
                             map.removeBuilding(building);
                         }
                     }
                 }
                 bulletsToRemove.add(bullet);
             }
         }
         map.removeBullets(bulletsToRemove);
     }
 
     private void processUnitReloads(Map map) {
         for (Unit unit : map.getUnits()) {
             if (unit.getTicksReloading() > 0) {
                 unit.setTicksReloading(unit.getTicksReloading() - 1);
             }
         }
     }
 
     private void fillPlayerBuildingTypes() {
         for (int playerId = 0; playerId < this.map.getPlayers().length; playerId++) {
             this.map.getPlayers()[playerId].getBuildingTypes().clear();
         }
         for (Building building : map.getBuildings()) {
             Player player = this.map.getPlayerById(building.getOwnerId());
             player.getBuildingTypes().add(building.getType());
         }
 
     }
 
     private void updatePlayerElectricity() {
         for (int playerId = 0; playerId < this.map.getPlayers().length; playerId++) {
             this.map.getPlayers()[playerId].setElectricity(getPlayerElectricity(map, playerId));
         }
     }
 
     private void sendUpdateMoney() {
         for (final ClientConnection clientConnection : gameSessionsService.getCurrentGameConnections()) {
             clientConnection.processInConnectionsContext(new Runnable() {
                 @Override
                 public void run() {
                     Integer playerId = connectionState.getPlayerId();
                     if (playerId != null) {
                         Player player = map.getPlayerById(playerId);
                         UpdateMoney updateMoney = new UpdateMoney();
                         updateMoney.setMoney((int) player.getMoney());
                         updateMoney.setElectricity((int) player.getElectricity());
                         clientConnection.sendMessage(updateMoney);
                     }
                 }
             });
 
         }
     }
 
     private long getPlayerElectricity(Map map, int playerId) {
         long delta = 0;
         for (Building building : map.getBuildingsByOwner(playerId)) {
             delta += building.getType().getElectricityDelta();
         }
         return delta;
     }
 
     private void sendAvailaibleConstructionOptionsUpdate() {
         for (ClientConnection clientConnection : gameSessionsService.getCurrentGameConnections()) {
             updateConstructionOptions(clientConnection);
         }
     }
 
     private void updateConstructionOptions(final ClientConnection clientConnection) {
         clientConnection.processInConnectionsContext(new Runnable() {
             @Override
             public void run() {
                 Integer selectedBuildingId = connectionState.getSelectedBuildingId();
                 Integer playerId = connectionState.getPlayerId();
                 updateConstructionOptions(clientConnection, selectedBuildingId, playerId);
             }
         });
 
     }
 
     private void updateConstructionOptions(ClientConnection clientConnection, Integer selectedBuildingId, Integer playerId) {
         if (selectedBuildingId != null) {
             Building building = map.getBuilding(selectedBuildingId);
             if (building == null) {
                 clientConnection.sendMessage(new UpdateConstructionOptions());
                 return;
             }
             EnumSet<ConstructionOption> constructionOptions = EnumSet.noneOf(ConstructionOption.class);
             if (!(building.isAwaitingClick() || building.getCurrentGoal() != null)) {
                 constructionOptions = building.getType().getConstructionOptions();
                 constructionOptions = filterAvailaibleConstructionOptions(constructionOptions, playerId);
             }
 
             List<OptionDTO> options = new ArrayList<OptionDTO>();
             for (ConstructionOption constructionOption : constructionOptions) {
                 OptionDTO option = OptionDTO.fromConstructionOption(constructionOption);
                 options.add(option);
             }
             UpdateConstructionOptions updateConstructionOptions = new UpdateConstructionOptions();
             updateConstructionOptions.setBuilderId(selectedBuildingId);
             if (building.getCurrentGoal() != null) {
                 if (building.getCurrentGoal() instanceof CreateBuilding) {
                     CreateBuilding createBuilding = (CreateBuilding) building.getCurrentGoal();
                     BuildingType buildingTypeBuilt = createBuilding.getBuildingType();
                     if (buildingTypeBuilt != null) {
                         updateConstructionOptions.setCurrentlyBuildingId(buildingTypeBuilt.getIdOnJS());
                         updateConstructionOptions.setCurrentlyBuildingOptionType(getConstructionOption(building.getType().getConstructionOptions(), buildingTypeBuilt));
                         double done = (double) building.getTicksAccumulated() / buildingTypeBuilt.getTicksToBuild();
                         byte percentsDone = (byte) Math.round(done * 100);
                         updateConstructionOptions.setPercentsDone(percentsDone);
                     }
                 }
                 if (building.getCurrentGoal() instanceof CreateUnit) {
                     CreateUnit createUnit = (CreateUnit) building.getCurrentGoal();
                     UnitType unitType = createUnit.getUnitType();
                     if (unitType != null) {
                         updateConstructionOptions.setCurrentlyBuildingId(unitType.getIdOnJS());
                         updateConstructionOptions.setCurrentlyBuildingOptionType(getConstructionOption(building.getType().getConstructionOptions(), unitType));
                         double done = (double) building.getTicksAccumulated() / unitType.getTicksToBuild();
                         byte percentsDone = (byte) Math.round(done * 100);
                         updateConstructionOptions.setPercentsDone(percentsDone);
                     }
                 }
             } else {
                 if (!(building.isAwaitingClick())) {
                     updateConstructionOptions.setReadyToBuild(true);
                 } else {
                     updateConstructionOptions.setCurrentlyBuildingId(building.getBuildingTypeBuilt().getIdOnJS());
                     updateConstructionOptions.setCurrentlyBuildingOptionType(getConstructionOption(building.getType().getConstructionOptions(), building.getBuildingTypeBuilt()));
                     updateConstructionOptions.setPercentsDone((byte) 100);
                 }
             }
             updateConstructionOptions.setOptions(options.toArray(new OptionDTO[options.size()]));
             clientConnection.sendMessage(updateConstructionOptions);
         } else {
             clientConnection.sendMessage(new UpdateConstructionOptions());
         }
     }
 
     private EnumSet<ConstructionOption> filterAvailaibleConstructionOptions(EnumSet<ConstructionOption> constructionOptions, int playerId) {
         Player player = map.getPlayerById(playerId);
         EnumSet<ConstructionOption> filtered = EnumSet.noneOf(ConstructionOption.class);
         for (ConstructionOption constructionOption : constructionOptions) {
             boolean prerequisitesFound = true;
             for (BuildingType buildingType : constructionOption.getPrerequisites()) {
                 if (!player.getBuildingTypes().contains(buildingType)) {
                     prerequisitesFound = false;
                     break;
                 }
             }
             if (prerequisitesFound) {
                 filtered.add(constructionOption);
             }
         }
         return filtered;
     }
 
     private byte getConstructionOption(EnumSet<ConstructionOption> constructionOptions, EntityType buildingTypeBuilt) {
         for (ConstructionOption constructionOption : constructionOptions) {
             if (constructionOption.getEntityToBuildIdOnJs() == buildingTypeBuilt.getIdOnJS()) {
                 return constructionOption.getIdOnJS();
             }
         }
         return -1;
     }
 
     private void processBuildingGoals(Map map) {
         for (Building building : map.getBuildings()) {
             try {
                 if (building.getCurrentGoal() != null) {
                     building.getCurrentGoal().process(building, map, this.tickCount);
                 }
             } catch (RuntimeException e) {
                 log.error("Exception while processing building goal", e);
             }
         }
     }
 
     private void processUserClicks(Map map) {
         for (GameStateChanger gameStateChanger : userActionService.drainActions()) {
             try {
                 autowireCapableBeanFactory.autowireBean(gameStateChanger);
                 gameStateChanger.changeGameState(map);
             } catch (RuntimeException e) {
                 log.error("Exception while processing user action", e);
             }
         }
     }
 
     @Override
     public void registerChangedTile(Point point) {
         changedTiles.add(point);
     }
 
     private void sendIncrementalUpdate() {
         UpdateMapIncremental update = new UpdateMapIncremental();
 
         List<UnitDTO> mapUnits = getMapUnits();
         update.setUnits(mapUnits.toArray(new UnitDTO[mapUnits.size()]));
 
         List<BuildingDTO> buildingDTOList = getBuildings();
         update.setBuildings(buildingDTOList.toArray(new BuildingDTO[buildingDTOList.size()]));
 
         List<TileWithCoordinatesDTO> tileDTOList = getChangedTiles();
         update.setChangedTiles(tileDTOList.toArray(new TileWithCoordinatesDTO[tileDTOList.size()]));
 
         List<BulletDTO> bulletDTOList = getBullets();
         update.setBullets(bulletDTOList.toArray(new BulletDTO[bulletDTOList.size()]));
 
         update.setTickCount(tickCount);
         gameSessionsService.sendUpdate(update);
 
     }
 
     private List<TileWithCoordinatesDTO> getChangedTiles() {
         List<TileWithCoordinatesDTO> result = new ArrayList<TileWithCoordinatesDTO>();
         for (Point changedTile : this.changedTiles) {
             TileWithCoordinatesDTO tile = TileWithCoordinatesDTO.fromTile(map.getTile(changedTile));
             result.add(tile);
         }
         return result;
     }
 
     private void processUnitsGoals(Map map) {
         for (Unit unit : map.getUnits()) {
             try {
                 if (unit.getCurrentGoal() != null) {
                     unit.getCurrentGoal().process(unit, map, this);
                 }
             } catch (Exception e) {
                 log.error("Exception while processing unit goal", e);
             }
         }
     }
 
     private void mapFillTileUsage(Map map) {
         map.clearUsageFlag();
         map.getRefineryEntranceList().clear();
         for (Building building : map.getBuildings()) {
             for (int x = 0; x < building.getType().getWidth(); x++) {
                 for (int y = 0; y < building.getType().getHeight(); y++) {
                     map.setUsedByBuilding(x + building.getX(), y + building.getY(), building.getId());
                 }
             }
         }
         for (Building building : map.getBuildingsByType(BuildingType.REFINERY)) {
             if (map.isPassable(building.getX(), building.getY() + 2)
                 || map.isPassable(building.getX() + 1, building.getY() + 2)
                 || map.isPassable(building.getX() + 2, building.getY() + 2)) {
                 Point point = building.getPoint();
                 point = new Point(point.getX() + 1, point.getY() + 1);
                 RefineryEntrance refineryEntrance = new RefineryEntrance(building.getOwnerId(), point, building.getId());
                 map.getRefineryEntranceList().put(point, refineryEntrance);
             }
         }
 
         for (Unit unit : map.getUnits()) {
             UnitGoal unitGoal = unit.getCurrentGoal();
             if (unitGoal == null) {
                 log.error("WE HAEV NO GOAL!");
                 unit.insertGoalBeforeCurrent(new Guard());
             }
             unit.getCurrentGoal().reserveTiles(unit, map);
         }
     }
 
     private List<BuildingDTO> getBuildings() {
         List<BuildingDTO> buildingDTOList = new ArrayList<BuildingDTO>();
         for (Building building : map.getBuildings()) {
             buildingDTOList.add(BuildingDTO.fromBuilding(building));
         }
         return buildingDTOList;
     }
 
     @Override
     public synchronized long getTickCount() {
         return tickCount;
     }
 
     @Override
     public synchronized void setTickCount(long tickCount) {
         this.tickCount = tickCount;
     }
 
     @Override
     public synchronized Integer getFreePlayer() {
         for (Player player : map.getPlayers()) {
             if (!player.isUsed()) {
                 player.setUsed(true);
                 return player.getId();
             }
         }
         return null;
     }
 
     @Override
     public synchronized void freePlayer(Integer playerId) {
         map.getPlayerById(playerId).setUsed(false);
     }
 
     @Override
     public synchronized List<Player> getPlayers() {
         return Arrays.asList(map.getPlayers());
     }
 }
