 package com.tacoid.gravicious;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Manifold;
 import com.badlogic.gdx.physics.box2d.World;
 import com.tacoid.gravicious.elements.GravitationalElement;
 import com.tacoid.gravicious.elements.LevelElement;
 import com.tacoid.gravicious.elements.Planet;
 
 public class GameMechanics implements ContactListener{
 
 	public enum GameState {
 		IDLE,
 		PAUSED,
 		RUNNING
 	}
 
 	public enum PlayerState {
 		WALKING,
 		FLYING
 	}
 
 	private Level level;
 	private Player player;
 
 	private GameState gameState;
 	private PlayerState playerState;
 	private GravitationalElement walkedElement;
 	private float playerAngle;
 	private float playerDirection;
 
 	public GameMechanics() {
 		gameState = GameState.IDLE;
 	}
 
 	public void init(Level level) {
 		this.level = level;
 		player = new Player(level.getWorld());
 		player.setX(level.getStartElement().getX());
 		player.setY(level.getStartElement().getY());
 		gameState = GameState.RUNNING;
 		playerState = PlayerState.FLYING;
 		level.getWorld().setContactListener(this);
 	}
 
 	public void start() {
 		gameState = GameState.RUNNING;
 	}
 
 	public void update(float delta) {
 		if(gameState == GameState.RUNNING) {
 			level.update(delta);
 			if(playerState == PlayerState.FLYING) {
 				World world = level.getWorld();
 				world.step(delta*10, 4, 4);
 				//world.clearForces();
 				for (LevelElement l : level.getLevelElements()) {
 					if (l instanceof GravitationalElement) {
 						GravitationalElement g = (GravitationalElement) l;
 						
 						Vector2 d = g.computeAttraction(player.getBody().getPosition(), 100);
 						if (d != null) {
 							player.getBody().applyForce(d, player.getBody().getWorldCenter());
 						}
 					}
 				}
 			} else {
 				playerAngle+=(0.01*playerDirection);
 				//System.out.println(playerAngle);
 				player.setX(walkedElement.getX() + (float) ((walkedElement.getRadius()*10+20)*Math.cos(playerAngle)));
 				player.setY(walkedElement.getY() + (float) ((walkedElement.getRadius()*10+20)*Math.sin(playerAngle)));
 
 			}
 		}
 	}
 
 	public void jump() {
 		if(playerState == PlayerState.WALKING) {
 			final float verticalForce = (float) Math.sqrt(2 * walkedElement.getGravity() * (20 - walkedElement.getRadius()));
 
 			//final float tangentForce = 1;
 			
 			Vector2 d = new Vector2(player.getX()-walkedElement.getX(), player.getY()-walkedElement.getY());
 			d.nor();
 			d.mul(verticalForce);
 			
 			d.rotate(playerDirection * 30);
 			
 			/*
 			Vector2 t = new Vector2(walkedElement.getY() - player.getY(), player.getX()-walkedElement.getX());
 			t.nor();
 			t.mul(playerDirection);
 			t.mul(tangentForce);
 			d.add(t);*/
 
 			System.out.println("force " + verticalForce);
 			player.getBody().setLinearVelocity(d);
 			
 			playerState = PlayerState.FLYING;
 		}
 	}
 
 	public void setLevel(Level level) {
 		this.level = level;
 	}
 
 	public Level getLevel() {
 		return level;
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	@Override
 	public void beginContact(Contact contact) {
 		Body bodyA = contact.getFixtureA().getBody();
 		Body bodyB = contact.getFixtureB().getBody();
 
 		if (bodyA.getUserData() instanceof Planet
 				|| bodyB.getUserData() instanceof Planet) {
 			System.out.println("Grounded");
 			Planet planet;
 			Body playerBody;
 			if(bodyA.getUserData() instanceof Planet) {
 				planet = (Planet) bodyA.getUserData();
 				playerBody = bodyB;
 			} else {
 				planet = (Planet) bodyB.getUserData();
 				playerBody = bodyA;
 			}
 
 			playerAngle = -(float)( Math.atan2(player.getX() - planet.getX(),  player.getY() - planet.getY())) + (float) Math.PI/2;
 			
 			/* Explication du calcul: 
 			 * On calcul tangent, le vecteur tangent au cercle au niveau du point de colision, dans le sens trigonometrique
			 * Le produit vectoriel entre tangent et le vecteur vitesse nous donne la projection scalaire de la vitesse sur tangent (car tangent est normalis�)
 			 * Si c'est positif, on tourne dans le sens trigo, sinon dans le sens inverse
 			 * */
 			Vector2 tangent = new Vector2(planet.getY() - player.getY(), player.getX()-planet.getX());
 			tangent.nor();
 			
			float cross = tangent.crs(playerBody.getLinearVelocity());
			if(cross>0) {
 				playerDirection = 1.0f;
 			} else {
 				playerDirection = -1.0f;
 			}
 			
			System.out.println(cross);
			
 			System.out.println("Angle="+playerAngle);
 			playerState = PlayerState.WALKING;
 			walkedElement = planet;
 		}
 	}
 
 	@Override
 	public void endContact(Contact contact) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void preSolve(Contact contact, Manifold oldManifold) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void postSolve(Contact contact, ContactImpulse impulse) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
