 package game.things;
 
 import game.*;
 
 public class Player extends AbstractGameThing {
 	private String renderer;
 	private final static int WALKDELAY = 50;
 
 	public Player(GameWorld world, String renderer){
 		super(world);
 		this.renderer = renderer;
 		update();
 	}
 
 	public Player(GameWorld world){
 		this(world, "character_cordi_empty");
 	}
 
 	public String renderer(){
 		return this.renderer;
 	}
 
	public String name(){
		return "<insert name here>";
	}

 	public boolean moveTo(final Level.Location where, final Runnable ondone){
 		/*
 		// call ((Level.Location)location()).nextTo(where).put(this) every so often, ensure only one moveTo at a time ..
 		return false; // can't move
 		*/
 		/*
 		where.put(this);
 		if(ondone != null)
 			ondone.run();
 		return true;
 		*/
 		Location l = location();
 		if(l instanceof Level.Location){
 			Level.Location to = ((Level.Location)l).nextTo(where, this);
 			if(to == null)
 				return false;
 			world().schedule(new Runnable(){
 				public void run(){
 					step(where, ondone, stepIdent = new Object());
 				}
 			}, WALKDELAY);
 			return true;
 		}
 		return false;
 	}
 
 	private Object stepIdent;
 	public void step(final Level.Location where, final Runnable ondone, final Object ident){
 		if(stepIdent != ident)
 			return;
 		Location l = location();
 		if(where.equals(l)){
 			if(ondone != null)
 				ondone.run();
 		}
 		else if(l instanceof Level.Location){
 			Level.Location to = ((Level.Location)l).nextTo(where, this);
 			if(to != null){
 				to.put(this);
 				world().schedule(new Runnable(){
 					public void run(){
 						step(where, ondone, ident);
 					}
 				}, WALKDELAY);
 			}
 		}
 	}
 
 	public boolean moveTo(Level.Location where){
 		return moveTo(where, null);
 	}
 }
