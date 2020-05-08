 package lv.k2611a.domain;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import lv.k2611a.util.AStar;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import lv.k2611a.util.Node;
 import lv.k2611a.util.Point;
 
 public class Map {
 
     private static final Logger log = LoggerFactory.getLogger(Map.class);
 
     private Tile[][] tiles;
     private Player[] players;
     private int width;
     private int height;
     private List<Unit> units;
     private List<Building> buildings;
     private List<Bullet> bullets;
     private int lastBulletId = 0;
 
     private HashMap<Point, RefineryEntrance> refineryEntranceList = new HashMap<Point, RefineryEntrance>();
     private Set<Integer> harvesters = new HashSet<Integer>();
     private Set<Integer> freeHarvesters = new HashSet<Integer>();
 
     public Map(int width, int height) {
         this(width, height, TileType.SAND);
     }
 
 
     public Map(int width, int height, TileType tileType) {
         this(width,height,tileType,8);
     }
 
 
 
     public Map(int width, int height, TileType tileType, int playerCount) {
         this.width = width;
         this.height = height;
         tiles = new Tile[height][];
         for (int y = 0; y < height; y++) {
             for (int x = 0; x < width; x++) {
                 if (tiles[y] == null) {
                     tiles[y] = new Tile[width];
                 }
                 tiles[y][x] = new Tile(x, y);
                 tiles[y][x].setTileType(tileType);
             }
         }
         units = new ArrayList<Unit>();
         buildings = new ArrayList<Building>();
         bullets = new ArrayList<Bullet>();
         players = new Player[playerCount];
         for (int i = 0; i < playerCount; i++) {
             Player player = new Player();
             player.setId(i);
             players[i] = player;
         }
     }
 
     public Tile getTile(Point point) {
         return getTile(point.getX(), point.getY());
     }
 
     public Tile getTile(int x, int y) {
         if (x < 0) {
             return null;
         }
         if (y < 0) {
             return null;
         }
         if (x >= width) {
             return null;
         }
         if (y >= height) {
             return null;
         }
         return tiles[y][x];
     }
 
     public int getWidth() {
         return width;
     }
 
     public int getHeight() {
         return height;
     }
 
     public List<Unit> getUnits() {
         List<Unit> result = new ArrayList<Unit>();
         for (Unit unit : units) {
             if (unit != null) {
                 result.add(unit);
             }
         }
         return result;
     }
 
     public List<Unit> getUnitsByIds(Set<Integer> ids) {
         List<Unit> unitList = new ArrayList<Unit>();
         for (Integer id : ids) {
             if (id != null) {
                 Unit unit = getUnit(id);
                 if (unit != null) {
                     unitList.add(unit);
                 }
             }
         }
         return unitList;
     }
 
     public int addUnit(Unit unit) {
         units.add(unit);
         unit.setId(units.size() - 1);
         setUsed(unit.getX(),unit.getY(),unit.getId());
         return unit.getId();
     }
 
     public int addBuilding(Building building) {
         buildings.add(building);
         building.setId(buildings.size() - 1);
         return building.getId();
     }
 
     public List<Unit> getUnitsByType(UnitType unitType) {
         List<Unit> result = new ArrayList<Unit>();
         for (Unit unit : units) {
             if (unit != null) {
                 if (unit.getUnitType() == UnitType.HARVESTER) {
                     result.add(unit);
                 }
             }
         }
         return result;
     }
 
     public List<Building> getBuildings() {
         return filterNonNulls(buildings);
     }
 
     private List<Building> filterNonNulls(List<Building> buildings) {
         List<Building> result = new ArrayList<Building>();
         for (Building building : buildings) {
             if (building != null) {
                 result.add(building);
             }
         }
         return result;
     }
 
     public List<Tile> getTilesByType(TileType tileType) {
         List<Tile> result = new ArrayList<Tile>();
         for (Tile[] tile : tiles) {
             for (Tile tile1 : tile) {
                 if (tile1.getTileType() == tileType) {
                     result.add(tile1);
                 }
             }
         }
         return result;
     }
 
     public List<Building> getBuildingsByType(BuildingType buildingType) {
         List<Building> result = new ArrayList<Building>();
         for (Building building : filterNonNulls(buildings)) {
             if (building.getType() == buildingType) {
                 result.add(building);
             }
         }
         return result;
     }
 
     public List<Building> getBuildingsByTypeAndOwner(BuildingType buildingType, int ownerId) {
         List<Building> result = new ArrayList<Building>();
         for (Building building : filterNonNulls(buildings)) {
             if (building.getType() == buildingType) {
                 if (building.getOwnerId() == ownerId) {
                     result.add(building);
                 }
             }
         }
         return result;
     }
 
 
     public List<Building> getBuildingsByOwner(int ownerId) {
         List<Building> result = new ArrayList<Building>();
         for (Building building : filterNonNulls(buildings)) {
             if (building.getOwnerId() == ownerId) {
                 result.add(building);
             }
         }
         return result;
     }
 
     public List<Tile> getTileNeighbours(int x, int y) {
         List<Tile> tileList = new ArrayList<Tile>();
         if (x > 0) {
             tileList.add(getTile(x - 1, y));
         }
         if (y > 0) {
             tileList.add(getTile(x, y - 1));
         }
         if ((x > 0) && (y > 0)) {
             tileList.add(getTile(x - 1, y - 1));
         }
         if (x < width - 1) {
             tileList.add(getTile(x + 1, y));
         }
         if (y < height - 1) {
             tileList.add(getTile(x, y + 1));
         }
         if ((x < width - 1) && (y < height - 1)) {
             tileList.add(getTile(x + 1, y + 1));
         }
 
         if ((x > 0) && (y < height - 1)) {
             tileList.add(getTile(x - 1, y + 1));
         }
         if ((x < width - 1) && (y > 0)) {
             tileList.add(getTile(x + 1, y - 1));
         }
         return tileList;
     }
 
 
     public List<Node> getTileNeighbourNodes(int x, int y) {
         List<Node> nodeList = new ArrayList<Node>();
         if (x > 0) {
             nodeList.add(new Node(x - 1, y));
         }
         if (y > 0) {
             nodeList.add(new Node(x, y - 1));
         }
         if (x < width - 1) {
             nodeList.add(new Node(x + 1, y));
         }
         if (y < height - 1) {
             nodeList.add(new Node(x, y + 1));
         }
 
         if ((x > 0) && (y > 0)) {
             nodeList.add(new Node(x - 1, y - 1));
         }
         if ((x < width - 1) && (y < height - 1)) {
             nodeList.add(new Node(x + 1, y + 1));
         }
 
         if ((x > 0) && (y < height - 1)) {
             nodeList.add(new Node(x - 1, y + 1));
         }
         if ((x < width - 1) && (y > 0)) {
             nodeList.add(new Node(x + 1, y - 1));
         }
         return nodeList;
     }
 
     public List<Tile> getTileDirectNeighbours(int x, int y) {
         List<Tile> tileList = new ArrayList<Tile>();
         if (x > 0) {
             tileList.add(getTile(x - 1, y));
         }
         if (y > 0) {
             tileList.add(getTile(x, y - 1));
         }
         if (x < width - 1) {
             tileList.add(getTile(x + 1, y));
         }
         if (y < height - 1) {
             tileList.add(getTile(x, y + 1));
         }
         return tileList;
     }
 
     public List<Tile> getTileVerticalNeighbours(int x, int y) {
         List<Tile> tileList = new ArrayList<Tile>();
         if (y > 0) {
             tileList.add(getTile(x, y - 1));
         }
         if (y < height - 1) {
             tileList.add(getTile(x, y + 1));
         }
         return tileList;
     }
 
     public List<Tile> getTileHorizontalNeighbours(int x, int y) {
         List<Tile> tileList = new ArrayList<Tile>();
         if (x > 0) {
             tileList.add(getTile(x - 1, y));
         }
         if (x < width - 1) {
             tileList.add(getTile(x + 1, y));
         }
         return tileList;
     }
 
     public boolean hasTileInSquare(int x, int y, int radius, EnumSet<TileType> tileTypes) {
         for (int currentX = x - radius; currentX < x + radius; currentX++) {
             for (int currentY = y - radius; currentY < y + radius; currentY++) {
                 if (currentX > 0) {
                     if (currentY > 0) {
                         if (currentX < width) {
                             if (currentY < height) {
                                 if (tileTypes.contains(getTile(currentX, currentY).getTileType())) {
                                     return true;
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return false;
     }
 
     public Unit getUnitAt(int x, int y) {
         for (Unit unit : units) {
             if (unit != null) {
                 if ((unit.getX() == x) && (unit.getY() == y)) {
                     return unit;
                 }
             }
         }
         return null;
     }
 
     public Building getBuildingAt(int x, int y) {
         for (Building building : filterNonNulls(buildings)) {
             if (building.getX() <= x && building.getX() + building.getType().getWidth() - 1 >= x) {
                 if (building.getY() <= y && building.getY() + building.getType().getHeight() - 1 >= y) {
                     return building;
                 }
             }
         }
         return null;
     }
 
     public boolean hasBuildingInSquare(int x, int y, int radius) {
         for (int currentX = x - radius; currentX < x + radius; currentX++) {
             for (int currentY = y - radius; currentY < y + radius; currentY++) {
                 if (currentX > 0) {
                     if (currentY > 0) {
                         if (currentX < width) {
                             if (currentY < height) {
                                 for (Building building : filterNonNulls(buildings)) {
                                     if (building.getX() == currentX) {
                                         if (building.getY() == currentY) {
                                             return true;
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return false;
     }
 
     public static Point getClosestNode(Point current, List<Point> points) {
         Point bestNode = null;
         double bestDistance = Double.MAX_VALUE;
         for (Point point : points) {
             double currentDistance = Map.getDistanceBetween(current, point);
             if (bestDistance > currentDistance) {
                 bestNode = point;
                 bestDistance = currentDistance;
             }
         }
         return bestNode;
     }
 
     public static double getDistanceBetween(Point node1, Point node2) {
         //if the nodes are on top or next to each other, return 1
         if (node1.getX() == node2.getX()) {
             return Math.abs(node1.getY() - node2.getY());
         }
         if (node1.getY() == node2.getY()) {
             return Math.abs(node1.getX() - node2.getX());
         }
         int deltaX = Math.abs(node1.getX() - node2.getX());
         int deltaY = Math.abs(node1.getY() - node2.getY());
         return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
     }
 
     public static double getDistanceBetween(Node node1, Node node2) {
         //if the nodes are on top or next to each other, return 1
         if (node1.getX() == node2.getX()) {
             return Math.abs(node1.getY() - node2.getY());
         }
         if (node1.getY() == node2.getY()) {
             return Math.abs(node1.getX() - node2.getX());
         }
         int deltaX = Math.abs(node1.getX() - node2.getX());
         int deltaY = Math.abs(node1.getY() - node2.getY());
         return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
     }
 
     // Check if the tile contains no building. Units are not considered an obstacle in this case, as they might move.
     public boolean isPassable(Node neighbor) {
         return getTile(neighbor.getX(), neighbor.getY()).isPassable();
     }
 
     // Check if the tile contains no buildings or units. Also used for Harvester move.
     public boolean isUnoccupied(Node neighbor, int unitId, int ownerid, boolean isHarvester) {
         if (refineryEntranceCheck(neighbor.getX(), neighbor.getY(), unitId, ownerid, isHarvester)) {
             return true;
         }
         return getTile(neighbor.getX(), neighbor.getY()).isUnoccupied(unitId);
     }
 
     private boolean refineryEntranceCheck(int x, int y, int unitId, int ownerid, boolean isHarvester) {
         Point point = new Point(x,y);
         if (isHarvester) {
             if (getTile(point).isUsedByUnit()) {
                 return false;
             }
             RefineryEntrance refineryEntrance = this.refineryEntranceList.get(point);
             // Refinery entrance should be passable for non-empty harvesters
             if (this.harvesters.contains(unitId) && !this.freeHarvesters.contains(unitId) && refineryEntrance != null) {
                  if (refineryEntrance.getOwnerId() == ownerid) {
                     return true;
                 }
             }
             // This is a reverse-lookup for 3 upper tiles of refinery when we are leaving refinery.
             if (this.freeHarvesters.contains(unitId)) {
                 for (int entranceX = x - 1; entranceX < x + 2; entranceX++) {
                     point = new Point(entranceX, y + 1);
                     refineryEntrance = this.refineryEntranceList.get(point);
                     if (refineryEntrance != null) {
                         if (refineryEntrance.getOwnerId() == ownerid) {
                             return true;
                         }
                     }
                 }
             }
         }
         return false;
     }
 
 
     // Check if the tile contains no buildings or units. Used only for Harvester pathfinding in AStar.
     public boolean isUnoccupiedHarvesterAStar(Node start, Node neighbor, int unitId, int ownerid, boolean isHarvester) {
         if (refineryEntranceCheck(neighbor.getX(), neighbor.getY(), unitId, ownerid, isHarvester, start.getX(), start.getY())) {
             return true;
         }
         return getTile(neighbor.getX(), neighbor.getY()).isUnoccupied(unitId);
     }
 
     private boolean refineryEntranceCheck(int x, int y, int unitId, int ownerid, boolean isHarvester, int startX, int startY) {
         Point point = new Point(x,y);
         Point startPoint = new Point(startX, startY);
         if (isHarvester) {
             if (getTile(point).isUsedByUnit()) {
                 return false;
             }
             RefineryEntrance refineryEntrance = this.refineryEntranceList.get(point);
             // Refinery entrance should be passable for non-empty harvesters
             if (this.harvesters.contains(unitId) && !this.freeHarvesters.contains(unitId) && refineryEntrance != null) {
                 if (refineryEntrance.getOwnerId() == ownerid) {
                     return true;
                 }
             }
             // This is a lookup for 3 upper tiles of refinery when we are planning to leave the refinery.
             refineryEntrance = this.refineryEntranceList.get(startPoint);
             if (this.freeHarvesters.contains(unitId) && refineryEntrance != null) {
                 if (refineryEntrance.getOwnerId() == ownerid) {
                     if (y == startY-1) {
                         for (int pointX = x - 1; pointX < x + 2; pointX++) {
                             if (pointX == startX) {
                                 return true;
                             }
                         }
                     }
                 }
             }
         }
         return false;
     }
 
     public boolean isUnoccupied(int x, int y) {
         Tile tile = getTile(x, y);
         if (tile == null) {
             return false;
         }
         return tile.isUnoccupied();
     }
 
     public boolean isPassable(int x, int y) {
         Tile tile = getTile(x, y);
         if (tile == null) {
             return false;
         }
         return tile.isPassable();
     }
 
 
     public void setUsed(int x, int y, int unitId) {
        getTile(x, y).setUsed(unitId);
     }
 
     public void clearUsageFlag() {
         for (Tile[] tile : tiles) {
             for (Tile tile1 : tile) {
                 tile1.setUsed(-1);
                 tile1.setPassableSegmentNumber(-1);
             }
         }
     }
 
     public Unit getUnit(int id) {
         if (id >= units.size()) {
             return null;
         }
         return units.get(id);
     }
 
     public Building getBuilding(int id) {
         if (id >= buildings.size()) {
             return null;
         }
         return buildings.get(id);
     }
 
     public Player getPlayerById(int id) {
         return this.players[id];
     }
 
     public Player[] getPlayers() {
         return players;
     }
 
     public Tile getNearestFreeTile(int x, int y) {
         if (isUnoccupied(x, y)) {
             getTile(x, y);
         }
         for (int radius = 0; radius < 10; radius++) {
             for (int currentX = x - radius; currentX < x + radius; currentX++) {
                 for (int currentY = y - radius; currentY < y + radius; currentY++) {
                     if (isUnoccupied(currentX, currentY)) {
                         return getTile(currentX, currentY);
                     }
                 }
             }
         }
         return null;
     }
 
     public Tile getNearestFreeTileForUnitPlacement(int minX, int maxX, int minY, int maxY) {
         for (int currentX = minX; currentX <= maxX; currentX++) {
             for (int currentY = maxY; currentY >= minY; currentY--) {
                 if (isUnoccupied(currentX, currentY)) {
                     return getTile(currentX, currentY);
                 }
             }
         }
         return null;
     }
 
 
     public Point getRandomFreeTile(Point near, int distanceFrom, int distanceTo) {
         Random r = new Random();
         int iterationCount = 0;
         while (iterationCount < 100) {
             int x = r.nextInt(width);
             int y = r.nextInt(height);
             Point candidate = new Point(x, y);
             double distanceBetween = Map.getDistanceBetween(near, candidate);
             if ((distanceBetween > distanceFrom) && (distanceBetween < distanceTo)) {
                 return candidate;
             }
             iterationCount++;
         }
         log.warn("Cannot find free tile spot in " + iterationCount + " iterations");
         return null;
     }
 
     public HashMap<Point, RefineryEntrance> getRefineryEntranceList() {
         return refineryEntranceList;
     }
 
     public Set<Integer> getHarvesters() {
         return harvesters;
     }
 
     public Set<Integer> getFreeHarvesters() {
         return freeHarvesters;
     }
 
     // precache simultaneously impassable segments, to avoid AStars worse-case scenario calculations. Like moving 100 units to impassable area.
     public void buildPassableSegmentCache() {
         int segmentNumber = 0;
         boolean found;
         do {
             found = false;
             for (Tile[] tile : tiles) {
                 for (Tile tile1 : tile) {
                     if (tile1.isUnoccupied()) {
                         if (tile1.getPassableSegmentNumber() == -1) {
                             segmentNumber++;
                             spreadSegment(segmentNumber, tile1);
                             found = true;
                         }
                     }
                 }
             }
         } while (found);
     }
 
     private void spreadSegment(int segmentNumber, Tile tile) {
         tile.setPassableSegmentNumber(segmentNumber);
         for (Tile neighbour : getTileNeighbours(tile.getX(), tile.getY())) {
             if (neighbour.isUnoccupied()) {
                 if (neighbour.getPassableSegmentNumber() == -1) {
                     spreadSegment(segmentNumber, neighbour);
                 }
             }
         }
     }
 
     public void removeUnit(Unit unit) {
         this.units.set(unit.getId(), null);
     }
 
     public void removeBuilding(Building building) {
         this.buildings.set(building.getId(), null);
     }
 
     public void removeBuilding(int id) {
         this.buildings.set(id, null);
     }
 
     public void removeBullet(Bullet bullet) {
         this.bullets.remove(bullet);
     }
 
     public void removeBullets(List<Bullet> bullets) {
         this.bullets.removeAll(bullets);
     }
 
     public int addBullet(Bullet bullet) {
         lastBulletId++;
         bullet.setId(lastBulletId);
         this.bullets.add(bullet);
         return lastBulletId;
     }
 
     public List<Bullet> getBullets() {
         return Collections.unmodifiableList(bullets);
     }
 
 }
