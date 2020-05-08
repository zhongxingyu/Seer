 package game.things;
 
 import game.*;
 
 import java.util.*;
 
 import util.*;
 
 import serialization.*;
 
 /**
  * 
  * @author wheelemaxw and zerzoumax
  *Enemy is a type of character, which has the option of being aggressive
  *or not. Either way wanders around the mape, and if aggressive attacks anything
  *within the defined radius
  *
  *Has an inventory for potentially dropped items
  *
  */
 public class Enemy extends Character implements Namable, Containable {
 	
 	/**
 	 * Custom serializers for Enemy
 	 * @param union
 	 * @param world
 	 */
 	public static void makeSerializer(final SerializerUnion<GameThing> union, final GameWorld world){
 		union.addIdentifier(new SerializerUnion.Identifier<GameThing>(){
 			public String type(GameThing g){
 				return g instanceof Enemy? "enemy" : null;
 			}
 		});
 
 		union.addSerializer("enemy", new Serializer<GameThing>(){
 			public Tree write(GameThing o){
 				Enemy in = (Enemy)o;
 				Tree out = new Tree();
 				out.add(new Tree.Entry("type", new Tree(in.type())));
 				out.add(new Tree.Entry("name", new Tree(in.name)));
 				out.add(new Tree.Entry("start", LocationS.s(world).write(in.start)));
 				out.add(new Tree.Entry("wander", Serializers.Serializer_Integer.write(in.wanderdist)));
 				out.add(new Tree.Entry("inventory", Container.serializer(union.serializer(), world).write(in.inventory)));
 				out.add(new Tree.Entry("aggressive", new Tree(Boolean.toString(in.aggressive))));
 				out.add(new Tree.Entry("radius", Serializers.Serializer_Integer.write(in.radius)));
 				return out;
 			}
 
 			public GameThing read(Tree in) throws ParseException {
 				return new Enemy(world,
 					in.find("type").value(),
 					in.find("name").value(),
 					LocationS.s(world).read(in.find("start")),
 					Serializers.Serializer_Integer.read(in.find("wander")),
 					Container.serializer(union.serializer(), world).read(in.find("inventory")),
					Boolean.parseBoolean(in.find("aggresive").value()),
 					Serializers.Serializer_Integer.read(in.find("radius")));
 			}
 		});
 	}
 
 	private String name;
 	private final Location start;
 	private int wanderdist;
 	private int radius;
 	private boolean aggressive;
 	private final Container inventory;
 
 
 	public Enemy(GameWorld world, String t, String n, Location sl, int wd, Container inv, boolean agr,int rad){
 		super(world, t);
 		name = n;
 		aggressive = agr;
 		radius = rad;
 		start = sl;
 		wanderdist = wd;
 		if(inv == null)
 			inventory = new Container(world);
 		else
 			inventory = inv;
 		health(1000);
 		setStats(12,12,12,12);
 		update();
 		new Runnable(){
 			public void run(){
 				Location l = start, ml = location();
 				Player p = null;
 
 				if(aggressive && ml instanceof Level.Location){
 					Level.Location ll = (Level.Location)ml;
 					Iterable<GameThing> box = ll.level().portion(new Position(ll.position().x()-radius,ll.position().y()-radius),new Position(ll.position().x()+radius,ll.position().y()+radius));
 					for(GameThing g : box){
 						if(g instanceof Player)
 							p = (Player)g;
 					}
 					if(p!=null)
 						attack(p);
 					else if(l instanceof Level.Location && ml instanceof Level.Location && (!busy() || ((Level.Location)l).dist((Level.Location)ml) > 2*wanderdist))
 						moveTo(((Level.Location)l).next(Direction.SOUTH, (int)(Math.random()*wanderdist*2 - wanderdist)).next(Direction.EAST, (int)(Math.random()*wanderdist*2 - wanderdist)));
 				}
 				else{
 					if(l instanceof Level.Location && ml instanceof Level.Location && (!busy() || ((Level.Location)l).dist((Level.Location)ml) > 2*wanderdist))
 						moveTo(((Level.Location)l).next(Direction.SOUTH, (int)(Math.random()*wanderdist*2 - wanderdist)).next(Direction.EAST, (int)(Math.random()*wanderdist*2 - wanderdist)));
 				}
 				if(!forgotten())
 					world().schedule(this, 3000);
 			}
 		}.run();
 	}
 
 	/**
 	 * @return name of Enemy
 	 */
 	public String name(){
 		return name;
 	}
 
 	/**
 	 * @param s - New name
 	 */
 	public String name(String s){
 		return name = s;
 	}
 
 	/**
 	 * @return A list of possible interactions, adding on to the superclass's (in ths case an empty list.)
 	 */
 	public List<String> interactions(){
 		List<String> out = new LinkedList<String>(super.interactions());
 		out.add("follow");
 		out.add("attack");
 		out.add("examine");
 		return out;
 	}
 
 	/**
 	 * Calls the appropriate interaction method in the Player
 	 */
 	public void interact(String name, Player who){
 		if(name.equals("follow"))
 			who.follow(this);
 		else if(name.equals("attack"))
 			who.attack(this);
 		else super.interact(name, who);
 	}
 	
 	/**
 	 * Getter method for the inventory
 	 * @return The Enemy's inventory
 	 */
 	public Container inventory(){
 		return inventory;
 	}
 
 	/**
 	 * Calls superclass's damage method (to reduce health etc, and then
 	 * emits a notifcation to the attacking Player about damage done.
 	 * Enemys automatically attack any Players that attack them
 	 * 
 	 * If the health reaches 0, a corpse is created, and the Enemy is forgotten
 	 * from the world
 	 */
 	public void damage(int amt, Character from){
 		if(!dying()){
 			super.damage(amt, from);
 			world().emitSay(this, from, from.name() + " hurts " + name() + " and their health is now " + health());
 			if(!attacking())
 				attack(from);
 		}
 		if(health() <= 0 && dying()){
 			final Enemy g = this;
 			world().schedule(new Runnable(){
 				public void run(){
 				final game.things.Corpse cp = new Corpse(g.world(),"corpse_1", g.inventory());
 				cp.location(g.location());
 				LocationS.NOWHERE.put(g);
 				world().forget(g);
 				}
 			}, 1500);
 		}
 	}
 	
 	/**
 	 * 
 	 * @return Wander distance
 	 */
 	public int walkdistance() {
 		return wanderdist;
 	}
 	
 	/**
 	 * 
 	 * @param d - The new wander distance
 	 * @return The new wander distance
 	 */
 	public int walkdistance(int d) {
 		return wanderdist = d;
 	}
 	
 	/**
 	 * Getter
 	 * @return True if aggressive
 	 */
 	public boolean aggressive(){
 		return aggressive;
 	}
 	
 	/**
 	 * Setter
 	 * @param b - new aggressive boolean
 	 */
 	public void aggressive(boolean b){
 		aggressive = b;
 	}
 	
 	/**
 	 * Getter
 	 * @return the aggressive radius
 	 */
 	public int radius(){
 		return radius;
 	}
 	
 	/**
 	 * Setter
 	 * @param i - new aggressive radius
 	 */
 	public void radius(int i){
 		radius = i;
 	}
 
 	@Override
 	public Map<String, Container> getContainers() {
 		Map<String,Container> returnmap = new HashMap<String,Container>();
 		if(inventory != null){
 			returnmap.put("loot", inventory);
 		}
 		return returnmap;
 	}
 }
