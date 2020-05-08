 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.fourisland.frigidearth;
 
 import com.fourisland.frigidearth.mobs.Mouse;
 import com.fourisland.frigidearth.mobs.Rat;
 import com.fourisland.frigidearth.mobs.Spider;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * @author hatkirby
  */
 public class MapViewGameState implements GameState
 {
     private final int TILE_WIDTH = 12;
     private final int TILE_HEIGHT = 12;
     private final int MAP_WIDTH = 100;
     private final int MAP_HEIGHT = 100;
     private final int MESSAGE_HEIGHT = 6;
     private final int VIEWPORT_WIDTH = Main.CANVAS_WIDTH / TILE_WIDTH;
     private final int VIEWPORT_HEIGHT = Main.CANVAS_HEIGHT / TILE_HEIGHT - MESSAGE_HEIGHT;
     private final int MAX_ROOM_WIDTH = 13;
     private final int MIN_ROOM_WIDTH = 7;
     private final int MAX_ROOM_HEIGHT = 13;
     private final int MIN_ROOM_HEIGHT = 7;
     private final int MAX_CORRIDOR_LENGTH = 6;
     private final int MIN_CORRIDOR_LENGTH = 2;
     private final int[][] OCTET_MULTIPLIERS = new int[][] {new int[] {1,0,0,-1,-1,0,0,1}, new int[] {0,1,-1,0,0,-1,1,0}, new int[] {0,1,1,0,0,-1,-1,0}, new int[] {1,0,0,1,-1,0,0,-1}};
     private Tile[][] grid;
     private boolean[][] gridLighting;
     private String[] messages = new String[MESSAGE_HEIGHT];
     private List<Room> rooms = new ArrayList<Room>();
     private List<Mob> mobs = new ArrayList<Mob>();
     private int playerx = 4;
     private int playery = 4;
     private int viewportx = 0;
     private int viewporty = 0;
     private int health = 15;
     private int maxHealth = 15;
     private int defense = 0;
     private int keyx;
     private int keyy;
     private boolean haveKey = false;
     private boolean snowGrow = false;
     private int heartbeat = 0;
     private int floor;
     private int spawnTimer = 0;
     private int level = 1;
     private int experience = 0;
     
     public MapViewGameState(int floor)
     {
         this.floor = floor;
         
         grid = new Tile[MAP_WIDTH][MAP_HEIGHT];
         gridLighting = new boolean[MAP_WIDTH][MAP_HEIGHT];
         
         for (int x=0; x<MAP_WIDTH; x++)
         {
             for (int y=0; y<MAP_HEIGHT; y++)
             {
                 if ((x == 0) || (x == MAP_WIDTH-1) || (y == 0) || (y == MAP_HEIGHT-1))
                 {
                     grid[x][y] = Tile.StoneWall;
                 } else {
                     grid[x][y] = Tile.Unused;
                 }
             }
         }
         
         Direction keyRoomDirection = Direction.getRandomDirection();
         Rectangle legalBounds = null;
         switch (keyRoomDirection)
         {
             case North:
                 legalBounds = new Rectangle(0, 14, MAP_WIDTH, MAP_HEIGHT-14);
                 break;
                 
             case East:
                 legalBounds = new Rectangle(0, 0, MAP_WIDTH-14, MAP_HEIGHT);
                 break;
                 
             case South:
                 legalBounds = new Rectangle(0, 0, MAP_WIDTH, MAP_HEIGHT-14);
                 break;
                 
             case West:
                 legalBounds = new Rectangle(14, 0, MAP_WIDTH-14, MAP_HEIGHT);
                 break;
         }
         
         makeRoom(MAP_WIDTH/2, MAP_HEIGHT/2, Direction.getRandomDirection(), legalBounds);
         
         int currentFeatures = 1;
         int objects = 300;
         
         for (int countingTries = 0; countingTries < 1000; countingTries++)
         {
             if (currentFeatures == objects)
             {
                 break;
             }
             
             int newx = 0;
             int xmod = 0;
             int newy = 0;
             int ymod = 0;
             Direction validTile = null;
             for (int testing = 0; testing < 1000; testing++)
             {
                 newx = Functions.random(1, MAP_WIDTH-1);
                 newy = Functions.random(1, MAP_HEIGHT-1);
                 validTile = null;
                 
                 if ((grid[newx][newy] == Tile.DirtWall) || (grid[newx][newy] == Tile.Corridor))
                 {
                     if ((grid[newx][newy+1] == Tile.DirtFloor) || (grid[newx][newy+1] == Tile.Corridor))
                     {
                         validTile = Direction.North;
                         xmod = 0;
                         ymod = -1;
                     } else if ((grid[newx-1][newy] == Tile.DirtFloor) || (grid[newx-1][newy] == Tile.Corridor))
                     {
                         validTile = Direction.East;
                         xmod = 1;
                         ymod = 0;
                     } else if ((grid[newx][newy-1] == Tile.DirtFloor) || (grid[newx][newy-1] == Tile.Corridor))
                     {
                         validTile = Direction.South;
                         xmod = 0;
                         ymod = 1;
                     } else if ((grid[newx+1][newy] == Tile.DirtFloor) || (grid[newx+1][newy] == Tile.Corridor))
                     {
                         validTile = Direction.West;
                         xmod = -1;
                         ymod = 0;
                     }
                     
                     if (validTile != null)
                     {
                         if (grid[newx][newy+1] == Tile.Door)
                         {
                             validTile = null;
                         } else if (grid[newx-1][newy] == Tile.Door)
                         {
                             validTile = null;
                         } else if (grid[newx][newy-1] == Tile.Door)
                         {
                             validTile = null;
                         } else if (grid[newx+1][newy] == Tile.Door)
                         {
                             validTile = null;
                         }
                     }
                     
                     if (validTile != null)
                     {
                         break;
                     }
                 }
             }
             
             if (validTile != null)
             {
                 if (Functions.random(0, 100) <= 75)
                 {
                     if (makeRoom(newx+xmod, newy+ymod, validTile, legalBounds))
                     {
                         currentFeatures++;
                         grid[newx][newy] = Tile.Door;
                         grid[newx+xmod][newy+ymod] = Tile.DirtFloor;
                     }
                 } else {
                     if (makeCorridor(newx+xmod, newy+ymod, validTile))
                     {
                         currentFeatures++;
                         grid[newx][newy] = Tile.Door;
                     }
                 }
             }
         }
         
         int newx = 0;
         int newy = 0;
         int ways = 0;
         int state = 0;
         while (state != 10)
         {
             for (int testing = 0; testing < 1000; testing++)
             {
                 newx = Functions.random(1, MAP_WIDTH-1);
                 newy = Functions.random(1, MAP_HEIGHT-2);
                 ways = 4;
                 
                 for (Direction dir : Direction.values())
                 {
                     Point to = dir.to(new Point(newx, newy));
                     
                     if ((isValidPosition(to.x, to.y)) && (grid[to.x][to.y] == Tile.DirtFloor) || (grid[to.x][to.y] == Tile.Corridor))
                     {
                         ways--;
                     }
                 }
                 
                 if (state == 0)
                 {
                     if (ways == 0)
                     {
                         grid[newx][newy] = Tile.UpStairs;
                         state = 1;
                         break;
                     }
                 } else if (state == 1)
                 {
                     if (ways == 0)
                     {
                         grid[newx][newy] = Tile.DownStairs;
                         playerx=newx;
                         playery=newy;
                         state = 10;
                         break;
                     }
                 }
             }
         }
         
         // Create key room
         Room oRoom = null;
         for (Room r : rooms)
         {
             if (oRoom == null)
             {
                 oRoom = r;
             } else if ((keyRoomDirection == Direction.North) && (r.getY() < oRoom.getY()))
             {
                 oRoom = r;
             } else if ((keyRoomDirection == Direction.East) && (r.getX() > oRoom.getX()))
             {
                 oRoom = r;
             } else if ((keyRoomDirection == Direction.South) && (r.getY() > oRoom.getY()))
             {
                 oRoom = r;
             } else if ((keyRoomDirection == Direction.West) && (r.getX() < oRoom.getX()))
             {
                 oRoom = r;
             }
         }
         
         Room room = null;
         switch (keyRoomDirection)
         {
             case North:
                 room = new Room(oRoom.getX()+oRoom.getWidth()/2-3, oRoom.getY()-13, 7, 13, false);
                 break;
                 
             case East:
                 room = new Room(oRoom.getX()+oRoom.getWidth(), oRoom.getY()+oRoom.getHeight()/2-3, 13, 7, false);
                 break;
                 
             case South:
                 room = new Room(oRoom.getX()+oRoom.getWidth()/2-3, oRoom.getY()+oRoom.getHeight(), 7, 13, false);
                 break;
                 
             case West:
                 room = new Room(oRoom.getX()-13, oRoom.getY()+oRoom.getHeight()/2-3, 13, 7, false);
                 break;
         }
         
         for (int ytemp=room.getY(); ytemp < room.getY()+room.getHeight(); ytemp++)
         {
             for (int xtemp=room.getX(); xtemp < room.getX()+room.getWidth(); xtemp++)
             {
                 if (xtemp == room.getX())
                 {
                     if (((keyRoomDirection == Direction.North) || (keyRoomDirection == Direction.South)) && (ytemp % 2 == 0) && (ytemp != room.getY()))
                     {
                         grid[xtemp][ytemp] = Tile.Window;
                     } else {
                         grid[xtemp][ytemp] = Tile.DirtWall;
                     }
                 } else if (xtemp == room.getX()+room.getWidth()-1)
                 {
                     if (((keyRoomDirection == Direction.North) || (keyRoomDirection == Direction.South)) && (ytemp % 2 == 0) && (ytemp != (room.getY()+room.getHeight())))
                     {
                         grid[xtemp][ytemp] = Tile.Window;
                     } else {
                         grid[xtemp][ytemp] = Tile.DirtWall;
                     }
                 } else if (ytemp == room.getY())
                 {
                     if (((keyRoomDirection == Direction.West) || (keyRoomDirection == Direction.East)) && (xtemp % 2 == 0) && (xtemp != room.getX()))
                     {
                         grid[xtemp][ytemp] = Tile.Window;
                     } else {
                         grid[xtemp][ytemp] = Tile.DirtWall;
                     }
                 } else if (ytemp == room.getY()+room.getHeight()-1)
                 {
                     if (((keyRoomDirection == Direction.West) || (keyRoomDirection == Direction.East)) && (xtemp % 2 == 0) && (xtemp != (room.getX()+room.getWidth())))
                     {
                         grid[xtemp][ytemp] = Tile.Window;
                     } else {
                         grid[xtemp][ytemp] = Tile.DirtWall;
                     }
                 } else {
                     grid[xtemp][ytemp] = Tile.DirtFloor;
                 }
             }
         }
         
         switch (keyRoomDirection)
         {
             case North:
                 grid[room.getX()+room.getWidth()/2][room.getY()+room.getHeight()] = Tile.Door;
                 grid[room.getX()+room.getWidth()/2][room.getY()+room.getHeight()-1] = Tile.DirtFloor;
                 keyx = room.getX()+3;
                 keyy = room.getY()+3;
                 break;
                 
             case East:
                 grid[room.getX()-1][room.getY()+room.getHeight()/2] = Tile.Door;
                 grid[room.getX()][room.getY()+room.getHeight()/2] = Tile.DirtFloor;
                 keyx = room.getX()+10;
                 keyy = room.getY()+3;
                 break;
                 
             case South:
                 grid[room.getX()+room.getWidth()/2][room.getY()-1] = Tile.Door;
                 grid[room.getX()+room.getWidth()/2][room.getY()] = Tile.DirtFloor;
                 keyx = room.getX()+3;
                 keyy = room.getY()+10;
                 break;
                 
             case West:
                 grid[room.getX()+room.getWidth()][room.getY()+room.getHeight()/2] = Tile.Door;
                 grid[room.getX()+room.getWidth()-1][room.getY()+room.getHeight()/2] = Tile.DirtFloor;
                 keyx = room.getX()+3;
                 keyy = room.getY()+3;
                 break;
         }
         
         rooms.add(room);
         
         adjustViewport();
         calculateFieldOfView();
     }
     
     private boolean makeRoom(int x, int y, Direction direction, Rectangle legalBounds)
     {
         int width = Functions.random(MIN_ROOM_WIDTH, MAX_ROOM_WIDTH);
         int height = Functions.random(MIN_ROOM_HEIGHT, MAX_ROOM_HEIGHT);
         Room room = null;
         Rectangle bounds = null;
         
         if (legalBounds == null)
         {
             bounds = new Rectangle(0, 0, MAP_WIDTH, MAP_HEIGHT);
         } else {
             bounds = legalBounds;
         }
         
         switch (direction)
         {
             case North:
                 room = new Room(x-width/2, y-height, width+1, height+1, true);
                 
                 break;
                 
             case East:
                 room = new Room(x, y-height/2, width+1, height+1, true);
                 
                 break;
                 
             case South:
                 room = new Room(x-width/2, y, width+1, height+1, true);
                 
                 break;
                 
             case West:
                 room = new Room(x-width, y-height/2, width+1, height+1, true);
                 
                 break;
         }
         
         for (int ytemp=room.getY(); ytemp < room.getY()+room.getHeight(); ytemp++)
         {
             if ((ytemp < bounds.y) || (ytemp > bounds.y+bounds.height ))
             {
                 return false;
             }
 
             for (int xtemp=room.getX(); xtemp < room.getX()+room.getWidth(); xtemp++)
             {
                 if ((xtemp < bounds.x) || (xtemp > bounds.x+bounds.width))
                 {
                     return false;
                 }
 
                 if (grid[xtemp][ytemp] != Tile.Unused)
                 {
                     return false;
                 }
             }
         }
 
         for (int ytemp=room.getY(); ytemp < room.getY()+room.getHeight(); ytemp++)
         {
             for (int xtemp=room.getX(); xtemp < room.getX()+room.getWidth(); xtemp++)
             {
                 if (xtemp == room.getX())
                 {
                     grid[xtemp][ytemp] = Tile.DirtWall;
                 } else if (xtemp == room.getX()+room.getWidth()-1)
                 {
                     grid[xtemp][ytemp] = Tile.DirtWall;
                 } else if (ytemp == room.getY())
                 {
                     grid[xtemp][ytemp] = Tile.DirtWall;
                 } else if (ytemp == room.getY()+room.getHeight()-1)
                 {
                     grid[xtemp][ytemp] = Tile.DirtWall;
                 } else {
                     grid[xtemp][ytemp] = Tile.DirtFloor;
                 }
             }
         }
         
         rooms.add(room);
         
         // Spawn some random monsters
         int perf = 60;
         for (;;)
         {
             if (Functions.random(0, 100) < perf)
             {
                 perf /= 2;
                 mobs.add(createInDepthMonster(room));
             } else {
                 break;
             }
         }
         
         return true;
     }
     
     private boolean makeCorridor(int x, int y, Direction direction)
     {
         int length = Functions.random(MIN_CORRIDOR_LENGTH, MAX_CORRIDOR_LENGTH);
         
         int xtemp = 0;
         int ytemp = 0;
         
         switch (direction)
         {
             case North:
                 if ((x < 0) || (x > MAP_WIDTH))
                 {
                     return false;
                 } else {
                     xtemp = x;
                 }
                 
                 for (ytemp = y; ytemp > (y-length); ytemp--)
                 {
                     if ((ytemp < 0) || (ytemp > MAP_HEIGHT))
                     {
                         return false;
                     }
                     
                     if (grid[xtemp][ytemp] != Tile.Unused)
                     {
                         return false;
                     }
                 }
                 
                 for (ytemp = y; ytemp > (y-length); ytemp--)
                 {
                     grid[xtemp][ytemp] = Tile.Corridor;
                 }
                 
                 break;
                 
             case East:
                 if ((y < 0) || (y > MAP_HEIGHT))
                 {
                     return false;
                 } else {
                     ytemp = y;
                 }
                 
                 for (xtemp = x; xtemp < (x+length); xtemp++)
                 {
                     if ((xtemp < 0) || (xtemp > MAP_WIDTH))
                     {
                         return false;
                     }
                     
                     if (grid[xtemp][ytemp] != Tile.Unused)
                     {
                         return false;
                     }
                 }
                 
                 for (xtemp = x; xtemp < (x+length); xtemp++)
                 {
                     grid[xtemp][ytemp] = Tile.Corridor;
                 }
                 
                 break;
                 
             case South:
                 if ((x < 0) || (x > MAP_WIDTH))
                 {
                     return false;
                 } else {
                     xtemp = x;
                 }
                 
                 for (ytemp = y; ytemp < (y+length); ytemp++)
                 {
                     if ((ytemp < 0) || (ytemp > MAP_HEIGHT))
                     {
                         return false;
                     }
                     
                     if (grid[xtemp][ytemp] != Tile.Unused)
                     {
                         return false;
                     }
                 }
                 
                 for (ytemp = y; ytemp < (y+length); ytemp++)
                 {
                     grid[xtemp][ytemp] = Tile.Corridor;
                 }
                 
                 break;
                 
             case West:
                 if ((y < 0) || (y > MAP_HEIGHT))
                 {
                     return false;
                 } else {
                     ytemp = y;
                 }
                 
                 for (xtemp = x; xtemp > (x-length); xtemp--)
                 {
                     if ((xtemp < 0) || (xtemp > MAP_WIDTH))
                     {
                         return false;
                     }
                     
                     if (grid[xtemp][ytemp] != Tile.Unused)
                     {
                         return false;
                     }
                 }
                 
                 for (xtemp = x; xtemp > (x-length); xtemp--)
                 {
                     grid[xtemp][ytemp] = Tile.Corridor;
                 }
                 
                 break;
         }
         
         return true;
     }
     
     private void calculateFieldOfView()
     {
         for (int x=0; x<MAP_WIDTH; x++)
         {
             for (int y=0; y<MAP_HEIGHT; y++)
             {
                 gridLighting[x][y] = false;
             }
         }
         
         for (int i=0; i<8; i++)
         {
             castLight(playerx, playery, 1, 1.0, 0.0, Math.max(VIEWPORT_WIDTH/2, VIEWPORT_HEIGHT/2), OCTET_MULTIPLIERS[0][i], OCTET_MULTIPLIERS[1][i], OCTET_MULTIPLIERS[2][i], OCTET_MULTIPLIERS[3][i], 0);
         }
     }
     
     private void castLight(int cx, int cy, int row, double start, double end, int radius, int xx, int xy, int yx, int yy, int id)
     {
         if (start < end)
         {
             return;
         }
         
         int r2 = radius * radius;
         for (int j=row; j<radius+1; j++)
         {
             int dx = -j-1;
             int dy = -j;
             boolean blocked = false;
             double newStart = 0.0;
             
             while (dx <= 0)
             {
                 dx++;
                 
                 int x = cx + dx*xx + dy*xy;
                 int y = cy + dx*yx + dy*yy;
                 double l_slope = ((double)dx-0.5)/((double)dy+0.5);
                 double r_slope = ((double)dx+0.5)/((double)dy-0.5);
                 
                 if (start < r_slope)
                 {
                     continue;
                 } else if (end > l_slope)
                 {
                     break;
                 } else {
                     if ((dx*dx + dy*dy) < r2)
                     {
                         gridLighting[x][y] = true;
                     }
                     
                     if (blocked)
                     {
                         if (grid[x][y].isBlocked())
                         {
                             newStart = r_slope;
                             continue;
                         } else {
                             blocked = false;
                             start = newStart;
                         }
                     } else {
                         if ((grid[x][y].isBlocked()) && (j < radius))
                         {
                             blocked = true;
                             castLight(cx, cy, j+1, start, l_slope, radius, xx, xy, yx, yy, id+1);
                             newStart = r_slope;
                         }
                     }
                 }
             }
             
             if (blocked)
             {
                 break;
             }
         }
     }
 
     public void render(Graphics2D g)
     {
         // Render tiles
         for (int x=viewportx; x<viewportx+VIEWPORT_WIDTH; x++)
         {
             for (int y=viewporty; y<viewporty+VIEWPORT_HEIGHT; y++)
             {
                 if (gridLighting[x][y])
                 {
                     char displayChar = grid[x][y].getDisplayCharacter();
                     Color displayColor = grid[x][y].getBackgroundColor();
 
                     if (!displayColor.equals(Color.BLACK))
                     {
                         g.setColor(displayColor);
                         g.fillRect((x-viewportx)*TILE_WIDTH, (y-viewporty)*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
                     }
 
                     if (displayChar != ' ')
                     {
                         g.drawImage(SystemFont.getCharacter(grid[x][y].getDisplayCharacter(), Color.WHITE), (x-viewportx)*TILE_WIDTH, (y-viewporty)*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT, null);
                     }
                 }
             }
         }
         
         // Render mobs
         for (Mob mob : mobs)
         {
             if ((gridLighting[mob.x][mob.y]) && (isInViewport(mob.x, mob.y)))
             {
                 g.drawImage(SystemFont.getCharacter(mob.getDisplayCharacter(), mob.getDisplayColor()), (mob.x-viewportx)*TILE_WIDTH, (mob.y-viewporty)*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT, null);
             }
         }
         
         // Render key
         if ((!haveKey) && (gridLighting[keyx][keyy]))
         {
             g.drawImage(SystemFont.getCharacter('k', Color.YELLOW), (keyx-viewportx)*TILE_WIDTH, (keyy-viewporty)*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT, null);
         }
         
         // Render player
         g.drawImage(SystemFont.getCharacter('@', Color.WHITE), (playerx-viewportx)*TILE_WIDTH, (playery-viewporty)*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT, null);
         
         // Render messages
         g.setColor(new Color(53, 63, 62));
         g.fillRect(0, VIEWPORT_HEIGHT*TILE_HEIGHT, Main.CANVAS_WIDTH, TILE_HEIGHT*MESSAGE_HEIGHT);
         
         for (int i=0; i<MESSAGE_HEIGHT; i++)
         {
             if (messages[i] != null)
             {
                 for (int j=0; j<messages[i].length(); j++)
                 {
                     g.drawImage(SystemFont.getCharacter(messages[i].charAt(j), Color.WHITE), j*TILE_WIDTH, (VIEWPORT_HEIGHT+i)*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT, null);
                 }
             }
         }
         
         // Render status bar
         g.drawImage(SystemFont.getCharacter((char) 3, Color.RED), TILE_WIDTH, 0, TILE_WIDTH, TILE_HEIGHT, null);
         String healthText = Integer.toString(health);
         double healthPercentage = ((double) health) / ((double) maxHealth);
         Color healthColor = Color.WHITE;
         if (healthPercentage < 0.2)
         {
             healthColor = Color.RED;
         } else if (healthPercentage < 0.55)
         {
             healthColor = Color.YELLOW;
         } else if (healthPercentage < 1)
         {
             healthColor = Color.GREEN;
         }
         
         for (int i=0; i<healthText.length(); i++)
         {
             g.drawImage(SystemFont.getCharacter(healthText.charAt(i), healthColor), (i+2)*TILE_WIDTH, 0, TILE_WIDTH, TILE_HEIGHT, null);
         }
         
         g.drawImage(SystemFont.getCharacter((char) 5, Color.GRAY), (healthText.length()+3)*TILE_WIDTH, 0, TILE_WIDTH, TILE_HEIGHT, null);
         int b = healthText.length()+4;
         String defenseText = Integer.toString(defense);
         for (int i=0; i<defenseText.length(); i++)
         {
             g.drawImage(SystemFont.getCharacter(defenseText.charAt(i), Color.WHITE), (i+b)*TILE_WIDTH, 0, TILE_WIDTH, TILE_HEIGHT, null);
         }
         
         b+=defenseText.length()+1;
         g.drawImage(SystemFont.getCharacter('E', Color.WHITE), (b)*TILE_WIDTH, 0, TILE_WIDTH, TILE_HEIGHT, null);
         g.drawImage(SystemFont.getCharacter('X', Color.WHITE), (b+1)*TILE_WIDTH, 0, TILE_WIDTH, TILE_HEIGHT, null);
         g.drawImage(SystemFont.getCharacter('P', Color.WHITE), (b+2)*TILE_WIDTH, 0, TILE_WIDTH, TILE_HEIGHT, null);
         b+=3;
         String expText = Functions.padLeft(Integer.toString(experience), 3, '0');
         for (int i=0; i<expText.length(); i++)
         {
             g.drawImage(SystemFont.getCharacter(expText.charAt(i), Color.WHITE), (i+b)*TILE_WIDTH, 0, TILE_WIDTH, TILE_HEIGHT, null);
         }
         b+=expText.length();
         g.drawImage(SystemFont.getCharacter(':', Color.WHITE), (b)*TILE_WIDTH, 0, TILE_WIDTH, TILE_HEIGHT, null);
         b++;
         String levelText = Integer.toString(level);
         for (int i=0; i<levelText.length(); i++)
         {
             g.drawImage(SystemFont.getCharacter(levelText.charAt(i), Color.WHITE), (i+b)*TILE_WIDTH, 0, TILE_WIDTH, TILE_HEIGHT, null);
         }
     }
     
     public void processInput(KeyEvent e)
     {
         // Handle input
         switch (e.getKeyCode())
         {
             case KeyEvent.VK_LEFT:
             case KeyEvent.VK_RIGHT:
             case KeyEvent.VK_UP:
             case KeyEvent.VK_DOWN:
                 Direction dir = Direction.fromKeyEvent(e);
                 Point to = dir.to(new Point(playerx, playery));
                 
                 if ((isValidPosition(to.x,to.y)) && (!grid[to.x][to.y].isBlocked()))
                 {
                     // Check for mobs
                     boolean foundMob = false;
                     for (Mob mob : mobs)
                     {
                         if (mob.getPosition().equals(to))
                         {
                             printMessage("You hit the " + mob.getName().toLowerCase());
                             mob.health -= level;
                             
                             if (mob.health <= 0)
                             {
                                 printMessage("You killed the " + mob.getName().toLowerCase() + "!");
                                 experience += (mob.getBaseExperience()/level);
                                 mobs.remove(mob);
                                 
                                if (experience >= 1000)
                                 {
                                     level++;
                                     experience -= 1000;
                                     
                                     printMessage("You grow to level " + level + "!");
                                 }
                             }
                             
                             foundMob = true;
                             break;
                         }
                     }
                     
                     if (!foundMob)
                     {
                         playerx = to.x;
                         playery = to.y;
                     }
                 } else {
                     printMessage("Blocked: " + dir.name());
                     
                     return;
                 }
                 
                 break;
                 
             case KeyEvent.VK_G:
                 if ((playerx == keyx) && (playery == keyy) && (!haveKey))
                 {
                     haveKey = true;
                     printMessage("You get the key");
                     printMessage("All the windows in the room shatter!");
                     
                     for (int x=0; x<MAP_WIDTH; x++)
                     {
                         for (int y=0; y<MAP_HEIGHT; y++)
                         {
                             if (grid[x][y] == Tile.Window)
                             {
                                 grid[x][y] = Tile.ShatteredWindow;
                             }
                         }
                     }
                 }
                 
                 break;
                 
             case KeyEvent.VK_W:
                 // Wait a turn
                 break;
                 
             default:
                 return;
         }
         
         // Move mobs
         for (Mob mob : mobs)
         {
             // If the mob is hostile, it should move toward the player IF IT CAN SEE the player
             // Also, if it is adjacent to the player, it should attack
             if ((mob.hostile) && (canSeePlayer(mob.x, mob.y)))
             {
                 if (arePointsAdjacent(playerx, playery, mob.x, mob.y, false))
                 {
                     // Attack!
                     health -= (mob.getAttackPower() - defense);
                     printMessage(mob.getBattleMessage());
                 } else {
                     List<Direction> path = findPath(mob.getPosition(), new Point(playerx, playery));
                     
                     if (path != null)
                     {
                         mob.moveInDirection(path.get(0));
                     }
                 }
             } else {
                 // If the mob isn't hostile, it should just move around randomly
                 Direction toDir = null;
 
                 for (int i=0; i<10; i++)
                 {
                     toDir = Direction.getRandomDirection();
                     Point to = toDir.to(mob.getPosition());
                     if ((isValidPosition(to.x,to.y)) && (!grid[to.x][to.y].isBlocked()) && (!to.equals(new Point(playerx, playery))))
                     {
                         mob.moveInDirection(toDir);
                         break;
                     }
                 }
             }
         }
         
         // Move snow
         if (snowGrow)
         {
             for (int x=0; x<MAP_WIDTH; x++)
             {
                 for (int y=0; y<MAP_HEIGHT; y++)
                 {
                     if (grid[x][y] == Tile.Snow)
                     {
                         for (Direction d : Direction.values())
                         {
                             Point to = d.to(new Point(x, y));
                             if ((!grid[to.x][to.y].isBlocked()) && (grid[to.x][to.y] != Tile.Snow) && (grid[to.x][to.y] != Tile.UpStairs))
                             {
                                 grid[to.x][to.y] = Tile.SnowTemp;
                             }
                         }
                     }
                 }
             }
 
             for (int x=0; x<MAP_WIDTH; x++)
             {
                 for (int y=0; y<MAP_HEIGHT; y++)
                 {
                     if (grid[x][y] == Tile.ShatteredWindow)
                     {
                         for (Direction d : Direction.values())
                         {
                             Point to = d.to(new Point(x, y));
                             if ((!grid[to.x][to.y].isBlocked()) && (grid[to.x][to.y] != Tile.Snow))
                             {
                                 grid[to.x][to.y] = Tile.Snow;
                             }
                         }
                     } else if (grid[x][y] == Tile.SnowTemp)
                     {
                         grid[x][y] = Tile.Snow;
                     }
                 }
             }
         }
         
         snowGrow = !snowGrow;
         
         // Heartbeat
         if (heartbeat % 2 == 0)
         {
             if (grid[playerx][playery] == Tile.Snow)
             {
                 health--;
             } else if (heartbeat == Functions.random(0, 3)) {
                 if (health < maxHealth)
                 {
                     health++;
                 }
             }
         }
         
         heartbeat++;
         if (heartbeat == 4) heartbeat = 0;
         
         // Spawn mobs
         if (spawnTimer == 10)
         {
             spawnTimer = 0;
             
             Room r = rooms.get(Functions.random(0, rooms.size()-1));
             if (r.canGenerateMonsters())
             {
                 Mob m = createInDepthMonster(r);
                 if (!gridLighting[m.x][m.y])
                 {
                     mobs.add(m);
                 }
             }
         } else {
             spawnTimer++;
         }
         
         // Do viewport stuff
         adjustViewport();
         calculateFieldOfView();
         
         // Handle death
         if (health <= 0)
         {
             printMessage("You have died! Press [ENTER] to quit.");
             
             Main.addInputable(new Inputable() {
                 @Override
                 public void processInput(KeyEvent e)
                 {
                     if (e.getKeyCode() == KeyEvent.VK_ENTER)
                     {
                         System.exit(0);
                     }
                 }
             });
         }
     }
     
     private void adjustViewport()
     {
         if (playerx > (VIEWPORT_WIDTH/2))
         {
             if (playerx < (MAP_WIDTH - (VIEWPORT_WIDTH/2-1)))
             {
                 viewportx = playerx - (VIEWPORT_WIDTH/2);
             } else {
                 viewportx = MAP_WIDTH - VIEWPORT_WIDTH;
             }
         } else {
             viewportx = 0;
         }
         
         if (playery > (VIEWPORT_HEIGHT/2))
         {
             if (playery < (MAP_HEIGHT - (VIEWPORT_HEIGHT/2-1)))
             {
                 viewporty = playery - (VIEWPORT_HEIGHT/2);
             } else {
                 viewporty = MAP_HEIGHT - VIEWPORT_HEIGHT;
             }
         } else {
             viewporty = 0;
         }
     }
     
     private boolean isValidPosition(int x, int y)
     {
         if (x < 0) return false;
         if (x > MAP_WIDTH) return false;
         if (y < 0) return false;
         if (y > MAP_HEIGHT) return false;
         
         return true;
     }
     
     private boolean isInViewport(int x, int y)
     {
         if (x < viewportx) return false;
         if (x > viewportx+VIEWPORT_WIDTH) return false;
         if (y < viewporty) return false;
         if (y > viewporty+VIEWPORT_HEIGHT) return false;
         
         return true;
     }
     
     private void printMessage(String message)
     {
         String temp = message;
         
         while (temp.length() > VIEWPORT_WIDTH)
         {
             String shortm = temp.substring(0, VIEWPORT_WIDTH);
             
             if ((temp.charAt(VIEWPORT_WIDTH) == ' ') || (shortm.endsWith(" ")))
             {
                 pushUpMessages(shortm);
                 temp = temp.substring(VIEWPORT_WIDTH);
             } else {
                 int lastSpace = shortm.lastIndexOf(" ");
                 pushUpMessages(shortm.substring(0, lastSpace));
                 temp = temp.substring(lastSpace);
             }
         }
         
         pushUpMessages(temp);
     }
     
     private void pushUpMessages(String message)
     {
         for (int i=1; i<MESSAGE_HEIGHT; i++)
         {
             messages[i-1] = messages[i];
         }
         
         messages[MESSAGE_HEIGHT-1] = message;
     }
     
     private boolean canSeePlayer(int mx, int my)
     {
         int dx = playerx - mx;
         int dy = playery - my;
         int ax = Math.abs(dx) << 1;
         int ay = Math.abs(dy) << 1;
         int sx = (int) Math.signum(dx);
         int sy = (int) Math.signum(dy);
         int x = mx;
         int y = my;
         
         if (ax > ay)
         {
             int t = ay - (ax >> 1);
             
             do
             {
                 if (t >= 0)
                 {
                     y += sy;
                     t -= ax;
                 }
                 
                 x += sx;
                 t += ay;
                 
                 if ((x == playerx) && (y == playery))
                 {
                     return true;
                 }
             } while (!grid[x][y].isBlocked());
             
             return false;
         } else {
             int t = ax - (ay >> 1);
             
             do
             {
                 if (t >= 0)
                 {
                     x += sx;
                     t -= ay;
                 }
                 
                 y += sy;
                 t += ax;
                 
                 if ((x == playerx) && (y == playery))
                 {
                     return true;
                 }
             } while (!grid[x][y].isBlocked());
             
             return false;
         }
     }
     
     private boolean arePointsAdjacent(int px, int py, int mx, int my, boolean includeDiagonals)
     {
         if (mx == (px-1))
         {
             if ((my == (py-1)) && (includeDiagonals)) return true;
             if (my == py) return true;
             if ((my == (py+1)) && (includeDiagonals)) return true;
         } else if (mx == px)
         {
             if (my == (py-1)) return true;
             if (my == (py+1)) return true;
         } else if (mx == (px+1))
         {
             if ((my == (py-1)) && (includeDiagonals)) return true;
             if (my == py) return true;
             if ((my == (py+1)) && (includeDiagonals)) return true;
         }
         
         return false;
     }
     
     private List<Direction> findPath(Point from, Point to)
     {
         return findPath(from, to, new ArrayList<Point>());
     }
     
     private List<Direction> findPath(Point from, Point to, List<Point> attempts)
     {
         /* Iterate over all of the directions and check if moving in that
          * direction would result in the destination position. If so, the
          * correct path has been acquired and thus we can return. */
         for (Direction d : Direction.values())
         {
             Point loc = d.to(from);
             if (to.equals(loc))
             {
                 List<Direction> moves = new ArrayList<Direction>();
                 moves.add(d);
                 
                 return moves;
             }
         }
         
         /* Calculate the directions to attempt and the order in which to do so
          * based on proximity to the destination */
         List<Direction> ds = new ArrayList<Direction>();
         for (Direction d : Direction.values())
         {
             Point loc = d.to(from);
             if ((isValidPosition(loc.x, loc.y)) && (!grid[loc.x][loc.y].isBlocked()))
             {
                 ds.add(d);
             }
         }
         
         List<Direction> tempd = new ArrayList<Direction>();
         
         if (to.x < from.x)
         {
             tempd.add(Direction.West);
         } else if (to.x > from.x)
         {
             tempd.add(Direction.East);
         } else {
             if (!ds.contains(Direction.North) || !ds.contains(Direction.South))
             {
                 tempd.add(Direction.West);
                 tempd.add(Direction.East);
             }
         }
         
         if (to.y < from.y)
         {
             tempd.add(Direction.North);
         } else if (to.y > from.y)
         {
             tempd.add(Direction.South);
         } else {
             if (!ds.contains(Direction.West) || !ds.contains(Direction.East))
             {
                 tempd.add(Direction.North);
                 tempd.add(Direction.South);
             }
         }
         
         // Remove calculated directions that aren't legal movements
         tempd.retainAll(ds);
         
         // Randomize directions so movement is more fluid
         Collections.shuffle(tempd);
         
         // Iterate over the suggested directions
         for (Direction d : tempd)
         {
             /* If the position in the suggested direction has not already been
              * covered, recursively search from the new position */
             Point loc = d.to(from);
             if (!attempts.contains(loc))
             {
                 attempts.add(loc);
                 
                 List<Direction> moves = findPath(loc, to, attempts);
                 if (moves != null)
                 {
                     moves.add(0, d);
                     return moves;
                 }
             }
         }
         
         return null;
     }
     
     private Mob createInDepthMonster(Room r)
     {
         int x = Functions.random(r.getX()+1, r.getX()+r.getWidth()-2);
         int y = Functions.random(r.getY()+1, r.getY()+r.getHeight()-2);
         
         List<Class> mobTypes = new ArrayList<Class>();
         switch (floor)
         {
             case 10:
             case 9:
             case 8:
             case 7:
             case 6:
             case 5:
             case 4:
             case 3:
             case 2:
             case 1:
                 mobTypes.add(Mouse.class);
                 mobTypes.add(Rat.class);
                 mobTypes.add(Spider.class);
         }
         
         try
         {
             return (Mob) mobTypes.get(Functions.random(0, mobTypes.size()-1)).getConstructor(int.class, int.class).newInstance(x, y);
         } catch (Exception ex)
         {
             return null;
         }
     }
 }
