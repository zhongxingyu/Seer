 package com.tehbeard.map.misc.spawner;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.tehbeard.map.entities.Entity;
 import com.tehbeard.map.factory.WorkerFactory;
 import com.tehbeard.mojang.nbt.CompoundTag;
 import com.tehbeard.mojang.nbt.ListTag;
 
 public class SpawnerData {
 	
 	private List<SpawnPotential> spawnPotentials = new ArrayList<SpawnPotential>();
 	
 	@Deprecated
 	private String entityId;
 	@Deprecated
 	private Entity spawnData;
 	
 	private short spawnCount;
 	private short spawnRange;
 	private short delay;
 	private short minSpawnDelay;
 	private short maxSpawnDelay;
 
 	private short maxNearbyEntities;
 	private short requiredPlayerRange;
 	
 	@SuppressWarnings("unchecked")
 	public SpawnerData(CompoundTag tag){
 		
 		for( CompoundTag spTag : (ListTag<CompoundTag>) tag.getList("SpawnPotentials")){
 			spawnPotentials.add(new SpawnPotential(spTag));
 		}
 		
 		entityId = tag.getString("EntityId");
 		spawnData = WorkerFactory.getInstance().getEntity(tag.getCompound("SpawnData"));
 		
		spawnCount = tag.getShort("spawnCount");
		spawnRange = tag.getShort("spawnRange");
		delay = tag.getShort("delay");
		minSpawnDelay = tag.getShort("minSpawnDelay");
		maxSpawnDelay = tag.getShort("maxSpawnDelay");
 		
		maxNearbyEntities = tag.getShort("maxNearbyEntities");
		requiredPlayerRange = tag.getShort("requiredPlayerRange");
 		
 		
 	}
 
 	public List<SpawnPotential> getSpawnPotentials() {
 		return spawnPotentials;
 	}
 
 	@Deprecated
 	public String getEntityId() {
 		return entityId;
 	}
 
 	@Deprecated
 	public Entity getSpawnData() {
 		return spawnData;
 	}
 
 	public short getSpawnCount() {
 		return spawnCount;
 	}
 
 	public short getSpawnRange() {
 		return spawnRange;
 	}
 
 	public short getDelay() {
 		return delay;
 	}
 
 	public short getMinSpawnDelay() {
 		return minSpawnDelay;
 	}
 
 	public short getMaxSpawnDelay() {
 		return maxSpawnDelay;
 	}
 
 	public short getMaxNearbyEntities() {
 		return maxNearbyEntities;
 	}
 
 	public short getRequiredPlayerRange() {
 		return requiredPlayerRange;
 	}
 	
 	
 	
 }
