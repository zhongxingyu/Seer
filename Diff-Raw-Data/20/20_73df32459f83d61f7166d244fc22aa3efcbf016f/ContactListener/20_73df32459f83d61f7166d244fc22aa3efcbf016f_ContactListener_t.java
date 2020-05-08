 package com.calumgilchrist.ld23.tinyworld.core;
 
 import org.jbox2d.callbacks.ContactImpulse;
 import org.jbox2d.collision.Manifold;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.contacts.Contact;
 
 public class ContactListener implements org.jbox2d.callbacks.ContactListener {
 
 	private Player player;
 	int createCount;
 	
 	public ContactListener(Player player) {
 		this.player = player;
 		createCount = 0;
 	}
 	
 	@Override
 	public void beginContact(Contact contact) {
 		
 	}
 
 	@Override
 	public void endContact(Contact contact) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void preSolve(Contact contact, Manifold oldManifold) {
 		
 	}
 
 	@Override
 	public void postSolve(Contact contact, ContactImpulse impulse) {
 		Body hitter;
 		
 		//Fixture A is never the contact, I think
		
		if (player.getBody().equals(contact.getFixtureA().m_body)) {
			
			hitter = contact.getFixtureB().m_body;
			Planetoid planet = DynamicFactory.getFromBody(hitter);
			
			if (planet != null) {
			
				player.addMass(hitter.m_mass);
	
				DynamicFactory.removeByBody(hitter);
				createCount++;
				System.out.println("Knock B");
			}
		} if (player.getBody().equals(contact.getFixtureB().m_body)) {
 			
 			hitter = contact.getFixtureA().m_body;
 			Planetoid planet = DynamicFactory.getFromBody(hitter);
 			
 			if (planet != null) {
 			
 				player.addMass(hitter.m_mass);
 	
 				DynamicFactory.removeByBody(hitter);
 				createCount++;
				System.out.println("Knock A");
 			}
 		}
		
 	}
 		
 	public int getCreateCount() {
 		return createCount;
 	}
 	
 	public void clearCreateCount() {
 		createCount = 0;
 	}
 }
