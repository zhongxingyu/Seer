 package gamelogic;
 
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.client.RestTemplate;
 import swag49.dao.DataAccessObject;
 import swag49.messaging.model.MessageDTO;
 import swag49.model.*;
 import swag49.model.Map;
 import swag49.util.Log;
 
 import java.util.*;
 
 public class MapLogic {
 
     @Autowired
     @Qualifier("mapDAO")
     private DataAccessObject<Map> mapDAO;
 
     @Autowired
     @Qualifier("baseDAO")
     private DataAccessObject<Base> baseDAO;
 
     @Autowired
     @Qualifier("squareDAO")
     private DataAccessObject<Square> squareDAO;
 
     @Autowired
     @Qualifier("playerDAO")
     private DataAccessObject<Player> playerDAO;
 
     @Autowired
     @Qualifier("troopDao")
     private DataAccessObject<Troop> troopDao;
 
 
     @Autowired
     @Qualifier("troopActionDao")
     private DataAccessObject<TroopAction> troopActionDao;
 
 
     @Autowired
     @Qualifier("tileDAO")
     private DataAccessObject<Tile> tileDAO;
 
     @Autowired
     @Qualifier("buildingDao")
     private DataAccessObject<Building> buildingDao;
 
     @Autowired
     @Qualifier("buildingLevelDao")
     private DataAccessObject<BuildingLevel> buildingLevelDao;
 
     @Autowired
     @Qualifier("buildActionDao")
     private DataAccessObject<BuildAction> buildActionDao;
 
     @Autowired
     private RestTemplate restTemplate;
 
 
     @Autowired
     @Qualifier("playerDao")
     private DataAccessObject<Player> playerDao;
 
     private static final int RANDOMTRIES = 1000;
 
     private static final int START_AMOUNT_WOOD = 100;
     private static final int START_AMOUNT_STONE = 100;
     private static final int START_AMOUNT_GOLD = 100;
     private static final int START_AMOUNT_CROPS = 100;
 
     private static final int DISTANCEFACTOR = 600;
 
     private static final int NO_SQUARES = 10;
     private static final int INCOME_WOOD = 15;
     private static final int INCOME_STONE = 15;
     private static final int INCOME_GOLD = 15;
     private static final int INCOME_CROPS = 15;
 
     @Log
     private static Logger logger;
     private static final String SUBJECT_FIGHTRESULT = "[Fight Result]";
 
     @Transactional
     public void initializePlayer(Map map, Player player) {
         map = mapDAO.get(map.getId());
         Tile homeBaseTile = findHomeBaseLocation(map);
 
 
         Base homeBase = createBase(homeBaseTile, player);
         homeBase.setHome(true);
 
         baseDAO.update(homeBase);
         tileDAO.update(homeBaseTile);
 
         player.setIncome(homeBase.getResourceProduction());
         player.setUpkeep(new ResourceValue());
         player.setResources(new ResourceValue(START_AMOUNT_WOOD, START_AMOUNT_CROPS, START_AMOUNT_GOLD, START_AMOUNT_STONE));
 
         player = playerDAO.update(player);
 
     }
 
 
     @Transactional
     public void build(Square square, BuildingType type) throws Exception {
         if (square.getBuilding() != null) {
             throw new Exception("Square not empty");
         } else {
             Building constructionYard = new Building(square);
 
             constructionYard.setType(type);
 
             // get zero-level
             BuildingLevel.Id id = new BuildingLevel.Id(0, type.getId());
             BuildingLevel level = buildingLevelDao.get(id);
 
             constructionYard.setIsOfLevel(level);
 
             constructionYard = buildingDao.create(constructionYard);
 
             // create BuildAction
             BuildAction action = new BuildAction();
             action.setConcerns(constructionYard);
             action.setTarget(square.getBase().getLocatedOn());
             action.setDuration(level.getUpgradeDuration());
             action.setStartDate(new Date());
             action.setPlayer(square.getBase().getOwner());
             action.setIsAbortable(true);
 
 
             action = buildActionDao.create(action);
         }
     }
 
 
     @Transactional
     public void sendTroops(Tile target, Set<Troop> troops) throws Exception {
 
         if (troops.isEmpty())
             throw new Exception("Troops must not be empty");
 
         //TODO: unsauber, aber was solls
         Tile source = troops.iterator().next().getPosition();
 
         // long timeNeeded = calculateTravelTime(source, target, troops);
         long timeNeeded = multipleStartPlacesTravelTime(troops, target);
 
         TroopAction action = new TroopAction();
 
         action.setConcerns(troops);
         action.setDuration(Long.valueOf(timeNeeded));
         action.setPlayer(troops.iterator().next().getOwner());
         action.setStartDate(new Date());
         action.setIsAbortable(true);
         action.setSource(source);
         action.setTarget(target);
 
         //remove troop location of troops
         for (Troop troop : troops) {
             troop.setPosition(null);
             troopDao.update(troop);
         }
 
         action = troopActionDao.create(action);
     }
 
     @Transactional
     private long multipleStartPlacesTravelTime(Collection<Troop> troops,
                                                Tile destination) {
 
         // sort troops according to start position
         HashMap<Tile, ArrayList<Troop>> map = new HashMap<Tile, ArrayList<Troop>>();
         for (Troop troop : troops) {
             if (!map.containsKey(troop.getPosition())) {
                 map.get(troop.getPosition()).add(troop);
             } else {
                 ArrayList<Troop> list = new ArrayList<Troop>();
                 list.add(troop);
                 map.put(troop.getPosition(), list);
             }
         }
 
         long timeNeeded = 0;
         for (Tile tile : map.keySet()) {
             timeNeeded = Math.max(timeNeeded,
                     calculateTravelTime(tile, destination, map.get(tile)));
         }
 
         return timeNeeded;
     }
 
     /**
      * Calculates the time needed for a group of troops to go from a start to
      * destination tile
      *
      * @param start
      * @param destination
      * @param troops
      * @return
      */
     @Transactional
     private long calculateTravelTime(Tile start, Tile destination,
                                      Collection<Troop> troops) {
 
         // get slowest troops
         int minSpeed = Integer.MAX_VALUE;
 
         for (Troop troop : troops) {
             minSpeed = Math.min(minSpeed, troop.getIsOfLevel().getSpeed());
         }
 
         // calculate the distance (number of tiles to pass)
         int distance = Math.abs(start.getId().getX()
                 - destination.getId().getX())
                 + Math.abs(start.getId().getY() - destination.getId().getY());
 
         long timeNeeded = (distance * DISTANCEFACTOR) / minSpeed;
 
         return timeNeeded;
     }
 
     @Transactional
     private Tile findHomeBaseLocation(Map map) {
         //try random
         Random rnd = new Random(0);
 
         ArrayList<Tile> tiles = new ArrayList(map.getConsistsOf());
         int tileNo = tiles.size();
         for (int i = 0; i < RANDOMTRIES; i++) {
             int j = rnd.nextInt(tileNo);
             Tile tile = tiles.get(j);
 
             if (tile.getBase() != null || tile.getSpecial() != ResourceType.NONE)
                 continue;
             else {
                 return tile;
             }
         }
 
         //check all sequentially
         for (Tile tile : tiles) {
             if (tile.getBase() != null || tile.getSpecial() != ResourceType.NONE)
                 continue;
             else {
                 return tile;
             }
         }
         return null;
     }
 
 
     @Transactional
     public Base createBase(Tile tile, Player owner) {
         Base base = new Base(tile);
 
 
         base.setLocatedOn(tile);
         base.setHome(false);
         base.setOwner(owner);
         base = baseDAO.create(base);
 
         Set<Square> squares = new HashSet<Square>();
         for (int i = 0; i < NO_SQUARES; i++) {
 
             squares.add(squareDAO.create(new Square(base, i)));
         }
 
         base.setConsistsOf(squares);
 
         ResourceValue resourceProduction = new ResourceValue(INCOME_WOOD, INCOME_CROPS, INCOME_GOLD, INCOME_STONE);
         base.setResourceProduction(resourceProduction);
         base = baseDAO.update(base);
 
         tile.setBase(base);
         tileDAO.update(tile);
 
         owner.getResources().add(resourceProduction);
         playerDAO.update(owner);
 
         return base;
     }
 
 
     @Transactional
     public void handleBuildAction(BuildAction action) {
         Building building = action.getConcerns();
 
         // get current level
         BuildingLevel currentLevel = building.getIsOfLevel();
 
         // get next level
         // TODO: getNextLevel-Funktion??
         BuildingLevel.Id id = new BuildingLevel.Id(currentLevel.getId()
                 .getLevel() + 1, currentLevel.getId().getBuildingTypeId());
         BuildingLevel nextLevel = buildingLevelDao.get(id);
 
         if (nextLevel != null) {
             building.setIsOfLevel(nextLevel);
 
             buildingDao.update(building);
 
             Player player = action.getPlayer();
             // update upkeep
             player.getUpkeep().remove(currentLevel.getUpkeepCosts());
             player.getUpkeep().add(nextLevel.getUpkeepCosts());
 
             // update income
             player.getIncome().remove(currentLevel.getUpkeepCosts());
             player.getIncome().add(nextLevel.getUpkeepCosts());
 
             playerDao.update(player);
         }
     }
 
 
     @Transactional
     public void handleTroopAction(TroopAction action) {
         Tile tile = action.getTarget();
 
         boolean canBuildBase = false;
 
 
         for (Troop troop : action.getConcerns()) {
             if (troop.getType().getCanFoundBase()) {
                 canBuildBase = true;
                 break;
             }
         }
 
 
         boolean enemyTerritory = false;
 
         if (tile.getBase() != null && tile.getBase().getOwner() != action.getPlayer()) {
             enemyTerritory = false;
         }
 
         // check if other troops are on that tile
         Set<Troop> defenders = tile.getTroops();
 
         if (!defenders.isEmpty() && defenders.iterator().next().getOwner() != action.getPlayer())
             enemyTerritory = false;
 
 
         if (!enemyTerritory) {
             tile.getTroops().addAll(action.getConcerns());
             if (canBuildBase && action.getShouldFoundBase()) {
                 if (tile.getBase() != null) {
                     //TODO: write errormsg
                 } else {
                     Base base = createBase(tile, action.getPlayer());
                     //TODO: write msg to player
                 }
             }
         } else {
             if (defenders.isEmpty()) {
                 if (canBuildBase && !tile.getBase().isHome()) {
                     //take base
                     Base base = tile.getBase();
                     base.setOwner(action.getPlayer());
                     base = baseDAO.update(base);
                     //TODO: write msg to both players
                 } else {
                     //rob base
                     ResourceValue booty = calculateBooty(tile.getBase().getOwner(), action.getConcerns());
 
                     action.getPlayer().getResources().add(booty);
                     //TODO: write ms to both players
                 }
             } else {
                 Player enemyOwner = defenders.iterator().next().getOwner();
                 // oho - enemies....FIGHT!!!!
                 Set<Troop> attackers = new HashSet<Troop>(action.getConcerns());
                 boolean attackerWin = calculateFight(action.getPlayer(), enemyOwner,
                         attackers, defenders, tile);
 
                 if (attackerWin) {
                     if (tile.getBase() != null) {
                         //rob base
                         ResourceValue booty = calculateBooty(enemyOwner, attackers);
                         action.getPlayer().getResources().add(booty);
 
                         if (canBuildBase && !tile.getBase().isHome()) {
                             Base base = tile.getBase();
                             base.setOwner(action.getPlayer());
                             base = baseDAO.update(base);
                             //TODO: write msg to both players
                         } else {
                             sendHome(attackers, action.getSource());
                         }
                     } else if (canBuildBase) {
                         Base base = createBase(tile, action.getPlayer());
                         //TODO: write msg to player
                     } else {
                         //stay
                     }
                 } else {
                     if (!attackers.isEmpty()) {
                         // send attacking army home to mom
                         sendHome(attackers, action.getSource());
                     }
                 }
 
             }
         }
     }
 
     @Transactional
     private void sendHome(Set<Troop> attackers, Tile destination) {
         TroopAction homeAction = new TroopAction();
         homeAction.setConcerns(attackers);
         homeAction.setShouldFoundBase(false);
         homeAction.setTarget(destination);
         homeAction.setIsAbortable(false);
 
         homeAction = troopActionDao.create(homeAction);
     }
 
     @Transactional
     private boolean calculateFight(Player attackers_owner, Player defenders_owner,
                                    Set<Troop> attackers, Set<Troop> defenders, Tile tile) {
 
         Collection<Troop> deadTroops_attacker = null;
         Collection<Troop> deadTroops_defenders = null;
 
         do {
             // calculate victims
             deadTroops_attacker = calculateAttack(attackers, defenders);
             deadTroops_defenders = calculateAttack(defenders, attackers);
 
             // remove victims of the sets
             if (!deadTroops_defenders.isEmpty())
                 defenders.removeAll(deadTroops_defenders);
 
             if (!deadTroops_attacker.isEmpty())
                 attackers.removeAll(deadTroops_attacker);
 
             // remove victims in persistence layer
             for (Troop troop : deadTroops_attacker) {
                 troopDao.delete(troop);
             }
 
             for (Troop troop : deadTroops_defenders) {
                 troopDao.delete(troop);
 
                 tile.getTroops().remove(troop);
             }
 
         } while (!deadTroops_attacker.isEmpty()
                 && !deadTroops_defenders.isEmpty());
 
         if (attackers.size() > 0 && defenders.isEmpty()) {
             // attacker win
             // TODO: write MSG to both player about the result of the fight
 
             // attacking troops stay on the tile
             tile.getTroops().addAll(attackers);
 
             return true;
         } else if (defenders.size() > 0 && attackers.isEmpty()) {
             // defenders win
             // TODO: write MSG to both player about the result of the fight
 
             return false;
         } else {
             // draw
             if (defenders.size() == 0 && attackers.size() == 0) {
                 // war - what is it good for?
 
                 // TODO: write MSG to both player that they lost their army
                 return false;
             } else {
                 // non-mexican standoff
 
                 // TODO: write MSG to both player about the result of the fight
                 sendMessage(attackers_owner, defenders_owner, SUBJECT_FIGHTRESULT, "Your army attacked the forces of player " + defenders_owner.getId() + " at tile (" + tile.getId().getX() + "," + tile.getId().getY() + ").");
                 sendMessage(defenders_owner, attackers_owner, SUBJECT_FIGHTRESULT, "Your army has been attacked by the forces of player " + attackers_owner.getId() + " at tile (" + tile.getId().getX() + "," + tile.getId().getY() + ").");
 
                 return false;
             }
         }
     }
 
     private ResourceValue calculateBooty(Player defenders_owner,
                                          Set<Troop> attackers) {
 
         ResourceValue booty = new ResourceValue();
         // determine maximal resources by dividing players resources by
         // the number of Bases
         int noCities = defenders_owner.getOwns().size();
         ResourceValue maxSteal = new ResourceValue(defenders_owner
                 .getResources().getAmount_wood() / noCities, defenders_owner
                 .getResources().getAmount_crops() / noCities, defenders_owner
                 .getResources().getAmount_gold() / noCities, defenders_owner
                 .getResources().getAmount_stone() / noCities);
 
         int cargo_total = 0;
         for (Troop troop : attackers) {
             cargo_total += troop.getIsOfLevel().getCargo_capacity();
         }
 
         int i = getNoNonzeroResources(maxSteal);
         while (cargo_total > 0 && i > 0) {
             // steal c amount of crops
             int c = Math.min(maxSteal.getAmount_crops(), cargo_total / i);
 
             booty.setAmount_crops(booty.getAmount_crops() + c);
             maxSteal.setAmount_crops(maxSteal.getAmount_crops() - c);
 
             // steal s amount of stone
             int s = Math.min(maxSteal.getAmount_stone(), cargo_total / i);
             booty.setAmount_stone(booty.getAmount_stone() + s);
             maxSteal.setAmount_stone(maxSteal.getAmount_stone() - s);
 
             // steal w amount of wood
             int w = Math.min(maxSteal.getAmount_wood(), cargo_total / i);
             booty.setAmount_wood(booty.getAmount_wood() + w);
             maxSteal.setAmount_wood(maxSteal.getAmount_wood() - w);
 
             // steal g amount of gold
             int g = Math.min(maxSteal.getAmount_gold(), cargo_total / i);
             booty.setAmount_gold(booty.getAmount_gold() + g);
             maxSteal.setAmount_gold(maxSteal.getAmount_gold() - g);
 
             cargo_total -= (c + s + w + g);
 
             i = getNoNonzeroResources(maxSteal);
         }
 
         return booty;
     }
 
     private int getNoNonzeroResources(ResourceValue value) {
         int i = 0;
 
         if (value.getAmount_crops() > 0)
             i++;
 
         if (value.getAmount_gold() > 0)
             i++;
 
         if (value.getAmount_stone() > 0)
             i++;
 
         if (value.getAmount_wood() > 0)
             i++;
         return i;
     }
 
     private Collection<Troop> calculateAttack(Set<Troop> attackers,
                                               Set<Troop> defenders) {
 
         List<Troop> deadTroops = new ArrayList<Troop>();
         int strength_attacker = 0;
 
         for (Troop troop : attackers) {
             strength_attacker += troop.getIsOfLevel().getStrength();
         }
         Iterator<Troop> iterator = defenders.iterator();
 
         while (strength_attacker > 0 && iterator.hasNext()) {
             Troop troop = iterator.next();
             if (strength_attacker >= troop.getIsOfLevel().getDefense()) {
                 deadTroops.add(troop);
                 strength_attacker -= troop.getIsOfLevel().getDefense();
             }
         }
 
         return deadTroops;
     }
 
     private void sendMessage(Player sender, Player receiver, String subject, String content) {
 
         MessageDTO message = new MessageDTO(subject, content, sender.getUserId(), null, receiver.getUserId(),
                null, new Date(), null, sender.getPlays().getUrl());
 
         restTemplate.put("http://localhost:8080/messaging/send", message);
     }
 }
