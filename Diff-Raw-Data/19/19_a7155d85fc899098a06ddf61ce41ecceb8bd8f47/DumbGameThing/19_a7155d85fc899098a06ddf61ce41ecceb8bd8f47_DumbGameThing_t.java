 package game;
 
 import java.io.Serializable;
 import java.util.*;
 
 import serialization.*;
 
 public class DumbGameThing extends AbstractGameThing.AbstractDumbGameThing {
 	private List<String> interactions;
 	private String defaultInteraction, renderer, name;
 	private Map<String, String> info;
 	private int renderLevel;
 
 	public DumbGameThing(long g, List<String> i, String d, String r, String n, int l, Map<String, String> inf){
 		this(null, g, i, d, r, n, l, inf);
 	}
 
 	public DumbGameThing(GameWorld w, long g, List<String> i, String d, String r, String n, int l, Map<String, String> inf){
 		super(w, g);
 		interactions = i;
 		defaultInteraction = d;
 		renderer = r;
 		name = n;
 		renderLevel = l;
 		info = inf;
 		if(w != null)
 			w.introduce(this, gid());
 	}
 	
 	@Override
 	public List<String> interactions() { // Melby fixed
 		return interactions != null? interactions : super.interactions();
 	}
 	
 	@Override
 	public String defaultInteraction() { // Melby fixed
 		return defaultInteraction != null? defaultInteraction : super.defaultInteraction();
 	}
 
 	@Override
 	public String name(){
 		return name != null? name : "null";
 	}
 
 	public DumbGameThing(GameWorld w, long g){
 		this(w, g, null, null, null, null, 0, null);
 	}
 
 	public DumbGameThing(GameThing gt){
 		this(gt.gid(), gt.interactions(), gt.defaultInteraction(), gt.renderer(), gt.name(), gt.renderLevel(), gt.info());
 	}
 
 	public void update(DumbGameThing o){
 		if(o.interactions != null)
 			interactions = o.interactions;
 		if(o.defaultInteraction != null)
 			defaultInteraction = o.defaultInteraction;
 		if(o.renderer != null)
 			renderer = o.renderer;
 		if(o.name != null)
 			name = o.name;
 		if(o.renderLevel != -1)
 			renderLevel = o.renderLevel;
 		if(o.info != null)
 			info = o.info;
 	}
 
 	public String renderer() {
 		return renderer != null? renderer : "null";
 	}
 
 	public Map<String, String> info(){
 		return info;
 	}
 
 	public static Serializer<DumbGameThing> serializer(final GameWorld w){
 		return new Serializer<DumbGameThing>(){
 			private final Serializer<List<String>> interS = new Serializers.Nullable<List<String>>(new Serializers.List<String>(Serializers.Serializer_String));
 			private final Serializer<String> nullStringS = new Serializers.Nullable<String>(Serializers.Serializer_String);
 			// private static final Serializer<Map<String, Object>> = ???
 
 			public Tree write(DumbGameThing in){
 				Tree out = new Tree();
 				out.add(new Tree.Entry("gid", Serializers.Serializer_Long.write(in.gid())));
 				out.add(new Tree.Entry("interactions", interS.write(in.interactions)));
 				out.add(new Tree.Entry("defaultinteraction", nullStringS.write(in.defaultInteraction)));
 				out.add(new Tree.Entry("renderer", nullStringS.write(in.renderer)));
 				out.add(new Tree.Entry("name", nullStringS.write(in.name)));
 				out.add(new Tree.Entry("renderLevel", Serializers.Serializer_Integer.write(in.renderLevel())));
 				out.add(new Tree.Entry("info", Serializers.map(Serializers.Serializer_String, Serializers.Serializer_String).write(in.info())));
 				return out;
 			}
 
			public DumbGameThing read(Tree in) throws ParseException {				
 				return new DumbGameThing(
 					Serializers.Serializer_Long.read(in.find("gid")),
 					interS.read(in.find("interactions")),
 					nullStringS.read(in.find("defaultinteraction")),
 					nullStringS.read(in.find("renderer")),
 					nullStringS.read(in.find("name")),
 					Serializers.Serializer_Integer.read(in.find("renderLevel")),
 					Serializers.map(Serializers.Serializer_String, Serializers.Serializer_String).read(in.find("info")));
 			}
 		};
 	}
 
 	@Override
 	public int renderLevel() {
 		return renderLevel;
 	}
 }
