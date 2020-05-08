 package com.secondhand.view.entities;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.secondhand.model.Entity;
 import com.secondhand.view.physics.MyPhysicsEntity;
 
 public abstract class EntityView implements PropertyChangeListener {
 	protected final IShape shape;
 	protected final Body body;
 	protected final Entity entity;
 	
 	public EntityView(final PhysicsWorld physicsWorld, final Entity entity , final IShape shape,
 			final Body body){
 		this.shape=shape;
 		entity.addPropertyChangeListener(this);
 		this.body = body;
 		this.entity=entity;
 		
 		// we always want to cull shapes.
 		if(!shape.isCullingEnabled()) {
 			shape.setCullingEnabled(true);
 		}
 		
 		
 		entity.setPhysics(	new MyPhysicsEntity(physicsWorld, entity, shape, body));
 	}
 	
 	public Body getBody() {
 		return this.body;
 	}
 	
 	public IShape getShape(){
 		return shape;
 	}
 
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 	}
 }
