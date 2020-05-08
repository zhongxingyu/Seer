 package game.things;
 
 import game.*;
 
 import java.util.*;
 
 import serialization.*;
 
 public class EquipmentGameThing extends PickupGameThing {
 	public static void makeSerializer(SerializerUnion<GameThing> union, final GameWorld world){
 		union.addIdentifier(new SerializerUnion.Identifier<GameThing>(){
 			public String type(GameThing g){
 				return g instanceof EquipmentGameThing? "equipment" : null;
 			}
 		});
 
 		union.addSerializer("equipment", new Serializer<GameThing>(){
 			public Tree write(GameThing o){
 				EquipmentGameThing in = (EquipmentGameThing)o;
 				Tree out = new Tree();
 				out.add(new Tree.Entry("attack", Serializers.Serializer_Integer.write(in.attack)));
 				out.add(new Tree.Entry("strength", Serializers.Serializer_Integer.write(in.strength)));
 				out.add(new Tree.Entry("defence", Serializers.Serializer_Integer.write(in.defence)));
 				out.add(new Tree.Entry("delay", Serializers.Serializer_Integer.write(in.delay)));
 				out.add(new Tree.Entry("slot", new Tree(in.slottype.toString())));
 				out.add(new Tree.Entry("name", new Tree(in.name)));
 				out.add(new Tree.Entry("renderer", new Tree(in.renderer)));
 				return out;
 			}
 
 			public GameThing read(Tree in){
 				return new EquipmentGameThing(world,
 					Serializers.Serializer_Integer.read(in.find("attack")),
 					Serializers.Serializer_Integer.read(in.find("strength")),
 					Serializers.Serializer_Integer.read(in.find("defence")),
 					Serializers.Serializer_Integer.read(in.find("delay")),
 					Slot.valueOf(in.find("slot").value()),
 					in.find("name").value(),
 					in.find("renderer").value());
 			}
 		});
 	}
 
 	public static enum Slot {
 		WEAPON, ARMOUR, SHIELD, GAUNTLET, BOOTS, HELMET, ACCESSORY;
 	}
 
 	private int attack, strength, defence, delay;
 	private String name, renderer;
 	private Slot slottype;
 
 	public EquipmentGameThing(GameWorld w, int a, int s, int d, int e, Slot sl, String nom, String ren){
 		super(w);
 		attack = a;
 		strength = s;
 		defence = d;
 		delay = e;
 		name = nom;
 		slottype = sl;
 		this.renderer = ren;
 		
 	}
 
 	public List<String> interactions(){
 		if(location() instanceof Container){
 			List<String> out = new LinkedList<String>(super.interactions());
 			out.add("equip");
 			return out;
 		}
 		return super.interactions();
 	}
 	
 	public void interact(String name, Player who){
 		if(name.equals("equip") && who.carrying(this))
 			who.equip(this);
 		else
 			super.interact(name, who);
 	}
 	
 	public Slot slot(){
 		return this.slottype;
 	}
 
 	@Override
 	public String renderer() {
 		return renderer;
 	}
 
 	@Override
 	public String name() {
 		return name;
 	}
 }
