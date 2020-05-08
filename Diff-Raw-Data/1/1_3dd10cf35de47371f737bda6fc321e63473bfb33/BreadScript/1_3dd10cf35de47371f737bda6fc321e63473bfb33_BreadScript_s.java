 package burd.customscripts;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import toritools.entity.Entity;
 import toritools.entity.Level;
 import toritools.math.MidpointChain;
 import toritools.math.Vector2;
 import toritools.scripting.EntityScript;
 import toritools.scripting.ScriptUtils;
 
 /**
  * When active, the bread stays in it's spot. Otherwise, the bread tails the
  * player.
  * 
  * @author toriscope
  * 
  */
 public class BreadScript implements EntityScript {
 
 	private static List<Entity> trailingQueue = new ArrayList<Entity>();
 
 	public static List<Entity> remainingList = new ArrayList<Entity>();
 
 	MidpointChain chain;
 
 	Vector2 origPos;
 
 	Entity player, trailBread;
 
 	@Override
 	public void onSpawn(Level level, Entity self) {
 
 		player = level.getEntityWithId("player");
 
 		// First spawn
 		if (origPos == null) {
 			origPos = self.pos.clone();
 		}
 
 		chain = new MidpointChain(self.pos.clone(), origPos, 15);
 
 		remainingList.add(self);
 	}
 
 	@Override
 	public void onUpdate(Level level, Entity self) {
 
 		if (self.active) {
 			chain.setB(origPos);
 			if (ScriptUtils.isColliding(self, player)) {
 				self.active = false;
 				trailBread = trailingQueue.isEmpty() ? player : trailingQueue
 						.get(trailingQueue.size() - 1);
 				trailingQueue.add(self);
 				remainingList.remove(self);
 			}
 		} else {
 			if (trailBread == player || trailingQueue.contains(trailBread))
 				chain.setB(trailBread.pos.clone());
 			else {
 				int index = trailingQueue.indexOf(self);
 				trailBread = index == 0 ? player : trailingQueue.get(index - 1);
 			}
 		}
 		if (Vector2.dist(chain.getA(), chain.getB()) > 10)
 			chain.smoothTowardB();
 		self.pos = chain.getA();
 
 		for (Entity nest : level.getEntitiesWithType("nest")) {
 			if (ScriptUtils.isColliding(self, nest)) {
 				level.killEntity(self);
 			}
 		}
 	}
 
 	@Override
 	public void onDeath(Level level, Entity self, boolean isRoomExit) {
 		trailingQueue.remove(self);
 		for (int i = 0; i < 5; i++) {
 			Entity blood = VolcanoParticleScript.getSparkle();
 			blood.pos = self.pos.clone();
 			level.spawnEntity(blood);
 		}
 	}
 
 }
