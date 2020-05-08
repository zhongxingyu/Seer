 package org.glowacki.core.test;
 
 import org.glowacki.core.CoreException;
 import org.glowacki.core.Direction;
 import org.glowacki.core.ICharacter;
 import org.glowacki.core.Level;
 
 public class MockCharacter
     implements ICharacter
 {
     private String name;
     private boolean player;
     private int x;
     private int y;
     private Level level;
 
     public MockCharacter(String name)
     {
         this(name, false);
     }
 
     public MockCharacter(String name, boolean player)
     {
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
 
     public int getX()
     {
         return x;
     }
 
     public int getY()
     {
         return y;
     }
 
     public boolean isPlayer()
     {
         return player;
     }
 
     public int move(Direction dir)
         throws CoreException
     {
        throw new UnimplementedError();
     }
 
     public void setLevel(Level level)
     {
         this.level = level;
     }
 
     public void setPosition(int x, int y)
     {
         this.x = x;
         this.y = y;
     }
 
     public void takeTurn()
     {
        throw new UnimplementedError();
     }
 
     public String toString()
     {
         return "Mock:" + name;
     }
 }
