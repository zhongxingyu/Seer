 package game.things;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import util.Direction;
 
 import serialization.*;
 
 import game.*;
 
 public class Door extends AbstractGameThing implements Togglable {
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
				out.add(new Tree.Entry("doorcode", new Tree(in.openRenderer)));
 				return out;
 			}
 
 			public GameThing read(Tree in) throws ParseException {
 				if(in.findNull("doorcode") == null){
 					return new Door(world, in.find("close").value(), in.find("open").value(),
 							Serializers.Serializer_Boolean.read(in.find("state")),
 							"blank");
 				}
 				else{
 					return new Door(world, in.find("close").value(), in.find("open").value(),
 							Serializers.Serializer_Boolean.read(in.find("state")),
 							in.findNull("doorcode").value());
 				}
 			}
 		});
 	}
 	private final String openRenderer;
 	private final String closedRenderer;
 	private String doorcode;
 	private boolean open;
 
 	public Door(GameWorld world, String closedRenderer, String openRenderer, boolean open, String drcd){
 		super(world);
 		this.openRenderer = openRenderer;
 		this.closedRenderer = closedRenderer;
 		this.open = open;
 		this.doorcode = drcd;
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
 
 	
 	public void walkAndSetOpen(final boolean s, final Player p, final String say){
 		Location l = location();
 		final GameThing g = this;
 		if(l instanceof Level.Location)
 			p.moveTo((Level.Location)l, 1, new Runnable(){
 				public void run(){
 					open = s;
 					update();
 					world().emitSay(g, p, say);
 				}
 			});
 			p.face(l);
 	}
 	
 	@Override
 	public void interact(String inter, Player who) {
 		if(inter.equals("close"))
 			walkAndSetOpen(false, who, "You close the door");
 		else if(inter.equals("open")){
 			if(!doorcode.equals("blank")){
 				for(GameThing gt : who.inventory().contents()){
 					if(gt instanceof game.things.Key && ((Key)gt).doorcode().equals(this.doorcode)){
 						walkAndSetOpen(true, who, "You unlock and open the door");
 						return;
 					}
 				}
 				walkAndSetOpen(false, who, "You can't open the "+doorcode+" door; it's locked");
 			}
 			else{
 				walkAndSetOpen(true, who, "You unlock the door");
 			}
 		}
 		else super.interact(inter, who);
 	}
 
 	@Override
 	public String name(){
 		return "Door";
 	}
 	
 	@Override
 	public boolean canWalkInto(Direction d, Character who){
 		return open;
 	}
 	
 	@Override
 	public int renderLevel() {
 		return ui.isometric.abstractions.IsoSquare.WALL;
 	}
 	
 	public String doorcode(){
 		return doorcode;
 	}
 	
 	public void setDoorcode(String drcd){
 		this.doorcode = drcd;
 	}
 
 	public void toggle(){
 		open ^= true;
 		update();
 	}
 }
