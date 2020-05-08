 /*******************************************************************************
  * Copyright 2013 See AUTHORS file.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.mangecailloux.pebble.entity;
 
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.IntMap;
 import com.badlogic.gdx.utils.Logger;
 import com.badlogic.gdx.utils.Pools;
 
 public class EntitiesManager 
 {
 	protected 	final Logger 					logger;
 	private 	final EntityWorld	   			world;
 	private 	final Array<Entity> 			entities;
 	private 	final Array<IEntityObserver> 	obervers;
 	private		final IntMap<Entity>			entitiesByHandle;
 	private		 	  int						currentHandle;
 	
 	protected EntitiesManager(EntityWorld _world)
 	{
 		// reset the global counter as static data can be retained between application instances
 		Entity.globalCounter = 0;
 		
 		logger = new Logger("EntityManager");
 		world = _world;
 		entities = new Array<Entity>(false, 8);
 		obervers = new Array<IEntityObserver>(false, 4);
 		
 		entitiesByHandle = new IntMap<Entity>(8);
 		currentHandle = 0;
 	}
 	
 	private Entity newEntity(EntityArchetype _archetype)
 	{
 		if(_archetype == null)
 			throw new RuntimeException("EntityManager::NewEntity -> Archetype cannot be null");
 		
 		if(_archetype.getComponentTypesCount() == 0)
 			throw new RuntimeException("EntityManager::NewEntity -> Archetype cannot be empty");
 		
 		 
 		 Entity entity = Pools.obtain(Entity.class);
		 entity.setEntityManager(this);
 		 logger.info("New " + entity);
 		 entity.initComponents(_archetype);
 		 return entity;
 	}
 	
 	private void addEntity(Entity _entity)
 	{
 		if(_entity != null)
 		{
 			logger.info("AddToWorld " + _entity);
 			_entity.setWorld(world, currentHandle);
 			
 			for(int o =0; o < obervers.size; ++o)
 			{
 				obervers.get(o).onAddToWorld(_entity);
 			}
 			entities.add(_entity);
 			entitiesByHandle.put(currentHandle, _entity);
 			
 			currentHandle ++;
 		}
 	}
 	
 	protected Entity addEntity(EntityArchetype _archetype)
 	{
 		Entity entity = newEntity(_archetype);
 		addEntity(entity);
 		return entity;
 	}
 	
 	protected void removeEntity(Entity _entity)
 	{
 		if(_entity != null)
 		{
 			if(entities.removeValue(_entity, true))
 			{
 				logger.info("RemoveFromWorld " + _entity);
 				for(int o = 0; o < obervers.size; ++o)
 				{
 					obervers.get(o).onRemoveFromWorld(_entity);
 				}
 				
 				entitiesByHandle.remove(_entity.getHandle());
 				
 				_entity.setWorld(null, EntityHandle.InvalidHandle);
 				Pools.free(_entity);
 			}
 		}
 	}
 	
 	protected void removeAllEntities()
 	{
 		if(entities.size == 0)
 			return;
 		
 		for(int i = 0; i < entities.size; ++i)
 		{
 			Entity entity = entities.get(i);
 			
 			logger.info("RemoveFromWorld " + entity);
 			for(int o = 0; o < obervers.size; ++o)
 			{
 				obervers.get(o).onRemoveFromWorld(entity);
 			}
 			entitiesByHandle.remove(entity.getHandle());
 			entity.setWorld(null, EntityHandle.InvalidHandle);
 			Pools.free(entity);
 		}
 		entities.clear();
 	}
 	
 	protected void addObserver(IEntityObserver _observer)
 	{
 		if(_observer != null && !obervers.contains(_observer, true))
 		{
 			obervers.add(_observer);
 		}
 	}
 	
 	protected void removeObserver(IEntityObserver _observer)
 	{
 		if(_observer != null)
 		{
 			obervers.removeValue(_observer, true);
 		}
 	}
 	
 	Entity getEntityByHandle(int _handle)
 	{
 		if(_handle == EntityHandle.InvalidHandle)
 			return null;
 		
 		return entitiesByHandle.get(_handle);
 	}
 }
