 package com.infectosaurus.components;
 
 import android.util.Log;
 
 import com.infectosaurus.BaseObject;
 import com.infectosaurus.GameObject;
 import com.infectosaurus.GameObjectHandler;
 import com.infectosaurus.GamePointers;
 
 public class AggressivMoveComponent extends Component{
 	
 	private GameObjectHandler gameObjHandler;
 
 	public AggressivMoveComponent(){
 		gameObjHandler = BaseObject.gamePointers.gameObjectHandler;
 	}
 
 	public void update(float dt, BaseObject parent) {
 		GameObject gameObject = (GameObject) parent;
 		GameObject target = gameObjHandler.getClosest(gameObject, gameObject.team.Human);
 		if(target != null){
			Log.d("MoveComp", "Found target, changing course");
 			gameObject.vel.set(target.pos);
 			gameObject.vel.subtract(gameObject.pos);
 			gameObject.vel.normalize();
 			gameObject.vel.multiply(gameObject.speed);
 		}
 	}
 }
