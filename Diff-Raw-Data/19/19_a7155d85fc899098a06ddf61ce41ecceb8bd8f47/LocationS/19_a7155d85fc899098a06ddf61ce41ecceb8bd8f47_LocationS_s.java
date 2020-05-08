 package game;
 
 import serialization.*;
 
 import java.util.*;
 
 // hrm ..
 public class LocationS {
 	public static Serializer<Location> s(final GameWorld w){
 		return new Serializer<Location>(){
 			public Tree write(Location in){
 				if(in instanceof Level.Location){
 					Tree out = new Tree();
 					out.add(new Tree.Entry("type", new Tree("level")));
 					out.add(new Tree.Entry("just", Level.Location.serializer(null).write((Level.Location)in)));
 					return out;
 				}
 				if(in instanceof Container){
 					Tree out = new Tree();
 					out.add(new Tree.Entry("type", new Tree("container")));
 					out.add(new Tree.Entry("just", Serializers.Serializer_Long.write(((Container)in).cid())));
 					return out;
 				}
 				if(in == NOWHERE){
 					Tree out = new Tree();
 					out.add(new Tree.Entry("type", new Tree("nowhere")));
 					return out;
 				}
 				throw new RuntimeException("wtf: " + in);
 			}
 			
 			// will think of making a general union type serializer helper thing later
			public Location read(Tree in){
 				String type = in.find("type").value();
 				if(type.equals("nowhere"))
 					return NOWHERE;
 				Tree cont = in.find("just");
 				if(type.equals("level"))
 					return Level.Location.serializer(w).read(cont);
 				if(type.equals("container"))
 					return w.containerWithCID(Serializers.Serializer_Long.read(cont));
 				throw new RuntimeException("wtf");
 			}
 		};
 	}
 
 	public final static Location NOWHERE = new Location(){
 		public void put(GameThing gt){
 			Location old = gt.location();
 			if(old != this)
 				old.remove(gt);
 			gt.location(this);
 		}
 
 		public Iterable<GameThing> contents(){
 			return Collections.emptyList();
 		}
 
 		public void remove(GameThing gt){}
 	};
 }
