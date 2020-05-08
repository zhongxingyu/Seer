 /**
  * The player character
  * The plan is to refactor 'player' into an 'alive' abstract class and have 'player' and 'monster' extend it
  * 
  */
 
 package courseProject.model;
 
 
 /** 
  * @author Micheal Hamon
  * @author Andrew Venus
  * @author Denis Dionne
  * @version 01/11/2012
  */
 public class Player extends Creature {
     private int limit;
     protected Room currRoom;
     /**
      * Constructor for objects of class Player
      * @param room The room the player starts in
      * @param healthMax the maximum health of the creature
      * @param attack the attack stat of the creature
      * @param defence the defence stat of the creature
      */
     public Player(Room room, int healthMax, int attack, int defence){
     	super("player",healthMax,attack,defence);
         currRoom = room;
         limit = 20;		//max weight
     }
     
     /**
      * Copy Constructor for objects of class Player
      * @param the player to copy
      */
     public Player(Player p){
     	super("player", p.healthMax, p.attack, p.defence);
     	currRoom = new Room(p.currRoom);
     	limit = p.limit;
     	health = p.health;
     	if(p.armor != null){
     		armor = new Item(p.armor);
     	}
     	if(p.weapon != null){
     		weapon = new Item(p.weapon);
     	}
     	inv = new Inventory(p.inv);
     }
     /**
      * moves the player to a new room
      * @param direction
      * @return string description of what happened
      */
     public String setRoom(String direction){
         Room nextRoom = currRoom.getExit(direction);
         if (nextRoom == null) {
             return "There is no door!";
         }
         else {
         	currRoom.revMonster();
             currRoom = nextRoom;
             return getLoc();
         }
     }
     /**
      * returns the current room the player is in.
      * @return the current room the player is in
      */
     public Room getRoom(){
         return currRoom;
     }
     /**
      * returns a description of the room and everything that is in the room which the player is in
      * @return room description, item and monster names
      */
     public String getLoc(){
         StringBuffer loc = new StringBuffer();
         String look = look();
         loc.append(currRoom.getLoc());
         if(!look.equals("")){
         	loc.append("\n");
             loc.append(look);
         }
         return loc.toString();
     }
     /**
      * Gets details about the current room
      * (exits and items)
      * @return String description of what is around the player
      */
     public String look(){
     	StringBuffer names = new StringBuffer();
     	String items = currRoom.getItemNames();
     	String monsters = currRoom.getMonsterNames();
     	if(!items.equals("")){
     		names.append("Items: ");
     		names.append(items);
     	}
     	if(!monsters.equals("")){
     		if(!(names.length()==0)){
     			names.append("\n");
     		}
     		names.append("Monsters: ");
     		names.append(monsters);
     	}
         return names.toString();
     }
     /**
      * Gets details on item
      * @param name of the item to look at
      * @return string to print
      */
     public String lookat(String name){
         String desc = currRoom.getItemFull(name);
             if(desc.equals("")){
                 return "There is no such item in this room";
             }else{
                 return desc;
             }
     }
     /**
      * drops an item into the current room
      * @param name to drop from inventory
      * @return whether the item was dropped
      */
     public boolean drop(String name){
     	Item i=inv.getItem(name);
     	if (i==null)
     	{
     		return false;
     	}
     	else
     	{
     		currRoom.drop(i);
     		inv.remove(i);
     		return true;
     	}
     }
     /**
      * picks up item from current room
      * @param name of item to pick up
      * @return string to print
      */
     public String pickup(String name){
     	int diff = (inv.getMass()+currRoom.getItemWeight(name))-limit;
         if(diff>0){
             return "Item is to heavy to pick up by "+diff+" lbs";
         }
     	
     	
     	Item item=currRoom.pickup(name);
         
         if(item!=null)
         {
         	inv.add(item);
             return "You have picked up "+name;
         }
         else
         {
         	return "Item does not exist";
         }
     }
     /**
      * do damage to target
      * @param target to attack
      * @return string to print
      */
     public String attack (String target){
     	Monster m = currRoom.getMonster(target);
     	if (m==null){
     		return "There is no such creature here";
     	}
     	String s = super.attack(m);
     	if(m.isDead()){
     		m.drop(currRoom);
     	}
     	return s;
     }
     /**
      * displays character's inventory and carry weight
      * @return string of the contents of your inventory
      */
     public String showInv(){
     	String s = "";
     	if(inv.isEmpty()){
     		return "weight: " + inv.getMass() +"/"+limit+ " lbs";
     	}
     	
     	s = inv.getItemNames();
 
     	s = s+ "\nweight: " + inv.getMass() +"/"+limit+ " lbs";
     	return s;
     }
     /**
      * use an item
      * weapons and armor get equipped
      * health items get used up
      * @param name
      * @return string to print
      */
     public String use(String name){
     	Item i=inv.getItem(name);
     	if(i!=null)
     	{
     	
     		String s = "";
     			if (i.getType().equals(ItemType.weapon)){
     				if(weapon!=null){
     					inv.add(weapon);
     				}
     				weapon = i;
     				inv.remove(i);
     				s = s + i.getName()+" was equiped";
     			}else if(i.getType().equals(ItemType.armor)){
     				if(armor!=null){
     					inv.add(armor);
     				}
     				armor = i;
     				inv.remove(i);
     				s = s + i.getName()+" was equiped";
     			}else if(i.getType().equals(ItemType.health)){
     				s = s + i.getName() + " was used";
     				s = s +"\n"+heal(i.getValue());
     				inv.remove(i);
     				
     			}
     			return s;
     	}
     	else
     	{
     		return "You don't have that";
     	}
     	
     }
     
     
     
     /**
      * Gets the characters current health and equipment
      * @return string with the stats of the player
      */
     public String character(){
     	StringBuffer buff = new StringBuffer();
     	buff.append("Health: ");
     	buff.append(health);
     	buff.append("/");
     	buff.append(healthMax);
     	buff.append("\nWeapon: ");
     	if(weapon==null){
     		buff.append("none");
     	}else{
     		buff.append(weapon.getName());
     	}
     	buff.append("\nArmor: ");
     	if(armor==null){
     		buff.append("none");
     	}else{
     		buff.append(armor.getName());
     	}
     	return buff.toString();
     }
     public boolean equals (Object o){
     	if(!(o instanceof Player)){
     		return false;
     	}
     	Player p2 = (Player)o;
    	if(this.character().equals(p2.character())&&this.showInv().equals(p2.showInv())){//&&this.getRoom().equals(p2.getRoom())
     		return true;
     	}
     	return false;
     }
 }
