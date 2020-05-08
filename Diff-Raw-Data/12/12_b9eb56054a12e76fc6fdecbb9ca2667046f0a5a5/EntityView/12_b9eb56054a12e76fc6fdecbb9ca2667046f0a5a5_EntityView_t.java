 package com.secondhand.view.entities;
 
 import java.beans.PropertyChangeListener;
 
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.secondhand.model.CircleEntity;
 import com.secondhand.model.Entity;
 import com.secondhand.view.opengl.Circle;
 import com.secondhand.view.physics.CustomPhysicsConnector;
 import com.secondhand.view.physics.MyPhysicsEntity;
 
 public abstract class EntityView implements PropertyChangeListener {
	protected final IShape shape;
 	//Need this for physics.
 	private final Entity entity;
 	private final Body body;
 	
 	public EntityView(final PhysicsWorld physicsWorld, final Entity entity , final IShape shape,
 			Body body){
 		this.shape=shape;
 		this.entity = entity;
 		entity.addPropertyChangeListener(this);
 		this.body = body;
 		
 		entity.setPhysics(	new MyPhysicsEntity(physicsWorld, entity, shape, body));
 	}
 	
 	public Body getBody() {
 		return this.body;
 	}
 	
 	public Entity getEntity() {
 		return this.entity;
 	}
 	
 	public IShape getShape(){
 		return shape;
 	}
 	
 }
