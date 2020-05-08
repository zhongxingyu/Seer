 package uk.co.sparedice.fmtanks1;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class Tank extends PhysicsActor {
 	
 	/* The angle of the tank main body. The tank will go forward in this direction */
 	private float bodyAngle = 0f; 
 	
 	/* The angle of the turret/gun. The tank will fire weapons in this direction */
 	private float turretAngle = 0f;
 	
 	private static final int TEAM_PLAYER = 0; // Human controlled tanks in single player
 	private static final int TEAM_ENEMY = 1; // AI controlled tanks in single player
 	private static final int TEAM_RED = 2; // Red Team in multiplayer
 	private static final int TEAM_BLUE = 3; // Blue Team in multiplayer
 	
 	/* The team ID of the tank. Defines who it can damage and who can damage it */
 	private int team = TEAM_PLAYER;
 	
 	private static Texture bodyTexture, turretTexture;
 	private static int half_width, half_height;
 	private static int width, height;
 	
 	private Sprite bodySprite, turretSprite;
 	
 	 public Tank() {
 		posX = 200;
 		posY = 200;
 		bodySprite = new Sprite(bodyTexture);
 		turretSprite = new Sprite(turretTexture);
 	}
 	
 	public static void loadStatic (){
 		bodyTexture = new Texture(Gdx.files.internal("tank_body_1.png"));
 		turretTexture = new Texture(Gdx.files.internal("tank_turret_1.png"));
 		half_width = bodyTexture.getWidth() / 2;
 		half_height = bodyTexture.getHeight() / 2;
 		width = bodyTexture.getWidth();
 		height = bodyTexture.getHeight();
 	}
 	
 	@Override
 	public void draw(SpriteBatch sb, float parentAlpha) {
 		bodySprite.setRotation(bodyAngle);
 		bodySprite.setPosition(posX, posY);
 		bodySprite.draw(sb);
 		turretSprite.setRotation(turretAngle);
 		turretSprite.setPosition(posX, posY);
 		turretSprite.draw(sb);
 	}
 	
	public void act(float delta){
 		turretAngle += 1;
 		applyPhysics();
 	}
 
 }
