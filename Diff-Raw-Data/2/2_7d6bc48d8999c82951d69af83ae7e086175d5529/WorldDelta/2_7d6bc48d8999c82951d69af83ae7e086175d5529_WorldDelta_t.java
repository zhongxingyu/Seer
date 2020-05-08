 package game;
 
 import serialization.*;
 
 public class WorldDelta {
 	public static interface Action {
 		public void apply(GameWorld w, WorldDelta wd);
 		public Tree toTree();
 		public String type();
 	}
 
 	public static class Put implements Action {
 		private final Tree in;
 
 		public Put(long g, Location l){
 			in = new Tree();
 			in.add(new Tree.Entry("gid", Serializers.Serializer_Long.write(g)));
 			in.add(new Tree.Entry("location", LocationS.s(null).write(l)));
 		}
 
 		public Put(Tree t){
 			in = t;
 		}
 
 		public void apply(GameWorld world, WorldDelta wd){
 			long gid = Serializers.Serializer_Long.read(in.find("gid"));
 			Location loc = LocationS.s(world).read(in.find("location"));
 			loc.put(world.thingWithGID(gid));
 		}
 
 		public static Serializer<Put> serializer(){
 			return new Serializer<Put>(){
 				public Tree write(/* Vladimir */ Put in){
 					return in.in;
 				}
 
 				public Put read(Tree in){
 					return new Put(in);
 				}
 			};
 		}
 
 		public Tree toTree(){
 			return in;
 		}
 
 		public String type(){
 			return "put";
 		}
 	}
 
 	// I don't like this duplication -_-
 	// Meh .. Java's repetitive enough anyway
 	// $ grep -r 'public' $(find . -name \*.java) | wc -l
 	// 369
 	public static class Introduce implements Action {
 		private final Tree in;
 
 		public Introduce(long g){
 			in = new Tree();
 			in.add(new Tree.Entry("gid", Serializers.Serializer_Long.write(g)));
 		}
 
 		public Introduce(Tree t){
 			in = t;
 		}
 
 		public void apply(GameWorld world, WorldDelta wd){
 			long gid = Serializers.Serializer_Long.read(in.find("gid"));
 			new DumbGameThing(world, gid);
 		}
 
 		public static Serializer<Introduce> serializer(){
 			return new Serializer<Introduce>(){
 				public Tree write(Introduce in){
 					return in.in;
 				}
 
 				public Introduce read(Tree in){
 					return new Introduce(in);
 				}
 			};
 		}
 
 		public Tree toTree(){
 			return in;
 		}
 
 		public String type(){
 			return "introduce";
 		}
 	}
 
 	public static class IntroduceContainer implements Action {
 		private final Tree in;
 
 		public IntroduceContainer(long g){
 			in = new Tree();
 			in.add(new Tree.Entry("cid", Serializers.Serializer_Long.write(g)));
 		}
 
 		public IntroduceContainer(Tree t){
 			in = t;
 		}
 
 		public void apply(GameWorld world, WorldDelta wd){
 			long gid = Serializers.Serializer_Long.read(in.find("cid"));
 			new Container(world, gid);
 		}
 
 		public static Serializer<IntroduceContainer> serializer(){
 			return new Serializer<IntroduceContainer>(){
 				public Tree write(IntroduceContainer in){
 					return in.in;
 				}
 
 				public IntroduceContainer read(Tree in){
 					return new IntroduceContainer(in);
 				}
 			};
 		}
 
 		public Tree toTree(){
 			return in;
 		}
 
 		public String type(){
 			return "introducecontainer";
 		}
 	}
 
 
 	public static class Forget implements Action {
 		private final Tree in;
 
 		public Forget(long g){
 			in = new Tree();
 			in.add(new Tree.Entry("gid", Serializers.Serializer_Long.write(g)));
 		}
 
 		public Forget(Tree t){
 			in = t;
 		}
 
 		public void apply(GameWorld world, WorldDelta wd){
 			long gid = Serializers.Serializer_Long.read(in.find("gid"));
 			new DumbGameThing(world, gid);
 		}
 
 		public static Serializer<Forget> serializer(){
 			return new Serializer<Forget>(){
 				public Tree write(Forget in /* what you saw */){
 					return in.in;
 				}
 
 				public Forget read(Tree in){
 					return new Forget(in);
 				}
 			};
 		}
 
 		public Tree toTree(){
 			return in;
 		}
 
 		public String type(){
 			return "forget";
 		}
 	}
 
 	public static class Update implements Action {
 		private final Tree in;
 
 		public Update(DumbGameThing d){
 			in = DumbGameThing.serializer(d.world()).write(d);
 		}
 
 		public Update(Tree t){
 			in = t;
 		}
 
 		public void apply(GameWorld world, WorldDelta wd){
 			DumbGameThing dgt = DumbGameThing.serializer(world).read(in);
 			// assumptions ..
 			((DumbGameThing)world.thingWithGID(dgt.gid())).update(dgt);
 		}
 
 		public static Serializer<Update> serializer(){
 			return new Serializer<Update>(){
 				public Tree write(Update in){
 					return in.in;
 				}
 
 				public Update read(Tree in){
 					return new Update(in);
 				}
 			};
 		}
 
 		public Tree toTree(){
 			return in;
 		}
 
 		public String type(){
 			return "update";
 		}
 	}
 
 	public static class Say implements Action {
 		private final Tree in;
 
 		public Say(long g, String w){
 			in = new Tree();
 			in.add(new Tree.Entry("gid", Serializers.Serializer_Long.write(g)));
 			in.add(new Tree.Entry("what", new Tree(w)));
 		}
 
 		public Say(Tree t){
 			in = t;
 		}
 
 		public void apply(GameWorld world, WorldDelta wd){
 			long gid = Serializers.Serializer_Long.read(in.find("gid"));
 			String what = in.find("what").value();
 			world.emitSay(world.thingWithGID(gid), what);
 		}
 
 		public long who(){
 			return Serializers.Serializer_Long.read(in.find("gid"));
 		}
 
 		public String what(){
 			return in.find("what").value();
 		}
 
 		public final static Serializer<Say> serializer(){
 			return new Serializer<Say>(){
 				public Tree write(Say in){
 					return in.in;
 				}
 
 				public Say read(Tree in){
 					return new Say(in);
 				}
 			};
 		}
 
 		public Tree toTree(){
 			return in;
 		}
 
 		public String type(){
 			return "say";
 		}
 	}
 
 	public static class Animate implements Action {
 		private final Tree in;
 
 		public Animate(long g, String w){
 			in = new Tree();
 			in.add(new Tree.Entry("gid", Serializers.Serializer_Long.write(g)));
 			in.add(new Tree.Entry("what", new Tree(w)));
 		}
 
 		public Animate(Tree t){
 			in = t;
 		}
 
 		public void apply(GameWorld world, WorldDelta wd){
 			long gid = Serializers.Serializer_Long.read(in.find("gid"));
 			String what = in.find("what").value();
 			world.emitAnimate(world.thingWithGID(gid), what);
 		}
 
 		public GameThing which(GameWorld world){
 			return world.thingWithGID(Serializers.Serializer_Long.read(in.find("gid")));
 		}
 
 		public String what(){
 			return in.find("what").value();
 		}
 
 		public final static Serializer<Animate> serializer(){
 			return new Serializer<Animate>(){
 				public Tree write(Animate in){
 					return in.in;
 				}
 
 				public Animate read(Tree in){
 					return new Animate(in);
 				}
 			};
 		}
 
 		public Tree toTree(){
 			return in;
 		}
 
 		public String type(){
 			return "animate";
 		}
 	}
 
 	public static class ShowContainer implements Action {
 		private final Tree in;
 
 		public ShowContainer(long g, String w){
 			in = new Tree();
 			in.add(new Tree.Entry("cid", Serializers.Serializer_Long.write(g)));
 			in.add(new Tree.Entry("what", new Tree(w)));
 		}
 
 		public ShowContainer(Tree t){
 			in = t;
 		}
 
 		public void apply(GameWorld world, WorldDelta wd){
 			long cid = Serializers.Serializer_Long.read(in.find("cid"));
 			String what = in.find("what").value();
 			world.emitShowContainer(world.containerWithCID(cid), what, world.thingWithGID(wd.to));
 		}
 
		public Container which(GameWorld world){
 			return world.containerWithCID(Serializers.Serializer_Long.read(in.find("cid")));
 		}
 
 		public String what(){
 			return in.find("what").value();
 		}
 
 		public final static Serializer<ShowContainer> serializer(){
 			return new Serializer<ShowContainer>(){
 				public Tree write(ShowContainer in){
 					return in.in;
 				}
 
 				public ShowContainer read(Tree in){
 					return new ShowContainer(in);
 				}
 			};
 		}
 
 		public Tree toTree(){
 			return in;
 		}
 
 		public String type(){
 			return "showcontainer";
 		}
 	}
 
 	public static class EmitSound implements Action {
 		private final Tree in;
 
 		public EmitSound(long g, String w){
 			in = new Tree();
 			in.add(new Tree.Entry("gid", Serializers.Serializer_Long.write(g)));
 			in.add(new Tree.Entry("what", new Tree(w)));
 		}
 
 		public EmitSound(Tree t){
 			in = t;
 		}
 
 		public void apply(GameWorld world, WorldDelta wd){
 			long gid = Serializers.Serializer_Long.read(in.find("gid"));
 			String what = in.find("what").value();
 			world.emitEmitSound(world.thingWithGID(gid), what);
 		}
 
 		public GameThing which(GameWorld world){
 			return world.thingWithGID(Serializers.Serializer_Long.read(in.find("gid")));
 		}
 
 		public String what(){
 			return in.find("what").value();
 		}
 
 		public final static Serializer<EmitSound> serializer(){
 			return new Serializer<EmitSound>(){
 				public Tree write(EmitSound in){
 					return in.in;
 				}
 
 				public EmitSound read(Tree in){
 					return new EmitSound(in);
 				}
 			};
 		}
 
 		public Tree toTree(){
 			return in;
 		}
 
 		public String type(){
 			return "emitsound";
 		}
 	}
 
 	private final Action action;
 	private final long to;
 
 	public Action action(){
 		return action;
 	}
 
 	public long to(){
 		return to;
 	}
 
 	public WorldDelta(Action a, long target){
 		action = a;
 		to = target;
 	}
 
 	public void apply(GameWorld w){
 		action.apply(w, this);
 	}
 
 	public final static Serializer<WorldDelta> SERIALIZER = new Serializer<WorldDelta>(){
 		public Tree write(WorldDelta in){
 			Tree out = new Tree();
 			out.add(new Tree.Entry("type", new Tree(in.action.type())));
 			out.add(new Tree.Entry("to", Serializers.Serializer_Long.write(in.to)));
 			out.add(new Tree.Entry("action", in.action.toTree()));
 			return out;
 		}
 
 		public WorldDelta read(Tree in){
 			Reader<? extends Action> as = null;
 			String type = in.find("type").value();
 			long target = Serializers.Serializer_Long.read(in.find("to"));
 			if(type.equals("put"))
 				as = Put.serializer();
 			else if(type.equals("introduce"))
 				as = Introduce.serializer();
 			else if(type.equals("forget"))
 				as = Forget.serializer();
 			else if(type.equals("update"))
 				as = Update.serializer();
 			else if(type.equals("say"))
 				as = Say.serializer();
 			else if(type.equals("animate"))
 				as = Animate.serializer();
 			else if(type.equals("introducecontainer"))
 				as = IntroduceContainer.serializer();
 			else if(type.equals("showcontainer"))
 				as = ShowContainer.serializer();
 			else if(type.equals("emitsound"))
 				as = EmitSound.serializer();
 			return new WorldDelta(as.read(in.find("action")), target);
 		}
 	};
 }
