 package swag49.web.controller;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.client.RestTemplate;
 import swag49.dao.DataAccessObject;
 import swag49.dao.TileDAO;
import swag49.gamelogic.MapLogic;
import swag49.gamelogic.exceptions.NotEnoughMoneyException;
 import swag49.model.*;
 import swag49.model.ResourceType;
 import swag49.model.helper.ResourceValueHelper;
 import swag49.transfer.model.*;
 import swag49.transfer.model.TroopDTO;
 import swag49.util.Log;
 import swag49.web.MapHelper;
 import swag49.web.model.*;
 
 import javax.annotation.Nullable;
 import javax.annotation.PostConstruct;
 import javax.validation.Valid;
 import java.util.*;
 import java.util.Map;
 
 @Controller
 @Scope(value = "session")
 @RequestMapping(value = "/map")
 public class MapController {
 
     private static final String userManagement = "http://localhost:8080/user/swag";
     private static final String tokenService = "/token/";
 
     private static final int VIEWSIZE = 3;
 
     @Log
     private static Logger logger;
 
     @Autowired
     @Qualifier("mapDAO")
     private DataAccessObject<swag49.model.Map, Long> mapDAO;
 
     @Autowired
     @Qualifier("troopActionDAO")
     private DataAccessObject<TroopAction, Long> troopActionDAO;
 
 
     @Autowired
     @Qualifier("buildActionDAO")
     private DataAccessObject<BuildAction, Long> buildActionDAO;
 
     @Autowired
     @Qualifier("troopUpgradeActionDAO")
     private DataAccessObject<TroopUpgradeAction, Long> troopUpgradeActionDAO;
 
     @Autowired
     @Qualifier("playerDAO")
     private DataAccessObject<Player, Long> playerDAO;
 
     @Autowired
     @Qualifier("tileDAO")
     private DataAccessObject<Tile, Tile.Id> tileDAO;
 
     @Autowired
     @Qualifier("buildingTypeDAO")
     private DataAccessObject<BuildingType, Long> buildingTypeDAO;
 
 
     @Autowired
     @Qualifier("buildingLevelDAO")
     private DataAccessObject<BuildingLevel, BuildingLevel.Id> buildingLevelDAO;
 
     @Autowired
     @Qualifier("squareDAO")
     private DataAccessObject<Square, Square.Id> squareDAO;
 
     @Autowired
     @Qualifier("baseDAO")
     private DataAccessObject<Base, Long> baseDAO;
 
     @Autowired
     @Qualifier("troopTypeDAO")
     private DataAccessObject<TroopType, Long> troopTypeDAO;
 
     @Autowired
     @Qualifier("troopLevelDAO")
     private DataAccessObject<TroopLevel, TroopLevel.Id> troopLevelDAO;
 
     @Autowired
     @Qualifier("troopDAO")
     private DataAccessObject<Troop, Long> troopDAO;
 
 
     @Autowired
     @Qualifier("buildingDAO")
     private DataAccessObject<Building, Square.Id> buildingDAO;
 
     @Autowired
     private RestTemplate restTemplate;
 
     @Autowired
     private NodeContext nodeContext;
 
     @Autowired
     private MapLogic mapLogic;
 
     private UUID userToken;
     private String userID;
     private String userName;
     private static final String NOTENOUGHRESOURCES = "notenoughmoney";
     private static final String ERROR = "error";
 
     //TODO: GET MAP AND PLAYER
     private swag49.model.Map map;
     private Player player;
 
     @PostConstruct
     @Transactional("swag49.map")
     public void init() {
         swag49.model.Map example = new swag49.model.Map();
         example.setUrl(nodeContext.getMapNodeUrl());
         logger.error("Map url {}", nodeContext.getMapNodeUrl());
 
         Collection<swag49.model.Map> maps = mapDAO.queryByExample(example);
         if (maps != null && maps.size() == 1) {
             map = maps.iterator().next();
             logger.debug("Map with id " + map.getId() + " found");
         } else {
             logger.error("Error while finding map");
         }
     }
 
     @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
     @Transactional("swag49.map")
     public String initPlayer(@ModelAttribute("tokenDTO") TokenDTO token) {
         System.out.println("Got request with token: " + token);
 
         UUID userToken = token.getToken();
 
         // verify token
         Map<String, UUID> vars = new HashMap<String, UUID>();
         vars.put("token", userToken);
         TokenDTO tokenDTO = restTemplate.getForObject(userManagement + tokenService + "{token}",
                 TokenDTO.class, vars);
 
         System.out.println("Got TokenDTO: " + tokenDTO);
 
         if (tokenDTO != null) {
             this.userToken = tokenDTO.getToken();
             this.userID = tokenDTO.getUserId();
             this.userName = tokenDTO.getUserName();
         }
 
         if (map != null) {
             //test if a player for that user exists
             Player example = new Player();
             example.setPlays(map);
 
             example.setUserId(this.userID);
             Collection<Player> playerValues = playerDAO.queryByExample(example);
 
             if (playerValues != null && playerValues.size() == 1) {
                 player = playerValues.iterator().next();
                 player.setOnline(true);
                 player = playerDAO.update(player);
                 logger.info("Player " + player.getId() + " found");
             } else if (playerValues != null && playerValues.size() == 0) {
                 // create new player & create start conditions ( map, resources, units, etc)
 
                 player = new Player();
                 player.setUserId(userID);
                 player.setPlays(map);
                 player.setOnline(true);
                 player.setDeleted(false);
                 player.setResources(new ResourceValue());
                 player.setIncome(new ResourceValue());
                 player.setUpkeep(new ResourceValue());
                 player = playerDAO.create(player);
 
                 Base base = mapLogic.initializePlayer(map, player);
 
                 //create a start unit
 
                 TroopType type = new TroopType();
                 type.setName("Swordsman");
                 type = troopTypeDAO.queryByExample(type).get(0);
 
 
                 TroopLevel.Id levelId = new TroopLevel.Id(1, type.getId());
                 TroopLevel level = troopLevelDAO.get(levelId);
 
                 Tile tile = base.getLocatedOn();
                 Troop startUnit = new Troop(type, level, tile, player);
 
                 startUnit = troopDAO.create(startUnit);
 
                 tile.getTroops().add(startUnit);
                 tileDAO.update(tile);
 
                 //create additional start units
 
                 startUnit = new Troop(type, level, tile, player);
 
                 startUnit = troopDAO.create(startUnit);
 
                 tile.getTroops().add(startUnit);
 
                 startUnit = troopDAO.create(startUnit);
                 tile.getTroops().add(startUnit);
 
                 startUnit = troopDAO.create(startUnit);
                 tile.getTroops().add(startUnit);
 
                 tileDAO.update(tile);
 
 
                 logger.info("Player " + player.getId() + " initialized");
             } else {
                 //THIS SHOULD NEVER HAPPEN
                 //TODO: fehlerfall gescheit behandeln
                 return "HELP";
             }
         }
         return "redirect:./";
     }
 
     @Transactional("swag49.map")
     public String buildTest() {
         try {
             logger.info("Start build Test");
             player = playerDAO.get(player.getId());
             Base base = player.getOwns().iterator().next();
 
             BuildingType goldMineType = new BuildingType();
             goldMineType.setName("Goldmine");
 
 
             goldMineType = buildingTypeDAO.queryByExample(goldMineType).get(0);
             Square emptySquare = null;
             for (Square square : base.getConsistsOf()) {
                 if (square.getBuilding() == null) {
                     emptySquare = square;
                     break;
                 }
             }
             logger.info("End build Test");
 
             mapLogic.build(emptySquare, goldMineType);
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         return "home";
     }

     @RequestMapping(value = "/messaging", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String messaging() {
         //buildTest();
         return "redirect:../messaging/";
     }
 
     @RequestMapping(value = "/statistics", method = RequestMethod.GET)
     public String statistics() {
         return "redirect:../statistics/";
     }
 
     @RequestMapping(value = "/sendtroops", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String getSendTroopsOverview(@RequestParam(value = "x", defaultValue = "-1") int x,
                                         @RequestParam(value = "y", defaultValue = "-1") int y,
                                         Model model) {
         //TODO: besser machen
         player = playerDAO.get(player.getId());
         map = mapDAO.get(map.getId());
 
         Troop sampleTroop = new Troop();
         sampleTroop.setOwner(player);
         List<Troop> troops = troopDAO.queryByExample(sampleTroop);
 
         //HashMap<Tile.Id, ArrayList<Troop>> troopsPerTile = Maps.newHashMap();
         ArrayList<TileDTO> tileList = new ArrayList<TileDTO>();
 
         for (Troop troop : troops) {
             Tile tile = troop.getPosition();
             SendTroopDTO troopDTO = new SendTroopDTO();
             troopDTO.setSendMe(false);
             troopDTO.setId(troop.getId());
             troopDTO.setName(troop.getType().getName());
 
             boolean existed = false;
             for (TileDTO exTile : tileList) {
                 if (exTile.equals(exTile)) {
                     exTile.getTroops().add(troopDTO);
                     existed = true;
                 }
             }
             if (existed == false) {
                 TileDTO newTile = new TileDTO();
                 newTile.setMapId(map.getId());
                 newTile.setX(tile.getId().getX());
                 newTile.setY(tile.getId().getY());
                 ArrayList<SendTroopDTO> sendTroops = new ArrayList<SendTroopDTO>();
                 sendTroops.add(troopDTO);
                 newTile.setTroops(sendTroops);
                 tileList.add(newTile);
             }
 
 //            if (troopsPerTile.containsKey(tile.getId())) {
 //                ArrayList<Troop> tmpTroops = troopsPerTile.get(tile.getId());
 //                tmpTroops.add(troop);
 //                troopsPerTile.put(tile.getId(), tmpTroops);
 //            } else {
 //                ArrayList<Troop> tmpTroops = new ArrayList<Troop>();
 //                tmpTroops.add(troop);
 //                troopsPerTile.put(tile.getId(), tmpTroops);
 //            }
         }
 
 //        TroopsPerTileDTO dto = new TroopsPerTileDTO();
 //        dto.setTroopsPerTile(troopsPerTile);
 
         TroopsPerTileDTO dto = new TroopsPerTileDTO();
         dto.setTileList(tileList);
         dto.setX(x);
         dto.setY(y);
         model.addAttribute("troopsPerTile", dto);
 
 //        model.addAttribute("troopsPerTile", tileList);
 
         return "sendtroops";
     }
 
     @RequestMapping(value = "/sendTroops", method = RequestMethod.POST)
     public String handleSendTroops(@ModelAttribute("troopsPerTile") TroopsPerTileDTO troopsPerTile, BindingResult bingBindingResult,
                                    Map<String, Object> modelMap) {
 
         //TODO: besser machen
         player = playerDAO.get(player.getId());
         map = mapDAO.get(map.getId());
 
         if (bingBindingResult.hasErrors()) {
             modelMap.put("view", false);
             return "redirect:../map";
         }
 
         Tile tile = tileDAO.get(new Tile.Id(map.getId(), troopsPerTile.getX(), troopsPerTile.getY()));
         HashSet<Troop> troopsToSend = new HashSet<Troop>();
 
         for(TileDTO tileDto : troopsPerTile.getTileList())
         {
             for(SendTroopDTO sendTroopDto : tileDto.getTroops())
             {
                 Troop sendTroop = troopDAO.get(sendTroopDto.getId());
                 troopsToSend.add(sendTroop);
             }
         }
         try {
             mapLogic.sendTroops(tile, troopsToSend);
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         return "redirect:../map";
     }
 
 
     @RequestMapping(value = "/tile", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String getTileOverview(@RequestParam(value = "x", defaultValue = "-1") int x,
                                   @RequestParam(value = "y", defaultValue = "-1") int y,
                                   Model model) {
         //TODO: besser machen
         player = playerDAO.get(player.getId());
         map = mapDAO.get(map.getId());
 
         Tile tile = new Tile(map, x, y);
 
         tile = tileDAO.get(tile.getId());
 
         TileOverviewDTOFull tileInfo = new TileOverviewDTOFull(tile.getId().getX(), tile.getId().getY());
         tileInfo.setBase(tile.getBase());
         tileInfo.setTroops(Sets.<TroopDTO>newHashSet(Collections2.transform(tile.getTroops(),
                 new Function<Troop, TroopDTO>() {
                     @Override
                     public TroopDTO apply(@Nullable Troop input) {
                         if (input == null) return null;
 
                         return new TroopDTO(input.getType().getName(), input.getIsOfLevel().getLevel(),
                                 input.getIsOfLevel().getStrength(), input.getIsOfLevel().getDefense(),
                                 input.getIsOfLevel().getSpeed(), input.getIsOfLevel().getCargo_capacity(),
                                 input.getId(), input.getActive());
                     }
                 })));
 
         if (tile.getBase() != null) {
 //            tileInfo.setHasBase(true);
 
             Base base = tile.getBase();
             if (base.getOwner().getUserId().equals(player.getUserId())) {
                 tileInfo.setEnemyTerritory(false);
                 tileInfo.setSquares(Sets.newHashSet(base.getConsistsOf()));
             } else {
                 tileInfo.setEnemyTerritory(true);
             }
 
             tileInfo.setBaseId(base.getId());
         } else {
 //              tileInfo.setHasBase(false);
             tileInfo.setEnemyTerritory(false);
         }
 
 
         if (tile.getTroops() != null) {
 //            if(!tile.getTroops().isEmpty())
 //                tileInfo.setHasTroops(true);
 //            else
 //                tileInfo.setHasTroops(false);
         } else {
 //            tileInfo.setHasTroops(true);
         }
 
         ResourceType specialResource = tile.getSpecial();
         if (specialResource == null)
             specialResource = ResourceType.NONE;
         String specialResourceString = specialResource.toString();
         tileInfo.setSpecialResource(specialResourceString);
 
         model.addAttribute("tileInfo", tileInfo);
 
         return "tile";
     }
 
 
     public UUID getUserToken() {
         return userToken;
     }
 
     public String getUserID() {
         return userID;
     }
 
     public String getUserName() {
         return userName;
     }
 
     //TEST
     @RequestMapping(value = "/buildingupgrade", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String getUpgradeBuilding(@RequestParam(value = "baseId", defaultValue = "-1") long baseId,
                                      @RequestParam(value = "position", defaultValue = "-1") int position,
                                      Model model) {
         player = playerDAO.get(player.getId());
         Building building = buildingDAO.get(new Square.Id(baseId, position));
         if (building != null) {
             try {
                 mapLogic.upgradeBuilding(building);
             } catch (NotEnoughMoneyException e) {
                 return NOTENOUGHRESOURCES;
             } catch (Exception e) {
                 logger.error("Error during /buildingupgrade", e);
                 return ERROR;
             }
 
             return "troopoverview";
         } else {
             return "error";
         }
     }
 
     @RequestMapping(value = "/troopupgrade", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String getUpgradeTroops(@RequestParam(value = "troopId", defaultValue = "-1") long troopId,
                                    Model model) {
         player = playerDAO.get(player.getId());
         Troop troop = troopDAO.get(troopId);
         if (troop != null) {
             //get next Level
             TroopLevel.Id id = new TroopLevel.Id(troop.getIsOfLevel().getLevel(), troop.getType().getId());
 
             TroopLevel nextLevel = troopLevelDAO.get(id);
             if (!ResourceValueHelper.geq(player.getResources(), nextLevel.getBuildCosts())) {
                 return NOTENOUGHRESOURCES;
             }
 
             try {
                 mapLogic.upgradeTroop(player, troop, nextLevel);
             } catch (Exception e) {
                 logger.error("Error during /troopupgrade", e);
                 return ERROR;
             }
 
             return "troopoverview";
         } else {
             return ERROR;
         }
     }
 
     @RequestMapping(value = "/traintroops", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String getTrainTroopOverview(@RequestParam(value = "baseId", defaultValue = "-1") long baseId,
                                         Model model) {
 
         Base base = baseDAO.get(baseId);
         if (base == null || !base.getOwner().getId().equals(player.getId())) {
             return ERROR;
         }
 
         player = playerDAO.get(player.getId());
         map = mapDAO.get(map.getId());
 
         List<TroopType> troopTypes = troopTypeDAO.queryByExample(new TroopType());
 
         List<TroopTypeDTO> troopTypeDTOList = new ArrayList<TroopTypeDTO>();
 
         for (TroopType type : troopTypes) {
             TroopTypeDTO dto = new TroopTypeDTO(type.getName(), type.getCanFoundBase(), null, type.getId());
 
             ResourceValueDTO costs = null;
             for (TroopLevel level : type.getLevels()) {
                 if (level.getLevel() == 1) {
                     costs = new ResourceValueDTO(level.getBuildCosts().getAmount_gold(),
                             level.getBuildCosts().getAmount_wood(), level.getBuildCosts().getAmount_stone(),
                             level.getBuildCosts().getAmount_crops());
                     break;
                 }
             }
 
             dto.setCosts(costs);
 
             troopTypeDTOList.add(dto);
         }
 
         model.addAttribute("troops", troopTypeDTOList);
         model.addAttribute("baseId", baseId);
         return "traintroops";
     }
 
 
     @RequestMapping(value = "/train", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String getTrainTroopOverview(@RequestParam(value = "baseId", defaultValue = "-1") long baseId,
                                         @RequestParam(value = "troopTypeId", defaultValue = "-1") long troopTypeId,
                                         Model model) {
 
         Base base = baseDAO.get(baseId);
         if (base == null || !base.getOwner().getId().equals(player.getId())) {
             return ERROR;
         }
 
         TroopType type = new TroopType();
         type.setId(troopTypeId);
         List<TroopType> typeList = troopTypeDAO.queryByExample(type);
         if (typeList == null || typeList.size() != 1) {
             return ERROR;
         } else {
             type = typeList.get(0);
         }
         player = playerDAO.get(player.getId());
         map = mapDAO.get(map.getId());
 
         TroopLevel.Id id = new TroopLevel.Id(1, type.getId());
         TroopLevel level = troopLevelDAO.get(id);
 
         if (!ResourceValueHelper.geq(player.getResources(), level.getBuildCosts())) {
             return NOTENOUGHRESOURCES;
         }
 
         try {
             mapLogic.buildTroop(player, type, level, base.getLocatedOn(), 1);
         } catch (NotEnoughMoneyException e) {
             logger.error("Error during train", e);
             return ERROR;
         }
 
         return "traintroops";
     }
 
     @RequestMapping(value = "/actions", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String getActionOverview(Model model) {
         Date now = new Date();
 
         player = playerDAO.get(player.getId());
 
         //troop actions
         TroopAction troopAction = new TroopAction();
         troopAction.setPlayer(player);
         List<TroopAction> troopActionsList = troopActionDAO.queryByExample(troopAction);
 
         List<TroopActionDTO> troopActionDTOList = new ArrayList<TroopActionDTO>();
 
         for (TroopAction action : troopActionsList) {
             if (action.getEndDate().after(now)) {
                 TroopActionDTO dto = new TroopActionDTO();
 
                 dto.setDestionation(action.getSource().getId().getX(), action.getSource().getId().getX());
                 dto.setIsAbortable(action.getIsAbortable());
                 dto.setStartDate(action.getStartDate());
                 dto.setEndDate(action.getEndDate());
                 dto.setId(action.getId());
 
                 troopActionDTOList.add(dto);
             }
         }
 
 
         //troop upgrades
         TroopUpgradeAction troopUpgradeAction = new TroopUpgradeAction();
         troopUpgradeAction.setPlayer(player);
         List<TroopUpgradeAction> troopUpgradeActionsList = troopUpgradeActionDAO.queryByExample(troopUpgradeAction);
 
         List<TroopUpgradeActionDTO> troopUpgradeActionDTOList = new ArrayList<TroopUpgradeActionDTO>();
 
         for (TroopUpgradeAction action : troopUpgradeActionsList) {
             if (action.getEndDate().after(now)) {
                 TroopUpgradeActionDTO dto = new TroopUpgradeActionDTO();
                 dto.setDestination_x(action.getTarget().getId().getX());
                 dto.setDestination_y(action.getTarget().getId().getY());
                 dto.setAbortable(action.getIsAbortable());
                 dto.setTroopName(action.getTroop().getType().getName());
                 dto.setLevel(action.getTroopLevel().getLevel());
                 dto.setId(action.getId());
 
                 troopUpgradeActionDTOList.add(dto);
             }
         }
 
         //buildings
         BuildAction buildAction = new BuildAction();
         buildAction.setPlayer(player);
         List<BuildAction> buildActionList = buildActionDAO.queryByExample(buildAction);
 
         List<BuildActionDTO> buildActionDTOList = new ArrayList<BuildActionDTO>();
 
         for (BuildAction action : buildActionList) {
             if (action.getEndDate().after(now)) {
                 BuildActionDTO dto = new BuildActionDTO();
 
                 dto.setDestination_x(action.getTarget().getId().getX());
                 dto.setDestination_y(action.getTarget().getId().getY());
                 dto.setIsAbortable(action.getIsAbortable());
                 dto.setBuildingName(action.getConcerns().getType().getName());
                 dto.setLevel(action.getConcerns().getIsOfLevel().getLevel() + 1);
                 dto.setSquareId(action.getConcerns().getSquare().getId().getPosition());
                 dto.setEndDate(action.getEndDate());
                 dto.setId(action.getId());
 
                 buildActionDTOList.add(dto);
             }
         }
 
         model.addAttribute("troopActions", troopActionDTOList);
 
         model.addAttribute("troopUpgradeActions", troopUpgradeActionDTOList);
 
         model.addAttribute("baseActions", buildActionDTOList);
 
 
         return "actions";
     }
 
     @RequestMapping(value = "/troopoverview", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String getTroopOverview(@RequestParam(value = "baseId", defaultValue = "-1") long baseId,
                                    Model model) {
         player = playerDAO.get(player.getId());
         map = mapDAO.get(map.getId());
         Base base = baseDAO.get(baseId);
 
         if (base == null || !base.getOwner().getId().equals(player.getId())) {
             return ERROR;
         }
 
 
         ArrayList<TroopDTO> troops = new ArrayList<TroopDTO>();
 
         for (Troop troop : base.getLocatedOn().getTroops()) {
             TroopDTO dto = new TroopDTO(troop.getType().getName(), troop.getIsOfLevel().getLevel(),
                     troop.getIsOfLevel().getStrength(), troop.getIsOfLevel().getDefense(),
                     troop.getIsOfLevel().getSpeed(), troop.getIsOfLevel().getCargo_capacity(), troop.getId(),
                     troop.getActive());
 
             //set upgrade
             //get current TroopTypeLevel
             TroopLevel currentLevel = troop.getIsOfLevel();
 
             //get next Level
             TroopLevel.Id id = new TroopLevel.Id(currentLevel.getLevel(), currentLevel.getId().getTroopTypeId());
 
             TroopLevel nextLevel = troopLevelDAO.get(id);
 
             if (nextLevel == null || troop.getActive() != Boolean.TRUE)
                 dto.setCanUpgrade(false);
             else {
                 dto.setCanUpgrade(true);
                 dto.setUpgradeCost(new ResourceValueDTO(nextLevel.getBuildCosts().getAmount_gold(),
                         nextLevel.getBuildCosts().getAmount_wood(), nextLevel.getBuildCosts().getAmount_stone(),
                         nextLevel.getBuildCosts().getAmount_crops()));
             }
 
             troops.add(dto);
         }
 
         model.addAttribute("troops", troops);
         model.addAttribute("baseId", baseId);
 
         return "troopoverview";
     }
 
     @RequestMapping(value = "/build", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String getBuildView(@RequestParam(value = "baseId", defaultValue = "-1") long baseId,
                                @RequestParam(value = "position", defaultValue = "-1") int position,
                                @RequestParam(value = "buildingTypeId", defaultValue = "-1") long buildingTypeId,
                                Model model) {
         //TODO: besser machen
         player = playerDAO.get(player.getId());
         map = mapDAO.get(map.getId());
         List<BuildingType> buildings = buildingTypeDAO.queryByExample(new BuildingType());
 
         if (buildingTypeId != -1 && position != -1 && baseId != -1) {
             Base base = baseDAO.get(baseId);
             Square square = squareDAO.get(new Square.Id(base.getId(), position));
             BuildingType buildingType = buildingTypeDAO.get(buildingTypeId);
 
             try {
                 mapLogic.build(square, buildingType);
             } catch (Exception e) {
                 logger.error("Error during build", e);
                 return ERROR;
             }
 
             return "buildSuccess";
         }
 
 //        ResourceValue resources = player.getResources();
 
         ArrayList<BuildingTypeDTO> availableBuildings = new ArrayList<BuildingTypeDTO>();
 
         for (BuildingType building : buildings) {
             BuildingTypeDTO buildingType = new BuildingTypeDTO(building.getId(), building.getName());
 
 
             Set<BuildingLevel> levels = building.getLevels();
 
             ResourceValue costs = new ResourceValue();
             for (BuildingLevel level : levels) {
                 if (level.getLevel() == 1) {
                     costs = level.getBuildCosts();
                 }
             }
 
             buildingType.setCosts(
                     new ResourceValueDTO(costs.getAmount_gold(), costs.getAmount_wood(), costs.getAmount_stone(),
                             costs.getAmount_crops()));
             availableBuildings.add(buildingType);
         }
 
         model.addAttribute("buildings", availableBuildings);
         model.addAttribute("baseId", baseId);
         model.addAttribute("position", position);
 
         return "build";
     }
 
 
     @RequestMapping(value = "/", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String getHomeview(@RequestParam(value = "xLow", defaultValue = "-1") int x_low,
                               @RequestParam(value = "yLow", defaultValue = "-1") int y_low,
                               @RequestParam(value = "xHigh", defaultValue = "-1") int x_high,
                               @RequestParam(value = "yHigh", defaultValue = "-1") int y_high, Model model,
                               Map<String, Object> tempMap) {
 
         tempMap.put("userID", this.userID);
 
         if(userID == null) {
             tempMap.put("userNode", nodeContext.getUserNodeUrl() + "/swag/user/");
             return "home";
         }
 
         //TODO: besser machen
         player = playerDAO.get(player.getId());
         map = mapDAO.get(map.getId());
 
 
         //default values
         if (x_low == x_high && y_low == y_high && y_low == x_low && x_low == -1) {
             //focus on home base
             Base homeBase = null;
             if (player.getOwns() != null && !player.getOwns().isEmpty()) {
                 for (Base base : player.getOwns()) {
                     if (base.isHome()) {
                         homeBase = base;
                         break;
                     }
                 }
                 int maxVal = (int) Math.sqrt(map.getConsistsOf().size());
 
                 x_low = Math.max(0, homeBase.getLocatedOn().getId().getX() - VIEWSIZE);
                 x_high = Math.min(maxVal, homeBase.getLocatedOn().getId().getX() + VIEWSIZE + 1);
                 y_low = Math.max(0, homeBase.getLocatedOn().getId().getY() - VIEWSIZE);
                 y_high = Math.min(maxVal, homeBase.getLocatedOn().getId().getY() + VIEWSIZE + 1);
             } else {
                 x_low = 0;
                 y_low = 0;
                 x_high = 2 * VIEWSIZE + 1;
                 y_high = 2 * VIEWSIZE + 1;
             }
         }
         model.addAttribute("xLow", x_low);
         model.addAttribute("yLow", y_low);
         model.addAttribute("xHigh", x_high);
         model.addAttribute("yHigh", y_high);
         ArrayList<ArrayList<TileOverviewDTO>> displayedTiles = new ArrayList<ArrayList<TileOverviewDTO>>();
 
         // get all visible tiles
         Tile.Id id = new Tile.Id(map.getId(), 0, 0);
         for (int y = y_low; y <= y_high; y++) {
             ArrayList<TileOverviewDTO> currentRow = new ArrayList<TileOverviewDTO>();
             for (int x = x_low; x <= x_high; x++) {
                 id.setX(x);
                 id.setY(y);
                 Tile tile = tileDAO.get(id);
                 if (tile != null) {
                     TileOverviewDTO dto = new TileOverviewDTO(tile.getId().getX(), tile.getId().getY());
 
                     dto.setSpecialResource(swag49.transfer.model.ResourceType.values()[tile.getSpecial().ordinal()]);
 
                     // TODO: TOOLTIP Java Script???
 
                     // create info
                     StringBuilder sb = new StringBuilder();
 
                     // check if base
                     if (tile.getBase() != null) {
                         if (!tile.getBase().getOwner().getId().equals(player.getId())) {
                             sb.append("Enemy base owned by ");
                             sb.append(tile.getBase().getOwner());
                         } else {
                             sb.append("Your base!");
                         }
                         sb.append("<br/>");
                     }
 
                     // check for troops
                     if (!tile.getTroops().isEmpty()) {
                         for (Troop troop : tile.getTroops()) {
                             sb.append("TODO");
                             sb.append("<br/>");
                         }
 
                         sb.append("<br/>");
                     }
 
                     // check for special resources
                     if (tile.getSpecial() != ResourceType.NONE) {
                         sb.append("Special resouce: ");
                         sb.append(tile.getSpecial().toString());
                     }
 
                     if (sb.length() == 0) {
                         sb.append("Empty Tile...");
                     }
 
                     dto.setInfo(sb.toString());
 
                     if (tile.getBase() != null)
                         dto.setHasBase(true);
                     else
                         dto.setHasBase(false);
 
                     if (checkForEnemyTerritory(tile))
                         dto.setEnemyTerritory(true);
                     else
                         dto.setEnemyTerritory(false);
 
                     if (tile.getTroops().isEmpty())
                         dto.setHasTroops(false);
                     else
                         dto.setHasTroops(true);
 
                     currentRow.add(dto);
                 }
 
             }
             displayedTiles.add(currentRow);
         }
 
         //  model.addAttribute("amount_gold", player.getResources().getAmount_gold());
         //  model.addAttribute("amount_wood", player.getResources().getAmount_wood());
         //  model.addAttribute("amount_stone", player.getResources().getAmount_stone());
         //  model.addAttribute("amount_crops", player.getResources().getAmount_crops());
         ResourceValueDTO resourceValue =
                 new ResourceValueDTO(player.getResources().getAmount_gold(), player.getResources().getAmount_wood(),
                         player.getResources().getAmount_stone(), player.getResources().getAmount_crops());
         model.addAttribute("tiles", displayedTiles);
         model.addAttribute("resources", resourceValue);
 
         return "home";
     }
 
 
     @RequestMapping(value = "/mapoverview", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String getMapOverview(@RequestParam(value = "xLow", defaultValue = "-1") int x_low,
                                  @RequestParam(value = "yLow", defaultValue = "-1") int y_low,
                                  @RequestParam(value = "xHigh", defaultValue = "-1") int x_high,
                                  @RequestParam(value = "yHigh", defaultValue = "-1") int y_high, Model model) {
         //TODO: besser machen
         player = playerDAO.get(player.getId());
         map = mapDAO.get(map.getId());
 
         //default values
         if (x_low == x_high && y_low == y_high && y_low == x_low && x_low == -1) {
             //focus on home base
             Base homeBase = null;
             if (player.getOwns() != null && !player.getOwns().isEmpty()) {
                 for (Base base : player.getOwns()) {
                     if (base.isHome()) {
                         homeBase = base;
                         break;
                     }
                 }
                 int maxVal = (int) Math.sqrt(map.getConsistsOf().size());
 
                 x_low = Math.max(0, homeBase.getLocatedOn().getId().getX() - VIEWSIZE);
                 x_high = Math.min(maxVal, homeBase.getLocatedOn().getId().getX() + VIEWSIZE + 1);
                 y_low = Math.max(0, homeBase.getLocatedOn().getId().getY() - VIEWSIZE);
                 y_high = Math.min(maxVal, homeBase.getLocatedOn().getId().getY() + VIEWSIZE + 1);
             } else {
                 x_low = 0;
                 y_low = 0;
                 x_high = 2 * VIEWSIZE + 1;
                 y_high = 2 * VIEWSIZE + 1;
             }
         }
         model.addAttribute("xLow", x_low);
         model.addAttribute("yLow", y_low);
         model.addAttribute("xHigh", x_high);
         model.addAttribute("yHigh", y_high);
         ArrayList<ArrayList<TileOverviewDTO>> displayedTiles = new ArrayList<ArrayList<TileOverviewDTO>>();
 
         // get all visible tiles
         Tile.Id id = new Tile.Id(map.getId(), 0, 0);
         for (int y = y_low; y <= y_high; y++) {
             ArrayList<TileOverviewDTO> currentRow = new ArrayList<TileOverviewDTO>();
             for (int x = x_low; x <= x_high; x++) {
                 id.setX(x);
                 id.setY(y);
                 Tile tile = tileDAO.get(id);
                 if (tile != null) {
                     TileOverviewDTO dto = new TileOverviewDTO(tile.getId().getX(), tile.getId().getY());
 
                     dto.setSpecialResource(swag49.transfer.model.ResourceType.values()[tile.getSpecial().ordinal()]);
 
                     // TODO: TOOLTIP Java Script???
 
                     // create info
                     StringBuilder sb = new StringBuilder();
 
                     // check if base
                     if (tile.getBase() != null) {
                         if (!tile.getBase().getOwner().getId().equals(player.getId())) {
                             sb.append("Enemy base owned by ");
                             sb.append(tile.getBase().getOwner());
                         } else {
                             sb.append("Your base!");
                         }
                         sb.append("<br/>");
                     }
 
                     // check for troops
                     if (!tile.getTroops().isEmpty()) {
                         for (Troop troop : tile.getTroops()) {
                             sb.append("TODO");
                             sb.append("<br/>");
                         }
 
                         sb.append("<br/>");
                     }
 
                     // check for special resources
                     if (tile.getSpecial() != ResourceType.NONE) {
                         sb.append("Special resouce: ");
                         sb.append(tile.getSpecial().toString());
                     }
 
                     if (sb.length() == 0) {
                         sb.append("Empty Tile...");
                     }
 
                     dto.setInfo(sb.toString());
 
                     if (tile.getBase() != null)
                         dto.setHasBase(true);
                     else
                         dto.setHasBase(false);
 
                     if (checkForEnemyTerritory(tile))
                         dto.setEnemyTerritory(true);
                     else
                         dto.setEnemyTerritory(false);
 
                     if (tile.getTroops().isEmpty())
                         dto.setHasTroops(false);
                     else
                         dto.setHasTroops(true);
 
                     currentRow.add(dto);
                 }
 
             }
             displayedTiles.add(currentRow);
         }
         model.addAttribute("tiles", displayedTiles);
 
         return "mapoverview";
     }
 
     @RequestMapping(value = "/playerresources", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String getPlayerResources(Model model) {
        if(player == null) {
            logger.info("no player yet set, not refreshing resources");
            return null;
        }
 
         //TODO: besser machen
         player = playerDAO.get(player.getId());
         map = mapDAO.get(map.getId());
 
         ResourceValue resourceValue = player.getResources();
 
         model.addAttribute("resources", resourceValue);
 
         return "playerresources";
     }
 
     private boolean checkForEnemyTerritory(Tile tile) {
         return tile.getBase() != null && tile.getBase().getOwner() != player ||
                 !tile.getTroops().isEmpty() && tile.getTroops().iterator().next().getOwner() != player;
     }
 
     @RequestMapping(value = "/logoutPlayer", method = RequestMethod.GET)
     @Transactional("swag49.map")
     public String logoutPlayer(SessionStatus status) {
         // close session for player
         status.setComplete();
         Player player = playerDAO.get(this.player.getId());
         player.setOnline(false);
         playerDAO.update(player);
 
         this.userID = null;

         return "redirect:"+ nodeContext.getUserNodeUrl() + "/swag/user/";
     }
 
 
 }
