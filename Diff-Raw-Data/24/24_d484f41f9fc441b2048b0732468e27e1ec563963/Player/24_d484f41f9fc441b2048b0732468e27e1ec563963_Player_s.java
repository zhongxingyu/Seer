 package ships;
 
 import application_states.GameState;
 import application_states.PauseState;
 import base.GameUtils;
 import base.Runner;
 import bullets.EMP;
 import bullets.Gun;
 import bullets.LaserBullet;
 import bullets.MultiShot;
 import bullets.PlasmaCannon;
 
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.input.RawInputListener;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.event.JoyAxisEvent;
 import com.jme3.input.event.JoyButtonEvent;
 import com.jme3.input.event.KeyInputEvent;
 import com.jme3.input.event.MouseButtonEvent;
 import com.jme3.input.event.MouseMotionEvent;
 import com.jme3.input.event.TouchEvent;
 import com.jme3.math.ColorRGBA;
 import com.jme3.scene.Spatial;
 
 public class Player extends Entity implements ActionListener, RawInputListener {
 
 	public float initialShield = 100;
 	public float shield = 100;
 	private float regenTime = 20;
 	private float regenRate = 20;
 	public float initialHealth = 100;
 
 	private float xRot = 0;
 	private float yRot = 0;
 	private float zRot = 0;
 	private float xRotSpeed = .3f;
 	private float yRotSpeed = .3f;
 	private float zRotSpeed = .3f;
 
 	private float lastHit = 0;
 
	public Gun[] weapons = new Gun[5];
 	public int weapon1 = 1, weapon2 = 2;
 
 	private float threshold = .23f;
 
 	public Player(Spatial model) {
 		super(model);
 		health = 100f;
 		speed = 30f;
 
 		lastHit = Runner.getInstance().getTimer().getTimeInSeconds();
 		setCollisionShape(CollisionShapeFactory.createDynamicMeshShape(model));
 
 		Gun laserGun = new Gun(LaserBullet.class, this, ColorRGBA.Green);
 		laserGun.speed = 700;
 		laserGun.damage = 3.5f;
 		weapons[1] = laserGun;
 
 		Gun plasmaGun = new Gun(PlasmaCannon.class, this, ColorRGBA.Orange);
 		plasmaGun.delayTime = 5;
 		plasmaGun.maxRange = 500;
 		plasmaGun.damage = 100;
 		plasmaGun.speed = 200;
 		weapons[2] = plasmaGun;
 
 		Gun empGun = new Gun(EMP.class, this, ColorRGBA.White);
 		empGun.delayTime = 10;
 		empGun.damage = 0;
 		empGun.maxRange = 1000;
 		weapons[3] = empGun;
 
 		Gun spreadGun = new Gun(MultiShot.class, this, ColorRGBA.Gray);
 		spreadGun.error = 0.05f;
 		spreadGun.maxRange = 500;
 		spreadGun.delayTime = 1f;
 		spreadGun.damage = 1;
 		spreadGun.speed = 300;
 		weapons[4] = spreadGun;
		
 		Gun superLaserGun =  new Gun(LaserBullet.class, this, ColorRGBA.Yellow);
 		superLaserGun.speed = 1000;
 		superLaserGun.damage = 10f;
 		superLaserGun.scale = 2f;
 		weapons[5] = superLaserGun;
		
 		Gun superPlasmaGun = new Gun(PlasmaCannon.class, this, ColorRGBA.Red);
 		superPlasmaGun.delayTime = 4;
 		superPlasmaGun.maxRange = 500;
 		superPlasmaGun.damage = 200;
 		superPlasmaGun.speed = 300;
 		superPlasmaGun.scale = 2;
 		weapons[6] = superPlasmaGun;
		
 		Gun superEmpGun = new Gun(EMP.class, this, ColorRGBA.White);
 		superEmpGun.delayTime = 8;
 		superEmpGun.damage = 10;
 		superEmpGun.maxRange = 10000;
		weapons[7] = empGun;
		
 		Gun superSpreadGun = new Gun(MultiShot.class, this, ColorRGBA.Gray);
 		superSpreadGun.error = 0.07f;
 		superSpreadGun.maxRange = 750;
 		superSpreadGun.delayTime = 0.7f;
 		superSpreadGun.damage = 5;
 		superSpreadGun.speed = 500;
 		superSpreadGun.scale = 2;
		weapons[8] = spreadGun;
 	}
 
 	@Override
 	public void updateObject(float tpf) {
 		getNode().rotate(	yRot*yRotSpeed*tpf,
 				xRot*xRotSpeed*tpf, 
 				0);
 
 		if(Runner.getInstance().getTimer().getTimeInSeconds() - lastHit > regenTime)
 			shield = Math.min(shield+regenRate*tpf, initialShield);
 
 		super.updateObject(tpf);
 	}
 
 	@Override
 	public void damage(float amount) {
 		if(shield > 0){
 			shield -= amount;
 			shield = Math.max(0, shield);
 		}
 		else health -= amount;
 	}
 
 	@Override
 	public void onJoyAxisEvent(JoyAxisEvent evt) {
 		if (evt.getAxis().getLogicalId().equals("y")) {
 			yRot = evt.getValue();
 			if (yRot < threshold && yRot > -threshold)
 				yRot = 0;
 		}
 		if (evt.getAxis().getLogicalId().equals("x")) {
 			xRot = -evt.getValue();
 			if (xRot < threshold && xRot > -threshold)
 				xRot = 0;
 		}
 	}
 
 	@Override
 	public void onJoyButtonEvent(JoyButtonEvent evt) {
 		if (evt.isPressed()) {
 			switch (evt.getButton().getButtonId()) {
 			case GameUtils.XBOX_START:
 				Runner.getState(GameState.class).setEnabled(false);
 				Runner.getState(PauseState.class).setEnabled(true);
 				break;
 			case GameUtils.XBOX_A_BUTTON:
 				weapons[weapon1].fire();
 				break;
 			case GameUtils.XBOX_B_BUTTON:
 				weapons[weapon2].fire();
 				break;
 			}
 
 		}
 	}
 
 	@Override
 	public void onAction(String name, boolean isPressed, float tpf) {
 		if(isPressed){
 			switch(name){
 			case "Up":
 				yRot = -1;
 				break;
 			case "Down":
 				yRot = 1;
 				break;
 			case "Right":
 				xRot = -1;
 				break;
 			case "Left":
 				xRot = 1;
 				break;
 			case "Shoot1":
				weapons[weapon1].fire(); 
 				break;
 			case "Shoot2":
 				weapons[weapon2].fire();
 				break;
 			}
 		}
 		else
 			switch(name){
 			case "Up": case "Down":
 				yRot = 0;
 				break;
 			case "Right": case "Left":
 				xRot = 0;
 				break;
 			}
 	}
 
 	public void beginInput() {}
 	public void endInput() {}
 	public void onMouseMotionEvent(MouseMotionEvent evt) {}
 	public void onMouseButtonEvent(MouseButtonEvent evt) {}
 	public void onKeyEvent(KeyInputEvent evt) {}
 	public void onTouchEvent(TouchEvent evt) {}
 }
