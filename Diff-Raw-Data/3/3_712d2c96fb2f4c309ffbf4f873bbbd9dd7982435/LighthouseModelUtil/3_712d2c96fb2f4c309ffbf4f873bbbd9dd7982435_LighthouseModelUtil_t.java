 package edu.uci.lighthouse.model;
 
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import edu.uci.lighthouse.model.LighthouseRelationship.TYPE;
 
 public class LighthouseModelUtil {
 
 	/**
 	 * Verify if the <code>relationship</code> "BELONGS" to a class(listEntitiesInside)
 	 * */
 	public static boolean isValidRelationship(LighthouseRelationship relationship, Collection<LighthouseEntity> listEntitiesInside) {
 		boolean result = true; 
 		if (!listEntitiesInside.contains(relationship.getFromEntity())) {
 			if (	relationship.getType()==LighthouseRelationship.TYPE.CALL 
 					|| relationship.getType()==LighthouseRelationship.TYPE.USES
 					|| relationship.getType()==LighthouseRelationship.TYPE.HOLDS
 					|| relationship.getType()==LighthouseRelationship.TYPE.RECEIVES
 					|| relationship.getType()==LighthouseRelationship.TYPE.RETURN) {
 				result = false;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Verify if an <code>paramEvent</code> was removed by some user.
 	 * It means that there is an NEWER event that has TYPE==REMOVE and it was already COMMITTED
 	 * */
 	public static boolean wasEventRemoved(List<LighthouseEvent> listEvents, LighthouseEvent paramEvent, LighthouseAuthor paramAuthor) {
 		for (LighthouseEvent event : listEvents) {
 			if (event.getArtifact().equals(paramEvent.getArtifact())) {
 				if (event.isCommitted()) {
 					if (event.getType() == LighthouseEvent.TYPE.REMOVE
 							&& (event.getCommittedTime().after(paramEvent.getCommittedTime()) 
 							|| event.getCommittedTime().equals(paramEvent.getCommittedTime()))) {
 						return true;
 					}
 				} else { // if is not committed - take the author in consideration
 					if ( event.getAuthor().equals(paramAuthor)
 							&& event.getType() == LighthouseEvent.TYPE.REMOVE
 							&& (event.getTimestamp().after(paramEvent.getTimestamp()) 
 							|| event.getTimestamp().equals(paramEvent.getTimestamp()))) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Get Entities INSIDE a class
 	 * */
 	private static LinkedHashSet<LighthouseEntity> getEntitiesInsideClass(LighthouseModel model, LighthouseEntity clazz) {
 		LinkedHashSet<LighthouseEntity> listEntity = new LinkedHashSet<LighthouseEntity>();
 		Collection<LighthouseRelationship> listRelInside = model.getRelationshipsTo(clazz,TYPE.INSIDE);
 		for (LighthouseRelationship rel : listRelInside) {
 			LighthouseEntity entity = rel.getFromEntity();
 			listEntity.add(entity);
			if (entity instanceof LighthouseClass) {
				listEntity.addAll(getEntitiesInsideClass(model,entity));
			}
 		}
 		return listEntity;
 	}
 	
 	/**
 	 * for each class - Get Entities INSIDE a class
 	 * */
 	public static Collection<LighthouseEntity> getEntitiesInsideClasses(LighthouseModel model, Collection<String> listClazzFqn) {
 		LinkedHashSet<LighthouseEntity> listEntity = new LinkedHashSet<LighthouseEntity>();
 		for (String clazzFqn : listClazzFqn) {
 			LighthouseEntity clazz = model.getEntity(clazzFqn);
 			listEntity.add(clazz);
 			listEntity.addAll(getEntitiesInsideClass(model, clazz));
 		}
 		return listEntity;
 	}
 	
 	/**
 	 * for each class - Get events related to entities/relationship that are INSIDE a class
 	 * */
 	public static Collection<LighthouseEvent> getEventsInside(LighthouseModel model, Collection<String> listClazzFqn) {
 		Collection<LighthouseEntity> listEntity = getEntitiesInsideClasses(model, listClazzFqn);
 		Collection<LighthouseRelationship> listRel = getRelationships(model, listEntity);
 		LinkedHashSet<LighthouseEvent> listEvents = getEventsByListEntityAndRel(model, listEntity, listRel);
 		return listEvents;
 	}
 	
 	/**
 	 * Get Events related to a list of Entities and Relationship
 	 * */
 	public static LinkedHashSet<LighthouseEvent> getEventsByListEntityAndRel(LighthouseModel model, Collection<LighthouseEntity> listEntity, Collection<LighthouseRelationship> listRel) {
 		LinkedHashSet<LighthouseEvent> listEvents = new LinkedHashSet<LighthouseEvent>();
 		for (LighthouseEntity entity : listEntity) {
 			listEvents.addAll(model.getEvents(entity));
 		}
 		for (LighthouseRelationship rel : listRel) {
 			listEvents.addAll(model.getEvents(rel));
 		}
 		return listEvents;
 	}
 
 	/**
 	 * Get Relationship related to a list of Entities
 	 * */
 	public static Collection<LighthouseRelationship> getRelationships(LighthouseModel model, Collection<LighthouseEntity> listEntity) {
 		LinkedHashSet<LighthouseRelationship> listRel = new LinkedHashSet<LighthouseRelationship>();
 		for (LighthouseEntity entity : listEntity) {
 			for (LighthouseRelationship rel : model.getRelationshipsFrom(entity)) {
 				listRel.add(rel);
 			}
 			for (LighthouseRelationship rel : model.getRelationshipsTo(entity)) {
 				if (LighthouseModelUtil.isValidRelationship(rel, listEntity)) {
 					listRel.add(rel);
 				}
 			}
 		}
 		return listRel;
 	}
 	
 }
