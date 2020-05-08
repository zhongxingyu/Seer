 package game;
 
 import serialization.*;
 
 public class ClientMessage {
 	public static interface Action {
 		public void apply(GameWorld w, long from);
 		public Tree toTree(GameWorld w);
 		public String type();
 	}
 
 	public static class Interaction implements Action {
 		private final long gid;
 		private final String name;
 
 		public Interaction(long g, String n){
 			gid = g; name = n;
 		}
 
 		public void apply(GameWorld world, long from){
 			world.thingWithGID(gid).interact(name, (game.things.Player)world.thingWithGID(from));
 		}
 
 		public static Serializer<Interaction> serializer(final GameWorld w){
 			return new Serializer<Interaction>(){
 				public Tree write(Interaction in){
 					Tree out = new Tree();
 					out.add(new Tree.Entry("gid", Serializers.Serializer_Long.write(in.gid)));
 					out.add(new Tree.Entry("name", new Tree(in.name)));
 					return out;
 				}
 
				public Interaction read(Tree in){
 					return new Interaction(
 						Serializers.Serializer_Long.read(in.find("gid")),
 						in.find("name").value());
 				}
 			};
 		}
 
 		public Tree toTree(GameWorld w){
 			return serializer(w).write(this);
 		}
 
 		public String type(){
 			return "interaction";
 		}
 	}
 
 	private final Action action;
 	// the Player's gid
 	private final long from;
 
 	public ClientMessage(Action a, long f){
 		action = a;
 		from = f;
 	}
 
 	public void apply(GameWorld w){
 		action.apply(w, from);
 	}
 
 	public final static Serializer<ClientMessage> serializer(final GameWorld w, final long from){
 		return new Serializer<ClientMessage>(){
 			public Tree write(ClientMessage in){
 				Tree out = new Tree();
 				out.add(new Tree.Entry("type", new Tree(in.action.type())));
 				out.add(new Tree.Entry("action", in.action.toTree(w)));
 				return out;
 			}
 
			public ClientMessage read(Tree in){
 				Reader<? extends Action> as = null;
 				String type = in.find("type").value();
 				if(type.equals("interaction"))
 					as = Interaction.serializer(w);
 				return new ClientMessage(as.read(in.find("action")), from);
 			}
 		};
 	}
 }
