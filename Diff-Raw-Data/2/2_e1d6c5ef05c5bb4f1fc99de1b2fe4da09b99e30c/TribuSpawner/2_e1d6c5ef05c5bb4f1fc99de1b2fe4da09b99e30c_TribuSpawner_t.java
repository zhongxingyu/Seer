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
 	/**
 	 * Is the round finish
 	 */
 	private boolean finished;
 	
 	/**
 	 * Health of zombie to spawn
 	 */
 	private int health;
 	/**
 	 *  A zombie just spawned
 	 */
 	private boolean justspawned;
 	/**
 	 * number of zombies to spawn
 	 */
 	private int totalToSpawn;
 	/**
 	 * Tribu
 	 */
 	private final Tribu plugin;
 	/**
 	 * Gonna start
 	 */
 	private boolean starting;
 	/**
 	 * spawned zombies
 	 */
 	private int alreadySpawned;
 	/**
 	 * Referenced zombies
 	 */
 	private LinkedList<CraftTribuZombie> zombies;
 
 	/**
 	 * Init the spawner
 	 * @param instance of Tribu
 	 */
 	public TribuSpawner(Tribu instance) {
 		plugin = instance;
 		alreadySpawned = 0;
 		totalToSpawn = 5;
 		finished = false;
 		starting = true;
 		health = 10;
 		zombies = new LinkedList<CraftTribuZombie>();
 	}
 
 	/**
 	 * Check that all referenced zombies are alive
 	 * Useful to check if a zombie has been despawned (too far away, killed but not caught) 
 	 * set finished if they are all dead
 	 */
 	public void checkZombies() {
 		Stack<CraftTribuZombie> toDelete = new Stack<CraftTribuZombie>();
 		for (CraftTribuZombie e : zombies)
 			if (e.isDead())
 				toDelete.push(e);
 		finished = toDelete.isEmpty();
		while (!toDelete.isEmpty())
 			removedZombieCallback(toDelete.pop(),false);
 
 	}
 
 	/**
 	 * Delete all zombies and prevent the spawner to continue spawning
 	 */
 	public void clearZombies() {
 		for (CraftTribuZombie zombie : zombies) {
 			zombie.remove();
 		}
 		resetTotal();
 		zombies.clear();
 	}
 
 	/**
 	 * Despawn a killed zombie
 	 * @param zombie zombie to unreference
 	 * @param drops drops to clear
 	 */
 	public void despawnZombie(CraftTribuZombie zombie, List<ItemStack> drops) {
 		if (zombies.remove(zombie)) {
 			drops.clear();
 			tryStartNextWave();
 		} 
 		// Else The zombie may have been deleted by "removedZombieCallback"
 		/*else {
 			
 			plugin.LogWarning("Unreferenced zombie despawned");
 		}*/
 	}
 
 	/**
 	 * Set that it's finish
 	 */
 	public void finishCallback() {
 		finished = true;
 	}
 
 	// Debug command
 	/**
 	 * This is a debug command returning the location of a living zombie
 	 * It prints info of this zombie on the console or a severe error
 	 * @return location of a living zombie
 	 */
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
 
 	/**
 	 * Get the number of zombie already spawned
 	 * @return number of zombie already spawned
 	 */
 	public int getTotal() {
 		return alreadySpawned;
 	}
 
 	/**
 	 * Get the first spawn in a loaded chunk
 	 * @return
 	 */
 	public Location getValidSpawn() {
 		for (Location curPos : plugin.getLevel().getSpawns().values()) {
 			if (curPos.getWorld().isChunkLoaded(curPos.getWorld().getChunkAt(curPos))) {
 				return curPos;
 			}
 		}
 		plugin.LogInfo(plugin.getLocale("Warning.AllSpawnsCurrentlyUnloaded"));
 		return null;
 
 	}
 
 	/**
 	 * Get the total quantity of zombie to spawn
 	 * (Not counting zombies killed)
 	 * @return total to spawn
 	 */
 	public int getMaxSpawn() {
 		return this.totalToSpawn;
 	}
 
 	/**
 	 * If the spawner should continue spawning
 	 * @return
 	 */
 	public boolean haveZombieToSpawn() {
 		return alreadySpawned < totalToSpawn;
 	}
 
 	/**
 	 * Check if the living entity is referenced here
 	 * @param ent
 	 * @return if the living entity was spawned by this
 	 */
 	public boolean isSpawned(LivingEntity ent) {
 		return zombies.contains(ent);
 	}
 
 	/**
 	 * The wave is completed if there is no zombie to spawn and zombies spawned are dead
 	 * @return is wave completed 
 	 */
 	public boolean isWaveCompleted() {
 		return !haveZombieToSpawn() && zombies.isEmpty();
 	}
 
 	/**
 	 * Is currently spawning a zombie ?
 	 * @return
 	 */
 	public boolean justSpawned() {
 		return justspawned;
 	}
 
 	/**
 	 * Kill & unreference a zombie 
 	 * @param e Zombie to despawn
 	 * @param removeReward Reward attakers ?
 	 */
 	public void removedZombieCallback(CraftTribuZombie e,boolean removeReward) {
 		if(removeReward)
 			e.setNoAttacker();
 		e.damage(Integer.MAX_VALUE);
 		zombies.remove(e);
 		alreadySpawned--;
 	}
 
 	/**
 	 * Prevent spawner to continue spawning but do not set it as finished
 	 */
 	public void resetTotal() {
 		alreadySpawned = 0;
 		finished = false;
 	}
 
 	/**
 	 * Set health of zombie to spawn
 	 * @param value Health
 	 */
 	public void setHealth(int value) {
 		health = value;
 	}
 
 	/**
 	 * Set the number of zombie to spawn
 	 * @param count
 	 */
 	public void setMaxSpawn(int count) {
 		totalToSpawn = count;
 	}
 
 	/**
 	 * Try to spawn a zombie
 	 * @return if zombies still have to spawn (before spawning it)
 	 */
 	public boolean spawnZombie() {
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
 		}else return false;
 		return true;
 	}
 
 	/**
 	 * Set that the spawner has started
 	 */
 	public void startingCallback() {
 		starting = false;
 	}
 
 	/**
 	 * Try to start the next wave if possible and return if it's starting 
 	 * @return
 	 */
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
