 package game.things;
 
 import game.*;
 
 import java.util.*;
 import util.*;
 
 import serialization.*;
 
 public class Enemy extends Character {
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
 				return out;
 			}
 
 			public GameThing read(Tree in){
 				return new Enemy(world,
 					in.find("type").value(),
 					in.find("name").value(),
 					LocationS.s(world).read(in.find("start")),
 					Serializers.Serializer_Integer.read(in.find("wander")));
 			}
 		});
 	}
 
 	private final String name;
 	private final Location start;
 	private final int wanderdist;
 
 	public Enemy(GameWorld world, String t, String n, Location sl, int wd){
 		super(world, t);
 		name = n;
 		start = sl;
 		wanderdist = wd;
 		health(1000);
 		update();
 		new Runnable(){
 			public void run(){
 				Location l = start, ml = location();
				if(l instanceof Level.Location && ml instanceof Level.Location && (!busy() || ((Level.Location)l).dist((Level.Location)ml) > 2*wanderdist))
 					moveTo(((Level.Location)l).next(Direction.SOUTH, (int)(Math.random()*(wanderdist*2 - wanderdist))).next(Direction.EAST, (int)(Math.random()*(wanderdist*2 - wanderdist))));
 				world().schedule(this, 3000);
 			}
 		}.run();
 	}
 
 	public String name(){
 		return name;
 	}
 
 	public List<String> interactions(){
 		List<String> out = new LinkedList<String>(super.interactions());
 		out.add("follow");
 		out.add("attack");
 		return out;
 	}
 
 	public void interact(String name, Player who){
 		if(name.equals("follow"))
 			who.follow(this);
 		else if(name.equals("attack"))
 			who.attack(this);
 		else super.interact(name, who);
 	}
 
 	public void damage(int amt, Character from){
 		super.damage(amt, from);
 		attack(from);
 		if(health() <= 0){
 			LocationS.NOWHERE.put(this);
 			forget();
 		}
 	}
 }
