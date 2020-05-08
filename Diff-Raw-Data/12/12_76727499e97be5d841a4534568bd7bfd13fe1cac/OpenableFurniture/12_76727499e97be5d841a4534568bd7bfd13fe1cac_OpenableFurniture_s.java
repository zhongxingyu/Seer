 package game.things;
 
 import java.util.*;
 
 import serialization.*;
 
 import util.Direction;
 import game.*;
 
 public class OpenableFurniture extends AbstractGameThing implements Togglable, Containable {
 	public static void makeSerializer(final SerializerUnion<GameThing> union, final GameWorld world){
 		union.addIdentifier(new SerializerUnion.Identifier<GameThing>(){
 			public String type(GameThing g){
 				return g instanceof OpenableFurniture? "OpenableFurniture" : null;
 			}
 		});
 		
 		union.addSerializer("OpenableFurniture", new Serializer<GameThing>(){
 			public Tree write(GameThing o){
 				OpenableFurniture in = (OpenableFurniture)o;
 				Tree out = new Tree();
 				out.add(new Tree.Entry("type", new Tree(in.renderer)));
 				out.add(new Tree.Entry("state", new Tree(Boolean.toString(in.open))));
 				out.add(new Tree.Entry("contents", Container.serializer(union.serializer(), world).write(in.contents)));
 				out.add(new Tree.Entry("doorcode", new Tree(in.doorcode)));
 				return out;
 			}
 
 			public GameThing read(Tree in) throws ParseException {
 				if(in.findNull("doorcode") == null){
 					return new OpenableFurniture(world, in.find("type").value(), Boolean.parseBoolean(in.find("state").value()), 
 							Container.serializer(union.serializer(), world).read(in.find("contents")),
 							"blank");
 				}
 				else{
 					return new OpenableFurniture(world, in.find("type").value(), Boolean.parseBoolean(in.find("state").value()), 
 							Container.serializer(union.serializer(), world).read(in.find("contents")),
 							in.find("doorcode").value());
 				}
 			}
 		});
 	}
 		
 	private final String renderer;
 	private boolean open;
 	private final Container contents;
 	private String doorcode;
 
 	
 	//For reading in (serializer to be completed)
 	public OpenableFurniture(GameWorld world, String name, boolean open, Container cont, String doorcode){
 		super(world);
 		renderer = name;
 		if(cont != null){
 			this.contents = cont;
 		}
 		else{
 			this.contents = new Container(world);
 		}
 		this.open = open;
 		if(doorcode == null)
 			this.doorcode = "blank";
 		else
 			this.doorcode = doorcode;
 		update();
 	}
 	
 	//For empty chest
 	public OpenableFurniture(GameWorld world, String name){
 		super(world);
 		renderer = name;
 		open = false;
 		contents = new Container(world);
 		doorcode = "blank";
 		update();
 	}
 
 
 	//need the default renderer for a chest, at the moment looks like a wall...
 	public OpenableFurniture(GameWorld world){
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
 	
 	public String doorcode(){
 		return doorcode;
 	}
 	
 	public void setDoorcode(String drcd){
 		this.doorcode = drcd;
 	}
 	
 	public List<String> interactions(){
 		ArrayList<String> interactions = new ArrayList<String>();
 		if(open){
 			interactions.add("view contents");
 			interactions.add("close");
 			}	
 		else{
 			interactions.add("open");
 			}
 		interactions.add("examine");
 		return interactions;
 			
 	}
 
 	public String name(){
 		return renderer;
 	}
 	
 	public Map<String,Container> getContainers(){
 		Map<String,Container> returnmap = new HashMap<String,Container>();
 		if(contents != null){
 			returnmap.put("contents", contents);
 		}
 		return returnmap;
 	}
 	
 	public void walkAndSetOpen(final boolean s, final Player p, final String say){
 		Location l = location();
 		final GameThing g = this;
 		if(l instanceof Level.Location){
 			p.moveTo((Level.Location)l, 1, new Runnable(){
 				public void run(){
 					open = s;
 					update();
 					world().emitSay(g, p, say);
 				}
 			});
 			p.face(l);
 		}
 	}
 	
 
 	
 	public void interact(String name, game.things.Player who){
 		if(name.equals("close"))
 			walkAndSetOpen(false, who, "You close the chest");
 		else if(name.equals("open")){
 			if(!doorcode.equals("blank")){
 				for(GameThing gt : who.inventory().contents()){
 					if(gt instanceof game.things.Key && ((Key)gt).doorcode().equals(this.doorcode)){
						walkAndSetOpen(true, who, "You unlock & open the chest");
 						return;
 					}
 				}
				walkAndSetOpen(false, who, "You cant open the "+doorcode+" chest, its locked");
 			}
 			else{
 				walkAndSetOpen(true, who, "You open the chest");
 			}
 		}
 		else if(name.equals("view contents")){
 			walkAndSetOpen(true,who, "You peer inside");
 			who.showContainer(contents, renderer);
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
 
 	public void toggle(){
 		open ^= true;
 		update();
 	}
 }
