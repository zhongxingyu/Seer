 package com.punchline.javalib.entities;
 
import java.util.ArrayList;

 /**
  * Base class for group templates. A group template will create multiple entities at the same time.
  * @author Nathaniel
  *
  */
 public interface EntityGroupTemplate {
 
 	/**
 	 * Creates a group of entities.
 	 * @param world The EntityWorld for the entities to inhabit.
 	 * @param args Arguments for the creation of the group.
 	 * @return The entity group.
 	 */
	public ArrayList<Entity> buildEntities(EntityWorld world, Object... args);
 	
 }
