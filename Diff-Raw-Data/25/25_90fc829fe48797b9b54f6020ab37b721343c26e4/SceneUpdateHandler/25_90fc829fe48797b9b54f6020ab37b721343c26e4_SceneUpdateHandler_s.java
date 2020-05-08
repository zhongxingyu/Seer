 package is.nord.dutchess;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import org.anddev.andengine.engine.handler.IUpdateHandler;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.text.ChangeableText;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
 import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
 
 import android.util.Log;
 
 import com.badlogic.gdx.math.Vector2;
 
 public class SceneUpdateHandler implements IUpdateHandler, IAccelerometerListener {
 
 	private CodenameDutchess activity;
 	private PhysicsWorld physicsWorld;
 	private Float timeLeft;
 	private Integer coinsLeft;
 	private ChangeableText timeText;
 	private ChangeableText coinsText;
 	private Scene scene;
 	private AgentSprite agent;
 	private List<GameObject> gameObjects;
 	
 	public SceneUpdateHandler(CodenameDutchess activity, PhysicsWorld physicsWorld, ChangeableText timeText, ChangeableText coinsText, float levelTime, int coinsLeft, Scene scene, AgentSprite agent) {
 		this.activity = activity;
 		this.physicsWorld = physicsWorld;
 		this.timeLeft = levelTime;
 		this.coinsLeft = coinsLeft;
 		this.timeText = timeText;
 		this.coinsText = coinsText;
 		this.scene = scene;
 		this.agent = agent;
 	}
 	
 	public void setGameObjects(List<GameObject> gameObjects) {
 		this.gameObjects = gameObjects;
 	}
 	
 	@Override
 	public void onUpdate(float pSecondsElapsed) {
 		timeLeft -= pSecondsElapsed;
 		if(timeLeft < 0.0) {
 			this.activity.gameOver();
 			return;
 		}
 		if(coinsLeft == 0) {
 			this.activity.gameWon();
 			return;
 		}
 		timeText.setText(String.valueOf(timeLeft.intValue()));
 		
 		for(GameObject gameObject: gameObjects) {
 			if(agent.collidesWith(gameObject))
 				gameObject.onCollision();
 		}
 	}
 
 	@Override
 	public void reset() {
 		// TODO Auto-generated method stub
 
 	}
 	
 	public void coinCollision(CoinSprite coin) {
 		if(!coin.isEnabled())
 			return;
 		
 		this.coinsLeft--;
 		this.coinsText.setText(coinsLeft.toString());
 		this.scene.getTopLayer().removeEntity(coin);
 		coin.disable();
 	}
 
 	@Override
 	public void onAccelerometerChanged(AccelerometerData pAccelerometerData) {
 		this.physicsWorld.setGravity(new Vector2(pAccelerometerData.getY(), pAccelerometerData.getX()));
 	}
 
 }
