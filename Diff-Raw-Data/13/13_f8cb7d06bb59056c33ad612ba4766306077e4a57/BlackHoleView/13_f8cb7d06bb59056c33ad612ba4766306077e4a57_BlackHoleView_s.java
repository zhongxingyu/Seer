 package com.secondhand.view.entities;
 
 import java.beans.PropertyChangeEvent;
 
 import org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.BlackHole;
 import com.secondhand.view.opengl.Circle;
 
 public class BlackHoleView extends EntityView {
 
 	//Circle circle;
 	//Body body;
 	private final BlackHole mBlackHole;
 
 	public BlackHoleView(BlackHole blackHole) {
 		super(blackHole, new Circle(blackHole.getPosX(), blackHole.getPosY(),
 				blackHole.getRadius()));
 		mBlackHole = blackHole;
 	}
 
 	/*private void setBody(Body body) {
 		this.body = body;
 	}*/
 
 	private void changeSize() {
 		final CircleShape shape = (CircleShape) getShape();
 		shape.setRadius(mBlackHole.getRadius()
 				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
	
		
 		//If it dont work this way.
 		/*final CircleShape shape = (CircleShape) getBody().getFixtureList()
 				.get(0).getShape();
 		shape.setRadius(this.getRadius()
 				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);*/
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getPropertyName() == "radius") {
 			updateRadius();
 		}
 	}
 
 	private void updateRadius() {
 
 	}
 
 }
