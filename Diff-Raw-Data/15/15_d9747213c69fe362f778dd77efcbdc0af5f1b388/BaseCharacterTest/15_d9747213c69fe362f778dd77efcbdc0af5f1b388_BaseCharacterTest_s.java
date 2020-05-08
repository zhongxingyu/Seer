 package org.glowacki.core;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 import org.glowacki.core.test.MapBuilder;
 import org.glowacki.core.test.MockCharacter;
 
 class MyCharacter
     extends BaseCharacter
 {
     private String name;
     private Level level;
     private boolean player;
 
     MyCharacter(String name, int str, int dex, int spd)
     {
         this(name, str, dex, spd, false);
     }
 
     MyCharacter(String name, int str, int dex, int spd, boolean player)
     {
         super(str, dex, spd);
 
         this.name = name;
         this.player = player;
     }
 
     public Level getLevel()
     {
         return level;
     }
 
     public String getName()
     {
         return name;
     }
 
     public boolean isPlayer()
     {
         return player;
     }
 
     public int move(Direction dir)
         throws CoreException
     {
        throw new Error("Unimplemented");
     }
 
     public void setLevel(Level level)
     {
         this.level = level;
     }
 
     public void takeTurn()
     {
        throw new Error("Unimplemented");
     }
 }
 
 class MockMap
     implements IMap
 {
     private int maxX;
     private int maxY;
     private Terrain terrain;
 
     MockMap(int maxX, int maxY)
     {
         this.maxX = maxX;
         this.maxY = maxY;
     }
 
     public int getMaxX()
     {
         return maxX;
     }
 
     public int getMaxY()
     {
         return maxY;
     }
 
     public Terrain getTerrain(int x, int y)
         throws MapException
     {
         return terrain;
     }
 
     public void moveTo(ICharacter ch, int x, int y)
         throws MapException
     {
         // do nothing
     }
 
     public void setTerrain(Terrain t)
     {
         terrain = t;
     }
 }
 
 public class BaseCharacterTest
     extends TestCase
 {
     public BaseCharacterTest(String name)
     {
         super(name);
     }
 
     public static Test suite()
     {
         return new TestSuite(BaseCharacterTest.class);
     }
 
     public void testCreate()
     {
         MyCharacter ch = new MyCharacter("a", 1, 2, 3);
         assertEquals("Bad initial X", -1, ch.getX());
         assertEquals("Bad initial Y", -1, ch.getY());
 
         final int x = 2;
         final int y = 2;
 
         ch.setPosition(x, y);
         assertEquals("Bad X", x, ch.getX());
         assertEquals("Bad Y", y, ch.getY());
     }
 
     public void testBadMove00()
         throws CoreException
     {
         MyCharacter ch = new MyCharacter("b", 1, 2, 3);
 
         MockMap map = new MockMap(3, 3);
         map.setTerrain(Terrain.FLOOR);
 
         Direction dir = Direction.LEFT;
         do {
             int x = 0;
             int y = 0;
 
             ch.setPosition(x, y);
 
             int expTurns = -1;
             switch (dir) {
             case LEFT:
                 break;
             case LEFT_UP:
                 break;
             case UP:
                 break;
             case RIGHT_UP:
                 break;
             case RIGHT:
                 x++;
                 expTurns = 4;
                 break;
             case RIGHT_DOWN:
                 x++;
                 y++;
                 expTurns = 5;
                 break;
             case DOWN:
                 y++;
                 expTurns = 3;
                 break;
             case LEFT_DOWN:
                 break;
             }
 
             int turns = ch.move(map, dir);
             assertEquals("Bad " + dir + " X", x, ch.getX());
             assertEquals("Bad " + dir + " Y", y, ch.getY());
             assertEquals("Bad " + dir + " number of turns", expTurns, turns);
             dir = dir.next();
         } while (dir != Direction.LEFT);
     }
 
     public void testBadMove22()
         throws CoreException
     {
         MyCharacter ch = new MyCharacter("b", 1, 2, 3);
 
         MockMap map = new MockMap(3, 3);
         map.setTerrain(Terrain.FLOOR);
 
         Direction dir = Direction.LEFT;
         do {
             int x = 3;
             int y = 3;
 
             ch.setPosition(x, y);
 
             int expTurns = -1;
             switch (dir) {
             case LEFT:
                 x--;
                 expTurns = 4;
                 break;
             case LEFT_UP:
                 x--;
                 y--;
                 expTurns = 5;
                 break;
             case UP:
                 y--;
                 expTurns = 3;
                 break;
             case RIGHT_UP:
                 break;
             case RIGHT:
                 break;
             case RIGHT_DOWN:
                 break;
             case DOWN:
                 break;
             case LEFT_DOWN:
                 break;
             }
 
             int turns = ch.move(map, dir);
             assertEquals("Bad " + dir + " X", x, ch.getX());
             assertEquals("Bad " + dir + " Y", y, ch.getY());
             assertEquals("Bad " + dir + " number of turns", expTurns, turns);
             dir = dir.next();
         } while (dir != Direction.LEFT);
     }
 
     public void testMove()
         throws CoreException
     {
         MyCharacter ch = new MyCharacter("b", 1, 2, 3);
 
         MockMap map = new MockMap(3, 3);
         map.setTerrain(Terrain.FLOOR);
 
         Direction dir = Direction.LEFT;
         do {
             int x = 2;
             int y = 2;
 
             ch.setPosition(x, y);
 
             switch (dir) {
             case LEFT:
                 x--;
                 break;
             case LEFT_UP:
                 x--;
                 y--;
                 break;
             case UP:
                 y--;
                 break;
             case RIGHT_UP:
                 x++;
                 y--;
                 break;
             case RIGHT:
                 x++;
                 break;
             case RIGHT_DOWN:
                 x++;
                 y++;
                 break;
             case DOWN:
                 y++;
                 break;
             case LEFT_DOWN:
                 x--;
                 y++;
                 break;
             }
 
             ch.move(map, dir);
             assertEquals("Bad X", x, ch.getX());
             assertEquals("Bad Y", y, ch.getY());
 
             dir = dir.next();
         } while (dir != Direction.LEFT);
     }
 
     public static void main(String[] args)
     {
         TestRunner.run(suite());
     }
 }
