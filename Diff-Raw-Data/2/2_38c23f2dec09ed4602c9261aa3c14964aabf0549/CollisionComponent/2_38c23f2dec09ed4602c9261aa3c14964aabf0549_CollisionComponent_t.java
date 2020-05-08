 package com.liongrid.infectosaurus.components;
 
 import com.liongrid.gameengine.CollisionCircle;
 import com.liongrid.gameengine.CollisionObject;
 import com.liongrid.gameengine.Component;
 import com.liongrid.gameengine.Shape;
 import com.liongrid.gameengine.tools.Vector2;
 import com.liongrid.infectosaurus.InfectoGameObject;
 
 /**
  * @author Lastis
  *	For this component to work, the InfectoGameObject needs to have a collision object with
  *	a shape. This component moves the parent away from any InfectoGameObjects it collides with.
  */
 public class CollisionComponent extends Component<InfectoGameObject>{
 
 	@Override
 	public void update(float dt, InfectoGameObject parent) {
 		if(parent.collisionObject == null) return;
 		CollisionObject parCollisionObject = parent.collisionObject;
 		for(int i = 0; i < parCollisionObject.collisionCnt ; i++){
 			moveObjectAway(parCollisionObject, parCollisionObject.collisions[i]);
 		}
 		
 	}
 	
 	private void moveObjectAway(CollisionObject shape1, CollisionObject shape2){
 		if(shape1.getShape() == Shape.CIRCLE && 
 			shape2.getShape() == Shape.CIRCLE){
 			
 			Shape.Circle circle1 = (Shape.Circle) shape1;
 			Shape.Circle circle2 = (Shape.Circle) shape2;
 			
 			Vector2 pos1 = circle1.getPos();
 			Vector2 pos2 = circle2.getPos();
 			float radius1 = circle1.getRadius();
 			float radius2 = circle2.getRadius();
 			
 			float[] AB = {pos1.x - pos2.x, pos1.y - pos2.y};
 			float absAB = (float) Math.sqrt(AB[0] * AB[0] + AB[1] * AB[1]);
 			
 			float cosPhi;
 			float sinPhi;
 			if(absAB != 0){
 				cosPhi = AB[0] / absAB;
 				sinPhi = AB[1] / absAB;
 			}else{ // If it`s zero, just teleport to the side instead of getting NaN
 				cosPhi = 1;
 				sinPhi = 0;
 			}
			
 			pos1.x = pos2.x + cosPhi * radius1 + cosPhi * radius2;
 			pos1.y = pos2.y + sinPhi * radius1 + sinPhi * radius2;
 		}
 	}
 }
