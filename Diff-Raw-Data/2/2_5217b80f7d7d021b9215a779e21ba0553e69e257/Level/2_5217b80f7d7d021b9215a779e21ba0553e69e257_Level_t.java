 package game;
 
 import util.*;
 import java.util.*;
 
 import serialization.*;
 
 public class Level implements Iterable<GameThing> {
 	private final GameWorld world;
 	private final int level;
 	private final QuadTree<GameThing> map = new QuadTree<GameThing>();
 
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
 
 				public Location read(Tree in){
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
 
 		public Location direct(Direction d){
 			return new Location(level, position, d);
 		}
 
 		public Location rotate(Direction d){
 			return direct(direction.compose(d));
 		}
 
 		public Location next(Direction d){
 			return new Location(level, new Position(position.x() + d.dx(), position.y() + d.dy()), d);
 		}
 
 		public Location nextTo(final Location where, final game.things.Player who){
 			if(where.equals(this))
 				return this;
 			Find.Node<Location> cur = Find.dijkstra(this, where, new Find.Nextator<Location>(){
 				public Iterable<Find.Node<Location>> next(Find.Node<Location> n){
 					List<Find.Node<Location>> out = new LinkedList<Find.Node<Location>>();
 					if(n.cost() < 200)
 						for(Direction d : Direction.values()){
 							Location p = n.value().next(d);
 							if(p.canWalkInto(d, who))
 								out.add(n.next(p, Math.abs(n.value().position.x() - where.position.x() + (n.value().position.y() - where.position.y()))));
 						}
 					return out;
 				}
 			});
 			if(cur == null)
 				return null;
 			while(!cur.from().value().equals(this))
 				cur = cur.from();
 			return cur.value();
 		}
 
 		public Iterable<GameThing> contents(){
 			return level.portion(position, position);
 		}
 
 		public boolean canWalkInto(Direction d, game.things.Player w){
 			return canWalkInto(d, w, false);
 		}
 
 		private boolean canWalkInto(Direction d, game.things.Player w, boolean second){
 			for(GameThing gt : contents())
 				if(!gt.canWalkInto(d, w))
 					return false;
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
 		for(Position bit : gt.area().translated(p))
 			map.put(bit, gt);
 	}
 
 	private void put(Position p, GameThing gt){
 		put(p, Direction.NORTH, gt);
 	}
 
 	private void remove(GameThing gt, Position pos){
 		// TODO: rotate area!!!
 		for(Position bit : gt.area().translated(pos))
 			map.remove(bit, gt);
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
 		Set<GameThing> res = new HashSet<GameThing>();
 		for(Map.Entry<Position, GameThing> kv : map.portion(min, max))
 			res.add(kv.getValue());
 		return res;
 	}
 
 	public Iterable<GameThing> portion(Area a){
 		return portion(a.position(), new Position(a.position().x() + a.width() - 1, a.position().y() + a.height() - 1));
 	}
 
 	public Iterator<GameThing> iterator(){
 		Set<GameThing> res = new HashSet<GameThing>();
 		for(Map.Entry<Position, GameThing> kv : map)
 			res.add(kv.getValue());
 		return res.iterator();
 	}
 }
