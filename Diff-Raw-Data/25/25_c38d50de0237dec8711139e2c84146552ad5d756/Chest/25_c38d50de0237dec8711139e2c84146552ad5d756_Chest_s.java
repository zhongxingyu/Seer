 package game.things;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import serialization.Serializer;
 
 import serialization.SerializerUnion;
 import serialization.Tree;
 import util.Direction;
 import game.*;
 
 public class Chest extends AbstractGameThing {
 	public static void makeSerializer(SerializerUnion<GameThing> union, final GameWorld world){
 		union.addIdentifier(new SerializerUnion.Identifier<GameThing>(){
 			public String type(GameThing g){
 				return g instanceof Chest? "chest" : null;
 			}
 		});
 		
 		union.addSerializer("chest", new Serializer<GameThing>(){
 			public Tree write(GameThing o){
 				Chest in = (Chest)o;
 				Tree out = new Tree();
 				out.add(new Tree.Entry("type", new Tree(in.renderer)));
 				return out;
 			}
 
 			public GameThing read(Tree in){
 				return new Chest(world, in.find("type").value());
 			}
 		});
 	}
 		
 	private final String renderer;
 	private boolean open;
	private final Container cont;
 
 	
 	//For reading in (serializer to be completed)
 	public Chest(GameWorld world, String name, boolean open, Container cont){
 		super(world);
 		renderer = name;
 		update();
		this.cont = cont;
 		this.open = open;
 	}
 	
 	//For empty chest
 	public Chest(GameWorld world, String name){
 		super(world);
 		renderer = name;
 		open = false;
 		update();
		cont = new Container(world);
 	}
 
 
 	//need the default renderer for a chest, at the moment looks like a wall...
 	public Chest(GameWorld world){
 		this(world, "chest_1");
 	}
 
 	public String renderer(){
 		return renderer + "_" + renderState();
 	}
 
 	private String renderState() {
 		if(open){
 			return "open";
 		}
 		else{
 			return "closed";
 		}
 	}
 	
 	public List<String> interactions(){
 		ArrayList<String> interactions = new ArrayList<String>();
 		if(open){
			interactions.add("close");
 			interactions.add("view contents");
 			}	
 		else{
 			interactions.add("open");
 			}
 		interactions.add("examine");
 		return interactions;
 			
 	}
 
 	public String name(){
 		return "Chest";
 	}
 	
 	public void interact(String name, game.things.Player who){
 		if(name.equals("open")){
 			open = true;
 		}
 		else if(name.equals("close")){
 			open = false;
 		}
 		else if(name.equals("view contents")){
			who.showContainer(cont, "Chest");
 		}
 		else super.interact(name, who);
 	}
 	
 	@Override
 	public boolean canWalkInto(Direction d, Character p) {
 		return false;
 	}
 	
 	@Override
 	public int renderLevel() {
 		return ui.isometric.abstractions.IsoSquare.FURNATURE;
 	}
 }
