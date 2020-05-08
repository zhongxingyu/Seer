 package me.ayan4m1.plugins.breedmeister;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Entity;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BreedMeister extends JavaPlugin implements Listener {
 	//Map of entity ids to timestamps representing when they may next breed
 	private HashMap<Integer, Long> breedTimes = new HashMap<Integer, Long>();
 
 	//Maximum distance (in blocks) from dispenser to animal
 	private Integer maxDistance = 5;
 
 	//Per-animal delay between breeding in minutes
 	private Integer breedDelay = 5;
 
 	//Radius to search (in blocks) for an empty block near animals when spawning baby
 	private Integer spawnRadius = 5;
 
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 
 	public void onDisable() {
 		this.breedTimes.clear();
 	}
 
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event) {
 		//Remove entity from breedTimes to prevent memory leaks
 		Integer entityId = event.getEntity().getEntityId();
 		if (this.breedTimes.containsKey(entityId)) {
 			this.breedTimes.remove(entityId);
 		}
 	}
 
 	@EventHandler
 	public void onBlockDispense(BlockDispenseEvent event) {
 		Block block = event.getBlock();
 
 		//Only continue if wheat is being dropped from a dispenser
 		if (block.getType() != Material.DISPENSER || event.getItem().getType() != Material.WHEAT) {
 			return;
 		}
 
 		List<Entity> entities = block.getWorld().getEntities();
 
 		//Search for two valid and distinct animals
 		Animals animalOne = this.findValidAnimal(entities, block.getLocation(), null);
 		if (animalOne == null) {
 			event.setCancelled(true);
 			return;
 		}
 
 		Animals animalTwo = this.findValidAnimal(entities, block.getLocation(), animalOne);
 		if (animalTwo == null) {
 			event.setCancelled(true);
 			return;
 		}
 
 		//Find an empty location to spawn the baby, return if none is found
 		Location newLoc = this.getNearestFreeBlock(animalTwo.getLocation());
 		if (newLoc == null) {
 			this.getLogger().warning("Tried to spawn baby, but couldn't find a free block!");
 			event.setCancelled(true);
 			return;
 		}
 
 		//Spawn the baby if a valid location was found
 		Animals newAnimal = (Animals)block.getWorld().spawnCreature(newLoc, animalTwo.getType());
 		newAnimal.setBaby();
 
 		//Add the animals to the bred list
 		Long nextBreedTime = (new Date().getTime() / 1000) + (this.breedDelay * 60);
 		this.breedTimes.put(animalOne.getEntityId(), nextBreedTime);
 		this.breedTimes.put(animalTwo.getEntityId(), nextBreedTime);
 
		event.getItem().setAmount(event.getItem().getAmount() - 1);
 
 	/**
 	 * Get the empty block nearest to the provided location 
 	 * @param animalLoc A location to search nearby
 	 * @return A Location or null if no empty blocks are found in the search radius
 	 */
 	private Location getNearestFreeBlock(Location animalLoc) {
 		if (animalLoc.getBlock().isEmpty()) {
 			return animalLoc;
 		}
 
 		//Check each block in our radius starting from the center
 		for(int x = 0; x <= (this.spawnRadius * 2); x++) {
 			for(int y = 0; y <= (this.spawnRadius * 2); y++) {
 				for(int z = 0; z <= (this.spawnRadius * 2); z++) {
 					Location testLoc = animalLoc.add(
 							(x % this.spawnRadius) * ((x < this.spawnRadius) ? -1 : 0),
 							(y % this.spawnRadius) * ((y < this.spawnRadius) ? -1 : 0),
 							(z % this.spawnRadius) * ((z < this.spawnRadius) ? -1 : 0));
 					if (testLoc.getBlock().isEmpty()) {
 						return testLoc;
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Search the entity list for an animal which is suitable for breeding
 	 * @param entities A list of the entities to search
 	 * @param dispenserLoc The Location of the dispenser being activated
 	 * @param exceptThis This entity will be skipped
 	 * @return An instance of Animals or null if no valid matches are found
 	 */
 	private Animals findValidAnimal(List<Entity> entities, Location dispenserLoc, Animals exceptThis) {
 		for(Entity e : entities) {
 			//Ensure entity is a living, adult animal
 			if (!(e instanceof Animals && ((Animals)e).canBreed()) || e.isDead()) {
 				continue;
 			}
 
 			//Ensure animal is within maxDistance blocks from the dispenser
 			if (dispenserLoc.distance(e.getLocation()) > this.maxDistance) {
 				continue;
 			}
 
 			//Ensure the animal can breed
 			if (this.breedTimes.containsKey(e.getEntityId())) {
 				if (this.breedTimes.get(e.getEntityId()) > (new Date().getTime() / 1000)) {
 					continue;
 				} else {
 					this.breedTimes.remove(e.getEntityId());
 				}
 			}
 
 			//Ensure the animal is not one we have already found
 			if (exceptThis != null
 					&& e.getType().equals(exceptThis.getType())
 					&& exceptThis.getEntityId() != e.getEntityId()) {
 				continue;
 			}
 
 			return (Animals)e;
 		}
 		return null;
 	}
 }
