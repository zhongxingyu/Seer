 package com.secondhand.view.entities;
 
 import java.beans.PropertyChangeEvent;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.BlackHole;
 import com.secondhand.view.opengl.Circle;
 
 public class BlackHoleView extends EntityView{
 	
 	Circle circle;
 	Body body;
 
 	public BlackHoleView(BlackHole blackHole){
 		super(blackHole);
		circle = new Circle(mBlackHole.getPosX(),mBlackHole.getPosY(),mBlackHole.getRadius());
 		//mBlackHole.addPropertyChangeListener(this);
 		//mBlackHole.setBody(mBlackHole.getPhysics().createType(circle,mBlackHole)); 
 	}
 	
 	private void setBody(Body body){
 		this.body=body;
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getPropertyName()=="radius"){
 			updateRadius();
 		}
 	}
 
 	private void updateRadius() {
 		
 		
 	}
 
 }
