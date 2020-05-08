 /* Acid - Provides a Java cell API to display fancy cell boxes.
  * Copyright (C) 2013  Miguel Gonzalez
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
  */
 
 package de.myreality.acidsnake.core;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import de.myreality.acidsnake.util.AbstractIndexable;
 import de.myreality.acidsnake.util.Direction;
 import de.myreality.acidsnake.util.IndexConverter;
 import de.myreality.acidsnake.util.WorldBinder;
 import de.myreality.acidsnake.world.SimpleWorldEntityFactory;
 import de.myreality.acidsnake.world.World;
 import de.myreality.acidsnake.world.WorldEntity;
 import de.myreality.acidsnake.world.WorldEntityFactory;
 import de.myreality.acidsnake.world.WorldEntityType;
 
 /**
  * Implementation of {@link Snake}
  * 
  * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
  * @since 1.0
  * @version 1.0
  */
 public class SimpleSnake extends AbstractIndexable implements Snake {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	
 	private Direction direction, fixedDirection;
 	
 	private List<SnakeChunk> chunks;
 	
 	private SnakeChunk head, tail;
 	
 	private World world;
 	
 	private boolean killed;
 	
 	private Set<SnakeListener> listeners;
 	
 	private WorldEntityFactory factory;
 	
 	private WorldBinder binder;
 	
 	private boolean directionApplied;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 	
 	public SimpleSnake(int indexX, int indexY, World world) {
 		super(indexX, indexY);
 		this.world = world;
 		listeners = new HashSet<SnakeListener>();
 		chunks = new ArrayList<SnakeChunk>();
 		factory = new SimpleWorldEntityFactory(world);	
 		fixedDirection = Direction.RIGHT;
 		setDirection(Direction.RIGHT);
 		binder = new WorldBinder(world);
 	}
 
 	// ===========================================================
 	// Getters and Setters
 	// ===========================================================
 
 	// ===========================================================
 	// Methods from Superclass
 	// ===========================================================
 
 	@Override
 	public void setDirection(Direction direction) {
 		
 		if (directionApplied) {
 			fixedDirection = this.direction;
 			directionApplied = false;
 		}
 		
 		if (this.direction == null || fixedDirection.isValid(direction)) {
 			this.direction = direction;
 		}
 	}
 	@Override
 	public Direction getDirection() {
 		return direction;
 	}
 
 	@Override
 	public void move() {
 		switch (direction) {
 			case DOWN:
 				setIndexY(getIndexY() + 1);
 				break;
 			case LEFT:
 				setIndexX(getIndexX() - 1);
 				break;
 			case RIGHT:
 				setIndexX(getIndexX() + 1);
 				break;
 			case UP:
 				setIndexY(getIndexY() - 1);
 				break;
 		}
 	}
 
 	@Override
 	public List<SnakeChunk> getChunks() {
 		return chunks;
 	}
 
 	@Override
 	public void addChunk() {
 		
 		int chunkX = getIndexX(), chunkY = getIndexY();
 		
 		if (tail != null) {
 			IndexConverter converter = new IndexConverter(tail);
 			chunkX = converter.getInvertedIndexX(getDirection());
 			chunkY = converter.getInvertedIndexY(getDirection());
 		}
 		
 		SnakeChunk chunk = (SnakeChunk) factory.create(chunkX, chunkY, WorldEntityType.SNAKE);
 		if (chunks.isEmpty()) {
 			head = chunk;
 		}
 		
 		chunks.add(chunk);
 		chunk.setIndex(chunkX, chunkY);
 		tail = chunk;
 	}
 
 	@Override
 	public void removeChunk() {
 		
 		SnakeChunk tail = getTail();		
 		
 		if (tail != null) {
 			
 			world.removeEntity(tail);
 			chunks.remove(tail);
 			
			tail = tail.getNext();
 		}
 		
 	}
 
 	@Override
 	public SnakeChunk getHead() {
 		return head;
 	}
 
 	@Override
 	public SnakeChunk getTail() {
 		return tail;
 	}
 
 	@Override
 	public World getWorld() {
 		return world;
 	}
 
 	@Override
 	public void addListener(SnakeListener listener) {
 		if (!listeners.contains(listener)) {
 			listeners.add(listener);
 		}
 	}
 
 	@Override
 	public void spawn() {
 		for (SnakeListener listener : listeners) {
 			listener.onSpawn(this);
 		}
 	}
 
 	@Override
 	public void kill() {
 		for (SnakeListener listener : listeners) {
 			listener.onKill(this);
 		}
 		
 		killed = true;
 	}
 
 	@Override
 	public boolean isKilled() {
 		return killed;
 	}
 
 	@Override
 	public void setIndex(int indexX, int indexY) {
 		
 		indexX = binder.bindIndexX(indexX);
 		indexY = binder.bindIndexY(indexY);
 		boolean collision = world.hasEntity(indexX, indexY);		
 		WorldEntity collisionTarget = world.getEntity(indexX, indexY);
 		
 		super.setIndex(indexX, indexY);
 		
 		
 		for (SnakeListener listener : listeners) {
 			listener.onEnterPosition(indexX, indexY, this);		
 			if (collision) {
 				listener.onCollide(indexX, indexY, this, collisionTarget);
 			}
 		}
 		
 		if (!isKilled()) {
 			
 			for (int index = chunks.size() - 1; index >= 0; --index) {
 				SnakeChunk chunk = chunks.get(index);
 				chunk.move();
 			}
 			
 			directionApplied = true;
 		}
 	}
 
 	@Override
 	public void build() {
 		spawn();
 	}
 
 	@Override
 	public int getLength() {
 		return chunks.size();
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	// ===========================================================
 	// Inner classes
 	// ===========================================================
 }
