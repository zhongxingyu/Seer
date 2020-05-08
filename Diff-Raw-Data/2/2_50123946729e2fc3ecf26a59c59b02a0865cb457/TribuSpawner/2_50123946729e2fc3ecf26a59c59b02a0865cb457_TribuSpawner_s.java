 /*******************************************************************************
  * Copyright or  or Copr. Quentin Godron (2011)
  * 
  * cafe.en.grain@gmail.com
  * 
  * This software is a computer program whose purpose is to create zombie 
  * survival games on Bukkit's server. 
  * 
  * This software is governed by the CeCILL-C license under French law and
  * abiding by the rules of distribution of free software.  You can  use, 
  * modify and/ or redistribute the software under the terms of the CeCILL-C
  * license as circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info". 
  * 
  * As a counterpart to the access to the source code and  rights to copy,
  * modify and redistribute granted by the license, users are provided only
  * with a limited warranty  and the software's author,  the holder of the
  * economic rights,  and the successive licensors  have only  limited
  * liability. 
  * 
  * In this respect, the user's attention is drawn to the risks associated
  * with loading,  using,  modifying and/or developing or reproducing the
  * software by the user in light of its specific status of free software,
  * that may mean  that it is complicated to manipulate,  and  that  also
  * therefore means  that it is reserved for developers  and  experienced
  * professionals having in-depth computer knowledge. Users are therefore
  * encouraged to load and test the software's suitability as regards their
  * requirements in conditions enabling the security of their systems and/or 
  * data to be ensured and,  more generally, to use and operate it in the 
  * same conditions as regards security. 
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C license and that you accept its terms.
  ******************************************************************************/
 package graindcafe.tribu;
 
 import graindcafe.tribu.Configuration.Constants;
 import graindcafe.tribu.TribuZombie.CannotSpawnException;
 import graindcafe.tribu.TribuZombie.CraftTribuZombie;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import org.bukkit.Location;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.ItemStack;
 
 public class TribuSpawner {
 	private boolean finished;
 	private int health;
 
 	private boolean justspawned;
 	// number of zombies to spawn
 	private int totalToSpawn;
 	private final Tribu plugin;
 	private boolean starting;
 	// spawned zombies
 	private int alreadySpawned;
 	private LinkedList<CraftTribuZombie> zombies;
 
 	public TribuSpawner(Tribu instance) {
 		plugin = instance;
 		alreadySpawned = 0;
 		totalToSpawn = 5;
 		finished = false;
 		starting = true;
 		health = 10;
 		zombies = new LinkedList<CraftTribuZombie>();
 	}
 
 	// check if a zombie has been despawned (too far, killed but not caught by
 	// event,...)
 	public void checkZombies() {
 		Stack<LivingEntity> toDelete = new Stack<LivingEntity>();
 		for (CraftTribuZombie e : zombies)
 			if (e.isDead())
 				toDelete.push(e);
 		if (finished && !toDelete.isEmpty())
 			finished = false;
 		while (!toDelete.isEmpty())
 			removedZombieCallback(toDelete.pop());
 
 	}
 
 	public void clearZombies() {
 		for (CraftTribuZombie zombie : zombies) {
 			zombie.remove();
 		}
 		resetTotal();
 		zombies.clear();
 	}
 
 	public void despawnZombie(CraftTribuZombie zombie, List<ItemStack> drops) {
 		if (zombies.contains(zombie)) {
 			zombies.remove(zombie);
 			drops.clear();
 			tryStartNextWave();
 		} else {
 			plugin.LogWarning("Unreferenced zombie despawned");
 		}
 	}
 
 	public void finishCallback() {
 		finished = true;
 	}
 
 	// Debug command
 	public Location getFirstZombieLocation() {
 		if (alreadySpawned > 0)
 			if (!zombies.isEmpty()) {
 				plugin.LogInfo("Health : " + zombies.get(0).getHealth());
 				plugin.LogInfo("LastDamage : " + zombies.get(0).getLastDamage());
 				plugin.LogInfo("isDead : " + zombies.get(0).isDead());
 				return zombies.get(0).getLocation();
 			} else {
 				plugin.getSpawnTimer().getState();
 				plugin.LogSevere("There is " + zombies.size() + " zombie alive of " + alreadySpawned + "/" + totalToSpawn + " spawned . The wave is "
 						+ (finished ? "finished" : "in progress"));
 				return null;
 			}
 		else
 			return null;
 	}
 
 	public int getTotal() {
 		return alreadySpawned;
 	}
 
 	// get the first spawn that is loaded
 	public Location getValidSpawn() {
 		for (Location curPos : plugin.getLevel().getSpawns().values()) {
 
 			if (curPos.getWorld().isChunkLoaded(curPos.getWorld().getChunkAt(curPos))) {
 				return curPos;
 			}
 		}
 		plugin.LogInfo(plugin.getLocale("Warning.AllSpawnsCurrentlyUnloaded"));
 		return null;
 
 	}
 
 	public int getMaxSpawn() {
 		return this.totalToSpawn;
 	}
 
 	public boolean haveZombieToSpawn() {
		return alreadySpawned != totalToSpawn;
 	}
 
 	public boolean isSpawned(LivingEntity ent) {
 		return zombies.contains(ent);
 	}
 
 	public boolean isWaveCompleted() {
 		return !haveZombieToSpawn() && zombies.isEmpty();
 	}
 
 	public boolean justSpawned() {
 		return justspawned;
 	}
 
 	public void removedZombieCallback(LivingEntity e) {
 		e.damage(Integer.MAX_VALUE);
 		zombies.remove(e);
 		alreadySpawned--;
 	}
 
 	public void resetTotal() {
 		alreadySpawned = 0;
 		finished = false;
 	}
 
 	public void setHealth(int value) {
 		health = value;
 	}
 
 	public void setMaxSpawn(int count) {
 		totalToSpawn = count;
 	}
 
 	public void SpawnZombie() {
 		if (alreadySpawned < totalToSpawn && !finished) {
 			Location pos = plugin.getLevel().getRandomZombieSpawn();
 			if (pos != null) {
 				if (!pos.getWorld().isChunkLoaded(pos.getWorld().getChunkAt(pos))) {
 					this.checkZombies();
 
 					pos = this.getValidSpawn();
 				}
 				if (pos != null) {
 					// Surrounded with justspawned so that the zombie isn't
 					// removed in the entity spawn listener
 					justspawned = true;
 					CraftTribuZombie zombie;
 					try {
 						zombie = (CraftTribuZombie) CraftTribuZombie.spawn(plugin, pos);
 						zombies.add(zombie);
 						zombie.setHealth(health);
 						alreadySpawned++;
 					} catch (CannotSpawnException e) {
 						// Impossible to spawn the zombie, maybe because of lack
 						// of
 						// space
 					}
 					justspawned = false;
 				}
 			}
 		}
 	}
 
 	public void startingCallback() {
 		starting = false;
 	}
 
 	// Try to start the next wave if possible and return if it's starting
 	public boolean tryStartNextWave() {
 		if (zombies.isEmpty() && finished && !starting) {
 			starting = true;
 			plugin.messagePlayers(plugin.getLocale("Broadcast.WaveComplete"));
 			plugin.getWaveStarter().incrementWave();
 			plugin.getWaveStarter().scheduleWave(Constants.TicksBySecond * plugin.config().WaveStartDelay);
 		}
 		return starting;
 	}
 
 }
