 package com.punchline.javalib.entities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class SystemManager {
 
 	private List<EntitySystem> systems = new ArrayList<EntitySystem>();
 	
 	/**
 	 * Adds a system to the SystemManager.
 	 * @param system The system to be added.
 	 * @param entities List of all active game entities, for 
 	 * determining which should be added to the system's processing list.
 	 */
 	public void addSystem(EntitySystem system, List<Entity> entities) {
 		systems.add(system);
 		
 		for (Entity e : entities) {
 			if (system.canProcess(e)) {
 				system.add(e);
 			}
 		}
 	}
 	
 	/**
 	 * Adds a system to the SystemManager. NOTE: Do not call this after entities have been created.
 	 * They will not be added to the system's processing list.
 	 * @param system The system to be added.
 	 */
 	public EntitySystem addSystem(EntitySystem system) {
 		systems.add(system);
 		return system;
 	}
 	
 	/**
 	 * Processes all incoming, outgoing, and modified {@link Entity Entities} to determine 
 	 * which systems should be notified. Then, runs all system processing.
 	 * @param newEntities Incoming entities.
 	 * @param changedEntities Modified entities.
 	 * @param removedEntities Outgoing entities.
 	 */
 	public void process(List<Entity> newEntities, List<Entity> changedEntities, List<Entity> removedEntities, float deltaSeconds) {
 		
 		for (EntitySystem system : systems) {
 			
 			for (Entity e : newEntities) {
 				if (system.canProcess(e)) {
 					system.add(e); //The system can process this Entity, so add it
 				}
 			}
 			
 			for (Entity e : changedEntities) {
 				if (system.isProcessing(e)) {
 					if (!system.canProcess(e)) {
 						system.remove(e);
 						continue;
 					}
 					
 					system.onChanged(e);
 				}
 				
 			}
 			
 			for (Entity e : removedEntities) {
 				if (system.isProcessing(e)) {
 					system.remove(e); //The system was processing this Entity, so remove it.
 				}
 			}
 			
 			if (system.getInterval() > 0) {
 				system.addElapsedInterval(deltaSeconds);
 				
 				if (system.getElapsedInterval() >= system.getInterval()) {
 					system.processEntities();
 					system.resetElapsedInterval();
 				}
 			} else {
 				system.processEntities();
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * @return A map of system names with their respective delta times, for measuring performance.
 	 */
 	public Map<String, Float> systemPerformance() {
 		Map<String, Float> performance = new HashMap<String, Float>();
 		
 		for (EntitySystem system : systems) {
			performance.put(system.getClass().getSimpleName(), system.deltaSeconds());
 		}
 		
 		return performance;
 	}
 	
 }
