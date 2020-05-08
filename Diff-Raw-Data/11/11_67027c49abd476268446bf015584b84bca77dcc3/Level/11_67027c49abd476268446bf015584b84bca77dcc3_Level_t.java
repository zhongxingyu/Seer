 package game;
 
 import util.*;
 import java.util.*;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import serialization.*;
 
 public class Level implements Iterable<GameThing> { // TODO: try/finally for locks
 	private final GameWorld world;
 	private final int level;
 	private QuadTree<GameThing> map = new QuadTree<GameThing>();
 //	private final DegenerateTrie<GameThing> map = new DegenerateTrie<GameThing>();
 	
 	private final ReentrantReadWriteLock mapLock = new ReentrantReadWriteLock();
 
 /*
 	public void clear(){
 		map = new QuadTree<GameThing>();
 	}
 	*/
 
 	public static class Location implements game.Location {
 		private final Level level;
 		private final Position position;
 		private final Direction direction;
 
 		public static Serializer<Location> serializer(final GameWorld w){
 			return new Serializer<Location>(){
 				public Tree write(Location in){
 					Tree out = new Tree();
 					out.add(new Tree.Entry("level", Serializers.Serializer_Integer.write(in.level.level)));
 					out.add(new Tree.Entry("position", Position.SERIALIZER.write(in.position)));
 					out.add(new Tree.Entry("direction", Direction.SERIALIZER.write(in.direction)));
 					return out;
 				}
 
 				public Location read(Tree in) throws ParseException {
 					return new Location(
 						w.level(Serializers.Serializer_Integer.read(in.find("level"))),
 						Position.SERIALIZER.read(in.find("position")),
 						Direction.SERIALIZER.read(in.find("direction")));
 				}
 			};
 		}
 
 		public Location(Level l, Position p, Direction d){
 			level = l; position = p; direction = d;
 		}
 
 		public Position position(){
 			return position;
 		}
 
 		public Direction direction(){
 			return direction;
 		}
 
 		public Level level(){
 			return level;
 		}
 
 		public int z(){
 			return level.level;
 		}
 
 		public Location direct(Direction d){
 			return new Location(level, position, d);
 		}
 
 		public Location rotate(Direction d){
 			return direct(direction.compose(d));
 		}
 
 		public Location next(Direction d, int l){
 			return new Location(level, new Position(position.x() + d.dx()*l, position.y() + d.dy()*l), l < 0? d.compose(Direction.SOUTH) : d);
 		}
 
 		public Location next(Direction d){
 			return next(d, 1);
 		}
 
		public Location next(){
			return next(direction);
		}

 		public Location above(int amt){
 			return new Location(level.world.level(level.level + amt), position, direction);
 		}
 
 		public Location above(){
 			return above(1);
 		}
 
 		public Location below(int amt){
 			return above(-amt);
 		}
 
 		public Location below(){
 			return below(1);
 		}
 
 		public int dist(Location l){
 			return Math.abs(l.position().x() - position.x()) + Math.abs(l.position().y() - position.y());
 		}
 
 		public Location nextTo(Location where, game.things.Character who, int dest){
 			return nextTo(where, who, dest, null);
 		}
 
 		public Location nextTo(final Location where, final game.things.Character who, final int dist, int[] len){
 //			System.out.println("nextTo(" + where.position + ", ..): " + where.contents());
 		/*
 			try{
 				java.io.FileWriter fw = new java.io.FileWriter("dbg.g");
 				where.level.map.toDot(fw);
 				fw.close();
 			}catch(java.io.IOException e){}
 			if(5 == 5)
 				throw new RuntimeException("halting");
 				*/
 			if(where.equals(this))
 				return this;
 			Find.Nextator<Location> nextator;
 			Find.Node<Location> cur = Find.dijkstra(this, nextator = new Find.Nextator<Location>(){
 				private int x = 0;
 
 				public Iterable<Find.Node<Location>> next(Find.Node<Location> n){
 					List<Find.Node<Location>> out = new LinkedList<Find.Node<Location>>();
 					if(x++ < 10000)
 						for(Direction d : Direction.values()){
 							Location p = n.value().next(d);
 							if(p.canWalkInto(d, who)){
 //								System.out.println(p.position + ".canWalkInto(..): " + p.contents());
 //								System.out.println("where.contents() = " + where.contents() + ", (where.level == p.level): " + (where.level == p.level));
 								out.add(n.next(p, 1 + Math.abs(n.value().position.x() - where.position.x()) + Math.abs(n.value().position.y() - where.position.y())));
 							}
 						}
 					return out;
 				}
 
 				public boolean end(Find.Node<Location> p){
 					return p.value().level.equals(level) && where.dist(p.value()) <= dist;
 				}
 			});
 			if(cur == null)
 				return null;
 			if(len != null)
 				len[0] = cur.length();
 			// eh .. someone's going to be put back in teh same place. Meh.
 			if(cur.value().equals(this))
 				return this;
 			while(!cur.from().value().equals(this))
 				cur = cur.from();
 				/*
 			if(!where.equals(cur.value())){
 				Location closest = null;
 				for(Direction d : Direction.values()){
 					Location p = cur.value().next(d);
 					if(closest == null || where.dist(p) < where.dist(closest))
 						closest = p;
 				}
 				return cur.value().direct(closest.direction());
 			}
 			*/
 			return cur.value();
 		}
 
 		public Iterable<GameThing> contents(){
 			return level.portion(position, position);
 		}
 
 		public boolean canWalkInto(Direction d, game.things.Character w){
 			return canWalkInto(d, w, false);
 		}
 
 		private boolean canWalkInto(Direction d, game.things.Character w, boolean second){
 			for(GameThing gt : contents())
 				if(!gt.canWalkInto(d, w))
 					return false;
 //				else
 //					System.out.println(gt + ".canWalkInto(..) = true; " + position);
 			return second || next(d.compose(Direction.SOUTH)).canWalkInto(d.compose(Direction.SOUTH), w, true);
 		}
 
 		public void put(GameThing gt){
 			gt.location().remove(gt);
 			level.put(position, direction, gt);
 			gt.location(this);
 		}
 
 		public void remove(GameThing gt){
 			level.remove(gt, position);
 		}
 
 		// hrm .. this equality disregards Direction
 		public int hashCode(){
 			return level.hashCode() ^ position.hashCode() ^ -882774422;
 		}
 
 		public boolean equals(Object o){
 			return this == o || o instanceof Location && level.equals(((Location)o).level) && position.equals(((Location)o).position);
 		}
 	}
 
 	public Level(GameWorld w, int l){
 		world = w; level = l;
 	}
 
 	public Location location(Position p, Direction d){
 		return new Location(this, p, d);
 	}
 
 	private void put(Position p, Direction d, GameThing gt){
 		// TODO: rotate area!!!
 		mapLock.writeLock().lock();
 		for(Position bit : gt.area().translated(p))
 			map.put(bit, gt);
 		mapLock.writeLock().unlock();
 	}
 
 	private void put(Position p, GameThing gt){
 		put(p, Direction.NORTH, gt);
 	}
 
 	private void remove(GameThing gt, Position pos){
 		// TODO: rotate area!!!
 		mapLock.writeLock().lock();
 		for(Position bit : gt.area().translated(pos))
 			map.remove(bit, gt);
 		mapLock.writeLock().unlock();
 	}
 
 /*
 	// convenience, maybe
 	private void move(Position to, GameThing gt){
 		gt.location().remove(gt);
 		put(to, gt);
 	}
 	*/
 /*
 	private void rotate(Direction to, GameThing gt){
 		Location l = gt.location();
 		if(l instanceof LevelLocation){
 			direct(((LevelLocation)l).direction().compose(to), gt);
 		}else
 			throw new RuntimeException("wtf");
 	}
 
 	private void direct(Direction to, GameThing gt){
 		Location l = gt.location();
 		if(l instanceof LevelLocation){
 			put(((LevelLocation)l).position(), to, gt);
 			gt.location(new LevelLocation(world, level, ((LevelLocation)l).position(), to));
 		}else
 			throw new RuntimeException("wtf");
 	}
 	*/
 
 	public boolean contains(GameThing gt){
 		game.Location l = gt.location();
 		if(l instanceof Location && ((Location)l).level() == this)
 			for(GameThing g : portion(((Location)l).position(), ((Location)l).position()))
 				if(gt == g)
 					return true;
 		return false;
 	}
 
 	public Iterable<GameThing> portion(Position min, Position max){
 		List<GameThing> res = new LinkedList<GameThing>();
 		mapLock.readLock().lock();
 		for(Map.Entry<Position, GameThing> kv : map.portion(min, max))
 			res.add(kv.getValue());
 		mapLock.readLock().unlock();
 		return res;
 	}
 
 	public Iterable<GameThing> portion(Area a){
 		return portion(a.position(), new Position(a.position().x() + a.width() - 1, a.position().y() + a.height() - 1));
 	}
 
 	public Iterator<GameThing> iterator(){
 		Set<GameThing> res = new HashSet<GameThing>();
 		mapLock.readLock().lock();
 		for(Map.Entry<Position, GameThing> kv : map)
 			res.add(kv.getValue());
 		mapLock.readLock().unlock();
 		return res.iterator();
 	}
 
 	public ReadWriteLock thingLock() {
 		return mapLock;
 	}
 
 	public boolean equals(Object o){
 		if(o instanceof Level){
 			Level l = (Level)o;
 			return l.level == level && l.world == world;
 		}
 		return false;
 	}
 
 	public int z(){
 		return level;
 	}
 
 	public int hashCode(){
 		return world.hashCode() ^ level ^ 0xabcde123;
 	}
 }
