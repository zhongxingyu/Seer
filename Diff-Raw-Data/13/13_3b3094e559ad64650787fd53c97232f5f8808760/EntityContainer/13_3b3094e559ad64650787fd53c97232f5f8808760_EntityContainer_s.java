 /*****************************************************************************
  * 
  * Copyright 2012 See AUTHORS file.
  * 
  * This file is part of Escape-IR.
  * 
  * Escape-IR is free software: you can redistribute it and/or modify
  * it under the terms of the zlib license. See the COPYING file.
  * 
  *****************************************************************************/
 
 package fr.escape.game.entity;
 
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 
 import org.jbox2d.dynamics.World;
 
 import android.graphics.Rect;
 
 import fr.escape.Objects;
 import fr.escape.app.Engine;
 import fr.escape.app.Graphics;
 import fr.escape.game.entity.bonus.Bonus;
 import fr.escape.game.entity.bonus.BonusFactory;
 import fr.escape.game.entity.notifier.EdgeNotifier;
 import fr.escape.game.entity.notifier.KillNotifier;
 import fr.escape.game.entity.ships.Ship;
import fr.escape.resources.Resources;
 
 /**
  * <p>
  * A composite which handle large bunch of {@link Entity}, a container for {@link Entity} in the World.
  * 
  * <p>
  * This Container implements {@link KillNotifier} and {@link EdgeNotifier} for removing
  * {@link Entity}.
  * 
  */
 public final class EntityContainer implements Updateable, KillNotifier, EdgeNotifier {
 
 	//private static final String TAG = EntityContainer.class.getSimpleName();
 	
 	private final Engine engine;
 	private final World world;
 	private final Rect edge;
 	private final LinkedHashSet<Entity> entities;
 	private final LinkedList<Entity> destroyed;
 	
 	/**
 	 * Default Constructor
 	 * 
 	 * @param world World used
 	 * @param margin Margin used for Edge World
 	 */
 	public EntityContainer(Engine engine, World world, int margin) {
 		
 		this.engine = engine;
 		this.world = world;
 		this.edge = new Rect(-margin, -margin, engine.getGraphics().getWidth() + margin, engine.getGraphics().getHeight() + margin);
 		this.entities = new LinkedHashSet<Entity>();
 		this.destroyed = new LinkedList<Entity>();
 		
 		//Foundation.ACTIVITY.debug(TAG, "EntityContainer created");
 		
 	}
 	
 	/**
 	 * Push an {@link Entity} in the Container.
 	 * 
 	 * @param e Entity
 	 * @return True if successful
 	 */
 	public boolean push(Entity e) {
 		Objects.requireNonNull(e);
 		//Foundation.ACTIVITY.debug(TAG, "Push this Entity: "+e);
 		return this.entities.add(e);
 	}
 	
 	/**
 	 * Remove an {@link Entity} in the Container.
 	 * 
 	 * @param e Entity
 	 * @return True if successful
 	 */
 	private boolean remove(Entity e) {
 		Objects.requireNonNull(e);
 		//Foundation.ACTIVITY.debug(TAG, "Remove this Entity: "+e);
 		return this.entities.remove(e);
 	}
 	
 	@Override
 	public boolean edgeReached(Entity e) {
 		toDestroy(Objects.requireNonNull(e));
 		return true;
 	}
 
 	@Override
 	public boolean destroy(Entity e) {
 		toDestroy(Objects.requireNonNull(e));
 		return true;
 	}
 	
 	@Override
 	public void update(Graphics graphics, long delta) {
 		Objects.requireNonNull(graphics);
 		for(Entity e : entities) {
 			e.update(graphics, delta);
 		}
 	}
 
 	/**
 	 * Get the number of {@link Entity} in this Container
 	 * 
 	 * @return Number of Entity
 	 */
 	public int size() {
 		return entities.size();
 	}
 
 	@Override
 	public boolean isInside(Rect edge) {
 		return this.edge.contains(edge);
 	}
 	
 	/**
 	 * Add an {@link Entity} on the Removing Queue.
 	 * 
 	 * @param e {@link Entity}
 	 */
 	public void toDestroy(Entity e) {
 		//Foundation.ACTIVITY.debug(TAG, "Add Entity in Removing Queue: "+e);
 		this.destroyed.add(e);
 	}
 	
 	/**
 	 * Flush {@link EntityContainer} by removing all {@link Entity}
 	 * listed in the Removing Queue.
 	 * 
 	 * @return True if successful
 	 */
 	public boolean flush() {
 		
 		for(Entity e : destroyed) {
 			//Foundation.ACTIVITY.debug(TAG, "Remove Entity: "+e+" "+((remove(e)?"[DONE]":"[FAIL]")));
 			if(e.getBody() != null) {
 				world.destroyBody(e.getBody());
 				e.setBody(null);
 			}
 		}
 		
 		destroyed.clear();
 		return true;
 	}
 
 	/**
 	 * Reset the {@link EntityContainer} by removing all {@link Entity} from this container
 	 * and the {@link World}.
 	 * 
 	 * @return True if successful
 	 */
 	public boolean reset() {
 		
 		if(!flush()) {
 			return false;
 		}
 		
 		Iterator<Entity> it = entities.iterator();
 		
 		while(it.hasNext()) {
 			Entity e = it.next();
 			it.remove();
 			world.destroyBody(e.getBody());
 			e.setBody(null);
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Return true if this container contains the specified {@link Entity}
 	 *  
 	 * @param e Entity
 	 * @return True if this container contains the given {@link Entity}
 	 */
 	public boolean contains(Entity e) {
 		Objects.requireNonNull(e);
 		return entities.contains(e);
 	}
 	
 	/**
 	 * Push a Bonus in World
 	 * 
 	 * @param x Bonus X Position
 	 * @param y Bonus Y Position
 	 * @return True if Successful.
 	 */
 	public boolean pushBonus(float x, float y) {
 		
 		Bonus bonus = BonusFactory.createBonus(engine, world, x, y, this);
 		
 		if(bonus != null) {
 			return push(bonus);
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Push a Ship in World
 	 * 
 	 * @param ship Ship to add in container.
 	 * @return True if Successful.
 	 */
 	public boolean pushShip(Ship ship) {
 		Objects.requireNonNull(ship).createBody(world);
 		return push(ship);
 	}
 
 }
