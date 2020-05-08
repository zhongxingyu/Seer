 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package loader.data.json;
 
 import java.util.ArrayList;
 
 import loader.data.DataException;
 import loader.data.IClosable;
 
 public class LevelData implements IClosable {
   public class CreepSpawnData {
     public String creep;
     public float  spawnTime;
   }
 
   public String level;
   public int[]  constraints;
   public String background;
   public String loading, completed, preBossImage;
 
   public String                    boss;
   public ArrayList<CreepSpawnData> creeps;
 
   @Override
   public void close() throws DataException {
     // No data needs clean up
   }

  @Override
  public String toString() {
    return level;
  }
 }
