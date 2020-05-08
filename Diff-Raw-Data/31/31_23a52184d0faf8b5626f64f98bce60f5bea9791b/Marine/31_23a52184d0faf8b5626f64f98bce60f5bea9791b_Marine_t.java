 package units;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Marine extends Unit{
 	private final int Healthmax, Healthregen;
 	private int Healthbar,  Healthmaxmod, Healthregenmod;
 	private final int Manamax, Manaregen;
 	private int Manabar, Manamaxmod, Manaregenmod;
 	private final int Armour;
 	private int Armourmod;
 	private final int Movementspeed;
 	private int Movementspeedmod;
 	private List<String> Modifiers = new ArrayList<String>();
 	//private List<Effect> Afflictions = new ArrayList<Effect>();
 	//private List<Ability> Abilities = new ArrayList<Ability>();
 	//private List<Subscription> Subscriptions = new ArrayList<Subscription>();
 	
 	public Marine(){
		super("Marine", 5);
 		Healthmax = 45;
 		Healthregen = 1;
 		Manamax = 20;
 		Manaregen = 1;
 		Armour = 1;
 		Movementspeed = 2;
 		Modifiers.add("terran");
 		Modifiers.add("biological");
 		Modifiers.add("light");
 		
 		
 	}
 	public void affectbar(String bar,int incurredchange){
 		switch(bar){
 		case "healthdown":
 			Healthbar -= incurredchange;
 			if(Healthbar <= 0)
 				OnDeath();
 		case "healthup":
 			Healthbar += incurredchange;
 		case "manadown":
 			Manabar -= incurredchange;
 		case "manaup":
 			Manabar += incurredchange;
 		}
 	}
 	public void update(){
 		int healthregen = gethealthregen();
 		if(Healthbar < (Healthmax + Healthmaxmod) & (Healthbar + healthregen) < (Healthmax + Healthmaxmod))
 			Healthbar += healthregen;
 		else if(Healthbar < (Healthmax + Healthmaxmod))
 			Healthbar = Healthmax + Healthmaxmod;
 		
 		int manaregen = gethealthregen();
 		if(Manabar < (Manamax + Manamaxmod) & (Manabar + manaregen) < (Manamax + Manamaxmod))
 			Manabar += manaregen;
 		else if(Manabar < (Manamax + Manamaxmod))
 			Manabar = Manamax + Manamaxmod;
 	}
 	public boolean isupdaterequired(){
 		if(Healthbar < (Healthmax + Healthmaxmod) 
 				| Manabar < (Manamax + Manamaxmod)){
 			if(updateOrder == 0){
 //				UpdateOrder updateorder = new UpdateOrder(this,1);
 // Queue update				updateOrder = toDoQueue.FindOrderList("Update").add(updateorder);
 			}
 		return true;
 		}else if(updateOrder != 0){
 // Cancel update			toDoQueue.FindOrderList("Update").remove(updateOrder);
 			updateOrder = 0;
 			return false;
 		}else
 			return false;
 	}
 	public int gethealthregen(){
 		int x = Healthregen + Healthregenmod;
 		if(x < 0)
 			return x;
 		else
 			return 0;
 	}
 	public int getmanaregen(){
 		int x = Manaregen + Manaregenmod;
 		if(x < 0)
 			return x;
 		else
 			return 0;
 	}
 	public int getmovespeed(){
 		return Movementspeed + Movementspeedmod;
 	}
 	public int getbasemovespeed(){
 		return Movementspeed;
 	}
 	public void addmovespeed(int modifier){
 		Movementspeedmod += modifier;
 	}
 	public void resetmovespeed(){
 		Movementspeedmod = 0;
 	}
 	public int getarmour(){
 		return Armour + Armourmod;
 	}
 	public int getbasearmour(){
 		return Armour;
 	}
 	public void addamour(int modifier){
 		Armourmod += modifier;
 	}
 	public void resetarmour(){
 		Armourmod = 0;
 	}
 	public List<String> getModifiers() {
 		return Modifiers;
 	}
 	public void addModifiers(String modifiers) {
 		Modifiers.add(modifiers);
 	}
 	public void removeModifiers(String modifiers) {
 		Modifiers.remove(modifiers);
 	}
 	public void OnSpawn(){
 		Healthbar = Healthmax;
 		Manabar = Manamax;
 	}
 	public void OnDeath(){
 		
 	}
 	public void OnLvlup(){
 		
 	}
 }
