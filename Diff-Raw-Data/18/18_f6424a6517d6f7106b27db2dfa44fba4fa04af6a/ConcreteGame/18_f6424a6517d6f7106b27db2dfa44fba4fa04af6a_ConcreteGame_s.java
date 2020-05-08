 package com.github.pmcompany.pustomario.core;
 
 import com.github.pmcompany.pustomario.io.View;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.*;
 
 import static com.github.pmcompany.pustomario.core.VectorDirection.*;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class ConcreteGame implements EventHandler, DataProvider, EventServer {
     private static final int TRACE_RAY_COEF = 1;
     private static final int HIT_DECREMENT_HP_VALUE = 10;
 
     private Map map;
     private java.util.Map<String, Player> players;
     private EventHandler generatedEventsEventHandler;
 
     private String playerName;
     private Player currentPlayer;
 
     private List<EventHandler> handlers;
 
     private long updateTime;
 
     private Player hitedPlayer;
 
     private List<Point> spawnPoints;
     private Rating rate;
 
     public ConcreteGame(String playerName) {
         this.playerName = playerName;
 
         players = new LinkedHashMap<String, Player>();
         handlers = new LinkedList<EventHandler>();
     }
 
     public void initGame() {
         spawnPoints = new LinkedList<Point>();
 
         loadMap();
 
         initPlayers();
 
         updateTime = System.currentTimeMillis();
     }
 
     private void initPlayers() {
         rate = new Rating();
 
         if (players.isEmpty()) {
             Player p = new Player(0, 0);
             p.setName(playerName);
 
             players.put(playerName, p);
         }
         
         for (Player p : players.values()) {
             spawn(p);
             rate.addPlayer(p);
         }
     }
 
     private void spawn(Player p) {
         System.out.println("Respawning player " + p.getName());
 
         Random rnd = new Random();
         Point spawnPoint = spawnPoints.get(rnd.nextInt(spawnPoints.size()));
         p.setHp(Player.START_HP);
         p.setX((spawnPoint.getX()-1) * View.TILE_WIDTH + 1);
         p.setY((spawnPoint.getY()-1) * View.TILE_HEIGHT + 1);
     }
 
     private void loadMap() {
         try {
             BufferedReader reader;
             reader = new BufferedReader(new FileReader("level.pml"));
 
             String[] levelParams = reader.readLine().split(" ");
 
             int levelWidth = Integer.parseInt(levelParams[0]);
             int levelHeight = Integer.parseInt(levelParams[1]);
             map = new Map(levelWidth, levelHeight);
 
             int tile;
             for (int j = levelHeight; j > 0; j--) {
                 String levelLine = reader.readLine();
                 for (int i = 1; i <= levelWidth; i++) {
                     tile = levelLine.charAt(i-1);
                     if (tile == '#') {
                         map.setAt(i, j, levelLine.charAt(i-1));
                     } else if (tile == '@') {
                         spawnPoints.add(new Point(i, j));
 //                        if (playerName != null) {
 //                            currentPlayer = new Player((i-1) * View.TILE_WIDTH + 1,
 //                                    (j-1) * View.TILE_HEIGHT + 1);
 //                        }
                     }
                 }
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         if (currentPlayer == null) {
             currentPlayer = new Player(1, 1);
         }
 
         currentPlayer.setName(playerName);
 
         players.put(currentPlayer.getName(), currentPlayer);
     }
 
     public void handleEvent(Event e) {
         Player p;
         if (e.getStringValue().equals(getPlayerName())) {
             p = currentPlayer;
         } else {
             p = players.get(e.getSender());
         }
 
         if (p != null) {
             System.out.printf("Handling event from %s %n", p.getName());
         }
 
         switch (e.getType()) {
 //            case ACCELERATE_X_PLAYER: {
 //                p.accelerateX(e.getFloatValue() * Physics.RUN_ACCELERATION_COEFFICIENT);
 //            } break;
 //            case ACCELERATE_Y_PLAYER: {
 //                p.accelerateY(e.getFloatValue());
 //            } break;
 
             case RUN_RIGHT: {
                p.accelerateX(Physics.RUN_SPEED);
             } break;
             case RUN_LEFT: {
                p.accelerateX(-Physics.RUN_SPEED);
             } break;
             case JUMP: {
                if (p.isCanJump()) {
                    p.accelerateY(Physics.JUMP_SPEED);
                    p.setCanJump(false);
                 }
             } break;
 
             case SHOOT: {
                 String[] pos = e.getStringValue().split(" ");
 
                 int sx = Integer.parseInt(pos[0]);
                 int sy = Integer.parseInt(pos[1]);
 
                 countShoot(currentPlayer.getPosition(), new Point(sx, sy));
 
                 if (hitedPlayer != null) {
                     Event event = new GameEvent(EventType.INCREMENT_HP, e.getSender(),
                             hitedPlayer.getName() + " -" + HIT_DECREMENT_HP_VALUE);
 
                     for (EventHandler h : handlers) {
                         h.handleEvent(event);
                     }
 
                     hitedPlayer = null;
                     handleEvent(event);
                 }
 
             } break;
 
             case INCREMENT_HP: {
                 String[] values = e.getStringValue().split(" ");
                 int leftHP = players.get(values[0]).changeHP(Integer.parseInt(values[1]));
 
                 if (leftHP <= 0) {
                     GameEvent event = new GameEvent(EventType.KILLED, e.getSender(), values[0]);
                     for (EventHandler h : handlers) {
                         h.handleEvent(event);
                     }
                     handleEvent(event);
                 }
             } break;
 
             case KILLED: {
                 System.out.printf("%s: %s killed %s%n", playerName, e.getSender(), e.getStringValue());
                 rate.incScore(players.get(e.getSender()), 1);
                 rate.incScore(players.get(e.getStringValue()), -1);
 
                 GameEvent event = new GameEvent(EventType.REBORN,
                         e.getSender(), e.getStringValue());
                 for (EventHandler h : handlers) {
                     h.handleEvent(event);
                 }
 
                 handleEvent(event);
             } break;
 
             case REBORN: {
                 spawn(players.get(e.getStringValue()));
             } break;
 
             case JOIN_NEW_PLAYER: {
                 String[] playerInfo = e.getStringValue().split(" ");
                 int x = Integer.parseInt(playerInfo[0]);
                 int y = Integer.parseInt(playerInfo[1]);
 
                 Player newPlayer = new Player(x, y);
                 newPlayer.setName(e.getSender());
 
                 players.put(newPlayer.getName(), newPlayer);
                 rate.addPlayer(newPlayer);
             } break;
 
             case MOVE_PLAYER: {
                 if (p != currentPlayer && p != null) {
                     String[] pos = e.getStringValue().split(" ");
                     int x = Integer.parseInt(pos[0]);
                     int y = Integer.parseInt(pos[1]);
 
                     p.setX(x);
                     p.setY(y);
                 }
             } break;
 
 //            case SET_PLAYER_X: {
 //                currentPlayer.setX(e.getIntValue());
 //            } break;
 //            case SET_PLAYER_Y: {
 //                currentPlayer.setY(e.getIntValue());
 //            } break;
         }
     }
 
     public int getTileAt(int x, int y) {
         return map.getAt(x, y);
     }
 
     public Point countTileByAbs(int absX, int absY) {
         return new Point((absX-1) / View.TILE_WIDTH + 1, (absY-1) / View.TILE_HEIGHT + 1);
     }
 
     public Point countAbsByTile(Point p) {
         return countAbsByTile(p.getX(), p.getY());
     }
 
     public Point countAbsByTile(int tX, int tY) {
         return new Point((tX - 1) * View.TILE_WIDTH + 1, (tY - 1) * View.TILE_HEIGHT + 1);
     }
 
     public boolean isTileBlocked(int x, int y) {
         return map.isBlocked(x, y);
     }
 
     public int getPlayerX() {
         return currentPlayer.getX();
     }
 
     public int getPlayerY() {
         return currentPlayer.getY();
     }
 
     public float getPlayerSpeedX() {
         return currentPlayer.getSpeedX();
     }
 
     public float getPlayerSpeedY() {
         return currentPlayer.getSpeedY();
     }
 
     public int getMapWidth() {
         return map.getWidth();
     }
 
     public int getMapHeight() {
         return map.getHeight();
     }
 
     public boolean isPlayerWatchingRight() {
         return currentPlayer.isWatchingRight();
     }
 
     public void preUpdate() {
         long currTime = System.currentTimeMillis();
         long diff = currTime - updateTime;
 
         // Count dynamic objects position,
         // using physical laws and time value
 
         for (Player p : players.values()) {
             movePlayer(p);
 
             if (p == currentPlayer && p.moved()) {
 //                System.out.printf("%b X %d->%d Y %d->%d%n", p.moved(), p.getPrevX(), p.getX(),
 //                        p.getPrevY(), p.getY());
 
                 for (EventHandler h : handlers) {
                     h.handleEvent(new GameEvent(EventType.MOVE_PLAYER, currentPlayer.getName(),
                             String.format("%d %d", currentPlayer.getX(), currentPlayer.getY())));
                 }
             }
         }
 
         updateTime = currTime;
     }
 
     public void postUpdate() {
 //        currentPlayer.setSpeedX(0);
 //        currentPlayer.setSpeedY(0);
     }
 
     private void movePlayer(Player currentPlayer) {
         currentPlayer.accelerateY(Physics.GRAVITY_ACCELERATION);
         currentPlayer.setSpeedX(currentPlayer.getSpeedX() * Physics.GROUND_ONE_ON_FRICTION);
 
         int px = currentPlayer.getX();
         int py = currentPlayer.getY();
         float speedX = currentPlayer.getSpeedX();
         float speedY = currentPlayer.getSpeedY();
 
         // Move currentPlayer
 
         if (currentPlayer == this.currentPlayer) {
             currentPlayer.setPrevX(px);
             currentPlayer.setPrevY(py);
         }
 
         Point newCrossedTile = null;
         List<Point> crossedTiles;
         boolean crosses = false;
 
         if (speedX != 0) {
             currentPlayer.setX(px + (int) speedX);
             crossedTiles = getPlayerCrossedTiles(currentPlayer);
 
             for (int i = 0; i < crossedTiles.size() && (! crosses); i++) {
                 newCrossedTile = crossedTiles.get(i);
 
                 if (isTileBlocked(newCrossedTile.getX(), newCrossedTile.getY())) {
                     crosses = true;
                 }
             }
 
             if (crosses) {
                 currentPlayer.setX(px);
                 currentPlayer.setSpeedX(0);
             }
         }
 
         crosses = false;
 
         if (speedY != 0) {
             currentPlayer.setY(py + (int) speedY);
             crossedTiles = getPlayerCrossedTiles(currentPlayer);
 
             for (int i = 0; i < crossedTiles.size() && (! crosses); i++) {
                 newCrossedTile = crossedTiles.get(i);
 
                 if (isTileBlocked(newCrossedTile.getX(), newCrossedTile.getY())) {
                     crosses = true;
                 }
             }
 
             if (crosses) {
                currentPlayer.setY(py);
 
                 if (speedY < 0) {
                     currentPlayer.setCanJump(true);
                 }
                 currentPlayer.setSpeedY(0);
             }
         }
     }
 
     public List<Point> getPlayerCrossedTiles(Player currentPlayer) {
         List<Point> tilesList = new LinkedList<Point>();
 
         int px = currentPlayer.getX();
         int py = currentPlayer.getY();
 
         int lx = 0;
         int ly = 0;
         int nx;
         int ny;
         int dx;
         int dy;
         Point p;
         for (int i = 0; i < 4; i++) {
             nx = px + lx * (View.TILE_WIDTH - 1);
             ny = py + ly * (View.TILE_HEIGHT - 1);
 
             dx = nx % View.TILE_WIDTH;
             dy = ny % View.TILE_HEIGHT;
 
             nx -= ((dx != 0)?dx:View.TILE_WIDTH) - 1;
             ny -= ((dy != 0)?dy:View.TILE_HEIGHT) - 1;
 
             p = countTileByAbs(nx, ny);
 
             if (! tilesList.contains(p)) {
                 tilesList.add(p);
             }
 
             if (i == 1) {
                 lx = 1;
             } else if (i == 0) {
                 ly = 1;
             } else if (i == 2) {
                 ly = 0;
             }
         }
 
         return tilesList;
     }
 
     /*
      *         1
      *         ^
      *     8 < . > 2
      *         V
      *         4
      */
     public List<Point> getPlayerNeighbourTiles(Player currentPlayer, int direction) {
         List<Point> points = new LinkedList<Point>();
 
         List<Point> crossedTiles = getPlayerCrossedTiles(currentPlayer);
 
         int lx;
         int ly;
 
         Point nP;
         Point crossedP;
         for (int i = 0; i < crossedTiles.size(); i++) {
             crossedP = crossedTiles.get(i);
 
             int k = 0;
             int directionMask;
             for (int j = 0; j < 4; j++) {
 
                 directionMask = 0;
 
                 if (direction != 0) {
                     if ((direction & UP) != 0) {
                         ly = 1;
                         directionMask |= UP;
                     } else if ((direction & DOWN) != 0) {
                         ly = -1;
                         directionMask |= DOWN;
                     } else {
                         ly = 0;
                     }
 
                     if ((direction & RIGHT) != 0) {
                         lx = 1;
                         directionMask |= RIGHT;
                     } else if ((direction & LEFT) != 0) {
                         lx = -1;
                         directionMask |= LEFT;
                     } else {
                         lx = 0;
                     }
 
                     nP = new Point(crossedP.getX() + lx, crossedP.getY() + ly);
 
                     if (! crossedTiles.contains(nP) && ! points.contains(nP)) {
                         points.add(nP);
                     }
                 }
 
                 k++;
             }
         }
 
         return points;
     }
 
 //    private int[][] getPlayerTiles() {
 //        int[][] pTiles = new int[4][2];
 //
 //        int px = currentPlayer.getX();
 //        int py = currentPlayer.getY();
 //        int rx = View.TILE_XRADIUS;
 //        int ry = View.TILE_YRADIUS;
 //
 //        pTiles[0][0] = px - rx;
 //        pTiles[0][1] = py + ry;
 //        pTiles[1][0] = px + rx;
 //        pTiles[1][1] = py + ry;
 //        pTiles[2][0] = px + rx;
 //        pTiles[2][1] = py - ry;
 //        pTiles[3][0] = px - rx;
 //        pTiles[3][1] = py - ry;
 //
 //        return pTiles;
 //    }
 
 
     public int getPlayerX(String name) {
         return players.get(name).getX();
     }
 
     public int getPlayerY(String name) {
         return players.get(name).getY();
     }
 
     public float getPlayerSpeedX(String name) {
         return players.get(name).getSpeedX();
     }
 
     public float getPlayerSpeedY(String name) {
         return players.get(name).getSpeedY();
     }
 
     public boolean isPlayerWatchingRight(String name) {
         return players.get(name).isWatchingRight();
     }
 
     public String getPlayerName() {
         return currentPlayer.getName();
     }
 
     public void setPlayerName(String newName) {
         currentPlayer.setName(newName);
     }
 
     public void addPlayer(String name, int x, int y) {
         Player p = new Player(x, y);
         p.setName(name);
 
         players.put(name, p);
     }
 
     public Iterator<Player> getPlayersIterator() {
         return players.values().iterator();
     }
 
     public int getPlayersNum() {
         return players.size();
     }
 
     public Player getPlayer() {
         return currentPlayer;
     }
 
     public void addEventHandler(EventHandler handler) {
         handlers.add(handler);
     }
 
     public void removeEventHandler(EventHandler handler) {
         handlers.remove(handler);
     }
 
     public Player getPlayerByName(String name) {
         return players.get(name);
     }
 
     public void removeMainPlayer() {
         players.remove(currentPlayer.getName());
     }
 
     public Point countShoot(Point start, Point aim) {
         return countShoot(start, aim, true);
     }
 
     public Point countShoot(Point start, Point aim, boolean fillShooted) {
         boolean done = false;
 
         double cx = start.getX();
         double cy = start.getY();
         int sx = aim.getX();
         int sy = aim.getY();
 
         System.out.printf("Counting shoot from %.0f:%.0f to %d:%d%n", cx, cy, sx, sy);
 
         double c = Math.sqrt(Math.pow(sx - cx, 2) + Math.pow(sy - cy, 2));
 
         double cosf = (sx - cx) / c;
         double sinf = (sy - cy) / c;
 
         Point currTile = countTileByAbs((int)cx, (int)cy);
         Point prevTile;
 
         while (! done) {
             cx += TRACE_RAY_COEF * cosf;
             cy += TRACE_RAY_COEF * sinf;
 
             if (0 < cx && cx < getMapWidth() * View.TILE_WIDTH &&
                     0 < cy && cy < getMapHeight() * View.TILE_HEIGHT) {
                 prevTile = currTile;
                 currTile = countTileByAbs((int)cx, (int)cy);
 
                 for (Player p : players.values()) {
                     if (p != currentPlayer
                             && p.getX() <= cx && cx <= p.getX() + View.TILE_WIDTH/2
                             && p.getY() <= cy && cy <= p.getY() + View.TILE_HEIGHT/2) {
                         done = true;
                         if (fillShooted) {
                             hitedPlayer = p;
                         }
                     }
                 }
 
                 if (! currTile.equals(prevTile)) {
                     if (getTileAt(currTile.getX(), currTile.getY()) == '#') {
                         done = true;
                     }
                 }
             } else {
                 done = true;
             }
         }
 
         return new Point((int)cx, (int)cy);
     }
 
     public Point getPlayerPosition() {
         return currentPlayer.getPosition();
     }
 
     public EventHandler getGeneratedEventsEventHandler() {
         return generatedEventsEventHandler;
     }
 
     public void setGeneratedEventsEventHandler(EventHandler generatedEventsEventHandler) {
         this.generatedEventsEventHandler = generatedEventsEventHandler;
     }
 
     public String getWinnerName() {
         Player p = rate.getWinner();
 
         if (p != null) {
             return p.getName();
         } else {
             return "N/a";
         }
     }
 
     public int getWinnerScore() {
         return rate.getScore(rate.getWinner());
     }
 
     public int getScore(Player p) {
         return rate.getScore(p);
     }
 }
