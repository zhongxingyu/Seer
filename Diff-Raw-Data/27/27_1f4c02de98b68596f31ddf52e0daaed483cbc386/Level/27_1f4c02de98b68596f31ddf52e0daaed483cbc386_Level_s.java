 package org.glowacki.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 class LevelException
     extends CoreException
 {
     LevelException(String msg)
     {
         super(msg);
     }
 }
 
 public class Level
 {
     private String name;
     private Map map;
 
     private Level prevLevel;
     private Level nextLevel;
 
     private List<ICharacter> players = new ArrayList<ICharacter>();
     private List<ICharacter> nonplayers = new ArrayList<ICharacter>();
 
     public Level(String name, Map map)
     {
         this.name = name;
         this.map = map;
     }
 
     public void addNextLevel(Level lvl)
    {
        this.nextLevel = lvl;
     }
 
     public void addNonplayer(ComputerCharacter ch, int x, int y)
         throws CoreException
     {
         map.insertCharacter(ch, x, y);
         nonplayers.add(ch);
     }
 
     public void addPreviousLevel(Level lvl)
    {
        this.prevLevel = lvl;
     }
 
     /**
      * This character is entering this level from above.
      *
      * @param ch character
      *
      * @throws CoreException if the level doesn't have an up staircase
      */
     public void enterDown(ICharacter ch)
         throws CoreException
     {
         MapPoint p = map.enterDown(ch);
 
         if (ch.isPlayer()) {
             players.add(ch);
         } else {
             nonplayers.add(ch);
         }
 
         ch.setLevel(this);
     }
 
     /**
      * This character is entering this level from below.
      *
      * @param ch character
      *
      * @throws CoreException if the level doesn't have a down staircase
      */
     public void enterUp(ICharacter ch)
         throws CoreException
     {
         MapPoint p = map.enterUp(ch);
 
         if (ch.isPlayer()) {
             players.add(ch);
         } else {
             nonplayers.add(ch);
         }
 
         ch.setLevel(this);
     }
 
     /**
      * Remove the character from this level.
      *
      * @param ch character to remove
      *
      * @throws CoreException if the character is not on this level
      */
     public void exit(ICharacter ch)
         throws CoreException
     {
         map.removeCharacter(ch);
 
         boolean result;
         if (ch.isPlayer()) {
             result = players.remove(ch);
         } else {
             result = nonplayers.remove(ch);
         }
 
         if (!result) {
             throw new LevelException(ch.getName() + " was not on this level");
         }
     }
 
     /**
      * Get a list of characters on this level.
      *
      * @return list of characters
      */
     public List<ICharacter> getCharacters()
     {
         List<ICharacter> characters =
             new ArrayList<ICharacter>(players);
         characters.addAll(nonplayers);
         return characters;
     }
 
     public Terrain getTerrain(int x, int y)
         throws MapException
     {
         return map.getTerrain(x, y);
     }
 
     public int getMaxX()
     {
         return map.getMaxX();
     }
 
     public int getMaxY()
     {
         return map.getMaxY();
     }
 
     public String getName()
     {
         return name;
     }
 
     public Level getNextLevel()
     {
         return nextLevel;
     }
 
     public String getPicture()
     {
         return map.getPicture();
     }
 
     public Level getPreviousLevel()
     {
         return prevLevel;
     }
 
     public boolean isOccupied(int x, int y)
         throws MapException
     {
         return map.isOccupied(x, y);
     }
 
     public void moveTo(ICharacter ch, int x, int y)
         throws MapException
     {
         map.moveTo(ch, x, y);
     }
 
     public String toString()
     {
         String pStr;
         if (players.size() == 0) {
             pStr = "";
         } else {
             pStr = " p*" + players.size();
         }
 
         String nStr;
         if (nonplayers.size() == 0) {
             nStr = "";
         } else {
             nStr = " n*" + nonplayers.size();
         }
 
         return name + "|" + map + "|" + pStr + nStr;
     }
 }
