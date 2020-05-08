 package game.things;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import util.Direction;
 
 import serialization.*;
 
 import game.*;
 
 public class Door extends AbstractGameThing {
 	public static void makeSerializer(SerializerUnion<GameThing> union, final GameWorld world){
 		union.addIdentifier(new SerializerUnion.Identifier<GameThing>(){
 			public String type(GameThing g){
 				return g instanceof Door? "door" : null;
 			}
 		});
 
 		union.addSerializer("door", new Serializer<GameThing>(){
 			public Tree write(GameThing o){
 				Door in = (Door)o;
 				Tree out = new Tree();
 				out.add(new Tree.Entry("open", new Tree(in.openRenderer)));
 				out.add(new Tree.Entry("close", new Tree(in.closedRenderer)));
 				out.add(new Tree.Entry("state", Serializers.Serializer_Boolean.write(in.open)));
 				return out;
 			}
 
 			public GameThing read(Tree in){
				return new Door(world, in.find("open").value(), in.find("close").value(), Serializers.Serializer_Boolean.read(in.find("state")));
 			}
 		});
 	}
 	private final String openRenderer;
 	private final String closedRenderer;
 	private boolean open;
 
 	public Door(GameWorld world, String closedRenderer, String openRenderer, boolean open){
 		super(world);
 		this.openRenderer = openRenderer;
 		this.closedRenderer = closedRenderer;
 		this.open = open;
 		update();
 	}
 
 	@Override
 	public String renderer(){
 		return open?openRenderer:closedRenderer;
 	}
 	
 	@Override
 	public List<String> interactions(){
 		return new ArrayList<String>(){private static final long serialVersionUID = 1L;{this.add(defaultInteraction());}};
 	}
 	
 	@Override
 	public String defaultInteraction() {
 		if(open) {
 			return "close";
 		}
 		else {
 			return "open";
 		}
 	}
 
 	public void walkAndSet(final boolean s, Player p){
 		Location l = location();
 		if(l instanceof Level.Location)
 			p.moveTo((Level.Location)l, 1, new Runnable(){
 				public void run(){
 					open = s;
 					update();
 				}
 			});
 	}
 	
 	@Override
 	public void interact(String inter, Player who) {
 		if(inter.equals("close"))
 			walkAndSet(false, who);
 		else if(inter.equals("open"))
 			walkAndSet(true, who);
 	}
 
 	@Override
 	public String name(){
 		return "Door";
 	}
 	
 	@Override
 	public boolean canWalkInto(Direction d, game.things.Player who){
 		return open;
 	}
 }
