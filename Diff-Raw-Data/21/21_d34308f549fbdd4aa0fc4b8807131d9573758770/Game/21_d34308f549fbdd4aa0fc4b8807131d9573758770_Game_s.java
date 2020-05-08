 package com.mel.entityframework;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.andengine.entity.sprite.Sprite;
 
 
 public class Game
 {
 	private Sprite canvas;
 	
 	public Map<Class, List<IEntity>> entities = new HashMap<Class, List<IEntity>>();
 	public List<Process> processes = new ArrayList<Process>();
 	
 	public Game(Sprite canvas){
 		this.canvas = canvas;
 	}
 	
 	public void addEntities(List entities){
 		for(Object entity:entities){
 			addEntity((IEntity)entity);
 		}
 	}
 	
 	public void addEntity(IEntity entity){
 		if(this.entities.containsKey(entity.getClass())==false){
 			this.entities.put(entity.getClass(), new ArrayList<IEntity>());

			if(entity.getSprite()!=null)
				this.canvas.attachChild(entity.getSprite());

			if(entity.getPosition()!=null)
				this.canvas.attachChild(entity.getPosition());
 		}
		
 		this.entities.get(entity.getClass()).add(entity);
 	}
 	
 	public List getEntities(Object entityType){
 		
 		List<IEntity> entities = this.entities.get(entityType);
 		if(entities!=null)
 			return new ArrayList<IEntity>(entities);
 		else
			return null;
 	}
 	
 	public IEntity getEntity(Object entityType){
 		List<IEntity> entities = this.entities.get(entityType);
 		if(entities!=null && entities.size()>0){
 			return entities.get(0);
 		}else{
 			return null;
 		}
 	}
 	
 	public void removeEntity(IEntity entity){
 		if(entity == null)
 			return;
 		
 		List entitiesList = this.entities.get(entity.getClass());
 		if(entitiesList!=null){
 			entitiesList.remove(entity);
 		}
 		entity.recycle();
 	}
 	
 	public void addProcess(Process system, int priority){
 		system.priority = priority;
 		system.onAddToGame( this );
 		this.processes.add(system);
 		Collections.sort(this.processes);
 	}
 	
 	public void removeProcess(Process system){
 		this.processes.remove(system);
 		//Collections.sort(this.systems); //no deberia ser necesario al quitar un elemento
 	}
 	
 	public void update(){
 		for(Process process:this.processes){
 			process.update();
 		}
 	}
 }
