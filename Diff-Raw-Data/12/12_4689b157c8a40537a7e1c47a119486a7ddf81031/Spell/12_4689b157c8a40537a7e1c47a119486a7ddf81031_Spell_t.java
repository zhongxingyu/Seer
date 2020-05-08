 package warborn.model.spells;
 
 import java.awt.Image;
 
 import javax.swing.ImageIcon;
 
 public abstract class Spell{
 	
 	private int manaCost;
 	private int timer = 0;
 	
 	public abstract boolean validTarget(SpellTargetable target);
 	public abstract String getName();
 	public abstract String getDescription();
 	public abstract void invoke (SpellTargetable target);
 	
 	public Spell(int mana){
 		manaCost = mana;
 	}
 	
 	public Image getImage(){
 		return new ImageIcon("WarbornData/images/spells/" + getName().replaceAll(" ", "") + ".png").getImage();
 	}
 	
 	public void tick(SpellTargetable model){
 		;//Does nothing by default. Is overrided if necessary.
 	}
 	
 	public int getManaCost(){
 		return manaCost;
 	}
 	
 	public int getTimer(){
 		return timer;
 	}
 	
 	protected void setTimer(int newTime){
 		timer = newTime;
 	}
 	
 	protected void decrementTimer(){
 		timer--;
 	}
 	
 	public boolean isInstant(){
		//is false by default and overrided if not.
 		return false;
 	}
 	
 	public String toString() {
 		return getName();
 	}
 	
 }
