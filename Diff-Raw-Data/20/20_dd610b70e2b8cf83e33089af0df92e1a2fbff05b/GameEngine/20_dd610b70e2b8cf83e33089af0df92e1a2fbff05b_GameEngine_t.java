 package com.irr310.server;
 
 import com.irr310.server.event.AddWorldObjectEvent;
 import com.irr310.server.event.DefaultEngineEventVisitor;
 import com.irr310.server.event.EngineEvent;
 import com.irr310.server.event.InitEngineEvent;
 import com.irr310.server.event.PauseEngineEvent;
 import com.irr310.server.event.QuitGameEvent;
 import com.irr310.server.event.StartEngineEvent;
 import com.irr310.server.event.UseScriptEvent;
 import com.irr310.server.game.world.Camera;
 import com.irr310.server.game.world.LinearEngine;
 import com.irr310.server.game.world.Shape;
 import com.irr310.server.game.world.WorldObject;
 
 public class GameEngine extends Engine {
 
 	public GameEngine() {
 	}
 
 	@Override
 	protected void frame() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void processEvent(EngineEvent e) {
 		e.accept(new GameEngineEventVisitor());
 	}
 
 	private final class GameEngineEventVisitor extends DefaultEngineEventVisitor {
 		@Override
 		public void visit(QuitGameEvent event) {
 			System.out.println("stopping game engine");
 			isRunning = false;
 		}
 
 		@Override
 		public void visit(StartEngineEvent event) {
 			pause(false);
 		}
 
 		@Override
 		public void visit(InitEngineEvent event) {
 		}
 
 		@Override
 		public void visit(PauseEngineEvent event) {
 			pause(true);
 		}
 
 		@Override
 		public void visit(AddWorldObjectEvent event) {
 			WorldObject o = null;
 
 			switch (event.getType()) {
 			case CAMERA:
 				o = new Camera();
 				break;
 			case LINEAR_ENGINE:
 				o = new LinearEngine();
 				break;
 			}
 
			if(event.getPosition() != null) {
				o.setPosition(event.getPosition());
			}
			
			if(event.getRotation() != null) {
				o.setRotation(event.getRotation());
			}
			
			if(event.getLinearSpeed() != null) {
				o.setLinearSpeed(event.getLinearSpeed());
			}
			
			if(event.getRotationSpeed() != null) {
				o.setRotationSpeed(event.getRotationSpeed());
			}
			
 			o.setShape(new Shape(o, new Vect3(1, 1, 1)));
 
 			if (event.getMass() != null) {
 				o.setMass(event.getMass());
 			}
 
 			o.setName(event.getName());
 			o.init();
 			
 			GameServer.getInstance().getGame().getWorld().addObject(o);
 		}
 	}
 
 	@Override
 	protected void init() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void end() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
