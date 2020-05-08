 /*
  * Copyright (c) 2009-2011 Daniel Oom, see licence.txt for more info.
  */
 
 package states;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import loader.data.DataException;
 import loader.data.json.CreepsData;
 import loader.data.json.CreepsData.CreepData;
 import loader.data.json.LevelData.CreepSpawnData;
 import loader.data.json.LevelData.CreepStateData;
 import loader.parser.ParserException;
 import main.Launcher;
 import main.World;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import other.CacheTool;
 import other.GameTime;
 import basics.ExMath;
 import entities.Creep;
 import factories.CreepFactory;
 
 public class CreepsState implements ICompState {
   private final World            world;
   private final ArrayList<Creep> notSpawnedCreeps;
 
   private float getCreepX(final int width) {
     return world.getX2() + width;
   }
 
   private float getCreepY(final int height) {
     return ExMath.random(0, world.getY2() - height);
   }
 
   public CreepsState(final CreepStateData sd, final World world)
     throws IOException, ParserException, SlickException, DataException {
     super();
     this.world = world;
 
     notSpawnedCreeps = new ArrayList<Creep>(sd.creeps.size());
 
     final CreepsData creepsData = CacheTool.getCreeps(Launcher.cache);
     for (final CreepSpawnData spawnData : sd.creeps) {
       final CreepData data = creepsData.getCreep(spawnData.creep);
 
       notSpawnedCreeps.add(CreepFactory.Make(getCreepX(data.hitBox[0]),
                                              getCreepY(data.hitBox[1]),
                                              (float) -Math.PI,
                                              spawnData.spawnTime, data));
     }
   }
 
   @Override
   public void update(final GameTime time) {
     final Iterator<Creep> it = notSpawnedCreeps.iterator();
     while (it.hasNext()) {
       final Creep c = it.next();
 
       if (c.getSpawnTime() < time.getElapsed()) {
         c.start();
         world.add(c);
         it.remove();
       }
     }
   }
 
   @Override
   public void render(final Graphics g) {}
 
   @Override
   public void start() {}
 
   @Override
   public boolean isFinished() {
    return notSpawnedCreeps.isEmpty();
   }
 }
