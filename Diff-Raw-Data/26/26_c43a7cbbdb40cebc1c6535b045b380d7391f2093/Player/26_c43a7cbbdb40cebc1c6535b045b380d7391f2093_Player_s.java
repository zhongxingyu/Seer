 package game.things;
 
 import game.*;
 
 import serialization.*;
 
 import java.util.*;
 
 public class Player extends AbstractGameThing {
 	public static void makeSerializer(SerializerUnion<GameThing> union, final GameWorld world){
 		union.addIdentifier(new SerializerUnion.Identifier<GameThing>(){
 			public String type(GameThing g){
 				return g instanceof Player? "player" : null;
 			}
 		});
 
 		union.addSerializer("player", new Serializer<GameThing>(){
 			public Tree write(GameThing o){
 				Player in = (Player)o;
 				Tree out = new Tree();
 				out.add(new Tree.Entry("type", new Tree(in.renderer)));
 				out.add(new Tree.Entry("name", new Tree(in.name)));
 				out.add(new Tree.Entry("location", LocationS.s(null).write(in.lastLocation == null? in.location() : in.lastLocation)));
 				return out;
 			}
 
 			public GameThing read(Tree in){
 				return new Player(world, in.find("type").value(), in.find("name").value(), LocationS.s(world).read(in.find("location")));
 			}
 		});
 	}
 
 	private Location lastLocation;
 	private String renderer;
 	private int health;
 	private final String name;
 	private final static int WALKDELAY = 50;
 	private final static List<String> interactions;
 	private final Container inventory;
 	static {
 		interactions = new LinkedList<String>();
 		interactions.add("follow");
 		interactions.add("attack");
 	}
 
 	public Player(GameWorld world, String renderer, String n, Location spawn){
 		super(world);
 		this.renderer = renderer;
 		name = n;
 		world.setPlayer(n, this);
 		lastLocation = spawn != null? spawn : LocationS.NOWHERE;
 		update();
 		this.health = 100;
		inventory = new Container();
 	//	world.schedule(blah, 1000);
 	}
 /*
 	private Runnable blah = new Runnable(){
 		public void run(){
 			world().schedule(blah, 5000);
 		}
 	};
 	*/
 
 	public Player(GameWorld world, String renderer, String n){
 		this(world, renderer, n, spawnPointWorkAroundCrap(world.getSpawnPoint()));
 	}
 
 	private static Location spawnPointWorkAroundCrap(SpawnPoint sp){
 		return sp != null? sp.location() : null;
 	}
 
 	public Player(GameWorld world){
 		this(world, "cordi");
 	}
 
 	public Player(GameWorld world, String renderer){
 		this(world, renderer, "<insert name here>");
 	}
 
 	public void logout(){
 		lastLocation = location();
 		LocationS.NOWHERE.put(this);
 	}
 
 	public void login(){
 		lastLocation.put(this);
 		lastLocation = null;
 	}
 
 	public String renderer(){
 		return "character_"+this.renderer+"_"+this.rendererState();
 	}
 	
 	private String rendererState() { // TODO: depends on equipped state
 		return "empty";
 	}
 
 	public String name(){
 		return name;
 	}
 
 	public List<String> interactions(){
 		return interactions;
 	}
 
 	public void interact(String name, Player who){
 		if(name != null) {
 			if(name.equals("follow"))
 				who.follow(this);
 			else if(name.equals("attack")){
 				who.attack(this);
 			}
 		}
 	}
 
 	public boolean moveTo(final Level.Location where, final int dist, final Runnable ondone, final boolean keepfollow){
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
 		if(!keepfollow){
 			following = null;
 			attacking = null;
 		}
 		Location l = location();
 		if(l instanceof Level.Location){
 			Level.Location to = ((Level.Location)l).nextTo(where, this, dist);
 			if(to == null)
 				return false;
 			world().schedule(new Runnable(){
 				public void run(){
 					step(where, ondone, dist, stepIdent = new Object());
 				}
 			}, WALKDELAY);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean moveTo(Level.Location where, int dist, Runnable ondone){
 		return moveTo(where, dist, ondone, false);
 	}
 
 	public boolean moveTo(Level.Location where, Runnable ondone){
 		return moveTo(where, 0, ondone);
 	}
 
 	private Object stepIdent;
 	private void step(final Level.Location where, final Runnable ondone, final int dist, final Object ident){
 		if(stepIdent != ident)
 			return;
 		Location l = location();
 		if(!(l instanceof Level.Location))
 			return;
 		if(((Level.Location)l).level().equals(where.level()) && where.dist((Level.Location)l) <= dist){
 			if(ondone != null)
 				ondone.run();
 		}else{
 			Level.Location to = ((Level.Location)l).nextTo(where, this, dist);
 			if(to != null){
 				to.put(this);
 				world().schedule(new Runnable(){
 					public void run(){
 						step(where, ondone, dist, ident);
 					}
 				}, WALKDELAY);
 			}
 		}
 	}
 
 	private GameThing following;
 	// Mm .. can't go: final Runnable tracker = .. and have it reference itself.
 	private Runnable tracker;
 	public void follow(final GameThing g, final int dist){
 		following = g;
 		tracker = new Runnable(){
 			public void run(){
 				if(g == following){
 					Location l = g.location();
 					if(l instanceof Level.Location && moveTo((Level.Location)l, 1, null, true))
 						g.track(tracker);
 				}
 			}
 		};
 		g.track(tracker);
 		tracker.run();
 	}
 
 	public void follow(GameThing g){
 		follow(g, 0);
 	}
 
 	private GameThing attacking;
 	private Runnable attacker;
 	public void attack(final GameThing g){
 		attacking = g;
 		attacker = new Runnable(){
 			public void run(){
 				if(attacking == g){
 					Location l = g.location();
 					if(l instanceof Level.Location){
 						Location ml = location();
 						if(ml instanceof Level.Location && ((Level.Location)l).dist((Level.Location)ml) <= 2){
 							animate("character_cordi_empty_punch");
 							damage(g, 10);
 						}
 					}
 					world().schedule(attacker, 500);
 				}
 			}
 		};
 		follow(g, 2);
 		attacker.run();
 		/*
 		if(attacking == g){
 			if(((Level.Location)this.location()).dist((Level.Location) g.location()) <= 2){
 				world().schedule(new Runnable(){
 					public void run(){
 						damage(g, 10);
 					}
 				}, 500);
 			}
 			else{
 				world().schedule(new Runnable(){
 					public void run(){
 						moveTo(((Level.Location)g.location()), 1, null, true);
 					}
 				}, 500);
 			}
 		}
 		*/
 	}
 	
 	public void damage(GameThing g, int amount){
 		((Player)g).health -= amount;
 		System.out.println(this.name() + " damages "+g.name() + " and his health is now " + ((Player)g).health);
 	}
 
 	public boolean moveTo(Level.Location where){
 		return moveTo(where, null);
 	}
 
 	public void pickup(GameThing g){
		
//		g.getCurLoc.remove(this);
 	}
 	
 	public void drop(GameThing g){
//		curLoc = g.location();
//		curLoc.put(this);
 	}
 	
 }
