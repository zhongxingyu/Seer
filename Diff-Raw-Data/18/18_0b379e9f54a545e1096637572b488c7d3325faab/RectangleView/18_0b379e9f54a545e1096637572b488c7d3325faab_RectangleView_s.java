 package com.secondhand.view.entities;
 
 import java.beans.PropertyChangeEvent;
 
 import org.anddev.andengine.entity.shape.RectangularShape;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.secondhand.model.PolygonEntity;
 import com.secondhand.model.RectangleEntity;
import com.secondhand.view.opengl.Polygon;
import com.secondhand.view.physics.MyPhysicsFactory;
 
 public class RectangleView extends EntityView {
 	
 	public RectangleView(final PhysicsWorld physicsWorld, final RectangleEntity rectangleEntity , final RectangularShape rectangle,
 			final FixtureDef fixtureDef) {
		
 		super(physicsWorld, rectangleEntity, rectangle,
				MyPhysicsFactory.createPolygonBody(physicsWorld, polygon, BodyType.DynamicBody, fixtureDef));
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 }
