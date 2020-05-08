 package com.pix.mind.world;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.math.Interpolation;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.Manifold;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.ActiveColors;
 import com.pix.mind.actors.PixGuyActor;
 import com.pix.mind.actors.PlatformActivatorActor;
 import com.pix.mind.actors.StaticPlatformActor;
 import com.pix.mind.actors.StaticWallActor;
 import com.pix.mind.box2d.bodies.PixGuy;
 
 public class Box2DWorldContactListener implements ContactListener {
 	PixMindGame game;
 	private ArrayList<StaticPlatformActor> platformList;
 	private ArrayList<PlatformActivatorActor> activatorList;
 	private boolean colliding = false;
 	private boolean walls = false;
 	private ActiveColors actColors;
 	private float lastPlatformHeight;
 	private float anteriorHeight;
 	private Screen nextLevel;
 	private PixMindGuiInitialization gui;
 
 	public Box2DWorldContactListener(PixMindGame game,
 			PixMindBox2DInitialization box2D, ActiveColors actColors) {
 		initialize(game, box2D, actColors);
 	}
 
 	public Box2DWorldContactListener(PixMindGame game,
 			PixMindBox2DInitialization box2D, ActiveColors actColors,
 			boolean walls) {
 		this.walls = walls;
 		initialize(game, box2D, actColors);
 	}
 
 	private void initialize(PixMindGame game, PixMindBox2DInitialization box2D,
 			ActiveColors actColors) {
 		this.game = game;
 		this.platformList = box2D.getPlatformList();
 		this.activatorList = box2D.getActivatorList();
 		this.actColors = actColors;
 
 	}
 
 	@Override
 	public void beginContact(Contact contact) {
 		Fixture fixGuy = null;
 		Fixture fixPlatform = null;
 		Fixture fixActivator = null;
 		Fixture fixWall = null;
 		Fixture otherContact = null;
 		
 		// get fixture pixguy
 			if (contact.getFixtureA().getUserData() instanceof PixGuyActor) {
 				fixGuy = contact.getFixtureA();
 				otherContact = contact.getFixtureB();
 			} else {
 				fixGuy = contact.getFixtureB();
 				otherContact = contact.getFixtureA();
 			}
 
 		// get fixture Platform
 			if (otherContact.getUserData() instanceof StaticPlatformActor) {
 				fixPlatform = otherContact;
 				// jump only if collide with a platform and its not sensor
 				if (!fixPlatform.isSensor()){
 					System.out.println("Colision PLATAFORMA");
 					collisionWithPlatform(fixPlatform, fixGuy);
 				}
 			} else
 		// get fixture PlatformActivator
 			if (otherContact.getUserData() instanceof PlatformActivatorActor) {
 				fixActivator = otherContact;
 				// collision with an Activator
 				System.out.println("Colision ACTIVADOR");
 				collisionWithActivator(fixActivator);
 			} else
 		// get fixture WallActor
 //			if (walls) { para qu necesitas "walls"?
 				if (otherContact.getUserData() instanceof StaticWallActor) {
 					fixWall = otherContact;
 					System.out.println("Colision MURO");
 					collisionWithWall(fixWall, fixGuy);
 //				}
 			}
 		colliding = true;
 	}
 
 	private void collisionWithActivator(Fixture fixActivator) {
 		PlatformActivatorActor platformActivatorActor = (PlatformActivatorActor) fixActivator
 				.getUserData();
 		if (platformActivatorActor.isActive()) {
 			// if activator is black go to next level
 			if (platformActivatorActor.color.equals(Color.BLACK)) {
 				gui.getMenuInGame().showWin();
 			} else {
 				// get all platform of the same color and change state
 				for (StaticPlatformActor sp : platformList) {
 					if (platformActivatorActor.color.equals(sp.color))
 						sp.setActive(false);
 				}
 				// get all activator of the same color and change state
 				for (PlatformActivatorActor sp : activatorList) {
 					if (platformActivatorActor.color.equals(sp.color)) {
 						sp.setActive(false);
 					}
 				}
 				actColors.removeActiveColor(actColors
 						.getActiveColorByColor(platformActivatorActor.color));
 				actColors.showArray();
 			}
 
 		} else {
 			if(PixMindGame.infoFx)				
 				PixMindGame.getGettingActivator().play(0.2f);
 			for (StaticPlatformActor sp : platformList) {
 				if (platformActivatorActor.color.equals(sp.color))
 					sp.setActive(true);
 			}
 			for (PlatformActivatorActor sp : activatorList) {
 				if (platformActivatorActor.color.equals(sp.color)) {
 					sp.setActive(true);
 					// System.out.println("Activating the color: " + sp.color);
 				}
 			}
 			// add new color
 			actColors.addNewActiveColor(actColors
 					.getActiveColorByColor(platformActivatorActor.color));
 			// remove older color, now with 0 position
 			for (int i = 0; i < actColors.activeColorActors.size(); i++) {
 				if (actColors.activeColorActors.get(i).position == 0) {
 					for (PlatformActivatorActor sp : activatorList) {
 						if (sp.color
 								.equals(actColors.activeColorActors.get(i).c))
 							sp.setActive(false);
 					}
 					for (StaticPlatformActor sp : platformList) {
 						if (sp.color
 								.equals(actColors.activeColorActors.get(i).c))
 							sp.setActive(false);
 					}
 				}
 			}
 		}
 	}
 
 	private void collisionWithPlatform(Fixture fixPlatform, Fixture fixGuy) {
 		// only jump if bottom position of pixguy is equal or above of top
 		// position of the platform
 		StaticPlatformActor platformActor = (StaticPlatformActor) fixPlatform
 				.getUserData();
 		float topPosPlatform = fixPlatform.getBody().getPosition().y
 				+ platformActor.getHeight() * PixMindGame.WORLD_TO_BOX / 2;
 		float bottomPosGuy = fixGuy.getBody().getPosition().y
 				- PixGuy.pixHeight * PixMindGame.WORLD_TO_BOX / 2;
 
 		if (bottomPosGuy >= topPosPlatform) {
 			if(PixMindGame.infoFx)			
 				PixMindGame.getBoing().play(0.7f);
 			anteriorHeight = lastPlatformHeight;
 			lastPlatformHeight = (fixPlatform.getBody().getPosition().y + platformActor
 					.getHeight() * PixMindGame.WORLD_TO_BOX / 2)
 					* PixMindGame.BOX_TO_WORLD;
 			if (lastPlatformHeight < anteriorHeight) {
 				anteriorHeight = lastPlatformHeight;
 			}
 			fixGuy.getBody().setLinearVelocity(
 					fixGuy.getBody().getLinearVelocity().x, 0);
 
 			fixGuy.getBody().applyLinearImpulse(new Vector2(0, 0.65f),
 					fixGuy.getBody().getWorldCenter(), true);
 
 			// animation
 			PixGuyActor pixguyActor = (PixGuyActor) fixGuy.getUserData();
 			if (pixguyActor.getActions().size != 0)
 				pixguyActor.removeAction(pixguyActor.getActions().get(0));
 			Interpolation interpolation = Interpolation.linear;
 			pixguyActor.addAction(Actions.sequence(
 					Actions.scaleTo(1.2f, 0.8f, 0.25f, interpolation),
 					Actions.scaleTo(1f, 1f, 0.25f, interpolation),
 					Actions.scaleTo(0.8f, 1.2f, 0.25f, interpolation),
 					Actions.scaleTo(1, 1, 0.25f, interpolation)));
 		}
 	}
 
 	
 	private void collisionWithWall(Fixture fixWall, Fixture fixGuy) {
 		 // HAY QUE RETOCARLO, DEBERAMOS DESHABILITAR UN CIERTO TIEMPO EL CONTROLADOR O HACER MENOS CONTUNDENTE SU EFECTO
 		if(PixMindGame.infoFx)			
 		PixMindGame.getBoing().play(0.7f);
 		
 		StaticWallActor wallActor = (StaticWallActor) fixWall.getUserData();
 		PixGuyActor pixActor = (PixGuyActor) fixGuy.getUserData();
 		
 		float rW = wallActor.getX() + (wallActor.getWidth() * PixMindGame.WORLD_TO_BOX) / 2;
 		float lW = wallActor.getX() - (wallActor.getWidth() * PixMindGame.WORLD_TO_BOX) / 2;
 		
 		float rP = pixActor.getX() + (pixActor.getWidth() * PixMindGame.WORLD_TO_BOX) / 2;
 		float lP = pixActor.getX() - (pixActor.getWidth() * PixMindGame.WORLD_TO_BOX) / 2;
 		
 		
 		//fixGuy.getBody().setLinearVelocity(fixGuy.getBody().getLinearVelocity().x, fixGuy.getBody().getLinearVelocity().y);
 		
 		fixGuy.getBody().setLinearVelocity(0, 0);
 		
 		float velActualX = fixGuy.getBody().getLinearVelocity().x;
 		float velActualY = fixGuy.getBody().getLinearVelocity().y;
 		
 		if (fixGuy.getBody().getPosition().x < fixWall.getBody().getPosition().x){
 			
 			fixGuy.getBody().setLinearVelocity(fixGuy.getBody().getLinearVelocity().x, fixGuy.getBody().getLinearVelocity().y);
 			fixGuy.getBody().setLinearVelocity(fixGuy.getBody().getLinearVelocity().x, fixGuy.getBody().getLinearVelocity().y);
 			
			
 		}
 		else{
 			
 			fixGuy.getBody().setLinearVelocity(- fixGuy.getBody().getLinearVelocity().x, fixGuy.getBody().getLinearVelocity().y);
 			fixGuy.getBody().applyLinearImpulse(new Vector2(2.0f, 0.6f), fixGuy.getBody().getWorldCenter(), true);
 			
 		}
 		
 	}
 	
 	@Override
 	public void endContact(Contact contact) {
 		colliding = false;
 	}
 
 	@Override
 	public void preSolve(Contact contact, Manifold oldManifold) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void postSolve(Contact contact, ContactImpulse impulse) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public boolean isColliding() {
 		return colliding;
 	}
 
 	public float getLastPlatformHeight() {
 		return lastPlatformHeight;
 	}
 
 	public void setLastPlatformHeight(float lastPlatformHeight) {
 		this.lastPlatformHeight = lastPlatformHeight;
 	}
 
 	public float getAnteriorHeight() {
 		return anteriorHeight;
 	}
 
 	public void setAnteriorHeight(float anteriorHeight) {
 		this.anteriorHeight = anteriorHeight;
 	}
 
 	public Screen getNextLevel() {
 		return nextLevel;
 	}
 
 	public void setNextLevel(Screen nextLevel) {
 		this.nextLevel = nextLevel;
 	}
 
 	public PixMindGuiInitialization getGui() {
 		return gui;
 	}
 
 	public void setGui(PixMindGuiInitialization gui) {
 		this.gui = gui;
 	}
 }
