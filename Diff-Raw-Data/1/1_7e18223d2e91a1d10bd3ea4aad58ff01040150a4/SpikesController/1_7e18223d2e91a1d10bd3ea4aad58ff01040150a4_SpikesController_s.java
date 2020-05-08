 package controller;
 
 import org.jbox2d.callbacks.ContactImpulse;
 import org.jbox2d.callbacks.ContactListener;
 import org.jbox2d.collision.Manifold;
 import org.jbox2d.dynamics.Fixture;
 import org.jbox2d.dynamics.contacts.Contact;
 
 import model.Spikes;
 import view.CharacterView;
 import view.SpikesView;
 
 public class SpikesController implements ContactListener {
 	
 	private Spikes spikes;
 	private SpikesView spikesView;
 	private InGameController inGameController;
 
 	public SpikesController(InGameController inGameController, int index){
 		this.inGameController = inGameController;
 		this.spikes = new Spikes(inGameController.getBlockMapController().getSpikesMap().getBlockList().get(index).getPosX(), 
 								inGameController.getBlockMapController().getSpikesMap().getBlockList().get(index).getPosY());
 		this.spikesView = new SpikesView(this.spikes, inGameController.getWorldController().getWorldView());
 		inGameController.getWorldController().getWorldView().getjBox2DWorld().setContactListener(this);
 	}
 
 	public Spikes getSpikes() {
 		return spikes;
 	}
 
 	public SpikesView getSpikesView() {
 		return spikesView;
 	}
 
 	@Override
 	public void beginContact(Contact contact) {
 		Fixture fixtA = contact.getFixtureA();
 		Fixture fixtB = contact.getFixtureB();
 		
 		if(fixtA.getUserData() != null && fixtB.getUserData() != null) {
 			if(fixtA.getUserData().equals("spikes") && fixtB.getUserData().equals("player") && 
 					inGameController.getCharacterController().getCharacter().getTimeSinceHit() > 1) {
 				this.inGameController.getCharacterController().getCharacterView().animateBlinking();
 				this.inGameController.getCharacterController().getCharacter().setOnSpikes(true);
				this.inGameController.getCharacterController().getCharacter().setTimeSinceHit(0);
 			}
 		}
 	}
 
 	@Override
 	public void endContact(Contact contact) {
 		Fixture fixtA = contact.getFixtureA();
 		Fixture fixtB = contact.getFixtureB();
 		
 		if(fixtA.getUserData() != null && fixtB.getUserData() != null) {
 			if(fixtA.getUserData().equals("spikes") && fixtB.getUserData().equals("player")) {
 				inGameController.getCharacterController().getCharacterView().animateWalking();	
 				this.inGameController.getCharacterController().getCharacter().setOnSpikes(false);
 			}
 		}
 	}
 
 	@Override
 	public void postSolve(Contact contact, ContactImpulse impulse) {}
 
 	@Override
 	public void preSolve(Contact contact, Manifold oldManifold) {}
 	
 }
